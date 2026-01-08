package com.lifeos.reward.dto;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.reward.domain.enums.RewardComponentType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RewardDefinition {
    // Defines the calculated rewards to be applied
    private long xpGain;
    private Map<AttributeType, Double> attributeGrowth;
    private int momentumBoost;
    private boolean streakExtended;
    private boolean confidenceCorrection;
    
    // Helper to convert to JSON-friendly payload for persistence
    public Map<String, Object> toPayloadMap() {
        return Map.of(
            RewardComponentType.XP_GAIN.name(), xpGain,
            RewardComponentType.ATTRIBUTE_GROWTH.name(), attributeGrowth,
            RewardComponentType.MOMENTUM_BOOST.name(), momentumBoost,
            RewardComponentType.STREAK_EXTENSION.name(), streakExtended,
            RewardComponentType.CONFIDENCE_CORRECTION.name(), confidenceCorrection
        );
    }
}
