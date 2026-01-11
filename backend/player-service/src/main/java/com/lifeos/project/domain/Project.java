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
    private int bossKeyReward = 1;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    public Project() {}

    public Project(UUID projectId, PlayerIdentity player, String title, String description, PlayerRank rankRequirement, int difficultyTier, ProjectStatus status, int minSubtasks, int durationDays, LocalDateTime startDate, LocalDateTime hardDeadline, int bossKeyReward, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.projectId = projectId;
        this.player = player;
        this.title = title;
        this.description = description;
        this.rankRequirement = rankRequirement;
        this.difficultyTier = difficultyTier;
        this.status = status;
        this.minSubtasks = minSubtasks;
        this.durationDays = durationDays;
        this.startDate = startDate;
        this.hardDeadline = hardDeadline;
        this.bossKeyReward = bossKeyReward;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = ProjectStatus.ACTIVE;
        if (startDate == null) startDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PlayerRank getRankRequirement() { return rankRequirement; }
    public void setRankRequirement(PlayerRank rankRequirement) { this.rankRequirement = rankRequirement; }
    public int getDifficultyTier() { return difficultyTier; }
    public void setDifficultyTier(int difficultyTier) { this.difficultyTier = difficultyTier; }
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public int getMinSubtasks() { return minSubtasks; }
    public void setMinSubtasks(int minSubtasks) { this.minSubtasks = minSubtasks; }
    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getHardDeadline() { return hardDeadline; }
    public void setHardDeadline(LocalDateTime hardDeadline) { this.hardDeadline = hardDeadline; }
    public int getBossKeyReward() { return bossKeyReward; }
    public void setBossKeyReward(int bossKeyReward) { this.bossKeyReward = bossKeyReward; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public static ProjectBuilder builder() {
        return new ProjectBuilder();
    }

    public static class ProjectBuilder {
        private UUID projectId;
        private PlayerIdentity player;
        private String title;
        private String description;
        private PlayerRank rankRequirement;
        private int difficultyTier;
        private ProjectStatus status;
        private int minSubtasks;
        private int durationDays;
        private LocalDateTime startDate;
        private LocalDateTime hardDeadline;
        private int bossKeyReward = 1;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        public ProjectBuilder projectId(UUID projectId) { this.projectId = projectId; return this; }
        public ProjectBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public ProjectBuilder title(String title) { this.title = title; return this; }
        public ProjectBuilder description(String description) { this.description = description; return this; }
        public ProjectBuilder rankRequirement(PlayerRank rankRequirement) { this.rankRequirement = rankRequirement; return this; }
        public ProjectBuilder difficultyTier(int difficultyTier) { this.difficultyTier = difficultyTier; return this; }
        public ProjectBuilder status(ProjectStatus status) { this.status = status; return this; }
        public ProjectBuilder minSubtasks(int minSubtasks) { this.minSubtasks = minSubtasks; return this; }
        public ProjectBuilder durationDays(int durationDays) { this.durationDays = durationDays; return this; }
        public ProjectBuilder startDate(LocalDateTime startDate) { this.startDate = startDate; return this; }
        public ProjectBuilder hardDeadline(LocalDateTime hardDeadline) { this.hardDeadline = hardDeadline; return this; }
        public ProjectBuilder bossKeyReward(int bossKeyReward) { this.bossKeyReward = bossKeyReward; return this; }
        public ProjectBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ProjectBuilder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }

        public Project build() {
            return new Project(projectId, player, title, description, rankRequirement, difficultyTier, status, minSubtasks, durationDays, startDate, hardDeadline, bossKeyReward, createdAt, completedAt);
        }
    }
}
