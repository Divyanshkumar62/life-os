package com.lifeos.event.handler;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.DomainEventHandler;
import com.lifeos.event.concrete.PenaltyZoneEnteredEvent;
import com.lifeos.streak.service.StreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StreakEventHandler implements DomainEventHandler<DomainEvent> {

    private final StreakService streakService;

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof PenaltyZoneEnteredEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        if (event instanceof PenaltyZoneEnteredEvent) {
            streakService.resetStreak(event.getPlayerId());
        }
    }
}
