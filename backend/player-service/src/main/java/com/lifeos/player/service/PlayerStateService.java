package com.lifeos.player.service;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.dto.PlayerStateResponse;

import java.util.UUID;

public interface PlayerStateService {
    PlayerStateResponse initializePlayer(String username);
    PlayerStateResponse getPlayerState(UUID playerId);
    void addXp(UUID playerId, long xpAmount);
    void updateAttribute(UUID playerId, AttributeType type, double valueChange);
    // Add more methods as logic expands
}
