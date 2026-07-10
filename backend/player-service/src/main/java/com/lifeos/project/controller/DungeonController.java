package com.lifeos.project.controller;

import com.lifeos.project.domain.Project;
import com.lifeos.project.dto.DungeonOpenRequest;
import com.lifeos.project.service.ProjectService;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.domain.enums.QuestType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/dungeons")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DungeonController {

    private static final Logger log = LoggerFactory.getLogger(DungeonController.class);

    private final ProjectService projectService;
    private final QuestRepository questRepository;

    private void checkNoActiveIntelQuests(UUID playerId) {
        boolean hasActiveIntelQuest = questRepository.findByPlayerPlayerIdAndState(playerId, QuestState.ACTIVE)
                .stream()
                .anyMatch(q -> q.getQuestType() == QuestType.INTEL_GATHERING);
        
        if (hasActiveIntelQuest) {
            log.warn("Player {} blocked from opening dungeon - active Intel Quest must be completed first", playerId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Complete your Intel Quests before creating a Dungeon");
        }
    }

    @PostMapping("/open")
    public ResponseEntity<Project> openDungeon(@RequestBody DungeonOpenRequest request) {
        log.info("Request to open dungeon: player {}, key {}", request.getPlayerId(), request.getKeyItemCode());
        
        // Enforce Mandatory Completion rule (PRD requirement)
        checkNoActiveIntelQuests(request.getPlayerId());
        
        try {
            Project project = projectService.openDungeon(request);
            return ResponseEntity.ok(project);
        } catch (IllegalStateException e) {
            log.warn("Dungeon opening gate check failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Dungeon opening validation failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/arise")
    public ResponseEntity<Project> ariseDungeon(
            @PathVariable("id") UUID dungeonId,
            @RequestParam UUID playerId) {
        log.info("Request to Arise failed dungeon {} for player {}", dungeonId, playerId);
        try {
            Project project = projectService.ariseDungeon(dungeonId, playerId);
            return ResponseEntity.ok(project);
        } catch (IllegalStateException e) {
            log.warn("Dungeon Arise failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Dungeon Arise validation failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
