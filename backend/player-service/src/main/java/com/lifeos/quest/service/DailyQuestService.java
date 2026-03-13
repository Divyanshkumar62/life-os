package com.lifeos.quest.service;

import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.onboarding.repository.PlayerProfileRepository;
import com.lifeos.onboarding.domain.PlayerProfile;
import com.lifeos.onboarding.service.IntelQuestGenerator;
import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.domain.enums.Priority;
import com.lifeos.quest.domain.enums.QuestType;
import com.lifeos.quest.dto.QuestRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
public class DailyQuestService {

    private static final Logger log = LoggerFactory.getLogger(DailyQuestService.class);

    private final PlayerIdentityRepository identityRepository;
    private final PlayerProfileRepository profileRepository;
    private final QuestLifecycleService questService;
    private final PlayerStateService playerStateService;
    private final IntelQuestGenerator intelGenerator;
    private final RedGateService redGateService;

    @Autowired
    public DailyQuestService(PlayerIdentityRepository identityRepository, PlayerProfileRepository profileRepository,
                           QuestLifecycleService questService, PlayerStateService playerStateService,
                           IntelQuestGenerator intelGenerator, RedGateService redGateService) {
        this.identityRepository = identityRepository;
        this.profileRepository = profileRepository;
        this.questService = questService;
        this.playerStateService = playerStateService;
        this.intelGenerator = intelGenerator;
        this.redGateService = redGateService;
    }

    @Transactional
    public void processPlayerReset(UUID playerId) {
        performDailyResetCheck(playerId);
    }

    @Transactional
    public void performDailyResetCheck(UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null || !identity.isOnboardingCompleted()) {
            return;
        }

        PlayerProfile profile = profileRepository.findById(playerId).orElse(null);
        if (profile == null || profile.getWakeUpTime() == null) {
            return;
        }

        java.time.ZoneOffset zoneOffset = java.time.ZoneOffset.UTC;
        try {
            if (profile.getTimezoneOffset() != null && !profile.getTimezoneOffset().isEmpty()) {
                zoneOffset = java.time.ZoneOffset.of(profile.getTimezoneOffset());
            }
        } catch (Exception e) {
            log.warn("Invalid timezone offset '{}' for player {}, defaulting to UTC", profile.getTimezoneOffset(), playerId);
        }

        java.time.ZonedDateTime playerLocalNow = java.time.Instant.now().atZone(zoneOffset);
        
        LocalTime wakeUp = profile.getWakeUpTime();
        LocalTime resetTime = wakeUp.minusHours(2);
        
        java.time.ZonedDateTime todayResetLocal = playerLocalNow.with(resetTime);
        java.time.ZonedDateTime yesterdayResetLocal = todayResetLocal.minusDays(1);
        java.time.ZonedDateTime effectiveResetThresholdLocal = playerLocalNow.isAfter(todayResetLocal) ? todayResetLocal : yesterdayResetLocal;

        LocalDateTime effectiveResetThreshold = effectiveResetThresholdLocal.withZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime();

        LocalDateTime lastReset = identity.getLastDailyReset();

        if (lastReset == null || lastReset.isBefore(effectiveResetThreshold)) {
            log.info("Daily Reset Triggered for player: {}. Threshold: {}", playerId, effectiveResetThreshold);
            evaluatePreviousDay(identity);
            triggerDailyReset(identity, effectiveResetThreshold);
        }
    }

    private void evaluatePreviousDay(PlayerIdentity identity) {
        UUID playerId = identity.getPlayerId();
        
        java.util.List<com.lifeos.quest.domain.Quest> activeDailies = 
            questService.getQuestRepository().findByPlayerPlayerIdAndQuestTypeAndState(
                playerId, QuestType.DISCIPLINE, com.lifeos.quest.domain.enums.QuestState.ACTIVE)
            .stream()
            .filter(q -> q.getCategory() == com.lifeos.quest.domain.enums.QuestCategory.SYSTEM_DAILY)
            .toList();

        java.util.List<com.lifeos.quest.domain.Quest> activeReflection = 
            questService.getQuestRepository().findByPlayerPlayerIdAndQuestTypeAndState(
                playerId, QuestType.REFLECTION, com.lifeos.quest.domain.enums.QuestState.ACTIVE)
            .stream()
            .filter(q -> q.getCategory() == com.lifeos.quest.domain.enums.QuestCategory.SYSTEM_DAILY)
            .toList();

        int uncompletedDailies = activeDailies.size() + activeReflection.size();
        
        int totalDailies = 3; 
        int completedDailies = totalDailies - uncompletedDailies;

        if (uncompletedDailies > 0) {
            log.warn("Player {} failed the 100% Daily Rule (Missed {} quests)", playerId, uncompletedDailies);
            
            boolean hasShield = playerStateService.hasActiveFlag(playerId, com.lifeos.player.domain.enums.StatusFlagType.PENALTY_SHIELD);
            
            if (hasShield && completedDailies == (totalDailies - 1)) {
                log.info("Ruler's Authority Activated! Player {} shielded from minor penalty. Consuming Shield.", playerId);
                playerStateService.removeStatusFlag(playerId, com.lifeos.player.domain.enums.StatusFlagType.PENALTY_SHIELD);
                
                activeDailies.forEach(q -> questService.expireQuest(q.getQuestId()));
                activeReflection.forEach(q -> questService.expireQuest(q.getQuestId()));
                
                return;
            } else if (hasShield) {
                log.warn("Player {} has Shield, but completed {}/{} quests. Shield cannot bypass total failure.", playerId, completedDailies, totalDailies);
            }
            
            activeDailies.forEach(q -> questService.failQuest(q.getQuestId()));
            activeReflection.forEach(q -> questService.failQuest(q.getQuestId()));

            boolean hasActiveExam = questService.getQuestRepository().findByPlayerPlayerIdAndQuestTypeAndState(
                playerId, QuestType.PROMOTION_EXAM, com.lifeos.quest.domain.enums.QuestState.ACTIVE).isPresent();

            if (hasActiveExam) {
                log.info("Igris Exception Triggered: Suspending active Promotion Exam for player {}", playerId);
                var examQuests = questService.getQuestRepository().findByPlayerPlayerIdAndQuestTypeAndState(
                    playerId, QuestType.PROMOTION_EXAM, com.lifeos.quest.domain.enums.QuestState.ACTIVE);
                
                examQuests.ifPresent(q -> {
                    q.setState(com.lifeos.quest.domain.enums.QuestState.SUSPENDED);
                    questService.getQuestRepository().save(q);
                });
            }
        }
    }

    private void triggerDailyReset(PlayerIdentity identity, LocalDateTime resetTimestamp) {
        if (identity.isRedGateActive()) {
            log.info("Skipping daily quest generation - player {} is in Red Gate", identity.getPlayerId());
            return;
        }

        redGateService.checkAndTriggerRandomRedGate(identity.getPlayerId());

        if (identity.isRedGateActive()) {
            log.info("Red Gate activated for player {}, skipping daily quests", identity.getPlayerId());
            return;
        }

        PlayerProfile profile = profileRepository.findByPlayerId(identity.getPlayerId()).orElse(null);
        String archetype = (profile != null && profile.getArchetype() != null) ? profile.getArchetype().toUpperCase() : "BALANCE";
        String goal = (profile != null && profile.getSixMonthGoal() != null) ? profile.getSixMonthGoal().toLowerCase() : "";
        String weakness = (profile != null && profile.getBiggestChallenge() != null) ? profile.getBiggestChallenge().toLowerCase() : "";

        String physTitle = "Daily: Physical Maintenance";
        String physDesc = "Complete your daily movement routine.";
        int physXp = 50;

        if (archetype.equals("BRAWN")) {
             physTitle = "Daily: Strength Training";
             physDesc = "Complete a resistance training session. Focus on hypertrophy.";
             physXp = 75;
        } else if (goal.contains("weight") || goal.contains("fat") || goal.contains("slim")) {
             physTitle = "Daily: Cardio Burn";
             physDesc = "Complete 30 minutes of Zone 2 cardio.";
        } else if (goal.contains("muscle") || goal.contains("bulk")) {
             physTitle = "Daily: Hypertrophy Work";
             physDesc = "Complete 4 sets of compound movements.";
             physXp = 60;
        }

        questService.assignQuest(QuestRequest.builder()
                .playerId(identity.getPlayerId())
                .title(physTitle)
                .description(physDesc)
                .questType(QuestType.DISCIPLINE)
                .category(com.lifeos.quest.domain.enums.QuestCategory.SYSTEM_DAILY)
                .difficultyTier(DifficultyTier.D)
                .priority(Priority.HIGH)
                .deadlineAt(resetTimestamp.plusHours(24))
                .successXp(physXp)
                .goldReward(20)
                .primaryAttribute(AttributeType.STRENGTH)
                .build());

        String focusTitle = "Daily: Deep Focus Block";
        String focusDesc = "Complete one 90-minute deep work session.";
        AttributeType focusAttr = AttributeType.INTELLIGENCE;

        if (goal.contains("code") || goal.contains("program") || goal.contains("dev")) {
             focusTitle = "Daily: Coding Kata";
             focusDesc = "Complete 1 hour of focused coding or algorithm practice.";
        } else if (goal.contains("write") || goal.contains("book")) {
             focusTitle = "Daily: Writing Sprint";
             focusDesc = "Write 500 words or spend 45 minutes separate from distractions.";
        } else if (goal.contains("business") || goal.contains("startup")) {
             focusTitle = "Daily: Strategic Planning";
             focusDesc = "Review KPIs and execute one key strategic task.";
             focusAttr = AttributeType.SENSE;
        }
        
        if (weakness.contains("procrastination") || weakness.contains("start")) {
             focusTitle = focusTitle + " (Eat The Frog)";
             focusDesc = "Do the hardest task FIRST. " + focusDesc;
        }

        questService.assignQuest(QuestRequest.builder()
                .playerId(identity.getPlayerId())
                .title(focusTitle)
                .description(focusDesc)
                .questType(QuestType.DISCIPLINE)
                .category(com.lifeos.quest.domain.enums.QuestCategory.SYSTEM_DAILY)
                .difficultyTier(DifficultyTier.C)
                .priority(Priority.HIGH)
                .deadlineAt(resetTimestamp.plusHours(24))
                .successXp(100)
                .goldReward(30)
                .primaryAttribute(focusAttr)
                .build());

        String reflectTitle = "Daily: Evening Reflection";
        String reflectDesc = "Review your day's progress and plan for tomorrow.";
        
        if (archetype.equals("BRAINS")) {
             reflectTitle = "Daily: Knowledge Intake";
             reflectDesc = "Read 10 pages of a non-fiction book or research paper.";
        }

        questService.assignQuest(QuestRequest.builder()
                .playerId(identity.getPlayerId())
                .title(reflectTitle)
                .description(reflectDesc)
                .questType(QuestType.REFLECTION)
                .category(com.lifeos.quest.domain.enums.QuestCategory.SYSTEM_DAILY)
                .difficultyTier(DifficultyTier.E)
                .priority(Priority.NORMAL)
                .deadlineAt(resetTimestamp.plusHours(24))
                .successXp(30)
                .goldReward(10)
                .primaryAttribute(AttributeType.SENSE)
                .build());

        long daysActive = java.time.temporal.ChronoUnit.DAYS.between(identity.getCreatedAt(), LocalDateTime.now());
        QuestRequest intelQuest = intelGenerator.generateFollowUpIntel(identity.getPlayerId(), daysActive);
        
        if (intelQuest != null) {
            log.info("Triggering Follow-Up Intel Quest for player {} (Day {})", identity.getPlayerId(), daysActive);
            questService.assignQuest(intelQuest);
        }

        identity.setLastDailyReset(LocalDateTime.now());
        identityRepository.save(identity);
    }
}
