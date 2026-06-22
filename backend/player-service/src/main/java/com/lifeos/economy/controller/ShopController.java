package com.lifeos.economy.controller;

import com.lifeos.economy.domain.ShopItem;
import com.lifeos.economy.domain.UserInventory;
import com.lifeos.economy.service.InventoryService;
import com.lifeos.economy.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final InventoryService inventoryService;

    @GetMapping("/items")
    public ResponseEntity<com.lifeos.economy.dto.ShopResponse> listItems(@RequestParam UUID playerId) {
        try {
            return ResponseEntity.ok(shopService.listItems(playerId));
        } catch (com.lifeos.system.exception.LockedFeatureException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @PostMapping("/purchase/{itemCode}")
    public ResponseEntity<com.lifeos.economy.dto.PurchaseResponse> purchaseItem(@RequestParam UUID playerId, @PathVariable String itemCode) {
        try {
            com.lifeos.economy.dto.PurchaseResponse response = shopService.purchaseItem(playerId, itemCode);
            return ResponseEntity.ok(response);
        } catch (com.lifeos.system.exception.LockedFeatureException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<UserInventory>> getInventory(@RequestParam UUID playerId) {
        try {
            return ResponseEntity.ok(inventoryService.getPlayerInventory(playerId));
        } catch (com.lifeos.system.exception.LockedFeatureException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }
}

