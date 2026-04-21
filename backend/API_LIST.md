# Backend API List

## 1. Player Management

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 1.1 | POST | `/players` | Create new player |
| 1.2 | GET | `/players/{playerId}/state` | Get player state |
| 1.3 | PUT | `/players/{playerId}/fcm-token` | Update FCM token |
| 1.4 | PUT | `/players/{playerId}/notifications` | Update notification settings |

## 2. Onboarding

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 2.1 | POST | `/api/onboarding/start` | Start onboarding |
| 2.2 | POST | `/api/onboarding/{playerId}/trial/complete` | Complete trial quest |
| 2.3 | POST | `/api/onboarding/{playerId}/awakening` | Complete awakening |
| 2.4 | GET | `/api/onboarding/{playerId}/status` | Get onboarding status |

## 3. Quests

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 3.1 | GET | `/api/quests/active` | Get active quests |
| 3.2 | POST | `/api/quests` | Create new quest |
| 3.3 | POST | `/api/quests/{questId}/complete` | Complete quest |
| 3.4 | POST | `/api/quests/{questId}/fail` | Fail quest |
| 3.5 | POST | `/api/quests/{questId}/expire` | Expire quest |
| 3.6 | GET | `/api/quests/red-gate/{playerId}/status` | Get red gate status |
| 3.7 | POST | `/api/quests/red-gate/{playerId}/trigger-key` | Trigger red gate key |
| 3.8 | POST | `/api/quests/red-gate/{playerId}/complete` | Complete red gate |
| 3.9 | POST | `/api/quests/red-gate/{playerId}/fail` | Fail red gate |

## 4. Projects

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 4.1 | POST | `/api/projects/create` | Create project |
| 4.2 | POST | `/api/projects/{projectId}/equip/{questId}` | Equip quest to project |
| 4.3 | POST | `/api/projects/{projectId}/complete` | Complete project |
| 4.4 | POST | `/api/projects/{projectId}/abandon` | Abandon project |
| 4.5 | GET | `/api/projects` | Get all projects |
| 4.6 | GET | `/api/projects/{projectId}` | Get project details |
| 4.7 | GET | `/api/projects/{projectId}/quests` | Get project quests |

## 5. Economy / Shop

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 5.1 | GET | `/api/shop/items` | Get shop items |
| 5.2 | POST | `/api/shop/purchase/{itemCode}` | Purchase item |
| 5.3 | GET | `/api/shop/inventory` | Get player inventory |
| 5.4 | POST | `/api/consumables/use/{itemCode}` | Use consumable |

## 6. Progression

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 6.1 | GET | `/api/progression/{playerId}/check-gate` | Check rank gate |
| 6.2 | GET | `/api/progression/{playerId}/can-promote` | Check promotion eligibility |
| 6.3 | POST | `/api/progression/{playerId}/request-promotion` | Request promotion |
| 6.4 | POST | `/api/progression/{playerId}/process-outcome` | Process quest outcome |

## 7. Job Change

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 7.1 | GET | `/api/player/job-change/{playerId}/status` | Get job change status |
| 7.2 | GET | `/api/player/job-change/{playerId}/quests` | Get job change quests |
| 7.3 | POST | `/api/player/job-change/{playerId}/accept` | Accept job change |
| 7.4 | POST | `/api/player/job-change/{playerId}/delay` | Delay job change |
| 7.5 | POST | `/api/player/job-change/quest/{questId}/complete` | Complete job change quest |
| 7.6 | POST | `/api/player/job-change/quest/{questId}/fail` | Fail job change quest |
| 7.7 | POST | `/api/player/job-change/{playerId}/skip-cooldown` | Skip job change cooldown |

## 8. Player Status

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 8.1 | GET | `/api/player/status-window/{playerId}` | Get status window |

## 9. Player Operations

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 9.1 | POST | `/api/player/stats/allocate` | Allocate stat points |

## 10. Notifications

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 10.1 | PUT | `/api/player/update-fcm-token` | Update FCM token |

## 11. System

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 11.1 | GET | `/api/system/alerts/{playerId}` | Get system alerts |

## 12. Admin

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 12.1 | POST | `/api/admin/players/{playerId}/level` | Update player level |
| 12.2 | POST | `/api/admin/players/{playerId}/add-xp` | Add XP to player |
| 12.3 | POST | `/api/admin/players/{playerId}/update-attribute` | Update player attribute |
| 12.4 | POST | `/api/admin/players/{playerId}/boss-keys` | Update boss keys |
| 12.5 | POST | `/api/admin/players/{playerId}/penalty/enter` | Enter penalty zone |
| 12.6 | POST | `/api/admin/players/{playerId}/penalty/exit` | Exit penalty zone |
| 12.7 | POST | `/api/admin/migration/recalculate-levels` | Recalculate levels |

---

## Quick Reference by Category

- **Player**: `/players`, `/api/player`
- **Onboarding**: `/api/onboarding`
- **Quests**: `/api/quests`
- **Projects**: `/api/projects`
- **Shop**: `/api/shop`, `/api/consumables`
- **Progression**: `/api/progression`
- **Job Change**: `/api/player/job-change`
- **System**: `/api/system`
- **Admin**: `/api/admin`

---

## Notes

- All endpoints return JSON
- `{playerId}` format: UUID (e.g., `1e1341a0-9d02-40bf-a1aa-89ca748469c2`)
- `{questId}`, `{projectId}` format: UUID
- `{itemCode}` format: String (e.g., `RUNESTONE_STEALTH`)
