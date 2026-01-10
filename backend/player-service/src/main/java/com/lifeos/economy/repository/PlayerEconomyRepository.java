package com.lifeos.economy.repository;

import com.lifeos.economy.domain.PlayerEconomy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlayerEconomyRepository extends JpaRepository<PlayerEconomy, UUID> {
}
