Reward Engine v1 – Implementation Specification
Goal Description

Implement a state-aware, elastic Reward Engine responsible for applying positive outcomes when a quest is completed.
The Reward Engine reinforces desired behavior while preventing reward farming by dynamically adjusting rewards based on PlayerState.

This engine works in coordination with:

Quest Lifecycle Engine

Player State Service

Core Design Principles

Elastic Rewards: Rewards adapt based on player condition (momentum, complacency, streaks).

Non-Exploitative: High performers are challenged, not overfed.

Composable: XP, attributes, and psychological effects are modular reward components.

Forward-Compatible: Supports staged reward application (v2-ready).

Architecture Decision (v1)

Reward Engine lives as a logical module:
com.lifeos.reward
inside player-service to maintain ACID guarantees.

XP is applied immediately.

Secondary rewards are staged (computed now, applied via internal hooks).

Domain Layer

com.lifeos.reward.domain

[NEW] RewardRecord.java

Audit log of applied rewards.

id (UUID)

playerId (UUID)

questId (UUID, unique)

rewardPayload (JSONB)

appliedAt (LocalDateTime)

Acts as idempotency + observability layer.

[NEW] RewardComponentType.java
XP_GAIN
ATTRIBUTE_GROWTH
MOMENTUM_BOOST
STREAK_EXTENSION
CONFIDENCE_CORRECTION

Service Layer

com.lifeos.reward.service

[NEW] RewardCalculationService.java
calculateReward(
  Quest quest,
  PlayerState playerState
): RewardDefinition

Base Logic

Base XP from Quest Difficulty & Priority

Base Attribute Growth from QuestType

Elastic Modifiers

Low Momentum → +XP / +Attribute Growth (comeback assist)

High Complacency → XP dampening

Long Active Streak → diminishing XP returns

RED / Ego-Breaker Quest → Attribute-heavy, low XP

Hard Constraints

No negative rewards

XP gain capped per quest

Attribute growth clamped to system-defined max delta

[NEW] RewardService.java
applyReward(UUID questId, UUID playerId)

Flow

Idempotency Check

If RewardRecord exists for quest → exit

Fetch PlayerState

Fetch Quest

Call RewardCalculationService

Apply Immediate Rewards

XP Gain

Persist RewardRecord

Dispatch Staged Rewards

Attribute growth

Momentum updates

Streak changes

Integration Changes
[MODIFY] PlayerStateService.java

New methods:

applyXpGain(UUID playerId, long amount)
queueAttributeGrowth(UUID playerId, AttributeType type, double delta)
adjustMomentum(UUID playerId, double delta)
extendStreak(UUID playerId)


Attribute growth is applied after XP to support staged execution.

[MODIFY] QuestLifecycleServiceImpl.java

In completeQuest():

rewardService.applyReward(questId, playerId)


Must execute after quest state is set to COMPLETED.

Reward Elasticity Matrix (v1)
Player Condition	Effect
Low Momentum	XP + Attribute Boost
High Complacency	XP Reduction
Long Streak	Diminishing XP
Ego-Breaker Quest	Minimal XP, Heavy Attribute Gain
Verification Plan
Automated Tests

Elasticity Test

Same quest, different PlayerStates → different rewards

Idempotency Test

applyReward called twice → only one RewardRecord

XP Cap Test

Ensure XP never exceeds per-quest max

Staged Apply Test

XP applied immediately, attributes queued

Explicit Non-Goals (v1)

No economy balancing UI

No reward prediction/exposure to player

No async queues or event buses