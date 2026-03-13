import React from 'react';

interface StatBarProps {
    label: string;
    value: number; // 0-20
    baseValue?: number;
    maxValue?: number;
    onChange?: (val: number) => void;
    color?: string;
}

export const StatBar: React.FC<StatBarProps> = ({
    label,
    value,
    baseValue = 0,
    maxValue = 20,
    onChange,
    color = '#0ea5e9'
}) => {
    const percentage = (value / maxValue) * 100;
    const basePercentage = (baseValue / maxValue) * 100;

    return (
        <div className="flex items-center gap-4 py-2 font-mono">
            <div className="w-24 text-right text-slate-400 font-bold">{label}</div>

            <div className="flex-1 relative h-6 bg-slate-900 border border-slate-700 rounded-sm overflow-hidden flex items-center px-1">
                {/* Grid Background */}
                <div className="absolute inset-0 bg-[linear-gradient(90deg,rgba(255,255,255,0.05)_1px,transparent_1px)] bg-[size:10%_100%] pointer-events-none" />

                {/* Base Value Bar (Darker) */}
                <div
                    className="absolute h-4 top-1 left-1"
                    style={{ width: `${basePercentage}%`, backgroundColor: color, opacity: 0.3 }}
                />

                {/* Current Value Bar (Brighter) */}
                <div
                    className="absolute h-4 top-1 left-1 transition-all duration-300"
                    style={{ width: `${percentage}%`, backgroundColor: color, boxShadow: `0 0 10px ${color}` }}
                />

                {/* Value Text Overlay */}
                <div className="relative z-10 text-xs text-white drop-shadow-md ml-2">
                    {value}
                </div>
            </div>

            {onChange && (
                <div className="flex gap-1">
                    <button
                        onClick={() => onChange(Math.max(0, value - 1))}
                        className="w-6 h-6 flex items-center justify-center border border-slate-600 hover:bg-slate-800 text-slate-400 rounded-sm"
                    >
                        -
                    </button>
                    <button
                        onClick={() => onChange(Math.min(maxValue, value + 1))}
                        className="w-6 h-6 flex items-center justify-center border border-slate-600 hover:bg-slate-800 text-slate-400 rounded-sm"
                    >
                        +
                    </button>
                </div>
            )}
        </div>
    );
};
