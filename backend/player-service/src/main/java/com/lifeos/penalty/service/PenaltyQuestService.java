package com.lifeos.penalty.service;

import com.lifeos.event.DomainEventPublisher;
import com.lifeos.penalty.domain.PenaltyQuest;
import com.lifeos.penalty.domain.enums.PenaltyQuestStatus;
import com.lifeos.penalty.domain.enums.PenaltyQuestType;
import com.lifeos.penalty.domain.enums.PenaltyTriggerReason;
import com.lifeos.penalty.domain.enums.WorkSource;
import com.lifeos.penalty.repository.PenaltyQuestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class PenaltyQuestService {

    private static final Logger log = LoggerFactory.getLogger(PenaltyQuestService.class);

    private final PenaltyQuestRepository questRepository;
    private final DomainEventPublisher eventPublisher;

    public PenaltyQuestService(PenaltyQuestRepository questRepository, DomainEventPublisher eventPublisher) {
        this.questRepository = questRepository;
        this.eventPublisher = eventPublisher;
    }

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
                .requiredCount(1) // Single survival trial
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

        // Apply work directly
        quest.setCompletedCount(quest.getCompletedCount() + workUnits);
        quest.setLastWorkDate(LocalDate.now());

        log.info("Recorded {} work units for player {}. Progress: {}/{}", 
                workUnits, playerId, quest.getCompletedCount(), quest.getRequiredCount());

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
                    map.put("todayWork", q.getCompletedCount());
                    map.put("maxDaily", 1);
                    return map;
                })
                .orElse(Map.of("active", false));
    }
}
