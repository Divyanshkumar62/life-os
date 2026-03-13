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
    private long totalXpAccumulated;
    private int freeStatPoints;
    private PlayerRank rank;
    private double rankProgressScore;
    private boolean xpFrozen;
    
    public PlayerProgressionDTO() {}

    public PlayerProgressionDTO(int level, long currentXp, long totalXpAccumulated, int freeStatPoints, PlayerRank rank, double rankProgressScore, boolean xpFrozen) {
        this.level = level;
        this.currentXp = currentXp;
        this.totalXpAccumulated = totalXpAccumulated;
        this.freeStatPoints = freeStatPoints;
        this.rank = rank;
        this.rankProgressScore = rankProgressScore;
        this.xpFrozen = xpFrozen;
    }

    // Getters and Setters
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public long getCurrentXp() { return currentXp; }
    public void setCurrentXp(long currentXp) { this.currentXp = currentXp; }
    public long getTotalXpAccumulated() { return totalXpAccumulated; }
    public void setTotalXpAccumulated(long totalXpAccumulated) { this.totalXpAccumulated = totalXpAccumulated; }
    public int getFreeStatPoints() { return freeStatPoints; }
    public void setFreeStatPoints(int freeStatPoints) { this.freeStatPoints = freeStatPoints; }
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
        private long totalXpAccumulated;
        private int freeStatPoints;
        private PlayerRank rank;
        private double rankProgressScore;
        private boolean xpFrozen;

        public PlayerProgressionDTOBuilder level(int level) { this.level = level; return this; }
        public PlayerProgressionDTOBuilder currentXp(long currentXp) { this.currentXp = currentXp; return this; }
        public PlayerProgressionDTOBuilder totalXpAccumulated(long totalXpAccumulated) { this.totalXpAccumulated = totalXpAccumulated; return this; }
        public PlayerProgressionDTOBuilder freeStatPoints(int freeStatPoints) { this.freeStatPoints = freeStatPoints; return this; }
        public PlayerProgressionDTOBuilder rank(PlayerRank rank) { this.rank = rank; return this; }
        public PlayerProgressionDTOBuilder rankProgressScore(double rankProgressScore) { this.rankProgressScore = rankProgressScore; return this; }
        public PlayerProgressionDTOBuilder xpFrozen(boolean xpFrozen) { this.xpFrozen = xpFrozen; return this; }

        public PlayerProgressionDTO build() {
            return new PlayerProgressionDTO(level, currentXp, totalXpAccumulated, freeStatPoints, rank, rankProgressScore, xpFrozen);
        }
    }
}
