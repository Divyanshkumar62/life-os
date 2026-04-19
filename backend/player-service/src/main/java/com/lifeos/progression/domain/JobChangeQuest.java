package com.lifeos.progression.domain;

import com.lifeos.player.domain.PlayerIdentity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_change_quest")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobChangeQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID questId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerIdentity player;

    @Column(name = "day_number", nullable = false)
    private int day; // 1, 2, or 3

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int estimatedMinutes;

    @Column(nullable = false)
    private String difficulty; // LOW, HIGH, BOSS, MEDIUM

    @Column(nullable = false)
    private String questType; // PHYSICAL, COGNITIVE, DAILY_HABIT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobChangeQuestState state;

    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON for boss_room, volume_test, etc.

    public enum JobChangeQuestState {
        PENDING,
        COMPLETED,
        FAILED
    }
}
