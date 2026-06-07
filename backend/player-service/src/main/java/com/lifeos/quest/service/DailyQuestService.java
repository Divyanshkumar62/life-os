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
    private final com.lifeos.ai.service.AIQuestService aiQuestService;

    @Autowired
    public DailyQuestService(PlayerIdentityRepository identityRepository, PlayerProfileRepository profileRepository,
                           QuestLifecycleService questService, PlayerStateService playerStateService,
                           IntelQuestGenerator intelGenerator, RedGateService redGateService,
                           com.lifeos.ai.service.AIQuestService aiQuestService) {
        this.identityRepository = identityRepository;
        this.profileRepository = profileRepository;
        this.questService = questService;
        this.playerStateService = playerStateService;
        this.intelGenerator = intelGenerator;
        this.redGateService = redGateService;
        this.aiQuestService = aiQuestService;
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
            .filter(q -> !q.getTitle().equals("[HIDDEN] The Architect's Original Trial"))
            .toList();

        java.util.List<com.lifeos.quest.domain.Quest> activeReflection = 
            questService.getQuestRepository().findByPlayerPlayerIdAndQuestTypeAndState(
                playerId, QuestType.REFLECTION, com.lifeos.quest.domain.enums.QuestState.ACTIVE)
            .stream()
            .filter(q -> q.getCategory() == com.lifeos.quest.domain.enums.QuestCategory.SYSTEM_DAILY)
            .filter(q -> !q.getTitle().equals("[HIDDEN] The Architect's Original Trial"))
            .toList();

        int uncompletedDailies = activeDailies.size() + activeReflection.size();
        
        int totalDailies = 3; 
        int completedDailies = totalDailies - uncompletedDailies;

        if (uncompletedDailies > 0) {
            log.warn("Player {} failed the 100% Daily Rule (Missed {} quests)", playerId, uncompletedDailies);
            
            playerStateService.accumulateDailyRestDebt(playerId, 20.0 * uncompletedDailies);
            
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

        // Clean up (expire) any active/assigned Architect's Trial from the previous reset period
        java.util.List<com.lifeos.quest.domain.Quest> activeArchitectTrial = 
            questService.getQuestRepository().findByPlayerPlayerIdAndQuestTypeAndState(
                playerId, QuestType.PHYSICAL, com.lifeos.quest.domain.enums.QuestState.ACTIVE)
            .stream()
            .filter(q -> q.getTitle().equals("[HIDDEN] The Architect's Original Trial"))
            .toList();
            
        activeArchitectTrial.forEach(q -> questService.expireQuest(q.getQuestId()));
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

        // Clear DAILY_CLEAR_REWARDED status flag
        playerStateService.removeStatusFlag(identity.getPlayerId(), com.lifeos.player.domain.enums.StatusFlagType.DAILY_CLEAR_REWARDED);

        // Call GeminiQuestService to generate the 3 daily quests!
        java.util.List<com.lifeos.quest.dto.QuestRequest> aiQuests = aiQuestService.generateQuests(identity.getPlayerId(), 3);
        
        for (com.lifeos.quest.dto.QuestRequest qr : aiQuests) {
            qr.setCategory(com.lifeos.quest.domain.enums.QuestCategory.SYSTEM_DAILY);
            qr.setDeadlineAt(resetTimestamp.plusHours(24));
            questService.assignQuest(qr);
        }

        // Append the 4th optional quest
        questService.assignQuest(QuestRequest.builder()
                .playerId(identity.getPlayerId())
                .title("[HIDDEN] The Architect's Original Trial")
                .description("Objective: 100 Push-ups, 100 Sit-ups, 100 Squats, 10km Run")
                .questType(QuestType.PHYSICAL)
                .category(com.lifeos.quest.domain.enums.QuestCategory.SYSTEM_DAILY)
                .difficultyTier(DifficultyTier.S)
                .priority(Priority.NORMAL)
                .deadlineAt(resetTimestamp.plusHours(24))
                .successXp(0)
                .goldReward(0L)
                .primaryAttribute(AttributeType.STR)
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

