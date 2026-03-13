package com.lifeos.onboarding.service;

import com.lifeos.onboarding.domain.PlayerProfile;
import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.domain.enums.Priority;
import com.lifeos.quest.domain.enums.QuestType;
import com.lifeos.quest.dto.QuestRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class IntelQuestGenerator {

    public QuestRequest generateFirstIntelQuest(UUID playerId) {
        return QuestRequest.builder()
                .playerId(playerId)
                .title("Intel: Physical Capability Analysis")
                .description("The System requires data to optimize your growth. \n\nGoal: Define your current fitness level and one specific physical goal for the next 30 days.\n\nReward: Strength +1")
                .questType(QuestType.INTEL_GATHERING)
                .difficultyTier(DifficultyTier.E)
                .priority(Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().plusHours(24))
                .successXp(50)
                .goldReward(0)
                .systemMutable(false)
                .primaryAttribute(com.lifeos.player.domain.enums.AttributeType.STRENGTH)
                .attributeDeltas(Map.of("STRENGTH", 1.0))
                .build();
    }
    
    public QuestRequest generateFollowUpIntel(UUID playerId, long daysActive) {
        if (daysActive == 3) {
            return generateMentalAnalysisQuest(playerId);
        } else if (daysActive == 7) {
            return generateEmotionalAnalysisQuest(playerId);
        }
        return null;
    }

    public QuestRequest generateMentalAnalysisQuest(UUID playerId) {
        return QuestRequest.builder()
                .playerId(playerId)
                .title("Intel: Cognitive Pattern Analysis")
                .description("The System requires data on your learning patterns.\n\nGoal: Identify your peak focus hours and preferred learning medium (Video vs Text).\n\nReward: Intelligence +1")
                .questType(QuestType.INTEL_GATHERING)
                .difficultyTier(DifficultyTier.D)
                .priority(Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().plusHours(48))
                .successXp(75)
                .goldReward(0)
                .systemMutable(false)
                .primaryAttribute(com.lifeos.player.domain.enums.AttributeType.INTELLIGENCE)
                .attributeDeltas(Map.of("INTELLIGENCE", 1.0))
                .build();
    }

    private QuestRequest generateEmotionalAnalysisQuest(UUID playerId) {
        return QuestRequest.builder()
                .playerId(playerId)
                .title("Intel: Emotional Resilience Scan")
                .description("The System detects potential stress triggers.\n\nGoal: List your top 3 stressors and one coping mechanism you currently use.\n\nReward: Wisdom +1")
                .questType(QuestType.INTEL_GATHERING)
                .difficultyTier(DifficultyTier.C)
                .priority(Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().plusHours(48))
                .successXp(100)
                .goldReward(0)
                .systemMutable(false)
                .primaryAttribute(com.lifeos.player.domain.enums.AttributeType.SENSE)
                .attributeDeltas(Map.of("SENSE", 1.0))
                .build();
    }
}
