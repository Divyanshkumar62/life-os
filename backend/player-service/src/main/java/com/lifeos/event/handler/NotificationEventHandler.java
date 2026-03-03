package com.lifeos.event.handler;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.DomainEventHandler;
import com.lifeos.event.concrete.*;
import com.lifeos.notification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler implements DomainEventHandler<DomainEvent> {

    private final PushNotificationService notificationService;

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof LevelUpEvent ||
               event instanceof RankPromotionEvent ||
               event instanceof QuestCompletedEvent ||
               event instanceof QuestFailedEvent ||
               event instanceof QuestExpiredEvent ||
               event instanceof DailyQuestCompletedEvent ||
               event instanceof DailyQuestFailedEvent ||
               event instanceof PenaltyAppliedEvent ||
               event instanceof PenaltyZoneEnteredEvent ||
               event instanceof PenaltyZoneExitedEvent ||
               event instanceof StreakBrokenEvent ||
               event instanceof DailyQuestGeneratedEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        try {
            if (event instanceof LevelUpEvent levelUpEvent) {
                notificationService.sendLevelUp(levelUpEvent.getPlayerId(), levelUpEvent.getNewLevel());
            }
            else if (event instanceof RankPromotionEvent rankPromotionEvent) {
                notificationService.sendRankPromotion(rankPromotionEvent.getPlayerId(), rankPromotionEvent.getNewRank());
            }
            else if (event instanceof QuestCompletedEvent questCompletedEvent) {
                log.debug("Quest completed - notification handled by LevelUpEvent if applicable");
            }
            else if (event instanceof QuestFailedEvent questFailedEvent) {
                log.debug("Quest failed for player: {}", questFailedEvent.getPlayerId());
            }
            else if (event instanceof QuestExpiredEvent questExpiredEvent) {
                log.debug("Quest expired for player: {}", questExpiredEvent.getPlayerId());
            }
            else if (event instanceof DailyQuestCompletedEvent dailyCompletedEvent) {
                log.debug("Daily quest completed for player: {}", dailyCompletedEvent.getPlayerId());
            }
            else if (event instanceof DailyQuestFailedEvent dailyFailedEvent) {
                log.debug("Daily quest failed for player: {}", dailyFailedEvent.getPlayerId());
            }
            else if (event instanceof PenaltyAppliedEvent penaltyAppliedEvent) {
                log.debug("Penalty applied to player: {}", penaltyAppliedEvent.getPlayerId());
            }
            else if (event instanceof PenaltyZoneEnteredEvent penaltyZoneEnteredEvent) {
                log.debug("Player entered penalty zone: {}", penaltyZoneEnteredEvent.getPlayerId());
            }
            else if (event instanceof PenaltyZoneExitedEvent penaltyZoneExitedEvent) {
                log.debug("Player exited penalty zone: {}", penaltyZoneExitedEvent.getPlayerId());
            }
            else if (event instanceof StreakBrokenEvent streakBrokenEvent) {
                log.debug("Streak broken for player: {}", streakBrokenEvent.getPlayerId());
            }
            else if (event instanceof DailyQuestGeneratedEvent dailyQuestGeneratedEvent) {
                notificationService.sendIntelQuestAvailable(dailyQuestGeneratedEvent.getPlayerId());
            }
        } catch (Exception e) {
            log.error("Error handling notification for event {}: {}", event.getClass().getSimpleName(), e.getMessage());
        }
    }
}
