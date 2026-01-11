package com.lifeos.player.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_history")
public class PlayerHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private PlayerIdentity player;

    @Column(name = "last_ego_breaker_at")
    private LocalDateTime lastEgoBreakerAt;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "completed_quests_json")
    private java.util.List<String> completedQuests; // Changed from String to List

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "failed_quests_json")
    private java.util.List<String> failedQuests;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "notable_events_json")
    private java.util.List<String> notableEvents;
    
    public PlayerHistory() {}

    public PlayerHistory(Long id, PlayerIdentity player, LocalDateTime lastEgoBreakerAt, java.util.List<String> completedQuests, java.util.List<String> failedQuests, java.util.List<String> notableEvents) {
        this.id = id;
        this.player = player;
        this.lastEgoBreakerAt = lastEgoBreakerAt;
        this.completedQuests = completedQuests;
        this.failedQuests = failedQuests;
        this.notableEvents = notableEvents;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
    public LocalDateTime getLastEgoBreakerAt() { return lastEgoBreakerAt; }
    public void setLastEgoBreakerAt(LocalDateTime lastEgoBreakerAt) { this.lastEgoBreakerAt = lastEgoBreakerAt; }
    public java.util.List<String> getCompletedQuests() { return completedQuests; }
    public void setCompletedQuests(java.util.List<String> completedQuests) { this.completedQuests = completedQuests; }
    public java.util.List<String> getFailedQuests() { return failedQuests; }
    public void setFailedQuests(java.util.List<String> failedQuests) { this.failedQuests = failedQuests; }
    public java.util.List<String> getNotableEvents() { return notableEvents; }
    public void setNotableEvents(java.util.List<String> notableEvents) { this.notableEvents = notableEvents; }

    public static PlayerHistoryBuilder builder() {
        return new PlayerHistoryBuilder();
    }

    public static class PlayerHistoryBuilder {
        private Long id;
        private PlayerIdentity player;
        private LocalDateTime lastEgoBreakerAt;
        private java.util.List<String> completedQuests;
        private java.util.List<String> failedQuests;
        private java.util.List<String> notableEvents;

        public PlayerHistoryBuilder id(Long id) { this.id = id; return this; }
        public PlayerHistoryBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public PlayerHistoryBuilder lastEgoBreakerAt(LocalDateTime lastEgoBreakerAt) { this.lastEgoBreakerAt = lastEgoBreakerAt; return this; }
        public PlayerHistoryBuilder completedQuests(java.util.List<String> completedQuests) { this.completedQuests = completedQuests; return this; }
        public PlayerHistoryBuilder failedQuests(java.util.List<String> failedQuests) { this.failedQuests = failedQuests; return this; }
        public PlayerHistoryBuilder notableEvents(java.util.List<String> notableEvents) { this.notableEvents = notableEvents; return this; }

        public PlayerHistory build() {
            return new PlayerHistory(id, player, lastEgoBreakerAt, completedQuests, failedQuests, notableEvents);
        }
    }
}
