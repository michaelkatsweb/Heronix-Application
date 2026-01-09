package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Report Schedule Entity
 *
 * Defines automated report generation schedules for recurring reports.
 *
 * Features:
 * - Flexible scheduling (daily, weekly, monthly)
 * - Multiple report types and formats
 * - Email delivery configuration
 * - Active/inactive status management
 * - Execution tracking
 *
 * Schedule Frequencies:
 * - DAILY - Execute every day at specified time
 * - WEEKLY - Execute on specified day of week
 * - MONTHLY - Execute on specified day of month
 * - CUSTOM - Custom cron expression
 *
 * Use Cases:
 * - Daily attendance reports sent to administrators
 * - Weekly summary reports for teachers
 * - Monthly absenteeism reports for counselors
 * - Custom scheduled reports for specific needs
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 57 - Scheduled Report Generation
 */
@Entity
@Table(name = "report_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Schedule name for identification
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Schedule description
     */
    @Column(length = 500)
    private String description;

    /**
     * Report type to generate
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportHistory.ReportType reportType;

    /**
     * Report format to generate
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "report_format", nullable = false)
    private ReportHistory.ReportFormat reportFormat;

    /**
     * Schedule frequency
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ScheduleFrequency frequency = ScheduleFrequency.DAILY;

    /**
     * Execution time (for DAILY, WEEKLY, MONTHLY)
     */
    @Column(name = "execution_time")
    private LocalTime executionTime;

    /**
     * Day of week (1=Monday, 7=Sunday) for WEEKLY frequency
     */
    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    /**
     * Day of month (1-31) for MONTHLY frequency
     */
    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    /**
     * Custom cron expression for CUSTOM frequency
     */
    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    /**
     * Recipients (comma-separated email addresses)
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String recipients;

    /**
     * Whether this schedule is active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Date range start (null = use yesterday for daily reports)
     */
    @Column(name = "date_range_start")
    private Integer dateRangeStart;

    /**
     * Date range end (null = use today for daily reports)
     */
    @Column(name = "date_range_end")
    private Integer dateRangeEnd;

    /**
     * Additional parameters (JSON format)
     */
    @Column(columnDefinition = "TEXT")
    private String parameters;

    /**
     * Created by user
     */
    @Column(name = "created_by")
    private String createdBy;

    /**
     * Last execution timestamp
     */
    @Column(name = "last_execution")
    private LocalDateTime lastExecution;

    /**
     * Next scheduled execution timestamp
     */
    @Column(name = "next_execution")
    private LocalDateTime nextExecution;

    /**
     * Execution count
     */
    @Column(name = "execution_count")
    @Builder.Default
    private Integer executionCount = 0;

    /**
     * Failure count
     */
    @Column(name = "failure_count")
    @Builder.Default
    private Integer failureCount = 0;

    /**
     * Last execution status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "last_status")
    private ExecutionStatus lastStatus;

    /**
     * Last error message
     */
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Schedule Frequency Types
     */
    public enum ScheduleFrequency {
        /**
         * Execute every day at specified time
         */
        DAILY,

        /**
         * Execute on specified day of week
         */
        WEEKLY,

        /**
         * Execute on specified day of month
         */
        MONTHLY,

        /**
         * Execute based on custom cron expression
         */
        CUSTOM
    }

    /**
     * Execution Status
     */
    public enum ExecutionStatus {
        /**
         * Successfully executed
         */
        SUCCESS,

        /**
         * Execution failed
         */
        FAILED,

        /**
         * Currently executing
         */
        RUNNING,

        /**
         * Skipped execution
         */
        SKIPPED
    }

    /**
     * Check if schedule is due for execution
     *
     * @return True if schedule should execute now
     */
    public boolean isDue() {
        if (!active) {
            return false;
        }

        if (nextExecution == null) {
            return true; // Never executed, run now
        }

        return LocalDateTime.now().isAfter(nextExecution);
    }

    /**
     * Increment execution count
     */
    public void incrementExecutionCount() {
        this.executionCount = (this.executionCount == null ? 0 : this.executionCount) + 1;
    }

    /**
     * Increment failure count
     */
    public void incrementFailureCount() {
        this.failureCount = (this.failureCount == null ? 0 : this.failureCount) + 1;
    }

    /**
     * Reset failure count
     */
    public void resetFailureCount() {
        this.failureCount = 0;
    }
}
