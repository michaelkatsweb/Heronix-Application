package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Record Transfer Request
 *
 * Represents a request to previous school for student records (transcript, IEP, 504, etc.)
 * Tracks the complete lifecycle of requesting and receiving records from another institution.
 *
 * Workflow:
 * 1. DRAFT - Request being prepared
 * 2. SENT - Request sent to previous school (fax, email, mail)
 * 3. ACKNOWLEDGED - Previous school confirmed receipt
 * 4. IN_PROGRESS - Previous school is preparing records
 * 5. RECEIVED - Records received (partial or complete)
 * 6. COMPLETE - All requested records received and verified
 * 7. CANCELLED - Request cancelled or withdrawn
 * 8. FAILED - Unable to obtain records from previous school
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Inquiry and Registration System
 */
@Entity
@Table(name = "record_transfer_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordTransferRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Parent enrollment application
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private EnrollmentApplication application;

    // ========================================================================
    // REQUEST METADATA
    // ========================================================================

    @Column(nullable = false, unique = true, length = 50)
    private String requestNumber; // e.g., "RTR-2025-001234"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private RequestStatus status = RequestStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Column
    private LocalDate dueDate; // Expected response date (typically 10 business days)

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RequestMethod requestMethod; // FAX, EMAIL, MAIL, ONLINE

    // ========================================================================
    // PREVIOUS SCHOOL INFORMATION
    // ========================================================================

    @Column(nullable = false, length = 200)
    private String schoolName;

    @Column(length = 100)
    private String schoolDistrict;

    @Column(length = 200)
    private String schoolAddress;

    @Column(length = 100)
    private String schoolCity;

    @Column(length = 50)
    private String schoolState;

    @Column(length = 20)
    private String schoolZipCode;

    @Column(length = 20)
    private String schoolPhone;

    @Column(length = 20)
    private String schoolFax;

    @Column(length = 100)
    private String schoolEmail;

    @Column(length = 100)
    private String registrarName;

    @Column(length = 100)
    private String registrarEmail;

    @Column(length = 20)
    private String registrarPhone;

    // ========================================================================
    // REQUESTED RECORDS
    // ========================================================================

    @Column
    @Builder.Default
    private Boolean requestTranscript = true;

    @Column
    private Boolean transcriptReceived;

    @Column
    private LocalDate transcriptReceivedDate;

    @Column
    @Builder.Default
    private Boolean requestIEP = false;

    @Column
    private Boolean iepReceived;

    @Column
    private LocalDate iepReceivedDate;

    @Column
    @Builder.Default
    private Boolean request504Plan = false;

    @Column
    private Boolean plan504Received;

    @Column
    private LocalDate plan504ReceivedDate;

    @Column
    @Builder.Default
    private Boolean requestDisciplineRecords = false;

    @Column
    private Boolean disciplineRecordsReceived;

    @Column
    private LocalDate disciplineRecordsReceivedDate;

    @Column
    @Builder.Default
    private Boolean requestAttendanceRecords = false;

    @Column
    private Boolean attendanceRecordsReceived;

    @Column
    private LocalDate attendanceRecordsReceivedDate;

    @Column
    @Builder.Default
    private Boolean requestHealthRecords = false;

    @Column
    private Boolean healthRecordsReceived;

    @Column
    private LocalDate healthRecordsReceivedDate;

    @Column(length = 1000)
    private String additionalRecordsRequested;

    // ========================================================================
    // COMMUNICATION TRACKING
    // ========================================================================

    @Column
    private LocalDateTime sentAt;

    @Column
    private LocalDateTime acknowledgedAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private Integer followUpCount; // Number of times we've followed up

    @Column
    private LocalDate lastFollowUpDate;

    @Column
    private LocalDate nextFollowUpDate;

    @Column(length = 2000)
    private String communicationLog; // Track all contact attempts

    @Column(length = 1000)
    private String receivedVia; // How records were received (mail, email, in-person, etc.)

    // ========================================================================
    // PARENT AUTHORIZATION
    // ========================================================================

    @Column
    private Boolean parentAuthorizationObtained;

    @Column
    private LocalDate authorizationDate;

    @Column(length = 500)
    private String authorizationFilePath; // Signed release form

    // ========================================================================
    // VERIFICATION AND NOTES
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_staff_id")
    private User verifiedBy;

    @Column
    private LocalDateTime verifiedAt;

    @Column
    private Boolean recordsComplete;

    @Column(length = 2000)
    private String verificationNotes;

    @Column(length = 1000)
    private String issuesEncountered;

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
     * Check if all requested records have been received
     */
    public boolean areAllRecordsReceived() {
        boolean allReceived = true;

        if (Boolean.TRUE.equals(requestTranscript)) {
            allReceived = allReceived && Boolean.TRUE.equals(transcriptReceived);
        }
        if (Boolean.TRUE.equals(requestIEP)) {
            allReceived = allReceived && Boolean.TRUE.equals(iepReceived);
        }
        if (Boolean.TRUE.equals(request504Plan)) {
            allReceived = allReceived && Boolean.TRUE.equals(plan504Received);
        }
        if (Boolean.TRUE.equals(requestDisciplineRecords)) {
            allReceived = allReceived && Boolean.TRUE.equals(disciplineRecordsReceived);
        }
        if (Boolean.TRUE.equals(requestAttendanceRecords)) {
            allReceived = allReceived && Boolean.TRUE.equals(attendanceRecordsReceived);
        }
        if (Boolean.TRUE.equals(requestHealthRecords)) {
            allReceived = allReceived && Boolean.TRUE.equals(healthRecordsReceived);
        }

        return allReceived;
    }

    /**
     * Check if request is overdue
     */
    public boolean isOverdue() {
        return dueDate != null &&
               dueDate.isBefore(LocalDate.now()) &&
               status != RequestStatus.COMPLETE &&
               status != RequestStatus.CANCELLED;
    }

    /**
     * Get count of records still pending
     */
    public int getPendingRecordsCount() {
        int pending = 0;

        if (Boolean.TRUE.equals(requestTranscript) && !Boolean.TRUE.equals(transcriptReceived)) {
            pending++;
        }
        if (Boolean.TRUE.equals(requestIEP) && !Boolean.TRUE.equals(iepReceived)) {
            pending++;
        }
        if (Boolean.TRUE.equals(request504Plan) && !Boolean.TRUE.equals(plan504Received)) {
            pending++;
        }
        if (Boolean.TRUE.equals(requestDisciplineRecords) && !Boolean.TRUE.equals(disciplineRecordsReceived)) {
            pending++;
        }
        if (Boolean.TRUE.equals(requestAttendanceRecords) && !Boolean.TRUE.equals(attendanceRecordsReceived)) {
            pending++;
        }
        if (Boolean.TRUE.equals(requestHealthRecords) && !Boolean.TRUE.equals(healthRecordsReceived)) {
            pending++;
        }

        return pending;
    }

    /**
     * Add communication note to log
     */
    public void addCommunicationNote(String note) {
        String timestamp = LocalDateTime.now().toString();
        String entry = String.format("[%s] %s\n", timestamp, note);

        if (communicationLog == null || communicationLog.isEmpty()) {
            communicationLog = entry;
        } else {
            communicationLog = entry + communicationLog; // Newest first
        }
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum RequestStatus {
        DRAFT("Draft - Not Yet Sent"),
        SENT("Sent to Previous School"),
        ACKNOWLEDGED("Acknowledged by School"),
        IN_PROGRESS("In Progress - School Preparing Records"),
        RECEIVED("Records Received (Partial)"),
        COMPLETE("Complete - All Records Received"),
        CANCELLED("Request Cancelled"),
        FAILED("Failed - Unable to Obtain Records");

        private final String displayName;

        RequestStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum RequestMethod {
        FAX("Fax"),
        EMAIL("Email"),
        MAIL("US Mail"),
        ONLINE("Online Portal"),
        IN_PERSON("In-Person Pickup");

        private final String displayName;

        RequestMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
