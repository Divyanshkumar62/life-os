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
import com.lifeos.quest.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
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
        
        System.out.println(String.format("Promotion requested: %s -> %s. Keys consumed: %d", 
            currentRank, currentRank.next(), template.getBossKeyCost()));
        
        return attempt;
    }

    /**
     * Processes the outcome of a Promotion Quest.
     * CRITICAL: Does NOT trigger Penalty Zone on failure.
     */
    @Transactional
    public void processPromotionOutcome(UUID attemptId, boolean success) {
        RankExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Exam attempt not found"));
        
        if (attempt.getStatus() != ExamStatus.UNLOCKED) {
            throw new IllegalStateException("Exam attempt is not in UNLOCKED state");
        }
        
        attempt.setCompletedAt(LocalDateTime.now());
        
        if (success) {
            // SUCCESS: Promote Rank, Unfreeze XP
            attempt.setStatus(ExamStatus.PASSED);
            examAttemptRepository.save(attempt);
            
            UUID playerId = attempt.getPlayer().getPlayerId();
            playerStateService.promoteRank(playerId);
            
            System.out.println("Rank Advanced for player " + playerId);
        } else {
            // FAILURE: Mark attempt as failed
            // Keys are LOST (already consumed)
            // XP remains FROZEN
            // NO Penalty Zone (CRITICAL FIX)
            attempt.setStatus(ExamStatus.FAILED);
            examAttemptRepository.save(attempt);
            
            System.out.println("Promotion Failed. Keys lost. Must earn new key via Project.");
        }
    }
}
