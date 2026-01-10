package com.lifeos.economy.service;

import com.lifeos.economy.domain.PlayerEconomy;
import com.lifeos.economy.repository.PlayerEconomyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EconomyService {

    private final PlayerEconomyRepository economyRepository;

    @Transactional
    public void addGold(UUID playerId, long amount, String source) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be positive");

        PlayerEconomy economy = economyRepository.findById(playerId)
                .orElse(PlayerEconomy.builder()
                        .playerId(playerId)
                        .goldBalance(0)
                        .totalGoldEarned(0)
                        .totalGoldSpent(0)
                        .build());

        economy.setGoldBalance(economy.getGoldBalance() + amount);
        economy.setTotalGoldEarned(economy.getTotalGoldEarned() + amount);
        
        economyRepository.save(economy);
        // TODO: Log source if needed
    }

    @Transactional
    public void deductGold(UUID playerId, long amount, String reason) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be positive");

        PlayerEconomy economy = economyRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player economy data not found"));

        if (economy.getGoldBalance() < amount) {
            throw new IllegalStateException("Insufficient funds");
        }

        economy.setGoldBalance(economy.getGoldBalance() - amount);
        economy.setTotalGoldSpent(economy.getTotalGoldSpent() + amount);
        
        economyRepository.save(economy);
    }
    
    @Transactional(readOnly = true)
    public PlayerEconomy getEconomyState(UUID playerId) {
         return economyRepository.findById(playerId)
                .orElse(PlayerEconomy.builder()
                        .playerId(playerId)
                        .goldBalance(0)
                        .totalGoldEarned(0)
                        .totalGoldSpent(0)
                        .build());
    }
}
