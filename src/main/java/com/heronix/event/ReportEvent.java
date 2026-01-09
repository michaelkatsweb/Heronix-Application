package com.heronix.event;

import com.heronix.model.domain.ReportHistory;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Report Event
 *
 * Application event fired when significant report-related events occur.
 *
 * Events are used to trigger notifications, logging, and other
 * reactive behaviors in the system.
 *
 * Event Types:
 * - REPORT_GENERATED - Report successfully created
 * - REPORT_FAILED - Report generation failed
 * - BATCH_STARTED - Batch export started
 * - BATCH_COMPLETED - Batch export completed
 * - THRESHOLD_EXCEEDED - Attendance threshold exceeded
 * - SCHEDULE_EXECUTED - Scheduled report executed
 *
 * Event Flow:
 * 1. Report service fires event
 * 2. Event listeners receive event
 * 3. Notifications sent to users
 * 4. Analytics updated
 * 5. Logs recorded
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 62 - Real-time Report Notifications & Alerts
 */
@Getter
public class ReportEvent extends ApplicationEvent {

    private final ReportEventType eventType;
    private final String reportName;
    private final ReportHistory.ReportType reportType;
    private final String generatedBy;
    private final LocalDateTime eventTime;
    private final String message;
    private final Object additionalData;

    /**
     * Constructor for report events
     *
     * @param source Event source
     * @param eventType Type of event
     * @param reportName Name/description of report
     * @param reportType Type of report
     * @param generatedBy User who generated report
     * @param message Event message
     */
    public ReportEvent(Object source,
                      ReportEventType eventType,
                      String reportName,
                      ReportHistory.ReportType reportType,
                      String generatedBy,
                      String message) {
        this(source, eventType, reportName, reportType, generatedBy, message, null);
    }

    /**
     * Constructor with additional data
     *
     * @param source Event source
     * @param eventType Type of event
     * @param reportName Name/description of report
     * @param reportType Type of report
     * @param generatedBy User who generated report
     * @param message Event message
     * @param additionalData Optional additional data
     */
    public ReportEvent(Object source,
                      ReportEventType eventType,
                      String reportName,
                      ReportHistory.ReportType reportType,
                      String generatedBy,
                      String message,
                      Object additionalData) {
        super(source);
        this.eventType = eventType;
        this.reportName = reportName;
        this.reportType = reportType;
        this.generatedBy = generatedBy;
        this.eventTime = LocalDateTime.now();
        this.message = message;
        this.additionalData = additionalData;
    }

    /**
     * Report Event Type Enumeration
     */
    public enum ReportEventType {
        /**
         * Report successfully generated
         */
        REPORT_GENERATED,

        /**
         * Report generation failed
         */
        REPORT_FAILED,

        /**
         * Batch export started
         */
        BATCH_STARTED,

        /**
         * Batch export completed
         */
        BATCH_COMPLETED,

        /**
         * Batch export failed
         */
        BATCH_FAILED,

        /**
         * Attendance threshold exceeded
         */
        THRESHOLD_EXCEEDED,

        /**
         * Chronic absenteeism detected
         */
        CHRONIC_ABSENTEEISM_DETECTED,

        /**
         * Scheduled report executed
         */
        SCHEDULE_EXECUTED,

        /**
         * Scheduled report failed
         */
        SCHEDULE_FAILED,

        /**
         * Report exported via email
         */
        REPORT_EMAILED,

        /**
         * Report cached
         */
        REPORT_CACHED,

        /**
         * Large report generated (performance notice)
         */
        LARGE_REPORT_GENERATED
    }

    /**
     * Check if event is a failure type
     */
    public boolean isFailure() {
        return eventType == ReportEventType.REPORT_FAILED ||
               eventType == ReportEventType.BATCH_FAILED ||
               eventType == ReportEventType.SCHEDULE_FAILED;
    }

    /**
     * Check if event requires immediate attention
     */
    public boolean isUrgent() {
        return eventType == ReportEventType.THRESHOLD_EXCEEDED ||
               eventType == ReportEventType.CHRONIC_ABSENTEEISM_DETECTED ||
               eventType == ReportEventType.REPORT_FAILED;
    }

    @Override
    public String toString() {
        return String.format("ReportEvent{type=%s, report='%s', by='%s', at=%s}",
                eventType, reportName, generatedBy, eventTime);
    }
}
