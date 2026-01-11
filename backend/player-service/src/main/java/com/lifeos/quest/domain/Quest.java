package com.lifeos.quest.domain;

import com.lifeos.quest.domain.enums.*;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.enums.AttributeType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quest")
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID questId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerIdentity player;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestType questType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_attribute")
    private AttributeType primaryAttribute;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyTier difficultyTier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestState state;

    private LocalDateTime assignedAt;
    private LocalDateTime startsAt;
    private LocalDateTime deadlineAt;
    private LocalDateTime lastModifiedAt;

    private boolean systemMutable = true;

    private boolean egoBreakerFlag;

    private double expectedFailureProbability;
    
    public Quest() {}

    public Quest(UUID questId, PlayerIdentity player, UUID projectId, String title, String description, QuestType questType, QuestCategory category, AttributeType primaryAttribute, DifficultyTier difficultyTier, Priority priority, QuestState state, LocalDateTime assignedAt, LocalDateTime startsAt, LocalDateTime deadlineAt, LocalDateTime lastModifiedAt, boolean systemMutable, boolean egoBreakerFlag, double expectedFailureProbability) {
        this.questId = questId;
        this.player = player;
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.questType = questType;
        this.category = category;
        this.primaryAttribute = primaryAttribute;
        this.difficultyTier = difficultyTier;
        this.priority = priority;
        this.state = state;
        this.assignedAt = assignedAt;
        this.startsAt = startsAt;
        this.deadlineAt = deadlineAt;
        this.lastModifiedAt = lastModifiedAt;
        this.systemMutable = systemMutable;
        this.egoBreakerFlag = egoBreakerFlag;
        this.expectedFailureProbability = expectedFailureProbability;
    }

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) assignedAt = LocalDateTime.now();
        if (lastModifiedAt == null) lastModifiedAt = LocalDateTime.now();
        if (category == null) category = QuestCategory.NORMAL;
        // Invariant: RED difficulty implies egoBreaker
        if (difficultyTier == DifficultyTier.RED) {
            egoBreakerFlag = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedAt = LocalDateTime.now();
        if (difficultyTier == DifficultyTier.RED) {
            egoBreakerFlag = true;
        }
    }
    
    // Getters and Setters
    public UUID getQuestId() { return questId; }
    public void setQuestId(UUID questId) { this.questId = questId; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }
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
    public QuestState getState() { return state; }
    public void setState(QuestState state) { this.state = state; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    public LocalDateTime getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
    public boolean isSystemMutable() { return systemMutable; }
    public void setSystemMutable(boolean systemMutable) { this.systemMutable = systemMutable; }
    public boolean isEgoBreakerFlag() { return egoBreakerFlag; }
    public void setEgoBreakerFlag(boolean egoBreakerFlag) { this.egoBreakerFlag = egoBreakerFlag; }
    public double getExpectedFailureProbability() { return expectedFailureProbability; }
    public void setExpectedFailureProbability(double expectedFailureProbability) { this.expectedFailureProbability = expectedFailureProbability; }

    public static QuestBuilder builder() {
        return new QuestBuilder();
    }

    public static class QuestBuilder {
        private UUID questId;
        private PlayerIdentity player;
        private UUID projectId;
        private String title;
        private String description;
        private QuestType questType;
        private QuestCategory category;
        private AttributeType primaryAttribute;
        private DifficultyTier difficultyTier;
        private Priority priority;
        private QuestState state;
        private LocalDateTime assignedAt;
        private LocalDateTime startsAt;
        private LocalDateTime deadlineAt;
        private LocalDateTime lastModifiedAt;
        private boolean systemMutable = true;
        private boolean egoBreakerFlag;
        private double expectedFailureProbability;

        public QuestBuilder questId(UUID questId) { this.questId = questId; return this; }
        public QuestBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public QuestBuilder projectId(UUID projectId) { this.projectId = projectId; return this; }
        public QuestBuilder title(String title) { this.title = title; return this; }
        public QuestBuilder description(String description) { this.description = description; return this; }
        public QuestBuilder questType(QuestType questType) { this.questType = questType; return this; }
        public QuestBuilder category(QuestCategory category) { this.category = category; return this; }
        public QuestBuilder primaryAttribute(AttributeType primaryAttribute) { this.primaryAttribute = primaryAttribute; return this; }
        public QuestBuilder difficultyTier(DifficultyTier difficultyTier) { this.difficultyTier = difficultyTier; return this; }
        public QuestBuilder priority(Priority priority) { this.priority = priority; return this; }
        public QuestBuilder state(QuestState state) { this.state = state; return this; }
        public QuestBuilder assignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; return this; }
        public QuestBuilder startsAt(LocalDateTime startsAt) { this.startsAt = startsAt; return this; }
        public QuestBuilder deadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; return this; }
        public QuestBuilder lastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; return this; }
        public QuestBuilder systemMutable(boolean systemMutable) { this.systemMutable = systemMutable; return this; }
        public QuestBuilder egoBreakerFlag(boolean egoBreakerFlag) { this.egoBreakerFlag = egoBreakerFlag; return this; }
        public QuestBuilder expectedFailureProbability(double expectedFailureProbability) { this.expectedFailureProbability = expectedFailureProbability; return this; }

        public Quest build() {
            return new Quest(questId, player, projectId, title, description, questType, category, primaryAttribute, difficultyTier, priority, state, assignedAt, startsAt, deadlineAt, lastModifiedAt, systemMutable, egoBreakerFlag, expectedFailureProbability);
        }
    }
}
