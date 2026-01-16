package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import com.lifeos.quest.domain.enums.QuestType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class QuestFailedEvent extends DomainEvent {
    private final UUID questId;
    private final String questTitle;
    private final QuestType questType;

    public QuestFailedEvent(UUID playerId, UUID questId, String questTitle, QuestType questType) {
        super(UUID.randomUUID(), playerId, LocalDateTime.now(), false, EventCategory.NORMAL);
        this.questId = questId;
        this.questTitle = questTitle;
        this.questType = questType;
    }
}
