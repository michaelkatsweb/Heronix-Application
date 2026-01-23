package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Registered Device Entity
 *
 * Represents a device that has been registered to sync with the SIS
 * via the DMZ server. Includes device authentication and certificate info.
 *
 * SECURITY:
 * - X.509 certificates (2048-bit RSA) for device authentication
 * - MAC address whitelisting for additional verification
 * - Device fingerprinting for fraud detection
 * - Maximum 5 devices per account
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 20, 2026
 */
@Entity
@Table(name = "registered_devices", indexes = {
    @Index(name = "idx_device_id", columnList = "device_id", unique = true),
    @Index(name = "idx_device_mac", columnList = "mac_address"),
    @Index(name = "idx_device_account", columnList = "account_token"),
    @Index(name = "idx_device_status", columnList = "status"),
    @Index(name = "idx_device_cert_serial", columnList = "certificate_serial_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisteredDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, unique = true, length = 20)
    private String deviceId;

    @Column(name = "account_token", nullable = false, length = 20)
    private String accountToken;

    @Column(name = "mac_address", nullable = false, length = 17)
    private String macAddress;

    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DeviceStatus status = DeviceStatus.PENDING_APPROVAL;

    @Column(name = "registration_requested_at", nullable = false)
    @Builder.Default
    private LocalDateTime registrationRequestedAt = LocalDateTime.now();

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejected_by", length = 100)
    private String rejectedBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoked_by", length = 100)
    private String revokedBy;

    @Column(name = "revocation_reason", length = 500)
    private String revocationReason;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    @Column(name = "removed_by", length = 100)
    private String removedBy;

    @Column(name = "certificate_serial_number", length = 50)
    private String certificateSerialNumber;

    @Column(name = "certificate_expires_at")
    private LocalDateTime certificateExpiresAt;

    @Column(name = "certificate_fingerprint", length = 100)
    private String certificateFingerprint;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    /**
     * Device status enum
     */
    public enum DeviceStatus {
        PENDING_APPROVAL,
        ACTIVE,
        SUSPENDED,
        REVOKED,
        REJECTED,
        REMOVED
    }

    /**
     * Check if device is currently active and valid
     */
    @Transient
    public boolean isActiveAndValid() {
        return status == DeviceStatus.ACTIVE &&
               (certificateExpiresAt == null || LocalDateTime.now().isBefore(certificateExpiresAt));
    }

    /**
     * Check if certificate is expired
     */
    @Transient
    public boolean isCertificateExpired() {
        return certificateExpiresAt != null && LocalDateTime.now().isAfter(certificateExpiresAt);
    }

    /**
     * Check if certificate is expiring soon (within 30 days)
     */
    @Transient
    public boolean isCertificateExpiringSoon() {
        return certificateExpiresAt != null &&
               certificateExpiresAt.isBefore(LocalDateTime.now().plusDays(30)) &&
               certificateExpiresAt.isAfter(LocalDateTime.now());
    }
}
