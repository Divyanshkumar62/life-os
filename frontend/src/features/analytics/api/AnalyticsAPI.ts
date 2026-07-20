import { api } from "../../../api/api";

/**
 * Phase 9: Observer Analytics & Telemetry — API Layer
 *
 * Dual network calls matching the backend upgrades:
 * - fetchDungeonGraveyard  → GET  /api/analytics/dungeon-graveyard
 * - fetchConfessions       → GET  /api/analytics/confessions
 * - ariseDungeon           → POST /api/dungeons/{dungeonId}/arise
 */

export interface DungeonGraveyardEntry {
  dungeonId: string;
  title: string;
  description: string;
  dungeonRank: string;
  dungeonStatus: "COMPLETED" | "FAILED" | "SHADOW" | "PERMADEATH" | "ABANDONED";
  totalFloors: number;
  completedFloors: number;
  createdAt: string;
  deadlineAt: string;
  completedAt: string | null;
  failedAt: string | null;
  abandonedAt: string | null;
}

export interface ConfessionEntry {
  id: number;
  text: string;
  accepted: boolean;
  timestamp: string;
  feedback: string;
  strikeCount: number;
  lockoutDurationHours: number | null;
  entryType: "CONFESSION" | "SURVIVAL_TASK" | "SYSTEM_MESSAGE";
}

export interface StatDataPoint {
  date: string;
  STR: number;
  INT: number;
  VIT: number;
  AGI: number;
  SEN: number;
  level: number;
  rank: string;
  isMilestone: boolean;
  milestoneLabel: string | null;
}

export const AnalyticsAPI = {
  /**
   * Fetch the dungeon graveyard — all completed, failed, abandoned,
   * shadow, and permadeath dungeons for the player.
   */
  fetchDungeonGraveyard: async (playerId: string): Promise<DungeonGraveyardEntry[]> => {
    return await api.get(`/analytics/dungeon-graveyard?playerId=${playerId}`);
  },

  /**
   * Fetch the confession graveyard — chronological log of confessions,
   * judgments, and Architect feedback.
   */
  fetchConfessions: async (playerId: string): Promise<ConfessionEntry[]> => {
    return await api.get(`/analytics/confessions?playerId=${playerId}`);
  },

  /**
   * Fetch the player's lifetime stat growth trajectory.
   */
  fetchStatGrowth: async (playerId: string): Promise<StatDataPoint[]> => {
    return await api.get(`/analytics/stats?playerId=${playerId}`);
  },

  /**
   * Fetch the heatmap (365-day grid of daily quest statuses).
   */
  fetchHeatmap: async (playerId: string): Promise<any[]> => {
    return await api.get(`/analytics/heatmap?playerId=${playerId}`);
  },

  /**
   * Mutation payload handler for the resurrection engine.
   * POST /api/dungeons/{dungeonId}/arise — consumes a COMMAND_ARISE item
   * and resurrects a FAILED dungeon into a SHADOW state.
   */
  ariseDungeon: async (dungeonId: string, playerId: string): Promise<any> => {
    return await api.post(`/dungeons/${dungeonId}/arise?playerId=${playerId}`);
  },
};