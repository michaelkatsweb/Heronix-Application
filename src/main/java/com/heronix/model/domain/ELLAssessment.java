package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ELL Assessment Entity
 * Tracks English language proficiency assessments (ACCESS, WIDA, ELPA21, etc.)
 * Supports annual testing requirements and reclassification criteria
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "ell_assessments", indexes = {
    @Index(name = "idx_ell_assessment_student", columnList = "ell_student_id"),
    @Index(name = "idx_ell_assessment_date", columnList = "assessment_date"),
    @Index(name = "idx_ell_assessment_type", columnList = "assessment_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ELLAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ell_student_id", nullable = false)
    private ELLStudent ellStudent;

    // Assessment Details
    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type", nullable = false, length = 50)
    private AssessmentType assessmentType;

    @Column(name = "assessment_name", length = 200)
    private String assessmentName;

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;

    @Column(name = "school_year", length = 20)
    private String schoolYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_purpose", length = 50)
    private AssessmentPurpose assessmentPurpose;

    // Overall Results
    @Column(name = "overall_proficiency_level")
    private Integer overallProficiencyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_performance_level", length = 30)
    private ELLStudent.ProficiencyLevel overallPerformanceLevel;

    @Column(name = "composite_score")
    private Double compositeScore;

    @Column(name = "scale_score")
    private Integer scaleScore;

    // Domain Scores
    @Column(name = "listening_score")
    private Double listeningScore;

    @Column(name = "listening_level")
    private Integer listeningLevel;

    @Column(name = "speaking_score")
    private Double speakingScore;

    @Column(name = "speaking_level")
    private Integer speakingLevel;

    @Column(name = "reading_score")
    private Double readingScore;

    @Column(name = "reading_level")
    private Integer readingLevel;

    @Column(name = "writing_score")
    private Double writingScore;

    @Column(name = "writing_level")
    private Integer writingLevel;

    @Column(name = "comprehension_score")
    private Double comprehensionScore;

    @Column(name = "comprehension_level")
    private Integer comprehensionLevel;

    // Literacy Scores (for some assessments)
    @Column(name = "literacy_score")
    private Double literacyScore;

    @Column(name = "literacy_level")
    private Integer literacyLevel;

    @Column(name = "oral_language_score")
    private Double oralLanguageScore;

    @Column(name = "oral_language_level")
    private Integer oralLanguageLevel;

    // Progress and Comparison
    @Column(name = "growth_from_previous_year")
    private Double growthFromPreviousYear;

    @Column(name = "met_growth_target")
    private Boolean metGrowthTarget;

    @Column(name = "percentile_rank")
    private Integer percentileRank;

    // Reclassification Criteria
    @Column(name = "meets_reclassification_criteria")
    @Builder.Default
    private Boolean meetsReclassificationCriteria = false;

    @Column(name = "reclassification_threshold_score")
    private Double reclassificationThresholdScore;

    @Column(name = "reclassification_notes", columnDefinition = "TEXT")
    private String reclassificationNotes;

    // Administration Details
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrator_id")
    private Teacher administrator;

    @Column(name = "administrator_name", length = 100)
    private String administratorName;

    @Column(name = "administration_location", length = 200)
    private String administrationLocation;

    @Column(name = "accommodations_provided", columnDefinition = "TEXT")
    private String accommodationsProvided;

    // Reporting
    @Column(name = "results_received_date")
    private LocalDate resultsReceivedDate;

    @Column(name = "parent_notification_sent")
    @Builder.Default
    private Boolean parentNotificationSent = false;

    @Column(name = "parent_notification_date")
    private LocalDate parentNotificationDate;

    @Column(name = "report_card_updated")
    @Builder.Default
    private Boolean reportCardUpdated = false;

    @Column(name = "state_reported")
    @Builder.Default
    private Boolean stateReported = false;

    @Column(name = "state_reporting_date")
    private LocalDate stateReportingDate;

    // Additional Information
    @Column(name = "testing_window_start")
    private LocalDate testingWindowStart;

    @Column(name = "testing_window_end")
    private LocalDate testingWindowEnd;

    @Column(name = "valid_score")
    @Builder.Default
    private Boolean validScore = true;

    @Column(name = "invalidation_reason", columnDefinition = "TEXT")
    private String invalidationReason;

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

    public enum AssessmentType {
        ACCESS("ACCESS for ELLs"),
        WIDA_SCREENER("WIDA Screener"),
        WIDA_MODEL("WIDA MODEL"),
        ELPA21("ELPA21"),
        ELPAC("ELPAC - English Language Proficiency Assessments for California"),
        LAS_LINKS("LAS Links"),
        NYSESLAT("NYSESLAT - New York State ESL Achievement Test"),
        IPT("IPT - IDEA Proficiency Test"),
        CELDT("CELDT"),
        TELPAS("TELPAS - Texas English Language Proficiency Assessment System"),
        W_APT("W-APT - WIDA ACCESS Placement Test"),
        HOME_LANGUAGE_SURVEY("Home Language Survey"),
        INFORMAL_ASSESSMENT("Informal Assessment"),
        OTHER("Other");

        private final String displayName;

        AssessmentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum AssessmentPurpose {
        INITIAL_IDENTIFICATION("Initial Identification"),
        ANNUAL_PROFICIENCY("Annual Proficiency Testing"),
        PROGRESS_MONITORING("Progress Monitoring"),
        RECLASSIFICATION("Reclassification/Exit Assessment"),
        PLACEMENT("Program Placement"),
        SCREENING("Screening"),
        INTERIM("Interim Assessment"),
        DIAGNOSTIC("Diagnostic");

        private final String displayName;

        AssessmentPurpose(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

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
    public boolean isAnnualAssessment() {
        return assessmentPurpose == AssessmentPurpose.ANNUAL_PROFICIENCY;
    }

    @Transient
    public boolean needsParentNotification() {
        return !parentNotificationSent || parentNotificationDate == null;
    }

    @Transient
    public boolean isComplete() {
        return resultsReceivedDate != null && validScore;
    }

    @Transient
    public boolean showsProgress() {
        return growthFromPreviousYear != null && growthFromPreviousYear > 0;
    }

    @Transient
    public Student getStudent() {
        return ellStudent != null ? ellStudent.getStudent() : null;
    }

    @Transient
    public Double getAverageDomainScore() {
        int count = 0;
        double sum = 0.0;

        if (listeningScore != null) {
            sum += listeningScore;
            count++;
        }
        if (speakingScore != null) {
            sum += speakingScore;
            count++;
        }
        if (readingScore != null) {
            sum += readingScore;
            count++;
        }
        if (writingScore != null) {
            sum += writingScore;
            count++;
        }

        return count > 0 ? sum / count : null;
    }

    @Transient
    public boolean hasAllDomainScores() {
        return listeningScore != null &&
               speakingScore != null &&
               readingScore != null &&
               writingScore != null;
    }

    @Transient
    public int getDaysSinceAssessment() {
        if (assessmentDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - assessmentDate.toEpochDay());
    }

    // Alias methods for UI compatibility
    @Transient
    public String getTestForm() {
        return assessmentName;
    }

    public void setTestForm(String testForm) {
        this.assessmentName = testForm;
    }

    @Transient
    public String getTestVersion() {
        return schoolYear;
    }

    public void setTestVersion(String testVersion) {
        this.schoolYear = testVersion;
    }

    @Transient
    public String getPerformanceLevel() {
        return overallPerformanceLevel != null ? overallPerformanceLevel.name() : null;
    }

    public void setPerformanceLevel(String performanceLevel) {
        if (performanceLevel != null) {
            try {
                this.overallPerformanceLevel = ELLStudent.ProficiencyLevel.valueOf(performanceLevel);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid performance level: {}", performanceLevel);
            }
        }
    }

    @Transient
    public Double getLiteracyCompositeScore() {
        return literacyScore;
    }

    public void setLiteracyCompositeScore(Double score) {
        this.literacyScore = score;
    }

    @Transient
    public Double getOralLanguageCompositeScore() {
        return oralLanguageScore;
    }

    public void setOralLanguageCompositeScore(Double score) {
        this.oralLanguageScore = score;
    }

    @Transient
    public Double getGrowthScore() {
        return growthFromPreviousYear;
    }

    public void setGrowthScore(Double score) {
        this.growthFromPreviousYear = score;
    }

    @Transient
    public boolean isMetGrowthTarget() {
        return metGrowthTarget != null && metGrowthTarget;
    }

    @Transient
    public Double getReclassificationThreshold() {
        return reclassificationThresholdScore;
    }

    public void setReclassificationThreshold(Double threshold) {
        this.reclassificationThresholdScore = threshold;
    }

    @Transient
    public boolean isMeetsReclassificationCriteria() {
        return meetsReclassificationCriteria != null && meetsReclassificationCriteria;
    }

    @Transient
    public Teacher getTestAdministrator() {
        return administrator;
    }

    public void setTestAdministrator(Teacher teacher) {
        this.administrator = teacher;
        if (teacher != null) {
            this.administratorName = teacher.getFullName();
        }
    }

    @Transient
    public String getTestLocation() {
        return administrationLocation;
    }

    public void setTestLocation(String location) {
        this.administrationLocation = location;
    }

    @Transient
    public Integer getTestDuration() {
        // Calculate duration based on testing window if available
        if (testingWindowStart != null && testingWindowEnd != null) {
            return (int) (testingWindowEnd.toEpochDay() - testingWindowStart.toEpochDay());
        }
        return null;
    }

    public void setTestDuration(Integer duration) {
        // Store duration by setting testing window end based on start date
        if (duration != null && testingWindowStart != null) {
            this.testingWindowEnd = testingWindowStart.plusDays(duration);
        }
    }

    @Transient
    public boolean isParentNotificationSent() {
        return parentNotificationSent != null && parentNotificationSent;
    }

    @Transient
    public boolean isReportedToState() {
        return stateReported != null && stateReported;
    }

    public void setReportedToState(boolean reported) {
        this.stateReported = reported;
        if (reported && stateReportingDate == null) {
            this.stateReportingDate = LocalDate.now();
        }
    }

    @Transient
    public boolean isScoreValid() {
        return validScore != null && validScore;
    }

    public void setScoreValid(boolean valid) {
        this.validScore = valid;
    }

    @Transient
    public String getAdditionalNotes() {
        return notes;
    }

    public void setAdditionalNotes(String notes) {
        this.notes = notes;
    }
}
