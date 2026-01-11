package com.lifeos.player.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_metrics")
public class PlayerMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private PlayerIdentity player;

    @Column(name = "quest_success_rate")
    private double questSuccessRate;

    @Column(name = "average_quest_difficulty")
    private double averageQuestDifficulty;

    @Column(name = "failure_streak")
    private int failureStreak;

    @Column(name = "recovery_rate")
    private double recoveryRate;
    
    public PlayerMetrics() {}

    public PlayerMetrics(Long id, PlayerIdentity player, double questSuccessRate, double averageQuestDifficulty, int failureStreak, double recoveryRate) {
        this.id = id;
        this.player = player;
        this.questSuccessRate = questSuccessRate;
        this.averageQuestDifficulty = averageQuestDifficulty;
        this.failureStreak = failureStreak;
        this.recoveryRate = recoveryRate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
    public double getQuestSuccessRate() { return questSuccessRate; }
    public void setQuestSuccessRate(double questSuccessRate) { this.questSuccessRate = questSuccessRate; }
    public double getAverageQuestDifficulty() { return averageQuestDifficulty; }
    public void setAverageQuestDifficulty(double averageQuestDifficulty) { this.averageQuestDifficulty = averageQuestDifficulty; }
    public int getFailureStreak() { return failureStreak; }
    public void setFailureStreak(int failureStreak) { this.failureStreak = failureStreak; }
    public double getRecoveryRate() { return recoveryRate; }
    public void setRecoveryRate(double recoveryRate) { this.recoveryRate = recoveryRate; }

    public static PlayerMetricsBuilder builder() {
        return new PlayerMetricsBuilder();
    }

    public static class PlayerMetricsBuilder {
        private Long id;
        private PlayerIdentity player;
        private double questSuccessRate;
        private double averageQuestDifficulty;
        private int failureStreak;
        private double recoveryRate;

        public PlayerMetricsBuilder id(Long id) { this.id = id; return this; }
        public PlayerMetricsBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public PlayerMetricsBuilder questSuccessRate(double questSuccessRate) { this.questSuccessRate = questSuccessRate; return this; }
        public PlayerMetricsBuilder averageQuestDifficulty(double averageQuestDifficulty) { this.averageQuestDifficulty = averageQuestDifficulty; return this; }
        public PlayerMetricsBuilder failureStreak(int failureStreak) { this.failureStreak = failureStreak; return this; }
        public PlayerMetricsBuilder recoveryRate(double recoveryRate) { this.recoveryRate = recoveryRate; return this; }

        public PlayerMetrics build() {
            return new PlayerMetrics(id, player, questSuccessRate, averageQuestDifficulty, failureStreak, recoveryRate);
        }
    }
}
