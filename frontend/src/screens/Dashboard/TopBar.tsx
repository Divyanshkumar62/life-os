import { SystemBadge } from '../../components/system';

export interface TopBarProps {
    userName: string;
    systemStatus: 'online' | 'offline' | 'maintenance';
    diagnosticMode?: boolean;
    onSettingsClick?: () => void;
    onNotificationsClick?: () => void;
}

/**
 * TopBar - Header with system status and user info
 * 
 * Responsibilities:
 * - Display system console title
 * - Show user name and status
 * - Provide access to settings and notifications
 */
export function TopBar({
    userName,
    systemStatus,
    diagnosticMode = false,
    onSettingsClick,
    onNotificationsClick,
}: TopBarProps) {
    const statusColors = {
        online: 'success' as const,
        offline: 'error' as const,
        maintenance: 'warning' as const,
    };

    return (
        <div className="flex items-center justify-between p-4 border-b border-gray-700 bg-gray-900">
            {/* Left: System Console */}
            <div className="flex items-center gap-4">
                <div className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-cyan-600 rounded flex items-center justify-center">
                        <svg className="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
                        </svg>
                    </div>
                    <div>
                        <h1 className="text-lg font-bold font-mono text-white">SYSTEM CONSOLE</h1>
                        {diagnosticMode && (
                            <p className="text-xs text-purple-400 uppercase tracking-wide">Diagnostic Mode: Active</p>
                        )}
                    </div>
                </div>

                <div className="h-6 w-px bg-gray-700" />

                {/* User Info */}
                <div className="flex items-center gap-2">
                    <span className="text-sm text-gray-400">USER:</span>
                    <span className="text-sm font-semibold text-white uppercase tracking-wide">{userName}</span>
                </div>
            </div>

            {/* Right: Status & Actions */}
            <div className="flex items-center gap-3">
                <SystemBadge variant={statusColors[systemStatus]} glow>
                    SYSTEM {systemStatus.toUpperCase()}
                </SystemBadge>

                {/* Settings Button */}
                <button
                    onClick={onSettingsClick}
                    className="p-2 hover:bg-gray-800 rounded transition-smooth"
                    aria-label="Settings"
                >
                    <svg className="w-5 h-5 text-gray-400 hover:text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                </button>

                {/* Notifications Button */}
                <button
                    onClick={onNotificationsClick}
                    className="p-2 hover:bg-gray-800 rounded transition-smooth relative"
                    aria-label="Notifications"
                >
                    <svg className="w-5 h-5 text-gray-400 hover:text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                    </svg>
                    <span className="absolute top-1 right-1 w-2 h-2 bg-error rounded-full animate-pulse-glow" />
                </button>
            </div>
        </div>
    );
}
