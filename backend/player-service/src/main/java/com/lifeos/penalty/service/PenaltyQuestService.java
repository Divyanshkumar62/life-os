package com.lifeos.penalty.service;

import com.lifeos.economy.service.EconomyService;
import com.lifeos.event.DomainEventPublisher;
import com.lifeos.penalty.domain.PenaltyQuest;
import com.lifeos.penalty.domain.enums.PenaltyQuestStatus;
import com.lifeos.penalty.domain.enums.PenaltyQuestType;
import com.lifeos.penalty.domain.enums.PenaltyTriggerReason;
import com.lifeos.penalty.domain.enums.WorkSource;
import com.lifeos.penalty.dto.SurvivalTaskDTO;
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
    private final EconomyService economyService;

    public PenaltyQuestService(PenaltyQuestRepository questRepository,
                               DomainEventPublisher eventPublisher,
                               EconomyService economyService) {
        this.questRepository = questRepository;
        this.eventPublisher = eventPublisher;
        this.economyService = economyService;
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

    @Transactional
    public SurvivalTaskDTO reportSurvivalTaskProgress(UUID playerId, int unitsCompleted) {
        PenaltyQuest quest = questRepository.findByPlayerIdAndStatus(playerId, PenaltyQuestStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("No active penalty quest found for player " + playerId));

        quest.setCompletedCount(quest.getCompletedCount() + unitsCompleted);
        quest.setTodayWorkUnits(quest.getTodayWorkUnits() + unitsCompleted);
        quest.setLastWorkDate(LocalDate.now());

        log.info("Survival task progress for player {}. Progress: {}/{}",
                playerId, quest.getCompletedCount(), quest.getRequiredCount());

        if (quest.getCompletedCount() >= quest.getRequiredCount()) {
            quest.setStatus(PenaltyQuestStatus.COMPLETED);
            quest.setCompletedAt(LocalDateTime.now());
            eventPublisher.publish(new com.lifeos.event.concrete.PenaltyQuestCompletedEvent(playerId));
            log.info("Survival task COMPLETED for player {}", playerId);
        }

        PenaltyQuest saved = questRepository.save(quest);
        SurvivalTaskDTO dto = toDTO(saved);
        dto.setStatus(saved.getStatus().name());
        dto.setEscaped(saved.getStatus() == PenaltyQuestStatus.COMPLETED);
        return dto;
    }

    @Transactional
    public SurvivalTaskDTO completeSurvivalTask(UUID playerId) {
        PenaltyQuest quest = questRepository.findByPlayerIdAndStatus(playerId, PenaltyQuestStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("No active penalty quest found for player " + playerId));

        quest.setCompletedCount(quest.getRequiredCount());
        quest.setStatus(PenaltyQuestStatus.COMPLETED);
        quest.setCompletedAt(LocalDateTime.now());

        eventPublisher.publish(new com.lifeos.event.concrete.PenaltyQuestCompletedEvent(playerId));
        log.info("Survival task force-completed for player {}", playerId);

        PenaltyQuest saved = questRepository.save(quest);
        SurvivalTaskDTO dto = toDTO(saved);
        dto.setStatus("COMPLETED");
        dto.setEscaped(true);
        return dto;
    }

    @Transactional
    public SurvivalTaskDTO rerollSurvivalTask(UUID playerId) {
        PenaltyQuest quest = questRepository.findByPlayerIdAndStatus(playerId, PenaltyQuestStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("No active penalty quest found for player " + playerId));

        long balance = economyService.getEconomyState(playerId).getGoldBalance().longValue();
        long deduction = Math.max(balance / 10, 100);
        long actualDeduction = Math.min(balance, deduction);

        if (actualDeduction > 0) {
            economyService.deductGold(playerId, actualDeduction, "Survival Task Reroll");
        }

        quest.setCompletedCount(0);
        quest.setTodayWorkUnits(0);
        quest.setLastWorkDate(null);

        PenaltyQuest saved = questRepository.save(quest);

        SurvivalTaskDTO dto = toDTO(saved);
        dto.setGoldDeducted(actualDeduction);
        return dto;
    }

    public SurvivalTaskDTO getActiveTaskDTO(UUID playerId) {
        PenaltyQuest quest = questRepository.findByPlayerIdAndStatus(playerId, PenaltyQuestStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("No active penalty quest found for player " + playerId));
        return toDTO(quest);
    }

    private SurvivalTaskDTO toDTO(PenaltyQuest quest) {
        double progress = quest.getRequiredCount() > 0
                ? (double) quest.getCompletedCount() / quest.getRequiredCount()
                : 0.0;

        String title = generateTitle(quest);
        String description = generateDescription(quest);

        return SurvivalTaskDTO.builder()
                .questId(quest.getId())
                .playerId(quest.getPlayerId())
                .type(quest.getType().name())
                .title(title)
                .description(description)
                .requiredCount(quest.getRequiredCount())
                .completedCount(quest.getCompletedCount())
                .progress(Math.min(progress, 1.0))
                .status(quest.getStatus().name())
                .createdAt(quest.getCreatedAt())
                .completedAt(quest.getCompletedAt())
                .escaped(quest.getStatus() == PenaltyQuestStatus.COMPLETED)
                .build();
    }

    private String generateTitle(PenaltyQuest quest) {
        if (quest.getType() == PenaltyQuestType.SURVIVAL) {
            return "The Architect's Crucible";
        }
        return "Survival Task";
    }

    private String generateDescription(PenaltyQuest quest) {
        if (quest.getTriggerReason() == PenaltyTriggerReason.EXAM_FAIL) {
            return "The System demands penance for your failed examination. Prove your worth through disciplined action. The Architect watches.";
        }
        return "Your discipline has faltered. The System requires proof of your commitment. Complete the assigned tasks to regain your standing.";
    }
}
