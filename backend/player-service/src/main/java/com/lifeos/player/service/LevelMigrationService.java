package com.lifeos.player.service;

import com.lifeos.player.domain.PlayerProgression;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.repository.PlayerProgressionRepository;
import com.lifeos.voice.domain.enums.SystemMessageType;
import com.lifeos.voice.event.VoiceSystemEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LevelMigrationService {

    private final PlayerProgressionRepository progressionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void recalculateAllPlayers() {
        List<PlayerProgression> allPlayers = progressionRepository.findAll();
        
        for (PlayerProgression p : allPlayers) {
            recalculatePlayer(p);
        }
    }

    private void recalculatePlayer(PlayerProgression p) {
        // 1. Backfill Total XP if missing (assuming 0 means missing or new player)
        // If system is new, totalXP tracks correctly.
        // If legacy, totalXP is 0 but Level > 1.
        if (p.getTotalXpAccumulated() == 0 && p.getLevel() > 1) {
            long legacyTotal = calculateLegacyTotalXp(p.getLevel(), p.getCurrentXp());
            p.setTotalXpAccumulated(legacyTotal);
        }
        
        // 2. Recalculate Level based on Total XP and New Curve
        // New Curve: Level L requires Sum(1..L-1) [ 100 * 1.1^k ]
        
        long totalXp = p.getTotalXpAccumulated();
        int newLevel = 1;
        long xpForNextLevel = calculateXpForLevel(newLevel); // XP needed to go 1 -> 2
        
        while (totalXp >= xpForNextLevel) {
            // Check Rank Cap
            if (newLevel + 1 > p.getRank().getLevelCap()) {
                // Cap reached
                p.setXpFrozen(true);
                break;
            }
            
            totalXp -= xpForNextLevel;
            newLevel++;
            xpForNextLevel = calculateXpForLevel(newLevel);
        }
        
        // 3. Update State
        int oldLevel = p.getLevel();
        p.setLevel(newLevel);
        p.setCurrentXp(totalXp); // Remaining XP
        
        progressionRepository.save(p);
        
        // 4. Notify
        if (oldLevel != newLevel) {
             eventPublisher.publishEvent(VoiceSystemEvent.builder()
                .playerId(p.getPlayer().getPlayerId())
                .type(SystemMessageType.SYSTEM_NOTICE)
                .payload(java.util.Map.of(
                    "message", "Physics recalibrated.\nLevel adjusted: " + oldLevel + " -> " + newLevel
                ))
                .build());
        }
    }

    private long calculateLegacyTotalXp(int level, long currentXp) {
        // Old Formula: Level L required L*100 XP to complete.
        // Total = Sum(1 to Level-1) [ i * 100 ] + currentXp
        // Sum(1..N) = N*(N+1)/2
        // N = level - 1
        long n = level - 1;
        long sumOfLevels = n * (n + 1) / 2;
        return (sumOfLevels * 100) + currentXp;
    }

    private long calculateXpForLevel(int level) {
         // Formula to complete Level L (to get to L+1): 100 * (1.1 ^ L)
         return (long) (100 * Math.pow(1.1, level));
    }
}
