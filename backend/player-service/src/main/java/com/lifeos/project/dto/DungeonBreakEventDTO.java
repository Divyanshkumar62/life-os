package com.lifeos.project.dto;

import com.lifeos.project.domain.DungeonBreakEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DungeonBreakEventDTO {
    private UUID projectId;
    private String projectTitle;
    private String dungeonRank;
    private long goldBefore;
    private long goldPenaltyAmount;
    private long goldAfter;
    private double vitMitigationPercent;
    private List<String> debuffsApplied;
    private int debuffDurationHours;
    private boolean penaltyZoneTriggered;
    private boolean doublePenaltyResolution;
    private LocalDateTime triggeredAt;
    private boolean acknowledged;

    public DungeonBreakEventDTO() {}

    public static DungeonBreakEventDTO fromEntity(DungeonBreakEvent entity) {
        if (entity == null) return null;
        DungeonBreakEventDTO dto = new DungeonBreakEventDTO();
        dto.setProjectId(entity.getProjectId());
        dto.setProjectTitle(entity.getProjectTitle());
        dto.setDungeonRank(entity.getDungeonRank());
        dto.setGoldBefore(entity.getGoldBefore());
        dto.setGoldPenaltyAmount(entity.getGoldPenaltyAmount());
        dto.setGoldAfter(entity.getGoldAfter());
        dto.setVitMitigationPercent(entity.getVitMitigationPercent());
        dto.setDebuffsApplied(entity.getDebuffsApplied());
        dto.setDebuffDurationHours(entity.getDebuffDurationHours());
        dto.setPenaltyZoneTriggered(entity.isPenaltyZoneTriggered());
        dto.setDoublePenaltyResolution(entity.isDoublePenaltyResolution());
        dto.setTriggeredAt(entity.getTriggeredAt());
        dto.setAcknowledged(entity.isAcknowledged());
        return dto;
    }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }
    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }
    public String getDungeonRank() { return dungeonRank; }
    public void setDungeonRank(String dungeonRank) { this.dungeonRank = dungeonRank; }
    public long getGoldBefore() { return goldBefore; }
    public void setGoldBefore(long goldBefore) { this.goldBefore = goldBefore; }
    public long getGoldPenaltyAmount() { return goldPenaltyAmount; }
    public void setGoldPenaltyAmount(long goldPenaltyAmount) { this.goldPenaltyAmount = goldPenaltyAmount; }
    public long getGoldAfter() { return goldAfter; }
    public void setGoldAfter(long goldAfter) { this.goldAfter = goldAfter; }
    public double getVitMitigationPercent() { return vitMitigationPercent; }
    public void setVitMitigationPercent(double vitMitigationPercent) { this.vitMitigationPercent = vitMitigationPercent; }
    public List<String> getDebuffsApplied() { return debuffsApplied; }
    public void setDebuffsApplied(List<String> debuffsApplied) { this.debuffsApplied = debuffsApplied; }
    public int getDebuffDurationHours() { return debuffDurationHours; }
    public void setDebuffDurationHours(int debuffDurationHours) { this.debuffDurationHours = debuffDurationHours; }
    public boolean isPenaltyZoneTriggered() { return penaltyZoneTriggered; }
    public void setPenaltyZoneTriggered(boolean penaltyZoneTriggered) { this.penaltyZoneTriggered = penaltyZoneTriggered; }
    public boolean isDoublePenaltyResolution() { return doublePenaltyResolution; }
    public void setDoublePenaltyResolution(boolean doublePenaltyResolution) { this.doublePenaltyResolution = doublePenaltyResolution; }
    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }
    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
}
