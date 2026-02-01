package com.heronix.service;

import com.heronix.model.domain.WithdrawalRecord;
import com.heronix.model.domain.WithdrawalRecord.WithdrawalStatus;
import com.heronix.model.domain.WithdrawalRecord.FinalStatus;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.repository.WithdrawalRepository;
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
 * Service for Student Withdrawal Management
 * Handles business logic for student withdrawal processing and clearance
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
@Transactional
public class WithdrawalService {

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Create new withdrawal record for a student
     */
    public WithdrawalRecord createWithdrawal(Long studentId, LocalDate withdrawalDate, Long createdByStaffId) {
        log.info("Creating withdrawal record for student ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        User createdBy = userRepository.findById(createdByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + createdByStaffId));

        // Check if student already has pending withdrawal
        if (withdrawalRepository.hasPendingWithdrawal(studentId)) {
            throw new IllegalStateException("Student already has a pending withdrawal");
        }

        String withdrawalNumber = generateWithdrawalNumber();

        WithdrawalRecord withdrawal = WithdrawalRecord.builder()
                .withdrawalNumber(withdrawalNumber)
                .status(WithdrawalStatus.DRAFT)
                .student(student)
                .currentGrade(student.getGradeLevel())
                .currentStatus(student.getStudentStatus() != null ? student.getStudentStatus().getDisplayName() : "Active")
                .withdrawalDate(withdrawalDate)
                .isTransferring(false)
                .totalClearanceItems(24)
                .clearedItems(0)
                .allCleared(false)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        WithdrawalRecord saved = withdrawalRepository.save(withdrawal);
        log.info("Created withdrawal record: {}", saved.getWithdrawalNumber());
        return saved;
    }

    /**
     * Generate unique withdrawal number
     */
    private String generateWithdrawalNumber() {
        int year = LocalDate.now().getYear();
        long count = withdrawalRepository.count();
        return String.format("WD-%d-%06d", year, count + 1);
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Get withdrawal record by ID
     */
    @Transactional(readOnly = true)
    public WithdrawalRecord getWithdrawalById(Long id) {
        return withdrawalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Withdrawal record not found: " + id));
    }

    /**
     * Get withdrawal record by withdrawal number
     */
    @Transactional(readOnly = true)
    public WithdrawalRecord getByWithdrawalNumber(String withdrawalNumber) {
        return withdrawalRepository.findByWithdrawalNumber(withdrawalNumber)
                .orElseThrow(() -> new IllegalArgumentException("Withdrawal record not found: " + withdrawalNumber));
    }

    /**
     * Get all withdrawal records for a student
     */
    @Transactional(readOnly = true)
    public List<WithdrawalRecord> getByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        return withdrawalRepository.findByStudent(student);
    }

    /**
     * Get most recent withdrawal for student
     */
    @Transactional(readOnly = true)
    public WithdrawalRecord getMostRecentWithdrawal(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        return withdrawalRepository.findFirstByStudentOrderByCreatedAtDesc(student)
                .orElse(null);
    }

    /**
     * Get all withdrawals by status
     */
    @Transactional(readOnly = true)
    public List<WithdrawalRecord> getByStatus(WithdrawalStatus status) {
        return withdrawalRepository.findByStatus(status);
    }

    /**
     * Get pending withdrawals
     */
    @Transactional(readOnly = true)
    public List<WithdrawalRecord> getPendingWithdrawals() {
        return withdrawalRepository.findPendingWithdrawals();
    }

    /**
     * Get withdrawals in clearance process
     */
    @Transactional(readOnly = true)
    public List<WithdrawalRecord> getInClearanceProcess() {
        return withdrawalRepository.findInClearanceProcess();
    }

    /**
     * Search by student name
     */
    @Transactional(readOnly = true)
    public List<WithdrawalRecord> searchByStudentName(String searchTerm) {
        return withdrawalRepository.searchByStudentName(searchTerm);
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update withdrawal record
     */
    public WithdrawalRecord updateWithdrawal(WithdrawalRecord withdrawal, Long updatedByStaffId) {
        log.info("Updating withdrawal record: {}", withdrawal.getWithdrawalNumber());

        User updatedBy = userRepository.findById(updatedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + updatedByStaffId));

        withdrawal.setUpdatedBy(updatedBy);
        withdrawal.setUpdatedAt(LocalDateTime.now());

        // Recalculate clearance completion
        withdrawal.calculateClearanceCompletion();

        WithdrawalRecord updated = withdrawalRepository.save(withdrawal);
        log.info("Updated withdrawal record: {}", updated.getWithdrawalNumber());
        return updated;
    }

    /**
     * Start clearance process
     */
    public WithdrawalRecord startClearance(Long withdrawalId, Long updatedByStaffId) {
        log.info("Starting clearance for withdrawal ID: {}", withdrawalId);

        WithdrawalRecord withdrawal = getWithdrawalById(withdrawalId);

        if (withdrawal.getStatus() != WithdrawalStatus.DRAFT) {
            throw new IllegalStateException("Only draft withdrawals can start clearance");
        }

        withdrawal.setStatus(WithdrawalStatus.PENDING_CLEARANCE);
        return updateWithdrawal(withdrawal, updatedByStaffId);
    }

    /**
     * Begin clearance in progress
     */
    public WithdrawalRecord beginClearanceProcess(Long withdrawalId, Long updatedByStaffId) {
        log.info("Beginning clearance process for withdrawal ID: {}", withdrawalId);

        WithdrawalRecord withdrawal = getWithdrawalById(withdrawalId);

        if (withdrawal.getStatus() != WithdrawalStatus.PENDING_CLEARANCE) {
            throw new IllegalStateException("Withdrawal must be in pending clearance status");
        }

        withdrawal.setStatus(WithdrawalStatus.CLEARANCE_IN_PROGRESS);
        return updateWithdrawal(withdrawal, updatedByStaffId);
    }

    /**
     * Mark withdrawal as cleared
     */
    public WithdrawalRecord markAsCleared(Long withdrawalId, Long updatedByStaffId) {
        log.info("Marking withdrawal ID: {} as cleared", withdrawalId);

        WithdrawalRecord withdrawal = getWithdrawalById(withdrawalId);
        withdrawal.calculateClearanceCompletion();

        if (!Boolean.TRUE.equals(withdrawal.getAllCleared())) {
            throw new IllegalStateException("Not all clearance items are completed");
        }

        withdrawal.setStatus(WithdrawalStatus.CLEARED);
        return updateWithdrawal(withdrawal, updatedByStaffId);
    }

    /**
     * Complete withdrawal processing
     */
    public WithdrawalRecord completeWithdrawal(Long withdrawalId, FinalStatus finalStatus, Long completedByStaffId) {
        log.info("Completing withdrawal ID: {} with final status: {}", withdrawalId, finalStatus);

        WithdrawalRecord withdrawal = getWithdrawalById(withdrawalId);

        if (!withdrawal.canBeCompleted()) {
            throw new IllegalStateException("Withdrawal cannot be completed. All clearance items and acknowledgments must be complete.");
        }

        User completedBy = userRepository.findById(completedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + completedByStaffId));

        withdrawal.setStatus(WithdrawalStatus.COMPLETED);
        withdrawal.setFinalStatus(finalStatus);
        withdrawal.setProcessedBy(completedBy);
        withdrawal.setProcessingDate(LocalDate.now());
        withdrawal.setEffectiveDate(withdrawal.getWithdrawalDate());

        return updateWithdrawal(withdrawal, completedByStaffId);
    }

    /**
     * Cancel withdrawal
     */
    public WithdrawalRecord cancelWithdrawal(Long withdrawalId, String reason, Long cancelledByStaffId) {
        log.info("Cancelling withdrawal ID: {}", withdrawalId);

        WithdrawalRecord withdrawal = getWithdrawalById(withdrawalId);

        if (withdrawal.getStatus() == WithdrawalStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed withdrawal");
        }

        withdrawal.setStatus(WithdrawalStatus.CANCELLED);
        withdrawal.setAdministrativeNotes(
                (withdrawal.getAdministrativeNotes() != null ? withdrawal.getAdministrativeNotes() + "\n\n" : "") +
                "CANCELLED: " + reason
        );

        return updateWithdrawal(withdrawal, cancelledByStaffId);
    }

    // ========================================================================
    // CLEARANCE OPERATIONS
    // ========================================================================

    /**
     * Check all clearance items (for bulk clearance)
     */
    public WithdrawalRecord checkAllClearanceItems(Long withdrawalId, Long updatedByStaffId) {
        log.info("Checking all clearance items for withdrawal ID: {}", withdrawalId);

        WithdrawalRecord withdrawal = getWithdrawalById(withdrawalId);

        // Academic
        withdrawal.setFinalGradesRecorded(true);
        withdrawal.setTranscriptPrinted(true);
        withdrawal.setIep504Finalized(true);
        withdrawal.setProgressReportsSent(true);

        // Library & Materials
        withdrawal.setLibraryBooksReturned(true);
        withdrawal.setTextbooksReturned(true);
        withdrawal.setLibraryFinesPaid(true);
        withdrawal.setDevicesReturned(true);
        withdrawal.setAthleticEquipmentReturned(true);
        withdrawal.setInstrumentsReturned(true);

        // Facilities
        withdrawal.setLockerCleared(true);
        withdrawal.setLockerLockReturned(true);
        withdrawal.setParkingPermitReturned(true);
        withdrawal.setIdCardReturned(true);

        // Financial
        withdrawal.setTuitionPaid(true);
        withdrawal.setCafeteriaBalanceSettled(true);
        withdrawal.setActivityFeesPaid(true);
        withdrawal.setDamageFeesPaid(true);

        // Administrative
        withdrawal.setRecordsReleaseSigned(true);
        withdrawal.setImmunizationsCopied(true);
        withdrawal.setPaperworkCompleted(true);
        withdrawal.setParentNotificationSent(true);
        withdrawal.setWithdrawalFormSigned(true);
        withdrawal.setFinalTranscriptRequested(true);

        return updateWithdrawal(withdrawal, updatedByStaffId);
    }

    /**
     * Update specific clearance item
     */
    public WithdrawalRecord updateClearanceItem(Long withdrawalId, String itemName, boolean checked, Long updatedByStaffId) {
        log.info("Updating clearance item '{}' to {} for withdrawal ID: {}", itemName, checked, withdrawalId);

        WithdrawalRecord withdrawal = getWithdrawalById(withdrawalId);

        // Use reflection or switch statement to update specific field
        // For simplicity, implementing key items
        switch (itemName) {
            case "finalGradesRecorded" -> withdrawal.setFinalGradesRecorded(checked);
            case "transcriptPrinted" -> withdrawal.setTranscriptPrinted(checked);
            case "libraryBooksReturned" -> withdrawal.setLibraryBooksReturned(checked);
            case "textbooksReturned" -> withdrawal.setTextbooksReturned(checked);
            case "tuitionPaid" -> withdrawal.setTuitionPaid(checked);
            // Add more cases as needed
        }

        return updateWithdrawal(withdrawal, updatedByStaffId);
    }

    // ========================================================================
    // STATISTICS & REPORTING
    // ========================================================================

    /**
     * Get withdrawal statistics
     */
    @Transactional(readOnly = true)
    public WithdrawalStatistics getStatistics() {
        long total = withdrawalRepository.count();
        long draft = withdrawalRepository.countByStatus(WithdrawalStatus.DRAFT);
        long pendingClearance = withdrawalRepository.countByStatus(WithdrawalStatus.PENDING_CLEARANCE);
        long clearanceInProgress = withdrawalRepository.countByStatus(WithdrawalStatus.CLEARANCE_IN_PROGRESS);
        long cleared = withdrawalRepository.countByStatus(WithdrawalStatus.CLEARED);
        long completed = withdrawalRepository.countByStatus(WithdrawalStatus.COMPLETED);
        long cancelled = withdrawalRepository.countByStatus(WithdrawalStatus.CANCELLED);
        long transfers = withdrawalRepository.countByIsTransferringTrue();
        Double avgClearance = withdrawalRepository.getAverageClearanceCompletion();

        return new WithdrawalStatistics(
                total,
                draft,
                pendingClearance,
                clearanceInProgress,
                cleared,
                completed,
                cancelled,
                transfers,
                avgClearance != null ? avgClearance : 0.0
        );
    }

    /**
     * Get withdrawal counts by type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCountByWithdrawalType() {
        return withdrawalRepository.getCountByWithdrawalType();
    }

    /**
     * Get withdrawal counts by month
     */
    @Transactional(readOnly = true)
    public List<Object[]> getWithdrawalCountsByMonth(LocalDate startDate, LocalDate endDate) {
        return withdrawalRepository.getWithdrawalCountsByMonth(startDate, endDate);
    }

    /**
     * Get withdrawal records needing attention
     */
    @Transactional(readOnly = true)
    public List<WithdrawalRecord> getWithdrawalsNeedingAttention() {
        LocalDate cutoffDate = LocalDate.now().minusDays(7); // 7 days old
        return withdrawalRepository.findNeedingAttention(cutoffDate);
    }

    /**
     * Statistics record
     */
    public record WithdrawalStatistics(
            long total,
            long draft,
            long pendingClearance,
            long clearanceInProgress,
            long cleared,
            long completed,
            long cancelled,
            long transfers,
            double averageClearanceCompletion
    ) {}

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Delete withdrawal record (only if in DRAFT status)
     */
    public void deleteWithdrawal(Long withdrawalId) {
        log.info("Deleting withdrawal record ID: {}", withdrawalId);

        WithdrawalRecord withdrawal = getWithdrawalById(withdrawalId);

        if (withdrawal.getStatus() != WithdrawalStatus.DRAFT) {
            throw new IllegalStateException("Only draft withdrawal records can be deleted");
        }

        withdrawalRepository.delete(withdrawal);
        log.info("Deleted withdrawal record: {}", withdrawal.getWithdrawalNumber());
    }
}
