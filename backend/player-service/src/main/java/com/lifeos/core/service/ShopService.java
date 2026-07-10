package com.lifeos.core.service;

import com.lifeos.core.dto.ShopPurchaseRequest;
import com.lifeos.core.dto.ShopPurchaseResponse;
import com.lifeos.core.entity.PlayerState;
import com.lifeos.core.entity.TemporalModifier;
import com.lifeos.core.repository.PlayerStateRepository;
import com.lifeos.core.repository.TemporalModifierRepository;
import com.lifeos.economy.domain.ShopItem;
import com.lifeos.economy.repository.ShopItemRepository;
import com.lifeos.economy.service.InventoryService;
import com.lifeos.player.domain.enums.PlayerRank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("coreShopService")
@RequiredArgsConstructor
public class ShopService {

    private final PlayerStateRepository playerStateRepository;
    private final TemporalModifierRepository temporalModifierRepository;
    private final ShopItemRepository shopItemRepository;
    private final InventoryService inventoryService;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ShopPurchaseResponse processPurchase(ShopPurchaseRequest request) {
        // Pessimistic Write Lock player state
        PlayerState player = playerStateRepository.findAndLockById(request.getPlayerId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + request.getPlayerId()));

        // 1. Level Restriction: Level >= 10
        if (player.getLevel() < 10) {
            throw new IllegalStateException("Shop is locked. Requires Level 10. Current Level: " + player.getLevel());
        }

        // 2. Lockout Check: Query temporal_modifier for active records of type PENALTY_ZONE or EVENT_FROZEN
        List<TemporalModifier> activeModifiers = temporalModifierRepository
                .findByPlayerPlayerIdAndIsActive(player.getPlayerId(), true);

        boolean isLockedOut = activeModifiers.stream()
                .anyMatch(m -> "PENALTY_ZONE".equals(m.getModifierType()) || "EVENT_FROZEN".equals(m.getModifierType()));
        if (isLockedOut) {
            throw new IllegalStateException("Shop access is blocked while in the Penalty Zone or during evaluation events.");
        }

        // 3. Debt Check: Validate player has no gold debt
        if (player.getGoldDebt() > 0) {
            throw new IllegalStateException("Shop is locked. You must clear your gold debt first.");
        }

        // Fetch ShopItem
        ShopItem item = shopItemRepository.findByCode(request.getItemCode())
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + request.getItemCode()));

        // 4. Rank Lock Verification: Check player rank >= item rank requirement
        PlayerRank playerRankEnum = PlayerRank.valueOf(player.getPlayerRank());
        PlayerRank itemRankEnum = item.getRankRequirement();
        if (itemRankEnum != null && playerRankEnum.ordinal() < itemRankEnum.ordinal()) {
            throw new IllegalStateException("Rank requirement not met. Required: " + itemRankEnum + ", Current: " + playerRankEnum);
        }

        // 5. Inventory Cap: For INSURANCE_SCROLL or HOURGLASS_OF_THE_MONARCH, check cap
        String itemCode = item.getCode();
        if (("INSURANCE_SCROLL".equals(itemCode) || "HOURGLASS_OF_THE_MONARCH".equals(itemCode))
                && inventoryService.hasItem(player.getPlayerId(), itemCode)) {
            throw new IllegalStateException("Inventory cap reached. You can only hold one " + itemCode + " at a time.");
        }

        long baseCost = item.getCost();
        long minPrice = (long) Math.ceil(baseCost / 2.0); // Capped at 50% discount (meaning min price is 50% of base cost)
        long calculatedPrice = (long) (baseCost * Math.pow(0.95, (double) player.getStatInt() / 10));
        long finalUnitPrice = Math.max(calculatedPrice, minPrice);
        long totalPrice = finalUnitPrice * request.getQuantity();

        // 7. Funds Check: Verify balance
        if (player.getGoldBalance() < totalPrice) {
            throw new IllegalStateException("Insufficient gold. Required: " + totalPrice + ", Available: " + player.getGoldBalance());
        }

        // Process purchase: Deduct gold, save player, add item to inventory
        player.setGoldBalance(player.getGoldBalance() - totalPrice);
        playerStateRepository.save(player);

        inventoryService.addItem(player.getPlayerId(), item.getItemId(), request.getQuantity());

        return ShopPurchaseResponse.builder()
                .playerId(player.getPlayerId())
                .itemCode(item.getCode())
                .quantity(request.getQuantity())
                .goldSpent(totalPrice)
                .remainingGold(player.getGoldBalance())
                .build();
    }
}
