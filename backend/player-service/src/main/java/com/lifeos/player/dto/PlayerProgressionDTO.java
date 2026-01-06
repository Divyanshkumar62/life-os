package com.lifeos.player.dto;

import com.lifeos.player.domain.enums.PlayerRank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProgressionDTO {
    private int level;
    private long currentXp;
    private PlayerRank rank;
    private double rankProgressScore;
}
