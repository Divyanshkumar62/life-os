package com.lifeos.voice.domain;

import com.lifeos.voice.domain.enums.SystemMessageType;
import com.lifeos.voice.domain.enums.SystemVoiceMode;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "system_messages")
public class SystemMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID playerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SystemVoiceMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SystemMessageType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String body;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public SystemMessage() {}

    public SystemMessage(UUID id, UUID playerId, SystemVoiceMode mode, SystemMessageType type, String title, String body, boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.playerId = playerId;
        this.mode = mode;
        this.type = type;
        this.title = title;
        this.body = body;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }

    public SystemVoiceMode getMode() { return mode; }
    public void setMode(SystemVoiceMode mode) { this.mode = mode; }

    public SystemMessageType getType() { return type; }
    public void setType(SystemMessageType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID playerId;
        private SystemVoiceMode mode;
        private SystemMessageType type;
        private String title;
        private String body;
        private boolean isRead;
        private LocalDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public Builder mode(SystemVoiceMode mode) { this.mode = mode; return this; }
        public Builder type(SystemMessageType type) { this.type = type; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder body(String body) { this.body = body; return this; }
        public Builder isRead(boolean isRead) { this.isRead = isRead; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public SystemMessage build() {
            return new SystemMessage(id, playerId, mode, type, title, body, isRead, createdAt);
        }
    }
}
