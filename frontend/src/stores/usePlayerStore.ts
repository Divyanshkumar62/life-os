import { create } from "zustand";
import { PlayerAPI, DashboardAPI } from "../api/api";

export interface PlayerAttributes {
  STR: number;
  INT: number;
  VIT: number;
  SEN: number;
  AGI: number;
  freePoints: number;
}

export interface PlayerState {
  playerId: string;
  username: string;
  level: number;
  xp: number;
  maxXp: number;
  gold: number;
  debt: number;
  rank: string;
  jobClass: string | null;
  jobChangeStatus: 'NOT_TRIGGERED' | 'AWAITING_ACCEPTANCE' | 'IN_PROGRESS' | 'COOLDOWN' | 'COMPLETED' | null;
  attributes: PlayerAttributes;
  penaltyActive: boolean;
  activeModifiers: string[];
  onboardingCompleted: boolean;
  loading: boolean;
  error: string | null;

  // Actions
  fetchPlayerState: (playerId: string) => Promise<void>;
  allocateStat: (statName: keyof Omit<PlayerAttributes, "freePoints">, amount: number) => Promise<void>;
  setPenaltyActive: (active: boolean) => void;
  setModifiers: (modifiers: string[]) => void;
  setOnboardingCompleted: (completed: boolean) => void;
  resetError: () => void;
}

const initialAttributes: PlayerAttributes = {
  STR: 10,
  INT: 10,
  VIT: 10,
  SEN: 10,
  AGI: 10,
  freePoints: 0,
};

export const usePlayerStore = create<PlayerState>((set, get) => ({
  playerId: "",
  username: "",
  level: 1,
  xp: 0,
  maxXp: 100,
  gold: 0,
  debt: 0,
  rank: "E",
  jobClass: null,
  jobChangeStatus: null,
  attributes: initialAttributes,
  penaltyActive: false,
  activeModifiers: [],
  onboardingCompleted: false,
  loading: false,
  error: null,

  fetchPlayerState: async (playerId: string) => {
    if (!playerId) return;
    set({ loading: true, error: null });
    try {
      const data = await DashboardAPI.getStatusWindow(playerId);
      set({
        playerId,
        username: data.username || "Hunter",
        jobClass: data.jobClass || null,
        jobChangeStatus: data.identity?.jobChangeStatus || null,
        level: data.identity?.level ?? 1,
        rank: data.identity?.rank ?? "E",
        xp: data.progression?.currentXp ?? 0,
        maxXp: data.progression?.maxXpForLevel ?? 100,
        gold: data.economy?.gold ?? 0,
        debt: data.economy?.debt ?? 0,
        attributes: {
          STR: data.attributes?.STR ?? 10,
          INT: data.attributes?.INT ?? 10,
          VIT: data.attributes?.VIT ?? 10,
          SEN: data.attributes?.SEN ?? 10,
          AGI: data.attributes?.AGI ?? 10,
          freePoints: data.attributes?.freePoints ?? 0,
        },
        penaltyActive: data.systemState?.penaltyActive ?? false,
        activeModifiers: data.systemState?.activeBuffs ?? [],
        onboardingCompleted: true,
        loading: false,
      });
    } catch (err: any) {
      set({ 
        error: err.message || "Failed to fetch player state", 
        loading: false,
        onboardingCompleted: false
      });
    }
  },

  allocateStat: async (statName: keyof Omit<PlayerAttributes, "freePoints">, amount: number) => {
    const state = get();
    const currentFreePoints = state.attributes.freePoints;

    if (currentFreePoints < amount) {
      set({ error: "Insufficient free stat points" });
      return;
    }

    // Capture snapshot for rollback
    const originalAttributes = { ...state.attributes };

    // Optimistic UI Update
    set({
      attributes: {
        ...state.attributes,
        [statName]: state.attributes[statName] + amount,
        freePoints: currentFreePoints - amount,
      },
      error: null,
    });

    try {
      await PlayerAPI.allocateStat(state.playerId, statName, amount);
    } catch (err: any) {
      // Rollback on failure
      set({
        attributes: originalAttributes,
        error: err.message || "Stat allocation failed. Rolled back.",
      });
      throw err;
    }
  },

  setPenaltyActive: (active: boolean) => {
    set({ penaltyActive: active });
  },

  setModifiers: (modifiers: string[]) => {
    set({ activeModifiers: modifiers });
  },

  setOnboardingCompleted: (completed: boolean) => {
    set({ onboardingCompleted: completed });
  },

  resetError: () => set({ error: null }),
}));
