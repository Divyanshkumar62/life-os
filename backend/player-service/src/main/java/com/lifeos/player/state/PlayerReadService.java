package com.lifeos.player.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerReadService {

    private final PlayerStateRepository repository;

    @Transactional(readOnly = true)
    public PlayerStateSnapshot getSnapshot(UUID playerId) {
        return repository.findById(playerId)
                .orElse(PlayerStateSnapshot.builder()
                        .playerId(playerId)
                        .activeFlags(Collections.emptySet())
                        .currentStreak(0)
                        .streakActive(false)
                        .inPenaltyZone(false)
                        .build());
    }

    @Transactional(readOnly = true)
    public boolean hasActiveFlag(UUID playerId, PlayerFlag flag) {
        return getSnapshot(playerId).getActiveFlags().contains(flag);
    }

    @Transactional(readOnly = true)
    public boolean isInPenalty(UUID playerId) {
        return getSnapshot(playerId).isInPenaltyZone();
    }

    @Transactional(readOnly = true)
    public boolean isRewardAllowed(UUID playerId) {
        PlayerStateSnapshot snapshot = getSnapshot(playerId);
        return !snapshot.isInPenaltyZone() 
            && !snapshot.getActiveFlags().contains(PlayerFlag.REWARDS_SUPPRESSED);
    }

    @Transactional(readOnly = true)
    public boolean isStreakActive(UUID playerId) {
        return getSnapshot(playerId).isStreakActive();
    }
}
