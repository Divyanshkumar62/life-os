package com.lifeos.economy.controller;

import com.lifeos.economy.domain.ShopItem;
import com.lifeos.economy.domain.UserInventory;
import com.lifeos.economy.service.InventoryService;
import com.lifeos.economy.service.ShopService;
import com.lifeos.player.repository.PlayerIdentityRepository;
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
    private final PlayerIdentityRepository identityRepository;

    private void checkOnboardingCompleted(UUID playerId) {
        boolean onboardingCompleted = identityRepository.findById(playerId)
                .map(p -> p.isOnboardingCompleted())
                .orElse(false);
        if (!onboardingCompleted) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Complete onboarding trial quests first");
        }
    }

    @GetMapping("/items")
    public ResponseEntity<List<ShopItem>> listItems(@RequestParam UUID playerId) {
        checkOnboardingCompleted(playerId);
        return ResponseEntity.ok(shopService.listItems(playerId));
    }

    @PostMapping("/purchase/{itemCode}")
    public ResponseEntity<Void> purchaseItem(@RequestParam UUID playerId, @PathVariable String itemCode) {
        checkOnboardingCompleted(playerId);
        shopService.purchaseItem(playerId, itemCode);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<UserInventory>> getInventory(@RequestParam UUID playerId) {
        checkOnboardingCompleted(playerId);
        return ResponseEntity.ok(inventoryService.getPlayerInventory(playerId));
    }
}
