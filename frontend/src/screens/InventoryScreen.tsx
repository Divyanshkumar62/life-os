import { useState, useEffect } from 'react';
import { InventoryGrid } from '../components/economy/InventoryGrid';
import { StatAllocationPanel } from '../components/progression/StatAllocationPanel';
import { User, Package, Loader } from 'lucide-react';
import { EconomyAPI, PlayerAPI } from '../api/api';
import { useSystemContext } from '../context/SystemContext';

interface InventoryItem {
    id: string;
    name: string;
    description: string;
    quantity: number;
    itemCode: string;
}

interface Stat {
    key: string;
    label: string;
    value: number;
}

interface InventoryScreenProps {
    playerId: string | null;
}

export const InventoryScreen = ({ playerId }: InventoryScreenProps) => {
    const [stats, setStats] = useState<Stat[]>([]);
    const [freePoints, setFreePoints] = useState(0);
    const [inventory, setInventory] = useState<InventoryItem[]>([]);
    const [loading, setLoading] = useState(true);

    const { statusWindow, refreshSystem } = useSystemContext();
    const playerName = statusWindow?.identity?.username || 'Hunter';

    const mapAttributesToStats = (attributes: any): Stat[] => [
        { key: 'STRENGTH', label: 'Strength', value: attributes?.STRENGTH || 0 },
        { key: 'AGILITY', label: 'Agility', value: attributes?.AGILITY || 0 },
        { key: 'INTELLECT', label: 'Intelligence', value: attributes?.INTELLECT || 0 },
        { key: 'VITALITY', label: 'Vitality', value: attributes?.VITALITY || 0 },
        { key: 'SENSE', label: 'Perception', value: attributes?.SENSE || 0 }
    ];

    const fetchData = async () => {
        if (!playerId) return;
        try {
            const invData = await EconomyAPI.fetchInventory(playerId);

            // Map Inventory
            const mappedInventory = invData.map((item: any) => ({
                id: item.id || Math.random().toString(), // Fallback if ID missing
                name: item.item.name,
                description: item.item.description,
                quantity: item.quantity,
                itemCode: item.item.code
            }));
            setInventory(mappedInventory);
            setInventory(mappedInventory);

        } catch (error) {
            console.error("Failed to load inventory:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, [playerId]);

    useEffect(() => {
        if (statusWindow) {
            setStats(mapAttributesToStats(statusWindow.attributes));
            setFreePoints(statusWindow.attributes?.freePoints || 0);
        }
    }, [statusWindow]);

    const handleUseItem = async (itemCode: string) => {
        if (!playerId) return;
        try {
            const updatedInv = await EconomyAPI.useConsumable(playerId, itemCode);
            // Re-map and update inventory
            const mappedInventory = updatedInv.map((item: any) => ({
                id: item.id,
                name: item.item.name,
                description: item.item.description,
                quantity: item.quantity,
                itemCode: item.item.code
            }));
            setInventory(mappedInventory);

            // Hard refresh Global Context to update HUD/Status
            await refreshSystem();

            alert(`Used item: ${itemCode}`);
        } catch (error) {
            console.error("Failed to use item:", error);
            alert("Cannot use item.");
        }
    };

    const handleAllocate = async (statKey: string, amount: number) => {
        if (!playerId || freePoints < amount) return;

        try {
            const updatedState = await PlayerAPI.allocateStat(playerId, statKey, amount);
            // Update local state from response
            setStats(mapAttributesToStats(updatedState.attributes));
            setFreePoints(updatedState.freeStatPoints);
            // Optimistically update or just trust use effect? Better to use response.
        } catch (error) {
            console.error("Allocation failed:", error);
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-black flex items-center justify-center text-solo-blue-500">
                <Loader className="animate-spin mr-2" /> Loading Details...
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-black text-white p-6 relative overflow-hidden flex flex-col md:flex-row gap-6">
            {/* Background FX */}
            <div className="absolute top-0 right-0 w-2/3 h-full bg-gradient-to-l from-solo-blue-900/10 to-transparent pointer-events-none" />

            {/* Left Column: Player Status */}
            <div className="w-full md:w-1/3 lg:w-1/4 flex flex-col gap-6 relative z-10">
                <div className="bg-gray-900/80 border border-solo-blue-900/50 rounded-lg p-6 shadow-glow-cyan">
                    <div className="flex items-center mb-4">
                        <User size={32} className="text-solo-blue-400 mr-3" />
                        <div>
                            <h2 className="text-xl font-bold text-white">{playerName.toUpperCase()}</h2>
                            <span className="text-sm text-solo-blue-500 font-mono">Shadow Monarch</span>
                        </div>
                    </div>
                    <div className="h-[1px] bg-solo-blue-900/50 mb-4" />
                    <StatAllocationPanel
                        level={statusWindow?.identity?.level || 1}
                        currentXp={statusWindow?.progression?.currentXp || 0}
                        maxXp={statusWindow?.progression?.maxXpForLevel || 100}
                        freePoints={freePoints}
                        stats={stats}
                        onAllocate={handleAllocate}
                    />
                </div>
            </div>

            {/* Right Column: Inventory */}
            <div className="flex-1 relative z-10">
                <div className="flex items-center justify-between mb-6 bg-gray-900/50 p-4 rounded-lg border-l-4 border-solo-blue-500">
                    <div className="flex items-center">
                        <Package size={24} className="text-solo-blue-300 mr-3" />
                        <h2 className="text-2xl font-black italic tracking-wider text-white">INVENTORY</h2>
                    </div>
                    <span className="text-gray-500 text-sm font-mono">{inventory.length} ITEMS</span>
                </div>

                <InventoryGrid
                    items={inventory}
                    onUseItem={handleUseItem}
                />
            </div>
        </div>
    );
};
