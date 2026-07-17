package com.lifeos.onboarding.service;

import com.lifeos.onboarding.domain.OnboardingProgress;
import com.lifeos.onboarding.domain.OnboardingStage;
import com.lifeos.onboarding.domain.PlayerProfile;
import com.lifeos.onboarding.dto.OnboardingResponse;
import com.lifeos.onboarding.dto.QuestionnaireRequest;
import com.lifeos.onboarding.dto.AwakeningPenaltyDTO;
import com.lifeos.onboarding.dto.AwakeningPenaltyResultDTO;
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

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

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
    private final com.lifeos.core.repository.PlayerStateRepository playerStateRepository;

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
        log.info("=== ONBOARDING START === Processing Awakening for player: {} with challenge: {}", playerId, request.getBiggestChallenge());
        
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

        // Infer missing questionnaire fields using Gemini
        log.info("Calling AI to infer missing questionnaire fields...");
        QuestionnaireRequest inferredRequest;
        try {
            inferredRequest = aiQuestService.inferQuestionnaire(request);
        } catch (Exception e) {
            log.error("!!! ERROR: Questionnaire inference failed, using fallback: {}", e.getMessage());
            request.setArchetype("BALANCE");
            request.setWakeUpTime(java.time.LocalTime.of(6, 0));
            inferredRequest = request;
        }

        // 1. Save Awakening Profile with all 5 questionnaire fields + inferred data
        log.info("Step 1: Saving player profile...");
        try {
            PlayerProfile profile = PlayerProfile.builder()
                    .playerId(playerId)
                    .archetype(inferredRequest.getArchetype())
                    .biggestChallenge(request.getBiggestChallenge())
                    .sixMonthGoal(request.getSixMonthGoal())
                    .wakeUpTime(inferredRequest.getWakeUpTime())
                    .availableTime(request.getAvailableTime())
                    .focusAreas(request.getFocusArea() != null ? List.of(request.getFocusArea()) : List.of())
                    .pastFailures(request.getPastFailures())
                    .quitReasons(request.getPastFailures())
                    .weaknesses(request.getBiggestChallenge() != null ? List.of(request.getBiggestChallenge()) : List.of())
                    .build();
            
            log.debug("Profile built: archetype={}, goal={}, challenge={}", inferredRequest.getArchetype(), request.getSixMonthGoal(), request.getBiggestChallenge());
            profileRepository.save(profile);
            log.info("Step 1: Profile saved successfully");
        } catch (Exception e) {
            log.error("!!! ERROR in Step 1 - Profile save failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save profile", e);
        }

        // 2. Auto-Calibrate Stats based on Archetype
        log.info("Step 2: Calibrating stats...");
        try {
            calibrateStats(playerId, inferredRequest.getArchetype());
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

        // 5c. Generate and assign the actual Trial Quest (Courage of the Weak)
        log.info("Step 5c: Generating and assigning Trial Quest (Courage of the Weak)...");
        try {
            QuestRequest trialQuestReq = trialQuestGenerator.generateTrialQuest(playerId);
            Quest trialQuest = questService.assignQuest(trialQuestReq);
            progress.setTrialQuestId(trialQuest.getQuestId());
            log.info("Assigned Trial Quest: {}, set trialQuestId to {}", trialQuest.getTitle(), trialQuest.getQuestId());
        } catch (Exception e) {
            log.error("!!! ERROR assigning Trial Quest: {}", e.getMessage(), e);
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

        if (intMod != 0) playerStateService.updateAttribute(playerId, AttributeType.INT, intMod);
        if (senMod != 0) playerStateService.updateAttribute(playerId, AttributeType.SEN, senMod);
        if (strMod != 0) playerStateService.updateAttribute(playerId, AttributeType.STR, strMod);
        if (vitMod != 0) playerStateService.updateAttribute(playerId, AttributeType.VIT, vitMod);
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
    
    @Transactional(readOnly = true)
    public OnboardingResponse getStatus(UUID playerId) {
        OnboardingProgress progress = getProgress_OrThrow(playerId);
        return buildResponse(progress, "Current Status Retrieved.");
    }

    private OnboardingProgress getProgress_OrThrow(UUID playerId) {
        return onboardingRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding progress not found for player: " + playerId));
    }

    private OnboardingResponse buildResponse(OnboardingProgress progress, String msg) {
        Quest trialQuest = null;
        if (progress.getTrialQuestId() != null) {
            trialQuest = questRepository.findById(progress.getTrialQuestId()).orElse(null);
        }
        return OnboardingResponse.builder()
                .playerId(progress.getPlayerId())
                .currentStage(progress.getCurrentStage())
                .trialQuest(trialQuest)
                .message(msg)
                .build();
    }
    
    private List<QuestRequest> createFallbackOnboardingQuests(UUID playerId, QuestionnaireRequest request) {
        String focusArea = request.getFocusArea() != null ? request.getFocusArea() : "General";
        String weakness = request.getBiggestChallenge() != null ? request.getBiggestChallenge() : "Consistency";
        String goal = request.getSixMonthGoal() != null ? request.getSixMonthGoal() : "Personal Growth";
        
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
                .primaryAttribute(AttributeType.VIT)
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
    
    @Transactional
    public OnboardingResponse completeTrial(UUID playerId) {
        log.info("Completing onboarding trial for player: {}", playerId);
        
        OnboardingProgress progress = getProgress_OrThrow(playerId);
        progress.setCurrentStage(OnboardingStage.COMPLETED);
        progress.setTrialCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        onboardingRepository.save(progress);
        
        PlayerIdentity identity = progress.getPlayer();
        identity.setOnboardingCompleted(true);
        identityRepository.save(identity);
        
        playerStateRepository.findById(playerId).ifPresent(coreState -> {
            coreState.setOnboardingCompleted(true);
            coreState.setLevel(10);
            coreState.setGoldBalance(10000);
            playerStateRepository.save(coreState);
            log.info("Updated core PlayerState for player: {} to onboardingCompleted = true, level = 10, gold = 10000", playerId);
        });

        var progression = progressionRepository.findByPlayerPlayerId(playerId).orElse(null);
        if (progression != null) {
            progression.setLevel(10);
            progression.setGold(10000L);
            progressionRepository.save(progression);
        }
        
        return buildResponse(progress, "Onboarding trial completed successfully. System unlocked.");
    }

    @Transactional
    public AwakeningPenaltyDTO failTrial(UUID playerId) {
        log.info("Trial failed for player {}, generating 1-hour physical task...", playerId);
        OnboardingProgress progress = getProgress_OrThrow(playerId);

        // Stage guard: a trial can only be failed from the TRIAL_QUEST stage.
        if (progress.getCurrentStage() != OnboardingStage.TRIAL_QUEST) {
            throw new IllegalStateException(
                    "Player must be in TRIAL_QUEST stage to fail a trial. Current stage: " + progress.getCurrentStage());
        }

        // Generate the 1-hour awakening penalty task
        QuestRequest penaltyQuest = aiQuestService.generateAwakeningPenalty(playerId);
        
        UUID penaltyId = UUID.randomUUID();
        progress.setCurrentStage(OnboardingStage.AWAKENING_PENALTY);
        progress.setPenaltyQuestId(penaltyId);
        progress.setPenaltyTitle(penaltyQuest.getTitle());
        progress.setPenaltyDescription(penaltyQuest.getDescription());
        // Set deadline exactly 1 hour from now
        progress.setPenaltyDeadlineAt(LocalDateTime.now().plusHours(1));
        onboardingRepository.save(progress);

        return AwakeningPenaltyDTO.builder()
                .playerId(playerId)
                .penaltyQuestId(penaltyId)
                .taskTitle(penaltyQuest.getTitle())
                .taskDescription(penaltyQuest.getDescription())
                .deadlineAt(progress.getPenaltyDeadlineAt())
                .stage(OnboardingStage.AWAKENING_PENALTY)
                .build();
    }

    @Transactional
    public AwakeningPenaltyResultDTO completePenalty(UUID playerId) {
        log.info("AWAKENING PENALTY CLEARED: Resetting trial for tomorrow for player {}", playerId);
        OnboardingProgress progress = getProgress_OrThrow(playerId);

        // Stage guard: the penalty can only be completed while serving it.
        if (progress.getCurrentStage() != OnboardingStage.AWAKENING_PENALTY) {
            throw new IllegalStateException("Player is not in AWAKENING_PENALTY stage");
        }

        // Timer enforcement: the 1-hour physical penalty must be fully endured before it can be cleared.
        if (progress.getPenaltyDeadlineAt() != null
                && LocalDateTime.now().isBefore(progress.getPenaltyDeadlineAt())) {
            throw new IllegalStateException("Penalty endurance time has not elapsed.");
        }

        // Change stage back to TRIAL_QUEST
        progress.setCurrentStage(OnboardingStage.TRIAL_QUEST);
        progress.setTrialCompleted(false);
        
        // Clear old penalty details
        progress.setPenaltyQuestId(null);
        progress.setPenaltyTitle(null);
        progress.setPenaltyDescription(null);
        progress.setPenaltyDeadlineAt(null);
        
        // Delete all old quests for this player so they get a fresh start tomorrow
        List<Quest> oldQuests = questRepository.findByPlayerPlayerId(playerId);
        questRepository.deleteAll(oldQuests);
        
        // Recreate the onboarding quests (Core AI quests, Intel quests, and Trial quest)
        // 1. Core AI Quests
        List<QuestRequest> coreQuests;
        try {
            coreQuests = aiQuestService.generateQuests(playerId, 3);
            for (QuestRequest q : coreQuests) {
                // Set deadline to tomorrow
                q.setDeadlineAt(LocalDateTime.now().plusDays(1).withHour(23).withMinute(59));
                questService.assignQuest(q);
            }
        } catch (Exception e) {
            log.error("Failed to generate AI quests during penalty reset", e);
        }
        
        // 2. Intel Quests
        try {
            QuestRequest intelQuest1 = intelQuestGenerator.generateFirstIntelQuest(playerId);
            if (intelQuest1 != null) {
                intelQuest1.setDeadlineAt(LocalDateTime.now().plusDays(1).withHour(23).withMinute(59));
                questService.assignQuest(intelQuest1);
            }
        } catch (Exception e) {
            log.error("Failed to generate Intel Quest during penalty reset", e);
        }
        
        // 3. Trial Quest
        try {
            QuestRequest trialQuestReq = trialQuestGenerator.generateTrialQuest(playerId);
            if (trialQuestReq != null) {
                // Trial quest gets 24h starting tomorrow
                trialQuestReq.setDeadlineAt(LocalDateTime.now().plusDays(1).withHour(23).withMinute(59));
                Quest trialQuest = questService.assignQuest(trialQuestReq);
                progress.setTrialQuestId(trialQuest.getQuestId());
            }
        } catch (Exception e) {
            log.error("Failed to generate Trial Quest during penalty reset", e);
        }
        
        onboardingRepository.save(progress);
        
        return AwakeningPenaltyResultDTO.builder()
                .playerId(playerId)
                .cleared(true)
                .trialResetDate(java.time.LocalDate.now().plusDays(1))
                .accountDeleted(false)
                .build();
    }

    @Transactional
    public AwakeningPenaltyResultDTO failPenalty(UUID playerId) {
        log.info("AWAKENING PENALTY FAILED: Wiping account for player {}", playerId);

        // Fetch-first: closes the silent-success loophole (unknown player -> 404, not a no-op 200).
        OnboardingProgress progress = getProgress_OrThrow(playerId);

        // Stage guard: an irreversible account wipe must never be reachable outside the penalty flow.
        if (progress.getCurrentStage() != OnboardingStage.AWAKENING_PENALTY) {
            throw new IllegalStateException("Player is not in AWAKENING_PENALTY stage");
        }

        String[] queries = {
            "DELETE FROM onboarding_progress WHERE player_id = :playerId",
            "DELETE FROM player_profiles WHERE player_id = :playerId",
            "DELETE FROM player_progression WHERE player_id = :playerId",
            "DELETE FROM player_state WHERE player_id = :playerId",
            "DELETE FROM quest_mutation_log WHERE quest_id IN (SELECT quest_id FROM quest WHERE player_id = :playerId)",
            "DELETE FROM quest_outcome_profile WHERE quest_id IN (SELECT quest_id FROM quest WHERE player_id = :playerId)",
            "DELETE FROM player_quest_link WHERE player_id = :playerId",
            "DELETE FROM quest WHERE player_id = :playerId",
            "DELETE FROM penalty_quests WHERE player_id = :playerId",
            "DELETE FROM penalty_record WHERE player_id = :playerId",
            "DELETE FROM player_journal WHERE player_id = :playerId",
            "DELETE FROM player_attribute WHERE player_id = :playerId",
            "DELETE FROM player_history WHERE player_id = :playerId",
            "DELETE FROM player_metadata WHERE player_id = :playerId",
            "DELETE FROM player_metrics WHERE player_id = :playerId",
            "DELETE FROM player_psych_state WHERE player_id = :playerId",
            "DELETE FROM player_status_flag WHERE player_id = :playerId",
            "DELETE FROM player_temporal_state WHERE player_id = :playerId",
            "DELETE FROM player_state_snapshot WHERE player_id = :playerId",
            "DELETE FROM job_change_quest WHERE player_id = :playerId",
            "DELETE FROM rank_exam_attempts WHERE player_id = :playerId",
            "DELETE FROM user_boss_keys WHERE player_id = :playerId",
            "DELETE FROM dungeon_break_events WHERE player_id = :playerId",
            "DELETE FROM project WHERE player_id = :playerId",
            "DELETE FROM reward_record WHERE player_id = :playerId",
            "DELETE FROM player_streak WHERE player_id = :playerId",
            "DELETE FROM system_event WHERE player_id = :playerId",
            "DELETE FROM system_messages WHERE player_id = :playerId",
            "DELETE FROM purchase_cooldown WHERE player_id = :playerId",
            "DELETE FROM purchase_transaction WHERE player_id = :playerId",
            "DELETE FROM user_inventory WHERE player_id = :playerId",
            "DELETE FROM player_economy WHERE player_id = :playerId",
            "DELETE FROM player_identity WHERE player_id = :playerId"
        };

        for (String q : queries) {
            try {
                entityManager.createNativeQuery(q)
                        .setParameter("playerId", playerId.toString())
                        .executeUpdate();
            } catch (Exception e) {
                try {
                    entityManager.createNativeQuery(q)
                            .setParameter("playerId", playerId)
                            .executeUpdate();
                } catch (Exception ex) {
                    log.warn("Could not execute deletion query '{}': {}", q, ex.getMessage());
                }
            }
        }

        return AwakeningPenaltyResultDTO.builder()
                .playerId(playerId)
                .cleared(false)
                .trialResetDate(null)
                .accountDeleted(true)
                .build();
    }

    @Transactional(readOnly = true)
    public AwakeningPenaltyDTO getPenaltyStatus(UUID playerId) {
        OnboardingProgress progress = getProgress_OrThrow(playerId);
        if (progress.getCurrentStage() != OnboardingStage.AWAKENING_PENALTY) {
            throw new IllegalStateException("Player is not in AWAKENING_PENALTY stage");
        }
        return AwakeningPenaltyDTO.builder()
                .playerId(playerId)
                .penaltyQuestId(progress.getPenaltyQuestId())
                .taskTitle(progress.getPenaltyTitle())
                .taskDescription(progress.getPenaltyDescription())
                .deadlineAt(progress.getPenaltyDeadlineAt())
                .stage(progress.getCurrentStage())
                .build();
    }
}
