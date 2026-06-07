package com.lifeos.penalty.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "player_journal")
public class PlayerJournal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "accepted", nullable = false)
    private boolean accepted;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    public PlayerJournal() {}

    public PlayerJournal(Long id, UUID playerId, String text, boolean accepted, LocalDateTime timestamp) {
        this.id = id;
        this.playerId = playerId;
        this.text = text;
        this.accepted = accepted;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static PlayerJournalBuilder builder() {
        return new PlayerJournalBuilder();
    }

    public static class PlayerJournalBuilder {
        private Long id;
        private UUID playerId;
        private String text;
        private boolean accepted;
        private LocalDateTime timestamp;

        public PlayerJournalBuilder id(Long id) { this.id = id; return this; }
        public PlayerJournalBuilder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public PlayerJournalBuilder text(String text) { this.text = text; return this; }
        public PlayerJournalBuilder accepted(boolean accepted) { this.accepted = accepted; return this; }
        public PlayerJournalBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public PlayerJournal build() {
            return new PlayerJournal(id, playerId, text, accepted, timestamp);
        }
    }
}
