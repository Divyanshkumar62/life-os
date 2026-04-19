import { SystemPanel, SystemProgressBar, SystemMetric, SystemBadge, SystemRadar } from '../../components/system';


import { useSystemContext } from '../../context/SystemContext';

// Removing props as it now relies entirely on SystemContext
export interface CurrentStatusPanelProps { }

/**
 * CurrentStatusPanel - Primary player progression display
 * 
 * Responsibilities:
 * - Show level and XP progress
 * - Display job class
 * - Show core stats (STR/AGI/INT)
 */
export function CurrentStatusPanel({ }: CurrentStatusPanelProps) {
    const { statusWindow } = useSystemContext();

    const currentXp = statusWindow?.progression?.currentXp || 0;
    const maxXp = statusWindow?.progression?.maxXpForLevel || 100;
    const level = statusWindow?.identity?.level || 1;
    const jobClass = statusWindow?.identity?.title || 'None';
    const strength = statusWindow?.attributes?.STR || 10;
    const agility = statusWindow?.attributes?.VIT || 10; // VIT mapped to UI Agility
    const intellect = statusWindow?.attributes?.INT || 10;

    const xpPercentage = maxXp > 0 ? (currentXp / maxXp) * 100 : 0;

    return (
        <SystemPanel glowColor="cyan" className="col-span-2">
            {/* Header */}
            <div className="mb-4">
                <p className="text-xs text-gray-400 uppercase tracking-wide mb-1">Current Status</p>
            </div>

            {/* Level Display */}
            <div className="flex items-baseline gap-2 mb-2">
                <h2 className="text-5xl font-bold text-white font-mono">LVL. {level}</h2>
                <SystemBadge variant="info" glow>
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

            {/* Radar Chart */}
            <div className="mb-6 flex justify-center">
                <SystemRadar
                    data={[
                        { stat: 'STR', value: strength, max: 100 },
                        { stat: 'VIT', value: agility, max: 100 }, // Mapping UI Agility to Backend VIT
                        { stat: 'INT', value: intellect, max: 100 },
                        { stat: 'SEN', value: 30, max: 100 } // Sensory default mapped
                    ]}
                    size={200}
                />
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
