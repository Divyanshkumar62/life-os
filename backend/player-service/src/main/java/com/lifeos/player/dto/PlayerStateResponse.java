package com.lifeos.player.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStateResponse {
    private PlayerIdentityDTO identity;
    private PlayerProgressionDTO progression;
    private List<PlayerAttributeDTO> attributes;
    private PlayerPsychStateDTO psychState;
    private PlayerMetricsDTO metrics;
    private List<PlayerStatusFlagDTO> activeFlags;
    private PlayerTemporalStateDTO temporalState;
    private PlayerHistoryDTO history;
}
