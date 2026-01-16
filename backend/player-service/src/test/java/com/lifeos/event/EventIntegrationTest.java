package com.lifeos.event;

import com.lifeos.event.concrete.QuestCompletedEvent;
import com.lifeos.event.concrete.QuestFailedEvent;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.domain.enums.QuestType;
import com.lifeos.quest.dto.QuestRequest;
import com.lifeos.quest.service.QuestLifecycleService;
import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.domain.enums.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class EventIntegrationTest {

    @Autowired
    private DomainEventPublisher eventPublisher;

    @Autowired
    private QuestLifecycleService questService;

    @Autowired
    private PlayerIdentityRepository playerRepository;

    @MockBean
    private PlayerStateService playerStateService;

    // We can also spy on specific handlers if we want to ensure they are invoked
    // For now, let's verify that the publisher bean verifies the suppression logic via integration if possible
    // Or just simplistic smoke test that context loads and publisher works

    @Test
    void testPublishEvent_ContextLoads() {
        // Setup a real quest so handlers don't fail on "Quest not found"
        String username = "IntegrationTester_" + UUID.randomUUID();
        PlayerIdentity player = playerRepository.save(PlayerIdentity.builder()
                .username(username)
                .build());
        UUID playerId = player.getPlayerId();

        QuestRequest request = QuestRequest.builder()
                .playerId(playerId)
                .title("Integration Task")
                .questType(QuestType.PHYSICAL)
                .difficultyTier(DifficultyTier.C)
                .priority(Priority.NORMAL)
                .successXp(100)
                .build();
        var quest = questService.assignQuest(request);

        // Stub PlayerState for RewardService calculation
        com.lifeos.player.dto.PlayerStateResponse state = com.lifeos.player.dto.PlayerStateResponse.builder()
                .activeFlags(java.util.Collections.emptyList())
                .psychState(com.lifeos.player.dto.PlayerPsychStateDTO.builder()
                        .momentum(50).complacency(0).confidenceBias(50).build())
                .temporalState(com.lifeos.player.dto.PlayerTemporalStateDTO.builder()
                        .activeStreakDays(0).build())
                .build();
        org.mockito.Mockito.when(playerStateService.getPlayerState(playerId)).thenReturn(state);

        // Smoke test: Publish event
        eventPublisher.publish(new QuestCompletedEvent(playerId, quest.getQuestId(), QuestType.PHYSICAL));
    }
}
