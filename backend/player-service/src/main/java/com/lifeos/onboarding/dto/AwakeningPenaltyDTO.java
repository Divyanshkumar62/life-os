package com.lifeos.onboarding.dto;

import com.lifeos.onboarding.domain.OnboardingStage;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwakeningPenaltyDTO {
    private UUID playerId;
    private UUID penaltyQuestId;
    private String taskTitle;
    private String taskDescription;
    private LocalDateTime deadlineAt;
    private OnboardingStage stage;
}
