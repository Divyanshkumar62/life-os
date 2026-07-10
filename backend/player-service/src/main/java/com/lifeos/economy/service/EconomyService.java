package com.lifeos.economy.service;

import com.lifeos.economy.domain.PlayerEconomy;
import com.lifeos.economy.repository.PlayerEconomyRepository;
import com.lifeos.player.repository.PlayerProgressionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EconomyService {

    private final PlayerEconomyRepository economyRepository;
    private final com.lifeos.player.repository.PlayerIdentityRepository playerIdentityRepository;
    private final PlayerProgressionRepository progressionRepository;

    public EconomyService(PlayerEconomyRepository economyRepository, 
                        com.lifeos.player.repository.PlayerIdentityRepository playerIdentityRepository,
                        PlayerProgressionRepository progressionRepository) {
        this.economyRepository = economyRepository;
        this.playerIdentityRepository = playerIdentityRepository;
        this.progressionRepository = progressionRepository;
    }

    @Transactional
    public void addGold(UUID playerId, long amount, String source) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be positive");
        BigDecimal amountBd = BigDecimal.valueOf(amount);

        PlayerEconomy economy = economyRepository.findByPlayerPlayerId(playerId)
                .orElseGet(() -> createInitialEconomy(playerId));

        economy.setGoldBalance(economy.getGoldBalance().add(amountBd));
        economy.setTotalGoldEarned(economy.getTotalGoldEarned().add(amountBd));
        economy.setLastTransactionAt(LocalDateTime.now());
        
        economyRepository.save(economy);
    }

    @Transactional
    public void deductGold(UUID playerId, long amount, String reason) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be positive");
        BigDecimal amountBd = BigDecimal.valueOf(amount);

        PlayerEconomy economy = economyRepository.findByPlayerPlayerId(playerId)
                .orElseGet(() -> createInitialEconomy(playerId));

        if (economy.getGoldBalance().compareTo(amountBd) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        economy.setGoldBalance(economy.getGoldBalance().subtract(amountBd));
        economy.setTotalGoldSpent(economy.getTotalGoldSpent().add(amountBd));
        economy.setLastTransactionAt(LocalDateTime.now());
        
        economyRepository.save(economy);
    }
    
    @Transactional(readOnly = true)
    public PlayerEconomy getEconomyState(UUID playerId) {
         return economyRepository.findByPlayerPlayerId(playerId)
                .orElseGet(() -> createInitialEconomy(playerId));
    }

    private PlayerEconomy createInitialEconomy(UUID playerId) {
        // We need player identity ref. Assuming it exists if we are here.
        var player = playerIdentityRepository.findById(playerId).orElseThrow();
        
        long initialGold = progressionRepository.findByPlayerPlayerId(playerId)
                .map(prog -> prog.getGold())
                .orElse(0L);
        BigDecimal initialGoldBd = BigDecimal.valueOf(initialGold);
        
        return PlayerEconomy.builder()
                .player(player)
                .goldBalance(initialGoldBd)
                .totalGoldEarned(initialGoldBd)
                .totalGoldSpent(BigDecimal.ZERO)
                .lastTransactionAt(LocalDateTime.now())
                .build();
    }
}
