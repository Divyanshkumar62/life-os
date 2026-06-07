import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { ProjectAPI, QuestAPI } from '../../api/api';
import type { Project, Quest } from '../../types/project';
import { ScreenFrame } from '../../components/layout';
import { Skull, Swords, Lock, CheckCircle2 } from 'lucide-react';

interface DungeonViewProps {
    playerId: string | null;
    projectId: string;
    onBack: () => void;
}

export function DungeonView({ projectId, onBack }: DungeonViewProps) {
    const [project, setProject] = useState<Project | null>(null);
    const [quests, setQuests] = useState<Quest[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchDungeonData();
    }, [projectId]);

    const fetchDungeonData = async () => {
        try {
            setLoading(true);
            const [projData, questsData] = await Promise.all([
                ProjectAPI.getProject(projectId),
                ProjectAPI.getProjectQuests(projectId)
            ]);
            setProject(projData);
            
            // Sort quests by creation or title (assuming title has "Floor 1", etc.)
            // Or just trust the returned order which is usually insertion order.
            setQuests(questsData);
        } catch (error) {
            console.error("Failed to fetch dungeon:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleClearFloor = async (questId: string) => {
        try {
            await QuestAPI.completeQuest(questId);
            fetchDungeonData();
        } catch (error) {
            console.error("Failed to clear floor:", error);
            alert("Failed to clear floor. Make sure it is assigned or you meet the requirements.");
        }
    };

    if (loading) {
        return (
            <ScreenFrame>
                <div className="flex items-center justify-center h-full text-solo-blue-500 animate-pulse text-2xl font-mono">
                    <div className="text-6xl mb-4 text-center">🌀</div>
                    ANALYZING DUNGEON STRUCTURE...
                </div>
            </ScreenFrame>
        );
    }

    if (!project) {
        return (
            <ScreenFrame>
                <div className="text-center text-red-500 mt-20">
                    <h2 className="text-2xl">Dungeon Not Found</h2>
                    <button onClick={onBack} className="mt-4 px-4 py-2 border border-gray-600 hover:text-white">Return</button>
                </div>
            </ScreenFrame>
        );
    }

    const isRedGate = project.stabilityStatus === 'BROKEN';
    const themeColor = isRedGate ? 'solo-red' : 'solo-blue';
    const bgGradient = isRedGate 
        ? 'from-solo-red-900/20 to-black' 
        : 'from-solo-blue-900/20 to-black';

    return (
        <ScreenFrame className={`bg-gradient-to-b ${bgGradient}`}>
            <div className="flex justify-between items-start mb-8 border-b border-gray-800 pb-4">
                <div>
                    <h1 className={`text-3xl font-bold text-white tracking-widest flex items-center gap-3`}>
                        <span className={`text-4xl text-${themeColor}-500`}>[</span>
                        {project.title.toUpperCase()}
                        <span className={`text-4xl text-${themeColor}-500`}>]</span>
                    </h1>
                    <div className="text-xs font-mono text-gray-500 tracking-[0.2em] mt-2 flex gap-4">
                        <span>RANK: <span className={`text-${themeColor}-400`}>{project.rank || 'UNKNOWN'}</span></span>
                        <span>STATUS: <span className={project.status === 'COMPLETED' ? 'text-green-500' : 'text-yellow-500'}>{project.status}</span></span>
                    </div>
                    <p className="text-gray-400 mt-2 max-w-2xl text-sm leading-relaxed border-l-2 border-gray-700 pl-4">
                        {project.description}
                    </p>
                </div>

                <button
                    onClick={onBack}
                    className="px-6 py-2 border border-gray-700 hover:border-white text-gray-400 hover:text-white transition-colors uppercase text-xs tracking-widest font-bold"
                >
                    Flee Dungeon
                </button>
            </div>

            <div className="space-y-4 pb-20">
                {quests.map((quest, index) => {
                    const isBossRoom = index === quests.length - 1;
                    const isCompleted = quest.state === 'COMPLETED';
                    const isPreviousCompleted = index === 0 || quests[index - 1].state === 'COMPLETED';
                    const isLocked = !isCompleted && !isPreviousCompleted;

                    return (
                        <motion.div
                            key={quest.questId}
                            initial={{ opacity: 0, x: -20 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ delay: index * 0.1 }}
                            className={`relative border p-6 rounded-lg overflow-hidden ${
                                isBossRoom 
                                    ? `border-${themeColor}-500/50 bg-${themeColor}-950/20 shadow-[0_0_15px_rgba(var(--tw-colors-${themeColor}-500),0.2)]` 
                                    : 'border-gray-800 bg-black/40'
                            } ${isLocked ? 'opacity-50 grayscale' : ''}`}
                        >
                            {/* Boss Room Indicator */}
                            {isBossRoom && (
                                <div className={`absolute top-0 right-0 w-32 h-32 bg-${themeColor}-500/10 rounded-full blur-3xl -mr-16 -mt-16 pointer-events-none`} />
                            )}

                            <div className="flex items-center justify-between relative z-10">
                                <div className="flex items-center gap-4">
                                    <div className={`p-3 rounded-full ${isCompleted ? 'bg-green-900/30 text-green-500' : isBossRoom ? `bg-${themeColor}-900/30 text-${themeColor}-500` : 'bg-gray-900 text-gray-500'}`}>
                                        {isCompleted ? <CheckCircle2 size={24} /> : isLocked ? <Lock size={24} /> : isBossRoom ? <Skull size={24} className="animate-pulse" /> : <Swords size={24} />}
                                    </div>
                                    <div>
                                        <h3 className={`text-xl font-bold tracking-wider ${isBossRoom && !isCompleted ? `text-${themeColor}-400` : 'text-white'}`}>
                                            {quest.title}
                                            {isBossRoom && <span className={`ml-3 text-xs border border-${themeColor}-500 text-${themeColor}-400 px-2 py-0.5 rounded tracking-widest`}>BOSS ROOM</span>}
                                        </h3>
                                        <div className="text-gray-400 text-sm mt-1">{quest.description}</div>
                                    </div>
                                </div>

                                <div className="flex items-center gap-6">
                                    <div className="text-right">
                                        <div className="text-xs text-gray-500 font-mono tracking-widest">REWARD</div>
                                        <div className="text-solo-blue-400 font-bold">{quest.xpReward || '?'} XP</div>
                                    </div>
                                    
                                    {!isCompleted && !isLocked && project.status !== 'COMPLETED' && (
                                        <button
                                            onClick={() => handleClearFloor(quest.questId)}
                                            className={`px-6 py-3 border uppercase text-xs tracking-widest font-bold transition-all ${
                                                isBossRoom
                                                    ? `border-${themeColor}-500 text-${themeColor}-400 hover:bg-${themeColor}-950`
                                                    : 'border-gray-600 text-white hover:border-white hover:bg-gray-800'
                                            }`}
                                        >
                                            {isBossRoom ? 'Subjugate Boss' : 'Clear Floor'}
                                        </button>
                                    )}
                                </div>
                            </div>
                        </motion.div>
                    );
                })}

                {project.status === 'COMPLETED' && (
                    <motion.div 
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="mt-8 text-center p-8 border border-green-500/50 bg-green-950/20 rounded-lg"
                    >
                        <h2 className="text-3xl font-bold text-green-400 tracking-[0.3em] mb-2">DUNGEON CLEARED</h2>
                        <p className="text-green-500/70 font-mono tracking-widest">ALL THREATS SUBJUGATED</p>
                    </motion.div>
                )}
            </div>
        </ScreenFrame>
    );
}
