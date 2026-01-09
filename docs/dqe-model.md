Daily Quest Engine v1

(Solo Leveling–Inspired Discipline System)

Core Philosophy

Daily Quests are:

Mandatory

Non-negotiable

Punitive when ignored

Predictable but unforgiving

They are not motivation tools.
They are the system’s way of enforcing baseline discipline.

1️⃣ Daily Quest Types (V1 – Locked)
A. SYSTEM DAILIES (Non-Optional)

These are auto-assigned every day.

Quest	Rule
Wake-up Window	Check-in within user-defined window
Movement	Steps / Workout / Mobility
Focus Block	30–90 min deep work
Reflection	Short journal / review

Failing any one triggers consequences.

B. EQUIPPED PROJECT SUBTASKS (Optional Boost)

Pulled from Active Projects.

Rules:

Max 1 equipped per active Project

Completing them gives:

Normal XP

Project progress

Failing them:

Only affects Project completion

Does NOT trigger Penalty Zone

This keeps Projects risky, not punitive.

2️⃣ Daily Quest Slot Model
Rank	System Dailies	Project Slots
E	2	0
D	3	1
C	4	2
B	4	3
A/S	5	3

System dailies scale with Rank → higher rank = higher baseline

3️⃣ Failure Logic (CRITICAL)
❌ Failure Condition

If any System Daily is not completed before daily reset:

➡️ Immediate Penalty Zone Trigger

No grace days.
No streak forgiveness.
No “tomorrow is fine”.

4️⃣ Penalty Zone (Daily Failure Variant)

This is NOT the same as Promotion failure.

Penalty Zone v1 Rules

Player is marked PENALTY_ZONE_ACTIVE

XP gain reduced to 0

Project progress blocked

Promotion requests locked

Exit Condition (Daily Failure)

User must complete Penalty Quest Set:

Penalty Quest	Rule
Physical	Mandatory movement
Mental	Reflection on failure
Order	Clean / organize / reset
Discipline	Re-do failed Daily

Once completed → Penalty Zone cleared.

5️⃣ XP & Reward Model
XP Rules

System Dailies → Small but consistent XP

Equipped Project Tasks → Normal XP

XP respects xpFrozen

No Streak Multipliers (V1)

Why?

Encourages fear of loss

Conflicts with penalty-based discipline

Introduces anxiety loops

Streaks exist only as a stat, not a multiplier.

6️⃣ Domain Model Additions
Domain Layer
DailyQuest {
  id
  playerId
  type (SYSTEM, PROJECT)
  difficulty (FIXED)
  status (PENDING, COMPLETED, FAILED)
  assignedAt
  expiresAt
}

Status Flags
PENALTY_ZONE_ACTIVE
DAILY_FAILED_TODAY

7️⃣ Service Layer Logic
DailyQuestService

generateDailyQuests(playerId)

completeDailyQuest(questId)

failExpiredQuests(playerId)

PenaltyService

enterPenaltyZone(playerId, reason)

clearPenaltyZone(playerId)

8️⃣ Daily Reset Flow (Midnight Job)

Evaluate incomplete System Dailies

If any failed → Penalty Zone

Expire all Daily Quests

Generate new ones

Reset DAILY_FAILED_TODAY

9️⃣ Abuse Prevention
Exploit	Protection
Skipping days	Immediate Penalty
Easy habits	System-defined quests
Overloading Projects	Rank-gated slots
XP grinding	XP Freeze
10️⃣ V1 Constraints (Important)

Explicitly NOT in v1:

Adaptive difficulty

Streak bonuses

“Rest days”

Emotional tuning

This is hard mode by design.