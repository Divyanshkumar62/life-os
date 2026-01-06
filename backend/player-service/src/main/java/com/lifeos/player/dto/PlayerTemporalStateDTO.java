package com.lifeos.player.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerTemporalStateDTO {
    private LocalDateTime lastQuestCompletedAt;
    private int activeStreakDays;
    private double restDebt;
    private double burnoutRiskScore;
}
