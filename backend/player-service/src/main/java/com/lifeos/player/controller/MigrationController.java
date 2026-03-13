package com.lifeos.player.controller;

import com.lifeos.player.service.LevelMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/migration")
@RequiredArgsConstructor
public class MigrationController {

    private final LevelMigrationService migrationService;

    @PostMapping("/recalculate-levels")
    public ResponseEntity<String> recalculateLevels() {
        migrationService.recalculateAllPlayers();
        return ResponseEntity.ok("Migration triggered: All player levels have been recalculated based on the new exponential curve.");
    }
}
