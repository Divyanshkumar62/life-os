# Life-OS API Reference

## Onboarding
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/onboarding/start?username={name}` | Create new player account |
| POST | `/api/onboarding/{playerId}/trial/complete` | Complete tutorial quest |
| POST | `/api/onboarding/{playerId}/awakening` | Set wake-up time & timezone |
| GET | `/api/onboarding/{playerId}/status` | Get onboarding progress |

## Player
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/player/{playerId}/state` | Get full player state |
| POST | `/api/player/stats/allocate` | Allocate stat points |
| GET | `/api/player/status-window/{playerId}` | Get status window data |

## Quests
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/quests` | Create new quest |
| GET | `/api/quests` | List player's quests |
| POST | `/api/quests/{questId}/complete` | Complete a quest |
| POST | `/api/quests/{questId}/fail` | Mark quest as failed |
| POST | `/api/quests/{questId}/expire` | Expire quest (system) |

## Progression
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/progression/{playerId}/check-gate` | Check level/XP status |
| GET | `/api/progression/{playerId}/can-promote` | Check if can promote |
| POST | `/api/progression/{playerId}/request-promotion` | Start promotion exam |
| POST | `/api/progression/{playerId}/process-outcome` | Process exam result |

## Shop / Economy
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/shop/items` | List available items |
| POST | `/api/shop/purchase/{itemCode}` | Purchase item |
| GET | `/api/shop/inventory` | View player inventory |
| POST | `/api/consumables/use/{itemCode}` | Use consumable item |

## Projects (Dungeons)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/projects/create` | Create new project/dungeon |
| GET | `/api/projects` | List player's projects |
| GET | `/api/projects/{projectId}` | Get project details |
| POST | `/api/projects/{projectId}/equip/{questId}` | Equip quest to project |
| POST | `/api/projects/{projectId}/complete` | Complete project |
| POST | `/api/projects/{projectId}/abandon` | Abandon project |

## Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin/players/{playerId}/level` | Set player level |
| POST | `/api/admin/players/{playerId}/add-xp` | Add XP to player |
| POST | `/api/admin/players/{playerId}/update-attribute` | Update attribute |
| POST | `/api/admin/players/{playerId}/boss-keys` | Add boss keys |
| POST | `/api/admin/players/{playerId}/penalty/enter` | Force enter penalty |
| POST | `/api/admin/players/{playerId}/penalty/exit` | Force exit penalty |
| POST | `/api/admin/migration/recalculate-levels` | Recalculate all levels |

## System
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/system/alerts/{playerId}` | Get system alerts |

## Player State (Legacy)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/players` | Create player |
| GET | `/players/{playerId}/state` | Get player state |
