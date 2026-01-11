package com.lifeos.progression.domain;

import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.enums.PlayerRank;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_boss_keys", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"player_id", "rank"})
})
public class UserBossKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerIdentity player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerRank rank; // The rank this key allows promotion FROM (e.g., E means E->D exam)

    @Column(nullable = false)
    private int keyCount = 0;
    
    public UserBossKey() {}

    public UserBossKey(UUID id, PlayerIdentity player, PlayerRank rank, int keyCount) {
        this.id = id;
        this.player = player;
        this.rank = rank;
        this.keyCount = keyCount;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
    public PlayerRank getRank() { return rank; }
    public void setRank(PlayerRank rank) { this.rank = rank; }
    public int getKeyCount() { return keyCount; }
    public void setKeyCount(int keyCount) { this.keyCount = keyCount; }

    public static UserBossKeyBuilder builder() {
        return new UserBossKeyBuilder();
    }

    public static class UserBossKeyBuilder {
        private UUID id;
        private PlayerIdentity player;
        private PlayerRank rank;
        private int keyCount = 0;

        public UserBossKeyBuilder id(UUID id) { this.id = id; return this; }
        public UserBossKeyBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public UserBossKeyBuilder rank(PlayerRank rank) { this.rank = rank; return this; }
        public UserBossKeyBuilder keyCount(int keyCount) { this.keyCount = keyCount; return this; }

        public UserBossKey build() {
            return new UserBossKey(id, player, rank, keyCount);
        }
    }
}
