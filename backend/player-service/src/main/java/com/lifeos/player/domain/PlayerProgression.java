package com.lifeos.player.domain;

import com.lifeos.player.domain.enums.PlayerRank;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_progression")
public class PlayerProgression {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private PlayerIdentity player;

    @Column(nullable = false)
    private int level;

    @Column(name = "current_xp", nullable = false)
    private long currentXp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerRank rank;

    @Column(name = "rank_progress_score")
    private double rankProgressScore;

    @Column(name = "xp_frozen", nullable = false)
    private boolean xpFrozen = false;
    
    public PlayerProgression() {}

    public PlayerProgression(Long id, PlayerIdentity player, int level, long currentXp, PlayerRank rank, double rankProgressScore, boolean xpFrozen) {
        this.id = id;
        this.player = player;
        this.level = level;
        this.currentXp = currentXp;
        this.rank = rank;
        this.rankProgressScore = rankProgressScore;
        this.xpFrozen = xpFrozen;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public long getCurrentXp() { return currentXp; }
    public void setCurrentXp(long currentXp) { this.currentXp = currentXp; }
    public PlayerRank getRank() { return rank; }
    public void setRank(PlayerRank rank) { this.rank = rank; }
    public double getRankProgressScore() { return rankProgressScore; }
    public void setRankProgressScore(double rankProgressScore) { this.rankProgressScore = rankProgressScore; }
    public boolean isXpFrozen() { return xpFrozen; }
    public void setXpFrozen(boolean xpFrozen) { this.xpFrozen = xpFrozen; }

    public static PlayerProgressionBuilder builder() {
        return new PlayerProgressionBuilder();
    }

    public static class PlayerProgressionBuilder {
        private Long id;
        private PlayerIdentity player;
        private int level;
        private long currentXp;
        private PlayerRank rank;
        private double rankProgressScore;
        private boolean xpFrozen = false;

        public PlayerProgressionBuilder id(Long id) { this.id = id; return this; }
        public PlayerProgressionBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public PlayerProgressionBuilder level(int level) { this.level = level; return this; }
        public PlayerProgressionBuilder currentXp(long currentXp) { this.currentXp = currentXp; return this; }
        public PlayerProgressionBuilder rank(PlayerRank rank) { this.rank = rank; return this; }
        public PlayerProgressionBuilder rankProgressScore(double rankProgressScore) { this.rankProgressScore = rankProgressScore; return this; }
        public PlayerProgressionBuilder xpFrozen(boolean xpFrozen) { this.xpFrozen = xpFrozen; return this; }

        public PlayerProgression build() {
            return new PlayerProgression(id, player, level, currentXp, rank, rankProgressScore, xpFrozen);
        }
    }
}
