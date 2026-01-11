package com.lifeos.voice.event;

import com.lifeos.voice.domain.enums.SystemMessageType;
import java.util.Map;
import java.util.UUID;

public class VoiceSystemEvent {
    private final UUID playerId;
    private final SystemMessageType type;
    private final Map<String, Object> payload;

    public VoiceSystemEvent(UUID playerId, SystemMessageType type, Map<String, Object> payload) {
        this.playerId = playerId;
        this.type = type;
        this.payload = payload;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public SystemMessageType getType() {
        return type;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID playerId;
        private SystemMessageType type;
        private Map<String, Object> payload;

        public Builder playerId(UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        public Builder type(SystemMessageType type) {
            this.type = type;
            return this;
        }

        public Builder payload(Map<String, Object> payload) {
            this.payload = payload;
            return this;
        }

        public VoiceSystemEvent build() {
            return new VoiceSystemEvent(playerId, type, payload);
        }
    }
}
