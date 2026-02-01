package com.heronix.gateway.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Audit service for all gateway transmissions.
 *
 * Logs all transmission attempts (successful, blocked, and failed)
 * for compliance and security monitoring.
 *
 * @author Heronix Development Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransmissionAuditService {

    // In production, this would write to a dedicated audit database/table
    // For now, we log with a specific marker for easy filtering

    private static final String AUDIT_MARKER = "GATEWAY_AUDIT";

    /**
     * Log a successful transmission
     */
    public void logSuccessfulTransmission(String transmissionId, String deviceId,
                                           String dataType, int originalFieldCount,
                                           int sanitizedFieldCount) {
        log.info("[{}] SUCCESS - ID: {} | Device: {} | Type: {} | Fields: {} -> {} (sanitized)",
            AUDIT_MARKER, transmissionId, deviceId, dataType,
            originalFieldCount, sanitizedFieldCount);

        // In production: persist to audit table
        persistAuditRecord(AuditRecord.builder()
            .transmissionId(transmissionId)
            .deviceId(deviceId)
            .status("SUCCESS")
            .dataType(dataType)
            .originalFieldCount(originalFieldCount)
            .sanitizedFieldCount(sanitizedFieldCount)
            .timestamp(LocalDateTime.now())
            .build());
    }

    /**
     * Log a blocked transmission
     */
    public void logBlockedTransmission(String transmissionId, String deviceId,
                                        String reason, String details) {
        log.warn("[{}] BLOCKED - ID: {} | Device: {} | Reason: {} | Details: {}",
            AUDIT_MARKER, transmissionId, deviceId, reason, details);

        persistAuditRecord(AuditRecord.builder()
            .transmissionId(transmissionId)
            .deviceId(deviceId)
            .status("BLOCKED")
            .blockReason(reason)
            .blockDetails(details)
            .timestamp(LocalDateTime.now())
            .build());
    }

    /**
     * Log a failed transmission
     */
    public void logFailedTransmission(String transmissionId, String deviceId, String error) {
        log.error("[{}] FAILED - ID: {} | Device: {} | Error: {}",
            AUDIT_MARKER, transmissionId, deviceId, error);

        persistAuditRecord(AuditRecord.builder()
            .transmissionId(transmissionId)
            .deviceId(deviceId)
            .status("FAILED")
            .errorMessage(error)
            .timestamp(LocalDateTime.now())
            .build());
    }

    /**
     * Log a notification transmission
     */
    public void logNotificationTransmission(String transmissionId, String deviceId,
                                             String notificationType) {
        log.info("[{}] NOTIFICATION - ID: {} | Device: {} | Type: {}",
            AUDIT_MARKER, transmissionId, deviceId, notificationType);

        persistAuditRecord(AuditRecord.builder()
            .transmissionId(transmissionId)
            .deviceId(deviceId)
            .status("SUCCESS")
            .dataType("NOTIFICATION:" + notificationType)
            .timestamp(LocalDateTime.now())
            .build());
    }

    /**
     * Log an unregistered device access attempt
     */
    public void logUnregisteredDeviceAttempt(String attemptedDeviceId, String sourceIp) {
        log.warn("[{}] UNREGISTERED_DEVICE - Attempted ID: {} | Source IP: {}",
            AUDIT_MARKER, attemptedDeviceId, sourceIp);

        persistAuditRecord(AuditRecord.builder()
            .transmissionId("N/A")
            .deviceId(attemptedDeviceId)
            .status("UNREGISTERED_ATTEMPT")
            .sourceIp(sourceIp)
            .timestamp(LocalDateTime.now())
            .build());
    }

    // In production, this would save to a database
    private void persistAuditRecord(AuditRecord record) {
        // TODO: Implement database persistence
        // auditRepository.save(record);
    }

    /**
     * Internal audit record structure
     */
    @lombok.Data
    @lombok.Builder
    private static class AuditRecord {
        private String transmissionId;
        private String deviceId;
        private String status;
        private String dataType;
        private Integer originalFieldCount;
        private Integer sanitizedFieldCount;
        private String blockReason;
        private String blockDetails;
        private String errorMessage;
        private String sourceIp;
        private LocalDateTime timestamp;
    }
}
