package com.heronix.service;

import com.heronix.model.domain.AttendanceAdjustment;
import com.heronix.model.domain.AttendanceAdjustment.AdjustmentSource;
import com.heronix.model.domain.AttendanceAdjustment.AdjustmentType;
import com.heronix.model.domain.AttendanceAdjustment.ApprovalStatus;
import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.AttendanceRecord.AttendanceStatus;
import com.heronix.model.domain.ExcuseCode;
import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceAdjustmentRepository;
import com.heronix.repository.AttendanceRecordRepository;
import com.heronix.repository.ExcuseCodeRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.security.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing attendance adjustments
 *
 * Handles all attendance modifications by clerks and administrators,
 * including excuse processing, error corrections, and pre-approved absences.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 58 - Attendance Enhancement - January 2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceAdjustmentService {

    private final AttendanceAdjustmentRepository adjustmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ExcuseCodeRepository excuseCodeRepository;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    // ========================================================================
    // EXCUSE ABSENCES
    // ========================================================================

    /**
     * Excuse a student's absence with an excuse code
     *
     * @param studentId Student ID
     * @param attendanceDate Date of absence
     * @param periodNumber Period number (null for all day)
     * @param excuseCodeId Excuse code ID
     * @param reason Reason for excusing
     * @param source Source of the excuse request
     * @param documentationProvided Whether documentation was provided
     * @return Created adjustment
     */
    @Transactional
    public AttendanceAdjustment excuseAbsence(
            Long studentId,
            LocalDate attendanceDate,
            Integer periodNumber,
            Long excuseCodeId,
            String reason,
            AdjustmentSource source,
            boolean documentationProvided) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        ExcuseCode excuseCode = excuseCodeRepository.findById(excuseCodeId)
                .orElseThrow(() -> new IllegalArgumentException("Excuse code not found: " + excuseCodeId));

        // Find the attendance record(s) to adjust
        List<AttendanceRecord> records = findAttendanceRecords(student, attendanceDate, periodNumber);

        // Determine if approval is required
        ApprovalStatus status = excuseCode.getRequiresApproval()
                ? ApprovalStatus.PENDING
                : ApprovalStatus.AUTO_APPROVED;

        // Determine new status based on excuse code
        AttendanceStatus newStatus = excuseCode.getCountsAsExcused()
                ? AttendanceStatus.EXCUSED_ABSENT
                : AttendanceStatus.UNEXCUSED_ABSENT;

        String currentUser = SecurityContext.getCurrentUsername().orElse("system");
        String currentRole = SecurityContext.getCurrentRole().map(r -> r.name()).orElse("CLERK");

        // Create adjustment for each record (or one for all day)
        AttendanceAdjustment adjustment = AttendanceAdjustment.builder()
                .student(student)
                .attendanceDate(attendanceDate)
                .periodNumber(periodNumber)
                .attendanceRecord(records.isEmpty() ? null : records.get(0))
                .adjustmentType(AdjustmentType.EXCUSE_ABSENCE)
                .originalStatus(records.isEmpty() ? null : records.get(0).getStatus())
                .newStatus(newStatus)
                .excuseCode(excuseCode)
                .reason(reason)
                .source(source)
                .documentationProvided(documentationProvided)
                .approvalStatus(status)
                .adjustedBy(currentUser)
                .adjustedByRole(currentRole)
                .adjustmentDate(LocalDateTime.now())
                .build();

        adjustment = adjustmentRepository.save(adjustment);

        // If auto-approved, apply immediately
        if (status == ApprovalStatus.AUTO_APPROVED) {
            applyAdjustment(adjustment);
        }

        log.info("Created excuse adjustment for student {} on {} - Status: {}",
                student.getStudentId(), attendanceDate, status);

        auditService.logAttendanceAdjustment(adjustment, "EXCUSE_CREATED");

        return adjustment;
    }

    // ========================================================================
    // MARK PRESENT/ABSENT/TARDY
    // ========================================================================

    /**
     * Mark student as present (correct an absence)
     */
    @Transactional
    public AttendanceAdjustment markPresent(
            Long studentId,
            LocalDate attendanceDate,
            Integer periodNumber,
            String reason,
            AdjustmentSource source) {

        return createStatusAdjustment(
                studentId, attendanceDate, periodNumber,
                AdjustmentType.MARK_PRESENT, AttendanceStatus.PRESENT,
                reason, source);
    }

    /**
     * Mark student as absent
     */
    @Transactional
    public AttendanceAdjustment markAbsent(
            Long studentId,
            LocalDate attendanceDate,
            Integer periodNumber,
            String reason,
            AdjustmentSource source) {

        return createStatusAdjustment(
                studentId, attendanceDate, periodNumber,
                AdjustmentType.MARK_ABSENT, AttendanceStatus.ABSENT,
                reason, source);
    }

    /**
     * Mark student as tardy
     */
    @Transactional
    public AttendanceAdjustment markTardy(
            Long studentId,
            LocalDate attendanceDate,
            Integer periodNumber,
            String reason,
            AdjustmentSource source) {

        return createStatusAdjustment(
                studentId, attendanceDate, periodNumber,
                AdjustmentType.MARK_TARDY, AttendanceStatus.TARDY,
                reason, source);
    }

    /**
     * Correct a data entry error
     */
    @Transactional
    public AttendanceAdjustment correctError(
            Long studentId,
            LocalDate attendanceDate,
            Integer periodNumber,
            AttendanceStatus newStatus,
            String reason) {

        return createStatusAdjustment(
                studentId, attendanceDate, periodNumber,
                AdjustmentType.CORRECT_ERROR, newStatus,
                reason, AdjustmentSource.OFFICE);
    }

    // ========================================================================
    // PRE-APPROVED ABSENCES
    // ========================================================================

    /**
     * Pre-approve a planned absence (vacation, college visit, etc.)
     */
    @Transactional
    public List<AttendanceAdjustment> preApproveAbsence(
            Long studentId,
            LocalDate startDate,
            LocalDate endDate,
            Long excuseCodeId,
            String reason,
            String notes) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        ExcuseCode excuseCode = excuseCodeRepository.findById(excuseCodeId)
                .orElseThrow(() -> new IllegalArgumentException("Excuse code not found: " + excuseCodeId));

        String currentUser = SecurityContext.getCurrentUsername().orElse("system");
        String currentRole = SecurityContext.getCurrentRole().map(r -> r.name()).orElse("CLERK");

        // Check if this exceeds maximum consecutive days
        long dayCount = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        ApprovalStatus status = ApprovalStatus.APPROVED;

        if (excuseCode.getMaxConsecutiveDays() != null &&
            excuseCode.getMaxConsecutiveDays() > 0 &&
            dayCount > excuseCode.getMaxConsecutiveDays()) {
            status = ApprovalStatus.PENDING;
            log.info("Pre-approved absence exceeds {} days for excuse code {}, requires approval",
                    excuseCode.getMaxConsecutiveDays(), excuseCode.getCode());
        }

        // Create adjustment for each day
        List<AttendanceAdjustment> adjustments = new java.util.ArrayList<>();
        LocalDate date = startDate;

        while (!date.isAfter(endDate)) {
            // Skip weekends (typically)
            if (date.getDayOfWeek().getValue() < 6) {
                AttendanceAdjustment adjustment = AttendanceAdjustment.builder()
                        .student(student)
                        .attendanceDate(date)
                        .periodNumber(null) // All day
                        .adjustmentType(AdjustmentType.PRE_APPROVE_ABSENCE)
                        .originalStatus(null)
                        .newStatus(AttendanceStatus.EXCUSED_ABSENT)
                        .excuseCode(excuseCode)
                        .reason(reason)
                        .notes(notes)
                        .source(AdjustmentSource.OFFICE)
                        .approvalStatus(status)
                        .adjustedBy(currentUser)
                        .adjustedByRole(currentRole)
                        .adjustmentDate(LocalDateTime.now())
                        .build();

                adjustments.add(adjustmentRepository.save(adjustment));
            }
            date = date.plusDays(1);
        }

        log.info("Created {} pre-approved absence adjustments for student {} from {} to {}",
                adjustments.size(), student.getStudentId(), startDate, endDate);

        return adjustments;
    }

    // ========================================================================
    // APPROVAL WORKFLOW
    // ========================================================================

    /**
     * Approve a pending adjustment
     */
    @Transactional
    public AttendanceAdjustment approveAdjustment(Long adjustmentId, String approvalNotes) {
        AttendanceAdjustment adjustment = adjustmentRepository.findById(adjustmentId)
                .orElseThrow(() -> new IllegalArgumentException("Adjustment not found: " + adjustmentId));

        if (adjustment.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Adjustment is not pending approval");
        }

        String currentUser = SecurityContext.getCurrentUsername().orElse("system");

        adjustment.setApprovalStatus(ApprovalStatus.APPROVED);
        adjustment.setApprovedBy(currentUser);
        adjustment.setApprovalDate(LocalDateTime.now());
        adjustment.setApprovalNotes(approvalNotes);

        adjustment = adjustmentRepository.save(adjustment);

        // Apply the adjustment
        applyAdjustment(adjustment);

        log.info("Approved and applied adjustment {} by {}", adjustmentId, currentUser);
        auditService.logAttendanceAdjustment(adjustment, "APPROVED");

        return adjustment;
    }

    /**
     * Reject a pending adjustment
     */
    @Transactional
    public AttendanceAdjustment rejectAdjustment(Long adjustmentId, String rejectionReason) {
        AttendanceAdjustment adjustment = adjustmentRepository.findById(adjustmentId)
                .orElseThrow(() -> new IllegalArgumentException("Adjustment not found: " + adjustmentId));

        if (adjustment.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Adjustment is not pending approval");
        }

        String currentUser = SecurityContext.getCurrentUsername().orElse("system");

        adjustment.setApprovalStatus(ApprovalStatus.REJECTED);
        adjustment.setApprovedBy(currentUser);
        adjustment.setApprovalDate(LocalDateTime.now());
        adjustment.setApprovalNotes(rejectionReason);

        adjustment = adjustmentRepository.save(adjustment);

        log.info("Rejected adjustment {} by {} - Reason: {}", adjustmentId, currentUser, rejectionReason);
        auditService.logAttendanceAdjustment(adjustment, "REJECTED");

        return adjustment;
    }

    /**
     * Get pending adjustments for approval
     */
    public List<AttendanceAdjustment> getPendingApprovals() {
        return adjustmentRepository.findByApprovalStatusOrderByAdjustmentDateAsc(ApprovalStatus.PENDING);
    }

    /**
     * Count pending approvals
     */
    public long countPendingApprovals() {
        return adjustmentRepository.countByApprovalStatus(ApprovalStatus.PENDING);
    }

    // ========================================================================
    // APPLY ADJUSTMENT
    // ========================================================================

    /**
     * Apply an approved adjustment to the attendance record
     */
    @Transactional
    public void applyAdjustment(AttendanceAdjustment adjustment) {
        if (Boolean.TRUE.equals(adjustment.getApplied())) {
            log.warn("Adjustment {} already applied, skipping", adjustment.getId());
            return;
        }

        Student student = adjustment.getStudent();
        LocalDate date = adjustment.getAttendanceDate();
        Integer period = adjustment.getPeriodNumber();

        List<AttendanceRecord> records = findAttendanceRecords(student, date, period);

        if (records.isEmpty()) {
            // No existing record, might need to create based on adjustment type
            log.warn("No attendance record found for student {} on {} period {}",
                    student.getStudentId(), date, period);
        } else {
            // Update existing records
            for (AttendanceRecord record : records) {
                record.setStatus(adjustment.getNewStatus());
                if (adjustment.getExcuseCode() != null) {
                    record.setExcuseCode(adjustment.getExcuseCode().getCode());
                }
                record.setNotes(appendNote(record.getNotes(),
                        "Adjusted: " + adjustment.getReason()));
                record.setVerified(true);

                attendanceRecordRepository.save(record);
            }
        }

        adjustment.setApplied(true);
        adjustment.setAppliedDate(LocalDateTime.now());
        adjustmentRepository.save(adjustment);

        log.info("Applied adjustment {} - {} records updated",
                adjustment.getId(), records.size());
    }

    // ========================================================================
    // QUERY METHODS
    // ========================================================================

    /**
     * Find all adjustments for a student
     */
    public List<AttendanceAdjustment> findByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        return adjustmentRepository.findByStudentOrderByAdjustmentDateDesc(student);
    }

    /**
     * Find adjustments within date range
     */
    public List<AttendanceAdjustment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return adjustmentRepository.findByAdjustmentDateBetweenOrderByAdjustmentDateDesc(startDate, endDate);
    }

    /**
     * Find adjustments made by a specific user
     */
    public List<AttendanceAdjustment> findByUser(String username) {
        return adjustmentRepository.findByAdjustedByOrderByAdjustmentDateDesc(username);
    }

    /**
     * Find recent adjustments (last N days)
     */
    public List<AttendanceAdjustment> findRecentAdjustments(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return adjustmentRepository.findRecentAdjustments(since);
    }

    /**
     * Get adjustment by ID
     */
    public Optional<AttendanceAdjustment> findById(Long id) {
        return adjustmentRepository.findById(id);
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private AttendanceAdjustment createStatusAdjustment(
            Long studentId,
            LocalDate attendanceDate,
            Integer periodNumber,
            AdjustmentType adjustmentType,
            AttendanceStatus newStatus,
            String reason,
            AdjustmentSource source) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        List<AttendanceRecord> records = findAttendanceRecords(student, attendanceDate, periodNumber);

        String currentUser = SecurityContext.getCurrentUsername().orElse("system");
        String currentRole = SecurityContext.getCurrentRole().map(r -> r.name()).orElse("CLERK");

        AttendanceAdjustment adjustment = AttendanceAdjustment.builder()
                .student(student)
                .attendanceDate(attendanceDate)
                .periodNumber(periodNumber)
                .attendanceRecord(records.isEmpty() ? null : records.get(0))
                .adjustmentType(adjustmentType)
                .originalStatus(records.isEmpty() ? null : records.get(0).getStatus())
                .newStatus(newStatus)
                .reason(reason)
                .source(source)
                .approvalStatus(ApprovalStatus.AUTO_APPROVED)
                .adjustedBy(currentUser)
                .adjustedByRole(currentRole)
                .adjustmentDate(LocalDateTime.now())
                .build();

        adjustment = adjustmentRepository.save(adjustment);

        // Apply immediately for auto-approved adjustments
        applyAdjustment(adjustment);

        log.info("Created and applied {} adjustment for student {} on {}",
                adjustmentType, student.getStudentId(), attendanceDate);

        auditService.logAttendanceAdjustment(adjustment, "CREATED_AND_APPLIED");

        return adjustment;
    }

    private List<AttendanceRecord> findAttendanceRecords(Student student, LocalDate date, Integer period) {
        if (period == null) {
            // All day - find all records for the date
            return attendanceRecordRepository.findByStudentAndAttendanceDateOrderByPeriodNumberAsc(
                    student, date);
        } else {
            // Specific period
            return attendanceRecordRepository.findByStudentAndAttendanceDateAndPeriodNumber(
                    student, date, period);
        }
    }

    private String appendNote(String existing, String newNote) {
        if (existing == null || existing.isEmpty()) {
            return newNote;
        }
        return existing + " | " + newNote;
    }
}
