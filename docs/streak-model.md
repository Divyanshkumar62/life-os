🔥 STREAK ENGINE v1 (FINALIZED)
1️⃣ What a Streak IS (Concept Lock)

A Streak represents unbroken daily obedience to the System.

Rule (Non-Negotiable):
A streak is tied to SYSTEM DAILIES only, not optional quests or projects.

If the user completes ALL System Dailies for a day, the streak continues.
If they miss even one, the streak breaks.

This keeps streaks:

Objective

Binary

Emotionally heavy

2️⃣ Streak Types (v1 Scope)
✅ Core System Streak (ONLY ONE)

Name: SYSTEM_DISCIPLINE_STREAK

Tracks:

Consecutive days of perfect compliance

No multiple streaks in v1 (avoid cognitive overload).

3️⃣ Streak Increment Rules
Condition	Result
All System Dailies completed	Streak +1
Any System Daily missed	Streak = 0
Penalty Zone entered	Streak = 0
Promotion Exam failed	Streak = 0

No soft failure. No forgiveness.

4️⃣ Streak Rewards (Dopamine, Not Power)

Streaks DO NOT:

Grant XP directly

Grant Stats

Grant Boss Keys

They ONLY influence:

🎯 Gold Multiplier (Economy Tie-in)
Streak Length	Gold Bonus
3 days	+5%
7 days	+10%
14 days	+20%
30 days	+35%
60 days	+50% (Hard Cap)

This:

Feels rewarding

Never bypasses effort gates

Never breaks rank integrity

5️⃣ Streak Repair (VERY LIMITED)
🔹 Streak Repair Potion (Shop Item)

Rules:

Can only repair 1 broken day

Must be used within 24 hours of break

Cannot be used:

During Penalty Zone

During Promotion Exam

Cost: High (Gold sink)

This maintains:

Mercy without softness

Fear without hopelessness

6️⃣ GA Implementation Plan
Domain Layer

com.lifeos.streak.domain

🆕 PlayerStreak
id (UUID)
playerId (UUID)
currentStreak (int)
longestStreak (int)
lastSuccessfulDate (LocalDate)

Service Layer

com.lifeos.streak.service

StreakService
processDailyCompletion(playerId, date)

Called at Daily Reset

If ALL System Dailies complete:

Increment streak

Update lastSuccessfulDate

Else:

resetStreak()

resetStreak(playerId)

currentStreak = 0

applyStreakRepair(playerId)

Validate:

Break ≤ 24h ago

Not in Penalty Zone

Not in Promotion Exam

Restore streak to previous value

Integration Points
Daily Quest Engine

At midnight reset:

if all system dailies complete:
    streakService.processDailyCompletion(...)
else:
    streakService.resetStreak(...)

Reward Engine

When calculating Gold:

gold = baseGold * (1 + streakBonusPercent)

Penalty Engine

On penalty entry:

streakService.resetStreak(...)

7️⃣ Failure Matrix (Streak Safety)
Scenario	Outcome
App not opened	No effect (only quest state matters)
Partial completion	Reset
Manual abandon	Reset
Timezone edge	Use server-local midnight
Duplicate reset call	Idempotent
8️⃣ Why This Streak Engine Works

✔ Brutal clarity
✔ High emotional weight
✔ Strong gold economy loop
✔ No power creep
✔ Fully Solo-Leveling aligned

This turns days into chains.
Breaking them hurts — and that’s the point.