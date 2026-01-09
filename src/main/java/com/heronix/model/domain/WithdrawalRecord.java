package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Withdrawal Record Entity
 *
 * Represents student withdrawal processing with comprehensive clearance checklist,
 * exit interview, and transfer documentation.
 *
 * Workflow:
 * 1. DRAFT - Creating withdrawal record
 * 2. PENDING_CLEARANCE - Awaiting clearance completion
 * 3. CLEARANCE_IN_PROGRESS - Clearing items
 * 4. CLEARED - All items cleared
 * 5. COMPLETED - Withdrawal processed
 * 6. CANCELLED - Withdrawal cancelled
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Entity
@Table(name = "withdrawal_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // WITHDRAWAL METADATA
    // ========================================================================

    @Column(nullable = false, unique = true, length = 50)
    private String withdrawalNumber; // e.g., "WD-2025-001234"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WithdrawalStatus status;

    // ========================================================================
    // STUDENT INFORMATION
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(length = 20)
    private String currentGrade;

    @Column(length = 30)
    private String currentStatus; // Before withdrawal

    @Column
    private LocalDate enrollmentDate;

    @Column
    private LocalDate lastAttendanceDate;

    // ========================================================================
    // WITHDRAWAL DETAILS
    // ========================================================================

    @Column(nullable = false)
    private LocalDate withdrawalDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private WithdrawalType withdrawalType;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private WithdrawalReason withdrawalReason;

    @Column(length = 2000)
    private String withdrawalDetails;

    @Column
    private Boolean isTransferring;

    // ========================================================================
    // RECEIVING SCHOOL (for transfers)
    // ========================================================================

    @Column(length = 200)
    private String receivingSchoolName;

    @Column(length = 200)
    private String receivingSchoolDistrict;

    @Column(length = 200)
    private String receivingSchoolLocation;

    @Column(length = 20)
    private String receivingSchoolPhone;

    @Column(length = 100)
    private String receivingSchoolEmail;

    @Column
    private LocalDate expectedStartDate;

    @Column(length = 200)
    private String recordsRequestedBy;

    // ========================================================================
    // EXIT INTERVIEW
    // ========================================================================

    @Column
    private LocalDate interviewDate;

    @Column(length = 200)
    private String interviewedBy;

    @Column
    private Boolean parentPresent;

    @Column
    private Boolean studentPresent;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private SatisfactionRating satisfactionRating;

    @Column(length = 2000)
    private String primaryFactors;

    @Column(length = 2000)
    private String academicFeedback;

    @Column(length = 2000)
    private String socialFeedback;

    @Column(length = 2000)
    private String improvementSuggestions;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RecommendSchool recommendSchool;

    @Column(length = 2000)
    private String exitInterviewNotes;

    // ========================================================================
    // CLEARANCE CHECKLIST - ACADEMIC (4 items)
    // ========================================================================

    @Column
    private Boolean finalGradesRecorded;

    @Column
    private Boolean transcriptPrinted;

    @Column
    private Boolean iep504Finalized;

    @Column
    private Boolean progressReportsSent;

    // ========================================================================
    // CLEARANCE CHECKLIST - LIBRARY & MATERIALS (6 items)
    // ========================================================================

    @Column
    private Boolean libraryBooksReturned;

    @Column
    private Boolean textbooksReturned;

    @Column
    private Boolean libraryFinesPaid;

    @Column
    private Boolean devicesReturned;

    @Column
    private Boolean athleticEquipmentReturned;

    @Column
    private Boolean instrumentsReturned;

    // ========================================================================
    // CLEARANCE CHECKLIST - FACILITIES (4 items)
    // ========================================================================

    @Column
    private Boolean lockerCleared;

    @Column
    private Boolean lockerLockReturned;

    @Column
    private Boolean parkingPermitReturned;

    @Column
    private Boolean idCardReturned;

    // ========================================================================
    // CLEARANCE CHECKLIST - FINANCIAL (4 items)
    // ========================================================================

    @Column
    private Boolean tuitionPaid;

    @Column
    private Boolean cafeteriaBalanceSettled;

    @Column
    private Boolean activityFeesPaid;

    @Column
    private Boolean damageFeesPaid;

    // ========================================================================
    // CLEARANCE CHECKLIST - ADMINISTRATIVE (6 items)
    // ========================================================================

    @Column
    private Boolean recordsReleaseSigned;

    @Column
    private Boolean immunizationsCopied;

    @Column
    private Boolean paperworkCompleted;

    @Column
    private Boolean parentNotificationSent;

    @Column
    private Boolean withdrawalFormSigned;

    @Column
    private Boolean finalTranscriptRequested;

    // ========================================================================
    // CLEARANCE SUMMARY
    // ========================================================================

    @Column
    private Integer totalClearanceItems;

    @Column
    private Integer clearedItems;

    @Column
    private Boolean allCleared;

    // ========================================================================
    // PARENT CONFIRMATION
    // ========================================================================

    @Column(length = 200)
    private String parentName;

    @Column(length = 20)
    private String parentPhone;

    @Column(length = 100)
    private String parentEmail;

    @Column
    private Boolean acknowledgedWithdrawal;

    @Column
    private Boolean acknowledgedClearance;

    @Column
    private Boolean acknowledgedRecords;

    @Column
    private Boolean acknowledgedNoRefund;

    @Column(length = 200)
    private String parentSignature;

    @Column
    private LocalDate signatureDate;

    // ========================================================================
    // ADMINISTRATIVE
    // ========================================================================

    @Column(length = 2000)
    private String administrativeNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_staff_id")
    private User processedBy;

    @Column
    private LocalDate processingDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FinalStatus finalStatus;

    @Column
    private LocalDate effectiveDate;

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

    public enum WithdrawalStatus {
        DRAFT("Draft"),
        PENDING_CLEARANCE("Pending Clearance"),
        CLEARANCE_IN_PROGRESS("Clearance In Progress"),
        CLEARED("Cleared"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");

        private final String displayName;

        WithdrawalStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum WithdrawalType {
        TRANSFER_TO_ANOTHER_SCHOOL("Transfer to Another School"),
        MOVED_OUT_OF_DISTRICT("Moved Out of District"),
        HOME_SCHOOL("Home School"),
        GRADUATION("Graduation"),
        DROPOUT("Dropout"),
        EXPELLED("Expelled"),
        MEDICAL_REASONS("Medical Reasons"),
        OTHER("Other");

        private final String displayName;

        WithdrawalType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum WithdrawalReason {
        FAMILY_RELOCATION("Family Relocation"),
        TRANSFER_TO_PUBLIC_SCHOOL("Transfer to Public School"),
        TRANSFER_TO_PRIVATE_SCHOOL("Transfer to Private School"),
        ACADEMIC_REASONS("Academic Reasons"),
        BEHAVIORAL_ISSUES("Behavioral Issues"),
        BULLYING_CONCERNS("Bullying Concerns"),
        FINANCIAL_HARDSHIP("Financial Hardship"),
        HEALTH_MEDICAL_REASONS("Health/Medical Reasons"),
        HOMESCHOOLING("Homeschooling"),
        DISSATISFACTION_WITH_SCHOOL("Dissatisfaction with School"),
        COMPLETED_GRADE_12("Completed Grade 12"),
        OTHER("Other");

        private final String displayName;

        WithdrawalReason(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SatisfactionRating {
        VERY_SATISFIED("Very Satisfied"),
        SATISFIED("Satisfied"),
        NEUTRAL("Neutral"),
        DISSATISFIED("Dissatisfied"),
        VERY_DISSATISFIED("Very Dissatisfied");

        private final String displayName;

        SatisfactionRating(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum RecommendSchool {
        YES("Yes"),
        NO("No"),
        MAYBE("Maybe");

        private final String displayName;

        RecommendSchool(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum FinalStatus {
        WITHDRAWN("Withdrawn"),
        TRANSFERRED("Transferred"),
        GRADUATED("Graduated"),
        DROPPED_OUT("Dropped Out"),
        EXPELLED("Expelled");

        private final String displayName;

        FinalStatus(String displayName) {
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
     * Calculate clearance completion
     */
    public void calculateClearanceCompletion() {
        totalClearanceItems = 24;
        clearedItems = 0;

        // Academic (4)
        if (Boolean.TRUE.equals(finalGradesRecorded)) clearedItems++;
        if (Boolean.TRUE.equals(transcriptPrinted)) clearedItems++;
        if (Boolean.TRUE.equals(iep504Finalized)) clearedItems++;
        if (Boolean.TRUE.equals(progressReportsSent)) clearedItems++;

        // Library & Materials (6)
        if (Boolean.TRUE.equals(libraryBooksReturned)) clearedItems++;
        if (Boolean.TRUE.equals(textbooksReturned)) clearedItems++;
        if (Boolean.TRUE.equals(libraryFinesPaid)) clearedItems++;
        if (Boolean.TRUE.equals(devicesReturned)) clearedItems++;
        if (Boolean.TRUE.equals(athleticEquipmentReturned)) clearedItems++;
        if (Boolean.TRUE.equals(instrumentsReturned)) clearedItems++;

        // Facilities (4)
        if (Boolean.TRUE.equals(lockerCleared)) clearedItems++;
        if (Boolean.TRUE.equals(lockerLockReturned)) clearedItems++;
        if (Boolean.TRUE.equals(parkingPermitReturned)) clearedItems++;
        if (Boolean.TRUE.equals(idCardReturned)) clearedItems++;

        // Financial (4)
        if (Boolean.TRUE.equals(tuitionPaid)) clearedItems++;
        if (Boolean.TRUE.equals(cafeteriaBalanceSettled)) clearedItems++;
        if (Boolean.TRUE.equals(activityFeesPaid)) clearedItems++;
        if (Boolean.TRUE.equals(damageFeesPaid)) clearedItems++;

        // Administrative (6)
        if (Boolean.TRUE.equals(recordsReleaseSigned)) clearedItems++;
        if (Boolean.TRUE.equals(immunizationsCopied)) clearedItems++;
        if (Boolean.TRUE.equals(paperworkCompleted)) clearedItems++;
        if (Boolean.TRUE.equals(parentNotificationSent)) clearedItems++;
        if (Boolean.TRUE.equals(withdrawalFormSigned)) clearedItems++;
        if (Boolean.TRUE.equals(finalTranscriptRequested)) clearedItems++;

        allCleared = (clearedItems == totalClearanceItems);
    }

    /**
     * Check if withdrawal can be completed
     */
    public boolean canBeCompleted() {
        calculateClearanceCompletion();
        return Boolean.TRUE.equals(allCleared) &&
               parentSignature != null &&
               Boolean.TRUE.equals(acknowledgedWithdrawal);
    }

    /**
     * Check if all parent acknowledgments are complete
     */
    public boolean areAcknowledgmentsComplete() {
        return Boolean.TRUE.equals(acknowledgedWithdrawal) &&
               Boolean.TRUE.equals(acknowledgedClearance) &&
               Boolean.TRUE.equals(acknowledgedRecords) &&
               Boolean.TRUE.equals(acknowledgedNoRefund);
    }
}
