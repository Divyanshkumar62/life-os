import type { ReactNode } from 'react';
import { clsx } from 'clsx';

export interface SystemPanelProps {
    children: ReactNode;
    title?: string;
    icon?: ReactNode;
    variant?: 'default' | 'glass' | 'solid';
    glowColor?: 'cyan' | 'teal' | 'none';
    className?: string;
}

/**
 * SystemPanel - Reusable card container with purple border and optional glow
 * 
 * Responsibilities:
 * - Provide consistent card layout
 * - Apply theme-appropriate styling
 * - Support multiple visual variants
 */
export function SystemPanel({
    children,
    title,
    icon,
    variant = 'default',
    glowColor = 'cyan',
    className,
}: SystemPanelProps) {
    return (
        <div
            className={clsx(
                'rounded-none p-4 transition-smooth border',
                {
                    // Variant Styles
                    'bg-solo-bg': variant === 'default',
                    'glass-panel': variant === 'glass',
                    'bg-black': variant === 'solid',

                    // Glow Styles
                    'border-solo-cyan system-glow': glowColor === 'cyan',
                    'border-teal-500': glowColor === 'teal',
                    'border-gray-800': glowColor === 'none',
                },
                className
            )}
        >
            {/* Header */}
            {(title || icon) && (
                <div className="flex items-center gap-2 mb-3 pb-2 border-b border-solo-cyan/30">
                    {icon && <div className="text-solo-cyan">{icon}</div>}
                    {title && (
                        <h3 className="text-sm font-bold text-white uppercase tracking-widest">
                            {title}
                        </h3>
                    )}
                </div>
            )}

            {/* Content */}
            <div>{children}</div>
        </div>
    );
}
