package com.lifeos.player.dto;

import com.lifeos.player.domain.enums.PlayerRank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PlayerProgressionDTO {
    private int level;
    private long currentXp;
    private PlayerRank rank;
    private double rankProgressScore;
    private boolean xpFrozen;
    
    public PlayerProgressionDTO() {}

    public PlayerProgressionDTO(int level, long currentXp, PlayerRank rank, double rankProgressScore, boolean xpFrozen) {
        this.level = level;
        this.currentXp = currentXp;
        this.rank = rank;
        this.rankProgressScore = rankProgressScore;
        this.xpFrozen = xpFrozen;
    }

    // Getters and Setters
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

    public static PlayerProgressionDTOBuilder builder() {
        return new PlayerProgressionDTOBuilder();
    }

    public static class PlayerProgressionDTOBuilder {
        private int level;
        private long currentXp;
        private PlayerRank rank;
        private double rankProgressScore;
        private boolean xpFrozen;

        public PlayerProgressionDTOBuilder level(int level) { this.level = level; return this; }
        public PlayerProgressionDTOBuilder currentXp(long currentXp) { this.currentXp = currentXp; return this; }
        public PlayerProgressionDTOBuilder rank(PlayerRank rank) { this.rank = rank; return this; }
        public PlayerProgressionDTOBuilder rankProgressScore(double rankProgressScore) { this.rankProgressScore = rankProgressScore; return this; }
        public PlayerProgressionDTOBuilder xpFrozen(boolean xpFrozen) { this.xpFrozen = xpFrozen; return this; }

        public PlayerProgressionDTO build() {
            return new PlayerProgressionDTO(level, currentXp, rank, rankProgressScore, xpFrozen);
        }
    }
}
