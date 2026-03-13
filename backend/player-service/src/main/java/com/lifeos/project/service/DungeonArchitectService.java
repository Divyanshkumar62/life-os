package com.lifeos.project.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.project.dto.DungeonResponse;
import com.lifeos.ai.service.impl.GeminiQuestService; // Reuse DTOs if possible, or define own
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class DungeonArchitectService {

    private static final Logger log = LoggerFactory.getLogger(DungeonArchitectService.class);

    @Value("${spring.ai.gemini.api-key}")
    private String apiKey;

    @Value("${spring.ai.gemini.url:https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent}")
    private String apiUrl;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;
    private final com.lifeos.player.service.PlayerHistoryService historyService;

    public DungeonArchitectService(ObjectMapper objectMapper, com.lifeos.player.service.PlayerHistoryService historyService) {
        this.objectMapper = objectMapper;
        this.historyService = historyService;
    }

    public DungeonResponse generateDungeon(java.util.UUID playerId, String goal, String userRank) {
        log.info("Summoning the Architect for goal: '{}', rank: {}", goal, userRank);

        // 1. Fetch Player History
        com.lifeos.player.service.PlayerHistoryService.PlayerDossier dossier = historyService.getPlayerDossier(playerId);

        // 2. Build Prompt
        String prompt = buildArchitectPrompt(goal, userRank, dossier);
        GeminiQuestService.GeminiRequest requestBody = new GeminiQuestService.GeminiRequest(prompt);

        // 3. Call AI
        return callGemini(requestBody);
    }

    private String buildArchitectPrompt(String goal, String userRank, com.lifeos.player.service.PlayerHistoryService.PlayerDossier dossier) {
        String historyContext = (dossier != null) ? dossier.toPromptString() : "New Player Record.";

        return """
            **ROLE:**
            You are "The Architect," the AI system behind a real-life Solo Leveling game. Your job is to analyze a user's real-world goal and construct a "Dungeon" (Project) for them to clear.
            
            **PLAYER CONTEXT:**
            %s
            
            **INPUT:**
            User Goal: "%s"
            User Current Rank: "%s" (e.g., E-Rank)
            
            **RULES:**
            1. **VALIDATION (The Gatekeeper):**
               - A Dungeon MUST require >7 days of effort and >5 distinct steps.
               - If the goal is too simple (e.g., "Clean room", "Read 1 chapter"), REJECT it. Tell the user this is a "Mob" (Daily Quest), not a "Dungeon."
               - If the goal is vague (e.g., "Get rich"), REJECT it. Demand specific clear conditions.
            
            2. **STRUCTURE (The Blueprint):**
               - **Rank:** Assign a difficulty Rank (E, D, C, B, A, S) based on effort/time required.
               - **Floors:** Break the goal into 5-15 actionable Sub-Quests. Each Sub-Quest is a "Floor" of the dungeon.
               - **Boss:** The final milestone is the "Dungeon Boss." Give it a cool metaphorical name (e.g., Goal: "Learn Python" -> Boss: "The Serpent of Syntax").
               - **Stats:** Assign a primary Stat (STR, INT, VIT, SEN) to each floor based on the activity.
            
            3. **REWARDS:**
               - **XP:** Scale XP based on Rank (E=100, D=200, etc.).
               - **Keys:** A Dungeon ALWAYS drops exactly **1 Boss Key** upon 100% completion.
            
            **OUTPUT FORMAT (STRICT JSON ONLY):**
            Return ONLY raw JSON. No markdown, no conversation.
            
            {
              "is_valid": boolean,
              "rejection_reason": "String (Only if is_valid is false. Use 'System' tone: 'Notice: Magical energy insufficient for Dungeon creation...')",
              "dungeon": {
                "title": "String (Epic Name for the Project)",
                "description": "String (System description of the mission)",
                "rank": "E" | "D" | "C" | "B" | "A" | "S",
                "boss_name": "String",
                "stat_focus": "STR" | "INT" | "VIT" | "SEN",
                "estimated_duration_days": number,
                "floors": [
                  {
                    "floor_num": 1,
                    "title": "String (Actionable Sub-Task)",
                    "xp": number,
                    "stat": "STR" | "INT" | "VIT" | "SEN"
                  }
                  // ... more floors
                ],
                "loot": {
                  "xp_total": number,
                  "gold": number,
                  "boss_keys": 1
                }
              }
            }
            """.formatted(goal, userRank);
    }

    private DungeonResponse callGemini(GeminiQuestService.GeminiRequest requestBody) {
        java.util.Set<String> probeUrls = new java.util.LinkedHashSet<>();
        probeUrls.add(apiUrl);
        probeUrls.add("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent");
        
        for (String probeUrl : probeUrls) {
            try {
                GeminiQuestService.GeminiResponse response = restClient.post()
                        .uri(probeUrl + "?key=" + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(GeminiQuestService.GeminiResponse.class);

                if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                    String text = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                    return parseResponse(text);
                }
            } catch (Exception e) {
                log.warn("Gemini call failed for URL {}: {}", probeUrl, e.getMessage());
            }
        }
        return DungeonResponse.builder().isValid(false).rejectionReason("System Offline: The Architect is unreachable.").build();
    }

    private DungeonResponse parseResponse(String jsonContent) {
        try {
            String cleanJson = jsonContent.replaceAll("```json", "").replaceAll("```", "").trim();
            if (cleanJson.startsWith("json")) {
                 cleanJson = cleanJson.substring(4).trim();
            }
            return objectMapper.readValue(cleanJson, DungeonResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse Dungeon Architect response", e);
            return DungeonResponse.builder().isValid(false).rejectionReason("System Error: The Architect spoke in riddles.").build();
        }
    }
}
