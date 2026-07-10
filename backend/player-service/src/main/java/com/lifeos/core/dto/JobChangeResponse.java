package com.lifeos.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobChangeResponse {
    private UUID playerId;
    private String jobClass;
    private EvolutionRewards evolutionRewards;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvolutionRewards {
        private long goldAwarded;
        private int statPointsAwarded;
        private List<String> itemsAwarded;
    }
}
