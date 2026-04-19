PRD UPDATE: Red Gate Instance Generation (v2.1)
1. Objective
Implement a high-stakes, unpredictable "Survival" event that overrides the standard UI and daily quest loop. Red Gates introduce extreme difficulty, zero escape options (lockout), and massive, unique rewards.

2. Unpredictable Trigger Mechanics
Context: Red Gates should never be scheduled. They must occur with a "sense of dread."

Requirements:

The "Glitch" Trigger:

Random Chance: A 10-15% chance to trigger upon the first app open after the 04:00 AM reset.

The Key Trigger: Using the S-Rank Red Gate Key from the Store (50,000 Gold) triggers an instance immediately on the next app load.

The Environmental Shift:

The frontend must execute the "Red Gate Glitch" animation (screen shake + digital distortion).

The useSystemAudio hook must fire the red-gate-alarm.mp3.

The UI theme must hard-swap to theme-red globally.

3. Gameplay: "The Sealed Reality"
Context: In the anime, once a Red Gate closes, you cannot leave until the Boss is dead.

Requirements:

Dashboard Lockout:

All standard "Daily Quests" and "Projects" are hidden and inaccessible.

The "Shop" and "Inventory" are locked (System interference).

The Raid Quest:

The DungeonArchitectService generates a single, complex Raid Quest based on the player's Rank and Class.

Higher Difficulty: These tasks should be 2x the complexity of a Tier 3 Daily Quest (e.g., "Complete 4 hours of Deep Work" or "Run 15km").

Temporal Pressure: * Red Gates have a fixed, short expiration (e.g., 4 to 12 hours).

The Temporal Countdown must be prominently displayed in the center of the screen.

4. Higher Difficulty, Higher Payout
Context: The risk must justify the rewards.

Requirements:

Failure Penalty: * Failing a Red Gate results in a Double Penalty: Instant streak reset AND a -10% Gold drain from the PlayerEconomy.

Success Rewards:

XP/Gold: 3x the standard payout of a Boss Kill.

Artifact Drop: A 100% guaranteed drop of a random "High-Tier Artifact" (e.g., Igris’s Cape or Vessel of the Monarch).

Attribute Boost: A permanent +2 to the player's primary Class attribute (e.g., STR for Vanguard, INT for Scholar).

5. Backend Logic (Integration Points)
RedGateService.java:

Create a method generateRedGateInstance(playerId).

This method must temporarily set system_state.red_gate_active = true in the database.

DailyQuestService Override:

Add a check: if (player.isRedGateActive()) return redGateRaidQuest;.

Notification Dispatch:

Immediately send a Tier 1 Critical Push Notification via FCM: "WARNING: You have been pulled into a Red Gate. Survival is the only option."