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
     * Requests a Promotion Quest.
     * In a real system, this sends a signal to QGI.
     * Here, we'll assume QGI listens/polls or we just return "true" that it was requested,
     * OR we check if one already exists.
     * For V1 integration, we might need to actually CREATE the quest entity so the user can see it.
     */
    @Transactional
    public void requestPromotionQuest(UUID playerId) {
        if (!checkRankGate(playerId)) {
            throw new IllegalStateException("Player is not at Rank Gate.");
        }
        
        // TODO: Call QGI.createQuest(...)
        // Since QGI isn't fully separate yet, we assume a Quest is generated.
        // We'll log it for now as "Event Emitted".
        System.out.println("Promotion Quest Requested for Player " + playerId);
        
        // For testing/verification without QGI:
        // logic should stop here. The 'request' is an event.
    }

    /**
     * Processes the outcome of a Promotion Quest.
     */
    @Transactional
    public void processPromotionOutcome(UUID playerId, boolean success) {
        if (success) {
            // Success: Promote Rank
            playerStateService.promoteRank(playerId);
            // Event: RankAdvanced
            System.out.println("Rank Advanced for player " + playerId);
        } else {
            // Failure: Penalty Zone, XP stays frozen.
            // 1. Set Status Flag: PENALTY_ZONE
            // Use PlayerStateService to add flag? We don't have addFlag method exposed yet.
            // We'll skip adding the Flag record strictly for now if method missing, 
            // OR assumes PenaltyService handles "effects".
            // But PENALTY_ZONE is a specific player state.
            // Let's assume we need to add `addStatusFlag` to PlayerStateService later or now.
            // For now, let's trigger a standard Penalty first.
            
            // 2. Apply Penalty (Severity HIGH/CRITICAL)
            // PenaltyService is usually triggered by QuestLifecycle automatically on Fail.
            // But Promotion Failure is distinct.
            // If QuestLifecycle calls this method, it ALSO calls PenaltyService?
            // "In failQuest(): If type == PROMOTION -> progressionService.processOutcome".
            // QuestLifecycle ALSO calls `penaltyService.applyPenalty`.
            // So we don't need to call penaltyService here manually if QuestLifecycle does it.
            // Wait, does QuestLifecycle know to use HIGH severity? 
            // PenaltyCalculationService determines severity. We should ensure it maps PROMOTION to HIGH.
            
            // 3. Mark Penalty Zone (if not covered by PenaltyService)
            // System.out.println("Enters Penalty Zone.");
            // We should ideally persist this.
        }
    }
}
