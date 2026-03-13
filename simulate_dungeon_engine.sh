#!/bin/bash

# Configuration
API_URL="http://localhost:8080/api"
PLAYER_ID="dbec1801-4927-4c07-937c-f5b2488344e1" # Using fixed ID for now

echo "=================================================="
echo "   SOLO LEVELING: DUNGEON ENGINE SIMULATION"
echo "=================================================="

# Helper function for JSON requests
post_json() {
  curl -s -X POST "$1" -H "Content-Type: application/json" -d "$2"
}

get_json() {
  curl -s -X GET "$1" -H "Content-Type: application/json"
}

# 1. Check Initial Status
echo ""
echo "[1] Checking Player Status"
get_json "$API_URL/dashboard/$PLAYER_ID"

# 2. Summon the Architect (Create Dungeon)
echo ""
echo "[2] Summoning the Architect: Creating 'Master Rust Programming' Dungeon..."
REQ='{"playerId":"'$PLAYER_ID'","goal":"Master the Rust programming language in 2 weeks","userRank":"E"}'
PROJECT_JSON=$(post_json "$API_URL/projects/create" "$REQ")
echo "Response: $PROJECT_JSON"

# Extract Project ID
PROJECT_ID=$(echo "$PROJECT_JSON" | grep -o '"projectId":"[^"]*"' | head -n 1 | cut -d'"' -f4)

if [ -z "$PROJECT_ID" ]; then
  echo "Error: Failed to create dungeon. Check logs."
  # exit 1 
  # Don't exit, might be existing project we can use?
  # Let's try to fetch active projects
fi

echo "Project ID: $PROJECT_ID"

# 3. List Active Projects
echo ""
echo "[3] Listing Active Projects"
PROJECTS=$(get_json "$API_URL/projects?playerId=$PLAYER_ID")
echo "$PROJECTS"

if [ -z "$PROJECT_ID" ]; then
    # Try to grab first active project
    PROJECT_ID=$(echo "$PROJECTS" | grep -o '"projectId":"[^"]*"' | head -n 1 | cut -d'"' -f4)
    echo "Using existing Project ID: $PROJECT_ID"
fi

if [ -z "$PROJECT_ID" ]; then
    echo "CRITICAL: No active project found."
    exit 1
fi

# 4. List Dungeon Floors (Sub-Quests)
echo ""
echo "[4] Listing Dungeon Floors (Sub-Quests)"
QUESTS_JSON=$(get_json "$API_URL/projects/$PROJECT_ID/quests")
echo "$QUESTS_JSON"

# Extract first PENDING quest
QUEST_ID=$(echo "$QUESTS_JSON" | grep -o '"questId":"[^"]*"' | head -n 1 | cut -d'"' -f4)
# Better grep for PENDING? assume first logic works for now. 
# Since newly created, all are PENDING.

echo "Target Floor Quest ID: $QUEST_ID"

# 5. Equip Sub-Quest
echo ""
echo "[5] Equipping Quest (Pulling to Daily)"
post_json "$API_URL/projects/$PROJECT_ID/equip/$QUEST_ID" ""

# 6. Verify Dashboard (Should show in Active Quests)
echo ""
echo "[6] Verifying Dashboard contains Equipped Quest"
DASHBOARD=$(get_json "$API_URL/dashboard/$PLAYER_ID")
# Check if QUEST_ID is present
if echo "$DASHBOARD" | grep -q "$QUEST_ID"; then
    echo "SUCCESS: Quest $QUEST_ID found in Dashboard!"
else
    echo "FAILURE: Quest $QUEST_ID NOT found in Dashboard."
fi

# 7. Complete the Quest (Progress Dungeon)
echo ""
echo "[7] Completing the Quest (Clearing the Floor)"
post_json "$API_URL/quests/$QUEST_ID/complete" ""

# 8. Verify Project Progress update
echo ""
echo "[8] Checking Project Status (Progress Update)"
PROJECT_DETAILS=$(get_json "$API_URL/projects/$PROJECT_ID")
echo "$PROJECT_DETAILS"
# Verify lastActivityAt is recent? Hard to parse time in bash.
# Verify stabilityStatus is STABLE.

echo ""
echo "=================================================="
echo "SIMULATION COMPLETE"
