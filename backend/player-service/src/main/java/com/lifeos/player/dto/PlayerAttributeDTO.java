package com.lifeos.player.dto;

import com.lifeos.player.domain.enums.AttributeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PlayerAttributeDTO {
    private AttributeType attributeType;
    private double baseValue;
    private double currentValue;
    private double growthVelocity;
    private double decayRate;
    
    public PlayerAttributeDTO() {}

    public PlayerAttributeDTO(AttributeType attributeType, double baseValue, double currentValue, double growthVelocity, double decayRate) {
        this.attributeType = attributeType;
        this.baseValue = baseValue;
        this.currentValue = currentValue;
        this.growthVelocity = growthVelocity;
        this.decayRate = decayRate;
    }

    // Getters and Setters
    public AttributeType getAttributeType() { return attributeType; }
    public void setAttributeType(AttributeType attributeType) { this.attributeType = attributeType; }
    public double getBaseValue() { return baseValue; }
    public void setBaseValue(double baseValue) { this.baseValue = baseValue; }
    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }
    public double getGrowthVelocity() { return growthVelocity; }
    public void setGrowthVelocity(double growthVelocity) { this.growthVelocity = growthVelocity; }
    public double getDecayRate() { return decayRate; }
    public void setDecayRate(double decayRate) { this.decayRate = decayRate; }

    public static PlayerAttributeDTOBuilder builder() {
        return new PlayerAttributeDTOBuilder();
    }

    public static class PlayerAttributeDTOBuilder {
        private AttributeType attributeType;
        private double baseValue;
        private double currentValue;
        private double growthVelocity;
        private double decayRate;

        public PlayerAttributeDTOBuilder attributeType(AttributeType attributeType) { this.attributeType = attributeType; return this; }
        public PlayerAttributeDTOBuilder baseValue(double baseValue) { this.baseValue = baseValue; return this; }
        public PlayerAttributeDTOBuilder currentValue(double currentValue) { this.currentValue = currentValue; return this; }
        public PlayerAttributeDTOBuilder growthVelocity(double growthVelocity) { this.growthVelocity = growthVelocity; return this; }
        public PlayerAttributeDTOBuilder decayRate(double decayRate) { this.decayRate = decayRate; return this; }

        public PlayerAttributeDTO build() {
            return new PlayerAttributeDTO(attributeType, baseValue, currentValue, growthVelocity, decayRate);
        }
    }
}
