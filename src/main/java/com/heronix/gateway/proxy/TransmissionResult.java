package com.heronix.gateway.proxy;

import com.heronix.gateway.encryption.EncryptedPayload;
import lombok.*;

/**
 * Result of a transmission attempt through the secure gateway.
 *
 * @author Heronix Development Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransmissionResult {

    /**
     * Unique transmission ID for tracking
     */
    private String transmissionId;

    /**
     * Whether the transmission was prepared successfully
     */
    private boolean success;

    /**
     * Whether the transmission was blocked by security
     */
    private boolean blocked;

    /**
     * Error code if blocked or failed
     */
    private String errorCode;

    /**
     * Error message if blocked or failed
     */
    private String errorMessage;

    /**
     * The encrypted payload ready for transmission
     */
    private EncryptedPayload encryptedPayload;

    /**
     * Successful transmission
     */
    public static TransmissionResult success(String transmissionId, EncryptedPayload payload) {
        return TransmissionResult.builder()
            .transmissionId(transmissionId)
            .success(true)
            .blocked(false)
            .encryptedPayload(payload)
            .build();
    }

    /**
     * Blocked transmission (security policy violation)
     */
    public static TransmissionResult blocked(String transmissionId, String errorCode, String message) {
        return TransmissionResult.builder()
            .transmissionId(transmissionId)
            .success(false)
            .blocked(true)
            .errorCode(errorCode)
            .errorMessage(message)
            .build();
    }

    /**
     * Failed transmission (technical error)
     */
    public static TransmissionResult error(String transmissionId, String message) {
        return TransmissionResult.builder()
            .transmissionId(transmissionId)
            .success(false)
            .blocked(false)
            .errorCode("TRANSMISSION_ERROR")
            .errorMessage(message)
            .build();
    }
}
