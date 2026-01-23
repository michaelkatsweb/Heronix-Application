package com.heronix.model.enums;

/**
 * Status of a network device
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 */
public enum NetworkDeviceStatus {
    ONLINE("Online", "Device is responding normally", "#28a745"),
    OFFLINE("Offline", "Device is not responding", "#dc3545"),
    WARNING("Warning", "Device is responding but with issues", "#ffc107"),
    MAINTENANCE("Maintenance", "Device is under maintenance", "#17a2b8"),
    UNKNOWN("Unknown", "Device status has not been checked", "#6c757d");

    private final String displayName;
    private final String description;
    private final String color;

    NetworkDeviceStatus(String displayName, String description, String color) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getColor() {
        return color;
    }

    public boolean isHealthy() {
        return this == ONLINE;
    }

    public boolean requiresAttention() {
        return this == OFFLINE || this == WARNING;
    }
}
