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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgressionService {

    private final PlayerStateService playerStateService;
    private final PenaltyService penaltyService;
    private final QuestRepository questRepository;
    private final UserBossKeyRepository bossKeyRepository;
    private final RankExamAttemptRepository examAttemptRepository;
    private final ApplicationEventPublisher eventPublisher;

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
        if (!canRequestPromotion(playerId)) {
            throw new IllegalStateException("Player is not eligible for promotion.");
        }
        
        var state = playerStateService.getPlayerState(playerId);
        var currentRank = state.getProgression().getRank();
        var template = com.lifeos.progression.domain.RankTransitionTemplate.from(currentRank);
        
        // Consume rank-specific Boss Keys
        UserBossKey bossKey = bossKeyRepository.findByPlayerPlayerIdAndRank(playerId, currentRank)
                .orElseThrow(() -> new IllegalStateException("Boss keys not found"));
        
        if (bossKey.getKeyCount() < template.getBossKeyCost()) {
            throw new IllegalStateException("Insufficient boss keys");
        }
        
        bossKey.setKeyCount(bossKey.getKeyCount() - template.getBossKeyCost());
        bossKeyRepository.save(bossKey);
        
        // Create RankExamAttempt record
        RankExamAttempt attempt = RankExamAttempt.builder()
                .player(state.getIdentity() != null ? null : null) // TODO: Fetch full player entity
                .fromRank(currentRank)
                .toRank(currentRank.next())
                .status(ExamStatus.UNLOCKED)
                .requiredKeys(template.getBossKeyCost())
                .consumedKeys(template.getBossKeyCost())
                .attemptNumber(1) // TODO: Calculate actual attempt number
                .unlockedAt(LocalDateTime.now())
                .build();
        
        attempt = examAttemptRepository.save(attempt);
        
        // SPAWN PROMOTION QUEST
        Quest examQuest = Quest.builder()
                .player(state.getIdentity() != null ? com.lifeos.player.domain.PlayerIdentity.builder().playerId(playerId).build() : null)
                .title("Rank Exam: " + currentRank + " -> " + currentRank.next())
                .description("Prove your worth. Failure results in immediate Penalty Zone.")
                .category(com.lifeos.quest.domain.enums.QuestCategory.MAIN) // or SYSTEM_DAILY? MAIN seems appropriate for Exam.
                .difficultyTier(com.lifeos.quest.domain.enums.DifficultyTier.RED) // Exams are hard
                .state(com.lifeos.quest.domain.enums.QuestState.ACTIVE)
                .priority(com.lifeos.quest.domain.enums.Priority.CRITICAL)
                .questType(com.lifeos.quest.domain.enums.QuestType.PROMOTION_EXAM)
                .assignedAt(LocalDateTime.now())
                .startsAt(LocalDateTime.now())
                .deadlineAt(LocalDateTime.now().plusDays(7)) // Default 7 days, should come from Template ideally
                .systemMutable(false)
                .primaryAttribute(null) // No single stats, holistic
                .build();
        
        questRepository.save(examQuest);
        
        System.out.println(String.format("Promotion requested: %s -> %s. Keys consumed: %d. Exam Quest Spawned.", 
            currentRank, currentRank.next(), template.getBossKeyCost()));
            
        // VOICE: PROMOTION_UNLOCKED (Quest is now Active)
        eventPublisher.publishEvent(VoiceSystemEvent.builder()
                .playerId(playerId)
                .type(SystemMessageType.PROMOTION_UNLOCKED)
                .build());
        
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
            
            System.out.println("Rank Advanced for player " + playerId);
            
            // VOICE: PROMOTION_PASSED
            eventPublisher.publishEvent(VoiceSystemEvent.builder()
                        .playerId(playerId)
                        .type(SystemMessageType.PROMOTION_PASSED)
                        .build());
        } else {
            // FAILURE: Mark attempt as failed
            // Keys are LOST (already consumed)
            // XP remains FROZEN
            // STRICT PENALTY: Enter Penalty Zone
            attempt.setStatus(ExamStatus.FAILED);
            examAttemptRepository.save(attempt);
            
            penaltyService.enterPenaltyZone(playerId, "Failed Rank Exam: " + attempt.getFromRank() + " -> " + attempt.getToRank());
            
            System.out.println("Promotion Failed. ENTERING PENALTY ZONE.");
            
            // VOICE: PROMOTION_FAILED
            eventPublisher.publishEvent(VoiceSystemEvent.builder()
                        .playerId(playerId)
                        .type(SystemMessageType.PROMOTION_FAILED)
                        .build());
        }
    }
}
