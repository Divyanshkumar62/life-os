package com.lifeos.economy.controller;

import com.lifeos.economy.domain.UserInventory;
import com.lifeos.economy.service.ConsumableService;
import com.lifeos.economy.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/consumables")
public class ConsumableController {

    private final ConsumableService consumableService;
    private final InventoryService inventoryService;

    public ConsumableController(ConsumableService consumableService, InventoryService inventoryService) {
        this.consumableService = consumableService;
        this.inventoryService = inventoryService;
    }

    @PostMapping("/use/{itemCode}")
    public ResponseEntity<List<UserInventory>> useItem(@RequestParam UUID playerId, @PathVariable String itemCode) {
        consumableService.useConsumable(playerId, itemCode);
        return ResponseEntity.ok(inventoryService.getPlayerInventory(playerId));
    }
}
