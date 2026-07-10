package com.lifeos.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurvivalTaskSubmitRequest {

    @NotNull(message = "Player ID is required")
    private UUID playerId;

    @NotNull(message = "Survival Task ID is required")
    private UUID survivalTaskId;

    @NotBlank(message = "Submission proof text is required")
    private String submissionText;

    private String proofMediaUrl;
}
