package com.lifeos.player.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerMetricsDTO {
    private double questSuccessRate;
    private double averageQuestDifficulty;
    private int failureStreak;
    private double recoveryRate;
}
