package com.heronix.service;

import com.heronix.model.domain.TransferStudent;
import com.heronix.model.domain.TransferStudent.TransferStatus;
import com.heronix.model.domain.TransferStudent.TransferType;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.repository.TransferStudentRepository;
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
 * Service for Transfer Student Management
 * Handles business logic for incoming transfer student processing
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
@Transactional
public class TransferStudentService {

    @Autowired
    private TransferStudentRepository transferStudentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Create new transfer student record
     */
    public TransferStudent createTransferRecord(String studentFirstName, String studentLastName,
                                               LocalDate dateOfBirth, LocalDate transferDate,
                                               String previousSchoolName, Long createdByStaffId) {
        log.info("Creating transfer record for: {} {}", studentFirstName, studentLastName);

        User createdBy = userRepository.findById(createdByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + createdByStaffId));

        String transferNumber = generateTransferNumber();

        TransferStudent transfer = TransferStudent.builder()
                .transferNumber(transferNumber)
                .status(TransferStatus.DRAFT)
                .transferDate(transferDate)
                .studentFirstName(studentFirstName)
                .studentLastName(studentLastName)
                .studentDateOfBirth(dateOfBirth)
                .previousSchoolName(previousSchoolName)
                .totalRecordsExpected(7)
                .recordsReceived(0)
                .allRecordsReceived(false)
                .residencyVerified(false)
                .birthCertificateVerified(false)
                .proofOfResidencyVerified(false)
                .parentIdVerified(false)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        TransferStudent saved = transferStudentRepository.save(transfer);
        log.info("Created transfer record: {}", saved.getTransferNumber());
        return saved;
    }

    /**
     * Generate unique transfer number
     */
    private String generateTransferNumber() {
        int year = LocalDate.now().getYear();
        long count = transferStudentRepository.count();
        return String.format("TRN-%d-%06d", year, count + 1);
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Get transfer record by ID
     */
    @Transactional(readOnly = true)
    public TransferStudent getTransferById(Long id) {
        return transferStudentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transfer record not found: " + id));
    }

    /**
     * Get by transfer number
     */
    @Transactional(readOnly = true)
    public TransferStudent getByTransferNumber(String transferNumber) {
        return transferStudentRepository.findByTransferNumber(transferNumber)
                .orElseThrow(() -> new IllegalArgumentException("Transfer record not found: " + transferNumber));
    }

    /**
     * Get all transfers by status
     */
    @Transactional(readOnly = true)
    public List<TransferStudent> getByStatus(TransferStatus status) {
        return transferStudentRepository.findByStatus(status);
    }

    /**
     * Get pending records transfers
     */
    @Transactional(readOnly = true)
    public List<TransferStudent> getPendingRecords() {
        return transferStudentRepository.findPendingRecords();
    }

    /**
     * Get transfers ready for placement
     */
    @Transactional(readOnly = true)
    public List<TransferStudent> getReadyForPlacement() {
        return transferStudentRepository.findReadyForPlacement();
    }

    /**
     * Get transfers by counselor
     */
    @Transactional(readOnly = true)
    public List<TransferStudent> getByCounselor(Long counselorId) {
        return transferStudentRepository.findByAssignedCounselor(counselorId);
    }

    /**
     * Get unassigned transfers
     */
    @Transactional(readOnly = true)
    public List<TransferStudent> getUnassignedTransfers() {
        return transferStudentRepository.findUnassignedTransfers();
    }

    /**
     * Search by student name
     */
    @Transactional(readOnly = true)
    public List<TransferStudent> searchByStudentName(String searchTerm) {
        return transferStudentRepository.searchByStudentName(searchTerm);
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update transfer record
     */
    public TransferStudent updateTransfer(TransferStudent transfer, Long updatedByStaffId) {
        log.info("Updating transfer record: {}", transfer.getTransferNumber());

        User updatedBy = userRepository.findById(updatedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + updatedByStaffId));

        transfer.setUpdatedBy(updatedBy);
        transfer.setUpdatedAt(LocalDateTime.now());

        // Recalculate records completion
        transfer.calculateRecordsCompletion();

        TransferStudent updated = transferStudentRepository.save(transfer);
        log.info("Updated transfer record: {}", updated.getTransferNumber());
        return updated;
    }

    /**
     * Request records from previous school
     */
    public TransferStudent requestRecords(Long transferId, Long updatedByStaffId) {
        log.info("Requesting records for transfer ID: {}", transferId);

        TransferStudent transfer = getTransferById(transferId);

        if (transfer.getStatus() != TransferStatus.DRAFT &&
            transfer.getStatus() != TransferStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Cannot request records for transfer in current status");
        }

        transfer.setStatus(TransferStatus.RECORDS_REQUESTED);
        return updateTransfer(transfer, updatedByStaffId);
    }

    /**
     * Mark record as received
     */
    public TransferStudent markRecordReceived(Long transferId, String recordType, Long updatedByStaffId) {
        log.info("Marking {} received for transfer ID: {}", recordType, transferId);

        TransferStudent transfer = getTransferById(transferId);
        LocalDate today = LocalDate.now();

        switch (recordType.toLowerCase()) {
            case "transcript" -> {
                transfer.setTranscriptReceived(true);
                transfer.setTranscriptReceivedDate(today);
            }
            case "immunization" -> {
                transfer.setImmunizationRecordsReceived(true);
                transfer.setImmunizationReceivedDate(today);
            }
            case "iep" -> {
                transfer.setIepReceived(true);
                transfer.setIepReceivedDate(today);
            }
            case "504" -> {
                transfer.setPlan504Received(true);
                transfer.setPlan504ReceivedDate(today);
            }
            case "discipline" -> {
                transfer.setDisciplineRecordsReceived(true);
                transfer.setDisciplineReceivedDate(today);
            }
            case "attendance" -> {
                transfer.setAttendanceRecordsReceived(true);
                transfer.setAttendanceReceivedDate(today);
            }
            case "testscores" -> {
                transfer.setTestScoresReceived(true);
                transfer.setTestScoresReceivedDate(today);
            }
        }

        transfer.calculateRecordsCompletion();

        // Auto-update status based on records
        if (transfer.getRecordsReceived() > 0 && !Boolean.TRUE.equals(transfer.getAllRecordsReceived())) {
            transfer.setStatus(TransferStatus.PARTIAL_RECORDS);
        } else if (Boolean.TRUE.equals(transfer.getAllRecordsReceived())) {
            transfer.setStatus(TransferStatus.COMPLETE_RECORDS);
        }

        return updateTransfer(transfer, updatedByStaffId);
    }

    /**
     * Assign counselor to transfer
     */
    public TransferStudent assignCounselor(Long transferId, Long counselorId, Long updatedByStaffId) {
        log.info("Assigning counselor ID: {} to transfer ID: {}", counselorId, transferId);

        TransferStudent transfer = getTransferById(transferId);
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("Counselor not found: " + counselorId));

        transfer.setAssignedCounselor(counselor);
        return updateTransfer(transfer, updatedByStaffId);
    }

    /**
     * Mark ready for placement
     */
    public TransferStudent markReadyForPlacement(Long transferId, String proposedGrade, Long updatedByStaffId) {
        log.info("Marking transfer ID: {} ready for placement in grade: {}", transferId, proposedGrade);

        TransferStudent transfer = getTransferById(transferId);

        if (!transfer.isReadyForEnrollment()) {
            throw new IllegalStateException("Transfer does not meet requirements for placement. " +
                    "Required: all records, birth certificate, residency verification");
        }

        transfer.setStatus(TransferStatus.READY_FOR_PLACEMENT);
        transfer.setProposedGradeLevel(proposedGrade);

        return updateTransfer(transfer, updatedByStaffId);
    }

    /**
     * Complete enrollment
     */
    public TransferStudent completeEnrollment(Long transferId, Long studentId, String assignedGrade,
                                             String homeroom, Long completedByStaffId) {
        log.info("Completing enrollment for transfer ID: {}", transferId);

        TransferStudent transfer = getTransferById(transferId);

        if (transfer.getStatus() != TransferStatus.READY_FOR_PLACEMENT) {
            throw new IllegalStateException("Transfer must be in READY_FOR_PLACEMENT status");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        User processedBy = userRepository.findById(completedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + completedByStaffId));

        transfer.setStudent(student);
        transfer.setStatus(TransferStatus.ENROLLED);
        transfer.setEnrollmentDate(LocalDate.now());
        transfer.setAssignedGradeLevel(assignedGrade);
        transfer.setAssignedHomeroom(homeroom);
        transfer.setProcessedBy(processedBy);
        transfer.setProcessingDate(LocalDate.now());

        return updateTransfer(transfer, completedByStaffId);
    }

    /**
     * Cancel transfer
     */
    public TransferStudent cancelTransfer(Long transferId, String reason, Long cancelledByStaffId) {
        log.info("Cancelling transfer ID: {}", transferId);

        TransferStudent transfer = getTransferById(transferId);

        if (transfer.getStatus() == TransferStatus.ENROLLED) {
            throw new IllegalStateException("Cannot cancel enrolled transfer");
        }

        transfer.setStatus(TransferStatus.CANCELLED);
        transfer.setAdministrativeNotes(
                (transfer.getAdministrativeNotes() != null ? transfer.getAdministrativeNotes() + "\n\n" : "") +
                "CANCELLED: " + reason
        );

        return updateTransfer(transfer, cancelledByStaffId);
    }

    // ========================================================================
    // VERIFICATION OPERATIONS
    // ========================================================================

    /**
     * Verify residency
     */
    public TransferStudent verifyResidency(Long transferId, Long verifiedByStaffId) {
        log.info("Verifying residency for transfer ID: {}", transferId);

        TransferStudent transfer = getTransferById(transferId);
        transfer.setResidencyVerified(true);
        transfer.setResidencyVerifiedDate(LocalDate.now());

        return updateTransfer(transfer, verifiedByStaffId);
    }

    /**
     * Verify documents
     */
    public TransferStudent verifyDocuments(Long transferId, boolean birthCert, boolean residency,
                                          boolean parentId, Long verifiedByStaffId) {
        log.info("Verifying documents for transfer ID: {}", transferId);

        TransferStudent transfer = getTransferById(transferId);
        transfer.setBirthCertificateVerified(birthCert);
        transfer.setProofOfResidencyVerified(residency);
        transfer.setParentIdVerified(parentId);

        return updateTransfer(transfer, verifiedByStaffId);
    }

    // ========================================================================
    // STATISTICS & REPORTING
    // ========================================================================

    /**
     * Get transfer statistics
     */
    @Transactional(readOnly = true)
    public TransferStatistics getStatistics() {
        long total = transferStudentRepository.count();
        long draft = transferStudentRepository.countByStatus(TransferStatus.DRAFT);
        long recordsRequested = transferStudentRepository.countByStatus(TransferStatus.RECORDS_REQUESTED);
        long partialRecords = transferStudentRepository.countByStatus(TransferStatus.PARTIAL_RECORDS);
        long completeRecords = transferStudentRepository.countByStatus(TransferStatus.COMPLETE_RECORDS);
        long readyForPlacement = transferStudentRepository.countByStatus(TransferStatus.READY_FOR_PLACEMENT);
        long enrolled = transferStudentRepository.countByStatus(TransferStatus.ENROLLED);
        long cancelled = transferStudentRepository.countByStatus(TransferStatus.CANCELLED);
        long midYear = transferStudentRepository.countByIsMidYearTransferTrue();
        Double avgRecords = transferStudentRepository.getAverageRecordsCompletion();

        return new TransferStatistics(
                total, draft, recordsRequested, partialRecords, completeRecords,
                readyForPlacement, enrolled, cancelled, midYear,
                avgRecords != null ? avgRecords : 0.0
        );
    }

    /**
     * Get transfer counts by type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCountByType() {
        return transferStudentRepository.getCountByTransferType();
    }

    /**
     * Get transfer counts by grade level
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCountByGradeLevel() {
        return transferStudentRepository.getCountByGradeLevel();
    }

    /**
     * Get transfers needing attention
     */
    @Transactional(readOnly = true)
    public List<TransferStudent> getTransfersNeedingAttention() {
        LocalDate cutoffDate = LocalDate.now().minusDays(14); // 2 weeks old
        return transferStudentRepository.findNeedingAttention(cutoffDate);
    }

    /**
     * Statistics record
     */
    public record TransferStatistics(
            long total,
            long draft,
            long recordsRequested,
            long partialRecords,
            long completeRecords,
            long readyForPlacement,
            long enrolled,
            long cancelled,
            long midYearTransfers,
            double averageRecordsCompletion
    ) {}

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Delete transfer record (only if in DRAFT status)
     */
    public void deleteTransfer(Long transferId) {
        log.info("Deleting transfer record ID: {}", transferId);

        TransferStudent transfer = getTransferById(transferId);

        if (transfer.getStatus() != TransferStatus.DRAFT) {
            throw new IllegalStateException("Only draft transfer records can be deleted");
        }

        transferStudentRepository.delete(transfer);
        log.info("Deleted transfer record: {}", transfer.getTransferNumber());
    }
}
