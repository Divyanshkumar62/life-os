package com.lifeos.economy.controller;

import com.lifeos.economy.dto.LootBoxResult;
import com.lifeos.economy.service.LootBoxService;
import com.lifeos.system.exception.LockedFeatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final LootBoxService lootBoxService;
    private final com.lifeos.economy.service.InventoryService inventoryService;
    private final com.lifeos.economy.repository.ShopItemRepository shopItemRepository;

    /**
     * Opens a loot box for the given player.
     * Consumes the box item and returns the reward payload.
     */
    @PostMapping("/open-box/{boxType}")
    public ResponseEntity<LootBoxResult> openBox(@RequestParam UUID playerId, @PathVariable String boxType) {
        try {
            LootBoxResult result = lootBoxService.openBox(playerId, boxType);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (LockedFeatureException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Admin cheat endpoint to grant specific items to a player's inventory by item code.
     */
    @PostMapping("/{playerId}/grant")
    public ResponseEntity<Void> grantItem(
            @PathVariable UUID playerId,
            @RequestParam String itemCode,
            @RequestParam(defaultValue = "1") int quantity) {
        
        com.lifeos.economy.domain.ShopItem item = shopItemRepository.findByCode(itemCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found with code: " + itemCode));
        
        inventoryService.addItem(playerId, item.getItemId(), quantity);
        return ResponseEntity.ok().build();
    }
}
