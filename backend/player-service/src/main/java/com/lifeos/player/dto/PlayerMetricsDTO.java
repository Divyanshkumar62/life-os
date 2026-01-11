package com.lifeos.player.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PlayerMetricsDTO {
    private double questSuccessRate;
    private double averageQuestDifficulty;
    private int failureStreak;
    private double recoveryRate;
    
    public PlayerMetricsDTO() {}

    public PlayerMetricsDTO(double questSuccessRate, double averageQuestDifficulty, int failureStreak, double recoveryRate) {
        this.questSuccessRate = questSuccessRate;
        this.averageQuestDifficulty = averageQuestDifficulty;
        this.failureStreak = failureStreak;
        this.recoveryRate = recoveryRate;
    }

    // Getters and Setters
    public double getQuestSuccessRate() { return questSuccessRate; }
    public void setQuestSuccessRate(double questSuccessRate) { this.questSuccessRate = questSuccessRate; }
    public double getAverageQuestDifficulty() { return averageQuestDifficulty; }
    public void setAverageQuestDifficulty(double averageQuestDifficulty) { this.averageQuestDifficulty = averageQuestDifficulty; }
    public int getFailureStreak() { return failureStreak; }
    public void setFailureStreak(int failureStreak) { this.failureStreak = failureStreak; }
    public double getRecoveryRate() { return recoveryRate; }
    public void setRecoveryRate(double recoveryRate) { this.recoveryRate = recoveryRate; }

    public static PlayerMetricsDTOBuilder builder() {
        return new PlayerMetricsDTOBuilder();
    }

    public static class PlayerMetricsDTOBuilder {
        private double questSuccessRate;
        private double averageQuestDifficulty;
        private int failureStreak;
        private double recoveryRate;

        public PlayerMetricsDTOBuilder questSuccessRate(double questSuccessRate) { this.questSuccessRate = questSuccessRate; return this; }
        public PlayerMetricsDTOBuilder averageQuestDifficulty(double averageQuestDifficulty) { this.averageQuestDifficulty = averageQuestDifficulty; return this; }
        public PlayerMetricsDTOBuilder failureStreak(int failureStreak) { this.failureStreak = failureStreak; return this; }
        public PlayerMetricsDTOBuilder recoveryRate(double recoveryRate) { this.recoveryRate = recoveryRate; return this; }

        public PlayerMetricsDTO build() {
            return new PlayerMetricsDTO(questSuccessRate, averageQuestDifficulty, failureStreak, recoveryRate);
        }
    }
}
