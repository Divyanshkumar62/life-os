package com.lifeos.economy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import com.lifeos.player.domain.PlayerIdentity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "player_economy")
public class PlayerEconomy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID economyId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerIdentity player;

    private BigDecimal goldBalance;
    private BigDecimal totalGoldEarned;
    private BigDecimal totalGoldSpent;

    private LocalDateTime lastTransactionAt;
    
    public PlayerEconomy() {}

    public PlayerEconomy(UUID economyId, PlayerIdentity player, BigDecimal goldBalance, BigDecimal totalGoldEarned, BigDecimal totalGoldSpent, LocalDateTime lastTransactionAt) {
        this.economyId = economyId;
        this.player = player;
        this.goldBalance = goldBalance;
        this.totalGoldEarned = totalGoldEarned;
        this.totalGoldSpent = totalGoldSpent;
        this.lastTransactionAt = lastTransactionAt;
    }

    @PrePersist
    protected void onCreate() {
        if (goldBalance == null) goldBalance = BigDecimal.ZERO;
        if (totalGoldEarned == null) totalGoldEarned = BigDecimal.ZERO;
        if (totalGoldSpent == null) totalGoldSpent = BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public UUID getEconomyId() { return economyId; }
    public void setEconomyId(UUID economyId) { this.economyId = economyId; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
    public BigDecimal getGoldBalance() { return goldBalance; }
    public void setGoldBalance(BigDecimal goldBalance) { this.goldBalance = goldBalance; }
    public BigDecimal getTotalGoldEarned() { return totalGoldEarned; }
    public void setTotalGoldEarned(BigDecimal totalGoldEarned) { this.totalGoldEarned = totalGoldEarned; }
    public BigDecimal getTotalGoldSpent() { return totalGoldSpent; }
    public void setTotalGoldSpent(BigDecimal totalGoldSpent) { this.totalGoldSpent = totalGoldSpent; }
    public LocalDateTime getLastTransactionAt() { return lastTransactionAt; }
    public void setLastTransactionAt(LocalDateTime lastTransactionAt) { this.lastTransactionAt = lastTransactionAt; }

    public static PlayerEconomyBuilder builder() {
        return new PlayerEconomyBuilder();
    }

    public static class PlayerEconomyBuilder {
        private UUID economyId;
        private PlayerIdentity player;
        private BigDecimal goldBalance;
        private BigDecimal totalGoldEarned;
        private BigDecimal totalGoldSpent;
        private LocalDateTime lastTransactionAt;

        public PlayerEconomyBuilder economyId(UUID economyId) { this.economyId = economyId; return this; }
        public PlayerEconomyBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public PlayerEconomyBuilder goldBalance(BigDecimal goldBalance) { this.goldBalance = goldBalance; return this; }
        public PlayerEconomyBuilder totalGoldEarned(BigDecimal totalGoldEarned) { this.totalGoldEarned = totalGoldEarned; return this; }
        public PlayerEconomyBuilder totalGoldSpent(BigDecimal totalGoldSpent) { this.totalGoldSpent = totalGoldSpent; return this; }
        public PlayerEconomyBuilder lastTransactionAt(LocalDateTime lastTransactionAt) { this.lastTransactionAt = lastTransactionAt; return this; }

        public PlayerEconomy build() {
            return new PlayerEconomy(economyId, player, goldBalance, totalGoldEarned, totalGoldSpent, lastTransactionAt);
        }
    }
}
