package com.lifeos.voice.service;

import com.lifeos.voice.domain.SystemMessage;
import com.lifeos.voice.domain.enums.SystemMessageType;
import com.lifeos.voice.domain.enums.SystemVoiceMode;
import com.lifeos.voice.repository.SystemMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SystemVoiceServiceTest {

    @Mock
    private SystemMessageRepository messageRepository;

    @InjectMocks
    private SystemVoiceService voiceService;

    private UUID playerId;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
    }

    @Test
    void testGenerateMessage_SimpleReward() {
        // When
        voiceService.generateMessage(playerId, SystemMessageType.QUEST_COMPLETED_SIMPLE, Collections.emptyMap(), UUID.randomUUID());

        // Then
        ArgumentCaptor<SystemMessage> captor = ArgumentCaptor.forClass(SystemMessage.class);
        verify(messageRepository).save(captor.capture());

        SystemMessage saved = captor.getValue();
        assertEquals(playerId, saved.getPlayerId());
        assertEquals(SystemVoiceMode.REWARD, saved.getMode());
        assertEquals("SYSTEM REWARD", saved.getTitle());
        assertEquals(SystemMessageType.QUEST_COMPLETED_SIMPLE.getTemplate(), saved.getBody());
        assertFalse(saved.isRead());
    }

    @Test
    void testGenerateMessage_WithPayload() {
        // Given template: "Specific template needed but not defined in Enum with vars yet"
        // Let's assume we adhere to what we have or test the replace logic generically.
        // Actually, current Enums don't have ${} vars in them yet in the detailed list, 
        // but the Service supports it. Let's act as if we are replacing something just to test logic
        // even if it's not in the string (it won't break).
        
        // Wait, I should add a test case that actually uses replacement if future templates use it.
        // For now, let's verify it DOESN'T error on empty payload or unused payload.
        
        Map<String, Object> payload = Map.of("xp", 100);
        voiceService.generateMessage(playerId, SystemMessageType.QUEST_COMPLETED_SIMPLE, payload, UUID.randomUUID());
        
        // Then
        ArgumentCaptor<SystemMessage> captor = ArgumentCaptor.forClass(SystemMessage.class);
        verify(messageRepository).save(captor.capture());
        assertEquals(SystemMessageType.QUEST_COMPLETED_SIMPLE.getTemplate(), captor.getValue().getBody());
    }

    @Test
    void testGenerateMessage_Penalty() {
        voiceService.generateMessage(playerId, SystemMessageType.PENALTY_ZONE_ENTRY, Collections.emptyMap(), UUID.randomUUID());

        ArgumentCaptor<SystemMessage> captor = ArgumentCaptor.forClass(SystemMessage.class);
        verify(messageRepository).save(captor.capture());

        SystemMessage saved = captor.getValue();
        assertEquals(SystemVoiceMode.PENALTY, saved.getMode());
        assertEquals("SYSTEM PENALTY", saved.getTitle());
        assertTrue(saved.getBody().contains("Penalty Zone activated"));
    }
    
    @Test
    void testTitleLogic() {
        // Check Failure
        voiceService.generateMessage(playerId, SystemMessageType.PROMOTION_FAILED, Collections.emptyMap(), UUID.randomUUID());
        ArgumentCaptor<SystemMessage> captor = ArgumentCaptor.forClass(SystemMessage.class);
        verify(messageRepository).save(captor.capture());
        assertEquals("SYSTEM FAILURE", captor.getValue().getTitle());
    }
}
