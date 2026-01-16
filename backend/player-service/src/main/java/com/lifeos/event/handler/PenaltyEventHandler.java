package com.lifeos.event.handler;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.DomainEventHandler;
import com.lifeos.event.concrete.DailyQuestFailedEvent;
import com.lifeos.event.concrete.PenaltyQuestCompletedEvent;
import com.lifeos.event.concrete.DailyQuestCompletedEvent;
import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.penalty.service.PenaltyQuestService;
import com.lifeos.penalty.domain.enums.WorkSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PenaltyEventHandler implements DomainEventHandler<DomainEvent> {

    private final PenaltyService penaltyService;
    private final PenaltyQuestService penaltyQuestService;

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof DailyQuestFailedEvent ||
               event instanceof PenaltyQuestCompletedEvent ||
               event instanceof DailyQuestCompletedEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        if (event instanceof DailyQuestFailedEvent) {
            // Trigger Penalty Entry
            penaltyService.enterPenaltyZone(event.getPlayerId(), "Start of Day Check (Daily Failed)");
        } else if (event instanceof PenaltyQuestCompletedEvent) {
            // Trigger Penalty Exit
            penaltyService.exitPenaltyZone(event.getPlayerId());
        } else if (event instanceof DailyQuestCompletedEvent) {
            // Record Potential Work
            penaltyQuestService.recordWork(event.getPlayerId(), 1, WorkSource.DAILY_QUEST);
        }
    }
}
