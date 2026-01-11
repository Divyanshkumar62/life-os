package com.lifeos.streak.service;

import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.dto.PlayerStatusFlagDTO;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.progression.domain.RankExamAttempt;
import com.lifeos.progression.domain.enums.ExamStatus;
import com.lifeos.progression.repository.RankExamAttemptRepository;
import com.lifeos.streak.domain.PlayerStreak;
import com.lifeos.streak.repository.PlayerStreakRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StreakServiceTest {

    @Mock private PlayerStreakRepository streakRepository;
    @Mock private PlayerStateService playerStateService;
    @Mock private RankExamAttemptRepository examRepository;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks private StreakService streakService;

    private UUID playerId;
    private PlayerStreak streak;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        streak = PlayerStreak.builder()
                .playerId(playerId)
                .currentStreak(5)
                .longestStreak(10)
                .previousStreak(0)
                .lastSuccessfulDate(LocalDate.now().minusDays(1))
                .build();
    }

    @Test
    void testProcessDetailCompletion_Success() {
        when(streakRepository.findByPlayerId(playerId)).thenReturn(Optional.of(streak));

        LocalDate yesterday = LocalDate.now().minusDays(1);
        streakService.processDailyCompletion(playerId, yesterday, true);

        assertEquals(6, streak.getCurrentStreak());
        assertEquals(yesterday, streak.getLastSuccessfulDate());
        verify(streakRepository).save(streak);
    }
    
    @Test
    void testProcessDetailCompletion_NewRecord() {
        streak.setCurrentStreak(10);
        streak.setLongestStreak(10);
        when(streakRepository.findByPlayerId(playerId)).thenReturn(Optional.of(streak));

        streakService.processDailyCompletion(playerId, LocalDate.now().minusDays(1), true);

        assertEquals(11, streak.getCurrentStreak());
        assertEquals(11, streak.getLongestStreak());
    }

    @Test
    void testProcessDetailCompletion_Failure() {
        streak.setCurrentStreak(5);
        when(streakRepository.findByPlayerId(playerId)).thenReturn(Optional.of(streak));

        LocalDate yesterday = LocalDate.now().minusDays(1);
        streakService.processDailyCompletion(playerId, yesterday, false);

        assertEquals(0, streak.getCurrentStreak());
        assertEquals(5, streak.getPreviousStreak()); // Saved for repair
        assertEquals(yesterday, streak.getLastBrokenDate());
        verify(streakRepository).save(streak);
    }

    @Test
    void testGetGoldMultiplier() {
        PlayerStreak s = PlayerStreak.builder().currentStreak(0).build();
        when(streakRepository.findByPlayerId(playerId)).thenReturn(Optional.of(s));
        
        // 0 days
        assertEquals(0.0, streakService.getGoldMultiplier(playerId));

        // 3 days
        s.setCurrentStreak(3);
        assertEquals(0.05, streakService.getGoldMultiplier(playerId));
        
        // 60 days
        s.setCurrentStreak(60);
        assertEquals(0.50, streakService.getGoldMultiplier(playerId));
    }

    @Test
    void testResetStreak() {
        when(streakRepository.findByPlayerId(playerId)).thenReturn(Optional.of(streak));
        
        streakService.resetStreak(playerId);
        
        assertEquals(0, streak.getCurrentStreak());
        assertEquals(5, streak.getPreviousStreak());
        assertNotNull(streak.getLastBrokenDate());
    }
    
    @Test
    void testApplyRepair_Success() {
        // Given broken streak
        streak.setCurrentStreak(0);
        streak.setPreviousStreak(10);
        streak.setLastBrokenDate(LocalDate.now().minusDays(1)); // Broke yesterday
        
        when(streakRepository.findByPlayerId(playerId)).thenReturn(Optional.of(streak));
        when(playerStateService.getPlayerState(playerId)).thenReturn(PlayerStateResponse.builder().activeFlags(Collections.emptyList()).build());
        when(examRepository.findLatestByPlayerId(playerId)).thenReturn(Optional.empty());

        streakService.applyStreakRepair(playerId);
        
        assertEquals(10, streak.getCurrentStreak());
        verify(streakRepository).save(streak);
    }

    @Test
    void testApplyRepair_Fail_ActiveStreak() {
        streak.setCurrentStreak(1);
        when(streakRepository.findByPlayerId(playerId)).thenReturn(Optional.of(streak));
        
        assertThrows(IllegalStateException.class, () -> streakService.applyStreakRepair(playerId));
    }

    @Test
    void testApplyRepair_Fail_TooLate() {
        streak.setCurrentStreak(0);
        streak.setLastBrokenDate(LocalDate.now().minusDays(2)); // 2 days ago
        when(streakRepository.findByPlayerId(playerId)).thenReturn(Optional.of(streak));
        
        assertThrows(IllegalStateException.class, () -> streakService.applyStreakRepair(playerId));
    }

    @Test
    void testApplyRepair_Fail_PenaltyZone() {
        streak.setCurrentStreak(0);
        streak.setLastBrokenDate(LocalDate.now().minusDays(1));
        when(streakRepository.findByPlayerId(playerId)).thenReturn(Optional.of(streak));
        
        PlayerStatusFlagDTO flag = new PlayerStatusFlagDTO();
        flag.setFlag(StatusFlagType.PENALTY_ZONE);
        when(playerStateService.getPlayerState(playerId)).thenReturn(PlayerStateResponse.builder().activeFlags(List.of(flag)).build());
        
        assertThrows(IllegalStateException.class, () -> streakService.applyStreakRepair(playerId));
    }
    
    @Test
    void testApplyRepair_Fail_ExamActive() {
        streak.setCurrentStreak(0);
        streak.setLastBrokenDate(LocalDate.now().minusDays(1));
        when(streakRepository.findByPlayerId(playerId)).thenReturn(Optional.of(streak));
        when(playerStateService.getPlayerState(playerId)).thenReturn(PlayerStateResponse.builder().activeFlags(Collections.emptyList()).build());

        RankExamAttempt exam = RankExamAttempt.builder().status(ExamStatus.UNLOCKED).build();
        when(examRepository.findLatestByPlayerId(playerId)).thenReturn(Optional.of(exam));
        
        assertThrows(IllegalStateException.class, () -> streakService.applyStreakRepair(playerId));
    }
}
