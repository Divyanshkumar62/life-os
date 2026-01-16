package com.lifeos.event.handler;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.DomainEventHandler;
import com.lifeos.event.concrete.QuestCompletedEvent;
import com.lifeos.event.concrete.QuestFailedEvent;
import com.lifeos.progression.service.ProgressionService;
import com.lifeos.quest.domain.enums.QuestType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProgressionEventHandler implements DomainEventHandler<DomainEvent> {

    private final ProgressionService progressionService;

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof QuestCompletedEvent || event instanceof QuestFailedEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        if (event instanceof QuestCompletedEvent questEvent) {
            if (questEvent.getQuestType() == QuestType.PROMOTION_EXAM) {
                progressionService.processPromotionOutcome(questEvent.getPlayerId(), true);
            }
        } else if (event instanceof QuestFailedEvent failEvent) {
            if (failEvent.getQuestType() == QuestType.PROMOTION_EXAM) {
                progressionService.processPromotionOutcome(failEvent.getPlayerId(), false);
            }
        }
    }
}
