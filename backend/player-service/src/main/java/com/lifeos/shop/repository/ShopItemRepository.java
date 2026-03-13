package com.lifeos.shop.repository;

import com.lifeos.shop.domain.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItem, UUID> {
    Optional<ShopItem> findByItemCode(String itemCode);
}
