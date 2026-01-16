package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class QuestExpiredEvent extends DomainEvent {
    private final UUID questId;
    private final String questTitle;

    public QuestExpiredEvent(UUID playerId, UUID questId, String questTitle) {
        super(UUID.randomUUID(), playerId, LocalDateTime.now(), false, EventCategory.NORMAL);
        this.questId = questId;
        this.questTitle = questTitle;
    }
}
