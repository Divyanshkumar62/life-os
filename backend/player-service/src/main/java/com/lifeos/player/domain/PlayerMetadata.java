package com.lifeos.player.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "player_metadata")
public class PlayerMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private PlayerIdentity player;

    @Column(name = "ui_theme")
    private String uiTheme;

    @Column(name = "unlocked_themes", columnDefinition = "TEXT")
    private String unlockedThemes; // JSON array of unlocked theme codes

    // Future metadata fields can go here (e.g., tutorial flags, preferences)

    public PlayerMetadata() {}

    public PlayerMetadata(UUID id, PlayerIdentity player, String uiTheme) {
        this.id = id;
        this.player = player;
        this.uiTheme = uiTheme;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public PlayerIdentity getPlayer() { return player; }
    public void setPlayer(PlayerIdentity player) { this.player = player; }
    public String getUiTheme() { return uiTheme; }
    public void setUiTheme(String uiTheme) { this.uiTheme = uiTheme; }
    public String getUnlockedThemes() { return unlockedThemes; }
    public void setUnlockedThemes(String unlockedThemes) { this.unlockedThemes = unlockedThemes; }

    public static PlayerMetadataBuilder builder() {
        return new PlayerMetadataBuilder();
    }

    public static class PlayerMetadataBuilder {
        private UUID id;
        private PlayerIdentity player;
        private String uiTheme;

        public PlayerMetadataBuilder id(UUID id) { this.id = id; return this; }
        public PlayerMetadataBuilder player(PlayerIdentity player) { this.player = player; return this; }
        public PlayerMetadataBuilder uiTheme(String uiTheme) { this.uiTheme = uiTheme; return this; }

        public PlayerMetadata build() {
            return new PlayerMetadata(id, player, uiTheme);
        }
    }
}
