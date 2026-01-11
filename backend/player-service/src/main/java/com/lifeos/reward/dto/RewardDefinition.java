package com.lifeos.reward.dto;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.reward.domain.enums.RewardComponentType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data // Assuming @Data might still work for hashCode/equals if Lombok is partially working, but safer to add getters manuall yif compilation failed. 
// Compilation said "cannot find symbol method getXpGain", so @Data is failing too.
public class RewardDefinition {
    // Defines the calculated rewards to be applied
    private long xpGain;
    private long goldGain;
    private Map<AttributeType, Double> attributeGrowth;
    private int momentumBoost;
    private boolean streakExtended;
    private boolean confidenceCorrection;
    
    public RewardDefinition() {}

    public RewardDefinition(long xpGain, long goldGain, Map<AttributeType, Double> attributeGrowth, int momentumBoost, boolean streakExtended, boolean confidenceCorrection) {
        this.xpGain = xpGain;
        this.goldGain = goldGain;
        this.attributeGrowth = attributeGrowth;
        this.momentumBoost = momentumBoost;
        this.streakExtended = streakExtended;
        this.confidenceCorrection = confidenceCorrection;
    }

    // Getters and Setters
    public long getXpGain() { return xpGain; }
    public void setXpGain(long xpGain) { this.xpGain = xpGain; }
    public long getGoldGain() { return goldGain; }
    public void setGoldGain(long goldGain) { this.goldGain = goldGain; }
    public Map<AttributeType, Double> getAttributeGrowth() { return attributeGrowth; }
    public void setAttributeGrowth(Map<AttributeType, Double> attributeGrowth) { this.attributeGrowth = attributeGrowth; }
    public int getMomentumBoost() { return momentumBoost; }
    public void setMomentumBoost(int momentumBoost) { this.momentumBoost = momentumBoost; }
    public boolean isStreakExtended() { return streakExtended; }
    public void setStreakExtended(boolean streakExtended) { this.streakExtended = streakExtended; }
    public boolean isConfidenceCorrection() { return confidenceCorrection; }
    public void setConfidenceCorrection(boolean confidenceCorrection) { this.confidenceCorrection = confidenceCorrection; }

    // Helper to convert to JSON-friendly payload for persistence
    public Map<String, Object> toPayloadMap() {
        return Map.of(
            RewardComponentType.XP_GAIN.name(), xpGain,
            RewardComponentType.GOLD_GAIN.name(), goldGain,
            RewardComponentType.ATTRIBUTE_GROWTH.name(), attributeGrowth != null ? attributeGrowth : Map.of(),
            RewardComponentType.MOMENTUM_BOOST.name(), momentumBoost,
            RewardComponentType.STREAK_EXTENSION.name(), streakExtended,
            RewardComponentType.CONFIDENCE_CORRECTION.name(), confidenceCorrection
        );
    }

    public static RewardDefinitionBuilder builder() {
        return new RewardDefinitionBuilder();
    }

    public static class RewardDefinitionBuilder {
        private long xpGain;
        private long goldGain;
        private Map<AttributeType, Double> attributeGrowth;
        private int momentumBoost;
        private boolean streakExtended;
        private boolean confidenceCorrection;

        public RewardDefinitionBuilder xpGain(long xpGain) { this.xpGain = xpGain; return this; }
        public RewardDefinitionBuilder goldGain(long goldGain) { this.goldGain = goldGain; return this; }
        public RewardDefinitionBuilder attributeGrowth(Map<AttributeType, Double> attributeGrowth) { this.attributeGrowth = attributeGrowth; return this; }
        public RewardDefinitionBuilder momentumBoost(int momentumBoost) { this.momentumBoost = momentumBoost; return this; }
        public RewardDefinitionBuilder streakExtended(boolean streakExtended) { this.streakExtended = streakExtended; return this; }
        public RewardDefinitionBuilder confidenceCorrection(boolean confidenceCorrection) { this.confidenceCorrection = confidenceCorrection; return this; }

        public RewardDefinition build() {
            return new RewardDefinition(xpGain, goldGain, attributeGrowth, momentumBoost, streakExtended, confidenceCorrection);
        }
    }
}
