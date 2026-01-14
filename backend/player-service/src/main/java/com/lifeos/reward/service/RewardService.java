package com.lifeos.reward.service;

import com.lifeos.player.service.PlayerStateService;
import com.lifeos.reward.domain.RewardRecord;
import com.lifeos.reward.dto.RewardDefinition;
import com.lifeos.reward.repository.RewardRecordRepository;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.quest.repository.QuestOutcomeProfileRepository;
import com.lifeos.streak.service.StreakService;
import com.lifeos.voice.domain.enums.SystemMessageType;
import com.lifeos.voice.event.VoiceSystemEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import com.lifeos.economy.service.EconomyService;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRecordRepository rewardRepository;
    private final RewardCalculationService calculationService;
    private final PlayerStateService playerStateService;
    private final QuestRepository questRepository;
    private final EconomyService economyService;
    private final StreakService streakService;
    private final ApplicationEventPublisher eventPublisher;
    // OutcomeRepo needed? CalculationService uses it. RewardService just passes ID/Entity.
    
    @Transactional
    public void applyReward(UUID questId, UUID playerId) {
        // 1. Idempotency Check (CRITICAL)
        if (rewardRepository.existsByQuestId(questId)) {
            // Already rewarded.
            return;
        }

        // 2. Fetch Context
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));
        
        // Fetch Player State (Read-only view for calculation)
        var playerState = playerStateService.getPlayerState(playerId);

        // PENALTY GUARD: No rewards if in Penalty Zone
        boolean inPenalty = playerState.getActiveFlags().stream()
                .anyMatch(f -> f.getFlag() == com.lifeos.player.domain.enums.StatusFlagType.PENALTY_ZONE);
        
        if (inPenalty) {
             // NO REWARDS during penalty
             org.slf4j.LoggerFactory.getLogger(RewardService.class).info("Reward suppressed: Player {} in Penalty Zone", playerId);
             return;
        }

        // 3. Calculate Reward
        RewardDefinition reward = calculationService.calculateReward(quest, playerState);

        // 4. Apply Rewards (Transactional via PlayerStateService)
        
        // XP
        if (reward.getXpGain() > 0) {
            playerStateService.addXp(playerId, reward.getXpGain());
        }

        // GOLD
        if (reward.getGoldGain() > 0) {
            long baseGold = reward.getGoldGain();
            double multiplier = streakService.getGoldMultiplier(playerId); 
            long finalGold = (long) (baseGold * (1 + multiplier));
            economyService.addGold(playerId, finalGold, "Quest Reward: " + quest.getTitle());
        }

        // Attributes
        if (reward.getAttributeGrowth() != null) {
            reward.getAttributeGrowth().forEach((type, amount) -> {
                if (amount > 0) {
                    playerStateService.updateAttribute(playerId, type, amount);
                }
            });
        }

        // Psych / Temporal
        if (reward.getMomentumBoost() != 0) {
            playerStateService.adjustMomentum(playerId, reward.getMomentumBoost());
        }
        
        if (reward.isStreakExtended()) {
            playerStateService.extendStreak(playerId);
        }
        
        // Confidence Correction? (Logic says LOWERS bias?)
        if (reward.isConfidenceCorrection()) {
            // "Correction" implies moving towards reasonable baseline or lowering overconfidence.
            // Let's assume -5 to confidence bias.
            playerStateService.updatePsychMetric(playerId, "CONFIDENCE", -5.0);
        }

        // 5. Persist Record
        RewardRecord record = RewardRecord.builder()
                .questId(questId)
                .playerId(playerId)
                .rewardPayload(reward.toPayloadMap())
                .appliedAt(LocalDateTime.now())
                .build();
        
        rewardRepository.save(record);
        
        // VOICE: QUEST_COMPLETED
        // TODO: Distinguish between Normal and Major
        eventPublisher.publishEvent(VoiceSystemEvent.builder()
                .playerId(playerId)
                .type(SystemMessageType.QUEST_COMPLETED_SIMPLE) // Default logic
                .payload(java.util.Collections.emptyMap())
                .build());
    }
}
