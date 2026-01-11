package com.lifeos.player.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_temporal_state")
public class PlayerTemporalState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private PlayerIdentity player;

    @Column(name = "last_quest_completed_at")
    private LocalDateTime lastQuestCompletedAt;

    @Column(name = "active_streak_days")
    private int activeStreakDays;

    @Column(name = "rest_debt")
    private double restDebt;

    @Column(name = "burnout_risk_score")
    private double burnoutRiskScore;

    @Column(name = "consecutive_daily_failures")
    private int consecutiveDailyFailures;
    
    public PlayerTemporalState() {}

    public PlayerTemporalState(Long id, PlayerIdentity player, LocalDateTime lastQuestCompletedAt, int activeStreakDays, double restDebt, double burnoutRiskScore, int consecutiveDailyFailures) {
        this.id = id;
        this.player = player;
        this.lastQuestCompletedAt = lastQuestCompletedAt;
        this.activeStreakDays = activeStreakDays;
        this.restDebt = restDebt;
        this.burnoutRiskScore = burnoutRiskScore;
        this.consecutiveDailyFailures = consecutiveDailyFailures;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
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

    public static PlayerTemporalStateBuilder builder() {
        return new PlayerTemporalStateBuilder();
    }

    public static class PlayerTemporalStateBuilder {
        private Long id;
        private PlayerIdentity player;
        private LocalDateTime lastQuestCompletedAt;
        private int activeStreakDays;
        private double restDebt;
        private double burnoutRiskScore;
        private int consecutiveDailyFailures;

        public PlayerTemporalStateBuilder id(Long id) { this.id = id; return this; }
        public PlayerTemporalStateBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public PlayerTemporalStateBuilder lastQuestCompletedAt(LocalDateTime lastQuestCompletedAt) { this.lastQuestCompletedAt = lastQuestCompletedAt; return this; }
        public PlayerTemporalStateBuilder activeStreakDays(int activeStreakDays) { this.activeStreakDays = activeStreakDays; return this; }
        public PlayerTemporalStateBuilder restDebt(double restDebt) { this.restDebt = restDebt; return this; }
        public PlayerTemporalStateBuilder burnoutRiskScore(double burnoutRiskScore) { this.burnoutRiskScore = burnoutRiskScore; return this; }
        public PlayerTemporalStateBuilder consecutiveDailyFailures(int consecutiveDailyFailures) { this.consecutiveDailyFailures = consecutiveDailyFailures; return this; }

        public PlayerTemporalState build() {
            return new PlayerTemporalState(id, player, lastQuestCompletedAt, activeStreakDays, restDebt, burnoutRiskScore, consecutiveDailyFailures);
        }
    }
}
