package com.lifeos.quest.dto;

import com.lifeos.quest.domain.enums.*;
import com.lifeos.player.domain.enums.AttributeType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class QuestRequest {
    private UUID playerId;
    private String title;
    private String description;
    private QuestType questType;
    private QuestCategory category;
    private AttributeType primaryAttribute;
    private DifficultyTier difficultyTier;
    private Priority priority;
    private LocalDateTime deadlineAt;
    
    // Outcome Profile Data
    private long successXp;
    private long failureXp;
    private long goldReward;
    private Map<String, Double> attributeDeltas; // e.g., {"STRENGTH": 1.0}
    
    // Optional
    private boolean systemMutable;
    private boolean egoBreakerFlag;
    private double expectedFailureProbability;
    
    public QuestRequest() {}

    public QuestRequest(UUID playerId, String title, String description, QuestType questType, QuestCategory category, AttributeType primaryAttribute, DifficultyTier difficultyTier, Priority priority, LocalDateTime deadlineAt, long successXp, long failureXp, long goldReward, Map<String, Double> attributeDeltas, boolean systemMutable, boolean egoBreakerFlag, double expectedFailureProbability) {
        this.playerId = playerId;
        this.title = title;
        this.description = description;
        this.questType = questType;
        this.category = category;
        this.primaryAttribute = primaryAttribute;
        this.difficultyTier = difficultyTier;
        this.priority = priority;
        this.deadlineAt = deadlineAt;
        this.successXp = successXp;
        this.failureXp = failureXp;
        this.goldReward = goldReward;
        this.attributeDeltas = attributeDeltas;
        this.systemMutable = systemMutable;
        this.egoBreakerFlag = egoBreakerFlag;
        this.expectedFailureProbability = expectedFailureProbability;
    }

    // Getters and Setters
    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public QuestType getQuestType() { return questType; }
    public void setQuestType(QuestType questType) { this.questType = questType; }
    public QuestCategory getCategory() { return category; }
    public void setCategory(QuestCategory category) { this.category = category; }
    public AttributeType getPrimaryAttribute() { return primaryAttribute; }
    public void setPrimaryAttribute(AttributeType primaryAttribute) { this.primaryAttribute = primaryAttribute; }
    public DifficultyTier getDifficultyTier() { return difficultyTier; }
    public void setDifficultyTier(DifficultyTier difficultyTier) { this.difficultyTier = difficultyTier; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public LocalDateTime getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; }
    public long getSuccessXp() { return successXp; }
    public void setSuccessXp(long successXp) { this.successXp = successXp; }
    public long getFailureXp() { return failureXp; }
    public void setFailureXp(long failureXp) { this.failureXp = failureXp; }
    public long getGoldReward() { return goldReward; }
    public void setGoldReward(long goldReward) { this.goldReward = goldReward; }
    public Map<String, Double> getAttributeDeltas() { return attributeDeltas; }
    public void setAttributeDeltas(Map<String, Double> attributeDeltas) { this.attributeDeltas = attributeDeltas; }
    public boolean isSystemMutable() { return systemMutable; }
    public void setSystemMutable(boolean systemMutable) { this.systemMutable = systemMutable; }
    public boolean isEgoBreakerFlag() { return egoBreakerFlag; }
    public void setEgoBreakerFlag(boolean egoBreakerFlag) { this.egoBreakerFlag = egoBreakerFlag; }
    public double getExpectedFailureProbability() { return expectedFailureProbability; }
    public void setExpectedFailureProbability(double expectedFailureProbability) { this.expectedFailureProbability = expectedFailureProbability; }

    public static QuestRequestBuilder builder() {
        return new QuestRequestBuilder();
    }

    public static class QuestRequestBuilder {
        private UUID playerId;
        private String title;
        private String description;
        private QuestType questType;
        private QuestCategory category;
        private AttributeType primaryAttribute;
        private DifficultyTier difficultyTier;
        private Priority priority;
        private LocalDateTime deadlineAt;
        private long successXp;
        private long failureXp;
        private long goldReward;
        private Map<String, Double> attributeDeltas;
        private boolean systemMutable;
        private boolean egoBreakerFlag;
        private double expectedFailureProbability;

        public QuestRequestBuilder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public QuestRequestBuilder title(String title) { this.title = title; return this; }
        public QuestRequestBuilder description(String description) { this.description = description; return this; }
        public QuestRequestBuilder questType(QuestType questType) { this.questType = questType; return this; }
        public QuestRequestBuilder category(QuestCategory category) { this.category = category; return this; }
        public QuestRequestBuilder primaryAttribute(AttributeType primaryAttribute) { this.primaryAttribute = primaryAttribute; return this; }
        public QuestRequestBuilder difficultyTier(DifficultyTier difficultyTier) { this.difficultyTier = difficultyTier; return this; }
        public QuestRequestBuilder priority(Priority priority) { this.priority = priority; return this; }
        public QuestRequestBuilder deadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; return this; }
        public QuestRequestBuilder successXp(long successXp) { this.successXp = successXp; return this; }
        public QuestRequestBuilder failureXp(long failureXp) { this.failureXp = failureXp; return this; }
        public QuestRequestBuilder goldReward(long goldReward) { this.goldReward = goldReward; return this; }
        public QuestRequestBuilder attributeDeltas(Map<String, Double> attributeDeltas) { this.attributeDeltas = attributeDeltas; return this; }
        public QuestRequestBuilder systemMutable(boolean systemMutable) { this.systemMutable = systemMutable; return this; }
        public QuestRequestBuilder egoBreakerFlag(boolean egoBreakerFlag) { this.egoBreakerFlag = egoBreakerFlag; return this; }
        public QuestRequestBuilder expectedFailureProbability(double expectedFailureProbability) { this.expectedFailureProbability = expectedFailureProbability; return this; }

        public QuestRequest build() {
            return new QuestRequest(playerId, title, description, questType, category, primaryAttribute, difficultyTier, priority, deadlineAt, successXp, failureXp, goldReward, attributeDeltas, systemMutable, egoBreakerFlag, expectedFailureProbability);
        }
    }
}
