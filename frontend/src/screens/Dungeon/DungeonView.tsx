import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { ProjectAPI, QuestAPI } from '../../api/api';
import type { Project, Quest } from '../../types/project';
import { ScreenFrame } from '../../components/layout';
import { Skull, Swords, Lock, CheckCircle2 } from 'lucide-react';
import { SpeedrunResultsScreen } from '../../features/dungeon/components/SpeedrunResultsScreen';
import type { SpeedrunResultDTO } from '../../features/dungeon/components/SpeedrunResultsScreen';
import { usePlayerStore } from '../../stores/usePlayerStore';

interface DungeonViewProps {
    playerId: string | null;
    projectId: string;
    onBack: () => void;
}

export function DungeonView({ projectId, onBack }: DungeonViewProps) {
    const [project, setProject] = useState<Project | null>(null);
    const [quests, setQuests] = useState<Quest[]>([]);
    const [loading, setLoading] = useState(true);
    const [speedrunResult, setSpeedrunResult] = useState<SpeedrunResultDTO | null>(null);

    const { penaltyActive } = usePlayerStore();

    useEffect(() => {
        fetchDungeonData();
    }, [projectId]);

    useEffect(() => {
        if (project && project.status === 'COMPLETED' && !speedrunResult) {
            ProjectAPI.completeProject(projectId)
                .then(res => setSpeedrunResult(res))
                .catch(err => console.error("Failed to load speedrun results:", err));
        }
    }, [project, projectId, speedrunResult]);

    const fetchDungeonData = async () => {
        try {
            setLoading(true);
            const [projData, questsData] = await Promise.all([
                ProjectAPI.getProject(projectId),
                ProjectAPI.getProjectQuests(projectId)
            ]);
            setProject(projData);
            setQuests(questsData);
        } catch (error) {
            console.error("Failed to fetch dungeon:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleClearFloor = async (questId: string, isBossRoom: boolean) => {
        try {
            await QuestAPI.completeQuest(questId);
            if (isBossRoom) {
                try {
                    const res = await ProjectAPI.completeProject(projectId);
                    setSpeedrunResult(res);
                } catch (e) {
                    console.error("Failed to complete project:", e);
                }
            }
            fetchDungeonData();
        } catch (error) {
            console.error("Failed to clear floor:", error);
            alert("Failed to clear floor. Make sure you meet the requirements.");
        }
    };

    if (loading) {
        return (
            <ScreenFrame>
                <div className="flex items-center justify-center h-full text-solo-cyan animate-pulse text-2xl font-mono">
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
    const themeColor = isRedGate ? 'solo-red' : 'solo-cyan';
    const bgGradient = isRedGate 
        ? 'from-solo-red/20 to-black' 
        : 'from-solo-cyan/20 to-black';

    return (
        <ScreenFrame className={`bg-gradient-to-b ${bgGradient}`}>
            <div className="flex justify-between items-start mb-8 border-b border-gray-800 pb-4">
                <div>
                    <h1 className={`text-3xl font-bold text-white tracking-widest flex items-center gap-3`}>
                        <span className={`text-4xl text-${themeColor}`}>[</span>
                        {project.title.toUpperCase()}
                        <span className={`text-4xl text-${themeColor}`}>]</span>
                    </h1>
                    <div className="text-xs font-mono text-gray-500 tracking-widest mt-2 flex flex-wrap gap-4 uppercase">
                        <span>RANK: <span className={`text-${themeColor}`}>{project.rank || 'UNKNOWN'}</span></span>
                        <span>STATUS: <span className={project.status === 'COMPLETED' ? 'text-green-500' : 'text-solo-gold'}>{project.status}</span></span>
                        {project.hardDeadline && (
                            <span>DEADLINE: <span className="text-red-500">{new Date(project.hardDeadline).toLocaleString()}</span></span>
                        )}
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

            <div className="relative min-h-[300px]">
                {penaltyActive && (
                    <div className="absolute inset-0 z-40 bg-black/80 border border-red-600/50 backdrop-blur-sm flex flex-col items-center justify-center text-center p-8">
                        <motion.div
                            initial={{ scale: 0.9, opacity: 0 }}
                            animate={{ scale: 1, opacity: 1 }}
                            className="space-y-3"
                        >
                            <Skull size={40} className="mx-auto text-red-500 animate-pulse" />
                            <h2 className="text-3xl font-extrabold text-red-500 tracking-[0.2em] uppercase">
                                Floors Sealed
                            </h2>
                            <p className="text-red-400 font-mono tracking-widest text-xs uppercase">
                                Penalty Zone Active
                            </p>
                            <p className="text-gray-500 text-[10px] max-w-md uppercase leading-relaxed mt-2 mx-auto">
                                All floor interactions are locked by the system. You must resolve the active survival quest to restore access to this gate.
                            </p>
                        </motion.div>
                    </div>
                )}

                <div className="space-y-4 pb-20">
                    {quests.map((quest, index) => {
                        const isBossRoom = index === quests.length - 1;
                        const isCompleted = quest.state === 'COMPLETED';
                        const isPreviousCompleted = index === 0 || quests[index - 1].state === 'COMPLETED';
                        const isLocked = !isCompleted && !isPreviousCompleted;

                        return (
                            <motion.div
                                key={quest.questId}
                                initial={{ opacity: 0, scale: 0.98 }}
                                animate={{ opacity: 1, scale: 1 }}
                                transition={{ delay: index * 0.05, duration: 0.2, ease: "easeOut" }}
                                className={`relative border p-6 rounded-none overflow-hidden ${
                                    isBossRoom 
                                        ? `border-${themeColor}/50 bg-${themeColor}/20 shadow-[0_0_15px_rgba(var(--tw-colors-${themeColor}),0.2)]` 
                                        : 'border-gray-800 glass-panel'
                                } ${isLocked ? 'opacity-50 grayscale' : ''}`}
                            >
                                {isBossRoom && (
                                    <div className={`absolute top-0 right-0 w-32 h-32 bg-${themeColor}/10 rounded-full blur-3xl -mr-16 -mt-16 pointer-events-none`} />
                                )}

                                <div className="flex items-center justify-between relative z-10">
                                    <div className="flex items-center gap-4">
                                        <div className={`p-3 rounded-none ${isCompleted ? 'bg-green-900/30 text-green-500' : isBossRoom ? `bg-${themeColor}/30 text-${themeColor}` : 'bg-gray-900 text-gray-500'}`}>
                                            {isCompleted ? <CheckCircle2 size={24} /> : isLocked ? <Lock size={24} /> : isBossRoom ? <Skull size={24} className="animate-pulse" /> : <Swords size={24} />}
                                        </div>
                                        <div>
                                            <h3 className={`text-xl font-bold tracking-wider uppercase ${isBossRoom && !isCompleted ? `text-${themeColor}` : 'text-white'}`}>
                                                {quest.title}
                                                {isBossRoom && <span className={`ml-3 text-xs border border-${themeColor} text-${themeColor} px-2 py-0.5 rounded-none tracking-widest`}>BOSS ROOM</span>}
                                            </h3>
                                            <div className="text-gray-400 text-sm mt-1 uppercase">{quest.description}</div>
                                        </div>
                                    </div>

                                    <div className="flex items-center gap-6">
                                        <div className="text-right">
                                            <div className="text-xs text-gray-500 font-mono tracking-widest uppercase">REWARD</div>
                                            <div className="text-solo-cyan font-bold font-mono">{quest.xpReward || '?'} XP</div>
                                        </div>
                                        
                                        {!isCompleted && !isLocked && project.status !== 'COMPLETED' && (
                                            <button
                                                onClick={() => handleClearFloor(quest.questId, isBossRoom)}
                                                className={`px-6 py-3 border rounded-none uppercase text-xs tracking-widest font-bold transition-all ${
                                                    isBossRoom
                                                        ? `border-${themeColor} text-${themeColor} hover:bg-${themeColor}/20 system-glow`
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
            </div>

            <SpeedrunResultsScreen result={speedrunResult} onClose={() => setSpeedrunResult(null)} />
        </ScreenFrame>
    );
}
