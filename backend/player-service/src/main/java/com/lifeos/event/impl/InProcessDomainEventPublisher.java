package com.lifeos.event.impl;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.DomainEventHandler;
import com.lifeos.event.DomainEventPublisher;
import com.lifeos.event.EventCategory;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.service.PlayerStateService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InProcessDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InProcessDomainEventPublisher.class);
    private final List<DomainEventHandler<?>> handlers;
    private final PlayerStateService playerStateService;

    @Override
    @SuppressWarnings("unchecked")
    public void publish(DomainEvent event) {
        // 1. Centralized Suppression Guard
        // Suppress if in Penalty AND event is NORMAL (Not Critical, Not Progress)
        boolean inPenalty = playerStateService.hasActiveFlag(event.getPlayerId(), StatusFlagType.PENALTY_ZONE);
        boolean isSuppressible = event.getCategory() == EventCategory.NORMAL && !event.isCritical();

        if (inPenalty && isSuppressible) {
            log.info("Event Suppressed via Penalty Rules: {}", event.getClass().getSimpleName());
            return;
        }

        // 2. Dispatch
        handlers.stream()
                .filter(h -> h.supports(event))
                .forEach(h -> ((DomainEventHandler<DomainEvent>) h).handle(event));
    }
}
