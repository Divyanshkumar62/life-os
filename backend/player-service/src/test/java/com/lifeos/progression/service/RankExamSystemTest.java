package com.lifeos.progression.service;

import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.PlayerProgression;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.dto.PlayerProgressionDTO;
import com.lifeos.player.dto.PlayerAttributeDTO;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.progression.domain.RankExamAttempt;
import com.lifeos.progression.domain.UserBossKey;
import com.lifeos.progression.domain.enums.ExamStatus;
import com.lifeos.progression.repository.RankExamAttemptRepository;
import com.lifeos.progression.repository.UserBossKeyRepository;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestType;
import com.lifeos.quest.repository.QuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankExamSystemTest {

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

    @BeforeEach
    void setUp() {
         playerId = UUID.randomUUID();
         
         // Mock Player State
         playerState = PlayerStateResponse.builder()
                 .identity(com.lifeos.player.dto.PlayerIdentityDTO.builder().playerId(playerId).build())
                 .progression(PlayerProgressionDTO.builder()
                         .rank(PlayerRank.E) // E -> D
                         .level(10) // Cap is 10 for E
                         .build())
                 .attributes(List.of(
                         PlayerAttributeDTO.builder().attributeType(AttributeType.STR).currentValue(10.0).build(),
                         PlayerAttributeDTO.builder().attributeType(AttributeType.INT).currentValue(10.0).build()
                 ))
                 .activeFlags(Collections.emptyList())
                 .build();
    }

    @Test
    void testRequestPromotion_Success() {
        // Given
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        
        // Mock Keys
        UserBossKey bossKey = UserBossKey.builder()
                .keyCount(3) // Need 3 for E->D (example)
                .rank(PlayerRank.E)
                .build();
        when(bossKeyRepository.findByPlayerPlayerIdAndRank(playerId, PlayerRank.E))
                .thenReturn(Optional.of(bossKey));
        
        // Mock Saving
        when(examAttemptRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Execute
        RankExamAttempt attempt = progressionService.requestPromotion(playerId);

        // Verify
        // 1. Keys Consumed (E->D cost is 1. Start 3 -> End 2)
        assertEquals(2, bossKey.getKeyCount()); 
        
        // 2. Exam Created
        assertEquals(ExamStatus.UNLOCKED, attempt.getStatus());
        
        // 3. Quest Spawned
        ArgumentCaptor<Quest> questCaptor = ArgumentCaptor.forClass(Quest.class);
        verify(questRepository).save(questCaptor.capture());
        assertEquals(QuestType.PROMOTION_EXAM, questCaptor.getValue().getQuestType());
    }

    @Test
    void testProcessOutCome_Success() {
        // Given
        RankExamAttempt attempt = RankExamAttempt.builder()
                .player(PlayerIdentity.builder().playerId(playerId).build())
                .fromRank(PlayerRank.E)
                .toRank(PlayerRank.D)
                .status(ExamStatus.UNLOCKED)
                .build();
        
        when(examAttemptRepository.findLatestByPlayerId(playerId)).thenReturn(Optional.of(attempt));

        // Execute
        progressionService.processPromotionOutcome(playerId, true);

        // Verify
        // 1. Exam Passed
        assertEquals(ExamStatus.PASSED, attempt.getStatus());
        // 2. Player Promoted
        verify(playerStateService).promoteRank(playerId);
        // 3. No Penalty
        verify(penaltyService, never()).enterPenaltyZone(any(), any());
    }

    @Test
    void testProcessOutcome_Failure_StrictPenalty() {
        // Given
        RankExamAttempt attempt = RankExamAttempt.builder()
                .player(PlayerIdentity.builder().playerId(playerId).build())
                .fromRank(PlayerRank.E)
                .toRank(PlayerRank.D)
                .status(ExamStatus.UNLOCKED)
                .build();
        
        when(examAttemptRepository.findLatestByPlayerId(playerId)).thenReturn(Optional.of(attempt));

        // Execute
        progressionService.processPromotionOutcome(playerId, false);

        // Verify
        // 1. Exam Failed
        assertEquals(ExamStatus.FAILED, attempt.getStatus());
        // 2. Penalty Zone Triggered (CRITICAL CHECK)
        verify(penaltyService).enterPenaltyZone(eq(playerId), contains("Failed Rank Exam"));
        // 3. Rank NOT promoted
        verify(playerStateService, never()).promoteRank(any());
    }
}
