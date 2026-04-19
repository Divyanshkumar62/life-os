import { useEffect, useState } from 'react';
import { api } from '../api/api';
import { useSystemAudio } from './useSystemAudio';

export interface SystemAlert {
    eventId: string;
    playerId: string;
    message: string;
    eventType: 'LEVEL_UP' | 'PENALTY_ALERT' | 'DUNGEON_BREAK' | 'PROMOTION_UPDATE' | 'GENERAL_NOTICE';
    createdAt: string;
}

export function useSystemVoice(playerId: string | null | undefined) {
    const [alerts, setAlerts] = useState<SystemAlert[]>([]);
    const { playLevelUp, playSystemAlert, playRedGateAlarm } = useSystemAudio();

    useEffect(() => {
        if (!playerId) return;

        const pollSystemVoice = async () => {
            try {
                // Fetch unconsumed alerts
                const newAlerts: SystemAlert[] = await api.get(`/system/alerts/${playerId}`);

                if (newAlerts && newAlerts.length > 0) {
                    // Update state to render them
                    setAlerts(prev => [...prev, ...newAlerts]);

                    // Trigger Audio Feedback
                    newAlerts.forEach(alert => {
                        switch (alert.eventType) {
                            case 'LEVEL_UP':
                                playLevelUp();
                                break;
                            case 'PENALTY_ALERT':
                            case 'DUNGEON_BREAK':
                                playRedGateAlarm();
                                break;
                            case 'PROMOTION_UPDATE':
                            case 'GENERAL_NOTICE':
                                playSystemAlert();
                                break;
                            default:
                                playSystemAlert();
                        }
                    });
                }
            } catch (error) {
                console.error("System Voice Polling Error:", error);
            }
        };

        // Poll every 5 seconds
        const intervalId = setInterval(pollSystemVoice, 5000);

        // Initial fetch
        pollSystemVoice();

        return () => clearInterval(intervalId);
    }, [playerId]);

    const consumeAlert = (eventId: string) => {
        setAlerts(prev => prev.filter(alert => alert.eventId !== eventId));
    };

    return { alerts, consumeAlert };
}
