package com.lifeos.event.concrete;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.EventCategory;
import com.lifeos.quest.domain.enums.QuestType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class QuestFailedEvent extends DomainEvent {
    private final UUID questId;
    private final String questTitle;
    private final QuestType questType;

    public QuestFailedEvent(UUID playerId, UUID questId, String questTitle, QuestType questType) {
        super(UUID.randomUUID(), playerId, java.time.LocalDateTime.now(), false, com.lifeos.event.EventCategory.NORMAL);
        this.questId = questId;
        this.questTitle = questTitle;
        this.questType = questType;
    }

    public UUID getQuestId() { return questId; }
    public String getQuestTitle() { return questTitle; }
    public QuestType getQuestType() { return questType; }
}
