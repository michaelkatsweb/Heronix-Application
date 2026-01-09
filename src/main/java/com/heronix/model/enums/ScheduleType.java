// FILE: ScheduleType.java
// LOCATION: /src/main/java/com/heronix/model/enums/ScheduleType.java
package com.heronix.model.enums;

public enum ScheduleType {
    TRADITIONAL("Traditional (6-8 periods)"),
    BLOCK("Block Schedule (4 periods)"),
    ROTATING("Rotating A/B Schedule"),
    MODULAR("Modular (Short periods)"),
    TRIMESTER("Trimester System"),
    QUARTER("Quarter System"),
    FLEX_MOD("Flexible Modular"),
    MASTER("Master Schedule");

    private final String displayName;

    ScheduleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}