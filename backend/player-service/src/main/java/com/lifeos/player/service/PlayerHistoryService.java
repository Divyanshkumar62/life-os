package com.lifeos.player.service;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.project.domain.enums.ProjectStatus;
import com.lifeos.project.repository.ProjectRepository;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.quest.repository.QuestRepository;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlayerHistoryService {

    private final ProjectRepository projectRepository;
    private final QuestRepository questRepository;
    private final PlayerStateService playerStateService;

    public PlayerHistoryService(ProjectRepository projectRepository, QuestRepository questRepository,
                              PlayerStateService playerStateService) {
        this.projectRepository = projectRepository;
        this.questRepository = questRepository;
        this.playerStateService = playerStateService;
    }

    @Transactional(readOnly = true)
    public PlayerDossier getPlayerDossier(UUID playerId) {
        // 1. Project History
        long completedProjects = projectRepository.countByPlayerPlayerIdAndStatus(playerId, ProjectStatus.COMPLETED);
        long failedProjects = projectRepository.countByPlayerPlayerIdAndStatus(playerId, ProjectStatus.FAILED);

        // 2. Quest Performance
        List<Quest> failedQuests = questRepository.findByPlayerPlayerIdAndState(playerId, QuestState.FAILED);
        Map<String, Long> failureExternal = failedQuests.stream()
                .filter(q -> q.getPrimaryAttribute() != null)
                .collect(Collectors.groupingBy(q -> q.getPrimaryAttribute().name(), Collectors.counting()));
        
        String strugglesWith = failureExternal.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");

        // 3. Attribute Profile
        var state = playerStateService.getPlayerState(playerId);
        var stats = state.getAttributes();
        
        String strongest = stats.stream()
                .max((a, b) -> Double.compare(a.getCurrentValue(), b.getCurrentValue()))
                .map(a -> a.getAttributeType().name())
                .orElse("Unknown");

        String weakest = stats.stream()
                .min((a, b) -> Double.compare(a.getCurrentValue(), b.getCurrentValue()))
                .map(a -> a.getAttributeType().name())
                .orElse("Unknown");

        return PlayerDossier.builder()
                .completedDungeons((int) completedProjects)
                .failedDungeons((int) failedProjects)
                .strugglesWithAttribute(strugglesWith)
                .strongestAttribute(strongest)
                .weakestAttribute(weakest)
                .build();
    }

    @Transactional(readOnly = true)
    public List<com.lifeos.project.domain.Project> getFailedDungeons(UUID playerId) {
        return projectRepository.findByPlayerPlayerIdAndStatus(playerId, ProjectStatus.FAILED);
    }

    @Data
    public static class PlayerDossier {
        private int completedDungeons;
        private int failedDungeons;
        private String strugglesWithAttribute;
        private String strongestAttribute;
        private String weakestAttribute;

        public static PlayerDossierBuilder builder() {
            return new PlayerDossierBuilder();
        }

        public static class PlayerDossierBuilder {
            private int completedDungeons;
            private int failedDungeons;
            private String strugglesWithAttribute;
            private String strongestAttribute;
            private String weakestAttribute;

            public PlayerDossierBuilder completedDungeons(int completedDungeons) { this.completedDungeons = completedDungeons; return this; }
            public PlayerDossierBuilder failedDungeons(int failedDungeons) { this.failedDungeons = failedDungeons; return this; }
            public PlayerDossierBuilder strugglesWithAttribute(String strugglesWithAttribute) { this.strugglesWithAttribute = strugglesWithAttribute; return this; }
            public PlayerDossierBuilder strongestAttribute(String strongestAttribute) { this.strongestAttribute = strongestAttribute; return this; }
            public PlayerDossierBuilder weakestAttribute(String weakestAttribute) { this.weakestAttribute = weakestAttribute; return this; }

            public PlayerDossier build() {
                PlayerDossier dossier = new PlayerDossier();
                dossier.completedDungeons = this.completedDungeons;
                dossier.failedDungeons = this.failedDungeons;
                dossier.strugglesWithAttribute = this.strugglesWithAttribute;
                dossier.strongestAttribute = this.strongestAttribute;
                dossier.weakestAttribute = this.weakestAttribute;
                return dossier;
            }
        }

        public String toPromptString() {
            return String.format(
                "Player Dossier:\n" +
                "- Dungeons Cleared: %d\n" +
                "- Dungeons Failed: %d\n" +
                "- Struggle Area: %s Quests\n" +
                "- Strongest Stat: %s\n" +
                "- Weakest Stat: %s\n",
                completedDungeons, failedDungeons, strugglesWithAttribute, strongestAttribute, weakestAttribute
            );
        }
    }
}
