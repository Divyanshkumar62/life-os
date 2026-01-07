Quest Model v1 — System Design Spec (Backend)
Scope

Defines the data structure for system-generated quests.
No generation logic, no UI, no messaging.

Core Principles (Locked)

Atomic quests (1 quest = 1 task)

All quests are time-bound

System may modify quests mid-flight

Backend-first, system-authoritative

Domain Package

com.lifeos.quest.domain

Entities
1. Quest

Represents a single, atomic system-assigned task.

Fields

questId (UUID, PK)

playerId (UUID, FK → PlayerIdentity)

title (String)

description (String)

questType (Enum)

difficultyTier (Enum: E, D, C, B, A, S, RED)

priority (Enum: LOW, NORMAL, HIGH, CRITICAL)

state (Enum: ASSIGNED, ACTIVE, COMPLETED, FAILED, EXPIRED)

assignedAt (LocalDateTime)

startsAt (LocalDateTime)

deadlineAt (LocalDateTime)

lastModifiedAt (LocalDateTime)

systemMutable (boolean = true)

egoBreakerFlag (boolean)

expectedFailureProbability (double 0–1)

2. QuestConstraint

System-imposed conditions attached to a quest.

Fields

id (Long, PK)

quest (ManyToOne)

constraintType (Enum)

value (String)

enforced (boolean)

Examples

Time-of-day only

Minimum energy state

No stacking with other quests

3. QuestOutcomeProfile

Defines rewards and penalties (data only).

Fields

id (Long, PK)

quest (OneToOne)

successXP (long)

failureXP (long)

attributeDeltaJson (JSONB)

statusFlagsOnSuccess (JSONB)

statusFlagsOnFailure (JSONB)

penaltyTier (Enum: NONE, LIGHT, MEDIUM, SEVERE)

4. QuestMutationLog

Tracks system mid-flight changes.

Fields

id (Long, PK)

quest (ManyToOne)

mutationType (Enum)

reason (String)

oldValueJson (JSONB)

newValueJson (JSONB)

mutatedAt (LocalDateTime)

5. PlayerQuestLink

Explicit join for player–quest lifecycle.

Fields

id (Long, PK)

playerId (UUID)

questId (UUID)

state (Enum)

activatedAt (LocalDateTime)

completedAt (LocalDateTime)

failedAt (LocalDateTime)

Enums
QuestType

DISCIPLINE

PHYSICAL

COGNITIVE

CAREER

REFLECTION

RECOVERY

EGO_BREAKER

QuestState

ASSIGNED

ACTIVE

COMPLETED

FAILED

EXPIRED

DifficultyTier

E, D, C, B, A, S, RED

ConstraintType

TIME_WINDOW

ENERGY_REQUIRED

NO_STACKING

LOCATION_LOCK

SYSTEM_ONLY

MutationType

DEADLINE_SHIFT

DIFFICULTY_ESCALATION

CONSTRAINT_ADDED

CONSTRAINT_REMOVED

Hard Invariants (Must Be Enforced)

Quest cannot be COMPLETED after deadline

FAILED or EXPIRED is terminal

RED difficulty ⇒ egoBreakerFlag = true

System mutations must be logged

Player cannot self-modify quests

Out of Scope (Explicit)

Quest Generation Intelligence

Difficulty calculation

Scheduling logic

Messaging text

UI mapping