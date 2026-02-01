package com.heronix.gateway.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heronix.gateway.device.*;
import com.heronix.gateway.encryption.*;
import com.heronix.gateway.sanitization.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Secure Outbound Proxy Service
 *
 * THE SINGLE GATEWAY FOR ALL EXTERNAL COMMUNICATIONS.
 *
 * This service is the ONLY authorized path for data to leave the SIS.
 * All external communications MUST flow through this service, which:
 *
 * 1. Verifies the receiving device is registered and authorized
 * 2. Sanitizes data to remove sensitive/identifying information
 * 3. Encrypts the sanitized data with device-specific keys
 * 4. Logs all transmission attempts for audit
 * 5. Enforces rate limiting and quotas
 *
 * NO DATA LEAVES THE SYSTEM WITHOUT PASSING THROUGH THIS GATEWAY.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Secure Gateway Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecureOutboundProxyService {

    private final DeviceRegistrationService deviceService;
    private final DataSanitizationService sanitizationService;
    private final GatewayEncryptionService encryptionService;
    private final TransmissionAuditService auditService;
    private final ObjectMapper objectMapper;

    /**
     * Transmit data to a registered device
     *
     * @param deviceId The target device ID
     * @param data The data to transmit (will be sanitized and encrypted)
     * @param context Sanitization context
     * @param sourceIp Source IP for verification
     * @param publicKeyHash Device's public key hash for verification
     * @return Transmission result
     */
    public TransmissionResult transmitToDevice(
            String deviceId,
            Map<String, Object> data,
            SanitizationContext context,
            String sourceIp,
            String publicKeyHash) {

        String transmissionId = UUID.randomUUID().toString();
        log.info("GATEWAY: Starting transmission {} to device {}", transmissionId, deviceId);

        try {
            // Step 1: Verify the device
            DeviceVerificationResult verification = deviceService.verifyDevice(
                deviceId, publicKeyHash, sourceIp
            );

            if (!verification.isVerified()) {
                log.warn("GATEWAY BLOCKED: Device verification failed - {} ({})",
                    deviceId, verification.getErrorCode());

                auditService.logBlockedTransmission(
                    transmissionId, deviceId, "VERIFICATION_FAILED",
                    verification.getErrorMessage()
                );

                return TransmissionResult.blocked(
                    transmissionId,
                    verification.getErrorCode(),
                    verification.getErrorMessage()
                );
            }

            RegisteredDevice device = verification.getDevice();

            // Step 2: Check permissions for this data type
            if (!hasRequiredPermissions(device, context)) {
                log.warn("GATEWAY BLOCKED: Device {} lacks required permissions for {}",
                    deviceId, context.getDataType());

                auditService.logBlockedTransmission(
                    transmissionId, deviceId, "INSUFFICIENT_PERMISSIONS",
                    "Missing permissions for " + context.getDataType()
                );

                return TransmissionResult.blocked(
                    transmissionId,
                    "INSUFFICIENT_PERMISSIONS",
                    "Device lacks required permissions for this data type"
                );
            }

            // Step 3: Sanitize the data
            log.debug("GATEWAY: Sanitizing data for transmission {}", transmissionId);
            Map<String, Object> sanitizedData = sanitizationService.sanitizeData(
                data, device, context
            );

            // Add transmission metadata
            sanitizedData.put("_transmissionId", transmissionId);
            sanitizedData.put("_transmittedAt", LocalDateTime.now().toString());

            // Step 4: Serialize to JSON
            String jsonData = objectMapper.writeValueAsString(sanitizedData);

            // Step 5: Get device's symmetric key and encrypt
            log.debug("GATEWAY: Encrypting data for transmission {}", transmissionId);
            String symmetricKey = deviceService.getDeviceSymmetricKey(deviceId);
            EncryptedPayload encrypted = encryptionService.encryptForDevice(jsonData, symmetricKey);

            // Add content hash for integrity verification
            encrypted.setContentHash(encryptionService.hash(jsonData));

            // Step 6: Record successful preparation
            deviceService.recordTransmission(deviceId, true);
            auditService.logSuccessfulTransmission(
                transmissionId, deviceId, context.getDataType().name(),
                data.size(), sanitizedData.size()
            );

            log.info("GATEWAY: Transmission {} prepared successfully for device {}",
                transmissionId, deviceId);

            return TransmissionResult.success(transmissionId, encrypted);

        } catch (Exception e) {
            log.error("GATEWAY ERROR: Transmission {} failed - {}", transmissionId, e.getMessage(), e);

            deviceService.recordTransmission(deviceId, false);
            auditService.logFailedTransmission(transmissionId, deviceId, e.getMessage());

            return TransmissionResult.error(transmissionId, e.getMessage());
        }
    }

    /**
     * Transmit a notification to a registered device
     */
    public TransmissionResult transmitNotification(
            String deviceId,
            NotificationContent notification,
            String sourceIp,
            String publicKeyHash) {

        String transmissionId = UUID.randomUUID().toString();
        log.info("GATEWAY: Starting notification transmission {} to device {}",
            transmissionId, deviceId);

        try {
            // Verify device
            DeviceVerificationResult verification = deviceService.verifyDevice(
                deviceId, publicKeyHash, sourceIp
            );

            if (!verification.isVerified()) {
                log.warn("GATEWAY BLOCKED: Notification transmission failed - device not verified");
                auditService.logBlockedTransmission(
                    transmissionId, deviceId, "VERIFICATION_FAILED",
                    verification.getErrorMessage()
                );
                return TransmissionResult.blocked(
                    transmissionId, verification.getErrorCode(), verification.getErrorMessage()
                );
            }

            RegisteredDevice device = verification.getDevice();

            // Check notification permissions
            if (!hasNotificationPermission(device, notification.getType())) {
                log.warn("GATEWAY BLOCKED: Device {} lacks notification permission", deviceId);
                auditService.logBlockedTransmission(
                    transmissionId, deviceId, "INSUFFICIENT_PERMISSIONS",
                    "Missing notification permission"
                );
                return TransmissionResult.blocked(
                    transmissionId, "INSUFFICIENT_PERMISSIONS",
                    "Device lacks permission for this notification type"
                );
            }

            // Sanitize notification
            NotificationContent sanitized = sanitizationService.sanitizeNotification(
                notification, device
            );

            // Convert to map for encryption
            Map<String, Object> notificationMap = new LinkedHashMap<>();
            notificationMap.put("type", sanitized.getType().name());
            notificationMap.put("subject", sanitized.getSubject());
            notificationMap.put("body", sanitized.getBody());
            notificationMap.put("priority", sanitized.getPriority().name());
            notificationMap.put("_transmissionId", transmissionId);
            notificationMap.put("_transmittedAt", LocalDateTime.now().toString());

            // Include recipient info only for delivery (not stored in audit)
            if (sanitized.getRecipientEmail() != null) {
                notificationMap.put("recipientEmail", sanitized.getRecipientEmail());
            }
            if (sanitized.getRecipientPhone() != null) {
                notificationMap.put("recipientPhone", sanitized.getRecipientPhone());
            }

            // Encrypt
            String jsonData = objectMapper.writeValueAsString(notificationMap);
            String symmetricKey = deviceService.getDeviceSymmetricKey(deviceId);
            EncryptedPayload encrypted = encryptionService.encryptForDevice(jsonData, symmetricKey);
            encrypted.setContentHash(encryptionService.hash(jsonData));

            // Record success
            deviceService.recordTransmission(deviceId, true);
            auditService.logNotificationTransmission(
                transmissionId, deviceId, notification.getType().name()
            );

            log.info("GATEWAY: Notification {} prepared successfully", transmissionId);
            return TransmissionResult.success(transmissionId, encrypted);

        } catch (Exception e) {
            log.error("GATEWAY ERROR: Notification transmission {} failed", transmissionId, e);
            deviceService.recordTransmission(deviceId, false);
            auditService.logFailedTransmission(transmissionId, deviceId, e.getMessage());
            return TransmissionResult.error(transmissionId, e.getMessage());
        }
    }

    /**
     * Transmit aggregate statistics to a device
     */
    public TransmissionResult transmitAggregateData(
            String deviceId,
            Map<String, Object> statistics,
            String sourceIp,
            String publicKeyHash) {

        String transmissionId = UUID.randomUUID().toString();

        try {
            // Verify device
            DeviceVerificationResult verification = deviceService.verifyDevice(
                deviceId, publicKeyHash, sourceIp
            );

            if (!verification.isVerified()) {
                return TransmissionResult.blocked(
                    transmissionId, verification.getErrorCode(), verification.getErrorMessage()
                );
            }

            RegisteredDevice device = verification.getDevice();

            // Sanitize aggregate data (applies k-anonymity protections)
            Map<String, Object> sanitized = sanitizationService.sanitizeAggregateData(
                statistics, device
            );

            if (sanitized.isEmpty()) {
                return TransmissionResult.blocked(
                    transmissionId, "NO_PERMISSION",
                    "Device lacks aggregate statistics permission"
                );
            }

            sanitized.put("_transmissionId", transmissionId);
            sanitized.put("_transmittedAt", LocalDateTime.now().toString());

            // Encrypt
            String jsonData = objectMapper.writeValueAsString(sanitized);
            String symmetricKey = deviceService.getDeviceSymmetricKey(deviceId);
            EncryptedPayload encrypted = encryptionService.encryptForDevice(jsonData, symmetricKey);
            encrypted.setContentHash(encryptionService.hash(jsonData));

            deviceService.recordTransmission(deviceId, true);
            auditService.logSuccessfulTransmission(
                transmissionId, deviceId, "AGGREGATE_STATISTICS",
                statistics.size(), sanitized.size()
            );

            return TransmissionResult.success(transmissionId, encrypted);

        } catch (Exception e) {
            log.error("GATEWAY ERROR: Aggregate transmission {} failed", transmissionId, e);
            deviceService.recordTransmission(deviceId, false);
            return TransmissionResult.error(transmissionId, e.getMessage());
        }
    }

    /**
     * Get list of all registered devices (for admin purposes)
     */
    public List<DeviceSummary> getRegisteredDevices() {
        List<RegisteredDevice> devices = deviceService.getActiveDevices();
        return devices.stream()
            .map(d -> new DeviceSummary(
                d.getDeviceId(),
                d.getDeviceName(),
                d.getDeviceType().name(),
                d.getOrganizationName(),
                d.getStatus().name(),
                d.getLastDataTransmissionAt(),
                d.getTransmissionCount()
            ))
            .toList();
    }

    /**
     * Check if a device would be allowed to receive specific data
     */
    public boolean canDeviceReceive(String deviceId, SanitizationContext.DataType dataType) {
        Optional<RegisteredDevice> deviceOpt = deviceService.getDevice(deviceId);

        if (deviceOpt.isEmpty()) {
            return false;
        }

        RegisteredDevice device = deviceOpt.get();
        if (!device.isActive()) {
            return false;
        }

        SanitizationContext context = SanitizationContext.builder()
            .dataType(dataType)
            .build();

        return hasRequiredPermissions(device, context);
    }

    // ================== Private Helper Methods ==================

    private boolean hasRequiredPermissions(RegisteredDevice device, SanitizationContext context) {
        return switch (context.getDataType()) {
            case STUDENT_RECORD ->
                device.hasPermission(RegisteredDevice.DataPermission.STUDENT_BASIC_INFO);
            case ATTENDANCE_RECORD ->
                device.hasPermission(RegisteredDevice.DataPermission.STUDENT_ATTENDANCE);
            case GRADE_RECORD ->
                device.hasPermission(RegisteredDevice.DataPermission.STUDENT_GRADES);
            case AGGREGATE_REPORT ->
                device.hasPermission(RegisteredDevice.DataPermission.AGGREGATE_STATISTICS);
            case COMPLIANCE_REPORT ->
                device.hasPermission(RegisteredDevice.DataPermission.COMPLIANCE_REPORTS);
            case SCHEDULE_DATA ->
                device.hasPermission(RegisteredDevice.DataPermission.SYNC_SCHEDULES);
            case NOTIFICATION ->
                hasAnyNotificationPermission(device);
        };
    }

    private boolean hasAnyNotificationPermission(RegisteredDevice device) {
        return device.hasPermission(RegisteredDevice.DataPermission.SEND_ATTENDANCE_ALERTS) ||
               device.hasPermission(RegisteredDevice.DataPermission.SEND_GRADE_UPDATES) ||
               device.hasPermission(RegisteredDevice.DataPermission.SEND_EMERGENCY_ALERTS) ||
               device.hasPermission(RegisteredDevice.DataPermission.SEND_GENERAL_NOTIFICATIONS);
    }

    private boolean hasNotificationPermission(RegisteredDevice device,
                                               NotificationContent.NotificationType type) {
        return switch (type) {
            case ATTENDANCE_ALERT ->
                device.hasPermission(RegisteredDevice.DataPermission.SEND_ATTENDANCE_ALERTS);
            case GRADE_UPDATE ->
                device.hasPermission(RegisteredDevice.DataPermission.SEND_GRADE_UPDATES);
            case EMERGENCY_NOTIFICATION ->
                device.hasPermission(RegisteredDevice.DataPermission.SEND_EMERGENCY_ALERTS);
            case GENERAL_ANNOUNCEMENT, SCHEDULE_CHANGE, PARENT_REMINDER ->
                device.hasPermission(RegisteredDevice.DataPermission.SEND_GENERAL_NOTIFICATIONS);
        };
    }

    /**
     * Summary of a registered device (for listing)
     */
    public record DeviceSummary(
        String deviceId,
        String deviceName,
        String deviceType,
        String organization,
        String status,
        LocalDateTime lastTransmission,
        Long transmissionCount
    ) {}
}
