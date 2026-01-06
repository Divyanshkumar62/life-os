package com.lifeos.player.service;

import com.lifeos.player.domain.*;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.dto.*;
import com.lifeos.player.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerStateServiceImpl implements PlayerStateService {

    private final PlayerIdentityRepository identityRepository;
    private final PlayerProgressionRepository progressionRepository;
    private final PlayerAttributeRepository attributeRepository;
    private final PlayerPsychStateRepository psychStateRepository;
    private final PlayerMetricsRepository metricsRepository;
    private final PlayerStatusFlagRepository flagRepository;
    private final PlayerTemporalStateRepository temporalStateRepository;
    private final PlayerHistoryRepository historyRepository;

    @Override
    @Transactional
    public PlayerStateResponse initializePlayer(String username) {
        // Validation: Check if username exists
        if (identityRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Create Identity
        PlayerIdentity identity = PlayerIdentity.builder()
                .username(username)
                .systemVersion("v1")
                .build();
        identity = identityRepository.save(identity);

        // Create Progression
        PlayerProgression progression = PlayerProgression.builder()
                .player(identity)
                .level(1)
                .currentXp(0)
                .rank(PlayerRank.F)
                .rankProgressScore(0.0)
                .build();
        progressionRepository.save(progression);

        // Create Attributes (Default set)
        List<PlayerAttribute> attributes = new ArrayList<>();
        for (AttributeType type : AttributeType.values()) {
            attributes.add(PlayerAttribute.builder()
                    .player(identity)
                    .attributeType(type)
                    .baseValue(10.0)
                    .currentValue(10.0)
                    .growthVelocity(0.1)
                    .decayRate(0.01)
                    .build());
        }
        attributeRepository.saveAll(attributes);

        // Create Psych State
        PlayerPsychState psychState = PlayerPsychState.builder()
                .player(identity)
                .momentum(50)
                .complacency(0)
                .stressLoad(0)
                .confidenceBias(50)
                .build();
        psychStateRepository.save(psychState);

        // Create Metrics
        PlayerMetrics metrics = PlayerMetrics.builder()
                .player(identity)
                .questSuccessRate(0.0)
                .averageQuestDifficulty(0.0)
                .failureStreak(0)
                .recoveryRate(1.0)
                .build();
        metricsRepository.save(metrics);

        // Create Temporal State
        PlayerTemporalState temporalState = PlayerTemporalState.builder()
                .player(identity)
                .lastQuestCompletedAt(null)
                .activeStreakDays(0)
                .restDebt(0.0)
                .burnoutRiskScore(0.0)
                .build();
        temporalStateRepository.save(temporalState);

        // Create History
        PlayerHistory history = PlayerHistory.builder()
                .player(identity)
                .lastEgoBreakerAt(null)
                .completedQuests(new ArrayList<>())
                .failedQuests(new ArrayList<>())
                .notableEvents(new ArrayList<>())
                .build();
        historyRepository.save(history);

        return getPlayerState(identity.getPlayerId());
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerStateResponse getPlayerState(UUID playerId) {
        PlayerIdentity identity = identityRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        return buildResponse(identity);
    }

    @Override
    @Transactional
    public void addXp(UUID playerId, long xpAmount) {
        var progression = progressionRepository.findByPlayerPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        long newXp = progression.getCurrentXp() + xpAmount;
        progression.setCurrentXp(newXp);

        // Simple Level Up Logic: Threshold = Level * 100
        // Loop in case of multiple level ups
        while (true) {
            long xpRequired = progression.getLevel() * 100L;
            if (progression.getCurrentXp() >= xpRequired) {
                progression.setCurrentXp(progression.getCurrentXp() - xpRequired);
                progression.setLevel(progression.getLevel() + 1);
                // TODO: Trigger level up event/notification
            } else {
                break;
            }
        }

        progressionRepository.save(progression);
    }

    @Override
    @Transactional
    public void updateAttribute(UUID playerId, AttributeType type, double valueChange) {
        var attribute = attributeRepository.findByPlayerPlayerIdAndAttributeType(playerId, type)
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found: " + type));

        double newValue = attribute.getCurrentValue() + valueChange;
        // Ensure non-negative? Design says "can temporarily drop", implies it exists.
        // Assuming min value 0 for sanity.
        if (newValue < 0) {
            newValue = 0;
        }
        attribute.setCurrentValue(newValue);
        attributeRepository.save(attribute);
    }

    private PlayerStateResponse buildResponse(PlayerIdentity identity) {
        // Fetch all components
        var progression = progressionRepository.findByPlayerPlayerId(identity.getPlayerId()).orElseThrow();
        var attributes = attributeRepository.findByPlayerPlayerId(identity.getPlayerId());
        var psychState = psychStateRepository.findByPlayerPlayerId(identity.getPlayerId()).orElseThrow();
        var metrics = metricsRepository.findByPlayerPlayerId(identity.getPlayerId()).orElseThrow();
        var flags = flagRepository.findByPlayerPlayerId(identity.getPlayerId());
        var temporal = temporalStateRepository.findByPlayerPlayerId(identity.getPlayerId()).orElseThrow();
        var history = historyRepository.findByPlayerPlayerId(identity.getPlayerId()).orElseThrow();

        // Convert to DTOs
        var identityDto = PlayerIdentityDTO.builder()
                .playerId(identity.getPlayerId())
                .username(identity.getUsername())
                .createdAt(identity.getCreatedAt())
                .systemVersion(identity.getSystemVersion())
                .build();

        var progressionDto = PlayerProgressionDTO.builder()
                .level(progression.getLevel())
                .currentXp(progression.getCurrentXp())
                .rank(progression.getRank())
                .rankProgressScore(progression.getRankProgressScore())
                .build();

        var attributeDtos = attributes.stream()
                .map(attr -> PlayerAttributeDTO.builder()
                        .attributeType(attr.getAttributeType())
                        .baseValue(attr.getBaseValue())
                        .currentValue(attr.getCurrentValue())
                        .growthVelocity(attr.getGrowthVelocity())
                        .decayRate(attr.getDecayRate())
                        .build())
                .collect(Collectors.toList());

        var psychStateDto = PlayerPsychStateDTO.builder()
                .momentum(psychState.getMomentum())
                .complacency(psychState.getComplacency())
                .stressLoad(psychState.getStressLoad())
                .confidenceBias(psychState.getConfidenceBias())
                .build();

        var metricsDto = PlayerMetricsDTO.builder()
                .questSuccessRate(metrics.getQuestSuccessRate())
                .averageQuestDifficulty(metrics.getAverageQuestDifficulty())
                .failureStreak(metrics.getFailureStreak())
                .recoveryRate(metrics.getRecoveryRate())
                .build();

        var flagDtos = flags.stream()
                .map(flag -> PlayerStatusFlagDTO.builder()
                        .flag(flag.getFlag())
                        .acquiredAt(flag.getAcquiredAt())
                        .expiresAt(flag.getExpiresAt())
                        .build())
                .collect(Collectors.toList());

        var temporalDto = PlayerTemporalStateDTO.builder()
                .lastQuestCompletedAt(temporal.getLastQuestCompletedAt())
                .activeStreakDays(temporal.getActiveStreakDays())
                .restDebt(temporal.getRestDebt())
                .burnoutRiskScore(temporal.getBurnoutRiskScore())
                .build();

        var historyDto = PlayerHistoryDTO.builder()
                .lastEgoBreakerAt(history.getLastEgoBreakerAt())
                .completedQuests(history.getCompletedQuests())
                .failedQuests(history.getFailedQuests())
                .notableEvents(history.getNotableEvents())
                .build();

        return PlayerStateResponse.builder()
                .identity(identityDto)
                .progression(progressionDto)
                .attributes(attributeDtos)
                .psychState(psychStateDto)
                .metrics(metricsDto)
                .activeFlags(flagDtos)
                .temporalState(temporalDto)
                .history(historyDto)
                .build();
    }
}
