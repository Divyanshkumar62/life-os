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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class DailyQuestService {

    private final QuestRepository questRepository;
    private final PlayerStateService playerStateService;
    private final PlayerIdentityRepository playerRepository;
    private final PenaltyService penaltyService;

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
        boolean penaltyTriggered = false;
        
        for (Quest daily : dailies) {
            // Strict check: If deadline passed and not completed
            if (daily.getDeadlineAt().isBefore(now)) {
                // Warning: Status is ACTIVE but time passed -> FAILED
                daily.setState(QuestState.FAILED);
                
                // Trigger Penalty (System Dailies = Mandatory)
                // Idempotency handled inside PenaltyService, but we only need to call once per batch really
                if (!penaltyTriggered) {
                    penaltyService.enterPenaltyZone(playerId, "DAILY_FAILURE_RESET");
                    penaltyTriggered = true;
                }
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
