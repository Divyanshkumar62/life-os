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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStateSnapshot {

    @Id
    @Column(name = "player_id")
    private UUID playerId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_state_flags", joinColumns = @JoinColumn(name = "player_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "flag")
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
