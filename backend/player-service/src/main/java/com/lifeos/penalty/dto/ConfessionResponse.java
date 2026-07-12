package com.lifeos.penalty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfessionResponse {
    private boolean accepted;
    private String feedback;
    private int attemptsRemaining;
    private LocalDateTime lockoutUntil;
    private UUID survivalTaskId;
    private boolean requiresSurvivalTask;
}
