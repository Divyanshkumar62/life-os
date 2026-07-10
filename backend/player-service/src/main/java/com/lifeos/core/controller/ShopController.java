package com.lifeos.core.controller;

import com.lifeos.core.dto.ShopPurchaseRequest;
import com.lifeos.core.dto.ShopPurchaseResponse;
import com.lifeos.core.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("coreShopController")
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping("/purchase")
    public ResponseEntity<ShopPurchaseResponse> purchase(@Valid @RequestBody ShopPurchaseRequest request) {
        ShopPurchaseResponse response = shopService.processPurchase(request);
        return ResponseEntity.ok(response);
    }
}
