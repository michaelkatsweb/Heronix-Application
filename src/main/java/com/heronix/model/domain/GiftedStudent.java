package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gifted Student Entity
 * Tracks gifted identification, program placement, and service delivery
 * Supports talent development and enrichment program management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "gifted_students", indexes = {
    @Index(name = "idx_gifted_student", columnList = "student_id"),
    @Index(name = "idx_gifted_status", columnList = "gifted_status"),
    @Index(name = "idx_gifted_program", columnList = "program_type"),
    @Index(name = "idx_gifted_area", columnList = "primary_gifted_area")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftedStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    // Identification
    @Column(name = "referral_date")
    private LocalDate referralDate;

    @Column(name = "referral_source", length = 200)
    private String referralSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "referral_type", length = 30)
    private ReferralType referralType;

    @Column(name = "screening_date")
    private LocalDate screeningDate;

    @Column(name = "screening_completed")
    @Builder.Default
    private Boolean screeningCompleted = false;

    @Column(name = "identification_date")
    private LocalDate identificationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gifted_status", nullable = false, length = 30)
    @Builder.Default
    private GiftedStatus giftedStatus = GiftedStatus.REFERRED;

    // Gifted Areas
    @Enumerated(EnumType.STRING)
    @Column(name = "primary_gifted_area", length = 50)
    private GiftedArea primaryGiftedArea;

    @ElementCollection
    @CollectionTable(name = "gifted_areas", joinColumns = @JoinColumn(name = "gifted_student_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "area")
    @Builder.Default
    private List<GiftedArea> giftedAreas = new ArrayList<>();

    // Eligibility
    @Column(name = "eligibility_determination_date")
    private LocalDate eligibilityDeterminationDate;

    @Column(name = "eligible_for_services")
    private Boolean eligibleForServices;

    @Column(name = "eligibility_criteria_met", columnDefinition = "TEXT")
    private String eligibilityCriteriaMet;

    @Column(name = "composite_score")
    private Double compositeScore;

    @Column(name = "iq_score")
    private Integer iqScore;

    @Column(name = "achievement_percentile")
    private Integer achievementPercentile;

    // Program Placement
    @Enumerated(EnumType.STRING)
    @Column(name = "program_type", length = 50)
    private ProgramType programType;

    @Column(name = "program_entry_date")
    private LocalDate programEntryDate;

    @Column(name = "program_exit_date")
    private LocalDate programExitDate;

    @ElementCollection
    @CollectionTable(name = "gifted_program_placements", joinColumns = @JoinColumn(name = "gifted_student_id"))
    @Column(name = "placement", length = 100)
    @Builder.Default
    private List<String> programPlacements = new ArrayList<>();

    // Service Delivery
    @Enumerated(EnumType.STRING)
    @Column(name = "service_delivery_model", length = 50)
    private ServiceDeliveryModel serviceDeliveryModel;

    @Column(name = "service_minutes_per_week")
    private Integer serviceMinutesPerWeek;

    @Column(name = "service_frequency", length = 100)
    private String serviceFrequency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_provider_id")
    private Teacher serviceProvider;

    @Column(name = "service_provider_name", length = 100)
    private String serviceProviderName;

    // Advanced Coursework
    @ElementCollection
    @CollectionTable(name = "gifted_advanced_courses", joinColumns = @JoinColumn(name = "gifted_student_id"))
    @Column(name = "course_name", length = 200)
    @Builder.Default
    private List<String> advancedCourses = new ArrayList<>();

    @Column(name = "ap_courses_enrolled")
    private Integer apCoursesEnrolled;

    @Column(name = "honors_courses_enrolled")
    private Integer honorsCoursesEnrolled;

    @Column(name = "dual_enrollment")
    @Builder.Default
    private Boolean dualEnrollment = false;

    @Column(name = "grade_acceleration")
    @Builder.Default
    private Boolean gradeAcceleration = false;

    @Column(name = "subject_acceleration", length = 200)
    private String subjectAcceleration;

    // Enrichment Programs
    @ElementCollection
    @CollectionTable(name = "gifted_enrichment_programs", joinColumns = @JoinColumn(name = "gifted_student_id"))
    @Column(name = "program_name", length = 200)
    @Builder.Default
    private List<String> enrichmentPrograms = new ArrayList<>();

    @Column(name = "competition_participation", columnDefinition = "TEXT")
    private String competitionParticipation;

    @Column(name = "special_projects", columnDefinition = "TEXT")
    private String specialProjects;

    // Cluster Grouping
    @Column(name = "cluster_grouped")
    @Builder.Default
    private Boolean clusterGrouped = false;

    @Column(name = "cluster_group_name", length = 100)
    private String clusterGroupName;

    @Column(name = "cluster_teacher", length = 100)
    private String clusterTeacher;

    // Progress Monitoring
    @Column(name = "last_progress_review_date")
    private LocalDate lastProgressReviewDate;

    @Column(name = "next_progress_review_date")
    private LocalDate nextProgressReviewDate;

    @Column(name = "annual_review_required")
    @Builder.Default
    private Boolean annualReviewRequired = true;

    @Column(name = "next_annual_review_date")
    private LocalDate nextAnnualReviewDate;

    // Talent Development
    @Column(name = "talent_area", length = 200)
    private String talentArea;

    @Column(name = "talent_development_plan_active")
    @Builder.Default
    private Boolean talentDevelopmentPlanActive = false;

    @Column(name = "talent_development_goals", columnDefinition = "TEXT")
    private String talentDevelopmentGoals;

    @Column(name = "mentorship_program")
    @Builder.Default
    private Boolean mentorshipProgram = false;

    @Column(name = "mentor_name", length = 100)
    private String mentorName;

    // Parent Communication
    @Column(name = "parent_notification_sent")
    @Builder.Default
    private Boolean parentNotificationSent = false;

    @Column(name = "parent_notification_date")
    private LocalDate parentNotificationDate;

    @Column(name = "parent_consent_received")
    private Boolean parentConsentReceived;

    @Column(name = "parent_consent_date")
    private LocalDate parentConsentDate;

    // Performance Tracking
    @Column(name = "current_gpa")
    private Double currentGpa;

    @Column(name = "performance_level", length = 30)
    private String performanceLevel;

    @Column(name = "meeting_expectations")
    @Builder.Default
    private Boolean meetingExpectations = true;

    @Column(name = "concerns", columnDefinition = "TEXT")
    private String concerns;

    // Additional Information
    @Column(name = "learning_characteristics", columnDefinition = "TEXT")
    private String learningCharacteristics;

    @Column(name = "social_emotional_needs", columnDefinition = "TEXT")
    private String socialEmotionalNeeds;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "interests", columnDefinition = "TEXT")
    private String interests;

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

    public enum GiftedStatus {
        REFERRED("Referred for Screening"),
        SCREENING_IN_PROGRESS("Screening In Progress"),
        SCREENING_COMPLETE("Screening Complete"),
        ASSESSMENT_IN_PROGRESS("Assessment In Progress"),
        ELIGIBLE("Eligible - Services Pending"),
        ACTIVE("Active - Receiving Services"),
        NOT_ELIGIBLE("Not Eligible"),
        EXITED("Exited Program"),
        INACTIVE("Inactive");

        private final String displayName;

        GiftedStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum GiftedArea {
        INTELLECTUAL("Intellectual/General"),
        SPECIFIC_ACADEMIC("Specific Academic Aptitude"),
        MATHEMATICS("Mathematics"),
        SCIENCE("Science"),
        READING_LANGUAGE_ARTS("Reading/Language Arts"),
        SOCIAL_STUDIES("Social Studies"),
        CREATIVE_THINKING("Creative Thinking"),
        LEADERSHIP("Leadership"),
        VISUAL_PERFORMING_ARTS("Visual/Performing Arts"),
        MUSIC("Music"),
        ART("Art"),
        DRAMA("Drama/Theatre"),
        PSYCHOMOTOR("Psychomotor/Athletic");

        private final String displayName;

        GiftedArea(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ProgramType {
        PULL_OUT("Pull-Out/Resource"),
        CLUSTER_GROUPING("Cluster Grouping"),
        SELF_CONTAINED("Self-Contained Gifted Class"),
        MAGNET_PROGRAM("Magnet Program"),
        HONORS_CLASSES("Honors Classes"),
        AP_IB("Advanced Placement/IB"),
        ENRICHMENT_ONLY("Enrichment Only"),
        DIFFERENTIATED_INSTRUCTION("Differentiated Instruction"),
        ACCELERATION("Acceleration Program"),
        DUAL_ENROLLMENT("Dual Enrollment"),
        HYBRID("Hybrid Model"),
        OTHER("Other");

        private final String displayName;

        ProgramType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ServiceDeliveryModel {
        PULL_OUT("Pull-Out"),
        PUSH_IN("Push-In"),
        CLUSTER_CLASS("Cluster Classroom"),
        SELF_CONTAINED("Self-Contained"),
        ENRICHMENT_ACTIVITIES("Enrichment Activities"),
        INDEPENDENT_STUDY("Independent Study"),
        MENTORSHIP("Mentorship"),
        ONLINE("Online/Virtual"),
        HYBRID("Hybrid");

        private final String displayName;

        ServiceDeliveryModel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ReferralType {
        TEACHER("Teacher Referral"),
        PARENT("Parent Referral"),
        STUDENT_SELF("Student Self-Referral"),
        ADMINISTRATOR("Administrator Referral"),
        TESTING("Testing/Assessment"),
        UNIVERSAL_SCREENING("Universal Screening"),
        OTHER("Other");

        private final String displayName;

        ReferralType(String displayName) {
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
    public boolean isActive() {
        return giftedStatus == GiftedStatus.ACTIVE;
    }

    @Transient
    public boolean isEligible() {
        return eligibleForServices != null && eligibleForServices;
    }

    @Transient
    public boolean needsAnnualReview() {
        if (!annualReviewRequired) return false;
        if (nextAnnualReviewDate == null) return true;
        return LocalDate.now().isAfter(nextAnnualReviewDate) ||
               LocalDate.now().isEqual(nextAnnualReviewDate);
    }

    @Transient
    public boolean needsProgressReview() {
        if (nextProgressReviewDate == null) return true;
        return LocalDate.now().isAfter(nextProgressReviewDate) ||
               LocalDate.now().isEqual(nextProgressReviewDate);
    }

    @Transient
    public boolean needsParentConsent() {
        return parentConsentReceived == null || !parentConsentReceived;
    }

    @Transient
    public int getDaysSinceProgressReview() {
        if (lastProgressReviewDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - lastProgressReviewDate.toEpochDay());
    }

    @Transient
    public int getDaysUntilAnnualReview() {
        if (nextAnnualReviewDate == null) return -1;
        return (int) (nextAnnualReviewDate.toEpochDay() - LocalDate.now().toEpochDay());
    }

    @Transient
    public boolean isUnderperforming() {
        return !meetingExpectations || (currentGpa != null && currentGpa < 3.0);
    }

    @Transient
    public int getGiftedAreaCount() {
        return giftedAreas != null ? giftedAreas.size() : 0;
    }

    @Transient
    public boolean isMultiTalented() {
        return getGiftedAreaCount() > 1;
    }

    @Transient
    public int getAdvancedCourseCount() {
        int count = 0;
        if (apCoursesEnrolled != null) count += apCoursesEnrolled;
        if (honorsCoursesEnrolled != null) count += honorsCoursesEnrolled;
        return count;
    }
}
