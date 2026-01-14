package com.lifeos.voice.domain.enums;

public enum SystemMessageType {
    // Reward
    QUEST_COMPLETED("Objective cleared.\nPerformance registered.", SystemVoiceMode.REWARD, false),
    QUEST_COMPLETED_SIMPLE("Quest completed.\nRewards applied.", SystemVoiceMode.REWARD, false),
    
    // Warning
    DAILY_INCOMPLETE("Daily objectives incomplete.\nFurther failure will result in penalties.", SystemVoiceMode.WARNING, true),
    
    // Penalty
    PENALTY_ZONE_ENTRY("Daily objectives failed.\nPenalty Zone activated.", SystemVoiceMode.PENALTY, true),
    PENALTY_ONGOING("Restricted mode active.\nComplete the Penalty Quest to regain access.", SystemVoiceMode.PENALTY, true),
    PENALTY_QUEST_COMPLETED("Penalty protocols satisfied.\nAccess restored.", SystemVoiceMode.REWARD, true),
    
    // Rank
    RANK_GATE_REACHED("Level limit reached.\nRank advancement required.", SystemVoiceMode.NEUTRAL, false),
    PROMOTION_UNLOCKED("Qualifications met.\nPromotion Exam unlocked.", SystemVoiceMode.PROMOTION, true),
    PROMOTION_PASSED("Rank advancement approved.\nLimits removed.", SystemVoiceMode.PROMOTION, true),
    PROMOTION_FAILED("Promotion attempt failed.\nPenalty conditions applied.", SystemVoiceMode.FAILURE, true),
    
    // Streak
    STREAK_BROKEN("Streak terminated.\nConsistency record reset.", SystemVoiceMode.FAILURE, true),
    
    // Generic
    SYSTEM_NOTICE("System Notice.", SystemVoiceMode.NEUTRAL, false);

    private final String template;
    private final SystemVoiceMode defaultMode;
    private final boolean critical;

    SystemMessageType(String template, SystemVoiceMode defaultMode, boolean critical) {
        this.template = template;
        this.defaultMode = defaultMode;
        this.critical = critical;
    }

    public String getTemplate() {
        return template;
    }

    public SystemVoiceMode getDefaultMode() {
        return defaultMode;
    }

    public boolean isCritical() {
        return critical;
    }
}
