package com.lifeos.quest.scheduler;

import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.quest.service.DailyQuestService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DailyResetScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyResetScheduler.class);

    private final PlayerIdentityRepository identityRepository;
    private final DailyQuestService dailyQuestService;

    @Scheduled(cron = "0 0 * * * *")
    public void runDailyResetCheck() {
        log.info("Starting hourly daily reset check for all players...");
        List<UUID> playerIds = identityRepository.findAllIds();
        log.info("Found {} player(s) to check.", playerIds.size());
        
        for (UUID playerId : playerIds) {
            try {
                dailyQuestService.performDailyResetCheck(playerId);
            } catch (Exception e) {
                log.error("Failed to perform daily reset check for player {}", playerId, e);
            }
        }
        log.info("Hourly daily reset check completed.");
    }
}
