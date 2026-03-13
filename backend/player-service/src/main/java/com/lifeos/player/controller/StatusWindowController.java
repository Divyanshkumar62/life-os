package com.lifeos.player.controller;

import com.lifeos.player.dto.StatusWindowResponse;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.service.StatusWindowAggregatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatusWindowController {

    private final StatusWindowAggregatorService statusWindowAggregatorService;
    private final PlayerIdentityRepository identityRepository;

    private void checkOnboardingCompleted(UUID playerId) {
        boolean onboardingCompleted = identityRepository.findById(playerId)
                .map(p -> p.isOnboardingCompleted())
                .orElse(false);
        if (!onboardingCompleted) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Complete onboarding trial quests first");
        }
    }

    @GetMapping("/status-window/{playerId}")
    public ResponseEntity<StatusWindowResponse> getStatusWindow(@PathVariable UUID playerId) {
        checkOnboardingCompleted(playerId);
        log.debug("Fetching aggregated status window for player: {}", playerId);
        StatusWindowResponse response = statusWindowAggregatorService.buildStatusWindow(playerId);
        return ResponseEntity.ok(response);
    }
}
