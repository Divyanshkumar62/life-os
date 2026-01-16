package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import com.lifeos.penalty.domain.enums.FailureReason;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PenaltyAppliedEvent extends DomainEvent {
    private final UUID questId;
    private final FailureReason reason;
    private final long xpDeduction;

    public PenaltyAppliedEvent(UUID playerId, UUID questId, FailureReason reason, long xpDeduction) {
        super(UUID.randomUUID(), playerId, LocalDateTime.now(), false, EventCategory.NEGATIVE);
        this.questId = questId;
        this.reason = reason;
        this.xpDeduction = xpDeduction;
    }
}
