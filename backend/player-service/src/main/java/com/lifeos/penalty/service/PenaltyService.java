package com.lifeos.penalty.service;

import com.lifeos.penalty.domain.PenaltyDefinition;
import com.lifeos.penalty.domain.PenaltyRecord;
import com.lifeos.penalty.domain.enums.FailureReason;
import com.lifeos.penalty.domain.enums.PenaltyTriggerReason;
import com.lifeos.penalty.repository.PenaltyQuestRepository;
import com.lifeos.penalty.repository.PenaltyRecordRepository;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.streak.service.StreakService;
import com.lifeos.event.DomainEventPublisher;
import com.lifeos.event.concrete.PenaltyZoneEnteredEvent;
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
    private final org.springframework.context.ApplicationEventPublisher eventPublisher; // Kept as per instruction's snippet

    private final PenaltyQuestService penaltyQuestService;
    private final PenaltyQuestRepository penaltyQuestRepository; // For exit guard
    private final DomainEventPublisher domainEventPublisher; // Added

    @Transactional
    public void applyPenalty(UUID questId, UUID playerId, FailureReason reason) {
        // ... (existing logic unchanged) ...
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
        if (def.getXpDeduction() > 0) {
            playerStateService.applyXpDeduction(playerId, def.getXpDeduction());
        }

        if (def.getDebuffAttribute() != null) {
            playerStateService.applyStatDebuff(
                    playerId, 
                    def.getDebuffAttribute(), 
                    def.getDebuffAmount(), 
                    def.getDebuffExpiresAt()
            );
        }

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
        streakService.resetStreak(playerId);
        
        // 4. Generate Penalty Quest (Official V1 Engine)
        // Map string reason to Enum (simplistic logic for now, default to MISSED_DAYS)
        PenaltyTriggerReason triggerReason = 
            "EXAM_FAIL".equals(reason) ? 
            PenaltyTriggerReason.EXAM_FAIL : 
            PenaltyTriggerReason.MISSED_DAYS;
        penaltyQuestService.generatePenaltyQuest(playerId, triggerReason);

        // Emit Domain Event (Handlers: Voice, Streak)
        domainEventPublisher.publish(new PenaltyZoneEnteredEvent(playerId));
    }

    @Transactional
    public void exitPenaltyZone(UUID playerId) {
        // RUNTIME GUARD: Check for COMPLETED penalty quest
        boolean hasCompletedQuest = penaltyQuestRepository.existsByPlayerIdAndStatus(
                playerId, 
                com.lifeos.penalty.domain.enums.PenaltyQuestStatus.COMPLETED
        );

        if (!hasCompletedQuest) {
            throw new IllegalStateException("Access Denied: Cannot exit Penalty Zone without completing a Penalty Quest.");
        }

        log.info("EXITING PENALTY ZONE: Player {}", playerId);
        
        // 1. Remove Penalty Zone Flag
        playerStateService.removeStatusFlag(playerId, com.lifeos.player.domain.enums.StatusFlagType.PENALTY_ZONE);
        
        // 2. Reset failures counter
        playerStateService.updateConsecutiveFailures(playerId, 0);
    }
}
