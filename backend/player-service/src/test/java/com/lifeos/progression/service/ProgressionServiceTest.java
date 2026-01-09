package com.lifeos.progression.service;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.dto.PlayerAttributeDTO;
import com.lifeos.player.dto.PlayerProgressionDTO;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.player.dto.PlayerStatusFlagDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
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
        
        // Setup E -> D Scenario
        // Cap 10, Cost 1 Key, Stats: PE 10.0, DISC 8.0
        
        playerState = PlayerStateResponse.builder()
                .progression(PlayerProgressionDTO.builder()
                        .level(10) // At Cap
                        .rank(PlayerRank.E)
                        .bossKeys(1) // Sufficient
                        .build())
                .attributes(new ArrayList<>())
                .activeFlags(new ArrayList<>())
                .build();
                
        // Add Stats
        playerState.getAttributes().add(PlayerAttributeDTO.builder().attributeType(AttributeType.PHYSICAL_ENERGY).currentValue(10.0).build());
        playerState.getAttributes().add(PlayerAttributeDTO.builder().attributeType(AttributeType.DISCIPLINE).currentValue(8.0).build());
    }

    @Test
    void testCanRequestPromotion_Success() {
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        // when(playerStateService.getEffectiveAttributes(playerId)).thenReturn(null); // Method removed from interface
        
        boolean can = progressionService.canRequestPromotion(playerId);
        
        assertTrue(can, "Should be eligible for promotion");
    }

    @Test
    void testCanRequestPromotion_Fail_NotAtCap() {
        playerState.getProgression().setLevel(9);
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        
        boolean can = progressionService.canRequestPromotion(playerId);
        
        assertFalse(can, "Should fail if not at cap");
    }

    @Test
    void testCanRequestPromotion_Fail_InsufficientKeys() {
        playerState.getProgression().setBossKeys(0);
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        
        boolean can = progressionService.canRequestPromotion(playerId);
        
        assertFalse(can, "Should fail if no keys");
    }
    
    @Test
    void testCanRequestPromotion_Fail_LowStats() {
        playerState.getAttributes().get(1).setCurrentValue(7.0); // Discipline < 8.0
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        
        boolean can = progressionService.canRequestPromotion(playerId);
        
        assertFalse(can, "Should fail if stats low");
    }
    
    @Test
    void testCanRequestPromotion_Fail_PenaltyZone() {
        playerState.getActiveFlags().add(PlayerStatusFlagDTO.builder().flag(StatusFlagType.PENALTY_ZONE).build());
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        
        boolean can = progressionService.canRequestPromotion(playerId);
        
        assertFalse(can, "Should fail if in Penalty Zone");
    }

    @Test
    void testRequestPromotionQuest_ConsumesKeys() {
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        
        progressionService.requestPromotionQuest(playerId);
        
        verify(playerStateService).consumeBossKeys(playerId, 1);
    }

    @Test
    void testProcessPromotionOutcome_Success() {
        progressionService.processPromotionOutcome(playerId, true);
        
        verify(playerStateService).promoteRank(playerId);
    }
}
