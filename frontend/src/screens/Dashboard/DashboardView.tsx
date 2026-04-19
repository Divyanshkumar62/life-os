import { useState, useEffect } from 'react';
import { ScreenFrame } from '../../components/layout';
import { PenaltyPopup } from '../../components/features/PenaltyZone/PenaltyPopup';
import { PromotionPopup } from '../../components/features/Promotion/PromotionPopup';
import { SystemInterruptionPopup } from '../../components/features/SystemInterruption/SystemInterruptionPopup';
import { TopBar } from './TopBar';
import { PlayerProfileCard } from './PlayerProfileCard';
import { CurrentStatusPanel } from './CurrentStatusPanel';
import { AssetsPanel } from './AssetsPanel';
import { DailyQuestsPanel } from './DailyQuestsPanel';
import { SystemLogPanel } from './SystemLogPanel';
import { CapacityPanel } from './CapacityPanel';
import { QuestAPI, ProjectAPI } from '../../api/api';
import { useSystemVoice } from '../../hooks/useSystemVoice';
import { useSystemContext } from '../../context/SystemContext';
import { clsx } from 'clsx';
import { AlertTriangle, Flame } from 'lucide-react';

/**
 * DashboardView - Main dashboard layout composition
 * 
 * Responsibilities:
 * - Compose all dashboard panels
 * - Handle responsive grid layout
 * - Fetch and display real player data
 */
export interface DashboardViewProps {
    playerId?: string | null;
    onViewSystemLog?: () => void;
    onViewDiagnostic?: () => void;
    onViewProfile?: () => void;
    onViewMissions?: () => void;
    onViewStore?: () => void;
    onViewInventory?: () => void;
    onViewGate?: () => void;
}

export function DashboardView({ playerId, onViewSystemLog, onViewStore, onViewInventory, onViewGate }: DashboardViewProps) {
    // State
    const [isPenaltyActive, setIsPenaltyActive] = useState(false);
    const [isPromotionActive, setIsPromotionActive] = useState(false);
    const [isInterruptionActive, setIsInterruptionActive] = useState(false);

    // Red Gate State
    const [isRedGateActive, setIsRedGateActive] = useState(false);

    // System Voice Polling
    const { alerts, consumeAlert } = useSystemVoice(playerId);

    // Consume Authoritative Global State
    const { statusWindow, loading: _loading } = useSystemContext();

    // Secondary Data State (Consider abstracting these later)
    const [activeQuests, setActiveQuests] = useState<any[]>([]);

    // Get user name and rank from status window
    const playerName = statusWindow?.identity?.username || statusWindow?.identity?.title || 'Hunter';
    const playerRank = statusWindow?.identity?.rank || 'E';
    
    // Map rank to system daily count
    const rankToDailyCount: Record<string, number> = {
        'F': 1, 'E': 2, 'D': 3, 'C': 4, 'B': 4, 'A': 5, 'S': 5
    };
    const maxDailyQuests = rankToDailyCount[playerRank] || 2;

    // Map backend quest to UI quest
    const mapBackendQuest = (q: any) => ({
        id: q.questId || q.id,
        title: q.title,
        goal: q.description || 'Complete',
        current: q.currentProgress || 0,
        target: q.targetProgress || 1,
        reward: q.successXp ? `${q.successXp} XP` : '',
        completed: q.state === 'COMPLETED'
    });

    useEffect(() => {
        if (playerId) {
            Promise.all([
                ProjectAPI.fetchProjects(playerId),
                QuestAPI.getActiveQuests(playerId).catch(() => []) // Requires an API method, mock handling for now
            ]).then(([projects, quests]) => {
                setActiveQuests(quests);

                // Check for Broken Dungeons (Red Gate)
                const hasBrokenDungeon = projects.some((p: any) => p.stabilityStatus === 'BROKEN');
                setIsRedGateActive(hasBrokenDungeon);
                if (hasBrokenDungeon) setIsInterruptionActive(true);
            });
        }
    }, [playerId]);

    // Sync Penalty/Promotion from global state
    useEffect(() => {
        if (statusWindow?.systemState) {
            setIsPenaltyActive(!!statusWindow.systemState.penaltyActive);
            // Promotion logic can be added here natively later
        }
    }, [statusWindow]);

    // Parse System Voice into System Logs
    const parsedAlerts = alerts.map(alert => ({
        id: alert.eventId,
        timestamp: new Date(alert.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        type: 'warning' as const, // Fallback, could map dynamically
        message: alert.message
    }));

    const mockLogs = [
        { id: '1', timestamp: '10:45 AM', type: 'success' as const, message: 'System Connected.' },
    ];

    const combinedLogs = [...parsedAlerts, ...mockLogs];

    // Listen for Critical Alerts to trigger Popups
    useEffect(() => {
        alerts.forEach(alert => {
            if (alert.eventType === 'PENALTY_ALERT') {
                setIsPenaltyActive(true);
            }
            if (alert.eventType === 'PROMOTION_UPDATE') {
                setIsPromotionActive(true);
            }
            // Consume automatically after reading into UI state
            consumeAlert(alert.eventId);
        });
    }, [alerts, consumeAlert]);

    if (_loading) {
        return (
            <ScreenFrame>
                <div className="flex items-center justify-center h-full text-blue-500 animate-pulse">
                    initiating_link...
                </div>
            </ScreenFrame>
        );
    }

    return (
        <ScreenFrame className={clsx(isRedGateActive && "border-solo-red-900 bg-[#0f0404]")}>
            {/* Red Gate Warning Banner */}
            {isRedGateActive && (
                <div className="bg-solo-red-950/80 border-b border-solo-red-600 p-2 text-center flex items-center justify-center gap-2 animate-pulse mb-4 rounded">
                    <AlertTriangle className="text-solo-red-500" size={16} />
                    <span className="text-solo-red-100 font-bold tracking-[0.2em] text-xs">
                        WARNING: DUNGEON BREAK IN PROGRESS
                    </span>
                    <AlertTriangle className="text-solo-red-500" size={16} />
                </div>
            )}

            {/* Top Bar */}
            <TopBar
                userName={playerName}
                systemStatus={isRedGateActive ? "maintenance" : "online"}
            />

            {/* Navigation / Quick Actions */}
            <div className="flex gap-4 mt-6">
                <button
                    onClick={onViewStore}
                    className={clsx(
                        "flex-1 border bg-gray-900/80 py-3 rounded-lg transition-all font-mono tracking-widest text-sm uppercase flex items-center justify-center group shadow-lg",
                        !isRedGateActive && "border-solo-blue-900/50 hover:border-solo-blue-500 text-solo-blue-400 hover:text-white shadow-glow-cyan hover:shadow-cyan-500/20",
                        isRedGateActive && "border-gray-800 text-gray-500 opacity-50 cursor-not-allowed" // Disable during break? Or just dim
                    )}
                >
                    <span className="mr-2 opacity-50 group-hover:opacity-100">🛒</span>
                    Store
                </button>
                <button
                    onClick={onViewInventory}
                    className={clsx(
                        "flex-1 border bg-gray-900/80 py-3 rounded-lg transition-all font-mono tracking-widest text-sm uppercase flex items-center justify-center group shadow-lg",
                        !isRedGateActive && "border-solo-blue-900/50 hover:border-solo-blue-500 text-solo-blue-400 hover:text-white shadow-glow-cyan hover:shadow-cyan-500/20",
                        isRedGateActive && "border-solo-red-900/50 text-solo-red-400 hover:text-white"
                    )}
                >
                    <span className="mr-2 opacity-50 group-hover:opacity-100">🎒</span>
                    Status
                </button>
                <button
                    onClick={onViewGate}
                    className={clsx(
                        "flex-1 border bg-gray-900/80 py-3 rounded-lg transition-all font-mono tracking-widest text-sm uppercase flex items-center justify-center group shadow-lg animate-pulse",
                        !isRedGateActive && "border-solo-blue-500 text-solo-blue-400 hover:bg-solo-blue-900/30",
                        isRedGateActive && "border-solo-red-500 bg-solo-red-900/20 text-solo-red-500 hover:bg-solo-red-900/50 shadow-glow-red"
                    )}
                >
                    <span className="mr-2 opacity-50 group-hover:opacity-100">{isRedGateActive ? <Flame size={14} /> : '🌀'}</span>
                    {isRedGateActive ? "ENTER RED GATE" : "SYSTEM GATES"}
                </button>
            </div>

            <div className={clsx("grid grid-cols-1 lg:grid-cols-4 gap-4 mt-4 transition-all duration-700", isPenaltyActive && "pointer-events-none blur-sm opacity-50")}>
                {/* Left Column */}
                <div className="space-y-4">
                    <PlayerProfileCard />
                    <AssetsPanel />
                </div>

                {/* Center Column (2 cols wide) */}
                <div className="lg:col-span-2 space-y-4">
                    <CurrentStatusPanel />

                    <DailyQuestsPanel
                        quests={activeQuests.map(mapBackendQuest)}
                        onQuestToggle={async (id, completed) => {
                            if (!completed) {
                                try {
                                    await QuestAPI.completeQuest(id);
                                } catch (error) {
                                    console.error("Failed to complete quest:", error);
                                }
                            }
                        }}
                        onClaimReward={(id) => console.log('Claim reward:', id)}
                    />
                </div>

                {/* Right Column */}
                <div className="space-y-4">
                    <SystemLogPanel
                        entries={combinedLogs}
                        onViewFullLog={onViewSystemLog}
                    />

                    <CapacityPanel
                        activeQuests={activeQuests.length}
                        maxQuests={maxDailyQuests}
                        inventoryLoad={0}
                        equippedSkills={[]}
                    />
                </div>
            </div>

            {/* Bottom Bar */}
            <div className="mt-4 p-3 border-t border-gray-700 flex justify-between items-center text-xs text-gray-500">
                <div className="flex items-center gap-4">
                    <span className="flex items-center gap-1">
                        <span className="w-2 h-2 bg-success rounded-full animate-pulse-glow" />
                        SERVER CONNECTED
                    </span>
                    <span>LATENCY: 12ms</span>
                    <span className="text-blue-500">PLAYER ID: {playerId ? playerId.substring(0, 8) + '...' : 'UNKNOWN'}</span>

                    {/* Dev Triggers */}
                    <div className="flex gap-2 ml-4 flex-wrap">
                        <button
                            onClick={() => setIsPenaltyActive(true)}
                            className="px-2 py-1 bg-red-900/30 text-red-500 border border-red-900 rounded hover:bg-red-900/50 transition-colors"
                        >
                            [DEV] PENALTY
                        </button>
                        <button
                            onClick={() => setIsPromotionActive(true)}
                            className="px-2 py-1 bg-blue-900/30 text-blue-500 border border-blue-900 rounded hover:bg-blue-900/50 transition-colors"
                        >
                            [DEV] PROMOTION
                        </button>
                        <button
                            onClick={() => setIsRedGateActive(!isRedGateActive)}
                            className="px-2 py-1 bg-[#450a0a] text-solo-red-500 border border-solo-red-900 rounded hover:bg-solo-red-900/50 transition-colors"
                        >
                            [DEV] RED GATE
                        </button>
                    </div>
                </div>
                <span>SYSTEM ARCHITECT // ADMIN ACCESS: RESTRICTED</span>
            </div>

            {/* Popups */}
            <PenaltyPopup
                isOpen={isPenaltyActive}
                onClose={() => setIsPenaltyActive(false)}
            />
            <PromotionPopup
                isOpen={isPromotionActive}
                onClose={() => setIsPromotionActive(false)}
            />
            <SystemInterruptionPopup
                isOpen={isInterruptionActive}
                onClose={() => setIsInterruptionActive(false)}
            />
        </ScreenFrame>
    );
}
