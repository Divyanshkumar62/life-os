package com.lifeos.penalty.domain;

import com.lifeos.penalty.domain.enums.PenaltySeverity;
import com.lifeos.penalty.domain.enums.PenaltyType;
import com.lifeos.player.domain.enums.AttributeType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Transient object returned by calculation service.
 * Not persisted directly.
 */
@Data
public class PenaltyDefinition {
    // Defines calculated penalty to be applied on failure
    private PenaltyType type;
    private PenaltySeverity severity;
    private long xpDeduction;
    private AttributeType debuffAttribute;
    private double debuffAmount;
    private LocalDateTime debuffExpiresAt;
    private boolean resetStreak;
    private boolean enterPenaltyZone;
    
    public PenaltyDefinition() {}

    public PenaltyDefinition(PenaltyType type, PenaltySeverity severity, long xpDeduction, AttributeType debuffAttribute, double debuffAmount, LocalDateTime debuffExpiresAt, boolean resetStreak, boolean enterPenaltyZone) {
        this.type = type;
        this.severity = severity;
        this.xpDeduction = xpDeduction;
        this.debuffAttribute = debuffAttribute;
        this.debuffAmount = debuffAmount;
        this.debuffExpiresAt = debuffExpiresAt;
        this.resetStreak = resetStreak;
        this.enterPenaltyZone = enterPenaltyZone;
    }
    
    // Getters and Setters
    public PenaltyType getType() { return type; }
    public void setType(PenaltyType type) { this.type = type; }
    public PenaltySeverity getSeverity() { return severity; }
    public void setSeverity(PenaltySeverity severity) { this.severity = severity; }
    public long getXpDeduction() { return xpDeduction; }
    public void setXpDeduction(long xpDeduction) { this.xpDeduction = xpDeduction; }
    public AttributeType getDebuffAttribute() { return debuffAttribute; }
    public void setDebuffAttribute(AttributeType debuffAttribute) { this.debuffAttribute = debuffAttribute; }
    public double getDebuffAmount() { return debuffAmount; }
    public void setDebuffAmount(double debuffAmount) { this.debuffAmount = debuffAmount; }
    public LocalDateTime getDebuffExpiresAt() { return debuffExpiresAt; }
    public void setDebuffExpiresAt(LocalDateTime debuffExpiresAt) { this.debuffExpiresAt = debuffExpiresAt; }
    public boolean isResetStreak() { return resetStreak; }
    public void setResetStreak(boolean resetStreak) { this.resetStreak = resetStreak; }
    public boolean isEnterPenaltyZone() { return enterPenaltyZone; }
    public void setEnterPenaltyZone(boolean enterPenaltyZone) { this.enterPenaltyZone = enterPenaltyZone; }

    public static PenaltyDefinitionBuilder builder() {
        return new PenaltyDefinitionBuilder();
    }

    public static class PenaltyDefinitionBuilder {
        private PenaltyType type;
        private PenaltySeverity severity;
        private long xpDeduction;
        private AttributeType debuffAttribute;
        private double debuffAmount;
        private LocalDateTime debuffExpiresAt;
        private boolean resetStreak;
        private boolean enterPenaltyZone;

        public PenaltyDefinitionBuilder type(PenaltyType type) { this.type = type; return this; }
        public PenaltyDefinitionBuilder severity(PenaltySeverity severity) { this.severity = severity; return this; }
        public PenaltyDefinitionBuilder xpDeduction(long xpDeduction) { this.xpDeduction = xpDeduction; return this; }
        public PenaltyDefinitionBuilder debuffAttribute(AttributeType debuffAttribute) { this.debuffAttribute = debuffAttribute; return this; }
        public PenaltyDefinitionBuilder debuffAmount(double debuffAmount) { this.debuffAmount = debuffAmount; return this; }
        public PenaltyDefinitionBuilder debuffExpiresAt(LocalDateTime debuffExpiresAt) { this.debuffExpiresAt = debuffExpiresAt; return this; }
        public PenaltyDefinitionBuilder resetStreak(boolean resetStreak) { this.resetStreak = resetStreak; return this; }
        public PenaltyDefinitionBuilder enterPenaltyZone(boolean enterPenaltyZone) { this.enterPenaltyZone = enterPenaltyZone; return this; }

        public PenaltyDefinition build() {
            return new PenaltyDefinition(type, severity, xpDeduction, debuffAttribute, debuffAmount, debuffExpiresAt, resetStreak, enterPenaltyZone);
        }
    }
}
