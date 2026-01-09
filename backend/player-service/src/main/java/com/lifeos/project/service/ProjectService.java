package com.lifeos.project.service;

import com.lifeos.player.domain.PlayerProgression;
import com.lifeos.player.repository.PlayerProgressionRepository;
import com.lifeos.project.domain.Project;
import com.lifeos.project.domain.enums.ProjectStatus;
import com.lifeos.project.repository.ProjectRepository;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.reward.domain.enums.RewardComponentType;
import com.lifeos.reward.service.RewardService;
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
    private final QuestRepository questRepository; // To check subtasks
    private final PlayerProgressionRepository progressionRepository;
    
    // Project Creation (Simplified for v1)
    @Transactional
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }
    
    @Transactional
    public void completeProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
                
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new IllegalStateException("Project is not active");
        }
        
        project.setStatus(ProjectStatus.COMPLETED);
        project.setCompletedAt(LocalDateTime.now());
        projectRepository.save(project);
        
        // Award Boss Key Logic
        checkAndAwardBossKey(project);
    }
    
    private void checkAndAwardBossKey(Project project) {
        // 1. Check Duration
        long actualDuration = ChronoUnit.DAYS.between(project.getCreatedAt(), project.getCompletedAt());
        // v1 Rule: Duration >= 3 days (or project.durationDays requirement?)
        // Plan says: "If Project.status == COMPLETED AND subtasks >= min AND duration >= threshold"
        // Let's use the field `durationDays` as the REQUIRED threshold set at creation.
        // And `minSubtasks` as the REQUIRED count.
        
        // Count completed subtasks
        long completedSubtasks = questRepository.countByProjectIdAndState(project.getProjectId(), com.lifeos.quest.domain.enums.QuestState.COMPLETED);
        
        if (completedSubtasks >= project.getMinSubtasks() && actualDuration >= project.getDurationDays()) {
            // Award Key
            awardBossKey(project.getPlayer().getPlayerId());
        }
    }
    
    private void awardBossKey(UUID playerId) {
        PlayerProgression progression = progressionRepository.findByPlayerPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        
        progression.setBossKeys(progression.getBossKeys() + 1);
        progressionRepository.save(progression);
        
        // TODO: Record this as a RewardRecord? 
        // Plan says: "Awards BOSS_KEY via RewardService (or direct Progression update if simple)."
        // Direct update is simpler for now as it's not tied to a single Quest completion but a Project.
        // Unless we create a dummy "Project Completion Quest"? 
        // For v1, direct update is fine.
        System.out.println("Boss Key Awarded to player " + playerId);
    }
}
