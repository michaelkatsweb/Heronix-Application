package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Enrollment Application for In-House Student Registration
 *
 * Represents the complete enrollment application for a new or returning student.
 * Managed by office staff during in-person registration.
 *
 * Workflow:
 * 1. DRAFT - Staff creates application, collects basic info
 * 2. DOCUMENTS_PENDING - Waiting for required documents (birth cert, residency, etc.)
 * 3. VERIFICATION_IN_PROGRESS - Staff verifying submitted documents
 * 4. RECORDS_REQUESTED - Waiting for records from previous school (IEP, transcript, etc.)
 * 5. PENDING_APPROVAL - All docs complete, awaiting admin approval
 * 6. APPROVED - Ready to create student account
 * 7. ENROLLED - Student account created, enrollment complete
 * 8. REJECTED - Application denied
 * 9. WITHDRAWN - Family withdrew application
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Inquiry and Registration System
 */
@Entity
@Table(name = "enrollment_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // APPLICATION METADATA
    // ========================================================================

    @Column(nullable = false, unique = true, length = 50)
    private String applicationNumber; // e.g., "2025-001234"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentType enrollmentType;

    @Column(nullable = false)
    private LocalDate applicationDate;

    @Column(length = 10)
    private String intendedGradeLevel; // "9", "10", "11", "12"

    @Column(length = 20)
    private String intendedSchoolYear; // "2025-2026"

    @Column
    private LocalDate requestedStartDate;

    // ========================================================================
    // STUDENT INFORMATION
    // ========================================================================

    @Column(length = 100, nullable = true)
    private String studentFirstName;

    @Column(length = 100, nullable = true)
    private String studentMiddleName;

    @Column(length = 100, nullable = true)
    private String studentLastName;

    @Column(length = 50, nullable = true)
    private String studentPreferredName;

    @Column(nullable = true)
    private LocalDate studentDateOfBirth;

    @Column(length = 20)
    private String studentGender; // "Male", "Female", "Non-binary", "Prefer not to say"

    @Column(length = 50)
    private String studentRace;

    @Column(length = 50)
    private String studentEthnicity;

    @Column(length = 100)
    private String studentNationality;

    @Column(length = 100)
    private String primaryLanguage;

    @Column
    private Boolean isEnglishLearner;

    // ========================================================================
    // CONTACT INFORMATION
    // ========================================================================

    @Column(length = 200)
    private String residentialAddress;

    @Column(length = 100)
    private String residentialCity;

    @Column(length = 50)
    private String residentialState;

    @Column(length = 20)
    private String residentialZipCode;

    @Column(length = 100)
    private String residentialCounty;

    @Column(length = 200)
    private String mailingAddress; // If different from residential

    @Column(length = 20)
    private String studentPhoneNumber;

    @Column(length = 100)
    private String studentEmail;

    // ========================================================================
    // PARENT/GUARDIAN 1
    // ========================================================================

    @Column(length = 100, nullable = true)
    private String parent1FirstName;

    @Column(length = 100, nullable = true)
    private String parent1LastName;

    @Column(length = 50, nullable = true)
    private String parent1Relationship; // "Mother", "Father", "Guardian", "Grandparent"

    @Column(length = 20, nullable = true)
    private String parent1PhoneNumber;

    @Column(length = 100, nullable = true)
    private String parent1Email;

    @Column(length = 100, nullable = true)
    private String parent1Employer;

    @Column(length = 20, nullable = true)
    private String parent1WorkPhone;

    @Column
    private Boolean parent1IsCustodial;

    @Column
    private Boolean parent1CanPickup;

    // ========================================================================
    // PARENT/GUARDIAN 2 (Optional)
    // ========================================================================

    @Column(length = 100, nullable = true)
    private String parent2FirstName;

    @Column(length = 100, nullable = true)
    private String parent2LastName;

    @Column(length = 50, nullable = true)
    private String parent2Relationship;

    @Column(length = 20, nullable = true)
    private String parent2PhoneNumber;

    @Column(length = 100, nullable = true)
    private String parent2Email;

    @Column(length = 100, nullable = true)
    private String parent2Employer;

    @Column(length = 20, nullable = true)
    private String parent2WorkPhone;

    @Column
    private Boolean parent2IsCustodial;

    @Column
    private Boolean parent2CanPickup;

    // ========================================================================
    // EMERGENCY CONTACTS
    // ========================================================================

    @Column(length = 100)
    private String emergencyContact1Name;

    @Column(length = 50)
    private String emergencyContact1Relationship;

    @Column(length = 20)
    private String emergencyContact1Phone;

    @Column(length = 100)
    private String emergencyContact2Name;

    @Column(length = 50)
    private String emergencyContact2Relationship;

    @Column(length = 20)
    private String emergencyContact2Phone;

    // ========================================================================
    // PREVIOUS SCHOOL INFORMATION
    // ========================================================================

    @Column(length = 200)
    private String previousSchoolName;

    @Column(length = 100)
    private String previousSchoolDistrict;

    @Column(length = 100)
    private String previousSchoolCity;

    @Column(length = 50)
    private String previousSchoolState;

    @Column(length = 20)
    private String previousSchoolPhone;

    @Column
    private LocalDate lastAttendanceDate;

    @Column(length = 10)
    private String lastGradeCompleted;

    @Column
    private Boolean isTransferStudent;

    // ========================================================================
    // SPECIAL PROGRAMS AND SERVICES
    // ========================================================================

    @Column
    private Boolean hasIEP;

    @Column
    private Boolean has504Plan;

    @Column
    private Boolean isGifted;

    @Column
    private Boolean needsESLServices;

    @Column
    private Boolean needsSpecialTransportation;

    @Column
    private Boolean isHomeless;

    @Column
    private Boolean isFosterCare;

    @Column
    private Boolean isMilitary;

    @Column(length = 50)
    private String lunchStatus; // "Free", "Reduced", "Paid"

    // ========================================================================
    // MEDICAL INFORMATION
    // ========================================================================

    @Column(length = 1000)
    private String medicalConditions;

    @Column(length = 1000)
    private String medications;

    @Column(length = 1000)
    private String allergies;

    @Column(length = 500)
    private String dietaryRestrictions;

    @Column
    private Boolean requiresDailyMedication;

    // ========================================================================
    // DOCUMENT VERIFICATION
    // ========================================================================

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EnrollmentDocument> documents = new ArrayList<>();

    @Column
    private Boolean birthCertificateVerified;

    @Column
    private Boolean residencyVerified;

    @Column
    private Boolean immunizationsVerified;

    @Column
    private Boolean transcriptReceived;

    @Column
    private Boolean iepReceived; // If applicable

    @Column
    private Boolean plan504Received; // If applicable

    // ========================================================================
    // APPROVAL AND ENROLLMENT
    // ========================================================================

    @Column
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime approvedAt;

    @Column
    private LocalDateTime enrolledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_staff_id")
    private User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_staff_id")
    private User processedBy;

    @Column(length = 1000)
    private String approvalNotes;

    @Column(length = 1000)
    private String rejectionReason;

    // Link to created student (once enrolled)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

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

    @Column(length = 2000)
    private String notes;

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Get student full name
     */
    public String getStudentFullName() {
        StringBuilder name = new StringBuilder();
        name.append(studentFirstName != null ? studentFirstName : "");
        if (studentMiddleName != null && !studentMiddleName.isEmpty()) {
            name.append(" ").append(studentMiddleName);
        }
        name.append(" ").append(studentLastName != null ? studentLastName : "");
        return name.toString().trim();
    }

    /**
     * Check if all required documents are verified
     */
    public boolean areRequiredDocumentsComplete() {
        boolean basicDocs = Boolean.TRUE.equals(birthCertificateVerified) &&
                           Boolean.TRUE.equals(residencyVerified) &&
                           Boolean.TRUE.equals(immunizationsVerified);

        // If transfer student, must have transcript
        if (Boolean.TRUE.equals(isTransferStudent)) {
            basicDocs = basicDocs && Boolean.TRUE.equals(transcriptReceived);
        }

        // If has IEP, must receive IEP
        if (Boolean.TRUE.equals(hasIEP)) {
            basicDocs = basicDocs && Boolean.TRUE.equals(iepReceived);
        }

        // If has 504, must receive 504 plan
        if (Boolean.TRUE.equals(has504Plan)) {
            basicDocs = basicDocs && Boolean.TRUE.equals(plan504Received);
        }

        return basicDocs;
    }

    /**
     * Check if application can be approved
     */
    public boolean canBeApproved() {
        return status == ApplicationStatus.PENDING_APPROVAL &&
               areRequiredDocumentsComplete();
    }

    /**
     * Add document to application
     */
    public void addDocument(EnrollmentDocument document) {
        documents.add(document);
        document.setApplication(this);
    }

    /**
     * Remove document from application
     */
    public void removeDocument(EnrollmentDocument document) {
        documents.remove(document);
        document.setApplication(null);
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum ApplicationStatus {
        DRAFT("Draft - In Progress"),
        DOCUMENTS_PENDING("Waiting for Documents"),
        VERIFICATION_IN_PROGRESS("Verifying Documents"),
        RECORDS_REQUESTED("Waiting for Previous School Records"),
        PENDING_APPROVAL("Pending Administrator Approval"),
        APPROVED("Approved - Ready to Enroll"),
        ENROLLED("Enrolled - Student Created"),
        REJECTED("Application Rejected"),
        WITHDRAWN("Application Withdrawn");

        private final String displayName;

        ApplicationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum EnrollmentType {
        NEW_STUDENT("New Student - First Time"),
        TRANSFER("Transfer from Another School"),
        RE_ENROLLMENT("Returning Student"),
        MID_YEAR("Mid-Year Enrollment");

        private final String displayName;

        EnrollmentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
