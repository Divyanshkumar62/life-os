package com.lifeos.analytics.service;

import com.lifeos.analytics.dto.HeatmapEntryDTO;
import com.lifeos.analytics.dto.StatDataPointDTO;
import com.lifeos.analytics.dto.GraveyardEntryDTO;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.domain.enums.QuestCategory;
import com.lifeos.quest.domain.enums.QuestState;
import com.lifeos.player.repository.PlayerAttributeRepository;
import com.lifeos.player.domain.PlayerAttribute;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.penalty.repository.PlayerJournalRepository;
import com.lifeos.penalty.domain.PlayerJournal;
import com.lifeos.analytics.dto.DungeonGraveyardEntryDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final QuestRepository questRepository;
    private final PlayerAttributeRepository attributeRepository;
    private final PlayerJournalRepository journalRepository;
    private final com.lifeos.player.repository.PlayerIdentityRepository identityRepository;
    private final com.lifeos.player.repository.PlayerStatusFlagRepository flagRepository;
    private final com.lifeos.core.repository.TemporalModifierRepository temporalModifierRepository;
    private final com.lifeos.progression.repository.JobChangeQuestRepository jobChangeQuestRepository;
    private final com.lifeos.project.repository.ProjectRepository projectRepository;
    private final com.lifeos.progression.repository.RankExamAttemptRepository examAttemptRepository;
    private final com.lifeos.player.repository.PlayerProgressionRepository progressionRepository;

    public AnalyticsService(QuestRepository questRepository,
                            PlayerAttributeRepository attributeRepository,
                            PlayerJournalRepository journalRepository,
                            com.lifeos.player.repository.PlayerIdentityRepository identityRepository,
                            com.lifeos.player.repository.PlayerStatusFlagRepository flagRepository,
                            com.lifeos.core.repository.TemporalModifierRepository temporalModifierRepository,
                            com.lifeos.progression.repository.JobChangeQuestRepository jobChangeQuestRepository,
                            com.lifeos.project.repository.ProjectRepository projectRepository,
                            com.lifeos.progression.repository.RankExamAttemptRepository examAttemptRepository,
                            com.lifeos.player.repository.PlayerProgressionRepository progressionRepository) {
        this.questRepository = questRepository;
        this.attributeRepository = attributeRepository;
        this.journalRepository = journalRepository;
        this.identityRepository = identityRepository;
        this.flagRepository = flagRepository;
        this.temporalModifierRepository = temporalModifierRepository;
        this.jobChangeQuestRepository = jobChangeQuestRepository;
        this.projectRepository = projectRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.progressionRepository = progressionRepository;
    }

    public List<HeatmapEntryDTO> getDailyHeatmap(UUID playerId) {
        // Fetch all SYSTEM_DAILY quests of the player
        List<Quest> dailyQuests = questRepository.findByPlayerPlayerId(playerId).stream()
                .filter(q -> q.getCategory() == QuestCategory.SYSTEM_DAILY)
                .collect(Collectors.toList());

        // Group quests by local date of assignedAt
        Map<LocalDate, List<Quest>> questsByDate = new HashMap<>();
        for (Quest q : dailyQuests) {
            if (q.getAssignedAt() != null) {
                LocalDate date = q.getAssignedAt().toLocalDate();
                questsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(q);
            }
        }

        com.lifeos.player.domain.PlayerIdentity identity = identityRepository.findById(playerId).orElse(null);
        List<com.lifeos.player.domain.PlayerStatusFlag> statusFlags = flagRepository.findByPlayerPlayerId(playerId);
        List<com.lifeos.core.entity.TemporalModifier> temporalModifiers = temporalModifierRepository.findByPlayerPlayerId(playerId);
        List<com.lifeos.progression.domain.JobChangeQuest> jobQuests = jobChangeQuestRepository.findByPlayerPlayerId(playerId);

        List<HeatmapEntryDTO> heatmap = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(364);

        for (int i = 0; i < 365; i++) {
            LocalDate date = startDate.plusDays(i);
            List<Quest> questsOnDay = questsByDate.getOrDefault(date, Collections.emptyList());

            boolean isPenaltyLocked = statusFlags.stream().anyMatch(f -> f.getFlag() == com.lifeos.player.domain.enums.StatusFlagType.PENALTY_ZONE
                    && f.getAcquiredAt().toLocalDate().compareTo(date) <= 0 
                    && (f.getExpiresAt() == null || f.getExpiresAt().toLocalDate().compareTo(date) >= 0))
                || temporalModifiers.stream().anyMatch(m -> "PENALTY_ZONE".equals(m.getModifierType())
                    && m.getStartsAt().toLocalDate().compareTo(date) <= 0 
                    && m.getExpiresAt().toLocalDate().compareTo(date) >= 0);

            boolean isStealthPaused = statusFlags.stream().anyMatch(f -> f.getFlag() == com.lifeos.player.domain.enums.StatusFlagType.RECOVERING
                    && f.getAcquiredAt().toLocalDate().compareTo(date) <= 0 
                    && (f.getExpiresAt() == null || f.getExpiresAt().toLocalDate().compareTo(date) >= 0));

            boolean hasJobQuestOnDay = jobQuests.stream().anyMatch(jq -> jq.getAssignedAt() != null 
                    && jq.getAssignedAt().toLocalDate().equals(date));

            boolean isEventFrozen = (identity != null && identity.isRedGateActive() && LocalDate.now().equals(date))
                || questsOnDay.stream().anyMatch(q -> q.getQuestType() == com.lifeos.quest.domain.enums.QuestType.RED_GATE)
                || (identity != null && ("ACTIVE".equals(identity.getJobChangeStatus()) || "IN_GAUNTLET".equals(identity.getJobChangeStatus())) && LocalDate.now().equals(date))
                || hasJobQuestOnDay;

            String status;
            if (isPenaltyLocked) {
                status = "PENALTY_LOCKED";
            } else if (isStealthPaused) {
                status = "STEALTH_PAUSED";
            } else if (isEventFrozen) {
                status = "EVENT_FROZEN";
            } else if (questsOnDay.isEmpty()) {
                status = "NO_QUESTS";
            } else {
                long completedCount = questsOnDay.stream()
                        .filter(q -> q.getState() == QuestState.COMPLETED)
                        .count();
                if (completedCount == questsOnDay.size()) {
                    status = "ALL_CLEARED";
                } else if (completedCount > 0) {
                    status = "PARTIAL_CLEARED";
                } else {
                    status = "FAILED";
                }
            }
            heatmap.add(new HeatmapEntryDTO(date, status, true));
        }

        return heatmap;
    }

    public List<StatDataPointDTO> getStatGrowth(UUID playerId) {
        // Fetch player identity
        com.lifeos.player.domain.PlayerIdentity identity = identityRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        // Determine player lifetime dates
        LocalDate today = LocalDate.now();
        LocalDate startDate = identity.getCreatedAt() != null ? identity.getCreatedAt().toLocalDate() : today.minusDays(29);
        
        int totalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, today) + 1;
        totalDays = Math.max(1, totalDays);

        // Load the player's attributes
        List<PlayerAttribute> attributes = attributeRepository.findByPlayerPlayerId(playerId);
        Map<AttributeType, PlayerAttribute> attributeMap = attributes.stream()
                .collect(Collectors.toMap(PlayerAttribute::getAttributeType, a -> a, (a1, a2) -> a1));

        // Get lifetime trajectories
        double[] strTrajectory = getTrajectoryForAttribute(playerId, AttributeType.STR, attributeMap.get(AttributeType.STR), totalDays);
        double[] intTrajectory = getTrajectoryForAttribute(playerId, AttributeType.INT, attributeMap.get(AttributeType.INT), totalDays);
        double[] vitTrajectory = getTrajectoryForAttribute(playerId, AttributeType.VIT, attributeMap.get(AttributeType.VIT), totalDays);
        double[] agiTrajectory = getTrajectoryForAttribute(playerId, AttributeType.AGI, attributeMap.get(AttributeType.AGI), totalDays);
        double[] senTrajectory = getTrajectoryForAttribute(playerId, AttributeType.SEN, attributeMap.get(AttributeType.SEN), totalDays);

        // Fetch progression for current level
        com.lifeos.player.domain.PlayerProgression progression = progressionRepository.findByPlayerPlayerId(playerId).orElse(null);
        int currentLevel = progression != null ? progression.getLevel() : 10;
        int baseLevel = 10;
        int levelGrowth = currentLevel - baseLevel;

        int[] levelTrajectory = new int[totalDays];
        if (levelGrowth <= 0 || totalDays <= 1) {
            for (int i = 0; i < totalDays; i++) {
                levelTrajectory[i] = currentLevel;
            }
        } else {
            long seed = playerId.getLeastSignificantBits() ^ 99999L;
            Random rand = new Random(seed);
            double[] weights = new double[totalDays - 1];
            double totalWeight = 0;
            for (int i = 0; i < totalDays - 1; i++) {
                if (rand.nextDouble() < 0.25) {
                    weights[i] = 1.0 + rand.nextDouble();
                    totalWeight += weights[i];
                } else {
                    weights[i] = 0;
                }
            }
            if (totalWeight == 0) {
                weights[0] = 1.0;
                totalWeight = 1.0;
            }
            levelTrajectory[0] = baseLevel;
            for (int i = 1; i < totalDays; i++) {
                double progress = 0;
                for (int j = 0; j < i; j++) {
                    progress += weights[j];
                }
                levelTrajectory[i] = baseLevel + (int) Math.round((progress / totalWeight) * levelGrowth);
            }
            levelTrajectory[totalDays - 1] = currentLevel;
            for (int i = 1; i < totalDays; i++) {
                if (levelTrajectory[i] < levelTrajectory[i - 1]) {
                    levelTrajectory[i] = levelTrajectory[i - 1];
                }
                if (levelTrajectory[i] > currentLevel) {
                    levelTrajectory[i] = currentLevel;
                }
            }
        }

        // Fetch passed exams for rank transition milestone mapping
        List<com.lifeos.progression.domain.RankExamAttempt> passedExams = examAttemptRepository.findByPlayerPlayerId(playerId).stream()
                .filter(a -> a.getStatus() == com.lifeos.progression.domain.enums.ExamStatus.PASSED && a.getCompletedAt() != null)
                .sorted(Comparator.comparing(com.lifeos.progression.domain.RankExamAttempt::getCompletedAt))
                .collect(Collectors.toList());

        List<StatDataPointDTO> dataPoints = new ArrayList<>();
        for (int i = 0; i < totalDays; i++) {
            LocalDate date = startDate.plusDays(i);
            
            boolean isMilestone = false;
            String milestoneLabel = null;
            com.lifeos.player.domain.enums.PlayerRank currentRankOnDate = com.lifeos.player.domain.enums.PlayerRank.E;

            for (com.lifeos.progression.domain.RankExamAttempt exam : passedExams) {
                LocalDate examDate = exam.getCompletedAt().toLocalDate();
                if (examDate.compareTo(date) <= 0) {
                    currentRankOnDate = exam.getToRank();
                }
                if (examDate.equals(date)) {
                    isMilestone = true;
                    milestoneLabel = exam.getFromRank().name() + "→" + exam.getToRank().name() + " PROMOTION";
                }
            }

            dataPoints.add(new StatDataPointDTO(
                    date,
                    strTrajectory[i],
                    intTrajectory[i],
                    vitTrajectory[i],
                    agiTrajectory[i],
                    senTrajectory[i],
                    levelTrajectory[i],
                    currentRankOnDate.name(),
                    isMilestone,
                    milestoneLabel
            ));
        }

        return dataPoints;
    }

    private double[] getTrajectoryForAttribute(UUID playerId, AttributeType type, PlayerAttribute attr, int totalDays) {
        double baseVal = 10.0;
        double currentVal = 10.0;

        if (attr != null) {
            baseVal = attr.getBaseValue();
            currentVal = attr.getCurrentValue();
        }

        double growth = currentVal - baseVal;
        double[] values = new double[totalDays];

        if (growth <= 0 || totalDays <= 1) {
            for (int i = 0; i < totalDays; i++) {
                values[i] = currentVal;
            }
        } else {
            // Seeded random for deterministic but organic growth steps
            long seed = playerId.getLeastSignificantBits() ^ type.name().hashCode();
            Random rand = new Random(seed);
            double[] weights = new double[totalDays - 1];
            double totalWeight = 0;

            for (int i = 0; i < totalDays - 1; i++) {
                if (rand.nextDouble() < 0.35) { // 35% chance of growth step on any day
                    weights[i] = 0.5 + rand.nextDouble();
                    totalWeight += weights[i];
                } else {
                    weights[i] = 0;
                }
            }

            if (totalWeight == 0) {
                weights[0] = 1.0;
                totalWeight = 1.0;
            }

            values[0] = baseVal;
            for (int i = 1; i < totalDays; i++) {
                values[i] = values[i - 1] + (weights[i - 1] / totalWeight) * growth;
                // Round to one decimal place to make it look clean
                values[i] = Math.round(values[i] * 10.0) / 10.0;
            }
        }

        return values;
    }

    public List<GraveyardEntryDTO> getJournalGraveyard(UUID playerId) {
        List<PlayerJournal> journals = journalRepository.findByPlayerIdOrderByTimestampAsc(playerId);
        List<GraveyardEntryDTO> graveyard = new ArrayList<>();
        int currentStrikeCount = 0;
        for (PlayerJournal pj : journals) {
            if (!pj.isAccepted()) {
                currentStrikeCount++;
            }
            
            Integer lockoutDuration = null;
            if (currentStrikeCount >= 3) {
                lockoutDuration = 4; // 4 hour lockout
            }

            String entryType = "CONFESSION";
            if (pj.getText().contains("SURVIVAL_TASK") || pj.getText().contains("survival task")) {
                entryType = "SURVIVAL_TASK";
            } else if (pj.getText().contains("SYSTEM") || pj.getText().contains("system message")) {
                entryType = "SYSTEM_MESSAGE";
            }

            graveyard.add(new GraveyardEntryDTO(
                    pj.getId(),
                    pj.getText(),
                    pj.isAccepted(),
                    pj.getTimestamp(),
                    pj.getFeedback(),
                    currentStrikeCount,
                    lockoutDuration,
                    entryType
            ));

            if (pj.isAccepted()) {
                currentStrikeCount = 0; // reset strikes on acceptance
            }
        }
        return graveyard;
    }

    public List<DungeonGraveyardEntryDTO> getDungeonGraveyard(UUID playerId) {
        List<com.lifeos.project.domain.Project> projects = projectRepository.findAll().stream()
                .filter(p -> p.getPlayer().getPlayerId().equals(playerId))
                .filter(p -> p.getStatus() == com.lifeos.project.domain.enums.ProjectStatus.COMPLETED 
                        || p.getStatus() == com.lifeos.project.domain.enums.ProjectStatus.FAILED 
                        || p.getStatus() == com.lifeos.project.domain.enums.ProjectStatus.SHADOW 
                        || p.getStatus() == com.lifeos.project.domain.enums.ProjectStatus.PERMADEATH 
                        || p.getStatus() == com.lifeos.project.domain.enums.ProjectStatus.ABANDONED)
                .collect(Collectors.toList());

        List<DungeonGraveyardEntryDTO> graveyard = new ArrayList<>();
        for (com.lifeos.project.domain.Project p : projects) {
            int totalFloors = questRepository.findByProjectId(p.getProjectId()).size();
            if (totalFloors == 0) {
                totalFloors = p.getMinSubtasks();
            }
            int completedFloors = (int) questRepository.countByProjectIdAndState(p.getProjectId(), QuestState.COMPLETED);

            LocalDateTime completedAt = (p.getStatus() == com.lifeos.project.domain.enums.ProjectStatus.COMPLETED 
                    || p.getStatus() == com.lifeos.project.domain.enums.ProjectStatus.SHADOW) ? p.getCompletedAt() : null;

            LocalDateTime failedAt = (p.getStatus() == com.lifeos.project.domain.enums.ProjectStatus.FAILED 
                    || p.getStatus() == com.lifeos.project.domain.enums.ProjectStatus.PERMADEATH) 
                    ? (p.getCompletedAt() != null ? p.getCompletedAt() : p.getHardDeadline()) : null;

            LocalDateTime abandonedAt = (p.getStatus() == com.lifeos.project.domain.enums.ProjectStatus.ABANDONED) 
                    ? (p.getCompletedAt() != null ? p.getCompletedAt() : p.getLastActivityAt()) : null;

            graveyard.add(new DungeonGraveyardEntryDTO(
                    p.getProjectId().toString(),
                    p.getTitle(),
                    p.getDescription(),
                    p.getRankRequirement().name(),
                    p.getStatus().name(),
                    totalFloors,
                    completedFloors,
                    p.getCreatedAt(),
                    p.getHardDeadline(),
                    completedAt,
                    failedAt,
                    abandonedAt
            ));
        }
        return graveyard;
    }
}
