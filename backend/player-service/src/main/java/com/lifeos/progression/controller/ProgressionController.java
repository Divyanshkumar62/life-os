package com.lifeos.progression.controller;

import com.lifeos.progression.domain.RankExamAttempt;
import com.lifeos.progression.service.ProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/progression")
@RequiredArgsConstructor
@Slf4j
public class ProgressionController {

    private final ProgressionService progressionService;

    @GetMapping("/{playerId}/check-gate")
    public ResponseEntity<Boolean> checkRankGate(@PathVariable UUID playerId) {
        log.info("Checking rank gate for player: {}", playerId);
        return ResponseEntity.ok(progressionService.checkRankGate(playerId));
    }

    @GetMapping("/{playerId}/can-promote")
    public ResponseEntity<Boolean> canRequestPromotion(@PathVariable UUID playerId) {
        log.info("Checking promotion eligibility for player: {}", playerId);
        return ResponseEntity.ok(progressionService.canRequestPromotion(playerId));
    }

    @PostMapping("/{playerId}/request-promotion")
    public ResponseEntity<RankExamAttempt> requestPromotion(@PathVariable UUID playerId) {
        log.info("Requesting promotion for player: {}", playerId);
        return ResponseEntity.ok(progressionService.requestPromotion(playerId));
    }

    @PostMapping("/{playerId}/process-outcome")
    public ResponseEntity<Void> processOutcome(
            @PathVariable UUID playerId,
            @RequestParam boolean success) {
        log.info("Processing promotion outcome for player: {}. Success: {}", playerId, success);
        progressionService.processPromotionOutcome(playerId, success);
        return ResponseEntity.ok().build();
    }
}
