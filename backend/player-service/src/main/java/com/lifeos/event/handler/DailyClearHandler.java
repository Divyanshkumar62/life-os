package com.lifeos.event.handler;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.DomainEventHandler;
import com.lifeos.event.concrete.QuestCompletedEvent;
import com.lifeos.event.concrete.DailyQuestCompletedEvent;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.economy.service.InventoryService;
import com.lifeos.economy.repository.ShopItemRepository;
import com.lifeos.economy.domain.ShopItem;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.domain.enums.QuestCategory;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.domain.PlayerIdentity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class DailyClearHandler implements DomainEventHandler<DomainEvent> {

    private static final Logger log = LoggerFactory.getLogger(DailyClearHandler.class);

    private final QuestRepository questRepository;
    private final PlayerIdentityRepository playerIdentityRepository;
    private final PlayerStateService playerStateService;
    private final PenaltyService penaltyService;
    private final InventoryService inventoryService;
    private final ShopItemRepository shopItemRepository;

    public DailyClearHandler(QuestRepository questRepository,
                             PlayerIdentityRepository playerIdentityRepository,
                             PlayerStateService playerStateService,
                             PenaltyService penaltyService,
                             InventoryService inventoryService,
                             ShopItemRepository shopItemRepository) {
        this.questRepository = questRepository;
        this.playerIdentityRepository = playerIdentityRepository;
        this.playerStateService = playerStateService;
        this.penaltyService = penaltyService;
        this.inventoryService = inventoryService;
        this.shopItemRepository = shopItemRepository;
    }

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof QuestCompletedEvent || event instanceof DailyQuestCompletedEvent;
    }

    @Override
    @Transactional
    public void handle(DomainEvent event) {
        if (event instanceof QuestCompletedEvent questCompletedEvent) {
            onQuestCompleted(questCompletedEvent);
        } else if (event instanceof DailyQuestCompletedEvent dailyQuestCompletedEvent) {
            onDailyQuestCompleted(dailyQuestCompletedEvent);
        }
    }

    private void onQuestCompleted(QuestCompletedEvent event) {
        UUID playerId = event.getPlayerId();
        Quest quest = questRepository.findById(event.getQuestId()).orElse(null);
        if (quest == null) return;

        if ("[HIDDEN] The Architect's Original Trial".equals(quest.getTitle())) {
            log.info("Architect's Original Trial Completed by player {}", playerId);
            ShopItem item = shopItemRepository.findByCode("BLESSED_RANDOM_BOX")
                    .orElseThrow(() -> new IllegalStateException("BLESSED_RANDOM_BOX item definition not found"));
            inventoryService.addItem(playerId, item.getItemId(), 1);
            log.info("Granted Blessed Random Box to player {}", playerId);
        }
    }

    private void onDailyQuestCompleted(DailyQuestCompletedEvent event) {
        UUID playerId = event.getPlayerId();

        // 1. Idempotency Check
        if (playerStateService.hasActiveFlag(playerId, StatusFlagType.DAILY_CLEAR_REWARDED)) {
            log.info("Daily Clear rewards already processed for player {} today.", playerId);
            return;
        }

        PlayerIdentity identity = playerIdentityRepository.findById(playerId).orElse(null);
        if (identity == null || identity.getLastDailyReset() == null) {
            return;
        }

        LocalDateTime lastReset = identity.getLastDailyReset();

        // 2. Fetch all daily quests assigned since the last reset period
        List<Quest> dailies = questRepository.findByPlayerPlayerId(playerId).stream()
                .filter(q -> q.getCategory() == QuestCategory.SYSTEM_DAILY)
                .filter(q -> !q.getAssignedAt().isBefore(lastReset))
                .filter(q -> !q.getTitle().equals("[HIDDEN] The Architect's Original Trial"))
                .toList();

        // 3. Verify exactly 3 core AI dailies are completed
        if (dailies.size() == 3 && dailies.stream().allMatch(q -> q.getState() == QuestState.COMPLETED)) {
            log.info("DAILY CLEAR ACHIEVED for player {}!", playerId);

            // Reward 1: Status Recovery (Clear all active debuffs & restore fatigue)
            penaltyService.clearTemporaryDebuffs(playerId);
            playerStateService.restoreFatigue(playerId);

            // Reward 3: 1 'Random Box' added to inventory
            ShopItem item = shopItemRepository.findByCode("RANDOM_BOX")
                    .orElseThrow(() -> new IllegalStateException("RANDOM_BOX item definition not found"));
            inventoryService.addItem(playerId, item.getItemId(), 1);

            // Apply Daily Clear flag to prevent double rewards
            playerStateService.applyStatusFlag(playerId, StatusFlagType.DAILY_CLEAR_REWARDED, lastReset.plusHours(26));

            log.info("DAILY CLEAR rewards successfully applied for player {}", playerId);
        }
    }
}
