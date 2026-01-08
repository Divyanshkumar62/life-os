package com.lifeos.player.domain.enums;

@lombok.Getter
@lombok.AllArgsConstructor
public enum PlayerRank {
    F(5),
    E(10),
    D(25),
    C(45),
    B(70),
    A(90),
    S(100),
    SS(999); // Uncapped/Prestige

    private final int levelCap;
    
    public PlayerRank next() {
        int ordinal = this.ordinal();
        if (ordinal >= values().length - 1) {
            return this;
        }
        return values()[ordinal + 1];
    }
}
