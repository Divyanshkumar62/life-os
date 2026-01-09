package com.lifeos.progression.domain;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.domain.enums.PlayerRank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum RankTransitionTemplate {

    E_TO_D(PlayerRank.E, PlayerRank.D, 1, 
           Map.of(AttributeType.PHYSICAL_ENERGY, 10.0, AttributeType.DISCIPLINE, 8.0)),
           
    D_TO_C(PlayerRank.D, PlayerRank.C, 2, 
           Map.of(AttributeType.DISCIPLINE, 15.0, AttributeType.FOCUS, 12.0)),
           
    C_TO_B(PlayerRank.C, PlayerRank.B, 3, 
           Map.of(AttributeType.DISCIPLINE, 25.0, AttributeType.FOCUS, 20.0, AttributeType.MENTAL_RESILIENCE, 15.0)), // Willpower mapped to Mental Resilience? Or need new Type? Assuming mapped for now.
           
    B_TO_A(PlayerRank.B, PlayerRank.A, 5, 
           Map.of(AttributeType.DISCIPLINE, 40.0, AttributeType.FOCUS, 35.0, AttributeType.EMOTIONAL_CONTROL, 25.0)),
           
    A_TO_S(PlayerRank.A, PlayerRank.S, 8, 
           Map.of(AttributeType.DISCIPLINE, 60.0)); // Placeholder for full "All Core Stats >= 60" check which requires custom logic.
    
    private final PlayerRank fromRank;
    private final PlayerRank toRank;
    private final int bossKeyCost;
    private final Map<AttributeType, Double> statRequirements;

    public static RankTransitionTemplate from(PlayerRank rank) {
        for (RankTransitionTemplate template : values()) {
            if (template.fromRank == rank) {
                return template;
            }
        }
        return null; // S or SS
    }
}
