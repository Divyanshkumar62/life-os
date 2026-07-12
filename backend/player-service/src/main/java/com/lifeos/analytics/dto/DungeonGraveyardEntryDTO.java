package com.lifeos.analytics.dto;

import java.time.LocalDateTime;

public class DungeonGraveyardEntryDTO {
    private String dungeonId;
    private String title;
    private String description;
    private String dungeonRank;
    private String dungeonStatus;
    private int totalFloors;
    private int completedFloors;
    private LocalDateTime createdAt;
    private LocalDateTime deadlineAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private LocalDateTime abandonedAt;

    public DungeonGraveyardEntryDTO() {}

    public DungeonGraveyardEntryDTO(String dungeonId, String title, String description, String dungeonRank, String dungeonStatus,
                                    int totalFloors, int completedFloors, LocalDateTime createdAt, LocalDateTime deadlineAt,
                                    LocalDateTime completedAt, LocalDateTime failedAt, LocalDateTime abandonedAt) {
        this.dungeonId = dungeonId;
        this.title = title;
        this.description = description;
        this.dungeonRank = dungeonRank;
        this.dungeonStatus = dungeonStatus;
        this.totalFloors = totalFloors;
        this.completedFloors = completedFloors;
        this.createdAt = createdAt;
        this.deadlineAt = deadlineAt;
        this.completedAt = completedAt;
        this.failedAt = failedAt;
        this.abandonedAt = abandonedAt;
    }

    public String getDungeonId() { return dungeonId; }
    public void setDungeonId(String dungeonId) { this.dungeonId = dungeonId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDungeonRank() { return dungeonRank; }
    public void setDungeonRank(String dungeonRank) { this.dungeonRank = dungeonRank; }

    public String getDungeonStatus() { return dungeonStatus; }
    public void setDungeonStatus(String dungeonStatus) { this.dungeonStatus = dungeonStatus; }

    public int getTotalFloors() { return totalFloors; }
    public void setTotalFloors(int totalFloors) { this.totalFloors = totalFloors; }

    public int getCompletedFloors() { return completedFloors; }
    public void setCompletedFloors(int completedFloors) { this.completedFloors = completedFloors; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }

    public LocalDateTime getAbandonedAt() { return abandonedAt; }
    public void setAbandonedAt(LocalDateTime abandonedAt) { this.abandonedAt = abandonedAt; }
}
