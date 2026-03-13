package com.lifeos.progression.service;

import com.lifeos.player.domain.PlayerIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JobClassCalculator {

    private static final Logger log = LoggerFactory.getLogger(JobClassCalculator.class);

    public static class JobClassResult {
        public String archetype;
        public String jobTitle;
        public String classMultiplierJson;

        public JobClassResult(String archetype, String jobTitle, String classMultiplierJson) {
            this.archetype = archetype;
            this.jobTitle = jobTitle;
            this.classMultiplierJson = classMultiplierJson;
        }
    }

    /**
     * Determines job class based on current attributes.
     * Uses real-time stats from the player_attribute table.
     */
    public JobClassResult calculateJobClass(
            int strength, int intelligence, int vitality, int sensibility,
            double physicalQuestRatio, boolean perfectClearance) {

        int physicalScore = strength + vitality;
        int cognitiveScore = intelligence + sensibility;
        int statDiff = Math.abs(physicalScore - cognitiveScore);
        int totalScore = physicalScore + cognitiveScore;
        double diffPercentage = (totalScore > 0) ? (double) statDiff / totalScore : 0;

        // Archetype A: The Vanguard (Combat/Physical Focus)
        if (physicalScore > cognitiveScore && diffPercentage >= 0.20) {
            return new JobClassResult(
                    "VANGUARD",
                    strength > vitality ? "Silver Knight" : "Berserker",
                    buildMultiplierJson(1.15, 1.0, 1.0)
            );
        }

        // Archetype B: The Scholar (Cognitive/Focus Focus)
        if (cognitiveScore > physicalScore && diffPercentage >= 0.20) {
            return new JobClassResult(
                    "SCHOLAR",
                    intelligence > sensibility ? "Grand Architect" : "Arcane Mage",
                    buildMultiplierJson(1.0, 1.15, 1.0)
            );
        }

        // Archetype C: The Shadow (Balanced/High Performance)
        if (diffPercentage <= 0.10 && perfectClearance) {
            return new JobClassResult(
                    "SHADOW",
                    "Shadow Necromancer",
                    buildMultiplierJson(1.05, 1.05, 1.0)
            );
        }

        // Default fallback: Balanced Shadow (without perfect clearance)
        return new JobClassResult(
                "SHADOW",
                "Shadow Necromancer",
                buildMultiplierJson(1.05, 1.05, 1.0)
        );
    }

    private String buildMultiplierJson(double physicalXp, double cognitiveXp, double goldYield) {
        Object obj = new Object() {
            public double PHYSICAL_XP = physicalXp;
            public double COGNITIVE_XP = cognitiveXp;
            public double GOLD_YIELD = goldYield;
        };
        // Manual JSON construction to avoid external dependencies
        return String.format("{\"PHYSICAL_XP\":%.2f,\"COGNITIVE_XP\":%.2f,\"GOLD_YIELD\":%.2f}",
                physicalXp, cognitiveXp, goldYield);
    }
}
