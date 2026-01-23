package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Certificate Revocation Entry Entity
 *
 * Represents an entry in the Certificate Revocation List (CRL).
 * Tracks revoked device certificates for security enforcement.
 *
 * SECURITY:
 * - All revoked certificates must be added to CRL
 * - CRL is synced to DMZ server (Server 3) for validation
 * - Revoked certificates are permanently blocked
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 20, 2026
 */
@Entity
@Table(name = "certificate_revocations", indexes = {
    @Index(name = "idx_crl_serial", columnList = "serial_number", unique = true),
    @Index(name = "idx_crl_revoked_at", columnList = "revoked_at"),
    @Index(name = "idx_crl_device_id", columnList = "device_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateRevocationEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", nullable = false, unique = true, length = 50)
    private String serialNumber;

    @Column(name = "device_id", length = 20)
    private String deviceId;

    @Column(name = "account_token", length = 20)
    private String accountToken;

    @Column(name = "revoked_at", nullable = false)
    @Builder.Default
    private LocalDateTime revokedAt = LocalDateTime.now();

    @Column(name = "revoked_by", nullable = false, length = 100)
    private String revokedBy;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "revocation_type", nullable = false, length = 30)
    @Builder.Default
    private RevocationType revocationType = RevocationType.SECURITY_CONCERN;

    @Column(name = "certificate_fingerprint", length = 100)
    private String certificateFingerprint;

    @Column(name = "original_expires_at")
    private LocalDateTime originalExpiresAt;

    @Column(name = "synced_to_dmz")
    @Builder.Default
    private Boolean syncedToDmz = false;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    /**
     * Revocation type enum
     */
    public enum RevocationType {
        KEY_COMPROMISE,         // Private key compromised
        AFFILIATION_CHANGED,    // User/account relationship changed
        SUPERSEDED,             // Certificate replaced with new one
        CESSATION,              // No longer needed
        SECURITY_CONCERN,       // General security concern
        DEVICE_LOST,            // Device lost or stolen
        DEVICE_REMOVED,         // Device removed from account
        CERTIFICATE_EXPIRED,    // Expired but manually revoked
        ADMINISTRATIVE          // Admin decision
    }

    /**
     * Get display reason including type
     */
    @Transient
    public String getDisplayReason() {
        return revocationType.name().replace('_', ' ') + ": " + reason;
    }
}
