package com.lifeos.player.controller;

import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.dto.StatAllocationRequest;
import com.lifeos.player.service.PlayerStateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/player")
public class PlayerController {

    private final PlayerStateService playerStateService;

    public PlayerController(PlayerStateService playerStateService) {
        this.playerStateService = playerStateService;
    }

    @PostMapping("/stats/allocate")
    public ResponseEntity<PlayerStateResponse> allocateStat(
            @RequestParam UUID playerId, 
            @RequestBody StatAllocationRequest request) {
        
        playerStateService.allocateFreeStatPoint(playerId, request.getStat(), request.getAmount());
        
        // Return updated state
        return ResponseEntity.ok(playerStateService.getPlayerState(playerId));
    }
}
