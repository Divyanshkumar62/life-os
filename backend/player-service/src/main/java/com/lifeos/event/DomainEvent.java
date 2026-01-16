package com.lifeos.event;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class DomainEvent {
    private final UUID eventId;
    private final UUID playerId;
    private final LocalDateTime occurredAt;
    private final boolean critical;
    private final EventCategory category;

    protected DomainEvent(UUID eventId, UUID playerId, LocalDateTime occurredAt, boolean critical, EventCategory category) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID();
        this.playerId = playerId;
        this.occurredAt = occurredAt != null ? occurredAt : LocalDateTime.now();
        this.critical = critical;
        this.category = category;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public boolean isCritical() {
        return critical;
    }

    public EventCategory getCategory() {
        return category;
    }
}
