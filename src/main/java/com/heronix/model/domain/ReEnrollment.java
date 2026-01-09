package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Re-Enrollment Record
 *
 * Tracks students who are re-enrolling after previously withdrawing.
 * Handles returning students with historical enrollment data.
 *
 * Workflow:
 * 1. DRAFT - Re-enrollment application being prepared
 * 2. PENDING_REVIEW - Awaiting review of previous enrollment history
 * 3. PENDING_APPROVAL - Awaiting approval from administration
 * 4. APPROVED - Re-enrollment approved
 * 5. RECORDS_UPDATED - Student records updated
 * 6. ENROLLED - Successfully re-enrolled
 * 7. REJECTED - Re-enrollment rejected
 * 8. CANCELLED - Application cancelled
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Entity
@Table(name = "re_enrollments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // RE-ENROLLMENT METADATA
    // ========================================================================

    @Column(nullable = false, unique = true, length = 50)
    private String reEnrollmentNumber; // e.g., "RNE-2025-001234"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReEnrollmentStatus status;

    @Column(nullable = false)
    private LocalDate applicationDate;

    @Column
    private LocalDate intendedEnrollmentDate;

    // ========================================================================
    // STUDENT INFORMATION
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(length = 20)
    private String requestedGradeLevel;

    @Column(length = 20)
    private String assignedGradeLevel;

    // ========================================================================
    // PREVIOUS ENROLLMENT HISTORY
    // ========================================================================

    @Column
    private LocalDate previousEnrollmentDate;

    @Column
    private LocalDate previousWithdrawalDate;

    @Column(length = 20)
    private String previousGradeLevel;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private WithdrawalReason previousWithdrawalReason;

    @Column(length = 2000)
    private String previousWithdrawalDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_withdrawal_id")
    private WithdrawalRecord previousWithdrawal;

    @Column
    private Integer totalPreviousEnrollments;

    @Column
    private LocalDate firstEnrollmentDate;

    @Column
    private LocalDate lastEnrollmentDate;

    // ========================================================================
    // TIME AWAY FROM SCHOOL
    // ========================================================================

    @Column
    private Integer monthsAway;

    @Column
    private Integer yearsAway;

    @Column(length = 200)
    private String schoolAttendedDuringAbsence;

    @Column(length = 100)
    private String cityStateAttended;

    @Column
    private Boolean wasHomeschooled;

    @Column(length = 2000)
    private String activitiesDuringAbsence;

    // ========================================================================
    // REASON FOR RETURN
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ReEnrollmentReason reason;

    @Column(length = 2000)
    private String reasonDetails;

    @Column
    private Boolean familyRelocation;

    @Column
    private Boolean improvedCircumstances;

    @Column
    private Boolean requestedSpecificProgram;

    @Column(length = 200)
    private String requestedProgramName;

    // ========================================================================
    // UPDATED STUDENT INFORMATION
    // ========================================================================

    @Column(length = 200)
    private String currentAddress;

    @Column(length = 100)
    private String currentCity;

    @Column(length = 2)
    private String currentState;

    @Column(length = 10)
    private String currentZipCode;

    @Column
    private Boolean addressChanged;

    @Column(length = 20)
    private String currentPhoneNumber;

    @Column
    private Boolean phoneChanged;

    @Column(length = 100)
    private String currentEmail;

    @Column
    private Boolean emailChanged;

    // ========================================================================
    // PARENT/GUARDIAN INFORMATION
    // ========================================================================

    @Column(length = 100)
    private String parent1FirstName;

    @Column(length = 100)
    private String parent1LastName;

    @Column(length = 20)
    private String parent1Phone;

    @Column(length = 100)
    private String parent1Email;

    @Column
    private Boolean parent1Changed;

    @Column(length = 100)
    private String parent2FirstName;

    @Column(length = 100)
    private String parent2LastName;

    @Column(length = 20)
    private String parent2Phone;

    @Column(length = 100)
    private String parent2Email;

    @Column
    private Boolean parent2Changed;

    // ========================================================================
    // RECORDS REVIEW
    // ========================================================================

    @Column
    private Boolean previousRecordsReviewed;

    @Column
    private LocalDate recordsReviewDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_staff_id")
    private User reviewedBy;

    @Column
    private Boolean transcriptCurrent;

    @Column
    private Boolean immunizationsCurrent;

    @Column
    private Boolean healthRecordsCurrent;

    @Column
    private Boolean disciplineRecordsReviewed;

    @Column
    private Boolean attendanceRecordsReviewed;

    @Column
    private Boolean hasOutstandingFees;

    @Column
    private Double outstandingFeesAmount;

    @Column
    private Boolean feesPaid;

    // ========================================================================
    // ACADEMIC REQUIREMENTS
    // ========================================================================

    @Column
    private Boolean placementTestRequired;

    @Column
    private LocalDate placementTestDate;

    @Column(length = 50)
    private String placementTestResult;

    @Column
    private Boolean academicInterviewRequired;

    @Column
    private LocalDate academicInterviewDate;

    @Column(length = 2000)
    private String academicInterviewNotes;

    @Column
    private Boolean specialEducationReview;

    @Column
    private Boolean giftedTalentedReview;

    // ========================================================================
    // APPROVAL PROCESS
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id")
    private User assignedCounselor;

    @Column
    private LocalDate counselorReviewDate;

    @Column(length = 2000)
    private String counselorRecommendation;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ApprovalDecision counselorDecision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "principal_id")
    private User principalReviewer;

    @Column
    private LocalDate principalReviewDate;

    @Column(length = 2000)
    private String principalNotes;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ApprovalDecision principalDecision;

    @Column(length = 2000)
    private String rejectionReason;

    // ========================================================================
    // ENROLLMENT PROCESSING
    // ========================================================================

    @Column
    private LocalDate approvalDate;

    @Column
    private LocalDate enrollmentDate;

    @Column(length = 100)
    private String assignedHomeroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_application_id")
    private EnrollmentApplication enrollmentApplication;

    @Column
    private Boolean studentIdReactivated;

    @Column(length = 50)
    private String reactivatedStudentId;

    // ========================================================================
    // CONDITIONS & REQUIREMENTS
    // ========================================================================

    @Column
    private Boolean conditionalApproval;

    @Column(length = 2000)
    private String approvalConditions;

    @Column
    private Boolean behavioralContract;

    @Column(length = 2000)
    private String behavioralContractTerms;

    @Column
    private Boolean academicPlan;

    @Column(length = 2000)
    private String academicPlanDetails;

    @Column
    private Boolean probationaryPeriod;

    @Column
    private Integer probationaryPeriodDays;

    // ========================================================================
    // ADMINISTRATIVE
    // ========================================================================

    @Column(length = 2000)
    private String administrativeNotes;

    @Column(length = 2000)
    private String specialCircumstances;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_staff_id")
    private User processedBy;

    @Column
    private LocalDate processingDate;

    // ========================================================================
    // AUDIT FIELDS
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_staff_id", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_staff_id")
    private User updatedBy;

    @Column
    private LocalDateTime updatedAt;

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum ReEnrollmentStatus {
        DRAFT("Draft"),
        PENDING_REVIEW("Pending Review"),
        PENDING_APPROVAL("Pending Approval"),
        APPROVED("Approved"),
        RECORDS_UPDATED("Records Updated"),
        ENROLLED("Enrolled"),
        REJECTED("Rejected"),
        CANCELLED("Cancelled");

        private final String displayName;

        ReEnrollmentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ReEnrollmentReason {
        FAMILY_RETURNED("Family Returned to Area"),
        DISSATISFACTION_RESOLVED("Previous Concerns Resolved"),
        PREFERRED_SCHOOL("Preferred School Choice"),
        FINANCIAL_IMPROVEMENT("Financial Situation Improved"),
        ACADEMIC_PROGRAM("Specific Academic Program"),
        SOCIAL_REASONS("Social/Friend Reasons"),
        MEDICAL_REASONS("Medical Reasons Resolved"),
        TRANSPORTATION("Transportation Now Available"),
        OTHER("Other");

        private final String displayName;

        ReEnrollmentReason(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum WithdrawalReason {
        FAMILY_RELOCATION("Family Relocation"),
        DISSATISFACTION("Dissatisfaction with School"),
        ACADEMIC_REASONS("Academic Reasons"),
        BEHAVIORAL_ISSUES("Behavioral Issues"),
        BULLYING("Bullying or Safety Concerns"),
        FINANCIAL("Financial Reasons"),
        MEDICAL("Medical Reasons"),
        OTHER("Other");

        private final String displayName;

        WithdrawalReason(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ApprovalDecision {
        APPROVED("Approved"),
        CONDITIONAL("Conditionally Approved"),
        DENIED("Denied"),
        PENDING("Pending");

        private final String displayName;

        ApprovalDecision(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Calculate time away from school
     */
    public void calculateTimeAway() {
        if (previousWithdrawalDate != null && applicationDate != null) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                    previousWithdrawalDate, applicationDate);
            monthsAway = (int) (daysBetween / 30);
            yearsAway = (int) (daysBetween / 365);
        }
    }

    /**
     * Check if ready for approval
     */
    public boolean isReadyForApproval() {
        return Boolean.TRUE.equals(previousRecordsReviewed) &&
               Boolean.TRUE.equals(transcriptCurrent) &&
               Boolean.TRUE.equals(immunizationsCurrent) &&
               (!Boolean.TRUE.equals(hasOutstandingFees) || Boolean.TRUE.equals(feesPaid)) &&
               counselorDecision == ApprovalDecision.APPROVED;
    }

    /**
     * Check if approved for enrollment
     */
    public boolean isApprovedForEnrollment() {
        return (principalDecision == ApprovalDecision.APPROVED ||
                principalDecision == ApprovalDecision.CONDITIONAL) &&
               assignedGradeLevel != null;
    }
}
