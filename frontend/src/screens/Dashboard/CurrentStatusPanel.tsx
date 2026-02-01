import { SystemPanel, SystemProgressBar, SystemMetric, SystemBadge } from '../../components/system';

export interface CurrentStatusPanelProps {
    level: number;
    currentXp: number;
    maxXp: number;
    jobClass: string;
    strength: number;
    agility: number;
    intellect: number;
}

/**
 * CurrentStatusPanel - Primary player progression display
 * 
 * Responsibilities:
 * - Show level and XP progress
 * - Display job class
 * - Show core stats (STR/AGI/INT)
 */
export function CurrentStatusPanel({
    level,
    currentXp,
    maxXp,
    jobClass,
    strength,
    agility,
    intellect,
}: CurrentStatusPanelProps) {
    const xpPercentage = (currentXp / maxXp) * 100;

    return (
        <SystemPanel glowColor="cyan" className="col-span-2">
            {/* Header */}
            <div className="mb-4">
                <p className="text-xs text-gray-400 uppercase tracking-wide mb-1">Current Status</p>
            </div>

            {/* Level Display */}
            <div className="flex items-baseline gap-2 mb-2">
                <h2 className="text-5xl font-bold text-white font-mono">LVL. {level}</h2>
                <SystemBadge variant="teal" glow>
                    {jobClass}
                </SystemBadge>
            </div>

            {/* XP Progress */}
            <div className="mb-6">
                <SystemProgressBar
                    current={currentXp}
                    max={maxXp}
                    label="EXP PROGRESS"
                    showValues
                    showPercentage
                    height="lg"
                />
                <div className="flex justify-between items-center mt-1">
                    <span className="text-xs text-gray-500 font-mono">{xpPercentage.toFixed(2)}%</span>
                    <span className="text-xs text-cyan-400 uppercase tracking-wide">To Next Level</span>
                </div>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-3 gap-4">
                <SystemMetric
                    label="Strength"
                    value={strength}
                    size="md"
                />
                <SystemMetric
                    label="Agility"
                    value={agility}
                    size="md"
                />
                <SystemMetric
                    label="Intellect"
                    value={intellect}
                    size="md"
                />
            </div>
        </SystemPanel>
    );
}
