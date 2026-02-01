package com.heronix.gateway.sanitization;

import lombok.*;
import java.util.Set;

/**
 * Context for data sanitization operations.
 *
 * @author Heronix Development Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanitizationContext {

    /**
     * Type of data being sanitized
     */
    private DataType dataType;

    /**
     * Purpose of the data transmission
     */
    private TransmissionPurpose purpose;

    /**
     * Additional fields to remove (beyond defaults)
     */
    private Set<String> additionalFieldsToRemove;

    /**
     * Whether to apply strict sanitization (removes more data)
     */
    @Builder.Default
    private boolean strictMode = true;

    /**
     * Whether to include metadata about sanitization
     */
    @Builder.Default
    private boolean includeMetadata = true;

    /**
     * Types of data that can be sanitized
     */
    public enum DataType {
        STUDENT_RECORD,
        ATTENDANCE_RECORD,
        GRADE_RECORD,
        NOTIFICATION,
        AGGREGATE_REPORT,
        SCHEDULE_DATA,
        COMPLIANCE_REPORT
    }

    /**
     * Purpose of the data transmission
     */
    public enum TransmissionPurpose {
        PARENT_NOTIFICATION,     // Notifications to parents
        DISTRICT_SYNC,           // Sync with district servers
        STATE_REPORTING,         // State compliance reporting
        BACKUP,                  // Disaster recovery
        ANALYTICS,               // External analytics
        AUDIT                    // Compliance audit
    }

    /**
     * Create context for parent notifications
     */
    public static SanitizationContext forParentNotification() {
        return SanitizationContext.builder()
            .dataType(DataType.NOTIFICATION)
            .purpose(TransmissionPurpose.PARENT_NOTIFICATION)
            .strictMode(true)
            .build();
    }

    /**
     * Create context for district sync
     */
    public static SanitizationContext forDistrictSync() {
        return SanitizationContext.builder()
            .dataType(DataType.STUDENT_RECORD)
            .purpose(TransmissionPurpose.DISTRICT_SYNC)
            .strictMode(true)
            .build();
    }

    /**
     * Create context for state reporting
     */
    public static SanitizationContext forStateReporting() {
        return SanitizationContext.builder()
            .dataType(DataType.COMPLIANCE_REPORT)
            .purpose(TransmissionPurpose.STATE_REPORTING)
            .strictMode(true)
            .build();
    }
}
