package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import java.util.UUID;
import java.time.LocalDateTime;

public class ProjectCompletedEvent extends DomainEvent {
    private final UUID projectId;
    private final int bossKeyReward;
    private final int baseXpReward;
    private final int baseGoldReward;

    public ProjectCompletedEvent(UUID playerId, UUID projectId, int bossKeyReward, int baseXpReward, int baseGoldReward) {
        super(UUID.randomUUID(), playerId, LocalDateTime.now(), false, EventCategory.POSITIVE);
        this.projectId = projectId;
        this.bossKeyReward = bossKeyReward;
        this.baseXpReward = baseXpReward;
        this.baseGoldReward = baseGoldReward;
    }

    public UUID getProjectId() { return projectId; }
    public int getBossKeyReward() { return bossKeyReward; }
    public int getBaseXpReward() { return baseXpReward; }
    public int getBaseGoldReward() { return baseGoldReward; }
}
