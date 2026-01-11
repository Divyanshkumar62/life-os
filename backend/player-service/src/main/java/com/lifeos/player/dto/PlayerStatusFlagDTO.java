package com.lifeos.player.dto;

import com.lifeos.player.domain.enums.StatusFlagType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
public class PlayerStatusFlagDTO {
    private StatusFlagType flag;
    private LocalDateTime acquiredAt;
    private LocalDateTime expiresAt;
    
    public PlayerStatusFlagDTO() {}

    public PlayerStatusFlagDTO(StatusFlagType flag, LocalDateTime acquiredAt, LocalDateTime expiresAt) {
        this.flag = flag;
        this.acquiredAt = acquiredAt;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public StatusFlagType getFlag() { return flag; }
    public void setFlag(StatusFlagType flag) { this.flag = flag; }
    public LocalDateTime getAcquiredAt() { return acquiredAt; }
    public void setAcquiredAt(LocalDateTime acquiredAt) { this.acquiredAt = acquiredAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public static PlayerStatusFlagDTOBuilder builder() {
        return new PlayerStatusFlagDTOBuilder();
    }

    public static class PlayerStatusFlagDTOBuilder {
        private StatusFlagType flag;
        private LocalDateTime acquiredAt;
        private LocalDateTime expiresAt;

        public PlayerStatusFlagDTOBuilder flag(StatusFlagType flag) { this.flag = flag; return this; }
        public PlayerStatusFlagDTOBuilder acquiredAt(LocalDateTime acquiredAt) { this.acquiredAt = acquiredAt; return this; }
        public PlayerStatusFlagDTOBuilder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }

        public PlayerStatusFlagDTO build() {
            return new PlayerStatusFlagDTO(flag, acquiredAt, expiresAt);
        }
    }
}
