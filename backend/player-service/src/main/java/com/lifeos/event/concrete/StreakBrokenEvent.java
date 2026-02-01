package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import java.time.LocalDateTime;
import java.util.UUID;

public class StreakBrokenEvent extends DomainEvent {
    private final int previousStreak;
    private final String reason;

    public StreakBrokenEvent(UUID playerId, int previousStreak, String reason) {
        super(UUID.randomUUID(), playerId, LocalDateTime.now(), true, EventCategory.PENALTY_PROGRESS);
        this.previousStreak = previousStreak;
        this.reason = reason;
    }

    public int getPreviousStreak() {
        return previousStreak;
    }

    public String getReason() {
        return reason;
    }
}
