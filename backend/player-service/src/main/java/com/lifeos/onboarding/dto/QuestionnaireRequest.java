package com.lifeos.onboarding.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class QuestionnaireRequest {
    // Personal Context
    private String ageRange;
    private String primaryRole;
    private String workSchedule;
    private String livingSituation;

    // Goals & Focus Areas
    private List<String> focusAreas;
    private String sixMonthGoal;
    private String biggestChallenge;

    // Weaknesses
    private List<String> weaknesses;
    private String pastFailures;
    private String quitReasons;

    // Preferences
    private String availableTime;
    private List<String> preferredQuestTypes;
    private String difficultyPreference;
}
