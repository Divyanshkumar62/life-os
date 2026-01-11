package com.lifeos.quest.service;

import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestCategory;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.domain.enums.SystemDailyTemplate;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.streak.service.StreakService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
//@Slf4j
public class DailyQuestService {

    private static final Logger log = LoggerFactory.getLogger(DailyQuestService.class);

    private final QuestRepository questRepository;
    private final PlayerStateService playerStateService;
    private final PlayerIdentityRepository playerRepository;
    private final PenaltyService penaltyService;
    private final StreakService streakService;

    /**
     * SYSTEM-WIDE RESET (Scheduled Job would call this)
     */
    public void runDailyResetCycle() {
        log.info("Starting System-Wide Daily Reset Cycle");
        // In a real system, paginate through players. For V1, fetch all ID's.
        List<UUID> allPlayerIds = playerRepository.findAllIds(); 
        
        for (UUID playerId : allPlayerIds) {
            try {
                processPlayerReset(playerId);
            } catch (Exception e) {
                log.error("Failed to reset daily quests for player {}", playerId, e);
            }
        }
        log.info("Daily Reset Cycle Completed");
    }

    @Transactional
    public void processPlayerReset(UUID playerId) {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. Fetch Active System Dailies
        List<Quest> dailies = questRepository.findByPlayerPlayerIdAndState(playerId, QuestState.ACTIVE).stream()
                .filter(q -> q.getCategory() == QuestCategory.SYSTEM_DAILY)
                .collect(Collectors.toList());
        
        // 2. Process Expiry & Penalties
        // 2. Process Expiry & Penalties
        boolean todayFailed = false;
        
        for (Quest daily : dailies) {
            // Strict check: If deadline passed and not completed
            if (daily.getDeadlineAt().isBefore(now)) {
                daily.setState(QuestState.FAILED);
                todayFailed = true;
            }
        }
        
        // 3. Update Streak (Evaluate YESTERDAY's performance, which is `dailies`)
        // Actually, `dailies` usually refers to TODAY's dailies from perspective of reset time?
        // Wait, "System-Wide Reset" usually runs at Midnight:00:01
        // So `dailies` fetched are the ones that just expired (deadlineAt was yesterday midnight/today midnight).
        // `daily.getDeadlineAt().isBefore(now)` implies they are expired.
        // So `todayFailed` flag captures if any of them failed.
        // If !todayFailed => All Success? We need to check if they are COMPLETED.
        // Wait, loop only checks expiry. We must check status too.
        
        boolean allCompleted = true;
        for (Quest daily : dailies) {
             if (daily.getState() != QuestState.COMPLETED) {
                 allCompleted = false;
                 // If not completed and expired, it fails.
                 if (daily.getDeadlineAt().isBefore(now)) {
                     daily.setState(QuestState.FAILED);
                 }
                 // If not completed and deadline passed -> Failed.
             }
        }
        
        // Refinement: `todayFailed` logic above was incomplete.
        // Let's re-eval: `todayFailed` meant "Did ANY fail?".
        // Streak requires ALL to be COMPLETED.
        // If any is FAILED or ACTIVE(expired), Streak breaks.
        
        boolean streakSuccess = true;
        for (Quest daily : dailies) {
             if (daily.getState() != QuestState.COMPLETED) {
                 streakSuccess = false;
             }
        }
        
        // Call Streak Service
        LocalDate yesterday = LocalDate.now().minusDays(1);
        try {
            streakService.processDailyCompletion(playerId, yesterday, streakSuccess);
        } catch (Exception e) {
            log.error("Failed to update streak for player {}", playerId, e);
        }
        
        // HYBRID TRIGGER LOGIC
        // Fetch current consecutive failures
        // We need to re-fetch state? Or rely on what we have? 
        // We didn't fetch state in this method yet.
        var state = playerStateService.getPlayerState(playerId);
        int currentFailures = state.getTemporalState().getConsecutiveDailyFailures();
        
        if (todayFailed) {
            int newFailures = currentFailures + 1;
            playerStateService.updateConsecutiveFailures(playerId, newFailures);
            
            if (newFailures == 1) {
                // STRIKE 1: WARNING
                // Apply Warning for 24h
                playerStateService.applyStatusFlag(playerId, com.lifeos.player.domain.enums.StatusFlagType.WARNING, now.plusDays(1));
                log.info("Player {} missed dailies. Consecutive: 1. Applied WARNING.", playerId);
            } else if (newFailures >= 2) {
                // STRIKE 2+: PENALTY ZONE
                penaltyService.enterPenaltyZone(playerId, "Consecutive Daily Failures: " + newFailures);
            }
        } else {
            // SUCCESS (No failures found among active dailies, meaning likely completed)
            // Reset counter if it was > 0
            if (currentFailures > 0) {
                playerStateService.updateConsecutiveFailures(playerId, 0);
                playerStateService.removeStatusFlag(playerId, com.lifeos.player.domain.enums.StatusFlagType.WARNING);
                log.info("Player {} completed dailies. Reset consecutive failures.", playerId);
            }
        }
        
        // Note: PROJECT_SUBTASKS are ignored here as requested. They naturally expire or stay active.
        
        questRepository.saveAll(dailies);
        
        // 3. Generate New Dailies for Next Cycle
        generateDailyQuests(playerId);
    }

    @Transactional
    public void generateDailyQuests(UUID playerId) {
        var state = playerStateService.getPlayerState(playerId);
        PlayerRank rank = state.getProgression().getRank();
        int count = rank.getSystemDailyCount();
        
        LocalDateTime tomorrowMidnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);
        
        // Select templates based on count (Simple sublist logic for V1)
        // E-Rank (2): Wake Up, Reflection
        // Higher Ranks add Movement, Focus, etc.
        
        SystemDailyTemplate[] allTemplates = SystemDailyTemplate.values();
        
        for (int i = 0; i < Math.min(count, allTemplates.length); i++) {
            SystemDailyTemplate tmpl = allTemplates[i];
            
            Quest quest = Quest.builder()
                    .player(state.getIdentity() != null ? com.lifeos.player.domain.PlayerIdentity.builder().playerId(playerId).build() : null) // Using simplified ref
                    .title(tmpl.getDefaultTitle())
                    .description("System generated daily task: " + tmpl.getDefaultTitle())
                    .category(QuestCategory.SYSTEM_DAILY)
                    .difficultyTier(tmpl.getDifficulty())
                    .state(QuestState.ACTIVE)
                    .priority(com.lifeos.quest.domain.enums.Priority.HIGH)
                    .questType(com.lifeos.quest.domain.enums.QuestType.DISCIPLINE) // Defaulting to Discipline
                    .assignedAt(LocalDateTime.now())
                    .startsAt(LocalDateTime.now())
                    .deadlineAt(tomorrowMidnight)
                    .systemMutable(true)
                    .build();
            
            questRepository.save(quest);
        }
        
        log.info("Generated {} daily quests for player {}", Math.min(count, allTemplates.length), playerId);
    }
}
