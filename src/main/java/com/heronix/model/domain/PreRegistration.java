package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Pre-Registration Entity for Upcoming School Year
 *
 * Represents early enrollment registration for returning students
 * advancing to the next grade level.
 *
 * Workflow:
 * 1. DRAFT - Staff/Parent creating pre-registration
 * 2. SUBMITTED - Pre-registration submitted for review
 * 3. UNDER_REVIEW - Being reviewed by admin
 * 4. APPROVED - Pre-registration approved, seat reserved
 * 5. CONFIRMED - Final enrollment confirmed
 * 6. CANCELLED - Pre-registration cancelled
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Entity
@Table(name = "pre_registrations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // REGISTRATION METADATA
    // ========================================================================

    @Column(nullable = false, unique = true, length = 50)
    private String registrationNumber; // e.g., "PRE-2025-001234"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RegistrationStatus status;

    @Column(nullable = false, length = 20)
    private String targetSchoolYear; // "2025-2026"

    @Column(nullable = false)
    private LocalDate registrationDate;

    // ========================================================================
    // STUDENT INFORMATION
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student; // Existing student (returning)

    @Column(nullable = false, length = 20)
    private String currentGrade;

    @Column(nullable = false, length = 20)
    private String nextGrade; // Grade advancing to

    @Column
    private Boolean isReturningStudent;

    @Column
    private Boolean hasCompletedPriorYear;

    @Column
    private Boolean isInGoodStanding;

    @Column(length = 100)
    private String studentEmail;

    // ========================================================================
    // PARENT/GUARDIAN VERIFICATION
    // ========================================================================

    @Column(length = 200)
    private String parentName;

    @Column(length = 20)
    private String parentPhone;

    @Column(length = 100)
    private String parentEmail;

    @Column(length = 500)
    private String currentAddress;

    @Column
    private Boolean addressChanged;

    @Column(length = 500)
    private String newAddress;

    @Column
    private Boolean emergencyContactsVerified;

    // ========================================================================
    // COURSE PREFERENCES
    // ========================================================================

    @Column(length = 2000)
    private String requiredCourses; // JSON or comma-separated

    @Column(length = 2000)
    private String electiveCourses; // JSON or comma-separated

    @Column
    private Boolean interestedInAPHonors;

    @Column
    private Boolean interestedInDualEnrollment;

    @Column
    private Boolean interestedInCTE;

    @Column
    private Boolean interestedInAthletics;

    @Column
    private Boolean interestedInFineArts;

    @Column
    private Boolean interestedInSTEM;

    // ========================================================================
    // SPECIAL SERVICES CONTINUATION
    // ========================================================================

    @Column
    private Boolean continueIEP;

    @Column
    private LocalDate iepReviewDate;

    @Column
    private Boolean continue504Plan;

    @Column
    private LocalDate plan504ReviewDate;

    @Column
    private Boolean continueESL;

    @Column(length = 50)
    private String languageProficiencyLevel;

    @Column
    private Boolean continueGiftedProgram;

    @Column
    private Boolean needsSpecialTransportation;

    @Column(length = 100)
    private String transportationType;

    @Column(length = 1000)
    private String medicalAccommodations;

    // ========================================================================
    // LUNCH PROGRAM & FEES
    // ========================================================================

    @Column(length = 50)
    private String lunchProgramStatus;

    @Column
    private Boolean needsLunchApplication;

    @Column
    private Boolean technologyFeeWaiverRequested;

    @Column
    private Boolean activityFeeWaiverRequested;

    @Column
    private Double estimatedTotalFees;

    // ========================================================================
    // SCHEDULE PREFERENCES
    // ========================================================================

    @Column(length = 50)
    private String preferredStartTime;

    @Column(length = 50)
    private String studyHallPreference;

    @Column(length = 50)
    private String lunchPeriodPreference;

    @Column
    private Boolean interestedInEarlyBird;

    @Column
    private Boolean interestedInAfterSchool;

    @Column(length = 1000)
    private String schedulingNotes;

    // ========================================================================
    // PARENT ACKNOWLEDGMENT
    // ========================================================================

    @Column
    private Boolean acknowledgedAccuracy;

    @Column
    private Boolean acknowledgedReview;

    @Column
    private Boolean acknowledgedDeadline;

    @Column
    private Boolean acknowledgedFees;

    @Column
    private Boolean acknowledgedUpdates;

    @Column(length = 200)
    private String parentSignature;

    @Column
    private LocalDate signatureDate;

    // ========================================================================
    // ADDITIONAL NOTES
    // ========================================================================

    @Column(length = 2000)
    private String additionalNotes;

    // ========================================================================
    // WORKFLOW & APPROVAL
    // ========================================================================

    @Column
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime reviewedAt;

    @Column
    private LocalDateTime approvedAt;

    @Column
    private LocalDateTime confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_staff_id")
    private User reviewedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_staff_id")
    private User approvedBy;

    @Column(length = 1000)
    private String reviewNotes;

    @Column(length = 1000)
    private String approvalNotes;

    @Column
    private Boolean seatReserved;

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

    public enum RegistrationStatus {
        DRAFT("Draft - In Progress"),
        SUBMITTED("Submitted for Review"),
        UNDER_REVIEW("Under Review"),
        APPROVED("Approved - Seat Reserved"),
        CONFIRMED("Final Enrollment Confirmed"),
        CANCELLED("Cancelled"),
        WAITLISTED("Waitlisted - No Seats Available");

        private final String displayName;

        RegistrationStatus(String displayName) {
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
     * Check if all required acknowledgments are complete
     */
    public boolean areAcknowledgmentsComplete() {
        return Boolean.TRUE.equals(acknowledgedAccuracy) &&
               Boolean.TRUE.equals(acknowledgedReview) &&
               Boolean.TRUE.equals(acknowledgedDeadline) &&
               Boolean.TRUE.equals(acknowledgedFees) &&
               Boolean.TRUE.equals(acknowledgedUpdates);
    }

    /**
     * Check if pre-registration can be submitted
     */
    public boolean canBeSubmitted() {
        return status == RegistrationStatus.DRAFT &&
               student != null &&
               nextGrade != null &&
               parentPhone != null &&
               parentEmail != null &&
               areAcknowledgmentsComplete() &&
               parentSignature != null;
    }

    /**
     * Check if pre-registration can be approved
     */
    public boolean canBeApproved() {
        return status == RegistrationStatus.UNDER_REVIEW &&
               isInGoodStanding != null &&
               isInGoodStanding;
    }
}
