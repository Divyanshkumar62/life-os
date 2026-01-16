package com.lifeos.quest.service;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.*;
import com.lifeos.quest.dto.QuestRequest;
import com.lifeos.quest.repository.PlayerQuestLinkRepository;
import com.lifeos.quest.repository.QuestRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuestLifecycleServiceTest {

    @Autowired
    private QuestLifecycleService questService;

    @Autowired
    private PlayerStateService playerStateService;

    @Autowired
    private QuestRepository questRepository;

    @Autowired
    private PlayerQuestLinkRepository linkRepository;

    @org.springframework.boot.test.mock.mockito.SpyBean
    private com.lifeos.event.DomainEventPublisher domainEventPublisher;

    private UUID playerId;

    @BeforeEach
    public void setup() {
        // Create a player for testing
        var response = playerStateService.initializePlayer("QuestTester_" + UUID.randomUUID());
        playerId = response.getIdentity().getPlayerId();
    }

    @Test
    public void testAssignQuest_GoldenPath() {
        QuestRequest request = QuestRequest.builder()
                .playerId(playerId)
                .title("Morning Run")
                .description("Run 5km")
                .questType(QuestType.PHYSICAL)
                .difficultyTier(DifficultyTier.C)
                .priority(Priority.HIGH)
                .deadlineAt(LocalDateTime.now().plusHours(2))
                .successXp(100)
                .build();

        Quest quest = questService.assignQuest(request);

        assertNotNull(quest.getQuestId());
        assertEquals(QuestState.ACTIVE, quest.getState()); // Should auto-activate
        assertEquals(DifficultyTier.C, quest.getDifficultyTier());

        // Verify Link
        var link = linkRepository.findByPlayerIdAndQuestId(playerId, quest.getQuestId()).orElseThrow();
        assertEquals(QuestState.ACTIVE, link.getState());
    }

    @Test
    public void testCompleteQuest_AwardsXp() {
        // Assign
        QuestRequest request = QuestRequest.builder()
                .playerId(playerId)
                .title("Task")
                .questType(QuestType.DISCIPLINE)
                .difficultyTier(DifficultyTier.D)
                .priority(Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().plusHours(1))
                .successXp(200)
                .goldReward(50L)
                .attributeDeltas(Map.of("DISCIPLINE", 1.0))
                .build();
        Quest quest = questService.assignQuest(request);
        UUID questId = quest.getQuestId();

        // Check Initial XP
        long initialXp = playerStateService.getPlayerState(playerId).getProgression().getCurrentXp();

        // Complete
        questService.completeQuest(questId);

        // Verify Quest State
        Quest updatedQuest = questRepository.findById(questId).orElseThrow();
        assertEquals(QuestState.COMPLETED, updatedQuest.getState());
        
        // Verify Link State
        var link = linkRepository.findByPlayerIdAndQuestId(playerId, questId).orElseThrow();
        assertEquals(QuestState.COMPLETED, link.getState());

        // Verify Player XP and Level
        // Level 1 -> 2 requires 100 XP. We added 200.
        // Expectation: Level 2, Current XP 100.
        var progression = playerStateService.getPlayerState(playerId).getProgression();
        assertEquals(2, progression.getLevel(), "Player should have leveled up");
        assertEquals(100, progression.getCurrentXp(), "Should have 100 XP remaining after level up cost");

        // Verify Event
        org.mockito.Mockito.verify(domainEventPublisher).publish(org.mockito.ArgumentMatchers.any(com.lifeos.event.concrete.QuestCompletedEvent.class));
    }

    @Test
    public void testFailQuest_UpdatesState() {
        // Assign
        QuestRequest request = QuestRequest.builder()
                .playerId(playerId)
                .title("Task Fail")
                .questType(QuestType.DISCIPLINE)
                .difficultyTier(DifficultyTier.E)
                .priority(Priority.LOW)
                .deadlineAt(LocalDateTime.now().plusHours(1))
                .build();
        Quest quest = questService.assignQuest(request);

        // Fail
        questService.failQuest(quest.getQuestId());

        // Verify
        Quest failedQuest = questRepository.findById(quest.getQuestId()).orElseThrow();
        assertEquals(QuestState.FAILED, failedQuest.getState());

        // Verify Event
        org.mockito.Mockito.verify(domainEventPublisher).publish(org.mockito.ArgumentMatchers.any(com.lifeos.event.concrete.QuestFailedEvent.class));
    }

    @Test
    public void testExpireQuest_UpdatesState() {
        // Assign
        QuestRequest request = QuestRequest.builder()
                .playerId(playerId)
                .title("Task Expire")
                .questType(QuestType.CAREER)
                .difficultyTier(DifficultyTier.B)
                .priority(Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().plusHours(1)) // In future, but we force expire
                .build();
        Quest quest = questService.assignQuest(request);

        // Expire
        questService.expireQuest(quest.getQuestId());

        // Verify
        Quest expiredQuest = questRepository.findById(quest.getQuestId()).orElseThrow();
        assertEquals(QuestState.EXPIRED, expiredQuest.getState());

        // Verify Event
        org.mockito.Mockito.verify(domainEventPublisher).publish(org.mockito.ArgumentMatchers.any(com.lifeos.event.concrete.QuestExpiredEvent.class));
    }
    
    @Test
    public void testInvariant_CannotCompleteAfterDeadline() {
         QuestRequest request = QuestRequest.builder()
                .playerId(playerId)
                .title("Late Task")
                .questType(QuestType.DISCIPLINE)
                .difficultyTier(DifficultyTier.D)
                .priority(Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().minusMinutes(1)) // In past
                .build();
        Quest quest = questService.assignQuest(request);
        
        // Try Complete
        assertThrows(IllegalStateException.class, () -> {
            questService.completeQuest(quest.getQuestId());
        });
    }

    @Test
    public void testInvariant_RedQuestIsEgoBreaker() {
        QuestRequest request = QuestRequest.builder()
                .playerId(playerId)
                .title("Hard Task")
                .questType(QuestType.EGO_BREAKER)
                .difficultyTier(DifficultyTier.RED)
                .priority(Priority.CRITICAL)
                .build();
        
        Quest quest = questService.assignQuest(request);
        assertTrue(quest.isEgoBreakerFlag(), "RED difficulty must set egoBreakerFlag true");
    }
}
