package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Teacher Evaluation Entity
 *
 * Tracks teacher evaluations, observations, rubric scores, improvement plans,
 * and goal setting. Supports multiple evaluation models (Danielson, Marzano, etc.).
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Entity
@Table(name = "teacher_evaluations", indexes = {
    @Index(name = "idx_eval_teacher", columnList = "teacher_id"),
    @Index(name = "idx_eval_evaluator", columnList = "evaluator_id"),
    @Index(name = "idx_eval_type", columnList = "evaluation_type"),
    @Index(name = "idx_eval_date", columnList = "evaluation_date"),
    @Index(name = "idx_eval_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Teacher being evaluated
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    /**
     * Administrator conducting evaluation
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private Teacher evaluator;

    /**
     * School year
     */
    @Column(name = "school_year", nullable = false)
    private String schoolYear;

    /**
     * Evaluation cycle (for multi-year evaluation systems)
     */
    @Column(name = "evaluation_cycle")
    private Integer evaluationCycle;

    /**
     * Type of evaluation
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type", nullable = false)
    private EvaluationType evaluationType;

    /**
     * Evaluation rubric/framework used
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "rubric_type")
    private RubricType rubricType;

    /**
     * Evaluation date
     */
    @Column(name = "evaluation_date")
    private LocalDate evaluationDate;

    /**
     * Scheduled observation date
     */
    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    /**
     * Scheduled observation time
     */
    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    /**
     * Current status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EvaluationStatus status = EvaluationStatus.SCHEDULED;

    /**
     * Course being observed (if classroom observation)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    /**
     * Observation location
     */
    @Column(name = "observation_location")
    private String observationLocation;

    /**
     * Pre-observation conference held?
     */
    @Column(name = "pre_conference_held")
    @Builder.Default
    private Boolean preConferenceHeld = false;

    /**
     * Pre-conference date
     */
    @Column(name = "pre_conference_date")
    private LocalDate preConferenceDate;

    /**
     * Pre-conference notes
     */
    @Column(name = "pre_conference_notes", columnDefinition = "TEXT")
    private String preConferenceNotes;

    /**
     * Observation start time
     */
    @Column(name = "observation_start_time")
    private LocalTime observationStartTime;

    /**
     * Observation end time
     */
    @Column(name = "observation_end_time")
    private LocalTime observationEndTime;

    /**
     * Observation notes
     */
    @Column(name = "observation_notes", columnDefinition = "TEXT")
    private String observationNotes;

    /**
     * Post-observation conference held?
     */
    @Column(name = "post_conference_held")
    @Builder.Default
    private Boolean postConferenceHeld = false;

    /**
     * Post-conference date
     */
    @Column(name = "post_conference_date")
    private LocalDate postConferenceDate;

    /**
     * Post-conference notes
     */
    @Column(name = "post_conference_notes", columnDefinition = "TEXT")
    private String postConferenceNotes;

    /**
     * Overall rating
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "overall_rating")
    private PerformanceRating overallRating;

    /**
     * Overall score (numeric)
     */
    @Column(name = "overall_score")
    private Double overallScore;

    /**
     * Domain 1 score (Planning and Preparation)
     */
    @Column(name = "domain1_score")
    private Double domain1Score;

    /**
     * Domain 1 rating
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "domain1_rating")
    private PerformanceRating domain1Rating;

    /**
     * Domain 1 comments
     */
    @Column(name = "domain1_comments", columnDefinition = "TEXT")
    private String domain1Comments;

    /**
     * Domain 2 score (Classroom Environment)
     */
    @Column(name = "domain2_score")
    private Double domain2Score;

    /**
     * Domain 2 rating
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "domain2_rating")
    private PerformanceRating domain2Rating;

    /**
     * Domain 2 comments
     */
    @Column(name = "domain2_comments", columnDefinition = "TEXT")
    private String domain2Comments;

    /**
     * Domain 3 score (Instruction)
     */
    @Column(name = "domain3_score")
    private Double domain3Score;

    /**
     * Domain 3 rating
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "domain3_rating")
    private PerformanceRating domain3Rating;

    /**
     * Domain 3 comments
     */
    @Column(name = "domain3_comments", columnDefinition = "TEXT")
    private String domain3Comments;

    /**
     * Domain 4 score (Professional Responsibilities)
     */
    @Column(name = "domain4_score")
    private Double domain4Score;

    /**
     * Domain 4 rating
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "domain4_rating")
    private PerformanceRating domain4Rating;

    /**
     * Domain 4 comments
     */
    @Column(name = "domain4_comments", columnDefinition = "TEXT")
    private String domain4Comments;

    /**
     * Strengths identified
     */
    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    /**
     * Areas for growth
     */
    @Column(name = "areas_for_growth", columnDefinition = "TEXT")
    private String areasForGrowth;

    /**
     * Recommendations
     */
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    /**
     * Commendations
     */
    @Column(name = "commendations", columnDefinition = "TEXT")
    private String commendations;

    /**
     * Teacher goals for next cycle
     */
    @ElementCollection
    @CollectionTable(name = "evaluation_goals", joinColumns = @JoinColumn(name = "evaluation_id"))
    @Column(name = "goal", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> goals = new ArrayList<>();

    /**
     * Improvement plan required?
     */
    @Column(name = "improvement_plan_required")
    @Builder.Default
    private Boolean improvementPlanRequired = false;

    /**
     * Improvement plan created?
     */
    @Column(name = "improvement_plan_created")
    @Builder.Default
    private Boolean improvementPlanCreated = false;

    /**
     * Improvement plan document path
     */
    @Column(name = "improvement_plan_document")
    private String improvementPlanDocument;

    /**
     * Supporting evidence/artifacts
     */
    @ElementCollection
    @CollectionTable(name = "evaluation_evidence", joinColumns = @JoinColumn(name = "evaluation_id"))
    @Column(name = "evidence_path")
    @Builder.Default
    private List<String> evidencePaths = new ArrayList<>();

    /**
     * Teacher signature date
     */
    @Column(name = "teacher_signature_date")
    private LocalDate teacherSignatureDate;

    /**
     * Teacher comments/response
     */
    @Column(name = "teacher_comments", columnDefinition = "TEXT")
    private String teacherComments;

    /**
     * Evaluator signature date
     */
    @Column(name = "evaluator_signature_date")
    private LocalDate evaluatorSignatureDate;

    /**
     * Final evaluation document path
     */
    @Column(name = "final_document_path")
    private String finalDocumentPath;

    /**
     * Submitted for review?
     */
    @Column(name = "submitted_for_review")
    @Builder.Default
    private Boolean submittedForReview = false;

    /**
     * Review submission date
     */
    @Column(name = "review_submission_date")
    private LocalDate reviewSubmissionDate;

    /**
     * Campus
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    private Campus campus;

    /**
     * Created timestamp
     */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Updated timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum EvaluationType {
        FORMAL_OBSERVATION,      // Formal scheduled observation
        INFORMAL_OBSERVATION,    // Informal walkthrough
        ANNOUNCED,               // Announced observation
        UNANNOUNCED,             // Unannounced observation
        SUMMATIVE,               // End-of-year summative evaluation
        FORMATIVE,               // Mid-year formative evaluation
        PEER_OBSERVATION,        // Peer-to-peer observation
        SELF_EVALUATION,         // Teacher self-assessment
        STUDENT_FEEDBACK,        // Student survey/feedback
        PORTFOLIO_REVIEW,        // Portfolio evaluation
        VIDEO_REVIEW,            // Video recording review
        OTHER                    // Other evaluation type
    }

    public enum RubricType {
        DANIELSON,               // Charlotte Danielson Framework
        MARZANO,                 // Marzano Teacher Evaluation Model
        STATE_MODEL,             // State-specific model
        DISTRICT_MODEL,          // District-developed model
        MCREL,                   // McREL Teacher Evaluation System
        TEACHSCAPE,              // Teachscape Reflect
        TAP,                     // System for Teacher and Student Advancement
        IMPACT,                  // IMPACT evaluation system
        CUSTOM,                  // Custom rubric
        OTHER                    // Other rubric type
    }

    public enum PerformanceRating {
        HIGHLY_EFFECTIVE,        // 4 - Highly Effective
        EFFECTIVE,               // 3 - Effective
        DEVELOPING,              // 2 - Developing/Needs Improvement
        INEFFECTIVE,             // 1 - Ineffective
        DISTINGUISHED,           // Alternative: Distinguished
        PROFICIENT,              // Alternative: Proficient
        BASIC,                   // Alternative: Basic
        UNSATISFACTORY          // Alternative: Unsatisfactory
    }

    public enum EvaluationStatus {
        SCHEDULED,               // Observation scheduled
        PRE_CONFERENCE_PENDING,  // Pre-conference needs to be held
        PRE_CONFERENCE_COMPLETE, // Pre-conference completed
        OBSERVATION_PENDING,     // Observation needs to be conducted
        OBSERVATION_COMPLETE,    // Observation conducted
        POST_CONFERENCE_PENDING, // Post-conference needs to be held
        POST_CONFERENCE_COMPLETE,// Post-conference completed
        DRAFT,                   // Evaluation in draft
        PENDING_TEACHER_REVIEW,  // Awaiting teacher review
        PENDING_SIGNATURE,       // Awaiting teacher signature
        COMPLETED,               // Evaluation completed
        APPEALED,                // Teacher appealed evaluation
        REVISED                  // Evaluation revised after appeal
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    @Transient
    public boolean isCompleted() {
        return status == EvaluationStatus.COMPLETED;
    }

    @Transient
    public boolean isPending() {
        return status == EvaluationStatus.SCHEDULED ||
               status == EvaluationStatus.OBSERVATION_PENDING;
    }

    @Transient
    public boolean needsSignature() {
        return status == EvaluationStatus.PENDING_SIGNATURE &&
               teacherSignatureDate == null;
    }

    @Transient
    public boolean isOverdue() {
        return scheduledDate != null &&
               scheduledDate.isBefore(LocalDate.now()) &&
               !isCompleted();
    }

    @Transient
    public int getObservationDurationMinutes() {
        if (observationStartTime == null || observationEndTime == null) {
            return 0;
        }
        return (int) java.time.Duration.between(observationStartTime, observationEndTime).toMinutes();
    }

    @Transient
    public boolean requiresImprovement() {
        return improvementPlanRequired ||
               overallRating == PerformanceRating.DEVELOPING ||
               overallRating == PerformanceRating.INEFFECTIVE ||
               overallRating == PerformanceRating.BASIC ||
               overallRating == PerformanceRating.UNSATISFACTORY;
    }

    @Transient
    public String getRatingDescription() {
        if (overallRating == null) return "Not Rated";
        return switch (overallRating) {
            case HIGHLY_EFFECTIVE, DISTINGUISHED -> "Excellent Performance";
            case EFFECTIVE, PROFICIENT -> "Satisfactory Performance";
            case DEVELOPING, BASIC -> "Needs Improvement";
            case INEFFECTIVE, UNSATISFACTORY -> "Unsatisfactory Performance";
        };
    }

    // ========================================================================
    // JPA LIFECYCLE CALLBACKS
    // ========================================================================

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
