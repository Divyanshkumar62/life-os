package com.lifeos.reward.service;

import com.lifeos.player.domain.PlayerPsychState;
import com.lifeos.player.domain.PlayerTemporalState;
import com.lifeos.player.dto.PlayerPsychStateDTO;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.dto.PlayerTemporalStateDTO;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.reward.domain.RewardRecord;
import com.lifeos.reward.repository.RewardRecordRepository;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.QuestOutcomeProfile;
import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.repository.QuestOutcomeProfileRepository;
import com.lifeos.quest.repository.QuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RewardServiceTest {

    @Mock private RewardRecordRepository rewardRepository;
    @Mock private QuestOutcomeProfileRepository outcomeRepository; // Used by CalculationService
    @Mock private PlayerStateService playerStateService;
    @Mock private QuestRepository questRepository;
    @Mock private com.lifeos.economy.service.EconomyService economyService; // Mock Economy
    @Mock private com.lifeos.streak.service.StreakService streakService;
    
    @InjectMocks
    private RewardService rewardService;

    // Use specific instance for calculation service to test logic, or mock it?
    // Let's use real calculation service but injected with mocks?
    // Integration style unit test is better here for logic verification.
    // However, RewardService injects CalculationService. If I want to test logic, I should test CalculationService or Spy it.
    // Let's Spy it.
    // But RewardService needs it injected.
    
    private RewardCalculationService calculationService;

    private UUID questId;
    private UUID playerId;
    private Quest quest;
    private QuestOutcomeProfile outcome;
    private PlayerStateResponse playerState;

    @BeforeEach
    void setUp() {
        questId = UUID.randomUUID();
        playerId = UUID.randomUUID();
        
        calculationService = new RewardCalculationService(outcomeRepository);
        // Update Constructor to include MOCK economyService
        rewardService = new RewardService(rewardRepository, calculationService, playerStateService, questRepository, economyService, streakService);

        quest = Quest.builder()
                .questId(questId)
                .difficultyTier(DifficultyTier.C)
                .build();

        outcome = QuestOutcomeProfile.builder()
                .successXp(100L)
                .goldReward(0L) // Initialize to avoid NPE
                .attributeDeltaJson(Map.of("DISCIPLINE", 1.0))
                .build();
        
        // Mock Player State
        playerState = PlayerStateResponse.builder()
                .psychState(PlayerPsychStateDTO.builder()
                        .momentum(50)
                        .complacency(0)
                        .confidenceBias(50)
                        .build())
                .temporalState(PlayerTemporalStateDTO.builder()
                        .activeStreakDays(0)
                        .build())
                .build();
    }

    @Test
    void testApplyReward_Idempotency() {
        when(rewardRepository.existsByQuestId(questId)).thenReturn(true);
        
        rewardService.applyReward(questId, playerId);
        
        verify(questRepository, never()).findById(any());
        verify(rewardRepository, never()).save(any());
    }

    @Test
    void testApplyReward_Standard() {
        when(rewardRepository.existsByQuestId(questId)).thenReturn(false);
        when(questRepository.findById(questId)).thenReturn(Optional.of(quest));
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        when(outcomeRepository.findByQuestQuestId(questId)).thenReturn(Optional.of(outcome));

        rewardService.applyReward(questId, playerId);

        // Verify Application
        verify(playerStateService).addXp(eq(playerId), eq(100L));
        verify(playerStateService).updateAttribute(eq(playerId), any(), eq(1.0));
        
        // Verify Persistence
        verify(rewardRepository).save(any(RewardRecord.class));
    }

    @Test
    void testElasticity_LowMomentum() {
        // Given Low Momentum
        playerState.getPsychState().setMomentum(10); // < 30
        
        when(rewardRepository.existsByQuestId(questId)).thenReturn(false);
        when(questRepository.findById(questId)).thenReturn(Optional.of(quest));
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        when(outcomeRepository.findByQuestQuestId(questId)).thenReturn(Optional.of(outcome));

        rewardService.applyReward(questId, playerId);
        
        // Expect Bonus XP (+20%) -> 120
        verify(playerStateService).addXp(eq(playerId), eq(120L));
        
        // Expect Bonus Attributes (+10%) -> 1.1
        verify(playerStateService).updateAttribute(eq(playerId), any(), eq(1.1));
    }

    @Test
    void testElasticity_HighComplacency() {
        // High Complacency
        playerState.getPsychState().setComplacency(80); // > 70
        
        when(rewardRepository.existsByQuestId(questId)).thenReturn(false);
        when(questRepository.findById(questId)).thenReturn(Optional.of(quest));
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        when(outcomeRepository.findByQuestQuestId(questId)).thenReturn(Optional.of(outcome));

        rewardService.applyReward(questId, playerId);
        
        // Expect Reduced XP (-30%) -> 70
        verify(playerStateService).addXp(eq(playerId), eq(70L));
    }
    
    @Test
    void testConfidenceCorrection() {
        // HARD (A tier?), High Confidence
        quest.setDifficultyTier(DifficultyTier.A); // Assuming Hard
        playerState.getPsychState().setConfidenceBias(90);
        
        when(rewardRepository.existsByQuestId(questId)).thenReturn(false);
        when(questRepository.findById(questId)).thenReturn(Optional.of(quest));
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        when(outcomeRepository.findByQuestQuestId(questId)).thenReturn(Optional.of(outcome));

        rewardService.applyReward(questId, playerId);
        
        // Verify Correction
        verify(playerStateService).updatePsychMetric(eq(playerId), eq("CONFIDENCE"), eq(-5.0));
    }
    
    @Test
    void testRewardWithGold() {
        // Given outcome has Gold
        outcome.setGoldReward(50L);
        
        when(rewardRepository.existsByQuestId(questId)).thenReturn(false);
        when(questRepository.findById(questId)).thenReturn(Optional.of(quest));
        when(playerStateService.getPlayerState(playerId)).thenReturn(playerState);
        when(outcomeRepository.findByQuestQuestId(questId)).thenReturn(Optional.of(outcome));

        rewardService.applyReward(questId, playerId);
        
        // Verify Economy Interaction
        verify(economyService).addGold(eq(playerId), eq(50L), anyString());
    }
}
