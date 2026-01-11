package com.lifeos.player.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "player_identity")
public class PlayerIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "player_id")
    private UUID playerId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "system_version")
    private String systemVersion;

    public PlayerIdentity() {}

    public PlayerIdentity(UUID playerId, String username, LocalDateTime createdAt, String systemVersion) {
        this.playerId = playerId;
        this.username = username;
        this.createdAt = createdAt;
        this.systemVersion = systemVersion;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters
    public UUID getPlayerId() { return playerId; }
    public String getUsername() { return username; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getSystemVersion() { return systemVersion; }

    // Setters
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public void setUsername(String username) { this.username = username; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setSystemVersion(String systemVersion) { this.systemVersion = systemVersion; }

    // Builder
    public static PlayerIdentityBuilder builder() {
        return new PlayerIdentityBuilder();
    }

    public static class PlayerIdentityBuilder {
        private UUID playerId;
        private String username;
        private LocalDateTime createdAt;
        private String systemVersion;

        public PlayerIdentityBuilder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public PlayerIdentityBuilder username(String username) { this.username = username; return this; }
        public PlayerIdentityBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PlayerIdentityBuilder systemVersion(String systemVersion) { this.systemVersion = systemVersion; return this; }

        public PlayerIdentity build() {
            return new PlayerIdentity(playerId, username, createdAt, systemVersion);
        }
    }
}
