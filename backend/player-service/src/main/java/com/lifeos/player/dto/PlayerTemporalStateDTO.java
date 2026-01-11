package com.lifeos.player.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
public class PlayerTemporalStateDTO {
    private LocalDateTime lastQuestCompletedAt;
    private int activeStreakDays;
    private double restDebt;
    private double burnoutRiskScore;
    private int consecutiveDailyFailures;
    
    public PlayerTemporalStateDTO() {}

    public PlayerTemporalStateDTO(LocalDateTime lastQuestCompletedAt, int activeStreakDays, double restDebt, double burnoutRiskScore, int consecutiveDailyFailures) {
        this.lastQuestCompletedAt = lastQuestCompletedAt;
        this.activeStreakDays = activeStreakDays;
        this.restDebt = restDebt;
        this.burnoutRiskScore = burnoutRiskScore;
        this.consecutiveDailyFailures = consecutiveDailyFailures;
    }

    // Getters and Setters
    public LocalDateTime getLastQuestCompletedAt() { return lastQuestCompletedAt; }
    public void setLastQuestCompletedAt(LocalDateTime lastQuestCompletedAt) { this.lastQuestCompletedAt = lastQuestCompletedAt; }
    public int getActiveStreakDays() { return activeStreakDays; }
    public void setActiveStreakDays(int activeStreakDays) { this.activeStreakDays = activeStreakDays; }
    public double getRestDebt() { return restDebt; }
    public void setRestDebt(double restDebt) { this.restDebt = restDebt; }
    public double getBurnoutRiskScore() { return burnoutRiskScore; }
    public void setBurnoutRiskScore(double burnoutRiskScore) { this.burnoutRiskScore = burnoutRiskScore; }
    public int getConsecutiveDailyFailures() { return consecutiveDailyFailures; }
    public void setConsecutiveDailyFailures(int consecutiveDailyFailures) { this.consecutiveDailyFailures = consecutiveDailyFailures; }

    public static PlayerTemporalStateDTOBuilder builder() {
        return new PlayerTemporalStateDTOBuilder();
    }

    public static class PlayerTemporalStateDTOBuilder {
        private LocalDateTime lastQuestCompletedAt;
        private int activeStreakDays;
        private double restDebt;
        private double burnoutRiskScore;
        private int consecutiveDailyFailures;

        public PlayerTemporalStateDTOBuilder lastQuestCompletedAt(LocalDateTime lastQuestCompletedAt) { this.lastQuestCompletedAt = lastQuestCompletedAt; return this; }
        public PlayerTemporalStateDTOBuilder activeStreakDays(int activeStreakDays) { this.activeStreakDays = activeStreakDays; return this; }
        public PlayerTemporalStateDTOBuilder restDebt(double restDebt) { this.restDebt = restDebt; return this; }
        public PlayerTemporalStateDTOBuilder burnoutRiskScore(double burnoutRiskScore) { this.burnoutRiskScore = burnoutRiskScore; return this; }
        public PlayerTemporalStateDTOBuilder consecutiveDailyFailures(int consecutiveDailyFailures) { this.consecutiveDailyFailures = consecutiveDailyFailures; return this; }

        public PlayerTemporalStateDTO build() {
            return new PlayerTemporalStateDTO(lastQuestCompletedAt, activeStreakDays, restDebt, burnoutRiskScore, consecutiveDailyFailures);
        }
    }
}
