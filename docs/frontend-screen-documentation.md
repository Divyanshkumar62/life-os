# Life-OS Frontend Screen Documentation

## Overview
This document provides a detailed breakdown of all screens and pages in the Life-OS frontend application. Each screen is documented with its purpose, components, data sources, and UI elements.

---

## Table of Contents
1. [App Entry Point](#app-entry-point)
2. [Onboarding Screens](#onboarding-screens)
3. [Main Dashboard](#main-dashboard)
4. [Dashboard Components](#dashboard-components)
5. [Feature Screens](#feature-screens)
6. [Popup/Modal Components](#popupmodal-components)
7. [Reusable UI Components](#reusable-ui-components)

---

## App Entry Point

### App.tsx
**Purpose:** Main application container that manages screen routing and global state providers.

**Routes/Screens Managed:**
- Onboarding Flow (conditional)
- Dashboard (default after onboarding)
- System Log
- Diagnostic
- Profile
- Missions
- Store
- Inventory
- System Gate

**Global Providers:**
- `SystemProvider` - Player state context
- `RedGateProvider` - Red Gate & Job Change state

---

## Onboarding Screens

### 1. WelcomeScreen
**Purpose:** Initial welcome screen showing when user first opens the app.

**UI Elements:**
- System Alert Window (amber border)
- Title: "SYSTEM ALERT"
- Subtitle: "MSG_ID: 001_AWAKENING"
- Large animated "!" icon
- Headline: "You Have Been Chosen"
- Description text about the System detecting potential
- Quote: "Those who lack the courage to start are destined to remain in the shadows."
- ACCEPT button (glowing, pulsating)
- DECLINE button (disabled, locked state)

**Actions:**
- Click "ACCEPT" → proceeds to SystemAnalysisScreen

---

### 2. SystemAnalysisScreen
**Purpose:** Questionnaire to assess player's strengths, weaknesses, and goals.

**UI Elements:**
- System Analysis Window (red border)
- Progress indicator: "PROTOCOL: EVALUATION_X/3"
- 3 Questions displayed one at a time:

**Question 1:** "WHAT 'MONSTER' STANDS BEFORE YOU?"
- Options: Burnout, Procrastination, Physical Weakness, Self-Doubt

**Question 2:** "HOW WILL YOU SURVIVE?"
- Options: Discipline, Strategy, Strength, Endurance

**Question 3:** "WHAT LIES BEYOND THE STRUGGLE?"
- Options: Control, Freedom, Power, Peace

- Each option is a selectable card with hover effects
- Confirm Selection button (red variant)

**Actions:**
- Select option → highlights selected card
- Confirm → advances to next question or completes

---

### 3. LoadingScreen
**Purpose:** Transition screen showing system "processing" animation.

**UI Elements:**
- Full-screen dark background (#020617)
- Animated magic circle loader (spinning rings)
- Terminal-style log output showing:
  - "INITIALIZING SYSTEM..."
  - "ANALYZING PLAYER DATA..."
  - "CALIBRATING ATTRIBUTE BASELINES..."
  - "GENERATING PERSONALIZED QUESTS..."
  - "SYNCHRONIZING WITH SERVER..."
  - "AWAKENING COMPLETE."
- Flash effect on completion

**Behavior:**
- Logs appear one by one with random delays
- Auto-advances after all logs complete

---

### 4. AwakeningScreen
**Purpose:** Shows player's初始属性 and confirms system acceptance.

**UI Elements:**
- Two-column layout (hidden on mobile)
- Left: Avatar silhouette placeholder
  - Player silhouette with blur effect
  - "PLAYER" label
  - "CLASS: NONE"
  - "LVL. 1" badge

- Right: Status Recovery Window (blue border)
  - Title: "STATUS RECOVERY"
  - Subtitle: "PLAYER_PROFILING_COMPLETE"
  - Welcome message: "Welcome, Player."
  - Stats display (all starting at 10):
    - DISCIPLINE
    - STRENGTH
    - INTELLECT
    - VITALITY
    - SENSE
  - Each stat has progress bar visualization
  - "CONFIRM STATUS" button (glowing, pulsating)

**Actions:**
- Click "CONFIRM STATUS" → proceeds to TrialQuestScreen

---

### 5. TrialQuestScreen
**Purpose:** Tutorial quest "Courage of the Weak" that player must complete.

**UI Elements:**
- Quest Info Window
  - Title: "QUEST INFO"
  - Subtitle: "DIFFICULTY: E-RANK"
  - Quest Name: "Courage of the Weak" (gold color)
  - Description text
  - Goal section with checklist:
    - Complete 3 focused work sessions (30m each)
  - Rewards panel (green):
    - + Status Recovery
    - + Player Rights
  - Failure panel (red):
    - PENALTY ZONE
    - (4 hours survival)
  - Countdown timer (24:00:00 format)
  - "CONFIRM COMPLETION" button

**Data Source:**
- Calls `/api/onboarding/{playerId}/trial/complete` on confirmation

**Actions:**
- Click "CONFIRM COMPLETION" → completes onboarding, enters main app

---

### 6. OnboardingLayout
**Purpose:** Wrapper component providing consistent styling for onboarding screens.

---

## Main Dashboard

### DashboardView
**Purpose:** Primary game interface after onboarding completion.

**Layout Sections:**
1. **Red Gate Warning Banner** (conditional)
   - Only shows when Red Gate is active
   - Red pulsing border
   - "WARNING: DUNGEON BREAK IN PROGRESS" text

2. **Top Bar**
   - System Console title
   - User name display
   - System status badge (online/offline/maintenance)
   - Settings & Notifications buttons

3. **Navigation Buttons**
   - Store button
   - Status (Inventory) button
   - System Gates button

4. **Three-Column Grid:**
   - Left Column (1/4 width): PlayerProfileCard, AssetsPanel
   - Center Column (2/4 width): CurrentStatusPanel, DailyQuestsPanel
   - Right Column (1/4 width): SystemLogPanel, CapacityPanel

5. **Bottom Status Bar**
   - Server connection status
   - Latency display
   - Player ID (truncated)
   - Dev test buttons (PENALTY, PROMOTION, RED GATE)

**Contains:**
- TopBar
- PlayerProfileCard
- CurrentStatusPanel
- DailyQuestsPanel
- AssetsPanel
- SystemLogPanel
- CapacityPanel

---

## Dashboard Components

### TopBar
**Purpose:** Header bar with system title and user info.

**UI Elements:**
- Left section:
  - System Console icon (cyan square with grid)
  - "SYSTEM CONSOLE" title
  - User label with player name
- Right section:
  - System status badge (colored: green/yellow/red)
  - Settings button (gear icon)
  - Notifications button (bell icon with pulse dot)

**Props:**
- `userName: string`
- `systemStatus: 'online' | 'offline' | 'maintenance'`
- `diagnosticMode?: boolean`
- `onSettingsClick?: () => void`
- `onNotificationsClick?: () => void`

---

### PlayerProfileCard
**Purpose:** Display player avatar, rank, and title.

**UI Elements:**
- Circular avatar image (placeholder: pravatar.cc)
- Rank badge overlay (shield icon)
- Rank display (e.g., "E-RANK")
- Title badge (e.g., "Shadow Monarch")
- Fatigue meter (progress bar)

**Data Source:** `statusWindow.identity`

---

### CurrentStatusPanel
**Purpose:** Show player's level, XP progress, and core stats.

**UI Elements:**
- Section title: "Current Status"
- Level display: "LVL. X" (large, bold)
- Job class badge
- XP Progress bar with:
  - Current XP / Max XP values
  - Percentage display
  - "EXP PROGRESS" label
  - "To Next Level" indicator
- Radar chart (4-axis: STR, VIT, INT, SEN)
- Stats grid (3 columns):
  - Strength
  - Agility  
  - Intellect

**Data Source:** `statusWindow.progression`, `statusWindow.attributes`

---

### DailyQuestsPanel
**Purpose:** Display and manage daily quests.

**UI Elements:**
- Tab navigation: "Daily Quests" | "Penalty Zone"
- Refresh countdown timer
- Quest list (scrollable):
  - Each quest card contains:
    - Checkbox (SystemCheckbox)
    - Quest title
    - Goal description with progress (current/target)
    - Reward display
    - Status badge OR Claim Reward button

**Tabs:**
1. **Daily Quests Tab:** Shows active daily quests
2. **Penalty Zone Tab:** Shows penalty quests if active

**Props:**
- `quests: Quest[]`
- `onQuestToggle?: (questId, completed) => void`
- `onClaimReward?: (questId) => void`

---

### AssetsPanel
**Purpose:** Display economy/financial information.

**UI Elements:**
- Title: "💰 ASSETS"
- Total Gold display (large)
- Weekly trend indicator (up/down arrow with percentage)
- Bar chart placeholder (7 bars)
- Stats grid:
  - Daily Income
  - Shop Stats
- "Access System Shop" button

**Data Source:** `statusWindow.economy`

---

### SystemLogPanel
**Purpose:** Show recent system events.

**UI Elements:**
- Title: "📜 SYSTEM LOG"
- Scrollable log list (max 64px height)
- Each log entry:
  - Timestamp (gray, monospace)
  - Type badge (info/success/warning/error)
  - Message text
- "VIEW FULL LOG" link (footer)

**Props:**
- `entries: LogEntry[]`
- `onViewFullLog?: () => void`

---

### CapacityPanel
**Purpose:** Show resource usage and capacity.

**UI Elements:**
- Title: "📊 CAPACITY"
- Active Quests section:
  - Label + count (e.g., "1 / 2")
  - Horizontal slot indicators (filled/empty squares)
- Inventory Load progress bar
- Equipped Skills grid:
  - 4 slots (square icons)
  - Empty slots show "+"

**Props:**
- `activeQuests: number`
- `maxQuests: number`
- `inventoryLoad: number`
- `equippedSkills: string[]`

---

## Feature Screens

### StoreScreen
**Purpose:** In-game shop for purchasing items.

**UI Elements:**
- Header:
  - Title: "SYSTEM STORE"
  - Subtitle: "Buy equipment and consumables."
  - Gold balance display (large, yellow)
- Item grid (responsive: 1-4 columns)
- Each ShopItemCard:
  - Item name
  - Description
  - Cost (gold)
  - Stock limit (if applicable)
  - Rank requirement badge (if locked)
  - Purchase button

**Data Source:** `/api/shop/items?playerId={id}`

**Actions:**
- Click purchase → calls `/api/shop/purchase/{itemCode}?playerId={id}`
- Checks: sufficient gold, rank requirement, stock available

---

### InventoryScreen
**Purpose:** View owned items and manage stats.

**UI Elements:**
- Two-column layout:
  - Left (1/3): Player status
    - User icon + name
    - Title: "Shadow Monarch"
    - StatAllocationPanel
  - Right (2/3): Inventory
    - Title: "INVENTORY" with item count
    - InventoryGrid showing owned items
    - Each item shows name, description, quantity
    - "USE" button on each item

**Components:**
- StatAllocationPanel
- InventoryGrid

**Data Source:** 
- Stats: `statusWindow.attributes`
- Inventory: `/api/shop/inventory?playerId={id}`

---

### SystemGateView
**Purpose:** Display and manage projects/dungeons.

**UI Elements:**
- Header:
  - Title: "SYSTEM GATE"
  - Subtitle: "DIMENSIONAL CONNECTIVITY: STABLE | CRITICAL FAILURE"
  - Return button
  - Scan Gates button (with radar icon)
- Gate Transition Overlay (when entering):
  - Full-screen dark overlay
  - Spinning portal animation
  - "Entering Dungeon" text
  - "INITIALIZING DIMENSIONAL TRANSFER..." message
- Gate Grid:
  - GateCard components showing each project
  - Each card shows:
    - Gate type (blue/red)
    - Rank requirement
    - Title
    - Boss name
    - Floor count
    - Time remaining (if Red Gate)
- Empty state: "NO GATES DETECTED" message
- Footer warning (when Red Gate active)

**Data Source:** `/api/projects?playerId={id}`

---

### ActiveMissionsView
**Purpose:** Detailed view of active missions/projects.

**UI Elements:**
- Header (sticky):
  - System OS logo + version
  - System status (ONLINE)
  - Settings & Notifications buttons
  - Back button
- Title: "SYSTEM INTERFACE : ACTIVE_MISSIONS"
- Top Statistics Row:
  - Capacity Card:
    - Slot Utilization: "X/Y Slots Engaged"
    - System Capacity Load percentage
    - Progress bar
    - Warning text (when full)
  - Daily Reset Card:
    - Countdown to daily reset (04:00 UTC)
- Main Mission Grid:
  - Active Mission Card (left):
    - Status badge: "Status: Active"
    - Rank reward badge
    - Image area (placeholder)
    - Mission title
    - Description
    - Completion progress bar
    - "CHECK IN" button
  - Locked Slot Card (right):
    - Lock icon
    - "System Lock" title
    - "SLOT LIMIT REACHED" message
    - Disabled "INITIATE_NEW_MISSION" button

---

### HunterProfileView
**Purpose:** Detailed player profile and statistics.

**UI Elements:**
- Header with navigation tabs
- Main content with:
  - Player name and title
  - Level progression display
  - Statistics charts
  - Achievement badges
  - Activity history
- This is a large comprehensive profile page

---

### SystemLogView
**Purpose:** Full system event log viewer.

**UI Elements:**
- Header:
  - System OS title
  - Back button
- Log table with columns:
  - Timestamp
  - Event Type badge
  - Message
- Log types with color coding:
  - SYS_BOOT (gray)
  - INFO (cyan)
  - RESTORE (green)
  - SUCCESS (cyan, bold)
  - WARN (yellow)
  - CRITICAL (red, bold)
  - ERROR (red)
- Blinking cursor effect at bottom

---

### DiagnosticView
**Purpose:** Advanced analytics and continuity monitoring.

**UI Elements:**
- Header with system info
- Main content:
  - Continuity Score display
  - Target Score
  - Event log table:
    - Event ID
    - Date
    - Event Type
    - Reason/Impact
    - Details
- Various diagnostic metrics and charts

---

## Popup/Modal Components

### PenaltyPopup
**Purpose:** Displayed when player enters penalty zone.

**UI Elements:**
- Full-screen red overlay
- Warning title: "⚠️ PENALTY ZONE ACTIVATED"
- Description of penalty consequences
- Penalty quest details
- "UNDERSTAND" button to dismiss

---

### PromotionPopup
**Purpose:** Shown when player is eligible for rank promotion.

**UI Elements:**
- Promotion eligibility notification
- Current rank → New rank display
- Requirements checklist
- "ACCEPT PROMOTION" / "DECLINE" buttons

---

### RedGatePopup
**Purpose:** Full-screen overlay during Red Gate survival event.

**UI Elements:**
- Glitchy animation overlay
- "⚠️ RED GATE ACTIVATED" title
- Countdown timer
- Quest details
- "SURVIVE" button

---

### SystemInterruptionPopup
**Purpose:** Generic system interruption notification.

**UI Elements:**
- Interruption title
- Description
- "UNDERSTAND" button

---

### JobChangePopup
**Purpose:** Job/class change notification and quest.

**UI Elements:**
- Job change notification
- Quest details
- Accept/Delay buttons

---

## Reusable UI Components

### SystemPanel
**Purpose:** Base container component with consistent styling.

**Props:**
- `title?: string`
- `glowColor?: 'cyan' | 'amber' | 'red'`
- `className?: string`
- `children: ReactNode`

---

### SystemButton
**Purpose:** Styled button component.

**Variants:**
- `primary` - Cyan border/background
- `ghost` - Transparent with border
- Custom icons support

**Sizes:** sm, md, lg

---

### SystemBadge
**Purpose:** Status indicator badges.

**Variants:**
- `success` - Green
- `warning` - Yellow/amber
- `error` - Red
- `info` - Cyan/blue

---

### SystemCheckbox
**Purpose:** Custom styled checkbox for quests.

---

### SystemProgressBar
**Purpose:** XP/Progress bars.

**Props:**
- `current: number`
- `max: number`
- `label?: string`
- `showValues?: boolean`
- `showPercentage?: boolean`
- `height?: 'sm' | 'md' | 'lg'`
- `color?: string`

---

### SystemRadar
**Purpose:** Radar/spider chart for stats.

**Props:**
- `data: { stat: string, value: number, max: number }[]`
- `size?: number`

---

### SystemTab
**Purpose:** Tab navigation component.

**Props:**
- `tabs: Tab[]`
- `activeTab: string`
- `onTabChange: (tabId: string) => void`

---

### SystemMetric
**Purpose:** Display numeric metrics with label.

---

### SystemWindow (Onboarding)
**Purpose:** Styled window container for onboarding screens.

**Props:**
- `title: string`
- `subtitle?: string`
- `borderColor?: string`
- `className?: string`

---

### GlowButton
**Purpose:** Glowing animated button for onboarding.

**Variants:**
- Default (cyan)
- Ghost (transparent)
- Danger (red)

**States:**
- Pulsating animation
- Disabled
- Loading

---

### StatBar
**Purpose:** Horizontal progress bar for stats.

---

### ScreenFrame
**Purpose:** Base screen container with consistent padding/background.

---

### StatAllocationPanel
**Purpose:** Allow players to allocate stat points.

**Props:**
- `level: number`
- `currentXp: number`
- `maxXp: number`
- `freePoints: number`
- `stats: Stat[]`
- `onAllocate: (statKey, amount) => void`

---

### ShopItemCard
**Purpose:** Individual item display in store.

**Props:**
- `name: string`
- `description: string`
- `cost: number`
- `rankRequirement?: string`
- `stockLimit?: number`
- `isLocked?: boolean`
- `onPurchase: () => void`

---

### InventoryGrid
**Purpose:** Grid display of inventory items.

**Props:**
- `items: InventoryItem[]`
- `onUseItem: (itemCode) => void`

---

### GateCard
**Purpose:** Project/dungeon display card.

**Props:**
- `id: string`
- `type: 'blue' | 'red'`
- `rank: string`
- `title: string`
- `bossName: string`
- `floorCount: number`
- `timeLeft?: string`
- `onClick: (id) => void`

---

## Theme & Design Tokens

### Color Palette
- **Primary:** Cyan (#0ea5e9)
- **Background:** Dark (#020617, #050911, #0a0f1a)
- **Surface:** Gray (#0c1220, #1a1a2e)
- **Border:** Various opacity grays
- **Accent:**
  - Amber (onboarding welcome)
  - Red (danger, penalties)
  - Green (success, rewards)
  - Gold (achievements)

### Typography
- **Headings:** Bold, tracking-widest, uppercase
- **Body:** Regular weight
- **Mono:** Consolas, Monaco for logs/data

### Animations
- Pulse glow effects
- Spinning loaders
- Slide-in transitions
- Fade effects

---

## Navigation Flow

```
App Entry
    │
    ├── Onboarding Required?
    │   ├── WelcomeScreen
    │   ├── SystemAnalysisScreen
    │   ├── LoadingScreen
    │   ├── AwakeningScreen
    │   └── TrialQuestScreen
    │
    └── Dashboard (Main)
        ├── TopBar
        ├── Navigation Buttons
        │   ├── StoreScreen
        │   ├── InventoryScreen
        │   └── SystemGateView
        │       └── ActiveMissionsView
        ├── Three-Column Content
        │   ├── PlayerProfileCard
        │   ├── CurrentStatusPanel
        │   ├── DailyQuestsPanel
        │   ├── AssetsPanel
        │   ├── SystemLogPanel
        │   └── CapacityPanel
        │
        └── Popups (Overlay)
            ├── PenaltyPopup
            ├── PromotionPopup
            ├── RedGatePopup
            └── JobChangePopup
```

---

## Data Dependencies

| Screen/Component | Data Source |
|----------------|-------------|
| DashboardView | `/api/player/status-window/{playerId}` |
| DailyQuestsPanel | `/api/quests/active?playerId={id}` |
| StoreScreen | `/api/shop/items?playerId={id}` |
| InventoryScreen | `/api/shop/inventory?playerId={id}` |
| SystemGateView | `/api/projects?playerId={id}` |
| SystemVoice | `/api/system/alerts/{playerId}` |

---

*Document Version: 1.0*
*Last Updated: March 2026*
