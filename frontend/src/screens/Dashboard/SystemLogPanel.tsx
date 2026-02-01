import { SystemPanel, SystemBadge } from '../../components/system';

export interface LogEntry {
    id: string;
    timestamp: string;
    type: 'info' | 'success' | 'warning' | 'error';
    message: string;
}

export interface SystemLogPanelProps {
    entries: LogEntry[];
}

/**
 * SystemLogPanel - Event log display
 * 
 * Responsibilities:
 * - Show system event history
 * - Color-code by severity
 * - Auto-scroll to latest
 */
export function SystemLogPanel({
    entries,
}: SystemLogPanelProps) {
    const typeVariants = {
        info: 'info' as const,
        success: 'success' as const,
        warning: 'warning' as const,
        error: 'error' as const,
    };

    return (
        <SystemPanel title="📜 SYSTEM LOG" glowColor="cyan">
            <div className="space-y-2 max-h-64 overflow-y-auto scrollbar-hide">
                {entries.map((entry) => (
                    <div
                        key={entry.id}
                        className="bg-gray-900 border-l-2 border-gray-700 p-2 text-xs animate-slide-in"
                    >
                        <div className="flex items-start gap-2 mb-1">
                            <span className="text-gray-500 font-mono">{entry.timestamp}</span>
                            <SystemBadge variant={typeVariants[entry.type]} size="sm">
                                {entry.type}
                            </SystemBadge>
                        </div>
                        <p className="text-gray-300">{entry.message}</p>
                    </div>
                ))}
            </div>
        </SystemPanel>
    );
}
