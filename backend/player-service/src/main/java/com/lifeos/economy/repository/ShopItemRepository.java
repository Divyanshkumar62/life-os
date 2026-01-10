package com.lifeos.economy.repository;

import com.lifeos.economy.domain.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItem, UUID> {
    Optional<ShopItem> findByCode(String code);
}
