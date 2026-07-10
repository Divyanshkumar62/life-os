package com.lifeos.core.scheduler;

import com.lifeos.core.entity.PlayerState;
import com.lifeos.core.repository.PlayerStateRepository;
import com.lifeos.core.service.DailyResetProcessorService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("coreDailyResetScheduler")
@RequiredArgsConstructor
public class DailyResetScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyResetScheduler.class);

    private final PlayerStateRepository playerStateRepository;
    private final DailyResetProcessorService dailyResetProcessorService;

    /**
     * Executes the daily reset sweep for all active players at midnight every day.
     * Cron expression "0 0 0 * * ?" triggers daily reset at 00:00:00.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void executeDailyResetSweep() {
        log.info("System Heartbeat: Triggering Daily Reset Sweep");
        
        List<PlayerState> players = playerStateRepository.findAll();
        
        for (PlayerState player : players) {
            try {
                // Delegate to transactional processor to execute in isolation and lock row
                dailyResetProcessorService.resetPlayer(player.getPlayerId());
            } catch (Exception e) {
                log.error("Failed to execute daily reset for player {}: {}", player.getPlayerId(), e.getMessage());
            }
        }
        
        log.info("Daily Reset Sweep complete for {} players", players.size());
    }
}
