package com.heronix.gateway.device;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a registered external device that is authorized to receive data from the SIS
 * through the secure gateway.
 *
 * All external receiving devices (district servers, parent portals, etc.) must be
 * registered and verified before any data can be transmitted to them.
 *
 * Security Features:
 * - Certificate-based authentication
 * - Device fingerprinting
 * - Expiration and revocation support
 * - Granular data access permissions
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Secure Gateway Implementation
 */
@Entity(name = "GatewayRegisteredDevice")
@Table(name = "registered_devices", indexes = {
    @Index(name = "idx_gw_device_id", columnList = "device_id", unique = true),
    @Index(name = "idx_gw_public_key_hash", columnList = "public_key_hash"),
    @Index(name = "idx_gw_device_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisteredDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique device identifier (UUID format)
     */
    @Column(name = "device_id", nullable = false, unique = true, length = 64)
    private String deviceId;

    /**
     * Human-readable device name
     */
    @Column(name = "device_name", nullable = false, length = 255)
    private String deviceName;

    /**
     * Type of device/system
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 50)
    private DeviceType deviceType;

    /**
     * Organization that owns this device
     */
    @Column(name = "organization_name", nullable = false, length = 255)
    private String organizationName;

    /**
     * Contact email for device administrator
     */
    @Column(name = "admin_email", length = 255)
    private String adminEmail;

    /**
     * SHA-256 hash of the device's public key certificate
     */
    @Column(name = "public_key_hash", nullable = false, length = 64)
    private String publicKeyHash;

    /**
     * Full public key certificate in PEM format
     */
    @Column(name = "public_key_certificate", columnDefinition = "TEXT")
    private String publicKeyCertificate;

    /**
     * Device fingerprint (hardware/software hash)
     */
    @Column(name = "device_fingerprint", length = 128)
    private String deviceFingerprint;

    /**
     * Allowed IP addresses/ranges (comma-separated CIDR notation)
     */
    @Column(name = "allowed_ip_ranges", length = 1024)
    private String allowedIpRanges;

    /**
     * Device status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DeviceStatus status = DeviceStatus.PENDING_APPROVAL;

    /**
     * Data access permissions granted to this device
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "device_permissions", joinColumns = @JoinColumn(name = "device_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    @Builder.Default
    private Set<DataPermission> permissions = new HashSet<>();

    /**
     * Encryption key for this device (AES-256, encrypted at rest)
     */
    @Column(name = "encrypted_symmetric_key", length = 512)
    private String encryptedSymmetricKey;

    /**
     * When the device was registered
     */
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    /**
     * When the registration expires (requires renewal)
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * When the device was last verified/authenticated
     */
    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    /**
     * When data was last sent to this device
     */
    @Column(name = "last_data_transmission_at")
    private LocalDateTime lastDataTransmissionAt;

    /**
     * Number of successful data transmissions
     */
    @Column(name = "transmission_count")
    @Builder.Default
    private Long transmissionCount = 0L;

    /**
     * Number of failed transmission attempts
     */
    @Column(name = "failed_transmission_count")
    @Builder.Default
    private Long failedTransmissionCount = 0L;

    /**
     * Notes about this device registration
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Who approved this device
     */
    @Column(name = "approved_by", length = 255)
    private String approvedBy;

    /**
     * When the device was approved
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * Reason for revocation (if revoked)
     */
    @Column(name = "revocation_reason", columnDefinition = "TEXT")
    private String revocationReason;

    /**
     * When the device was revoked
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            // Default expiration: 1 year
            expiresAt = registeredAt.plusYears(1);
        }
    }

    /**
     * Check if device is currently active and valid
     */
    public boolean isActive() {
        return status == DeviceStatus.ACTIVE &&
               LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Check if device has a specific permission
     */
    public boolean hasPermission(DataPermission permission) {
        return permissions.contains(permission);
    }

    /**
     * Record a successful transmission
     */
    public void recordSuccessfulTransmission() {
        this.transmissionCount++;
        this.lastDataTransmissionAt = LocalDateTime.now();
    }

    /**
     * Record a failed transmission
     */
    public void recordFailedTransmission() {
        this.failedTransmissionCount++;
    }

    /**
     * Types of devices that can be registered
     */
    public enum DeviceType {
        DISTRICT_SERVER,        // State/district education servers
        PARENT_PORTAL,          // Parent notification systems
        EMAIL_RELAY,            // Email gateway servers
        SMS_GATEWAY,            // SMS notification gateway
        BACKUP_SERVER,          // Backup/disaster recovery
        ANALYTICS_PLATFORM,     // External analytics/reporting
        LMS_INTEGRATION,        // Learning Management System
        THIRD_PARTY_API,        // Other authorized integrations
        AUDIT_SYSTEM            // Compliance/audit systems
    }

    /**
     * Device registration status
     */
    public enum DeviceStatus {
        PENDING_APPROVAL,       // Awaiting admin approval
        ACTIVE,                 // Approved and operational
        SUSPENDED,              // Temporarily disabled
        REVOKED,                // Permanently disabled
        EXPIRED                 // Registration expired
    }

    /**
     * Granular data permissions
     */
    public enum DataPermission {
        // Student Data
        STUDENT_BASIC_INFO,         // Name, grade level (sanitized)
        STUDENT_CONTACT_INFO,       // Parent contact (sanitized)
        STUDENT_ATTENDANCE,         // Attendance records (anonymized)
        STUDENT_GRADES,             // Academic performance (aggregated)

        // Notification Types
        SEND_ATTENDANCE_ALERTS,     // Attendance notifications
        SEND_GRADE_UPDATES,         // Grade notifications
        SEND_EMERGENCY_ALERTS,      // Emergency communications
        SEND_GENERAL_NOTIFICATIONS, // General school notifications

        // Administrative Data
        AGGREGATE_STATISTICS,       // School-level statistics only
        COMPLIANCE_REPORTS,         // State compliance data

        // System Data
        SYNC_SCHEDULES,             // Schedule sync (limited data)
        AUDIT_LOGS                  // Access to audit trail
    }
}
