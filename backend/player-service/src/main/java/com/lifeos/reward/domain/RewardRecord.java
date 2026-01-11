package com.lifeos.reward.domain;

import com.lifeos.reward.domain.enums.RewardComponentType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "reward_record")
public class RewardRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID playerId;

    // Idempotency Key: Unique per Quest
    @Column(nullable = false, unique = true)
    private UUID questId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> rewardPayload; 

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    public RewardRecord() {}

    public RewardRecord(UUID id, UUID playerId, UUID questId, Map<String, Object> rewardPayload, LocalDateTime appliedAt) {
        this.id = id;
        this.playerId = playerId;
        this.questId = questId;
        this.rewardPayload = rewardPayload;
        this.appliedAt = appliedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public UUID getQuestId() { return questId; }
    public void setQuestId(UUID questId) { this.questId = questId; }
    public Map<String, Object> getRewardPayload() { return rewardPayload; }
    public void setRewardPayload(Map<String, Object> rewardPayload) { this.rewardPayload = rewardPayload; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public static RewardRecordBuilder builder() {
        return new RewardRecordBuilder();
    }

    public static class RewardRecordBuilder {
        private UUID id;
        private UUID playerId;
        private UUID questId;
        private Map<String, Object> rewardPayload;
        private LocalDateTime appliedAt;

        public RewardRecordBuilder id(UUID id) { this.id = id; return this; }
        public RewardRecordBuilder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public RewardRecordBuilder questId(UUID questId) { this.questId = questId; return this; }
        public RewardRecordBuilder rewardPayload(Map<String, Object> rewardPayload) { this.rewardPayload = rewardPayload; return this; }
        public RewardRecordBuilder appliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; return this; }

        public RewardRecord build() {
            return new RewardRecord(id, playerId, questId, rewardPayload, appliedAt);
        }
    }
}
