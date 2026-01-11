package com.lifeos.penalty.service;

import com.lifeos.penalty.domain.PenaltyRecord;
import com.lifeos.penalty.domain.enums.FailureReason;
import com.lifeos.penalty.domain.enums.PenaltyType;
import com.lifeos.penalty.repository.PenaltyRecordRepository;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.streak.service.StreakService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PenaltyServiceTest {

    @Mock
    private PenaltyRecordRepository penaltyRepository;
    
    @Spy
    private PenaltyCalculationService calculationService; // Use real calculation logic
    
    @Mock
    private PlayerStateService playerStateService;
    
    @Mock private QuestRepository questRepository;
    @Mock private com.lifeos.streak.service.StreakService streakService;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PenaltyService penaltyService;

    private UUID questId;
    private UUID playerId;
    private Quest quest;

    @BeforeEach
    void setUp() {
        questId = UUID.randomUUID();
        playerId = UUID.randomUUID();
        quest = Quest.builder()
                .questId(questId)
                //.playerId(playerId) // Entity has 'player' object, simplistic mock here
                .difficultyTier(DifficultyTier.C)
                .build();
    }

    @Test
    void testApplyPenalty_Idempotency() {
        // Given: Penalty already exists
        when(penaltyRepository.existsByQuestId(questId)).thenReturn(true);

        // When
        penaltyService.applyPenalty(questId, playerId, FailureReason.FAILED);

        // Then
        verify(penaltyRepository, never()).save(any());
        verify(playerStateService, never()).applyXpDeduction(any(), anyLong());
    }

    @Test
    void testApplyPenalty_XpDeduction() {
        // Given
        when(penaltyRepository.existsByQuestId(questId)).thenReturn(false);
        when(questRepository.findById(questId)).thenReturn(Optional.of(quest));
        
        // C Tier -> 15 XP Penalty (Medium Severity usually, but let's check basic XP)
        // With Abandoned -> Low Severity -> Just XP.
        
        // When
        penaltyService.applyPenalty(questId, playerId, FailureReason.ABANDONED);

        // Then
        ArgumentCaptor<PenaltyRecord> captor = ArgumentCaptor.forClass(PenaltyRecord.class);
        verify(penaltyRepository).save(captor.capture());
        
        PenaltyRecord saved = captor.getValue();
        assertEquals(PenaltyType.XP_DEDUCTION, saved.getType());
        assertEquals(15L, saved.getValuePayload().get("xpDeduction"));
        
        verify(playerStateService).applyXpDeduction(eq(playerId), eq(15L));
    }

    @Test
    void testApplyPenalty_CriticalFailure() {
        // Given
        quest.setDifficultyTier(DifficultyTier.RED); // Critical
        when(penaltyRepository.existsByQuestId(questId)).thenReturn(false);
        when(questRepository.findById(questId)).thenReturn(Optional.of(quest));

        // When
        penaltyService.applyPenalty(questId, playerId, FailureReason.FAILED);

        // Then
        // Should trigger Streak Reset + Heavy Debuff + XP
        verify(playerStateService).resetStreak(eq(playerId));
        verify(playerStateService).applyXpDeduction(eq(playerId), eq(100L)); // Red = 100
        verify(playerStateService).applyStatDebuff(eq(playerId), any(), eq(10.0), any());
        
        ArgumentCaptor<PenaltyRecord> captor = ArgumentCaptor.forClass(PenaltyRecord.class);
        verify(penaltyRepository).save(captor.capture());
        assertEquals(PenaltyType.STREAK_RESET, captor.getValue().getType());
    }
}
