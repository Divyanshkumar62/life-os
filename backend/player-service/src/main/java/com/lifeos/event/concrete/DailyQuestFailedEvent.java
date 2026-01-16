package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import java.time.LocalDateTime;
import java.util.UUID;

public class DailyQuestFailedEvent extends DomainEvent {
    public DailyQuestFailedEvent(UUID playerId) {
        super(UUID.randomUUID(), playerId, LocalDateTime.now(), true, EventCategory.SYSTEM);
    }
}
