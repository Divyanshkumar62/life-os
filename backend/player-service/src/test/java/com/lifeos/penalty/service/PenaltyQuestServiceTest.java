package com.lifeos.penalty.service;

import com.lifeos.penalty.domain.PenaltyQuest;
import com.lifeos.penalty.domain.enums.PenaltyQuestStatus;
import com.lifeos.penalty.domain.enums.PenaltyQuestType;
import com.lifeos.penalty.domain.enums.PenaltyTriggerReason;
import com.lifeos.penalty.domain.enums.WorkSource;
import com.lifeos.penalty.repository.PenaltyQuestRepository;
import com.lifeos.voice.domain.enums.SystemMessageType;
import com.lifeos.voice.event.VoiceSystemEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PenaltyQuestServiceTest {

    @Mock
    private PenaltyQuestRepository questRepository;

    @Mock
    private com.lifeos.event.DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private PenaltyQuestService questService;

    private UUID playerId;
    private PenaltyQuest activeQuest;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        activeQuest = PenaltyQuest.builder()
                .playerId(playerId)
                .type(PenaltyQuestType.SURVIVAL)
                .triggerReason(PenaltyTriggerReason.MISSED_DAYS)
                .requiredCount(10)
                .completedCount(0)
                .todayWorkUnits(0)
                .status(PenaltyQuestStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .lastWorkDate(LocalDate.now())
                .build();
    }

    @Test
    void testGeneratePenaltyQuest_Success() {
        when(questRepository.existsByPlayerIdAndStatus(playerId, PenaltyQuestStatus.ACTIVE)).thenReturn(false);

        questService.generatePenaltyQuest(playerId, PenaltyTriggerReason.MISSED_DAYS);

        ArgumentCaptor<PenaltyQuest> captor = ArgumentCaptor.forClass(PenaltyQuest.class);
        verify(questRepository).save(captor.capture());

        PenaltyQuest saved = captor.getValue();
        assertEquals(playerId, saved.getPlayerId());
        assertEquals(PenaltyQuestType.SURVIVAL, saved.getType());
        assertEquals(PenaltyTriggerReason.MISSED_DAYS, saved.getTriggerReason());
        assertEquals(10, saved.getRequiredCount());
        assertEquals(PenaltyQuestStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void testGeneratePenaltyQuest_Idempotency() {
        when(questRepository.existsByPlayerIdAndStatus(playerId, PenaltyQuestStatus.ACTIVE)).thenReturn(true);

        questService.generatePenaltyQuest(playerId, PenaltyTriggerReason.MISSED_DAYS);

        verify(questRepository, never()).save(any());
    }

    @Test
    void testRecordWork_InvalidSource() {
        questService.recordWork(playerId, 1, WorkSource.SYSTEM_DEFINED_ONLY); // Invalid for work recording via API/Quest

        verify(questRepository, never()).findByPlayerIdAndStatus(any(), any());
        verify(questRepository, never()).save(any());
    }

    @Test
    void testRecordWork_IncreaseProgress_RespectsCap() {
        activeQuest.setTodayWorkUnits(0);
        when(questRepository.findByPlayerIdAndStatus(playerId, PenaltyQuestStatus.ACTIVE)).thenReturn(Optional.of(activeQuest));

        // Submit 2 units
        questService.recordWork(playerId, 2, WorkSource.DAILY_QUEST);
        
        verify(questRepository).save(activeQuest);
        assertEquals(2, activeQuest.getCompletedCount());
        assertEquals(2, activeQuest.getTodayWorkUnits());

        // Submit 2 more (Total 4, Cap is 3)
        questService.recordWork(playerId, 2, WorkSource.DAILY_QUEST);
        
        // Should only add 1 more to reach cap of 3
        assertEquals(3, activeQuest.getCompletedCount());
        assertEquals(3, activeQuest.getTodayWorkUnits());
    }

    @Test
    void testRecordWork_NewDayReset() {
        activeQuest.setTodayWorkUnits(3); // Cap reached yesterday
        activeQuest.setLastWorkDate(LocalDate.now().minusDays(1)); 
        when(questRepository.findByPlayerIdAndStatus(playerId, PenaltyQuestStatus.ACTIVE)).thenReturn(Optional.of(activeQuest));

        questService.recordWork(playerId, 1, WorkSource.DAILY_QUEST);

        assertEquals(1, activeQuest.getCompletedCount()); // 0 + 1 (assuming completed count was 0 initially for test, but actually context matters)
        // Wait, setup has completedCount=0. Simple logic.
        // It resets todayWorkUnits to 0 then adds 1 -> todayWorkUnits=1. OK.
        
        assertEquals(1, activeQuest.getTodayWorkUnits());
        assertEquals(LocalDate.now(), activeQuest.getLastWorkDate());
    }

    @Test
    void testRecordWork_Completion() {
        activeQuest.setRequiredCount(10);
        activeQuest.setCompletedCount(9);
        activeQuest.setTodayWorkUnits(0);
        when(questRepository.findByPlayerIdAndStatus(playerId, PenaltyQuestStatus.ACTIVE)).thenReturn(Optional.of(activeQuest));

        questService.recordWork(playerId, 1, WorkSource.DAILY_QUEST);

        assertEquals(10, activeQuest.getCompletedCount());
        assertEquals(PenaltyQuestStatus.COMPLETED, activeQuest.getStatus());
        assertNotNull(activeQuest.getCompletedAt());
        
        // Handled by Domain Event
        verify(domainEventPublisher).publish(any(com.lifeos.event.concrete.PenaltyQuestCompletedEvent.class));
    }
    
    @Test
    void testGetStatus() {
        when(questRepository.findByPlayerIdAndStatus(playerId, PenaltyQuestStatus.ACTIVE)).thenReturn(Optional.of(activeQuest));
        
        Map<String, Object> status = questService.getPenaltyQuestStatus(playerId);
        
        assertTrue((Boolean) status.get("active"));
        assertEquals("SURVIVAL", status.get("type"));
        assertEquals(0, status.get("completed"));
        assertEquals(10, status.get("required"));
    }
}
