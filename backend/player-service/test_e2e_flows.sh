#!/bin/bash

# ==============================================================================
# Solo Leveling Life-OS: End-to-End API Integration Testing Script
# ==============================================================================

BASE_URL="http://localhost:8080/api"
CURL_CMD="curl.exe"
PLAYER_ID=""
TRIAL_QUEST_ID=""

# Generate a unique username for this execution run to ensure repeatability
RANDOM_SUFFIX=$((1 + $RANDOM % 100000))
USERNAME="JinWoo_${RANDOM_SUFFIX}"

echo "=============================================================================="
echo "STARTING E2E USER JOURNEYS INTEGRATION TESTS"
echo "Username: ${USERNAME}"
echo "=============================================================================="

# ------------------------------------------------------------------------------
# FLOW A: Onboarding, Daily Completion, and Shop Purchase
# ------------------------------------------------------------------------------
echo ""
echo "------------------------------------------------------------------------------"
echo "FLOW A: Onboarding, Daily Completion, and Shop Purchase"
echo "------------------------------------------------------------------------------"

echo "Step 1: Initialize Player (Awakening). Expected: 200 OK"
INIT_RESP=$($CURL_CMD -s -X POST "${BASE_URL}/onboarding/start?username=${USERNAME}" \
  -H "Content-Type: application/json")
echo "Response: ${INIT_RESP}"

# Extract Player ID
PLAYER_ID=$(echo "${INIT_RESP}" | grep -o '"playerId":"[^"]*' | grep -o '[^"]*$')
if [ -z "${PLAYER_ID}" ]; then
  PLAYER_ID="d3b07384-d113-49cd-a5d6-89e02315fa4d"
fi
echo "Extracted Player ID: ${PLAYER_ID}"

echo ""
echo "Step 2: Submit Awakening Questionnaire. Expected: 200 OK"
$CURL_CMD -s -X POST "${BASE_URL}/onboarding/${PLAYER_ID}/awakening" \
  -H "Content-Type: application/json" \
  -d '{
    "weakness": "Lacks physical endurance",
    "focusArea": "Physical conditioning",
    "mainGoal": "Awaken as an S-Rank hunter",
    "timeCommitment": "FOUR_HOURS"
  }'
echo ""

echo ""
echo "Step 3: Complete Awakening Trial. Expected: 200 OK"
$CURL_CMD -s -X POST "${BASE_URL}/onboarding/${PLAYER_ID}/trial/complete" \
  -H "Content-Type: application/json"
echo ""

echo ""
echo "Step 4: Check Status Window (Level should be 10, Gold balance 10,000). Expected: 200 OK"
$CURL_CMD -s -X GET "${BASE_URL}/player/status-window/${PLAYER_ID}"
echo ""

echo ""
echo "Step 5: Purchase Insurance Scroll (2500 Gold). Expected: 200 OK"
$CURL_CMD -s -X POST "${BASE_URL}/shop/purchase/INSURANCE_SCROLL?playerId=${PLAYER_ID}" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 1}'
echo ""


# ------------------------------------------------------------------------------
# FLOW B: Daily Reset Failure, Penalty Lockout, Confession, and Escape
# ------------------------------------------------------------------------------
echo ""
echo "------------------------------------------------------------------------------"
echo "FLOW B: Daily Reset Failure, Penalty Lockout, Confession, and Escape"
echo "------------------------------------------------------------------------------"

echo "Forcing Player into Penalty Zone via Admin API..."
$CURL_CMD -s -X POST "${BASE_URL}/admin/players/${PLAYER_ID}/penalty/enter?reason=Fail"
echo ""

echo "Step 1: Attempt Shop Purchase during active Penalty. Expected: 403 Forbidden / 400 Bad Request"
$CURL_CMD -s -X POST "${BASE_URL}/shop/purchase/INSURANCE_SCROLL?playerId=${PLAYER_ID}" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 1}'
echo ""

echo ""
echo "Step 2: Submit Written Confession (Step 1 of escape). Expected: 200 OK"
$CURL_CMD -s -X POST "${BASE_URL}/penalty/confess?playerId=${PLAYER_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "I failed my vessel maintenance today because I prioritized trivial resting over system leveling. I will fulfill the next command without hesitation."
  }'
echo ""

# Note: Submitting confession clears the penalty flag from PlayerIdentity status flags, but we must
# also complete the core survival task to escape the core penalty state.

echo ""
echo "Step 3: Submit Survival Task completion (Step 2 of escape). Expected: 200 OK"
$CURL_CMD -s -X POST "${BASE_URL}/penalty/submit-task" \
  -H "Content-Type: application/json" \
  -d "{
    \"playerId\": \"${PLAYER_ID}\",
    \"survivalTaskId\": \"00000000-0000-0000-0000-000000000000\",
    \"submissionText\": \"Task completed successfully\"
  }"
echo ""

echo ""
echo "Step 4: Shop access restored post-escape. Expected: 200 OK"
$CURL_CMD -s -X POST "${BASE_URL}/shop/purchase/INSURANCE_SCROLL?playerId=${PLAYER_ID}" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 1}'
echo ""


# ------------------------------------------------------------------------------
# FLOW C: The Economy Exploit Checks (Rank lockouts, Zero gold rerolls)
# ------------------------------------------------------------------------------
echo ""
echo "------------------------------------------------------------------------------"
echo "FLOW C: Rank Locks and Zero Gold Rerolls"
echo "------------------------------------------------------------------------------"

echo "Step 1: Purchase MONARCH_EXEMPTION (20000 gold, Rank E) at current balance (~3500). Expected: fails with insufficient funds"
$CURL_CMD -s -X POST "${BASE_URL}/shop/purchase/MONARCH_EXEMPTION?playerId=${PLAYER_ID}" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 1}'
echo ""

echo "Forcing Player into Penalty Zone for reroll draining..."
$CURL_CMD -s -X POST "${BASE_URL}/admin/players/${PLAYER_ID}/penalty/enter?reason=Fail"
echo ""

echo "Draining Gold to 0 via repeated rerolls (each deducts 10%, min 100)..."
REROLL_COUNT=0
MAX_REROLLS=50
while [ $REROLL_COUNT -lt $MAX_REROLLS ]; do
  REROLL_RESP=$($CURL_CMD -s -X POST "${BASE_URL}/penalty/reroll" \
    -H "Content-Type: application/json" \
    -d "{
      \"playerId\": \"${PLAYER_ID}\",
      \"reason\": \"Physical injury prevents outdoor activity\"
    }")
  REROLL_COUNT=$((REROLL_COUNT + 1))
  GOLD=$(echo "$REROLL_RESP" | grep -o '"remainingGold":[0-9]*' | grep -o '[0-9]*$')
  if [ -z "$GOLD" ]; then
    echo "  Reroll #${REROLL_COUNT}: ${REROLL_RESP}"
    break
  fi
  if [ "$GOLD" = "0" ] 2>/dev/null; then
    echo "  Reroll #${REROLL_COUNT}: gold=0 (drained)"
    break
  fi
  if [ $((REROLL_COUNT % 5)) -eq 0 ]; then
    echo "  Reroll #${REROLL_COUNT}: gold=${GOLD}"
  fi
done
echo "Total rerolls executed: ${REROLL_COUNT}"

echo ""
echo "Step 2: Verify zero gold balance and debt applied. Expected: 200 OK"
$CURL_CMD -s -X GET "${BASE_URL}/player/status-window/${PLAYER_ID}"
echo ""

echo ""
echo "E2E testing execution script completed."
