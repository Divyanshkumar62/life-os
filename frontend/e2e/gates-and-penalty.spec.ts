import { test, expect, type Page } from '@playwright/test';
import { triggerDungeonBreak } from './support/test-api';

/**
 * Suite 2 — The Punishment Loop (Dungeon Break → Penalty Zone → Confession → Survival).
 *
 * PREREQUISITES (all currently MISSING — see the blocker list in the delivery notes):
 *  1. Session bootstrap: `playerId` is held in App.tsx React state with NO persistence
 *     (no localStorage/query-param). A test hook is required to hydrate an authenticated
 *     session on a deep link. Provide E2E_SESSION_HOOK=1 once it exists.
 *  2. Seed data: an existing player + ACTIVE dungeon. Supply via env:
 *     E2E_PLAYER_ID, E2E_PROJECT_ID.
 *  3. Backend test endpoints (test-api.ts) + POST /api/test/reset-db (globalSetup).
 *  4. Status-window polling must refresh `dungeonBreakActive` so DungeonBreakGuard redirects.
 *
 * CONFESSION CONTRACT CORRECTION (verified in ConfessionForm.tsx):
 *  The real rule is a MAX of 600 *WORDS* (input blocked past 600 words); submit is disabled
 *  ONLY when the textarea is empty/whitespace. There is no "<600 chars disabled / >600 chars
 *  valid" behavior. Acceptance is decided by the backend LLM (`res.accepted`), so this suite
 *  intercepts POST /api/penalty/confess to make the outcome deterministic.
 */

const PLAYER_ID = process.env.E2E_PLAYER_ID ?? '';
const PROJECT_ID = process.env.E2E_PROJECT_ID ?? '';

/** Deterministic, LLM-free confession judgment + survival task. */
async function mockPenaltyJudgment(page: Page): Promise<void> {
  // Architect ACCEPTS the confession → PenaltyZoneScreen advances to the survival step.
  await page.route('**/api/penalty/confess**', async (route) => {
    if (route.request().method() !== 'POST') return route.fallback();
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        accepted: true,
        feedback: 'Your reflection is sincere. The System restores partial access.',
        attemptsRemaining: 3,
        lockoutUntil: null,
        survivalTaskId: '00000000-0000-4000-8000-00000000a5ce',
        requiresSurvivalTask: true,
      }),
    });
  });

  // SurvivalTaskView reads GET /penalty/active-task to render the survival UI.
  await page.route('**/api/penalty/active-task**', async (route) => {
    if (route.request().method() !== 'GET') return route.fallback();
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        questId: '00000000-0000-4000-8000-00000000a5ce',
        playerId: PLAYER_ID,
        type: 'SURVIVAL',
        title: 'The Architect\'s Crucible',
        description: 'Prove your discipline through action.',
        requiredCount: 1,
        completedCount: 0,
        progress: 0,
        status: 'ACTIVE',
        createdAt: new Date().toISOString(),
        completedAt: null,
        goldDeducted: null,
        escaped: null,
      }),
    });
  });
}

test.describe('Suite 2 — Dungeon Break → Penalty Zone', () => {
  test.skip(
    !process.env.E2E_SESSION_HOOK || !PLAYER_ID || !PROJECT_ID,
    'Blocked: needs a session-bootstrap hook + seeded player/dungeon (E2E_SESSION_HOOK, E2E_PLAYER_ID, E2E_PROJECT_ID). See prerequisites.',
  );

  test('a dungeon break hijacks the UI and routes through confession into the survival task', async ({
    page,
    request,
  }) => {
    await mockPenaltyJudgment(page);

    // Step 1: user is inside a dungeon.
    await page.goto(`/dungeon/${PROJECT_ID}`);

    // Step 2: force the break server-side.
    await triggerDungeonBreak(request, PROJECT_ID);

    // Step 3: DungeonBreakGuard forcibly hijacks routing to /dungeon-break once the store
    // hydrates `dungeonBreakActive` from the next status-window poll.
    await expect(page).toHaveURL(/\/dungeon-break$/, { timeout: 30_000 });
    await expect(page.getByText('DUNGEON BREAK ACTIVE')).toBeVisible();

    // Step 4: acknowledge → redirect to /penalty.
    await page.getByRole('button', { name: 'Acknowledge Break & Enter Penalty Zone' }).click();
    await expect(page).toHaveURL(/\/penalty$/);

    // PenaltyZoneScreen opens on the 'entry' step (PenaltyPopup) — accept to reach confession.
    await page.getByRole('button', { name: 'Accept Survival Quest' }).click();

    // Step 5 (real negative boundary): an EMPTY confession leaves submit disabled.
    const submit = page.getByRole('button', { name: 'Submit Confession' });
    await expect(page.getByText('0 / 600 WORDS')).toBeVisible();
    await expect(submit).toBeDisabled();

    // Step 6: a non-empty confession enables submit; the judgment is mocked as accepted.
    await page
      .getByPlaceholder(/Begin your confession/i)
      .fill('I failed to hold my discipline today. Tomorrow I start with the hardest task first.');
    await expect(submit).toBeEnabled();
    await submit.click();

    // Step 7: transition to the Survival Task UI (escape to Dashboard only follows task completion).
    await expect(page.getByText('The Architect\'s Crucible')).toBeVisible({ timeout: 10_000 });
    await expect(page.getByRole('button', { name: /Report Progress/i })).toBeVisible();
  });
});
