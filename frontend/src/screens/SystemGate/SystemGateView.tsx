import { useState, useEffect } from 'react';
import { GateCard } from '../../components/features/SystemGate/GateCard';
import { ProjectAPI } from '../../api/api';
import type { Project } from '../../types/project';
import { ScreenFrame } from '../../components/layout';
import { Radar, ShieldAlert } from 'lucide-react';

interface SystemGateViewProps {
    playerId: string | null;
    onBack: () => void;
}

export function SystemGateView({ playerId, onBack }: SystemGateViewProps) {
    const [projects, setProjects] = useState<Project[]>([]);
    const [loading, setLoading] = useState(true);
    const [scanning, setScanning] = useState(false);

    const [enteringGateId, setEnteringGateId] = useState<string | null>(null);

    useEffect(() => {
        if (playerId) {
            fetchGates();
        }
    }, [playerId]);

    const fetchGates = async () => {
        try {
            setLoading(true);
            const data = await ProjectAPI.fetchProjects(playerId!);
            setProjects(data);
        } catch (error) {
            console.error("Failed to fetch gates:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleScan = () => {
        setScanning(true);
        setTimeout(() => {
            setScanning(false);
            fetchGates();
        }, 2000);
    };

    const handleEnterGate = (projectId: string) => {
        setEnteringGateId(projectId);
        setTimeout(() => {
            console.log(`Entering Gate: ${projectId}`);
            setEnteringGateId(null);
            // In future, navigate to /dungeon/:id
        }, 3000);
    };

    // Calculate system status
    const redGates = projects.filter(p => p.stabilityStatus === 'BROKEN');
    const systemStatus = redGates.length > 0 ? 'CRITICAL FAILURE' : 'STABLE';
    const statusColor = redGates.length > 0 ? 'text-solo-red-500' : 'text-solo-blue-400';

    return (
        <ScreenFrame>
            {/* Gate Transition Overlay */}
            {enteringGateId && (
                <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black animate-fade-in duration-1000">
                    <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_var(--tw-gradient-stops))] from-solo-blue-600/50 via-black to-black animate-pulse-slow scale-150" />
                    <div className="relative text-center space-y-4 animate-slide-in">
                        <div className="text-6xl animate-spin-slow">🌀</div>
                        <h2 className="text-3xl font-bold text-white tracking-[0.5em] uppercase">
                            Entering Dungeon
                        </h2>
                        <div className="text-solo-blue-400 font-mono text-sm tracking-widest animate-pulse">
                            INITIALIZING DIMENSIONAL TRANSFER...
                        </div>
                    </div>
                </div>
            )}

            {/* Header */}
            <div className="flex justify-between items-center mb-8 border-b border-gray-800 pb-4">
                <div>
                    <h1 className="text-3xl font-bold text-white tracking-widest flex items-center gap-3">
                        <span className="text-4xl text-solo-blue-500">[</span>
                        SYSTEM GATE
                        <span className="text-4xl text-solo-blue-500">]</span>
                    </h1>
                    <div className="text-xs font-mono text-gray-500 tracking-[0.2em] mt-1 pl-4">
                        DIMENSIONAL CONNECTIVITY: <span className={statusColor}>{systemStatus}</span>
                    </div>
                </div>

                <div className="flex gap-4">
                    <button
                        onClick={onBack}
                        className="px-6 py-2 border border-gray-700 hover:border-white text-gray-400 hover:text-white transition-colors uppercase text-xs tracking-widest font-bold"
                    >
                        Return
                    </button>
                    <button
                        onClick={handleScan}
                        disabled={scanning}
                        className={`px-8 py-2 bg-solo-blue-900/20 border border-solo-blue-500 text-solo-blue-400 hover:bg-solo-blue-900/40 transition-all uppercase text-xs tracking-widest font-bold flex items-center gap-2 ${scanning ? 'animate-pulse' : ''}`}
                    >
                        <Radar size={16} className={scanning ? 'animate-spin' : ''} />
                        {scanning ? 'SCANNING...' : 'SCAN GATES'}
                    </button>
                </div>
            </div>

            {/* Gate Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 overflow-y-auto max-h-[70vh] p-2">
                {loading ? (
                    <div className="col-span-full flex flex-col items-center justify-center py-20 text-solo-blue-500/50 animate-pulse">
                        <div className="text-6xl mb-4">🌀</div>
                        <div className="text-xl font-mono tracking-widest">OPENING DIMENSIONAL RIFT...</div>
                    </div>
                ) : projects.length === 0 ? (
                    <div className="col-span-full flex flex-col items-center justify-center py-20 text-gray-600 border border-dashed border-gray-800 rounded bg-black/20">
                        <ShieldAlert size={48} className="mb-4 opacity-50" />
                        <div className="text-lg font-mono tracking-widest">NO GATES DETECTED</div>
                        <div className="text-xs mt-2">The dungeon is quiet... for now.</div>
                    </div>
                ) : (
                    projects.map(project => (
                        <GateCard
                            key={project.projectId}
                            id={project.projectId}
                            type={project.stabilityStatus === 'BROKEN' ? 'red' : 'blue'}
                            rank={project.rank || 'E'}
                            title={project.title}
                            bossName={project.bossName || 'Unknown Entity'}
                            floorCount={project.floorsTotal}
                            timeLeft={project.stabilityStatus === 'BROKEN' ? 'INVASION IMMINENT' : undefined}
                            onClick={handleEnterGate}
                        />
                    ))
                )}
            </div>

            {/* Footer Warning if Red Gate exists */}
            {redGates.length > 0 && (
                <div className="fixed bottom-0 left-0 right-0 bg-solo-red-950/90 border-t-4 border-solo-red-600 p-4 flex items-center justify-center gap-4 animate-bounce-subtle z-50">
                    <AlertTriangle size={24} className="text-solo-red-500 animate-pulse" />
                    <div className="text-solo-red-100 font-bold tracking-widest text-lg">
                        WARNING: DUNGEON BREAK DETECTED. IMMEDIATE SUBJUGATION REQUIRED.
                    </div>
                    <AlertTriangle size={24} className="text-solo-red-500 animate-pulse" />
                </div>
            )}
        </ScreenFrame>
    );
}

// Helper for Footer
import { AlertTriangle } from 'lucide-react';
