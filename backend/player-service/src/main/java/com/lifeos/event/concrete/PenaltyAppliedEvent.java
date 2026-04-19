package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import com.lifeos.penalty.domain.enums.FailureReason;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class PenaltyAppliedEvent extends DomainEvent {
    private final UUID questId;
    private final FailureReason reason;
    private final long xpDeduction;

    public PenaltyAppliedEvent(UUID playerId, UUID questId, FailureReason reason, long xpDeduction) {
        super(UUID.randomUUID(), playerId, java.time.LocalDateTime.now(), false, com.lifeos.event.EventCategory.NEGATIVE);
        this.questId = questId;
        this.reason = reason;
        this.xpDeduction = xpDeduction;
    }

    public UUID getQuestId() { return questId; }
    public FailureReason getReason() { return reason; }
    public long getXpDeduction() { return xpDeduction; }
}
