import type { Page } from '@playwright/test';

/**
 * AI (Grok/LLM) interception for the Awakening Penalty flow.
 *
 * Why two routes, not one:
 *   - `POST /api/onboarding/{id}/trial/fail` is where the backend `OnboardingService.failTrial`
 *     invokes the LLM (`aiQuestService.generateAwakeningPenalty`). Intercepting it bypasses the
 *     real model call — this is the endpoint named in the directive.
 *   - The `AwakeningPenaltyScreen` component, however, renders its task title / description /
 *     1-hour countdown from `GET /api/onboarding/{id}/penalty/status` (verified in source), NOT
 *     from the `trial/fail` response body. To make the rendered screen deterministic and fully
 *     LLM-free, we fulfill that GET with the SAME static DTO.
 *
 * The mock payload matches the real `AwakeningPenaltyDTO` schema exactly (verified against
 * `onboarding/dto/AwakeningPenaltyDTO.java`): playerId, penaltyQuestId, taskTitle,
 * taskDescription, deadlineAt, stage.
 */

const STATIC_PENALTY_QUEST_ID = '00000000-0000-4000-8000-000000000abc';

export const MOCK_PENALTY_TASK_TITLE = 'MOCK PENALTY: THE IRON PENANCE';
export const MOCK_PENALTY_TASK_DESCRIPTION =
  'Complete 100 push-ups, 100 sit-ups, and a 10km run within the hour. (Static test fixture — no LLM involved.)';

/**
 * Builds an AwakeningPenaltyDTO whose deadline is exactly one hour out, so the
 * frontend countdown renders at ~60:00 (MM:SS) when the penalty screen mounts.
 */
function buildPenaltyDto() {
  const deadlineAt = new Date(Date.now() + 60 * 60 * 1000).toISOString();
  return {
    playerId: '00000000-0000-4000-8000-0000000000ff',
    penaltyQuestId: STATIC_PENALTY_QUEST_ID,
    taskTitle: MOCK_PENALTY_TASK_TITLE,
    taskDescription: MOCK_PENALTY_TASK_DESCRIPTION,
    deadlineAt,
    stage: 'AWAKENING_PENALTY',
  };
}

/**
 * Registers request interception on `page`. Must be called BEFORE the "Fail Trial"
 * action so the routes are live when the requests fire.
 *
 * NOTE: does NOT intercept `/penalty/fail` — that call is intentionally left to hit the
 * real backend so the destructive 33-table cascade delete is genuinely exercised.
 */
export async function mockGrokTask(page: Page): Promise<void> {
  // 1) Directive-named LLM bypass: the trial-failure penalty generation endpoint.
  await page.route('**/api/onboarding/*/trial/fail', async (route) => {
    if (route.request().method() !== 'POST') return route.fallback();
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(buildPenaltyDto()),
    });
  });

  // 2) The endpoint the AwakeningPenaltyScreen actually reads to render its UI.
  await page.route('**/api/onboarding/*/penalty/status', async (route) => {
    if (route.request().method() !== 'GET') return route.fallback();
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(buildPenaltyDto()),
    });
  });
}
