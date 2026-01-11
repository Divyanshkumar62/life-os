package com.lifeos.quest.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "quest_outcome_profile")
public class QuestOutcomeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID outcomeId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    private Long successXp;
    private Long failureXp;
    private Long goldReward;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Double> attributeDeltaJson;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> statusFlagsOnSuccess;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> statusFlagsOnFailure;

    private String penaltyTier;
    
    public QuestOutcomeProfile() {}

    public QuestOutcomeProfile(UUID outcomeId, Quest quest, Long successXp, Long failureXp, Long goldReward, Map<String, Double> attributeDeltaJson, Map<String, Object> statusFlagsOnSuccess, Map<String, Object> statusFlagsOnFailure, String penaltyTier) {
        this.outcomeId = outcomeId;
        this.quest = quest;
        this.successXp = successXp;
        this.failureXp = failureXp;
        this.goldReward = goldReward;
        this.attributeDeltaJson = attributeDeltaJson;
        this.statusFlagsOnSuccess = statusFlagsOnSuccess;
        this.statusFlagsOnFailure = statusFlagsOnFailure;
        this.penaltyTier = penaltyTier;
    }
    
    // Getters and Setters
    public UUID getOutcomeId() { return outcomeId; }
    public void setOutcomeId(UUID outcomeId) { this.outcomeId = outcomeId; }
    public Quest getQuest() { return quest; }
    public void setQuest(Quest quest) { this.quest = quest; }
    public Long getSuccessXp() { return successXp; }
    public void setSuccessXp(Long successXp) { this.successXp = successXp; }
    public Long getFailureXp() { return failureXp; }
    public void setFailureXp(Long failureXp) { this.failureXp = failureXp; }
    public Long getGoldReward() { return goldReward; }
    public void setGoldReward(Long goldReward) { this.goldReward = goldReward; }
    public Map<String, Double> getAttributeDeltaJson() { return attributeDeltaJson; }
    public void setAttributeDeltaJson(Map<String, Double> attributeDeltaJson) { this.attributeDeltaJson = attributeDeltaJson; }
    public Map<String, Object> getStatusFlagsOnSuccess() { return statusFlagsOnSuccess; }
    public void setStatusFlagsOnSuccess(Map<String, Object> statusFlagsOnSuccess) { this.statusFlagsOnSuccess = statusFlagsOnSuccess; }
    public Map<String, Object> getStatusFlagsOnFailure() { return statusFlagsOnFailure; }
    public void setStatusFlagsOnFailure(Map<String, Object> statusFlagsOnFailure) { this.statusFlagsOnFailure = statusFlagsOnFailure; }
    public String getPenaltyTier() { return penaltyTier; }
    public void setPenaltyTier(String penaltyTier) { this.penaltyTier = penaltyTier; }

    public static QuestOutcomeProfileBuilder builder() {
        return new QuestOutcomeProfileBuilder();
    }

    public static class QuestOutcomeProfileBuilder {
        private UUID outcomeId;
        private Quest quest;
        private Long successXp;
        private Long failureXp;
        private Long goldReward;
        private Map<String, Double> attributeDeltaJson;
        private Map<String, Object> statusFlagsOnSuccess;
        private Map<String, Object> statusFlagsOnFailure;
        private String penaltyTier;

        public QuestOutcomeProfileBuilder outcomeId(UUID outcomeId) { this.outcomeId = outcomeId; return this; }
        public QuestOutcomeProfileBuilder quest(Quest quest) { this.quest = quest; return this; }
        public QuestOutcomeProfileBuilder successXp(Long successXp) { this.successXp = successXp; return this; }
        public QuestOutcomeProfileBuilder failureXp(Long failureXp) { this.failureXp = failureXp; return this; }
        public QuestOutcomeProfileBuilder goldReward(Long goldReward) { this.goldReward = goldReward; return this; }
        public QuestOutcomeProfileBuilder attributeDeltaJson(Map<String, Double> attributeDeltaJson) { this.attributeDeltaJson = attributeDeltaJson; return this; }
        public QuestOutcomeProfileBuilder statusFlagsOnSuccess(Map<String, Object> statusFlagsOnSuccess) { this.statusFlagsOnSuccess = statusFlagsOnSuccess; return this; }
        public QuestOutcomeProfileBuilder statusFlagsOnFailure(Map<String, Object> statusFlagsOnFailure) { this.statusFlagsOnFailure = statusFlagsOnFailure; return this; }
        public QuestOutcomeProfileBuilder penaltyTier(String penaltyTier) { this.penaltyTier = penaltyTier; return this; }

        public QuestOutcomeProfile build() {
            return new QuestOutcomeProfile(outcomeId, quest, successXp, failureXp, goldReward, attributeDeltaJson, statusFlagsOnSuccess, statusFlagsOnFailure, penaltyTier);
        }
    }
}
