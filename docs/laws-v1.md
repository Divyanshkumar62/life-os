System Integrity & Invariants (Hard Laws)

This section defines non-negotiable system laws enforced at the database level (Postgres constraints, triggers, or policies).
These invariants exist to prevent game-breaking states, regardless of application bugs, race conditions, or service failures.

Rule:
If application logic violates a Law, the database must reject the operation.

Law 1: The Penalty Embargo

Statement
When penalty_active = true, the player is under full embargo.

Invariant

XP must not increase

Gold must not increase

Allowed

XP may be calculated or buffered

XP may be stored in a non-applied buffer column

Forbidden

Direct updates to xp_total, gold_balance, or equivalent reward columns

Enforcement (DB-level)

Trigger on UPDATE of XP/Gold fields

If NEW.penalty_active = true AND value increases → RAISE EXCEPTION

Reason
Prevents:

Reward leakage during Penalty Zone

Silent progression exploits

Logic drift between services

Law 2: The Rank Ceiling

Statement
A player’s current_level may never exceed their Rank’s rank_cap unless a Promotion has been successfully completed.

Invariant

current_level <= rank_cap

XP may overflow into an XP buffer

Level increment is locked until promotion success

Allowed

XP accumulation beyond cap (buffered)

Promotion attempts while capped

Forbidden

Level increment past rank cap without promotion

Enforcement (DB-level)

CHECK constraint or trigger:

If NEW.current_level > rank_cap

AND promotion_status != 'passed'
→ RAISE EXCEPTION

Reason
Prevents:

Rank skipping

Power inflation

Promotion bypass bugs

Law 3: Slot Limits

Statement
Active Projects are strictly limited by Rank-defined capacity.

Invariant

active_projects_count <= rank.project_slot_limit

Allowed

Creating new projects only if slots available

Completing or abandoning projects to free slots

Forbidden

More active projects than rank allows

Enforcement (DB-level)

Trigger on INSERT / UPDATE of Projects

Count active projects per user

If limit exceeded → RAISE EXCEPTION

Reason
Prevents:

Parallel grinding exploits

Over-allocation bugs

Rank meaning erosion

Law 4: State Exclusivity (Penalty Supremacy)

Statement
Penalty state always overrides progression states.

Invariant

A user cannot have:

penalty_active = true
AND
promotion_status = 'in_progress'


Allowed

Entering penalty immediately cancels promotion attempts

Promotion may resume only after penalty exit

Forbidden

Promotion while penalized

Dual progression states

Enforcement (DB-level)

CHECK constraint or trigger:

If both conditions are true → RAISE EXCEPTION

Reason
Prevents:

Ambiguous state machines

Exploits during promotion challenges

Conflicting UI/logic flows

Law 5: Key Purity (Boss Keys)

Statement
Boss Keys are sacred progression artifacts.

Invariant

Boss Keys can be generated only from:

Project Completion

Boss Unlock workflows

Never from:

Shop purchases

Random drops

Grind loops

Manual grants

Allowed

Key issuance via verified Project Completion events

Forbidden

Any direct insert/update from Shop or Economy flows

Enforcement (DB-level)

Restrict key creation via:

Stored procedure only

Trigger validating source_type = 'PROJECT_COMPLETION'

Any other source → RAISE EXCEPTION

Reason
Prevents:

Pay-to-win leaks

Economy inflation

Endgame trivialization

Design Principle (Explicit)

These Laws:

Do not replace application logic

Do not depend on services behaving correctly

Exist as the final authority

If the database allows it, the system allows it.
If the database rejects it, the system must adapt.

Status

These invariants are mandatory

All future features must be validated against them

Any schema change must re-verify compliance