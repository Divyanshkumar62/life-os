package com.lifeos.voice.domain.enums;

public enum SystemMessageType {
    // Reward
    QUEST_COMPLETED("Objective cleared.\nPerformance registered.", SystemVoiceMode.REWARD),
    QUEST_COMPLETED_SIMPLE("Quest completed.\nRewards applied.", SystemVoiceMode.REWARD),
    
    // Warning
    DAILY_INCOMPLETE("Daily objectives incomplete.\nFurther failure will result in penalties.", SystemVoiceMode.WARNING),
    
    // Penalty
    PENALTY_ZONE_ENTRY("Daily objectives failed.\nPenalty Zone activated.", SystemVoiceMode.PENALTY),
    PENALTY_ONGOING("Restricted mode active.\nComplete the Penalty Quest to regain access.", SystemVoiceMode.PENALTY),
    PENALTY_QUEST_COMPLETED("Penalty protocols satisfied.\nAccess restored.", SystemVoiceMode.REWARD),
    
    // Rank
    RANK_GATE_REACHED("Level limit reached.\nRank advancement required.", SystemVoiceMode.NEUTRAL),
    PROMOTION_UNLOCKED("Qualifications met.\nPromotion Exam unlocked.", SystemVoiceMode.PROMOTION),
    PROMOTION_PASSED("Rank advancement approved.\nLimits removed.", SystemVoiceMode.PROMOTION),
    PROMOTION_FAILED("Promotion attempt failed.\nPenalty conditions applied.", SystemVoiceMode.FAILURE),
    
    // Streak
    STREAK_BROKEN("Streak terminated.\nConsistency record reset.", SystemVoiceMode.FAILURE),
    
    // Generic
    SYSTEM_NOTICE("System Notice.", SystemVoiceMode.NEUTRAL);

    private final String template;
    private final SystemVoiceMode defaultMode;

    SystemMessageType(String template, SystemVoiceMode defaultMode) {
        this.template = template;
        this.defaultMode = defaultMode;
    }

    public String getTemplate() {
        return template;
    }

    public SystemVoiceMode getDefaultMode() {
        return defaultMode;
    }
}
