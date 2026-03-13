package com.lifeos.notification.controller;

import java.util.UUID;

public class UpdateFcmRequest {
    private UUID playerId;
    private String token;

    public UpdateFcmRequest() {}

    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
