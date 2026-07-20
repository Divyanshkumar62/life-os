import React from 'react';
import type { Quest } from '../../types/project';
import { Swords } from 'lucide-react';

interface IntelQuestCardProps {
    quest: Quest;
    onComplete: (id: string) => void;
}

export const IntelQuestCard: React.FC<IntelQuestCardProps> = ({ quest, onComplete }) => {
    const statRewards = Object.entries(quest.attributeDeltas || {}).map(([stat, val]) => {
        return `+${val} ${stat}`;
    });

    const isCompleted = quest.state === 'COMPLETED';

    return (
        <div className={`p-4 border transition-all relative overflow-hidden ${
            isCompleted 
                ? 'border-green-500/20 bg-green-950/5' 
                : 'border-cyan-500/30 bg-cyan-950/5 hover:border-cyan-500/60 shadow-[0_0_10px_rgba(6,182,212,0.05)] hover:shadow-[0_0_15px_rgba(6,182,212,0.1)]'
        }`}>
            {!isCompleted && (
                <div className="absolute top-0 right-0 w-16 h-16 bg-cyan-500/5 rounded-full blur-xl pointer-events-none" />
            )}

            <div className="flex items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                    <div className={`p-2 border rounded-none ${
                        isCompleted ? 'border-green-500/40 text-green-500' : 'border-cyan-500/40 text-cyan-400'
                    }`}>
                        <Swords size={16} className={!isCompleted ? 'animate-pulse' : ''} />
                    </div>
                    <div>
                        <h4 className={`font-bold tracking-wider font-mono text-sm uppercase ${
                            isCompleted ? 'text-green-500/80 line-through' : 'text-cyan-400'
                        }`}>
                            {quest.title}
                        </h4>
                        <p className="text-gray-400 text-xs mt-1 uppercase">{quest.description}</p>
                    </div>
                </div>

                <div className="flex items-center gap-4">
                    <div className="flex flex-col items-end gap-1 font-mono">
                        {statRewards.map((reward, i) => (
                            <span key={i} className="text-xs bg-cyan-950/40 border border-cyan-500/30 text-cyan-400 px-2 py-0.5 rounded-none font-bold uppercase tracking-wider">
                                {reward}
                            </span>
                        ))}
                        <span className="text-[10px] text-gray-500">{quest.xpReward} XP</span>
                    </div>

                    {!isCompleted && (
                        <button
                            onClick={() => onComplete(quest.questId)}
                            className="px-4 py-2 border border-cyan-500 text-cyan-400 hover:bg-cyan-500/10 transition-all font-mono text-xs uppercase tracking-wider font-bold"
                        >
                            Complete
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};
