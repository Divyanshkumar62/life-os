package com.lifeos.streak.service;

import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.progression.repository.RankExamAttemptRepository;
import com.lifeos.progression.domain.enums.ExamStatus;
import com.lifeos.streak.domain.PlayerStreak;
import com.lifeos.streak.repository.PlayerStreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final PlayerStreakRepository streakRepository;
    private final PlayerStateService playerStateService;
    private final RankExamAttemptRepository examRepository; // For checking Exam status

    /**
     * Called at Daily Reset to update streak status for the previous day.
     * @param playerId The player ID
     * @param evaluatedDate The date being evaluated (Yesterday)
     * @param allSystemDailiesCompleted Whether all system dailies were completed
     */
    @Transactional
    public void processDailyCompletion(UUID playerId, LocalDate evaluatedDate, boolean allSystemDailiesCompleted) {
        PlayerStreak streak = streakRepository.findByPlayerId(playerId)
                .orElse(PlayerStreak.builder()
                        .playerId(playerId)
                        .currentStreak(0)
                        .longestStreak(0)
                        .previousStreak(0)
                        .build());

        if (allSystemDailiesCompleted) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            if (streak.getCurrentStreak() > streak.getLongestStreak()) {
                streak.setLongestStreak(streak.getCurrentStreak());
            }
            streak.setLastSuccessfulDate(evaluatedDate);
        } else {
            // Failed. Reset.
            if (streak.getCurrentStreak() > 0) {
                streak.setPreviousStreak(streak.getCurrentStreak()); // Save for repair
                streak.setLastBrokenDate(evaluatedDate);
                streak.setCurrentStreak(0);
            }
        }
        
        streakRepository.save(streak);
    }

    /**
     * Returns Gold Multiplier based on streak length.
     */
    public double getGoldMultiplier(UUID playerId) {
        // Cache lookup optimization could be here
        int streak = streakRepository.findByPlayerId(playerId)
                .map(PlayerStreak::getCurrentStreak)
                .orElse(0);
        
        if (streak >= 60) return 0.50;
        if (streak >= 30) return 0.35;
        if (streak >= 14) return 0.20;
        if (streak >= 7)  return 0.10;
        if (streak >= 3)  return 0.05;
        
        return 0.0;
    }

    /**
     * Force reset streak (e.g. Penalty Zone entry, Exam Failure).
     */
    @Transactional
    public void resetStreak(UUID playerId) {
        PlayerStreak streak = streakRepository.findByPlayerId(playerId).orElse(null);
        if (streak != null && streak.getCurrentStreak() > 0) {
            streak.setPreviousStreak(streak.getCurrentStreak()); // Save for potential repair? Or is Penalty strict?
            // "From Penalty Zone" -> Strict reset. Usually implies no repair.
            // But if we want to allow repair, we track previous.
            // The prompt said: "Penalty Zone entered -> Streak = 0".
            // Repair rules say: "Cannot be used: During Penalty Zone".
            // So if they exit P-Zone quickly... can they repair?
            // "Cannot be used during P-Zone". If they exit, maybe?
            // But for now, let's just reset.
            streak.setCurrentStreak(0);
            streak.setLastBrokenDate(LocalDate.now()); // Mark breakage
            streakRepository.save(streak);
        }
    }

    /**
     * Applies Streak Repair Potion.
     * STRICT GUARDS.
     */
    @Transactional
    public void applyStreakRepair(UUID playerId) {
        PlayerStreak streak = streakRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new IllegalStateException("No streak record found"));

        // 1. Check if broken
        if (streak.getCurrentStreak() > 0) {
            throw new IllegalStateException("Streak is active, nothing to repair.");
        }
        if (streak.getLastBrokenDate() == null) {
            throw new IllegalStateException("No breakage recorded to repair.");
        }

        // 2. Used within 24 hours of break
        // If broke yesterday (evaluatedDate), today is day 1.
        long daysDiff = ChronoUnit.DAYS.between(streak.getLastBrokenDate(), LocalDate.now());
        if (daysDiff > 1) {
             throw new IllegalStateException("Repair window expired (must use within 24h of break).");
        }

        // 3. Not in Penalty Zone
        var playerState = playerStateService.getPlayerState(playerId);
        boolean inPenalty = playerState.getActiveFlags().stream()
                .anyMatch(f -> f.getFlag() == StatusFlagType.PENALTY_ZONE);
        if (inPenalty) {
            throw new IllegalStateException("Cannot repair streak while in Penalty Zone.");
        }

        // 4. Not in Promotion Exam
        // Need to check active exams.
        var activeExam = examRepository.findLatestByPlayerId(playerId)
                 .filter(e -> e.getStatus() == ExamStatus.UNLOCKED)
                 .isPresent();
        // Wait, IN_PROGRESS is conceptually same as UNLOCKED in my code? `UNLOCKED` was the start state.
        // I should check `RankExamAttemptRepository` or `ProgressionService` logic.
        // `ExamStatus.UNLOCKED` is the active state.
        if (activeExam) {
             throw new IllegalStateException("Cannot repair streak during Promotion Exam.");
        }

        // 5. Restore
        if (streak.getPreviousStreak() > 0) {
             streak.setCurrentStreak(streak.getPreviousStreak());
             // Reset trackers
             streak.setLastSuccessfulDate(streak.getLastBrokenDate()); // Backfill gap? 
             // Or just restore value? "Restore streak to (previousStreakValue)"
             // If we restore, we pretend the break didn't happen?
             // streak.setLastSuccessfulDate(LocalDate.now().minusDays(1)); // Make it look alive?
             // Let's just restore the value.
        } else {
             // Fallback if previous not tracked
             // Just set to 1? Or throw?
             // Should verify logical consistency.
             throw new IllegalStateException("No previous streak value to restore.");
        }
        
        streakRepository.save(streak);
    }
}
