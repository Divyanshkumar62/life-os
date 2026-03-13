package com.lifeos.notification.dto;

public enum NotificationType {
    DUNGEON_BREAK("high"),
    MIDNIGHT_COUNTDOWN("high"),
    LEVEL_UP("normal"),
    RANK_PROMOTION("normal"),
    SHOP_RESTOCK("low"),
    INTEL_QUEST("low");

    private final String priority;

    NotificationType(String priority) {
        this.priority = priority;
    }

    public String getPriority() {
        return priority;
    }
}
