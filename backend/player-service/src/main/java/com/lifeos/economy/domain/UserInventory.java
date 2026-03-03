package com.lifeos.economy.domain;

import com.lifeos.player.domain.PlayerIdentity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_inventory", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"player_id", "item_id"})
})
public class UserInventory {

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
    private int quantity;

    private LocalDateTime acquiredAt;
    
    public UserInventory() {}

    public UserInventory(UUID id, PlayerIdentity player, ShopItem item, int quantity, LocalDateTime acquiredAt) {
        this.id = id;
        this.player = player;
        this.item = item;
        this.quantity = quantity;
        this.acquiredAt = acquiredAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (acquiredAt == null) acquiredAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
    public ShopItem getItem() { return item; }
    public void setItem(ShopItem item) { this.item = item; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public LocalDateTime getAcquiredAt() { return acquiredAt; }
    public void setAcquiredAt(LocalDateTime acquiredAt) { this.acquiredAt = acquiredAt; }

    public static UserInventoryBuilder builder() {
        return new UserInventoryBuilder();
    }

    public static class UserInventoryBuilder {
        private UUID id;
        private PlayerIdentity player;
        private ShopItem item;
        private int quantity;
        private LocalDateTime acquiredAt;

        public UserInventoryBuilder id(UUID id) { this.id = id; return this; }
        public UserInventoryBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public UserInventoryBuilder item(ShopItem item) { this.item = item; return this; }
        public UserInventoryBuilder quantity(int quantity) { this.quantity = quantity; return this; }
        public UserInventoryBuilder acquiredAt(LocalDateTime acquiredAt) { this.acquiredAt = acquiredAt; return this; }

        public UserInventory build() {
            return new UserInventory(id, player, item, quantity, acquiredAt);
        }
    }
}
