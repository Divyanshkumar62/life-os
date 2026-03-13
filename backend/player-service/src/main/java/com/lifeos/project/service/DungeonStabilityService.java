package com.lifeos.project.service;

import com.lifeos.project.domain.Project;
import com.lifeos.project.domain.enums.ProjectStability;
import com.lifeos.project.domain.enums.ProjectStatus;
import com.lifeos.project.repository.ProjectRepository;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.Priority;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DungeonStabilityService {

    private static final Logger log = LoggerFactory.getLogger(DungeonStabilityService.class);

    private final ProjectRepository projectRepository;
    private final QuestRepository questRepository;
    // private final NotificationService notificationService; // If exists

    @Scheduled(cron = "0 0 4 * * *") // Daily at 4 AM
    @Transactional
    public void performDailyStabilityChecks() {
        log.info("Starting Daily Dungeon Stability Check...");
        List<Project> activeProjects = projectRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProjectStatus.ACTIVE)
                .collect(Collectors.toList());

        for (Project project : activeProjects) {
            checkProjectStability(project);
        }
    }

    // Public method for manual/lazy checks
    @Transactional
    public void checkAllProjectsStability(UUID playerId) {
        List<Project> activeProjects = projectRepository.findAll().stream() // Ideally findByPlayerAndStatus
                .filter(p -> p.getPlayer().getPlayerId().equals(playerId) && p.getStatus() == ProjectStatus.ACTIVE)
                .collect(Collectors.toList());
        
        for (Project project : activeProjects) {
            checkProjectStability(project);
        }
    }

    private void checkProjectStability(Project project) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastActive = project.getLastActivityAt();
        
        if (lastActive == null) {
            lastActive = project.getCreatedAt();
        }

        long daysInactive = java.time.temporal.ChronoUnit.DAYS.between(lastActive, now);

        // Rule 1: Warning (3 Days)
        if (daysInactive >= 3 && daysInactive < 4) {
            if (project.getStabilityStatus() != ProjectStability.UNSTABLE) {
                project.setStabilityStatus(ProjectStability.UNSTABLE);
                projectRepository.save(project);
                log.warn("Dungeon Tremor detected: Project {} is UNSTABLE.", project.getTitle());
                // notificationService.send("Tremors detected in " + project.getTitle());
            }
        }
        // Rule 2: Invasion (4+ Days)
        else if (daysInactive >= 4) {
            if (project.getStabilityStatus() != ProjectStability.BROKEN) {
                triggerDungeonBreak(project);
            }
        }
    }

    private void triggerDungeonBreak(Project project) {
        log.warn("DUNGEON BREAK! Monsters invading from: {}", project.getTitle());
        
        project.setStabilityStatus(ProjectStability.BROKEN);
        // Reduce Final XP Multiplier
        project.setFinalXpMultiplier(Math.max(0.1, project.getFinalXpMultiplier() - 0.1));
        projectRepository.save(project);

        // Force Equip 2 Random Pending Sub-Quests
        List<Quest> pendingQuests = questRepository.findByProjectId(project.getProjectId()).stream()
                .filter(q -> q.getState() == QuestState.PENDING)
                .collect(Collectors.toList());
        
        if (pendingQuests.isEmpty()) {
            log.info("No monsters left to invade (No pending quests).");
            return;
        }

        Collections.shuffle(pendingQuests);
        List<Quest> invasionForce = pendingQuests.stream().limit(2).collect(Collectors.toList());

        for (Quest quest : invasionForce) {
            // Force Activate
            quest.setState(QuestState.ASSIGNED); // active context
            quest.setAssignedAt(LocalDateTime.now());
            quest.setStartsAt(LocalDateTime.now());
            quest.setDeadlineAt(LocalDateTime.now().plusHours(24)); // Tight deadline
            quest.setPriority(Priority.CRITICAL);
            quest.setSystemMutable(false); // Locked
            
            // Apply Double Penalty Flag?
            // "Double Penalty" logic might need to be checked in PenaltyService upon failure.
            // Or we can encode it in description or a specific metadata field.
            // For V1, we just make it CRITICAL and assume PenaltyService handles CRITICAL harshly.
            // (PenaltyService usually checks Difficulty, Priority isn't effectively used for XP penalty yet).
            // Let's rely on Priority.CRITICAL for UI styling.
            
            quest.setDescription("[INVASION] " + quest.getDescription());
            
            questRepository.save(quest);
        }

        // notificationService.send("DUNGEON BREAK! Monsters have escaped from " + project.getTitle());
    }
}
