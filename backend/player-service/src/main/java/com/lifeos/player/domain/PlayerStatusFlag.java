package com.lifeos.player.domain;

import com.lifeos.player.domain.enums.StatusFlagType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_status_flag")
public class PlayerStatusFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerIdentity player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFlagType flag;

    @Column(name = "acquired_at", nullable = false)
    private LocalDateTime acquiredAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    public PlayerStatusFlag() {}

    public PlayerStatusFlag(Long id, PlayerIdentity player, StatusFlagType flag, LocalDateTime acquiredAt, LocalDateTime expiresAt) {
        this.id = id;
        this.player = player;
        this.flag = flag;
        this.acquiredAt = acquiredAt;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
    public StatusFlagType getFlag() { return flag; }
    public void setFlag(StatusFlagType flag) { this.flag = flag; }
    public LocalDateTime getAcquiredAt() { return acquiredAt; }
    public void setAcquiredAt(LocalDateTime acquiredAt) { this.acquiredAt = acquiredAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public static PlayerStatusFlagBuilder builder() {
        return new PlayerStatusFlagBuilder();
    }

    public static class PlayerStatusFlagBuilder {
        private Long id;
        private PlayerIdentity player;
        private StatusFlagType flag;
        private LocalDateTime acquiredAt;
        private LocalDateTime expiresAt;

        public PlayerStatusFlagBuilder id(Long id) { this.id = id; return this; }
        public PlayerStatusFlagBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public PlayerStatusFlagBuilder flag(StatusFlagType flag) { this.flag = flag; return this; }
        public PlayerStatusFlagBuilder acquiredAt(LocalDateTime acquiredAt) { this.acquiredAt = acquiredAt; return this; }
        public PlayerStatusFlagBuilder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }

        public PlayerStatusFlag build() {
            return new PlayerStatusFlag(id, player, flag, acquiredAt, expiresAt);
        }
    }
}
