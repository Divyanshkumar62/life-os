import { SystemPanel, SystemMetric, SystemButton } from '../../components/system';

export interface AssetsPanelProps {
    totalGold: number;
    weeklyTrend: number;
    dailyIncome: number;
    shopStats: number;
}

/**
 * AssetsPanel - Economy/assets display
 * 
 * Responsibilities:
 * - Show total gold
 * - Display weekly trend
 * - Show income metrics
 * - Provide shop access
 */
export function AssetsPanel({
    totalGold,
    weeklyTrend,
    dailyIncome,
    shopStats,
}: AssetsPanelProps) {
    const trendDirection = weeklyTrend >= 0 ? 'up' : 'down';

    return (
        <SystemPanel title="💰 ASSETS" glowColor="cyan">
            {/* Total Gold */}
            <div className="mb-4">
                <SystemMetric
                    label="Total Gold"
                    value={`${totalGold.toLocaleString()} G`}
                    trend={trendDirection}
                    trendValue={`${weeklyTrend > 0 ? '+' : ''}${weeklyTrend}% this week`}
                    size="lg"
                />
            </div>

            {/* Chart Placeholder */}
            <div className="mb-4 h-24 bg-gray-900 rounded border border-gray-700 flex items-end justify-around p-2">
                {[30, 45, 60, 50, 70, 65, 80].map((height, i) => (
                    <div
                        key={i}
                        className="w-8 bg-gradient-to-t from-cyan-600 to-cyan-400 rounded-t"
                        style={{ height: `${height}%` }}
                    />
                ))}
            </div>

            {/* Income Stats */}
            <div className="grid grid-cols-2 gap-3 mb-4">
                <div className="bg-gray-900 p-2 rounded border border-gray-700">
                    <p className="text-xs text-gray-400 uppercase mb-1">Daily Income</p>
                    <p className="text-lg font-bold text-white font-mono">+{dailyIncome}G</p>
                </div>
                <div className="bg-gray-900 p-2 rounded border border-gray-700">
                    <p className="text-xs text-gray-400 uppercase mb-1">Shop Stats</p>
                    <p className="text-lg font-bold text-white font-mono">{shopStats}</p>
                </div>
            </div>

            {/* Shop Button */}
            <SystemButton variant="primary" fullWidth icon={<span>🏪</span>}>
                Access System Shop
            </SystemButton>
        </SystemPanel>
    );
}
