package com.lifeos.notification.service;

import com.lifeos.notification.dto.FcmPayload;
import com.lifeos.notification.dto.NotificationType;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.repository.PlayerIdentityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final PlayerIdentityRepository identityRepository;

    @Async
    public void sendNotification(UUID playerId, NotificationType type, String title, String body, Map<String, String> dataPayload) {
        try {
            PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
            
            if (identity == null) {
                log.warn("Player not found for notification: {}", playerId);
                return;
            }
            
            if (!identity.isNotificationsEnabled()) {
                log.info("Notifications disabled for player: {}", playerId);
                return;
            }
            
            String fcmToken = identity.getFcmToken();
            if (fcmToken == null || fcmToken.isBlank()) {
                log.warn("No FCM token for player: {}", playerId);
                return;
            }
            
            // Build and send FCM message
            FcmPayload payload = FcmPayload.builder()
                    .token(fcmToken)
                    .title(title)
                    .body(body)
                    .data(dataPayload)
                    .priority(type.getPriority())
                    .build();
            
            // Attempt to send via FirebaseMessaging if initialized
            try {
                Message.Builder mb = Message.builder()
                        .setToken(fcmToken)
                        .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                        .putAllData(dataPayload != null ? dataPayload : Map.of())
                        .setAndroidConfig(AndroidConfig.builder()
                                .setPriority(AndroidConfig.Priority.HIGH)
                                .setNotification(AndroidNotification.builder().setChannelId("system_announcements").build())
                                .build());

                Message message = mb.build();

                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Sent FCM message to player {}: response={}", playerId, response);
            } catch (IllegalStateException ise) {
                // FirebaseApp not initialized or other runtime problems
                log.warn("Firebase not initialized or unavailable - falling back to logging. {}", ise.getMessage());
                log.info("=== PUSH NOTIFICATION ===");
                log.info("Player: {} ({})", identity.getUsername(), playerId);
                log.info("Type: {} (Priority: {})", type, type.getPriority());
                log.info("Title: {}", title);
                log.info("Body: {}", body);
                log.info("Data: {}", dataPayload);
            } catch (Exception e) {
                log.error("Error sending FCM message to {}: {}", playerId, e.getMessage(), e);
            }
            
        } catch (Exception e) {
            log.error("Failed to send push notification to player {}: {}", playerId, e.getMessage());
        }
    }

    // Tier 1: Critical / Survival (High Priority)
    @Async
    public void sendDungeonBreakWarning(UUID playerId, String projectName) {
        sendNotification(playerId, 
            NotificationType.DUNGEON_BREAK,
            "CRITICAL: Dungeon Break Imminent",
            "Monsters are invading your schedule. Enter " + projectName + " immediately.",
            Map.of("screen", "dungeon", "action", "enter", "project", projectName));
    }

    @Async
    public void sendMidnightCountdown(UUID playerId) {
        sendNotification(playerId,
            NotificationType.MIDNIGHT_COUNTDOWN,
            "WARNING: Survival Quest starts in 60 minutes",
            "Complete your tasks or face the Penalty Zone.",
            Map.of("screen", "quests", "action", "countdown"));
    }

    // Tier 2: Progression & Economy (Medium Priority)
    @Async
    public void sendLevelUp(UUID playerId, int newLevel) {
        sendNotification(playerId,
            NotificationType.LEVEL_UP,
            "Level Up!",
            "Your attributes have been refreshed. New Level: " + newLevel,
            Map.of("screen", "profile", "action", "levelup", "level", String.valueOf(newLevel)));
    }

    @Async
    public void sendRankPromotion(UUID playerId, String newRank) {
        sendNotification(playerId,
            NotificationType.RANK_PROMOTION,
            "Rank Promotion!",
            "New Rank: " + newRank,
            Map.of("screen", "profile", "action", "promotion", "rank", newRank));
    }

    @Async
    public void sendShopRestock(UUID playerId) {
        sendNotification(playerId,
            NotificationType.SHOP_RESTOCK,
            "New items available in the System Store",
            "Prepare for your next raid.",
            Map.of("screen", "shop", "action", "restock"));
    }

    // Tier 3: Context & Intel (Low Priority)
    @Async
    public void sendIntelQuestAvailable(UUID playerId) {
        sendNotification(playerId,
            NotificationType.INTEL_QUEST,
            "System Synchronization Required",
            "New Intel Quest available.",
            Map.of("screen", "intel", "action", "new"));
    }
}
