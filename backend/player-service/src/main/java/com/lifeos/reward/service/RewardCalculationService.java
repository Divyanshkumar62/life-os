package com.lifeos.reward.service;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.reward.dto.RewardDefinition;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.QuestOutcomeProfile;
import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.repository.QuestOutcomeProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RewardCalculationService {

    private final QuestOutcomeProfileRepository outcomeRepository;

    public RewardDefinition calculateReward(Quest quest, PlayerStateResponse playerState) {
        // 1. Fetch Base Rewards
        QuestOutcomeProfile outcome = outcomeRepository.findByQuestQuestId(quest.getQuestId())
                .orElseThrow(() -> new IllegalArgumentException("Outcome profile missing for quest: " + quest.getQuestId()));

        long baseXp = outcome.getSuccessXp();
        Map<String, Double> baseAttribs = outcome.getAttributeDeltaJson();

        // 2. Elastic Logic Variables
        double multiplier = 1.0;
        int momentum = playerState.getPsychState().getMomentum();
        int complacency = playerState.getPsychState().getComplacency();
        int confidence = playerState.getPsychState().getConfidenceBias();
        int streak = playerState.getTemporalState().getActiveStreakDays();

        // 3. Apply Multipliers (Elastic Matrix)
        
        // Low Momentum Catch-up
        if (momentum < 30) {
            multiplier += 0.20; // +20% XP
        }

        // High Complacency Penalty
        if (complacency > 70) {
            multiplier -= 0.30; // -30% XP
        }

        // Long Streak Diminishing Returns (Encourage harder quests)
        if (streak > 10) {
            multiplier -= 0.10; // -10% XP
        }

        // 4. Calculate Final XP
        long finalXp = (long) (baseXp * multiplier);
        // Guard: XP Floor
        if (finalXp < 0) finalXp = 0;

        // 5. Calculate Attributes
        // Low Momentum -> Boost Attributes too? Spec says "XP + Attribute Boost".
        // Let's add 10% boost to attributes if momentum < 30
        Map<AttributeType, Double> finalAttribs = new HashMap<>();
        if (baseAttribs != null) {
            for (Map.Entry<String, Double> entry : baseAttribs.entrySet()) {
                try {
                    AttributeType type = AttributeType.valueOf(entry.getKey());
                    double val = entry.getValue();
                    if (momentum < 30) {
                        val *= 1.10;
                    }
                    // Guard: Attribute Delta Floor
                    if (val < 0) val = 0;
                    finalAttribs.put(type, val);
                } catch (IllegalArgumentException e) {
                    // Ignore invalid enum keys
                }
            }
        }

        // 6. Psych Logic
        // Momentum Boost: Standard small boost, maybe higher if difficulty is high
        int momentumBoost = 2; // Default
        if (quest.getDifficultyTier() == DifficultyTier.RED) momentumBoost = 10;
        // Clamp 0-100 handling done in storage usually, but here just defining delta.

        // Streak Extension
        // Rule: Only if NOT Ego Breaker (RED?) and not barely passing (assumed binary success for now).
        // Let's assume RED tier is Ego Breaker.
        boolean extendStreak = (quest.getDifficultyTier() != DifficultyTier.RED);

        // Confidence Correction
        // Rule: HARD+ difficulty AND (High Bias (>80) OR High Complacency (>70))
        boolean confidenceCorrection = false;
        if (quest.getDifficultyTier().ordinal() >= DifficultyTier.B.ordinal()) { 
            if (confidence > 80 || complacency > 70) {
                confidenceCorrection = true;
            }
        }

        // 7. Gold Logic
        long baseGold = outcome.getGoldReward();
        // V1: Direct pass-through. No logic mapping for now unless Difficulty mapped in Profile creation.
        // Assuming Profile has correct value.
        
        return RewardDefinition.builder()
                .xpGain(finalXp)
                .goldGain(baseGold)
                .attributeGrowth(finalAttribs)
                .momentumBoost(momentumBoost)
                .streakExtended(extendStreak)
                .confidenceCorrection(confidenceCorrection)
                .build();
    }
}
