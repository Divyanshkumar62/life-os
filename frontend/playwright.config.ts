import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright E2E configuration — Life-OS frontend (React/Vite) + Java/Postgres backend.
 *
 * Runtime topology (verified against source):
 *  - Frontend dev server: Vite, http://localhost:5173 (package.json "dev": "vite").
 *  - Backend: Spring Boot on http://localhost:8080. The React screens call the backend
 *    via HARD-CODED absolute URLs (`http://localhost:8080/api/...`), NOT a Vite proxy,
 *    so route interception globs must be host-agnostic (`**` prefix).
 *
 * globalSetup resets the database to a clean baseline before the run.
 */

export const FRONTEND_URL = 'http://localhost:5173';
export const BACKEND_URL = 'http://localhost:8080';

export default defineConfig({
  testDir: './e2e',
  globalSetup: require.resolve('./e2e/support/global-setup'),

  // A DB-wiping suite must never run in parallel against a single shared backend/DB.
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never' }]],

  timeout: 60_000,
  expect: { timeout: 10_000 },

  use: {
    baseURL: FRONTEND_URL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],

  /**
   * Auto-starts ONLY the frontend. The Java backend on :8080 must already be running;
   * globalSetup verifies backend reachability via the reset-db endpoint and aborts the
   * run with a clear message if it is unavailable.
   */
  webServer: {
    command: 'npm run dev',
    url: FRONTEND_URL,
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
});
