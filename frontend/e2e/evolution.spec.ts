import { test, expect, type Page } from '@playwright/test';
import { setPlayerLevel, completeGauntlet } from './support/test-api';

/**
 * Suite 3 — The Evolution (Level 40 → Job Change → Class Selection).
 *
 * PREREQUISITES / BLOCKERS (verified against source):
 *  1. Session bootstrap: `playerId` is not persisted (App.tsx useState only). A test hook is
 *     required to hydrate an authenticated session. Provide E2E_SESSION_HOOK=1 + E2E_PLAYER_ID.
 *  2. LevelUpOverlay is driven by the App `useSystemVoice` loop reacting to a LEVEL_UP alert —
 *     `setPlayerLevel` must publish the real LevelUpEvent → VoiceSystemEvent for it to appear.
 *  3. ⛔ CLASS SELECTION IS UNREACHABLE IN PRODUCTION: `JobChangeSelectGuard` and
 *     `ClassSelectionScreen` are NOT mounted in App.tsx (no `/job-change/select-class` route),
 *     and `JobChangePopup` explicitly hides itself at `AWAITING_CLASS_SELECTION`. The directed
 *     steps 4–6 cannot run until this is wired — that test is marked `fixme`.
 *  4. Label drift: `ClassSelectionScreen` cards are "VANGUARD / SCHOLAR / SHADOW" and submit the
 *     id "Shadow" (not "Shadow Necromancer"); the confirm button reads "EVOLVE".
 */

const PLAYER_ID = process.env.E2E_PLAYER_ID ?? '';

/** Deterministic, LLM-free class evolution. */
async function mockSelectClass(page: Page): Promise<void> {
  await page.route('**/api/player/job-change/select-class**', async (route) => {
    if (route.request().method() !== 'POST') return route.fallback();
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        playerId: PLAYER_ID,
        jobClass: 'Shadow',
        evolutionRewards: {
          goldAwarded: 50000,
          statPointsAwarded: 25,
          itemsAwarded: [],
        },
      }),
    });
  });
}

test.describe('Suite 3 — The Evolution', () => {
  test.skip(
    !process.env.E2E_SESSION_HOOK || !PLAYER_ID,
    'Blocked: needs a session-bootstrap hook + seeded player (E2E_SESSION_HOOK, E2E_PLAYER_ID).',
  );

  test('reaching Level 40 surfaces the LevelUpOverlay and routes to point allocation', async ({
    page,
    request,
  }) => {
    // Step 1: force Level 40 (must also publish the LEVEL_UP voice event — see prereq #2).
    await setPlayerLevel(request, PLAYER_ID, 40);

    await page.goto('/dashboard');

    // Step 2: the global portal overlay appears (rendered to document.body).
    await expect(page.getByText('LEVEL UP')).toBeVisible({ timeout: 30_000 });
    await expect(page.getByText('LV.40')).toBeVisible();

    // Step 3: allocate points → App wires onAllocate to navigate to /inventory.
    await page.getByRole('button', { name: 'ALLOCATE POINTS' }).click();
    await expect(page.getByText('LEVEL UP')).toBeHidden();
    await expect(page).toHaveURL(/\/inventory$/);
  });

  /**
   * ⛔ Blocked by prerequisite #3 — kept as an executable spec so it activates the moment
   * ClassSelectionScreen is routed behind JobChangeSelectGuard. Uses the REAL component
   * selectors (SHADOW card + EVOLVE button), not the directive's "Shadow Necromancer" label.
   */
  test.fixme(
    'a completed gauntlet forces class selection and evolving as SHADOW returns to the dashboard',
    async ({ page, request }) => {
      await mockSelectClass(page);

      // Step: simulate the 3-day grind → AWAITING_CLASS_SELECTION.
      await completeGauntlet(request, PLAYER_ID);

      // Directed: JobChangeSelectGuard intercepts and forces the class-selection route.
      await page.goto('/dashboard');
      await expect(page).toHaveURL(/\/job-change\/select-class$/);
      await expect(page.getByText('SECOND AWAKENING')).toBeVisible();

      // Select the Shadow path (card labeled "SHADOW", id "Shadow") and evolve.
      await page.getByRole('button', { name: /SHADOW/ }).click();
      await page.getByRole('button', { name: 'EVOLVE' }).click();

      // ClassSelectionScreen navigates to /dashboard ~2.5s after a successful evolve.
      await expect(page).toHaveURL(/\/dashboard$/, { timeout: 10_000 });
    },
  );
});
