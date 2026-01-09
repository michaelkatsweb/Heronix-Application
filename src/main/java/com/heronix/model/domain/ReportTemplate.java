package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Report Template Entity
 *
 * Defines custom report templates that can be used to generate
 * standardized reports with specific configurations.
 *
 * Templates allow users to:
 * - Save frequently used report configurations
 * - Ensure consistent formatting and content
 * - Quickly generate reports without manual configuration
 * - Share report configurations across users/departments
 *
 * Template Types:
 * - DAILY_STANDARD - Standard daily attendance report
 * - WEEKLY_SUMMARY - Weekly attendance summary
 * - MONTHLY_OVERVIEW - Monthly attendance overview
 * - CUSTOM - User-defined custom template
 *
 * Configuration Options:
 * - Report type and format
 * - Date range parameters
 * - Included sections and metrics
 * - Styling and branding options
 * - Export format preferences
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 61 - Enhanced Report Export Capabilities
 */
@Entity
@Table(name = "report_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Template name (user-friendly display name)
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Template description
     */
    @Column(length = 500)
    private String description;

    /**
     * Template type
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TemplateType templateType;

    /**
     * Report type this template generates
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReportHistory.ReportType reportType;

    /**
     * Default export format
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportHistory.ReportFormat defaultFormat;

    /**
     * Template configuration (JSON)
     *
     * Stores template-specific settings such as:
     * - Date range parameters
     * - Included sections
     * - Styling options
     * - Custom filters
     */
    @Column(columnDefinition = "TEXT")
    private String configuration;

    /**
     * Template owner/creator
     */
    @Column(length = 100)
    private String createdBy;

    /**
     * Template creation timestamp
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Whether template is active/available
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Whether template is shared with all users
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean shared = false;

    /**
     * Whether this is a system template (non-deletable)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean systemTemplate = false;

    /**
     * Usage count (how many times template has been used)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    /**
     * Template version number
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    /**
     * Template category for organization
     */
    @Column(length = 50)
    private String category;

    /**
     * Tags for searching/filtering (comma-separated)
     */
    @Column(length = 200)
    private String tags;

    /**
     * Template Type Enumeration
     */
    public enum TemplateType {
        /**
         * Standard daily attendance report
         */
        DAILY_STANDARD,

        /**
         * Weekly attendance summary
         */
        WEEKLY_SUMMARY,

        /**
         * Monthly attendance overview
         */
        MONTHLY_OVERVIEW,

        /**
         * Quarterly attendance analysis
         */
        QUARTERLY_ANALYSIS,

        /**
         * Annual attendance report
         */
        ANNUAL_REPORT,

        /**
         * Grade-level specific template
         */
        GRADE_LEVEL,

        /**
         * Student-specific template
         */
        STUDENT_FOCUSED,

        /**
         * Chronic absenteeism template
         */
        CHRONIC_ABSENTEEISM,

        /**
         * Custom user-defined template
         */
        CUSTOM
    }

    /**
     * Lifecycle callback - set creation timestamp
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Lifecycle callback - update modification timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Increment usage count
     */
    public void incrementUsageCount() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
    }

    /**
     * Check if template is editable
     */
    public boolean isEditable() {
        return !systemTemplate;
    }

    /**
     * Check if template is deletable
     */
    public boolean isDeletable() {
        return !systemTemplate;
    }
}
