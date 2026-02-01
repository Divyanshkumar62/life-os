package com.lifeos.player.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerReadServiceTest {

    @Mock
    private PlayerStateRepository repository;

    private PlayerReadService service;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        service = new PlayerReadService(repository);
        playerId = UUID.randomUUID();
    }

    @Test
    void testGetSnapshot_ReturnsEmptyDefault() {
        when(repository.findById(playerId)).thenReturn(Optional.empty());

        PlayerStateSnapshot snapshot = service.getSnapshot(playerId);

        assertNotNull(snapshot);
        assertEquals(playerId, snapshot.getPlayerId());
        assertFalse(snapshot.isInPenaltyZone());
        assertFalse(snapshot.isStreakActive());
    }

    @Test
    void testIsInPenalty_ReturnsTrue() {
        PlayerStateSnapshot existing = PlayerStateSnapshot.builder()
                .playerId(playerId)
                .inPenaltyZone(true)
                .build();
        when(repository.findById(playerId)).thenReturn(Optional.of(existing));

        assertTrue(service.isInPenalty(playerId));
    }

    @Test
    void testIsRewardAllowed_Allowed() {
        when(repository.findById(playerId)).thenReturn(Optional.empty()); // Not in penalty, no flags
        assertTrue(service.isRewardAllowed(playerId));
    }

    @Test
    void testIsRewardAllowed_DeniedByPenalty() {
        PlayerStateSnapshot existing = PlayerStateSnapshot.builder()
                .playerId(playerId)
                .inPenaltyZone(true)
                .build();
        when(repository.findById(playerId)).thenReturn(Optional.of(existing));
        
        assertFalse(service.isRewardAllowed(playerId));
    }

    @Test
    void testIsRewardAllowed_DeniedByFlag() {
        PlayerStateSnapshot existing = PlayerStateSnapshot.builder()
                .playerId(playerId)
                .activeFlags(Set.of(PlayerFlag.REWARDS_SUPPRESSED))
                .build();
        when(repository.findById(playerId)).thenReturn(Optional.of(existing));
        
        assertFalse(service.isRewardAllowed(playerId));
    }
}
