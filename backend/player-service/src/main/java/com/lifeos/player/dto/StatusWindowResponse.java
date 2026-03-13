package com.lifeos.player.dto;

import java.util.List;

public class StatusWindowResponse {

    private Identity identity;
    private Progression progression;
    private Attributes attributes;
    private Economy economy;
    private SystemState systemState;

    public Identity getIdentity() { return identity; }
    public void setIdentity(Identity identity) { this.identity = identity; }
    public Progression getProgression() { return progression; }
    public void setProgression(Progression progression) { this.progression = progression; }
    public Attributes getAttributes() { return attributes; }
    public void setAttributes(Attributes attributes) { this.attributes = attributes; }
    public Economy getEconomy() { return economy; }
    public void setEconomy(Economy economy) { this.economy = economy; }
    public SystemState getSystemState() { return systemState; }
    public void setSystemState(SystemState systemState) { this.systemState = systemState; }

    public static StatusWindowBuilder builder() {
        return new StatusWindowBuilder();
    }

    public static class StatusWindowBuilder {
        private Identity identity;
        private Progression progression;
        private Attributes attributes;
        private Economy economy;
        private SystemState systemState;

        public StatusWindowBuilder identity(Identity identity) {
            this.identity = identity;
            return this;
        }
        public StatusWindowBuilder progression(Progression progression) {
            this.progression = progression;
            return this;
        }
        public StatusWindowBuilder attributes(Attributes attributes) {
            this.attributes = attributes;
            return this;
        }
        public StatusWindowBuilder economy(Economy economy) {
            this.economy = economy;
            return this;
        }
        public StatusWindowBuilder systemState(SystemState systemState) {
            this.systemState = systemState;
            return this;
        }
        public StatusWindowResponse build() {
            StatusWindowResponse response = new StatusWindowResponse();
            response.identity = this.identity;
            response.progression = this.progression;
            response.attributes = this.attributes;
            response.economy = this.economy;
            response.systemState = this.systemState;
            return response;
        }
    }

    public static class Identity {
        private int level;
        private String rank;
        private String title;
        private String equippedTheme;

        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public String getRank() { return rank; }
        public void setRank(String rank) { this.rank = rank; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getEquippedTheme() { return equippedTheme; }
        public void setEquippedTheme(String equippedTheme) { this.equippedTheme = equippedTheme; }

        public static IdentityBuilder builder() {
            return new IdentityBuilder();
        }

        public static class IdentityBuilder {
            private int level;
            private String rank;
            private String title;
            private String equippedTheme;

            public IdentityBuilder level(int level) { this.level = level; return this; }
            public IdentityBuilder rank(String rank) { this.rank = rank; return this; }
            public IdentityBuilder title(String title) { this.title = title; return this; }
            public IdentityBuilder equippedTheme(String equippedTheme) { this.equippedTheme = equippedTheme; return this; }
            public Identity build() {
                Identity i = new Identity();
                i.level = this.level;
                i.rank = this.rank;
                i.title = this.title;
                i.equippedTheme = this.equippedTheme;
                return i;
            }
        }
    }

    public static class Progression {
        private long currentXp;
        private long maxXpForLevel;

        public long getCurrentXp() { return currentXp; }
        public void setCurrentXp(long currentXp) { this.currentXp = currentXp; }
        public long getMaxXpForLevel() { return maxXpForLevel; }
        public void setMaxXpForLevel(long maxXpForLevel) { this.maxXpForLevel = maxXpForLevel; }

        public static ProgressionBuilder builder() {
            return new ProgressionBuilder();
        }

        public static class ProgressionBuilder {
            private long currentXp;
            private long maxXpForLevel;

            public ProgressionBuilder currentXp(long currentXp) { this.currentXp = currentXp; return this; }
            public ProgressionBuilder maxXpForLevel(long maxXpForLevel) { this.maxXpForLevel = maxXpForLevel; return this; }
            public Progression build() {
                Progression p = new Progression();
                p.currentXp = this.currentXp;
                p.maxXpForLevel = this.maxXpForLevel;
                return p;
            }
        }
    }

    public static class Attributes {
        private int STR;
        private int INT;
        private int VIT;
        private int SEN;
        private int freePoints;

        public int getSTR() { return STR; }
        public void setSTR(int STR) { this.STR = STR; }
        public int getINT() { return INT; }
        public void setINT(int INT) { this.INT = INT; }
        public int getVIT() { return VIT; }
        public void setVIT(int VIT) { this.VIT = VIT; }
        public int getSEN() { return SEN; }
        public void setSEN(int SEN) { this.SEN = SEN; }
        public int getFreePoints() { return freePoints; }
        public void setFreePoints(int freePoints) { this.freePoints = freePoints; }

        public static AttributesBuilder builder() {
            return new AttributesBuilder();
        }

        public static class AttributesBuilder {
            private int STR;
            private int INT;
            private int VIT;
            private int SEN;
            private int freePoints;

            public AttributesBuilder STR(int STR) { this.STR = STR; return this; }
            public AttributesBuilder INT(int INT) { this.INT = INT; return this; }
            public AttributesBuilder VIT(int VIT) { this.VIT = VIT; return this; }
            public AttributesBuilder SEN(int SEN) { this.SEN = SEN; return this; }
            public AttributesBuilder freePoints(int freePoints) { this.freePoints = freePoints; return this; }
            public Attributes build() {
                Attributes a = new Attributes();
                a.STR = this.STR;
                a.INT = this.INT;
                a.VIT = this.VIT;
                a.SEN = this.SEN;
                a.freePoints = this.freePoints;
                return a;
            }
        }
    }

    public static class Economy {
        private long gold;

        public long getGold() { return gold; }
        public void setGold(long gold) { this.gold = gold; }

        public static EconomyBuilder builder() {
            return new EconomyBuilder();
        }

        public static class EconomyBuilder {
            private long gold;

            public EconomyBuilder gold(long gold) { this.gold = gold; return this; }
            public Economy build() {
                Economy e = new Economy();
                e.gold = this.gold;
                return e;
            }
        }
    }

    public static class SystemState {
        private boolean penaltyActive;
        private List<String> activeBuffs;

        public boolean isPenaltyActive() { return penaltyActive; }
        public void setPenaltyActive(boolean penaltyActive) { this.penaltyActive = penaltyActive; }
        public List<String> getActiveBuffs() { return activeBuffs; }
        public void setActiveBuffs(List<String> activeBuffs) { this.activeBuffs = activeBuffs; }

        public static SystemStateBuilder builder() {
            return new SystemStateBuilder();
        }

        public static class SystemStateBuilder {
            private boolean penaltyActive;
            private List<String> activeBuffs;

            public SystemStateBuilder penaltyActive(boolean penaltyActive) { this.penaltyActive = penaltyActive; return this; }
            public SystemStateBuilder activeBuffs(List<String> activeBuffs) { this.activeBuffs = activeBuffs; return this; }
            public SystemState build() {
                SystemState s = new SystemState();
                s.penaltyActive = this.penaltyActive;
                s.activeBuffs = this.activeBuffs;
                return s;
            }
        }
    }
}
