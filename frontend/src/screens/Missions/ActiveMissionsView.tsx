// import { clsx } from "clsx";

interface ActiveMissionsViewProps {
    onBack?: () => void;
}

export function ActiveMissionsView({ onBack }: ActiveMissionsViewProps) {
    return (
        <div className="min-h-screen w-full bg-[#050911] text-white flex flex-col font-mono selection:bg-cyan-500/30 overflow-x-hidden">

            {/* Header */}
            <header className="flex justify-between items-center px-8 py-4 border-b border-cyan-900/30 bg-[#050911]/95 backdrop-blur-sm sticky top-0 z-50">
                <div className="flex items-center gap-4">
                    <div className="w-10 h-10 bg-cyan-900/20 flex items-center justify-center rounded border border-cyan-500/30">
                        <span className="text-cyan-400 font-bold text-lg">{`>_`}</span>
                    </div>
                    <div className="flex flex-col">
                        <span className="text-xl font-black tracking-widest text-white">SYSTEM_OS</span>
                        <span className="text-[10px] tracking-[0.2em] text-gray-500 font-bold">VER 4.1.2 // ADMIN ACCESS</span>
                    </div>
                </div>

                <div className="flex items-center gap-4">
                    <div className="px-4 py-1.5 border border-cyan-500/30 bg-cyan-900/10 rounded flex items-center gap-2">
                        <div className="w-2 h-2 rounded-full bg-cyan-500 animate-pulse shadow-[0_0_8px_rgba(6,182,212,0.8)]" />
                        <span className="text-[10px] tracking-widest text-cyan-400 font-bold">SYSTEM_STATUS: ONLINE</span>
                    </div>
                    <div className="flex gap-2">
                        <button className="w-9 h-9 border border-gray-700 hover:border-cyan-500/50 flex items-center justify-center rounded bg-[#0a0a0a] transition-all text-gray-400 hover:text-cyan-400">
                            ⚙
                        </button>
                        <button className="w-9 h-9 border border-gray-700 hover:border-cyan-500/50 flex items-center justify-center rounded bg-[#0a0a0a] transition-all text-gray-400 hover:text-cyan-400">
                            🔔
                        </button>
                        <button
                            onClick={onBack}
                            className="w-9 h-9 border border-gray-700 hover:border-red-500/50 flex items-center justify-center rounded bg-[#0a0a0a] transition-all text-gray-400 hover:text-red-400 font-bold text-xs"
                        >
                            SJ
                        </button>
                    </div>
                </div>
            </header>

            {/* Main Content */}
            <div className="flex-1 p-8 flex flex-col gap-8 relative">

                {/* Background Pattern */}
                <div className="absolute inset-0 opacity-[0.03] pointer-events-none fixed"
                    style={{
                        backgroundImage: 'linear-gradient(rgba(6, 182, 212, 0.1) 1px, transparent 1px), linear-gradient(90deg, rgba(6, 182, 212, 0.1) 1px, transparent 1px)',
                        backgroundSize: '30px 30px'
                    }}
                />

                {/* Page Title */}
                <div className="relative z-10 animate-slide-in">
                    <h1 className="text-3xl font-bold tracking-wider mb-1 flex items-center gap-3">
                        <span className="text-cyan-500 text-4xl">[</span>
                        <span className="tracking-[0.1em]">SYSTEM INTERFACE : ACTIVE_MISSIONS</span>
                        <span className="text-cyan-500 text-4xl">]</span>
                    </h1>
                    <div className="text-sm font-mono text-gray-400 tracking-widest pl-6 flex items-center gap-2">
                        <span className="text-cyan-500">&gt;</span> RETRIEVING CURRENT USER OBJECTIVES...
                    </div>
                </div>

                {/* Top Statistics Row */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 relative z-10">

                    {/* Capacity Card */}
                    <div className="lg:col-span-2 bg-[#0c1220] border border-gray-800 p-6 relative overflow-hidden group hover:border-cyan-900/50 transition-colors">
                        <div className="flex justify-between items-start mb-6">
                            <div>
                                <div className="text-[10px] tracking-[0.2em] text-gray-400 font-bold mb-1">SLOT UTILIZATION</div>
                                <div className="flex items-baseline gap-2">
                                    <span className="text-5xl font-mono text-white">1/1</span>
                                    <span className="text-xs tracking-widest text-purple-400 font-bold uppercase">Slots Engaged</span>
                                </div>
                            </div>
                            <div className="text-right">
                                <div className="text-[10px] tracking-[0.2em] text-gray-400 font-bold mb-1">SYSTEM CAPACITY LOAD</div>
                                <div className="text-right text-cyan-400 font-bold text-sm">100%</div>
                            </div>
                        </div>

                        {/* Progress Bar */}
                        <div className="relative h-4 bg-gray-900 w-full mb-3 rounded-sm overflow-hidden">
                            <div className="absolute inset-0 bg-[repeating-linear-gradient(45deg,rgba(147,51,234,0.1),rgba(147,51,234,0.1)_10px,transparent_10px,transparent_20px)] opacity-50" />
                            <div className="h-full bg-gradient-to-r from-purple-600 to-purple-500 w-full shadow-[0_0_15px_rgba(168,85,247,0.5)] bg-[length:20px_20px] bg-[linear-gradient(45deg,rgba(255,255,255,0.1)_25%,transparent_25%,transparent_50%,rgba(255,255,255,0.1)_50%,rgba(255,255,255,0.1)_75%,transparent_75%,transparent)]" />
                        </div>

                        <div className="text-[9px] font-mono text-red-400 tracking-wider">
                            &gt; WARNING: MAXIMUM CAPACITY REACHED. COMPLETE ACTIVE MISSION TO FREE RESOURCES.
                        </div>
                    </div>

                    {/* Daily Reset Card */}
                    <div className="bg-[#0c1220] border border-gray-800 p-6 flex flex-col justify-center relative overflow-hidden group hover:border-cyan-900/50 transition-colors">
                        <div className="flex items-center gap-2 mb-4">
                            <div className="animate-spin text-gray-500">⏱</div>
                            <div className="text-[10px] tracking-[0.2em] text-gray-400 font-bold uppercase">Daily Reset</div>
                        </div>
                        <div className="text-4xl font-mono font-bold text-white tracking-widest mb-2">
                            04 : 23 : 59
                        </div>
                        <div className="text-[10px] tracking-widest text-gray-600">
                            UTC +00:00
                        </div>
                    </div>
                </div>

                {/* Main Mission Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 relative z-10 flex-1 min-h-[400px]">

                    {/* Active Mission Card - Left */}
                    <div className="border border-purple-500/30 bg-[#0c1220] flex flex-col relative group">

                        {/* Status Label */}
                        <div className="absolute top-6 left-6 z-20">
                            <span className="bg-purple-600 text-white text-[10px] font-bold px-3 py-1 tracking-widest uppercase shadow-[0_0_10px_rgba(147,51,234,0.4)]">
                                Status: Active
                            </span>
                        </div>

                        {/* Rank Reward Label */}
                        <div className="absolute md:top-48 md:right-6 bottom-40 right-6 z-20">
                            <span className="bg-[#1a1a00] border border-yellow-500/30 text-yellow-500 text-[10px] font-bold px-3 py-1 tracking-widest uppercase flex items-center gap-2">
                                <span>🏆</span> Rank E Reward
                            </span>
                        </div>

                        {/* Image Area */}
                        <div className="h-56 w-full relative overflow-hidden border-b border-purple-500/20">
                            <div className="absolute inset-0 bg-purple-900/10 z-10 mix-blend-overlay" />
                            <div className="absolute inset-0 bg-[url('https://cdn.dribbble.com/users/188836/screenshots/15474668/media/c0353c70f0321a6907534433d7b4b726.png?resize=1600x1200&vertical=center')] bg-cover bg-center grayscale opacity-60 group-hover:grayscale-0 transition-all duration-700" />
                            <div className="absolute inset-0 bg-gradient-to-t from-[#0c1220] to-transparent" />
                        </div>

                        {/* Content Area */}
                        <div className="p-8 flex-1 flex flex-col">
                            <div className="flex justify-between items-start mb-2">
                                <div className="text-[11px] font-bold tracking-[0.15em] text-purple-400">
                                    MISSION_ID: #004
                                </div>
                                <div className="flex gap-1">
                                    <div className="w-1 h-1 bg-gray-600 rounded-full" />
                                    <div className="w-1 h-1 bg-gray-600 rounded-full" />
                                    <div className="w-1 h-1 bg-gray-600 rounded-full" />
                                </div>
                            </div>

                            <h2 className="text-2xl font-bold text-white mb-2 tracking-wide font-sans">
                                LEARN TYPESCRIPT: GENERICS
                            </h2>

                            <p className="text-gray-400 text-xs leading-relaxed mb-6 font-mono">
                                Complete the advanced module on generic constraints and utility types. Submit final codebase for review.
                            </p>

                            {/* Progress Section */}
                            <div className="mt-auto">
                                <div className="flex justify-between text-[10px] font-bold tracking-widest text-gray-500 mb-2">
                                    <span>COMPLETION RATE</span>
                                    <span>45%</span>
                                </div>
                                <div className="h-1.5 bg-gray-800 w-full rounded-sm overflow-hidden mb-4">
                                    <div className="h-full bg-purple-600 w-[45%] shadow-[0_0_10px_rgba(147,51,234,0.5)]" />
                                </div>

                                <div className="text-[10px] font-mono text-purple-500/70 tracking-wider mb-6 animate-pulse">
                                    &gt;&gt; DIAGNOSTIC SCANNING ACTIVE...
                                </div>

                                <div className="flex gap-3">
                                    <button className="flex-1 bg-purple-600 hover:bg-purple-500 text-white font-bold py-3 text-xs tracking-[0.2em] transition-all shadow-[0_0_20px_rgba(147,51,234,0.3)]">
                                        CHECK IN
                                    </button>
                                    <button className="w-12 border border-gray-700 hover:border-purple-500 hover:text-purple-500 text-gray-500 flex items-center justify-center transition-all bg-[#080c14]">
                                        👁
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Locked Slot - Right */}
                    <div className="border border-gray-800 border-dashed bg-[#080c14]/50 flex flex-col items-center justify-center relative overflow-hidden group">

                        {/* Stamp */}
                        <div className="absolute top-8 right-8 transform rotate-12 border-2 border-red-900 text-red-900 px-4 py-2 text-xl font-black uppercase tracking-widest opacity-40 select-none">
                            Limit Exceeded
                        </div>

                        <div className="bg-[#0c1220] w-16 h-16 rounded border border-gray-700 flex items-center justify-center mb-6 shadow-xl">
                            <span className="text-2xl text-gray-600">🔒</span>
                        </div>

                        <h3 className="text-lg font-bold text-gray-400 tracking-[0.2em] mb-3 uppercase">
                            System Lock
                        </h3>

                        <div className="text-center text-[10px] text-gray-600 font-mono tracking-wider leading-relaxed max-w-[250px] mb-8">
                            SLOT LIMIT REACHED.<br />
                            COMPLETE ACTIVE MISSION TO<br />
                            UNLOCK NEW OBJECTIVE SLOTS.
                        </div>

                        <button disabled className="border border-gray-700 text-gray-600 px-8 py-3 text-[10px] tracking-[0.2em] font-bold cursor-not-allowed hover:bg-white/5 transition-colors">
                            + INITIATE_NEW_MISSION
                        </button>
                    </div>

                </div>
            </div>

            {/* Footer Logs */}
            <div className="px-8 py-4 border-t border-gray-800 bg-[#050911] font-mono text-[9px] text-gray-600 space-y-1">
                <div>&gt; SYSTEM_LOG: User login detected at 14:02 UTC.</div>
                <div>&gt; SYSTEM_LOG: Mission #004 progress updated +15%.</div>
                <div className="text-purple-500/80">&gt; SYSTEM_LOG: WARNING - Utilization at 100%. Efficiency dropping.</div>
                <div className="mt-2 text-cyan-500 animate-pulse font-bold tracking-widest flex items-center gap-2">
                    <span className="w-1.5 h-1.5 bg-cyan-500 rounded-full" />
                    SYSTEM ONLINE // WAITING FOR INPUT...
                </div>
            </div>

        </div>
    );
}
