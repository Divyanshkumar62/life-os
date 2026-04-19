import { clsx } from 'clsx';

export interface SystemProgressBarProps {
    current: number;
    max: number;
    label?: string;
    color?: 'cyan' | 'teal' | 'success' | 'warning' | 'error';
    showPercentage?: boolean;
    showValues?: boolean;
    height?: 'sm' | 'md' | 'lg';
    className?: string;
}

/**
 * SystemProgressBar - Animated progress bar with gradient
 * 
 * Responsibilities:
 * - Display progress visually
 * - Show optional labels and values
 * - Support multiple color schemes
 */
export function SystemProgressBar({
    current,
    max,
    label,
    color = 'cyan',
    showPercentage = false,
    showValues = false,
    height = 'md',
    className,
}: SystemProgressBarProps) {
    const percentage = Math.min(100, Math.max(0, (current / max) * 100));

    const colorClasses = {
        cyan: 'progress-gradient',
        teal: 'bg-gradient-to-r from-teal-600 to-teal-400',
        success: 'bg-success',
        warning: 'bg-warning',
        error: 'bg-error',
    };

    const heightClasses = {
        sm: 'h-1',
        md: 'h-2',
        lg: 'h-3',
    };

    return (
        <div className={clsx('w-full', className)}>
            {/* Label Row */}
            {(label || showPercentage || showValues) && (
                <div className="flex justify-between items-center mb-1">
                    {label && (
                        <span className="text-xs text-gray-400 uppercase tracking-wide">
                            {label}
                        </span>
                    )}
                    <div className="flex items-center gap-2 text-xs">
                        {showValues && (
                            <span className="text-gray-400 font-mono">
                                {current.toLocaleString()} / {max.toLocaleString()}
                            </span>
                        )}
                        {showPercentage && (
                            <span className="text-cyan-400 font-semibold">
                                {percentage.toFixed(1)}%
                            </span>
                        )}
                    </div>
                </div>
            )}

            {/* Progress Bar */}
            <div className={clsx('w-full bg-gray-700 rounded-full overflow-hidden', heightClasses[height])}>
                <div
                    className={clsx(
                        'h-full transition-all duration-500 ease-out',
                        colorClasses[color]
                    )}
                    style={{ width: `${percentage}%` }}
                />
            </div>
        </div>
    );
}
