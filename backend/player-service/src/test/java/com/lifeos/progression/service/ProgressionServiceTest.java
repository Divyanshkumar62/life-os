package com.lifeos.progression.service;

import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.dto.PlayerProgressionDTO;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.quest.repository.QuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProgressionServiceTest {

    @Mock private PlayerStateService playerStateService;
    @Mock private PenaltyService penaltyService;
    @Mock private QuestRepository questRepository;

    @InjectMocks
    private ProgressionService progressionService;

    private UUID playerId;
    private PlayerStateResponse playerState;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        
        playerState = PlayerStateResponse.builder()
                .progression(PlayerProgressionDTO.builder()
                        .level(10)
                        .rank(PlayerRank.E) // Cap 10
                        .build())
                .build();
    }

    @Test
    void testCheckRankGate_AtCap() {
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        
        boolean atCap = progressionService.checkRankGate(playerId);
        
        assertTrue(atCap, "Player should be at rank gate (Level 10, Rank E Cap 10)");
    }
    
    @Test
    void testCheckRankGate_BelowCap() {
        playerState.getProgression().setLevel(9);
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        
        boolean atCap = progressionService.checkRankGate(playerId);
        
        assertFalse(atCap, "Player should NOT be at rank gate");
    }

    @Test
    void testRequestPromotionQuest_Success() {
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        
        assertDoesNotThrow(() -> progressionService.requestPromotionQuest(playerId));
    }
    
    @Test
    void testRequestPromotionQuest_Fail_NotAtGate() {
        playerState.getProgression().setLevel(9);
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        
        assertThrows(IllegalStateException.class, () -> progressionService.requestPromotionQuest(playerId));
    }

    @Test
    void testProcessPromotionOutcome_Success() {
        progressionService.processPromotionOutcome(playerId, true);
        
        verify(playerStateService).promoteRank(playerId);
        // Verify no penalty
        verifyNoInteractions(penaltyService);
    }

    @Test
    void testProcessPromotionOutcome_Failure() {
        progressionService.processPromotionOutcome(playerId, false);
        
        // Verify NO promotion
        verify(playerStateService, never()).promoteRank(playerId);
        
        // Verify XP Reset / Freeze handling? 
        // Logic says "XP is NOT reset". 
        // This test confirms we do NOT call any XP reset/deduction methods like applyXpDeduction.
        verify(playerStateService, never()).applyXpDeduction(any(), anyLong()); // Assuming this shouldn't happen.
        
        // Verify Penalty Logic triggered? No, "triggers PENALTY_ZONE flag".
        // Current implementation was TODO. Plan says "Triggers PENALTY_ZONE".
        // Implementation check: 
        // log "Penalty Zone". 
        // We need to confirm that logic executes without error.
    }
}
