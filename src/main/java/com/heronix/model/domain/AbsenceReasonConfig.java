package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Absence Reason Configuration Entity
 * Configurable absence reasons with categorization, documentation requirements, and reporting rules
 * Supports excused/unexcused classification, truancy tracking, and automated notifications
 *
 * Note: This is the configuration entity for absence reasons. The actual absence reason enum
 * used for substitute assignments is com.heronix.model.enums.AbsenceReason
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 26, 2025
 */
@Slf4j
@Entity
@Table(name = "absence_reasons", indexes = {
    @Index(name = "idx_absence_reason_code", columnList = "code", unique = true),
    @Index(name = "idx_absence_reason_category", columnList = "category"),
    @Index(name = "idx_absence_reason_excused", columnList = "excused"),
    @Index(name = "idx_absence_reason_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceReasonConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Information
    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "description", nullable = false, length = 100)
    private String description;

    @Column(name = "long_description", columnDefinition = "TEXT")
    private String longDescription;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    // Categorization
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "excused", nullable = false)
    @Builder.Default
    private boolean excused = false;

    @Column(name = "counts_toward_truancy", nullable = false)
    @Builder.Default
    private boolean countsTowardTruancy = false;

    @Column(name = "affects_attendance_rate", nullable = false)
    @Builder.Default
    private boolean affectsAttendanceRate = true;

    // Documentation Requirements
    @Column(name = "requires_documentation")
    @Builder.Default
    private boolean requiresDocumentation = false;

    @Column(name = "document_type", length = 200)
    private String documentType;

    @Column(name = "max_days_without_docs")
    private Integer maxDaysWithoutDocs;

    @Column(name = "auto_approve")
    @Builder.Default
    private boolean autoApprove = false;

    @Column(name = "auto_excuse_school_activities")
    @Builder.Default
    private boolean autoExcuseSchoolActivities = false;

    // State Reporting
    @Column(name = "state_code", length = 20)
    private String stateCode;

    @Column(name = "federal_code", length = 20)
    private String federalCode;

    @Column(name = "include_in_state_reports")
    @Builder.Default
    private boolean includeInStateReports = true;

    @Column(name = "chronic_absence_indicator")
    @Builder.Default
    private boolean chronicAbsenceIndicator = true;

    // Notifications & Automated Actions
    @Column(name = "notify_parents")
    @Builder.Default
    private boolean notifyParents = true;

    @Column(name = "notify_administration")
    @Builder.Default
    private boolean notifyAdministration = false;

    @Column(name = "notification_template", length = 100)
    private String notificationTemplate;

    @Column(name = "trigger_intervention")
    @Builder.Default
    private boolean triggerIntervention = false;

    // Additional Settings
    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "color_code", length = 7)
    private String colorCode;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper Methods

    @Transient
    public boolean shouldNotifyParents() {
        return notifyParents && active;
    }

    @Transient
    public boolean shouldNotifyAdministration() {
        return notifyAdministration && active;
    }

    @Transient
    public boolean needsDocumentation() {
        return requiresDocumentation && active;
    }

    @Transient
    public boolean canAutoExcuse() {
        return autoApprove && requiresDocumentation;
    }

    @Transient
    public boolean contributesToTruancy() {
        return countsTowardTruancy && !excused && active;
    }

    @Transient
    public String getFullDescription() {
        return code + " - " + description;
    }

    @Transient
    public String getDisplayName() {
        return description != null ? description : code;
    }

    @Transient
    public boolean isSchoolActivity() {
        return "School Activity".equalsIgnoreCase(category);
    }

    @Transient
    public boolean isMedical() {
        return category != null && category.toLowerCase().contains("medical");
    }

    @Transient
    public boolean isDisciplinary() {
        return category != null &&
               (category.toLowerCase().contains("suspension") ||
                category.toLowerCase().contains("disciplinary"));
    }

    @Override
    public String toString() {
        return code + " - " + description;
    }
}
