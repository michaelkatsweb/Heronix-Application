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
 * Gifted Assessment Entity
 * Tracks screening, assessment, and evaluation for gifted identification
 * Supports multiple assessment types and composite scoring
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "gifted_assessments", indexes = {
    @Index(name = "idx_gifted_assessment_student", columnList = "gifted_student_id"),
    @Index(name = "idx_gifted_assessment_date", columnList = "assessment_date"),
    @Index(name = "idx_gifted_assessment_type", columnList = "assessment_type"),
    @Index(name = "idx_gifted_assessment_purpose", columnList = "assessment_purpose")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftedAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gifted_student_id", nullable = false)
    private GiftedStudent giftedStudent;

    // Assessment Details
    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type", nullable = false, length = 50)
    private AssessmentType assessmentType;

    @Column(name = "assessment_name", length = 200)
    private String assessmentName;

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_purpose", length = 50)
    private AssessmentPurpose assessmentPurpose;

    // Scores
    @Column(name = "overall_score")
    private Double overallScore;

    @Column(name = "composite_score")
    private Double compositeScore;

    @Column(name = "percentile_rank")
    private Integer percentileRank;

    @Column(name = "standard_score")
    private Integer standardScore;

    @Column(name = "scaled_score")
    private Integer scaledScore;

    // IQ/Cognitive Assessment
    @Column(name = "full_scale_iq")
    private Integer fullScaleIq;

    @Column(name = "verbal_iq")
    private Integer verbalIq;

    @Column(name = "performance_iq")
    private Integer performanceIq;

    @Column(name = "working_memory_index")
    private Integer workingMemoryIndex;

    @Column(name = "processing_speed_index")
    private Integer processingSpeedIndex;

    // Achievement Scores
    @Column(name = "reading_score")
    private Double readingScore;

    @Column(name = "reading_percentile")
    private Integer readingPercentile;

    @Column(name = "math_score")
    private Double mathScore;

    @Column(name = "math_percentile")
    private Integer mathPercentile;

    @Column(name = "writing_score")
    private Double writingScore;

    @Column(name = "writing_percentile")
    private Integer writingPercentile;

    @Column(name = "science_score")
    private Double scienceScore;

    @Column(name = "science_percentile")
    private Integer sciencePercentile;

    // Creativity/Other Scores
    @Column(name = "creativity_score")
    private Double creativityScore;

    @Column(name = "creativity_percentile")
    private Integer creativityPercentile;

    @Column(name = "leadership_score")
    private Double leadershipScore;

    @Column(name = "arts_score")
    private Double artsScore;

    // Eligibility
    @Column(name = "meets_eligibility_criteria")
    @Builder.Default
    private Boolean meetsEligibilityCriteria = false;

    @Column(name = "eligibility_threshold_score")
    private Double eligibilityThresholdScore;

    @Column(name = "recommended_for_services")
    private Boolean recommendedForServices;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_area", length = 50)
    private GiftedStudent.GiftedArea recommendedArea;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    // Administration Details
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrator_id")
    private Teacher administrator;

    @Column(name = "administrator_name", length = 100)
    private String administratorName;

    @Column(name = "administration_location", length = 200)
    private String administrationLocation;

    @Column(name = "testing_conditions", columnDefinition = "TEXT")
    private String testingConditions;

    // Results and Reporting
    @Column(name = "results_received_date")
    private LocalDate resultsReceivedDate;

    @Column(name = "parent_notification_sent")
    @Builder.Default
    private Boolean parentNotificationSent = false;

    @Column(name = "parent_notification_date")
    private LocalDate parentNotificationDate;

    @Column(name = "valid_score")
    @Builder.Default
    private Boolean validScore = true;

    @Column(name = "invalidation_reason", columnDefinition = "TEXT")
    private String invalidationReason;

    @Column(name = "interpretation", columnDefinition = "TEXT")
    private String interpretation;

    @Column(name = "strengths_identified", columnDefinition = "TEXT")
    private String strengthsIdentified;

    @Column(name = "areas_for_development", columnDefinition = "TEXT")
    private String areasForDevelopment;

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
        COGNITIVE_IQ("Cognitive/IQ Assessment"),
        ACHIEVEMENT("Achievement Test"),
        CREATIVITY("Creativity Assessment"),
        APTITUDE("Aptitude Test"),
        SCREENING("Screening Tool"),
        PORTFOLIO("Portfolio Assessment"),
        TEACHER_RATING("Teacher Rating Scale"),
        PARENT_RATING("Parent Rating Scale"),
        PERFORMANCE_TASK("Performance Task"),
        OBSERVATION("Observation"),
        INTERVIEW("Interview"),
        NAGLIERI("Naglieri Nonverbal Ability Test"),
        COGAT("CogAT - Cognitive Abilities Test"),
        RAVENS("Raven's Progressive Matrices"),
        WISC("WISC - Wechsler Intelligence Scale"),
        WPPSI("WPPSI - Wechsler Preschool Primary Scale"),
        STANFORD_BINET("Stanford-Binet Intelligence Scales"),
        OLSAT("OLSAT - Otis-Lennon School Ability Test"),
        TORRANCE("Torrance Tests of Creative Thinking"),
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
        INITIAL_SCREENING("Initial Screening"),
        COMPREHENSIVE_EVALUATION("Comprehensive Evaluation"),
        ELIGIBILITY_DETERMINATION("Eligibility Determination"),
        PROGRESS_MONITORING("Progress Monitoring"),
        RE_EVALUATION("Re-evaluation"),
        PROGRAM_PLACEMENT("Program Placement"),
        IDENTIFICATION("Identification"),
        UNIVERSAL_SCREENING("Universal Screening");

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
    public boolean isComplete() {
        return resultsReceivedDate != null && validScore;
    }

    @Transient
    public boolean needsParentNotification() {
        return !parentNotificationSent || parentNotificationDate == null;
    }

    @Transient
    public boolean meetsHighGiftedCriteria() {
        return fullScaleIq != null && fullScaleIq >= 130;
    }

    @Transient
    public boolean meetsExceptionalCriteria() {
        return fullScaleIq != null && fullScaleIq >= 145;
    }

    @Transient
    public Student getStudent() {
        return giftedStudent != null ? giftedStudent.getStudent() : null;
    }

    @Transient
    public boolean hasAchievementScores() {
        return readingScore != null || mathScore != null ||
               writingScore != null || scienceScore != null;
    }

    @Transient
    public Double getAverageAchievementPercentile() {
        int count = 0;
        int sum = 0;

        if (readingPercentile != null) {
            sum += readingPercentile;
            count++;
        }
        if (mathPercentile != null) {
            sum += mathPercentile;
            count++;
        }
        if (writingPercentile != null) {
            sum += writingPercentile;
            count++;
        }
        if (sciencePercentile != null) {
            sum += sciencePercentile;
            count++;
        }

        return count > 0 ? (double) sum / count : null;
    }

    @Transient
    public int getDaysSinceAssessment() {
        if (assessmentDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - assessmentDate.toEpochDay());
    }

    @Transient
    public boolean isRecent() {
        return getDaysSinceAssessment() >= 0 && getDaysSinceAssessment() <= 365;
    }
}
