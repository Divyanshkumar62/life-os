package com.lifeos.project.service;

import com.lifeos.player.domain.PlayerProgression;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.repository.PlayerProgressionRepository;
import com.lifeos.progression.domain.UserBossKey;
import com.lifeos.progression.repository.UserBossKeyRepository;
import com.lifeos.project.domain.Project;
import com.lifeos.project.domain.enums.ProjectStatus;
import com.lifeos.project.repository.ProjectRepository;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.project.dto.ProjectCreationRequest;
import com.lifeos.project.dto.DungeonResponse;
import com.lifeos.project.domain.enums.ProjectStability;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestCategory;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.domain.enums.QuestType;
import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.domain.enums.Priority;
import com.lifeos.player.domain.enums.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final QuestRepository questRepository;
    private final PlayerProgressionRepository progressionRepository;
    private final UserBossKeyRepository bossKeyRepository;
    private final com.lifeos.player.service.PlayerStateService playerStateService;
    private final DungeonArchitectService dungeonArchitect;
    private final com.lifeos.quest.service.QuestLifecycleService questLifecycleService;
    private final com.lifeos.event.DomainEventPublisher domainEventPublisher;
    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    public ProjectService(ProjectRepository projectRepository, QuestRepository questRepository,
                          PlayerProgressionRepository progressionRepository, UserBossKeyRepository bossKeyRepository,
                          com.lifeos.player.service.PlayerStateService playerStateService,
                          DungeonArchitectService dungeonArchitect,
                          com.lifeos.quest.service.QuestLifecycleService questLifecycleService,
                          com.lifeos.event.DomainEventPublisher domainEventPublisher) {
        this.projectRepository = projectRepository;
        this.questRepository = questRepository;
        this.progressionRepository = progressionRepository;
        this.bossKeyRepository = bossKeyRepository;
        this.playerStateService = playerStateService;
        this.dungeonArchitect = dungeonArchitect;
        this.questLifecycleService = questLifecycleService;
        this.domainEventPublisher = domainEventPublisher;
    }
