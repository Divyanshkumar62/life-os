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
public class SurvivalTaskDTO {
    private UUID questId;
    private UUID playerId;
    private String type;
    private String title;
    private String description;
    private int requiredCount;
    private int completedCount;
    private double progress;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Long goldDeducted;
    private Boolean escaped;
}
