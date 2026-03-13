# Player Service Testing Strategy

## 1. Overview

This document outlines the testing strategy for the `player-service` backend in the Life OS application. The player service manages player identity, progression, attributes, psychological state, metrics, and temporal state.

### Tech Stack
- **Framework**: Spring Boot 3.2.1
- **Language**: Java 17
- **Database**: H2 (test), PostgreSQL (prod)
- **ORM**: Spring Data JPA / Hibernate
- **Testing**: JUnit 5, Spring Boot Test

---

## 2. Test Categories

### 2.1 Unit Tests
- Test individual methods in isolation
- Use mocks for dependencies
- Fast execution, no database required

### 2.2 Integration Tests
- Test service layer with real database (H2)
- Use `@SpringBootTest` with `@Transactional` for rollback
- Test cross-service interactions

### 2.3 Controller Tests
- Test REST endpoints
- Validate request/response DTOs

---

## 3. Test Structure

```
src/test/java/com/lifeos/player/
├── service/
│   ├── PlayerStateServiceTest.java       (existing)
│   ├── PlayerStateServiceUnitTest.java   (new - mocks)
│   ├── PlayerInitializationTest.java     (new)
│   ├── PlayerXpSystemTest.java           (new)
│   ├── PlayerAttributeTest.java          (new)
│   ├── PlayerPsychStateTest.java         (new)
│   └── PlayerProgressionTest.java        (new)
├── controller/
│   └── PlayerControllerTest.java         (new)
└── state/
    └── PlayerReadServiceTest.java        (existing)
```

---

## 4. Test Coverage Requirements

### 4.1 PlayerStateService

| Method | Test Scenarios |
|--------|----------------|
| `initializePlayer` | - Successful initialization<br>- Duplicate username throws exception |
| `getPlayerState` | - Valid player returns state<br>- Invalid player throws exception<br>- State includes all sub-entities |
| `addXp` | - Normal XP addition<br>- Level up triggers correctly<br>- XP freeze at rank cap<br>- Negative XP throws exception |
| `updateAttribute` | - Attribute value updates correctly<br>- Minimum value clamped to 0 |
| `updatePsychMetric` | - MOMENTUM, STRESS, COMPLACENCY, CONFIDENCE updates<br>- Values clamped 0-100<br>- Invalid metric throws exception |
| `incrementStat` | - Stat increments correctly<br>- Negative result clamped to 0 |
| `applyXpDeduction` | - XP deducted correctly<br>- Never below 0 |
| `resetStreak` | - Streak resets to 0 |
| `applyStatusFlag` | - New flag created<br>- Existing flag extended |
| `removeStatusFlag` | - Flag removed successfully |
| `hasActiveFlag` | - Returns true for active flag<br>- Returns false for expired/non-existent |
| `updateConsecutiveFailures` | - Failures count updates |
| `extendStreak` | - Streak increments<br>- LastQuestCompletedAt updated |
| `adjustMomentum` | - Momentum adjusts within bounds |
| `promoteRank` | - Rank promoted correctly<br>- XP unfrozen on promotion |
| `setLevel` | - Level set directly |
| `addFreeStatPoints` | - Points added to pool |
| `allocateFreeStatPoint` | - Valid allocation<br>- Insufficient points throws exception<br>- Amount <= 0 throws exception |

### 4.2 Edge Cases

- **XP Overflow**: Test behavior when XP exceeds level cap before promotion
- **Rank Cap**: Test XP freeze when at max rank level
- **Psych Metrics**: Test boundary values (0, 100) for all psych metrics
- **Concurrent Modifications**: Consider testing transactional behavior

---

## 5. Test Data Setup

### 5.1 Test Fixtures

```java
// Common test player builder
PlayerIdentity createTestPlayer(String username) {
    return PlayerIdentity.builder()
        .username(username)
        .systemVersion("v1")
        .build();
}

// Player with full state for integration tests
PlayerStateResponse createFullTestPlayer(PlayerStateService service, String username) {
    return service.initializePlayer(username);
}
```

### 5.2 Test Constants

```java
class TestConstants {
    static final String TEST_USERNAME = "testuser";
    static final long XP_FOR_LEVEL_1 = 100L;
    static final long XP_FOR_LEVEL_2 = 110L;
    static final int INITIAL_CORE_STAT = 0;
    static final int INITIAL_SECONDARY_STAT = 10;
    static final int INITIAL_MOMENTUM = 50;
}
```

---

## 6. Test Execution

### 6.1 Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PlayerStateServiceTest

# Run with coverage
mvn test jacoco:report
```

### 6.2 Test Configuration

Tests use H2 in-memory database with auto-generated schema:

```yaml
# application.yml (test profile default)
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: update
```

---

## 7. Example Test Cases

### 7.1 Initialize Player Test

```java
@SpringBootTest
@Transactional
class PlayerInitializationTest {

    @Autowired
    private PlayerStateService service;

    @Test
    void initializePlayer_Success() {
        PlayerStateResponse response = service.initializePlayer("newuser");
        
        assertNotNull(response.getIdentity().getPlayerId());
        assertEquals("newuser", response.getIdentity().getUsername());
        assertEquals(1, response.getProgression().getLevel());
        assertEquals(0, response.getProgression().getCurrentXp());
        assertEquals(PlayerRank.F, response.getProgression().getRank());
        assertEquals(0, response.getProgression().getFreeStatPoints());
        
        // Check attributes created
        assertEquals(AttributeType.values().length, response.getAttributes().size());
        
        // Check psych state defaults
        assertEquals(50, response.getPsychState().getMomentum());
        assertEquals(0, response.getPsychState().getStressLoad());
    }

    @Test
    void initializePlayer_DuplicateUsername_ThrowsException() {
        service.initializePlayer("duplicate");
        
        assertThrows(IllegalArgumentException.class, 
            () -> service.initializePlayer("duplicate"));
    }
}
```

### 7.2 XP Level Up Test

```java
@SpringBootTest
@Transactional
class PlayerXpSystemTest {

    @Autowired
    private PlayerStateService service;

    @Test
    void addXp_LevelUp_TriggersCorrectly() {
        PlayerStateResponse player = service.initializePlayer("levelup");
        UUID playerId = player.getIdentity().getPlayerId();
        
        // Level 1 requires 100 XP (100 * 1.1^1 = 110)
        service.addXp(playerId, 110);
        
        PlayerStateResponse updated = service.getPlayerState(playerId);
        assertEquals(2, updated.getProgression().getLevel());
        assertEquals(0, updated.getProgression().getCurrentXp()); // remainder
    }
}
```

### 7.3 Psych Metrics Boundary Test

```java
@SpringBootTest
@Transactional
class PlayerPsychStateTest {

    @Autowired
    private PlayerStateService service;

    @ParameterizedTest
    @CsvSource({
        "MOMENTUM, 60, 60",
        "MOMENTUM, -10, 0",
        "STRESS, 150, 100"
    })
    void updatePsychMetric_ClampedTo100(String metric, double change, int expected) {
        PlayerStateResponse player = service.initializePlayer("psych");
        service.updatePsychMetric(player.getIdentity().getPlayerId(), metric, change);
        
        PlayerStateResponse updated = service.getPlayerState(player.getIdentity().getPlayerId());
        
        if (metric.equals("MOMENTUM")) {
            assertEquals(expected, updated.getPsychState().getMomentum());
        }
    }
}
```

---

## 8. Best Practices

1. **Always use `@Transactional`** on integration tests to rollback changes
2. **Use descriptive test names**: `method_scenario_expectedResult`
3. **Test one thing per test** - avoid multiple assertions that could mask failures
4. **Test edge cases**: null values, boundaries, empty collections
5. **Verify state changes** by fetching the entity after modification
6. **Mock external services** when testing controllers
7. **Use test-specific configurations** to ensure isolated test environment

---

## 9. Known Issues / Technical Debt

1. **Missing setters in entities**: Some entities like `PlayerIdentity` have inconsistent getter/setter generation with Lombok. Review entity builders.
2. **Builder pattern issues**: Some builders may not include all fields. Verify before use.
3. **Event publishing**: Some tests may need to verify event publication using `@Mock` on `ApplicationEventPublisher`.

---

## 10. CI/CD Integration

Tests are automatically run on every build. Add to pipeline:

```bash
mvn verify -Dspring.profiles.active=test
```

---

## Appendix: Entity Reference

### PlayerIdentity
- `playerId` (UUID)
- `username` (String)
- `createdAt` (LocalDateTime)
- `systemVersion` (String)

### PlayerProgression
- `level` (int)
- `currentXp` (long)
- `rank` (PlayerRank)
- `xpFrozen` (boolean)
- `totalXpAccumulated` (long)
- `freeStatPoints` (int)

### PlayerAttribute
- `attributeType` (AttributeType)
- `baseValue` (double)
- `currentValue` (double)

### PlayerPsychState
- `momentum` (int)
- `stressLoad` (int)
- `complacency` (int)
- `confidenceBias` (int)

### PlayerRank
- F(5), E(10), D(25), C(45), B(70), A(90), S(100), SS(999)

### AttributeType
- DISCIPLINE, FOCUS, PHYSICAL_ENERGY, MENTAL_RESILIENCE, LEARNING_SPEED, EMOTIONAL_CONTROL, STR, INT, VIT, SEN
