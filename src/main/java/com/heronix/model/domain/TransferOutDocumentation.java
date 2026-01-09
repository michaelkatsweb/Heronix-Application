package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transfer Out Documentation Record
 *
 * Tracks students transferring out to other schools with documentation,
 * records transfer, and acknowledgment tracking.
 *
 * Workflow:
 * 1. DRAFT - Transfer out request being prepared
 * 2. PENDING_WITHDRAWAL - Awaiting withdrawal processing
 * 3. RECORDS_PREPARATION - Preparing records package
 * 4. READY_TO_SEND - Records package ready for transmission
 * 5. SENT - Records sent to destination school
 * 6. ACKNOWLEDGED - Destination school acknowledged receipt
 * 7. COMPLETED - Transfer out process completed
 * 8. CANCELLED - Transfer out cancelled
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Entity
@Table(name = "transfer_out_documentation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferOutDocumentation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // TRANSFER OUT METADATA
    // ========================================================================

    @Column(nullable = false, unique = true, length = 50)
    private String transferOutNumber; // e.g., "TRO-2025-001234"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransferOutStatus status;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Column
    private LocalDate expectedTransferDate;

    @Column
    private LocalDate actualTransferDate;

    // ========================================================================
    // STUDENT INFORMATION
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "withdrawal_record_id")
    private WithdrawalRecord withdrawalRecord; // Link to withdrawal if applicable

    @Column(length = 20)
    private String currentGradeLevel;

    @Column(length = 100)
    private String currentHomeroom;

    @Column
    private LocalDate lastAttendanceDate;

    // ========================================================================
    // DESTINATION SCHOOL INFORMATION
    // ========================================================================

    @Column(length = 200)
    private String destinationSchoolName;

    @Column(length = 200)
    private String destinationSchoolDistrict;

    @Column(length = 100)
    private String destinationSchoolCity;

    @Column(length = 2)
    private String destinationSchoolState;

    @Column(length = 10)
    private String destinationSchoolZip;

    @Column(length = 20)
    private String destinationSchoolPhone;

    @Column(length = 100)
    private String destinationSchoolEmail;

    @Column(length = 100)
    private String destinationSchoolFax;

    @Column(length = 200)
    private String destinationContactPerson;

    @Column(length = 100)
    private String destinationContactEmail;

    @Column(length = 20)
    private String destinationContactPhone;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TransferOutType transferType;

    // ========================================================================
    // RECORDS PACKAGE CONTENTS
    // ========================================================================

    @Column
    private Boolean transcriptIncluded;

    @Column
    private LocalDate transcriptGeneratedDate;

    @Column
    private Boolean immunizationRecordsIncluded;

    @Column
    private Boolean iepIncluded;

    @Column
    private Boolean plan504Included;

    @Column
    private Boolean disciplineRecordsIncluded;

    @Column
    private Boolean attendanceRecordsIncluded;

    @Column
    private Boolean testScoresIncluded;

    @Column
    private Boolean healthRecordsIncluded;

    @Column
    private Boolean specialEducationRecordsIncluded;

    @Column
    private Boolean counselingRecordsIncluded;

    @Column
    private Boolean athleticEligibilityIncluded;

    @Column
    private Boolean cumulativeFolderIncluded;

    @Column
    private Integer totalDocumentsIncluded;

    @Column
    private Integer documentsPackaged;

    @Column
    private Boolean allDocumentsPackaged;

    // ========================================================================
    // TRANSMISSION INFORMATION
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TransmissionMethod transmissionMethod;

    @Column
    private LocalDate sentDate;

    @Column(length = 100)
    private String trackingNumber;

    @Column(length = 100)
    private String confirmationNumber;

    @Column
    private Boolean requiresParentConsent;

    @Column
    private Boolean parentConsentObtained;

    @Column
    private LocalDate parentConsentDate;

    @Column
    private Boolean ferpaReleaseObtained;

    @Column
    private LocalDate ferpaReleaseDate;

    // ========================================================================
    // ACKNOWLEDGMENT & COMPLETION
    // ========================================================================

    @Column
    private Boolean destinationAcknowledged;

    @Column
    private LocalDate acknowledgmentDate;

    @Column(length = 100)
    private String acknowledgedBy;

    @Column(length = 500)
    private String acknowledgmentMethod; // Email, Phone, Fax, Letter

    @Column(length = 2000)
    private String acknowledgmentNotes;

    @Column
    private Boolean recordsReceivedInGoodOrder;

    @Column(length = 2000)
    private String receivingSchoolFeedback;

    // ========================================================================
    // FEES & CHARGES
    // ========================================================================

    @Column
    private Boolean hasOutstandingFees;

    @Column
    private Double outstandingFeesAmount;

    @Column
    private Boolean feesPaidBeforeRelease;

    @Column
    private Boolean recordsHeldForNonPayment;

    @Column
    private Double documentPreparationFee;

    @Column
    private Double rushProcessingFee;

    @Column
    private Double shippingFee;

    @Column
    private Double totalFees;

    @Column
    private Boolean feesPaid;

    @Column
    private LocalDate feesPaidDate;

    // ========================================================================
    // URGENCY & SPECIAL HANDLING
    // ========================================================================

    @Column
    private Boolean urgentRequest;

    @Column
    private Boolean expeditedProcessing;

    @Column(length = 2000)
    private String urgencyReason;

    @Column
    private Boolean certifiedMailRequired;

    @Column
    private Boolean signatureRequired;

    @Column
    private Boolean internationalTransfer;

    @Column
    private Boolean apostilleRequired;

    @Column
    private Boolean translationRequired;

    @Column(length = 50)
    private String translationLanguage;

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

    @Column(length = 2000)
    private String parentRequestNotes;

    // ========================================================================
    // TRANSFER REASON & CIRCUMSTANCES
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TransferOutReason transferReason;

    @Column(length = 2000)
    private String transferReasonDetails;

    @Column
    private Boolean isMidYearTransfer;

    @Column
    private Boolean isEmergencyTransfer;

    @Column(length = 2000)
    private String specialCircumstances;

    // ========================================================================
    // PROCESSING INFORMATION
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;

    @Column
    private LocalDate processingStartDate;

    @Column
    private LocalDate processingCompletedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prepared_by_staff_id")
    private User preparedBy;

    @Column
    private LocalDate preparedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sent_by_staff_id")
    private User sentBy;

    @Column(length = 2000)
    private String processingNotes;

    @Column(length = 2000)
    private String administrativeNotes;

    // ========================================================================
    // FOLLOW-UP
    // ========================================================================

    @Column
    private Boolean followUpRequired;

    @Column
    private LocalDate followUpDate;

    @Column(length = 2000)
    private String followUpNotes;

    @Column
    private Integer followUpAttempts;

    @Column
    private LocalDate lastFollowUpDate;

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

    public enum TransferOutStatus {
        DRAFT("Draft"),
        PENDING_WITHDRAWAL("Pending Withdrawal"),
        RECORDS_PREPARATION("Records Preparation"),
        PENDING_CONSENT("Pending Parent Consent"),
        PENDING_FEES("Pending Fee Payment"),
        READY_TO_SEND("Ready to Send"),
        SENT("Sent"),
        ACKNOWLEDGED("Acknowledged"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");

        private final String displayName;

        TransferOutStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TransferOutType {
        WITHIN_DISTRICT("Within District Transfer"),
        OUT_OF_DISTRICT("Out of District Transfer"),
        OUT_OF_STATE("Out of State Transfer"),
        INTERNATIONAL("International Transfer"),
        PRIVATE_SCHOOL("Transfer to Private School"),
        HOMESCHOOL("Transfer to Homeschool"),
        ONLINE_SCHOOL("Transfer to Online School"),
        ALTERNATIVE_PROGRAM("Transfer to Alternative Program");

        private final String displayName;

        TransferOutType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TransmissionMethod {
        US_MAIL("US Mail"),
        CERTIFIED_MAIL("Certified Mail"),
        COURIER("Courier Service"),
        FAX("Fax"),
        EMAIL("Email"),
        ELECTRONIC_TRANSCRIPT("Electronic Transcript System"),
        PARCHMENT("Parchment"),
        NAVIANCE("Naviance"),
        HAND_DELIVERY("Hand Delivery to Parent"),
        PICKUP("Picked up by Parent");

        private final String displayName;

        TransmissionMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TransferOutReason {
        FAMILY_RELOCATION("Family Relocation"),
        PARENTAL_CHOICE("Parental Choice"),
        ACADEMIC_PROGRAM("Seeking Different Academic Program"),
        SPECIAL_EDUCATION("Special Education Services"),
        ATHLETIC_OPPORTUNITY("Athletic Opportunity"),
        DISTRICT_BOUNDARY("District Boundary Change"),
        SAFETY_CONCERNS("Safety Concerns"),
        TRANSPORTATION("Transportation Issues"),
        MEDICAL_REASONS("Medical Reasons"),
        MILITARY_TRANSFER("Military Family Transfer"),
        OTHER("Other");

        private final String displayName;

        TransferOutReason(String displayName) {
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
     * Calculate documents completion percentage
     */
    public void calculateDocumentsCompletion() {
        totalDocumentsIncluded = 12; // Total possible documents
        documentsPackaged = 0;

        if (Boolean.TRUE.equals(transcriptIncluded)) documentsPackaged++;
        if (Boolean.TRUE.equals(immunizationRecordsIncluded)) documentsPackaged++;
        if (Boolean.TRUE.equals(iepIncluded)) documentsPackaged++;
        if (Boolean.TRUE.equals(plan504Included)) documentsPackaged++;
        if (Boolean.TRUE.equals(disciplineRecordsIncluded)) documentsPackaged++;
        if (Boolean.TRUE.equals(attendanceRecordsIncluded)) documentsPackaged++;
        if (Boolean.TRUE.equals(testScoresIncluded)) documentsPackaged++;
        if (Boolean.TRUE.equals(healthRecordsIncluded)) documentsPackaged++;
        if (Boolean.TRUE.equals(specialEducationRecordsIncluded)) documentsPackaged++;
        if (Boolean.TRUE.equals(counselingRecordsIncluded)) documentsPackaged++;
        if (Boolean.TRUE.equals(athleticEligibilityIncluded)) documentsPackaged++;
        if (Boolean.TRUE.equals(cumulativeFolderIncluded)) documentsPackaged++;

        allDocumentsPackaged = (documentsPackaged >= totalDocumentsIncluded);
    }

    /**
     * Calculate total fees
     */
    public void calculateTotalFees() {
        totalFees = 0.0;
        if (documentPreparationFee != null) totalFees += documentPreparationFee;
        if (rushProcessingFee != null) totalFees += rushProcessingFee;
        if (shippingFee != null) totalFees += shippingFee;
    }

    /**
     * Check if ready to send
     */
    public boolean isReadyToSend() {
        calculateDocumentsCompletion();
        return Boolean.TRUE.equals(allDocumentsPackaged) &&
               (!Boolean.TRUE.equals(requiresParentConsent) || Boolean.TRUE.equals(parentConsentObtained)) &&
               (!Boolean.TRUE.equals(hasOutstandingFees) || Boolean.TRUE.equals(feesPaidBeforeRelease)) &&
               destinationSchoolName != null &&
               transmissionMethod != null;
    }

    /**
     * Check if completed
     */
    public boolean isCompleted() {
        return status == TransferOutStatus.COMPLETED &&
               Boolean.TRUE.equals(destinationAcknowledged) &&
               sentDate != null;
    }

    /**
     * Get destination school full address
     */
    public String getDestinationSchoolFullAddress() {
        StringBuilder address = new StringBuilder();
        if (destinationSchoolName != null) address.append(destinationSchoolName);
        if (destinationSchoolCity != null) {
            if (address.length() > 0) address.append(", ");
            address.append(destinationSchoolCity);
        }
        if (destinationSchoolState != null) {
            if (address.length() > 0) address.append(", ");
            address.append(destinationSchoolState);
        }
        if (destinationSchoolZip != null) {
            if (address.length() > 0) address.append(" ");
            address.append(destinationSchoolZip);
        }
        return address.toString().trim();
    }
}
