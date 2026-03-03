package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import java.util.UUID;
import java.time.LocalDateTime;

public class ProjectCompletedEvent extends DomainEvent {
    private final UUID projectId;
    private final int bossKeyReward;

    public ProjectCompletedEvent(UUID playerId, UUID projectId, int bossKeyReward) {
        super(UUID.randomUUID(), playerId, LocalDateTime.now(), false, EventCategory.POSITIVE);
        this.projectId = projectId;
        this.bossKeyReward = bossKeyReward;
    }

    public UUID getProjectId() { return projectId; }
    public int getBossKeyReward() { return bossKeyReward; }
}
