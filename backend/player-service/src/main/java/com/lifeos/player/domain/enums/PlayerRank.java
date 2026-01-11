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
}
