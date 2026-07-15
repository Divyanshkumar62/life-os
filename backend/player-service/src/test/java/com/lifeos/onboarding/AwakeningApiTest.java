package com.lifeos.onboarding;

import com.lifeos.ai.service.AIQuestService;
import com.lifeos.onboarding.domain.OnboardingProgress;
import com.lifeos.onboarding.domain.OnboardingStage;
import com.lifeos.onboarding.repository.OnboardingProgressRepository;
import com.lifeos.onboarding.service.IntelQuestGenerator;
import com.lifeos.onboarding.service.TrialQuestGenerator;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.dto.QuestRequest;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.quest.service.QuestLifecycleService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.mockito.BDDMockito;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;

/**
 * REST Assured + JUnit 5 API contract suite for the Phase 1 Awakening Penalty module.
 *
 * <p><b>Enforcement mode: Option B (STRICT PRD CONTRACT).</b> These tests assert the
 * behaviour the PRD/architecture intends, NOT the behaviour the current backend exhibits.
 * They are therefore expected to <b>FAIL LOUDLY</b> against the current implementation
 * wherever the code diverges from the contract. Each such assertion is tagged
 * {@code DIVERGENCE} in its display name and documents the current (flawed) behaviour.</p>
 *
 * <p>Contract enforced (per approved Test Plan + directive):</p>
 * <ul>
 *   <li>Nonexistent player id  -> <b>404 Not Found</b>   (current: 500 via GlobalExceptionHandler)</li>
 *   <li>Malformed UUID          -> <b>400 Bad Request</b> (current: 500, catch-all swallows type mismatch)</li>
 *   <li>Wrong OnboardingStage   -> <b>409 Conflict</b>    (current: 500 or silent success, no stage guard)</li>
 *   <li>/penalty/complete before the 1-hour deadline -> <b>409 Conflict</b> (current: 200, no timer check)</li>
 * </ul>
 *
 * <p>The suite boots the full application context on H2 (profile {@code awakening-test})
 * and drives the live {@link com.lifeos.onboarding.controller.OnboardingController} over HTTP.
 * All LLM-backed collaborators are replaced with {@link MockBean} so no live Gemini/Grok
 * calls are made.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("awakening-test")
@DisplayName("Phase 1 Awakening Penalty API — strict PRD contract (Option B)")
class AwakeningApiTest {

    /**
     * The production {@code failPenalty()} performs ~33 native cross-table DELETE
     * statements. Most of those tables are NOT part of the H2 mapped schema, so the
     * wipe path cannot commit cleanly on H2 (a missing-table error marks the JPA
     * transaction rollback-only). Tests that actually reach the wipe are therefore
     * guarded by this flag so they do not report FALSE environmental failures.
     *
     * Run them against a MySQL-parity schema with: {@code -Dawakening.mysql.parity=true}
     */
    private static final boolean MYSQL_PARITY =
            Boolean.getBoolean("awakening.mysql.parity");

    private static final String BASE = "/api/onboarding";

    @LocalServerPort
    private int port;

    @Autowired private PlayerIdentityRepository playerIdentityRepository;
    @Autowired private OnboardingProgressRepository onboardingRepository;
    @Autowired private QuestRepository questRepository;
    @Autowired private PlatformTransactionManager txManager;

    /** Seeds rows in a single committed transaction so @MapsId associations stay managed. */
    private TransactionTemplate txTemplate;

    // --- LLM-backed collaborators: mocked so NO live model calls occur ---
    @MockBean private AIQuestService aiQuestService;
    @MockBean private TrialQuestGenerator trialQuestGenerator;
    @MockBean private IntelQuestGenerator intelQuestGenerator;
    @MockBean private QuestLifecycleService questService;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        txTemplate = new TransactionTemplate(txManager);

        // Clean slate — children (FK -> player_identity) first.
        onboardingRepository.deleteAll();
        questRepository.deleteAll();
        playerIdentityRepository.deleteAll();

        // Bounded stubs reflecting the real collaborator contracts.
        BDDMockito.given(aiQuestService.generateAwakeningPenalty(any())).willReturn(sampleQuest());
        BDDMockito.given(aiQuestService.generateQuests(any(), anyInt())).willReturn(List.of(sampleQuest()));
        BDDMockito.given(trialQuestGenerator.generateTrialQuest(any())).willReturn(sampleQuest());
        BDDMockito.given(intelQuestGenerator.generateFirstIntelQuest(any())).willReturn(sampleQuest());
        BDDMockito.given(intelQuestGenerator.generateMentalAnalysisQuest(any())).willReturn(sampleQuest());

        // assignQuest returns a Quest; a Mockito stub keeps getQuestId() null-safe.
        BDDMockito.given(questService.assignQuest(any())).willReturn(mock(Quest.class));
    }

    // =====================================================================
    // Endpoint 1 — POST /{playerId}/trial/fail  -> AwakeningPenaltyDTO
    // =====================================================================

    @Test
    @DisplayName("TC-101 HP: trial/fail in TRIAL_QUEST returns 200 + AwakeningPenaltyDTO")
    void tc101_failTrial_happyPath() {
        UUID pid = seedPlayer(OnboardingStage.TRIAL_QUEST);

        given()
            .pathParam("pid", pid)
        .when()
            .post(BASE + "/{pid}/trial/fail")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("playerId", equalTo(pid.toString()))
            .body("stage", equalTo("AWAKENING_PENALTY"))
            .body("penaltyQuestId", notNullValue())
            .body("taskTitle", notNullValue())
            .body("taskDescription", notNullValue())
            .body("deadlineAt", notNullValue());

        // State assertion: persisted stage transitioned and 1-hour deadline was set.
        OnboardingProgress after = onboardingRepository.findById(pid).orElseThrow();
        assertEquals(OnboardingStage.AWAKENING_PENALTY, after.getCurrentStage());
        assertNotNull(after.getPenaltyDeadlineAt());
    }

    @Test
    @DisplayName("TC-103 DIVERGENCE: trial/fail while ALREADY in AWAKENING_PENALTY must be 409 (current: 200, resets 1h timer)")
    void tc103_failTrial_reEntry_shouldConflict() {
        UUID pid = seedPenalty(OnboardingStage.AWAKENING_PENALTY, LocalDateTime.now().plusMinutes(30));

        given()
            .pathParam("pid", pid)
        .when()
            .post(BASE + "/{pid}/trial/fail")
        .then()
            // Contract: re-entering the penalty must be rejected (prevents timer-gaming).
            .statusCode(409);
    }

    @Test
    @DisplayName("TC-104 DIVERGENCE: trial/fail from QUESTIONNAIRE stage must be 409 (current: 200, no stage guard)")
    void tc104_failTrial_wrongStage_shouldConflict() {
        UUID pid = seedPlayer(OnboardingStage.QUESTIONNAIRE);

        given()
            .pathParam("pid", pid)
        .when()
            .post(BASE + "/{pid}/trial/fail")
        .then()
            .statusCode(409);
    }

    @Test
    @DisplayName("TC-105 Resilience: AI penalty generation failure surfaces 5xx, not a 200 with empty task")
    void tc105_failTrial_aiFailure() {
        UUID pid = seedPlayer(OnboardingStage.TRIAL_QUEST);
        BDDMockito.given(aiQuestService.generateAwakeningPenalty(any()))
                .willThrow(new RuntimeException("LLM upstream unavailable"));

        given()
            .pathParam("pid", pid)
        .when()
            .post(BASE + "/{pid}/trial/fail")
        .then()
            // Current behaviour: 500 (AI call is unwrapped). Documented resilience gap.
            .statusCode(500);
    }

    @Test
    @DisplayName("TC-106 DIVERGENCE: trial/fail for nonexistent player must be 404 (current: 500)")
    void tc106_failTrial_nonexistentPlayer_shouldBeNotFound() {
        given()
            .pathParam("pid", UUID.randomUUID())
        .when()
            .post(BASE + "/{pid}/trial/fail")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("TC-107 DIVERGENCE: trial/fail with malformed UUID must be 400 (current: 500)")
    void tc107_failTrial_malformedUuid_shouldBeBadRequest() {
        given()
        .when()
            .post(BASE + "/not-a-valid-uuid/trial/fail")
        .then()
            .statusCode(400);
    }

    // =====================================================================
    // Endpoint 2 — GET /{playerId}/penalty/status  -> AwakeningPenaltyDTO
    // =====================================================================

    @Test
    @DisplayName("TC-201 HP: penalty/status in AWAKENING_PENALTY returns 200 + persisted task")
    void tc201_penaltyStatus_happyPath() {
        UUID pid = seedPenalty(OnboardingStage.AWAKENING_PENALTY, LocalDateTime.now().plusMinutes(45));

        given()
            .pathParam("pid", pid)
        .when()
            .get(BASE + "/{pid}/penalty/status")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("playerId", equalTo(pid.toString()))
            .body("stage", equalTo("AWAKENING_PENALTY"))
            .body("penaltyQuestId", notNullValue())
            .body("taskTitle", notNullValue())
            .body("deadlineAt", notNullValue());
    }

    @Test
    @DisplayName("TC-203 DIVERGENCE: penalty/status when NOT in penalty stage must be 409 (current: 500)")
    void tc203_penaltyStatus_wrongStage_shouldConflict() {
        UUID pid = seedPlayer(OnboardingStage.TRIAL_QUEST);

        given()
            .pathParam("pid", pid)
        .when()
            .get(BASE + "/{pid}/penalty/status")
        .then()
            .statusCode(409);
    }

    @Test
    @DisplayName("TC-204 DIVERGENCE: penalty/status for nonexistent player must be 404 (current: 500)")
    void tc204_penaltyStatus_nonexistentPlayer_shouldBeNotFound() {
        given()
            .pathParam("pid", UUID.randomUUID())
        .when()
            .get(BASE + "/{pid}/penalty/status")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("TC-205 DIVERGENCE: penalty/status with malformed UUID must be 400 (current: 500)")
    void tc205_penaltyStatus_malformedUuid_shouldBeBadRequest() {
        given()
        .when()
            .get(BASE + "/not-a-valid-uuid/penalty/status")
        .then()
            .statusCode(400);
    }

    // =====================================================================
    // Endpoint 3 — POST /{playerId}/penalty/complete -> AwakeningPenaltyResultDTO
    // =====================================================================

    @Test
    @DisplayName("TC-301 HP: penalty/complete AFTER the 1h deadline returns 200, cleared=true, resets trial")
    void tc301_completePenalty_happyPath() {
        // Penalty was endured: deadline is in the past.
        UUID pid = seedPenalty(OnboardingStage.AWAKENING_PENALTY, LocalDateTime.now().minusMinutes(5));

        given()
            .pathParam("pid", pid)
        .when()
            .post(BASE + "/{pid}/penalty/complete")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("playerId", equalTo(pid.toString()))
            .body("cleared", is(true))
            .body("accountDeleted", is(false))
            .body("trialResetDate", notNullValue());

        OnboardingProgress after = onboardingRepository.findById(pid).orElseThrow();
        assertEquals(OnboardingStage.TRIAL_QUEST, after.getCurrentStage());
    }

    @Test
    @DisplayName("TC-303 DIVERGENCE: penalty/complete BEFORE the 1h deadline must be 409 (current: 200, no timer check)")
    void tc303_completePenalty_beforeTimer_shouldConflict() {
        // Deadline still ~59 minutes away: the penalty has NOT been served.
        UUID pid = seedPenalty(OnboardingStage.AWAKENING_PENALTY, LocalDateTime.now().plusMinutes(59));

        given()
            .pathParam("pid", pid)
        .when()
            .post(BASE + "/{pid}/penalty/complete")
        .then()
            // Contract: the 1-hour physical penalty must be endured before completion.
            .statusCode(409);

        // And the player must remain locked in the penalty stage.
        OnboardingProgress after = onboardingRepository.findById(pid).orElseThrow();
        assertEquals(OnboardingStage.AWAKENING_PENALTY, after.getCurrentStage());
    }

    @Test
    @DisplayName("TC-304 DIVERGENCE: penalty/complete when NOT in penalty stage must be 409 (current: 200, destructive)")
    void tc304_completePenalty_wrongStage_shouldConflict() {
        UUID pid = seedPlayer(OnboardingStage.TRIAL_QUEST);

        given()
            .pathParam("pid", pid)
        .when()
            .post(BASE + "/{pid}/penalty/complete")
        .then()
            .statusCode(409);
    }

    @Test
    @DisplayName("TC-306 DIVERGENCE: penalty/complete for nonexistent player must be 404 (current: 500)")
    void tc306_completePenalty_nonexistentPlayer_shouldBeNotFound() {
        given()
            .pathParam("pid", UUID.randomUUID())
        .when()
            .post(BASE + "/{pid}/penalty/complete")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("TC-307 DIVERGENCE: penalty/complete with malformed UUID must be 400 (current: 500)")
    void tc307_completePenalty_malformedUuid_shouldBeBadRequest() {
        given()
        .when()
            .post(BASE + "/not-a-valid-uuid/penalty/complete")
        .then()
            .statusCode(400);
    }

    // =====================================================================
    // Endpoint 4 — POST /{playerId}/penalty/fail -> AwakeningPenaltyResultDTO
    // (Destructive: exercises the native multi-table account wipe.)
    // =====================================================================

    @Test
    @DisplayName("TC-401 HP: penalty/fail in AWAKENING_PENALTY returns 200, accountDeleted=true [MySQL parity]")
    void tc401_failPenalty_happyPath() {
        Assumptions.assumeTrue(MYSQL_PARITY,
                "Requires MySQL-parity schema (-Dawakening.mysql.parity=true): the wipe issues native DELETEs across ~33 tables absent from the H2 mapped schema.");
        UUID pid = seedPenalty(OnboardingStage.AWAKENING_PENALTY, LocalDateTime.now().plusMinutes(10));

        given()
            .pathParam("pid", pid)
        .when()
            .post(BASE + "/{pid}/penalty/fail")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("playerId", equalTo(pid.toString()))
            .body("cleared", is(false))
            .body("accountDeleted", is(true))
            .body("trialResetDate", org.hamcrest.Matchers.nullValue());

        // Account must be gone.
        assertEquals(false, playerIdentityRepository.findById(pid).isPresent());
    }

    @Test
    @DisplayName("TC-403 DIVERGENCE: penalty/fail from a non-penalty stage must be 409 (current: 200, irreversible wipe) [MySQL parity]")
    void tc403_failPenalty_wrongStage_shouldConflict() {
        Assumptions.assumeTrue(MYSQL_PARITY,
                "Requires MySQL-parity schema; the endpoint reaches the destructive wipe path.");
        UUID pid = seedPlayer(OnboardingStage.TRIAL_QUEST);

        given()
            .pathParam("pid", pid)
        .when()
            .post(BASE + "/{pid}/penalty/fail")
        .then()
            // Contract: an account wipe must never be reachable outside the penalty flow.
            .statusCode(409);

        // The player must still exist.
        assertEquals(true, playerIdentityRepository.findById(pid).isPresent());
    }

    @Test
    @DisplayName("TC-404 DIVERGENCE: penalty/fail for nonexistent player must be 404 (current: 200, silent success) [MySQL parity]")
    void tc404_failPenalty_nonexistentPlayer_shouldBeNotFound() {
        Assumptions.assumeTrue(MYSQL_PARITY,
                "Requires MySQL-parity schema; the endpoint reaches the destructive wipe path even for unknown ids.");
        given()
            .pathParam("pid", UUID.randomUUID())
        .when()
            .post(BASE + "/{pid}/penalty/fail")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("TC-406 DIVERGENCE: penalty/fail with malformed UUID must be 400 (current: 500)")
    void tc406_failPenalty_malformedUuid_shouldBeBadRequest() {
        // Fails at path-variable binding BEFORE the wipe runs, so this is H2-safe.
        given()
        .when()
            .post(BASE + "/not-a-valid-uuid/penalty/fail")
        .then()
            .statusCode(400);
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    /** Seeds a committed player + onboarding row in the given stage. Returns playerId. */
    private UUID seedPlayer(OnboardingStage stage) {
        // Both persists run in ONE transaction so the @MapsId player association
        // remains managed when the OnboardingProgress is persisted.
        return txTemplate.execute(status -> {
            PlayerIdentity identity = PlayerIdentity.builder()
                    .username("awk-" + UUID.randomUUID())
                    .createdAt(LocalDateTime.now())
                    .onboardingCompleted(false)
                    .build();
            identity = playerIdentityRepository.save(identity);

            OnboardingProgress progress = OnboardingProgress.builder()
                    .player(identity)
                    .currentStage(stage)
                    .trialCompleted(false)
                    .build();
            onboardingRepository.save(progress);
            return identity.getPlayerId();
        });
    }

    /** Seeds a player in the given stage with populated penalty fields and an explicit deadline. */
    private UUID seedPenalty(OnboardingStage stage, LocalDateTime deadline) {
        UUID pid = seedPlayer(stage);
        txTemplate.executeWithoutResult(status -> {
            OnboardingProgress progress = onboardingRepository.findById(pid).orElseThrow();
            progress.setPenaltyQuestId(UUID.randomUUID());
            progress.setPenaltyTitle("1-Hour Physical Trial");
            progress.setPenaltyDescription("Complete 100 burpees within the hour. The System is watching.");
            progress.setPenaltyDeadlineAt(deadline);
            onboardingRepository.save(progress);
        });
        return pid;
    }

    /** Minimal valid QuestRequest for collaborator stubs (only title/description are read downstream). */
    private QuestRequest sampleQuest() {
        return QuestRequest.builder()
                .playerId(UUID.randomUUID())
                .title("Awakening Penalty: Endure")
                .description("A brutal 1-hour physical survival task generated for the failed trial.")
                .build();
    }
}
