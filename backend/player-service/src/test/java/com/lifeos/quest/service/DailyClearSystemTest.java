package com.lifeos.quest.service;

import com.lifeos.economy.service.InventoryService;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.*;
import com.lifeos.quest.dto.QuestRequest;
import com.lifeos.quest.repository.QuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-mysql")
@Transactional
public class DailyClearSystemTest {

    @Autowired
    private QuestLifecycleService questService;

    @Autowired
    private PlayerStateService playerStateService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private QuestRepository questRepository;

    @Autowired
    private com.lifeos.player.repository.PlayerIdentityRepository playerIdentityRepository;

    private UUID playerId;

    @BeforeEach
    public void setup() {
        var response = playerStateService.initializePlayer("DCTester_" + UUID.randomUUID().toString().substring(0, 8));
        playerId = response.getIdentity().getPlayerId();
        
        // Ensure player is onboarding completed so dailies evaluate and shop works
        var identity = playerIdentityRepository.findById(playerId).orElseThrow();
        identity.setOnboardingCompleted(true);
        identity.setLastDailyReset(LocalDateTime.now().minusHours(1));
        playerIdentityRepository.save(identity);
    }

    @Test
    public void testDailyClearRewards() {
        // Assign 3 SYSTEM_DAILY quests
        Quest q1 = questService.assignQuest(QuestRequest.builder()
                .playerId(playerId)
                .title("Daily Quest 1")
                .questType(QuestType.DISCIPLINE)
                .category(QuestCategory.SYSTEM_DAILY)
                .difficultyTier(DifficultyTier.E)
                .priority(Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().plusHours(12))
                .build());

        Quest q2 = questService.assignQuest(QuestRequest.builder()
                .playerId(playerId)
                .title("Daily Quest 2")
                .questType(QuestType.PHYSICAL)
                .category(QuestCategory.SYSTEM_DAILY)
                .difficultyTier(DifficultyTier.E)
                .priority(Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().plusHours(12))
                .build());

        Quest q3 = questService.assignQuest(QuestRequest.builder()
                .playerId(playerId)
                .title("Daily Quest 3")
                .questType(QuestType.REFLECTION)
                .category(QuestCategory.SYSTEM_DAILY)
                .difficultyTier(DifficultyTier.E)
                .priority(Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().plusHours(12))
                .build());

        // Apply fatigue debuff to test status recovery
        playerStateService.applyStatusFlag(playerId, StatusFlagType.FATIGUED, LocalDateTime.now().plusHours(5));
        assertTrue(playerStateService.hasActiveFlag(playerId, StatusFlagType.FATIGUED));

        // Initial free stat points
        int initialStatPoints = playerStateService.getPlayerState(playerId).getProgression().getFreeStatPoints();

        // Complete 1
        questService.completeQuest(q1.getQuestId());
        // Complete 2
        questService.completeQuest(q2.getQuestId());
        // Complete 3 (Daily Clear triggers)
        questService.completeQuest(q3.getQuestId());

        // Verify Status Recovery (Fatigue removed)
        assertFalse(playerStateService.hasActiveFlag(playerId, StatusFlagType.FATIGUED));

        // Verify No Free Stat Points added (pure action-based attributes overhaul)
        int currentStatPoints = playerStateService.getPlayerState(playerId).getProgression().getFreeStatPoints();
        assertEquals(initialStatPoints, currentStatPoints);

        // Verify Random Box added to inventory
        assertTrue(inventoryService.hasItem(playerId, "RANDOM_BOX"));
        
        // Verify Daily Clear flag is set
        assertTrue(playerStateService.hasActiveFlag(playerId, StatusFlagType.DAILY_CLEAR_REWARDED));
    }

    @Test
    public void testArchitectTrialReward() {
        Quest trial = questService.assignQuest(QuestRequest.builder()
                .playerId(playerId)
                .title("[HIDDEN] The Architect's Original Trial")
                .questType(QuestType.PHYSICAL)
                .category(QuestCategory.SYSTEM_DAILY)
                .difficultyTier(DifficultyTier.S)
                .priority(Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().plusHours(12))
                .build());

        // Complete it
        questService.completeQuest(trial.getQuestId());

        // Verify Blessed Random Box is granted
        assertTrue(inventoryService.hasItem(playerId, "BLESSED_RANDOM_BOX"));
    }

    @Test
    public void testArchitectTrialNoPenaltyOnFail() {
        Quest trial = questService.assignQuest(QuestRequest.builder()
                .playerId(playerId)
                .title("[HIDDEN] The Architect's Original Trial")
                .questType(QuestType.PHYSICAL)
                .category(QuestCategory.SYSTEM_DAILY)
                .difficultyTier(DifficultyTier.S)
                .priority(Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().plusHours(12))
                .build());

        // Fail it
        assertDoesNotThrow(() -> questService.failQuest(trial.getQuestId()));

        // Exited penalty zone or not entered?
        assertFalse(playerStateService.hasActiveFlag(playerId, StatusFlagType.PENALTY_ZONE));
    }
}
