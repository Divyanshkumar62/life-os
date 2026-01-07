package com.lifeos.player.service;

import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.repository.PlayerPsychStateRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class PlayerVerificationTest {

    @Autowired
    private PlayerStateService playerStateService;

    @Autowired
    private PlayerPsychStateRepository psychStateRepository;

    @Autowired
    private PlayerIdentityRepository identityRepository;

    @Test
    public void checklist_appStartsCleanly() {
        // If this test runs, the app context has loaded successfully.
        Assertions.assertNotNull(playerStateService, "Service should be loaded");
    }

    @Test
    public void checklist_enumsMappedCorrectly() {
        var response = playerStateService.initializePlayer("enumTester");
        Assertions.assertEquals(PlayerRank.F, response.getProgression().getRank(), "Enum Rank.F should be persisted and retrieved");
    }

    @Test
    public void checklist_constraintsEnforced() {
        var response = playerStateService.initializePlayer("constraintTester");
        var identity = identityRepository.findByUsername("constraintTester").orElseThrow();
        var psychState = psychStateRepository.findByPlayerPlayerId(identity.getPlayerId()).orElseThrow();

        // Try to set invalid values directly via Repository (JPA Validation)
        psychState.setMomentum(150); // Max is 100
        assertThrows(ConstraintViolationException.class, () -> {
            psychStateRepository.saveAndFlush(psychState);
        }, "Should throw ConstraintViolationException for momentum > 100");
    }

    @Test
    public void guard_xpCannotBeNegative() {
        var response = playerStateService.initializePlayer("xpGuardTester");
        UUID playerId = response.getIdentity().getPlayerId();

        assertThrows(IllegalArgumentException.class, () -> {
            playerStateService.addXp(playerId, -50);
        }, "Should check for negative XP");
    }

    @Test
    public void guard_psychStateClamped() {
        var response = playerStateService.initializePlayer("psychGuardTester");
        UUID playerId = response.getIdentity().getPlayerId();

        // 1. Try to boost Momentum above 100
        // Default is 50. Add 100. Should be clamped to 100.
        playerStateService.updatePsychMetric(playerId, "MOMENTUM", 100.0);
        var newState = playerStateService.getPlayerState(playerId);
        Assertions.assertEquals(100, newState.getPsychState().getMomentum(), "Momentum should be clamped at 100");

        // 2. Try to drop Stress below 0
        // Default is 0. Subtract 50. Should be clamped to 0.
        playerStateService.updatePsychMetric(playerId, "STRESS", -50.0);
        newState = playerStateService.getPlayerState(playerId);
        Assertions.assertEquals(0, newState.getPsychState().getStressLoad(), "Stress should be clamped at 0");
    }
}
