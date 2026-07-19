import { clsx } from 'clsx';

export interface SystemProgressBarProps {
    current: number;
    max: number;
    label?: string;
    color?: 'cyan' | 'teal' | 'success' | 'warning' | 'error' | 'electric-cyan';
    showPercentage?: boolean;
    showValues?: boolean;
    height?: 'sm' | 'md' | 'lg';
    className?: string;
    frozen?: boolean;
}

/**
 * SystemProgressBar - Progress bar implementing the design specifications.
 */
export function SystemProgressBar({
    current,
    max,
    label,
    color = 'electric-cyan',
    showPercentage = false,
    showValues = false,
    height = 'md',
    className,
    frozen = false,
}: SystemProgressBarProps) {
    const percentage = Math.min(100, Math.max(0, (current / max) * 100));

    const colorClasses = {
        cyan: 'bg-cyan-500',
        teal: 'bg-teal-500',
        success: 'bg-tertiary',
        warning: 'bg-warning',
        error: 'bg-error',
        'electric-cyan': 'bg-[#22D3EE]',
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
                <div className="flex justify-between items-center mb-1 font-space">
                    {label && (
                        <span className="text-xs text-on-surface-variant uppercase tracking-wider">
                            {label}
                        </span>
                    )}
                    <div className="flex items-center gap-2 text-xs font-mono">
                        {showValues && (
                            <span className="text-on-surface-variant">
                                {current.toLocaleString()} / {max.toLocaleString()}
                            </span>
                        )}
                        {showPercentage && (
                            <span className="text-secondary font-semibold">
                                {percentage.toFixed(1)}%
                            </span>
                        )}
                    </div>
                </div>
            )}

            {/* Progress Bar Container */}
            <div className={clsx('w-full bg-gray-800 rounded-none overflow-hidden relative border border-gray-700', heightClasses[height])}>
                <div
                    className={clsx(
                        'h-full transition-all duration-500 ease-out relative',
                        colorClasses[color],
                        frozen && 'opacity-70 bg-striped animate-pulse'
                    )}
                    style={{ width: `${percentage}%` }}
                >
                    {/* Add stripe overlay element if frozen to overlay color */}
                    {frozen && (
                        <div className="absolute inset-0 bg-striped bg-repeat" />
                    )}
                </div>
            </div>
        </div>
    );
}
