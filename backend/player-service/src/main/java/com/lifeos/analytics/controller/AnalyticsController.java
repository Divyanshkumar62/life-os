package com.lifeos.analytics.controller;

import com.lifeos.analytics.dto.HeatmapEntryDTO;
import com.lifeos.analytics.dto.StatDataPointDTO;
import com.lifeos.analytics.dto.GraveyardEntryDTO;
import com.lifeos.analytics.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/heatmap")
    public ResponseEntity<List<HeatmapEntryDTO>> getDailyHeatmap(@RequestParam UUID playerId) {
        log.info("Fetching daily heatmap for player: {}", playerId);
        List<HeatmapEntryDTO> heatmap = analyticsService.getDailyHeatmap(playerId);
        return ResponseEntity.ok(heatmap);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<StatDataPointDTO>> getStatGrowth(@RequestParam UUID playerId) {
        log.info("Fetching stat growth trajectory for player: {}", playerId);
        List<StatDataPointDTO> stats = analyticsService.getStatGrowth(playerId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/graveyard")
    public ResponseEntity<List<GraveyardEntryDTO>> getJournalGraveyard(@RequestParam UUID playerId) {
        log.info("Fetching confession graveyard for player: {}", playerId);
        List<GraveyardEntryDTO> graveyard = analyticsService.getJournalGraveyard(playerId);
        return ResponseEntity.ok(graveyard);
    }
}
