package com.heronix.dto;

import com.heronix.model.domain.ReportHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Report Schedule Configuration DTO
 *
 * Comprehensive configuration object for scheduling automated report generation.
 *
 * Supports Multiple Scheduling Patterns:
 * - Daily: Execute every N days at specific time
 * - Weekly: Execute on specific days of week
 * - Monthly: Execute on specific day of month
 * - Custom: Full cron expression support
 *
 * Features:
 * - Start/end date boundaries
 * - Execution time configuration
 * - Report parameter templates
 * - Email distribution lists
 * - Retry configuration
 * - Pause/resume capability
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 65 - Report Scheduling & Automation Enhancements
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportScheduleConfig {

    /**
     * Schedule frequency type
     */
    public enum ScheduleFrequency {
        DAILY,          // Every N days
        WEEKLY,         // Specific days of week
        MONTHLY,        // Specific day of month
        CUSTOM_CRON     // Custom cron expression
    }

    /**
     * Schedule execution status
     */
    public enum ScheduleStatus {
        ACTIVE,         // Currently running
        PAUSED,         // Temporarily suspended
        DISABLED,       // Permanently disabled
        COMPLETED       // End date reached
    }

    // ============================================================
    // Basic Schedule Information
    // ============================================================

    /**
     * Unique schedule identifier
     */
    private Long scheduleId;

    /**
     * Schedule name/description
     */
    private String scheduleName;

    /**
     * Report type to generate
     */
    private ReportHistory.ReportType reportType;

    /**
     * Report format (PDF, Excel, CSV)
     */
    private ReportHistory.ReportFormat reportFormat;

    /**
     * Schedule frequency
     */
    private ScheduleFrequency frequency;

    /**
     * Current schedule status
     */
    private ScheduleStatus status;

    /**
     * Schedule owner username
     */
    private String createdBy;

    // ============================================================
    // Timing Configuration
    // ============================================================

    /**
     * Start date for schedule (inclusive)
     */
    private LocalDate startDate;

    /**
     * End date for schedule (inclusive, null = indefinite)
     */
    private LocalDate endDate;

    /**
     * Time of day to execute (24-hour format)
     */
    private LocalTime executionTime;

    /**
     * Timezone for execution time (e.g., "America/New_York")
     */
    private String timezone;

    // ============================================================
    // Frequency-Specific Configuration
    // ============================================================

    /**
     * For DAILY: Execute every N days (default: 1)
     */
    private Integer intervalDays;

    /**
     * For WEEKLY: Days of week to execute
     */
    private List<DayOfWeek> daysOfWeek;

    /**
     * For MONTHLY: Day of month to execute (1-31, or -1 for last day)
     */
    private Integer dayOfMonth;

    /**
     * For CUSTOM_CRON: Full cron expression
     * Format: "second minute hour day month dayOfWeek"
     * Example: "0 0 9 * * MON-FRI" (9 AM weekdays)
     */
    private String cronExpression;

    // ============================================================
    // Report Parameters
    // ============================================================

    /**
     * Report-specific parameters
     * Examples:
     * - gradeLevel: "9th Grade"
     * - dateRange: "LAST_30_DAYS"
     * - includeExcused: "true"
     */
    private Map<String, String> reportParameters;

    /**
     * Whether to use dynamic date ranges
     * (e.g., "last week" instead of fixed dates)
     */
    private Boolean useDynamicDates;

    // ============================================================
    // Distribution Configuration
    // ============================================================

    /**
     * Email recipients for generated reports
     */
    private List<String> emailRecipients;

    /**
     * Whether to attach report file to email
     */
    private Boolean attachToEmail;

    /**
     * Whether to save report to history
     */
    private Boolean saveToHistory;

    /**
     * Custom email subject template
     */
    private String emailSubject;

    /**
     * Custom email body template
     */
    private String emailBody;

    // ============================================================
    // Retry & Error Handling
    // ============================================================

    /**
     * Number of retry attempts on failure (default: 3)
     */
    private Integer maxRetries;

    /**
     * Retry delay in minutes (default: 5)
     */
    private Integer retryDelayMinutes;

    /**
     * Whether to send notification on failure
     */
    private Boolean notifyOnFailure;

    /**
     * Whether to send notification on success
     */
    private Boolean notifyOnSuccess;

    // ============================================================
    // Execution Tracking
    // ============================================================

    /**
     * Last successful execution timestamp
     */
    private java.time.LocalDateTime lastExecutionTime;

    /**
     * Last execution status
     */
    private String lastExecutionStatus;

    /**
     * Next scheduled execution time
     */
    private java.time.LocalDateTime nextExecutionTime;

    /**
     * Total number of executions
     */
    private Integer totalExecutions;

    /**
     * Number of successful executions
     */
    private Integer successfulExecutions;

    /**
     * Number of failed executions
     */
    private Integer failedExecutions;

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Check if schedule is currently active
     */
    public boolean isActive() {
        if (status != ScheduleStatus.ACTIVE) {
            return false;
        }

        LocalDate today = LocalDate.now();
        if (startDate != null && today.isBefore(startDate)) {
            return false;
        }

        if (endDate != null && today.isAfter(endDate)) {
            return false;
        }

        return true;
    }

    /**
     * Check if schedule should execute today
     */
    public boolean shouldExecuteToday() {
        if (!isActive()) {
            return false;
        }

        LocalDate today = LocalDate.now();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();

        return switch (frequency) {
            case DAILY -> {
                if (intervalDays == null || intervalDays <= 0) {
                    yield true; // Every day
                }
                // Check if enough days have passed since start date
                if (startDate == null) {
                    yield true;
                }
                long daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(startDate, today);
                yield daysSinceStart % intervalDays == 0;
            }
            case WEEKLY -> daysOfWeek != null && daysOfWeek.contains(todayDayOfWeek);
            case MONTHLY -> dayOfMonth != null &&
                    (dayOfMonth == today.getDayOfMonth() ||
                            (dayOfMonth == -1 && today.getDayOfMonth() == today.lengthOfMonth()));
            case CUSTOM_CRON -> true; // Cron expression handled by scheduler
        };
    }

    /**
     * Get human-readable schedule description
     */
    public String getScheduleDescription() {
        return switch (frequency) {
            case DAILY -> intervalDays == null || intervalDays == 1 ?
                    "Daily at " + executionTime :
                    "Every " + intervalDays + " days at " + executionTime;
            case WEEKLY -> "Weekly on " + daysOfWeek + " at " + executionTime;
            case MONTHLY -> dayOfMonth == -1 ?
                    "Last day of month at " + executionTime :
                    "Day " + dayOfMonth + " of month at " + executionTime;
            case CUSTOM_CRON -> "Custom schedule: " + cronExpression;
        };
    }
}
