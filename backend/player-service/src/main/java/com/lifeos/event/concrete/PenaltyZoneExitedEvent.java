package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PenaltyZoneExitedEvent extends DomainEvent {
    
    public PenaltyZoneExitedEvent(UUID playerId) {
        super(UUID.randomUUID(), playerId, LocalDateTime.now(), false, EventCategory.POSITIVE); 
    }
}
