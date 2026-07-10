package com.lifeos.player.controller;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.progression.domain.UserBossKey;
import com.lifeos.progression.repository.UserBossKeyRepository;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.core.entity.PlayerState;
import com.lifeos.core.entity.TemporalModifier;
import com.lifeos.core.repository.PlayerStateRepository;
import com.lifeos.core.repository.TemporalModifierRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final PlayerStateService playerStateService;
    private final PenaltyService penaltyService;
    private final UserBossKeyRepository bossKeyRepository;
    private final PlayerIdentityRepository playerIdentityRepository;
    private final PlayerStateRepository corePlayerStateRepository;
    private final TemporalModifierRepository temporalModifierRepository;

    public AdminController(PlayerStateService playerStateService, PenaltyService penaltyService, 
                          UserBossKeyRepository bossKeyRepository, PlayerIdentityRepository playerIdentityRepository,
                          PlayerStateRepository corePlayerStateRepository, TemporalModifierRepository temporalModifierRepository) {
        this.playerStateService = playerStateService;
        this.penaltyService = penaltyService;
        this.bossKeyRepository = bossKeyRepository;
        this.playerIdentityRepository = playerIdentityRepository;
        this.corePlayerStateRepository = corePlayerStateRepository;
        this.temporalModifierRepository = temporalModifierRepository;
    }

    @PostMapping("/players/{playerId}/level")
    public ResponseEntity<Void> setLevel(@PathVariable UUID playerId, @RequestParam int level) {
        log.info("Admin: Setting level to {} for player: {}", level, playerId);
        playerStateService.setLevel(playerId, level);
        return ResponseEntity.ok().build();
    }

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
        
        // Also create core TemporalModifier
        PlayerState corePlayer = corePlayerStateRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Core PlayerState not found"));
                
        // Ensure no active PENALTY_ZONE modifier exists to prevent duplicates
        boolean hasActivePenalty = temporalModifierRepository.findByPlayerPlayerIdAndIsActive(playerId, true)
                .stream()
                .anyMatch(m -> "PENALTY_ZONE".equals(m.getModifierType()));
                
        if (!hasActivePenalty) {
            TemporalModifier modifier = TemporalModifier.builder()
                    .player(corePlayer)
                    .modifierType("PENALTY_ZONE")
                    .startsAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusYears(100))
                    .isActive(true)
                    .build();
            temporalModifierRepository.save(modifier);
        }
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/players/{playerId}/penalty/exit")
    public ResponseEntity<Void> exitPenaltyZone(@PathVariable UUID playerId) {
        log.info("Admin: Forcing player {} to exit penalty zone", playerId);
        penaltyService.exitPenaltyZone(playerId);
        
        // Also deactivate core TemporalModifier
        temporalModifierRepository.findByPlayerPlayerIdAndIsActive(playerId, true)
                .stream()
                .filter(m -> "PENALTY_ZONE".equals(m.getModifierType()))
                .forEach(m -> {
                    m.setActive(false);
                    temporalModifierRepository.save(m);
                });
                
        return ResponseEntity.ok().build();
    }
}
