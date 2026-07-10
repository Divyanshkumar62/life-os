package com.lifeos.core.service;

import com.lifeos.core.entity.PlayerState;
import com.lifeos.core.entity.TemporalModifier;
import com.lifeos.core.repository.PlayerStateRepository;
import com.lifeos.core.repository.TemporalModifierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DailyResetProcessorService {

    private final PlayerStateRepository playerStateRepository;
    private final TemporalModifierRepository temporalModifierRepository;
    private final AnalyticsService analyticsService;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void resetPlayer(UUID playerId) {
        // Pessimistic Write Lock player state
        PlayerState player = playerStateRepository.findAndLockById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        // Check if player has active EVENT_FROZEN temporal modifier
        List<TemporalModifier> activeModifiers = temporalModifierRepository
                .findByPlayerPlayerIdAndIsActive(playerId, true);

        boolean isFrozen = activeModifiers.stream()
                .anyMatch(m -> "EVENT_FROZEN".equals(m.getModifierType()));
        if (isFrozen) {
            // Event Frozen: skip resets (e.g. Crucible or Job Change in progress)
            return;
        }

        // Evaluate daily quest status.
        boolean failedDailies = playerStateRepository.hasFailedActiveDailies(playerId);

        if (failedDailies) {
            // Insert a new TemporalModifier of type PENALTY_ZONE
            TemporalModifier penalty = TemporalModifier.builder()
                    .player(player)
                    .modifierType("PENALTY_ZONE")
                    .startsAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .isActive(true)
                    .build();
            temporalModifierRepository.save(penalty);

            // Apply unmitigated gold penalty and accumulate to debt if balance runs dry
            long balance = player.getGoldBalance();
            double vitMitigation = Math.min((double) player.getStatVit() / 100.0, 0.50);
            double drainPercentage = 0.20 * (1.0 - vitMitigation);
            long penaltyDeduction = Math.max((long) (balance * drainPercentage), 500);
            
            if (balance >= penaltyDeduction) {
                player.setGoldBalance(balance - penaltyDeduction);
            } else {
                player.setGoldBalance(0);
                player.setGoldDebt(player.getGoldDebt() + (penaltyDeduction - balance));
            }
            playerStateRepository.save(player);

            // Write PENALTY_LOCKED status to Redis heatmap
            analyticsService.updateHeatmap(playerId, LocalDate.now(), "PENALTY_LOCKED");
        } else {
            // Success status write-through update
            analyticsService.updateHeatmap(playerId, LocalDate.now(), "ALL_CLEARED");
        }
    }
}
