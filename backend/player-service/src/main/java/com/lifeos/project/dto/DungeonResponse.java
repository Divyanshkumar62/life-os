package com.lifeos.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DungeonResponse {

    @JsonProperty("is_valid")
    private boolean isValid;

    @JsonProperty("rejection_reason")
    private String rejectionReason;

    private DungeonData dungeon;

    public boolean isValid() { return isValid; }
    public void setValid(boolean valid) { isValid = valid; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public DungeonData getDungeon() { return dungeon; }
    public void setDungeon(DungeonData dungeon) { this.dungeon = dungeon; }

    public static DungeonResponseBuilder builder() {
        return new DungeonResponseBuilder();
    }

    public static class DungeonResponseBuilder {
        private boolean isValid;
        private String rejectionReason;
        private DungeonData dungeon;

        public DungeonResponseBuilder isValid(boolean isValid) { this.isValid = isValid; return this; }
        public DungeonResponseBuilder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }
        public DungeonResponseBuilder dungeon(DungeonData dungeon) { this.dungeon = dungeon; return this; }

        public DungeonResponse build() {
            DungeonResponse response = new DungeonResponse();
            response.isValid = this.isValid;
            response.rejectionReason = this.rejectionReason;
            response.dungeon = this.dungeon;
            return response;
        }
    }

    public static class DungeonData {
        private String title;
        private String description;
        private String rank;
        
        @JsonProperty("boss_name")
        private String bossName;
        
        @JsonProperty("stat_focus")
        private String statFocus;
        
        @JsonProperty("estimated_duration_days")
        private int estimatedDurationDays;
        
        private List<DungeonFloor> floors;
        private DungeonLoot loot;
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getRank() { return rank; }
        public void setRank(String rank) { this.rank = rank; }
        public String getBossName() { return bossName; }
        public void setBossName(String bossName) { this.bossName = bossName; }
        public String getStatFocus() { return statFocus; }
        public void setStatFocus(String statFocus) { this.statFocus = statFocus; }
        public int getEstimatedDurationDays() { return estimatedDurationDays; }
        public void setEstimatedDurationDays(int estimatedDurationDays) { this.estimatedDurationDays = estimatedDurationDays; }
        public List<DungeonFloor> getFloors() { return floors; }
        public void setFloors(List<DungeonFloor> floors) { this.floors = floors; }
        public DungeonLoot getLoot() { return loot; }
        public void setLoot(DungeonLoot loot) { this.loot = loot; }
    }

    public static class DungeonFloor {
        @JsonProperty("floor_num")
        private int floorNum;
        
        private String title;
        private int xp;
        private String stat;
        
        public int getFloorNum() { return floorNum; }
        public void setFloorNum(int floorNum) { this.floorNum = floorNum; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public int getXp() { return xp; }
        public void setXp(int xp) { this.xp = xp; }
        public String getStat() { return stat; }
    }

    public static class DungeonLoot {
        @JsonProperty("xp_total")
        private int xpTotal;
        private int gold;
        
        @JsonProperty("boss_keys")
        private int bossKeys;
        
        public int getXpTotal() { return xpTotal; }
        public void setXpTotal(int xpTotal) { this.xpTotal = xpTotal; }
        public int getGold() { return gold; }
        public void setGold(int gold) { this.gold = gold; }
        public int getBossKeys() { return bossKeys; }
        public void setBossKeys(int bossKeys) { this.bossKeys = bossKeys; }
    }
}
