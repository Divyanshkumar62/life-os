package com.lifeos.player.dto;

import java.util.List;

public class StatusWindowResponse {

    private Identity identity;
    private Progression progression;
    private Attributes attributes;
    private Economy economy;
    private SystemState systemState;
    private List<String> systemMessages = new java.util.ArrayList<>();

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
    public List<String> getSystemMessages() {
        if (systemMessages == null) systemMessages = new java.util.ArrayList<>();
        return systemMessages;
    }
    public void setSystemMessages(List<String> systemMessages) { this.systemMessages = systemMessages; }

    public static StatusWindowBuilder builder() {
        return new StatusWindowBuilder();
    }

    public static class StatusWindowBuilder {
        private Identity identity;
        private Progression progression;
        private Attributes attributes;
        private Economy economy;
        private SystemState systemState;
        private List<String> systemMessages;

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
        public StatusWindowBuilder systemMessages(List<String> systemMessages) {
            this.systemMessages = systemMessages;
            return this;
        }
        public StatusWindowResponse build() {
            StatusWindowResponse response = new StatusWindowResponse();
            response.identity = this.identity;
            response.progression = this.progression;
            response.attributes = this.attributes;
            response.economy = this.economy;
            response.systemState = this.systemState;
            response.systemMessages = this.systemMessages != null ? this.systemMessages : new java.util.ArrayList<>();
            return response;
        }
    }

    public static class Identity {
        private int level;
        private String rank;
        private String title;
        private String equippedTheme;
        private String jobChangeStatus;

        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public String getRank() { return rank; }
        public void setRank(String rank) { this.rank = rank; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getEquippedTheme() { return equippedTheme; }
        public void setEquippedTheme(String equippedTheme) { this.equippedTheme = equippedTheme; }
        public String getJobChangeStatus() { return jobChangeStatus; }
        public void setJobChangeStatus(String jobChangeStatus) { this.jobChangeStatus = jobChangeStatus; }

        public static IdentityBuilder builder() {
            return new IdentityBuilder();
        }

        public static class IdentityBuilder {
            private int level;
            private String rank;
            private String title;
            private String equippedTheme;
            private String jobChangeStatus;

            public IdentityBuilder level(int level) { this.level = level; return this; }
            public IdentityBuilder rank(String rank) { this.rank = rank; return this; }
            public IdentityBuilder title(String title) { this.title = title; return this; }
            public IdentityBuilder equippedTheme(String equippedTheme) { this.equippedTheme = equippedTheme; return this; }
            public IdentityBuilder jobChangeStatus(String jobChangeStatus) { this.jobChangeStatus = jobChangeStatus; return this; }
            public Identity build() {
                Identity i = new Identity();
                i.level = this.level;
                i.rank = this.rank;
                i.title = this.title;
                i.equippedTheme = this.equippedTheme;
                i.jobChangeStatus = this.jobChangeStatus;
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
        private int AGI;
        private int freePoints;

        public int getSTR() { return STR; }
        public void setSTR(int STR) { this.STR = STR; }
        public int getINT() { return INT; }
        public void setINT(int INT) { this.INT = INT; }
        public int getVIT() { return VIT; }
        public void setVIT(int VIT) { this.VIT = VIT; }
        public int getSEN() { return SEN; }
        public void setSEN(int SEN) { this.SEN = SEN; }
        public int getAGI() { return AGI; }
        public void setAGI(int AGI) { this.AGI = AGI; }
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
            private int AGI;
            private int freePoints;

            public AttributesBuilder STR(int STR) { this.STR = STR; return this; }
            public AttributesBuilder INT(int INT) { this.INT = INT; return this; }
            public AttributesBuilder VIT(int VIT) { this.VIT = VIT; return this; }
            public AttributesBuilder SEN(int SEN) { this.SEN = SEN; return this; }
            public AttributesBuilder AGI(int AGI) { this.AGI = AGI; return this; }
            public AttributesBuilder freePoints(int freePoints) { this.freePoints = freePoints; return this; }
            public Attributes build() {
                Attributes a = new Attributes();
                a.STR = this.STR;
                a.INT = this.INT;
                a.VIT = this.VIT;
                a.SEN = this.SEN;
                a.AGI = this.AGI;
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
        private String wakeUpTime;
        private boolean dungeonBreakActive;
        private com.lifeos.project.dto.DungeonBreakEventDTO activeDungeonBreakEvent;

        public boolean isPenaltyActive() { return penaltyActive; }
        public void setPenaltyActive(boolean penaltyActive) { this.penaltyActive = penaltyActive; }
        public List<String> getActiveBuffs() { return activeBuffs; }
        public void setActiveBuffs(List<String> activeBuffs) { this.activeBuffs = activeBuffs; }
        public String getWakeUpTime() { return wakeUpTime; }
        public void setWakeUpTime(String wakeUpTime) { this.wakeUpTime = wakeUpTime; }
        public boolean isDungeonBreakActive() { return dungeonBreakActive; }
        public void setDungeonBreakActive(boolean dungeonBreakActive) { this.dungeonBreakActive = dungeonBreakActive; }
        public com.lifeos.project.dto.DungeonBreakEventDTO getActiveDungeonBreakEvent() { return activeDungeonBreakEvent; }
        public void setActiveDungeonBreakEvent(com.lifeos.project.dto.DungeonBreakEventDTO activeDungeonBreakEvent) { this.activeDungeonBreakEvent = activeDungeonBreakEvent; }

        public static SystemStateBuilder builder() {
            return new SystemStateBuilder();
        }

        public static class SystemStateBuilder {
            private boolean penaltyActive;
            private List<String> activeBuffs;
            private String wakeUpTime;
            private boolean dungeonBreakActive;
            private com.lifeos.project.dto.DungeonBreakEventDTO activeDungeonBreakEvent;

            public SystemStateBuilder penaltyActive(boolean penaltyActive) { this.penaltyActive = penaltyActive; return this; }
            public SystemStateBuilder activeBuffs(List<String> activeBuffs) { this.activeBuffs = activeBuffs; return this; }
            public SystemStateBuilder wakeUpTime(String wakeUpTime) { this.wakeUpTime = wakeUpTime; return this; }
            public SystemStateBuilder dungeonBreakActive(boolean dungeonBreakActive) { this.dungeonBreakActive = dungeonBreakActive; return this; }
            public SystemStateBuilder activeDungeonBreakEvent(com.lifeos.project.dto.DungeonBreakEventDTO activeDungeonBreakEvent) { this.activeDungeonBreakEvent = activeDungeonBreakEvent; return this; }
            public SystemState build() {
                SystemState s = new SystemState();
                s.penaltyActive = this.penaltyActive;
                s.activeBuffs = this.activeBuffs;
                s.wakeUpTime = this.wakeUpTime;
                s.dungeonBreakActive = this.dungeonBreakActive;
                s.activeDungeonBreakEvent = this.activeDungeonBreakEvent;
                return s;
            }
        }
    }
}
