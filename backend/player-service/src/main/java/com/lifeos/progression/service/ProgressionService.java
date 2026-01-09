package com.lifeos.progression.service;

import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.domain.enums.QuestType;
import com.lifeos.quest.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgressionService {

    private final PlayerStateService playerStateService;
    private final PenaltyService penaltyService;
    private final QuestRepository questRepository;
    // In V2, inject QGI Service. For V1, we simulate QGI creation here or call a Factory.
    // Assuming for V1 we create the entity directly? Or usage of QuestLifecycleService?
    // Plan: "Request creation... from QGI (Mocked/Wrapped)".


    /**
     * Checks if player is at Rank Gate (Level == Cap).
     */
    public boolean checkRankGate(UUID playerId) {
        var state = playerStateService.getPlayerState(playerId);
        int currentLevel = state.getProgression().getLevel();
        int cap = state.getProgression().getRank().getLevelCap();
        return currentLevel >= cap;
    }

    /**
     * Validates if a player is eligible to request a Promotion Quest.
     * Criteria:
     * 1. At Rank Gate (Level >= Cap)
     * 2. Has sufficient Boss Keys
     * 3. Meets minimum Stat Requirements from Template
     * 4. NOT in Penalty Zone (Status Flag check)
     */
    public boolean canRequestPromotion(UUID playerId) {
        if (!checkRankGate(playerId)) return false;

        var state = playerStateService.getPlayerState(playerId);
        var currentRank = state.getProgression().getRank();
        var template = com.lifeos.progression.domain.RankTransitionTemplate.from(currentRank);
        
        if (template == null) return false; // S Rank or maxed

        // Check Keys
        if (state.getProgression().getBossKeys() < template.getBossKeyCost()) {
            return false;
        }

        // Check Stats
        // var effectiveStats = playerStateService.getEffectiveAttributes(playerId); 
        // Note: getEffectiveAttributes returns Map<AttributeType, Double>? 
        // Need to ensure playerStateService exposes this or we look at `state.getAttributes()`
        // Assuming state.getAttributes() is available in response.
        
        for (var entry : template.getStatRequirements().entrySet()) {
            var type = entry.getKey();
            var requiredVal = entry.getValue();
            
            // Find attribute in list
            var attr = state.getAttributes().stream()
                    .filter(a -> a.getAttributeType() == type)
                    .findFirst();
            
            if (attr.isEmpty() || attr.get().getCurrentValue() < requiredVal) {
                return false;
            }
        }
        
        // Check Penalty Zone
        boolean inPenaltyZone = state.getActiveFlags().stream()
                .anyMatch(f -> f.getFlag() == StatusFlagType.PENALTY_ZONE);
                
        if (inPenaltyZone) return false;

        return true;
    }

    /**
     * Requests a Promotion Quest.
     * Consumes Boss Keys immediately.
     */
    @Transactional
    public void requestPromotionQuest(UUID playerId) {
         if (!canRequestPromotion(playerId)) {
            throw new IllegalStateException("Player is not eligible for promotion.");
        }
        
        var state = playerStateService.getPlayerState(playerId);
        var template = com.lifeos.progression.domain.RankTransitionTemplate.from(state.getProgression().getRank());
        
        // Consume Keys
        // Need method in PlayerStateService to deduct keys OR use repo directly here?
        // Ideally PlayerStateService.
        // Assuming we add `consumeBossKeys` to PlayerStateService.
        playerStateService.consumeBossKeys(playerId, template.getBossKeyCost());
        
        // Create Quest (Mocked/Simulated)
        // TODO: Call QGI.createQuest(...) with DIFFICULTY = FIXED_BY_RANK
        System.out.println("Promotion Quest Requested for Player " + playerId + ". Keys Consumed: " + template.getBossKeyCost());
    }

    /**
     * Processes the outcome of a Promotion Quest.
     */
    @Transactional
    public void processPromotionOutcome(UUID playerId, boolean success) {
        if (success) {
            // Success: Promote Rank (Unfreezes XP internally in promoteRank)
            playerStateService.promoteRank(playerId);
            System.out.println("Rank Advanced for player " + playerId);
        } else {
            // Failure: Penalty Zone, XP stays frozen.
            // Keys are NOT refunded.
            
            // 1. Set Status Flag: PENALTY_ZONE
            // TODO: Add addStatusFlag to PlayerStateService
            // playerStateService.addStatusFlag(playerId, StatusFlagType.PENALTY_ZONE);
            System.out.println("Promotion Failed. Player enters PENALTY_ZONE.");
            
            // 2. Ensure XP Frozen (Redundant check, but good for safety)
            // playerStateService.setXpFrozen(playerId, true);
        }
    }
}
