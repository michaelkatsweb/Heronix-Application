package com.heronix.gateway.device;

import lombok.*;
import java.util.Set;

/**
 * Request DTO for device registration.
 *
 * @author Heronix Development Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRegistrationRequest {

    /**
     * Unique device identifier (UUID format recommended)
     */
    private String deviceId;

    /**
     * Human-readable device name
     */
    private String deviceName;

    /**
     * Type of device/system
     */
    private RegisteredDevice.DeviceType deviceType;

    /**
     * Organization that owns this device
     */
    private String organizationName;

    /**
     * Contact email for device administrator
     */
    private String adminEmail;

    /**
     * Public key certificate in PEM format
     */
    private String publicKeyCertificate;

    /**
     * Device fingerprint (hardware/software hash)
     */
    private String deviceFingerprint;

    /**
     * Allowed IP addresses/ranges (comma-separated CIDR notation)
     */
    private String allowedIpRanges;

    /**
     * Data permissions requested
     */
    private Set<RegisteredDevice.DataPermission> requestedPermissions;

    /**
     * Registration notes
     */
    private String notes;
}
