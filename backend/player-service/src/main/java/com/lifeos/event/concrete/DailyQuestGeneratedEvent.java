package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DailyQuestGeneratedEvent extends DomainEvent {
    private final UUID questId;

    public DailyQuestGeneratedEvent(UUID playerId, UUID questId) {
        super(UUID.randomUUID(), playerId, LocalDateTime.now(), false, EventCategory.NORMAL);
        this.questId = questId;
    }
}
