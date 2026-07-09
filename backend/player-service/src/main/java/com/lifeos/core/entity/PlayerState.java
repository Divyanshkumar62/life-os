package com.lifeos.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "player_state")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerState {

    @Id
    @Column(name = "player_id")
    private UUID playerId;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "level", nullable = false)
    @Builder.Default
    private int level = 1;

    @Column(name = "xp", nullable = false)
    @Builder.Default
    private long xp = 0;

    @Column(name = "gold_balance", nullable = false)
    @Builder.Default
    private long goldBalance = 0;

    @Column(name = "gold_debt", nullable = false)
    @Builder.Default
    private long goldDebt = 0;

    @Column(name = "player_rank", length = 1, nullable = false)
    @Builder.Default
    private String playerRank = "E";

    @Column(name = "stat_str", nullable = false)
    @Builder.Default
    private int statStr = 10;

    @Column(name = "stat_vit", nullable = false)
    @Builder.Default
    private int statVit = 10;

    @Column(name = "stat_int", nullable = false)
    @Builder.Default
    private int statInt = 10;

    @Column(name = "stat_agi", nullable = false)
    @Builder.Default
    private int statAgi = 10;

    @Column(name = "stat_sen", nullable = false)
    @Builder.Default
    private int statSen = 10;

    @Column(name = "free_stat_points", nullable = false)
    @Builder.Default
    private int freeStatPoints = 0;

    @Column(name = "onboarding_completed", nullable = false)
    @Builder.Default
    private boolean onboardingCompleted = false;

    @Column(name = "timezone_offset", nullable = false)
    @Builder.Default
    private int timezoneOffset = 0;

    @Column(name = "scheduled_wakeup", nullable = false)
    @Builder.Default
    private LocalTime scheduledWakeup = LocalTime.of(8, 0);

    @Column(name = "wakeup_lock_until")
    private LocalDateTime wakeupLockUntil;

    @Column(name = "job_class", length = 20)
    private String jobClass;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
