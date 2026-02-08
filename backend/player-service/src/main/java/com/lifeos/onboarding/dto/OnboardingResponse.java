package com.lifeos.onboarding.dto;

import com.lifeos.quest.domain.Quest;
import com.lifeos.onboarding.domain.OnboardingStage;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OnboardingResponse {
    private UUID playerId;
    private OnboardingStage currentStage;
    private Quest trialQuest;
    private String message;
}
