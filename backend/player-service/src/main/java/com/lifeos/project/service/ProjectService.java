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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final QuestRepository questRepository;
    private final PlayerProgressionRepository progressionRepository;
    private final UserBossKeyRepository bossKeyRepository;
    
    @Transactional
    public Project createProject(Project project) {
        // STRICT VALIDATION (GA-Ready)
        
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
}
