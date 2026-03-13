package com.lifeos.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.ai.service.PlayerContextService;
import com.lifeos.quest.dto.QuestRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

// Note: Testing private method 'parseQuests' via public 'generateQuests' 
// requires mocking the RestClient which is complex. 
// For this unit test, we will focus on whether it handles "placeholder" key correctly
// and if we can extract logic to a public parser if needed. 
// For now, let's test the key validation.

@ExtendWith(MockitoExtension.class)
public class GeminiQuestServiceTest {

    @Mock
    private PlayerContextService contextService;
    
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GeminiQuestService geminiQuestService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(geminiQuestService, "apiKey", "placeholder");
    }

    @Test
    void generateQuests_ShouldReturnEmpty_WhenKeyIsPlaceholder() {
        List<QuestRequest> quests = geminiQuestService.generateQuests(UUID.randomUUID(), 3);
        assertTrue(quests.isEmpty(), "Should return empty list for placeholder key");
    }
}
