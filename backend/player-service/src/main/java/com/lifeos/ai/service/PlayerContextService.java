package com.lifeos.ai.service;

import com.lifeos.onboarding.domain.PlayerProfile;
import com.lifeos.onboarding.repository.PlayerProfileRepository;
import com.lifeos.player.domain.PlayerProgression;
import com.lifeos.player.repository.PlayerProgressionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PlayerContextService {

    private final PlayerProfileRepository profileRepository;
    private final PlayerProgressionRepository progressionRepository;
    
    public PlayerContextService(PlayerProfileRepository profileRepository, PlayerProgressionRepository progressionRepository) {
        this.profileRepository = profileRepository;
        this.progressionRepository = progressionRepository;
    }
    
    @Transactional(readOnly = true)
    public String buildContextPrompt(UUID playerId) {
        log.debug("Building context prompt for player: {}", playerId);
        
        PlayerProfile profile = profileRepository.findById(playerId).orElse(null);
        
        if (profile == null) {
            log.warn("No profile found for player: {}, using minimal context", playerId);
            return "PLAYER CONTEXT:\nNew player - no history available.\nFocus: Getting started";
        }
        
        PlayerProgression progression = progressionRepository.findByPlayerPlayerId(playerId).orElse(null);
        String currentRank = (progression != null && progression.getRank() != null) ? progression.getRank().name() : "E";
        int currentLevel = (progression != null) ? progression.getLevel() : 1;

        StringBuilder sb = new StringBuilder();
        sb.append("PLAYER CONTEXT:\n");
        sb.append("Current Rank: ").append(currentRank).append(" (Level ").append(currentLevel).append(")\n");
        sb.append("Age Range: ").append(safeGet(profile.getAgeRange())).append("\n");
        sb.append("Primary Role: ").append(safeGet(profile.getPrimaryRole())).append("\n");
        sb.append("Work Schedule: ").append(safeGet(profile.getWorkSchedule())).append("\n");
        sb.append("Living Situation: ").append(safeGet(profile.getLivingSituation())).append("\n");
        sb.append("\n");
        
        // Safe handling of lists
        List<String> focusAreas = profile.getFocusAreas();
        sb.append("FOCUS AREAS: ").append(focusAreas != null ? String.join(", ", focusAreas) : "General").append("\n");
        
        sb.append("6-MONTH GOAL: ").append(safeGet(profile.getSixMonthGoal())).append("\n");
        sb.append("\n");
        sb.append("WEAKNESSES TO EXPLOIT:\n");
        
        List<String> weaknesses = profile.getWeaknesses();
        if (weaknesses != null && !weaknesses.isEmpty()) {
            for (String weakness : weaknesses) {
                sb.append("  - ").append(weakness).append("\n");
            }
        } else {
            // Use biggest challenge as weakness for new players
            String challenge = profile.getBiggestChallenge();
            if (challenge != null) {
                sb.append("  - ").append(challenge).append("\n");
            } else {
                sb.append("  - Unknown (new player)\n");
            }
        }
        
        sb.append("\n");
        sb.append("PAST FAILURES: ").append(safeGet(profile.getPastFailures())).append("\n");
        sb.append("WHY THEY QUIT BEFORE: ").append(safeGet(profile.getQuitReasons())).append("\n");
        sb.append("BIGGEST CHALLENGE: ").append(safeGet(profile.getBiggestChallenge())).append("\n");
        sb.append("AVAILABLE TIME: ").append(safeGet(profile.getAvailableTime())).append("\n");
        
        List<String> preferredTypes = profile.getPreferredQuestTypes();
        sb.append("PREFERRED QUEST TYPES: ").append(preferredTypes != null ? String.join(", ", preferredTypes) : "Any").append("\n");
        sb.append("DIFFICULTY PREFERENCE: ").append(safeGet(profile.getDifficultyPreference())).append("\n");
        
        if (profile.getQuestionnaireData() != null && !profile.getQuestionnaireData().isEmpty()) {
            sb.append("\nADDITIONAL AWAKENING DATA (Psychological Profile):\n");
            profile.getQuestionnaireData().forEach((key, value) -> {
                sb.append("  - ").append(key).append(": ").append(value).append("\n");
            });
        }
        
        log.debug("Context prompt built successfully for player: {}", playerId);
        return sb.toString();
    }
    
    private String safeGet(String value) {
        return value != null ? value : "Not specified";
    }
}
