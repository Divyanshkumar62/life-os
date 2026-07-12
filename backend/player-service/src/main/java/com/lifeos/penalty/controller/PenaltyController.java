package com.lifeos.penalty.controller;

import com.lifeos.ai.service.ConfessionResult;
import com.lifeos.ai.service.ConfessionService;
import com.lifeos.penalty.domain.PlayerJournal;
import com.lifeos.penalty.dto.ConfessionRequest;
import com.lifeos.penalty.dto.ConfessionResponse;
import com.lifeos.penalty.dto.SurvivalTaskDTO;
import com.lifeos.penalty.repository.PlayerJournalRepository;
import com.lifeos.penalty.service.PenaltyQuestService;
import com.lifeos.player.domain.PlayerTemporalState;
import com.lifeos.player.repository.PlayerTemporalStateRepository;
import com.lifeos.player.service.PlayerStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
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
    private final PenaltyQuestService penaltyQuestService;

    public PenaltyController(ConfessionService confessionService,
                             PlayerJournalRepository journalRepository,
                             PlayerTemporalStateRepository temporalStateRepository,
                             PlayerStateService playerStateService,
                             PenaltyQuestService penaltyQuestService) {
        this.confessionService = confessionService;
        this.journalRepository = journalRepository;
        this.temporalStateRepository = temporalStateRepository;
        this.playerStateService = playerStateService;
        this.penaltyQuestService = penaltyQuestService;
    }

    @PostMapping("/confess")
    public ResponseEntity<ConfessionResponse> submitConfession(
            @RequestParam UUID playerId,
            @RequestBody ConfessionRequest request) {

        log.info("Confession submission request received for player: {}", playerId);

        PlayerTemporalState temporal = temporalStateRepository.findByPlayerPlayerId(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player temporal state not found"));

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

        ConfessionResult result = confessionService.judgeConfession(playerId, request.getText());

        PlayerJournal journal = PlayerJournal.builder()
                .playerId(playerId)
                .text(request.getText())
                .accepted(result.isAccepted())
                .timestamp(now)
                .feedback(result.getFeedback())
                .build();
        journalRepository.save(journal);

        if (result.isAccepted()) {
            log.info("Confession accepted for player {}. Proceeding to survival task.", playerId);

            temporal.setFailedConfessionAttempts(0);
            temporal.setPenaltyLockoutUntil(null);
            temporalStateRepository.save(temporal);

            playerStateService.updateConsecutiveFailures(playerId, 0);

            UUID survivalTaskId = null;
            boolean requiresSurvivalTask = false;
            try {
                SurvivalTaskDTO activeTask = penaltyQuestService.getActiveTaskDTO(playerId);
                survivalTaskId = activeTask.getQuestId();
                requiresSurvivalTask = true;
            } catch (Exception e) {
                log.warn("No active penalty quest found for player {} after accepted confession", playerId);
            }

            return ResponseEntity.ok(
                    ConfessionResponse.builder()
                            .accepted(true)
                            .feedback(result.getFeedback())
                            .attemptsRemaining(3)
                            .lockoutUntil(null)
                            .survivalTaskId(survivalTaskId)
                            .requiresSurvivalTask(requiresSurvivalTask)
                            .build()
            );
        } else {
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

    @GetMapping("/active-task")
    public ResponseEntity<SurvivalTaskDTO> getActiveTask(@RequestParam UUID playerId) {
        log.info("Fetching active survival task for player: {}", playerId);
        try {
            SurvivalTaskDTO task = penaltyQuestService.getActiveTaskDTO(playerId);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/task/{questId}/progress")
    public ResponseEntity<SurvivalTaskDTO> reportProgress(
            @PathVariable UUID questId,
            @RequestParam UUID playerId,
            @RequestBody Map<String, Integer> body) {
        int unitsCompleted = body.getOrDefault("unitsCompleted", 1);
        log.info("Progress report for player {} quest {}: +{} units", playerId, questId, unitsCompleted);

        SurvivalTaskDTO dto = penaltyQuestService.reportSurvivalTaskProgress(playerId, unitsCompleted);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/task/{questId}/complete")
    public ResponseEntity<SurvivalTaskDTO> completeTask(
            @PathVariable UUID questId,
            @RequestParam UUID playerId) {
        log.info("Force-completing survival task for player {} quest {}", playerId, questId);

        SurvivalTaskDTO dto = penaltyQuestService.completeSurvivalTask(playerId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/task/{questId}/reroll")
    public ResponseEntity<SurvivalTaskDTO> rerollTask(
            @PathVariable UUID questId,
            @RequestParam UUID playerId) {
        log.info("Rerolling survival task for player {} quest {}", playerId, questId);

        SurvivalTaskDTO dto = penaltyQuestService.rerollSurvivalTask(playerId);
        return ResponseEntity.ok(dto);
    }
}
