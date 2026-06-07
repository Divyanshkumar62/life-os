package com.lifeos.onboarding.dto;

import java.time.LocalTime;

public class QuestionnaireRequest {
    
    // Inferred/Calculated fields
    private String archetype; // "BRAINS", "BRAWN", "BALANCE"
    private LocalTime wakeUpTime; // e.g. "06:00"

    // 5 Narrative Questionnaire answers
    private String biggestChallenge; // The Beast
    private String pastFailures;     // The Graveyard
    private String focusArea;        // The Weapon
    private String sixMonthGoal;     // The Throne
    private String availableTime;    // The Hourglass
    
    public QuestionnaireRequest() {}

    public QuestionnaireRequest(String archetype, LocalTime wakeUpTime, String biggestChallenge, 
                                String pastFailures, String focusArea, String sixMonthGoal, 
                                String availableTime) {
        this.archetype = archetype;
        this.wakeUpTime = wakeUpTime;
        this.biggestChallenge = biggestChallenge;
        this.pastFailures = pastFailures;
        this.focusArea = focusArea;
        this.sixMonthGoal = sixMonthGoal;
        this.availableTime = availableTime;
    }

    // Secondary constructor for easy instantiation of narrative fields
    public QuestionnaireRequest(String biggestChallenge, String pastFailures, String focusArea, 
                                String sixMonthGoal, String availableTime) {
        this.biggestChallenge = biggestChallenge;
        this.pastFailures = pastFailures;
        this.focusArea = focusArea;
        this.sixMonthGoal = sixMonthGoal;
        this.availableTime = availableTime;
    }

    public String getArchetype() { return archetype; }
    public LocalTime getWakeUpTime() { return wakeUpTime; }
    public String getBiggestChallenge() { return biggestChallenge; }
    public String getPastFailures() { return pastFailures; }
    public String getFocusArea() { return focusArea; }
    public String getSixMonthGoal() { return sixMonthGoal; }
    public String getAvailableTime() { return availableTime; }

    public void setArchetype(String archetype) { this.archetype = archetype; }
    public void setWakeUpTime(LocalTime wakeUpTime) { this.wakeUpTime = wakeUpTime; }
    public void setBiggestChallenge(String biggestChallenge) { this.biggestChallenge = biggestChallenge; }
    public void setPastFailures(String pastFailures) { this.pastFailures = pastFailures; }
    public void setFocusArea(String focusArea) { this.focusArea = focusArea; }
    public void setSixMonthGoal(String sixMonthGoal) { this.sixMonthGoal = sixMonthGoal; }
    public void setAvailableTime(String availableTime) { this.availableTime = availableTime; }
}
