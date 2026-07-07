export const API_BASE_URL = 'http://localhost:8080/api';

type MessageListener = (messages: string[]) => void;
const listeners = new Set<MessageListener>();

export const systemMessageEmitter = {
    subscribe(listener: MessageListener) {
        listeners.add(listener);
        return () => {
            listeners.delete(listener);
        };
    },
    emit(messages: string[]) {
        listeners.forEach((listener) => listener(messages));
    }
};

import { getMockDataByView } from "../utils/mockData";

export let activeSandboxView: string | null = null;
export const setSandboxView = (view: string | null) => {
    activeSandboxView = view;
};

function getMockResponseForGet(endpoint: string, view: string): any {
    const mockData = getMockDataByView()[view];
    if (!mockData) return undefined;

    if (endpoint.includes("/player/status-window/")) {
        return mockData.statusWindow || {};
    }

    if (endpoint.includes("/quests/active")) {
        return { quests: mockData.quests || [] };
    }

    if (endpoint.includes("/quests/red-gate/")) {
        return { active: false, quest: null };
    }

    if (endpoint.includes("/projects")) {
        const parts = endpoint.split("/projects/");
        if (parts.length > 1 && parts[1]) {
            const subparts = parts[1].split("/");
            if (subparts.length > 1 && subparts[1] === "quests") {
                return mockData.quests || [];
            }
            return mockData.project || mockData.projects?.[0] || {};
        }
        return mockData.projects || [];
    }

    if (endpoint.includes("/shop/items")) {
        return { items: mockData.items || [] };
    }

    if (endpoint.includes("/shop/inventory")) {
        return { items: [] };
    }

    if (endpoint.includes("/player/job-change/") && endpoint.endsWith("/status")) {
        return mockData.jobChange || { status: "NOT_TRIGGERED", jobClass: null, xpFrozen: false, cooldownUntil: null };
    }

    if (endpoint.includes("/player/job-change/") && endpoint.endsWith("/quests")) {
        return mockData.quests || [];
    }

    if (endpoint.includes("/analytics/heatmap")) {
        return mockData.heatmap || [];
    }

    if (endpoint.includes("/analytics/stats")) {
        return mockData.stats || [];
    }

    if (endpoint.includes("/analytics/graveyard")) {
        return mockData.graveyard || [];
    }

    return undefined;
}

function getMockResponseForPost(endpoint: string, _view: string, body?: any): any {
    if (endpoint.includes("/penalty/confess")) {
        const text = body?.text || "";
        const isSincere = text.trim().split(/\s+/).length >= 10; // Simple check for visual QA
        if (isSincere) {
            return {
                accepted: true,
                feedback: "[SYSTEM] Sincerity confirmed. The Architect has lifted the lockdown."
            };
        } else {
            return {
                accepted: false,
                feedback: "[SYSTEM] Insincere confession. Suffer the lockout extension.",
                attemptsRemaining: 0,
                lockoutUntil: new Date(Date.now() + 4 * 60 * 60 * 1000).toISOString()
            };
        }
    }

    if (endpoint.includes("/quests/") && endpoint.endsWith("/complete")) {
        return { quest: { state: "COMPLETED" } };
    }

    if (endpoint.includes("/shop/purchase/")) {
        return { success: true };
    }

    if (endpoint.includes("/player/job-change/quest/") && endpoint.endsWith("/complete")) {
        return { success: true };
    }

    return { success: true };
}

export const api = {
    async get(endpoint: string) {
        if (activeSandboxView) {
            const mockResponse = getMockResponseForGet(endpoint, activeSandboxView);
            if (mockResponse !== undefined) {
                return mockResponse;
            }
        }

        const res = await fetch(`${API_BASE_URL}${endpoint}`);
        if (!res.ok) throw new Error(`API GET ${endpoint} Failed`);
        const json = await res.json();
        if (json && typeof json === 'object' && Array.isArray(json.systemMessages) && json.systemMessages.length > 0) {
            systemMessageEmitter.emit(json.systemMessages);
        }
        return json;
    },

    async post(endpoint: string, body?: any) {
        if (activeSandboxView) {
            const mockResponse = getMockResponseForPost(endpoint, activeSandboxView, body);
            if (mockResponse !== undefined) {
                return mockResponse;
            }
        }

        const res = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: body ? JSON.stringify(body) : undefined
        });
        if (!res.ok) throw new Error(`API POST ${endpoint} Failed`);
        const json = await res.json();
        if (json && typeof json === 'object' && Array.isArray(json.systemMessages) && json.systemMessages.length > 0) {
            systemMessageEmitter.emit(json.systemMessages);
        }
        return json;
    }
};

export const DashboardAPI = {
    getStatusWindow: async (playerId: string) => {
        return await api.get(`/player/status-window/${playerId}`);
    }
};

export const QuestAPI = {
    completeQuest: async (questId: string) => {
        const res = await api.post(`/quests/${questId}/complete`);
        return res.quest;
    },
    failQuest: async (questId: string) => {
        const res = await api.post(`/quests/${questId}/fail`);
        return res.quest;
    },
    getActiveQuests: async (playerId: string) => {
        const res = await api.get(`/quests/active?playerId=${playerId}`);
        return res.quests || [];
    }
};

export const EconomyAPI = {
    fetchShopItems: async (playerId: string) => {
        const res = await api.get(`/shop/items?playerId=${playerId}`);
        return res.items || [];
    },
    purchaseItem: async (playerId: string, itemCode: string) => {
        return await api.post(`/shop/purchase/${itemCode}?playerId=${playerId}`);
    },
    fetchInventory: async (playerId: string) => {
        return await api.get(`/shop/inventory?playerId=${playerId}`);
    },
    useConsumable: async (playerId: string, itemCode: string) => {
        return await api.post(`/consumables/use/${itemCode}?playerId=${playerId}`);
    },
    openBox: async (playerId: string, boxCode: string) => {
        return await api.post(`/inventory/open-box/${boxCode}?playerId=${playerId}`);
    }
};

export const PlayerAPI = {
    allocateStat: async (playerId: string, stat: string, amount: number) => {
        return await api.post(`/player/stats/allocate?playerId=${playerId}`, { stat, amount });
    }
};

export const ProjectAPI = {
    fetchProjects: async (playerId: string) => {
        return await api.get(`/projects?playerId=${playerId}`);
    },
    equipQuest: async (projectId: string, questId: string) => {
        return await api.post(`/projects/${projectId}/equip/${questId}`);
    },
    getProject: async (projectId: string) => {
        return await api.get(`/projects/${projectId}`);
    },
    getProjectQuests: async (projectId: string) => {
        return await api.get(`/projects/${projectId}/quests`);
    }
};

export const RedGateAPI = {
    getStatus: async (playerId: string) => {
        return await api.get(`/quests/red-gate/${playerId}/status`);
    },
    triggerWithKey: async (playerId: string) => {
        return await api.post(`/quests/red-gate/${playerId}/trigger-key`);
    },
    complete: async (playerId: string) => {
        return await api.post(`/quests/red-gate/${playerId}/complete`);
    },
    fail: async (playerId: string) => {
        return await api.post(`/quests/red-gate/${playerId}/fail`);
    }
};

export const JobChangeAPI = {
    getStatus: async (playerId: string) => {
        return await api.get(`/player/job-change/${playerId}/status`);
    },
    getQuests: async (playerId: string) => {
        return await api.get(`/player/job-change/${playerId}/quests`);
    },
    accept: async (playerId: string) => {
        return await api.post(`/player/job-change/${playerId}/accept`);
    },
    delay: async (playerId: string) => {
        return await api.post(`/player/job-change/${playerId}/delay`);
    },
    skipCooldown: async (playerId: string) => {
        return await api.post(`/player/job-change/${playerId}/skip-cooldown`);
    },
    completeQuest: async (questId: string) => {
        return await api.post(`/player/job-change/quest/${questId}/complete`);
    },
    failQuest: async (questId: string) => {
        return await api.post(`/player/job-change/quest/${questId}/fail`);
    }
};

export const ProgressionAPI = {
    checkGate: async (playerId: string) => {
        return await api.get(`/progression/${playerId}/check-gate`);
    },
    canPromote: async (playerId: string) => {
        return await api.get(`/progression/${playerId}/can-promote`);
    },
    requestPromotion: async (playerId: string) => {
        return await api.post(`/progression/${playerId}/request-promotion`);
    },
    processOutcome: async (playerId: string, success: boolean) => {
        return await api.post(`/progression/${playerId}/process-outcome?success=${success}`);
    }
};

export const PenaltyAPI = {
    submitConfession: async (playerId: string, text: string) => {
        return await api.post(`/penalty/confess?playerId=${playerId}`, { text });
    }
};

export const AnalyticsAPI = {
    fetchHeatmap: async (playerId: string) => {
        return await api.get(`/analytics/heatmap?playerId=${playerId}`);
    },
    fetchStatGrowth: async (playerId: string) => {
        return await api.get(`/analytics/stats?playerId=${playerId}`);
    },
    fetchGraveyard: async (playerId: string) => {
        return await api.get(`/analytics/graveyard?playerId=${playerId}`);
    }
};

