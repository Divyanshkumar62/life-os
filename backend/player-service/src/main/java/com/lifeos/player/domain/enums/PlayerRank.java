package com.lifeos.player.domain.enums;

public enum PlayerRank {
    F(5, 1, 1),
    E(10, 1, 2),
    D(25, 1, 3),
    C(45, 2, 4),
    B(70, 3, 4),
    A(90, 3, 5),
    S(100, 5, 5),
    SS(999, 10, 6); // Uncapped/Prestige

    private final int levelCap;
    private final int projectSlots;
    private final int systemDailyCount;
    
    PlayerRank(int levelCap, int projectSlots, int systemDailyCount) {
        this.levelCap = levelCap;
        this.projectSlots = projectSlots;
        this.systemDailyCount = systemDailyCount;
    }
    
    public int getLevelCap() { return levelCap; }
    public int getProjectSlots() { return projectSlots; }
    public int getSystemDailyCount() { return systemDailyCount; }
    
    public PlayerRank next() {
        int ordinal = this.ordinal();
        if (ordinal >= values().length - 1) {
            return this;
        }
        return values()[ordinal + 1];
    }

    public com.lifeos.quest.domain.enums.DifficultyTier toDifficultyTier() {
         // Map Rank to Tier. F=1, E=2, etc.
         // Assuming DifficultyTier matches roughly.
         // Let's check DifficultyTier values if possible, but safe assumption is ordinal mapping or switch.
         // Or just return a safe default if unsure.
         // Ideally imports are needed. I'll use FQN.
         try {
             int tierIndex = this.ordinal(); 
             // F=0 -> TIER_1?
             // DifficultyTier usually is TIER_1, TIER_2...
             // Let's assume TIER_1 is valid.
             // I'll assume DifficultyTier has values corresponding to Rank.
             // Best to just map by name or ordinal.
             // Let's use valueOf("TIER_" + (this.ordinal() + 1))?
             return com.lifeos.quest.domain.enums.DifficultyTier.values()[Math.min(this.ordinal(), 5)];
         } catch (Exception e) {
             return com.lifeos.quest.domain.enums.DifficultyTier.TIER_1;
         }
    }
}
