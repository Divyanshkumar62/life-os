




# Later.md - Future Tasks

## Push Notifications / Firebase Integration

### Completed
- [x] Firebase Admin SDK dependency added (pom.xml)
- [x] PushNotificationService with all notification types
- [x] FCM token endpoints in PlayerStateController
- [x] Async/non-blocking notifications (@Async)
- [x] Scheduled midnight countdown (11 PM daily)
- [x] NotificationEventHandler wired to domain events
- [x] RankPromotionEvent created and wired

### Pending
- [ ] Add Firebase credentials (firebase-adminsdk JSON key file)
- [ ] Integrate Firebase Admin SDK in PushNotificationService (replace TODO logging)
- [ ] Add notification service configuration in application.properties/yaml
- [ ] Test FCM message delivery

---

## Job Change System

### Completed
- [x] PlayerIdentity: jobClass, classMultiplier, classUnlockedAt, jobChangeStatus, jobChangeCooldownUntil fields
- [x] JobChangeService: trigger, accept, delay, complete, fail logic
- [x] JobChangeArchitect: 3-day gauntlet generator (Day 1/2/3 quests)
- [x] JobClassCalculator: archetype determination (Vanguard/Scholar/Shadow)
- [x] JobChangeController: status, accept, delay, skip-cooldown endpoints
- [x] LevelUpRewardHandler: triggers job change at Level 40
- [x] Project suspension/resume on job change
- [x] JobChangeQuest entity and repository for storing gauntlet quests
- [x] Real stats fetching from player_attribute table
- [x] Physical quest ratio calculation from history
- [x] Award 20 stat points on completion
- [x] Award 2 A-rank items on completion
- [x] Award 50,000 gold on completion
- [x] Unlock theme based on job class
- [x] Throw player into Penalty Zone on failure
- [x] Send notification on job change failure
- [x] Verify "Elixir of Second Awakening" item for cooldown skip
- [x] Deduct item from inventory on cooldown skip
- [x] Added quests endpoints: GET /quests, POST /quest/{id}/complete, POST /quest/{id}/fail
- [x] PlayerMetadata: added unlockedThemes field

### Pending
- [ ] Add "Elixir of Second Awakening" item to shop database (itemCode: ELIXIR_SECOND_AWAKENING)
- [ ] Add A-RANK items to shop database for rewards
- [ ] Test the complete job change flow

---

## Theme System (Partial)
- PlayerMetadata has ui_theme and unlocked_themes fields
- StatusWindowAggregatorService uses theme from profile
- Job Change unlocks themes automatically

---

## Red Gate System

### Completed (Backend)
- [x] RED_GATE QuestType enum
- [x] PlayerIdentity: redGateActive, redGateExpiresAt, redGateQuestId fields
- [x] RedGateService: full logic (trigger, complete, fail, expire)
- [x] Random trigger (12%) on daily reset
- [x] Key trigger via S_RANK_RED_GATE_KEY item
- [x] Red Gate Quest generation (DifficultyTier.RED)
- [x] Daily quests skipped when Red Gate active
- [x] Failure: streak reset + 10% gold drain
- [x] Success: 3x XP/Gold + artifact drop + +2 attribute
- [x] Push notifications
- [x] Shop/Inventory lock API (isShopLocked, isInventoryLocked)
- [x] Remaining time API (getRemainingSeconds)

### Frontend Only (Cannot implement in backend)
- [x] Red Gate Glitch animation (screen shake + digital distortion) - Added in App.tsx
- [x] Audio: red-gate-alarm.mp3 via useSystemAudio hook - Already exists
- [x] UI theme hard-swap to "theme-red" - Added in App.tsx
- [x] Temporal countdown display in center of screen - Added in RedGatePopup

### Frontend Implementation (Completed)
- [x] RedGateAPI and JobChangeAPI endpoints in api.ts
- [x] RedGateContext for state management
- [x] RedGatePopup component with countdown timer
- [x] JobChangePopup component
- [x] App.tsx integration for Red Gate override
- [x] Shop/Inventory lock when Red Gate active
- [x] useSystemAudio hook plays red-gate-alarm.mp3

### Database Items Needed
- [ ] S_RANK_RED_GATE_KEY item in shop (50,000 gold cost)
- [ ] At least 1 ARTIFACT item for guaranteed drop

---

## General / Testing
- [ ] End-to-end testing of all new features
- [ ] Ensure database migrations run for new columns
