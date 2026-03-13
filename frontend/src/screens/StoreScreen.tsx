import { useState, useEffect } from 'react';
import { ShopItemCard } from '../components/economy/ShopItemCard';
import { ShoppingBag, Loader } from 'lucide-react';
import { EconomyAPI } from '../api/api';
import { useSystemContext } from '../context/SystemContext';

interface ShopItem {
    itemId: string;
    code: string;
    name: string;
    description: string;
    cost: number;
    stockLimit?: number;
    rankRequirement?: string;
}

interface StoreScreenProps {
    playerId: string | null;
}

export const StoreScreen = ({ playerId }: StoreScreenProps) => {
    const [items, setItems] = useState<ShopItem[]>([]);
    const [loading, setLoading] = useState(true);
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [_purchasing, setPurchasing] = useState<string | null>(null);

    const { statusWindow, refreshSystem } = useSystemContext();
    const playerGold = statusWindow?.economy?.gold || 0;
    const playerRank = statusWindow?.identity?.rank || 'E-RANK';

    const fetchData = async () => {
        if (!playerId) return;
        try {
            const shopItems = await EconomyAPI.fetchShopItems(playerId);
            setItems(shopItems);
        } catch (error) {
            console.error("Failed to load store:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, [playerId]);

    const handlePurchase = async (item: ShopItem) => {
        if (!playerId) return;
        if (playerGold < item.cost) {
            alert("Insufficient Funds!");
            return;
        }

        setPurchasing(item.code);
        try {
            await EconomyAPI.purchaseItem(playerId, item.code);
            alert(`Purchased: ${item.name}`);
            await fetchData(); // Refresh to update stock
            await refreshSystem(); // Hard refresh global state to deduct gold natively
        } catch (error) {
            console.error("Purchase failed:", error);
            alert("Purchase failed. Check rank or stock.");
        } finally {
            setPurchasing(null);
        }
    };

    // Helper to check rank
    const isRankLocked = (req?: string) => {
        if (!req) return false;
        const ranks = ['E-RANK', 'D-RANK', 'C-RANK', 'B-RANK', 'A-RANK', 'S-RANK', 'SS-RANK'];
        const reqIndex = ranks.indexOf(req);
        const playerIndex = ranks.indexOf(playerRank);
        return playerIndex < reqIndex;
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-black flex items-center justify-center text-solo-blue-500">
                <Loader className="animate-spin mr-2" /> Loading System Store...
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-black text-white p-6 relative overflow-hidden">
            {/* Background Ambience */}
            <div className="absolute top-0 left-0 w-full h-full bg-grid-pattern opacity-10 pointer-events-none" />
            <div className="absolute top-[-10%] right-[-10%] w-[500px] h-[500px] bg-solo-blue-900/20 rounded-full blur-3xl pointer-events-none" />

            {/* Header */}
            <header className="relative z-10 flex items-center justify-between mb-8 border-b border-solo-blue-900/50 pb-4">
                <div className="flex items-center">
                    <div className="ml-12"> {/* Spacing for Back button */}
                        <h1 className="text-3xl font-black italic tracking-wider text-transparent bg-clip-text bg-gradient-to-r from-white to-solo-blue-300">
                            SYSTEM STORE
                        </h1>
                        <p className="text-sm text-solo-blue-500 font-mono tracking-widest">Buy equipment and consumables.</p>
                    </div>
                </div>

                {/* Currency Display */}
                <div className="flex items-center bg-gray-900/80 border border-solo-blue-700/50 px-4 py-2 rounded-lg shadow-glow-cyan">
                    <ShoppingBag size={20} className="text-yellow-400 mr-2" />
                    <div className="text-right">
                        <p className="text-[10px] text-gray-400 uppercase">Current Funds</p>
                        <p className="text-xl font-bold text-yellow-400 font-mono">{playerGold.toLocaleString()} G</p>
                    </div>
                </div>
            </header>

            {/* Content */}
            <div className="relative z-10 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {items.map((item) => {
                    const locked = isRankLocked(item.rankRequirement);
                    return (
                        <ShopItemCard
                            key={item.itemId}
                            name={item.name}
                            description={item.description}
                            cost={item.cost}
                            rankRequirement={item.rankRequirement}
                            stockLimit={item.stockLimit}
                            isLocked={locked}
                            onPurchase={() => handlePurchase(item)}
                        />
                    );
                })}
            </div>
        </div>
    );
};
