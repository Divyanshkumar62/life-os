package com.lifeos.player.state;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "player_state_snapshot")
public class PlayerStateSnapshot {
    
    public PlayerStateSnapshot() {}

    public PlayerStateSnapshot(UUID playerId, Set<PlayerFlag> activeFlags, int currentStreak, boolean streakActive, boolean inPenaltyZone) {
        this.playerId = playerId;
        this.activeFlags = activeFlags != null ? activeFlags : new java.util.HashSet<>();
        this.currentStreak = currentStreak;
        this.streakActive = streakActive;
        this.inPenaltyZone = inPenaltyZone;
    }

    // Getters
    public UUID getPlayerId() { return playerId; }
    public Set<PlayerFlag> getActiveFlags() { return activeFlags; }
    public int getCurrentStreak() { return currentStreak; }
    public boolean isStreakActive() { return streakActive; }
    public boolean isInPenaltyZone() { return inPenaltyZone; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }

    // Setters
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public void setActiveFlags(Set<PlayerFlag> activeFlags) { this.activeFlags = activeFlags; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public void setStreakActive(boolean streakActive) { this.streakActive = streakActive; }
    public void setInPenaltyZone(boolean inPenaltyZone) { this.inPenaltyZone = inPenaltyZone; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }

    // Simple Builder
    public static class PlayerStateSnapshotBuilder {
        private UUID playerId;
        private Set<PlayerFlag> activeFlags = new java.util.HashSet<>();
        private int currentStreak;
        private boolean streakActive;
        private boolean inPenaltyZone;

        public PlayerStateSnapshotBuilder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public PlayerStateSnapshotBuilder activeFlags(Set<PlayerFlag> activeFlags) { this.activeFlags = activeFlags; return this; }
        public PlayerStateSnapshotBuilder currentStreak(int currentStreak) { this.currentStreak = currentStreak; return this; }
        public PlayerStateSnapshotBuilder streakActive(boolean streakActive) { this.streakActive = streakActive; return this; }
        public PlayerStateSnapshotBuilder inPenaltyZone(boolean inPenaltyZone) { this.inPenaltyZone = inPenaltyZone; return this; }

        public PlayerStateSnapshot build() {
            return new PlayerStateSnapshot(playerId, activeFlags, currentStreak, streakActive, inPenaltyZone);
        }
    }

    public static PlayerStateSnapshotBuilder builder() {
        return new PlayerStateSnapshotBuilder();
    }

    @Id
    @Column(name = "player_id")
    private UUID playerId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_state_flags", joinColumns = @JoinColumn(name = "player_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "flag", columnDefinition = "VARCHAR(255)")
    @Builder.Default
    private Set<PlayerFlag> activeFlags = new HashSet<>();

    @Column(name = "current_streak")
    private int currentStreak;

    @Column(name = "streak_active")
    private boolean streakActive;

    @Column(name = "in_penalty_zone")
    private boolean inPenaltyZone;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
