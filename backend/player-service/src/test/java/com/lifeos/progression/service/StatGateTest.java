package com.lifeos.progression.service;

import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.player.domain.PlayerAttribute;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.PlayerProgression;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.progression.domain.RankTransitionTemplate;
import com.lifeos.progression.domain.UserBossKey;
import com.lifeos.progression.repository.RankExamAttemptRepository;
import com.lifeos.progression.repository.UserBossKeyRepository;
import com.lifeos.quest.repository.QuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatGateTest {

    @Mock private PlayerStateService playerStateService;
    @Mock private PenaltyService penaltyService;
    @Mock private QuestRepository questRepository;
    @Mock private UserBossKeyRepository bossKeyRepository;
    @Mock private RankExamAttemptRepository examAttemptRepository;

    @InjectMocks
    private ProgressionService progressionService;

    private UUID playerId;
    private PlayerIdentity identity;
    private PlayerProgression progression;
    private PlayerStateResponse mockResponse;
    private List<PlayerAttribute> attributes;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        identity = PlayerIdentity.builder().playerId(playerId).build();
        progression = PlayerProgression.builder()
                .player(identity)
                // Set level to Cap to pass level gate (E cap is 10)
                .level(10)
                .rank(PlayerRank.E) // Next is D
                .build();
        
        attributes = new ArrayList<>();
        
        // Mock PlayerState service getPlayerState implementation details?
        // Ah, service calls playerStateService.getPlayerState(playerId) which returns DTO
        // BUT checkRankGate calls playerStateService.getPlayerState...
        
        // Wait, ProgressionService.canRequestPromotion logic:
        // 1. checkRankGate -> returns boolean (calls playerStateService)
        // 2. getPlayerState again to get Full State.
        
        // Let's Mock getPlayerState to return a mock response with progression and attributes.
        // It's easier if we mock the Response DTO.
        
        // However, ProgressionService code accesses `state.getAttributes()` which is a List<PlayerAttributeDTO>.
        // BUT ProgressionService line 46: `var state = playerStateService.getPlayerState(playerId);`
        // Then line 63: `state.getAttributes().stream()`
        // The DTO has `List<PlayerAttributeDTO>`.
        // The check logic in ProgressionService uses `PlayerAttributeDTO`.
        
    }

    @Test
    void testCanRequestPromotion_DeniedIfStatsLow() {
        // Mock State: Level 10 (met), Rank E.
        // E->D Requires: STR 5, INT 5.
        // Current Stats: STR 1, INT 1.
        
        // Mock Attributes DTO
        var strAttr = com.lifeos.player.dto.PlayerAttributeDTO.builder()
                .attributeType(AttributeType.STR)
                .currentValue(1.0)
                .build();
        var intAttr = com.lifeos.player.dto.PlayerAttributeDTO.builder()
                .attributeType(AttributeType.INT)
                .currentValue(1.0)
                .build();
                
        var mockProgDto = com.lifeos.player.dto.PlayerProgressionDTO.builder()
                .level(10)
                .rank(PlayerRank.E)
                .build();
                
        // Mock Response
        var stateResponse = com.lifeos.player.dto.PlayerStateResponse.builder()
                .progression(mockProgDto)
                .attributes(List.of(strAttr, intAttr))
                .activeFlags(List.of()) // No Penalty Zone
                .build();
        
        when(playerStateService.getPlayerState(playerId)).thenReturn(stateResponse);
        
        // checkRankGate calls getPlayerState too...
        // Assuming one mock call handles multiple invocations or we stub strictly.
        // Actually checkRankGate uses getPlayerState.
        
        boolean eligible = progressionService.canRequestPromotion(playerId);
        
        assertFalse(eligible, "Should be denied due to low stats");
    }

    @Test
    void testCanRequestPromotion_AllowedIfStatsMet() {
        // Mock State: Level 10 (met), Rank E.
        // E->D Requires: STR 5, INT 5.
        // Current Stats: STR 10, INT 10.
        
        var strAttr = com.lifeos.player.dto.PlayerAttributeDTO.builder()
                .attributeType(AttributeType.STR)
                .currentValue(10.0)
                .build();
        var intAttr = com.lifeos.player.dto.PlayerAttributeDTO.builder()
                .attributeType(AttributeType.INT)
                .currentValue(10.0)
                .build();
                
        var mockProgDto = com.lifeos.player.dto.PlayerProgressionDTO.builder()
                .level(10)
                .rank(PlayerRank.E)
                .build();
                
        var stateResponse = com.lifeos.player.dto.PlayerStateResponse.builder()
                .progression(mockProgDto)
                .attributes(List.of(strAttr, intAttr))
                .activeFlags(List.of())
                .build();
        
        when(playerStateService.getPlayerState(playerId)).thenReturn(stateResponse);
        
        // Mock Keys: Requirement is 1 key
        UserBossKey bossKey = new UserBossKey();
        bossKey.setKeyCount(10);
        when(bossKeyRepository.findByPlayerPlayerIdAndRank(any(), any())).thenReturn(Optional.of(bossKey));

        boolean eligible = progressionService.canRequestPromotion(playerId);
        
        assertTrue(eligible, "Should be allowed when stats and keys are met");
    }
}
