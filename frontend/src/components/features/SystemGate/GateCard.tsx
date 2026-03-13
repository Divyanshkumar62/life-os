import React from 'react';
import { clsx } from 'clsx';
import { AlertTriangle, MapPin, Skull } from 'lucide-react';

interface GateCardProps {
    id: string;
    type: 'blue' | 'red';
    rank: string;
    title: string;
    bossName: string;
    timeLeft?: string;
    floorCount?: number;
    onClick: (id: string) => void;
}

export const GateCard: React.FC<GateCardProps> = ({
    id,
    type,
    rank,
    title,
    bossName,
    timeLeft,
    floorCount,
    onClick
}) => {
    const isRed = type === 'red';

    return (
        <div
            onClick={() => onClick(id)}
            className={clsx(
                "relative group cursor-pointer overflow-hidden transition-all duration-500 hover:scale-[1.02]",
                "border h-64 flex flex-col p-6",
                // Blue Theme
                !isRed && "bg-[#0c1220] border-solo-blue-900/50 hover:border-solo-blue-500 shadow-lg hover:shadow-solo-blue-500/20",
                // Red Theme
                isRed && "bg-[#1a0505] border-solo-red-900 hover:border-solo-red-500 shadow-lg hover:shadow-solo-red-500/30 animate-pulse-slow"
            )}
        >
            {/* Background Effects */}
            <div className={clsx(
                "absolute inset-0 opacity-10 pointer-events-none transition-opacity duration-500 group-hover:opacity-20",
                // Grid or swirls
                !isRed && "bg-[radial-gradient(circle_at_center,_var(--tw-gradient-stops))] from-solo-blue-500/30 to-transparent",
                isRed && "bg-[radial-gradient(circle_at_center,_var(--tw-gradient-stops))] from-solo-red-600/30 to-transparent"
            )} />

            {/* Rank Seal */}
            <div className={clsx(
                "absolute top-0 right-0 p-4 border-l border-b backdrop-blur-sm",
                !isRed && "border-solo-blue-900/50 bg-black/40 text-solo-blue-400",
                isRed && "border-solo-red-900/50 bg-black/60 text-solo-red-500 font-bold"
            )}>
                <span className="text-4xl font-black font-mono">{rank}</span>
            </div>

            {/* Content Header */}
            <div className="relative z-10 mt-2">
                <div className={clsx(
                    "inline-flex items-center gap-2 px-2 py-1 rounded border mb-3 text-[10px] tracking-widest font-bold uppercase",
                    !isRed && "border-solo-blue-500/30 bg-solo-blue-900/20 text-solo-blue-300",
                    isRed && "border-solo-red-500/50 bg-solo-red-900/40 text-solo-red-400 animate-pulse"
                )}>
                    {isRed ? <AlertTriangle size={12} /> : <MapPin size={12} />}
                    {isRed ? "RED GATE DETECTED" : "DUNGEON INSTANCE"}
                </div>

                <h3 className={clsx(
                    "text-xl font-bold font-sans tracking-wide max-w-[80%]",
                    !isRed && "text-white group-hover:text-solo-blue-300",
                    isRed && "text-solo-red-100 group-hover:text-white"
                )}>
                    {title}
                </h3>
            </div>

            {/* Middle Section: Boss & Details */}
            <div className="mt-auto relative z-10 space-y-3">
                <div className="flex items-center gap-3">
                    <div className={clsx(
                        "w-8 h-8 rounded flex items-center justify-center border",
                        !isRed && "bg-black border-gray-800 text-gray-500",
                        isRed && "bg-solo-red-950 border-solo-red-800 text-solo-red-500"
                    )}>
                        <Skull size={16} />
                    </div>
                    <div>
                        <div className="text-[10px] text-gray-500 tracking-wider">BOSS ENTITY</div>
                        <div className={clsx(
                            "font-bold text-sm",
                            !isRed && "text-gray-300",
                            isRed && "text-solo-red-300"
                        )}>
                            {bossName}
                        </div>
                    </div>
                </div>

                {/* Footer Info */}
                <div className="pt-3 border-t border-dashed flex justify-between items-center text-xs font-mono">
                    <span className={clsx(!isRed ? "border-gray-800 text-gray-500" : "border-solo-red-900/50 text-solo-red-400/70")}>
                        {floorCount ? `${floorCount} FLOORS` : 'UNKNOWN DEPTH'}
                    </span>

                    {timeLeft && (
                        <span className={clsx(
                            "font-bold",
                            isRed ? "text-red-500 animate-pulse" : "text-solo-blue-400"
                        )}>
                            {timeLeft}
                        </span>
                    )}
                </div>
            </div>

            {/* Hover Glitch Line (Optional) */}
            <div className={clsx(
                "absolute bottom-0 left-0 w-full h-1 transform scale-x-0 group-hover:scale-x-100 transition-transform duration-300",
                !isRed && "bg-solo-blue-500",
                isRed && "bg-solo-red-500"
            )} />
        </div>
    );
};
