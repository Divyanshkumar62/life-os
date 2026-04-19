#!/bin/bash

# Configuration
API_URL="http://localhost:8080/api"
# Using a fixed UUID for simulation consistency, or extract from a previous run if needed.
# For now, let's use the ID from the "Awakening" simulation if available, or a hardcoded one.
PLAYER_ID="dbec1801-4927-4c07-937c-f5b2488344e1" 

echo "=================================================="
echo "   SOLO LEVELING: RANK PROMOTION SIMULATION"
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
echo "[1] Checking Player Status (Base State)"
get_json "$API_URL/dashboard/$PLAYER_ID"

# 2. Simulate Reaching Level Cap (Cheat)
# We need to add this endpoint to AdminController first.
echo ""
echo "[2] [CHEAT] Setting Level to 10 (E-Rank Cap)"
post_json "$API_URL/admin/players/$PLAYER_ID/level?level=10" ""

# 3. Add Boss Keys (Cheat)
echo ""
echo "[3] [CHEAT] Granting 5 Boss Keys"
post_json "$API_URL/admin/players/$PLAYER_ID/keys?count=5" ""

# 4. Request Promotion
echo ""
echo "[4] Requesting Promotion Exam (AI Generation)"
# This triggers the AI call, so it might take a few seconds.
RESPONSE=$(post_json "$API_URL/progression/promotion/request/$PLAYER_ID" "")
echo "Response: $RESPONSE"

# Extract Quest ID from response (assuming it returns RankExamAttempt with details or we check dashboard)
# Let's check dashboard to find the active PROMOTION_EXAM quest.
echo ""
echo "[5] Verifying Assigned Exam on Dashboard"
DASHBOARD=$(get_json "$API_URL/dashboard/$PLAYER_ID")
echo "$DASHBOARD"

# Extract Quest ID using grep/sed for simplicity (or jq if available, but staying safe)
# Looking for "questType":"PROMOTION_EXAM" ... "id":"..."
QUEST_ID=$(echo "$DASHBOARD" | grep -o '"id":"[^"]*"' | head -n 1 | cut -d'"' -f4)

if [ -z "$QUEST_ID" ]; then
  echo "Error: No Promotion Exam found on dashboard."
  exit 1
fi

echo "Found Exam Quest ID: $QUEST_ID"

# 5. Simulate Success (Pass the Exam)
echo ""
echo "[6] Simulating Exam Success (Complete Quest)"
post_json "$API_URL/quests/$QUEST_ID/complete" ""

# 6. Verify Rank Up
echo ""
echo "[7] Verifying Rank Up (E -> D)"
FINAL_DASHBOARD=$(get_json "$API_URL/dashboard/$PLAYER_ID")
echo "$FINAL_DASHBOARD"

echo ""
echo "=================================================="
echo "SIMULATION COMPLETE"
