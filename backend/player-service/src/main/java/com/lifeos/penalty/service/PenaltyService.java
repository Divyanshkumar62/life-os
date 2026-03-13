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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.lifeos.system.service.SystemVoiceService;
import com.lifeos.system.domain.enums.SystemEventType;

@Service
public class PenaltyService {

    private static final Logger log = LoggerFactory.getLogger(PenaltyService.class);

    private final PenaltyRecordRepository penaltyRepository;
    private final PenaltyCalculationService calculationService;
    private final PlayerStateService playerStateService;
    private final QuestRepository questRepository;
    private final StreakService streakService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final PenaltyQuestService penaltyQuestService;
    private final PenaltyQuestRepository penaltyQuestRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final SystemVoiceService systemVoiceService;

    public PenaltyService(PenaltyRecordRepository penaltyRepository, 
                         PenaltyCalculationService calculationService,
                         PlayerStateService playerStateService,
                         QuestRepository questRepository,
                         StreakService streakService,
                         org.springframework.context.ApplicationEventPublisher eventPublisher,
                         PenaltyQuestService penaltyQuestService,
                         PenaltyQuestRepository penaltyQuestRepository,
                         DomainEventPublisher domainEventPublisher,
                         SystemVoiceService systemVoiceService) {
        this.penaltyRepository = penaltyRepository;
        this.calculationService = calculationService;
        this.playerStateService = playerStateService;
        this.questRepository = questRepository;
        this.streakService = streakService;
        this.eventPublisher = eventPublisher;
        this.penaltyQuestService = penaltyQuestService;
        this.penaltyQuestRepository = penaltyQuestRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.systemVoiceService = systemVoiceService;
    }

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

        if (def.isResetStreak()) {
            playerStateService.resetStreak(playerId);
        }

        // 4. Persist Record
        Map<String, Object> payload = new HashMap<>();
        payload.put("xpDeduction", def.getXpDeduction());
        
        PenaltyRecord record = PenaltyRecord.builder()
                .playerId(playerId)
                .questId(questId)
                .type(def.getType())
                .severity(def.getSeverity())
                .valuePayload(payload)
                .appliedAt(LocalDateTime.now())
                .build();

        penaltyRepository.save(record);
        log.info("Applied penalty {} to player {} for quest {}", def.getSeverity(), playerId, questId);

        // Emit Event - The PenaltyZoneEventHandler will pick this up to generate AI Penalty Quests
        domainEventPublisher.publish(new com.lifeos.event.concrete.PenaltyAppliedEvent(playerId, questId, reason, def.getXpDeduction()));
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
        systemVoiceService.emitEvent(playerId, SystemEventType.PENALTY_ALERT, "Warning. You have been placed in the Penalty Zone. All privileges are revoked.");
        
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
        systemVoiceService.emitEvent(playerId, SystemEventType.GENERAL_NOTICE, "Penalty Sequence Completed. Standard operations have resumed.");
        
        // 1. Remove Penalty Zone Flag
        playerStateService.removeStatusFlag(playerId, com.lifeos.player.domain.enums.StatusFlagType.PENALTY_ZONE);
        
        // 2. Reset failures counter
        playerStateService.updateConsecutiveFailures(playerId, 0);

        // 3. Igris Exception: Resume any suspended Promotion Exams
        var suspendedExams = questRepository.findByPlayerPlayerIdAndQuestTypeAndState(
            playerId, 
            com.lifeos.quest.domain.enums.QuestType.PROMOTION_EXAM, 
            com.lifeos.quest.domain.enums.QuestState.SUSPENDED
        );
        
        suspendedExams.ifPresent(quest -> {
            log.info("Igris Exception: Resuming suspended Promotion Exam for player {}", playerId);
            quest.setState(com.lifeos.quest.domain.enums.QuestState.ACTIVE);
            questRepository.save(quest);
        });

        // Emit Event
        domainEventPublisher.publish(new com.lifeos.event.concrete.PenaltyZoneExitedEvent(playerId));
    }
}
