package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import com.lifeos.quest.domain.enums.QuestType;
import java.time.LocalDateTime;
import java.util.UUID;

public class QuestCompletedEvent extends DomainEvent {
    private final UUID questId;
    private final QuestType questType;

    public QuestCompletedEvent(UUID playerId, UUID questId, QuestType questType) {
        super(UUID.randomUUID(), playerId, LocalDateTime.now(), false, EventCategory.PENALTY_PROGRESS);
        this.questId = questId;
        this.questType = questType;
    }

    public UUID getQuestId() {
        return questId;
    }

    public QuestType getQuestType() {
        return questType;
    }
}
