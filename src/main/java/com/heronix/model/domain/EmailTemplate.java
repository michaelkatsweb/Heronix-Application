package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Email Template Entity
 *
 * Defines customizable email templates for automated report delivery
 * and system notifications.
 *
 * Features:
 * - Template variables (placeholders) for dynamic content
 * - Support for HTML and plain text formats
 * - Template versioning and activation status
 * - Audit tracking (created/updated timestamps)
 *
 * Template Variables:
 * - {{reportName}} - Report type name
 * - {{reportDate}} - Report generation date
 * - {{generatedBy}} - User who generated the report
 * - {{reportFormat}} - Report format (Excel, PDF)
 * - {{recordCount}} - Number of records in report
 * - {{fileName}} - Generated file name
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 56 - Email Template System
 */
@Entity
@Table(name = "email_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Template type - identifies the purpose of the template
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TemplateType templateType;

    /**
     * Template name for display purposes
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Email subject line (supports template variables)
     */
    @Column(nullable = false, length = 200)
    private String subject;

    /**
     * Email body content (supports template variables and HTML)
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /**
     * Whether this template is active/enabled
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Whether to send as HTML email
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean htmlFormat = true;

    /**
     * Template version (for versioning and rollback)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    /**
     * Template description
     */
    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Email Template Types
     */
    public enum TemplateType {
        /**
         * Template for daily attendance report delivery
         */
        DAILY_REPORT_NOTIFICATION,

        /**
         * Template for student summary report delivery
         */
        STUDENT_SUMMARY_NOTIFICATION,

        /**
         * Template for chronic absenteeism report delivery
         */
        CHRONIC_ABSENTEEISM_NOTIFICATION,

        /**
         * Template for report generation failure notification
         */
        REPORT_GENERATION_FAILURE,

        /**
         * Template for scheduled report delivery
         */
        SCHEDULED_REPORT_NOTIFICATION,

        /**
         * Generic report ready notification
         */
        REPORT_READY_NOTIFICATION
    }
}
