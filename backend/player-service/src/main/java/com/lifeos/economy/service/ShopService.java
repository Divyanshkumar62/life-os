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
import java.util.List;
import java.util.UUID;

@Service
public class ShopService {
n    private static final Logger log = LoggerFactory.getLogger(ShopService.class);

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

    @Transactional(readOnly = true)
    public List<ShopItem> listItems(UUID playerId) {
        if (isShopLocked(playerId)) {
            throw new IllegalStateException("Shop is locked due to Penalty Zone active.");
        }
        // TODO: Could filter by Rank here if desired, but "window shopping" is usually allowed
        return itemRepository.findAll();
    }

    @Transactional
    public void purchaseItem(UUID playerId, String itemCode) {
        // 1. Penalty Zone Check
        if (isShopLocked(playerId)) {
            throw new IllegalStateException("Shop is locked due to Penalty Zone active.");
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

        // 3. Stock Limit Check (Global or Per-User depending on requirement? Plan said "Per-User purchase limit" separate from Cooldown?)
        // The plan's "User Decisions" clarified: "Per-User purchase limits with time gates".
        // "Stock Limits" in plan referred to "Artificial Scarcity" (e.g. only 1 per month).
        // Let's implement Stock Limit as Global limit if field exists, or use Cooldown for time-gating.
        // Wait, plan implementation section: "Stock Limit Check ... global purchases ... countByItemId".
        // But user decision: "Stock Limits: Per-User ... It doesn't run out because other Hunters bought it".
        // SO STOCK LIMIT field in ShopItem should be interpreted as LIFETIME PER USER LIMIT?
        // Or should I use `purchase_cooldown_hours` for time gating, and `stock_limit` for absolute lifetime limit?
        // User said: "Cosmetics (Skins): Limit 1 (Once you own it, you own it)".
        // So `stock_limit` = 1 means "Max 1 per user lifetime".
        // Let's check user inventory for this item count? OR transaction count?
        // Inventory quantity tells us how many they HAVE. Transaction count tells us how many they BOUGHT.
        // If it's a consumable, they might have 0 in inventory but bought 100.
        // If it's a Cosmetic, they have 1.
        // Let's interpret `stockLimit` as "Max Quantity Owned + Consumed"? Or just "Max Owned"?
        // Usually "Limit 1" for cosmetic means "You can't buy it if you already have it".
        // Let's check Inventory for Stock Limit if it is defined.
        
        if (item.getStockLimit() != null) {
            // Check how many user has BOUGHT total? Or has currently?
            // "Limit 1" cosmetic -> If I have it, I can't buy it.
            // If I bought a potion limit 1/week, that's cooldown.
            // If I bought a "Starter Pack" limit 1 lifetime... 
            // Let's count total transactions for this user for this item.
            long userPurchases = xactionRepository.countByPlayerIdAndItemId(playerId, item.getItemId());
            if (userPurchases >= item.getStockLimit()) {
                 throw new IllegalStateException("You have reached the purchase limit for this item.");
            }
        }

        // 4. Purchase Cooldown Check (Time-Gating)
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
        economyService.deductGold(playerId, item.getCost(), "Purchase: " + item.getName());

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
                .cost(item.getCost())
                .build();
        xactionRepository.save(xaction);

        // 9. Apply Immediate Effects
        // Effect handling moved strictly to ConsumableService to enable inventory staging.

        log.info("Item {} purchased and added to inventory.", item.getName());
    }

    private boolean isShopLocked(UUID playerId) {
        PlayerStateResponse state = playerStateService.getPlayerState(playerId);
        return state.getActiveFlags().stream()
                .anyMatch(f -> f.getFlag() == StatusFlagType.PENALTY_ZONE);
    }
}
