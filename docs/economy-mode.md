✅ Economy Engine v1 — FINALIZED SPEC
1️⃣ Currency Model

Single Currency: GOLD

Meaning:

Gold = immediate dopamine

XP = long-term growth

They are never interchangeable

Implementation Rule

currency: {
  gold: number // integer only
}


No secondary currencies. No gems. No soft points.

2️⃣ Gold Generation Rules

Effort-Based ONLY

Gold is awarded from:

Daily Quests

Promotion Quests

Projects (milestones & completion)

Explicitly NOT allowed:

Login rewards

Time-based accrual

Idle generation

Streak-only bonuses

Core Rule

If no effort → no gold.

3️⃣ Shop Power Level (Critical Lock)

Convenience / Cosmetic ONLY

Allowed Items:

🧪 Potions

Streak Repair (very limited, costly)

🎨 Themes / UI Skins

⚙️ Quality of Life upgrades

Example: +1 daily reroll, UI clarity tools

Strictly Forbidden

❌ XP

❌ Stats

❌ Rank skips

❌ Boss keys

❌ Promotion bypasses

Ranks are sacred. Gold cannot touch them.

4️⃣ Gold Sink Strategy

Recurring Sinks > One-time sinks

Design Intent:

Gold should never pile up infinitely

Users should feel poor but powerful

Examples:

Potions expire after use

Cosmetic tiers get progressively expensive

QoL upgrades have maintenance cost (later versions)

5️⃣ Penalty Zone Interaction

Hard Lock Confirmed

When:

penalty_active === true


Then:

🚫 Shop button disabled

🚫 No spending gold to escape

🚫 No UI loopholes

Only escape:

Complete Penalty Quest

This preserves fear, respect, and authority of the System.

🧠 ECONOMY ENGINE v1 — DESIGN PHILOSOPHY CHECK

No pay-to-win

No grind exploits

No comfort inflation

Gold feels earned

Punishment feels unavoidable

This is rare discipline. Most apps fail right here.