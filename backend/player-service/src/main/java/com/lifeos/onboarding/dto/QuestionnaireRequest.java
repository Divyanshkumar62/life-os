package com.lifeos.onboarding.dto;

import java.time.LocalTime;
import java.util.UUID;

public class QuestionnaireRequest {
    
    // 7 Awakening Questions
    
    // 1. Archetype - "What describes you best?"
    private String archetype; // "BRAINS", "BRAWN", "BALANCE"
    
    // 2. Primary Weakness - "What's your biggest struggle?"
    private String primaryWeakness; // e.g. "Procrastination", "Lack of focus"
    
    // 3. Main Goal - "What do you want to achieve in 6 months?"
    private String mainGoal; // e.g. "Build muscle", "Learn coding"
    
    // 4. Wake Up Time - "What time do you usually wake up?"
    private LocalTime wakeUpTime; // e.g. "06:30"
    
    // 5. Biggest Challenge - "What's currently holding you back?"
    private String biggestChallenge; // e.g. "Time management", "Energy"
    
    // 6. Available Time - "How many hours per day can you commit?"
    private String availableTime; // e.g. "2-4 hours", "1 hour"
    
    // 7. Focus Area - "What do you want to improve most?"
    private String focusArea; // e.g. "Physical", "Mental", "Career"
    
    public QuestionnaireRequest() {}

    public QuestionnaireRequest(String archetype, String primaryWeakness, String mainGoal, 
                                LocalTime wakeUpTime, String biggestChallenge, 
                                String availableTime, String focusArea) {
        this.archetype = archetype;
        this.primaryWeakness = primaryWeakness;
        this.mainGoal = mainGoal;
        this.wakeUpTime = wakeUpTime;
        this.biggestChallenge = biggestChallenge;
        this.availableTime = availableTime;
        this.focusArea = focusArea;
    }

    public String getArchetype() { return archetype; }
    public String getPrimaryWeakness() { return primaryWeakness; }
    public String getMainGoal() { return mainGoal; }
    public LocalTime getWakeUpTime() { return wakeUpTime; }
    public String getBiggestChallenge() { return biggestChallenge; }
    public String getAvailableTime() { return availableTime; }
    public String getFocusArea() { return focusArea; }

    public void setArchetype(String archetype) { this.archetype = archetype; }
    public void setPrimaryWeakness(String primaryWeakness) { this.primaryWeakness = primaryWeakness; }
    public void setMainGoal(String mainGoal) { this.mainGoal = mainGoal; }
    public void setWakeUpTime(LocalTime wakeUpTime) { this.wakeUpTime = wakeUpTime; }
    public void setBiggestChallenge(String biggestChallenge) { this.biggestChallenge = biggestChallenge; }
    public void setAvailableTime(String availableTime) { this.availableTime = availableTime; }
    public void setFocusArea(String focusArea) { this.focusArea = focusArea; }
}
