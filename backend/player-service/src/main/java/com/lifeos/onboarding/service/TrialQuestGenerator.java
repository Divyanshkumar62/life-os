package com.lifeos.onboarding.service;

import com.lifeos.quest.dto.QuestRequest;
import com.lifeos.quest.domain.enums.QuestType;
import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.domain.enums.Priority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class TrialQuestGenerator {

    public QuestRequest generateTrialQuest(UUID playerId) {
        return QuestRequest.builder()
                .playerId(playerId)
                .title("System Qualification: Courage of the Weak")
                .description("The System has detected potential in you. To awaken your true abilities, you must prove your resolve. Complete 3 focused work sessions of 30 minutes each within 24 hours.")
                .questType(QuestType.SYSTEM_TRIAL)
                .difficultyTier(DifficultyTier.E)
                .priority(Priority.CRITICAL)
                .deadlineAt(LocalDateTime.now().plusHours(24))
                .successXp(100)
                .failureXp(0)
                .goldReward(0)
                .attributeDeltas(Map.of("DISCIPLINE", 1.0))
                .systemMutable(false) // Cannot be abandoned easily
                .build();
    }
}
