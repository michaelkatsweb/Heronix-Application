package com.heronix.model.domain;

import com.heronix.model.enums.NetworkDeviceStatus;
import com.heronix.model.enums.NetworkDeviceType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Network Device Entity
 *
 * Represents a network device (printer, access point, switch, etc.) that can be
 * monitored and managed through the Network Panel. Supports status tracking,
 * ping checks, and historical monitoring.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 21, 2026
 */
@Entity
@Table(name = "network_devices", indexes = {
    @Index(name = "idx_network_device_ip", columnList = "ip_address", unique = true),
    @Index(name = "idx_network_device_mac", columnList = "mac_address"),
    @Index(name = "idx_network_device_type", columnList = "device_type"),
    @Index(name = "idx_network_device_status", columnList = "status"),
    @Index(name = "idx_network_device_location", columnList = "location")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "ip_address", nullable = false, unique = true, length = 45)
    private String ipAddress;

    @Column(name = "mac_address", length = 17)
    private String macAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 30)
    private NetworkDeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private NetworkDeviceStatus status = NetworkDeviceStatus.UNKNOWN;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "port", nullable = false)
    @Builder.Default
    private Integer port = 0;

    @Column(name = "hostname", length = 255)
    private String hostname;

    @Column(name = "snmp_community", length = 50)
    private String snmpCommunity;

    @Column(name = "monitoring_enabled", nullable = false)
    @Builder.Default
    private Boolean monitoringEnabled = true;

    @Column(name = "ping_interval_seconds", nullable = false)
    @Builder.Default
    private Integer pingIntervalSeconds = 60;

    @Column(name = "ping_timeout_ms", nullable = false)
    @Builder.Default
    private Integer pingTimeoutMs = 5000;

    @Column(name = "last_ping_time")
    private LocalDateTime lastPingTime;

    @Column(name = "last_ping_latency_ms")
    private Long lastPingLatencyMs;

    @Column(name = "last_status_change")
    private LocalDateTime lastStatusChange;

    @Column(name = "consecutive_failures")
    @Builder.Default
    private Integer consecutiveFailures = 0;

    @Column(name = "total_uptime_seconds")
    @Builder.Default
    private Long totalUptimeSeconds = 0L;

    @Column(name = "total_downtime_seconds")
    @Builder.Default
    private Long totalDowntimeSeconds = 0L;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "alert_on_offline", nullable = false)
    @Builder.Default
    private Boolean alertOnOffline = true;

    @Column(name = "alert_email", length = 255)
    private String alertEmail;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Check if device is currently online
     */
    @Transient
    public boolean isOnline() {
        return status == NetworkDeviceStatus.ONLINE;
    }

    /**
     * Check if device requires attention (offline or warning)
     */
    @Transient
    public boolean requiresAttention() {
        return status.requiresAttention();
    }

    /**
     * Calculate uptime percentage
     */
    @Transient
    public double getUptimePercentage() {
        long totalTime = totalUptimeSeconds + totalDowntimeSeconds;
        if (totalTime == 0) return 100.0;
        return (totalUptimeSeconds * 100.0) / totalTime;
    }

    /**
     * Get formatted uptime percentage
     */
    @Transient
    public String getFormattedUptimePercentage() {
        return String.format("%.2f%%", getUptimePercentage());
    }

    /**
     * Get formatted last ping latency
     */
    @Transient
    public String getFormattedLatency() {
        if (lastPingLatencyMs == null) return "N/A";
        return lastPingLatencyMs + " ms";
    }

    /**
     * Update status and track uptime/downtime
     */
    public void updateStatus(NetworkDeviceStatus newStatus) {
        if (this.status != newStatus) {
            LocalDateTime now = LocalDateTime.now();
            if (lastStatusChange != null) {
                long secondsSinceChange = java.time.Duration.between(lastStatusChange, now).getSeconds();
                if (this.status == NetworkDeviceStatus.ONLINE) {
                    totalUptimeSeconds += secondsSinceChange;
                } else if (this.status == NetworkDeviceStatus.OFFLINE) {
                    totalDowntimeSeconds += secondsSinceChange;
                }
            }
            this.lastStatusChange = now;
            this.status = newStatus;

            if (newStatus == NetworkDeviceStatus.ONLINE) {
                this.consecutiveFailures = 0;
            }
        }
    }

    /**
     * Record a successful ping
     */
    public void recordSuccessfulPing(long latencyMs) {
        this.lastPingTime = LocalDateTime.now();
        this.lastPingLatencyMs = latencyMs;
        this.consecutiveFailures = 0;
        updateStatus(NetworkDeviceStatus.ONLINE);
    }

    /**
     * Record a failed ping
     */
    public void recordFailedPing() {
        this.lastPingTime = LocalDateTime.now();
        this.lastPingLatencyMs = null;
        this.consecutiveFailures++;
        if (consecutiveFailures >= 3) {
            updateStatus(NetworkDeviceStatus.OFFLINE);
        } else {
            updateStatus(NetworkDeviceStatus.WARNING);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
