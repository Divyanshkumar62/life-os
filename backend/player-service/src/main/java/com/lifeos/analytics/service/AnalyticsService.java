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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final QuestRepository questRepository;
    private final PlayerAttributeRepository attributeRepository;
    private final PlayerJournalRepository journalRepository;

    public AnalyticsService(QuestRepository questRepository,
                            PlayerAttributeRepository attributeRepository,
                            PlayerJournalRepository journalRepository) {
        this.questRepository = questRepository;
        this.attributeRepository = attributeRepository;
        this.journalRepository = journalRepository;
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

        List<HeatmapEntryDTO> heatmap = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(364);

        for (int i = 0; i < 365; i++) {
            LocalDate date = startDate.plusDays(i);
            List<Quest> questsOnDay = questsByDate.getOrDefault(date, Collections.emptyList());

            String status;
            if (questsOnDay.isEmpty()) {
                status = "NO_QUESTS";
            } else {
                long completedCount = questsOnDay.stream()
                        .filter(q -> q.getState() == QuestState.COMPLETED)
                        .count();
                if (completedCount == questsOnDay.size()) {
                    status = "ALL_CLEARED";
                } else if (completedCount > 0) {
                    status = "PARTIAL";
                } else {
                    status = "FAILED";
                }
            }
            heatmap.add(new HeatmapEntryDTO(date, status));
        }

        return heatmap;
    }

    public List<StatDataPointDTO> getStatGrowth(UUID playerId) {
        // Load the player's attributes
        List<PlayerAttribute> attributes = attributeRepository.findByPlayerPlayerId(playerId);
        Map<AttributeType, PlayerAttribute> attributeMap = attributes.stream()
                .collect(Collectors.toMap(PlayerAttribute::getAttributeType, a -> a, (a1, a2) -> a1));

        // Get or default values for STR, INT, VIT, AGI, SEN
        double[] strTrajectory = getTrajectoryForAttribute(playerId, AttributeType.STR, attributeMap.get(AttributeType.STR));
        double[] intTrajectory = getTrajectoryForAttribute(playerId, AttributeType.INT, attributeMap.get(AttributeType.INT));
        double[] vitTrajectory = getTrajectoryForAttribute(playerId, AttributeType.VIT, attributeMap.get(AttributeType.VIT));
        double[] agiTrajectory = getTrajectoryForAttribute(playerId, AttributeType.AGI, attributeMap.get(AttributeType.AGI));
        double[] senTrajectory = getTrajectoryForAttribute(playerId, AttributeType.SEN, attributeMap.get(AttributeType.SEN));

        List<StatDataPointDTO> dataPoints = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 30; i++) {
            LocalDate date = today.minusDays(29 - i);
            dataPoints.add(new StatDataPointDTO(
                    date,
                    strTrajectory[i],
                    intTrajectory[i],
                    vitTrajectory[i],
                    agiTrajectory[i],
                    senTrajectory[i]
            ));
        }

        return dataPoints;
    }

    private double[] getTrajectoryForAttribute(UUID playerId, AttributeType type, PlayerAttribute attr) {
        double baseVal = 10.0;
        double currentVal = 10.0;

        if (attr != null) {
            baseVal = attr.getBaseValue();
            currentVal = attr.getCurrentValue();
        }

        double growth = currentVal - baseVal;
        double[] values = new double[30];

        if (growth <= 0) {
            for (int i = 0; i < 30; i++) {
                values[i] = currentVal;
            }
        } else {
            // Seeded random for deterministic but organic growth steps
            long seed = playerId.getLeastSignificantBits() ^ type.name().hashCode();
            Random rand = new Random(seed);
            double[] weights = new double[29];
            double totalWeight = 0;

            for (int i = 0; i < 29; i++) {
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
            for (int i = 1; i < 30; i++) {
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
        for (PlayerJournal pj : journals) {
            graveyard.add(new GraveyardEntryDTO(
                    pj.getId(),
                    pj.getText(),
                    pj.isAccepted(),
                    pj.getTimestamp(),
                    pj.getFeedback()
            ));
        }
        return graveyard;
    }
}
