package com.lifeos.core.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopPurchaseRequest {

    @NotNull(message = "Player ID is required")
    private UUID playerId;

    @NotBlank(message = "Item code is required")
    private String itemCode;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
