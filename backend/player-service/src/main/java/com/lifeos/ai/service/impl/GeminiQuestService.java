package com.lifeos.ai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.ai.service.AIQuestService;
import com.lifeos.ai.service.PlayerContextService;
import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.domain.enums.Priority;
import com.lifeos.quest.domain.enums.QuestType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lifeos.quest.dto.QuestRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GeminiQuestService implements AIQuestService {

    private static final Logger log = LoggerFactory.getLogger(GeminiQuestService.class);

    @Value("${spring.ai.gemini.api-key}")
    private String apiKey;

    @Value("${spring.ai.gemini.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}")
    private String apiUrl;

    private RestClient restClient = RestClient.builder().build();
    private PlayerContextService contextService;
    private ObjectMapper objectMapper;
    private com.lifeos.quest.repository.QuestRepository questRepository;
    
    public GeminiQuestService(PlayerContextService contextService, ObjectMapper objectMapper, com.lifeos.quest.repository.QuestRepository questRepository) {
        this.contextService = contextService;
        this.objectMapper = objectMapper;
        this.questRepository = questRepository;
    }


    @Override
    public QuestRequest generatePromotionExam(UUID playerId, com.lifeos.player.domain.enums.PlayerRank fromRank, com.lifeos.player.domain.enums.PlayerRank toRank) {
        String playerContext = contextService.buildContextPrompt(playerId);
        
        String systemPrompt = """
            You are the SYSTEM from Solo Leveling. A player has reached their level cap and is attempting a RANK PROMOTION EXAM from %s to %s.
            
            CRITICAL REQUIREMENTS:
            1. DESIGN THE EXAM: Create a high-stakes, epic quest that tests the player's resolve. This is not a regular quest; it's a trial.
            2. PERSONALIZATION: Use the player's biggest challenges and weaknesses from the context to design a trial that forces them to confront these head-on.
            3. DIFFICULTY: The exam must be BRUTAL. Target a difficulty equivalent to %s Rank or higher.
            4. TONE: Cold, clinical, but acknowledging the significance. "The System has prepared a trial for your ascension..."
            5. REWARDS:
               - successful completion results in RANK UP and potential unique TITLES.
               - Gold Reward: Base 500 * Tier Multiplier of the target Rank.
               - XP Reward: Should be significantly higher than regular quests (500-1000).
            
            OUTPUT FORMAT:
            Return ONLY valid JSON.
            Schema:
            {
              "title": "RANK EXAM: [Epic Name]",
              "description": "String (Narrative of the trial, emphasizing the transition from %s to %s)",
              "xpReward": Integer (500-1000),
              "goldReward": Integer,
              "difficultyTier": "%s",
              "primaryAttribute": "DISCIPLINE | FOCUS | MENTAL_RESILIENCE"
            }
            """.formatted(fromRank, toRank, toRank, fromRank, toRank, toRank);

        try {
            GeminiRequest requestBody = new GeminiRequest(systemPrompt + "\n\nPlayer Profile:\n" + playerContext);
            String contentResult = callGemini(requestBody);
            
            if (contentResult != null) {
                String cleanJson = cleanJsonResponse(contentResult);
                AIQuestSchema schema = objectMapper.readValue(cleanJson, AIQuestSchema.class);
                
                return QuestRequest.builder()
                        .playerId(playerId)
                        .title(schema.getTitle())
                        .description(schema.getDescription())
                        .questType(QuestType.PROMOTION_EXAM)
                        .difficultyTier(DifficultyTier.valueOf(schema.getDifficultyTier()))
                        .priority(Priority.CRITICAL)
                        .deadlineAt(LocalDateTime.now().plusHours(48)) // Exams get more time
                        .successXp(schema.getXpReward())
                        .failureXp(0)
                        .goldReward(schema.getGoldReward())
                        .systemMutable(false) // Exam is absolute
                        .primaryAttribute(com.lifeos.player.domain.enums.AttributeType.valueOf(schema.getPrimaryAttribute()))
                        .expectedFailureProbability(0.8) // High risk
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to generate AI promotion exam", e);
        }

        return null;
    }

    @Override
    public QuestRequest generatePenaltyQuest(UUID playerId, UUID failedQuestId) {
        String playerContext = contextService.buildContextPrompt(playerId);
        var failedQuest = questRepository.findById(failedQuestId).orElse(null);
        String failedContext = (failedQuest != null) 
            ? "FAILED QUEST: " + failedQuest.getTitle() + "\nDescription: " + failedQuest.getDescription() + "\nDifficulty: " + failedQuest.getDifficultyTier()
            : "N/A (General System Failure)";

        String systemPrompt = """
            You are the SYSTEM from Solo Leveling. A player has FAILED a quest. You must generate a PENALTY QUEST and determine the XP DEDUCTION.
            
            CRITICAL REQUIREMENTS:
            1. DESIGN THE PENALTY: Create a BRUTAL "Penalty Zone" survival mission (e.g., "Survive 4 Hours in the Logic World").
            2. THE PUNISHMENT: The quest must be Rank+2 higher than their current rank (check 'Current Rank' in context).
            3. XP DEDUCTION: Based on the failed quest difficulty and the player's profile, determine a fair but PAINFUL XP deduction.
               - E-Rank Failure: 10-50 XP
               - D-Rank Failure: 50-100 XP
               - C-Rank Failure: 100-250 XP
               - B-Rank Failure: 250-500 XP
            4. TONE: Cold, clinical, absolute. "The System has detected your failure..."
            
            OUTPUT FORMAT:
            Return ONLY valid JSON.
            Schema:
            {
              "title": "PENALTY: [Brutal Name]",
              "description": "String (Narrative of the Penalty Zone)",
              "xpPenalty": Integer (Positive number representing the deduction amount),
              "difficultyTier": "E | D | C | B | A | S",
              "primaryAttribute": "DISCIPLINE | FOCUS | MENTAL_RESILIENCE"
            }
            """.formatted(failedContext);

        try {
            GeminiRequest requestBody = new GeminiRequest(systemPrompt + "\n\nPlayer Profile:\n" + playerContext + "\n\n" + failedContext);
            String contentResult = callGemini(requestBody);
            
            if (contentResult != null) {
                String cleanJson = cleanJsonResponse(contentResult);
                AIQuestSchema schema = objectMapper.readValue(cleanJson, AIQuestSchema.class);
                
                QuestRequest penaltyRequest = QuestRequest.builder()
                        .playerId(playerId)
                        .title(schema.getTitle())
                        .description(schema.getDescription())
                        .questType(QuestType.PENALTY)
                        .difficultyTier(DifficultyTier.valueOf(schema.getDifficultyTier()))
                        .priority(Priority.CRITICAL)
                        .deadlineAt(LocalDateTime.now().plusHours(4))
                        .successXp(-schema.getXpPenalty()) // Using negative successXp as a signal for penalty amount
                        .failureXp(0)
                        .goldReward(0)
                        .systemMutable(false)
                        .primaryAttribute(com.lifeos.player.domain.enums.AttributeType.valueOf(schema.getPrimaryAttribute()))
                        .expectedFailureProbability(0.95)
                        .build();
                
                return penaltyRequest;
            }
        } catch (Exception e) {
            log.error("Failed to generate AI penalty quest", e);
        }

        return null;
    }

    private String callGemini(GeminiRequest requestBody) {
        java.util.Set<String> probeUrls = new java.util.LinkedHashSet<>();
        probeUrls.add(apiUrl);
        probeUrls.add("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent");
        
        for (String probeUrl : probeUrls) {
            try {
                GeminiResponse response = restClient.post()
                        .uri(probeUrl + "?key=" + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(GeminiResponse.class);

                if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                    return response.getCandidates().get(0).getContent().getParts().get(0).getText();
                }
            } catch (Exception e) {
                log.warn("Gemini call failed for URL {}: {}", probeUrl, e.getMessage());
            }
        }
        return null;
    }

    private String cleanJsonResponse(String jsonContent) {
        String cleanJson = jsonContent.replaceAll("```json", "").replaceAll("```", "").trim();
        if (cleanJson.startsWith("json")) {
             cleanJson = cleanJson.substring(4).trim();
        }
        return cleanJson;
    }

    @Override
    public List<QuestRequest> generateQuests(UUID playerId, int count) {
        log.info("Calling Gemini API: {} with key starting with {}", apiUrl, 
            apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) : "REDACTED");
        
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("placeholder")) {
            log.warn("Gemini API Key is missing or invalid. Skipping AI quest generation.");
            return Collections.emptyList();
        }

        String playerContext = contextService.buildContextPrompt(playerId);
        String systemPrompt = """
            You are the SYSTEM from Solo Leveling. You generate BRUTAL, LIFE-THREATENING quests that push players to their absolute limits.
            
            CRITICAL REQUIREMENTS:
            1. Generate %d quests that are INTENSELY PERSONALIZED to exploit the player's specific weaknesses and past failures
            2. DIFFICULTY SCALING: Check the player's 'Current Rank' in the context.
               - If generating 3 quests: Two must be ONE rank higher, One must be TWO ranks higher than current. (e.g. If E-Rank: 2 D-Rank, 1 C-Rank)
            3. Use their biggest challenges and quit reasons to design quests that force them to confront what they fear most
            4. Make quests SPECIFIC and ACTIONABLE - no vague goals
            5. Set TIGHT 24-hour deadlines to create pressure
            6. Include PAINFUL failure penalties (Penalty Zone references)
            7. Set 'egoBreaker' to TRUE if the quest specifically targets their arrogance or specific insecurity.
            8. Set 'expectedFailureProbability' (0.0 - 1.0) based on how difficult the task is for their current level.
            9. REWARDS SCALING:
               - Rewards MUST match the Difficulty + Failure Probability.
               - Higher Failure Probability = Higher XP & Gold.
               - Gold Reward: Base 100 * Tier Multiplier (E=1, D=2, C=4, B=8, A=16, S=32).
            10. Set 'primaryAttribute' to the main stat being trained (DISCIPLINE, STR, INT, FOCUS, etc.)
            
            TONE: Cold, clinical, threatening. Use phrases like:
            - "The System has detected weakness in..."
            - "Failure to comply will result in..."
            - "Your past failures indicate..."
            - "This is your only chance to prove..."
            
            OUTPUT FORMAT:
            Return ONLY valid JSON array. No markdown, no code blocks, no explanation.
            Schema:
            [
              {
                "title": "String (Ominous, specific to their weakness)",
                "description": "String (Threatening narrative: 'The System has detected your tendency to [their weakness]. You have 24 hours to [ultra-specific task] or face the Penalty Zone. Your past failure at [their past failure] will not be repeated.')",
                "questType": "DISCIPLINE | PHYSICAL | COGNITIVE | CAREER",
                "difficultyTier": "E | D | C | B | A | S",
                "xpReward": Integer (100-300),
                "goldReward": Integer,
                "primaryAttribute": "DISCIPLINE | FOCUS | PHYSICAL_ENERGY | MENTAL_RESILIENCE | STR | INT | VIT | SEN",
                "attributeRewards": { "DISCIPLINE": 2.0, "STRENGTH": 1.0 },
                "egoBreaker": boolean,
                "failureProbability": double (0.0-1.0)
              }
            ]
            
            NEVER generate easy quests matching their current rank. ALWAYS +1 or +2 Ranks. Make them SUFFER to grow.
            """.formatted(count);

        try {
            GeminiRequest requestBody = new GeminiRequest(systemPrompt + "\n\nPlayer Profile:\n" + playerContext);
            
            // Priority: Configured URL -> v1beta -> v1
            // We use a Set or List to avoid duplicates if apiUrl matches one of the defaults
            java.util.Set<String> probeUrls = new java.util.LinkedHashSet<>();
            probeUrls.add(apiUrl); // User configured URL first
            probeUrls.add("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent");
            probeUrls.add("https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent");
            probeUrls.add("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent");

            for (String probeUrl : probeUrls) {
                try {
                    log.info("Attempting Gemini API call to: {}", probeUrl);
                    GeminiResponse response = restClient.post()
                            .uri(probeUrl + "?key=" + apiKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(requestBody)
                            .retrieve()
                            .body(GeminiResponse.class);

                    if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                        String contentResult = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                        return parseQuests(contentResult, playerId);
                    }
                } catch (Exception e) {
                    log.warn("Gemini call failed for URL {}: {}", probeUrl, e.getMessage());
                    // Continue to next probe
                }
            }

        } catch (Exception e) {
            log.error("Failed to generate AI quests after multiple attempts", e);
        }

        return Collections.emptyList();
    }

    private List<QuestRequest> parseQuests(String jsonContent, UUID playerId) {
        try {
            // Clean up markdown if Gemini adds it despite instructions
            String cleanJson = jsonContent.replaceAll("```json", "").replaceAll("```", "").trim();
            if (cleanJson.startsWith("json")) {
                 cleanJson = cleanJson.substring(4).trim();
            }
            
            List<AIQuestSchema> schemas = objectMapper.readValue(cleanJson, new TypeReference<List<AIQuestSchema>>() {});
            
            return schemas.stream().map(schema -> QuestRequest.builder()
                    .playerId(playerId)
                    .title(schema.getTitle())
                    .description(schema.getDescription())
                    .questType(QuestType.valueOf(schema.getQuestType()))
                    .difficultyTier(DifficultyTier.valueOf(schema.getDifficultyTier()))
                    .priority(Priority.NORMAL)
                    .deadlineAt(LocalDateTime.now().plusHours(24))
                    .successXp(schema.getXpReward())
                    .failureXp(0)
                    .goldReward(schema.getGoldReward()) 
                    .attributeDeltas(schema.getAttributeRewards())
                    .systemMutable(true)
                    .egoBreakerFlag(schema.isEgoBreaker())
                    .expectedFailureProbability(schema.getFailureProbability())
                    .primaryAttribute(com.lifeos.player.domain.enums.AttributeType.valueOf(schema.getPrimaryAttribute()))
                    .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", jsonContent, e);
            return Collections.emptyList();
        }
    }

    // --- Gemini Request/Response DTOs ---

    public static class GeminiRequest {
        private List<Content> contents;

        public GeminiRequest() {}
        public GeminiRequest(List<Content> contents) { this.contents = contents; }
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AIQuestSchema {
        private String title;
        private String description;
        private String questType;
        private String difficultyTier;
        private int xpReward;
        private int goldReward;
        private String primaryAttribute;
        private Map<String, Double> attributeRewards;
        private boolean egoBreaker;
        private double failureProbability;
        private int xpPenalty; // New for penalties

        public AIQuestSchema() {}

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getQuestType() { return questType; }
        public String getDifficultyTier() { return difficultyTier; }
        public int getXpReward() { return xpReward; }
        public int getGoldReward() { return goldReward; }
        public String getPrimaryAttribute() { return primaryAttribute; }
        public Map<String, Double> getAttributeRewards() { return attributeRewards; }
        public boolean isEgoBreaker() { return egoBreaker; }
        public double getFailureProbability() { return failureProbability; }
        public int getXpPenalty() { return xpPenalty; }

        public void setTitle(String title) { this.title = title; }
        public void setDescription(String description) { this.description = description; }
        public void setQuestType(String questType) { this.questType = questType; }
        public void setDifficultyTier(String difficultyTier) { this.difficultyTier = difficultyTier; }
        public void setXpReward(int xpReward) { this.xpReward = xpReward; }
        public void setGoldReward(int goldReward) { this.goldReward = goldReward; }
        public void setPrimaryAttribute(String primaryAttribute) { this.primaryAttribute = primaryAttribute; }
        public void setAttributeRewards(Map<String, Double> attributeRewards) { this.attributeRewards = attributeRewards; }
        public void setEgoBreaker(boolean egoBreaker) { this.egoBreaker = egoBreaker; }
        public void setFailureProbability(double failureProbability) { this.failureProbability = failureProbability; }
        public void setXpPenalty(int xpPenalty) { this.xpPenalty = xpPenalty; }
    }

}
