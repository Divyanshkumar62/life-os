package com.lifeos.player.service;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.dto.PlayerStateResponse;

import java.util.UUID;

public interface PlayerStateService {
    PlayerStateResponse initializePlayer(String username);
    PlayerStateResponse getPlayerState(UUID playerId);
    void addXp(UUID playerId, long xpAmount);
    void updateAttribute(UUID playerId, AttributeType type, double valueChange);
    void updatePsychMetric(UUID playerId, String metricName, double valueChange); // metricName: MOMENTUM, STRESS, COMPLACENCY, CONFIDENCE
    void incrementStat(UUID playerId, AttributeType type, int amount);

    // Penalty Methods
    void applyXpDeduction(UUID playerId, long amount);
    void resetStreak(UUID playerId);
    void applyStatDebuff(UUID playerId, AttributeType type, double amount, java.time.LocalDateTime expiresAt);
    void applyStatusFlag(UUID playerId, com.lifeos.player.domain.enums.StatusFlagType flag, java.time.LocalDateTime expiresAt);
    // Reward Methods
    void extendStreak(UUID playerId);
    void adjustMomentum(UUID playerId, int delta);
    
    // Progression Methods
    void promoteRank(UUID playerId);
}
