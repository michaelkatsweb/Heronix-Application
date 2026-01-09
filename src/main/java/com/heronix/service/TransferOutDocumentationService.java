package com.heronix.service;

import com.heronix.model.domain.TransferOutDocumentation;
import com.heronix.model.domain.TransferOutDocumentation.TransferOutStatus;
import com.heronix.model.domain.TransferOutDocumentation.TransmissionMethod;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.repository.TransferOutDocumentationRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Transfer Out Documentation Management
 * Handles business logic for outgoing transfer student records
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
@Transactional
public class TransferOutDocumentationService {

    @Autowired
    private TransferOutDocumentationRepository transferOutRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Create new transfer out documentation record
     */
    public TransferOutDocumentation createTransferOut(Long studentId, String destinationSchoolName,
                                                     LocalDate expectedTransferDate, Long createdByStaffId) {
        log.info("Creating transfer out documentation for student ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        if (transferOutRepository.hasPendingTransferOut(studentId)) {
            throw new IllegalStateException("Student already has a pending transfer out");
        }

        User createdBy = userRepository.findById(createdByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + createdByStaffId));

        String transferOutNumber = generateTransferOutNumber();

        TransferOutDocumentation transferOut = TransferOutDocumentation.builder()
                .transferOutNumber(transferOutNumber)
                .status(TransferOutStatus.DRAFT)
                .requestDate(LocalDate.now())
                .student(student)
                .destinationSchoolName(destinationSchoolName)
                .expectedTransferDate(expectedTransferDate)
                .totalDocumentsIncluded(12)
                .documentsPackaged(0)
                .allDocumentsPackaged(false)
                .requiresParentConsent(true)
                .parentConsentObtained(false)
                .ferpaReleaseObtained(false)
                .hasOutstandingFees(false)
                .destinationAcknowledged(false)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        TransferOutDocumentation saved = transferOutRepository.save(transferOut);
        log.info("Created transfer out: {}", saved.getTransferOutNumber());
        return saved;
    }

    /**
     * Generate unique transfer out number
     */
    private String generateTransferOutNumber() {
        int year = LocalDate.now().getYear();
        long count = transferOutRepository.count();
        return String.format("TRO-%d-%06d", year, count + 1);
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Get transfer out by ID
     */
    @Transactional(readOnly = true)
    public TransferOutDocumentation getTransferOutById(Long id) {
        return transferOutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transfer out record not found: " + id));
    }

    /**
     * Get by transfer out number
     */
    @Transactional(readOnly = true)
    public TransferOutDocumentation getByTransferOutNumber(String transferOutNumber) {
        return transferOutRepository.findByTransferOutNumber(transferOutNumber)
                .orElseThrow(() -> new IllegalArgumentException("Transfer out record not found: " + transferOutNumber));
    }

    /**
     * Get all transfers by status
     */
    @Transactional(readOnly = true)
    public List<TransferOutDocumentation> getByStatus(TransferOutStatus status) {
        return transferOutRepository.findByStatus(status);
    }

    /**
     * Get transfers ready to send
     */
    @Transactional(readOnly = true)
    public List<TransferOutDocumentation> getReadyToSend() {
        return transferOutRepository.findReadyToSend();
    }

    /**
     * Get transfers pending acknowledgment
     */
    @Transactional(readOnly = true)
    public List<TransferOutDocumentation> getPendingAcknowledgment() {
        return transferOutRepository.findPendingAcknowledgment();
    }

    /**
     * Get transfers by assigned staff
     */
    @Transactional(readOnly = true)
    public List<TransferOutDocumentation> getByAssignedStaff(Long staffId) {
        return transferOutRepository.findByAssignedStaff(staffId);
    }

    /**
     * Get unassigned transfer outs
     */
    @Transactional(readOnly = true)
    public List<TransferOutDocumentation> getUnassignedTransferOuts() {
        return transferOutRepository.findUnassignedTransferOuts();
    }

    /**
     * Get by destination school
     */
    @Transactional(readOnly = true)
    public List<TransferOutDocumentation> searchByDestinationSchool(String schoolName) {
        return transferOutRepository.findByDestinationSchoolNameContainingIgnoreCase(schoolName);
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update transfer out record
     */
    public TransferOutDocumentation updateTransferOut(TransferOutDocumentation transferOut, Long updatedByStaffId) {
        log.info("Updating transfer out record: {}", transferOut.getTransferOutNumber());

        User updatedBy = userRepository.findById(updatedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + updatedByStaffId));

        transferOut.setUpdatedBy(updatedBy);
        transferOut.setUpdatedAt(LocalDateTime.now());

        // Recalculate documents completion
        transferOut.calculateDocumentsCompletion();
        transferOut.calculateTotalFees();

        TransferOutDocumentation updated = transferOutRepository.save(transferOut);
        log.info("Updated transfer out record: {}", updated.getTransferOutNumber());
        return updated;
    }

    /**
     * Start records preparation
     */
    public TransferOutDocumentation startRecordsPreparation(Long transferOutId, Long updatedByStaffId) {
        log.info("Starting records preparation for transfer out ID: {}", transferOutId);

        TransferOutDocumentation transferOut = getTransferOutById(transferOutId);

        if (transferOut.getStatus() != TransferOutStatus.DRAFT &&
            transferOut.getStatus() != TransferOutStatus.PENDING_WITHDRAWAL) {
            throw new IllegalStateException("Cannot start records preparation from current status");
        }

        transferOut.setStatus(TransferOutStatus.RECORDS_PREPARATION);
        transferOut.setProcessingStartDate(LocalDate.now());

        return updateTransferOut(transferOut, updatedByStaffId);
    }

    /**
     * Mark document as included
     */
    public TransferOutDocumentation markDocumentIncluded(Long transferOutId, String documentType,
                                                        Long updatedByStaffId) {
        log.info("Marking {} included for transfer out ID: {}", documentType, transferOutId);

        TransferOutDocumentation transferOut = getTransferOutById(transferOutId);
        LocalDate today = LocalDate.now();

        switch (documentType.toLowerCase()) {
            case "transcript" -> {
                transferOut.setTranscriptIncluded(true);
                transferOut.setTranscriptGeneratedDate(today);
            }
            case "immunization" -> transferOut.setImmunizationRecordsIncluded(true);
            case "iep" -> transferOut.setIepIncluded(true);
            case "504" -> transferOut.setPlan504Included(true);
            case "discipline" -> transferOut.setDisciplineRecordsIncluded(true);
            case "attendance" -> transferOut.setAttendanceRecordsIncluded(true);
            case "testscores" -> transferOut.setTestScoresIncluded(true);
            case "health" -> transferOut.setHealthRecordsIncluded(true);
            case "specialed" -> transferOut.setSpecialEducationRecordsIncluded(true);
            case "counseling" -> transferOut.setCounselingRecordsIncluded(true);
            case "athletic" -> transferOut.setAthleticEligibilityIncluded(true);
            case "cumulative" -> transferOut.setCumulativeFolderIncluded(true);
        }

        transferOut.calculateDocumentsCompletion();

        // Auto-update status based on documents completion
        if (Boolean.TRUE.equals(transferOut.getAllDocumentsPackaged()) &&
            transferOut.getStatus() == TransferOutStatus.RECORDS_PREPARATION) {

            // Check if consent is needed
            if (Boolean.TRUE.equals(transferOut.getRequiresParentConsent()) &&
                !Boolean.TRUE.equals(transferOut.getParentConsentObtained())) {
                transferOut.setStatus(TransferOutStatus.PENDING_CONSENT);
            }
            // Check if fees need to be paid
            else if (Boolean.TRUE.equals(transferOut.getHasOutstandingFees()) &&
                     !Boolean.TRUE.equals(transferOut.getFeesPaidBeforeRelease())) {
                transferOut.setStatus(TransferOutStatus.PENDING_FEES);
            }
            // Otherwise, ready to send
            else {
                transferOut.setStatus(TransferOutStatus.READY_TO_SEND);
                transferOut.setProcessingCompletedDate(today);
            }
        }

        return updateTransferOut(transferOut, updatedByStaffId);
    }

    /**
     * Mark all documents as included
     */
    public TransferOutDocumentation includeAllDocuments(Long transferOutId, Long updatedByStaffId) {
        log.info("Including all documents for transfer out ID: {}", transferOutId);

        TransferOutDocumentation transferOut = getTransferOutById(transferOutId);
        LocalDate today = LocalDate.now();

        // Set all documents to included
        transferOut.setTranscriptIncluded(true);
        transferOut.setTranscriptGeneratedDate(today);
        transferOut.setImmunizationRecordsIncluded(true);
        transferOut.setIepIncluded(true);
        transferOut.setPlan504Included(true);
        transferOut.setDisciplineRecordsIncluded(true);
        transferOut.setAttendanceRecordsIncluded(true);
        transferOut.setTestScoresIncluded(true);
        transferOut.setHealthRecordsIncluded(true);
        transferOut.setSpecialEducationRecordsIncluded(true);
        transferOut.setCounselingRecordsIncluded(true);
        transferOut.setAthleticEligibilityIncluded(true);
        transferOut.setCumulativeFolderIncluded(true);

        return updateTransferOut(transferOut, updatedByStaffId);
    }

    /**
     * Mark transfer out as ready to send
     */
    public TransferOutDocumentation markReadyToSend(Long transferOutId, Long updatedByStaffId) {
        log.info("Marking transfer out ID: {} as ready to send", transferOutId);

        TransferOutDocumentation transferOut = getTransferOutById(transferOutId);

        if (!transferOut.isReadyToSend()) {
            throw new IllegalStateException("Transfer out is not ready to send. Please ensure all required documents are included and requirements are met.");
        }

        transferOut.setStatus(TransferOutStatus.READY_TO_SEND);

        return updateTransferOut(transferOut, updatedByStaffId);
    }

    /**
     * Assign staff member to transfer out
     */
    public TransferOutDocumentation assignStaff(Long transferOutId, Long staffId, Long updatedByStaffId) {
        log.info("Assigning staff ID: {} to transfer out ID: {}", staffId, transferOutId);

        TransferOutDocumentation transferOut = getTransferOutById(transferOutId);
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        transferOut.setAssignedStaff(staff);
        return updateTransferOut(transferOut, updatedByStaffId);
    }

    /**
     * Record parent consent
     */
    public TransferOutDocumentation recordParentConsent(Long transferOutId, Long updatedByStaffId) {
        log.info("Recording parent consent for transfer out ID: {}", transferOutId);

        TransferOutDocumentation transferOut = getTransferOutById(transferOutId);
        transferOut.setParentConsentObtained(true);
        transferOut.setParentConsentDate(LocalDate.now());
        transferOut.setFerpaReleaseObtained(true);
        transferOut.setFerpaReleaseDate(LocalDate.now());

        // Move to next status if ready
        if (transferOut.getStatus() == TransferOutStatus.PENDING_CONSENT) {
            if (Boolean.TRUE.equals(transferOut.getHasOutstandingFees()) &&
                !Boolean.TRUE.equals(transferOut.getFeesPaidBeforeRelease())) {
                transferOut.setStatus(TransferOutStatus.PENDING_FEES);
            } else if (transferOut.isReadyToSend()) {
                transferOut.setStatus(TransferOutStatus.READY_TO_SEND);
            }
        }

        return updateTransferOut(transferOut, updatedByStaffId);
    }

    /**
     * Record fee payment
     */
    public TransferOutDocumentation recordFeePayment(Long transferOutId, Long updatedByStaffId) {
        log.info("Recording fee payment for transfer out ID: {}", transferOutId);

        TransferOutDocumentation transferOut = getTransferOutById(transferOutId);
        transferOut.setFeesPaid(true);
        transferOut.setFeesPaidDate(LocalDate.now());
        transferOut.setFeesPaidBeforeRelease(true);

        // Move to next status if ready
        if (transferOut.getStatus() == TransferOutStatus.PENDING_FEES && transferOut.isReadyToSend()) {
            transferOut.setStatus(TransferOutStatus.READY_TO_SEND);
        }

        return updateTransferOut(transferOut, updatedByStaffId);
    }

    /**
     * Send records to destination school
     */
    public TransferOutDocumentation sendRecords(Long transferOutId, TransmissionMethod method,
                                               String trackingNumber, Long sentByStaffId) {
        log.info("Sending records for transfer out ID: {} via {}", transferOutId, method);

        TransferOutDocumentation transferOut = getTransferOutById(transferOutId);

        if (!transferOut.isReadyToSend()) {
            throw new IllegalStateException("Transfer out not ready to send. Required: all documents, " +
                    "parent consent (if required), fees paid (if applicable)");
        }

        User sentBy = userRepository.findById(sentByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + sentByStaffId));

        transferOut.setStatus(TransferOutStatus.SENT);
        transferOut.setTransmissionMethod(method);
        transferOut.setSentDate(LocalDate.now());
        transferOut.setTrackingNumber(trackingNumber);
        transferOut.setSentBy(sentBy);

        // Set follow-up date for acknowledgment (7 days for electronic, 14 for physical mail)
        if (method == TransmissionMethod.EMAIL ||
            method == TransmissionMethod.ELECTRONIC_TRANSCRIPT ||
            method == TransmissionMethod.PARCHMENT ||
            method == TransmissionMethod.NAVIANCE) {
            transferOut.setFollowUpRequired(true);
            transferOut.setFollowUpDate(LocalDate.now().plusDays(7));
        } else {
            transferOut.setFollowUpRequired(true);
            transferOut.setFollowUpDate(LocalDate.now().plusDays(14));
        }

        return updateTransferOut(transferOut, sentByStaffId);
    }

    /**
     * Record acknowledgment from destination school
     */
    public TransferOutDocumentation recordAcknowledgment(Long transferOutId, String acknowledgedBy,
                                                        String acknowledgmentMethod, Long updatedByStaffId) {
        log.info("Recording acknowledgment for transfer out ID: {}", transferOutId);

        TransferOutDocumentation transferOut = getTransferOutById(transferOutId);

        if (transferOut.getStatus() != TransferOutStatus.SENT) {
            throw new IllegalStateException("Can only acknowledge records that have been sent");
        }

        transferOut.setDestinationAcknowledged(true);
        transferOut.setAcknowledgmentDate(LocalDate.now());
        transferOut.setAcknowledgedBy(acknowledgedBy);
        transferOut.setAcknowledgmentMethod(acknowledgmentMethod);
        transferOut.setStatus(TransferOutStatus.ACKNOWLEDGED);
        transferOut.setFollowUpRequired(false);

        return updateTransferOut(transferOut, updatedByStaffId);
    }

    /**
     * Complete transfer out process
     */
    public TransferOutDocumentation completeTransferOut(Long transferOutId, Long completedByStaffId) {
        log.info("Completing transfer out ID: {}", transferOutId);

        TransferOutDocumentation transferOut = getTransferOutById(transferOutId);

        if (transferOut.getStatus() != TransferOutStatus.ACKNOWLEDGED) {
            throw new IllegalStateException("Transfer out must be acknowledged before completion");
        }

        transferOut.setStatus(TransferOutStatus.COMPLETED);
        transferOut.setProcessingCompletedDate(LocalDate.now());

        return updateTransferOut(transferOut, completedByStaffId);
    }

    /**
     * Cancel transfer out
     */
    public TransferOutDocumentation cancelTransferOut(Long transferOutId, String reason, Long cancelledByStaffId) {
        log.info("Cancelling transfer out ID: {}", transferOutId);

        TransferOutDocumentation transferOut = getTransferOutById(transferOutId);

        if (transferOut.getStatus() == TransferOutStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed transfer out");
        }

        transferOut.setStatus(TransferOutStatus.CANCELLED);
        transferOut.setAdministrativeNotes(
                (transferOut.getAdministrativeNotes() != null ? transferOut.getAdministrativeNotes() + "\n\n" : "") +
                "CANCELLED: " + reason
        );

        return updateTransferOut(transferOut, cancelledByStaffId);
    }

    /**
     * Add follow-up note
     */
    public TransferOutDocumentation addFollowUpNote(Long transferOutId, String note, Long updatedByStaffId) {
        log.info("Adding follow-up note for transfer out ID: {}", transferOutId);

        TransferOutDocumentation transferOut = getTransferOutById(transferOutId);

        Integer attempts = transferOut.getFollowUpAttempts();
        transferOut.setFollowUpAttempts(attempts != null ? attempts + 1 : 1);
        transferOut.setLastFollowUpDate(LocalDate.now());

        String existingNotes = transferOut.getFollowUpNotes();
        String newNote = LocalDate.now() + ": " + note;
        transferOut.setFollowUpNotes(
                existingNotes != null ? existingNotes + "\n" + newNote : newNote
        );

        return updateTransferOut(transferOut, updatedByStaffId);
    }

    // ========================================================================
    // STATISTICS & REPORTING
    // ========================================================================

    /**
     * Get transfer out statistics
     */
    @Transactional(readOnly = true)
    public TransferOutStatistics getStatistics() {
        long total = transferOutRepository.count();
        long draft = transferOutRepository.countByStatus(TransferOutStatus.DRAFT);
        long recordsPrep = transferOutRepository.countByStatus(TransferOutStatus.RECORDS_PREPARATION);
        long readyToSend = transferOutRepository.countByStatus(TransferOutStatus.READY_TO_SEND);
        long sent = transferOutRepository.countByStatus(TransferOutStatus.SENT);
        long acknowledged = transferOutRepository.countByStatus(TransferOutStatus.ACKNOWLEDGED);
        long completed = transferOutRepository.countByStatus(TransferOutStatus.COMPLETED);
        long cancelled = transferOutRepository.countByStatus(TransferOutStatus.CANCELLED);
        long midYear = transferOutRepository.countByIsMidYearTransferTrue();
        long international = transferOutRepository.countByInternationalTransferTrue();
        Double avgDocuments = transferOutRepository.getAverageDocumentsCompletion();
        Double avgProcessingDays = transferOutRepository.getAverageProcessingDays();

        return new TransferOutStatistics(
                total, draft, recordsPrep, readyToSend, sent, acknowledged, completed, cancelled,
                midYear, international,
                avgDocuments != null ? avgDocuments : 0.0,
                avgProcessingDays != null ? avgProcessingDays : 0.0
        );
    }

    /**
     * Get transfer out counts by destination school
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCountByDestinationSchool() {
        return transferOutRepository.getCountByDestinationSchool();
    }

    /**
     * Get transfers needing attention
     */
    @Transactional(readOnly = true)
    public List<TransferOutDocumentation> getTransfersNeedingAttention() {
        LocalDate processingCutoff = LocalDate.now().minusDays(7); // 1 week old
        LocalDate acknowledgmentCutoff = LocalDate.now().minusDays(21); // 3 weeks old
        LocalDate today = LocalDate.now();
        return transferOutRepository.findNeedingAttention(processingCutoff, acknowledgmentCutoff, today);
    }

    /**
     * Statistics record
     */
    public record TransferOutStatistics(
            long total,
            long draft,
            long recordsPreparation,
            long readyToSend,
            long sent,
            long acknowledged,
            long completed,
            long cancelled,
            long midYearTransfers,
            long internationalTransfers,
            double averageDocumentsCompletion,
            double averageProcessingDays
    ) {}

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Delete transfer out record (only if in DRAFT status)
     */
    public void deleteTransferOut(Long transferOutId) {
        log.info("Deleting transfer out record ID: {}", transferOutId);

        TransferOutDocumentation transferOut = getTransferOutById(transferOutId);

        if (transferOut.getStatus() != TransferOutStatus.DRAFT) {
            throw new IllegalStateException("Only draft transfer out records can be deleted");
        }

        transferOutRepository.delete(transferOut);
        log.info("Deleted transfer out record: {}", transferOut.getTransferOutNumber());
    }
}
