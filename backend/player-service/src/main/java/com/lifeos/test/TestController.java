package com.lifeos.test;

import com.lifeos.event.concrete.LevelUpEvent;
import com.lifeos.player.domain.PlayerProgression;
import com.lifeos.player.repository.PlayerProgressionRepository;
import com.lifeos.progression.service.JobChangeService;
import com.lifeos.project.domain.Project;
import com.lifeos.project.repository.ProjectRepository;
import com.lifeos.project.service.DungeonBreakDaemon;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Test-only support endpoints for E2E (Playwright) state hydration.
 *
 * Guarded by {@code @Profile("test")} so the beans are only instantiated when the
 * application runs with the "test" Spring profile — never in the production runtime.
 */
@RestController
@RequestMapping("/api/test")
@Profile("test")
@CrossOrigin(origins = "*")
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    /**
     * Player-owned tables in FK-safe (children-before-parents) deletion order. Mirrors the
     * cascade in {@code OnboardingService.failPenalty} but wipes every row (no player filter).
     */
    private static final List<String> WIPE_ORDER = List.of(
            "onboarding_progress",
            "player_profiles",
            "player_progression",
            "player_state",
            "quest_mutation_log",
            "quest_outcome_profile",
            "player_quest_link",
            "quest",
            "penalty_quests",
            "penalty_record",
            "player_journal",
            "player_attribute",
            "player_history",
            "player_metadata",
            "player_metrics",
            "player_psych_state",
            "player_status_flag",
            "player_temporal_state",
            "player_state_snapshot",
            "job_change_quest",
            "rank_exam_attempts",
            "user_boss_keys",
            "dungeon_break_events",
            "project",
            "reward_record",
            "player_streak",
            "system_event",
            "system_messages",
            "purchase_cooldown",
            "purchase_transaction",
            "user_inventory",
            "player_economy",
            "player_identity"
    );

    @PersistenceContext
    private EntityManager entityManager;

    private final ProjectRepository projectRepository;
    private final DungeonBreakDaemon dungeonBreakDaemon;
    private final PlayerProgressionRepository progressionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final JobChangeService jobChangeService;

    public TestController(ProjectRepository projectRepository,
                          DungeonBreakDaemon dungeonBreakDaemon,
                          PlayerProgressionRepository progressionRepository,
                          ApplicationEventPublisher eventPublisher,
                          JobChangeService jobChangeService) {
        this.projectRepository = projectRepository;
        this.dungeonBreakDaemon = dungeonBreakDaemon;
        this.progressionRepository = progressionRepository;
        this.eventPublisher = eventPublisher;
        this.jobChangeService = jobChangeService;
    }

    /** Truncate the 33 player-owned tables to a clean baseline. */
    @PostMapping("/reset-db")
    @Transactional
    public ResponseEntity<Map<String, Object>> resetDb() {
        log.warn("[TEST] Resetting database — wiping {} player-owned tables.", WIPE_ORDER.size());
        int wiped = 0;
        for (String table : WIPE_ORDER) {
            entityManager.createNativeQuery("DELETE FROM " + table).executeUpdate();
            wiped++;
        }
        return ResponseEntity.ok(Map.of("status", "ok", "tablesWiped", wiped));
    }

    /** Force a Dungeon Break for a specific project (30% gold drain + Penalty Zone). */
    @PostMapping("/trigger-dungeon-break")
    public ResponseEntity<Map<String, Object>> triggerDungeonBreak(@RequestBody Map<String, String> body) {
        UUID projectId = UUID.fromString(requireField(body, "projectId"));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: " + projectId));

        log.warn("[TEST] Triggering Dungeon Break for project {}.", projectId);
        dungeonBreakDaemon.triggerDungeonBreak(project);
        return ResponseEntity.ok(Map.of("status", "ok", "projectId", projectId.toString()));
    }

    /** Set a player's level to X and publish a LevelUpEvent so the voice/overlay pipeline fires. */
    @PostMapping("/set-level")
    @Transactional
    public ResponseEntity<Map<String, Object>> setLevel(@RequestBody Map<String, Object> body) {
        UUID playerId = UUID.fromString(String.valueOf(requireObject(body, "playerId")));
        int newLevel = ((Number) requireObject(body, "level")).intValue();

        PlayerProgression progression = progressionRepository.findByPlayerPlayerId(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Progression not found: " + playerId));

        int previousLevel = progression.getLevel();
        progression.setLevel(newLevel);
        progressionRepository.save(progression);

        log.warn("[TEST] Set player {} level {} -> {} and publishing LevelUpEvent.", playerId, previousLevel, newLevel);
        eventPublisher.publishEvent(new LevelUpEvent(playerId, newLevel, previousLevel));

        return ResponseEntity.ok(Map.of("status", "ok", "previousLevel", previousLevel, "newLevel", newLevel));
    }

    /** Force the Job Change state machine to AWAITING_CLASS_SELECTION (skips the 3-day gauntlet). */
    @PostMapping("/complete-gauntlet")
    public ResponseEntity<Map<String, Object>> completeGauntlet(@RequestBody Map<String, String> body) {
        UUID playerId = UUID.fromString(requireField(body, "playerId"));
        log.warn("[TEST] Forcing Job Change to AWAITING_CLASS_SELECTION for player {}.", playerId);
        jobChangeService.completeJobChange(playerId, true);
        return ResponseEntity.ok(Map.of("status", "ok", "playerId", playerId.toString()));
    }

    private static String requireField(Map<String, String> body, String key) {
        String value = body == null ? null : body.get(key);
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required field: " + key);
        }
        return value;
    }

    private static Object requireObject(Map<String, Object> body, String key) {
        Object value = body == null ? null : body.get(key);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required field: " + key);
        }
        return value;
    }
}
