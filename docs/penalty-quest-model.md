🔴 Penalty Quest Engine v1 — Backend Design
1️⃣ Purpose (Non-Negotiable)

The Penalty Quest Engine exists to:

Provide a single, mandatory exit path from Penalty Zone

Enforce effort-based redemption

Prevent bypass via gold, streaks, or shop

Restore system trust only after proof of compliance

Philosophy:
“You don’t apologize to the System. You work your way back in.”

2️⃣ Hard Constraints (LOCK THESE)

These rules prevent future dilution.

Only ONE active penalty quest per player

Penalty quest is system-generated

Cannot be skipped, rerolled, or bought out

No rewards except Penalty Exit

Must be completed in full

Progress persists across days

Failure to work on it does NOT reset it (no infinite punishment loops)

3️⃣ Domain Layer
🔴 Entity: PenaltyQuest
PenaltyQuest
-------------
id
playerId
type              // SURVIVAL
requiredCount     // e.g. 10, 20
completedCount
status            // ACTIVE, COMPLETED
createdAt
completedAt


Notes

type allows V2 expansion (discipline, reflection, endurance)

requiredCount is fixed at creation

No dueDate — pressure comes from system locks, not timers

🔴 Repository
PenaltyQuestRepository extends JpaRepository<PenaltyQuest, UUID>


Key queries:

findActiveByPlayerId

existsActiveByPlayerId

4️⃣ Service Layer
🔴 PenaltyQuestService

This is the authority for redemption.

Core Methods
1️⃣ generatePenaltyQuest(UUID playerId)

Called ONLY from:

PenaltyService.enterPenaltyZone(...)

Logic:

If active penalty quest exists → do nothing

Else:

Create SURVIVAL quest

requiredCount based on severity rules (v1 static)

Example v1 rule:

requiredCount = 10

2️⃣ recordWork(UUID playerId, int workUnits)

Called when:

Low-tier tasks completed

Backlog cleared

System-defined “grind actions”

Logic:

Fetch active penalty quest

Increment completedCount

If completedCount >= requiredCount:

Mark quest COMPLETED

Call PenaltyService.exitPenaltyZone(playerId)

⚠️ This method is not public-facing.
Only system actions can call it.

3️⃣ getPenaltyQuestStatus(UUID playerId)

Returns:

{
  "active": true,
  "type": "SURVIVAL",
  "completed": 6,
  "required": 10
}


Used by UI + System Voice.

5️⃣ Integration Points (CRITICAL)
🔗 PenaltyService
On Enter
enterPenaltyZone(playerId):
  isPenaltyActive = true
  streakService.resetStreak(playerId)
  penaltyQuestService.generatePenaltyQuest(playerId)

On Exit
exitPenaltyZone(playerId):
  isPenaltyActive = false
  unlockSystems()


Exit is ONLY callable by PenaltyQuestService.

🔗 DailyQuestService

While penalty active:

Normal dailies still exist

BUT:

Rewards suppressed

Streak blocked

Project creation locked

Optional V1 rule:

Certain daily completions count as workUnits

🔗 RewardService

Penalty quests give NO GOLD

Exit itself is the reward

6️⃣ Lifecycle Flow (End-to-End)
DAY 1: Missed
→ Warning

DAY 2: Missed again
→ enterPenaltyZone()
→ generatePenaltyQuest(required = 10)

Penalty State:
- Shop ❌
- Projects ❌
- Promotions ❌
- UI: SYSTEM RED

User completes grind tasks
→ recordWork(+1)
→ progress persists

completedCount == 10
→ quest COMPLETED
→ exitPenaltyZone()
→ system restored

7️⃣ Failure & Exploit Protection
Exploit Attempt	Result
Ignore penalty quest	System stays locked
Try to buy exit	Not allowed
Reset app	Quest persists
Finish 90% then fail day	No reset
Enter penalty twice	Same quest continues
8️⃣ V1 Acceptance Criteria

Penalty Quest Engine is DONE when:

 Penalty always creates a quest

 Exit is impossible without completion

 Quest progress persists

 Exit unlocks system cleanly

 No rewards besides exit

 No alternate bypass paths exist