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
}
