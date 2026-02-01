package com.heronix.gateway.encryption;

import lombok.*;
import java.time.Instant;

/**
 * Encrypted payload for secure transmission.
 *
 * @author Heronix Development Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncryptedPayload {

    /**
     * Encryption algorithm used
     */
    private String algorithm;

    /**
     * Base64-encoded encrypted data (includes IV)
     */
    private String encryptedData;

    /**
     * Length of the IV in bytes
     */
    private int ivLength;

    /**
     * Digital signature of the payload (optional)
     */
    private String signature;

    /**
     * Hash of the original plaintext (for integrity verification)
     */
    private String contentHash;

    /**
     * Timestamp when encrypted
     */
    @Builder.Default
    private long timestamp = Instant.now().toEpochMilli();

    /**
     * Payload version for compatibility
     */
    @Builder.Default
    private String version = "1.0";

    /**
     * Check if payload has a signature
     */
    public boolean isSigned() {
        return signature != null && !signature.isEmpty();
    }

    /**
     * Check if payload is still valid (within time window)
     */
    public boolean isValid(long maxAgeMillis) {
        long now = Instant.now().toEpochMilli();
        return (now - timestamp) <= maxAgeMillis;
    }
}
