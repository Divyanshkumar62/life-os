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
    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private ShopCategory category;

    @Column(name = "stock_limit")
    private Integer stockLimit; // Nullable - null means unlimited

    @Enumerated(EnumType.STRING)
    @Column(name = "rank_requirement", columnDefinition = "VARCHAR(255)")
    private com.lifeos.player.domain.enums.PlayerRank rankRequirement;

    @Column(name = "purchase_cooldown_hours")
    private Integer purchaseCooldownHours;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> effectPayload;

    @Column(name = "image_url")
    private String imageUrl;
    
    public ShopItem() {}

    public ShopItem(UUID itemId, String code, String name, String description, long cost, ShopCategory category, Map<String, Object> effectPayload, String imageUrl) {
        this.itemId = itemId;
        this.code = code;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.category = category;
        this.effectPayload = effectPayload;
        this.imageUrl = imageUrl;
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
    public Integer getStockLimit() { return stockLimit; }
    public void setStockLimit(Integer stockLimit) { this.stockLimit = stockLimit; }
    public com.lifeos.player.domain.enums.PlayerRank getRankRequirement() { return rankRequirement; }
    public void setRankRequirement(com.lifeos.player.domain.enums.PlayerRank rankRequirement) { this.rankRequirement = rankRequirement; }
    public Integer getPurchaseCooldownHours() { return purchaseCooldownHours; }
    public void setPurchaseCooldownHours(Integer purchaseCooldownHours) { this.purchaseCooldownHours = purchaseCooldownHours; }
    public Map<String, Object> getEffectPayload() { return effectPayload; }
    public void setEffectPayload(Map<String, Object> effectPayload) { this.effectPayload = effectPayload; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

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
        private Integer stockLimit;
        private com.lifeos.player.domain.enums.PlayerRank rankRequirement;
        private Integer purchaseCooldownHours;
        private Map<String, Object> effectPayload;
        private String imageUrl;

        public ShopItemBuilder itemId(UUID itemId) { this.itemId = itemId; return this; }
        public ShopItemBuilder code(String code) { this.code = code; return this; }
        public ShopItemBuilder name(String name) { this.name = name; return this; }
        public ShopItemBuilder description(String description) { this.description = description; return this; }
        public ShopItemBuilder cost(long cost) { this.cost = cost; return this; }
        public ShopItemBuilder category(ShopCategory category) { this.category = category; return this; }
        public ShopItemBuilder stockLimit(Integer stockLimit) { this.stockLimit = stockLimit; return this; }
        public ShopItemBuilder rankRequirement(com.lifeos.player.domain.enums.PlayerRank rankRequirement) { this.rankRequirement = rankRequirement; return this; }
        public ShopItemBuilder purchaseCooldownHours(Integer purchaseCooldownHours) { this.purchaseCooldownHours = purchaseCooldownHours; return this; }
        public ShopItemBuilder effectPayload(Map<String, Object> effectPayload) { this.effectPayload = effectPayload; return this; }
        public ShopItemBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }

        public ShopItem build() {
            ShopItem item = new ShopItem(itemId, code, name, description, cost, category, effectPayload, imageUrl);
            item.setStockLimit(stockLimit);
            item.setRankRequirement(rankRequirement);
            item.setPurchaseCooldownHours(purchaseCooldownHours);
            return item;
        }
    }
}
