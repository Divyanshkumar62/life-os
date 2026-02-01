import type { ReactNode } from 'react';
import { clsx } from 'clsx';

export interface SystemMetricProps {
    label: string;
    value: string | number;
    icon?: ReactNode;
    trend?: 'up' | 'down' | 'neutral';
    trendValue?: string;
    size?: 'sm' | 'md' | 'lg';
    className?: string;
}

/**
 * SystemMetric - Display a single stat/metric
 * 
 * Responsibilities:
 * - Show labeled numeric value
 * - Display optional trend indicator
 * - Support multiple sizes
 */
export function SystemMetric({
    label,
    value,
    icon,
    trend,
    trendValue,
    size = 'md',
    className,
}: SystemMetricProps) {
    const sizeClasses = {
        sm: {
            value: 'text-xl',
            label: 'text-xs',
        },
        md: {
            value: 'text-2xl',
            label: 'text-sm',
        },
        lg: {
            value: 'text-4xl',
            label: 'text-base',
        },
    };

    const trendColors = {
        up: 'text-success',
        down: 'text-error',
        neutral: 'text-gray-400',
    };

    return (
        <div className={clsx('flex flex-col', className)}>
            {/* Label */}
            <div className="flex items-center gap-1 mb-1">
                {icon && <div className="text-purple-400 text-sm">{icon}</div>}
                <span className={clsx(sizeClasses[size].label, 'text-gray-400 uppercase tracking-wide')}>
                    {label}
                </span>
            </div>

            {/* Value */}
            <div className="flex items-baseline gap-2">
                <span className={clsx(sizeClasses[size].value, 'font-bold text-white font-mono')}>
                    {value}
                </span>

                {/* Trend Indicator */}
                {trend && trendValue && (
                    <span className={clsx('text-xs font-semibold', trendColors[trend])}>
                        {trend === 'up' && '↑'}
                        {trend === 'down' && '↓'}
                        {trendValue}
                    </span>
                )}
            </div>
        </div>
    );
}
