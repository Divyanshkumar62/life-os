# Life-OS 7-Day Simulation Guide

## Overview

This document provides a detailed, day-by-day simulation of how a new player experiences the Life-OS backend system. It covers onboarding, daily quests, progression, economy, penalties, and promotion exams.

---

## System Architecture

### Core Entities
- **PlayerIdentity**: Username, player ID, account status
- **PlayerProgression**: Level, XP, rank, total XP accumulated
- **PlayerPsychState**: Momentum, stress, confidence bias
- **PlayerTemporalState**: Streak days, wake-up time, timezone
- **PlayerAttributes**: FOCUS, DISCIPLINE, STR, INT, WISDOM

### Ranks (Lowest to Highest)
```
F → E → D → C → B → A → S → SS
```

Each rank has a **level cap**:
| Rank | Level Cap |
|------|-----------|
| F    | 10        |
| E    | 10        |
| D    | 15        |
| C    | 20        |
| B    | 30        |
| A    | 50        |
| S    | 75        |
| SS   | 100       |

### XP Formula
```
XP required for level = 100 × (1.1 ^ current_level)
```
- Level 1→2: 110 XP
- Level 2→3: 121 XP
- Level 3→4: 133 XP

---

## Day 1: Onboarding

### Morning - Account Creation
**API Call:**
```
POST /api/onboarding/start?username=diyansh
```

**Backend Process:**
1. `OnboardingService.startOnboarding()` creates:
   - **PlayerIdentity** with username "diyansh"
   - **PlayerProgression**: Level 1, Rank F, 0 XP, XP not frozen
   - **PlayerPsychState**: Momentum 50, Stress 0, ConfidenceBias 50
   - **PlayerTemporalState**: Streak 0, no wake-up time yet
   - **PlayerAttributes**: All at 10.0

2. System generates **3 tutorial quests** via `IntelQuestGenerator`:
   - Quest 1: "Complete Your First Task" (PHYSICAL, Difficulty E)
   - Quest 2: "Focus for 25 Minutes" (MENTAL, Difficulty E)
   - Quest 3: "Read for 10 Minutes" (DISCIPLINE, Difficulty E)

**Response:**
```json
{
  "playerId": "uuid-1234",
  "message": "Welcome to Life-OS! Your journey begins now.",
  "onboardingStage": "TUTORIAL_QUEST_1"
}
```

### Afternoon - Setting Up Profile
**API Call:**
```
POST /api/onboarding/profile?playerId=uuid-1234&wakeUpTime=07:00&timezoneOffset=+05:30
```

**Backend Process:**
1. `OnboardingService.completeOnboarding()`:
   - Sets `wakeUpTime` to 07:00
   - Sets `timezoneOffset` to +05:30 (IST)
   - Marks `onboardingCompleted = true`
   - Sets `lastDailyReset` to today's reset time (05:00 IST)

2. **DailyQuestService** detects new profile:
   - Generates **3 daily quests** based on player rank (F)
   - Quests expire at 05:00 IST next day

### Evening - Completing First Quest
**API Call:**
```
POST /api/quests/{questId}/complete
```

**Backend Process:**
1. `QuestLifecycleService.completeQuest()` validates:
   - Quest is in ACTIVE state
   - Current time < deadline (not expired)
   - Player exists

2. **XP Calculation:**
   ```
   Success XP = baseXP × difficultyMultiplier
   Example: 50 × 1.2 (Difficulty C) = 60 XP
   ```

3. **Event Publishing:**
   ```
   QuestCompletedEvent(playerId, questId, questType)
   ↓
   RewardService listens:
     - Add XP to player
     - Add gold reward
     - Apply attribute bonuses
   ↓
   SystemVoiceService logs: "[REWARD] Quest completed. Rewards applied."
   ```

4. **State After Completion:**
   - Player XP: 60
   - Player Level: Still 1 (need 110 for level 2)
   - Gold: +25
   - Attributes: FOCUS +1.0

---

## Day 2: Building Momentum

### Morning - Wake Up & Daily Reset
**Time: 07:00 IST**

**Background Process:**
1. **DailyQuestService.performDailyResetCheck()** runs:
   - Checks player's `lastDailyReset` timestamp
   - If 24+ hours passed: increments streak
   - Generates new daily quests

2. **System generates 3 new daily quests:**
   - "Morning Run" (PHYSICAL, Difficulty D)
   - "Deep Work Session" (CAREER, Difficulty C)
   - "Meditation" (MENTAL, Difficulty E)

### Morning - Completing Quests
**API Calls:**
```
POST /api/quests/{questId}/complete  (×3)
```

**Backend Process:**
1. Each completion triggers:
   - XP gain (e.g., 40, 60, 30 XP)
   - Gold reward
   - Attribute increments

2. **Level Up Check:**
   ```
   Total XP after 3 quests: 130
   Level 1→2 requires: 110 XP
   
   Player levels up!
   Remaining XP: 130 - 110 = 20
   New Level: 2
   ```

3. **Event Publishing:**
   ```
   LevelUpEvent(playerId, newLevel=2)
   ↓
   SystemVoiceService: "[LEVEL UP] You have reached Level 2!"
   ```

### Evening - First Failure
**Situation:** Player misses the "Evening Review" daily quest (deadline passed)

**Backend Process:**
1. **QuestLifecycleService.expireQuest()**:
   - Marks quest as EXPIRED
   - Publishes `QuestExpiredEvent`

2. **Hybrid Trigger System (DailyQuestService):**
   ```
   consecutiveDailyFailures: 0 → 1
   
   If failures == 1:
   ├── Apply WARNING status flag
   └── SystemVoice: "[WARNING] You've missed a daily objective."
   ```

---

## Day 3: First Penalty Zone

### Scenario: Player misses 2 consecutive daily quests

### Morning - Penalty Zone Triggered
**Backend Process:**
1. **Hybrid System detects 2nd failure:**
   ```
   consecutiveDailyFailures: 1 → 2
   → Triggers PENALTY_ZONE
   ```

2. **PenaltyService.enterPenaltyZone():**
   - Sets player flag: `PENALTY_ZONE = true`
   - Sets `xpFrozen = true` (prevents XP gain)
   - Publishes `PenaltyZoneEnteredEvent`
   - Generates **Penalty Quest**: "Escape the Penalty Zone"

3. **System Voice:**
   ```
   "[PENALTY] Daily objectives failed. Penalty Zone activated."
   "[PENALTY] You have been restricted from XP gains until cleared."
   ```

### During Penalty Zone (24 hours)
**Restrictions:**
- ❌ Cannot gain XP from quests
- ❌ Cannot promote to next rank
- ❌ Cannot enter dungeons/projects
- ✅ Can still complete quests (no XP reward)
- ✅ Can purchase items from shop
- ✅ Can view stats

### Escaping Penalty Zone
**Option 1: Complete Penalty Quest**
```
POST /api/quests/{penaltyQuestId}/complete
```
- Removes PENALTY_ZONE flag
- Resets consecutiveDailyFailures to 0
- Unfreezes XP

**Option 2: Wait 24 hours**
- Automatic exit after cooldown

---

## Day 4: Economy & Shop

### Morning - Earning Gold
**Status after 3 days:**
- Level: 2
- XP: 20 (Level 2)
- Gold: 150
- Rank: F

**API Call:**
```
GET /api/shop/items
```

**Backend Response:**
```json
[
  {
    "code": "MANA_POTION",
    "name": "Mana Potion",
    "description": "Restores 50 Mental Energy",
    "cost": 500,
    "category": "CONSUMABLE",
    "rankRequirement": "E"
  },
  {
    "code": "BANDAGES", 
    "name": "Bandages",
    "description": "Reduces Penalty Zone duration by 30 mins",
    "cost": 100,
    "category": "CONSUMABLE",
    "rankRequirement": "E"
  }
]
```

### Purchasing Items
**API Call:**
```
POST /api/consumable/purchase?playerId=uuid&itemCode=MANA_POTION
```

**Backend Process:**
1. **ShopController.purchaseItem():**
   - Validates player has enough gold
   - Validates rank requirement (need E rank)
   - Checks stock limit

2. **Transaction:**
   ```
   Player gold: 150 - 100 = 50
   Inventory: +1 Mana Potion
   ```

---

## Day 5: Leveling & Attributes

### Morning - Attribute Progression
**Current Stats:**
```
Level: 2 (20/121 XP to next level)
Attributes:
  - FOCUS: 13.0
  - DISCIPLINE: 12.0
  - STR: 10.0
  - INT: 10.0
  - WISDOM: 10.0
```

### Completing High-Difficulty Quest
**API Call:**
```
POST /api/quests/{questId}/complete
```

**Quest Details:**
- Difficulty: B (High)
- XP Reward: 150
- Attribute: FOCUS +3.0

**Backend Process:**
1. XP Calculation:
   ```
   150 XP × 1.0 (no multipliers) = 150 XP
   Current: 20 + 150 = 170 XP
   ```

2. **Double Level Up:**
   ```
   Level 2→3 needs: 121 XP
   After 121 XP: Level 3, Remaining: 49 XP
   
   Level 3→4 needs: 133 XP
   49 < 133, stop here
   ```

3. **Final State:**
   ```
   Level: 3
   XP: 49 (towards level 4)
   FOCUS: 13.0 + 3.0 = 16.0
   ```

---

## Day 6: Reaching Rank Cap

### Morning - At Level Cap
**Current Status:**
```
Level: 10 (Level cap for Rank F)
XP: 500 (frozen)
Rank: F
```

### Trying to Level Up
**API Call:**
```
POST /api/quests/{questId}/complete
```

**Backend Process:**
1. **Rank-Gate Logic:**
   ```java
   if (level >= rank.getLevelCap()) {
       setXpFrozen(true);
       return; // XP not added
   }
   ```

2. **System Response:**
   ```
   SystemVoice: "[BLOCK] You have reached Rank F level cap. 
                 Promote to rank E to continue leveling."
   ```

3. **Player sees:**
   - "Level Cap Reached" notification
   - Prompt to attempt promotion

---

## Day 7: First Promotion Exam

### Prerequisites for E-Rank Promotion
1. ✅ Level 10 (cap reached)
2. ✅ Attributes minimum:
   - DISCIPLINE ≥ 5.0
   - FOCUS ≥ 5.0
3. ✅ Boss Key required (purchased from shop)

### Initiating Promotion
**API Call:**
```
POST /api/progression/promotion/request
```

**Backend Process:**
1. **ProgressionService.canRequestPromotion():**
   ```java
   if (level < rank.getLevelCap()) return false;
   if (noBossKey) return false;
   if (attributesBelowThreshold) return false;
   if (inPenaltyZone) return false;
   return true;
   ```

2. **Consuming Boss Key:**
   ```
   Boss Key: E_Rank Key -1
   Remaining: 0
   ```

3. **Creating Exam:**
   ```
   RankExamAttempt:
     - fromRank: F
     - toRank: E
     - status: UNLOCKED
   ```

4. **AI-Generated Promotion Quest:**
   ```
   Quest: "Rank F → E Promotion Exam"
   Type: PROMOTION_EXAM
   Difficulty: A (very hard)
   ```

### Passing the Exam
**API Call:**
```
POST /api/quests/{promotionQuestId}/complete
```

**Backend Process:**
1. **QuestLifecycleService.completeQuest():**
   - Marks quest COMPLETED
   - Publishes `QuestCompletedEvent`

2. **ProgressionService.processPromotionOutcome(true):**
   ```java
   // Success path
   attempt.setStatus(ExamStatus.PASSED);
   player.setRank(nextRank);  // F → E
   player.setLevel(1);        // Reset to level 1
   player.setXpFrozen(false);
   ```

3. **New Rank Benefits:**
   ```
   Rank: E
   Level: 1 (fresh start)
   Level Cap: 10
   New quests unlock (higher difficulty)
   New shop items unlock
   ```

4. **System Voice:**
   ```
   "[PROMOTION] Congratulations! You have been promoted to E-Rank!"
   "[PROMOTION] Your dedication has been recognized. New challenges await."
   ```

---

## API Reference Summary

### Player Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/onboarding/start` | Create new player |
| POST | `/api/onboarding/profile` | Set wake-up time |
| GET | `/api/player/{id}/state` | Get full player state |
| POST | `/api/player/{id}/xp` | Add XP (admin) |

### Quest Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/quests` | List player's quests |
| POST | `/api/quests` | Create new quest |
| POST | `/api/quests/{id}/complete` | Complete quest |
| POST | `/api/quests/{id}/fail` | Fail quest |
| POST | `/api/quests/{id}/expire` | Expire quest |

### Progression Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/progression/promotion/request` | Request promotion |
| POST | `/api/progression/promotion/process` | Process exam result |
| GET | `/api/progression/keys` | List boss keys |

### Economy Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/shop/items` | List shop items |
| POST | `/api/consumable/purchase` | Buy item |
| GET | `/api/player/{id}/gold` | Check gold balance |

---

## Event System Flow

```
QuestCompletedEvent
    ├──→ RewardService
    │       ├──→ Add XP
    │       ├──→ Add Gold
    │       └──→ Update Attributes
    │
    ├──→ ProgressionService
    │       └──→ Check Level Up
    │
    └──→ SystemVoiceService
            └──→ Log reward message

PenaltyZoneEnteredEvent
    ├──→ PlayerStateService
    │       └──→ Set xpFrozen = true
    │
    ├──→ PenaltyQuestService
    │       └──→ Generate Penalty Quest
    │
    └──→ SystemVoiceService
            └──→ Log penalty message

LevelUpEvent
    └──→ SystemVoiceService
            └──→ "[LEVEL UP] Level X achieved!"
```

---

## Database Tables

### Core Tables
- `player_identity` - User accounts
- `player_progression` - Level, XP, rank
- `player_psych_state` - Mental state metrics
- `player_temporal_state` - Streaks, timing
- `player_attribute` - FOCUS, DISCIPLINE, etc.
- `player_state_flags` - Active status flags

### Quest Tables
- `quest` - All quest definitions
- `player_quest_link` - Player-quest relationships
- `quest_mutation_log` - Audit trail

### Progression Tables
- `rank_exam_attempt` - Promotion exam records
- `user_boss_key` - Boss keys inventory

### Economy Tables
- `shop_item` - Available items
- `player_inventory` - Purchased items

---

## Conclusion

This 7-day simulation demonstrates the core Life-OS gameplay loop:

1. **Onboard** → Create character, set schedule
2. **Quest** → Complete daily/heroic quests
3. **Level** → Gain XP, upgrade attributes
4. **Earn** → Collect gold, buy items
5. **Promote** → Pass exams, reach new ranks
6. **Avoid Penalties** → Complete daily quests
7. **Repeat** → Infinite progression loop

The system is designed to mirror real-life productivity gamification, with meaningful consequences for missing commitments (penalties) and rewards for consistency (streaks, promotions).
