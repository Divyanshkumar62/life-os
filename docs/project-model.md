🧠 MASTER SUMMARY — GAMIFICATION ENGINE v1 (GA-READY)
Core Philosophy (V1)

V1 is built around three progression pillars:

Consistency (Daily Engine) → XP, Momentum, Stability

Commitment (Project / Dungeon Engine) → Boss Keys

Proof (Promotion Engine) → Rank Advancement

No pillar can substitute another.
Grinding alone does not rank you up.
Projects alone do not rank you up.
Only structured proof under pressure does.

1️⃣ DATABASE SCHEMA ADDITIONS (Projects / Keys)

Below is minimum viable schema for GA.
Names are indicative; adapt to your stack.

A. projects (Dungeon Instances)

Each row = one Dungeon run.

Project {
  id: UUID
  userId: UUID

  title: string
  description: string

  rankRequirement: Rank   // E, D, C, B
  difficultyTier: number  // 1–5 (internal tuning)

  minDurationDays: number // enforced = 7
  minSubtasks: number     // enforced = 5

  startDate: Date
  hardDeadline: Date

  status: enum(
    ACTIVE,
    COMPLETED,
    FAILED,
    ABANDONED
  )

  completionPercentage: number // derived

  bossKeyReward: number // default = 1 (v2 scalable)

  createdAt
  updatedAt
}

B. project_subtasks

Strict validation happens here.

ProjectSubtask {
  id: UUID
  projectId: UUID

  title: string
  description: string

  isCompleted: boolean
  completedAt: Date | null

  canEquipAsDailyQuest: boolean // true by default

  createdAt
}

C. boss_keys (User Inventory)
UserBossKeys {
  userId: UUID
  rank: Rank // which rank exam this key applies to
  count: number
}


V1 rule: Keys are rank-bound, not generic.
Prevents hoarding for future skips.

D. rank_exams (Promotion Attempts)
RankExamAttempt {
  id: UUID
  userId: UUID

  fromRank: Rank
  toRank: Rank

  requiredBossKeys: number // default = 1
  consumedBossKeys: number

  status: enum(
    LOCKED,
    UNLOCKED,
    PASSED,
    FAILED
  )

  attemptNumber: number

  unlockedAt
  completedAt
}

2️⃣ LOGIC FLOW — E ➝ D RANK (END-TO-END)

This is the canonical player journey in V1.

STEP 1: Entry — E-Rank Initialization

User starts at E-Rank.

System state:

Daily Quest Slots: ✅ Active

Project Slots: 1 unlocked

Promotion Exam: 🔒 Locked

Boss Keys: 0

STEP 2: Daily Grind (Stability Phase)

User performs:

Daily Quests

Habit-linked actions

Journals / routines

System rewards:

XP

Momentum

Streak integrity

⚠️ Important Constraint
Daily XP cannot unlock ranks.
It only:

Keeps Momentum healthy

Prevents Penalty Zone

Prepares user for Projects

STEP 3: Project Creation (Dungeon Entry)

User starts a Project.

System enforces STRICT VALIDATION:

Duration ≥ 7 days

Subtasks ≥ 5

Hard deadline set

Rank slot availability checked (E-rank = 1)

If validation fails → Project creation denied.

STEP 4: Project Execution (Dungeon Progression)

During Project lifecycle:

Subtasks can be equipped as Daily Quests

Completing them gives:

Standard XP (daily engine)

Progress toward Project completion

Key rule:

XP is granted even if Project eventually fails,
but Boss Keys are not.

STEP 5: Completion Check (Boss Defeat)

At deadline or final subtask completion:

✅ If 100% completed before deadline

Project status → COMPLETED

Award:

+1 Boss Key (E→D)

Unlock:

Rank Exam (if key requirement met)

❌ If failed or abandoned

Project status → FAILED / ABANDONED

Award:

❌ No Boss Key

❌ No Project XP bonus

No Penalty Zone (soft punishment)

Slot is freed

STEP 6: Promotion Exam Unlock

Condition:

UserBossKeys[E→D] >= 1

System:

Consumes 1 Boss Key

Creates RankExamAttempt

Exam status → UNLOCKED

STEP 7: Promotion Exam (Proof Phase)

Exam characteristics (V1):

Time-bound

Multi-part (focus, discipline, execution)

Cannot be brute-forced via retries

Outcomes:

✅ PASS

User Rank → D

Unlocks:

Higher XP multipliers

New quest difficulty tiers

Stronger Project templates

❌ FAIL

Rank unchanged

Exam attempt marked FAILED

Must earn another Boss Key via a new Project

3️⃣ SYSTEM GUARANTEES (WHY THIS WORKS)
🚫 No Fast Climbing

One Project slot at E/D

One Boss Key per Project

One Key per exam attempt

→ Minimum real-world time is enforced naturally

🧠 Skill Over Time, Not Days

Ranks are not day-based.
They are proof-based:

Can take weeks or months

Depends on execution quality, not login streaks