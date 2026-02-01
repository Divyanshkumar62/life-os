package com.lifeos;

import com.lifeos.event.concrete.PenaltyZoneEnteredEvent;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.PlayerProgression;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.player.repository.PlayerProgressionRepository;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.player.state.PlayerFlag;
import com.lifeos.player.state.PlayerStateRepository;
import com.lifeos.progression.domain.RankExamAttempt;
import com.lifeos.progression.domain.enums.ExamStatus;
import com.lifeos.progression.repository.RankExamAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class SystemLawsIntegrationTest {

    @Autowired
    private PlayerStateService playerService;

    @Autowired
    private PlayerProgressionRepository progressionRepository;
    
    @Autowired
    private PlayerStateRepository stateRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private PlayerIdentityRepository playerRepository;

    @Autowired
    private RankExamAttemptRepository examRepository;

    @BeforeEach
    void setUp() {
        // Apply H2 Triggers explicitly if not using @Sql
        jdbcTemplate.execute("RUNSCRIPT FROM 'classpath:db/test-laws-h2.sql'");
    }

    @Test
    void testLaw1_PenaltyEmbargo_PreventsXpIncrease() {
        // 1. Setup Player
        UUID playerId = createPlayer("LawBreaker1");
        
        // 2. Initial XP
        playerService.addXp(playerId, 100); 

        // 3. Enter Penalty using Event
        eventPublisher.publishEvent(new PenaltyZoneEnteredEvent(playerId));

        // 4. Attempt to Add XP -> Should Fail via DB Trigger
        Exception exception = assertThrows(Exception.class, () -> {
             playerService.addXp(playerId, 50);
        });
        
        System.out.println("CAUGHT EXCEPTION: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("Law 1 Violation") 
                || (exception.getCause() != null && exception.getCause().getMessage().contains("Law 1 Violation")));
    }

    @Test
    void testLaw2_RankCeiling_PreventsOverLeveling() {
         // 1. Setup Player
        UUID playerId = createPlayer("LawBreaker2");
        
        // 2. Set Rank F (Cap 10)
        PlayerProgression prog = progressionRepository.findByPlayerPlayerId(playerId).orElseThrow();
        prog.setRank(PlayerRank.F);
        prog.setLevel(10);
        progressionRepository.save(prog);

        // 3. Attempt to set Level 11 -> Should Fail
        Exception exception = assertThrows(Exception.class, () -> {
             prog.setLevel(11);
             progressionRepository.saveAndFlush(prog);
        });
        
        assertTrue(exception.getMessage().contains("Law 2 Violation") 
             || (exception.getCause() != null && exception.getCause().getMessage().contains("Law 2 Violation")));
    }
    
    @Test
    void testLaw4_StateExclusivity_BlocksPromotionDuringPenalty() {
        // 1. Setup Player
        UUID playerId = createPlayer("LawBreaker4");
        PlayerIdentity player = playerRepository.findByUsername("LawBreaker4").get();
        
        // 2. Enter Penalty
        eventPublisher.publishEvent(new PenaltyZoneEnteredEvent(playerId));
        
        // 3. Attempt to Start Promotion Exam (Insert Attempt) -> Should Fail
        RankExamAttempt attempt = RankExamAttempt.builder()
                .player(player)
                .fromRank(PlayerRank.F)
                .toRank(PlayerRank.E)
                .status(ExamStatus.UNLOCKED)
                .requiredKeys(1)
                .consumedKeys(1)
                .attemptNumber(1)
                .build();
                
        Exception exception = assertThrows(Exception.class, () -> {
             examRepository.saveAndFlush(attempt);
        });
        
        assertTrue(exception.getMessage().contains("Law 4 Violation") 
             || (exception.getCause() != null && exception.getCause().getMessage().contains("Law 4 Violation")));
    }
    
    private UUID createPlayer(String username) {
        // Handle existing
        Optional<PlayerIdentity> existing = playerRepository.findByUsername(username);
        if (existing.isPresent()) return existing.get().getPlayerId();
        
        playerService.initializePlayer(username);
        return playerRepository.findByUsername(username).get().getPlayerId();
    }
}
