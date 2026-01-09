package com.lifeos.quest.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SystemDailyTemplate {
    WAKE_UP("Wake Up", DifficultyTier.E),
    MOVEMENT("Movement", DifficultyTier.D),
    FOCUS_BLOCK("Deep Focus", DifficultyTier.C),
    REFLECTION("Reflection", DifficultyTier.E);

    private final String defaultTitle;
    private final DifficultyTier difficulty;
}
