import React, { useState } from 'react';
import type { Quest } from '../../types/project';
import { IntelQuestCard } from './IntelQuestCard';
import { ChevronDown, ChevronUp, AlertCircle } from 'lucide-react';

interface IntelQuestSectionProps {
    quests: Quest[];
    onComplete: (id: string) => void;
}

export const IntelQuestSection: React.FC<IntelQuestSectionProps> = ({ quests, onComplete }) => {
    const [isOpen, setIsOpen] = useState(true);

    if (!quests || quests.length === 0) return null;

    const activeCount = quests.filter(q => q.state !== 'COMPLETED').length;

    return (
        <div className="border border-cyan-500/30 bg-black/40 shadow-lg">
            <div 
                onClick={() => setIsOpen(!isOpen)}
                className="flex items-center justify-between p-4 cursor-pointer hover:bg-cyan-950/10 border-b border-cyan-500/20 select-none"
            >
                <div className="flex items-center gap-2 text-cyan-400">
                    <AlertCircle size={16} className="animate-pulse" />
                    <h3 className="font-bold font-mono text-xs uppercase tracking-[0.2em]">
                        System Calibration ({activeCount} Active Intel)
                    </h3>
                </div>
                <div className="text-cyan-400">
                    {isOpen ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                </div>
            </div>

            {isOpen && (
                <div className="p-4 space-y-3">
                    <p className="text-[10px] text-gray-500 font-mono tracking-wider uppercase leading-relaxed mb-1">
                        * Intel quests are designed for progressive profiling. They do not impact penalty zone status and will expire silently.
                    </p>
                    {quests.map((quest) => (
                        <IntelQuestCard 
                            key={quest.questId} 
                            quest={quest} 
                            onComplete={onComplete} 
                        />
                    ))}
                </div>
            )}
        </div>
    );
};
