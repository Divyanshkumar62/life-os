package com.lifeos.quest.service;

import com.lifeos.event.DomainEventPublisher;
import com.lifeos.event.concrete.DailyQuestCompletedEvent;
import com.lifeos.event.concrete.QuestCompletedEvent;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.QuestOutcomeProfile;
import com.lifeos.quest.domain.QuestMutationLog;
import com.lifeos.quest.domain.PlayerQuestLink;
import com.lifeos.quest.domain.enums.MutationType;
import com.lifeos.quest.domain.enums.QuestCategory;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.domain.enums.QuestType;
import com.lifeos.quest.dto.QuestRequest;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.quest.repository.QuestOutcomeProfileRepository;
import com.lifeos.quest.repository.QuestMutationLogRepository;
import com.lifeos.quest.repository.PlayerQuestLinkRepository;
import com.lifeos.player.domain.enums.StatusFlagType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestLifecycleServiceImpl implements QuestLifecycleService {

    private final QuestRepository questRepository;
    private final QuestOutcomeProfileRepository outcomeRepository;
    private final QuestMutationLogRepository mutationLogRepository;
    private final PlayerQuestLinkRepository linkRepository;
    
    // Services retained for Fail/Expire logic or non-event paths
    private final com.lifeos.penalty.service.PenaltyService penaltyService;
    private final com.lifeos.progression.service.ProgressionService progressionService;
    private final PlayerStateService playerStateService;
    private final com.lifeos.player.repository.PlayerIdentityRepository playerIdentityRepository;
    
    // Event Publisher
    private final DomainEventPublisher domainEventPublisher;


    @Override
    @Transactional
    public Quest assignQuest(QuestRequest request) {
        var player = playerIdentityRepository.findById(request.getPlayerId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        Quest quest = Quest.builder()
                .player(player)
                .title(request.getTitle())
                .description(request.getDescription())
                .questType(request.getQuestType())
                .difficultyTier(request.getDifficultyTier())
                .priority(request.getPriority())
                .state(QuestState.ASSIGNED)
                .deadlineAt(request.getDeadlineAt())
                .systemMutable(request.isSystemMutable())
                .build();
        
        // Invariant check: RED -> egoBreaker handled by @PrePersist in Entity, but good to be aware.
        
        quest = questRepository.save(quest);

        // Create Outcome Profile
        QuestOutcomeProfile outcome = QuestOutcomeProfile.builder()
                .quest(quest)
                .successXp(request.getSuccessXp())
                .failureXp(request.getFailureXp())
                .attributeDeltaJson(request.getAttributeDeltas())
                .build();
        outcomeRepository.save(outcome);

        // Create Link (Projection)
        PlayerQuestLink link = PlayerQuestLink.builder()
                .playerId(player.getPlayerId())
                .questId(quest.getQuestId())
                .state(QuestState.ASSIGNED)
                .build();
        linkRepository.save(link);

        // Auto-activate for now? Spec says "ASSIGNED" -> "ACTIVE". 
        // Let's assume user accepts it or it auto-activates. Spec implies System Assigned.
        // Let's move to ACTIVE immediately for V1 simplicity? 
        // "System may modify quests mid-flight". 
        // Let's keep ASSIGNED. A separate "activate" step might be needed or we implicitly activate.
        // For atomic quests (1 quest = 1 task), usually they are assigned and active.
        // Update: Let's set to ACTIVE immediately if it starts now.
        if (quest.getStartsAt() == null || !quest.getStartsAt().isAfter(LocalDateTime.now())) {
            quest.setState(QuestState.ACTIVE);
            quest.setStartsAt(LocalDateTime.now());
            link.setState(QuestState.ACTIVE);
            link.setActivatedAt(LocalDateTime.now());
            // save updates
            questRepository.save(quest);
            linkRepository.save(link);
        }

        return quest;
    }

    @Override
    @Transactional
    public void updateQuest(UUID questId, Map<String, Object> updates, String reason) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found"));

        // Log mutation
        QuestMutationLog log = QuestMutationLog.builder()
                .quest(quest)
                .mutationType(MutationType.DIFFICULTY_ESCALATION) // Defaulting or inferring? 
                // Ideally passing mutation type in arguments or inferring.
                // For V1 let's assume generic Update.
                .mutationType(MutationType.DEADLINE_SHIFT) // Placeholder, logic needed to determine type
                .reason(reason)
                // .oldValueJson(...)
                // .newValueJson(...)
                .build();
        
        // Simplify for V1: Just supporting deadline shift
        if (updates.containsKey("deadlineAt")) {
            log.setMutationType(MutationType.DEADLINE_SHIFT);
            // Capture old value
            // ...
            quest.setDeadlineAt((LocalDateTime) updates.get("deadlineAt"));
        }
        
        mutationLogRepository.save(log);
        questRepository.save(quest);
    }

    @Override
    @Transactional
    public void completeQuest(UUID questId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found"));

        if (quest.getState() != QuestState.ACTIVE) {
            throw new IllegalStateException("Quest must be ACTIVE to complete. Current: " + quest.getState());
        }

        if (quest.getDeadlineAt() != null && LocalDateTime.now().isAfter(quest.getDeadlineAt())) {
            throw new IllegalStateException("Quest has expired, cannot complete.");
        }

        // 1. Update Quest State
        quest.setState(QuestState.COMPLETED);
        questRepository.save(quest);

        // 2. Update Link
        var link = linkRepository.findByPlayerIdAndQuestId(quest.getPlayer().getPlayerId(), questId)
                .orElse(new PlayerQuestLink());
        link.setState(QuestState.COMPLETED);
        link.setCompletedAt(LocalDateTime.now());
        linkRepository.save(link);

        // 3. Emit Domain Event
        // Events: QuestCompletedEvent (Generic), DailyQuestCompletedEvent (Specific)
        var event = new QuestCompletedEvent(quest.getPlayer().getPlayerId(), questId, quest.getQuestType());
        domainEventPublisher.publish(event);
        
        if (quest.getCategory() == QuestCategory.SYSTEM_DAILY) {
            domainEventPublisher.publish(new DailyQuestCompletedEvent(quest.getPlayer().getPlayerId()));
        }
        
        // Note: RewardService, Stats, Progression, Penalty Work are now handled by EventHandlers listing to these events.
        // Legacy direct calls removed.
    }

    @Override
    @Transactional
    public void failQuest(UUID questId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found"));

        if (quest.getState() != QuestState.ACTIVE && quest.getState() != QuestState.ASSIGNED) {
            // If already failed or completed, ignore? Or throw?
            throw new IllegalStateException("Quest is not in a fail-able state: " + quest.getState());
        }

        quest.setState(QuestState.FAILED);
        questRepository.save(quest);

        // Apply Penalties
        penaltyService.applyPenalty(questId, quest.getPlayer().getPlayerId(), com.lifeos.penalty.domain.enums.FailureReason.FAILED);
        
        // Progression Check (Promotion Fail)
        if (quest.getQuestType() == com.lifeos.quest.domain.enums.QuestType.PROMOTION_EXAM) {
            progressionService.processPromotionOutcome(quest.getPlayer().getPlayerId(), false);
        }

        // Update Link
        var link = linkRepository.findByPlayerIdAndQuestId(quest.getPlayer().getPlayerId(), questId)
                .orElseThrow(() -> new IllegalStateException("Link missing"));
        link.setState(QuestState.FAILED);
        link.setFailedAt(LocalDateTime.now());
        linkRepository.save(link);
    }

    @Override
    @Transactional
    public void expireQuest(UUID questId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found"));

        if (quest.getState() != QuestState.ACTIVE) {
            return; // Only active quests expire
        }

        quest.setState(QuestState.EXPIRED);
        questRepository.save(quest);
        
        // Apply Penalties (Expiration)
        penaltyService.applyPenalty(questId, quest.getPlayer().getPlayerId(), com.lifeos.penalty.domain.enums.FailureReason.EXPIRED);
        
        var link = linkRepository.findByPlayerIdAndQuestId(quest.getPlayer().getPlayerId(), questId)
                .orElse(new PlayerQuestLink());
        link.setState(QuestState.EXPIRED);
        linkRepository.save(link);
    }
}
