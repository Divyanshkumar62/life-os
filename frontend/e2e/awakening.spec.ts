import { test, expect } from '@playwright/test';
import { mockGrokTask, MOCK_PENALTY_TASK_TITLE } from './support/mock-grok';

/**
 * Suite 1 — The Awakening Wipe (Phase 1).
 *
 * Journey: questionnaire → Awakening → Trial → manual Fail Trial → Awakening Penalty →
 *          "I Failed" → type "DELETE CHARACTER" → wipe → back to the start (/onboarding welcome).
 *
 * Selector policy: the app currently ships NO `data-testid` hooks (verified), so this suite
 * uses accessible role/text locators. See the QA note accompanying this file — adding stable
 * test IDs is a recommended follow-up to harden these selectors.
 */
test.describe('Phase 1 — The Awakening Wipe', () => {
  test('failing the awakening penalty cascade-deletes the account and returns to onboarding start', async ({
    page,
  }) => {
    // Bypass the real LLM before any onboarding action fires.
    await mockGrokTask(page);

    await page.goto('/onboarding');

    // ── Step 1: Welcome → questionnaire ────────────────────────────────────────
    await expect(page.getByText('You Have Been Chosen')).toBeVisible();
    await page.getByRole('button', { name: 'ACCEPT' }).click(); // POST /api/onboarding/start

    // ── Step 1 (cont.): SystemAnalysisScreen — 3 phases ────────────────────────
    // Phase 0: "WORSHIP THE LORD" — two free-text answers.
    await page.getByPlaceholder('Describe the beast you must conquer...').fill('Procrastination');
    await page
      .getByPlaceholder('Why did you lose resolve in the past? Be honest...')
      .fill('Lost consistency after week one');
    await page.getByRole('button', { name: 'Next Phase' }).click();

    // Phase 1: "PRAISE THE LORD" — single-select focus area.
    await page.getByRole('button', { name: /MENTAL SHARPNESS/ }).click();
    await page.getByRole('button', { name: 'Next Phase' }).click();

    // Phase 2: "PROVE YOUR FAITH" — goal + time commitment, then submit.
    await page
      .getByPlaceholder('What is your primary milestone 6 months from now?...')
      .fill('Ship the Life-OS MVP');
    await page.getByRole('button', { name: /2-4 HOURS/ }).click();
    await page.getByRole('button', { name: 'Prove your Faith' }).click(); // POST /{id}/awakening

    // ── LoadingScreen (auto-advances) → AwakeningScreen ────────────────────────
    await expect(page.getByRole('button', { name: 'CONFIRM STATUS' })).toBeVisible({
      timeout: 20_000,
    });
    await page.getByRole('button', { name: 'CONFIRM STATUS' }).click();

    // ── Step 2: TrialQuestScreen ───────────────────────────────────────────────
    await expect(page.getByText('Courage of the Weak')).toBeVisible();

    // ── Step 3: manual "Fail Trial" trigger (POST /trial/fail — intercepted) ───
    await page.getByRole('button', { name: 'Fail Trial' }).click();

    // ── Step 4: assert transition to AwakeningPenaltyScreen + 1-hour countdown ─
    await expect(page.getByText('AWAKENING PENALTY')).toBeVisible();
    await expect(page.getByText('STATUS: PENALTY ACTIVE')).toBeVisible();
    await expect(page.getByText(MOCK_PENALTY_TASK_TITLE)).toBeVisible();
    // Countdown renders MM:SS; mocked deadline is +1h, so it starts at ~60:00.
    await expect(page.getByText(/^(60|59):\d{2}$/)).toBeVisible();

    // ── Step 5: "I Failed" opens the wipe-confirmation modal ───────────────────
    await page.getByRole('button', { name: 'I Failed' }).click();
    await expect(page.getByText('IMMEDIATE WIPE WARNING')).toBeVisible();

    const wipeButton = page.getByRole('button', { name: 'WIPE MY ACCOUNT' });
    await expect(wipeButton).toBeDisabled(); // guard: disabled until exact phrase typed

    // ── Step 6: type the confirmation phrase and confirm ───────────────────────
    await page.getByPlaceholder('TYPE HERE...').fill('DELETE CHARACTER');
    await expect(wipeButton).toBeEnabled();

    // ── Step 7: assert the wipe API fires + the 33-table cascade resolves ──────
    const [wipeResponse] = await Promise.all([
      page.waitForResponse(
        (res) =>
          /\/api\/onboarding\/[^/]+\/penalty\/fail$/.test(res.url()) &&
          res.request().method() === 'POST',
      ),
      wipeButton.click(),
    ]);
    expect(wipeResponse.ok()).toBeTruthy(); // 2xx == cascade delete resolved server-side

    // ── Step 7 (cont.): routed back to the absolute beginning of the app ───────
    await expect(page.getByText('You Have Been Chosen')).toBeVisible();
    await expect(page.getByRole('button', { name: 'ACCEPT' })).toBeVisible();
    await expect(page).toHaveURL(/\/onboarding$/);
  });
});
