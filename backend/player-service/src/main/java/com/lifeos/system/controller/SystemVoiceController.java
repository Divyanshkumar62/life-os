package com.lifeos.system.controller;

import com.lifeos.system.domain.SystemEvent;
import com.lifeos.system.service.SystemVoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemVoiceController {

    private static final Logger log = LoggerFactory.getLogger(SystemVoiceController.class);

    private final SystemVoiceService systemVoiceService;

    public SystemVoiceController(SystemVoiceService systemVoiceService) {
        this.systemVoiceService = systemVoiceService;
    }

    /**
     * Retrieves unconsumed system alerts/events for the player.
     * This endpoint is continuously polled by the frontend to trigger global
     * SystemToast notifications for achievements, level ups, penalties, etc.
     */
    @GetMapping("/alerts/{playerId}")
    public ResponseEntity<List<SystemEvent>> getUnconsumedAlerts(@PathVariable UUID playerId) {
        log.debug("Polling System Voice Alerts for player: {}", playerId);
        List<SystemEvent> alerts = systemVoiceService.getPlayerEvents(playerId);
        return ResponseEntity.ok(alerts);
    }
}
