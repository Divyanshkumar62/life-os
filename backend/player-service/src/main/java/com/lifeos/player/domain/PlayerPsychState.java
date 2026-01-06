package com.lifeos.player.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_psych_state")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPsychState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private PlayerIdentity player;

    @Min(0)
    @Max(100)
    @Column(nullable = false)
    private int momentum;

    @Min(0)
    @Max(100)
    @Column(nullable = false)
    private int complacency;

    @Min(0)
    @Max(100)
    @Column(name = "stress_load", nullable = false)
    private int stressLoad;

    @Min(0)
    @Max(100)
    @Column(name = "confidence_bias", nullable = false)
    private int confidenceBias;
}
