export interface Quest {
    questId: string;
    title: string;
    description: string;
    difficulty: string; // 'E', 'D', 'C', ...
    state: 'PENDING' | 'ASSIGNED' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
    xpReward: number;
    projectId?: string;
    priority?: 'NORMAL' | 'HIGH' | 'CRITICAL';
}

export interface Project {
    projectId: string;
    title: string;
    description: string;
    status: 'ACTIVE' | 'COMPLETED' | 'FAILED' | 'ABANDONED';
    stabilityStatus: 'STABLE' | 'UNSTABLE' | 'BROKEN';
    bossName?: string;
    rank?: string; // 'E', 'D', ...
    createdAt: string;
    lastActivityAt?: string;
    floorsTotal?: number;
    floorsCompleted?: number;
}
