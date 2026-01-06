package com.lifeos.player.domain;

import com.lifeos.player.domain.enums.AttributeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_attribute", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"player_id", "attribute_type"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerIdentity player;

    @Enumerated(EnumType.STRING)
    @Column(name = "attribute_type", nullable = false)
    private AttributeType attributeType;

    @Column(name = "base_value", nullable = false)
    private double baseValue;

    @Column(name = "current_value", nullable = false)
    private double currentValue;

    @Column(name = "growth_velocity")
    private double growthVelocity;

    @Column(name = "decay_rate")
    private double decayRate;
}
