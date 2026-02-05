package com.lifeos.onboarding.controller;

import com.lifeos.onboarding.dto.CalibrationRequest;
import com.lifeos.onboarding.dto.OnboardingResponse;
import com.lifeos.onboarding.dto.QuestionnaireRequest;
import com.lifeos.onboarding.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@Slf4j
public class OnboardingController {

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

    @PostMapping("/{playerId}/questionnaire")
    public ResponseEntity<OnboardingResponse> submitQuestionnaire(
            @PathVariable UUID playerId,
            @RequestBody QuestionnaireRequest request) {
        log.info("Submitting questionnaire for player: {}", playerId);
        return ResponseEntity.ok(onboardingService.submitQuestionnaire(playerId, request));
    }

    @PostMapping("/{playerId}/calibrate")
    public ResponseEntity<OnboardingResponse> calibrateAttributes(
            @PathVariable UUID playerId,
            @RequestBody CalibrationRequest request) {
        log.info("Calibrating attributes for player: {}", playerId);
        return ResponseEntity.ok(onboardingService.calibrateAttributes(playerId, request));
    }

    @GetMapping("/{playerId}/status")
    public ResponseEntity<OnboardingResponse> getStatus(@PathVariable UUID playerId) {
        return ResponseEntity.ok(onboardingService.getStatus(playerId));
    }
}
