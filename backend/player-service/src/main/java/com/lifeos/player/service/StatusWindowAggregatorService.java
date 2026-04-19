package com.lifeos.player.service;

import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.PlayerStatusFlag;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.dto.StatusWindowResponse;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.repository.PlayerStatusFlagRepository;
import com.lifeos.player.repository.PlayerProgressionRepository;
import com.lifeos.player.repository.PlayerAttributeRepository;
import com.lifeos.player.domain.PlayerProgression;
import com.lifeos.player.domain.PlayerAttribute;
import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.onboarding.domain.PlayerProfile;
import com.lifeos.onboarding.repository.PlayerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatusWindowAggregatorService {

    private final PlayerIdentityRepository identityRepository;
    private final PlayerProfileRepository profileRepository;
    private final PlayerProgressionRepository progressionRepository;
    private final PlayerAttributeRepository attributeRepository;
    private final PlayerStatusFlagRepository statusFlagRepository;

    @Transactional(readOnly = true)
    public StatusWindowResponse buildStatusWindow(UUID playerId) {
        log.debug("Aggregating Status Window for player: {}", playerId);

        PlayerIdentity identity = identityRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player identity not found: " + playerId));

        PlayerProfile profile = profileRepository.findByPlayerId(playerId)
                .orElse(null);

        PlayerProgression progression = progressionRepository.findByPlayerPlayerId(playerId)
                .orElse(null);

        List<PlayerAttribute> attributes = attributeRepository.findByPlayerPlayerId(playerId);
        
        long goldAmount = progression != null ? progression.getGold() : 0L;

        List<PlayerStatusFlag> activeFlags = statusFlagRepository.findByPlayerPlayerIdAndExpiresAtAfter(playerId, LocalDateTime.now());
        
        boolean penaltyActive = activeFlags.stream()
                .anyMatch(flag -> flag.getFlag() == StatusFlagType.PENALTY_ZONE);
                
        List<String> activeBuffs = activeFlags.stream()
                .filter(flag -> flag.getFlag() != StatusFlagType.PENALTY_ZONE && flag.getFlag() != StatusFlagType.WARNING)
                .map(flag -> flag.getFlag().name())
                .collect(Collectors.toList());

        int level = progression != null ? progression.getLevel() : 1;
        String rank = progression != null && progression.getRank() != null ? progression.getRank().name() : "F";
        long currentXp = progression != null ? progression.getCurrentXp() : 0L;
        long maxXpForLevel = calculateMaxXp(level);
        
        int str = getAttributeValue(attributes, AttributeType.STR);
        int intel = getAttributeValue(attributes, AttributeType.INT);
        int vit = getAttributeValue(attributes, AttributeType.VIT);
        int sen = getAttributeValue(attributes, AttributeType.SEN);
        int freePoints = progression != null ? progression.getFreeStatPoints() : 0;

        String title = profile != null && profile.getTitle() != null ? profile.getTitle() : "None";
        String theme = profile != null && profile.getDisplayTheme() != null ? profile.getDisplayTheme() : "shadow_purple";

        return StatusWindowResponse.builder()
                .identity(StatusWindowResponse.Identity.builder()
                        .level(level)
                        .rank(rank)
                        .title(title)
                        .equippedTheme(theme)
                        .build())
                .progression(StatusWindowResponse.Progression.builder()
                        .currentXp(currentXp)
                        .maxXpForLevel(maxXpForLevel)
                        .build())
                .attributes(StatusWindowResponse.Attributes.builder()
                        .STR(str)
                        .INT(intel)
                        .VIT(vit)
                        .SEN(sen)
                        .freePoints(freePoints)
                        .build())
                .economy(StatusWindowResponse.Economy.builder()
                        .gold(goldAmount)
                        .build())
                .systemState(StatusWindowResponse.SystemState.builder()
                        .penaltyActive(penaltyActive)
                        .activeBuffs(activeBuffs)
                        .build())
                .build();
    }

    private int getAttributeValue(List<PlayerAttribute> attributes, AttributeType type) {
        return attributes.stream()
                .filter(a -> a.getAttributeType() == type)
                .mapToInt(a -> (int) a.getCurrentValue())
                .findFirst()
                .orElse(10);
    }

    private long calculateMaxXp(int level) {
        return (long) (100 * Math.pow(1.15, level - 1));
    }
}
