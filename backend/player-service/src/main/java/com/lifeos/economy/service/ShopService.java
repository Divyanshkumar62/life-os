package com.lifeos.economy.service;

import com.lifeos.economy.domain.PurchaseTransaction;
import com.lifeos.economy.domain.ShopItem;
import com.lifeos.economy.repository.PurchaseTransactionRepository;
import com.lifeos.economy.repository.ShopItemRepository;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.streak.service.StreakService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lifeos.economy.dto.PurchaseResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ShopService {
    private static final Logger log = LoggerFactory.getLogger(ShopService.class);

    private final ShopItemRepository itemRepository;
    private final EconomyService economyService;
    private final PurchaseTransactionRepository xactionRepository;
    private final PlayerStateService playerStateService;
    private final StreakService streakService;
    private final InventoryService inventoryService;
    private final com.lifeos.economy.repository.PurchaseCooldownRepository cooldownRepository;
    private final com.lifeos.player.repository.PlayerIdentityRepository playerIdentityRepository;

    public ShopService(ShopItemRepository itemRepository, EconomyService economyService,
                      PurchaseTransactionRepository xactionRepository, PlayerStateService playerStateService,
                      StreakService streakService, InventoryService inventoryService,
                      com.lifeos.economy.repository.PurchaseCooldownRepository cooldownRepository,
                      com.lifeos.player.repository.PlayerIdentityRepository playerIdentityRepository) {
        this.itemRepository = itemRepository;
        this.economyService = economyService;
        this.xactionRepository = xactionRepository;
        this.playerStateService = playerStateService;
        this.streakService = streakService;
        this.inventoryService = inventoryService;
        this.cooldownRepository = cooldownRepository;
        this.playerIdentityRepository = playerIdentityRepository;
    }

    private boolean isCoreShopItem(String code) {
        return "INSURANCE_SCROLL".equals(code)
                || "FATIGUE_REMEDY".equals(code)
                || "RUNE_OF_SWIFTNESS".equals(code)
                || "RUNE_OF_BOUNTY".equals(code)
                || "RUNE_OF_PRESENCE".equals(code)
                || "MONARCH_EXEMPTION".equals(code);
    }

    @Transactional(readOnly = true)
    public com.lifeos.economy.dto.ShopResponse listItems(UUID playerId) {
        checkShopAccess(playerId);
        List<ShopItem> allItems = itemRepository.findAll();
        
        int intelValue = getIntelValue(playerId);
        double discountPercent = Math.min(20.0, intelValue * 0.5);
        
        List<ShopItem> discountedItems = allItems.stream()
                .filter(item -> isCoreShopItem(item.getCode()))
                .map(item -> {
                    long baseCost = item.getCost();
                    long finalCost = (long) (baseCost * (1.0 - (discountPercent / 100.0)));
                    ShopItem mapped = ShopItem.builder()
                            .itemId(item.getItemId())
                            .code(item.getCode())
                            .name(item.getName())
                            .description(item.getDescription())
                            .cost(finalCost)
                            .category(item.getCategory())
                            .stockLimit(item.getStockLimit())
                            .rankRequirement(item.getRankRequirement())
                            .purchaseCooldownHours(item.getPurchaseCooldownHours())
                            .effectPayload(item.getEffectPayload())
                            .imageUrl(item.getImageUrl())
                            .build();
                    mapped.setBaseCost(baseCost);
                    return mapped;
                })
                .collect(java.util.stream.Collectors.toList());

        java.util.List<String> systemMessages = new java.util.ArrayList<>();
        if (discountPercent > 0) {
            systemMessages.add(String.format("[SYSTEM] INT stat applied %.1f%% discount.", discountPercent));
        }

        return com.lifeos.economy.dto.ShopResponse.builder()
                .items(discountedItems)
                .systemMessages(systemMessages)
                .build();
    }

    @Transactional
    public PurchaseResponse purchaseItem(UUID playerId, String itemCode) {
        checkShopAccess(playerId);

        if (!isCoreShopItem(itemCode)) {
            throw new IllegalArgumentException("Item is not purchasable: " + itemCode);
        }

        ShopItem item = itemRepository.findByCode(itemCode)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemCode));

        PlayerStateResponse state = playerStateService.getPlayerState(playerId);

        // 2. Rank Requirement Check
        if (item.getRankRequirement() != null) {
            if (state.getProgression().getRank().ordinal() < item.getRankRequirement().ordinal()) {
                throw new IllegalStateException("Insufficient Rank: " + item.getRankRequirement() + " required.");
            }
        }

        // 3. Stock Limit Check
        if (item.getStockLimit() != null) {
            long userPurchases = xactionRepository.countByPlayerIdAndItemId(playerId, item.getItemId());
            if (userPurchases >= item.getStockLimit()) {
                 throw new IllegalStateException("You have reached the purchase limit for this item.");
            }
        }

        // 4. Purchase Cooldown Check
        if (item.getPurchaseCooldownHours() != null) {
            var cooldownOpt = cooldownRepository.findByPlayerPlayerIdAndItemItemId(playerId, item.getItemId());
            if (cooldownOpt.isPresent()) {
                LocalDateTime availableAt = cooldownOpt.get().getLastPurchasedAt().plusHours(item.getPurchaseCooldownHours());
                if (LocalDateTime.now().isBefore(availableAt)) {
                    throw new IllegalStateException("Item is on cooldown. Available at: " + availableAt);
                }
            }
        }

        // 5. Deduct Gold
        int intelValue = getIntelValue(playerId);
        double discountPercent = Math.min(20.0, intelValue * 0.5);
        long discountedCost = (long) (item.getCost() * (1.0 - (discountPercent / 100.0)));

        economyService.deductGold(playerId, discountedCost, "Purchase: " + item.getName());

        // 6. Add to Inventory
        inventoryService.addItem(playerId, item.getItemId(), 1);

        // 7. Update/Create Cooldown Record
        if (item.getPurchaseCooldownHours() != null) {
            var cooldown = cooldownRepository.findByPlayerPlayerIdAndItemItemId(playerId, item.getItemId())
                    .orElse(com.lifeos.economy.domain.PurchaseCooldown.builder()
                            .player(playerIdentityRepository.getReferenceById(playerId))
                            .item(item)
                            .build());
            cooldown.setLastPurchasedAt(LocalDateTime.now());
            cooldownRepository.save(cooldown);
        }

        // 8. Record Transaction
        PurchaseTransaction xaction = PurchaseTransaction.builder()
                .playerId(playerId)
                .itemId(item.getItemId())
                .cost(discountedCost)
                .build();
        xactionRepository.save(xaction);

        log.info("Item {} purchased and added to inventory.", item.getName());

        java.util.List<String> systemMessages = new java.util.ArrayList<>();
        if (discountPercent > 0) {
            systemMessages.add(String.format("[SYSTEM] Purchased %s. INT stat applied %.1f%% discount.", item.getName(), discountPercent));
        } else {
            systemMessages.add(String.format("[SYSTEM] Purchased %s.", item.getName()));
        }

        return PurchaseResponse.builder()
                .success(true)
                .systemMessages(systemMessages)
                .build();
    }

    private void checkShopAccess(UUID playerId) {
        boolean onboardingCompleted = playerIdentityRepository.findById(playerId)
                .map(p -> p.isOnboardingCompleted())
                .orElse(false);
        if (!onboardingCompleted) {
            throw new com.lifeos.system.exception.LockedFeatureException("Complete onboarding trial quests first");
        }

        if (isShopLocked(playerId)) {
            throw new com.lifeos.system.exception.LockedFeatureException("Shop is locked due to Penalty Zone active.");
        }

        PlayerStateResponse state = playerStateService.getPlayerState(playerId);
        if (state.getProgression().getLevel() < 10) {
            throw new com.lifeos.system.exception.LockedFeatureException("ACCESS DENIED: The System Store unlocks at Level 10. Current power level is insufficient.");
        }
    }

    private boolean isShopLocked(UUID playerId) {
        PlayerStateResponse state = playerStateService.getPlayerState(playerId);
        return state.getActiveFlags().stream()
                .anyMatch(f -> f.getFlag() == StatusFlagType.PENALTY_ZONE);
    }

    private int getIntelValue(UUID playerId) {
        try {
            PlayerStateResponse state = playerStateService.getPlayerState(playerId);
            if (state == null || state.getAttributes() == null) return 0;
            return state.getAttributes().stream()
                    .filter(a -> com.lifeos.player.domain.enums.AttributeType.INT == a.getAttributeType())
                    .mapToInt(a -> (int) a.getCurrentValue())
                    .findFirst()
                    .orElse(0);
        } catch (Exception e) {
            log.warn("Could not fetch INT for player {}: {}", playerId, e.getMessage());
            return 0;
        }
    }
}
