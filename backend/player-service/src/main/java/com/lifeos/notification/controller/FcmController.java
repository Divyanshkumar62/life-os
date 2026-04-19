package com.lifeos.notification.controller;

import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.repository.PlayerIdentityRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class FcmController {

    private final PlayerIdentityRepository identityRepository;
    private static final Logger log = LoggerFactory.getLogger(FcmController.class);

    @PutMapping("/update-fcm-token")
    public ResponseEntity<Void> updateFcmToken(@RequestBody UpdateFcmRequest req) {
        if (req == null || req.getPlayerId() == null || req.getToken() == null || req.getToken().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        PlayerIdentity identity = identityRepository.findById(req.getPlayerId()).orElse(null);
        if (identity == null) {
            return ResponseEntity.notFound().build();
        }

        identity.setFcmToken(req.getToken());
        identityRepository.save(identity);
        log.info("Updated FCM token for player {}", req.getPlayerId());
        return ResponseEntity.ok().build();
    }
}
