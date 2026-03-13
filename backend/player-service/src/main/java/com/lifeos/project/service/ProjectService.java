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
import com.lifeos.project.domain.enums.ProjectStability;
import com.lifeos.quest.dto.QuestRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final QuestRepository questRepository;
    private final PlayerProgressionRepository progressionRepository;
    private final UserBossKeyRepository bossKeyRepository;
    private final com.lifeos.player.service.PlayerStateService playerStateService;
    private final DungeonArchitectService dungeonArchitect;
    private final com.lifeos.quest.service.QuestLifecycleService questLifecycleService;
    private final com.lifeos.event.DomainEventPublisher domainEventPublisher;

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
        // STRICT VALIDATION (GA-Ready)
        

        // 0. Penalty Zone Guard
        var state = playerStateService.getPlayerState(project.getPlayer().getPlayerId());
        boolean isPenaltyActive = state.getActiveFlags().stream()
                .anyMatch(f -> f.getFlag() == com.lifeos.player.domain.enums.StatusFlagType.PENALTY_ZONE);
        
        if (isPenaltyActive) {
            throw new IllegalStateException("Project creation is LOCKED while in Penalty Zone.");
        }

        // 1. Duration >= 7 days
        if (project.getDurationDays() < 7) {
            throw new IllegalArgumentException("Project duration must be at least 7 days");
        }
        
        // 2. Subtasks >= 5
        if (project.getMinSubtasks() < 5) {
            throw new IllegalArgumentException("Project must have at least 5 subtasks");
        }
        
        // 3. Rank requirement matches user rank
        PlayerProgression progression = progressionRepository.findByPlayerPlayerId(project.getPlayer().getPlayerId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
                
        if (project.getRankRequirement() != progression.getRank()) {
            throw new IllegalArgumentException("Project rank requirement does not match player rank");
        }
        
        // 4. Slot availability check (E-Rank = 1 slot)
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
        
        // Set hard deadline based on start date + duration
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
        
        // Check if deadline passed
        if (LocalDateTime.now().isAfter(project.getHardDeadline())) {
            project.setStatus(ProjectStatus.FAILED);
            projectRepository.save(project);
            System.out.println("Project failed: Deadline passed");
            return;
        }
        
        // Count completed subtasks
        long completedSubtasks = questRepository.countByProjectIdAndState(
            project.getProjectId(), 
            com.lifeos.quest.domain.enums.QuestState.COMPLETED
        );
        
        // Completion check: 100% completion required
        if (completedSubtasks < project.getMinSubtasks()) {
            throw new IllegalStateException(
                String.format("Cannot complete project: Only %d/%d subtasks completed", 
                    completedSubtasks, project.getMinSubtasks())
            );
        }
        
        // Check actual duration
        long actualDuration = ChronoUnit.DAYS.between(project.getStartDate(), LocalDateTime.now());
        if (actualDuration < project.getDurationDays()) {
            throw new IllegalStateException(
                String.format("Cannot complete project: Only %d/%d days elapsed", 
                    actualDuration, project.getDurationDays())
            );
        }
        
        // SUCCESS: Update status
        project.setStatus(ProjectStatus.COMPLETED);
        project.setCompletedAt(LocalDateTime.now());
        projectRepository.save(project);
        
        // Award rank-specific Boss Key
        awardRankSpecificBossKey(project);
        
        // Publish Completion Event
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
        
        // No keys awarded for abandoned projects
        System.out.println("Project abandoned for player " + project.getPlayer().getPlayerId());
    }

    @Transactional
    public void updateProgress(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            return; // Ignore updates for non-active projects
        }

        // 1. Update Activity (Prevent Rot)
        project.setLastActivityAt(LocalDateTime.now());
        if (project.getStabilityStatus() != ProjectStability.STABLE) {
             project.setStabilityStatus(ProjectStability.STABLE);
        }
        
        // 2. Check Progression
        long completedSubtasks = questRepository.countByProjectIdAndState(
            projectId, 
            QuestState.COMPLETED
        );
        
        // 3. Auto-Complete if 100%
        if (completedSubtasks >= project.getMinSubtasks()) {
            // Check minimum duration?
            // If they speedrun it in 1 day but duration is 7 days, do we allow it?
            // The dungeon validaton says "Requires > 7 days of effort".
            // If they do it faster, maybe they are just too strong?
            // Solo Leveling Logic: If you kill the boss, the gate closes. Speed is good.
            // But we have a specific check in completeProject:
            // "if (actualDuration < project.getDurationDays())" -> throws Exception.
            
            // We should relax this check or make it a warning? 
            // If it's a real project, you can't "Learn Python" in 1 day if you set it for 14.
            // But if you finish all 15 subtasks, haven't you finished?
            // Let's TRY to complete. If it fails due to duration, it just stays active until duration passes?
            // That's annoying. "You finished all tasks but must wait 6 days".
            // Better: Allow early completion but maybe reward less XP? Or just allow it.
            
            // For now, I will attempt completion. If it throws, we catch it and log, keeping it active.
            try {
                completeProject(projectId);
            } catch (Exception e) {
                // Likely duration constraint.
                // We should perhaps update status to 'PENDING_COMPLETION' or just leave ACTIVE.
                System.out.println("Project " + projectId + " progress 100% but completion rejected: " + e.getMessage());
                projectRepository.save(project); // Ensure lastActivityAt is saved
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
        // 1. Validate Input
        if (request.getGoal() == null || request.getGoal().length() < 10) {
            throw new IllegalArgumentException("Project goal is too vague. Defines specific clear conditions.");
        }

        // 2. Summon the Architect (AI Generation)
        DungeonResponse response = dungeonArchitect.generateDungeon(request.getPlayerId(), request.getGoal(), request.getUserRank());
        
        if (!response.isValid()) {
            throw new IllegalArgumentException(response.getRejectionReason());
        }

        DungeonResponse.DungeonData dungeonData = response.getDungeon();

        // 3. Create Project Entity
        com.lifeos.player.domain.PlayerIdentity player = new com.lifeos.player.domain.PlayerIdentity();
        player.setPlayerId(request.getPlayerId()); // Reference only

        Project project = Project.builder()
                .player(player)
                .title(dungeonData.getTitle())
                .description(dungeonData.getDescription())
                .rankRequirement(PlayerRank.valueOf(dungeonData.getRank())) // Ensure Request sends valid Rank string
                .difficultyTier(determineTierFromRank(dungeonData.getRank()))
                .status(ProjectStatus.ACTIVE)
                .minSubtasks(dungeonData.getFloors().size())
                .durationDays(dungeonData.getEstimatedDurationDays())
                .startDate(LocalDateTime.now())
                .hardDeadline(LocalDateTime.now().plusDays(dungeonData.getEstimatedDurationDays()))
                .bossKeyReward(1) // Always 1
                .lastActivityAt(LocalDateTime.now())
                .stabilityStatus(ProjectStability.STABLE)
                .finalXpMultiplier(1.0)
                .build();

        // Validate generic create rules (slots, penalty zone etc.)
        // We reuse the logic from createProject but need to be careful not to duplicate checks that might fail 
        // because we constructed a partial object.
        // Let's call a private validator or just duplicate the critical checks here.
        validateProjectCreation(project); 

        Project savedProject = projectRepository.save(project);

        // 4. Create Floor Quests (Persistence)
        for (DungeonResponse.DungeonFloor floor : dungeonData.getFloors()) {
            Quest floorQuest = Quest.builder()
                    .player(player)
                    .projectId(savedProject.getProjectId())
                    .title("Floor " + floor.getFloorNum() + ": " + floor.getTitle())
                    .description("Sub-quest for dungeon: " + savedProject.getTitle())
                    .questType(QuestType.DISCIPLINE) // Default type, can be refined based on stat
                    .category(QuestCategory.PROJECT_SUBTASK)
                    .primaryAttribute(AttributeType.valueOf(floor.getStat()))
                    .difficultyTier(project.getRankRequirement().toDifficultyTier()) // Map Rank to Tier
                    .priority(Priority.NORMAL)
                    .state(QuestState.PENDING) // Waiting to be equipped
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
            throw new IllegalStateException("Quest is not in PENDING state (already active or completed)");
        }

        // Activate the quest
        quest.setState(QuestState.ASSIGNED); // Or ASSIGNED? 
        // "Equip" means it's now in the daily list.
        // We rely on QuestLifecycleService to handle "Assigning"? 
        // Or just update state manually here since it's a specific logic.
        // Let's manually set it to ASSIGNED and set dates.
        quest.setAssignedAt(LocalDateTime.now());
        quest.setDeadlineAt(LocalDateTime.now().plusHours(24)); // Daily cycle
        quest.setStartsAt(LocalDateTime.now());
        
        questRepository.save(quest);

        // Update Project Activity
        updateProjectActivity(projectId);
    }
    
    private void updateProjectActivity(UUID projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null) {
            project.setLastActivityAt(LocalDateTime.now());
            // If it was unstable, maybe restore stability?
            // "Lore: Activity stabilizes the dungeon."
            if (project.getStabilityStatus() != ProjectStability.STABLE) {
                 project.setStabilityStatus(ProjectStability.STABLE);
            }
            projectRepository.save(project);
        }
    }

    private int determineTierFromRank(String rank) {
        // Simple mapping
        try {
            return PlayerRank.valueOf(rank).ordinal() + 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private void validateProjectCreation(Project project) {
         // 0. Penalty Zone Guard
        var state = playerStateService.getPlayerState(project.getPlayer().getPlayerId());
        boolean isPenaltyActive = state.getActiveFlags().stream()
                .anyMatch(f -> f.getFlag() == com.lifeos.player.domain.enums.StatusFlagType.PENALTY_ZONE);
        
        if (isPenaltyActive) {
            throw new IllegalStateException("Project creation is LOCKED while in Penalty Zone.");
        }

         // 1. Slot availability check
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
        
        // Find or create UserBossKey entry for the current rank
        UserBossKey bossKey = bossKeyRepository.findByPlayerPlayerIdAndRank(
            project.getPlayer().getPlayerId(), 
            currentRank
        ).orElseGet(() -> UserBossKey.builder()
            .player(project.getPlayer())
            .rank(currentRank)
            .keyCount(0)
            .build());
        
        // Award keys based on project reward (default = 1)
        bossKey.setKeyCount(bossKey.getKeyCount() + project.getBossKeyReward());
        bossKeyRepository.save(bossKey);
        
        System.out.println(String.format("Boss Key awarded: +%d %s keys to player %s", 
            project.getBossKeyReward(), currentRank, project.getPlayer().getPlayerId()));
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

    /**
     * Suspend all active projects for a player (Temporal Stasis during Job Change).
     * Projects cannot be edited or completed while suspended.
     */
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

    /**
     * Resume player projects and extend their deadlines by daysToAdd.
     * Called after Job Change quest completion/failure (Temporal Stasis ends).
     */
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
