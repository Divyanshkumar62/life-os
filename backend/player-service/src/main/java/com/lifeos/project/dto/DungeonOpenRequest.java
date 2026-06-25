package com.lifeos.project.dto;

import java.util.UUID;

public class DungeonOpenRequest {
    private UUID playerId;
    private String keyItemCode;

    public DungeonOpenRequest() {}

    public DungeonOpenRequest(UUID playerId, String keyItemCode) {
        this.playerId = playerId;
        this.keyItemCode = keyItemCode;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public String getKeyItemCode() {
        return keyItemCode;
    }

    public void setKeyItemCode(String keyItemCode) {
        this.keyItemCode = keyItemCode;
    }
}
