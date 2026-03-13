package com.lifeos.onboarding.controller;

import com.lifeos.onboarding.dto.CalibrationRequest;
import com.lifeos.onboarding.dto.OnboardingResponse;
import com.lifeos.onboarding.dto.QuestionnaireRequest;
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

    @PostMapping("/{playerId}/trial/complete")
    public ResponseEntity<OnboardingResponse> completeTrial(@PathVariable UUID playerId) {
        log.info("Request to complete trial for player: {}", playerId);
        return ResponseEntity.ok(onboardingService.completeTrialQuest(playerId));
    }

    @PostMapping("/{playerId}/awakening")
    public ResponseEntity<OnboardingResponse> submitAwakening(
            @PathVariable UUID playerId,
            @RequestBody QuestionnaireRequest request) {
        log.info("Submitting awakening (5-Question) for player: {}", playerId);
        return ResponseEntity.ok(onboardingService.submitAwakening(playerId, request));
    }

    // Calibrate endpoint removed (merged into Awakening)

    @GetMapping("/{playerId}/status")
    public ResponseEntity<OnboardingResponse> getStatus(@PathVariable UUID playerId) {
        return ResponseEntity.ok(onboardingService.getStatus(playerId));
    }
}
