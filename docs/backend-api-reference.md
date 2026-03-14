# Life-OS Backend API Documentation

## Overview

Life-OS is a gamified productivity system inspired by *Solo Leveling*. The backend is a **rule-driven, stateful progression system** where every action has consequences, rewards are earned, and failures carry costs. The system enforces rules deterministically without negotiation or bypass options.

## Architecture

### Technology Stack
- **Language**: Java 17+
- **Framework**: Spring Boot
- **Database**: MySQL (production) / H2 (development/testing)
- **API Style**: REST
- **Event System**: In-process Domain Events for service decoupling

### Core Design Principles
1. **State > Actions** - System reacts to state transitions, not user intent
2. **Event-Driven Decoupling** - Services emit domain events instead of calling each other
3. **Penalty Supremacy** - Penalty overrides all progression systems
4. **Database-Enforced Integrity** - Critical rules enforced at DB level

---

## Base URL

```
Development: http://localhost:8080
Production: Configured via environment
```

---

## Authentication

Currently, authentication is handled via `playerId` passed as:
- **Path parameter**: `/api/player/{playerId}/...`
- **Query parameter**: `?playerId=uuid`

> **Note**: In production, implement proper JWT/OAuth authentication.

---

## Common Headers

```http
Content-Type: application/json
Accept: application/json
```

---

## API Endpoints

### 1. Onboarding

New players must complete onboarding before accessing the main system.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/onboarding/start?username={name}` | Create new player account |
| POST | `/api/onboarding/{playerId}/trial/complete` | Complete tutorial quest (Courage of the Weak) |
| POST | `/api/onboarding/{playerId}/awakening` | Submit questionnaire (6-month goal, biggest challenge, archetype) |
| GET | `/api/onboarding/{playerId}/status` | Get onboarding progress |

#### Request Bodies

**QuestionnaireRequest** (POST /awakening):
```json
{
  "sixMonthGoal": "string",
  "biggestChallenge": "string",
  "archetype": "BRAINS | BRAWN | BALANCED",
  "wakeUpTime": "HH:mm",
  "timezone": "Asia/Kolkata"
}
```

#### Response: OnboardingResponse
```json
{
  "playerId": "uuid",
  "username": "string",
  "status": "PENDING_TRIAL | TRIAL_COMPLETE | PENDING_AWAKENING | AWAKENING_COMPLETE | ACTIVE",
  "currentQuestId": "uuid or null"
}
```

---

### 2. Player State (Status Window)

The primary endpoint for getting player status. Call this frequently to keep UI in sync.

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/player/status-window/{playerId}` | Get full player status (IDENTITY, PROGRESSION, ATTRIBUTES, ECONOMY, SYSTEM_STATE) |

#### Response: StatusWindowResponse

```json
{
  "identity": {
    "level": 1,
    "rank": "E",
    "title": "Monster Slayer",
    "equippedTheme": "theme-default"
  },
  "progression": {
    "currentXp": 150,
    "maxXpForLevel": 500
  },
  "attributes": {
    "STR": 10,
    "INT": 12,
    "VIT": 8,
    "SEN": 5,
    "freePoints": 0
  },
  "economy": {
    "gold": 100
  },
  "systemState": {
    "penaltyActive": false,
    "activeBuffs": []
  }
}
```

**Attribute Types:**
- `STR` - Strength (physical tasks)
- `INT` - Intelligence (cognitive tasks)
- `VIT` - Vitality (health/energy management)
- `SEN` - Sensation (perception/awareness)

**Rank Progression:**
| Rank | Level Cap | Project Slots | System Dailies |
|------|------------|---------------|----------------|
| F    | 5          | 1             | 1              |
| E    | 10         | 1             | 2              |
| D    | 25         | 1             | 3              |
| C    | 45         | 2             | 4              |
| B    | 70         | 3             | 4              |
| A    | 90         | 3             | 5              |
| S    | 100        | 5             | 5              |

---

### 3. Quests

Quests are system-assigned atomic tasks. They can be daily tasks, penalty quests, or project sub-quests.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/quests` | Create new quest (system-generated) |
| POST | `/api/quests/{questId}/complete` | Mark quest as completed |
| POST | `/api/quests/{questId}/fail` | Mark quest as failed |
| POST | `/api/quests/{questId}/expire` | Expire quest (system only) |
| GET | `/api/quests/red-gate/{playerId}/status` | Check Red Gate status |
| POST | `/api/quests/red-gate/{playerId}/trigger-key` | Trigger Red Gate with boss key |
| POST | `/api/quests/red-gate/{playerId}/complete` | Complete Red Gate |
| POST | `/api/quests/red-gate/{playerId}/fail` | Fail Red Gate |

#### Quest Types
- `DISCIPLINE` - Daily habits
- `PHYSICAL` - Exercise, health
- `COGNITIVE` - Learning, thinking
- `CAREER` - Work-related
- `REFLECTION` - Journaling, review
- `RECOVERY` - Rest, sleep
- `EGO_BREAKER` - High-difficulty challenge
- `PROMOTION_EXAM` - Rank advancement exam
- `PENALTY` - Penalty zone task
- `SYSTEM_TRIAL` - Tutorial quest
- `INTEL_GATHERING` - Diagnostic quest (new players)
- `RED_GATE` - Survival gate event

#### Quest States
- `ASSIGNED` - Quest given but not started
- `ACTIVE` - Player accepted and working
- `COMPLETED` - Successfully finished
- `FAILED` - Player failed manually
- `EXPIRED` - Missed deadline

#### Difficulty Tiers
- `TIER_1` through `TIER_6` (E → S rank equivalent)
- `RED` - Extreme difficulty (Red Gate only)

#### QuestResponse (Internal)
```json
{
  "questId": "uuid",
  "playerId": "uuid",
  "title": "string",
  "description": "string",
  "questType": "DISCIPLINE",
  "difficultyTier": "TIER_2",
  "priority": "NORMAL",
  "state": "ACTIVE",
  "assignedAt": "ISO8601",
  "startsAt": "ISO8601",
  "deadlineAt": "ISO8601",
  "expectedXP": 50,
  "expectedGold": 10
}
```

---

### 4. Projects (Dungeons)

Projects are long-term commitments (7+ days) that yield **Boss Keys** required for promotion exams.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/projects/create` | Create new project/dungeon |
| GET | `/api/projects?playerId={id}` | List player's projects |
| GET | `/api/projects/{projectId}` | Get project details |
| GET | `/api/projects/{projectId}/quests` | Get project sub-quests |
| POST | `/api/projects/{projectId}/equip/{questId}` | Equip quest to project |
| POST | `/api/projects/{projectId}/complete` | Complete project (all sub-quests done) |
| POST | `/api/projects/{projectId}/abandon` | Abandon project (forfeit rewards) |

#### Project Creation Rules
- Minimum duration: 7 days
- Minimum sub-tasks: 5
- Rank-based slot limits (see Rank table above)
- Cannot have active Intel Gathering quests

#### Project States
- `ACTIVE` - In progress
- `COMPLETED` - Successfully finished (rewards Boss Key)
- `FAILED` - Missed deadline (no rewards)
- `ABANDONED` - Player quit (no rewards)

#### Request: ProjectCreationRequest
```json
{
  "playerId": "uuid",
  "title": "Learn Java Spring Boot",
  "description": "Complete a full course and build 3 projects",
  "minDurationDays": 14,
  "minSubtasks": 10
}
```

#### Response: Project
```json
{
  "id": "uuid",
  "playerId": "uuid",
  "title": "Learn Java Spring Boot",
  "description": "Complete a full course...",
  "rankRequirement": "E",
  "difficultyTier": 2,
  "minDurationDays": 14,
  "minSubtasks": 10,
  "startDate": "ISO8601",
  "hardDeadline": "ISO8601",
  "status": "ACTIVE",
  "completionPercentage": 30,
  "bossKeyReward": 1,
  "createdAt": "ISO8601"
}
```

---

### 5. Progression (Rank & Promotion)

Rank advancement requires completing **Promotion Exams** that consume **Boss Keys**.

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/progression/{playerId}/check-gate` | Check if player reached level cap |
| GET | `/api/progression/{playerId}/can-promote` | Check if can start promotion |
| POST | `/api/progression/{playerId}/request-promotion` | Start promotion exam |
| POST | `/api/progression/{playerId}/process-outcome?success={true\|false}` | Process exam result |

#### Promotion Flow
1. Player reaches level cap for current rank
2. Player has required Boss Keys (1 for E→D, more for higher ranks)
3. Player requests promotion → Exam quest generated
4. Player completes exam quest
5. System processes outcome:
   - **Success**: Rank increases, new level cap unlocked
   - **Failure**: Penalty zone entry, streak reset

#### Response: RankExamAttempt
```json
{
  "id": "uuid",
  "playerId": "uuid",
  "fromRank": "E",
  "toRank": "D",
  "requiredBossKeys": 1,
  "consumedBossKeys": 1,
  "status": "PASSED",
  "attemptNumber": 1,
  "unlockedAt": "ISO8601",
  "completedAt": "ISO8601"
}
```

---

### 6. Shop & Economy

Gold is earned through quests and projects. Shop items are **cosmetic/convenience only** - no power progression items.

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/shop/items?playerId={id}` | List available items |
| POST | `/api/shop/purchase/{itemCode}?playerId={id}` | Purchase item |
| GET | `/api/shop/inventory?playerId={id}` | View player inventory |

#### Shop Rules
- Shop disabled during Penalty Zone
- Shop disabled until onboarding complete
- Cannot buy XP, stats, rank skips, or boss keys
- **Gold Sinks**: Potions (streak repair), themes, QoL items

#### Gold Generation Sources
- Daily Quests completion
- Project completion
- Promotion exam success

#### Gold Multipliers (Streak Bonus)
| Streak | Bonus |
|--------|-------|
| 3 days | +5% |
| 7 days | +10% |
| 14 days | +20% |
| 30 days | +35% |
| 60 days | +50% (cap) |

---

### 7. System Voice & Alerts

System messages are cold, factual announcements. They are **event-driven** and **ephemeral** (fire-and-forget).

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/system/alerts/{playerId}` | Get pending system messages |

#### System Voice Rules
- Triggered by domain events only
- Suppressed during Penalty Zone (except critical messages)
- No conversation/chat capability

---

### 8. Notifications (FCM)

Push notification token management.

| Method | Endpoint | Description |
|--------|----------|-------------|
| PUT | `/api/player/update-fcm-token` | Update Firebase Cloud Messaging token |

#### Request
```json
{
  "playerId": "uuid",
  "token": "firebase-token-string"
}
```

---

### 9. Red Gate (High-Stakes Events)

Random survival events with extreme difficulty and rewards.

#### Trigger Conditions
1. **Random**: 10-15% chance on first app open after 04:00 reset
2. **Key Trigger**: Use S-Rank Red Gate Key (50,000 Gold)

#### Red Gate Rules
- **Duration**: 4-12 hours
- **Lockout**: Daily quests and projects hidden
- **Failure**: Double penalty (streak reset + 10% gold drain)
- **Success**:
  - 3x standard Boss Kill rewards
  - Guaranteed artifact drop
  - +2 to primary attribute

#### API Endpoints
- GET `/api/quests/red-gate/{playerId}/status` - Check if active
- POST `/api/quests/red-gate/{playerId}/trigger-key` - Use boss key
- POST `/api/quests/red-gate/{playerId}/complete` - Complete
- POST `/api/quests/red-gate/{playerId}/fail` - Fail

---

### 10. Admin (Debug/Testing)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin/players/{playerId}/level?level={n}` | Set player level |
| POST | `/api/admin/players/{playerId}/add-xp?xp={n}` | Add XP |
| POST | `/api/admin/players/{playerId}/update-attribute?type={STR}&value={10}` | Update attribute |
| POST | `/api/admin/players/{playerId}/boss-keys?count={1}&rank={E}` | Add boss keys |
| POST | `/api/admin/players/{playerId}/penalty/enter` | Force enter penalty |
| POST | `/api/admin/players/{playerId}/penalty/exit` | Force exit penalty |
| POST | `/api/admin/migration/recalculate-levels` | Recalculate all levels |

> **Note**: These endpoints should be protected in production.

---

## Domain Events

The system uses events for decoupling. Key events:

| Event | Critical | Handlers |
|-------|----------|----------|
| DailyQuestCompleted | No | Streak, Reward |
| DailyQuestFailed | Yes | Penalty |
| QuestCompleted | No | Reward, Voice |
| PenaltyZoneEntered | Yes | Voice, Streak |
| PenaltyQuestCompleted | Yes | Penalty Exit |
| StreakBroken | Yes | Voice |
| ProjectCompleted | Yes | Progression |
| RankPromotionPassed | Yes | Voice |

---

## System Invariants (Enforced at DB Level)

1. **Penalty Embargo**: No XP/Gold increase when penalty_active = true
2. **Rank Ceiling**: Level cannot exceed rank_cap unless promoted
3. **Slot Limits**: Active projects limited by rank
4. **State Exclusivity**: Cannot be in penalty AND promotion simultaneously
5. **Key Purity**: Boss Keys ONLY from project completion

---

## WebSocket (Future)

Not implemented in V1. For real-time updates, poll `/api/player/status-window/{playerId}` every 5-10 seconds.

---

## Error Responses

```json
{
  "error": "string",
  "message": "string",
  "timestamp": "ISO8601"
}
```

#### Common HTTP Codes
- `200` - Success
- `400` - Bad Request (validation failed)
- `403` - Forbidden (onboarding incomplete, penalty active, etc.)
- `404` - Not Found
- `500` - Internal Server Error

---

## Frontend Integration Checklist

1. **Onboarding Flow**
   - [ ] Call `/api/onboarding/start` to create account
   - [ ] Display trial quest
   - [ ] Call `/api/onboarding/{playerId}/trial/complete` when done
   - [ ] Show questionnaire form
   - [ ] Call `/api/onboarding/{playerId}/awakening` to submit

2. **Main Dashboard**
   - [ ] Poll `/api/player/status-window/{playerId}` on app launch
   - [ ] Subscribe to system alerts via `/api/system/alerts/{playerId}`
   - [ ] Display rank, level, XP progress bar

3. **Daily Quests**
   - [ ] Display active quests from backend
   - [ ] Show complete/fail buttons
   - [ ] Handle deadline countdown

4. **Projects**
   - [ ] List projects with completion percentage
   - [ ] Show create form (min 7 days, 5 subtasks)
   - [ ] Handle boss key rewards

5. **Shop**
   - [ ] Check if onboarding complete before showing
   - [ ] Disable during penalty zone
   - [ ] Display gold balance

6. **Red Gate**
   - [ ] Show glitch animation on trigger
   - [ ] Play alarm sound
   - [ ] Lock standard UI
   - [ ] Show countdown timer

7. **Promotion**
   - [ ] Show promotion button when eligible (level cap + boss key)
   - [ ] Handle exam quest
   - [ ] Show success/failure consequences

---

## Testing Endpoints

For development, use curl:

```bash
# Start onboarding
curl -X POST "http://localhost:8080/api/onboarding/start?username=testuser"

# Get status window
curl "http://localhost:8080/api/player/status-window/{playerId}"

# Complete a quest
curl -X POST "http://localhost:8080/api/quests/{questId}/complete"

# Check rank gate
curl "http://localhost:8080/api/progression/{playerId}/check-gate"
```

---

## Configuration

### Environment Variables
```
GEMINI_API_KEY=your-gemini-api-key
DB_PASSWORD=your-database-password
SPRING_PROFILES_ACTIVE=local|prod|mysql
```

### Profiles
- `local` - H2 in-memory, dev tools
- `mysql` - MySQL local
- `prod` - MySQL production
- `test-mysql` - MySQL testing

---

## Glossary

| Term | Definition |
|------|------------|
| **XP** | Experience points, accumulates toward level |
| **Gold** | Currency for shop items |
| **Rank** | Status tier (F → E → D → C → B → A → S) |
| **Boss Key** | Required for promotion exams |
| **Streak** | Consecutive days of completing all system dailies |
| **Penalty Zone** | Locked state after critical failures |
| **Red Gate** | Random high-difficulty survival event |
| **System Voice** | Automated system announcements |

---

*Document Version: 1.0*
*Last Updated: March 2026*
