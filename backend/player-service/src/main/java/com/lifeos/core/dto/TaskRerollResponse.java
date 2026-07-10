package com.lifeos.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRerollResponse {
    private UUID playerId;
    private long goldDeducted;
    private long remainingGold;
    private NewSurvivalTask newSurvivalTask;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewSurvivalTask {
        private UUID survivalTaskId;
        private String description;
        private int timeLimitHours;
    }
}
