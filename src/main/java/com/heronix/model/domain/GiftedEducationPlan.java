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
 * Gifted Education Plan (GEP) Entity
 * Tracks individualized plans for gifted students including goals, strategies, and progress
 * Similar to IEP but for gifted education services
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "gifted_education_plans", indexes = {
    @Index(name = "idx_gep_student", columnList = "gifted_student_id"),
    @Index(name = "idx_gep_status", columnList = "status"),
    @Index(name = "idx_gep_review_date", columnList = "next_review_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftedEducationPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gifted_student_id", nullable = false)
    private GiftedStudent giftedStudent;

    // Plan Details
    @Column(name = "plan_number", unique = true, length = 50)
    private String planNumber;

    @Column(name = "school_year", length = 20)
    private String schoolYear;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private PlanStatus status = PlanStatus.DRAFT;

    // Areas of Giftedness
    @ElementCollection
    @CollectionTable(name = "gep_gifted_areas", joinColumns = @JoinColumn(name = "gep_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "area")
    @Builder.Default
    private List<GiftedStudent.GiftedArea> areasAddressed = new ArrayList<>();

    // Goals and Objectives
    @Column(name = "academic_goals", columnDefinition = "TEXT")
    private String academicGoals;

    @Column(name = "creative_productive_goals", columnDefinition = "TEXT")
    private String creativeProductiveGoals;

    @Column(name = "social_emotional_goals", columnDefinition = "TEXT")
    private String socialEmotionalGoals;

    @ElementCollection
    @CollectionTable(name = "gep_goals", joinColumns = @JoinColumn(name = "gep_id"))
    @Column(name = "goal", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> goals = new ArrayList<>();

    // Strategies and Differentiation
    @Column(name = "differentiation_strategies", columnDefinition = "TEXT")
    private String differentiationStrategies;

    @Column(name = "acceleration_strategies", columnDefinition = "TEXT")
    private String accelerationStrategies;

    @Column(name = "enrichment_strategies", columnDefinition = "TEXT")
    private String enrichmentStrategies;

    @ElementCollection
    @CollectionTable(name = "gep_instructional_strategies", joinColumns = @JoinColumn(name = "gep_id"))
    @Column(name = "strategy", length = 200)
    @Builder.Default
    private List<String> instructionalStrategies = new ArrayList<>();

    // Services
    @Column(name = "pull_out_services_minutes")
    private Integer pullOutServicesMinutes;

    @Column(name = "enrichment_activities", columnDefinition = "TEXT")
    private String enrichmentActivities;

    @Column(name = "independent_study_projects", columnDefinition = "TEXT")
    private String independentStudyProjects;

    @Column(name = "mentorship_plan", columnDefinition = "TEXT")
    private String mentorshipPlan;

    @Column(name = "competition_participation_plan", columnDefinition = "TEXT")
    private String competitionParticipationPlan;

    // Advanced Coursework
    @ElementCollection
    @CollectionTable(name = "gep_advanced_courses", joinColumns = @JoinColumn(name = "gep_id"))
    @Column(name = "course_name", length = 200)
    @Builder.Default
    private List<String> plannedAdvancedCourses = new ArrayList<>();

    @Column(name = "grade_acceleration_plan", columnDefinition = "TEXT")
    private String gradeAccelerationPlan;

    @Column(name = "subject_acceleration_plan", columnDefinition = "TEXT")
    private String subjectAccelerationPlan;

    // Progress Monitoring
    @Column(name = "progress_monitoring_method", columnDefinition = "TEXT")
    private String progressMonitoringMethod;

    @Column(name = "progress_monitoring_frequency", length = 100)
    private String progressMonitoringFrequency;

    @Column(name = "last_progress_review_date")
    private LocalDate lastProgressReviewDate;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    @Column(name = "progress_summary", columnDefinition = "TEXT")
    private String progressSummary;

    // Team Members
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_manager_id")
    private Teacher caseManager;

    @Column(name = "case_manager_name", length = 100)
    private String caseManagerName;

    @ElementCollection
    @CollectionTable(name = "gep_team_members", joinColumns = @JoinColumn(name = "gep_id"))
    @Column(name = "member_name", length = 100)
    @Builder.Default
    private List<String> teamMembers = new ArrayList<>();

    // Parent Involvement
    @Column(name = "parent_input", columnDefinition = "TEXT")
    private String parentInput;

    @Column(name = "parent_consent_received")
    private Boolean parentConsentReceived;

    @Column(name = "parent_consent_date")
    private LocalDate parentConsentDate;

    @Column(name = "parent_conference_held")
    @Builder.Default
    private Boolean parentConferenceHeld = false;

    @Column(name = "parent_conference_date")
    private LocalDate parentConferenceDate;

    // Student Input
    @Column(name = "student_interests", columnDefinition = "TEXT")
    private String studentInterests;

    @Column(name = "student_goals", columnDefinition = "TEXT")
    private String studentGoals;

    @Column(name = "student_preferred_learning_style", columnDefinition = "TEXT")
    private String studentPreferredLearningStyle;

    // Accommodations
    @ElementCollection
    @CollectionTable(name = "gep_accommodations", joinColumns = @JoinColumn(name = "gep_id"))
    @Column(name = "accommodation", length = 200)
    @Builder.Default
    private List<String> accommodations = new ArrayList<>();

    @Column(name = "social_emotional_supports", columnDefinition = "TEXT")
    private String socialEmotionalSupports;

    // Performance Expectations
    @Column(name = "performance_expectations", columnDefinition = "TEXT")
    private String performanceExpectations;

    @Column(name = "grading_expectations", columnDefinition = "TEXT")
    private String gradingExpectations;

    // Review History
    @Column(name = "annual_review_completed")
    @Builder.Default
    private Boolean annualReviewCompleted = false;

    @Column(name = "annual_review_date")
    private LocalDate annualReviewDate;

    @Column(name = "revision_history", columnDefinition = "TEXT")
    private String revisionHistory;

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

    public enum PlanStatus {
        DRAFT("Draft"),
        PENDING_APPROVAL("Pending Approval"),
        ACTIVE("Active"),
        UNDER_REVIEW("Under Review"),
        REVISED("Revised"),
        COMPLETED("Completed"),
        INACTIVE("Inactive");

        private final String displayName;

        PlanStatus(String displayName) {
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
        return status == PlanStatus.ACTIVE &&
               LocalDate.now().isBefore(endDate);
    }

    @Transient
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    @Transient
    public boolean isDueForReview() {
        if (nextReviewDate == null) return false;
        return LocalDate.now().isAfter(nextReviewDate) ||
               LocalDate.now().isEqual(nextReviewDate);
    }

    @Transient
    public boolean needsParentConsent() {
        return parentConsentReceived == null || !parentConsentReceived;
    }

    @Transient
    public Student getStudent() {
        return giftedStudent != null ? giftedStudent.getStudent() : null;
    }

    @Transient
    public int getGoalCount() {
        return goals != null ? goals.size() : 0;
    }

    @Transient
    public int getStrategyCount() {
        return instructionalStrategies != null ? instructionalStrategies.size() : 0;
    }

    @Transient
    public int getDaysSinceProgressReview() {
        if (lastProgressReviewDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - lastProgressReviewDate.toEpochDay());
    }

    @Transient
    public boolean isOverdueForReview() {
        return getDaysSinceProgressReview() > 90;
    }
}
