package com.lifeos.onboarding.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "player_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProfile {
    @Id
    private UUID playerId;
    
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
