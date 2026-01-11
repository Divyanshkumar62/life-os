package com.lifeos.economy.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchase_transaction")
public class PurchaseTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID playerId;

    @Column(nullable = false)
    private UUID itemId;

    @Column(nullable = false)
    private long cost;

    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    public PurchaseTransaction() {}

    public PurchaseTransaction(UUID id, UUID playerId, UUID itemId, long cost, LocalDateTime timestamp) {
        this.id = id;
        this.playerId = playerId;
        this.itemId = itemId;
        this.cost = cost;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }
    public long getCost() { return cost; }
    public void setCost(long cost) { this.cost = cost; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static PurchaseTransactionBuilder builder() {
        return new PurchaseTransactionBuilder();
    }

    public static class PurchaseTransactionBuilder {
        private UUID id;
        private UUID playerId;
        private UUID itemId;
        private long cost;
        private LocalDateTime timestamp;

        public PurchaseTransactionBuilder id(UUID id) { this.id = id; return this; }
        public PurchaseTransactionBuilder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public PurchaseTransactionBuilder itemId(UUID itemId) { this.itemId = itemId; return this; }
        public PurchaseTransactionBuilder cost(long cost) { this.cost = cost; return this; }
        public PurchaseTransactionBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public PurchaseTransaction build() {
            return new PurchaseTransaction(id, playerId, itemId, cost, timestamp);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) timestamp = LocalDateTime.now();
    }
}
