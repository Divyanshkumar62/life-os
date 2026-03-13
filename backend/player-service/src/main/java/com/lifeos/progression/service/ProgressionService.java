package com.lifeos.progression.service;

import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.progression.domain.RankExamAttempt;
import com.lifeos.progression.domain.UserBossKey;
import com.lifeos.progression.domain.enums.ExamStatus;
import com.lifeos.progression.repository.RankExamAttemptRepository;
import com.lifeos.progression.repository.UserBossKeyRepository;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.voice.domain.enums.SystemMessageType;
import com.lifeos.voice.event.VoiceSystemEvent;
import com.lifeos.player.repository.PlayerIdentityRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import com.lifeos.system.service.SystemVoiceService;
import com.lifeos.system.domain.enums.SystemEventType;
import org.springframework.context.annotation.Lazy;

@Service
public class ProgressionService {

    private static final Logger log = LoggerFactory.getLogger(ProgressionService.class);

    private final PlayerStateService playerStateService;
    private final PenaltyService penaltyService;
    private final QuestRepository questRepository;
    private final UserBossKeyRepository bossKeyRepository;
    private final RankExamAttemptRepository examAttemptRepository;
    private final PlayerIdentityRepository playerIdentityRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final com.lifeos.ai.service.AIQuestService aiQuestService;
    private final com.lifeos.quest.service.QuestLifecycleService questLifecycleService;
    private final SystemVoiceService systemVoiceService;

    public ProgressionService(PlayerStateService playerStateService, @Lazy PenaltyService penaltyService,
                            QuestRepository questRepository, UserBossKeyRepository bossKeyRepository,
                            RankExamAttemptRepository examAttemptRepository,
                            PlayerIdentityRepository playerIdentityRepository,
                            ApplicationEventPublisher eventPublisher,
                            com.lifeos.ai.service.AIQuestService aiQuestService,
                            @Lazy com.lifeos.quest.service.QuestLifecycleService questLifecycleService,
                            SystemVoiceService systemVoiceService) {
        this.playerStateService = playerStateService;
        this.penaltyService = penaltyService;
        this.questRepository = questRepository;
        this.bossKeyRepository = bossKeyRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.playerIdentityRepository = playerIdentityRepository;
        this.eventPublisher = eventPublisher;
        this.aiQuestService = aiQuestService;
        this.questLifecycleService = questLifecycleService;
        this.systemVoiceService = systemVoiceService;
    }

    /**
     * Checks if player is at Rank Gate (Level == Cap).
     */
    public boolean checkRankGate(UUID playerId) {
        var state = playerStateService.getPlayerState(playerId);
        int currentLevel = state.getProgression().getLevel();
        int cap = state.getProgression().getRank().getLevelCap();
        return currentLevel >= cap;
    }

    /**
     * Validates if a player is eligible to request a Promotion Quest.
     */
    public boolean canRequestPromotion(UUID playerId) {
        if (!checkRankGate(playerId)) return false;

        var state = playerStateService.getPlayerState(playerId);
        var currentRank = state.getProgression().getRank();
        var template = com.lifeos.progression.domain.RankTransitionTemplate.from(currentRank);
        
        if (template == null) return false; // S Rank or maxed

        // Check rank-specific Boss Keys
        var bossKeyOpt = bossKeyRepository.findByPlayerPlayerIdAndRank(playerId, currentRank);
        if (bossKeyOpt.isEmpty() || bossKeyOpt.get().getKeyCount() < template.getBossKeyCost()) {
            return false;
        }

        // Check Stats
        for (var entry : template.getStatRequirements().entrySet()) {
            var type = entry.getKey();
            var requiredVal = entry.getValue();
            
            var attr = state.getAttributes().stream()
                    .filter(a -> a.getAttributeType() == type)
                    .findFirst();
            
            if (attr.isEmpty() || attr.get().getCurrentValue() < requiredVal) {
                return false;
            }
        }
        
        // Check Penalty Zone
        boolean inPenaltyZone = state.getActiveFlags().stream()
                .anyMatch(f -> f.getFlag() == StatusFlagType.PENALTY_ZONE);
                
        if (inPenaltyZone) return false;

        return true;
    }

    /**
     * Requests a Promotion Quest.
     * Consumes rank-specific Boss Keys immediately.
     * Creates RankExamAttempt record.
     */
    @Transactional
    public RankExamAttempt requestPromotion(UUID playerId) {
        log.info("Promotion request for player: {}", playerId);
        if (!canRequestPromotion(playerId)) {
            log.warn("Player {} is not eligible for promotion", playerId);
            throw new IllegalStateException("Player is not eligible for promotion.");
        }
        
        var state = playerStateService.getPlayerState(playerId);
        var currentRank = state.getProgression().getRank();
        var template = com.lifeos.progression.domain.RankTransitionTemplate.from(currentRank);
        
        log.info("Current rank: {}, Next rank: {}, Key cost: {}", currentRank, currentRank.next(), template.getBossKeyCost());

        // Fetch full player entity
        var player = playerIdentityRepository.findById(playerId)
                .orElseThrow(() -> new IllegalStateException("Player entity not found"));

        // Consume rank-specific Boss Keys
        UserBossKey bossKey = bossKeyRepository.findByPlayerPlayerIdAndRank(playerId, currentRank)
                .orElseThrow(() -> new IllegalStateException("Boss keys not found"));
        
        if (bossKey.getKeyCount() < template.getBossKeyCost()) {
            log.error("Player {} does not have enough boss keys. Required: {}, Found: {}", 
                playerId, template.getBossKeyCost(), bossKey.getKeyCount());
            throw new IllegalStateException("Insufficient boss keys");
        }
        
        bossKey.setKeyCount(bossKey.getKeyCount() - template.getBossKeyCost());
        bossKeyRepository.save(bossKey);
        
        // Create RankExamAttempt record
        RankExamAttempt attempt = RankExamAttempt.builder()
                .player(player)
                .fromRank(currentRank)
                .toRank(currentRank.next())
                .status(ExamStatus.UNLOCKED)
                .requiredKeys(template.getBossKeyCost())
                .consumedKeys(template.getBossKeyCost())
                .attemptNumber(1) 
                .unlockedAt(LocalDateTime.now())
                .build();
        
        attempt = examAttemptRepository.save(attempt);
        
        // SPAWN AI PROMOTION QUEST
        var questRequest = aiQuestService.generatePromotionExam(playerId, currentRank, currentRank.next());
        if (questRequest == null) {
            log.error("Failed to generate AI promotion exam for player {}", playerId);
            throw new IllegalStateException("System could not generate trial. Ascension delayed.");
        }
        
        // Ensure quest type is correct
        questRequest.setQuestType(com.lifeos.quest.domain.enums.QuestType.PROMOTION_EXAM);
        
        // Assign via Lifecycle Service to get links and outcome profiles
        questLifecycleService.assignQuest(questRequest);
        
        log.info("Promotion initiated with AI trial for player {}. Exam ID: {}", 
            playerId, attempt.getId());
            
        // VOICE: PROMOTION_UNLOCKED
        eventPublisher.publishEvent(VoiceSystemEvent.builder()
                .playerId(playerId)
                .type(SystemMessageType.PROMOTION_UNLOCKED)
                .build());
                
        systemVoiceService.emitEvent(playerId, SystemEventType.PROMOTION_UPDATE, "Promotion Exam Unlocked. Prepare yourself.");
        
        return attempt;
    }

    /**
     * Processes the outcome of a Promotion Quest.
     * CRITICAL: Triggers Penalty Zone on failure.
     */
    
    
    @Transactional
    public void processPromotionOutcome(UUID playerId, boolean success) {
        // Find latest UNLOCKED/InProgress attempt for this player
        // We assume 1 active exam at a time.
        RankExamAttempt attempt = examAttemptRepository.findLatestByPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("No active exam attempt found for player " + playerId));
        
        if (attempt.getStatus() != ExamStatus.UNLOCKED) {
             // Maybe they clicked it twice? Or concurrency?
             // If already processed, ignore?
             // throw new IllegalStateException("Exam attempt is not in UNLOCKED state");
             return; 
        }
        
        attempt.setCompletedAt(LocalDateTime.now());
        
        if (success) {
            // SUCCESS: Promote Rank, Unfreeze XP
            attempt.setStatus(ExamStatus.PASSED);
            examAttemptRepository.save(attempt);
            
            playerStateService.promoteRank(playerId);
            
            log.info("Rank Advanced for player {}: {} -> {}", playerId, attempt.getFromRank(), attempt.getToRank());
            
            // VOICE: PROMOTION_PASSED
            eventPublisher.publishEvent(VoiceSystemEvent.builder()
                        .playerId(playerId)
                        .type(SystemMessageType.PROMOTION_PASSED)
                        .build());
                        
            systemVoiceService.emitEvent(playerId, SystemEventType.PROMOTION_UPDATE, "Promotion Successful. Your Rank has increased.");
        } else {
            // FAILURE: Mark attempt as failed
            // Keys are LOST (already consumed)
            // XP remains FROZEN
            // STRICT PENALTY: Enter Penalty Zone
            attempt.setStatus(ExamStatus.FAILED);
            examAttemptRepository.save(attempt);
            
            penaltyService.enterPenaltyZone(playerId, "Failed Rank Exam: " + attempt.getFromRank() + " -> " + attempt.getToRank());
            
            log.warn("Promotion Failed for player {}. ENTERING PENALTY ZONE. Reason: Failed Exam", playerId);
            
            // VOICE: PROMOTION_FAILED
            eventPublisher.publishEvent(VoiceSystemEvent.builder()
                        .playerId(playerId)
                        .type(SystemMessageType.PROMOTION_FAILED)
                        .build());
                        
            systemVoiceService.emitEvent(playerId, SystemEventType.PENALTY_ALERT, "Promotion Failed. Penalty Zone Initiated.");
        }
    }
}
