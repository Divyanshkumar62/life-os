package com.lifeos.event.impl;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.DomainEventHandler;
import com.lifeos.event.EventCategory;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.service.PlayerStateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InProcessDomainEventPublisherTest {

    @Mock
    private PlayerStateService playerStateService;

    @Mock
    private DomainEventHandler<DomainEvent> handler;

    private InProcessDomainEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new InProcessDomainEventPublisher(List.of(handler), playerStateService);
    }

    @Test
    void testPublish_WithPenaltyUsingSuppression_SuppressesNormalEvent() {
        UUID playerId = UUID.randomUUID();
        DomainEvent normalEvent = new TestEvent(playerId, EventCategory.NORMAL, false);

        when(playerStateService.hasActiveFlag(playerId, StatusFlagType.PENALTY_ZONE)).thenReturn(true);

        publisher.publish(normalEvent);

        verify(handler, never()).handle(any());
    }

    @Test
    void testPublish_WithPenalty_AllowsCriticalEvent() {
        UUID playerId = UUID.randomUUID();
        DomainEvent criticalEvent = new TestEvent(playerId, EventCategory.NORMAL, true);

        when(playerStateService.hasActiveFlag(playerId, StatusFlagType.PENALTY_ZONE)).thenReturn(true);
        when(handler.supports(criticalEvent)).thenReturn(true);

        publisher.publish(criticalEvent);

        verify(handler).handle(criticalEvent);
    }

    @Test
    void testPublish_NoPenalty_AllowsNormalEvent() {
        UUID playerId = UUID.randomUUID();
        DomainEvent normalEvent = new TestEvent(playerId, EventCategory.NORMAL, false);

        when(playerStateService.hasActiveFlag(playerId, StatusFlagType.PENALTY_ZONE)).thenReturn(false);
        when(handler.supports(normalEvent)).thenReturn(true);

        publisher.publish(normalEvent);

        verify(handler).handle(normalEvent);
    }

    // specific test event implementation
    static class TestEvent extends DomainEvent {
        protected TestEvent(UUID playerId, EventCategory category, boolean critical) {
            super(UUID.randomUUID(), playerId, LocalDateTime.now(), critical, category);
        }
    }
}
