package com.lifeos.player.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PlayerIdentityDTO {
    private UUID playerId;
    private String username;
    private LocalDateTime createdAt;
    private String systemVersion;
    
    public PlayerIdentityDTO() {}

    public PlayerIdentityDTO(UUID playerId, String username, LocalDateTime createdAt, String systemVersion) {
        this.playerId = playerId;
        this.username = username;
        this.createdAt = createdAt;
        this.systemVersion = systemVersion;
    }

    // Getters and Setters
    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getSystemVersion() { return systemVersion; }
    public void setSystemVersion(String systemVersion) { this.systemVersion = systemVersion; }

    public static PlayerIdentityDTOBuilder builder() {
        return new PlayerIdentityDTOBuilder();
    }

    public static class PlayerIdentityDTOBuilder {
        private UUID playerId;
        private String username;
        private LocalDateTime createdAt;
        private String systemVersion;

        public PlayerIdentityDTOBuilder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public PlayerIdentityDTOBuilder username(String username) { this.username = username; return this; }
        public PlayerIdentityDTOBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PlayerIdentityDTOBuilder systemVersion(String systemVersion) { this.systemVersion = systemVersion; return this; }

        public PlayerIdentityDTO build() {
            return new PlayerIdentityDTO(playerId, username, createdAt, systemVersion);
        }
    }
}
