package com.lifeos.economy.service;

import com.lifeos.economy.domain.PlayerEconomy;
import com.lifeos.economy.repository.PlayerEconomyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EconomyService {

    private final PlayerEconomyRepository economyRepository;
    private final com.lifeos.player.repository.PlayerIdentityRepository playerIdentityRepository;

    @Transactional
    public void addGold(UUID playerId, long amount, String source) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be positive");
        BigDecimal amountBd = BigDecimal.valueOf(amount);

        PlayerEconomy economy = economyRepository.findById(playerId)
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

        PlayerEconomy economy = economyRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player economy data not found"));

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
         return economyRepository.findById(playerId)
                .orElseGet(() -> createInitialEconomy(playerId));
    }

    private PlayerEconomy createInitialEconomy(UUID playerId) {
        // We need player identity ref. Assuming it exists if we are here.
        var player = playerIdentityRepository.findById(playerId).orElseThrow();
        
        return PlayerEconomy.builder()
                .player(player)
                .goldBalance(BigDecimal.ZERO)
                .totalGoldEarned(BigDecimal.ZERO)
                .totalGoldSpent(BigDecimal.ZERO)
                .lastTransactionAt(LocalDateTime.now())
                .build();
    }
}
