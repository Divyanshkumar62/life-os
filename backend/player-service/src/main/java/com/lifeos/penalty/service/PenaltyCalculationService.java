package com.lifeos.penalty.service;

import com.lifeos.penalty.domain.PenaltyDefinition;
import com.lifeos.penalty.domain.enums.FailureReason;
import com.lifeos.penalty.domain.enums.PenaltySeverity;
import com.lifeos.penalty.domain.enums.PenaltyType;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.domain.enums.QuestType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PenaltyCalculationService {

    private final Random random = new Random();

    /**
     * Calculate the penalty for a failed quest without player attribute context.
     * Uses default 25% XP deduction with no AGI dodge.
     */
    public PenaltyDefinition calculatePenalty(Quest quest, FailureReason reason, long currentXp) {
        return calculatePenalty(quest, reason, currentXp, null);
    }

    /**
     * Calculate the penalty for a failed quest.
     * AGI stat grants a (AGI * 0.5)% chance (capped at 30%) to completely dodge
     * the XP and Gold drain portions of the penalty.
     *
     * @param playerState optional — if null, AGI dodge is skipped
     */
    public PenaltyDefinition calculatePenalty(Quest quest, FailureReason reason, long currentXp,
                                               PlayerStateResponse playerState) {
        List<String> systemMessages = new ArrayList<>();

        // --- AGI DODGE CHECK ---
        boolean dodgePenaltyDrain = false;
        if (playerState != null) {
            int agiValue = getAttributeValue(playerState, AttributeType.AGI);
            if (agiValue > 0) {
                double dodgeChance = Math.min(agiValue * 0.005, 0.30); // cap at 30%
                if (random.nextDouble() < dodgeChance) {
                    dodgePenaltyDrain = true;
                    systemMessages.add(String.format(
                        "[SYSTEM] High AGI (%d) successfully evaded Penalty Drain. XP and Gold drain nullified.",
                        agiValue
                    ));
                }
            }
        }

        // 1. Determine Severity based on FailureReason and Quest Priority/Diff
        PenaltySeverity severity = determineSeverity(quest, reason);

        // Deduct 25% of the player's current XP (nullified if AGI dodge succeeded)
        long xpDeduction = dodgePenaltyDrain ? 0L : (long) (currentXp * 0.25);

        // 2. Build Definition based on Severity
        var builder = PenaltyDefinition.builder()
                .severity(severity)
                .xpDeduction(xpDeduction)
                .systemMessages(systemMessages);

        switch (severity) {
            case LOW -> {
                builder.type(PenaltyType.XP_DEDUCTION);
                // Just XP
            }
            case MEDIUM -> {
                builder.type(PenaltyType.STAT_DEBUFF); // Dominant type
                // XP + Mild Debuff (e.g., -5% for 6 hours)
                builder.debuffAttribute(pickRandomAttribute());
                builder.debuffAmount(5.0);
                builder.debuffExpiresAt(LocalDateTime.now().plusHours(6));
            }
            case HIGH, CRITICAL -> {
                builder.type(PenaltyType.STREAK_RESET); // Dominant type
                // XP + Heavy Debuff (e.g., -10% for 12 hours) + Streak Reset
                builder.debuffAttribute(pickRandomAttribute());
                builder.debuffAmount(10.0);
                builder.debuffExpiresAt(LocalDateTime.now().plusHours(12));
                builder.resetStreak(true);
            }
        }

        return builder.build();
    }

    private PenaltySeverity determineSeverity(Quest quest, FailureReason reason) {
        if (reason == FailureReason.ABANDONED) return PenaltySeverity.LOW;

        // Critical failures
        if (quest.getDifficultyTier() == DifficultyTier.RED || quest.getDifficultyTier() == DifficultyTier.S) {
            return PenaltySeverity.CRITICAL;
        }

        // Standard scaling
        if (quest.getDifficultyTier().ordinal() >= DifficultyTier.B.ordinal()) {
            return PenaltySeverity.HIGH;
        }

        return PenaltySeverity.MEDIUM;
    }

    private long calculateBaseXpPenalty(DifficultyTier tier) {
        return switch (tier) {
            case E -> 5;
            case D -> 10;
            case C -> 15;
            case B -> 25;
            case A -> 40;
            case S -> 60;
            case RED -> 100;
            case TIER_1, TIER_2, TIER_3, TIER_4, TIER_5, TIER_6 -> 20;
        };
    }

    private AttributeType pickRandomAttribute() {
        // V1: DISCIPLINE for willpower failure
        return AttributeType.DISCIPLINE;
    }

    /**
     * Safely retrieves an integer attribute value from player state.
     */
    private int getAttributeValue(PlayerStateResponse state, AttributeType type) {
        if (state.getAttributes() == null) return 0;
        return state.getAttributes().stream()
                .filter(a -> type == a.getAttributeType())
                .mapToInt(a -> (int) a.getCurrentValue())
                .findFirst()
                .orElse(0);
    }
}
