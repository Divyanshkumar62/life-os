package com.lifeos.project.controller;

import com.lifeos.project.domain.Project;
import com.lifeos.project.dto.ProjectCreationRequest;
import com.lifeos.project.service.ProjectService;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.domain.enums.QuestType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;
    private final QuestRepository questRepository;

    /**
     * PRD: Enforce Mandatory Completion - Pin Intel Quests; block Dungeon entry if intel_quest_active = true
     */
    private void checkNoActiveIntelQuests(UUID playerId) {
        boolean hasActiveIntelQuest = questRepository.findByPlayerPlayerIdAndState(playerId, QuestState.ACTIVE)
                .stream()
                .anyMatch(q -> q.getQuestType() == QuestType.INTEL_GATHERING);
        
        if (hasActiveIntelQuest) {
            log.warn("Player {} blocked from creating dungeon - active Intel Quest must be completed first", playerId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Complete your Intel Quests before creating a Dungeon");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Project> createDungeon(@RequestBody ProjectCreationRequest request) {
        // Check for active Intel Quests (PRD requirement)
        checkNoActiveIntelQuests(request.getPlayerId());
        
        log.info("Received request to create dungeon for player: {}", request.getPlayerId());
        try {
            Project project = projectService.createDungeonProject(request);
            return ResponseEntity.ok(project);
        } catch (IllegalArgumentException e) {
            log.warn("Dungeon creation validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{projectId}/equip/{questId}")
    public ResponseEntity<Void> equipSubQuest(@PathVariable UUID projectId, @PathVariable UUID questId) {
        log.info("Equipping sub-quest {} for project {}", questId, projectId);
        try {
            projectService.equipSubQuest(projectId, questId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to equip quest", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{projectId}/complete")
    public ResponseEntity<Void> completeProject(@PathVariable UUID projectId) {
        log.info("Attempting manual completion of project {}", projectId);
        try {
            projectService.completeProject(projectId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.warn("Project completion failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{projectId}/abandon")
    public ResponseEntity<Void> abandonProject(@PathVariable UUID projectId) {
        log.info("Abandoning project {}", projectId);
        projectService.abandonProject(projectId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<java.util.List<Project>> getActiveProjects(@RequestParam UUID playerId) {
        // Simple fetch for verification
        // In real app, might want specific DTOs
        // Assuming ProjectRepository is available via Service? Service doesn't expose "findAll"
        // I need to add getProjects to ProjectService or use Repository here?
        // Better to use Service.
        return ResponseEntity.ok(projectService.getPlayerProjects(playerId));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProject(@PathVariable UUID projectId) {
         return projectService.getProject(projectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{projectId}/quests")
    public ResponseEntity<java.util.List<com.lifeos.quest.domain.Quest>> getProjectQuests(@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.getProjectQuests(projectId));
    }
}
