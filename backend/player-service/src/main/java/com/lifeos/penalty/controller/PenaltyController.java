package com.lifeos.penalty.controller;

import com.lifeos.ai.service.ConfessionResult;
import com.lifeos.ai.service.ConfessionService;
import com.lifeos.penalty.domain.PlayerJournal;
import com.lifeos.penalty.dto.ConfessionRequest;
import com.lifeos.penalty.dto.ConfessionResponse;
import com.lifeos.penalty.repository.PlayerJournalRepository;
import com.lifeos.player.domain.PlayerTemporalState;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.repository.PlayerTemporalStateRepository;
import com.lifeos.player.service.PlayerStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/penalty")
@CrossOrigin(origins = "*")
public class PenaltyController {

    private static final Logger log = LoggerFactory.getLogger(PenaltyController.class);

    private final ConfessionService confessionService;
    private final PlayerJournalRepository journalRepository;
    private final PlayerTemporalStateRepository temporalStateRepository;
    private final PlayerStateService playerStateService;

    public PenaltyController(ConfessionService confessionService,
                             PlayerJournalRepository journalRepository,
                             PlayerTemporalStateRepository temporalStateRepository,
                             PlayerStateService playerStateService) {
        this.confessionService = confessionService;
        this.journalRepository = journalRepository;
        this.temporalStateRepository = temporalStateRepository;
        this.playerStateService = playerStateService;
    }

    @PostMapping("/confess")
    public ResponseEntity<ConfessionResponse> submitConfession(
            @RequestParam UUID playerId,
            @RequestBody ConfessionRequest request) {
        
        log.info("Confession submission request received for player: {}", playerId);

        // 1. Fetch temporal state
        PlayerTemporalState temporal = temporalStateRepository.findByPlayerPlayerId(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player temporal state not found"));

        // 2. Check Lockout State
        LocalDateTime now = LocalDateTime.now();
        if (temporal.getPenaltyLockoutUntil() != null && temporal.getPenaltyLockoutUntil().isAfter(now)) {
            log.warn("Player {} is under Penalty lockout until {}", playerId, temporal.getPenaltyLockoutUntil());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ConfessionResponse.builder()
                            .accepted(false)
                            .feedback("You are currently locked out of submission. Suffer the penalty.")
                            .attemptsRemaining(0)
                            .lockoutUntil(temporal.getPenaltyLockoutUntil())
                            .build()
            );
        }

        // 3. Judge the Confession using Gemini/Fallback
        ConfessionResult result = confessionService.judgeConfession(playerId, request.getText());

        // 4. Log permanently to PlayerJournal
        PlayerJournal journal = PlayerJournal.builder()
                .playerId(playerId)
                .text(request.getText())
                .accepted(result.isAccepted())
                .timestamp(now)
                .build();
        journalRepository.save(journal);

        // 5. Strike Handling & Flag clearing logic
        if (result.isAccepted()) {
            log.info("Confession accepted for player {}. Clearing penalty state.", playerId);
            
            // Clear flag
            playerStateService.removeStatusFlag(playerId, StatusFlagType.PENALTY_ZONE);
            
            // Reset attempts
            temporal.setFailedConfessionAttempts(0);
            temporal.setPenaltyLockoutUntil(null);
            temporalStateRepository.save(temporal);

            // Reset failures counter
            playerStateService.updateConsecutiveFailures(playerId, 0);

            return ResponseEntity.ok(
                    ConfessionResponse.builder()
                            .accepted(true)
                            .feedback(result.getFeedback())
                            .attemptsRemaining(3)
                            .lockoutUntil(null)
                            .build()
            );
        } else {
            // Increment failed attempts
            int attempts = temporal.getFailedConfessionAttempts() + 1;
            temporal.setFailedConfessionAttempts(attempts);
            
            LocalDateTime lockoutTime = null;
            if (attempts >= 3) {
                lockoutTime = now.plusHours(4);
                temporal.setPenaltyLockoutUntil(lockoutTime);
                log.warn("Player {} has reached 3 failed confession attempts. Setting 4-hour lockout.", playerId);
            }
            temporalStateRepository.save(temporal);

            int chancesRemaining = Math.max(0, 3 - attempts);
            
            return ResponseEntity.ok(
                    ConfessionResponse.builder()
                            .accepted(false)
                            .feedback(result.getFeedback())
                            .attemptsRemaining(chancesRemaining)
                            .lockoutUntil(lockoutTime)
                            .build()
            );
        }
    }
}
