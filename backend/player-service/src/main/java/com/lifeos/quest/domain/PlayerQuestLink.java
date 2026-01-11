package com.lifeos.quest.domain;

import com.lifeos.quest.domain.enums.QuestState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

// Projection entity for efficient querying of player's quest history/status
// Projection entity for efficient querying of player's quest history/status
@Entity
@Table(name = "player_quest_link")
public class PlayerQuestLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID playerId;

    @Column(nullable = false)
    private UUID questId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestState state;

    private LocalDateTime activatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    
    public PlayerQuestLink() {}

    public PlayerQuestLink(Long id, UUID playerId, UUID questId, QuestState state, LocalDateTime activatedAt, LocalDateTime completedAt, LocalDateTime failedAt) {
        this.id = id;
        this.playerId = playerId;
        this.questId = questId;
        this.state = state;
        this.activatedAt = activatedAt;
        this.completedAt = completedAt;
        this.failedAt = failedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public UUID getQuestId() { return questId; }
    public void setQuestId(UUID questId) { this.questId = questId; }
    public QuestState getState() { return state; }
    public void setState(QuestState state) { this.state = state; }
    public LocalDateTime getActivatedAt() { return activatedAt; }
    public void setActivatedAt(LocalDateTime activatedAt) { this.activatedAt = activatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }

    public static PlayerQuestLinkBuilder builder() {
        return new PlayerQuestLinkBuilder();
    }

    public static class PlayerQuestLinkBuilder {
        private Long id;
        private UUID playerId;
        private UUID questId;
        private QuestState state;
        private LocalDateTime activatedAt;
        private LocalDateTime completedAt;
        private LocalDateTime failedAt;

        public PlayerQuestLinkBuilder id(Long id) { this.id = id; return this; }
        public PlayerQuestLinkBuilder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public PlayerQuestLinkBuilder questId(UUID questId) { this.questId = questId; return this; }
        public PlayerQuestLinkBuilder state(QuestState state) { this.state = state; return this; }
        public PlayerQuestLinkBuilder activatedAt(LocalDateTime activatedAt) { this.activatedAt = activatedAt; return this; }
        public PlayerQuestLinkBuilder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }
        public PlayerQuestLinkBuilder failedAt(LocalDateTime failedAt) { this.failedAt = failedAt; return this; }

        public PlayerQuestLink build() {
            return new PlayerQuestLink(id, playerId, questId, state, activatedAt, completedAt, failedAt);
        }
    }
}
