import type { FullConfig } from '@playwright/test';
import { BACKEND_URL } from '../../playwright.config';

/**
 * Global setup — runs ONCE before the entire suite.
 *
 * Hits the backend test-reset endpoint to guarantee a clean database baseline so
 * suites (especially the destructive account-wipe suite) start from a known state.
 *
 * ⚠️ PREREQUISITE — NOT YET IMPLEMENTED IN THE BACKEND (verified via source grep):
 *   `POST /api/test/reset-db` does not currently exist. It MUST be added as a
 *   test-profile-only endpoint (e.g. guarded by @Profile("test") / a TEST env flag)
 *   that truncates player-owned tables. Until it exists this setup will abort the run
 *   loudly — by design — rather than let tests run against dirty/prod data.
 */
async function globalSetup(_config: FullConfig): Promise<void> {
  const resetUrl = `${BACKEND_URL}/api/test/reset-db`;

  let response: Response;
  try {
    response = await fetch(resetUrl, { method: 'POST' });
  } catch (cause) {
    throw new Error(
      `[global-setup] Could not reach the backend at ${BACKEND_URL}. ` +
        `Start the Java/Postgres service before running E2E tests. Cause: ${String(cause)}`,
    );
  }

  if (response.status === 404) {
    throw new Error(
      `[global-setup] POST /api/test/reset-db returned 404 — the test-reset endpoint ` +
        `is not implemented. Add a test-profile-only reset endpoint before running E2E tests.`,
    );
  }

  if (!response.ok) {
    throw new Error(
      `[global-setup] Database reset failed with HTTP ${response.status}. Aborting suite.`,
    );
  }

  // eslint-disable-next-line no-console
  console.log('[global-setup] Database reset OK — clean baseline established.');
}

export default globalSetup;
