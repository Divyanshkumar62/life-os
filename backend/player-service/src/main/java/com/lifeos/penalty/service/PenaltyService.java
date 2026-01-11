package com.lifeos.penalty.service;

import com.lifeos.penalty.domain.PenaltyDefinition;
import com.lifeos.penalty.domain.PenaltyRecord;
import com.lifeos.penalty.domain.enums.FailureReason;
import com.lifeos.penalty.repository.PenaltyRecordRepository;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.streak.service.StreakService;
import com.lifeos.voice.domain.enums.SystemMessageType;
import com.lifeos.voice.event.VoiceSystemEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PenaltyService {

    private static final Logger log = LoggerFactory.getLogger(PenaltyService.class);

    private final PenaltyRecordRepository penaltyRepository;
    private final PenaltyCalculationService calculationService;
    private final PlayerStateService playerStateService;
    private final QuestRepository questRepository;
    private final StreakService streakService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Transactional
    public void applyPenalty(UUID questId, UUID playerId, FailureReason reason) {
        // 1. Idempotency Guard
        if (penaltyRepository.existsByQuestId(questId)) {
            log.info("Penalty already applied for quest {}", questId);
            return;
        }

        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found"));

        // 2. Calculate
        PenaltyDefinition def = calculationService.calculatePenalty(quest, reason);

        // 3. Apply Effects
        // XP Deduction (Floor logic is inside PlayerStateService)
        if (def.getXpDeduction() > 0) {
            playerStateService.applyXpDeduction(playerId, def.getXpDeduction());
        }

        // Debuff
        if (def.getDebuffAttribute() != null) {
            playerStateService.applyStatDebuff(
                    playerId, 
                    def.getDebuffAttribute(), 
                    def.getDebuffAmount(), 
                    def.getDebuffExpiresAt()
            );
        }

        // Streak Reset
        if (def.isResetStreak()) {
            playerStateService.resetStreak(playerId);
        }

        // 4. Persist Record
        Map<String, Object> payload = new HashMap<>();
        payload.put("xpDeduction", def.getXpDeduction());
        if (def.getDebuffAttribute() != null) {
            payload.put("debuffAttr", def.getDebuffAttribute().name());
            payload.put("debuffAmount", def.getDebuffAmount());
        }
        
        PenaltyRecord record = PenaltyRecord.builder()
                .playerId(playerId)
                .questId(questId)
                .type(def.getType())
                .severity(def.getSeverity())
                .valuePayload(payload)
                .appliedAt(LocalDateTime.now())
                .expiresAt(def.getDebuffExpiresAt())
                .build();

        penaltyRepository.save(record);
        log.info("Applied penalty {} to player {} for quest {}", def.getSeverity(), playerId, questId);
    }

    @Transactional
    public void enterPenaltyZone(UUID playerId, String reason) {
        // Idempotency: Check if player is already in Penalty Zone
        var state = playerStateService.getPlayerState(playerId);
        boolean alreadyInPenalty = state.getActiveFlags().stream()
                .anyMatch(f -> f.getFlag() == com.lifeos.player.domain.enums.StatusFlagType.PENALTY_ZONE);
                
        if (alreadyInPenalty) {
            log.warn("Player {} already in Penalty Zone. Trigger ignored.", playerId);
            return;
        }

        log.info("ENTERING PENALTY ZONE: Player {} Reason: {}", playerId, reason);
        
        // 1. Apply Penalty Zone Flag
        playerStateService.applyStatusFlag(
            playerId, 
            com.lifeos.player.domain.enums.StatusFlagType.PENALTY_ZONE, 
            LocalDateTime.now().plusYears(100) 
        );
        
        // 2. Clear Warning Flag (if any, redundant but clean)
        playerStateService.removeStatusFlag(playerId, com.lifeos.player.domain.enums.StatusFlagType.WARNING);
        
        // 3. Reset Streaks
        playerStateService.resetStreak(playerId);
        streakService.resetStreak(playerId); // New Streak Engine Reset
        
        // 4. Generate SURVIVAL PROTOCOL Quest
        Quest survivalQuest = Quest.builder()
                .player(com.lifeos.player.domain.PlayerIdentity.builder().playerId(playerId).build())
                .title("SURVIVAL PROTOCOL")
                .description("You have violated the system. Complete 100 Pushups + 100 Situps to restore access.\n" +
                             "Project Creation and Rank Promotions are LOCKED until this is done.")
                .category(com.lifeos.quest.domain.enums.QuestCategory.SYSTEM_DAILY) // Or specific PENALTY category? Using SYSTEM_DAILY to ensure visibility/priority, or maybe new Category?
                // Plan said QuestType PENALTY. Category can remain SYSTEM_DAILY or MAIN. Let's use SYSTEM_DAILY for priority.
                .difficultyTier(com.lifeos.quest.domain.enums.DifficultyTier.RED)
                .state(com.lifeos.quest.domain.enums.QuestState.ACTIVE)
                .priority(com.lifeos.quest.domain.enums.Priority.CRITICAL)
                .questType(com.lifeos.quest.domain.enums.QuestType.PENALTY)
                .assignedAt(LocalDateTime.now())
                .startsAt(LocalDateTime.now())
                .deadlineAt(LocalDateTime.now().plusYears(100)) // Must not expire
                .systemMutable(false) // User cannot delete
                .build();
                
        questRepository.save(survivalQuest);
        log.info("Generated SURVIVAL PROTOCOL quest for player {}", playerId);
        
        // VOICE: PENALTY_ZONE_ENTRY
        eventPublisher.publishEvent(VoiceSystemEvent.builder()
                .playerId(playerId)
                .type(SystemMessageType.PENALTY_ZONE_ENTRY)
                .build());
    }

    @Transactional
    public void exitPenaltyZone(UUID playerId) {
        log.info("EXITING PENALTY ZONE: Player {}", playerId);
        
        // 1. Remove Penalty Zone Flag
        playerStateService.removeStatusFlag(playerId, com.lifeos.player.domain.enums.StatusFlagType.PENALTY_ZONE);
        
        // 2. Reset failures counter through Temporal State?
        // Logic might reside in PlayerStateService, but here we just ensure the flag is gone.
        // Hybrid Trigger logic in DailyQuestService resets counter on successful day. 
        // But if they just finished the Penalty Quest, does that count as a "successful day"?
        // Probably yes, but let's explicity reset the counter here to be safe/merciful.
        playerStateService.updateConsecutiveFailures(playerId, 0);
    }
}
