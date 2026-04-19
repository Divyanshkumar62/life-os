package com.lifeos.notification.service;

import com.lifeos.player.repository.PlayerIdentityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledNotificationService {

    private final PushNotificationService notificationService;
    private final PlayerIdentityRepository identityRepository;

    @Scheduled(cron = "0 0 23 * * *") // Daily at 11 PM (one hour before midnight)
    public void sendMidnightCountdownNotifications() {
        log.info("Sending midnight countdown notifications to all players");
        
        List<UUID> playerIds = identityRepository.findAll().stream()
                .map(p -> p.getPlayerId())
                .toList();
        
        for (UUID playerId : playerIds) {
            try {
                notificationService.sendMidnightCountdown(playerId);
            } catch (Exception e) {
                log.error("Failed to send midnight countdown to player {}: {}", playerId, e.getMessage());
            }
        }
        
        log.info("Sent midnight countdown notifications to {} players", playerIds.size());
    }
}
