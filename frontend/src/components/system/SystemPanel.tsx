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
                'rounded-lg p-4 transition-smooth',
                {
                    // Variant Styles
                    'bg-gray-800 border': variant === 'default',
                    'glass border': variant === 'glass',
                    'bg-gray-900 border': variant === 'solid',

                    // Glow Styles
                    'border-glow-cyan': glowColor === 'cyan',
                    'border-glow-teal': glowColor === 'teal',
                    'border-gray-700': glowColor === 'none',
                },
                'card-shadow',
                className
            )}
        >
            {/* Header */}
            {(title || icon) && (
                <div className="flex items-center gap-2 mb-3 pb-2 border-b border-gray-700">
                    {icon && <div className="text-cyan-400">{icon}</div>}
                    {title && (
                        <h3 className="text-sm font-semibold text-gray-300 uppercase tracking-wider">
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
