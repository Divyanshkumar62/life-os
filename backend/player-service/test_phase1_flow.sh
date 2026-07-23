#!/bin/bash

# ==============================================================================
# Solo Leveling Life-OS: Phase 1 Onboarding & Awakening Integration Test Script
# ==============================================================================

BASE_URL="http://localhost:8080/api"
CURL_CMD="curl.exe"

# Generate a unique username for this execution run to ensure repeatability
RANDOM_SUFFIX=$((1 + $RANDOM % 100000))
USERNAME="JinWoo_Phase1_${RANDOM_SUFFIX}"

echo "=============================================================================="
echo "PHASE 1: AWAKENING & ONBOARDING (THE HAPPY PATH)"
echo "Username: ${USERNAME}"
echo "=============================================================================="

# ------------------------------------------------------------------------------
# STEP 1: Initialize Player (Awakening Start)
# ------------------------------------------------------------------------------
echo ""
echo "[STEP 1] Initialize Player (Awakening Start). Expected: 200 OK, Stage: QUESTIONNAIRE"
INIT_RESP=$($CURL_CMD -s -X POST "${BASE_URL}/onboarding/start?username=${USERNAME}" \
  -H "Content-Type: application/json")
echo "Response: ${INIT_RESP}"

# Extract Player ID
PLAYER_ID=$(echo "${INIT_RESP}" | jq -r '.playerId')
echo "Extracted Player ID: ${PLAYER_ID}"

# ------------------------------------------------------------------------------
# STEP 2: Submit Awakening Questionnaire
# ------------------------------------------------------------------------------
echo ""
echo "[STEP 2] Submit Awakening Questionnaire. Expected: 200 OK, Stage: TRIAL_QUEST"
AWAKE_RESP=$($CURL_CMD -s -X POST "${BASE_URL}/onboarding/${PLAYER_ID}/awakening" \
  -H "Content-Type: application/json" \
  -d '{
    "weakness": "Lacks physical endurance",
    "focusArea": "Physical conditioning",
    "mainGoal": "Awaken as an S-Rank hunter",
    "timeCommitment": "FOUR_HOURS",
    "biggestChallenge": "Procrastination and low energy",
    "sixMonthGoal": "Run a half-marathon and build a daily fitness habit",
    "pastFailures": "Quit gym after 2 weeks, gave up on dieting"
  }')
echo "Response: ${AWAKE_RESP}"

# ------------------------------------------------------------------------------
# STEP 3: Fetch Quests and Complete "Courage of the Weak"
# ------------------------------------------------------------------------------
echo ""
echo "[STEP 3] Fetch Quests & Complete Awakening Trial Quest (Courage of the Weak)"
QUESTS_RESP=$($CURL_CMD -s -X GET "${BASE_URL}/players/${PLAYER_ID}/quests")
echo "All Active Quests:"
echo "${QUESTS_RESP}" | jq -r '.quests[] | "- [\(.questId)] \(.title) (Type: \(.questType), Category: \(.category))"'

# Extract Trial Quest ID
TRIAL_QUEST_ID=$(echo "${QUESTS_RESP}" | jq -r '.quests[] | select(.title == "System Qualification: Courage of the Weak" or .category == "TRIAL") | .questId')

if [ -n "${TRIAL_QUEST_ID}" ] && [ "${TRIAL_QUEST_ID}" != "null" ]; then
  echo "Found Trial Quest ID: ${TRIAL_QUEST_ID}"
  echo "Completing Trial Quest..."
  COMP_RESP=$($CURL_CMD -s -X PATCH "${BASE_URL}/quests/${TRIAL_QUEST_ID}/status?action=COMPLETE")
  echo "Completion Response: ${COMP_RESP}"
else
  echo "Error: Trial Quest 'Courage of the Weak' not found!"
fi

# ------------------------------------------------------------------------------
# STEP 4: Fetch Player Status Window
# ------------------------------------------------------------------------------
echo ""
echo "[STEP 4] Fetch Player Status Window. Expected: 403 Forbidden (Onboarding incomplete)"
STATUS_RESP=$($CURL_CMD -s -X GET "${BASE_URL}/player/status-window/${PLAYER_ID}")
echo "Status Window Response: ${STATUS_RESP}"

# ------------------------------------------------------------------------------
# STEP 5: Shop Lockout Verification
# ------------------------------------------------------------------------------
echo ""
echo "[STEP 5] Attempt Shop Purchase (Verify Onboarding Lockout). Expected: 403 Forbidden"
SHOP_RESP=$($CURL_CMD -s -X POST "${BASE_URL}/shop/purchase/INSURANCE_SCROLL?playerId=${PLAYER_ID}" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 1}')
echo "Shop Purchase Response: ${SHOP_RESP}"

# ------------------------------------------------------------------------------
# STEP 6: Complete Remaining Active Quests
# ------------------------------------------------------------------------------
echo ""
echo "[STEP 6] Fetch and Complete Remaining Active Quests"
REMAINING_RESP=$($CURL_CMD -s -X GET "${BASE_URL}/players/${PLAYER_ID}/quests")
ACTIVE_IDS=$(echo "${REMAINING_RESP}" | jq -r '.quests[] | select(.state == "ACTIVE") | .questId')

for QID in ${ACTIVE_IDS}; do
  QTITLE=$(echo "${REMAINING_RESP}" | jq -r ".quests[] | select(.questId == \"${QID}\") | .title")
  QDESC=$(echo "${REMAINING_RESP}" | jq -r ".quests[] | select(.questId == \"${QID}\") | .description")
  echo ""
  echo "--> Completing Quest: ${QTITLE}"
  echo "    Description: ${QDESC}"
  COMP_RESP=$($CURL_CMD -s -X PATCH "${BASE_URL}/quests/${QID}/status?action=COMPLETE")
  echo "    Response: ${COMP_RESP}"
done

# ------------------------------------------------------------------------------
# STEP 7: Verify Onboarding Completed and System Unlocked
# ------------------------------------------------------------------------------
echo ""
echo "[STEP 7] Verify Onboarding Completed and System Unlocked"
FINAL_STATUS_RESP=$($CURL_CMD -s -X GET "${BASE_URL}/player/status-window/${PLAYER_ID}")
echo "Final Status Window (Expected Level 10, Gold 10,000):"
echo "${FINAL_STATUS_RESP}" | jq .

FINAL_STAGE_RESP=$($CURL_CMD -s -X GET "${BASE_URL}/onboarding/${PLAYER_ID}/status")
echo "Onboarding Status Stage (Expected COMPLETED):"
echo "${FINAL_STAGE_RESP}" | jq .

echo ""
echo "Phase 1 flow test completed."
