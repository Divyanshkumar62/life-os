package com.lifeos.player.controller;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.progression.domain.UserBossKey;
import com.lifeos.progression.repository.UserBossKeyRepository;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.repository.PlayerIdentityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final PlayerStateService playerStateService;
    private final PenaltyService penaltyService;
    private final UserBossKeyRepository bossKeyRepository;
    private final PlayerIdentityRepository playerIdentityRepository;

    @PostMapping("/players/{playerId}/add-xp")
    public ResponseEntity<Void> addXp(@PathVariable UUID playerId, @RequestParam long amount) {
        log.info("Admin: Adding {} XP to player: {}", amount, playerId);
        playerStateService.addXp(playerId, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/players/{playerId}/update-attribute")
    public ResponseEntity<Void> updateAttribute(
            @PathVariable UUID playerId,
            @RequestParam AttributeType type,
            @RequestParam double valueChange) {
        log.info("Admin: Updating attribute {} for player: {} by {}", type, playerId, valueChange);
        playerStateService.updateAttribute(playerId, type, valueChange);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/players/{playerId}/boss-keys")
    public ResponseEntity<Void> grantBossKey(
            @PathVariable UUID playerId,
            @RequestParam PlayerRank rank,
            @RequestParam int count) {
        log.info("Admin: Granting {} boss keys (Rank {}) to player: {}", count, rank, playerId);
        PlayerIdentity player = playerIdentityRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        UserBossKey bossKey = bossKeyRepository.findByPlayerPlayerIdAndRank(playerId, rank)
                .orElse(UserBossKey.builder()
                        .player(player)
                        .rank(rank)
                        .keyCount(0)
                        .build());
        
        bossKey.setKeyCount(bossKey.getKeyCount() + count);
        bossKeyRepository.save(bossKey);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/players/{playerId}/penalty/enter")
    public ResponseEntity<Void> enterPenaltyZone(@PathVariable UUID playerId, @RequestParam String reason) {
        log.warn("Admin: Forcing player {} into penalty zone. Reason: {}", playerId, reason);
        penaltyService.enterPenaltyZone(playerId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/players/{playerId}/penalty/exit")
    public ResponseEntity<Void> exitPenaltyZone(@PathVariable UUID playerId) {
        log.info("Admin: Forcing player {} to exit penalty zone", playerId);
        penaltyService.exitPenaltyZone(playerId);
        return ResponseEntity.ok().build();
    }
}
