package com.lifeos.player.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "player_identity")
public class PlayerIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "player_id")
    private UUID playerId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "system_version")
    private String systemVersion;

    @Column(name = "onboarding_completed")
    private boolean onboardingCompleted = false;

    @Column(name = "last_daily_reset")
    private LocalDateTime lastDailyReset;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "notifications_enabled")
    private boolean notificationsEnabled = true;

    @Column(name = "biggest_challenge", length = 255)
    private String biggestChallenge;

    @Column(name = "six_month_goal", length = 255)
    private String sixMonthGoal;

    @Column(name = "job_class")
    private String jobClass;

    @Column(name = "class_multiplier", columnDefinition = "JSON")
    private String classMultiplier;

    @Column(name = "class_unlocked_at")
    private LocalDateTime classUnlockedAt;

    @Column(name = "job_change_status")
    private String jobChangeStatus = "NOT_TRIGGERED";

    @Column(name = "job_change_cooldown_until")
    private LocalDateTime jobChangeCooldownUntil;

    @Column(name = "xp_frozen")
    private boolean xpFrozen = false;

    @Column(name = "red_gate_active")
    private boolean redGateActive = false;

    @Column(name = "red_gate_expires_at")
    private LocalDateTime redGateExpiresAt;

    @Column(name = "red_gate_quest_id")
    private UUID redGateQuestId;

    public PlayerIdentity() {}

    public PlayerIdentity(UUID playerId, String username, LocalDateTime createdAt, String systemVersion, boolean onboardingCompleted, LocalDateTime lastDailyReset) {
        this.playerId = playerId;
        this.username = username;
        this.createdAt = createdAt;
        this.systemVersion = systemVersion;
        this.onboardingCompleted = onboardingCompleted;
        this.lastDailyReset = lastDailyReset;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters
    public UUID getPlayerId() { return playerId; }
    public String getUsername() { return username; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getSystemVersion() { return systemVersion; }
    public boolean isOnboardingCompleted() { return onboardingCompleted; }
    public LocalDateTime getLastDailyReset() { return lastDailyReset; }
    public String getFcmToken() { return fcmToken; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public String getBiggestChallenge() { return biggestChallenge; }
    public String getSixMonthGoal() { return sixMonthGoal; }
    public String getJobClass() { return jobClass; }
    public String getClassMultiplier() { return classMultiplier; }
    public LocalDateTime getClassUnlockedAt() { return classUnlockedAt; }
    public String getJobChangeStatus() { return jobChangeStatus; }
    public LocalDateTime getJobChangeCooldownUntil() { return jobChangeCooldownUntil; }
    public boolean isXpFrozen() { return xpFrozen; }
    public boolean isRedGateActive() { return redGateActive; }
    public LocalDateTime getRedGateExpiresAt() { return redGateExpiresAt; }
    public UUID getRedGateQuestId() { return redGateQuestId; }

    // Setters
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public void setOnboardingCompleted(boolean onboardingCompleted) { this.onboardingCompleted = onboardingCompleted; }
    public void setLastDailyReset(LocalDateTime lastDailyReset) { this.lastDailyReset = lastDailyReset; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
    public void setBiggestChallenge(String biggestChallenge) { this.biggestChallenge = biggestChallenge; }
    public void setSixMonthGoal(String sixMonthGoal) { this.sixMonthGoal = sixMonthGoal; }
    public void setJobClass(String jobClass) { this.jobClass = jobClass; }
    public void setClassMultiplier(String classMultiplier) { this.classMultiplier = classMultiplier; }
    public void setClassUnlockedAt(LocalDateTime classUnlockedAt) { this.classUnlockedAt = classUnlockedAt; }
    public void setJobChangeStatus(String jobChangeStatus) { this.jobChangeStatus = jobChangeStatus; }
    public void setJobChangeCooldownUntil(LocalDateTime jobChangeCooldownUntil) { this.jobChangeCooldownUntil = jobChangeCooldownUntil; }
    public void setXpFrozen(boolean xpFrozen) { this.xpFrozen = xpFrozen; }
    public void setRedGateActive(boolean redGateActive) { this.redGateActive = redGateActive; }
    public void setRedGateExpiresAt(LocalDateTime redGateExpiresAt) { this.redGateExpiresAt = redGateExpiresAt; }
    public void setRedGateQuestId(UUID redGateQuestId) { this.redGateQuestId = redGateQuestId; }

    // Builder
    public static PlayerIdentityBuilder builder() {
        return new PlayerIdentityBuilder();
    }

    public static class PlayerIdentityBuilder {
        private UUID playerId;
        private String username;
        private LocalDateTime createdAt;
        private String systemVersion;
        private LocalDateTime lastDailyReset;
        private String fcmToken;
        private boolean notificationsEnabled = true;
        private String biggestChallenge;
        private String sixMonthGoal;
        private String jobClass;
        private String classMultiplier;
        private LocalDateTime classUnlockedAt;
        private String jobChangeStatus = "NOT_TRIGGERED";
        private LocalDateTime jobChangeCooldownUntil;
        private boolean xpFrozen = false;
        private boolean onboardingCompleted;
        private boolean redGateActive = false;
        private LocalDateTime redGateExpiresAt;
        private UUID redGateQuestId;

        public PlayerIdentityBuilder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public PlayerIdentityBuilder username(String username) { this.username = username; return this; }
        public PlayerIdentityBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PlayerIdentityBuilder systemVersion(String systemVersion) { this.systemVersion = systemVersion; return this; }
        public PlayerIdentityBuilder onboardingCompleted(boolean onboardingCompleted) { this.onboardingCompleted = onboardingCompleted; return this; }
        public PlayerIdentityBuilder lastDailyReset(LocalDateTime lastDailyReset) { this.lastDailyReset = lastDailyReset; return this; }
        public PlayerIdentityBuilder fcmToken(String fcmToken) { this.fcmToken = fcmToken; return this; }
        public PlayerIdentityBuilder notificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; return this; }
        public PlayerIdentityBuilder biggestChallenge(String biggestChallenge) { this.biggestChallenge = biggestChallenge; return this; }
        public PlayerIdentityBuilder sixMonthGoal(String sixMonthGoal) { this.sixMonthGoal = sixMonthGoal; return this; }
        public PlayerIdentityBuilder jobClass(String jobClass) { this.jobClass = jobClass; return this; }
        public PlayerIdentityBuilder classMultiplier(String classMultiplier) { this.classMultiplier = classMultiplier; return this; }
        public PlayerIdentityBuilder classUnlockedAt(LocalDateTime classUnlockedAt) { this.classUnlockedAt = classUnlockedAt; return this; }
        public PlayerIdentityBuilder jobChangeStatus(String jobChangeStatus) { this.jobChangeStatus = jobChangeStatus; return this; }
        public PlayerIdentityBuilder jobChangeCooldownUntil(LocalDateTime jobChangeCooldownUntil) { this.jobChangeCooldownUntil = jobChangeCooldownUntil; return this; }
        public PlayerIdentityBuilder xpFrozen(boolean xpFrozen) { this.xpFrozen = xpFrozen; return this; }
        public PlayerIdentityBuilder redGateActive(boolean redGateActive) { this.redGateActive = redGateActive; return this; }
        public PlayerIdentityBuilder redGateExpiresAt(LocalDateTime redGateExpiresAt) { this.redGateExpiresAt = redGateExpiresAt; return this; }
        public PlayerIdentityBuilder redGateQuestId(UUID redGateQuestId) { this.redGateQuestId = redGateQuestId; return this; }

        public PlayerIdentity build() {
            PlayerIdentity identity = new PlayerIdentity(playerId, username, createdAt, systemVersion, onboardingCompleted, lastDailyReset);
            identity.setFcmToken(fcmToken);
            identity.setNotificationsEnabled(notificationsEnabled);
            identity.setBiggestChallenge(biggestChallenge);
            identity.setSixMonthGoal(sixMonthGoal);
            identity.setJobClass(jobClass);
            identity.setClassMultiplier(classMultiplier);
            identity.setClassUnlockedAt(classUnlockedAt);
            identity.setJobChangeStatus(jobChangeStatus);
            identity.setJobChangeCooldownUntil(jobChangeCooldownUntil);
            identity.setXpFrozen(xpFrozen);
            return identity;
        }
    }
}
