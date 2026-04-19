export const API_BASE_URL = 'http://localhost:8080/api';

export const api = {
    async get(endpoint: string) {
        const res = await fetch(`${API_BASE_URL}${endpoint}`);
        if (!res.ok) throw new Error(`API GET ${endpoint} Failed`);
        return await res.json();
    },

    async post(endpoint: string, body?: any) {
        const res = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: body ? JSON.stringify(body) : undefined
        });
        if (!res.ok) throw new Error(`API POST ${endpoint} Failed`);
        return await res.json();
    }
};

export const DashboardAPI = {
    getStatusWindow: async (playerId: string) => {
        return await api.get(`/player/status-window/${playerId}`);
    }
};

export const QuestAPI = {
    completeQuest: async (questId: string) => {
        return await api.post(`/quests/${questId}/complete`);
    },
    failQuest: async (questId: string) => {
        return await api.post(`/quests/${questId}/fail`);
    },
    getActiveQuests: async (playerId: string) => {
        return await api.get(`/quests/active?playerId=${playerId}`);
    }
};

export const EconomyAPI = {
    fetchShopItems: async (playerId: string) => {
        return await api.get(`/shop/items?playerId=${playerId}`);
    },
    purchaseItem: async (playerId: string, itemCode: string) => {
        return await api.post(`/shop/purchase/${itemCode}?playerId=${playerId}`);
    },
    fetchInventory: async (playerId: string) => {
        return await api.get(`/shop/inventory?playerId=${playerId}`);
    },
    useConsumable: async (playerId: string, itemCode: string) => {
        return await api.post(`/consumables/use/${itemCode}?playerId=${playerId}`);
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
