package com.lifeos.quest.service;

import com.lifeos.quest.domain.enums.DifficultyTier;
import com.lifeos.quest.domain.enums.Priority;
import com.lifeos.quest.domain.enums.QuestType;
import com.lifeos.quest.dto.QuestRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class QuestFallbackService {

    public List<QuestRequest> generateFallbackQuests(UUID playerId) {
        return List.of(
            QuestRequest.builder()
                .playerId(playerId)
                .title("Iron Body: Initial Conditioning")
                .description("The System has detected a baseline physical state. Begin the transformation with 100 pushups, 100 situps, and a 10km run (or 30 mins cardio). Failure is not an option.")
                .questType(QuestType.PHYSICAL)
                .difficultyTier(DifficultyTier.D)
                .priority(Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().plusHours(24))
                .successXp(150)
                .failureXp(0)
                .goldReward(0)
                .attributeDeltas(Map.of("STRENGTH", 1.0, "VITALITY", 0.5))
                .systemMutable(true)
                .build(),
            
            QuestRequest.builder()
                .playerId(playerId)
                .title("Mind Palace: Focused Deep Work")
                .description("Your cognitive focus is scattered. Complete 90 minutes of undistracted deep work on your most important goal. Notifications must be silenced.")
                .questType(QuestType.COGNITIVE)
                .difficultyTier(DifficultyTier.D)
                .priority(Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().plusHours(24))
                .successXp(150)
                .failureXp(0)
                .goldReward(0)
                .attributeDeltas(Map.of("INTELLECT", 1.0, "DISCIPLINE", 0.5))
                .systemMutable(true)
                .build(),
                
            QuestRequest.builder()
                .playerId(playerId)
                .title("Cold Resolve: The Morning Ritual")
                .description("Discipline is forged in discomfort. Wake up before 7:00 AM and complete a 2-minute cold shower. Master your impulses.")
                .questType(QuestType.DISCIPLINE)
                .difficultyTier(DifficultyTier.D)
                .priority(Priority.NORMAL)
                .deadlineAt(LocalDateTime.now().plusHours(24))
                .successXp(200)
                .failureXp(0)
                .goldReward(0)
                .attributeDeltas(Map.of("DISCIPLINE", 1.5, "SENSE", 0.5))
                .systemMutable(true)
                .build()
        );
    }
}
