package com.lifeos.project.dto;

public class LootItemDTO {
    private String itemCode;
    private String itemName;
    private String rarity;
    private String description;

    public LootItemDTO() {}

    public LootItemDTO(String itemCode, String itemName, String rarity, String description) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.rarity = rarity;
        this.description = description;
    }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
