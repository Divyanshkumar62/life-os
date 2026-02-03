package com.lifeos.player.controller;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.progression.domain.UserBossKey;
import com.lifeos.progression.repository.UserBossKeyRepository;
import com.lifeos.player.repository.PlayerIdentityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PlayerStateService playerStateService;
    private final PenaltyService penaltyService;
    private final UserBossKeyRepository bossKeyRepository;
    private final PlayerIdentityRepository identityRepository;

    @PostMapping("/players/{playerId}/add-xp")
    public ResponseEntity<Void> addXp(@PathVariable UUID playerId, @RequestParam long amount) {
        playerStateService.addXp(playerId, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/players/{playerId}/update-attribute")
    public ResponseEntity<Void> updateAttribute(
            @PathVariable UUID playerId,
            @RequestParam AttributeType type,
            @RequestParam double valueChange) {
        playerStateService.updateAttribute(playerId, type, valueChange);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/players/{playerId}/boss-keys")
    public ResponseEntity<Void> grantBossKey(
            @PathVariable UUID playerId,
            @RequestParam PlayerRank rank,
            @RequestParam int count) {
        
        var identity = identityRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        var bossKey = bossKeyRepository.findByPlayerPlayerIdAndRank(playerId, rank)
                .orElse(com.lifeos.progression.domain.UserBossKey.builder()
                        .player(identity)
                        .rank(rank)
                        .keyCount(0)
                        .build());
        
        bossKey.setKeyCount(bossKey.getKeyCount() + count);
        bossKeyRepository.save(bossKey);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/players/{playerId}/penalty/enter")
    public ResponseEntity<Void> enterPenaltyZone(@PathVariable UUID playerId, @RequestParam String reason) {
        penaltyService.enterPenaltyZone(playerId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/players/{playerId}/penalty/exit")
    public ResponseEntity<Void> exitPenaltyZone(@PathVariable UUID playerId) {
        penaltyService.exitPenaltyZone(playerId);
        return ResponseEntity.ok().build();
    }
}
