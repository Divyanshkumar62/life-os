package com.lifeos.player.state;

import com.lifeos.event.concrete.PenaltyZoneEnteredEvent;
import com.lifeos.event.concrete.PenaltyZoneExitedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReadModelIntegrationTest {

    @Autowired
    private PlayerReadService readService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private PlayerStateRepository repository;

    @Test
    void testEventFlow_UpdatesReadModel() {
        UUID playerId = UUID.randomUUID();

        // 1. Initial State
        assertFalse(readService.isInPenalty(playerId));

        // 2. Publish Penalty Event
        publisher.publishEvent(new PenaltyZoneEnteredEvent(playerId));

        // 3. Verify Update (Synchronous transactional listener)
        assertTrue(readService.isInPenalty(playerId));
        assertTrue(readService.hasActiveFlag(playerId, PlayerFlag.PENALTY_ZONE));

        // 4. Publish Exit Event
        publisher.publishEvent(new PenaltyZoneExitedEvent(playerId));

        // 5. Verify Cleanup
        assertFalse(readService.isInPenalty(playerId));
        assertFalse(readService.hasActiveFlag(playerId, PlayerFlag.PENALTY_ZONE));
    }
}
