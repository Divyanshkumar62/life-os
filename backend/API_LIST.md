# Life-OS Backend API Documentation

## Base URL
```
http://localhost:8080
```

---

## Flow Overview

```
1. Create Player
2. Start Onboarding → Complete Trial → Submit Awakening
3. Get Active Quests → Complete/Fail Quests
4. Create Projects → Equip Quests → Complete Projects
5. Shop: View Items → Purchase Items → Use Consumables
6. Progression: Check Gate → Request Promotion → Process Outcome
7. Job Change (optional)
8. Admin Operations
```

---

## 1. Player Creation

### 1.1 POST /players - Create New Player

**Description:** Initialize a new player with username

**Request:**
```bash
curl -X POST "http://localhost:8080/players?username=yourname"
```

**Response:** Returns player state with playerId (save this UUID!)

```json
{
  "playerId": "uuid-here",
  "username": "yourname",
  "level": 1,
  "currentXp": 0,
  "rank": "F",
  ...
}
```

---

## 2. Onboarding

### 2.1 POST /api/onboarding/start - Start Onboarding

**Description:** Begin onboarding process for player

**Request:**
```bash
curl -X POST "http://localhost:8080/api/onboarding/start?username=testplayer"
```

**Response:**
```json
{
  "playerId": "uuid",
  "stage": "TRIAL_QUEST",
  "trialQuestId": "uuid",
  ...
}
```

### 2.2 POST /api/onboarding/{playerId}/trial/complete - Complete Trial Quest

**Description:** Complete the trial quest (unlocks shop access)

**Request:**
```bash
curl -X POST "http://localhost:8080/api/onboarding/{playerId}/trial/complete"
```

**Example:**
```bash
curl -X POST "http://localhost:8080/api/onboarding/1e1341a0-9d02-40bf-a1aa-89ca748469c2/trial/complete"
```

### 2.3 POST /api/onboarding/{playerId}/awakening - Submit Awakening Questionnaire

**Description:** Submit 7-question awakening form

**Request:**
```bash
curl -X POST "http://localhost:8080/api/onboarding/{playerId}/awakening" \
  -H "Content-Type: application/json" \
  -d '{
    "archetype": "BRAINS",
    "primaryWeakness": "Procrastination",
    "mainGoal": "Learn coding",
    "wakeUpTime": "06:30",
    "biggestChallenge": "Time management",
    "availableTime": "2-4 hours",
    "focusArea": "Career"
  }'
```

**Field Values:**
- `archetype`: "BRAINS" | "BRAWN" | "BALANCE"
- `primaryWeakness`: String (e.g., "Procrastination", "Lack of focus")
- `mainGoal`: String (e.g., "Build muscle", "Learn coding")
- `wakeUpTime`: "HH:mm" format (e.g., "06:30")
- `biggestChallenge`: String (e.g., "Time management", "Energy")
- `availableTime`: String (e.g., "1 hour", "2-4 hours", "5+ hours")
- `focusArea`: "Physical" | "Mental" | "Career"

### 2.4 GET /api/onboarding/{playerId}/status - Get Onboarding Status

**Description:** Check current onboarding stage

**Request:**
```bash
curl -X GET "http://localhost:8080/api/onboarding/{playerId}/status"
```

**Example:**
```bash
curl -X GET "http://localhost:8080/api/onboarding/1e1341a0-9d02-40bf-a1aa-89ca748469c2/status"
```

---

## 3. Quests

### 3.1 GET /api/quests/active - Get Active Quests

**Description:** Fetch all active quests for a player

**Request:**
```bash
curl -X GET "http://localhost:8080/api/quests/active?playerId={playerId}"
```

**Example:**
```bash
curl -X GET "http://localhost:8080/api/quests/active?playerId=1e1341a0-9d02-40bf-a1aa-89ca748469c2"
```

### 3.2 POST /api/quests - Create/Assign Quest

**Description:** Create a new quest for player

**Request:**
```bash
curl -X POST "http://localhost:8080/api/quests" \
  -H "Content-Type: application/json" \
  -d '{
    "playerId": "uuid",
    "title": "Morning Workout",
    "description": "Do 30 min exercise",
    "questType": "PHYSICAL",
    "category": "NORMAL",
    "primaryAttribute": "STRENGTH",
    "difficultyTier": "D",
    "priority": "NORMAL",
    "deadlineAt": "2026-04-23T08:00:00",
    "successXp": 100,
    "failureXp": 10,
    "goldReward": 50,
    "attributeDeltas": {"STRENGTH": 1.0},
    "systemMutable": true,
    "egoBreakerFlag": false,
    "expectedFailureProbability": 0.3
  }'
```

**Field Values:**
- `questType`: "DISCIPLINE" | "PHYSICAL" | "COGNITIVE" | "CAREER" | "REFLECTION" | "RECOVERY" | "EGO_BREAKER" | "PROMOTION_EXAM" | "PENALTY" | "SYSTEM_TRIAL" | "INTEL_GATHERING" | "RED_GATE"
- `category`: "NORMAL" | "SYSTEM_DAILY" | "PROJECT_SUBTASK" | "MAIN"
- `primaryAttribute`: "STRENGTH" | "INTELLIGENCE" | "VITALITY" | "SENSE" | "DISCIPLINE" | "FOCUS" | "PHYSICAL_ENERGY" | "MENTAL_RESILIENCE" | "LEARNING_SPEED" | "EMOTIONAL_CONTROL"
- `difficultyTier`: "E" | "D" | "C" | "B" | "A" | "S" | "RED" | "TIER_1" ... "TIER_6"
- `priority`: "LOW" | "NORMAL" | "HIGH" | "CRITICAL"
- `deadlineAt`: ISO datetime (optional)

### 3.3 POST /api/quests/{questId}/complete - Complete Quest

**Description:** Mark quest as completed (success)

**Request:**
```bash
curl -X POST "http://localhost:8080/api/quests/{questId}/complete"
```

**Example:**
```bash
curl -X POST "http://localhost:8080/api/quests/a1b2c3d4-5678-90ab-cdef-123456789abc/complete"
```

### 3.4 POST /api/quests/{questId}/fail - Fail Quest

**Description:** Mark quest as failed

**Request:**
```bash
curl -X POST "http://localhost:8080/api/quests/{questId}/fail"
```

### 3.5 POST /api/quests/{questId}/expire - Expire Quest

**Description:** Expire a quest without completing/failing

**Request:**
```bash
curl -X POST "http://localhost:8080/api/quests/{questId}/expire"
```

### 3.6 GET /api/quests/red-gate/{playerId}/status - Get Red Gate Status

**Description:** Check if Red Gate is active

**Request:**
```bash
curl -X GET "http://localhost:8080/api/quests/red-gate/{playerId}/status"
```

### 3.7 POST /api/quests/red-gate/{playerId}/trigger-key - Trigger Red Gate with Key

**Description:** Use a boss key to trigger Red Gate

**Request:**
```bash
curl -X POST "http://localhost:8080/api/quests/red-gate/{playerId}/trigger-key"
```

### 3.8 POST /api/quests/red-gate/{playerId}/complete - Complete Red Gate

**Description:** Complete Red Gate quest

**Request:**
```bash
curl -X POST "http://localhost:8080/api/quests/red-gate/{playerId}/complete"
```

### 3.9 POST /api/quests/red-gate/{playerId}/fail - Fail Red Gate

**Description:** Fail Red Gate quest

**Request:**
```bash
curl -X POST "http://localhost:8080/api/quests/red-gate/{playerId}/fail"
```

---

## 4. Projects (Dungeon)

### 4.1 POST /api/projects/create - Create Project

**Description:** Create a new project (dungeon)

**Request:**
```bash
curl -X POST "http://localhost:8080/api/projects/create" \
  -H "Content-Type: application/json" \
  -d '{
    "playerId": "uuid",
    "goal": "Build a todo app",
    "userRank": "F"
  }'
```

**Field Values:**
- `userRank`: "F" | "E" | "D" | "C" | "B" | "A" | "S" | "SS"

### 4.2 POST /api/projects/{projectId}/equip/{questId} - Equip Quest

**Description:** Add a quest as sub-task to project

**Request:**
```bash
curl -X POST "http://localhost:8080/api/projects/{projectId}/equip/{questId}"
```

### 4.3 POST /api/projects/{projectId}/complete - Complete Project

**Description:** Mark project as completed

**Request:**
```bash
curl -X POST "http://localhost:8080/api/projects/{projectId}/complete"
```

### 4.4 POST /api/projects/{projectId}/abandon - Abandon Project

**Description:** Abandon a project

**Request:**
```bash
curl -X POST "http://localhost:8080/api/projects/{projectId}/abandon"
```

### 4.5 GET /api/projects - Get Player Projects

**Description:** Get all projects for player

**Request:**
```bash
curl -X GET "http://localhost:8080/api/projects?playerId={playerId}"
```

### 4.6 GET /api/projects/{projectId} - Get Project Details

**Request:**
```bash
curl -X GET "http://localhost:8080/api/projects/{projectId}"
```

### 4.7 GET /api/projects/{projectId}/quests - Get Project Quests

**Request:**
```bash
curl -X GET "http://localhost:8080/api/projects/{projectId}/quests"
```

---

## 5. Economy / Shop

### 5.1 GET /api/shop/items - Get Shop Items

**Description:** List available shop items (requires completed onboarding)

**Request:**
```bash
curl -X GET "http://localhost:8080/api/shop/items?playerId={playerId}"
```

**Example:**
```bash
curl -X GET "http://localhost:8080/api/shop/items?playerId=1e1341a0-9d02-40bf-a1aa-89ca748469c2"
```

### 5.2 POST /api/shop/purchase/{itemCode} - Purchase Item

**Description:** Buy an item using gold

**Request:**
```bash
curl -X POST "http://localhost:8080/api/shop/purchase/{itemCode}?playerId={playerId}"
```

**Available Item Codes:**
- `RUNESTONE_STEALTH` - Runestone: Stealth (100 gold)
- `RUNESTONE_MUTILATE` - Runestone: Mutilate (150 gold)
- `RED_GATE_KEY_S` - S-Rank Red Gate Key (500 gold)
- `COMMAND_ARISE` - Command: Arise (200 gold)

**Example:**
```bash
curl -X POST "http://localhost:8080/api/shop/purchase/RUNESTONE_STEALTH?playerId=1e1341a0-9d02-40bf-a1aa-89ca748469c2"
```

### 5.3 GET /api/shop/inventory - Get Player Inventory

**Request:**
```bash
curl -X GET "http://localhost:8080/api/shop/inventory?playerId={playerId}"
```

### 5.4 POST /api/consumables/use/{itemCode} - Use Consumable

**Description:** Use a consumable item

**Request:**
```bash
curl -X POST "http://localhost:8080/api/consumables/use/{itemCode}?playerId={playerId}"
```

---

## 6. Progression

### 6.1 GET /api/progression/{playerId}/check-gate - Check Rank Gate

**Description:** Check if player meets rank requirements

**Request:**
```bash
curl -X GET "http://localhost:8080/api/progression/{playerId}/check-gate"
```

### 6.2 GET /api/progression/{playerId}/can-promote - Check Promotion Eligibility

**Request:**
```bash
curl -X GET "http://localhost:8080/api/progression/{playerId}/can-promote"
```

### 6.3 POST /api/progression/{playerId}/request-promotion - Request Promotion

**Description:** Initiate promotion exam

**Request:**
```bash
curl -X POST "http://localhost:8080/api/progression/{playerId}/request-promotion"
```

### 6.4 POST /api/progression/{playerId}/process-outcome - Process Promotion Outcome

**Request:**
```bash
curl -X POST "http://localhost:8080/api/progression/{playerId}/process-outcome?success=true"
```

**Parameters:**
- `success`: true | false

---

## 7. Job Change

### 7.1 GET /api/player/job-change/{playerId}/status - Get Job Change Status

**Request:**
```bash
curl -X GET "http://localhost:8080/api/player/job-change/{playerId}/status"
```

### 7.2 GET /api/player/job-change/{playerId}/quests - Get Job Change Quests

**Request:**
```bash
curl -X GET "http://localhost:8080/api/player/job-change/{playerId}/quests"
```

### 7.3 POST /api/player/job-change/{playerId}/accept - Accept Job Change

**Request:**
```bash
curl -X POST "http://localhost:8080/api/player/job-change/{playerId}/accept"
```

### 7.4 POST /api/player/job-change/{playerId}/delay - Delay Job Change

**Request:**
```bash
curl -X POST "http://localhost:8080/api/player/job-change/{playerId}/delay"
```

### 7.5 POST /api/player/job-change/quest/{questId}/complete - Complete Job Change Quest

**Request:**
```bash
curl -X POST "http://localhost:8080/api/player/job-change/quest/{questId}/complete"
```

### 7.6 POST /api/player/job-change/quest/{questId}/fail - Fail Job Change Quest

**Request:**
```bash
curl -X POST "http://localhost:8080/api/player/job-change/quest/{questId}/fail"
```

### 7.7 POST /api/player/job-change/{playerId}/skip-cooldown - Skip Cooldown

**Request:**
```bash
curl -X POST "http://localhost:8080/api/player/job-change/{playerId}/skip-cooldown"
```

---

## 8. Player State

### 8.1 GET /players/{playerId}/state - Get Player State

**Request:**
```bash
curl -X GET "http://localhost:8080/players/{playerId}/state"
```

### 8.2 PUT /players/{playerId}/fcm-token - Update FCM Token

**Request:**
```bash
curl -X PUT "http://localhost:8080/players/{playerId}/fcm-token?token={token}"
```

### 8.3 PUT /players/{playerId}/notifications - Update Notifications

**Request:**
```bash
curl -X PUT "http://localhost:8080/players/{playerId}/notifications?enabled=true"
```

---

## 9. Status Window

### 9.1 GET /api/player/status-window/{playerId} - Get Status Window

**Request:**
```bash
curl -X GET "http://localhost:8080/api/player/status-window/{playerId}"
```

---

## 10. Admin Operations

### 10.1 POST /api/admin/players/{playerId}/level - Set Player Level

**Request:**
```bash
curl -X POST "http://localhost:8080/api/admin/players/{playerId}/level?level=5"
```

### 10.2 POST /api/admin/players/{playerId}/add-xp - Add XP

**Request:**
```bash
curl -X POST "http://localhost:8080/api/admin/players/{playerId}/add-xp?amount=1000"
```

### 10.3 POST /api/admin/players/{playerId}/update-attribute - Update Attribute

**Request:**
```bash
curl -X POST "http://localhost:8080/api/admin/players/{playerId}/update-attribute?type=STRENGTH&valueChange=5.0"
```

**Attribute Types:** STRENGTH, INTELLIGENCE, VITALITY, SENSE, DISCIPLINE, FOCUS, PHYSICAL_ENERGY, MENTAL_RESILIENCE, LEARNING_SPEED, EMOTIONAL_CONTROL

### 10.4 POST /api/admin/players/{playerId}/boss-keys - Grant Boss Keys

**Request:**
```bash
curl -X POST "http://localhost:8080/api/admin/players/{playerId}/boss-keys?rank=S&count=1"
```

### 10.5 POST /api/admin/players/{playerId}/penalty/enter - Enter Penalty Zone

**Request:**
```bash
curl -X POST "http://localhost:8080/api/admin/players/{playerId}/penalty/enter?reason=missed_quest"
```

### 10.6 POST /api/admin/players/{playerId}/penalty/exit - Exit Penalty Zone

**Request:**
```bash
curl -X POST "http://localhost:8080/api/admin/players/{playerId}/penalty/exit"
```

### 10.7 POST /api/admin/migration/recalculate-levels - Recalculate Levels

**Request:**
```bash
curl -X POST "http://localhost:8080/api/admin/migration/recalculate-levels"
```

---

## 11. System

### 11.1 GET /api/system/alerts/{playerId} - Get System Alerts

**Request:**
```bash
curl -X GET "http://localhost:8080/api/system/alerts/{playerId}"
```

---

## Testing Order (Serial Flow)

```bash
# Step 1: Create Player
curl -X POST "http://localhost:8080/players?username=testuser"

# Step 2: Start Onboarding
curl -X POST "http://localhost:8080/api/onboarding/start?username=testuser"

# Step 3: Complete Trial Quest (need playerId from Step 2)
curl -X POST "http://localhost:8080/api/onboarding/PLAYER_ID/trial/complete"

# Step 4: Submit Awakening
curl -X POST "http://localhost:8080/api/onboarding/PLAYER_ID/awakening" \
  -H "Content-Type: application/json" \
  -d '{
    "archetype":"BRAINS",
    "primaryWeakness":"Procrastination",
    "mainGoal":"Learn coding",
    "wakeUpTime":"06:30",
    "biggestChallenge":"Time management",
    "availableTime":"2-4 hours",
    "focusArea":"Career"
  }'

# Step 5: Create Quest
curl -X POST "http://localhost:8080/api/quests" \
  -H "Content-Type: application/json" \
  -d '{
    "playerId":"PLAYER_ID",
    "title":"Test Quest",
    "description":"Test desc",
    "questType":"DISCIPLINE",
    "category":"NORMAL",
    "primaryAttribute":"DISCIPLINE",
    "difficultyTier":"D",
    "priority":"NORMAL",
    "successXp":100,
    "failureXp":10,
    "goldReward":50,
    "systemMutable":true,
    "egoBreakerFlag":false,
    "expectedFailureProbability":0.3
  }'

# Step 6: Get Active Quests
curl -X GET "http://localhost:8080/api/quests/active?playerId=PLAYER_ID"

# Step 7: Complete Quest (need questId from Step 6)
curl -X POST "http://localhost:8080/api/quests/QUEST_ID/complete"

# Step 8: Create Project
curl -X POST "http://localhost:8080/api/projects/create" \
  -H "Content-Type: application/json" \
  -d '{
    "playerId":"PLAYER_ID",
    "goal":"Build app",
    "userRank":"F"
  }'

# Step 9: Shop - Get Items
curl -X GET "http://localhost:8080/api/shop/items?playerId=PLAYER_ID"

# Step 10: Shop - Purchase
curl -X POST "http://localhost:8080/api/shop/purchase/RUNESTONE_STEALTH?playerId=PLAYER_ID"

# Step 11: Check Progression
curl -X GET "http://localhost:8080/api/progression/PLAYER_ID/check-gate"

# Step 12: Get Player State
curl -X GET "http://localhost:8080/players/PLAYER_ID/state"
```

---

## Notes

- All UUIDs in format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`
- Dates in ISO 8601: `2026-04-23T08:00:00`
- Times in HH:mm: "06:30"
- Enums are case-sensitive strings
- Save returned UUIDs from each step for next steps