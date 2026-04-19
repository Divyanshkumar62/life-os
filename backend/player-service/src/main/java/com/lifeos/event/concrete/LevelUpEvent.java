package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import lombok.Getter;

import java.util.UUID;

@Getter
public class LevelUpEvent extends DomainEvent {
    private final UUID playerId;
    private final int newLevel;
    
    public LevelUpEvent(UUID playerId, int newLevel) {
        super(UUID.randomUUID(), playerId, java.time.LocalDateTime.now(), false, EventCategory.POSITIVE);
        this.playerId = playerId;
        this.newLevel = newLevel;
    }
}
