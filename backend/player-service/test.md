Below is the end to end testing of a player from creation to rank promotion(F -> E), tested on git bash while following the 'Testing Plan' artifact. Review them all from 1 to 5 : 
1) 
curl -X POST "http://localhost:8080/players?username=SoloLeveler"
{"identity":{"playerId":"ad2414a3-4f33-4dbb-813d-e32b45310f51","username":"SoloLeveler","createdAt":"2026-02-04T18:26:20.6698001","systemVersion":"v1"},"progression":{"level":1,"currentXp":0,"rank":"F","rankProgressScore":0.0,"xpFrozen":false},"attributes":[{"attributeType":"DISCIPLINE","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"FOCUS","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"PHYSICAL_ENERGY","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"MENTAL_RESILIENCE","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"LEARNING_SPEED","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"EMOTIONAL_CONTROL","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"STR","baseValue":0.0,"currentValue":0.0,"growthVelocity":0.1,"decayRate":0.0},{"attributeType":"INT","baseValue":0.0,"currentValue":0.0,"growthVelocity":0.1,"decayRate":0.0},{"attributeType":"VIT","baseValue":0.0,"currentValue":0.0,"growthVelocity":0.1,"decayRate":0.0},{"attributeType":"SEN","baseValue":0.0,"currentValue":0.0,"growthVelocity":0.1,"decayRate":0.0}],"psychState":{"momentum":50,"complacency":0,"stressLoad":0,"confidenceBias":50},"metrics":{"questSuccessRate":0.0,"averageQuestDifficulty":0.0,"failureStreak":0,"recoveryRate":1.0},"activeFlags":[],"temporalState":{"lastQuestCompletedAt":null,"activeStreakDays":0,"restDebt":0.0,"burnoutRiskScore":0.0,"consecutiveDailyFailures":0},"history":{"lastEgoBreakerAt":null,"completedQuests":[],"failedQuests":[],"notableEvents":[]}}


2) 
$ curl -X POST "http://localhost:8080/api/admin/players/ad2414a3-4f33-4dbb-813d-e32b45310f51/add-xp?amount=1000"

user@DESKTOP-OLSCBDL MINGW64 ~/Desktop/life-os (frontend-v1)
$ curl -X GET "http://localhost:8080/players/ad2414a3-4f33-4dbb-813d-e32b45310f51/state"
{"identity":{"playerId":"ad2414a3-4f33-4dbb-813d-e32b45310f51","username":"SoloLeveler","createdAt":"2026-02-04T18:26:20.6698","systemVersion":"v1"},"progression":{"level":5,"currentXp":0,"rank":"F","rankProgressScore":0.0,"xpFrozen":false},"attributes":[{"attributeType":"DISCIPLINE","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"FOCUS","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"PHYSICAL_ENERGY","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"MENTAL_RESILIENCE","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"LEARNING_SPEED","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"EMOTIONAL_CONTROL","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"STR","baseValue":0.0,"currentValue":0.0,"growthVelocity":0.1,"decayRate":0.0},{"attributeType":"INT","baseValue":0.0,"currentValue":0.0,"growthVelocity":0.1,"decayRate":0.0},{"attributeType":"VIT","baseValue":0.0,"currentValue":0.0,"growthVelocity":0.1,"decayRate":0.0},{"attributeType":"SEN","baseValue":0.0,"currentValue":0.0,"growthVelocity":0.1,"decayRate":0.0}],"psychState":{"momentum":50,"complacency":0,"stressLoad":0,"confidenceBias":50},"metrics":{"questSuccessRate":0.0,"averageQuestDifficulty":0.0,"failureStreak":0,"recoveryRate":1.0},"activeFlags":[],"temporalState":{"lastQuestCompletedAt":null,"activeStreakDays":0,"restDebt":0.0,"burnoutRiskScore":0.0,"consecutiveDailyFailures":0},"history":{"lastEgoBreakerAt":null,"completedQuests":[],"failedQuests":[],"notableEvents":[]}}


3) 
$ curl -X POST "http://localhost:8080/api/admin/players/ad2414a3-4f33-4dbb-813d-e32b45310f51/update-attribute?type=STR&valueChange=10.0"

user@DESKTOP-OLSCBDL MINGW64 ~/Desktop/life-os (frontend-v1)
$ curl -X POST "http://localhost:8080/api/admin/players/ad2414a3-4f33-4dbb-813d-e32b45310f51/update-attribute?type=INT&valueChange=10.0"

user@DESKTOP-OLSCBDL MINGW64 ~/Desktop/life-os (frontend-v1)
$ curl -X POST "http://localhost:8080/api/admin/players/ad2414a3-4f33-4dbb-813d-e32b45310f51/boss-key
s?rank=F&count=1"

user@DESKTOP-OLSCBDL MINGW64 ~/Desktop/life-os (frontend-v1)
$ curl -X GET "http://localhost:8080/api/progression/ad2414a3-4f33-4dbb-813d-e32b45310f51/can-promote
"
false

4) 
curl -X POST "http://localhost:8080/api/progression/ad2414a3-4f33-4dbb-813d-e32b45310f51/request-pr
omotion"
{"timestamp":"2026-02-04T13:13:17.802+00:00","status":500,"error":"Internal Server Error","path":"/api/progression/ad2414a3-4f33-4dbb-813d-e32b45310f51/request-promotion"


5) 
curl -X POST "http://localhost:8080/api/progression/ad2414a3-4f33-4dbb-813d-e32b45310f51/process-outcome?success=true"
{"timestamp":"2026-02-04T13:14:48.615+00:00","status":500,"error":"Internal Server Error","path":"/api/progression/ad2414a3-4f33-4dbb-813d-e32b45310f51/process-outcome"}
user@DESKTOP-OLSCBDL MINGW64 ~/Desktop/life-os (frontend-v1)
$ curl -X GET "http://localhost:8080/players/ad2414a3-4f33-4dbb-813d-e32b45310f51/state"
{"identity":{"playerId":"ad2414a3-4f33-4dbb-813d-e32b45310f51","username":"SoloLeveler","createdAt":"2026-02-04T18:26:20.6698","systemVersion":"v1"},"progression":{"level":5,"currentXp":0,"rank":"F","rankProgressScore":0.0,"xpFrozen":false},"attributes":[{"attributeType":"DISCIPLINE","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"FOCUS","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"PHYSICAL_ENERGY","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"MENTAL_RESILIENCE","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"LEARNING_SPEED","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"EMOTIONAL_CONTROL","baseValue":10.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.01},{"attributeType":"STR","baseValue":0.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.0},{"attributeType":"INT","baseValue":0.0,"currentValue":10.0,"growthVelocity":0.1,"decayRate":0.0},{"attributeType":"VIT","baseValue":0.0,"currentValue":0.0,"growthVelocity":0.1,"decayRate":0.0},{"attributeType":"SEN","baseValue":0.0,"currentValue":0.0,"growthVelocity":0.1,"decayRate":0.0}],"psychState":{"momentum":50,"complacency":0,"stressLoad":0,"confidenceBias":50},"metrics":{"questSuccessRate":0.0,"averageQuestDifficulty":0.0,"failureStreak":0,"recoveryRate":1.0},"activeFlags":[],"temporalState":{"lastQuestCompletedAt":null,"activeStreakDays":0,"restDebt":0.0,"burnoutRiskScore":0.0,"consecutiveDailyFailures":0},"history":{"lastEgoBreakerAt":null,"completedQuests":[],"failedQuests":[],"notableEvents":[]}}


---

## The Path to Rank E: A Simulation Story

This story guides you through a realistic simulation of a player rising from an F-Rank "Nothing" to an E-Rank "Novice".

### Act 1: The Awakening (Creation)
**SoloLeveler** wakes up in the system. They are Level 1, Rank F, with base stats of 10.0 in Discipline/Focus but **0.0** in physical traits like STR.

**Command:**
```bash
curl -X POST "http://localhost:8080/players?username=SoloLeveler"
```

---

### Act 2: The Daily Grind (XP & Leveling)
For 5 days, SoloLeveler completes simple daily quests. They earn XP and reach **Level 5**. At this point, their XP "freezes" because they've hit the Rank F ceiling.

**Simulation (Admin Jump):**
```bash
# Jump to the F-Rank cap (Level 5)
curl -X POST "http://localhost:8080/api/admin/players/{playerId}/add-xp?amount=1000"
```

---

### Act 3: Breaking the Limits (Attribute Prep)
To reach Rank E, SoloLeveler must prove they have the physical strength and mental sharpness. The Rank Transition requirements for F -> E are minimal, but let's imagine they need **10.0 STR** and **10.0 INT**.

**Simulation (Admin Stat Update):**
```bash
# Train STR
curl -X POST "http://localhost:8080/api/admin/players/{playerId}/update-attribute?type=STR&valueChange=10.0"
# Train INT
curl -X POST "http://localhost:8080/api/admin/players/{playerId}/update-attribute?type=INT&valueChange=10.0"
```

---

### Act 4: The Golden Key (Boss Key)
Promotion isn't free. SoloLeveler must acquire a **Boss Key (Rank F)** from a high-difficulty quest or the system store.

**Simulation (Admin Grant):**
```bash
curl -X POST "http://localhost:8080/api/admin/players/{playerId}/boss-keys?rank=F&count=1"
```

---

### Act 5: The Exam (Promotion Request)
With the key and stats, SoloLeveler is now **Eligible**. They request the promotion. The system consumes the key and spawns a **CRITICAL Rank Exam Quest**.

**Command:**
```bash
# Check if ready (Should return true now)
curl -X GET "http://localhost:8080/api/progression/{playerId}/can-promote"

# Request the Exam
curl -X POST "http://localhost:8080/api/progression/{playerId}/request-promotion"
```

---

### Act 6: Ascension (Processing Outcome)
SoloLeveler faces the exam and **SUCCEEDS**. Their rank is promoted to E, their Level Cap increases to 10, and their XP is unfrozen.

**Command:**
```bash
curl -X POST "http://localhost:8080/api/progression/{playerId}/process-outcome?success=true"
```

**Final Verification:**
SoloLeveler is now **Rank E**.
```bash
curl -X GET "http://localhost:8080/players/{playerId}/state"
```

NOTE/ Observation:
1) No proper loggin system, not able to understand when i hit an api call i do not see any thign or logs in running process to identiy whether it worked or not
2) No proper response formats on POST and GET methods, some return empty. check above
3) Need to know game mechanics first so simulate a dummy store for me on how a player progress from F to E rank, make it realistic and descriptive for human understanding