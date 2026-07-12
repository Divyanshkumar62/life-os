import { useState, useEffect } from 'react';
import { Eye, RefreshCw } from 'lucide-react';
import { ScreenFrame } from '../components/layout/ScreenFrame';
import { TelemetryHeatmap } from '../features/analytics/components/TelemetryHeatmap';
import { DungeonGraveyard } from '../features/analytics/components/DungeonGraveyard';
import { StatGrowthChart } from '../features/analytics/components/StatGrowthChart';
import { ConfessionGraveyard } from '../features/analytics/components/ConfessionGraveyard';

interface ObserverScreenProps {
  playerId: string | null;
  onBack: () => void;
}

export function ObserverScreen({ playerId, onBack }: ObserverScreenProps) {
    const [loading, setLoading] = useState<boolean>(true);
    const [refreshing, setRefreshing] = useState<boolean>(false);

    // Simulate initial load delay for the loading state
    useEffect(() => {
        const timer = setTimeout(() => setLoading(false), 600);
        return () => clearTimeout(timer);
    }, []);

    const handleRefresh = () => {
        setRefreshing(true);
        // Force re-mount of child components by briefly toggling a key
        setTimeout(() => setRefreshing(false), 500);
    };

  return (
    <ScreenFrame onBack={onBack} className="bg-[#020617] min-h-screen select-none font-mono">
      {/* Top Header */}
      <div className="flex justify-between items-center mb-6 pl-12">
        <div>
          <h1 className="text-2xl font-bold tracking-[0.2em] text-white uppercase flex items-center gap-2">
            <Eye className="text-solo-cyan animate-pulse" size={24} /> THE SYSTEM OBSERVER
          </h1>
          <p className="text-[10px] text-gray-500 uppercase tracking-widest">
            Authoritative Player Logs & Growth Analytics
          </p>
        </div>
        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="p-2 border border-solo-cyan/30 text-solo-cyan hover:border-solo-cyan hover:bg-solo-cyan/10 transition-colors uppercase text-xs flex items-center gap-1.5"
        >
          <RefreshCw size={14} className={refreshing ? 'animate-spin' : ''} />
          {refreshing ? 'Syncing...' : 'Sync logs'}
        </button>
      </div>

      {loading ? (
        <div className="flex flex-col items-center justify-center h-96 text-solo-cyan animate-pulse">
          <Eye size={48} className="animate-spin mb-4" />
          <span className="text-sm tracking-widest">INITIATING LINK WITH DATABASE...</span>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Main Visualizations (Left and Middle columns) */}
          <div className="lg:col-span-2 space-y-6">
            
            {/* Daily Heatmap Grid (Modularized) */}
            <TelemetryHeatmap playerId={playerId || ""} key={`heatmap-${refreshing}`} />

            {/* Stat Growth Trajectory (Extracted Component) */}
            <StatGrowthChart playerId={playerId || ""} key={`stats-${refreshing}`} />

            {/* Dungeon Graveyard Section (Modularized) */}
            <DungeonGraveyard playerId={playerId || ""} key={`dungeons-${refreshing}`} />
          </div>

          {/* Confession Graveyard (Extracted Component) */}
          <div className="lg:col-span-1">
            <ConfessionGraveyard playerId={playerId || ""} key={`confessions-${refreshing}`} />
          </div>

        </div>
      )}
    </ScreenFrame>
  );
}
export default ObserverScreen;