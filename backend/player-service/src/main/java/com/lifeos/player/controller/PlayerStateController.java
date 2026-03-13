package com.lifeos.player.controller;

import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.service.PlayerStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerStateController {

    private final PlayerStateService playerStateService;
    private final PlayerIdentityRepository identityRepository;

    @PostMapping
    public ResponseEntity<PlayerStateResponse> initializePlayer(@RequestParam String username) {
        return ResponseEntity.ok(playerStateService.initializePlayer(username));
    }

    @GetMapping("/{playerId}/state")
    public ResponseEntity<PlayerStateResponse> getPlayerState(@PathVariable UUID playerId) {
        return ResponseEntity.ok(playerStateService.getPlayerState(playerId));
    }

    @PutMapping("/{playerId}/fcm-token")
    public ResponseEntity<Void> updateFcmToken(@PathVariable UUID playerId, @RequestParam String token) {
        PlayerIdentity identity = identityRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        
        identity.setFcmToken(token);
        identityRepository.save(identity);
        
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{playerId}/notifications")
    public ResponseEntity<Void> updateNotifications(@PathVariable UUID playerId, @RequestParam boolean enabled) {
        PlayerIdentity identity = identityRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        
        identity.setNotificationsEnabled(enabled);
        identityRepository.save(identity);
        
        return ResponseEntity.ok().build();
    }
}
