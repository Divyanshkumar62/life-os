package com.lifeos.economy.service;

import com.lifeos.economy.domain.ShopItem;
import com.lifeos.economy.domain.UserInventory;
import com.lifeos.economy.repository.ShopItemRepository;
import com.lifeos.economy.repository.UserInventoryRepository;
import com.lifeos.player.repository.PlayerIdentityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private final UserInventoryRepository inventoryRepository;
    private final ShopItemRepository shopItemRepository;
    private final PlayerIdentityRepository playerIdentityRepository;

    public InventoryService(UserInventoryRepository inventoryRepository, ShopItemRepository shopItemRepository,
                          PlayerIdentityRepository playerIdentityRepository) {
        this.inventoryRepository = inventoryRepository;
        this.shopItemRepository = shopItemRepository;
        this.playerIdentityRepository = playerIdentityRepository;
    }

    /**
     * Adds an item to the user's inventory.
     * If the item already exists, increments the quantity.
     */
    @Transactional
    public void addItem(UUID playerId, UUID itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        UserInventory inventoryItem = inventoryRepository.findByPlayerPlayerIdAndItemItemId(playerId, itemId)
                .orElseGet(() -> createNewEntry(playerId, itemId));

        inventoryItem.setQuantity(inventoryItem.getQuantity() + quantity);
        inventoryRepository.save(inventoryItem);
    }

    /**
     * Consumes (removes) an item from the user's inventory.
     * Decrements quantity. If quantity reaches 0, the row is deleted.
     */
    @Transactional
    public void consumeItem(UUID playerId, UUID itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        UserInventory inventoryItem = inventoryRepository.findByPlayerPlayerIdAndItemItemId(playerId, itemId)
                .orElseThrow(() -> new IllegalStateException("Item not found in inventory"));

        if (inventoryItem.getQuantity() < quantity) {
            throw new IllegalStateException("Insufficient quantity. Owned: " + inventoryItem.getQuantity() + ", Required: " + quantity);
        }

        int newQuantity = inventoryItem.getQuantity() - quantity;
        if (newQuantity == 0) {
            inventoryRepository.delete(inventoryItem);
        } else {
            inventoryItem.setQuantity(newQuantity);
            inventoryRepository.save(inventoryItem);
        }
    }

    /**
     * Retrieves all items in the user's inventory.
     */
    @Transactional(readOnly = true)
    public List<UserInventory> getPlayerInventory(UUID playerId) {
        return inventoryRepository.findByPlayerPlayerId(playerId);
    }

    /**
     * Checks if a user has a specific item (by code).
     */
    @Transactional(readOnly = true)
    public boolean hasItem(UUID playerId, String itemCode) {
        return inventoryRepository.findByPlayerIdAndItemCode(playerId, itemCode).isPresent();
    }
    
    /**
     * Checks if a user has a specific item (by ID) with minimum quantity.
     */
    @Transactional(readOnly = true)
    public boolean hasItem(UUID playerId, UUID itemId, int minQuantity) {
        return inventoryRepository.findByPlayerPlayerIdAndItemItemId(playerId, itemId)
                .map(item -> item.getQuantity() >= minQuantity)
                .orElse(false);
    }

    private UserInventory createNewEntry(UUID playerId, UUID itemId) {
        var player = playerIdentityRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
        
        ShopItem item = shopItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        // Use builder directly
        return UserInventory.builder()
                .player(player)
                .item(item)
                .quantity(0) // Will be incremented by caller
                .build();
    }
}
