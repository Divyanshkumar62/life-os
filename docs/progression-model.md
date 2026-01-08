Progression Engine v1 Specification
Goal Description

Implement a Rank-Gated Progression Engine where player growth is constrained by Level Caps tied to Rank. Rank advancement is achieved exclusively via Promotion Quests. Levels represent continuous effort; Ranks represent qualification and system trust.

Core Concepts
Level

Continuous numeric progression

Increases via XP

Subject to Rank-based Level Cap

XP gain halts when cap is reached

Rank

Discrete status tier (E → D → C → B → A → S)

Unlocks:

System privileges

Quest categories

Higher Level Caps

Can never decrease

Rank Configuration (v1)
Rank	Level Cap
E	10
D	25
C	45
B	70
A	90
S	100

(Levels beyond 100 handled by Prestige system, v2)

Promotion Gate Mechanism

When:

player.level == rank.levelCap

Then:

XP gain is frozen

System emits:

“Qualifications for Rank Advancement met.”

Progression Engine requests a Promotion Quest from QGI:

Difficulty: HIGH or RED

Type: BOSS / EGO-TEST

Retryable

Cannot be skipped

Promotion Quest Outcomes
PASS

Rank increments by 1 tier

New Level Cap unlocked

XP unfreezes

System Message (long-form, cold, impactful)

FAIL

Rank unchanged

Level unchanged

Player enters Penalty Zone state

Retry allowed after cooldown (24h default)

Failure Handling (No Downgrades)

Rank is immutable

Failure consequences are immediate:

Penalty Quests

XP suppression

Correction Tasks

Player must exit Penalty Zone before normal progression resumes

Endgame Transition (Soft Cap)

When:

rank == S && level == 100

Then:

Player enters National Level

XP curve becomes exponential

Rewards shift from:

Attributes → Influence

Power → Control / Cosmetics / Meta privileges

(Level 101+ is Prestige Domain — tracked but not power-inflating)

Integration Points
Depends On

PlayerStateService

QuestLifecycleService

RewardEngine

PenaltyEngine

QGI (Promotion Quest generation)

Emits

RankGateReached Event

PromotionQuestRequested Event

RankAdvanced Event

Verification Scenarios

XP Freeze Test

Player at Level 10 (E-Rank)

Complete quest

XP does not increase

Promotion Pass

Complete Promotion Quest

Rank increases

XP resumes

Promotion Fail

Fail Promotion Quest

Rank unchanged

Penalty triggered

Rank Immutability

Apply multiple failures

Rank never decreases