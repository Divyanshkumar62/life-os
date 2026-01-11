RANK EXAM ENGINE v1
1️⃣ Concept Lock — What a Rank Exam IS

A Rank Exam is:

A one-time, deterministic trial

Triggered ONLY by:

Correct Rank-Specific Boss Keys

Meeting Stat Gates

Being Penalty-Free

Implemented as a special Quest Instance

Binary outcome: PASS or FAIL
(No partial credit, no retries without cost)

Think: Solo Leveling Dungeon Gate opens → enter or stay weak.

2️⃣ Exam Structure (Locked Decisions)
🔹 Exam Trigger

Manual, Intentional Action

User presses “Request Promotion”

System validates:

Correct Boss Keys (rank-bound)

Stat thresholds

XP cap reached

penalty_active == false

If any fail → hard reject.

🔹 Exam Cost

Boss Keys are CONSUMED on entry

Even if the user fails

This prevents:

Retry spamming

“I’ll just try and see”

Fear is preserved.

🔹 Exam Format (v1)

Each exam spawns a PROMOTION_QUEST with:

Property	Value
Difficulty	FIXED_BY_RANK
Time Limit	Rank-specific (e.g., 7 days)
Scaling	❌ None
Retry	❌ No
Abandon	Counts as FAIL
🔹 Exam Themes (Narrative + Mechanical)
Transition	Exam Theme
E → D	“Proof of Consistency”
D → C	“Stress Tolerance Trial”
C → B	“Execution Under Load”
B → A	“Leadership & Autonomy Test”
A → S	“Absolute Self-Mastery”

These are not cosmetic — they determine quest composition.

3️⃣ GA Implementation Plan
Domain Layer

com.lifeos.rankexam.domain

✅ RankExamAttempt
id (UUID)
playerId (UUID)
fromRank (PlayerRank)
toRank (PlayerRank)
status (LOCKED, UNLOCKED, PASSED, FAILED)
requiredKeys (int)
consumedKeys (int)
startedAt (LocalDateTime)
completedAt (LocalDateTime)

Quest Layer
🔁 Reuse Quest Entity

Add:

QuestType = PROMOTION_EXAM


Rules:

Promotion quests:

Cannot be edited

Cannot be skipped

Cannot be re-rolled

Service Layer

com.lifeos.progression.service

requestPromotion(playerId)

Flow:

Validate stat gates

Validate rank-specific keys

Validate XP cap

Validate penalty_active == false

Consume keys

Create RankExamAttempt (UNLOCKED)

Spawn PROMOTION_EXAM quest

processPromotionOutcome(playerId, result)

If PASS

Promote Rank

Increase XP cap

Clear xpFrozen

Mark exam PASSED

If FAIL

Mark exam FAILED

Enter Penalty Zone

XP remains frozen

Rank unchanged

QuestLifecycle Integration

In completeQuest():

if quest.type == PROMOTION_EXAM:
    progressionService.processPromotionOutcome(playerId, PASS)


In failQuest():

if quest.type == PROMOTION_EXAM:
    progressionService.processPromotionOutcome(playerId, FAIL)

4️⃣ Failure & Edge-Case Matrix (Critical)
Scenario	Outcome
User abandons exam	FAIL + Penalty
User misses system daily during exam	FAIL + Penalty
User enters Penalty during exam	Auto FAIL
App crash mid-exam	Exam persists
User reaches deadline	Auto FAIL

No mercy. Predictability > forgiveness.

5️⃣ Exam Difficulty Matrix (v1 Defaults)
Rank	Duration	Required Actions
E → D	3–5 days	Perfect dailies
D → C	5–7 days	Dailies + 1 Project Subtask
C → B	7 days	High volume + zero misses
B → A	10 days	Self-directed execution
A → S	14 days	Near-flawless run

Exact numbers can be tuned later — structure is locked.

6️⃣ Why This Engine Works

✔ Exams are feared
✔ Keys give Projects real meaning
✔ Failure hurts but doesn’t erase progress
✔ Rank inflation is impossible
✔ System feels alive and watching

This is not gamification — this is authority.