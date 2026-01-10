package com.lifeos.economy.service;

import com.lifeos.economy.domain.PurchaseTransaction;
import com.lifeos.economy.domain.ShopItem;
import com.lifeos.economy.repository.PurchaseTransactionRepository;
import com.lifeos.economy.repository.ShopItemRepository;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.service.PlayerStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopItemRepository itemRepository;
    private final EconomyService economyService;
    private final PurchaseTransactionRepository xactionRepository;
    private final PlayerStateService playerStateService;

    @Transactional(readOnly = true)
    public List<ShopItem> listItems(UUID playerId) {
        if (isShopLocked(playerId)) {
            throw new IllegalStateException("Shop is locked due to Penalty Zone active.");
        }
        return itemRepository.findAll();
    }

    @Transactional
    public void purchaseItem(UUID playerId, String itemCode) {
        if (isShopLocked(playerId)) {
            throw new IllegalStateException("Shop is locked due to Penalty Zone active.");
        }

        ShopItem item = itemRepository.findByCode(itemCode)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemCode));

        // Deduct Gold (Throw if insufficient)
        economyService.deductGold(playerId, item.getCost(), "Purchase: " + item.getName());

        // Record Transaction
        PurchaseTransaction xaction = PurchaseTransaction.builder()
                .playerId(playerId)
                .itemId(item.getItemId())
                .cost(item.getCost())
                .build();
        xactionRepository.save(xaction);

        // Apply Effect
        // V1: Simple Console Log or Hook
        System.out.println("Applying effect for " + item.getName() + ": " + item.getEffectPayload());
        
        // TODO: Implement dedicated Effect Service or Inventory
    }

    private boolean isShopLocked(UUID playerId) {
        PlayerStateResponse state = playerStateService.getPlayerState(playerId);
        return state.getActiveFlags().stream()
                .anyMatch(f -> f.getFlag() == StatusFlagType.PENALTY_ZONE);
    }
}
