package com.heronix.gateway.encryption;

/**
 * Exception for encryption/decryption errors.
 *
 * @author Heronix Development Team
 * @version 1.0
 */
public class EncryptionException extends RuntimeException {

    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
