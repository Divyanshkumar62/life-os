package com.lifeos.player.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@lombok.Data // Keeping Data if it works for equals/hashcode, but override getters
public class PlayerStateResponse {
    private PlayerIdentityDTO identity;
    private PlayerProgressionDTO progression;
    private List<PlayerAttributeDTO> attributes;
    private PlayerPsychStateDTO psychState;
    private PlayerMetricsDTO metrics;
    private List<PlayerStatusFlagDTO> activeFlags;
    private PlayerTemporalStateDTO temporalState;
    private PlayerHistoryDTO history;
    
    public PlayerStateResponse() {}

    public PlayerStateResponse(PlayerIdentityDTO identity, PlayerProgressionDTO progression, List<PlayerAttributeDTO> attributes, PlayerPsychStateDTO psychState, PlayerMetricsDTO metrics, List<PlayerStatusFlagDTO> activeFlags, PlayerTemporalStateDTO temporalState, PlayerHistoryDTO history) {
        this.identity = identity;
        this.progression = progression;
        this.attributes = attributes;
        this.psychState = psychState;
        this.metrics = metrics;
        this.activeFlags = activeFlags;
        this.temporalState = temporalState;
        this.history = history;
    }

    // Getters
    public PlayerIdentityDTO getIdentity() { return identity; }
    public void setIdentity(PlayerIdentityDTO identity) { this.identity = identity; }
    public PlayerProgressionDTO getProgression() { return progression; }
    public void setProgression(PlayerProgressionDTO progression) { this.progression = progression; }
    public List<PlayerAttributeDTO> getAttributes() { return attributes; }
    public void setAttributes(List<PlayerAttributeDTO> attributes) { this.attributes = attributes; }
    public PlayerPsychStateDTO getPsychState() { return psychState; }
    public void setPsychState(PlayerPsychStateDTO psychState) { this.psychState = psychState; }
    public PlayerMetricsDTO getMetrics() { return metrics; }
    public void setMetrics(PlayerMetricsDTO metrics) { this.metrics = metrics; }
    public List<PlayerStatusFlagDTO> getActiveFlags() { return activeFlags; }
    public void setActiveFlags(List<PlayerStatusFlagDTO> activeFlags) { this.activeFlags = activeFlags; }
    public PlayerTemporalStateDTO getTemporalState() { return temporalState; }
    public void setTemporalState(PlayerTemporalStateDTO temporalState) { this.temporalState = temporalState; }
    public PlayerHistoryDTO getHistory() { return history; }
    public void setHistory(PlayerHistoryDTO history) { this.history = history; }

    public static PlayerStateResponseBuilder builder() {
        return new PlayerStateResponseBuilder();
    }

    public static class PlayerStateResponseBuilder {
        private PlayerIdentityDTO identity;
        private PlayerProgressionDTO progression;
        private List<PlayerAttributeDTO> attributes;
        private PlayerPsychStateDTO psychState;
        private PlayerMetricsDTO metrics;
        private List<PlayerStatusFlagDTO> activeFlags;
        private PlayerTemporalStateDTO temporalState;
        private PlayerHistoryDTO history;

        public PlayerStateResponseBuilder identity(PlayerIdentityDTO identity) { this.identity = identity; return this; }
        public PlayerStateResponseBuilder progression(PlayerProgressionDTO progression) { this.progression = progression; return this; }
        public PlayerStateResponseBuilder attributes(List<PlayerAttributeDTO> attributes) { this.attributes = attributes; return this; }
        public PlayerStateResponseBuilder psychState(PlayerPsychStateDTO psychState) { this.psychState = psychState; return this; }
        public PlayerStateResponseBuilder metrics(PlayerMetricsDTO metrics) { this.metrics = metrics; return this; }
        public PlayerStateResponseBuilder activeFlags(List<PlayerStatusFlagDTO> activeFlags) { this.activeFlags = activeFlags; return this; }
        public PlayerStateResponseBuilder temporalState(PlayerTemporalStateDTO temporalState) { this.temporalState = temporalState; return this; }
        public PlayerStateResponseBuilder history(PlayerHistoryDTO history) { this.history = history; return this; }

        public PlayerStateResponse build() {
            return new PlayerStateResponse(identity, progression, attributes, psychState, metrics, activeFlags, temporalState, history);
        }
    }
}
