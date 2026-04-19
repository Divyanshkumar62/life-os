package com.lifeos.project.dto;

import java.util.UUID;

public class ProjectCreationRequest {
    private UUID playerId;
    private String goal;
    private String userRank;

    public ProjectCreationRequest() {}

    public ProjectCreationRequest(UUID playerId, String goal, String userRank) {
        this.playerId = playerId;
        this.goal = goal;
        this.userRank = userRank;
    }

    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public String getUserRank() { return userRank; }
    public void setUserRank(String userRank) { this.userRank = userRank; }
}
