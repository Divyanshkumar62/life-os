# Rank Transition Templates v1 – System Specification

## Goal
Define deterministic, rank-gated promotion mechanics inspired by Solo Leveling.
Rank advancement is NOT time-based. Promotion is achieved exclusively via Promotion Exam Quests,
which are unlocked using Boss Keys earned from Project completion.

---

## Core Concepts (Authoritative)

### 1. Level vs Rank
- **Level**: Continuous XP-based progression from daily habits and quests.
- **Rank**: Discrete status tier that:
  - Imposes a Level Cap
  - Unlocks system privileges
  - Can ONLY be advanced via Promotion Exam Quests

XP gain is **frozen** when Level Cap is reached until promotion succeeds.

---

### 2. Boss Keys
- Boss Keys are NOT time-based.
- Boss Keys drop ONLY on successful completion of a **Project**.
- Projects are multi-step, multi-day goals (not habits).
- Boss Keys are consumed on Promotion Exam entry.

---

### 3. Promotion Exams
- Promotion Exams are deterministic by Rank.
- Criteria are fixed and identical for all users.
- Exam content (tasks) is contextualized to the user.
- Exam outcomes:
  - PASS → Rank advances
  - FAIL → Rank unchanged, Penalty Zone triggered
  - ACE (Hidden Objective) → Bonus rewards

---

## Rank Transition Templates

---

## E → D Rank

### Prerequisites
- Level ≥ 10
- Stats:
  - STR ≥ 10
  - DISCIPLINE ≥ 8

### Entry Cost
- Boss Keys Required: 1
- Source: Any completed Project (≥ 5 subtasks, ≥ 3-day span)

### Promotion Exam Quest
**Theme:** Proof of Awakening  
**Conditions:**
- Complete all daily habits in a single day
- Complete one system-tagged “Uncomfortable Task”

### Hidden Objective (Ace)
- Complete exam with ZERO reminders or nudges

### Rewards
- Rank → D
- Unlock Level Cap → 25
- Ace Bonus:
  - +1 Stat Point
  - Bonus Gold
  - Title: “Awakened”

---

## D → C Rank

### Prerequisites
- Level ≥ 25
- Stats:
  - DISCIPLINE ≥ 15
  - FOCUS ≥ 12

### Entry Cost
- Boss Keys Required: 2
- Constraint: Must come from 2 distinct Projects

### Promotion Exam Quest
**Theme:** The Week of Hell  
**Conditions:**
- Maintain zero failed habits for 5 consecutive days
- Complete one Deep Work task ≥ 90 minutes

### Hidden Objective (Ace)
- Complete at least one system-identified avoided/postponed task

### Rewards
- Rank → C
- Unlock Level Cap → 45
- Ace Bonus:
  - Momentum Boost
  - Rare Cosmetic Badge
  - Bonus XP (non-farmable)

---

## C → B Rank

### Prerequisites
- Level ≥ 45
- Stats:
  - DISCIPLINE ≥ 25
  - FOCUS ≥ 20
  - WILLPOWER ≥ 15

### Entry Cost
- Boss Keys Required: 3
- Constraint: ≥ 1 High-Friction Project required

### Promotion Exam Quest
**Theme:** The System’s Trial  
**Conditions:**
- 7-day exam window
- One daily quest per day is system-assigned
- Easy quests grant ZERO XP
- Mandatory mid-project review completion

### Hidden Objective (Ace)
- Complete exam without entering Penalty Zone

### Rewards
- Rank → B
- Unlock Level Cap → 70
- Ace Bonus:
  - Temporary Attribute Growth Multiplier
  - Unlock Advanced Quest Types

---

## B → A Rank

### Prerequisites
- Level ≥ 70
- Stats:
  - DISCIPLINE ≥ 40
  - FOCUS ≥ 35
  - EMOTIONAL_CONTROL ≥ 25

### Entry Cost
- Boss Keys Required: 5
- Constraint: Projects must span multiple life domains

### Promotion Exam Quest
**Theme:** Command Authority  
**Conditions:**
- Design and execute a self-imposed 7-day system
- Must include:
  - One sacrifice rule (comfort removal)
  - One leadership action (mentor, teach, delegate)

### Hidden Objective (Ace)
- No detected motivation spikes (consistency-based execution)

### Rewards
- Rank → A
- Unlock Level Cap → 90
- Ace Bonus:
  - Prestige Currency
  - UI Theme Unlock
  - Title: “Authority”

---

## A → S Rank

### Prerequisites
- Level ≥ 100
- Stats:
  - All Core Stats ≥ 60
- Integrity Score ≥ 90%

### Entry Cost
- Boss Keys Required: 8
- Constraint:
  - Includes ≥ 1 Legacy Project (life-defining goal)

### Promotion Exam Quest
**Theme:** The Monarch’s Ascent  
**Conditions:**
- 30-day evaluation window
- No Penalty Zone entries
- Sustained high-effort output
- One irreversible life commitment

### Hidden Objective (Ace)
- System detects net-positive influence on others

### Rewards
- Rank → S
- Unlock National-Level Status
- Ace Bonus:
  - Cosmetic-only Power Symbols
  - God-Mode Privileges (non-stat)

---

## Failure Rules (Global)

- Rank is NEVER downgraded.
- Failed Promotion Exam:
  - Rank unchanged
  - XP remains frozen
  - Penalty Zone activated
- Retry requires:
  - Completion of Penalty Quest
  - New Boss Key consumption

---

## Non-Goals (v1)
- No auto-promotion
- No time-based rank progression
- No rank regression mechanics
