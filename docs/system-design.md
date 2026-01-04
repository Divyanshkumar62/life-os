# Player State Model v1 – System Design Specification

## Role & Context
You are designing the **Player State Model v1** for a gamified, AI-driven Life OS inspired by *Solo Leveling*.  
This model represents the **single source of truth** for a player’s current condition, progress, and readiness for quests.

This is **backend-first**, logic-focused, and UI-agnostic.

The system behaves like a cold mentor:
- Growth-oriented
- Allows failure
- Adapts difficulty
- Uses uncertainty
- Does NOT permanently fail the player

---

## Core Design Principles

1. **State > Scores**
   - The player is modeled as a dynamic state machine, not just numbers.

2. **Separation of Concerns**
   - Levels, Ranks, Attributes, and Status Flags are independent but interacting systems.

3. **Growth Through Stress**
   - Failure, difficulty spikes, and ego-breaker quests are intentional design elements.

4. **Non-Determinism**
   - The system may expect failure but is never 100% certain of outcomes.

---

## Player State Model – High-Level Structure

The Player State is composed of the following domains:

- Identity
- Progression (Level & Rank)
- Attributes
- Skills
- Psychological State
- Performance Metrics
- Status Flags
- Temporal Data
- History & Memory

---

## 1. Player Identity

Immutable or rarely changing data.

Fields:
- playerId (UUID)
- username
- createdAt
- systemVersion (for migrations)

---

## 2. Progression System

### 2.1 Level
Represents **accumulated growth over time**.

- level (integer)
- currentXP
- xpToNextLevel
- levelGrowthRate (dynamic modifier)

Rules:
- Level increases via consistent effort, not single achievements.
- Failure does NOT reduce level.
- Level affects baseline quest difficulty.

---

### 2.2 Rank
Represents **recognized breakthroughs or major milestones**.

- rank (ENUM: F, E, D, C, B, A, S, SS)
- rankProgressScore
- lastRankUpAt

Rules:
- Rank is NOT tied directly to level.
- Rank increases through:
  - Major quest completion
  - Long-term consistency
  - Overcoming high-difficulty challenges
- Rank never decreases, but can stagnate.

---

## 3. Attribute System

Attributes represent **capabilities**, not traits.

Each attribute has:
- baseValue
- currentValue
- growthVelocity
- decayRate (if neglected)

Initial core attributes:
- Discipline
- Focus
- PhysicalEnergy
- MentalResilience
- LearningSpeed
- EmotionalControl

Rules:
- Attributes grow unevenly.
- Over-optimization in one attribute may trigger ego-breaker logic.
- Attributes can temporarily drop due to fatigue or failure states.

---

## 4. Skill Axes (Optional v1-lite)

Skills are **applied expressions** of attributes.

Example:
- Skill: Deep Work
  - Depends on: Focus + Discipline

Fields:
- skillId
- proficiencyLevel
- usageFrequency

Note:
- Skills evolve slower than attributes.
- Skills are NOT required for MVP calculations but must be compatible.

---

## 5. Psychological State Model

Represents **how the player is currently behaving**, not how they feel.

Fields (normalized 0–100):
- momentum (consistency & streak-based)
- complacency (ease without effort)
- stressLoad (pressure accumulation)
- confidenceBias (overconfidence indicator)

Rules:
- High momentum + high complacency = ego-breaker trigger candidate
- High stressLoad suppresses quest difficulty
- confidenceBias affects system messaging tone

---

## 6. Performance Metrics

Short- and mid-term evaluation inputs.

Fields:
- questSuccessRate (rolling window)
- averageQuestDifficulty
- failureStreak
- recoveryRate (how fast player rebounds after failure)

Used by:
- Quest Generator
- Ego-Breaker Trigger Logic
- Penalty Engine

---

## 7. Status Flags (Boolean / Enum)

Transient but critical system signals.

Examples:
- FATIGUED
- OVERCONFIDENT
- STALLED
- RECOVERING
- HIGH_RISK
- EGO_EXPOSED

Rules:
- Flags influence quest selection and messaging.
- Flags auto-expire unless reinforced.
- Multiple flags can coexist.

---

## 8. Temporal State

Time-aware data.

Fields:
- lastQuestCompletedAt
- activeStreakDays
- restDebt
- burnoutRiskScore

Rules:
- Rest is allowed but not guaranteed.
- Excessive rest without effort increases complacency.
- Burnout risk suppresses ego-breaker probability.

---

## 9. History & Memory

Used for learning and personalization.

Fields:
- completedQuests (IDs + outcomes)
- failedQuests (IDs + reasons)
- lastEgoBreakerAt
- notableEvents (rank-ups, major failures)

Rules:
- History informs future difficulty.
- Ego-breakers cannot be triggered too frequently.
- The system “remembers” behavioral patterns.

---

## 10. System Guarantees

- The player can **always recover**
- Failure is framed as data, not punishment
- No permanent lockout states
- Difficulty adapts, not softens blindly

---

## Output Expectations for Implementation

From this design, generate:
1. Backend-ready data models (Java / Spring Boot compatible)
2. Clear separation between mutable and immutable fields
3. Extensible enums for Rank, Flags, Attributes
4. No UI assumptions
5. No hard-coded difficulty values (use modifiers)

---

## End of Specification
