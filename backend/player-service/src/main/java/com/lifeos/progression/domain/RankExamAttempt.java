package com.lifeos.progression.domain;

import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.progression.domain.enums.ExamStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rank_exam_attempts")
public class RankExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerIdentity player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerRank fromRank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerRank toRank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamStatus status;

    @Column(nullable = false)
    private int requiredKeys;

    @Column(nullable = false)
    private int consumedKeys;

    @Column(nullable = false)
    private int attemptNumber;

    private LocalDateTime unlockedAt;
    private LocalDateTime completedAt;
    
    public RankExamAttempt() {}

    public RankExamAttempt(UUID id, PlayerIdentity player, PlayerRank fromRank, PlayerRank toRank, ExamStatus status, int requiredKeys, int consumedKeys, int attemptNumber, LocalDateTime unlockedAt, LocalDateTime completedAt) {
        this.id = id;
        this.player = player;
        this.fromRank = fromRank;
        this.toRank = toRank;
        this.status = status;
        this.requiredKeys = requiredKeys;
        this.consumedKeys = consumedKeys;
        this.attemptNumber = attemptNumber;
        this.unlockedAt = unlockedAt;
        this.completedAt = completedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
    public PlayerRank getFromRank() { return fromRank; }
    public void setFromRank(PlayerRank fromRank) { this.fromRank = fromRank; }
    public PlayerRank getToRank() { return toRank; }
    public void setToRank(PlayerRank toRank) { this.toRank = toRank; }
    public ExamStatus getStatus() { return status; }
    public void setStatus(ExamStatus status) { this.status = status; }
    public int getRequiredKeys() { return requiredKeys; }
    public void setRequiredKeys(int requiredKeys) { this.requiredKeys = requiredKeys; }
    public int getConsumedKeys() { return consumedKeys; }
    public void setConsumedKeys(int consumedKeys) { this.consumedKeys = consumedKeys; }
    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public static RankExamAttemptBuilder builder() {
        return new RankExamAttemptBuilder();
    }

    public static class RankExamAttemptBuilder {
        private UUID id;
        private PlayerIdentity player;
        private PlayerRank fromRank;
        private PlayerRank toRank;
        private ExamStatus status;
        private int requiredKeys;
        private int consumedKeys;
        private int attemptNumber;
        private LocalDateTime unlockedAt;
        private LocalDateTime completedAt;

        public RankExamAttemptBuilder id(UUID id) { this.id = id; return this; }
        public RankExamAttemptBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public RankExamAttemptBuilder fromRank(PlayerRank fromRank) { this.fromRank = fromRank; return this; }
        public RankExamAttemptBuilder toRank(PlayerRank toRank) { this.toRank = toRank; return this; }
        public RankExamAttemptBuilder status(ExamStatus status) { this.status = status; return this; }
        public RankExamAttemptBuilder requiredKeys(int requiredKeys) { this.requiredKeys = requiredKeys; return this; }
        public RankExamAttemptBuilder consumedKeys(int consumedKeys) { this.consumedKeys = consumedKeys; return this; }
        public RankExamAttemptBuilder attemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; return this; }
        public RankExamAttemptBuilder unlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; return this; }
        public RankExamAttemptBuilder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }

        public RankExamAttempt build() {
            return new RankExamAttempt(id, player, fromRank, toRank, status, requiredKeys, consumedKeys, attemptNumber, unlockedAt, completedAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) status = ExamStatus.LOCKED;
    }
}
