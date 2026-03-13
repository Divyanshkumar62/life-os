package com.lifeos.onboarding.dto;

import com.lifeos.quest.domain.Quest;
import com.lifeos.onboarding.domain.OnboardingStage;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

public class OnboardingResponse {
    private UUID playerId;
    private OnboardingStage currentStage;
    private Quest trialQuest;
    private String message;

    public OnboardingResponse() {}

    public OnboardingResponse(UUID playerId, OnboardingStage currentStage, Quest trialQuest, String message) {
        this.playerId = playerId;
        this.currentStage = currentStage;
        this.trialQuest = trialQuest;
        this.message = message;
    }

    // Getters
    public UUID getPlayerId() { return playerId; }
    public OnboardingStage getCurrentStage() { return currentStage; }
    public Quest getTrialQuest() { return trialQuest; }
    public String getMessage() { return message; }

    // Setters
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public void setCurrentStage(OnboardingStage currentStage) { this.currentStage = currentStage; }
    public void setTrialQuest(Quest trialQuest) { this.trialQuest = trialQuest; }
    public void setMessage(String message) { this.message = message; }

    // Simple Builder
    public static class OnboardingResponseBuilder {
        private UUID playerId;
        private OnboardingStage currentStage;
        private Quest trialQuest;
        private String message;

        public OnboardingResponseBuilder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public OnboardingResponseBuilder currentStage(OnboardingStage currentStage) { this.currentStage = currentStage; return this; }
        public OnboardingResponseBuilder trialQuest(Quest trialQuest) { this.trialQuest = trialQuest; return this; }
        public OnboardingResponseBuilder message(String message) { this.message = message; return this; }

        public OnboardingResponse build() {
            return new OnboardingResponse(playerId, currentStage, trialQuest, message);
        }
    }

    public static OnboardingResponseBuilder builder() {
        return new OnboardingResponseBuilder();
    }
}
