package com.lifeos.progression.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiAIService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIService.class);
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent}")
    private String geminiApiUrl;

    public List<String> generateTasks(String prompt) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            log.warn("Gemini API key not configured, returning empty list");
            return List.of();
        }

        try {
            // Build request body for Gemini API
            Map<String, Object> request = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            Map<String, Object> parts = new HashMap<>();

            parts.put("text", prompt);
            contents.put("parts", Arrays.asList(parts));
            request.put("contents", Arrays.asList(contents));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            // Call Gemini API with API key
            String url = geminiApiUrl + "?key=" + geminiApiKey;
            Object response = restTemplate.postForObject(url, entity, Object.class);

            // Parse response and extract text (this is a simplified version)
            // In real implementation, you'd parse the Gemini response structure
            log.debug("Gemini API response received");

            // For now, return empty list - in production you'd parse the response
            // and extract the generated tasks
            return List.of();

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            return List.of();
        }
    }
}
