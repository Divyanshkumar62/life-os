package com.lifeos.quest.service;

import com.lifeos.event.DomainEventPublisher;
import com.lifeos.event.concrete.DailyQuestCompletedEvent;
import com.lifeos.event.concrete.QuestCompletedEvent;
import com.lifeos.event.concrete.QuestFailedEvent;
import com.lifeos.event.concrete.QuestExpiredEvent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class QuestLifecycleServiceImpl implements QuestLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(QuestLifecycleServiceImpl.class);
    
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

    public QuestLifecycleServiceImpl(QuestRepository questRepository, QuestOutcomeProfileRepository outcomeRepository,
                                   QuestMutationLogRepository mutationLogRepository, PlayerQuestLinkRepository linkRepository,
                                   @Lazy com.lifeos.penalty.service.PenaltyService penaltyService,
                                   @Lazy com.lifeos.progression.service.ProgressionService progressionService,
                                   PlayerStateService playerStateService,
                                   com.lifeos.player.repository.PlayerIdentityRepository playerIdentityRepository,
                                   DomainEventPublisher domainEventPublisher) {
        this.questRepository = questRepository;
        this.outcomeRepository = outcomeRepository;
        this.mutationLogRepository = mutationLogRepository;
        this.linkRepository = linkRepository;
        this.penaltyService = penaltyService;
        this.progressionService = progressionService;
        this.playerStateService = playerStateService;
        this.playerIdentityRepository = playerIdentityRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public QuestRepository getQuestRepository() {
        return questRepository;
    }


    @Override
    @Transactional
    public Quest assignQuest(QuestRequest request) {
        log.info("QUEST SERVICE: Assigning quest '{}' for player {}", request.getTitle(), request.getPlayerId());
        
        var player = playerIdentityRepository.findById(request.getPlayerId())
                .orElseThrow(() -> {
                    log.error("QUEST SERVICE: Player not found: {}", request.getPlayerId());
                    return new IllegalArgumentException("Player not found");
                });

        log.debug("QUEST SERVICE: Found player: {}", player.getUsername());

        Quest quest = Quest.builder()
                .player(player)
                .title(request.getTitle())
                .description(request.getDescription())
                .questType(request.getQuestType())
                .category(QuestCategory.NORMAL) // Default to NORMAL if not specified
                .primaryAttribute(request.getPrimaryAttribute())
                .difficultyTier(request.getDifficultyTier())
                .priority(request.getPriority())
                .state(QuestState.ASSIGNED)
                .deadlineAt(request.getDeadlineAt())
                .systemMutable(request.isSystemMutable())
                .egoBreakerFlag(request.isEgoBreakerFlag())
                .expectedFailureProbability(request.getExpectedFailureProbability())
                .build();
        
        // Blocking Logic: If a PENALTY or PROMOTION_EXAM quest is active, block everything else
        if (request.getQuestType() != QuestType.PENALTY && request.getQuestType() != QuestType.PROMOTION_EXAM && hasBlockingQuest(player.getPlayerId())) {
            log.warn("QUEST SERVICE: Blocking quest assignment - player has active PENALTY or PROMOTION_EXAM");
            throw new IllegalStateException("A mandatory trial is active. You must focus on your current objective.");
        }
        
        // Invariant check: RED -> egoBreaker handled by @PrePersist in Entity, but good to be aware.
        
        quest = questRepository.save(quest);
        log.info("QUEST SERVICE: Quest saved with ID: {}", quest.getQuestId());

        // Create Outcome Profile
        QuestOutcomeProfile outcome = QuestOutcomeProfile.builder()
                .quest(quest)
                .successXp(request.getSuccessXp())
                .failureXp(request.getFailureXp())
                .goldReward(request.getGoldReward())
                .attributeDeltaJson(request.getAttributeDeltas())
                .build();
        outcomeRepository.save(outcome);
        log.debug("QUEST SERVICE: Outcome profile saved");

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
        log.info("=== COMPLETE QUEST called for: {}", questId);
        
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> {
                    log.error("Quest not found: {}", questId);
                    return new IllegalArgumentException("Quest not found: " + questId);
                });

        log.info("Found quest: {} with state: {}", quest.getTitle(), quest.getState());

        if (quest.getState() != QuestState.ACTIVE) {
            log.error("Quest {} is not ACTIVE, current state: {}", questId, quest.getState());
            throw new IllegalStateException("Quest must be ACTIVE to complete. Current: " + quest.getState());
        }

        // Blocking Logic: If a PENALTY or PROMOTION_EXAM quest is active, block everything else
        if (quest.getQuestType() != QuestType.PENALTY && quest.getQuestType() != QuestType.PROMOTION_EXAM && hasBlockingQuest(quest.getPlayer().getPlayerId())) {
            log.warn("Blocking quest completion - player has PENALTY or PROMOTION_EXAM");
            throw new IllegalStateException("A mandatory trial is active. You must focus on your current objective.");
        }

        // SYSTEM AUTHORITY: Server-side temporal evaluation strictly enforced.
        LocalDateTime actualServerTime = LocalDateTime.now();
        if (quest.getDeadlineAt() != null && actualServerTime.isAfter(quest.getDeadlineAt())) {
            log.error("Quest {} has expired. Deadline: {}, Now: {}", questId, quest.getDeadlineAt(), actualServerTime);
            throw new com.lifeos.system.exception.SystemAuthorityException("Quest has expired relative to Server Time. Nice try, Hunter.");
        }

        // 1. Update Quest State
        quest.setState(QuestState.COMPLETED);
        questRepository.save(quest);
        log.info("Quest {} state updated to COMPLETED", questId);

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
            throw new IllegalStateException("Quest is not in a fail-able state: " + quest.getState());
        }

        quest.setState(QuestState.FAILED);
        questRepository.save(quest);

        // Apply Penalties ONLY for non-INTEL_GATHERING quests
        // Intel quests: fail = no penalty, just blocks dungeon entry
        if (quest.getQuestType() != QuestType.INTEL_GATHERING) {
            log.info("Applying penalty for failed quest: {} (type: {})", quest.getTitle(), quest.getQuestType());
            penaltyService.applyPenalty(questId, quest.getPlayer().getPlayerId(), com.lifeos.penalty.domain.enums.FailureReason.FAILED);
        } else {
            log.info("Intel Quest failed: {} - No penalty applied, only dungeon entry blocked", quest.getTitle());
        }
        
        // Update Link
        var link = linkRepository.findByPlayerIdAndQuestId(quest.getPlayer().getPlayerId(), questId)
                .orElseThrow(() -> new IllegalStateException("Link missing"));
        link.setState(QuestState.FAILED);
        link.setFailedAt(LocalDateTime.now());
        linkRepository.save(link);

        // Emit Event
        domainEventPublisher.publish(new QuestFailedEvent(quest.getPlayer().getPlayerId(), questId, quest.getTitle(), quest.getQuestType()));
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

        // Emit Event
        domainEventPublisher.publish(new QuestExpiredEvent(quest.getPlayer().getPlayerId(), questId, quest.getTitle()));
    }

    @Override
    public List<Quest> getActiveQuests(UUID playerId) {
        log.info("Fetching active quests for player: {}", playerId);
        return questRepository.findByPlayerPlayerIdAndState(playerId, QuestState.ACTIVE);
    }

    private boolean hasBlockingQuest(UUID playerId) {
        return questRepository.findByPlayerPlayerIdAndQuestTypeAndState(playerId, QuestType.PENALTY, QuestState.ACTIVE).isPresent() ||
               questRepository.findByPlayerPlayerIdAndQuestTypeAndState(playerId, QuestType.PENALTY, QuestState.ASSIGNED).isPresent() ||
               questRepository.findByPlayerPlayerIdAndQuestTypeAndState(playerId, QuestType.PROMOTION_EXAM, QuestState.ACTIVE).isPresent() ||
               questRepository.findByPlayerPlayerIdAndQuestTypeAndState(playerId, QuestType.PROMOTION_EXAM, QuestState.ASSIGNED).isPresent();
    }
}
