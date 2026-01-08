package com.lifeos.penalty.service;

import com.lifeos.penalty.domain.PenaltyDefinition;
import com.lifeos.penalty.domain.enums.FailureReason;
import com.lifeos.penalty.domain.enums.PenaltySeverity;
import com.lifeos.penalty.domain.enums.PenaltyType;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.domain.enums.QuestType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PenaltyCalculationService {

    public PenaltyDefinition calculatePenalty(Quest quest, FailureReason reason) {
        // 1. Determine Severity based on FailureReason and Quest Priority/Diff
        PenaltySeverity severity = determineSeverity(quest, reason);

        // 2. Base XP Penalty
        long xpPenalty = calculateBaseXpPenalty(quest.getDifficultyTier());

        // 3. Build Definition based on Severity
        var builder = PenaltyDefinition.builder()
                .severity(severity)
                .xpDeduction(xpPenalty); // Initial base

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
        };
    }

    private AttributeType pickRandomAttribute() {
        // V1: Simple deterministic pick or random. 
        // For distinctness, let's pick based on time to be deterministic without seed.
        // Or just hardcode DISCIPLINE for v1 as it represents "willpower failure".
        return AttributeType.DISCIPLINE; 
    }
}
