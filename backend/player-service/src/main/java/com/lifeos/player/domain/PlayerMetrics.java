package com.lifeos.player.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
