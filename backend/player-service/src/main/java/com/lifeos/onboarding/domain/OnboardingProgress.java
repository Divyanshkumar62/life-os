package com.lifeos.onboarding.domain;

import com.lifeos.player.domain.PlayerIdentity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "onboarding_progress")
public class OnboardingProgress {
    
    public OnboardingProgress() {}

    public OnboardingProgress(PlayerIdentity player, OnboardingStage currentStage, boolean trialCompleted) {
        this.player = player;
        this.currentStage = currentStage;
        this.trialCompleted = trialCompleted;
    }

    // Getters
    public UUID getPlayerId() { return playerId; }
    public PlayerIdentity getPlayer() { return player; }
    public OnboardingStage getCurrentStage() { return currentStage; }
    public UUID getTrialQuestId() { return trialQuestId; }
    public boolean isTrialCompleted() { return trialCompleted; }
    public Map<String, Object> getQuestionnaireData() { return questionnaireData; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    // Setters
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
    public void setCurrentStage(OnboardingStage currentStage) { this.currentStage = currentStage; }
    public void setTrialQuestId(UUID trialQuestId) { this.trialQuestId = trialQuestId; }
    public void setTrialCompleted(boolean trialCompleted) { this.trialCompleted = trialCompleted; }
    public void setQuestionnaireData(Map<String, Object> questionnaireData) { this.questionnaireData = questionnaireData; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    // Simple Builder
    public static class OnboardingProgressBuilder {
        private PlayerIdentity player;
        private OnboardingStage currentStage;
        private boolean trialCompleted;

        public OnboardingProgressBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public OnboardingProgressBuilder currentStage(OnboardingStage currentStage) { this.currentStage = currentStage; return this; }
        public OnboardingProgressBuilder trialCompleted(boolean trialCompleted) { this.trialCompleted = trialCompleted; return this; }

        public OnboardingProgress build() {
            return new OnboardingProgress(player, currentStage, trialCompleted);
        }
    }

    public static OnboardingProgressBuilder builder() {
        return new OnboardingProgressBuilder();
    }
    
    @Id
    @Column(name = "player_id")
    private UUID playerId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "player_id")
    private PlayerIdentity player;
    
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(255)")
    private OnboardingStage currentStage;
    
    private UUID trialQuestId;
    private boolean trialCompleted;
    
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> questionnaireData;
    
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (currentStage == null) {
            currentStage = OnboardingStage.TRIAL_QUEST;
        }
    }
}
