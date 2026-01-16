package com.lifeos.quest.service;

import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.dto.PlayerIdentityDTO;
import com.lifeos.player.dto.PlayerProgressionDTO;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.domain.enums.QuestType;
import com.lifeos.quest.repository.PlayerQuestLinkRepository;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.reward.service.RewardService;
import com.lifeos.progression.service.ProgressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatSystemTest {

    @Mock private QuestRepository questRepository;
    @Mock private PlayerQuestLinkRepository linkRepository;
    @Mock private RewardService rewardService;
    @Mock private ProgressionService progressionService;
    @Mock private PlayerStateService playerStateService;
    @Mock private com.lifeos.event.DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private QuestLifecycleServiceImpl questLifecycleService;

    private UUID playerId;
    private PlayerIdentity playerIdentity;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        playerIdentity = PlayerIdentity.builder().playerId(playerId).build();
    }

    @Test
    void testCompleteQuest_IncrementsStr_WhenTagged() {
        Quest strQuest = Quest.builder()
                .questId(UUID.randomUUID())
                .player(playerIdentity)
                .questType(QuestType.CAREER)
                .state(QuestState.ACTIVE)
                .primaryAttribute(AttributeType.STR)
                .build();

        when(questRepository.findById(strQuest.getQuestId())).thenReturn(Optional.of(strQuest));
        when(linkRepository.findByPlayerIdAndQuestId(any(), any())).thenReturn(Optional.empty());

        questLifecycleService.completeQuest(strQuest.getQuestId());

        // Verify Event Published
        verify(domainEventPublisher).publish(any(com.lifeos.event.concrete.QuestCompletedEvent.class));
    }

    @Test
    void testCompleteQuest_Promotion_DoesNotIncrementStats() {
        Quest promoQuest = Quest.builder()
                .questId(UUID.randomUUID())
                .player(playerIdentity)
                .questType(QuestType.PROMOTION_EXAM) // GUARD TRIGGER
                .state(QuestState.ACTIVE)
                .primaryAttribute(AttributeType.STR) // Even if tagged
                .build();

        when(questRepository.findById(promoQuest.getQuestId())).thenReturn(Optional.of(promoQuest));
        when(linkRepository.findByPlayerIdAndQuestId(any(), any())).thenReturn(Optional.empty());

        questLifecycleService.completeQuest(promoQuest.getQuestId());

        // Verify Event Published
        verify(domainEventPublisher).publish(any(com.lifeos.event.concrete.QuestCompletedEvent.class));
    }

    @Test
    void testCompleteQuest_NoAttribute_DoesNotIncrement() {
        Quest normalQuest = Quest.builder()
                .questId(UUID.randomUUID())
                .player(playerIdentity)
                .questType(QuestType.CAREER)
                .state(QuestState.ACTIVE)
                .primaryAttribute(null)
                .build();

        when(questRepository.findById(normalQuest.getQuestId())).thenReturn(Optional.of(normalQuest));
        when(linkRepository.findByPlayerIdAndQuestId(any(), any())).thenReturn(Optional.empty());

        questLifecycleService.completeQuest(normalQuest.getQuestId());

        // Verify Event Published
        verify(domainEventPublisher).publish(any(com.lifeos.event.concrete.QuestCompletedEvent.class));
    }
}
