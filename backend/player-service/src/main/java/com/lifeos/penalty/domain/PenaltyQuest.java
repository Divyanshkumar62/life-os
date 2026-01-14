package com.lifeos.penalty.domain;

import com.lifeos.penalty.domain.enums.PenaltyQuestStatus;
import com.lifeos.penalty.domain.enums.PenaltyQuestType;
import com.lifeos.penalty.domain.enums.PenaltyTriggerReason;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "penalty_quests")
public class PenaltyQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID playerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PenaltyQuestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PenaltyTriggerReason triggerReason;

    @Column(nullable = false)
    private int requiredCount;

    @Column(nullable = false)
    private int completedCount;

    @Column(nullable = false)
    private int todayWorkUnits;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PenaltyQuestStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    private LocalDate lastWorkDate;

    public PenaltyQuest() {}

    public PenaltyQuest(UUID id, UUID playerId, PenaltyQuestType type, PenaltyTriggerReason triggerReason, int requiredCount, int completedCount, int todayWorkUnits, PenaltyQuestStatus status, LocalDateTime createdAt, LocalDateTime completedAt, LocalDate lastWorkDate) {
        this.id = id;
        this.playerId = playerId;
        this.type = type;
        this.triggerReason = triggerReason;
        this.requiredCount = requiredCount;
        this.completedCount = completedCount;
        this.todayWorkUnits = todayWorkUnits;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.lastWorkDate = lastWorkDate;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }

    public PenaltyQuestType getType() { return type; }
    public void setType(PenaltyQuestType type) { this.type = type; }

    public PenaltyTriggerReason getTriggerReason() { return triggerReason; }
    public void setTriggerReason(PenaltyTriggerReason triggerReason) { this.triggerReason = triggerReason; }

    public int getRequiredCount() { return requiredCount; }
    public void setRequiredCount(int requiredCount) { this.requiredCount = requiredCount; }

    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }

    public int getTodayWorkUnits() { return todayWorkUnits; }
    public void setTodayWorkUnits(int todayWorkUnits) { this.todayWorkUnits = todayWorkUnits; }

    public PenaltyQuestStatus getStatus() { return status; }
    public void setStatus(PenaltyQuestStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDate getLastWorkDate() { return lastWorkDate; }
    public void setLastWorkDate(LocalDate lastWorkDate) { this.lastWorkDate = lastWorkDate; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID playerId;
        private PenaltyQuestType type;
        private PenaltyTriggerReason triggerReason;
        private int requiredCount;
        private int completedCount;
        private int todayWorkUnits;
        private PenaltyQuestStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
        private LocalDate lastWorkDate;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public Builder type(PenaltyQuestType type) { this.type = type; return this; }
        public Builder triggerReason(PenaltyTriggerReason triggerReason) { this.triggerReason = triggerReason; return this; }
        public Builder requiredCount(int requiredCount) { this.requiredCount = requiredCount; return this; }
        public Builder completedCount(int completedCount) { this.completedCount = completedCount; return this; }
        public Builder todayWorkUnits(int todayWorkUnits) { this.todayWorkUnits = todayWorkUnits; return this; }
        public Builder status(PenaltyQuestStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }
        public Builder lastWorkDate(LocalDate lastWorkDate) { this.lastWorkDate = lastWorkDate; return this; }

        public PenaltyQuest build() {
            return new PenaltyQuest(id, playerId, type, triggerReason, requiredCount, completedCount, todayWorkUnits, status, createdAt, completedAt, lastWorkDate);
        }
    }
}
