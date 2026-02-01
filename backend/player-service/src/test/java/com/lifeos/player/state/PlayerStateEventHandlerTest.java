package com.lifeos.player.state;

import com.lifeos.event.concrete.PenaltyZoneEnteredEvent;
import com.lifeos.event.concrete.PenaltyZoneExitedEvent;
import com.lifeos.event.concrete.DailyQuestFailedEvent;
import com.lifeos.event.concrete.QuestCompletedEvent;
import com.lifeos.quest.domain.enums.QuestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerStateEventHandlerTest {

    @Mock
    private PlayerStateRepository repository;

    private PlayerStateEventHandler handler;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        handler = new PlayerStateEventHandler(repository);
        playerId = UUID.randomUUID();
    }

    @Test
    void testOnPenaltyEntered_UpdatesSnapshot() {
        when(repository.findById(playerId)).thenReturn(Optional.empty());

        handler.onPenaltyEntered(new PenaltyZoneEnteredEvent(playerId));

        verify(repository).save(argThat(snapshot -> 
            snapshot.getPlayerId().equals(playerId) &&
            snapshot.isInPenaltyZone() &&
            snapshot.getActiveFlags().contains(PlayerFlag.PENALTY_ZONE) &&
            !snapshot.isStreakActive() &&
            snapshot.getActiveFlags().contains(PlayerFlag.STREAK_BROKEN)
        ));
    }

    @Test
    void testOnPenaltyExited_UpdatesSnapshot() {
        PlayerStateSnapshot existing = PlayerStateSnapshot.builder()
                .playerId(playerId)
                .inPenaltyZone(true)
                .build();
        existing.getActiveFlags().add(PlayerFlag.PENALTY_ZONE);
        
        when(repository.findById(playerId)).thenReturn(Optional.of(existing));

        handler.onPenaltyExited(new PenaltyZoneExitedEvent(playerId));

        verify(repository).save(argThat(snapshot -> 
            snapshot.getPlayerId().equals(playerId) &&
            !snapshot.isInPenaltyZone() &&
            !snapshot.getActiveFlags().contains(PlayerFlag.PENALTY_ZONE)
        ));
    }

    @Test
    void testOnDailyQuestFailed_BreaksStreak() {
        when(repository.findById(playerId)).thenReturn(Optional.empty());

        handler.onDailyQuestFailed(new DailyQuestFailedEvent(playerId));

        verify(repository).save(argThat(snapshot -> 
            snapshot.getPlayerId().equals(playerId) &&
            !snapshot.isStreakActive() &&
            snapshot.getActiveFlags().contains(PlayerFlag.STREAK_BROKEN)
        ));
    }
    
    @Test
    void testOnQuestCompleted_IncrementsStreak_WhenActive() {
        PlayerStateSnapshot existing = PlayerStateSnapshot.builder()
                .playerId(playerId)
                .streakActive(true)
                .currentStreak(5)
                .build();
        when(repository.findById(playerId)).thenReturn(Optional.of(existing));

        handler.onQuestCompleted(new QuestCompletedEvent(playerId, UUID.randomUUID(), QuestType.DISCIPLINE));

        verify(repository).save(argThat(snapshot -> 
            snapshot.getCurrentStreak() == 6 &&
            snapshot.isStreakActive()
        ));
    }
    
    @Test
    void testOnQuestCompleted_DoesNotIncrementStreak_WhenBroken() {
        PlayerStateSnapshot existing = PlayerStateSnapshot.builder()
                .playerId(playerId)
                .streakActive(false)
                .currentStreak(5)
                .build();
        when(repository.findById(playerId)).thenReturn(Optional.of(existing));

        handler.onQuestCompleted(new QuestCompletedEvent(playerId, UUID.randomUUID(), QuestType.DISCIPLINE));

        verify(repository).save(argThat(snapshot -> 
            snapshot.getCurrentStreak() == 5 &&
            !snapshot.isStreakActive()
        ));
    }

    @Test
    void testOnStreakBroken_UpdatesSnapshot() {
        when(repository.findById(playerId)).thenReturn(Optional.empty());

        handler.onStreakBroken(new com.lifeos.event.concrete.StreakBrokenEvent(playerId, 5, "DAILY_FAILURE"));

        verify(repository).save(argThat(snapshot -> 
            snapshot.getPlayerId().equals(playerId) &&
            !snapshot.isStreakActive() &&
            snapshot.getActiveFlags().contains(PlayerFlag.STREAK_BROKEN)
        ));
    }
}
