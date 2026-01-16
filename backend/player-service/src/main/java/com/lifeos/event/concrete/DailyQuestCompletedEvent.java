package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import java.time.LocalDateTime;
import java.util.UUID;

public class DailyQuestCompletedEvent extends DomainEvent {
    public DailyQuestCompletedEvent(UUID playerId) {
        super(UUID.randomUUID(), playerId, LocalDateTime.now(), false, EventCategory.PENALTY_PROGRESS);
    }
}
