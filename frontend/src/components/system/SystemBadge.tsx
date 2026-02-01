import type { ReactNode } from 'react';
import { clsx } from 'clsx';

export interface SystemBadgeProps {
    children: ReactNode;
    variant?: 'success' | 'warning' | 'error' | 'info' | 'cyan' | 'teal' | 'blue';
    size?: 'sm' | 'md' | 'lg';
    glow?: boolean;
    className?: string;
}

/**
 * SystemBadge - Colored badge/label with optional glow
 * 
 * Responsibilities:
 * - Display status or category labels
 * - Support multiple color variants
 * - Optional glow effect
 */
export function SystemBadge({
    children,
    variant = 'cyan',
    size = 'md',
    glow = false,
    className,
}: SystemBadgeProps) {
    const variantClasses = {
        success: 'bg-success/20 text-success border-success',
        warning: 'bg-warning/20 text-warning border-warning',
        error: 'bg-error/20 text-error border-error',
        info: 'bg-info/20 text-info border-info',
        cyan: 'bg-cyan-500/20 text-cyan-400 border-cyan-500',
        teal: 'bg-teal-500/20 text-teal-400 border-teal-500',
        blue: 'bg-blue-500/20 text-blue-400 border-blue-500',
    };

    const sizeClasses = {
        sm: 'text-xs px-2 py-0.5',
        md: 'text-sm px-3 py-1',
        lg: 'text-base px-4 py-1.5',
    };

    const glowClasses = {
        success: 'shadow-[0_0_10px_rgba(16,185,129,0.5)]',
        warning: 'shadow-[0_0_10px_rgba(245,158,11,0.5)]',
        error: 'shadow-[0_0_10px_rgba(239,68,68,0.5)]',
        info: 'shadow-[0_0_10px_rgba(59,130,246,0.5)]',
        cyan: 'shadow-glow-cyan',
        teal: 'shadow-glow-teal',
        blue: 'shadow-glow-blue',
    };

    return (
        <span
            className={clsx(
                'inline-flex items-center justify-center',
                'rounded-md border font-semibold uppercase tracking-wide',
                'transition-smooth',
                variantClasses[variant],
                sizeClasses[size],
                glow && glowClasses[variant],
                className
            )}
        >
            {children}
        </span>
    );
}
