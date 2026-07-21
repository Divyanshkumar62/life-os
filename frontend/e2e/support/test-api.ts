import type { APIRequestContext } from '@playwright/test';

/**
 * Backend state-hydration helpers for E2E.
 *
 * These hit test-only backend endpoints that DO NOT EXIST YET (to be built).
 * They must be exposed ONLY under a test profile (e.g. @Profile("test") / env flag),
 * never in production. Each returns the parsed JSON body (if any) and throws with
 * context on a non-2xx response so suites fail fast with a clear reason.
 *
 * Signature note: the directive lists these as `triggerDungeonBreak(projectId)` etc.
 * Playwright helpers need an APIRequestContext to issue requests, so each takes
 * `request` as the first argument (idiomatic Playwright). Use the `request` fixture:
 *   test('...', async ({ page, request }) => { await triggerDungeonBreak(request, id); });
 */

const BACKEND_URL = process.env.E2E_BACKEND_URL ?? 'http://localhost:8080';

async function postTest(
  request: APIRequestContext,
  path: string,
  data: Record<string, unknown>,
): Promise<unknown> {
  const url = `${BACKEND_URL}${path}`;
  const res = await request.post(url, { data });
  if (res.status() === 404) {
    throw new Error(
      `[test-api] ${path} returned 404 — this test endpoint is not implemented yet. ` +
        `Build it behind a test-only profile before enabling the suite.`,
    );
  }
  if (!res.ok()) {
    throw new Error(`[test-api] POST ${path} failed with HTTP ${res.status()}.`);
  }
  const body = await res.text();
  return body ? JSON.parse(body) : null;
}

/** Force an active dungeon into the "Dungeon Break" state (30% gold drain + Penalty Zone). */
export function triggerDungeonBreak(request: APIRequestContext, projectId: string) {
  return postTest(request, '/api/test/trigger-dungeon-break', { projectId });
}

/**
 * Set a player's level directly.
 * ⚠ To make the frontend `LevelUpOverlay` appear, this endpoint must also drive the real
 * level-up path (publish `LevelUpEvent` → `VoiceSystemEvent(LEVEL_UP)`); a bare DB update
 * will change the number but never surface the overlay the UI listens for.
 */
export function setPlayerLevel(request: APIRequestContext, playerId: string, level: number) {
  return postTest(request, '/api/test/set-level', { playerId, level });
}

/** Simulate a full 3-day gauntlet clear → leaves the player at `AWAITING_CLASS_SELECTION`. */
export function completeGauntlet(request: APIRequestContext, playerId: string) {
  return postTest(request, '/api/test/complete-gauntlet', { playerId });
}
