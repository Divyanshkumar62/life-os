🧠 System Voice Engine v1

(The “System” Personality Layer)

Goal

Create a deterministic, authoritative, emotionally weighted System voice that:

Reinforces discipline

Creates fear of failure

Makes progression feel earned

Never sounds like a coach, friend, or therapist

The System is not supportive.
It is objective, cold, and fair.

1️⃣ Core Voice Philosophy (LOCKED)
❌ What the System is NOT

Not motivational (“You got this 💪”)

Not empathetic (“It’s okay to fail”)

Not casual or chatty

Not personalized in tone

✅ What the System IS

Formal

Minimal

Unemotional

Absolute

Lore-consistent

Think:

“A divine OS issuing judgments.”

2️⃣ Voice Modes (Finite State Model)

The System does not improvise tone freely.
It operates in explicit modes, derived from player state.

SystemVoiceMode (Enum)
NEUTRAL
REWARD
WARNING
PENALTY
PROMOTION
FAILURE

Mode Resolution Priority (IMPORTANT)

Highest wins:

PENALTY

FAILURE

PROMOTION

WARNING

REWARD

NEUTRAL

Example:

Player completes a quest but is in Penalty Zone → PENALTY voice overrides REWARD.

3️⃣ Trigger → Voice Mapping (Deterministic)
Event	Voice Mode
Quest completed	REWARD
Daily failed (1st day)	WARNING
Daily failed (2nd day)	PENALTY
Enter Penalty Zone	PENALTY
Promotion Quest unlocked	PROMOTION
Promotion passed	PROMOTION
Promotion failed	FAILURE
Streak broken	FAILURE
Rank gate reached	NEUTRAL (System Notification)

No randomness. No ML. No tone drift.

4️⃣ Message Structure (CRITICAL)

Every System message follows one of these fixed templates.

Base Template
[SYSTEM MESSAGE]
<Primary Statement>
<Optional Consequence or Status Line>


No emojis.
No exclamation spam.
No encouragement.

5️⃣ Canonical Message Templates (V1 LOCK)
🟢 Reward (REWARD)
[SYSTEM MESSAGE]
Quest completed.
Rewards applied.


Optional (only for major actions):

[SYSTEM MESSAGE]
Objective cleared.
Performance registered.

🟡 Warning (WARNING)
[SYSTEM WARNING]
Daily objectives incomplete.
Further failure will result in penalties.

🔴 Penalty Zone Entry (PENALTY)
[SYSTEM PENALTY]
Daily objectives failed.
Penalty Zone activated.

🔴 Penalty Ongoing (PENALTY)
[SYSTEM PENALTY]
Restricted mode active.
Complete the Penalty Quest to regain access.

🧱 Rank Gate Reached (NEUTRAL)
[SYSTEM]
Level limit reached.
Rank advancement required.

⚔ Promotion Quest Unlocked (PROMOTION)
[SYSTEM NOTICE]
Qualifications met.
Promotion Exam unlocked.

🟣 Promotion Passed (PROMOTION)
[SYSTEM SUCCESS]
Rank advancement approved.
Limits removed.

⚫ Promotion Failed (FAILURE)
[SYSTEM FAILURE]
Promotion attempt failed.
Penalty conditions applied.

💀 Streak Broken (FAILURE)
[SYSTEM FAILURE]
Streak terminated.
Consistency record reset.

6️⃣ Personalization Boundary (VERY IMPORTANT)
What CAN be personalized

Numbers (XP, Gold)

Rank names

Quest titles

Stat values

What CANNOT be personalized

Tone

Sentence structure

Emotional framing

Example (Allowed):

Rank advancement approved.
You are now C-Rank.


Example (NOT allowed):

Congrats! You’ve finally made it to C-Rank 🎉

7️⃣ Implementation Plan (GA-Ready)
Domain Layer
SystemVoiceMode.java
SystemMessageType.java

Service Layer
SystemVoiceService.java
SystemMessage generateMessage(
    UUID playerId,
    SystemEventType event,
    Map<String, Object> payload
);


Responsibilities:

Resolve Voice Mode

Select Template

Inject dynamic values

Emit event for UI

Integration Points
Service	Emits Event
DailyQuestService	DAILY_FAILED
PenaltyService	PENALTY_ENTERED
RewardService	QUEST_COMPLETED
ProgressionService	RANK_GATE_REACHED, PROMOTION_RESULT
StreakService	STREAK_BROKEN
8️⃣ UI Contract (NON-NEGOTIABLE)

System messages are modal or toast with high contrast

PENALTY mode forces red theme

No dismiss animation for Penalty entry

Promotion success gets slow, heavy animation

Failure gets instant hard cut

This is part of the psychology.

9️⃣ V1 Non-Goals (Explicit)

❌ AI-generated free text
❌ Emotional encouragement
❌ User-written System messages
❌ Humor
❌ Chatbot-style responses

Those destroy authority.

10️⃣ Why This Works

The System feels objective

Failure feels final

Success feels earned

Silence between events builds tension

Users fear red text

This is how Solo Leveling made progression addictive.