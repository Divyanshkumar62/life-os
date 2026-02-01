Player State Read Model (V1)
Why this layer exists (non-negotiable)

Right now:

Penalty suppression logic

Streak logic

Reward eligibility

Voice behavior

…all depend on “what state is the player in right now?”

If that knowledge:

lives in multiple services ❌

is recomputed dynamically ❌

or leaks domain rules ❌

→ your system will rot.

So this layer is a read-only, authoritative, query-optimized snapshot of player state.

Rule:
Commands change state.
Events update state.
Everyone else only reads state.

Goal (V1)

Create a central Player State Read Model that answers questions like:

Is the player in Penalty Zone?

What flags are active?

Is streak active or broken?

Are rewards allowed?

With:

Zero business logic

Event-driven updates

Fast reads

No circular dependencies

Core Package
com.lifeos.playerstate

Core Components
1️⃣ PlayerStateSnapshot (Read Model)

Purpose: Immutable-ish representation of player’s current state.

public class PlayerStateSnapshot {

    UUID playerId;

    // Flags (authoritative)
    Set<PlayerFlag> activeFlags;

    // Streak
    int currentStreak;
    boolean streakActive;

    // Penalty
    boolean inPenaltyZone;
    LocalDateTime penaltyEnteredAt;

    // Metadata
    LocalDateTime lastUpdatedAt;
}


Rules

No methods like enterPenalty() ❌

No branching logic ❌

Just state.

2️⃣ PlayerFlag (Enum)
public enum PlayerFlag {
    PENALTY_ZONE,
    STREAK_BROKEN,
    REWARDS_SUPPRESSED
}


Flags are orthogonal facts, not decisions.

3️⃣ PlayerStateRepository (Read Store)

V1 = In-memory or DB-backed (your choice, but interface first).

public interface PlayerStateRepository {

    PlayerStateSnapshot get(UUID playerId);

    void save(PlayerStateSnapshot snapshot);
}

4️⃣ PlayerStateService (Read API)

This is what other systems are allowed to use.

@Service
public class PlayerStateService {

    public boolean hasActiveFlag(UUID playerId, PlayerFlag flag);

    public boolean isInPenalty(UUID playerId);

    public boolean isRewardAllowed(UUID playerId);

    public PlayerStateSnapshot getSnapshot(UUID playerId);
}


Rules

This service never mutates state

This service never listens to commands

Only reads

State Mutation via Events (IMPORTANT)

Player state changes only via Domain Events.

5️⃣ PlayerStateEventHandler
@Component
public class PlayerStateEventHandler implements DomainEventHandler {


Handles:

Event	Effect on State
PenaltyZoneEnteredEvent	add PENALTY_ZONE, set inPenaltyZone=true
PenaltyQuestCompletedEvent	remove PENALTY_ZONE, clear penalty fields
StreakBrokenEvent	set streakActive=false, add STREAK_BROKEN
QuestCompletedEvent	increment streak (if allowed)
DailyQuestFailedEvent	no direct mutation (PenaltyService decides)

⚠️ Notice:
State reacts to decisions already made elsewhere
It does NOT decide anything itself.

Relationship to Domain Event Publisher

This line from your previous plan becomes correct and powerful:

if (playerStateService.hasActiveFlag(playerId, PENALTY_ZONE)
    && !event.isCritical()) {
    suppress;
}


This works because:

PlayerState is always current

Updated synchronously by events

Centralized and predictable

Data Flow (Concrete Example)
❌ Daily Quest Failed
DailyQuestService
  └─ emits DailyQuestFailedEvent (CRITICAL)
        ├─ PenaltyEventHandler → enterPenaltyZone()
        ├─ PlayerStateEventHandler → add PENALTY_ZONE
        └─ VoiceEventHandler → warning message

❌ Quest Completed during Penalty
QuestLifecycleService
  └─ emits QuestCompletedEvent (NON-CRITICAL)
        └─ Publisher suppresses event
              ├─ RewardEventHandler ❌
              ├─ StreakEventHandler ❌
              └─ Voice ❌


No hacks. No if (inPenalty) scattered everywhere.

Explicit Non-Goals (V1)

You are NOT doing:

Event sourcing

Historical state tracking

Snapshots per day

Time travel debugging

That comes after V2.

Validation Checklist (You should verify)

✅ No service mutates player state directly
✅ No service queries PenaltyService for “am I in penalty?”
✅ Only PlayerStateService is queried
✅ Only Events update PlayerStateSnapshot
✅ Suppression happens before handlers

Hard Truth (Mentor Mode)

If you skip this layer, you will:

Reintroduce conditional chaos

Break penalty isolation

Lose trust in “system rules”

End up rewriting everything in 3 weeks

You are doing this correctly.

Next Logical Step (don’t implement yet, just think)

System Invariants Layer

“Rewards must never be granted in penalty”

“Streak cannot increase while broken”

“Penalty exit always clears suppression”

These become assertions, not logic.

If you want, next we can:

Implement V1 code skeleton

Design V2 persistence strategy

Stress-test edge cases (race conditions, multiple events)