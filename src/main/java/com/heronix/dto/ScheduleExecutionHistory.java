package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Schedule Execution History DTO
 *
 * Tracks individual execution instances of scheduled reports.
 *
 * Features:
 * - Execution timestamp tracking
 * - Success/failure status
 * - Error message capture
 * - Execution duration
 * - Generated report reference
 * - Retry attempt tracking
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 65 - Report Scheduling & Automation Enhancements
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleExecutionHistory {

    /**
     * Execution status
     */
    public enum ExecutionStatus {
        PENDING,        // Queued for execution
        IN_PROGRESS,    // Currently executing
        COMPLETED,      // Successfully completed
        FAILED,         // Execution failed
        RETRYING,       // Retrying after failure
        CANCELLED       // Manually cancelled
    }

    /**
     * Unique execution ID
     */
    private Long executionId;

    /**
     * Schedule ID this execution belongs to
     */
    private Long scheduleId;

    /**
     * Schedule name
     */
    private String scheduleName;

    /**
     * Execution start time
     */
    private LocalDateTime startTime;

    /**
     * Execution end time
     */
    private LocalDateTime endTime;

    /**
     * Execution duration in milliseconds
     */
    private Long durationMs;

    /**
     * Execution status
     */
    private ExecutionStatus status;

    /**
     * Generated report ID (if successful)
     */
    private Long reportId;

    /**
     * Error message (if failed)
     */
    private String errorMessage;

    /**
     * Stack trace (if failed)
     */
    private String stackTrace;

    /**
     * Retry attempt number (0 = first attempt)
     */
    private Integer retryAttempt;

    /**
     * Number of records processed
     */
    private Integer recordsProcessed;

    /**
     * Report file size in bytes
     */
    private Long fileSizeBytes;

    /**
     * Whether report was emailed
     */
    private Boolean emailSent;

    /**
     * Number of email recipients
     */
    private Integer emailRecipientCount;

    /**
     * Triggered by (SCHEDULED, MANUAL, RETRY)
     */
    private String triggeredBy;

    /**
     * Additional execution metadata (JSON)
     */
    private String metadata;

    /**
     * Get execution duration in seconds
     */
    public Double getDurationSeconds() {
        if (durationMs == null) {
            return null;
        }
        return durationMs / 1000.0;
    }

    /**
     * Check if execution was successful
     */
    public boolean isSuccessful() {
        return status == ExecutionStatus.COMPLETED;
    }

    /**
     * Check if execution failed
     */
    public boolean isFailed() {
        return status == ExecutionStatus.FAILED;
    }

    /**
     * Get human-readable file size
     */
    public String getFormattedFileSize() {
        if (fileSizeBytes == null) {
            return "N/A";
        }

        if (fileSizeBytes < 1024) {
            return fileSizeBytes + " B";
        } else if (fileSizeBytes < 1024 * 1024) {
            return String.format("%.2f KB", fileSizeBytes / 1024.0);
        } else {
            return String.format("%.2f MB", fileSizeBytes / (1024.0 * 1024.0));
        }
    }
}
