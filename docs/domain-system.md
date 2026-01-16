Domain Events Layer v1 —
Why This Layer Exists (Non-Negotiable)

Right now:

Daily Quest Engine

Penalty Engine

Progression Engine

Economy Engine

System Voice Engine

…all work, but they are implicitly coupled.

The Domain Events Layer turns:

“Service A calls Service B”
into
“State change happened → system reacts”

This is what allows:

Penalty suppression

Voice control

Analytics

V2 async / queueing

Clean rollback rules

Without this layer, v2 will collapse under complexity.

Core Principles (V1 Rules)

Events are facts, not commands

Events are immutable

Services do NOT call each other for side effects

System Voice reacts to events

Penalty suppression happens at event emission

Event Taxonomy (V1 Canonical Set)

These are the ONLY events allowed in v1.

🔁 Daily Loop Events
DAILY_QUEST_COMPLETED
DAILY_QUEST_FAILED
DAILY_RESET_PROCESSED

⚔️ Penalty Events
PENALTY_ZONE_ENTERED
PENALTY_WORK_RECORDED
PENALTY_QUEST_COMPLETED
PENALTY_ZONE_EXITED

🧬 Progression Events
RANK_GATE_REACHED
PROMOTION_UNLOCKED
PROMOTION_ATTEMPT_STARTED
PROMOTION_PASSED
PROMOTION_FAILED

🏗️ Project Events
PROJECT_CREATED
PROJECT_COMPLETED
PROJECT_FAILED

💰 Economy / Reward Events
GOLD_EARNED
GOLD_SPENT
STREAK_BONUS_APPLIED

Domain Event Contract (Base Class)
public abstract class DomainEvent {
    UUID eventId;
    UUID playerId;
    LocalDateTime occurredAt;
    boolean critical;
}


Rules:

eventId is REQUIRED (idempotency)

critical is REQUIRED (Penalty override logic)

No setters

No business logic

Event Emission Rules (CRITICAL)
❌ Forbidden
penaltyService.enterPenaltyZone();
voiceService.sendMessage(...);
economyService.addGold(...);

✅ Allowed
eventPublisher.publish(new PenaltyZoneEnteredEvent(...));

Event Publisher (V1 In-Process)
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}

V1 Implementation
@Component
public class InProcessEventPublisher implements DomainEventPublisher {

    private final List<DomainEventHandler<?>> handlers;

    public void publish(DomainEvent event) {
        handlers.forEach(h -> h.handleIfSupported(event));
    }
}


✔️ Synchronous
✔️ Transaction-bound
✔️ Deterministic

Event Handler Contract
public interface DomainEventHandler<T extends DomainEvent> {
    boolean supports(DomainEvent event);
    void handle(T event);
}


Each engine registers its own handlers.

Penalty Suppression (Proper Location)
🔒 THIS IS THE KEY FIX YOU ASKED FOR EARLIER

Suppression happens HERE, not in VoiceService.

public void publish(DomainEvent event) {
    if (playerInPenalty(event.playerId) && !event.critical) {
        return; // suppressed
    }
    dispatch(event);
}


✔️ Centralized
✔️ Predictable
✔️ Zero cross-service querying

Example: Daily Failure → Penalty → Voice
Step 1: Daily Engine Emits
publish(new DailyQuestFailedEvent(playerId, critical=true));

Step 2: Penalty Handler Reacts
@EventHandler
onDailyFailed → penaltyService.enterPenaltyZone();

Step 3: Penalty Emits
publish(new PenaltyZoneEnteredEvent(playerId, critical=true));

Step 4: Voice Handler Reacts
SystemVoiceService.send("PENALTY_ZONE_ENTRY");


👉 No service directly calls another. Ever.

Handler Ownership Map
Event	Handler
DAILY_QUEST_FAILED	PenaltyEngine
PENALTY_ZONE_ENTERED	VoiceEngine, StreakEngine
QUEST_COMPLETED	RewardEngine
GOLD_EARNED	Analytics (v2)
PROJECT_COMPLETED	ProgressionEngine
PROMOTION_PASSED	PlayerState + Voice
STREAK_BROKEN	Voice + Economy
Idempotency Rule (MANDATORY)

Each handler must check eventId:

if (eventLog.exists(event.getEventId())) return;


V1 shortcut:

In-memory Set OR

DB unique constraint on (eventId, handler)

What This Unlocks Immediately

With this layer in place, you get for free:

Penalty override correctness

No duplicate side effects

Clean mental model

V2 async migration

Replay capability later

Testing becomes trivial