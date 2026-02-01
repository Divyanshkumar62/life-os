package com.lifeos.player.state;

import com.lifeos.event.concrete.*;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlayerStateEventHandler {

    private final PlayerStateRepository repository;

    @EventListener
    @Transactional
    public void onPenaltyEntered(PenaltyZoneEnteredEvent event) {
        updateSnapshot(event.getPlayerId(), snapshot -> {
            snapshot.setInPenaltyZone(true);
            snapshot.getActiveFlags().add(PlayerFlag.PENALTY_ZONE);
            // Typically entering penalty implies broken streak unless shielded
            snapshot.setStreakActive(false);
            snapshot.getActiveFlags().add(PlayerFlag.STREAK_BROKEN);
        });
    }

    @EventListener
    @Transactional
    public void onPenaltyExited(PenaltyZoneExitedEvent event) {
        updateSnapshot(event.getPlayerId(), snapshot -> {
            snapshot.setInPenaltyZone(false);
            snapshot.getActiveFlags().remove(PlayerFlag.PENALTY_ZONE);
            // Exiting penalty doesn't auto-restore streak, but removes the immediate block
            // Logic for streak restoration is complex, but read model just records "Not in penalty"
        });
    }

    @EventListener
    @Transactional
    public void onQuestCompleted(QuestCompletedEvent event) {
        updateSnapshot(event.getPlayerId(), snapshot -> {
            // Only increment streak if it is currently active
            if (snapshot.isStreakActive()) {
                snapshot.setCurrentStreak(snapshot.getCurrentStreak() + 1);
            }
            // If strictly adhering to "Events update state", we implicitly trust the event.
            // However, Law: "Streak cannot increase while broken". 
            // So checking isStreakActive() is correct.
        });
    }

    @EventListener
    @Transactional
    public void onDailyQuestFailed(DailyQuestFailedEvent event) {
       updateSnapshot(event.getPlayerId(), snapshot -> {
           snapshot.setStreakActive(false);
           snapshot.getActiveFlags().add(PlayerFlag.STREAK_BROKEN);
           // Optional: Reset count? Usually streak preserves count but becomes inactive until recovered.
           // For now, just mark inactive.
       });
    }
    
    // Helper to fetch-update-save
    @EventListener
    @Transactional
    public void onStreakBroken(StreakBrokenEvent event) {
        updateSnapshot(event.getPlayerId(), snapshot -> {
            snapshot.setStreakActive(false);
            snapshot.getActiveFlags().add(PlayerFlag.STREAK_BROKEN);
        });
    }

    private void updateSnapshot(UUID playerId, java.util.function.Consumer<PlayerStateSnapshot> updater) {
        PlayerStateSnapshot snapshot = repository.findById(playerId)
                .orElse(PlayerStateSnapshot.builder()
                        .playerId(playerId)
                        .build());
        updater.accept(snapshot);
        repository.save(snapshot);
    }
}
