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
           Map.of(AttributeType.STR, 5.0, AttributeType.INT, 5.0)),
           
    D_TO_C(PlayerRank.D, PlayerRank.C, 2, 
           Map.of(AttributeType.STR, 10.0, AttributeType.INT, 10.0, AttributeType.VIT, 5.0)),
           
    C_TO_B(PlayerRank.C, PlayerRank.B, 3, 
           Map.of(AttributeType.STR, 20.0, AttributeType.INT, 20.0, AttributeType.VIT, 10.0, AttributeType.SEN, 10.0)),
           
    B_TO_A(PlayerRank.B, PlayerRank.A, 5, 
           Map.of(AttributeType.STR, 30.0, AttributeType.INT, 30.0, AttributeType.VIT, 20.0, AttributeType.SEN, 20.0)),
           
    A_TO_S(PlayerRank.A, PlayerRank.S, 8, 
           Map.of(AttributeType.STR, 50.0, AttributeType.INT, 50.0, AttributeType.VIT, 40.0, AttributeType.SEN, 40.0));
    
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
