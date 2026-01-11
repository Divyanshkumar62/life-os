package com.lifeos.player.domain;

import com.lifeos.player.domain.enums.AttributeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_attribute", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"player_id", "attribute_type"})
})
public class PlayerAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerIdentity player;

    @Enumerated(EnumType.STRING)
    @Column(name = "attribute_type", nullable = false)
    private AttributeType attributeType;

    @Column(name = "base_value", nullable = false)
    private double baseValue;

    @Column(name = "current_value", nullable = false)
    private double currentValue;

    @Column(name = "growth_velocity")
    private double growthVelocity;

    @Column(name = "decay_rate")
    private double decayRate;
    
    public PlayerAttribute() {}

    public PlayerAttribute(Long id, PlayerIdentity player, AttributeType attributeType, double baseValue, double currentValue, double growthVelocity, double decayRate) {
        this.id = id;
        this.player = player;
        this.attributeType = attributeType;
        this.baseValue = baseValue;
        this.currentValue = currentValue;
        this.growthVelocity = growthVelocity;
        this.decayRate = decayRate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
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

    public static PlayerAttributeBuilder builder() {
        return new PlayerAttributeBuilder();
    }

    public static class PlayerAttributeBuilder {
        private Long id;
        private PlayerIdentity player;
        private AttributeType attributeType;
        private double baseValue;
        private double currentValue;
        private double growthVelocity;
        private double decayRate;

        public PlayerAttributeBuilder id(Long id) { this.id = id; return this; }
        public PlayerAttributeBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public PlayerAttributeBuilder attributeType(AttributeType attributeType) { this.attributeType = attributeType; return this; }
        public PlayerAttributeBuilder baseValue(double baseValue) { this.baseValue = baseValue; return this; }
        public PlayerAttributeBuilder currentValue(double currentValue) { this.currentValue = currentValue; return this; }
        public PlayerAttributeBuilder growthVelocity(double growthVelocity) { this.growthVelocity = growthVelocity; return this; }
        public PlayerAttributeBuilder decayRate(double decayRate) { this.decayRate = decayRate; return this; }

        public PlayerAttribute build() {
            return new PlayerAttribute(id, player, attributeType, baseValue, currentValue, growthVelocity, decayRate);
        }
    }
}
