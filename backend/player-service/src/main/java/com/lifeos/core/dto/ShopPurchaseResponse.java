package com.lifeos.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopPurchaseResponse {
    private UUID playerId;
    private String itemCode;
    private int quantity;
    private long goldSpent;
    private long remainingGold;
}
