package com.lifeos.onboarding.controller;

import com.lifeos.onboarding.dto.CalibrationRequest;
import com.lifeos.onboarding.dto.OnboardingResponse;
import com.lifeos.onboarding.dto.QuestionnaireRequest;
import com.lifeos.onboarding.dto.AwakeningPenaltyDTO;
import com.lifeos.onboarding.dto.AwakeningPenaltyResultDTO;
import com.lifeos.onboarding.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow frontend access
public class OnboardingController {

    private static final Logger log = LoggerFactory.getLogger(OnboardingController.class);

    private final OnboardingService onboardingService;

    @PostMapping("/start")
    public ResponseEntity<OnboardingResponse> startOnboarding(@RequestParam String username) {
        log.info("Request to start onboarding for: {}", username);
        return ResponseEntity.ok(onboardingService.startOnboarding(username));
    }


    @PostMapping("/{playerId}/awakening")
    public ResponseEntity<OnboardingResponse> submitAwakening(
            @PathVariable UUID playerId,
            @RequestBody QuestionnaireRequest request) {
        log.info("Submitting awakening (5-Question) for player: {}", playerId);
        return ResponseEntity.ok(onboardingService.submitAwakening(playerId, request));
    }

    @PostMapping("/{playerId}/trial/complete")
    public ResponseEntity<OnboardingResponse> completeTrial(@PathVariable UUID playerId) {
        log.info("Completing onboarding trial for player: {}", playerId);
        return ResponseEntity.ok(onboardingService.completeTrial(playerId));
    }

    // Calibrate endpoint removed (merged into Awakening)

    @GetMapping("/{playerId}/status")
    public ResponseEntity<OnboardingResponse> getStatus(@PathVariable UUID playerId) {
        return ResponseEntity.ok(onboardingService.getStatus(playerId));
    }

    @PostMapping("/{playerId}/trial/fail")
    public ResponseEntity<AwakeningPenaltyDTO> failTrial(@PathVariable UUID playerId) {
        log.info("Trial failed for player: {}", playerId);
        return ResponseEntity.ok(onboardingService.failTrial(playerId));
    }

    @GetMapping("/{playerId}/penalty/status")
    public ResponseEntity<AwakeningPenaltyDTO> getPenaltyStatus(@PathVariable UUID playerId) {
        log.info("Retrieving penalty status for player: {}", playerId);
        return ResponseEntity.ok(onboardingService.getPenaltyStatus(playerId));
    }

    @PostMapping("/{playerId}/penalty/complete")
    public ResponseEntity<AwakeningPenaltyResultDTO> completePenalty(@PathVariable UUID playerId) {
        log.info("Completing awakening penalty for player: {}", playerId);
        return ResponseEntity.ok(onboardingService.completePenalty(playerId));
    }

    @PostMapping("/{playerId}/penalty/fail")
    public ResponseEntity<AwakeningPenaltyResultDTO> failPenalty(@PathVariable UUID playerId) {
        log.info("Failing awakening penalty / account wipe for player: {}", playerId);
        return ResponseEntity.ok(onboardingService.failPenalty(playerId));
    }
}
