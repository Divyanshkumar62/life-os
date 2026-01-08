# Penalty Engine v1 – System Design Prompt (for Google Antigravity)

## Context

You are working inside a **gamified Life OS** inspired by *Solo Leveling*. The system already has:

* Player Service (player state, XP, level, stats)
* Quest Service (daily / weekly quests, status, rewards)

The **Penalty Engine** is responsible for enforcing consequences when a player fails, abandons, or violates quest rules. This is critical to maintain discipline, fairness, and long-term progression balance.

This document defines **Penalty Engine v1** for implementation.

---

## 1. Purpose of Penalty Engine

The Penalty Engine should:

* Detect quest failures or violations
* Calculate penalties deterministically
* Apply penalties to Player State
* Record penalty history for analytics & transparency

The goal is **behavior correction**, not punishment overload.

---

## 2. High-Level Responsibilities

Penalty Engine v1 must:

1. Listen to quest outcome events
2. Decide if a penalty is applicable
3. Calculate penalty severity
4. Apply penalties atomically
5. Emit penalty-applied events

---

## 3. Trigger Conditions (Inputs)

Penalty Engine is **event-driven**.

### Primary Triggers

* `QUEST_FAILED`
* `QUEST_ABANDONED`
* `QUEST_EXPIRED`

### Optional (future-ready, not mandatory for v1)

* `STREAK_BROKEN`
* `SYSTEM_RULE_VIOLATION`

Each trigger must include:

```json
{
  "playerId": "uuid",
  "questId": "uuid",
  "questType": "DAILY | WEEKLY | STORY",
  "difficulty": "EASY | MEDIUM | HARD | BOSS",
  "failureReason": "TIMEOUT | MANUAL_ABORT | INACTIVITY"
}
```

---

## 4. Penalty Types (v1 Scope)

Penalty Engine v1 supports **non-destructive penalties only**.

### Allowed Penalties

* XP deduction
* Stat debuff (temporary)
* Streak reset

### Explicitly NOT allowed in v1

* Level downgrade
* Permanent stat loss
* Inventory wipe

---

## 5. Penalty Rules

### Rule 1: XP Penalty

XP loss is based on quest difficulty:

| Difficulty | XP Penalty |
| ---------- | ---------- |
| EASY       | 5–10 XP    |
| MEDIUM     | 10–20 XP   |
| HARD       | 20–40 XP   |
| BOSS       | 50+ XP     |

Constraints:

* XP should never go below 0
* Penalty XP ≤ 30% of XP reward

---

### Rule 2: Stat Debuff

Temporary stat reduction:

* Affects 1 random core stat (e.g. Discipline, Focus)
* Debuff range: 5%–15%
* Duration: 6–24 hours

Example:

```
Discipline -10% for 12 hours
```

---

### Rule 3: Streak Impact

If quest is part of a streak system:

* Daily quest failure → daily streak reset
* Weekly quest failure → weekly streak decrement by 1

---

## 6. Penalty Calculation Logic

Penalty calculation must be:

* Deterministic
* Idempotent
* Reproducible

Suggested flow:

1. Read quest metadata
2. Map difficulty → base penalty
3. Apply modifiers (player level, active buffs)
4. Cap penalties

---

## 7. Data Model (Penalty Record)

Define a `PenaltyRecord` entity:

```json
{
  "penaltyId": "uuid",
  "playerId": "uuid",
  "questId": "uuid",
  "penaltyType": "XP | STAT | STREAK",
  "value": "number | object",
  "appliedAt": "timestamp",
  "expiresAt": "timestamp | null",
  "reason": "string"
}
```

---

## 8. API / Interface Expectations

Penalty Engine should expose:

### Internal API

* `applyPenalty(eventPayload)`
* `getActivePenalties(playerId)`

### Events Emitted

* `PENALTY_APPLIED`
* `PENALTY_EXPIRED`

---

## 9. Consistency & Safety Rules

* Must use transactions when modifying Player State
* Must prevent duplicate penalties for same quest
* Must log every penalty application

---

## 10. Non-Goals (v1)

Penalty Engine v1 does NOT handle:

* Psychological profiling
* Adaptive punishment
* Social penalties

These are reserved for v2+.

---

## 11. Deliverables for GA

Google Antigravity should:

1. Generate Penalty Engine microservice
2. Define domain models
3. Create event listeners
4. Implement penalty calculation logic
5. Expose internal APIs
6. Include basic unit tests

---

## End of Spec

This spec is authoritative for Penalty Engine v1.
