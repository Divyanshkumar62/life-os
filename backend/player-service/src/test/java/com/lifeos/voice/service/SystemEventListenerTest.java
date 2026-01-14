package com.lifeos.voice.service;

import com.lifeos.player.service.PlayerStateService;
import com.lifeos.voice.domain.enums.SystemMessageType;
import com.lifeos.voice.event.VoiceSystemEvent;
import com.lifeos.player.domain.enums.StatusFlagType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemEventListenerTest {

    @Mock
    private SystemVoiceService voiceService;

    @Mock
    private PlayerStateService playerStateService;

    @InjectMocks
    private SystemEventListener eventListener;

    private UUID playerId;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
    }

    @Test
    void testHandleEvent_NormalState_PassThrough() {
        // Given: No Penalty
        when(playerStateService.hasActiveFlag(playerId, StatusFlagType.PENALTY_ZONE)).thenReturn(false);
        
        VoiceSystemEvent event = VoiceSystemEvent.builder()
                .playerId(playerId)
                .type(SystemMessageType.QUEST_COMPLETED)
                .payload(Collections.emptyMap())
                .build();

        // When
        eventListener.handleVoiceSystemEvent(event);

        // Then: Passed to Service
        verify(voiceService).generateMessage(eq(playerId), eq(SystemMessageType.QUEST_COMPLETED), any(), eq(event.getEventId()));
    }

    @Test
    void testHandleEvent_PenaltyState_SuppressNonCritical() {
        // Given: Active Penalty
        when(playerStateService.hasActiveFlag(playerId, StatusFlagType.PENALTY_ZONE)).thenReturn(true);
        
        VoiceSystemEvent event = VoiceSystemEvent.builder()
                .playerId(playerId)
                .type(SystemMessageType.QUEST_COMPLETED) // Non-Critical
                .payload(Collections.emptyMap())
                .build();

        // When
        eventListener.handleVoiceSystemEvent(event);

        // Then: Suppressed (Service NOT called)
        verify(voiceService, never()).generateMessage(any(), any(), any(), any());
    }

    @Test
    void testHandleEvent_PenaltyState_AllowCritical() {
        // Given: Active Penalty
        when(playerStateService.hasActiveFlag(playerId, StatusFlagType.PENALTY_ZONE)).thenReturn(true);
        
        VoiceSystemEvent event = VoiceSystemEvent.builder()
                .playerId(playerId)
                .type(SystemMessageType.PENALTY_QUEST_COMPLETED) // Critical
                .payload(Collections.emptyMap())
                .build();

        // When
        eventListener.handleVoiceSystemEvent(event);

        // Then: Passed to Service
        verify(voiceService).generateMessage(eq(playerId), eq(SystemMessageType.PENALTY_QUEST_COMPLETED), any(), eq(event.getEventId()));
    }
}
