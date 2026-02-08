package com.lifeos.ai.service.impl;

import com.lifeos.ai.service.AIQuestService;
import com.lifeos.quest.dto.QuestRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiQuestService implements AIQuestService {

    @Value("${ai.gemini.api-key:placeholder}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-pro}")
    private String model;

    private final RestClient restClient = RestClient.create();

    @Override
    public List<QuestRequest> generateQuests(UUID playerId, int count) {
        if ("placeholder".equals(apiKey)) {
            log.warn("Gemini API Key is missing. Skipping AI quest generation.");
            return Collections.emptyList();
        }

        // TODO: Implement actual API call to Google Generative AI
        // Endpoint: https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=YOUR_API_KEY
        
        log.info("Generating {} quests for player {} using model {}", count, playerId, model);
        
        // Placeholder logic for Phase 0.1
        return Collections.emptyList();
    }
}
