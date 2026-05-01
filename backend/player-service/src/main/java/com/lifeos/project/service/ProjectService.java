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
    
    @Transactional
    public Project createProject(Project project) {
        var state = playerStateService.getPlayerState(project.getPlayer().getPlayerId());
        boolean isPenaltyActive = state.getActiveFlags().stream()
                .anyMatch(f -> f.getFlag() == com.lifeos.player.domain.enums.StatusFlagType.PENALTY_ZONE);
        
        if (isPenaltyActive) {
            throw new IllegalStateException("Project creation is LOCKED while in Penalty Zone.");
        }

        if (project.getDurationDays() < 7) {
            throw new IllegalArgumentException("Project duration must be at least 7 days");
        }
        
        if (project.getMinSubtasks() < 5) {
            throw new IllegalArgumentException("Project must have at least 5 subtasks");
        }
        
        PlayerProgression progression = progressionRepository.findByPlayerPlayerId(project.getPlayer().getPlayerId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
                
        if (project.getRankRequirement() != progression.getRank()) {
            throw new IllegalArgumentException("Project rank requirement does not match player rank");
        }
        
        long activeProjects = projectRepository.countByPlayerPlayerIdAndStatus(
            project.getPlayer().getPlayerId(), 
            ProjectStatus.ACTIVE
        );
        
        int maxSlots = progression.getRank().getProjectSlots();
        if (activeProjects >= maxSlots) {
            throw new IllegalStateException(
                String.format("Maximum project slots (%d) reached for rank %s", maxSlots, progression.getRank())
            );
        }
        
        if (project.getHardDeadline() == null) {
            project.setHardDeadline(project.getStartDate().plusDays(project.getDurationDays()));
        }
        
        return projectRepository.save(project);
    }
    
    @Transactional
    public void completeProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
                
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new IllegalStateException("Project is not active");
        }
        
        if (LocalDateTime.now().isAfter(project.getHardDeadline())) {
            project.setStatus(ProjectStatus.FAILED);
            projectRepository.save(project);
            log.info("Project failed: Deadline passed");
            return;
        }
        
        long completedSubtasks = questRepository.countByProjectIdAndState(
            project.getProjectId(), 
            QuestState.COMPLETED
        );
        
        if (completedSubtasks < project.getMinSubtasks()) {
            throw new IllegalStateException(
                String.format("Cannot complete project: Only %d/%d subtasks completed", 
                    completedSubtasks, project.getMinSubtasks())
            );
        }
        
        long actualDuration = ChronoUnit.DAYS.between(project.getStartDate(), LocalDateTime.now());
        if (actualDuration < project.getDurationDays()) {
            throw new IllegalStateException(
                String.format("Cannot complete project: Only %d/%d days elapsed", 
                    actualDuration, project.getDurationDays())
            );
        }
        
        project.setStatus(ProjectStatus.COMPLETED);
        project.setCompletedAt(LocalDateTime.now());
        projectRepository.save(project);
        
        awardRankSpecificBossKey(project);
        
        domainEventPublisher.publish(new com.lifeos.event.concrete.ProjectCompletedEvent(
            project.getPlayer().getPlayerId(),
            project.getProjectId(),
            project.getBossKeyReward()
        ));
    }
    
    @Transactional
    public void abandonProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
                
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new IllegalStateException("Project is not active");
        }
        
        project.setStatus(ProjectStatus.ABANDONED);
        projectRepository.save(project);
        
        log.info("Project abandoned for player {}", project.getPlayer().getPlayerId());
    }

    @Transactional
    public void updateProgress(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            return;
        }

        project.setLastActivityAt(LocalDateTime.now());
        if (project.getStabilityStatus() != ProjectStability.STABLE) {
             project.setStabilityStatus(ProjectStability.STABLE);
        }
        
        long completedSubtasks = questRepository.countByProjectIdAndState(
            projectId, 
            QuestState.COMPLETED
        );
        
        if (completedSubtasks >= project.getMinSubtasks()) {
            try {
                completeProject(projectId);
            } catch (Exception e) {
                log.info("Project {} progress 100% but completion rejected: {}", projectId, e.getMessage());
                projectRepository.save(project);
            }
        } else {
            projectRepository.save(project);
        }
    }

    public java.util.List<Quest> getProjectQuests(UUID projectId) {
        return questRepository.findByProjectId(projectId);
    }
    
    
    @Transactional
    public Project createDungeonProject(ProjectCreationRequest request) {
        if (request.getGoal() == null || request.getGoal().length() < 10) {
            throw new IllegalArgumentException("Project goal is too vague. Defines specific clear conditions.");
        }

        DungeonResponse response = dungeonArchitect.generateDungeon(request.getPlayerId(), request.getGoal(), request.getUserRank());
        
        if (!response.isValid()) {
            throw new IllegalArgumentException(response.getRejectionReason());
        }

        DungeonResponse.DungeonData dungeonData = response.getDungeon();

        com.lifeos.player.domain.PlayerIdentity player = new com.lifeos.player.domain.PlayerIdentity();
        player.setPlayerId(request.getPlayerId());

        Project project = Project.builder()
                .player(player)
                .title(dungeonData.getTitle())
                .description(dungeonData.getDescription())
                .rankRequirement(PlayerRank.valueOf(dungeonData.getRank()))
                .difficultyTier(determineTierFromRank(dungeonData.getRank()))
                .status(ProjectStatus.ACTIVE)
                .minSubtasks(dungeonData.getFloors().size())
                .durationDays(dungeonData.getEstimatedDurationDays())
                .startDate(LocalDateTime.now())
                .hardDeadline(LocalDateTime.now().plusDays(dungeonData.getEstimatedDurationDays()))
                .bossKeyReward(1)
                .lastActivityAt(LocalDateTime.now())
                .stabilityStatus(ProjectStability.STABLE)
                .finalXpMultiplier(1.0)
                .build();

        validateProjectCreation(project);

        Project savedProject = projectRepository.save(project);

        for (DungeonResponse.DungeonFloor floor : dungeonData.getFloors()) {
            Quest floorQuest = Quest.builder()
                    .player(player)
                    .projectId(savedProject.getProjectId())
                    .title("Floor " + floor.getFloorNum() + ": " + floor.getTitle())
                    .description("Sub-quest for dungeon: " + savedProject.getTitle())
                    .questType(QuestType.DISCIPLINE)
                    .category(QuestCategory.PROJECT_SUBTASK)
                    .primaryAttribute(AttributeType.valueOf(floor.getStat()))
                    .difficultyTier(project.getRankRequirement().toDifficultyTier())
                    .priority(Priority.NORMAL)
                    .state(QuestState.PENDING)
                    .systemMutable(false)
                    .build();
            
            questRepository.save(floorQuest);
        }

        return savedProject;
    }

    @Transactional
    public void equipSubQuest(UUID projectId, UUID questId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found"));

        if (!quest.getProjectId().equals(projectId)) {
            throw new IllegalArgumentException("Quest does not belong to this project");
        }

        if (quest.getState() != QuestState.PENDING) {
            throw new IllegalStateException("Quest is not in PENDING state");
        }

        quest.setState(QuestState.ASSIGNED);
        quest.setAssignedAt(LocalDateTime.now());
        quest.setDeadlineAt(LocalDateTime.now().plusHours(24));
        quest.setStartsAt(LocalDateTime.now());
        
        questRepository.save(quest);
        updateProjectActivity(projectId);
    }
    
    private void updateProjectActivity(UUID projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null) {
            project.setLastActivityAt(LocalDateTime.now());
            if (project.getStabilityStatus() != ProjectStability.STABLE) {
                 project.setStabilityStatus(ProjectStability.STABLE);
            }
            projectRepository.save(project);
        }
    }

    private int determineTierFromRank(String rank) {
        try {
            return PlayerRank.valueOf(rank).ordinal() + 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private void validateProjectCreation(Project project) {
        var state = playerStateService.getPlayerState(project.getPlayer().getPlayerId());
        boolean isPenaltyActive = state.getActiveFlags().stream()
                .anyMatch(f -> f.getFlag() == com.lifeos.player.domain.enums.StatusFlagType.PENALTY_ZONE);
        
        if (isPenaltyActive) {
            throw new IllegalStateException("Project creation is LOCKED while in Penalty Zone.");
        }

        PlayerProgression progression = progressionRepository.findByPlayerPlayerId(project.getPlayer().getPlayerId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        long activeProjects = projectRepository.countByPlayerPlayerIdAndStatus(
            project.getPlayer().getPlayerId(), 
            ProjectStatus.ACTIVE
        );
        
        int maxSlots = progression.getRank().getProjectSlots();
        if (activeProjects >= maxSlots) {
            throw new IllegalStateException(
                String.format("Maximum project slots (%d) reached for rank %s", maxSlots, progression.getRank())
            );
        }
    }

    private void awardRankSpecificBossKey(Project project) {
        PlayerProgression progression = progressionRepository.findByPlayerPlayerId(project.getPlayer().getPlayerId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        
        PlayerRank currentRank = progression.getRank();
        
        UserBossKey bossKey = bossKeyRepository.findByPlayerPlayerIdAndRank(
            project.getPlayer().getPlayerId(), 
            currentRank
        ).orElseGet(() -> UserBossKey.builder()
            .player(project.getPlayer())
            .rank(currentRank)
            .keyCount(0)
            .build());
        
        bossKey.setKeyCount(bossKey.getKeyCount() + project.getBossKeyReward());
        bossKeyRepository.save(bossKey);
        
        log.info("Boss Key awarded: +{} {} keys to player {}", 
            project.getBossKeyReward(), currentRank, project.getPlayer().getPlayerId());
    }

    @Transactional(readOnly = true)
    public java.util.List<Project> getPlayerProjects(UUID playerId) {
        return projectRepository.findAll().stream()
            .filter(p -> p.getPlayer().getPlayerId().equals(playerId))
            .toList();
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Project> getProject(UUID projectId) {
        return projectRepository.findById(projectId);
    }

    @Transactional
    public void suspendPlayerProjects(UUID playerId) {
        var activeProjects = projectRepository.findAll().stream()
                .filter(p -> p.getPlayer().getPlayerId().equals(playerId) && p.getStatus() == ProjectStatus.ACTIVE)
                .toList();
        
        for (Project project : activeProjects) {
            project.setStatus(ProjectStatus.SUSPENDED);
            projectRepository.save(project);
        }
    }

    @Transactional
    public void resumePlayerProjects(UUID playerId, int daysToAdd) {
        var suspendedProjects = projectRepository.findAll().stream()
                .filter(p -> p.getPlayer().getPlayerId().equals(playerId) && p.getStatus() == ProjectStatus.SUSPENDED)
                .toList();
        
        for (Project project : suspendedProjects) {
            project.setStatus(ProjectStatus.ACTIVE);
            project.setHardDeadline(project.getHardDeadline().plusDays(daysToAdd));
            projectRepository.save(project);
        }
    }
}