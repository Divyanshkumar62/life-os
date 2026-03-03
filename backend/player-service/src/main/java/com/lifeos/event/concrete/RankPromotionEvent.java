package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RankPromotionEvent extends DomainEvent {
    private final UUID playerId;
    private final String newRank;
    
    public RankPromotionEvent(UUID playerId, String newRank) {
        super(UUID.randomUUID(), playerId, java.time.LocalDateTime.now(), false, EventCategory.POSITIVE);
        this.playerId = playerId;
        this.newRank = newRank;
    }
}
