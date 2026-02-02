import { SystemButton } from '../../system';

export interface PenaltyPopupProps {
    isOpen: boolean;
    onClose: () => void;
}

export function PenaltyPopup({ isOpen, onClose }: PenaltyPopupProps) {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop / Overlay */}
            <div
                className="absolute inset-0 bg-black/95 backdrop-blur-sm"
                onClick={onClose}
            />

            {/* Main Modal Container */}
            <div className="relative z-10 w-full max-w-5xl mx-4 animate-in fade-in zoom-in duration-300">

                {/* Top Alert Banner */}
                <div className="flex items-center justify-between mb-2 text-cyan-400">
                    <div className="flex items-center gap-3">
                        <span className="text-3xl">⚠️</span>
                        <div>
                            <h2 className="text-xl font-bold tracking-widest uppercase font-mono">Penalty Zone Active</h2>
                            <p className="text-xs text-cyan-600 tracking-[0.2em] font-mono">SYSTEM LOCKDOWN INITIATED // EXTERNAL ACCESS DISABLED</p>
                        </div>
                    </div>
                    <div className="flex items-center gap-2">
                        <div className="w-2 h-2 rounded-full bg-cyan-500 animate-pulse" />
                        <span className="text-xs font-mono tracking-wider">LIVE MONITORING</span>
                    </div>
                </div>

                {/* Main Card Content */}
                <div className="grid grid-cols-1 md:grid-cols-2 bg-gray-900 border-2 border-cyan-500 shadow-[0_0_50px_rgba(34,211,238,0.3)]">

                    {/* LEFT COLUMN: Visual/Image */}
                    <div className="relative h-96 md:h-auto bg-gray-800 overflow-hidden group">
                        {/* "MANDATORY" Tag */}
                        <div className="absolute top-0 left-0 bg-cyan-600 text-black font-bold text-xs px-3 py-1 uppercase tracking-widest z-10">
                            Mandatory
                        </div>

                        {/* Image Placeholder (Desolate Landscape feel) */}
                        <div className="absolute inset-0 bg-gradient-to-b from-gray-900 via-gray-800 to-black opacity-80" />
                        <div className="absolute inset-0 flex items-center justify-center p-8">
                            <div className="w-48 h-48 rounded-full bg-gradient-to-tr from-cyan-900/50 to-gray-900 border border-cyan-500/30 flex items-center justify-center shadow-[0_0_30px_rgba(34,211,238,0.1)]">
                                <span className="text-6xl grayscale opacity-50">🏃</span>
                            </div>
                        </div>

                        {/* Overlay Grid/Scanlines */}
                        <div className="absolute inset-0 bg-[linear-gradient(rgba(18,18,18,0)_50%,rgba(0,0,0,0.25)_50%),linear-gradient(90deg,rgba(255,0,0,0.06),rgba(255,0,0,0.02),rgba(255,0,0,0.06))] z-0 pointer-events-none bg-[length:100%_4px,6px_100%]" />

                        {/* Bottom Info */}
                        <div className="absolute bottom-6 left-6 right-6">
                            <div className="flex items-center gap-2 text-cyan-400 mb-2">
                                <span className="text-lg">📍</span>
                                <span className="font-mono text-sm tracking-widest">PENALTY ZONE [OUTDOORS]</span>
                            </div>
                            <div className="inline-block px-2 py-1 border border-cyan-500/50 bg-cyan-500/10 text-[10px] text-cyan-300 font-mono tracking-wider">
                                TEMP: 45°C // DANGER: S-RANK
                            </div>
                        </div>
                    </div>

                    {/* RIGHT COLUMN: Details & Actions */}
                    <div className="p-8 flex flex-col justify-between bg-black/50 backdrop-blur">

                        {/* Header */}
                        <div>
                            <div className="flex justify-between items-start mb-4">
                                <h1 className="text-4xl font-bold text-white uppercase tracking-tight font-mono">
                                    Survival: 10KM Run
                                </h1>
                                <div className="text-4xl opacity-20 font-black text-gray-700">!</div>
                            </div>
                            <p className="text-gray-400 text-sm leading-relaxed mb-8">
                                The system has detected a lack of discipline. Complete the objective to restore system access. Failure is not an option.
                            </p>
                        </div>

                        {/* Progress Section */}
                        <div className="bg-gray-800/50 p-6 border border-gray-700 mb-6">
                            <div className="flex justify-between items-end mb-2">
                                <span className="text-sm text-cyan-500 font-mono tracking-widest uppercase">Distance Covered</span>
                                <div className="text-white font-mono">
                                    <span className="text-2xl font-bold">0.0</span>
                                    <span className="text-gray-500 text-sm"> / 10.0 KM</span>
                                </div>
                            </div>

                            {/* Segmented Progress Bar Look */}
                            <div className="h-4 w-full flex gap-1">
                                {[...Array(10)].map((_, i) => (
                                    <div key={i} className="flex-1 bg-gray-800 border border-gray-700 first:rounded-l last:rounded-r" />
                                ))}
                            </div>
                            <div className="flex justify-between text-[10px] text-gray-600 font-mono mt-1 uppercase">
                                <span>Start</span>
                                <span>50%</span>
                                <span>Target</span>
                            </div>
                        </div>

                        {/* Stats Grid */}
                        <div className="grid grid-cols-2 gap-4 mb-8">
                            <div className="bg-gray-900 border-l-2 border-cyan-500 p-3">
                                <span className="block text-[10px] text-gray-500 uppercase tracking-wider mb-1">Time Remaining</span>
                                <div className="flex items-baseline gap-2">
                                    <span className="text-xl font-mono text-white font-bold">03:59:59</span>
                                    <span className="text-[10px] text-cyan-500 animate-pulse">Running</span>
                                </div>
                            </div>
                            <div className="bg-gray-900 border-l-2 border-white p-3">
                                <span className="block text-[10px] text-gray-500 uppercase tracking-wider mb-1">Penalty Status</span>
                                <div className="flex items-baseline gap-2">
                                    <span className="text-xl font-mono text-white font-bold">ACTIVE</span>
                                </div>
                            </div>
                        </div>

                        {/* Action Button */}
                        <SystemButton
                            variant="primary"
                            fullWidth
                            size="lg"
                            className="h-16 text-lg tracking-[0.2em] shadow-[0_0_20px_rgba(34,211,238,0.4)] hover:shadow-[0_0_30px_rgba(34,211,238,0.6)]"
                            onClick={() => { }} // Placeholder
                        >
                            <span className="mr-4">🏃</span>
                            BEGIN TRACKING
                        </SystemButton>
                    </div>
                </div>

                {/* Decoration Footer */}
                <div className="mt-4 flex justify-between items-end opacity-50">
                    <div className="text-[10px] text-cyan-900 font-mono tracking-[0.3em]">
                        SYS_ID_8473_PENALTY_PROTOCOL_V1
                    </div>

                    {/* Mini System Log */}
                    <div className="hidden md:block w-64 text-[10px] font-mono text-cyan-800 text-right">
                        <p>&gt; [SYS] User located: Sector 7G</p>
                        <p>&gt; [SYS] Locking external tools...</p>
                        <p>&gt; [ERR] Escape attempt blocked.</p>
                    </div>
                </div>
            </div>
        </div>
    );
}
