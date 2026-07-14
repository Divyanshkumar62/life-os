package com.lifeos.onboarding.dto;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwakeningPenaltyResultDTO {
    private UUID playerId;
    private boolean cleared;
    private LocalDate trialResetDate;
    private boolean accountDeleted;
}
