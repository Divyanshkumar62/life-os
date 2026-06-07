package com.lifeos.reward.service;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.reward.dto.RewardDefinition;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.QuestOutcomeProfile;
import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.domain.enums.QuestType;
import com.lifeos.quest.repository.QuestOutcomeProfileRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RewardCalculationService {

    private final QuestOutcomeProfileRepository outcomeRepository;

    public RewardCalculationService(QuestOutcomeProfileRepository outcomeRepository) {
        this.outcomeRepository = outcomeRepository;
    }

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

        // 3. Apply Multipliers (Elastic Matrix)
        // Low Momentum Catch-up
        if (momentum < 30) {
            multiplier += 0.20; // +20% XP
        }

        // --- ATTRIBUTE UTILITY ENGINE ---
        // STR Bonus: +1% XP per STR point for PHYSICAL quests
        // INT Bonus: +1% XP per INT point for COGNITIVE quests
        List<String> systemMessages = new ArrayList<>();
        int strValue = getAttributeValue(playerState, AttributeType.STR);
        int intValue = getAttributeValue(playerState, AttributeType.INT);

        if (quest.getQuestType() == QuestType.PHYSICAL && strValue > 0) {
            double strBonus = strValue * 0.01;
            multiplier += strBonus;
            systemMessages.add(String.format("[SYSTEM] STR stat (%d) applied +%.0f%% XP bonus to Physical quest.", strValue, strBonus * 100));
        }

        if (quest.getQuestType() == QuestType.COGNITIVE && intValue > 0) {
            double intBonus = intValue * 0.01;
            multiplier += intBonus;
            systemMessages.add(String.format("[SYSTEM] INT stat (%d) applied +%.0f%% XP bonus to Cognitive quest.", intValue, intBonus * 100));
        }

        // 4. Calculate Final XP
        long finalXp = (long) (baseXp * multiplier);
        // Guard: XP Floor
        if (finalXp < 0) finalXp = 0;

        // 5. Calculate Attributes
        // Rule: PROMOTION_EXAM does not give individual attribute growth (handled by ProgressionService)
        Map<AttributeType, Double> finalAttribs = new HashMap<>();
        if (baseAttribs != null && quest.getQuestType() != QuestType.PROMOTION_EXAM) {
            for (Map.Entry<String, Double> entry : baseAttribs.entrySet()) {
                try {
                    AttributeType type = AttributeType.valueOf(entry.getKey());
                    double val = entry.getValue();
                    // Guard: Attribute Delta Floor
                    if (val < 0) val = 0;
                    finalAttribs.put(type, val);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid attribute type received from AI: " + entry.getKey(), e);
                }
            }
        }

        // 6. Psych Logic
        int momentumBoost = 2; // Default
        if (quest.getDifficultyTier() == DifficultyTier.RED) momentumBoost = 10;

        boolean extendStreak = (quest.getDifficultyTier() != DifficultyTier.RED);

        // Confidence Correction: HARD+ difficulty AND (High Bias (>80) OR High Complacency (>70))
        boolean confidenceCorrection = false;
        if (quest.getDifficultyTier().ordinal() >= DifficultyTier.B.ordinal()) {
            if (confidence > 80 || complacency > 70) {
                confidenceCorrection = true;
            }
        }

        // 7. Gold Logic
        long baseGold = outcome.getGoldReward() != null ? outcome.getGoldReward() : 0L;

        return RewardDefinition.builder()
                .xpGain(finalXp)
                .goldGain(baseGold)
                .attributeGrowth(finalAttribs)
                .momentumBoost(momentumBoost)
                .streakExtended(extendStreak)
                .confidenceCorrection(confidenceCorrection)
                .systemMessages(systemMessages)
                .build();
    }

    /**
     * Safely retrieves the integer value of a core stat from a PlayerStateResponse.
     * Falls back to 0 if the stat cannot be found.
     */
    private int getAttributeValue(PlayerStateResponse state, AttributeType type) {
        if (state.getAttributes() == null) return 0;
        return state.getAttributes().stream()
                .filter(a -> type == a.getAttributeType())
                .mapToInt(a -> (int) a.getCurrentValue())
                .findFirst()
                .orElse(0);
    }
}
