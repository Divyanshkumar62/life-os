package com.lifeos.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dungeon_project")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DungeonProject {

    @Id
    @Column(name = "dungeon_id")
    private UUID dungeonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerState player;

    @Column(name = "dungeon_rank", length = 1, nullable = false)
    private String dungeonRank;

    @Column(name = "dungeon_status", length = 20, nullable = false)
    private String dungeonStatus;

    @Column(name = "total_floors", nullable = false)
    @Builder.Default
    private int totalFloors = 5;

    @Column(name = "completed_floors", nullable = false)
    @Builder.Default
    private int completedFloors = 0;

    @Column(name = "mutilate_count", nullable = false)
    @Builder.Default
    private int mutilateCount = 0;

    @Column(name = "hard_deadline", nullable = false)
    private LocalDateTime hardDeadline;

    @Column(name = "speedrun_eligible", nullable = false)
    @Builder.Default
    private boolean speedrunEligible = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
