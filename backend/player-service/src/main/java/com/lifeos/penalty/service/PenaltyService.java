package com.lifeos.penalty.service;

import com.lifeos.penalty.domain.PenaltyDefinition;
import com.lifeos.penalty.domain.PenaltyRecord;
import com.lifeos.penalty.domain.enums.FailureReason;
import com.lifeos.penalty.repository.PenaltyRecordRepository;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PenaltyService {

    private final PenaltyRecordRepository penaltyRepository;
    private final PenaltyCalculationService calculationService;
    private final PlayerStateService playerStateService;
    private final QuestRepository questRepository;

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
        
        // Apply Penalty Zone Flag
        // Use a long duration (e.g. 100 years) or until manually cleared
        playerStateService.applyStatusFlag(
            playerId, 
            com.lifeos.player.domain.enums.StatusFlagType.PENALTY_ZONE, 
            LocalDateTime.now().plusYears(100) 
        );
        
        // Zero out XP gain? (Handled by xpFrozen or status checks in addXp)
        // Reset Streaks? (Design choice: DQE says "No streak forgiveness")
        playerStateService.resetStreak(playerId);
    }
}
