package com.lifeos.player.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_temporal_state")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
