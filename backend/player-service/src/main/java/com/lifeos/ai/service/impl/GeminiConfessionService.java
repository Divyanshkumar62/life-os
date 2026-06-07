package com.lifeos.ai.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.ai.service.ConfessionResult;
import com.lifeos.ai.service.ConfessionService;
import com.lifeos.onboarding.domain.PlayerProfile;
import com.lifeos.onboarding.repository.PlayerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Service
public class GeminiConfessionService implements ConfessionService {

    private static final Logger log = LoggerFactory.getLogger(GeminiConfessionService.class);

    @Value("${spring.ai.gemini.api-key}")
    private String apiKey;

    @Value("${spring.ai.gemini.url:https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash-8b:generateContent}")
    private String apiUrl;

    private final RestClient restClient = RestClient.builder().build();
    private final PlayerProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    public GeminiConfessionService(PlayerProfileRepository profileRepository, ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ConfessionResult judgeConfession(UUID playerId, String confessionText) {
        log.info("Evaluating confession for player {}", playerId);

        PlayerProfile profile = profileRepository.findByPlayerId(playerId).orElse(null);
        String challenge = profile != null ? profile.getBiggestChallenge() : "None";

        String systemPrompt = "You are the Architect of the System. The player failed their daily quests. Analyze this confession (max 600 words). If it shows genuine accountability and a real plan to improve, accept it. If it is lazy, an excuse (e.g., 'I was tired'), or trolling, brutally reject it. Return strictly JSON: { 'accepted': boolean, 'feedback': 'string (max 2 sentences)' }.";
        String fullPrompt = systemPrompt + "\n\nPlayer's Biggest Challenge: " + challenge + "\nPlayer's Confession: " + confessionText;

        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("placeholder")) {
            log.warn("Gemini API Key is missing. Triggering local clinical fallback.");
            return evaluateFallback(confessionText);
        }

        GeminiRequest requestBody = new GeminiRequest(fullPrompt);
        java.util.Set<String> probeUrls = new java.util.LinkedHashSet<>();
        probeUrls.add(apiUrl);
        probeUrls.add("https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash-8b:generateContent");
        probeUrls.add("https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent");

        for (String probeUrl : probeUrls) {
            try {
                log.info("Attempting Gemini Confession API call to: {}", probeUrl);
                GeminiResponse response = restClient.post()
                        .uri(probeUrl + "?key=" + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(GeminiResponse.class);

                if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                    String contentResult = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                    String cleanJson = cleanJsonResponse(contentResult);
                    ConfessionSchema schema = objectMapper.readValue(cleanJson, ConfessionSchema.class);
                    log.info("Gemini evaluation response parsed successfully: accepted={}", schema.isAccepted());
                    return ConfessionResult.builder()
                            .accepted(schema.isAccepted())
                            .feedback(schema.getFeedback())
                            .build();
                }
            } catch (Exception e) {
                log.warn("Gemini confession evaluation failed for URL {}: {}", probeUrl, e.getMessage());
            }
        }

        log.warn("All Gemini Confession API calls failed. Triggering local clinical fallback.");
        return evaluateFallback(confessionText);
    }

    private ConfessionResult evaluateFallback(String confessionText) {
        if (confessionText == null) {
            return new ConfessionResult(false, "[SYSTEM] The Architect rejects your silence. Submit a written confession.");
        }
        String cleanText = confessionText.trim().toLowerCase();
        
        // Troll check
        if (cleanText.length() < 30) {
            return new ConfessionResult(false, "[SYSTEM] Confession is too brief. The Architect demands depth and accountability.");
        }
        if (cleanText.contains("lol") || cleanText.contains("troll") || cleanText.contains("whatever") || cleanText.contains("no") || cleanText.contains("lazy") || cleanText.contains("sleep")) {
            return new ConfessionResult(false, "[SYSTEM] Excuses and mockery will not clear this penalty. Take responsibility for your shortcomings.");
        }
        
        return new ConfessionResult(true, "[SYSTEM] The Architect senses sincere self-correction. Access to the System has been restored.");
    }

    private String cleanJsonResponse(String response) {
        if (response == null) return "{}";
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConfessionSchema {
        private boolean accepted;
        private String feedback;

        public ConfessionSchema() {}

        public boolean isAccepted() { return accepted; }
        public void setAccepted(boolean accepted) { this.accepted = accepted; }
        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }

    public static class GeminiRequest {
        private List<Content> contents;

        public GeminiRequest() {}
        public GeminiRequest(String promptText) {
            this.contents = List.of(new Content(List.of(new Part(promptText)), "user"));
        }
        public List<Content> getContents() { return contents; }
        public void setContents(List<Content> contents) { this.contents = contents; }
    }

    public static class Content {
        private List<Part> parts;
        private String role = "user";

        public Content() {}
        public Content(List<Part> parts, String role) { this.parts = parts; this.role = role; }
        public List<Part> getParts() { return parts; }
        public void setParts(List<Part> parts) { this.parts = parts; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class Part {
        private String text;

        public Part() {}
        public Part(String text) { this.text = text; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    public static class GeminiResponse {
        private List<Candidate> candidates;

        public GeminiResponse() {}
        public List<Candidate> getCandidates() { return candidates; }
        public void setCandidates(List<Candidate> candidates) { this.candidates = candidates; }
    }

    public static class Candidate {
        private Content content;

        public Candidate() {}
        public Content getContent() { return content; }
        public void setContent(Content content) { this.content = content; }
    }
}
