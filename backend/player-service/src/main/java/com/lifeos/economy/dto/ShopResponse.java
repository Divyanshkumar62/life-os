package com.lifeos.economy.dto;

import com.lifeos.economy.domain.ShopItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO wrapper for shop listings including system notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopResponse {
    private List<ShopItem> items = new ArrayList<>();
    private List<String> systemMessages = new ArrayList<>();
}
