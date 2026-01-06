package com.lifeos.player.dto;

import com.lifeos.player.domain.enums.AttributeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAttributeDTO {
    private AttributeType attributeType;
    private double baseValue;
    private double currentValue;
    private double growthVelocity;
    private double decayRate;
}
