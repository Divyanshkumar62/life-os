package com.lifeos.player.service;

import com.lifeos.player.domain.PlayerPsychState;
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
    public void checklist_circularDependencies() {
        // Spring Boot fails startup on circular dependencies by default.
        // Successful context load implies passing this check.
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

        // Try to set invalid values
        psychState.setMomentum(150); // Max is 100

        // Save should fail IF validation is triggered.
        // Note: JPA save() usually requires a Validator to be configured or flush to DB to trigger constraints if defined in DB.
        // Spring Boot Validator is on classpath.
        
        assertThrows(ConstraintViolationException.class, () -> {
            psychStateRepository.saveAndFlush(psychState);
        }, "Should throw ConstraintViolationException for momentum > 100");
    }
}
