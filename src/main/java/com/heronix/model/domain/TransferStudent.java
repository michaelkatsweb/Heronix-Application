package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transfer Student Record
 *
 * Tracks incoming transfer students with their previous school information,
 * record transfer status, and enrollment processing.
 *
 * Workflow:
 * 1. DRAFT - Transfer application being prepared
 * 2. PENDING_VERIFICATION - Awaiting verification of previous enrollment
 * 3. RECORDS_REQUESTED - Records request sent to previous school
 * 4. PARTIAL_RECORDS - Some records received
 * 5. COMPLETE_RECORDS - All records received
 * 6. READY_FOR_PLACEMENT - Ready for grade/course placement
 * 7. ENROLLED - Successfully enrolled
 * 8. CANCELLED - Transfer cancelled
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Entity
@Table(name = "transfer_students")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // TRANSFER METADATA
    // ========================================================================

    @Column(nullable = false, unique = true, length = 50)
    private String transferNumber; // e.g., "TRN-2025-001234"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransferStatus status;

    @Column(nullable = false)
    private LocalDate transferDate;

    @Column
    private LocalDate expectedEnrollmentDate;

    // ========================================================================
    // STUDENT INFORMATION
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student; // May be null initially, linked after student creation

    @Column(length = 100)
    private String studentFirstName;

    @Column(length = 100)
    private String studentLastName;

    @Column
    private LocalDate studentDateOfBirth;

    @Column(length = 50)
    private String studentGender;

    @Column(length = 20)
    private String currentGradeLevel;

    @Column(length = 20)
    private String proposedGradeLevel;

    // ========================================================================
    // PREVIOUS SCHOOL INFORMATION
    // ========================================================================

    @Column(length = 200)
    private String previousSchoolName;

    @Column(length = 200)
    private String previousSchoolDistrict;

    @Column(length = 100)
    private String previousSchoolCity;

    @Column(length = 2)
    private String previousSchoolState;

    @Column(length = 10)
    private String previousSchoolZip;

    @Column(length = 20)
    private String previousSchoolPhone;

    @Column(length = 100)
    private String previousSchoolEmail;

    @Column
    private LocalDate lastAttendanceDate;

    @Column(length = 20)
    private String lastGradeCompleted;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private WithdrawalReason reasonForLeaving;

    // ========================================================================
    // RECORDS TRANSFER
    // ========================================================================

    @Column
    private Boolean transcriptReceived;

    @Column
    private LocalDate transcriptReceivedDate;

    @Column
    private Boolean immunizationRecordsReceived;

    @Column
    private LocalDate immunizationReceivedDate;

    @Column
    private Boolean iepReceived;

    @Column
    private LocalDate iepReceivedDate;

    @Column
    private Boolean plan504Received;

    @Column
    private LocalDate plan504ReceivedDate;

    @Column
    private Boolean disciplineRecordsReceived;

    @Column
    private LocalDate disciplineReceivedDate;

    @Column
    private Boolean attendanceRecordsReceived;

    @Column
    private LocalDate attendanceReceivedDate;

    @Column
    private Boolean testScoresReceived;

    @Column
    private LocalDate testScoresReceivedDate;

    @Column
    private Integer totalRecordsExpected;

    @Column
    private Integer recordsReceived;

    @Column
    private Boolean allRecordsReceived;

    // ========================================================================
    // PLACEMENT & ASSESSMENT
    // ========================================================================

    @Column
    private Boolean placementTestRequired;

    @Column
    private LocalDate placementTestDate;

    @Column(length = 50)
    private String placementTestResult;

    @Column
    private Boolean englishProficiencyRequired;

    @Column(length = 50)
    private String englishProficiencyLevel;

    @Column
    private Boolean specialEducationEvaluation;

    @Column
    private Boolean giftedTalentedEvaluation;

    @Column(length = 2000)
    private String placementNotes;

    // ========================================================================
    // PARENT/GUARDIAN INFORMATION
    // ========================================================================

    @Column(length = 100)
    private String parentFirstName;

    @Column(length = 100)
    private String parentLastName;

    @Column(length = 20)
    private String parentPhone;

    @Column(length = 100)
    private String parentEmail;

    @Column(length = 50)
    private String parentRelationship;

    @Column(length = 200)
    private String residentialAddress;

    @Column(length = 100)
    private String residentialCity;

    @Column(length = 2)
    private String residentialState;

    @Column(length = 10)
    private String residentialZipCode;

    @Column
    private Boolean residencyVerified;

    @Column
    private LocalDate residencyVerifiedDate;

    // ========================================================================
    // TRANSFER REASON & CIRCUMSTANCES
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TransferType transferType;

    @Column(length = 2000)
    private String transferReason;

    @Column
    private Boolean isMidYearTransfer;

    @Column
    private Boolean isEmergencyTransfer;

    @Column
    private Boolean requiresInterpreter;

    @Column(length = 50)
    private String preferredLanguage;

    // ========================================================================
    // ENROLLMENT PROCESSING
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_counselor_id")
    private User assignedCounselor;

    @Column
    private LocalDate counselorReviewDate;

    @Column(length = 2000)
    private String counselorNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_application_id")
    private EnrollmentApplication enrollmentApplication;

    @Column
    private LocalDate enrollmentDate;

    @Column(length = 20)
    private String assignedGradeLevel;

    @Column(length = 100)
    private String assignedHomeroom;

    // ========================================================================
    // DOCUMENTS & VERIFICATION
    // ========================================================================

    @Column
    private Boolean birthCertificateVerified;

    @Column
    private Boolean proofOfResidencyVerified;

    @Column
    private Boolean parentIdVerified;

    @Column
    private Boolean custodyDocumentsReceived;

    @Column(length = 2000)
    private String documentNotes;

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

    public enum TransferStatus {
        DRAFT("Draft"),
        PENDING_VERIFICATION("Pending Verification"),
        RECORDS_REQUESTED("Records Requested"),
        PARTIAL_RECORDS("Partial Records Received"),
        COMPLETE_RECORDS("Complete Records Received"),
        READY_FOR_PLACEMENT("Ready for Placement"),
        ENROLLED("Enrolled"),
        CANCELLED("Cancelled");

        private final String displayName;

        TransferStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TransferType {
        WITHIN_DISTRICT("Within District Transfer"),
        OUT_OF_DISTRICT("Out of District Transfer"),
        OUT_OF_STATE("Out of State Transfer"),
        INTERNATIONAL("International Transfer"),
        HOME_SCHOOL_TO_SCHOOL("Homeschool to School"),
        PRIVATE_TO_PUBLIC("Private to Public School"),
        PUBLIC_TO_PRIVATE("Public to Private School");

        private final String displayName;

        TransferType(String displayName) {
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

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Calculate records completion percentage
     */
    public void calculateRecordsCompletion() {
        totalRecordsExpected = 7; // transcript, immunization, IEP, 504, discipline, attendance, test scores
        recordsReceived = 0;

        if (Boolean.TRUE.equals(transcriptReceived)) recordsReceived++;
        if (Boolean.TRUE.equals(immunizationRecordsReceived)) recordsReceived++;
        if (Boolean.TRUE.equals(iepReceived)) recordsReceived++;
        if (Boolean.TRUE.equals(plan504Received)) recordsReceived++;
        if (Boolean.TRUE.equals(disciplineRecordsReceived)) recordsReceived++;
        if (Boolean.TRUE.equals(attendanceRecordsReceived)) recordsReceived++;
        if (Boolean.TRUE.equals(testScoresReceived)) recordsReceived++;

        allRecordsReceived = (recordsReceived >= totalRecordsExpected);
    }

    /**
     * Check if ready for enrollment
     */
    public boolean isReadyForEnrollment() {
        calculateRecordsCompletion();
        return Boolean.TRUE.equals(allRecordsReceived) &&
               Boolean.TRUE.equals(birthCertificateVerified) &&
               Boolean.TRUE.equals(proofOfResidencyVerified) &&
               proposedGradeLevel != null;
    }

    /**
     * Get student full name
     */
    public String getStudentFullName() {
        StringBuilder name = new StringBuilder();
        if (studentFirstName != null) name.append(studentFirstName);
        if (studentLastName != null) {
            if (name.length() > 0) name.append(" ");
            name.append(studentLastName);
        }
        return name.toString().trim();
    }
}
