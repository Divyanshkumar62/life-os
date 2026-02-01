import { useState } from 'react';
import { SystemPanel, SystemTab, SystemCheckbox, SystemButton, SystemBadge } from '../../components/system';
import type { Tab } from '../../components/system';

export interface Quest {
    id: string;
    title: string;
    goal: string;
    current: number;
    target: number;
    reward: string;
    completed: boolean;
}

export interface DailyQuestsPanelProps {
    quests: Quest[];
    onQuestToggle?: (questId: string, completed: boolean) => void;
    onClaimReward?: (questId: string) => void;
}

/**
 * DailyQuestsPanel - Quest management UI
 * 
 * Responsibilities:
 * - Display quest list
 * - Handle quest completion
 * - Allow reward claiming
 */
export function DailyQuestsPanel({
    quests,
    onQuestToggle,
    onClaimReward,
}: DailyQuestsPanelProps) {
    const [activeTab, setActiveTab] = useState('daily');

    const tabs: Tab[] = [
        { id: 'daily', label: 'Daily Quests', icon: '📋' },
        { id: 'penalty', label: 'Penalty Zone', icon: '⚠️' },
    ];

    const refreshTime = '04:23:11';

    return (
        <SystemPanel glowColor="cyan" className="col-span-2">
            {/* Tabs */}
            <div className="mb-4">
                <SystemTab
                    tabs={tabs}
                    activeTab={activeTab}
                    onTabChange={setActiveTab}
                />
            </div>

            {/* Refresh Timer */}
            <div className="flex justify-end mb-3">
                <span className="text-xs text-gray-400 font-mono">
                    REFRESH IN: {refreshTime}
                </span>
            </div>

            {/* Quest List */}
            <div className="space-y-3">
                {quests.map((quest) => (
                    <div
                        key={quest.id}
                        className="bg-gray-900 border border-gray-700 rounded p-3 hover:border-cyan-600 transition-smooth"
                    >
                        <div className="flex items-start justify-between gap-3">
                            {/* Checkbox & Info */}
                            <div className="flex items-start gap-3 flex-1">
                                <SystemCheckbox
                                    checked={quest.completed}
                                    onCheckedChange={(checked) => onQuestToggle?.(quest.id, checked)}
                                />

                                <div className="flex-1">
                                    <h4 className="text-sm font-semibold text-white mb-1">
                                        {quest.title}
                                    </h4>
                                    <p className="text-xs text-gray-400 mb-2">
                                        {quest.goal}: <span className="font-mono">{quest.current} / {quest.target}</span>
                                    </p>
                                    <p className="text-xs text-cyan-400">
                                        REWARD: {quest.reward}
                                    </p>
                                </div>
                            </div>

                            {/* Status Badge or Claim Button */}
                            <div>
                                {quest.completed ? (
                                    <SystemButton
                                        variant="primary"
                                        size="sm"
                                        onClick={() => onClaimReward?.(quest.id)}
                                    >
                                        Claim Reward
                                    </SystemButton>
                                ) : (
                                    <SystemBadge variant="warning" size="sm">
                                        Incomplete
                                    </SystemBadge>
                                )}
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </SystemPanel>
    );
}
