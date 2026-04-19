package com.lifeos.quest.service;

import com.lifeos.economy.service.EconomyService;
import com.lifeos.economy.service.InventoryService;
import com.lifeos.economy.repository.PlayerEconomyRepository;
import com.lifeos.notification.service.PushNotificationService;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.PlayerProgression;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.repository.PlayerAttributeRepository;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.repository.PlayerProgressionRepository;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.domain.enums.Priority;
import com.lifeos.quest.domain.enums.QuestCategory;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.domain.enums.QuestType;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.shop.domain.ShopItem;
import com.lifeos.shop.repository.ShopItemRepository;
import com.lifeos.streak.service.StreakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedGateService {

    private static final double RED_GATE_TRIGGER_CHANCE = 0.12;
    private static final int MIN_EXPIRATION_HOURS = 4;
    private static final int MAX_EXPIRATION_HOURS = 12;
    private static final int SUCCESS_XP_MULTIPLIER = 3;
    private static final int SUCCESS_GOLD_MULTIPLIER = 3;
    private static final double GOLD_DRAIN_PERCENTAGE = 0.10;
    private static final String RED_GATE_KEY_CODE = "S_RANK_RED_GATE_KEY";

    private final PlayerIdentityRepository identityRepository;
    private final QuestRepository questRepository;
    private final PlayerProgressionRepository progressionRepository;
    private final PlayerAttributeRepository attributeRepository;
    private final EconomyService economyService;
    private final PlayerEconomyRepository economyRepository;
    private final InventoryService inventoryService;
    private final StreakService streakService;
    private final PushNotificationService pushNotificationService;
    private final ShopItemRepository shopItemRepository;

    private final Random random = new Random();

    public boolean isRedGateActive(UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null || !identity.isRedGateActive()) {
            return false;
        }
        if (identity.getRedGateExpiresAt() != null && LocalDateTime.now().isAfter(identity.getRedGateExpiresAt())) {
            log.info("Red Gate expired for player {}", playerId);
            expireRedGate(playerId);
            return false;
        }
        return true;
    }

    public void checkAndTriggerRandomRedGate(UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null || !identity.isOnboardingCompleted()) {
            return;
        }
        if (identity.isRedGateActive()) {
            log.info("Player {} already in Red Gate", playerId);
            return;
        }
        if (identity.isXpFrozen()) {
            log.info("Player {} XP is frozen, skipping Red Gate trigger", playerId);
            return;
        }

        if (random.nextDouble() < RED_GATE_TRIGGER_CHANCE) {
            log.info("Random Red Gate triggered for player {} (chance: {}%)", playerId, RED_GATE_TRIGGER_CHANCE * 100);
            activateRedGate(playerId, false);
        }
    }

    public void triggerRedGateWithKey(UUID playerId) {
        List<ShopItem> keyItems = shopItemRepository.findAll().stream()
                .filter(item -> RED_GATE_KEY_CODE.equals(item.getItemCode()) && item.isActive())
                .toList();

        if (keyItems.isEmpty()) {
            throw new IllegalStateException("Red Gate Key not available in shop");
        }

        ShopItem keyItem = keyItems.get(0);
        inventoryService.addItem(playerId, keyItem.getItemId(), -1);

        activateRedGate(playerId, true);
        log.info("Player {} triggered Red Gate with key", playerId);
    }

    @Transactional
    public void activateRedGate(UUID playerId, boolean forced) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null) {
            return;
        }

        if (identity.isRedGateActive()) {
            log.warn("Player {} already in Red Gate", playerId);
            return;
        }

        int expirationHours = MIN_EXPIRATION_HOURS + random.nextInt(MAX_EXPIRATION_HOURS - MIN_EXPIRATION_HOURS + 1);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);

        Quest raidQuest = generateRaidQuest(playerId, identity, expirationHours);

        identity.setRedGateActive(true);
        identity.setRedGateExpiresAt(expiresAt);
        identity.setRedGateQuestId(raidQuest.getQuestId());
        identity.setXpFrozen(true);
        identityRepository.save(identity);

        pushNotificationService.sendNotification(
                playerId,
                null,
                "WARNING: Red Gate Opened!",
                "You have been pulled into a Sealed Reality. Survival is the only option. " + expirationHours + " hours remaining.",
                null
        );

        log.info("Red Gate activated for player {}, expires at {}, quest: {}", playerId, expiresAt, raidQuest.getTitle());
    }

    private Quest generateRaidQuest(UUID playerId, PlayerIdentity identity, int expirationHours) {
        String jobClass = identity.getJobClass();
        String primaryAttribute = "SCHOLAR".equals(jobClass) ? "INT" : 
                                  "VANGUARD".equals(jobClass) ? "STR" : "INT";

        String questTitle = "Red Gate Raid: Survive the Sealed Reality";
        String questDesc = String.format(
                "A Red Gate has opened. You must complete this extreme challenge within %d hours. " +
                "Failure will result in double penalty. Success grants 3x rewards and a guaranteed artifact.",
                expirationHours
        );

        Quest quest = Quest.builder()
                .player(identity)
                .title(questTitle)
                .description(questDesc)
                .questType(QuestType.RED_GATE)
                .category(QuestCategory.SYSTEM_DAILY)
                .primaryAttribute(AttributeType.valueOf(primaryAttribute))
                .difficultyTier(DifficultyTier.RED)
                .priority(Priority.CRITICAL)
                .state(QuestState.ACTIVE)
                .deadlineAt(LocalDateTime.now().plusHours(expirationHours))
                .startsAt(LocalDateTime.now())
                .systemMutable(false)
                .egoBreakerFlag(false)
                .expectedFailureProbability(0.7)
                .build();

        quest = questRepository.save(quest);
        return quest;
    }

    @Transactional
    public void completeRedGate(UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null || !identity.isRedGateActive()) {
            log.warn("No active Red Gate for player {}", playerId);
            return;
        }

        Quest quest = questRepository.findById(identity.getRedGateQuestId()).orElse(null);
        if (quest != null) {
            quest.setState(QuestState.COMPLETED);
            questRepository.save(quest);
        }

        PlayerProgression progression = progressionRepository.findByPlayerPlayerId(playerId).orElse(null);
        if (progression != null) {
            long baseXp = 300;
            long bonusXp = baseXp * SUCCESS_XP_MULTIPLIER;
            progression.setCurrentXp(progression.getCurrentXp() + (int) bonusXp);
            
            long baseGold = 500;
            long bonusGold = baseGold * SUCCESS_GOLD_MULTIPLIER;
            economyService.addGold(playerId, bonusGold, "Red Gate Success Reward");
            
            progressionRepository.save(progression);
        }

        String jobClass = identity.getJobClass();
        AttributeType primaryAttr = "SCHOLAR".equals(jobClass) ? AttributeType.INTELLIGENCE :
                                   "VANGUARD".equals(jobClass) ? AttributeType.STRENGTH : AttributeType.INTELLIGENCE;
        attributeRepository.findByPlayerPlayerIdAndAttributeType(playerId, primaryAttr).ifPresent(attr -> {
            attr.setCurrentValue(attr.getCurrentValue() + 2);
            attributeRepository.save(attr);
            log.info("Awarded +2 to {} for player {}", primaryAttr, playerId);
        });

        List<ShopItem> artifacts = shopItemRepository.findAll().stream()
                .filter(item -> "ARTIFACT".equals(item.getItemType()) && item.isActive())
                .toList();
        
        if (!artifacts.isEmpty()) {
            ShopItem artifact = artifacts.get(random.nextInt(artifacts.size()));
            inventoryService.addItem(playerId, artifact.getItemId(), 1);
            log.info("Awarded artifact: {} to player {}", artifact.getItemName(), playerId);
        }

        identity.setRedGateActive(false);
        identity.setRedGateExpiresAt(null);
        identity.setRedGateQuestId(null);
        identity.setXpFrozen(false);
        identityRepository.save(identity);

        pushNotificationService.sendNotification(
                playerId,
                null,
                "Red Gate Conquered!",
                "You survived the Sealed Reality! 3x rewards + artifact claimed.",
                null
        );

        log.info("Player {} completed Red Gate successfully", playerId);
    }

    @Transactional
    public void failRedGate(UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null || !identity.isRedGateActive()) {
            log.warn("No active Red Gate for player {}", playerId);
            return;
        }

        Quest quest = questRepository.findById(identity.getRedGateQuestId()).orElse(null);
        if (quest != null) {
            quest.setState(QuestState.FAILED);
            questRepository.save(quest);
        }

        streakService.resetStreak(playerId);
        log.info("Streak reset for player {} due to Red Gate failure", playerId);

        PlayerProgression progression = progressionRepository.findByPlayerPlayerId(playerId).orElse(null);
        if (progression != null) {
            var economy = economyRepository.findById(playerId).orElse(null);
            long currentGold = economy != null ? economy.getGoldBalance().longValue() : 0;
            long goldDrain = (long) (currentGold * GOLD_DRAIN_PERCENTAGE);
            economyService.deductGold(playerId, goldDrain, "Red Gate Failure Penalty");
            log.info("Drained {} gold from player {} (10% penalty)", goldDrain, playerId);
        }

        identity.setRedGateActive(false);
        identity.setRedGateExpiresAt(null);
        identity.setRedGateQuestId(null);
        identity.setXpFrozen(false);
        identityRepository.save(identity);

        pushNotificationService.sendNotification(
                playerId,
                null,
                "Red Gate Failed",
                "The Sealed Reality has broken you. Streak reset and -10% gold penalty applied.",
                null
        );

        log.info("Player {} failed Red Gate", playerId);
    }

    private void expireRedGate(UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null) {
            return;
        }

        Quest quest = questRepository.findById(identity.getRedGateQuestId()).orElse(null);
        if (quest != null && quest.getState() == QuestState.ACTIVE) {
            quest.setState(QuestState.EXPIRED);
            questRepository.save(quest);
        }

        identity.setRedGateActive(false);
        identity.setRedGateExpiresAt(null);
        identity.setRedGateQuestId(null);
        identity.setXpFrozen(false);
        identityRepository.save(identity);

        log.info("Red Gate expired for player {}", playerId);
    }

    public Quest getActiveRedGateQuest(UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null || !identity.isRedGateActive() || identity.getRedGateQuestId() == null) {
            return null;
        }
        return questRepository.findById(identity.getRedGateQuestId()).orElse(null);
    }

    public boolean isShopLocked(UUID playerId) {
        return isRedGateActive(playerId);
    }

    public boolean isInventoryLocked(UUID playerId) {
        return isRedGateActive(playerId);
    }

    public Long getRemainingSeconds(UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null || !identity.isRedGateActive() || identity.getRedGateExpiresAt() == null) {
            return null;
        }
        java.time.Duration duration = java.time.Duration.between(LocalDateTime.now(), identity.getRedGateExpiresAt());
        return duration.isNegative() ? 0L : duration.getSeconds();
    }
}
