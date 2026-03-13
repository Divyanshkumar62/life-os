import React from 'react';

interface SystemBadgeProps {
    label?: string;
    variant?: 'default' | 'success' | 'warning' | 'error' | 'info';
    pulsing?: boolean;
    children?: React.ReactNode;
    size?: string;
    glow?: boolean;
}

export const SystemBadge: React.FC<SystemBadgeProps> = ({
    label,
    variant = 'default',
    pulsing = false,
    children,
    size = 'sm',
    glow = false,
}) => {
    const variants = {
        default: 'bg-slate-800 text-slate-300 border-slate-600',
        success: 'bg-green-900/30 text-green-400 border-green-800',
        warning: 'bg-amber-900/30 text-amber-400 border-amber-800',
        error: 'bg-red-900/30 text-red-400 border-red-800',
        info: 'bg-blue-900/30 text-cyan-400 border-blue-800',
    };

    return (
        <span className={`
      px-2 py-0.5 rounded text-[10px] font-mono uppercase tracking-wider border
      ${variants[variant]}
      ${pulsing ? 'animate-pulse' : ''}
      ${glow ? 'shadow-[0_0_10px_currentColor]' : ''}
      ${size === 'lg' ? 'text-sm px-3 py-1' : ''}
    `}>
            {children || label}
        </span>
    );
};
