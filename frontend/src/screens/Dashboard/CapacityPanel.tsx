import { SystemPanel, SystemProgressBar } from '../../components/system';

export interface CapacityPanelProps {
    activeQuests: number;
    maxQuests: number;
    inventoryLoad: number;
    equippedSkills: string[];
}

/**
 * CapacityPanel - Resource capacity display
 * 
 * Responsibilities:
 * - Show active quest slots
 * - Display inventory load
 * - Show equipped skills
 */
export function CapacityPanel({
    activeQuests,
    maxQuests,
    inventoryLoad,
    equippedSkills,
}: CapacityPanelProps) {
    return (
        <SystemPanel title="📊 CAPACITY" glowColor="cyan">
            {/* Active Quests */}
            <div className="mb-4">
                <div className="flex justify-between items-center mb-2">
                    <span className="text-xs text-gray-400 uppercase">Active Quests</span>
                    <span className="text-sm font-bold text-white font-mono">
                        {activeQuests} / {maxQuests}
                    </span>
                </div>
                <div className="flex gap-1">
                    {Array.from({ length: maxQuests }).map((_, i) => (
                        <div
                            key={i}
                            className={`h-2 flex-1 rounded ${i < activeQuests ? 'bg-cyan-500' : 'bg-gray-700'
                                }`}
                        />
                    ))}
                </div>
            </div>

            {/* Inventory Load */}
            <div className="mb-4">
                <SystemProgressBar
                    current={inventoryLoad}
                    max={100}
                    label="Inventory Load"
                    color="cyan"
                    showPercentage
                    height="sm"
                />
            </div>

            {/* Equipped Skills */}
            <div>
                <p className="text-xs text-gray-400 uppercase mb-2">Equipped Skills</p>
                <div className="grid grid-cols-4 gap-2">
                    {equippedSkills.map((skill, i) => (
                        <div
                            key={i}
                            className="aspect-square bg-gray-900 border border-cyan-500 rounded flex items-center justify-center text-2xl hover:bg-gray-800 transition-smooth cursor-pointer"
                            title={skill}
                        >
                            {skill}
                        </div>
                    ))}
                    {/* Empty Slot */}
                    {equippedSkills.length < 4 && (
                        <div className="aspect-square bg-gray-900 border border-gray-700 border-dashed rounded flex items-center justify-center text-gray-600 text-xl">
                            +
                        </div>
                    )}
                </div>
            </div>
        </SystemPanel>
    );
}
