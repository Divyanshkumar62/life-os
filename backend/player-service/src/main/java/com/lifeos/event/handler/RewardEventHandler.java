package com.lifeos.event.handler;

import com.lifeos.event.DomainEvent;
import com.lifeos.event.DomainEventHandler;
import com.lifeos.event.concrete.QuestCompletedEvent;
import com.lifeos.reward.service.RewardService;
import org.springframework.stereotype.Component;

@Component
public class RewardEventHandler implements DomainEventHandler<DomainEvent> {

    private final RewardService rewardService;

    public RewardEventHandler(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof QuestCompletedEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        if (event instanceof QuestCompletedEvent questEvent) {
            // RewardService handles its own guards
            rewardService.applyReward(questEvent.getQuestId(), questEvent.getPlayerId());
        }
    }
}
