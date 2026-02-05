package com.lifeos.ai.service;

import com.lifeos.onboarding.domain.PlayerProfile;
import com.lifeos.onboarding.repository.PlayerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerContextService {

    private final PlayerProfileRepository profileRepository;
    
    @Transactional(readOnly = true)
    public String buildContextPrompt(UUID playerId) {
        PlayerProfile profile = profileRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        
        StringBuilder sb = new StringBuilder();
        sb.append("Player Profile:\n");
        sb.append("- Role: ").append(profile.getPrimaryRole()).append("\n");
        sb.append("- Focus Areas: ").append(String.join(", ", profile.getFocusAreas())).append("\n");
        sb.append("- Weaknesses: ").append(String.join(", ", profile.getWeaknesses())).append("\n");
        sb.append("- Goals: ").append(profile.getSixMonthGoal()).append("\n");
        
        // Add stats, recent quests, etc. later
        
        return sb.toString();
    }
}
