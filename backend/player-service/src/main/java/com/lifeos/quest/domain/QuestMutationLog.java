package com.lifeos.quest.domain;

import com.lifeos.quest.domain.enums.MutationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "quest_mutation_log")
public class QuestMutationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MutationType mutationType;

    private String reason;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> oldValueJson;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> newValueJson;

    private LocalDateTime mutatedAt;
    
    public QuestMutationLog() {}

    public QuestMutationLog(Long id, Quest quest, MutationType mutationType, String reason, Map<String, Object> oldValueJson, Map<String, Object> newValueJson, LocalDateTime mutatedAt) {
        this.id = id;
        this.quest = quest;
        this.mutationType = mutationType;
        this.reason = reason;
        this.oldValueJson = oldValueJson;
        this.newValueJson = newValueJson;
        this.mutatedAt = mutatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Quest getQuest() { return quest; }
    public void setQuest(Quest quest) { this.quest = quest; }
    public MutationType getMutationType() { return mutationType; }
    public void setMutationType(MutationType mutationType) { this.mutationType = mutationType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Map<String, Object> getOldValueJson() { return oldValueJson; }
    public void setOldValueJson(Map<String, Object> oldValueJson) { this.oldValueJson = oldValueJson; }
    public Map<String, Object> getNewValueJson() { return newValueJson; }
    public void setNewValueJson(Map<String, Object> newValueJson) { this.newValueJson = newValueJson; }
    public LocalDateTime getMutatedAt() { return mutatedAt; }
    public void setMutatedAt(LocalDateTime mutatedAt) { this.mutatedAt = mutatedAt; }

    public static QuestMutationLogBuilder builder() {
        return new QuestMutationLogBuilder();
    }

    public static class QuestMutationLogBuilder {
        private Long id;
        private Quest quest;
        private MutationType mutationType;
        private String reason;
        private Map<String, Object> oldValueJson;
        private Map<String, Object> newValueJson;
        private LocalDateTime mutatedAt;

        public QuestMutationLogBuilder id(Long id) { this.id = id; return this; }
        public QuestMutationLogBuilder quest(Quest quest) { this.quest = quest; return this; }
        public QuestMutationLogBuilder mutationType(MutationType mutationType) { this.mutationType = mutationType; return this; }
        public QuestMutationLogBuilder reason(String reason) { this.reason = reason; return this; }
        public QuestMutationLogBuilder oldValueJson(Map<String, Object> oldValueJson) { this.oldValueJson = oldValueJson; return this; }
        public QuestMutationLogBuilder newValueJson(Map<String, Object> newValueJson) { this.newValueJson = newValueJson; return this; }
        public QuestMutationLogBuilder mutatedAt(LocalDateTime mutatedAt) { this.mutatedAt = mutatedAt; return this; }

        public QuestMutationLog build() {
            return new QuestMutationLog(id, quest, mutationType, reason, oldValueJson, newValueJson, mutatedAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (mutatedAt == null) mutatedAt = LocalDateTime.now();
    }
}
