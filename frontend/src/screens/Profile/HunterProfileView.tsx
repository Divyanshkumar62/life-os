import { clsx } from "clsx";
import { useSystemContext } from "../../context/SystemContext";

interface HunterProfileViewProps {
  onBack: () => void;
}

export function HunterProfileView({ onBack }: HunterProfileViewProps) {
  const { statusWindow, jobClass } = useSystemContext();
  
  const level = statusWindow?.identity?.level || 1;
  const rank = statusWindow?.identity?.rank || "F";
  const currentXp = statusWindow?.progression?.currentXp || 0;
  const maxXp = statusWindow?.progression?.maxXpForLevel || 100;
  const xpPercentage = maxXp > 0 ? (currentXp / maxXp) * 100 : 0;
  const isXpFrozen = statusWindow?.progression?.xpFrozen || false;

  const displayClass = jobClass || "Novice Hunter";
  const classStatus = jobClass ? "ACTIVE" : "PENDING";

  const rankCaps: Record<string, number> = {
    F: 5,
    E: 10,
    D: 25,
    C: 45,
    B: 70,
    A: 90,
    S: 100,
    SS: 999
  };
  const currentCap = rankCaps[rank] || 100;

  const nextRankMap: Record<string, string> = {
    F: "E",
    E: "D",
    D: "C",
    C: "B",
    B: "A",
    A: "S",
    S: "SS",
    SS: "SS"
  };
  const nextRank = nextRankMap[rank] || "SS";

  const getRequirements = () => {
    const requirements: Array<{ label: string; met: boolean }> = [];
    
    requirements.push({
      label: `Reach Level ${currentCap} (Current: ${level})`,
      met: level >= currentCap
    });
    
    const str = statusWindow?.attributes?.STR || 10;
    const intel = statusWindow?.attributes?.INT || 10;
    const vit = statusWindow?.attributes?.VIT || 10;
    const sen = statusWindow?.attributes?.SEN || 10;
    
    if (rank === "E") {
      requirements.push({ label: `Strength >= 5 (Current: ${str})`, met: str >= 5 });
      requirements.push({ label: `Intellect >= 5 (Current: ${intel})`, met: intel >= 5 });
    } else if (rank === "D") {
      requirements.push({ label: `Strength >= 10 (Current: ${str})`, met: str >= 10 });
      requirements.push({ label: `Intellect >= 10 (Current: ${intel})`, met: intel >= 10 });
      requirements.push({ label: `Vitality >= 5 (Current: ${vit})`, met: vit >= 5 });
    } else if (rank === "C") {
      requirements.push({ label: `Strength >= 20 (Current: ${str})`, met: str >= 20 });
      requirements.push({ label: `Intellect >= 20 (Current: ${intel})`, met: intel >= 20 });
      requirements.push({ label: `Vitality >= 10 (Current: ${vit})`, met: vit >= 10 });
      requirements.push({ label: `Sensibility >= 10 (Current: ${sen})`, met: sen >= 10 });
    } else if (rank === "B") {
      requirements.push({ label: `Strength >= 30 (Current: ${str})`, met: str >= 30 });
      requirements.push({ label: `Intellect >= 30 (Current: ${intel})`, met: intel >= 30 });
      requirements.push({ label: `Vitality >= 20 (Current: ${vit})`, met: vit >= 20 });
      requirements.push({ label: `Sensibility >= 20 (Current: ${sen})`, met: sen >= 20 });
    } else if (rank === "A") {
      requirements.push({ label: `Strength >= 50 (Current: ${str})`, met: str >= 50 });
      requirements.push({ label: `Intellect >= 50 (Current: ${intel})`, met: intel >= 50 });
      requirements.push({ label: `Vitality >= 40 (Current: ${vit})`, met: vit >= 40 });
      requirements.push({ label: `Sensibility >= 40 (Current: ${sen})`, met: sen >= 40 });
    }
    
    const keyCosts: Record<string, number> = { F: 1, E: 1, D: 2, C: 3, B: 5, A: 8 };
    const keyCost = keyCosts[rank] || 0;
    if (keyCost > 0) {
      requirements.push({
        label: `Obtain ${keyCost} Rank ${rank} Boss Key(s) for Exam Entry`,
        met: false // Goal checked dynamically on backend
      });
    }
    
    return requirements;
  };

  const reqs = getRequirements();
  const allMet = reqs.every(r => r.met || r.label.includes("Boss Key"));

  return (
    <div className="min-h-screen w-full bg-[#0a0f1a] text-white flex flex-col font-mono selection:bg-cyan-500/30">
      {/* Header - Sticky */}
      <header className="flex justify-between items-center px-8 py-4 border-b border-cyan-900/40 bg-[#0a0f1a]/95 backdrop-blur-sm sticky top-0 z-50 shadow-lg shadow-black/20">
        <div className="flex items-center gap-4">
          <div className="w-8 h-8 bg-cyan-950/40 flex items-center justify-center rounded border border-cyan-500/30">
            <span className="text-cyan-400 text-xs">⟡</span>
          </div>
          <div className="text-xs tracking-[0.2em] font-bold text-gray-400">
            SYSTEM_OS <span className="text-cyan-500">V.4.2.0</span>
          </div>
        </div>

        <nav className="flex gap-8 text-[10px] tracking-[0.15em] font-bold">
          {["DASHBOARD", "DIAGNOSTIC", "INVENTORY", "GUILD"].map((item) => (
            <button
              key={item}
              className={clsx(
                "hover:text-white transition-colors uppercase relative py-1",
                item === "DIAGNOSTIC"
                  ? "text-cyan-400 after:absolute after:bottom-0 after:left-0 after:w-full after:h-[2px] after:bg-cyan-500"
                  : "text-gray-500",
              )}
            >
              {item}
            </button>
          ))}
        </nav>

        <div className="flex items-center gap-6 text-[9px] tracking-[0.15em]">
          <div className="text-gray-500 text-right">
            <div>SERVER: ASIA_01</div>
            <div className="flex items-center justify-end gap-1.5 text-green-500 mt-0.5">
              <div className="w-1.5 h-1.5 rounded-full bg-green-500 shadow-[0_0_5px_rgba(34,197,94,0.5)]" />
              ONLINE
            </div>
          </div>
          <button
            onClick={onBack}
            className="px-5 py-2 border border-cyan-900/50 bg-cyan-950/20 hover:bg-cyan-900/40 text-cyan-300 text-[10px] tracking-widest transition-all"
          >
            LOGOUT
          </button>
        </div>
      </header>

      {/* Main Content Container - Allows growing */}
      <div className="flex-1 flex flex-col relative">
        {/* Fixed Background - spans full height */}
        <div
          className="absolute inset-0 opacity-[0.04] pointer-events-none fixed z-0"
          style={{
            backgroundImage:
              "linear-gradient(rgba(34, 211, 238, 0.2) 1px, transparent 1px), linear-gradient(90deg, rgba(34, 211, 238, 0.2) 1px, transparent 1px)",
            backgroundSize: "40px 40px",
          }}
        />

        <div className="p-8 pb-24 relative z-10 flex flex-col gap-8">
          {/* Top Section */}
          <div className="flex justify-between items-end">
            <div>
              <div className="flex items-center gap-2 text-[10px] tracking-[0.2em] text-cyan-500 mb-1 font-bold">
                <span className="text-xs">🛡</span> IDENTITY VERIFIED
              </div>
              <h1
                className="text-5xl font-black tracking-tighter mb-2"
                style={{ fontFamily: "Inter, sans-serif" }}
              >
                HUNTER PROFILE
              </h1>
              <div className="text-[11px] tracking-[0.1em] text-gray-500 font-mono">
                CLASS: {displayClass.toUpperCase()} ({classStatus})
              </div>
            </div>

            <div className="text-right">
              <div className="text-[10px] tracking-[0.2em] text-gray-500 mb-1">
                SYNC RATE
              </div>
              <div className="text-3xl font-bold text-green-400 tracking-wider">
                98.4%
              </div>
            </div>
          </div>

          {/* Grid Layout */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
            {/* Left Column (Avatar + Attributes) */}
            <div className="lg:col-span-4 flex flex-col gap-4 sticky top-24">
              {/* Avatar */}
              <div className="aspect-[3/4] rounded-sm border border-cyan-900/30 bg-[#0c1220] relative overflow-hidden group p-1 shrink-0">
                <div className="absolute inset-2 border border-cyan-500/10 z-20 pointer-events-none" />
                <div className="absolute top-4 right-4 w-4 h-[1px] bg-cyan-400/30 z-20" />
                <div className="absolute top-4 right-4 w-[1px] h-4 bg-cyan-400/30 z-20" />

                <div className="w-full h-full bg-gradient-to-b from-gray-800 to-black relative">
                  <div className="absolute inset-0 bg-[url('https://i.pinimg.com/736x/2e/da/c3/2edac3b333989045b854e7d483486c4f.jpg')] bg-cover bg-center grayscale opacity-80 mix-blend-luminosity group-hover:grayscale-0 transition-all duration-700" />
                  <div className="absolute inset-0 bg-gradient-to-t from-[#0a0f1a] via-transparent to-transparent opacity-90" />
                </div>

                <div className="absolute bottom-6 left-6 z-30">
                  <div className="text-[10px] tracking-[0.2em] text-cyan-500/70 mb-0">
                    CURRENT RANK
                  </div>
                  <div
                    className="text-8xl font-black text-white leading-[0.8] tracking-tighter drop-shadow-[0_0_15px_rgba(34,211,238,0.5)]"
                    style={{ fontFamily: "Inter, sans-serif" }}
                  >
                    {rank}
                  </div>
                </div>

                <div className="absolute bottom-6 right-6 z-30">
                  <div className={clsx(
                    "px-3 py-1 text-[10px] tracking-[0.15em] font-bold border",
                    isXpFrozen ? "bg-red-900/30 text-red-500 border-red-900/50" : "bg-green-900/30 text-green-500 border-green-900/50"
                  )}>
                    {isXpFrozen ? "STAGNANT" : "ACTIVE"}
                  </div>
                </div>
              </div>

              {/* Resource Stats */}
              <div className="bg-[#0c1220] border border-cyan-900/30 p-5 flex flex-col gap-5 shrink-0">
                <h3 className="text-[11px] font-bold tracking-[0.1em] text-white flex items-center gap-2">
                  <span className="animate-spin text-cyan-500">⚙</span> RESOURCE
                  ALLOCATION
                </h3>

                <div className="space-y-5">
                  <div className="space-y-1.5">
                    <div className="flex justify-between text-[10px] text-gray-400 tracking-wider">
                      <span>Active Quests</span>
                      <span className="font-mono">
                        2 <span className="text-gray-600">/ 3</span>
                      </span>
                    </div>
                    <div className="h-1.5 bg-gray-800 w-full rounded-sm overflow-hidden">
                      <div className="h-full bg-cyan-600 w-2/3 shadow-[0_0_10px_rgba(8,145,178,0.5)]" />
                    </div>
                  </div>

                  <div className="space-y-1.5">
                    <div className="flex justify-between text-[10px] text-gray-400 tracking-wider">
                      <span>Inventory Slots</span>
                      <span className="font-mono">
                        14 <span className="text-gray-600">/ 20</span>
                      </span>
                    </div>
                    <div className="h-1.5 bg-gray-800 w-full rounded-sm overflow-hidden">
                      <div className="h-full bg-blue-500 w-[70%]" />
                    </div>
                  </div>

                  <div className="flex justify-between items-center pt-1 border-t border-cyan-900/20">
                    <span className="text-[10px] text-gray-400 tracking-wider">
                      Mana Recovery
                    </span>
                    <span className="text-[10px] font-mono text-green-400">
                      1.2 / sec
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Right Column (Progression + Promotion) */}
            <div className="lg:col-span-8 flex flex-col gap-4">
              {/* Level Card */}
              <div className="h-40 bg-[#0c1220] border border-cyan-900/30 p-6 relative overflow-hidden shrink-0">
                <div className="relative z-10 flex flex-col justify-between h-full">
                  <div className="flex justify-between items-start">
                     <div>
                       <div className="text-[10px] tracking-[0.3em] text-cyan-500/80 mb-2 font-bold">
                         PLAYER LEVEL
                       </div>
                       <div className="flex items-baseline gap-3">
                         <span className="text-6xl font-black text-white leading-none tracking-tighter">
                           {level}
                         </span>
                         <span className="text-[10px] text-gray-600 tracking-widest">
                           / CAP {currentCap}
                         </span>
                       </div>
                     </div>
                     <div className="text-right">
                       <div className="text-[9px] text-gray-500 tracking-widest mb-1">
                         EXP ACCUMULATION
                       </div>
                       <div className="font-mono text-xl text-white tracking-widest">
                         {currentXp.toLocaleString()}{" "}
                         <span className="text-gray-600 text-sm">/ {maxXp.toLocaleString()}</span>
                       </div>
                     </div>
                  </div>

                  <div>
                    <div className="h-4 bg-gray-900/50 w-full rounded-sm overflow-hidden relative mb-2">
                       <div className="absolute inset-0 flex items-center justify-center text-[9px] font-bold z-20 text-white/90">
                         {xpPercentage.toFixed(0)}%
                       </div>
                       <div className="h-full bg-cyan-600 relative" style={{ width: `${Math.min(100, xpPercentage)}%` }}>
                         <div className="absolute right-0 top-0 bottom-0 w-[1px] bg-white/50" />
                       </div>
                    </div>
                    <div className="flex justify-between text-[9px]">
                       <span className={clsx("tracking-wider font-mono", isXpFrozen ? "text-red-500" : "text-green-500")}>
                         {isXpFrozen ? "⚠️ XP FROZEN (RANK EXAM REQUIRED)" : "⚡ SYSTEM STATUS: STABLE"}
                       </span>
                       <span className="text-gray-600 tracking-widest">
                         NEXT MILESTONE: LVL {currentCap}
                       </span>
                    </div>
                  </div>
                </div>
                <div className="absolute top-4 right-4 text-8xl text-cyan-500/5">
                  📊
                </div>
              </div>

              {/* Promotion Status - Allow vertical expansion */}
              <div className="bg-[#0c1220] border border-cyan-900/30 p-8 relative overflow-hidden min-h-[500px] flex flex-col">
                <div
                  className="absolute inset-0 opacity-[0.03]"
                  style={{
                    backgroundImage:
                      "linear-gradient(rgba(34, 211, 238, 0.1) 1px, transparent 1px), linear-gradient(90deg, rgba(34, 211, 238, 0.1) 1px, transparent 1px)",
                    backgroundSize: "20px 20px",
                  }}
                />

                <div className="relative z-10 flex-1 flex flex-col">
                  <div className="flex justify-between items-center mb-10">
                    <h3 className="text-lg font-bold tracking-widest text-white">
                      PROMOTION STATUS
                    </h3>
                    {rank !== "SS" && (
                      <button className={clsx(
                        "text-[10px] border px-3 py-1.5 transition-colors uppercase tracking-widest bg-black/50 font-bold",
                        allMet ? "border-solo-cyan text-solo-cyan hover:bg-solo-cyan/20 cursor-pointer" : "border-gray-800 text-gray-600 cursor-not-allowed"
                      )}>
                        Request Exam
                      </button>
                    )}
                  </div>

                  <div className="flex flex-col xl:flex-row gap-12 flex-1">
                    <div className="flex flex-col items-center justify-center py-8">
                      <div className="w-40 h-40 rounded-full border-4 border-dashed border-gray-800 flex items-center justify-center relative">
                        <div className={clsx("absolute inset-0 rounded-full border-t-4 transform -rotate-45", allMet ? "border-solo-cyan animate-pulse" : "border-gray-700")} />
                        <div className="text-5xl">{rank === "SS" ? "👑" : allMet ? "🔓" : "🔒"}</div>
                        <div className="absolute -bottom-3 bg-gray-800 text-[9px] px-2 py-0.5 rounded text-gray-400 tracking-wider font-bold">
                          RANK {nextRank}
                        </div>
                      </div>
                    </div>

                    <div className="flex-1 flex flex-col justify-center gap-6">
                      {rank === "SS" ? (
                        <div className="text-[11px] font-bold text-solo-gold flex items-center gap-2 tracking-wider bg-solo-gold/10 p-3 border border-solo-gold/20 rounded-sm">
                          <span>👑</span> ASCENDED: MAX RANK REACHED
                        </div>
                      ) : (
                        <>
                          <div className={clsx(
                            "text-[11px] font-bold flex items-center gap-2 tracking-wider p-3 border rounded-sm",
                            allMet ? "text-solo-cyan bg-solo-cyan/10 border-solo-cyan/20" : "text-red-500 bg-red-900/10 border-red-900/20"
                          )}>
                            <span>{allMet ? "✓" : "⚠️"}</span> {allMet ? "ELIGIBLE: READY FOR RANK EXAM" : "LOCKED: REQUIREMENTS NOT MET"}
                          </div>

                          <div className="space-y-4 font-mono text-[11px]">
                            {reqs.map((req, i) => (
                              <div key={i} className={clsx(
                                "flex items-center justify-between p-4 border-l-2",
                                req.met ? "bg-cyan-900/10 border-green-500" : "bg-cyan-900/5 border-gray-700"
                              )}>
                                <div className="flex items-center gap-3">
                                  <div className={clsx(
                                    "w-4 h-4 rounded-full flex items-center justify-center text-[8px] font-bold",
                                    req.met ? "bg-green-500 text-black" : "border border-gray-600 text-gray-400"
                                  )}>
                                    {req.met ? "✓" : "✗"}
                                  </div>
                                  <span className={req.met ? "text-gray-300" : "text-gray-400"}>
                                    {req.label}
                                  </span>
                                </div>
                                <span className={clsx("text-[10px] tracking-widest", req.met ? "text-green-500" : "text-gray-600")}>
                                  {req.met ? "COMPLETE" : "PENDING"}
                                </span>
                              </div>
                            ))}
                          </div>
                        </>
                      )}
                    </div>
                  </div>

                  <div className="mt-8 border-t border-cyan-900/20 pt-6 font-mono text-[10px] space-y-2">
                    <div className="text-gray-500 bg-black/40 p-2 font-bold">
                      <span className="text-cyan-500 mr-2">&gt;</span>
                      SYSTEM_LOG: {rank === "SS" ? "Rank Ascension complete." : allMet ? `Capabilities sufficient for Rank ${nextRank} promotion.` : `Capabilities insufficient for Rank ${nextRank} promotion.`}
                    </div>
                    {rank !== "SS" && !allMet && (
                      <div className="text-gray-500 bg-black/40 p-2 font-bold">
                        <span className="text-cyan-500 mr-2">&gt;</span>
                        SUGGESTION: Focus on leveling up and attribute allocation.
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Footer Status - Sticky Bottom */}
      <div className="px-6 py-2 border-t border-cyan-900/30 bg-[#05080f] flex justify-between items-center text-[9px] font-mono tracking-widest text-gray-500 uppercase fixed bottom-0 left-0 right-0 z-50">
        <div className="flex items-center gap-4">
          <span className="text-green-500 flex items-center gap-2">
            <span className="w-1.5 h-1.5 bg-green-500 rounded-full" />
            SYSTEM STABLE
          </span>
          <span>|</span>
          <span>LATENCY: 4MS</span>
          <span>|</span>
          <span>ENCRYPTION: AES-256</span>
        </div>
        <div className="animate-pulse text-cyan-600">
          WAITING FOR INPUT...
        </div>
      </div>
    </div>
  );
}
