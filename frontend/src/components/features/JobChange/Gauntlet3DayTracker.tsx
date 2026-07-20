import React from "react";
import { CheckCircle, XCircle, Lock, Circle } from "lucide-react";

interface Quest {
  questId: string;
  day: number;
  title: string;
  questType: string;
  difficulty: string;
  state: string;
}

interface Gauntlet3DayTrackerProps {
  quests: Quest[];
  loading: boolean;
  onCompleteQuest: (questId: string, day: number) => void;
}

const DAY_LABELS: Record<number, { title: string; subtitle: string }> = {
  1: { title: "The Endless Swarm", subtitle: "Volume: 6-8 micro-tasks" },
  2: { title: "The Royal Guards", subtitle: "Intensity: 3 Deep Work tasks" },
  3: { title: "The Blood-Red Commander", subtitle: "Boss: Final Narrative Quest" },
};

export const Gauntlet3DayTracker: React.FC<Gauntlet3DayTrackerProps> = ({ quests, loading, onCompleteQuest }) => {
  const days = [1, 2, 3];

  const getDayStatus = (day: number): "locked" | "active" | "completed" => {
    const dayQuests = quests.filter((q) => q.day === day);
    if (dayQuests.length === 0) return day === 1 ? "active" : "locked";
    const allCompleted = dayQuests.every((q) => q.state === "COMPLETED");
    if (allCompleted) return "completed";
    const prevDayQuests = quests.filter((q) => q.day === day - 1);
    const prevAllCompleted = day === 1 || prevDayQuests.every((q) => q.state === "COMPLETED");
    if (!prevAllCompleted) return "locked";
    return "active";
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between px-2">
        {days.map((day, i) => {
          const status = getDayStatus(day);
          return (
            <React.Fragment key={day}>
              <div className="flex flex-col items-center gap-1.5">
                <div className={`w-10 h-10 rounded-full flex items-center justify-center border-2 transition-all ${
                  status === "completed" ? "border-green-500 bg-green-950/40 text-green-400" :
                  status === "active" ? "border-solo-cyan bg-solo-cyan/10 text-solo-cyan animate-pulse" :
                  "border-gray-700 bg-gray-900/50 text-gray-600"
                }`}>
                  {status === "completed" ? <CheckCircle size={16} /> :
                   status === "locked" ? <Lock size={14} /> :
                   <Circle size={14} />}
                </div>
                <span className={`text-[9px] uppercase font-bold tracking-widest ${
                  status === "completed" ? "text-green-400" :
                  status === "active" ? "text-solo-cyan" :
                  "text-gray-600"
                }`}>
                  Day {day}
                </span>
              </div>
              {i < days.length - 1 && (
                <div className={`flex-1 h-px mx-2 ${
                  getDayStatus(day) === "completed" ? "bg-green-500/50" : "bg-gray-800"
                }`} />
              )}
            </React.Fragment>
          );
        })}
      </div>

      {days.map((day) => {
        const status = getDayStatus(day);
        const dayQuests = quests.filter((q) => q.day === day);
        const isLocked = status === "locked";

        return (
          <div
            key={day}
            className={`relative border p-4 transition-all ${
              isLocked ? "border-gray-800 bg-gray-950/30 opacity-50" :
              status === "active" ? "border-solo-cyan/40 bg-solo-cyan/[0.02]" :
              "border-green-900/40 bg-green-950/10"
            }`}
          >
            {isLocked && (
              <div className="absolute inset-0 backdrop-blur-[1px] flex items-center justify-center z-10">
                <div className="flex items-center gap-2 text-gray-600">
                  <Lock size={14} />
                  <span className="text-[10px] uppercase tracking-widest font-bold">Locked</span>
                </div>
              </div>
            )}

            <div className="mb-3">
              <h4 className={`text-xs font-bold uppercase tracking-widest ${
                isLocked ? "text-gray-600" :
                status === "active" ? "text-solo-cyan" :
                "text-green-400"
              }`}>
                Day {day}: {DAY_LABELS[day].title}
              </h4>
              <p className="text-[9px] text-gray-500 uppercase tracking-wider mt-0.5">
                {DAY_LABELS[day].subtitle}
              </p>
            </div>

            <div className="space-y-2">
              {dayQuests.length === 0 && (
                <p className="text-[10px] text-gray-600 italic font-mono">No quests assigned for this day.</p>
              )}
              {dayQuests.map((quest) => {
                const isComplete = quest.state === "COMPLETED";
                const isFailed = quest.state === "FAILED";
                const isPending = quest.state === "PENDING" || !quest.state;

                return (
                  <div key={quest.questId} className={`flex items-center justify-between py-1.5 px-2 border-l-2 ${
                    isComplete ? "border-green-500/50 bg-green-950/5" :
                    isFailed ? "border-red-500/50 bg-red-950/5" :
                    "border-gray-700"
                  }`}>
                    <div className="flex flex-col gap-0.5 max-w-[65%]">
                      <span className={`text-[11px] font-mono uppercase tracking-wide ${
                        isComplete ? "text-green-400 line-through" :
                        isFailed ? "text-red-400" :
                        "text-gray-300"
                      }`}>
                        {quest.title}
                      </span>
                      <span className="text-[8px] text-gray-600 font-mono uppercase tracking-wider">
                        {quest.questType} ({quest.difficulty})
                      </span>
                    </div>
                    <div className="flex items-center gap-2">
                      {isPending && !isLocked && (
                        <button
                          onClick={() => onCompleteQuest(quest.questId, quest.day)}
                          disabled={loading}
                          className="px-2 py-1 bg-green-900/30 hover:bg-green-700 border border-green-500/60 text-green-200 text-[9px] rounded-none transition-all font-bold tracking-widest uppercase disabled:opacity-50 font-mono"
                        >
                          Complete
                        </button>
                      )}
                      <span className={`flex items-center gap-1 px-1.5 py-0.5 text-[9px] font-bold font-mono tracking-widest ${
                        isComplete ? "text-green-400" :
                        isFailed ? "text-red-400" :
                        isLocked ? "text-gray-600" :
                        "text-yellow-400"
                      }`}>
                        {isComplete && <CheckCircle size={10} />}
                        {isFailed && <XCircle size={10} />}
                        {isPending && !isLocked && <Circle size={10} />}
                        {isComplete ? "Done" : isFailed ? "Failed" : isLocked ? "—" : "Pending"}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        );
      })}
    </div>
  );
};
