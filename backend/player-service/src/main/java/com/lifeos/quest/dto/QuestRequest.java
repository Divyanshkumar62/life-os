package com.lifeos.quest.dto;

import com.lifeos.quest.domain.enums.*;
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
    private DifficultyTier difficultyTier;
    private Priority priority;
    private LocalDateTime deadlineAt;
    
    // Outcome Profile Data
    private long successXp;
    private long failureXp;
    private Map<String, Double> attributeDeltas; // e.g., {"STRENGTH": 1.0}
    
    // Optional
    private boolean systemMutable;
    
    public QuestRequest() {}

    public QuestRequest(UUID playerId, String title, String description, QuestType questType, DifficultyTier difficultyTier, Priority priority, LocalDateTime deadlineAt, long successXp, long failureXp, Map<String, Double> attributeDeltas, boolean systemMutable) {
        this.playerId = playerId;
        this.title = title;
        this.description = description;
        this.questType = questType;
        this.difficultyTier = difficultyTier;
        this.priority = priority;
        this.deadlineAt = deadlineAt;
        this.successXp = successXp;
        this.failureXp = failureXp;
        this.attributeDeltas = attributeDeltas;
        this.systemMutable = systemMutable;
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
    public Map<String, Double> getAttributeDeltas() { return attributeDeltas; }
    public void setAttributeDeltas(Map<String, Double> attributeDeltas) { this.attributeDeltas = attributeDeltas; }
    public boolean isSystemMutable() { return systemMutable; }
    public void setSystemMutable(boolean systemMutable) { this.systemMutable = systemMutable; }

    public static QuestRequestBuilder builder() {
        return new QuestRequestBuilder();
    }

    public static class QuestRequestBuilder {
        private UUID playerId;
        private String title;
        private String description;
        private QuestType questType;
        private DifficultyTier difficultyTier;
        private Priority priority;
        private LocalDateTime deadlineAt;
        private long successXp;
        private long failureXp;
        private Map<String, Double> attributeDeltas;
        private boolean systemMutable;

        public QuestRequestBuilder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public QuestRequestBuilder title(String title) { this.title = title; return this; }
        public QuestRequestBuilder description(String description) { this.description = description; return this; }
        public QuestRequestBuilder questType(QuestType questType) { this.questType = questType; return this; }
        public QuestRequestBuilder difficultyTier(DifficultyTier difficultyTier) { this.difficultyTier = difficultyTier; return this; }
        public QuestRequestBuilder priority(Priority priority) { this.priority = priority; return this; }
        public QuestRequestBuilder deadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; return this; }
        public QuestRequestBuilder successXp(long successXp) { this.successXp = successXp; return this; }
        public QuestRequestBuilder failureXp(long failureXp) { this.failureXp = failureXp; return this; }
        public QuestRequestBuilder attributeDeltas(Map<String, Double> attributeDeltas) { this.attributeDeltas = attributeDeltas; return this; }
        public QuestRequestBuilder systemMutable(boolean systemMutable) { this.systemMutable = systemMutable; return this; }

        public QuestRequest build() {
            return new QuestRequest(playerId, title, description, questType, difficultyTier, priority, deadlineAt, successXp, failureXp, attributeDeltas, systemMutable);
        }
    }
}
