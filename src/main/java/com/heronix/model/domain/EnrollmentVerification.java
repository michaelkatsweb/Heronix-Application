package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enrollment Verification Record
 *
 * Tracks requests for enrollment verification letters and certificates.
 * Used for proof of enrollment for various purposes (loans, visas, insurance, etc.)
 *
 * Workflow:
 * 1. DRAFT - Request being prepared
 * 2. PENDING_VERIFICATION - Awaiting enrollment status verification
 * 3. VERIFIED - Enrollment verified
 * 4. GENERATED - Document generated
 * 5. DELIVERED - Document delivered to requester
 * 6. CANCELLED - Request cancelled
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Entity
@Table(name = "enrollment_verifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // VERIFICATION METADATA
    // ========================================================================

    @Column(nullable = false, unique = true, length = 50)
    private String verificationNumber; // e.g., "ENV-2025-001234"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationStatus status;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Column
    private LocalDate verificationDate;

    @Column
    private LocalDate deliveryDate;

    // ========================================================================
    // STUDENT INFORMATION
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(length = 20)
    private String studentIdNumber;

    @Column(length = 20)
    private String currentGradeLevel;

    @Column(length = 100)
    private String currentHomeroom;

    @Column
    private LocalDate enrollmentDate;

    @Column
    private LocalDate expectedGraduationDate;

    // ========================================================================
    // REQUESTER INFORMATION
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RequesterType requesterType;

    @Column(length = 100)
    private String requesterName;

    @Column(length = 50)
    private String requesterRelationship; // For parent/guardian/relative

    @Column(length = 200)
    private String requesterOrganization; // For institutions/agencies

    @Column(length = 100)
    private String requesterEmail;

    @Column(length = 20)
    private String requesterPhone;

    @Column(length = 200)
    private String requesterAddress;

    @Column(length = 100)
    private String requesterCity;

    @Column(length = 2)
    private String requesterState;

    @Column(length = 10)
    private String requesterZipCode;

    // ========================================================================
    // PURPOSE & DETAILS
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private VerificationPurpose purpose;

    @Column(length = 2000)
    private String purposeDetails;

    @Column(length = 200)
    private String destinationInstitution; // School, college, agency receiving verification

    @Column(length = 200)
    private String destinationCountry; // For international purposes

    @Column
    private Boolean urgentRequest;

    @Column
    private LocalDate neededByDate;

    // ========================================================================
    // VERIFICATION DETAILS
    // ========================================================================

    @Column
    private Boolean fullTimeEnrollment;

    @Column
    private Boolean partTimeEnrollment;

    @Column(length = 20)
    private String enrollmentStatus; // "Active", "Good Standing", etc.

    @Column
    private Boolean academicGoodStanding;

    @Column
    private Boolean behavioralGoodStanding;

    @Column
    private Boolean hasOutstandingFees;

    @Column
    private Boolean includeGPA;

    @Column
    private Double currentGPA;

    @Column
    private Boolean includeAttendance;

    @Column
    private Double attendancePercentage;

    @Column
    private Boolean includeCourseSchedule;

    @Column(length = 2000)
    private String courseSchedule;

    @Column
    private Boolean includeExpectedGraduation;

    @Column
    private Boolean includeCreditsEarned;

    @Column
    private Integer creditsEarned;

    @Column
    private Integer creditsRequired;

    // ========================================================================
    // DOCUMENT GENERATION
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DocumentType documentType;

    @Column(length = 100)
    private String documentTemplateName;

    @Column
    private Boolean officialSeal;

    @Column
    private Boolean notarized;

    @Column
    private LocalDate notarizationDate;

    @Column(length = 100)
    private String notaryName;

    @Column
    private Boolean apostilleRequired;

    @Column(length = 100)
    private String apostilleCountry;

    @Column(length = 500)
    private String documentFilePath;

    @Column(length = 100)
    private String documentFileName;

    // ========================================================================
    // DELIVERY INFORMATION
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DeliveryMethod deliveryMethod;

    @Column(length = 200)
    private String deliveryAddress;

    @Column(length = 100)
    private String deliveryEmail;

    @Column(length = 50)
    private String trackingNumber;

    @Column
    private Boolean signatureRequired;

    @Column(length = 2000)
    private String deliveryNotes;

    // ========================================================================
    // FEES & PAYMENT
    // ========================================================================

    @Column
    private Double verificationFee;

    @Column
    private Double rushFee;

    @Column
    private Double apostilleFee;

    @Column
    private Double deliveryFee;

    @Column
    private Double totalFee;

    @Column
    private Boolean feePaid;

    @Column
    private LocalDate paymentDate;

    @Column(length = 100)
    private String paymentMethod;

    @Column(length = 100)
    private String paymentReferenceNumber;

    // ========================================================================
    // AUTHORIZATION & CONSENT
    // ========================================================================

    @Column
    private Boolean studentConsentObtained;

    @Column
    private LocalDate consentDate;

    @Column(length = 200)
    private String consentSignature;

    @Column
    private Boolean parentConsentRequired;

    @Column
    private Boolean parentConsentObtained;

    @Column(length = 200)
    private String parentSignature;

    @Column(length = 500)
    private String consentDocumentPath;

    // ========================================================================
    // VERIFICATION & APPROVAL
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_staff_id")
    private User verifiedBy;

    @Column
    private LocalDate verifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_staff_id")
    private User approvedBy;

    @Column
    private LocalDate approvedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by_staff_id")
    private User generatedBy;

    @Column
    private LocalDate generatedDate;

    // ========================================================================
    // VALIDITY & EXPIRATION
    // ========================================================================

    @Column
    private LocalDate validFrom;

    @Column
    private LocalDate validUntil;

    @Column
    private Integer validityDays; // Days document is valid

    @Column
    private Boolean hasExpiration;

    // ========================================================================
    // ADMINISTRATIVE
    // ========================================================================

    @Column(length = 2000)
    private String administrativeNotes;

    @Column(length = 2000)
    private String specialInstructions;

    @Column(length = 2000)
    private String rejectionReason;

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

    public enum VerificationStatus {
        DRAFT("Draft"),
        PENDING_VERIFICATION("Pending Verification"),
        VERIFIED("Verified"),
        GENERATED("Document Generated"),
        DELIVERED("Delivered"),
        CANCELLED("Cancelled");

        private final String displayName;

        VerificationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum RequesterType {
        STUDENT("Student"),
        PARENT_GUARDIAN("Parent/Guardian"),
        RELATIVE("Relative"),
        SCHOOL("Educational Institution"),
        GOVERNMENT_AGENCY("Government Agency"),
        INSURANCE_COMPANY("Insurance Company"),
        EMPLOYER("Employer"),
        LENDING_INSTITUTION("Lending Institution"),
        IMMIGRATION("Immigration Services"),
        OTHER("Other");

        private final String displayName;

        RequesterType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum VerificationPurpose {
        COLLEGE_APPLICATION("College Application"),
        SCHOLARSHIP("Scholarship Application"),
        STUDENT_LOAN("Student Loan"),
        INSURANCE("Insurance Purposes"),
        VISA_IMMIGRATION("Visa/Immigration"),
        WORK_PERMIT("Work Permit"),
        GOVERNMENT_BENEFIT("Government Benefit"),
        EMPLOYMENT("Employment Verification"),
        HOUSING("Housing Application"),
        TRANSFER_SCHOOL("Transfer to Another School"),
        FINANCIAL_AID("Financial Aid"),
        TAX_PURPOSES("Tax Purposes"),
        OTHER("Other");

        private final String displayName;

        VerificationPurpose(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DocumentType {
        ENROLLMENT_LETTER("Enrollment Verification Letter"),
        ENROLLMENT_CERTIFICATE("Enrollment Certificate"),
        GOOD_STANDING_LETTER("Letter of Good Standing"),
        TRANSCRIPT_WITH_ENROLLMENT("Transcript with Enrollment Verification"),
        ATTENDANCE_CERTIFICATE("Attendance Certificate"),
        EXPECTED_GRADUATION_LETTER("Expected Graduation Letter"),
        FULL_TIME_STATUS_LETTER("Full-Time Status Letter"),
        CUSTOM_VERIFICATION("Custom Verification");

        private final String displayName;

        DocumentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DeliveryMethod {
        EMAIL("Email"),
        POSTAL_MAIL("Postal Mail"),
        EXPRESS_MAIL("Express Mail"),
        PICKUP("In-Person Pickup"),
        FAX("Fax"),
        ELECTRONIC_PORTAL("Electronic Portal"),
        COURIER("Courier Service");

        private final String displayName;

        DeliveryMethod(String displayName) {
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
     * Calculate total fee
     */
    public void calculateTotalFee() {
        totalFee = 0.0;
        if (verificationFee != null) totalFee += verificationFee;
        if (rushFee != null) totalFee += rushFee;
        if (apostilleFee != null) totalFee += apostilleFee;
        if (deliveryFee != null) totalFee += deliveryFee;
    }

    /**
     * Check if ready for generation
     */
    public boolean isReadyForGeneration() {
        return status == VerificationStatus.VERIFIED &&
               Boolean.TRUE.equals(studentConsentObtained) &&
               (!Boolean.TRUE.equals(parentConsentRequired) || Boolean.TRUE.equals(parentConsentObtained)) &&
               (totalFee == 0.0 || Boolean.TRUE.equals(feePaid));
    }

    /**
     * Check if document is valid
     */
    public boolean isValid() {
        if (!Boolean.TRUE.equals(hasExpiration)) {
            return true;
        }
        LocalDate today = LocalDate.now();
        return validFrom != null && validUntil != null &&
               !today.isBefore(validFrom) && !today.isAfter(validUntil);
    }

    /**
     * Calculate validity period
     */
    public void calculateValidityPeriod() {
        if (Boolean.TRUE.equals(hasExpiration) && validFrom != null && validityDays != null) {
            validUntil = validFrom.plusDays(validityDays);
        }
    }
}
