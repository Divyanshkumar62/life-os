package com.lifeos.streak.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.PrePersist;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "player_streak")
public class PlayerStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID playerId;

    @Column(nullable = false)
    private int currentStreak;

    @Column(nullable = false)
    private int longestStreak;

    @Column(nullable = false)
    private int previousStreak;

    private LocalDate lastSuccessfulDate;

    private LocalDate lastBrokenDate;

    public PlayerStreak() {}

    public PlayerStreak(UUID id, UUID playerId, int currentStreak, int longestStreak, int previousStreak, LocalDate lastSuccessfulDate, LocalDate lastBrokenDate) {
        this.id = id;
        this.playerId = playerId;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.previousStreak = previousStreak;
        this.lastSuccessfulDate = lastSuccessfulDate;
        this.lastBrokenDate = lastBrokenDate;
    }

    @PrePersist
    protected void onCreate() {
        if (currentStreak < 0) currentStreak = 0;
        if (longestStreak < 0) longestStreak = 0;
        if (previousStreak < 0) previousStreak = 0;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    
    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

    public int getPreviousStreak() { return previousStreak; }
    public void setPreviousStreak(int previousStreak) { this.previousStreak = previousStreak; }
    
    public LocalDate getLastSuccessfulDate() { return lastSuccessfulDate; }
    public void setLastSuccessfulDate(LocalDate lastSuccessfulDate) { this.lastSuccessfulDate = lastSuccessfulDate; }

    public LocalDate getLastBrokenDate() { return lastBrokenDate; }
    public void setLastBrokenDate(LocalDate lastBrokenDate) { this.lastBrokenDate = lastBrokenDate; }

    public static PlayerStreakBuilder builder() {
        return new PlayerStreakBuilder();
    }

    public static class PlayerStreakBuilder {
        private UUID id;
        private UUID playerId;
        private int currentStreak;
        private int longestStreak;
        private int previousStreak;
        private LocalDate lastSuccessfulDate;
        private LocalDate lastBrokenDate;

        public PlayerStreakBuilder id(UUID id) { this.id = id; return this; }
        public PlayerStreakBuilder playerId(UUID playerId) { this.playerId = playerId; return this; }
        public PlayerStreakBuilder currentStreak(int currentStreak) { this.currentStreak = currentStreak; return this; }
        public PlayerStreakBuilder longestStreak(int longestStreak) { this.longestStreak = longestStreak; return this; }
        public PlayerStreakBuilder previousStreak(int previousStreak) { this.previousStreak = previousStreak; return this; }
        public PlayerStreakBuilder lastSuccessfulDate(LocalDate lastSuccessfulDate) { this.lastSuccessfulDate = lastSuccessfulDate; return this; }
        public PlayerStreakBuilder lastBrokenDate(LocalDate lastBrokenDate) { this.lastBrokenDate = lastBrokenDate; return this; }

        public PlayerStreak build() {
            return new PlayerStreak(id, playerId, currentStreak, longestStreak, previousStreak, lastSuccessfulDate, lastBrokenDate);
        }
    }
}
