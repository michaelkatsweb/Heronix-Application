package com.heronix.model.enums;

/**
 * Types of network status checks
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 */
public enum NetworkCheckType {
    PING("Ping", "ICMP ping test"),
    PORT_SCAN("Port Scan", "TCP port connectivity check"),
    DNS_LOOKUP("DNS Lookup", "DNS name resolution"),
    TRACE_ROUTE("Trace Route", "Network path trace"),
    SERVICE_CHECK("Service Check", "Application service health check"),
    HTTP_CHECK("HTTP Check", "HTTP/HTTPS endpoint check");

    private final String displayName;
    private final String description;

    NetworkCheckType(String displayName, String description) {
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
