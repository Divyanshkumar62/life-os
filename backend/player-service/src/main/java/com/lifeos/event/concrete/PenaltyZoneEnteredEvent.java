package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import java.time.LocalDateTime;
import java.util.UUID;

public class PenaltyZoneEnteredEvent extends DomainEvent {
    public PenaltyZoneEnteredEvent(UUID playerId) {
        super(UUID.randomUUID(), playerId, LocalDateTime.now(), true, EventCategory.SYSTEM);
    }
}
