package com.heronix.model.enums;

/**
 * Types of network devices that can be managed
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 */
public enum NetworkDeviceType {
    PRINTER("Printer", "Network printer or print server"),
    ACCESS_POINT("Access Point", "Wireless access point"),
    SWITCH("Switch", "Network switch"),
    ROUTER("Router", "Network router or gateway"),
    SERVER("Server", "Physical or virtual server"),
    WORKSTATION("Workstation", "Desktop computer or workstation"),
    FIREWALL("Firewall", "Network firewall appliance"),
    NAS("NAS", "Network attached storage"),
    VOIP_PHONE("VoIP Phone", "Voice over IP phone"),
    CAMERA("Camera", "IP security camera"),
    OTHER("Other", "Other network device");

    private final String displayName;
    private final String description;

    NetworkDeviceType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
