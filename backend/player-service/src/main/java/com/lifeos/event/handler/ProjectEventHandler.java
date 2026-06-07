package com.lifeos.event.handler;

import com.lifeos.event.concrete.ProjectCompletedEvent;
import com.lifeos.event.concrete.QuestCompletedEvent;
import com.lifeos.project.service.ProjectService;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.quest.domain.Quest;
import com.lifeos.player.service.PlayerStateService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ProjectEventHandler {

    private final ProjectService projectService;
    private final QuestRepository questRepository;
    private final PlayerStateService playerStateService;
    private final com.lifeos.economy.service.EconomyService economyService;

    public ProjectEventHandler(ProjectService projectService, QuestRepository questRepository, PlayerStateService playerStateService, com.lifeos.economy.service.EconomyService economyService) {
        this.projectService = projectService;
        this.questRepository = questRepository;
        this.playerStateService = playerStateService;
        this.economyService = economyService;
    }

    @EventListener
    public void handleQuestCompleted(QuestCompletedEvent event) {
        // We only care if the quest belongs to a project
        Quest quest = questRepository.findById(event.getQuestId()).orElse(null);
        if (quest != null && quest.getProjectId() != null) {
            projectService.updateProgress(quest.getProjectId());
        }
    }

    @EventListener
    public void handleProjectCompleted(ProjectCompletedEvent event) {
        // Award Dynamic AI Loot
        long xpReward = event.getBaseXpReward();
        long goldReward = event.getBaseGoldReward();
        
        if (xpReward > 0) {
            playerStateService.addXp(event.getPlayerId(), xpReward);
        }
        
        if (goldReward > 0) {
            economyService.addGold(event.getPlayerId(), goldReward, "Dungeon Clear: " + event.getProjectId());
        }
        
        System.out.println("DUNGEON CLEARED! Player " + event.getPlayerId() + " completed Project " + event.getProjectId());
        System.out.println("Boss Key Acquired: " + event.getBossKeyReward());
        System.out.println("XP Awarded: " + xpReward);
        System.out.println("Gold Awarded: " + goldReward);
    }
}
