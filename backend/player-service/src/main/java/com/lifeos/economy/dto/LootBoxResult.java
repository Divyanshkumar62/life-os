package com.lifeos.economy.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO representing the result of opening a loot box.
 */
@Data
@Builder
public class LootBoxResult {
    /** Type of the box that was opened, e.g., RANDOM_BOX */
    private String boxType;
    /** Either GOLD or ITEM */
    private String dropType;
    /** For ITEM drops, the code of the item (e.g., BANDAGES, KEY_A_RANK). Null for gold. */
    private String itemCode;
    /** Human readable name of the item, if applicable. */
    private String itemName;
    /** Amount of gold awarded, if dropType is GOLD. */
    private Long amount;
    /** Message describing the outcome, suitable for UI display. */
    private String message;
    /** Optional system messages that triggered during this operation (e.g., SEN loot buff active). */
    private List<String> systemMessages;
}
