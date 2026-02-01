package com.heronix.gateway.device;

import lombok.*;

/**
 * Result of device verification attempt.
 *
 * @author Heronix Development Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceVerificationResult {

    private boolean verified;
    private String errorCode;
    private String errorMessage;
    private RegisteredDevice device;

    // Error codes
    public static final String ERROR_UNREGISTERED = "DEVICE_NOT_REGISTERED";
    public static final String ERROR_INACTIVE = "DEVICE_INACTIVE";
    public static final String ERROR_INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String ERROR_IP_NOT_ALLOWED = "IP_NOT_ALLOWED";
    public static final String ERROR_EXPIRED = "DEVICE_EXPIRED";

    /**
     * Successful verification
     */
    public static DeviceVerificationResult success(RegisteredDevice device) {
        return DeviceVerificationResult.builder()
            .verified(true)
            .device(device)
            .build();
    }

    /**
     * Unregistered device
     */
    public static DeviceVerificationResult unregistered() {
        return DeviceVerificationResult.builder()
            .verified(false)
            .errorCode(ERROR_UNREGISTERED)
            .errorMessage("Device is not registered in the system")
            .build();
    }

    /**
     * Inactive device (suspended, revoked, expired)
     */
    public static DeviceVerificationResult inactive(String status) {
        return DeviceVerificationResult.builder()
            .verified(false)
            .errorCode(ERROR_INACTIVE)
            .errorMessage("Device registration is " + status.toLowerCase())
            .build();
    }

    /**
     * Invalid credentials (public key mismatch)
     */
    public static DeviceVerificationResult invalidCredentials() {
        return DeviceVerificationResult.builder()
            .verified(false)
            .errorCode(ERROR_INVALID_CREDENTIALS)
            .errorMessage("Device credentials do not match")
            .build();
    }

    /**
     * IP address not in allowed range
     */
    public static DeviceVerificationResult ipNotAllowed() {
        return DeviceVerificationResult.builder()
            .verified(false)
            .errorCode(ERROR_IP_NOT_ALLOWED)
            .errorMessage("Request IP address is not allowed for this device")
            .build();
    }
}
