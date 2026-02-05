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
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingProgress {
    
    @Id
    @Column(name = "player_id")
    private UUID playerId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "player_id")
    private PlayerIdentity player;
    
    @Enumerated(EnumType.STRING)
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
