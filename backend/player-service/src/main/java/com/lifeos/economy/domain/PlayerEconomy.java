package com.lifeos.economy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "player_economy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerEconomy {

    @Id
    @Column(name = "player_id")
    private UUID playerId;

    @Column(nullable = false)
    private long goldBalance;

    @Column(nullable = false)
    private long totalGoldEarned;

    @Column(nullable = false)
    private long totalGoldSpent;
}
