Stat System v1 
Goal Description

Implement a permanent, action-linked Attribute system inspired by Solo Leveling, where stats represent proven effort, not abstract XP. Stats act as hard gates for Rank Promotions and shape long-term progression.

Stats are:

Earned only via completion of tagged actions

Permanent (no loss, no respec in v1)

Linear growth

Rank-critical, not cosmetic

Core Attributes (v1 – Locked)
Attribute	Code	Meaning (System Interpretation)
Strength	STR	Physical activity, effort output, health discipline
Intelligence	INT	Deep work, study, problem-solving
Vitality	VIT	Recovery, sleep, energy management
Sense	SEN	Planning, awareness, reflection, foresight

⚠️ No secondary stats, no derived stats in v1.

Stat Growth Rules (Hard Rules)

Action-Linked Only

Stats do NOT grow from XP

Stats do NOT grow from level-ups

Stats grow ONLY when completing a Quest/Project tagged with that Attribute

Linear Growth

Each qualifying completion = +1 stat point

No multipliers, no decay, no diminishing returns (v1)

Primary Attribute Only

Each Quest/Project has exactly ONE primaryAttribute

Completion grants +1 ONLY to that attribute

No split rewards in v1

Permanent

Stats never decrease

Penalty Zone does NOT reduce stats

Stats represent historical effort, not current state

Domain Changes
Domain Layer

com.lifeos.player.domain

[MODIFY] PlayerStats.java (or embedded in PlayerState)
STR (int)
INT (int)
VIT (int)
SEN (int)


Initialization:

All stats start at 0

Domain Layer

com.lifeos.quest.domain

[MODIFY] Quest.java

Add:

primaryAttribute (Enum: STR, INT, VIT, SEN)


Rules:

Mandatory for:

SYSTEM_DAILY

PROJECT_SUBTASK

PROMOTION (for hidden objectives only, not stat gain)

Service Layer Changes
[MODIFY] PlayerStateService.java

Add methods:

incrementStat(UUID playerId, AttributeType type, int amount)
getStats(UUID playerId)


Rules:

amount is always +1 in v1

No validation against rank or level

No negative paths

[MODIFY] RewardService.java (Integration)

When applying rewards:

If rewardPayload contains ATTRIBUTE_GROWTH:

Call incrementStat(playerId, attribute, +1)

XP and Stats remain separate pipelines

[MODIFY] QuestLifecycleServiceImpl.java

On completeQuest():

Apply Reward Engine

If quest has primaryAttribute:

Increment corresponding stat by +1

Promotion Quests:

DO NOT grant stats (exam ≠ training)

Rank Promotion Integration (CRITICAL)

Stats are used ONLY in:

ProgressionService.canRequestPromotion()


Validation:

stats[STR] >= required
stats[INT] >= required
stats[VIT] >= required
stats[SEN] >= required


❌ Stats are NOT checked:

During daily quests

During projects

During penalties

Example Rank Stat Gates (Illustrative – Finalized Later)
Rank	Stat Expectations (Example)
E → D	STR 5, INT 5
D → C	STR 10, INT 10, VIT 5
C → B	STR 20, INT 20, SEN 10
B → A	Balanced ≥ 30
A → S	≥ 50 all stats

(Values adjustable without refactor.)

Explicit Non-Goals (v1)

🚫 No stat decay
🚫 No stat respec
🚫 No derived stats (e.g., Power Level)
🚫 No scaling rewards
🚫 No stat penalties

This keeps v1 deterministic, debuggable, and fair.

Verification Plan
Automated Tests

StatIncrementTest

Complete STR-tagged quest → STR +1

Complete INT-tagged quest → INT +1

Isolation Test

Gain XP → Stats unchanged

Level up → Stats unchanged

Promotion Gate Test

Insufficient stats → Promotion blocked

Sufficient stats → Promotion allowed