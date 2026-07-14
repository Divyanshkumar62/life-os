package com.lifeos.player.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelUpResultDTO {
    private UUID playerId;
    private int newLevel;
    private int previousLevel;
    private int statPointsAwarded;
    private long goldAwarded;
    private boolean debuffsCleansed;
    private boolean xpFrozen;
    private long xpBurned;
    private int rankCapLevel;
}
