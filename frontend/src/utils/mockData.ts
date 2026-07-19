import type { Project, Quest } from "../types/project";

// Define TypeScript structures representing backend DTOs to ensure type safety.
export interface StatusWindowResponse {
  jobClass?: string | null;
  identity?: {
    level?: number;
    rank?: string;
    title?: string;
    equippedTheme?: string;
    jobChangeStatus?: 'NOT_TRIGGERED' | 'AWAITING_ACCEPTANCE' | 'IN_PROGRESS' | 'COOLDOWN' | 'COMPLETED' | null;
  };
  progression?: {
    currentXp?: number;
    maxXpForLevel?: number;
  };
  attributes?: {
    STR?: number;
    INT?: number;
    VIT?: number;
    SEN?: number;
    AGI?: number;
    freePoints?: number;
  };
  economy?: {
    gold?: number;
  };
  systemState?: {
    penaltyActive?: boolean;
    activeBuffs?: string[];
    wakeUpTime?: string;
  };
  temporalState?: {
    penaltyLockoutUntil?: string | null;
    failedConfessionAttempts?: number;
  };
  systemMessages?: string[];
}

export interface ShopItem {
  itemId: string;
  code: string;
  name: string;
  description: string;
  cost: number;
  baseCost?: number;
  stockLimit?: number;
  rankRequirement?: string;
}

export interface JobChangeQuest {
  questId: string;
  day: number;
  title: string;
  questType: string;
  difficulty: string;
  state: 'PENDING' | 'COMPLETED' | 'ASSIGNED' | 'FAILED';
}

export interface JobChangeStatus {
  jobClass: string | null;
  status: 'NOT_TRIGGERED' | 'AWAITING_ACCEPTANCE' | 'IN_PROGRESS' | 'AWAITING_CLASS_SELECTION' | 'COOLDOWN' | 'COMPLETED' | null;
  xpFrozen: boolean;
  cooldownUntil: string | null;
  loading?: boolean;
}

export interface HeatmapEntry {
  date: string;
  status: 'ALL_CLEARED' | 'PARTIAL' | 'FAILED' | 'NO_QUESTS';
}

export interface StatDataPoint {
  date: string;
  STR: number;
  INT: number;
  VIT: number;
  AGI: number;
  SEN: number;
}

export interface GraveyardEntry {
  id: number;
  text: string;
  accepted: boolean;
  timestamp: string;
  feedback: string;
}

// ----------------------------------------------------
// 1. Mock Dashboard State
// Level 24, C-Rank, with 3 active daily quests.
// ----------------------------------------------------
export const mockDashboardState: StatusWindowResponse = {
  jobClass: "Silver Knight",
  identity: {
    level: 24,
    rank: "C",
    title: "C-Rank Hunter",
    equippedTheme: "theme-default",
    jobChangeStatus: "NOT_TRIGGERED"
  },
  progression: {
    currentXp: 1200,
    maxXpForLevel: 2500
  },
  attributes: {
    STR: 45,
    INT: 30,
    VIT: 40,
    SEN: 35,
    AGI: 38,
    freePoints: 5
  },
  economy: {
    gold: 1500
  },
  systemState: {
    penaltyActive: false,
    activeBuffs: [],
    wakeUpTime: "08:00"
  },
  temporalState: {
    penaltyLockoutUntil: null,
    failedConfessionAttempts: 0
  }
};

export const mockDashboardQuests: Quest[] = [
  {
    questId: "daily-pushups",
    title: "Daily Training: Push-ups",
    description: "Complete 100 push-ups to strengthen your upper body.",
    difficulty: "C",
    state: "ASSIGNED",
    xpReward: 100,
    priority: "NORMAL"
  },
  {
    questId: "daily-situps",
    title: "Daily Training: Sit-ups",
    description: "Complete 100 sit-ups for core stability.",
    difficulty: "C",
    state: "COMPLETED",
    xpReward: 100,
    priority: "NORMAL"
  },
  {
    questId: "daily-running",
    title: "Daily Training: Running",
    description: "Run 10km to improve stamina.",
    difficulty: "C",
    state: "ASSIGNED",
    xpReward: 150,
    priority: "HIGH"
  }
];

export const mockDashboardProjects: Project[] = [
  {
    projectId: "project-1",
    title: "Vanguard Preparation",
    description: "Prepare physical assets and conditioning.",
    status: "ACTIVE",
    stabilityStatus: "STABLE",
    rank: "C",
    createdAt: new Date().toISOString(),
    floorsTotal: 1,
    floorsCompleted: 0
  }
];

// ----------------------------------------------------
// 2. Mock Dungeon State
// An active C-Rank Goblin Lair with 3 floors.
// ----------------------------------------------------
export const mockDungeonProject: Project = {
  projectId: "dungeon-goblin-lair",
  title: "Goblin Lair",
  description: "A dark cavern filled with aggressive goblins. The air smells of damp stone and blood. Clear all floors to defeat the Goblin Shaman.",
  status: "ACTIVE",
  stabilityStatus: "STABLE",
  bossName: "Goblin Shaman",
  rank: "C",
  createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(),
  floorsTotal: 3,
  floorsCompleted: 1
};

export const mockDungeonQuests: Quest[] = [
  {
    questId: "goblin-floor-1",
    title: "Goblin Lair - Floor 1",
    description: "Clear the goblin vanguard guarding the entrance tunnels.",
    difficulty: "C",
    state: "COMPLETED",
    xpReward: 300,
    projectId: "dungeon-goblin-lair"
  },
  {
    questId: "goblin-floor-2",
    title: "Goblin Lair - Floor 2",
    description: "Defeat the goblin hunters patrolling the inner ruins.",
    difficulty: "C",
    state: "ASSIGNED",
    xpReward: 400,
    projectId: "dungeon-goblin-lair"
  },
  {
    questId: "goblin-floor-3",
    title: "Goblin Lair - Floor 3 (Boss Gate)",
    description: "Infiltrate the shaman's chambers and execute the boss.",
    difficulty: "C",
    state: "PENDING",
    xpReward: 600,
    projectId: "dungeon-goblin-lair"
  }
];

export const mockDungeonState = {
  project: mockDungeonProject,
  quests: mockDungeonQuests,
  statusWindow: {
    ...mockDashboardState,
    identity: {
      ...mockDashboardState.identity,
      equippedTheme: "theme-default"
    }
  }
};

// ----------------------------------------------------
// 3. Mock Penalty State
// An active Architect Lockout with a 4-hour timer.
// ----------------------------------------------------
// Lockout end time is exactly 4 hours from current execution.
const getFourHoursFromNow = () => new Date(Date.now() + 4 * 60 * 60 * 1000).toISOString();

export const getMockPenaltyState = (): StatusWindowResponse => ({
  jobClass: "Silver Knight",
  identity: {
    level: 24,
    rank: "C",
    title: "C-Rank Hunter",
    equippedTheme: "theme-vanguard",
    jobChangeStatus: "NOT_TRIGGERED"
  },
  progression: {
    currentXp: 1200,
    maxXpForLevel: 2500
  },
  attributes: {
    STR: 45,
    INT: 30,
    VIT: 40,
    SEN: 35,
    AGI: 38,
    freePoints: 5
  },
  economy: {
    gold: 1500
  },
  systemState: {
    penaltyActive: true,
    activeBuffs: [],
    wakeUpTime: "08:00"
  },
  temporalState: {
    penaltyLockoutUntil: getFourHoursFromNow(),
    failedConfessionAttempts: 3
  }
});

// ----------------------------------------------------
// 4. Mock Store State
// Level 10 unlocked, 5,000 Gold, displaying the Monarch's Exemption.
// ----------------------------------------------------
export const mockStoreStatusWindow: StatusWindowResponse = {
  jobClass: "Novice Hunter",
  identity: {
    level: 10,
    rank: "C",
    title: "Monarch Exemption Seeker",
    equippedTheme: "theme-default",
    jobChangeStatus: "NOT_TRIGGERED"
  },
  progression: {
    currentXp: 500,
    maxXpForLevel: 1000
  },
  attributes: {
    STR: 20,
    INT: 20,
    VIT: 20,
    SEN: 20,
    AGI: 20,
    freePoints: 0
  },
  economy: {
    gold: 5000
  },
  systemState: {
    penaltyActive: false,
    activeBuffs: [],
    wakeUpTime: "08:00"
  },
  temporalState: {
    penaltyLockoutUntil: null,
    failedConfessionAttempts: 0
  }
};

export const mockStoreItems: ShopItem[] = [
  {
    itemId: "item-monarch-exemption",
    code: "MONARCH_EXEMPTION",
    name: "Monarch's Exemption",
    description: "A legendary pass that exempts the bearer from a single system penalty zone eviction.",
    cost: 3000,
    stockLimit: 1,
    rankRequirement: "E-RANK"
  },
  {
    itemId: "item-hp-potion-mid",
    code: "HP_POTION_MID",
    name: "Mid-Grade HP Potion",
    description: "Restores 30% of maximum Health Points instantly.",
    cost: 500,
    stockLimit: 5,
    rankRequirement: "E-RANK"
  },
  {
    itemId: "item-defense-charm",
    code: "DEFENSE_CHARM",
    name: "Architect's Guard Charm",
    description: "Reduces damage taken by 10% for the next 24 hours.",
    cost: 1200,
    stockLimit: 2,
    rankRequirement: "D-RANK"
  }
];

export const mockStoreState = {
  statusWindow: mockStoreStatusWindow,
  items: mockStoreItems
};

// ----------------------------------------------------
// 5. Mock Job Change State
// Level 40 gauntlet active, awaiting final Boss Trial.
// ----------------------------------------------------
export const mockJobChangeStatusWindow: StatusWindowResponse = {
  jobClass: "Shadow Necromancer",
  identity: {
    level: 40,
    rank: "B",
    title: "Shadow Necromancer Candidate",
    equippedTheme: "theme-shadow",
    jobChangeStatus: "IN_PROGRESS"
  },
  progression: {
    currentXp: 8000,
    maxXpForLevel: 12000
  },
  attributes: {
    STR: 85,
    INT: 55,
    VIT: 70,
    SEN: 60,
    AGI: 75,
    freePoints: 10
  },
  economy: {
    gold: 25000
  },
  systemState: {
    penaltyActive: false,
    activeBuffs: ["SHADOW_VEIL"],
    wakeUpTime: "08:00"
  },
  temporalState: {
    penaltyLockoutUntil: null,
    failedConfessionAttempts: 0
  }
};

export const mockJobChangeStatus: JobChangeStatus = {
  jobClass: "Shadow Necromancer",
  status: "IN_PROGRESS",
  xpFrozen: true,
  cooldownUntil: null
};

export const mockJobChangeQuests: JobChangeQuest[] = [
  {
    questId: "job-day-1",
    day: 1,
    title: "The Endless Swarm (Volume Test - 6-8 tasks)",
    questType: "VOLUME_TEST",
    difficulty: "B",
    state: "COMPLETED"
  },
  {
    questId: "job-day-2",
    day: 2,
    title: "The Royal Guards (Intensity Test - 3 Deep Work tasks)",
    questType: "INTENSITY_TEST",
    difficulty: "A",
    state: "COMPLETED"
  },
  {
    questId: "job-day-3",
    day: 3,
    title: "The Blood-Red Commander (Boss Room - Final Trial)",
    questType: "BOSS_TRIAL",
    difficulty: "S",
    state: "PENDING"
  }
];

export const mockJobChangeState = {
  statusWindow: mockJobChangeStatusWindow,
  jobChange: mockJobChangeStatus,
  quests: mockJobChangeQuests
};

// ----------------------------------------------------
// 6. Mock Observer State
// A populated 365-day heatmap and stat trajectory.
// ----------------------------------------------------
export const mockObserverStatusWindow: StatusWindowResponse = {
  jobClass: "Grand Architect",
  identity: {
    level: 35,
    rank: "B",
    title: "Grand Architect",
    equippedTheme: "theme-scholar",
    jobChangeStatus: "COMPLETED"
  },
  progression: {
    currentXp: 4500,
    maxXpForLevel: 10000
  },
  attributes: {
    STR: 40,
    INT: 95,
    VIT: 50,
    SEN: 70,
    AGI: 65,
    freePoints: 0
  },
  economy: {
    gold: 18000
  },
  systemState: {
    penaltyActive: false,
    activeBuffs: ["ARCANE_FLOW"],
    wakeUpTime: "07:00"
  },
  temporalState: {
    penaltyLockoutUntil: null,
    failedConfessionAttempts: 0
  }
};

// Generates a mock 365-day history ending today
export const generateMockHeatmap = (): HeatmapEntry[] => {
  const entries: HeatmapEntry[] = [];
  const today = new Date();
  const statuses: ('ALL_CLEARED' | 'PARTIAL' | 'FAILED' | 'NO_QUESTS')[] = [
    'ALL_CLEARED', 'ALL_CLEARED', 'PARTIAL', 'NO_QUESTS', 'ALL_CLEARED', 'FAILED', 'PARTIAL'
  ];

  for (let i = 364; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(today.getDate() - i);
    const dateStr = d.toISOString().split('T')[0];
    
    // Choose status based on deterministic pseudo-random index
    const seed = (d.getFullYear() * 100 + d.getMonth()) * 100 + d.getDate();
    const status = statuses[seed % statuses.length];
    
    entries.push({
      date: dateStr,
      status: status
    });
  }
  return entries;
};

// Generates a 30-day stat history trajectory showing steady level up
export const generateMockStatGrowth = (): StatDataPoint[] => {
  const points: StatDataPoint[] = [];
  const today = new Date();
  
  for (let i = 30; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(today.getDate() - i);
    const dateStr = d.toISOString().split('T')[0];
    
    // Growth progression
    const factor = (30 - i) / 30;
    points.push({
      date: dateStr,
      STR: Math.floor(30 + 10 * factor),
      INT: Math.floor(75 + 20 * factor),
      VIT: Math.floor(40 + 10 * factor),
      AGI: Math.floor(55 + 10 * factor),
      SEN: Math.floor(60 + 10 * factor)
    });
  }
  return points;
};

export const mockGraveyard: GraveyardEntry[] = [
  {
    id: 1,
    text: "Did not complete the daily workout. Excuses were too weak.",
    accepted: false,
    timestamp: new Date(Date.now() - 15 * 24 * 60 * 60 * 1000).toLocaleDateString(),
    feedback: "The Architect demands sincerity. Shuttering access."
  },
  {
    id: 2,
    text: "My apologies, I was occupied with a family emergency and couldn't run.",
    accepted: true,
    timestamp: new Date(Date.now() - 12 * 24 * 60 * 60 * 1000).toLocaleDateString(),
    feedback: "Reasoning verified. Access re-established."
  },
  {
    id: 3,
    text: "Forgot to record progress for reflection.",
    accepted: false,
    timestamp: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toLocaleDateString(),
    feedback: "Shallow defense. 2-Hour lockout initiated."
  }
];

export const mockObserverState = {
  statusWindow: mockObserverStatusWindow,
  heatmap: generateMockHeatmap(),
  stats: generateMockStatGrowth(),
  graveyard: mockGraveyard
};

// Main mapped configuration helper
export const getMockDataByView = (): Record<string, any> => ({
  onboarding: {
    statusWindow: mockDashboardState,
    jobChange: mockJobChangeStatus
  },
  profile: {
    statusWindow: mockDashboardState,
    jobChange: mockJobChangeStatus
  },
  dashboard: {
    statusWindow: mockDashboardState,
    quests: mockDashboardQuests,
    projects: mockDashboardProjects
  },
  dungeon: {
    statusWindow: mockDungeonState.statusWindow,
    project: mockDungeonProject,
    quests: mockDungeonQuests
  },
  penalty: {
    statusWindow: getMockPenaltyState(),
    quests: [],
    projects: []
  },
  store: {
    statusWindow: mockStoreStatusWindow,
    items: mockStoreItems
  },
  job_change: {
    statusWindow: mockJobChangeStatusWindow,
    jobChange: mockJobChangeStatus,
    quests: mockJobChangeQuests
  },
  observer: {
    statusWindow: mockObserverStatusWindow,
    heatmap: mockObserverState.heatmap,
    stats: mockObserverState.stats,
    graveyard: mockObserverState.graveyard
  }
});
