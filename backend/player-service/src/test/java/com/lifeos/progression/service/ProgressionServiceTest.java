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
import com.lifeos.progression.domain.RankExamAttempt;
import com.lifeos.progression.domain.UserBossKey;
import com.lifeos.progression.domain.enums.ExamStatus;
import com.lifeos.progression.repository.RankExamAttemptRepository;
import com.lifeos.progression.repository.UserBossKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProgressionServiceTest {

    @Mock private PlayerStateService playerStateService;
    @Mock private PenaltyService penaltyService;
    @Mock private QuestRepository questRepository;
    @Mock private UserBossKeyRepository bossKeyRepository;
    @Mock private RankExamAttemptRepository examAttemptRepository;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProgressionService progressionService;

    private UUID playerId;
    private PlayerStateResponse playerState;
    private UserBossKey userBossKey;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        
        // Setup E -> D Scenario
        // Cap 10, Cost 1 Key, Stats: PE 10.0, DISC 8.0
        
        playerState = PlayerStateResponse.builder()
                .progression(PlayerProgressionDTO.builder()
                        .level(10) // At Cap
                        .rank(PlayerRank.E)
                        .xpFrozen(true)
                        .build())
                .attributes(new ArrayList<>())
                .activeFlags(new ArrayList<>())
                .build();
                
        // Add Stats (STR 5.0, INT 5.0 required for E->D)
        playerState.getAttributes().add(PlayerAttributeDTO.builder().attributeType(AttributeType.STR).currentValue(10.0).build());
        playerState.getAttributes().add(PlayerAttributeDTO.builder().attributeType(AttributeType.INT).currentValue(10.0).build());
        
        // Mock UserBossKey
        userBossKey = UserBossKey.builder()
                .rank(PlayerRank.E)
                .keyCount(1)
                .build();
    }

    @Test
    void testCanRequestPromotion_Success() {
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        when(bossKeyRepository.findByPlayerPlayerIdAndRank(playerId, PlayerRank.E))
                .thenReturn(Optional.of(userBossKey));
        
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
        userBossKey.setKeyCount(0);
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        when(bossKeyRepository.findByPlayerPlayerIdAndRank(playerId, PlayerRank.E))
                .thenReturn(Optional.of(userBossKey));
        
        boolean can = progressionService.canRequestPromotion(playerId);
        
        assertFalse(can, "Should fail if no keys");
    }
    
    @Test
    void testCanRequestPromotion_Fail_LowStats() {
        playerState.getAttributes().get(1).setCurrentValue(4.0); // Discipline < 5.0 (Required for E->D)
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        when(bossKeyRepository.findByPlayerPlayerIdAndRank(playerId, PlayerRank.E))
                .thenReturn(Optional.of(userBossKey));
        
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
    void testRequestPromotion_Success() {
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        when(bossKeyRepository.findByPlayerPlayerIdAndRank(playerId, PlayerRank.E))
                .thenReturn(Optional.of(userBossKey));
        
        // Mock save
        when(examAttemptRepository.save(any(RankExamAttempt.class))).thenAnswer(i -> i.getArgument(0));

        RankExamAttempt attempt = progressionService.requestPromotion(playerId);
        
        assertNotNull(attempt);
        assertEquals(ExamStatus.UNLOCKED, attempt.getStatus());
        assertEquals(0, userBossKey.getKeyCount()); // Key consumed
        verify(bossKeyRepository).save(userBossKey);
    }
    
    @Test
    void testProcessPromotionOutcome_Success() {
        UUID attemptId = UUID.randomUUID();
        RankExamAttempt attempt = RankExamAttempt.builder()
                .id(attemptId)
                .player(com.lifeos.player.domain.PlayerIdentity.builder().username("test").build())
                .status(ExamStatus.UNLOCKED)
                .build();
        attempt.getPlayer().setPlayerId(playerId);

        when(examAttemptRepository.findLatestByPlayerId(playerId)).thenReturn(Optional.of(attempt));

        progressionService.processPromotionOutcome(playerId, true);
        
        assertEquals(ExamStatus.PASSED, attempt.getStatus());
        verify(playerStateService).promoteRank(playerId);
    }

    @Test
    void testProcessPromotionOutcome_Failure_NoPenaltyZone() {
        UUID attemptId = UUID.randomUUID();
        RankExamAttempt attempt = RankExamAttempt.builder()
                .id(attemptId)
                .player(com.lifeos.player.domain.PlayerIdentity.builder().username("test").build())
                .status(ExamStatus.UNLOCKED)
                .build();
                
        when(examAttemptRepository.findLatestByPlayerId(playerId)).thenReturn(Optional.of(attempt));
        
        progressionService.processPromotionOutcome(playerId, false);
        
        assertEquals(ExamStatus.FAILED, attempt.getStatus());
        // Verify promoteRank is NOT called
        verify(playerStateService, never()).promoteRank(any());
        // Verify NO penalty flags added (method doesn't even exist in service anymore for this context)
    }
}
