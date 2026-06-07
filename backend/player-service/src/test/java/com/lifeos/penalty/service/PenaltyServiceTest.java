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
import org.junit.jupiter.api.Disabled;
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
    @Mock private com.lifeos.event.DomainEventPublisher domainEventPublisher;
    @Mock private com.lifeos.penalty.service.PenaltyQuestService penaltyQuestService;
    @Mock private com.lifeos.penalty.repository.PenaltyQuestRepository penaltyQuestRepository;
    @Mock private com.lifeos.system.service.SystemVoiceService systemVoiceService;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;
    @Mock private com.lifeos.economy.service.EconomyService economyService;

    private PenaltyService penaltyService;

    private UUID questId;
    private UUID playerId;
    private Quest quest;

    @BeforeEach
    void setUp() {
        // Create service manually with all mocks
        penaltyService = new PenaltyService(
            penaltyRepository,
            calculationService,
            playerStateService,
            questRepository,
            streakService,
            eventPublisher,
            penaltyQuestService,
            penaltyQuestRepository,
            domainEventPublisher,
            systemVoiceService,
            economyService
        );
        
        questId = UUID.randomUUID();
        playerId = UUID.randomUUID();
        quest = Quest.builder()
                .questId(questId)
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
        
        com.lifeos.player.dto.PlayerStateResponse state = com.lifeos.player.dto.PlayerStateResponse.builder()
                .progression(com.lifeos.player.dto.PlayerProgressionDTO.builder().currentXp(100L).build())
                .activeFlags(java.util.Collections.emptyList())
                .build();
        when(playerStateService.getPlayerState(playerId)).thenReturn(state);
        
        // When
        penaltyService.applyPenalty(questId, playerId, FailureReason.ABANDONED);

        // Then
        ArgumentCaptor<PenaltyRecord> captor = ArgumentCaptor.forClass(PenaltyRecord.class);
        verify(penaltyRepository).save(captor.capture());
        
        PenaltyRecord saved = captor.getValue();
        assertEquals(com.lifeos.penalty.domain.enums.PenaltyType.XP_DEDUCTION, saved.getType());
        
        // Verify Event
        verify(domainEventPublisher).publish(any(com.lifeos.event.concrete.PenaltyAppliedEvent.class));
    }

    @Test
    void testEnterPenaltyZone_EmitsEvent() {
        // Mock PlayerStateResponse with empty activeFlags to prevent NPE
        com.lifeos.player.dto.PlayerStateResponse state = com.lifeos.player.dto.PlayerStateResponse.builder()
                .activeFlags(java.util.Collections.emptyList())
                .build();
        when(playerStateService.getPlayerState(any())).thenReturn(state);
        
        // Stub economy state to prevent NPE on Gold drain
        com.lifeos.economy.domain.PlayerEconomy mockEconomy = com.lifeos.economy.domain.PlayerEconomy.builder()
                .goldBalance(java.math.BigDecimal.valueOf(100L))
                .build();
        when(economyService.getEconomyState(any())).thenReturn(mockEconomy);

        // When
        penaltyService.enterPenaltyZone(playerId, "Test Reason");

        // Then
        verify(penaltyQuestService).generatePenaltyQuest(eq(playerId), any());
        verify(domainEventPublisher).publish(any(com.lifeos.event.concrete.PenaltyZoneEnteredEvent.class));
    }
}
