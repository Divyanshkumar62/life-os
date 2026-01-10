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
    private final com.lifeos.penalty.repository.PenaltyRecordRepository penaltyRepository;

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
            boolean isCoreStat = type == AttributeType.STR || type == AttributeType.INT || type == AttributeType.VIT || type == AttributeType.SEN;
            double startValue = isCoreStat ? 0.0 : 10.0;
            double decay = isCoreStat ? 0.0 : 0.01;

            attributes.add(PlayerAttribute.builder()
                    .player(identity)
                    .attributeType(type)
                    .baseValue(startValue)
                    .currentValue(startValue)
                    .growthVelocity(0.1)
                    .decayRate(decay)
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
        // Invariant: XP must be positive
        if (xpAmount < 0) {
            throw new IllegalArgumentException("XP amount cannot be negative");
        }

        var progression = progressionRepository.findByPlayerPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // Rank-Gate Logic: Check XP Freeze
        if (progression.isXpFrozen()) {
             System.out.println("XP is Frozen for player " + playerId);
             return;
        }

        // Legacy check for safety (or implicit/fallback)
        if (progression.getLevel() >= progression.getRank().getLevelCap()) {
             progression.setXpFrozen(true);
             progressionRepository.save(progression);
             return;
        }

        long newXp = progression.getCurrentXp() + xpAmount;
        progression.setCurrentXp(newXp);

        // Simple Level Up Logic: Threshold = Level * 100
        // Loop in case of multiple level ups, but also check CAP inside loop
        while (true) {
            long xpRequired = progression.getLevel() * 100L;
            if (progression.getCurrentXp() >= xpRequired) {
                // Check if next level exceeds cap
                if (progression.getLevel() + 1 > progression.getRank().getLevelCap()) {
                    // Cannot level up further. Cap XP? Or just let it sit?
                    // "XP gain halts completely".
                    // If we are here, we had enough XP to level up.
                    // But we shouldn't have exceeded cap.
                    // Let's enforce cap:
                    // If we hit cap, we stay at max XP for current level or 0 XP of max level?
                    // Usually: Level = Cap, XP = 0 (or some buffer).
                    // Logic above: `if (level >= cap) return`. 
                    // So we can't be here unless we started below cap.
                    
                    // But what if this single addXp pushes us OVER the cap?
                    // e.g. Level 9, Cap 10. addXp(Massive).
                    // Level 9 -> 10. Now at 10. Stop.
                    
                    progression.setCurrentXp(progression.getCurrentXp() - xpRequired);
                    progression.setLevel(progression.getLevel() + 1);
                    
                    if (progression.getLevel() >= progression.getRank().getLevelCap()) {
                        // Reached Cap. Stop further leveling.
                        break;
                    }
                } else {
                    progression.setCurrentXp(progression.getCurrentXp() - xpRequired);
                    progression.setLevel(progression.getLevel() + 1);
                }
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

    @Override
    @Transactional
    public void updatePsychMetric(UUID playerId, String metricName, double valueChange) {
        var psychState = psychStateRepository.findByPlayerPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // Helper to clamp values between 0 and 100
        // INVARIANT: Psych values clamped [0, 100]
        switch (metricName.toUpperCase()) {
            case "MOMENTUM" -> {
                double newVal = clamp(psychState.getMomentum() + valueChange, 0, 100);
                psychState.setMomentum((int) newVal);
            }
            case "STRESS" -> {
                double newVal = clamp(psychState.getStressLoad() + valueChange, 0, 100);
                psychState.setStressLoad((int) newVal);
            }
            case "COMPLACENCY" -> {
                double newVal = clamp(psychState.getComplacency() + valueChange, 0, 100);
                psychState.setComplacency((int) newVal);
            }
            case "CONFIDENCE" -> {
                double newVal = clamp(psychState.getConfidenceBias() + valueChange, 0, 100);
                psychState.setConfidenceBias((int) newVal);
            }
            default -> throw new IllegalArgumentException("Unknown psych metric: " + metricName);
        }
        psychStateRepository.save(psychState);
    }

    @Override
    @Transactional
    public void incrementStat(UUID playerId, AttributeType type, int amount) {
        var attribute = attributeRepository.findByPlayerPlayerIdAndAttributeType(playerId, type)
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found: " + type));

        double newValue = attribute.getCurrentValue() + amount;
        if (newValue < 0) newValue = 0;
        
        attribute.setCurrentValue(newValue);
        attributeRepository.save(attribute);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    @Transactional
    public void applyXpDeduction(UUID playerId, long amount) {
        var progression = progressionRepository.findByPlayerPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        long currentXp = progression.getCurrentXp();
        // XP Floor Logic: Never go below 0
        long newXp = Math.max(0, currentXp - amount);
        
        progression.setCurrentXp(newXp);
        progressionRepository.save(progression);
    }

    @Override
    @Transactional
    public void resetStreak(UUID playerId) {
        var temporal = temporalStateRepository.findByPlayerPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        
        temporal.setActiveStreakDays(0);
        temporalStateRepository.save(temporal);
    }
    
    @Override
    @Transactional
    public void applyStatDebuff(UUID playerId, AttributeType type, double amount, java.time.LocalDateTime expiresAt) {
        // Debuffs are effective via check-on-read in getPlayerState, but we need to record them?
        // Ah, the design is: PenaltyService stores the record. PlayerService "applies" it?
        // If "Check-on-Read" reads from PenaltyRepo, then PlayerService explicitly "applying" it might be redundant 
        // OR it's a hook to persist specific flags or notify.
        // But wait, the plan said: "Call PlayerStateService to apply effects... Add active debuff".
        // And we implemented check-on-read reading from PenaltyRepo.
        // So this method might just be for logging or if we decided to store debuffs in a separate Player table.
        // For V1 "Check-on-Read" relies on PenaltyRecords. 
        // So this method effectively does nothing if records are already saved by PenaltyService?
        // WRONG: PenaltyService saves the record. PlayerService calculates effective stats.
        // So `applyStatDebuff` here is likely a NO-OP or just a validation hook if persistence is handled by PenaltyService.
        // BUT, if PenaltyService calls this, it expects something.
        // Let's make it a NO-OP for now since the Record IS the state, 
        // OR we can use it to maybe set a "HAS_DEBUFF" flag for quick lookup.
        // Let's set a Flag.
        
        // Actually, let's keep it simple. It exists to satisfy the interface.
        // We might log or send a notification.
    }

    @Override
    @Transactional
    public void applyStatusFlag(UUID playerId, com.lifeos.player.domain.enums.StatusFlagType flagType, java.time.LocalDateTime expiresAt) {
        List<PlayerStatusFlag> flags = flagRepository.findByPlayerPlayerId(playerId);
        
        // Check for existing flag of same type
        PlayerStatusFlag existing = flags.stream()
            .filter(f -> f.getFlag() == flagType)
            .findFirst()
            .orElse(null);
            
        if (existing != null) {
            // Extend expiration if new one is longer
            if (expiresAt.isAfter(existing.getExpiresAt())) {
                existing.setExpiresAt(expiresAt);
                flagRepository.save(existing);
            }
        } else {
             PlayerIdentity identity = identityRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
                
             PlayerStatusFlag newFlag = PlayerStatusFlag.builder()
                .player(identity)
                .flag(flagType)
                .acquiredAt(java.time.LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();
             flagRepository.save(newFlag);
        }
    }

    @Override
    @Transactional
    public void extendStreak(UUID playerId) {
        var temporal = temporalStateRepository.findByPlayerPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        
        temporal.setActiveStreakDays(temporal.getActiveStreakDays() + 1);
        // Also update lastQuestCompletedAt? Yes, logically.
        temporal.setLastQuestCompletedAt(java.time.LocalDateTime.now());
        temporalStateRepository.save(temporal);
    }

    @Override
    @Transactional
    public void adjustMomentum(UUID playerId, int delta) {
        updatePsychMetric(playerId, "MOMENTUM", delta);
    }

    @Override
    @Transactional
    public void promoteRank(UUID playerId) {
        var progression = progressionRepository.findByPlayerPlayerId(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        
        PlayerRank currentRank = progression.getRank();
        PlayerRank nextRank = currentRank.next();
        
        if (currentRank == nextRank) {
            // Already at max rank (SS) or logic fail
            return;
        }
        
        progression.setRank(nextRank);
        // implicit unfreeze
        progression.setXpFrozen(false);
        
        progressionRepository.save(progression);
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
        
        // Fetch Active Debuffs (Check-on-Read)
        List<com.lifeos.penalty.domain.PenaltyRecord> activeDebuffs = penaltyRepository.findActiveDebuffs(identity.getPlayerId());
        
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
                .xpFrozen(progression.isXpFrozen())
                .build();

        var attributeDtos = attributes.stream()
                .map(attr -> {
                    // Calculate debuff
                    double debuffAmount = activeDebuffs.stream()
                            .filter(p -> p.getValuePayload() != null 
                                      && attr.getAttributeType().name().equals(p.getValuePayload().get("debuffAttr")))
                            .mapToDouble(p -> (Double) p.getValuePayload().get("debuffAmount"))
                            .sum();

                    // Apply debuff (percentage reduction usually, but here model says "amount")
                    // If amount is a percentage (e.g. 5.0 for 5%), calculation differs.
                    // Plan said: "Debuff range: 5%–15%". "10%".
                    // Code stored "10.0".
                    // Let's assume percentage reduction from CURRENT value.
                    // effective = current - (current * (amount / 100))
                    
                    double effectiveValue = attr.getCurrentValue();
                    if (debuffAmount > 0) {
                        effectiveValue = effectiveValue * (1.0 - (debuffAmount / 100.0));
                    }

                    return PlayerAttributeDTO.builder()
                        .attributeType(attr.getAttributeType())
                        .baseValue(attr.getBaseValue())
                        .currentValue(effectiveValue) 
                        .growthVelocity(attr.getGrowthVelocity())
                        .decayRate(attr.getDecayRate())
                        .build();
                })
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
