package com.lifeos.progression.service;

import com.lifeos.player.domain.PlayerIdentity;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JobChangeArchitect {

    private static final Logger log = LoggerFactory.getLogger(JobChangeArchitect.class);
    private final GeminiAIService geminiAIService;

    public static class DailyGauntletQuest {
        public int day;
        public String title;
        public String description;
        public int estimatedMinutes;
        public String difficulty;
        public String type; // PHYSICAL, COGNITIVE, or DAILY_HABIT
        public Map<String, String> metadata; // e.g., biggest_challenge reference

        public DailyGauntletQuest(int day, String title, String description, int mins, String difficulty, String type) {
            this.day = day;
            this.title = title;
            this.description = description;
            this.estimatedMinutes = mins;
            this.difficulty = difficulty;
            this.type = type;
            this.metadata = new HashMap<>();
        }
    }

    public List<DailyGauntletQuest> generateThreeDayGauntlet(PlayerIdentity player) {
        List<DailyGauntletQuest> gauntlet = new ArrayList<>();

        log.info("Generating 3-Day Job Change Gauntlet for player: {}", player.getPlayerId());

        // Day 1: The Endless Swarm (Volume Test)
        gauntlet.addAll(generateDay1Quests());

        // Day 2: The Royal Guards (Intensity Test)
        gauntlet.addAll(generateDay2Quests(player));

        // Day 3: The Blood-Red Commander (Boss Room)
        gauntlet.addAll(generateDay3Quests(player));

        return gauntlet;
    }

    private List<DailyGauntletQuest> generateDay1Quests() {
        List<DailyGauntletQuest> day1 = new ArrayList<>();

        String[] day1Tasks = {
                "5-minute focus block on top priority",
                "Quick email triage (sort inbox)",
                "Morning stretch routine",
                "Hydrate and take vitamins",
                "Plan today's 3 most important tasks",
                "10-minute walk or movement break",
                "Review progress from yesterday",
                "Clear one notification backlog"
        };

        // Generate 6-8 random low-difficulty tasks
        Random rand = new Random();
        int numTasks = 6 + rand.nextInt(3); // 6-8
        Set<Integer> used = new HashSet<>();

        for (int i = 0; i < numTasks && used.size() < day1Tasks.length; i++) {
            int idx = rand.nextInt(day1Tasks.length);
            if (used.add(idx)) {
                DailyGauntletQuest q = new DailyGauntletQuest(1, day1Tasks[idx], "The Endless Swarm: Complete this task", 5, "LOW", "PHYSICAL");
                q.metadata.put("volume_test", "true");
                day1.add(q);
            }
        }

        log.info("Generated {} Day 1 quests", day1.size());
        return day1;
    }

    private List<DailyGauntletQuest> generateDay2Quests(PlayerIdentity player) {
        List<DailyGauntletQuest> day2 = new ArrayList<>();

        try {
            // Use Gemini to generate 3 high-difficulty tasks based on biggest_challenge
            String challenge = player.getBiggestChallenge() != null ? player.getBiggestChallenge() : "improve productivity";
            String goal = player.getSixMonthGoal() != null ? player.getSixMonthGoal() : "achieve personal goals";

            String prompt = String.format(
                    "Generate 3 distinct, actionable, 90-minute deep-work tasks that directly confront this challenge: '%s'. " +
                    "Context: Six-month goal is '%s'. " +
                    "Format each as: TITLE|DESCRIPTION. Separate by newline.",
                    challenge, goal
            );

            List<String> aiTasks = geminiAIService.generateTasks(prompt);

            for (int i = 0; i < Math.min(3, aiTasks.size()); i++) {
                String taskLine = aiTasks.get(i);
                String[] parts = taskLine.split("\\|");
                String title = parts.length > 0 ? parts[0].trim() : "Royal Guard Challenge " + (i + 1);
                String desc = parts.length > 1 ? parts[1].trim() : "Confront your biggest challenge";

                DailyGauntletQuest q = new DailyGauntletQuest(2, title, "The Royal Guards: " + desc, 90, "HIGH", "COGNITIVE");
                q.metadata.put("intensity_test", "true");
                day2.add(q);
            }
        } catch (Exception e) {
            log.error("Gemini API failed, using fallback tasks: {}", e.getMessage());
            // Fallback 3 tasks
            day2.add(new DailyGauntletQuest(2, "Deep Work: Core Challenge", "The Royal Guards: Tackle your biggest challenge head-on", 90, "HIGH", "COGNITIVE"));
            day2.add(new DailyGauntletQuest(2, "Strategic Planning Session", "The Royal Guards: Plan your next 30-day sprint", 90, "HIGH", "COGNITIVE"));
            day2.add(new DailyGauntletQuest(2, "Major Project Milestone", "The Royal Guards: Complete a significant project deliverable", 90, "HIGH", "COGNITIVE"));
        }

        log.info("Generated {} Day 2 quests", day2.size());
        return day2;
    }

    private List<DailyGauntletQuest> generateDay3Quests(PlayerIdentity player) {
        List<DailyGauntletQuest> day3 = new ArrayList<>();

        // 1 Boss task (player's biggest challenge)
        String bossTitle = "The Blood-Red Commander: Final Trial";
        String bossDesc = player.getBiggestChallenge() != null
                ? "Conquer: " + player.getBiggestChallenge()
                : "Overcome your greatest fear";

        DailyGauntletQuest bossTa = new DailyGauntletQuest(3, bossTitle, bossDesc, 120, "BOSS", "COGNITIVE");
        bossTa.metadata.put("boss_room", "true");
        day3.add(bossTa);

        // + 3 standard daily habits
        String[] dailyHabits = {
                "Morning Routine",
                "Evening Reflection",
                "Physical Activity"
        };

        for (String habit : dailyHabits) {
            DailyGauntletQuest q = new DailyGauntletQuest(3, habit, "Maintain your discipline", 20, "MEDIUM", "DAILY_HABIT");
            q.metadata.put("daily_habit", "true");
            day3.add(q);
        }

        log.info("Generated {} Day 3 quests", day3.size());
        return day3;
    }
}
