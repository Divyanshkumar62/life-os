package com.lifeos.project.domain;

import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.project.domain.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerIdentity player;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerRank rankRequirement;

    @Column(nullable = false)
    private int difficultyTier; // 1-5, cosmetic for V1

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Column(nullable = false)
    private int minSubtasks; // Enforced = 5

    @Column(nullable = false)
    private int durationDays; // Enforced = 7

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime hardDeadline;

    @Column(nullable = false)
    @Builder.Default
    private int bossKeyReward = 1;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = ProjectStatus.ACTIVE;
        if (startDate == null) startDate = LocalDateTime.now();
    }
}
