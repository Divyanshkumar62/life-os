package com.lifeos.core.service;

import com.lifeos.core.dto.SurvivalTaskSubmitRequest;
import com.lifeos.core.dto.SurvivalTaskSubmitResponse;
import com.lifeos.core.dto.TaskRerollResponse;
import com.lifeos.core.entity.PlayerState;
import com.lifeos.core.entity.TemporalModifier;
import com.lifeos.core.repository.PlayerStateRepository;
import com.lifeos.core.repository.TemporalModifierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service("corePenaltyService")
@RequiredArgsConstructor
public class PenaltyService {

    private final PlayerStateRepository playerStateRepository;
    private final TemporalModifierRepository temporalModifierRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SurvivalTaskSubmitResponse submitSurvivalTask(SurvivalTaskSubmitRequest request) {
        // Pessimistic Write Lock player state
        PlayerState player = playerStateRepository.findAndLockById(request.getPlayerId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + request.getPlayerId()));

        // Check if player is in Penalty Zone
        List<TemporalModifier> activeModifiers = temporalModifierRepository
                .findByPlayerPlayerIdAndIsActive(player.getPlayerId(), true);

        TemporalModifier penaltyModifier = activeModifiers.stream()
                .filter(m -> "PENALTY_ZONE".equals(m.getModifierType()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Player is not in the Penalty Zone"));

        // Deactivate penalty modifier
        penaltyModifier.setActive(false);
        temporalModifierRepository.save(penaltyModifier);

        return SurvivalTaskSubmitResponse.builder()
                .playerId(player.getPlayerId())
                .status("CLEARED")
                .escaped(true)
                .escapedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TaskRerollResponse rerollTask(UUID playerId, String reason) {
        // Pessimistic Write Lock player state
        PlayerState player = playerStateRepository.findAndLockById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        // Check if player is in Penalty Zone
        List<TemporalModifier> activeModifiers = temporalModifierRepository
                .findByPlayerPlayerIdAndIsActive(player.getPlayerId(), true);

        activeModifiers.stream()
                .filter(m -> "PENALTY_ZONE".equals(m.getModifierType()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Player is not in the Penalty Zone to reroll task"));

        // Deduct 10% Gold unmitigated (minimum 100 Gold, pushing to debt if needed)
        long currentGold = player.getGoldBalance();
        long goldDeducted = Math.max(currentGold / 10, 100);
        if (currentGold >= goldDeducted) {
            player.setGoldBalance(currentGold - goldDeducted);
        } else {
            player.setGoldBalance(0);
            player.setGoldDebt(player.getGoldDebt() + (goldDeducted - currentGold));
        }
        playerStateRepository.save(player);

        // Generate a new survival task
        UUID newTaskId = UUID.randomUUID();
        String description = "Perform 300 Squats indoors (Rerolled due to: " + reason + ")";

        return TaskRerollResponse.builder()
                .playerId(playerId)
                .goldDeducted(goldDeducted)
                .remainingGold(player.getGoldBalance())
                .newSurvivalTask(TaskRerollResponse.NewSurvivalTask.builder()
                        .survivalTaskId(newTaskId)
                        .description(description)
                        .timeLimitHours(24)
                        .build())
                .build();
    }
}
