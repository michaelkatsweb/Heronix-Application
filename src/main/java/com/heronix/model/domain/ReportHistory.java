package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Report History Entity
 *
 * Tracks generated attendance reports for audit trail and history.
 * Stores metadata about each report generation including type, format,
 * parameters, file location, and generation status.
 *
 * Report Types:
 * - DAILY_ATTENDANCE - Daily attendance report
 * - STUDENT_SUMMARY - Student attendance summary
 * - CHRONIC_ABSENTEEISM - Chronic absenteeism report
 * - WEEKLY_SUMMARY - Weekly attendance summary
 * - MONTHLY_SUMMARY - Monthly attendance summary
 *
 * Report Formats:
 * - EXCEL - Microsoft Excel (.xlsx)
 * - PDF - Portable Document Format (.pdf)
 * - CSV - Comma Separated Values (.csv)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 50 - Report API Endpoints
 */
@Entity
@Table(name = "report_history", indexes = {
    @Index(name = "idx_report_type_generated", columnList = "report_type, generated_at"),
    @Index(name = "idx_generated_by_date", columnList = "generated_by, generated_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_format", nullable = false)
    private ReportFormat reportFormat;

    @Column(name = "report_name", nullable = false)
    private String reportName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "start_date")
    private java.time.LocalDate startDate;

    @Column(name = "end_date")
    private java.time.LocalDate endDate;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    @Column(name = "generated_by")
    private String generatedBy;

    @Column(name = "generated_at", nullable = false)
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "scheduled")
    @Builder.Default
    private Boolean scheduled = false;

    @Column(name = "emailed")
    @Builder.Default
    private Boolean emailed = false;

    @Column(name = "download_count")
    @Builder.Default
    private Integer downloadCount = 0;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    /**
     * Report Types
     */
    public enum ReportType {
        DAILY_ATTENDANCE,
        STUDENT_SUMMARY,
        CHRONIC_ABSENTEEISM,
        WEEKLY_SUMMARY,
        MONTHLY_SUMMARY,
        CUSTOM
    }

    /**
     * Report Formats
     */
    public enum ReportFormat {
        EXCEL,
        PDF,
        CSV,
        JSON
    }

    /**
     * Report Generation Status
     */
    public enum ReportStatus {
        PENDING,
        GENERATING,
        COMPLETED,
        FAILED,
        ARCHIVED,
        DELETED
    }

    // ========================================================================
    // JPA LIFECYCLE CALLBACKS
    // ========================================================================

    /**
     * Called before entity is persisted to database
     */
    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}
