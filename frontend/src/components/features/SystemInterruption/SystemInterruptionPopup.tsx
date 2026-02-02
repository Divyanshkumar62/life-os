import { SystemButton } from '../../system';

export interface SystemInterruptionPopupProps {
    isOpen: boolean;
    onClose: () => void;
}

export function SystemInterruptionPopup({ isOpen, onClose }: SystemInterruptionPopupProps) {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black/95 backdrop-blur-md transition-opacity duration-300"
                onClick={onClose}
            />

            {/* Main Modal Container */}
            <div className="relative z-10 w-full max-w-lg mx-4 animate-in fade-in zoom-in-95 duration-500">

                {/* Outer Border with Corners */}
                <div className="absolute -inset-[2px] border border-cyan-500/30 rounded-sm pointer-events-none">
                    <div className="absolute top-0 left-0 w-2 h-2 bg-cyan-400" />
                    <div className="absolute top-0 right-0 w-2 h-2 bg-cyan-400" />
                    <div className="absolute bottom-0 left-0 w-2 h-2 bg-cyan-400" />
                    <div className="absolute bottom-0 right-0 w-2 h-2 bg-cyan-400" />
                </div>

                {/* Card Content */}
                <div className="bg-[#050b14] border border-cyan-500/50 p-1 shadow-[0_0_50px_rgba(34,211,238,0.15)] relative overflow-hidden flex flex-col items-stretch">

                    {/* Header Bar */}
                    <div className="h-8 bg-cyan-950/40 border-b border-cyan-500/30 flex items-center justify-between px-3">
                        <div className="flex items-center gap-2 text-cyan-500 text-[10px] font-mono font-bold tracking-widest uppercase">
                            <span className="text-sm">⚠️</span> SYSTEM INTERRUPTION
                        </div>
                        <div className="flex gap-1">
                            <div className="w-1 h-1 bg-cyan-500 rounded-full" />
                            <div className="w-1 h-1 bg-cyan-500 rounded-full" />
                            <div className="w-1 h-1 bg-cyan-500 rounded-full" />
                        </div>
                    </div>

                    {/* Content Body */}
                    <div className="p-8 flex flex-col items-center text-center relative">
                        {/* Background Scanlines */}
                        <div className="absolute inset-0 bg-[linear-gradient(rgba(18,18,18,0)_50%,rgba(0,0,0,0.25)_50%),linear-gradient(90deg,rgba(255,0,0,0.06),rgba(255,0,0,0.02),rgba(255,0,0,0.06))] pointer-events-none bg-[length:100%_4px,6px_100%] opacity-20" />

                        {/* Tag */}
                        <div className="border border-cyan-800 bg-black/50 px-3 py-1 mb-6 backdrop-blur">
                            <span className="text-[10px] text-cyan-400 font-mono tracking-[0.2em] uppercase">
                                [ System Notification ]
                            </span>
                        </div>

                        {/* Title */}
                        <h2 className="text-3xl md:text-4xl font-black text-white tracking-tighter mb-1 drop-shadow-[0_0_10px_rgba(255,255,255,0.5)] glitch-text" data-text="ATTENTION REQUIRED">
                            ATTENTION REQUIRED
                        </h2>
                        <div className="text-lg md:text-xl font-bold text-cyan-400 font-mono tracking-widest mb-8 drop-shadow-[0_0_15px_rgba(34,211,238,0.8)]">
                            // ERROR_CODE_00
                        </div>

                        {/* Message Box */}
                        <div className="relative w-full mb-8">
                            <div className="absolute left-0 top-2 bottom-2 w-[1px] bg-cyan-800" />
                            <div className="absolute right-0 top-2 bottom-2 w-[1px] bg-cyan-800" />
                            <p className="px-6 text-gray-400 text-sm leading-relaxed font-mono">
                                The System challenges you. Failure to accept will result in a permanent penalty to your stats.
                            </p>
                            <div className="flex justify-center mt-4">
                                <span className="text-cyan-900 text-[8px]">♥</span>
                            </div>
                        </div>

                        {/* Info Grid */}
                        <div className="w-full grid grid-cols-2 gap-px bg-cyan-900/30 border border-cyan-900/30 mb-6">
                            <div className="bg-[#080f1a] p-3 flex justify-between items-center">
                                <span className="text-[9px] text-cyan-600 font-mono uppercase">System_ID</span>
                                <span className="text-[10px] text-white font-mono">#884-A</span>
                            </div>
                            <div className="bg-[#080f1a] p-3 flex justify-between items-center">
                                <span className="text-[9px] text-cyan-600 font-mono uppercase">Alert_Level</span>
                                <span className="text-[10px] text-red-500 font-mono uppercase font-bold animate-pulse">Critical</span>
                            </div>
                            <div className="bg-[#080f1a] p-3 flex justify-between items-center">
                                <span className="text-[9px] text-cyan-600 font-mono uppercase">Time_Rem</span>
                                <span className="text-[10px] text-white font-mono flex items-center gap-1">
                                    <span className="text-cyan-400">∞</span> INVALID
                                </span>
                            </div>
                            <div className="bg-[#080f1a] p-3 flex justify-between items-center">
                                <span className="text-[9px] text-cyan-600 font-mono uppercase">Penalty</span>
                                <span className="text-[10px] text-purple-400 font-mono uppercase">Unknown</span>
                            </div>
                        </div>

                        {/* Synchronization Bar */}
                        <div className="w-full mb-8">
                            <div className="flex justify-between text-[9px] text-cyan-500 font-mono mb-1 uppercase tracking-wider">
                                <span>System Synchronization</span>
                                <span>100%</span>
                            </div>
                            <div className="h-1 w-full bg-gray-800">
                                <div className="h-full w-full bg-gradient-to-r from-cyan-600 via-cyan-400 to-white shadow-[0_0_10px_rgba(34,211,238,0.5)]" />
                            </div>
                            <div className="text-right text-[8px] text-gray-600 mt-1 uppercase font-mono">
                                Ready for Input
                            </div>
                        </div>

                        {/* Button */}
                        <SystemButton
                            variant="primary"
                            fullWidth
                            className="h-14 bg-cyan-900/20 border border-cyan-500/50 hover:bg-cyan-500 hover:text-black hover:border-cyan-400 transition-all duration-300 group relative overflow-hidden"
                            onClick={() => { }}
                        >
                            <span className="relative z-10 flex items-center justify-center gap-2 text-sm font-bold tracking-[0.2em] uppercase">
                                <span className="bg-cyan-400 text-black rounded-full w-4 h-4 flex items-center justify-center text-[10px]">✓</span>
                                Accept Mission
                            </span>
                            {/* Hover Fill Effect */}
                            <div className="absolute inset-x-0 bottom-0 h-0 bg-cyan-500 transition-all duration-300 group-hover:h-full opacity-20" />
                        </SystemButton>

                    </div>
                </div>
            </div>
        </div>
    );
}
