package com.lifeos.onboarding.service;

import com.lifeos.onboarding.domain.OnboardingProgress;
import com.lifeos.onboarding.domain.OnboardingStage;
import com.lifeos.onboarding.domain.PlayerProfile;
import com.lifeos.onboarding.dto.OnboardingResponse;
import com.lifeos.onboarding.dto.QuestionnaireRequest;
import com.lifeos.onboarding.repository.OnboardingProgressRepository;
import com.lifeos.onboarding.repository.PlayerProfileRepository;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.PlayerProgression;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.domain.enums.QuestType;
import com.lifeos.quest.dto.QuestRequest;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.quest.service.QuestFallbackService;
import com.lifeos.quest.service.QuestLifecycleService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private static final Logger log = LoggerFactory.getLogger(OnboardingService.class);

    private final PlayerStateService playerStateService;
    private final OnboardingProgressRepository onboardingRepository;
    private final PlayerProfileRepository profileRepository;
    private final PlayerIdentityRepository identityRepository;
    private final com.lifeos.player.repository.PlayerProgressionRepository progressionRepository;
    private final QuestLifecycleService questService;
    private final QuestRepository questRepository;
    private final TrialQuestGenerator trialQuestGenerator;
    private final QuestFallbackService questFallbackService;
    private final com.lifeos.ai.service.AIQuestService aiQuestService;
    private final IntelQuestGenerator intelQuestGenerator;

    @Transactional
    public OnboardingResponse startOnboarding(String username) {
        log.info("Starting onboarding for username: {}", username);
        
        // 1. Initialize Player Identity & Basic State
        var stateResponse = playerStateService.initializePlayer(username);
        UUID playerId = stateResponse.getIdentity().getPlayerId();
        PlayerIdentity player = identityRepository.findById(playerId)
                .orElseThrow(() -> new IllegalStateException("Player created but not found"));

        // 2. Create Onboarding Progress (Start at QUESTIONNAIRE/AWAKENING)
        OnboardingProgress progress = OnboardingProgress.builder()
                .player(player)
                .currentStage(OnboardingStage.QUESTIONNAIRE)
                .trialCompleted(false)
                .build();
        
        onboardingRepository.save(progress);

        return OnboardingResponse.builder()
                .playerId(playerId)
                .currentStage(OnboardingStage.QUESTIONNAIRE)
                .message("System Evaluation Initiated.")
                .build();
    }

    @Transactional
    public OnboardingResponse submitAwakening(UUID playerId, QuestionnaireRequest request) {
        log.info("=== ONBOARDING START === Processing Awakening for player: {} with archetype: {}", playerId, request.getArchetype());
        
        try {
            OnboardingProgress progress = getProgress_OrThrow(playerId);
            log.debug("Found onboarding progress: current stage = {}", progress.getCurrentStage());
        } catch (Exception e) {
            log.error("!!! ERROR: Could not find onboarding progress for player: {}", playerId, e);
            throw new RuntimeException("Onboarding progress not found", e);
        }
        
        OnboardingProgress progress = getProgress_OrThrow(playerId);
        
        if (progress.getCurrentStage() != OnboardingStage.QUESTIONNAIRE) {
            log.warn("Invalid stage for Awakening: {}", progress.getCurrentStage());
        }

        // 1. Save Awakening Profile with all 7 questionnaire fields
        log.info("Step 1: Saving player profile...");
        try {
            PlayerProfile profile = PlayerProfile.builder()
                    .playerId(playerId)
                    .archetype(request.getArchetype())
                    .biggestChallenge(request.getBiggestChallenge())
                    .sixMonthGoal(request.getMainGoal())
                    .wakeUpTime(request.getWakeUpTime())
                    .availableTime(request.getAvailableTime())
                    .focusAreas(request.getFocusArea() != null ? List.of(request.getFocusArea()) : List.of())
                    .build();
            
            log.debug("Profile built: archetype={}, goal={}, challenge={}", request.getArchetype(), request.getMainGoal(), request.getBiggestChallenge());
            profileRepository.save(profile);
            log.info("Step 1: Profile saved successfully");
        } catch (Exception e) {
            log.error("!!! ERROR in Step 1 - Profile save failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save profile", e);
        }

        // 2. Auto-Calibrate Stats based on Archetype
        log.info("Step 2: Calibrating stats...");
        try {
            calibrateStats(playerId, request.getArchetype());
            log.info("Step 2: Stats calibrated");
        } catch (Exception e) {
            log.error("!!! ERROR in Step 2 - Stats calibration failed: {}", e.getMessage(), e);
        }
        
        // 3. Set Rank E and Class (if any implied, or generic)
        log.info("Step 3: Initializing progression...");
        try {
            initializeProgression(playerId);
            log.info("Step 3: Progression initialized");
        } catch (Exception e) {
            log.error("!!! ERROR in Step 3 - Progression init failed: {}", e.getMessage(), e);
        }

        // 4. Generate 3 Core Quests using Gemini AI based on player's context
        // All core quests MUST have 24-hour deadline
        log.info("Step 4: Generating 3 core quests via Gemini AI...");
        List<QuestRequest> coreQuests;
        
        try {
            log.info("Calling AI to generate 3 personalized core quests for player: {}", playerId);
            coreQuests = aiQuestService.generateQuests(playerId, 3);
            
            // Ensure all have 24hr deadline
            for (QuestRequest q : coreQuests) {
                if (q.getDeadlineAt() == null || q.getDeadlineAt().isAfter(LocalDateTime.now().plusHours(24))) {
                    q.setDeadlineAt(LocalDateTime.now().plusHours(24));
                }
            }
            
            log.info("AI generated {} core quests with 24hr deadline", coreQuests.size());
        } catch (Exception e) {
            log.error("!!! ERROR in Step 4 - AI quest generation failed: {}. This should not happen!", e.getMessage(), e);
            throw new RuntimeException("Failed to generate AI quests - player cannot proceed without AI-generated quests", e);
        }
        
        log.info("Final core quests to assign: {}", coreQuests.size());
        
        for (QuestRequest coreQuest : coreQuests) {
            try {
                log.debug("Assigning core quest: {}", coreQuest.getTitle());
                questService.assignQuest(coreQuest);
                log.info("Assigned core quest: {}", coreQuest.getTitle());
            } catch (Exception e) {
                log.error("!!! ERROR assigning core quest '{}': {}", coreQuest.getTitle(), e.getMessage(), e);
            }
        }

        // 5. Generate Intel Quests with Adaptive Volume Logic
        // Intel quests: 24hr deadline, NO penalty on fail (only blocks dungeon)
        log.info("Step 5: Generating Intel quests (24hr deadline, no penalty on fail)...");
        int intelQuestCount;
        try {
            intelQuestCount = calculateIntelQuestCount(playerId);
            log.info("Intel quest count calculated: {}", intelQuestCount);
        } catch (Exception e) {
            log.error("!!! ERROR calculating Intel quest count: {}", e.getMessage(), e);
            intelQuestCount = 2;
        }
        
        // Always generate at least 1 Intel Quest for new players (Physical Analysis)
        try {
            log.debug("Generating Intel Quest 1: Physical Analysis (24hr deadline)");
            QuestRequest intelQuest1 = intelQuestGenerator.generateFirstIntelQuest(playerId);
            intelQuest1.setDeadlineAt(LocalDateTime.now().plusHours(24)); // 24hr deadline
            questService.assignQuest(intelQuest1);
            log.info("Assigned Intel Quest 1: {}", intelQuest1.getTitle());
        } catch (Exception e) {
            log.error("!!! ERROR assigning Intel Quest 1: {}", e.getMessage(), e);
        }
        
        // Generate additional Intel Quests based on adaptive volume
        if (intelQuestCount >= 2) {
            log.info("Step 5b: Generating Intel Quest 2 (24hr deadline)...");
            try {
                QuestRequest intelQuest2 = intelQuestGenerator.generateMentalAnalysisQuest(playerId);
                if (intelQuest2 != null) {
                    intelQuest2.setDeadlineAt(LocalDateTime.now().plusHours(24)); // 24hr deadline
                    questService.assignQuest(intelQuest2);
                    log.info("Assigned Intel Quest 2: {}", intelQuest2.getTitle());
                }
            } catch (Exception e) {
                log.error("!!! ERROR assigning Intel Quest 2: {}", e.getMessage(), e);
            }
        } else {
            log.info("Step 5b: Skipping Intel Quest 2 (count = {})", intelQuestCount);
        }

        // 6. Keep player in TRIAL_QUEST state until all quests are completed
        log.info("Step 6: Saving onboarding progress...");
        try {
            progress.setCurrentStage(OnboardingStage.TRIAL_QUEST);
            progress.setTrialCompleted(false);
            onboardingRepository.save(progress);
            log.info("Onboarding progress saved: TRIAL_QUEST");
        } catch (Exception e) {
            log.error("!!! ERROR saving onboarding progress: {}", e.getMessage(), e);
        }
        
        // Keep onboardingCompleted = false - player cannot access full system yet
        log.info("Step 7: Saving player identity...");
        try {
            PlayerIdentity identity = progress.getPlayer();
            identity.setOnboardingCompleted(false);
            identityRepository.save(identity);
            log.info("Player identity saved: onboardingCompleted = false");
        } catch (Exception e) {
            log.error("!!! ERROR saving player identity: {}", e.getMessage(), e);
        }
        
        log.info("=== ONBOARDING COMPLETE === Returning response for player: {}", playerId);
        
        return buildResponse(progress, "Awakening Initiated. Complete all assigned quests to unlock the System.");
    }

    private void calibrateStats(UUID playerId, String archetype) {
        // Reset to base 0 (or assume initializePlayer set them to 10? We'll use updateAttribute relative to base or set absolute if possible)
        // Since we don't have setAttribute, we'll assume base is near 0 or 10. 
        // Better strategy: Calculate deltas from "Flat 10" baseline.
        // Let's assume initializePlayer gives 10 to all.
        
        int intMod = 0, senMod = 0, strMod = 0, vitMod = 0, agiMod = 0;

        switch (archetype != null ? archetype.toUpperCase() : "BALANCE") {
            case "BRAINS":
                intMod = +2; senMod = +2; strMod = -2; vitMod = -2;
                break;
            case "BRAWN":
                strMod = +2; vitMod = +2; intMod = -2; senMod = -2;
                break;
            case "BALANCE":
            default:
                // Flat 10
                break;
        }

        if (intMod != 0) playerStateService.updateAttribute(playerId, AttributeType.INTELLIGENCE, intMod);
        if (senMod != 0) playerStateService.updateAttribute(playerId, AttributeType.SENSE, senMod);
        if (strMod != 0) playerStateService.updateAttribute(playerId, AttributeType.STRENGTH, strMod);
        if (vitMod != 0) playerStateService.updateAttribute(playerId, AttributeType.VITALITY, vitMod);
        // AGI stays 10
    }

    private void initializeProgression(UUID playerId) {
        PlayerProgression progression = progressionRepository.findByPlayerPlayerId(playerId)
                .orElseGet(() -> PlayerProgression.builder().player(identityRepository.getReferenceById(playerId)).level(1).build());
        
        progression.setRank(com.lifeos.player.domain.enums.PlayerRank.E);
        progression.setHunterClass("None"); // Start as None, unlock later
        progressionRepository.save(progression);
    }

    // Deprecated / Bridge methods for backward compatibility if needed, else removed.
    
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
    
    private List<QuestRequest> createFallbackOnboardingQuests(UUID playerId, QuestionnaireRequest request) {
        String focusArea = request.getFocusArea() != null ? request.getFocusArea() : "General";
        String weakness = request.getPrimaryWeakness() != null ? request.getPrimaryWeakness() : "Consistency";
        String goal = request.getMainGoal() != null ? request.getMainGoal() : "Personal Growth";
        
        return List.of(
            QuestRequest.builder()
                .playerId(playerId)
                .title("Foundation: Build Your Routine")
                .description("Start building a consistent routine. Task: Complete a 30-minute focused session on your goal: " + goal)
                .questType(QuestType.DISCIPLINE)
                .difficultyTier(com.lifeos.quest.domain.enums.DifficultyTier.E)
                .priority(com.lifeos.quest.domain.enums.Priority.HIGH)
                .deadlineAt(LocalDateTime.now().plusHours(24))
                .successXp(50)
                .goldReward(50)
                .systemMutable(false)
                .primaryAttribute(AttributeType.DISCIPLINE)
                .build(),
            QuestRequest.builder()
                .playerId(playerId)
                .title("Challenge: Face Your Weakness")
                .description("Address your struggle with: " + weakness + ". Complete a focused 45-minute session.")
                .questType(QuestType.COGNITIVE)
                .difficultyTier(com.lifeos.quest.domain.enums.DifficultyTier.E)
                .priority(com.lifeos.quest.domain.enums.Priority.HIGH)
                .deadlineAt(LocalDateTime.now().plusHours(24))
                .successXp(75)
                .goldReward(75)
                .systemMutable(false)
                .primaryAttribute(AttributeType.FOCUS)
                .build(),
            QuestRequest.builder()
                .playerId(playerId)
                .title("Growth: " + focusArea + " Development")
                .description("Begin developing your " + focusArea + " abilities. Complete a 30-minute practice session.")
                .questType(QuestType.PHYSICAL)
                .difficultyTier(com.lifeos.quest.domain.enums.DifficultyTier.E)
                .priority(com.lifeos.quest.domain.enums.Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().plusHours(24))
                .successXp(50)
                .goldReward(50)
                .systemMutable(false)
                .primaryAttribute(AttributeType.VITALITY)
                .build()
        );
    }
    
    /**
     * PRD Adaptive Volume Logic:
     * - If player_tenure < 7 days, always issue 2 Intel Quests
     * - If failure_rate > 30% in last 3 days, issue 2 Intel Quests for "Diagnostic Calibration"
     * - Else, issue 1 or 0 Intel Quests
     */
    private int calculateIntelQuestCount(UUID playerId) {
        try {
            PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
            if (identity == null) {
                return 2; // Default for new players
            }
            
            // Check player tenure (days since creation)
            long daysSinceCreation = java.time.Duration.between(identity.getCreatedAt(), java.time.LocalDateTime.now()).toDays();
            log.debug("Player {} tenure: {} days", playerId, daysSinceCreation);
            
            if (daysSinceCreation < 7) {
                log.info("Player {} is new (< 7 days), issuing 2 Intel Quests", playerId);
                return 2;
            }
            
            // Check failure rate in last 3 days
            double failureRate = calculateFailureRate(playerId);
            log.debug("Player {} failure rate: {}%", playerId, failureRate * 100);
            
            if (failureRate > 0.30) {
                log.info("Player {} has high failure rate ({}%), issuing 2 Intel Quests for Diagnostic Calibration", 
                    playerId, failureRate * 100);
                return 2;
            }
            
            // Default: 1 Intel Quest
            return 1;
            
        } catch (Exception e) {
            log.error("Error calculating Intel Quest count, defaulting to 2", e);
            return 2;
        }
    }
    
    /**
     * Calculate failure rate based on quest completions in the last 3 days
     */
    private double calculateFailureRate(UUID playerId) {
        try {
            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
            
            // Get all quests attempted in last 3 days
            List<Quest> recentQuests = questRepository.findByPlayerPlayerId(playerId)
                    .stream()
                    .filter(q -> q.getAssignedAt() != null && q.getAssignedAt().isAfter(threeDaysAgo))
                    .toList();
            
            if (recentQuests.isEmpty()) {
                return 0.0; // No history, no failure
            }
            
            long failed = recentQuests.stream()
                    .filter(q -> q.getState() == com.lifeos.quest.domain.enums.QuestState.FAILED)
                    .count();
            
            return (double) failed / recentQuests.size();
            
        } catch (Exception e) {
            log.error("Error calculating failure rate", e);
            return 0.0;
        }
    }
     
    // Legacy support methods (can delete if clean break)
    @Transactional
    public OnboardingResponse completeTrialQuest(UUID playerId) {
        // Bypass or map to new flow
        return getStatus(playerId);
    }
    
    /**
     * Unlock the JOB_CHANGE stage in onboarding for a player.
     * Called when player reaches Level 40.
     */
    @Transactional
    public void unlockJobChangeStage(UUID playerId) {
        var progress = onboardingRepository.findById(playerId).orElse(null);
        if (progress == null) {
            log.warn("No onboarding progress found for player {}", playerId);
            return;
        }
        
        // Only unlock if currently in earlier stage
        if (progress.getCurrentStage() == OnboardingStage.COMPLETED) {
            log.info("Player {} already completed onboarding", playerId);
            return;
        }
        
        // Allow unlock from any stage
        log.info("Unlocking JOB_CHANGE stage for player {}", playerId);
        progress.setCurrentStage(OnboardingStage.JOB_CHANGE);
        onboardingRepository.save(progress);
        
        log.info("JOB_CHANGE stage unlocked for player {}", playerId);
    }
}
