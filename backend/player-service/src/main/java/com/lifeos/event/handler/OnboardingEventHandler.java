package com.lifeos.event.handler;

import com.lifeos.event.concrete.QuestCompletedEvent;
import com.lifeos.onboarding.domain.OnboardingProgress;
import com.lifeos.onboarding.domain.OnboardingStage;
import com.lifeos.onboarding.repository.OnboardingProgressRepository;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnboardingEventHandler {

    private final OnboardingProgressRepository onboardingRepository;
    private final PlayerIdentityRepository identityRepository;
    private final QuestRepository questRepository;

    @EventListener
    public void onQuestCompleted(QuestCompletedEvent event) {
        UUID playerId = event.getPlayerId();
        
        // Check if player is in onboarding trial phase
        OnboardingProgress progress = onboardingRepository.findById(playerId).orElse(null);
        if (progress == null || progress.getCurrentStage() != OnboardingStage.TRIAL_QUEST) {
            return; // Not in onboarding trial
        }
        
        // Check if onboarding is already completed
        PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        if (identity == null || identity.isOnboardingCompleted()) {
            return;
        }
        
        // Count remaining active onboarding quests
        List<Quest> activeOnboardingQuests = questRepository.findByPlayerPlayerIdAndState(playerId, QuestState.ACTIVE)
                .stream()
                .filter(q -> q.getQuestType() != com.lifeos.quest.domain.enums.QuestType.PENALTY)
                .filter(q -> q.getQuestType() != com.lifeos.quest.domain.enums.QuestType.PROMOTION_EXAM)
                .toList();
        
        if (activeOnboardingQuests.isEmpty()) {
            // All onboarding quests completed! Unlock the system
            log.info("All onboarding quests completed for player: {}. Unlocking system.", playerId);
            
            progress.setCurrentStage(OnboardingStage.COMPLETED);
            progress.setTrialCompleted(true);
            progress.setCompletedAt(java.time.LocalDateTime.now());
            onboardingRepository.save(progress);
            
            identity.setOnboardingCompleted(true);
            identityRepository.save(identity);
            
            log.info("Player {} has completed onboarding and unlocked the system!", playerId);
        } else {
            log.debug("Player {} has {} remaining onboarding quests", playerId, activeOnboardingQuests.size());
        }
    }
}
