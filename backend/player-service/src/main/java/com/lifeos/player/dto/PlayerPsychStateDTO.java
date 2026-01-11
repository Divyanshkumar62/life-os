package com.lifeos.player.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PlayerPsychStateDTO {
    private int momentum;
    private int complacency;
    private int stressLoad;
    private int confidenceBias;
    
    public PlayerPsychStateDTO() {}

    public PlayerPsychStateDTO(int momentum, int complacency, int stressLoad, int confidenceBias) {
        this.momentum = momentum;
        this.complacency = complacency;
        this.stressLoad = stressLoad;
        this.confidenceBias = confidenceBias;
    }

    // Getters and Setters
    public int getMomentum() { return momentum; }
    public void setMomentum(int momentum) { this.momentum = momentum; }
    public int getComplacency() { return complacency; }
    public void setComplacency(int complacency) { this.complacency = complacency; }
    public int getStressLoad() { return stressLoad; }
    public void setStressLoad(int stressLoad) { this.stressLoad = stressLoad; }
    public int getConfidenceBias() { return confidenceBias; }
    public void setConfidenceBias(int confidenceBias) { this.confidenceBias = confidenceBias; }
    
    public static PlayerPsychStateDTOBuilder builder() {
        return new PlayerPsychStateDTOBuilder();
    }

    public static class PlayerPsychStateDTOBuilder {
        private int momentum;
        private int complacency;
        private int stressLoad;
        private int confidenceBias;

        public PlayerPsychStateDTOBuilder momentum(int momentum) { this.momentum = momentum; return this; }
        public PlayerPsychStateDTOBuilder complacency(int complacency) { this.complacency = complacency; return this; }
        public PlayerPsychStateDTOBuilder stressLoad(int stressLoad) { this.stressLoad = stressLoad; return this; }
        public PlayerPsychStateDTOBuilder confidenceBias(int confidenceBias) { this.confidenceBias = confidenceBias; return this; }

        public PlayerPsychStateDTO build() {
            return new PlayerPsychStateDTO(momentum, complacency, stressLoad, confidenceBias);
        }
    }
}
