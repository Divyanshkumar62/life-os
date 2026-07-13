package com.lifeos.project.dto;

import java.util.List;
import java.util.UUID;

public class SpeedrunResultDTO {
    private UUID projectId;
    private String projectTitle;
    private int actualDurationDays;
    private int estimatedDurationDays;
    private double speedrunMultiplier;
    private int baseXp;
    private int bonusXp;
    private int totalXp;
    private int baseGold;
    private int bonusGold;
    private int totalGold;
    private List<LootItemDTO> lootDrops;

    public SpeedrunResultDTO() {}

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }
    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }
    public int getActualDurationDays() { return actualDurationDays; }
    public void setActualDurationDays(int actualDurationDays) { this.actualDurationDays = actualDurationDays; }
    public int getEstimatedDurationDays() { return estimatedDurationDays; }
    public void setEstimatedDurationDays(int estimatedDurationDays) { this.estimatedDurationDays = estimatedDurationDays; }
    public double getSpeedrunMultiplier() { return speedrunMultiplier; }
    public void setSpeedrunMultiplier(double speedrunMultiplier) { this.speedrunMultiplier = speedrunMultiplier; }
    public int getBaseXp() { return baseXp; }
    public void setBaseXp(int baseXp) { this.baseXp = baseXp; }
    public int getBonusXp() { return bonusXp; }
    public void setBonusXp(int bonusXp) { this.bonusXp = bonusXp; }
    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }
    public int getBaseGold() { return baseGold; }
    public void setBaseGold(int baseGold) { this.baseGold = baseGold; }
    public int getBonusGold() { return bonusGold; }
    public void setBonusGold(int bonusGold) { this.bonusGold = bonusGold; }
    public int getTotalGold() { return totalGold; }
    public void setTotalGold(int totalGold) { this.totalGold = totalGold; }
    public List<LootItemDTO> getLootDrops() { return lootDrops; }
    public void setLootDrops(List<LootItemDTO> lootDrops) { this.lootDrops = lootDrops; }
}
