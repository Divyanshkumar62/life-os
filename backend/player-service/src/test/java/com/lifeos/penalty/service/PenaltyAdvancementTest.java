package com.lifeos.penalty.service;

import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.PlayerTemporalState;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.dto.PlayerTemporalStateDTO;
import com.lifeos.player.dto.PlayerStatusFlagDTO;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.project.domain.Project;
import com.lifeos.project.service.ProjectService;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestCategory;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.domain.enums.QuestType;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.quest.service.DailyQuestService;
import com.lifeos.quest.service.QuestLifecycleServiceImpl;
import com.lifeos.player.dto.PlayerProgressionDTO;
import com.lifeos.player.domain.enums.PlayerRank;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PenaltyAdvancementTest {

    @Mock private PlayerStateService playerStateService;
    @Mock private QuestRepository questRepository;
    @Mock private PenaltyService penaltyService; // Mocking for DailyQuestService tests
    @Mock private ProjectService projectService; 
    
    // We need real instances for logic testing where possible, but here we test interactions mostly.
    @InjectMocks private DailyQuestService dailyQuestService; // To test Hybrid Trigger
    
    private UUID playerId;
    private PlayerStateResponse playerState;


// ...

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        
        // Base Player State
        playerState = PlayerStateResponse.builder()
                .temporalState(PlayerTemporalStateDTO.builder()
                        .consecutiveDailyFailures(0)
                        .build())
                .progression(PlayerProgressionDTO.builder()
                        .rank(PlayerRank.F) // Default Rank
                        .build())
                .activeFlags(Collections.emptyList())
                .build();
    }

    // --- Hybrid Trigger Tests (DailyQuestService) ---

    @Test
    void testHybridTrigger_Warning() {
        // Given: 1 Missed Daily, Current Failures = 0
        Quest missedDaily = Quest.builder()
                .deadlineAt(LocalDateTime.now().minusHours(1))
                .category(QuestCategory.SYSTEM_DAILY)
                .build();
        
        when(questRepository.findByPlayerPlayerIdAndState(playerId, QuestState.ACTIVE))
                .thenReturn(List.of(missedDaily));
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);

        // Execute
        dailyQuestService.processPlayerReset(playerId);

        // Verify
        // 1. Update Failures to 1
        verify(playerStateService).updateConsecutiveFailures(playerId, 1);
        // 2. Apply WARNING
        verify(playerStateService).applyStatusFlag(eq(playerId), eq(StatusFlagType.WARNING), any());
        // 3. Do NOT enter Penalty Zone
        verify(penaltyService, never()).enterPenaltyZone(any(), any());
    }

    @Test
    void testHybridTrigger_PenaltyZone() {
        // Given: 1 Missed Daily, Current Failures = 1
        playerState.getTemporalState().setConsecutiveDailyFailures(1);
        
        Quest missedDaily = Quest.builder()
                .deadlineAt(LocalDateTime.now().minusHours(1))
                .category(QuestCategory.SYSTEM_DAILY)
                .build();
        
        when(questRepository.findByPlayerPlayerIdAndState(playerId, QuestState.ACTIVE))
                .thenReturn(List.of(missedDaily));
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);

        // Execute
        dailyQuestService.processPlayerReset(playerId);

        // Verify
        // 1. Update Failures to 2
        verify(playerStateService).updateConsecutiveFailures(playerId, 2);
        // 2. Trigger Penalty Zone
        verify(penaltyService).enterPenaltyZone(eq(playerId), contains("Consecutive"));
    }

    @Test
    void testHybridTrigger_Reset() {
        // Given: NO Missed Dailies, Current Failures = 1
        playerState.getTemporalState().setConsecutiveDailyFailures(1);
        
        // No active dailies returned (or implied completed/not expired)
        // Note: Logic iterates ACTIVE. If list empty, todayFailed = false.
        when(questRepository.findByPlayerPlayerIdAndState(playerId, QuestState.ACTIVE))
                .thenReturn(Collections.emptyList()); 
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);

        // Execute
        dailyQuestService.processPlayerReset(playerId);

        // Verify
        // 1. Reset Failures to 0
        verify(playerStateService).updateConsecutiveFailures(playerId, 0);
        // 2. Remove Warning
        verify(playerStateService).removeStatusFlag(playerId, StatusFlagType.WARNING);
    }

    // --- Penalty Service Tests (Quest Gen & Exit) ---
    // We need to inject Mocks INTO PenaltyService to test it. Mockito @InjectMocks handles one.
    // Let's create a separate test instance for PenaltyService logic.
    
    @Test
    void testPenaltyService_EnterZone() {
        // Setup PenaltyService manually since we used @InjectMocks for DailyQuestService
        QuestRepository qRepo = mock(QuestRepository.class);
        PlayerStateService psService = mock(PlayerStateService.class);
        // PenaltyCalculationService - not needed for enter/exit
        PenaltyService realPenaltyService = new PenaltyService(null, null, psService, qRepo);
        
        when(psService.getPlayerState(playerId)).thenReturn(playerState);

        // Execute
        realPenaltyService.enterPenaltyZone(playerId, "Test");

        // Verify
        // 1. Flag Applied
        verify(psService).applyStatusFlag(eq(playerId), eq(StatusFlagType.PENALTY_ZONE), any());
        // 2. Warning Removed
        verify(psService).removeStatusFlag(playerId, StatusFlagType.WARNING);
        // 3. Quest Generated
        ArgumentCaptor<Quest> questCaptor = ArgumentCaptor.forClass(Quest.class);
        verify(qRepo).save(questCaptor.capture());
        
        Quest generated = questCaptor.getValue();
        assertEquals(QuestType.PENALTY, generated.getQuestType());
        assertEquals("SURVIVAL PROTOCOL", generated.getTitle());
    }

    @Test
    void testPenaltyService_ExitZone() {
        PlayerStateService psService = mock(PlayerStateService.class);
        PenaltyService realPenaltyService = new PenaltyService(null, null, psService, null);

        // Execute
        realPenaltyService.exitPenaltyZone(playerId);

        // Verify
        verify(psService).removeStatusFlag(playerId, StatusFlagType.PENALTY_ZONE);
        verify(psService).updateConsecutiveFailures(playerId, 0);
    }

    // --- Project Integration Test ---
    
    @Test
    void testProjectCreation_Locked() {
        // Setup ProjectService with Mock PlayerStateService
        PlayerStateService psService = mock(PlayerStateService.class);
        ProjectService realProjectService = new ProjectService(null, null, null, null, psService);
        
        // Given Penalty Zone Active
        PlayerStatusFlagDTO penaltyFlag = PlayerStatusFlagDTO.builder().flag(StatusFlagType.PENALTY_ZONE).build();
        PlayerStateResponse penaltyState = PlayerStateResponse.builder()
                .activeFlags(List.of(penaltyFlag))
                .build();
        when(psService.getPlayerState(playerId)).thenReturn(penaltyState);

        Project project = Project.builder()
                .player(PlayerIdentity.builder().playerId(playerId).build())
                .build();

        // Execute & Verify
        assertThrows(IllegalStateException.class, () -> realProjectService.createProject(project));
    }
}
