package com.lifeos.player.controller;

import com.lifeos.player.dto.PlayerStateResponse;
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

    @PostMapping
    public ResponseEntity<PlayerStateResponse> initializePlayer(@RequestParam String username) {
        return ResponseEntity.ok(playerStateService.initializePlayer(username));
    }

    @GetMapping("/{playerId}/state")
    public ResponseEntity<PlayerStateResponse> getPlayerState(@PathVariable UUID playerId) {
        return ResponseEntity.ok(playerStateService.getPlayerState(playerId));
    }
}
