package com.lifeos.penalty.domain.enums;

public enum PenaltySeverity {
    LOW,        // Small XP deduction
    MEDIUM,     // XP + Mild Debuff
    HIGH,       // XP + Debuff + Streak Reset
    CRITICAL    // All above + Longer duration
}
