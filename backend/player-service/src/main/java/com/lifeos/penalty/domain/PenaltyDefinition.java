package com.lifeos.penalty.domain;

import com.lifeos.penalty.domain.enums.PenaltySeverity;
import com.lifeos.penalty.domain.enums.PenaltyType;
import com.lifeos.player.domain.enums.AttributeType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Transient object returned by calculation service.
 * Not persisted directly.
 */
@Data
@Builder
public class PenaltyDefinition {
    private PenaltyType type;
    private PenaltySeverity severity;
    private long xpDeduction;
    private AttributeType debuffAttribute;
    private double debuffAmount;
    private LocalDateTime debuffExpiresAt;
    private boolean resetStreak;
}
