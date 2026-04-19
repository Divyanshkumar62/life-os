package com.lifeos.onboarding.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "player_profiles")
public class PlayerProfile {
    
    // New Fields for Awakening
    private String archetype; // BRAINS, BRAWN, BALANCE
    private java.time.LocalTime wakeUpTime;
    
    public PlayerProfile() {}

    public PlayerProfile(UUID playerId, String ageRange, String primaryRole, String workSchedule, String livingSituation, List<String> focusAreas, String sixMonthGoal, String biggestChallenge, List<String> weaknesses, String pastFailures, String quitReasons, String availableTime, List<String> preferredQuestTypes, String difficultyPreference, Map<String, String> questionnaireData, String archetype, java.time.LocalTime wakeUpTime) {
        this.playerId = playerId;
        this.ageRange = ageRange;
        this.primaryRole = primaryRole;
        this.workSchedule = workSchedule;
        this.livingSituation = livingSituation;
        this.focusAreas = focusAreas;
        this.sixMonthGoal = sixMonthGoal;
        this.biggestChallenge = biggestChallenge;
        this.weaknesses = weaknesses;
        this.pastFailures = pastFailures;
        this.quitReasons = quitReasons;
        this.availableTime = availableTime;
        this.preferredQuestTypes = preferredQuestTypes;
        this.difficultyPreference = difficultyPreference;
        this.questionnaireData = questionnaireData;
        this.archetype = archetype;
        this.wakeUpTime = wakeUpTime;
    }

    // Getters
    public UUID getPlayerId() { return playerId; }
    public String getAgeRange() { return ageRange; }
    public String getPrimaryRole() { return primaryRole; }
    public String getWorkSchedule() { return workSchedule; }
    public String getLivingSituation() { return livingSituation; }
    public List<String> getFocusAreas() { return focusAreas; }
    public String getSixMonthGoal() { return sixMonthGoal; }
    public String getBiggestChallenge() { return biggestChallenge; }
    public List<String> getWeaknesses() { return weaknesses; }
    public String getPastFailures() { return pastFailures; }
    public String getQuitReasons() { return quitReasons; }
    public String getAvailableTime() { return availableTime; }
    public List<String> getPreferredQuestTypes() { return preferredQuestTypes; }
    public String getDifficultyPreference() { return difficultyPreference; }
    public Map<String, String> getQuestionnaireData() { return questionnaireData; }
    public String getArchetype() { return archetype; }
    public java.time.LocalTime getWakeUpTime() { return wakeUpTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getTimezoneOffset() { return timezoneOffset; }
    public String getTitle() { return playerId != null ? playerId.toString() : ""; }
    public String getDisplayTheme() { return "default"; }

    // Setters
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public void setAgeRange(String ageRange) { this.ageRange = ageRange; }
    public void setPrimaryRole(String primaryRole) { this.primaryRole = primaryRole; }
    public void setWorkSchedule(String workSchedule) { this.workSchedule = workSchedule; }
    public void setLivingSituation(String livingSituation) { this.livingSituation = livingSituation; }
    public void setFocusAreas(List<String> focusAreas) { this.focusAreas = focusAreas; }
    public void setSixMonthGoal(String sixMonthGoal) { this.sixMonthGoal = sixMonthGoal; }
    public void setBiggestChallenge(String biggestChallenge) { this.biggestChallenge = biggestChallenge; }
    public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; }
    public void setPastFailures(String pastFailures) { this.pastFailures = pastFailures; }
    public void setQuitReasons(String quitReasons) { this.quitReasons = quitReasons; }
    public void setAvailableTime(String availableTime) { this.availableTime = availableTime; }
    public void setPreferredQuestTypes(List<String> preferredQuestTypes) { this.preferredQuestTypes = preferredQuestTypes; }
    public void setDifficultyPreference(String difficultyPreference) { this.difficultyPreference = difficultyPreference; }
    public void setQuestionnaireData(Map<String, String> questionnaireData) { this.questionnaireData = questionnaireData; }
    public void setArchetype(String archetype) { this.archetype = archetype; }
    public void setWakeUpTime(java.time.LocalTime wakeUpTime) { this.wakeUpTime = wakeUpTime; }

    // Simple Builder
    public static class PlayerProfileBuilder {
        private UUID playerId;
        private String ageRange;
        private String primaryRole;
        private String workSchedule;
        private String livingSituation;
        private List<String> focusAreas;
        private String sixMonthGoal;
        private String biggestChallenge;
        private List<String> weaknesses;
        private String pastFailures;
        private String quitReasons;
        private String availableTime;
        private List<String> preferredQuestTypes;
        private String difficultyPreference;
        private Map<String, String> questionnaireData;
        private String archetype;
        private java.time.LocalTime wakeUpTime;

        public PlayerProfileBuilder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public PlayerProfileBuilder ageRange(String ageRange) { this.ageRange = ageRange; return this; }
        public PlayerProfileBuilder primaryRole(String primaryRole) { this.primaryRole = primaryRole; return this; }
        public PlayerProfileBuilder workSchedule(String workSchedule) { this.workSchedule = workSchedule; return this; }
        public PlayerProfileBuilder livingSituation(String livingSituation) { this.livingSituation = livingSituation; return this; }
        public PlayerProfileBuilder focusAreas(List<String> focusAreas) { this.focusAreas = focusAreas; return this; }
        public PlayerProfileBuilder sixMonthGoal(String sixMonthGoal) { this.sixMonthGoal = sixMonthGoal; return this; }
        public PlayerProfileBuilder biggestChallenge(String biggestChallenge) { this.biggestChallenge = biggestChallenge; return this; }
        public PlayerProfileBuilder weaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; return this; }
        public PlayerProfileBuilder pastFailures(String pastFailures) { this.pastFailures = pastFailures; return this; }
        public PlayerProfileBuilder quitReasons(String quitReasons) { this.quitReasons = quitReasons; return this; }
        public PlayerProfileBuilder availableTime(String availableTime) { this.availableTime = availableTime; return this; }
        public PlayerProfileBuilder preferredQuestTypes(List<String> preferredQuestTypes) { this.preferredQuestTypes = preferredQuestTypes; return this; }
        public PlayerProfileBuilder difficultyPreference(String difficultyPreference) { this.difficultyPreference = difficultyPreference; return this; }
        public PlayerProfileBuilder questionnaireData(Map<String, String> questionnaireData) { this.questionnaireData = questionnaireData; return this; }
        public PlayerProfileBuilder archetype(String archetype) { this.archetype = archetype; return this; }
        public PlayerProfileBuilder wakeUpTime(java.time.LocalTime wakeUpTime) { this.wakeUpTime = wakeUpTime; return this; }

        public PlayerProfile build() {
            return new PlayerProfile(playerId, ageRange, primaryRole, workSchedule, livingSituation, focusAreas, sixMonthGoal, biggestChallenge, weaknesses, pastFailures, quitReasons, availableTime, preferredQuestTypes, difficultyPreference, questionnaireData, archetype, wakeUpTime);
        }
    }

    public static PlayerProfileBuilder builder() {
        return new PlayerProfileBuilder();
    }
    @Id
    private UUID playerId;
    
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String timezoneOffset = "+00:00"; 
    
    // Personal Context
    private String ageRange;
    private String primaryRole;
    private String workSchedule;
    private String livingSituation;
    
    // Goals & Focus Areas
    @ElementCollection
    @CollectionTable(name = "profile_focus_areas", joinColumns = @JoinColumn(name = "player_id"))
    @Column(name = "focus_area")
    private List<String> focusAreas; // Max 3
    
    private String sixMonthGoal;
    private String biggestChallenge;
    
    // Weaknesses
    @ElementCollection
    @CollectionTable(name = "profile_weaknesses", joinColumns = @JoinColumn(name = "player_id"))
    @Column(name = "weakness")
    private List<String> weaknesses;
    
    private String pastFailures;
    private String quitReasons;
    
    // Preferences
    private String availableTime;
    
    @ElementCollection
    @CollectionTable(name = "profile_preferred_quest_types", joinColumns = @JoinColumn(name = "player_id"))
    @Column(name = "quest_type")
    private List<String> preferredQuestTypes;
    
    private String difficultyPreference;
    
    @ElementCollection
    @CollectionTable(name = "profile_questionnaire_data", joinColumns = @JoinColumn(name = "player_id"))
    @MapKeyColumn(name = "question_key")
    @Column(name = "answer_value", length = 1000)
    private Map<String, String> questionnaireData;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
