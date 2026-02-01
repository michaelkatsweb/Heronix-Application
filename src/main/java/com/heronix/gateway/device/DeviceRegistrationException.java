package com.heronix.gateway.device;

/**
 * Exception for device registration errors.
 *
 * @author Heronix Development Team
 * @version 1.0
 */
public class DeviceRegistrationException extends RuntimeException {

    public DeviceRegistrationException(String message) {
        super(message);
    }

    public DeviceRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
