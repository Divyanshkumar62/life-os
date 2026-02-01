import { SystemPanel, SystemBadge } from '../../components/system';

export interface PlayerProfileCardProps {
    avatarUrl: string;
    rank: string;
    title: string;
    fatigue: number;
}

/**
 * PlayerProfileCard - Player identity display
 * 
 * Responsibilities:
 * - Show player avatar
 * - Display rank and title
 * - Show fatigue meter
 */
export function PlayerProfileCard({
    avatarUrl,
    rank,
    title,
    fatigue,
}: PlayerProfileCardProps) {
    return (
        <SystemPanel glowColor="cyan" className="flex flex-col items-center">
            {/* Avatar */}
            <div className="relative mb-4">
                <div className="w-24 h-24 rounded-full border-4 border-cyan-500 overflow-hidden shadow-glow-cyan">
                    <img
                        src={avatarUrl}
                        alt="Player Avatar"
                        className="w-full h-full object-cover"
                    />
                </div>

                {/* Rank Badge Overlay */}
                <div className="absolute -bottom-2 left-1/2 transform -translate-x-1/2">
                    <div className="w-12 h-12 bg-gray-800 border-2 border-cyan-500 rounded-full flex items-center justify-center shadow-glow-cyan">
                        <span className="text-2xl">🛡️</span>
                    </div>
                </div>
            </div>

            {/* Rank */}
            <div className="text-center mb-2">
                <h2 className="text-2xl font-bold text-white font-mono">{rank}-RANK</h2>
            </div>

            {/* Title */}
            <div className="mb-4">
                <SystemBadge variant="cyan" glow>
                    {title}
                </SystemBadge>
            </div>

            {/* Fatigue */}
            <div className="w-full">
                <div className="flex justify-between items-center mb-1">
                    <span className="text-xs text-gray-400 uppercase">Fatigue</span>
                    <span className="text-xs text-gray-400 font-mono">{fatigue}%</span>
                </div>
                <div className="w-full h-2 bg-gray-700 rounded-full overflow-hidden">
                    <div
                        className="h-full bg-gradient-to-r from-success to-warning transition-all duration-500"
                        style={{ width: `${fatigue}%` }}
                    />
                </div>
            </div>
        </SystemPanel>
    );
}
