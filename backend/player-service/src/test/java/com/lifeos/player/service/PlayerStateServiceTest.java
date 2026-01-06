package com.lifeos.player.service;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.repository.PlayerIdentityRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PlayerStateServiceTest {

    @Autowired
    private PlayerStateService playerStateService;

    @Autowired
    private PlayerIdentityRepository identityRepository;

    @Test
    public void testFullFlow() {
        String username = "testuser";

        // 1. Initialize
        PlayerStateResponse response = playerStateService.initializePlayer(username);
        assertNotNull(response);
        assertEquals(username, response.getIdentity().getUsername());
        assertEquals(1, response.getProgression().getLevel());
        assertEquals(10.0, response.getAttributes().get(0).getCurrentValue());

        UUID playerId = response.getIdentity().getPlayerId();

        // 2. Add XP (No Level Up)
        playerStateService.addXp(playerId, 50);
        response = playerStateService.getPlayerState(playerId);
        assertEquals(50, response.getProgression().getCurrentXp());
        assertEquals(1, response.getProgression().getLevel());

        // 3. Add XP (Level Up) -> Threshold 100 for Level 1
        playerStateService.addXp(playerId, 60); // Total 110 -> Level 2, Remainder 10
        response = playerStateService.getPlayerState(playerId);
        assertEquals(2, response.getProgression().getLevel());
        assertEquals(10, response.getProgression().getCurrentXp());

        // 4. Update Attribute
        playerStateService.updateAttribute(playerId, AttributeType.FOCUS, 5.0);
        response = playerStateService.getPlayerState(playerId);
        double focusValue = response.getAttributes().stream()
                .filter(a -> a.getAttributeType() == AttributeType.FOCUS)
                .findFirst().orElseThrow()
                .getCurrentValue();
        assertEquals(15.0, focusValue);
    }
}
