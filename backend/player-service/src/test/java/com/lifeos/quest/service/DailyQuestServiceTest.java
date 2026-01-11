package com.lifeos.quest.service;

import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.dto.PlayerIdentityDTO;
import com.lifeos.player.dto.PlayerProgressionDTO;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.dto.PlayerTemporalStateDTO;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.dto.PlayerStatusFlagDTO;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestCategory;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.repository.QuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DailyQuestServiceTest {

    @Mock private QuestRepository questRepository;
    @Mock private PlayerStateService playerStateService;
    @Mock private PlayerIdentityRepository playerRepository;
    @Mock private PenaltyService penaltyService;
    @Mock private com.lifeos.streak.service.StreakService streakService;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DailyQuestService dailyQuestService;

    private UUID playerId;
    private PlayerStateResponse playerState;
    private PlayerIdentityDTO identity;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        identity = PlayerIdentityDTO.builder().playerId(playerId).build();
        
        PlayerProgressionDTO progression = PlayerProgressionDTO.builder()
                .rank(PlayerRank.E)
                .build();
                
        // Ensure Temporal State is not null
        PlayerTemporalStateDTO temporal = PlayerTemporalStateDTO.builder()
                .consecutiveDailyFailures(0)
                .build();

        playerState = PlayerStateResponse.builder()
                .identity(identity)
                .progression(progression)
                .temporalState(temporal)
                .activeFlags(Collections.emptyList())
                .build();
    }

    @Test
    void testRunDailyResetCycle() {
        when(playerRepository.findAllIds()).thenReturn(List.of(playerId));
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        when(questRepository.findByPlayerPlayerIdAndState(playerId, QuestState.ACTIVE))
                .thenReturn(Collections.emptyList());
                
        dailyQuestService.runDailyResetCycle();
        
        // Should trigger generation for the player
        verify(questRepository, atLeastOnce()).save(any(Quest.class));
    }

    @Test
    void testProcessReset_MarksFailed_AndtriggersPenalty() {
        Quest expiredDaily = Quest.builder()
                .questId(UUID.randomUUID())
                .category(QuestCategory.SYSTEM_DAILY)
                .state(QuestState.ACTIVE)
                .deadlineAt(LocalDateTime.now().minusMinutes(1)) // Expired
                .build();
                
        // Set Failures to 1 so next failure triggers Penalty (Threshold >= 2)
        playerState.getTemporalState().setConsecutiveDailyFailures(1);
        
        when(questRepository.findByPlayerPlayerIdAndState(playerId, QuestState.ACTIVE))
                .thenReturn(List.of(expiredDaily));
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);

        dailyQuestService.processPlayerReset(playerId);
        
        assertEquals(QuestState.FAILED, expiredDaily.getState());
        verify(penaltyService).enterPenaltyZone(eq(playerId), contains("Consecutive"));
        verify(questRepository).saveAll(anyList());
    }

    @Test
    void testProcessReset_IgnoresProjectSubtasks() {
        Quest projectTask = Quest.builder()
                .questId(UUID.randomUUID())
                .category(QuestCategory.PROJECT_SUBTASK)
                .state(QuestState.ACTIVE)
                .deadlineAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(questRepository.findByPlayerPlayerIdAndState(playerId, QuestState.ACTIVE))
                .thenReturn(List.of(projectTask)); // Should be filtered out by stream logic
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        
        dailyQuestService.processPlayerReset(playerId);
        
        // Should NOT be marked FAILED by this service (logic filters for SYSTEM_DAILY)
        assertEquals(QuestState.ACTIVE, projectTask.getState());
        verify(penaltyService, never()).enterPenaltyZone(any(), any());
    }
    
    @Test
    void testGenerateDailyQuests_ERank_Count() {
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState); // Rank E = 2 dailies
        
        dailyQuestService.generateDailyQuests(playerId);
        
        ArgumentCaptor<Quest> captor = ArgumentCaptor.forClass(Quest.class);
        verify(questRepository, times(2)).save(captor.capture());
        
        List<Quest> saved = captor.getAllValues();
        assertEquals(QuestCategory.SYSTEM_DAILY, saved.get(0).getCategory());
        assertNotNull(saved.get(0).getDeadlineAt());
    }
}
