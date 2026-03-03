package com.lifeos.economy.domain;

import com.lifeos.player.domain.PlayerIdentity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchase_cooldown", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"player_id", "item_id"})
})
public class PurchaseCooldown {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerIdentity player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ShopItem item;

    @Column(nullable = false)
    private LocalDateTime lastPurchasedAt;
    
    public PurchaseCooldown() {}

    public PurchaseCooldown(UUID id, PlayerIdentity player, ShopItem item, LocalDateTime lastPurchasedAt) {
        this.id = id;
        this.player = player;
        this.item = item;
        this.lastPurchasedAt = lastPurchasedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
    public ShopItem getItem() { return item; }
    public void setItem(ShopItem item) { this.item = item; }
    public LocalDateTime getLastPurchasedAt() { return lastPurchasedAt; }
    public void setLastPurchasedAt(LocalDateTime lastPurchasedAt) { this.lastPurchasedAt = lastPurchasedAt; }

    public static PurchaseCooldownBuilder builder() {
        return new PurchaseCooldownBuilder();
    }

    public static class PurchaseCooldownBuilder {
        private UUID id;
        private PlayerIdentity player;
        private ShopItem item;
        private LocalDateTime lastPurchasedAt;

        public PurchaseCooldownBuilder id(UUID id) { this.id = id; return this; }
        public PurchaseCooldownBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public PurchaseCooldownBuilder item(ShopItem item) { this.item = item; return this; }
        public PurchaseCooldownBuilder lastPurchasedAt(LocalDateTime lastPurchasedAt) { this.lastPurchasedAt = lastPurchasedAt; return this; }

        public PurchaseCooldown build() {
            return new PurchaseCooldown(id, player, item, lastPurchasedAt);
        }
    }
}
