package com.lifeos.penalty.service;

import com.lifeos.event.DomainEventPublisher;
import com.lifeos.penalty.domain.PenaltyQuest;
import com.lifeos.penalty.domain.enums.PenaltyQuestStatus;
import com.lifeos.penalty.domain.enums.PenaltyQuestType;
import com.lifeos.penalty.domain.enums.PenaltyTriggerReason;
import com.lifeos.penalty.domain.enums.WorkSource;
import com.lifeos.penalty.repository.PenaltyQuestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PenaltyQuestService {

    private static final Logger log = LoggerFactory.getLogger(PenaltyQuestService.class);
    private static final int MAX_DAILY_WORK_UNITS = 3;

    private final PenaltyQuestRepository questRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public void generatePenaltyQuest(UUID playerId, PenaltyTriggerReason reason) {
        // Idempotency Guard
        boolean exists = questRepository.existsByPlayerIdAndStatus(playerId, PenaltyQuestStatus.ACTIVE);
        if (exists) {
            log.info("Active penalty quest already exists for player {}", playerId);
            return;
        }

        PenaltyQuest quest = PenaltyQuest.builder()
                .playerId(playerId)
                .type(PenaltyQuestType.SURVIVAL)
                .triggerReason(reason)
                .requiredCount(10) // V1 Fixed limit
                .completedCount(0)
                .todayWorkUnits(0)
                .status(PenaltyQuestStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .lastWorkDate(null)
                .build();

        questRepository.save(quest);
        log.info("Generated Penalty Quest (SURVIVAL) for player {}. Reason: {}", playerId, reason);
    }

    @Transactional
    public void recordWork(UUID playerId, int workUnits, WorkSource source) {
        // Guard #1: Source Validation
        if (source != WorkSource.DAILY_QUEST && source != WorkSource.BACKLOG_CLEAR) {
            log.warn("Invalid work source applied to penalty: {}", source);
            return;
        }

        // Fetch ACTIVE quest
        PenaltyQuest quest = questRepository.findByPlayerIdAndStatus(playerId, PenaltyQuestStatus.ACTIVE)
                .orElse(null);

        if (quest == null) {
            log.warn("No active penalty quest found for player {}. Work ignored.", playerId);
            return;
        }

        LocalDate today = LocalDate.now();

        // Guard #2: Daily Cap Logic
        if (quest.getLastWorkDate() == null || !quest.getLastWorkDate().equals(today)) {
            // New day, reset counter
            quest.setTodayWorkUnits(0);
            quest.setLastWorkDate(today);
        }

        if (quest.getTodayWorkUnits() >= MAX_DAILY_WORK_UNITS) {
            log.info("Daily penalty work cap reached for player {}. Units ignored.", playerId);
            return;
        }

        // Apply partial work if units > remaining cap (though usually units=1)
        int allowedUnits = Math.min(workUnits, MAX_DAILY_WORK_UNITS - quest.getTodayWorkUnits());
        
        if (allowedUnits <= 0) return;

        quest.setCompletedCount(quest.getCompletedCount() + allowedUnits);
        quest.setTodayWorkUnits(quest.getTodayWorkUnits() + allowedUnits);

        log.info("Recorded {} work units for player {}. Progress: {}/{}", 
                allowedUnits, playerId, quest.getCompletedCount(), quest.getRequiredCount());

        if (quest.getCompletedCount() >= quest.getRequiredCount()) {
            quest.setStatus(PenaltyQuestStatus.COMPLETED);
            quest.setCompletedAt(LocalDateTime.now());
            
            // Domain Event: Handlers will trigger Exit and Voice
            eventPublisher.publish(new com.lifeos.event.concrete.PenaltyQuestCompletedEvent(playerId));
        }

        questRepository.save(quest);
    }

    public Map<String, Object> getPenaltyQuestStatus(UUID playerId) {
        return questRepository.findByPlayerIdAndStatus(playerId, PenaltyQuestStatus.ACTIVE)
                .map(q -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("active", true);
                    map.put("type", q.getType().name());
                    map.put("completed", q.getCompletedCount());
                    map.put("required", q.getRequiredCount());
                    map.put("todayWork", q.getTodayWorkUnits());
                    map.put("maxDaily", MAX_DAILY_WORK_UNITS);
                    return map;
                })
                .orElse(Map.of("active", false));
    }
}
