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
    
    public PlayerPsychState() {}

    public PlayerPsychState(Long id, PlayerIdentity player, int momentum, int complacency, int stressLoad, int confidenceBias) {
        this.id = id;
        this.player = player;
        this.momentum = momentum;
        this.complacency = complacency;
        this.stressLoad = stressLoad;
        this.confidenceBias = confidenceBias;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
    public int getMomentum() { return momentum; }
    public void setMomentum(int momentum) { this.momentum = momentum; }
    public int getComplacency() { return complacency; }
    public void setComplacency(int complacency) { this.complacency = complacency; }
    public int getStressLoad() { return stressLoad; }
    public void setStressLoad(int stressLoad) { this.stressLoad = stressLoad; }
    public int getConfidenceBias() { return confidenceBias; }
    public void setConfidenceBias(int confidenceBias) { this.confidenceBias = confidenceBias; }

    public static PlayerPsychStateBuilder builder() {
        return new PlayerPsychStateBuilder();
    }

    public static class PlayerPsychStateBuilder {
        private Long id;
        private PlayerIdentity player;
        private int momentum;
        private int complacency;
        private int stressLoad;
        private int confidenceBias;

        public PlayerPsychStateBuilder id(Long id) { this.id = id; return this; }
        public PlayerPsychStateBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public PlayerPsychStateBuilder momentum(int momentum) { this.momentum = momentum; return this; }
        public PlayerPsychStateBuilder complacency(int complacency) { this.complacency = complacency; return this; }
        public PlayerPsychStateBuilder stressLoad(int stressLoad) { this.stressLoad = stressLoad; return this; }
        public PlayerPsychStateBuilder confidenceBias(int confidenceBias) { this.confidenceBias = confidenceBias; return this; }

        public PlayerPsychState build() {
            return new PlayerPsychState(id, player, momentum, complacency, stressLoad, confidenceBias);
        }
    }
}
