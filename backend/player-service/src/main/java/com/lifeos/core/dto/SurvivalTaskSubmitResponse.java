package com.lifeos.core.dto;

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
public class SurvivalTaskSubmitResponse {
    private UUID playerId;
    private String status;
    private boolean escaped;
    private LocalDateTime escapedAt;
}
