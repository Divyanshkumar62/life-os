package com.lifeos.economy.domain;

import com.lifeos.economy.domain.enums.ShopCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "shop_item")
public class ShopItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID itemId;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private long cost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShopCategory category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> effectPayload;
    
    public ShopItem() {}

    public ShopItem(UUID itemId, String code, String name, String description, long cost, ShopCategory category, Map<String, Object> effectPayload) {
        this.itemId = itemId;
        this.code = code;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.category = category;
        this.effectPayload = effectPayload;
    }

    // Getters and Setters
    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getCost() { return cost; }
    public void setCost(long cost) { this.cost = cost; }
    public ShopCategory getCategory() { return category; }
    public void setCategory(ShopCategory category) { this.category = category; }
    public Map<String, Object> getEffectPayload() { return effectPayload; }
    public void setEffectPayload(Map<String, Object> effectPayload) { this.effectPayload = effectPayload; }

    public static ShopItemBuilder builder() {
        return new ShopItemBuilder();
    }

    public static class ShopItemBuilder {
        private UUID itemId;
        private String code;
        private String name;
        private String description;
        private long cost;
        private ShopCategory category;
        private Map<String, Object> effectPayload;

        public ShopItemBuilder itemId(UUID itemId) { this.itemId = itemId; return this; }
        public ShopItemBuilder code(String code) { this.code = code; return this; }
        public ShopItemBuilder name(String name) { this.name = name; return this; }
        public ShopItemBuilder description(String description) { this.description = description; return this; }
        public ShopItemBuilder cost(long cost) { this.cost = cost; return this; }
        public ShopItemBuilder category(ShopCategory category) { this.category = category; return this; }
        public ShopItemBuilder effectPayload(Map<String, Object> effectPayload) { this.effectPayload = effectPayload; return this; }

        public ShopItem build() {
            return new ShopItem(itemId, code, name, description, cost, category, effectPayload);
        }
    }
}
