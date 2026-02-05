package com.lifeos.onboarding.service;

import com.lifeos.onboarding.domain.OnboardingProgress;
import com.lifeos.onboarding.domain.OnboardingStage;
import com.lifeos.onboarding.domain.PlayerProfile;
import com.lifeos.onboarding.dto.CalibrationRequest;
import com.lifeos.onboarding.dto.OnboardingResponse;
import com.lifeos.onboarding.dto.QuestionnaireRequest;
import com.lifeos.onboarding.repository.OnboardingProgressRepository;
import com.lifeos.onboarding.repository.PlayerProfileRepository;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.dto.QuestRequest;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.quest.service.QuestLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private final PlayerStateService playerStateService;
    private final OnboardingProgressRepository onboardingRepository;
    private final PlayerProfileRepository profileRepository;
    private final PlayerIdentityRepository identityRepository;
    private final QuestLifecycleService questService;
    private final QuestRepository questRepository;
    private final TrialQuestGenerator trialQuestGenerator;

    @Transactional
    public OnboardingResponse startOnboarding(String username) {
        log.info("Starting onboarding for username: {}", username);
        
        // 1. Initialize Player Identity & Basic State
        var stateResponse = playerStateService.initializePlayer(username);
        UUID playerId = stateResponse.getIdentity().getPlayerId();
        PlayerIdentity player = identityRepository.findById(playerId)
                .orElseThrow(() -> new IllegalStateException("Player created but not found"));

        // 2. Generate Trial Quest
        QuestRequest trialReq = trialQuestGenerator.generateTrialQuest(playerId);
        Quest quest = questService.assignQuest(trialReq);

        // 3. Create Onboarding Progress
        OnboardingProgress progress = OnboardingProgress.builder()
                .player(player)
                .currentStage(OnboardingStage.TRIAL_QUEST)
                .trialQuestId(quest.getQuestId())
                .trialCompleted(false)
                .build();
        
        onboardingRepository.save(progress);

        return OnboardingResponse.builder()
                .playerId(playerId)
                .currentStage(OnboardingStage.TRIAL_QUEST)
                .trialQuest(quest)
                .message("System Qualification Initiated. Complete the Trial Quest to unlock full access.")
                .build();
    }

    @Transactional
    public OnboardingResponse completeTrialQuest(UUID playerId) {
        OnboardingProgress progress = getProgress_OrThrow(playerId);
        
        if (progress.getCurrentStage() != OnboardingStage.TRIAL_QUEST) {
            return buildResponse(progress, "Trial already completed or skipped.");
        }

        Quest trialQuest = questRepository.findById(progress.getTrialQuestId())
                .orElseThrow(() -> new IllegalArgumentException("Trial quest not found"));

        if (trialQuest.getState() != QuestState.COMPLETED) {
            throw new IllegalStateException("Trial quest is not completed yet. You must complete the quest actions first.");
        }

        progress.setTrialCompleted(true);
        progress.setCurrentStage(OnboardingStage.QUESTIONNAIRE);
        onboardingRepository.save(progress);

        return buildResponse(progress, "Trial Passed. Proceed to Player Profiling.");
    }

    @Transactional
    public OnboardingResponse submitQuestionnaire(UUID playerId, QuestionnaireRequest request) {
        OnboardingProgress progress = getProgress_OrThrow(playerId);
        
        if (progress.getCurrentStage() != OnboardingStage.QUESTIONNAIRE) {
            throw new IllegalStateException("Invalid stage for questionnaire submission.");
        }

        // Save Profile
        PlayerProfile profile = PlayerProfile.builder()
                .playerId(playerId)
                .ageRange(request.getAgeRange())
                .primaryRole(request.getPrimaryRole())
                .workSchedule(request.getWorkSchedule())
                .livingSituation(request.getLivingSituation())
                .focusAreas(request.getFocusAreas())
                .sixMonthGoal(request.getSixMonthGoal())
                .biggestChallenge(request.getBiggestChallenge())
                .weaknesses(request.getWeaknesses())
                .pastFailures(request.getPastFailures())
                .quitReasons(request.getQuitReasons())
                .availableTime(request.getAvailableTime())
                .preferredQuestTypes(request.getPreferredQuestTypes())
                .difficultyPreference(request.getDifficultyPreference())
                .build();
        
        profileRepository.save(profile);

        // Update Progress
        // Store raw data in progress just in case, or verify mapping
        // progress.setQuestionnaireData(map(request)); // Optional if we store JSON
        progress.setCurrentStage(OnboardingStage.CALIBRATION);
        onboardingRepository.save(progress);

        return buildResponse(progress, "Profile Saved. Proceed to Attribute Calibration.");
    }

    @Transactional
    public OnboardingResponse calibrateAttributes(UUID playerId, CalibrationRequest request) {
        OnboardingProgress progress = getProgress_OrThrow(playerId);
        
        if (progress.getCurrentStage() != OnboardingStage.CALIBRATION) {
            throw new IllegalStateException("Invalid stage for calibration.");
        }

        // Logic: self-rating 1-10 -> Base Value
        // 1-3 -> 5.0, 4-6 -> 10.0, 7-10 -> 15.0
        request.getAttributeRatings().forEach((attrName, rating) -> {
            try {
                AttributeType type = AttributeType.valueOf(attrName.toUpperCase());
                double baseValue = calculateBaseValue(rating);
                
                // We need a way to SET base value, but PlayerStateService only has updateAttribute (delta)
                // Assuming newly created player has 10.0 (or 0.0 for some).
                // Should we calculate delta?
                // Let's assume initializePlayer sets them to 10.0 (default).
                // Or we can add setAttributeBaseValue to PlayerStateService?
                // For now, I'll use updateAttribute with delta.
                // Assuming default is 10.0.
                // If I want 15.0, I add 5.0. 
                // However, I don't know current value easily without fetching state.
                // Better approach: Just add some bonus based on rating, on top of default.
                // 1-3: -5.0 (weakness), 4-6: 0 (average), 7-10: +5.0 (strength)
                
                double delta = calculateCalibrationDelta(rating);
                if (delta != 0) {
                    playerStateService.updateAttribute(playerId, type, delta);
                }
                
            } catch (IllegalArgumentException e) {
                log.warn("Invalid attribute type in calibration: {}", attrName);
            }
        });

        // Finalize
        progress.setCurrentStage(OnboardingStage.COMPLETED);
        progress.setCompletedAt(LocalDateTime.now());
        onboardingRepository.save(progress);

        // Update Identity Flag
        PlayerIdentity identity = progress.getPlayer();
        identity.setOnboardingCompleted(true);
        identityRepository.save(identity);
        
        // Trigger AI Quest Generation (Async/Event preferrably)
        // For Phase 0.1, we just mark complete.
        
        return buildResponse(progress, "Onboarding Completed. Welcome to the System.");
    }
    
    public OnboardingResponse getStatus(UUID playerId) {
        OnboardingProgress progress = getProgress_OrThrow(playerId);
        return buildResponse(progress, "Current Status Retrieved.");
    }

    private OnboardingProgress getProgress_OrThrow(UUID playerId) {
        return onboardingRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding progress not found for player: " + playerId));
    }

    private OnboardingResponse buildResponse(OnboardingProgress progress, String msg) {
        return OnboardingResponse.builder()
                .playerId(progress.getPlayerId())
                .currentStage(progress.getCurrentStage())
                .message(msg)
                .build();
    }
    
    private double calculateBaseValue(int rating) {
        if (rating <= 3) return 5.0;
        if (rating <= 6) return 10.0;
        return 15.0;
    }
    
    private double calculateCalibrationDelta(int rating) {
        if (rating <= 3) return -5.0; // Weakness
        if (rating <= 6) return 0.0;  // Average
        return 5.0;  // Strength
    }
}
