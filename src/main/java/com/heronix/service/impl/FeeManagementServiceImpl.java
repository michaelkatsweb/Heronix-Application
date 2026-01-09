package com.heronix.service.impl;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import com.heronix.service.FeeManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of FeeManagementService
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeeManagementServiceImpl implements FeeManagementService {

    private final FeeRepository feeRepository;
    private final StudentFeeRepository studentFeeRepository;
    private final FeePaymentRepository paymentRepository;
    private final StudentRepository studentRepository;

    // ========================================================================
    // FEE CRUD
    // ========================================================================

    @Override
    @Transactional
    public Fee createFee(Fee fee) {
        log.info("Creating new fee: {}", fee.getFeeName());
        return feeRepository.save(fee);
    }

    @Override
    @Transactional
    public Fee updateFee(Long feeId, Fee fee) {
        log.info("Updating fee ID: {}", feeId);
        Fee existing = feeRepository.findById(feeId)
                .orElseThrow(() -> new IllegalArgumentException("Fee not found with ID: " + feeId));

        existing.setFeeName(fee.getFeeName());
        existing.setDescription(fee.getDescription());
        existing.setAmount(fee.getAmount());
        existing.setDueDate(fee.getDueDate());
        existing.setMandatory(fee.getMandatory());
        existing.setWaivable(fee.getWaivable());
        existing.setActive(fee.getActive());

        return feeRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteFee(Long feeId) {
        log.info("Deleting fee ID: {}", feeId);
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new IllegalArgumentException("Fee not found with ID: " + feeId));
        fee.setActive(false);
        feeRepository.save(fee);
    }

    @Override
    @Transactional(readOnly = true)
    public Fee getFeeById(Long feeId) {
        return feeRepository.findById(feeId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Fee> getAllActiveFees() {
        return feeRepository.findByActiveTrueOrderByFeeNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Fee> getFeesByType(Fee.FeeType feeType) {
        return feeRepository.findByFeeTypeAndActiveTrue(feeType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Fee> getFeesByAcademicYear(String academicYear) {
        return feeRepository.findByAcademicYearAndActiveTrue(academicYear);
    }

    // ========================================================================
    // STUDENT FEE ASSIGNMENT
    // ========================================================================

    @Override
    @Transactional
    public StudentFee assignFeeToStudent(Long studentId, Long feeId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new IllegalArgumentException("Fee not found with ID: " + feeId));

        // Check if already assigned
        Optional<StudentFee> existing = studentFeeRepository.findByStudentIdAndFeeId(studentId, feeId);
        if (existing.isPresent()) {
            log.warn("Fee {} already assigned to student {}", feeId, studentId);
            return existing.get();
        }

        StudentFee studentFee = StudentFee.builder()
                .student(student)
                .fee(fee)
                .amountDue(fee.getAmount())
                .amountPaid(BigDecimal.ZERO)
                .amountWaived(BigDecimal.ZERO)
                .dueDate(fee.getDueDate())
                .status(StudentFee.FeeStatus.PENDING)
                .waived(false)
                .build();

        StudentFee saved = studentFeeRepository.save(studentFee);
        log.info("Assigned fee {} to student {}", fee.getFeeName(), student.getFullName());
        return saved;
    }

    @Override
    @Transactional
    public StudentFee assignFeeToStudent(Long studentId, Long feeId, BigDecimal customAmount, LocalDate customDueDate) {
        StudentFee studentFee = assignFeeToStudent(studentId, feeId);
        if (customAmount != null) {
            studentFee.setAmountDue(customAmount);
        }
        if (customDueDate != null) {
            studentFee.setDueDate(customDueDate);
        }
        return studentFeeRepository.save(studentFee);
    }

    @Override
    @Transactional
    public void assignFeeToMultipleStudents(Long feeId, List<Long> studentIds) {
        log.info("Assigning fee {} to {} students", feeId, studentIds.size());
        for (Long studentId : studentIds) {
            try {
                assignFeeToStudent(studentId, feeId);
            } catch (Exception e) {
                log.error("Failed to assign fee {} to student {}: {}", feeId, studentId, e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void assignFeesToGradeLevel(String gradeLevel, String academicYear) {
        log.info("Assigning fees to grade level: {}, academic year: {}", gradeLevel, academicYear);

        List<Fee> applicableFees = feeRepository.findApplicableFeesForStudent(gradeLevel, academicYear);
        List<Student> students = studentRepository.findByGradeLevel(gradeLevel);

        for (Student student : students) {
            for (Fee fee : applicableFees) {
                try {
                    assignFeeToStudent(student.getId(), fee.getId());
                } catch (Exception e) {
                    log.error("Failed to assign fee {} to student {}: {}",
                            fee.getId(), student.getId(), e.getMessage());
                }
            }
        }
    }

    @Override
    @Transactional
    public void removeStudentFee(Long studentFeeId) {
        log.info("Removing student fee ID: {}", studentFeeId);
        studentFeeRepository.deleteById(studentFeeId);
    }

    // ========================================================================
    // PAYMENT PROCESSING
    // ========================================================================

    @Override
    @Transactional
    public FeePayment recordPayment(Long studentFeeId, BigDecimal amount,
                                     FeePayment.PaymentMethod method, String receivedBy) {
        return recordPayment(studentFeeId, amount, method, null, receivedBy);
    }

    @Override
    @Transactional
    public FeePayment recordPayment(Long studentFeeId, BigDecimal amount, FeePayment.PaymentMethod method,
                                     String transactionId, String receivedBy) {
        log.info("Recording payment of {} for student fee ID: {}", amount, studentFeeId);

        StudentFee studentFee = studentFeeRepository.findById(studentFeeId)
                .orElseThrow(() -> new IllegalArgumentException("Student fee not found with ID: " + studentFeeId));

        // Create payment record
        FeePayment payment = FeePayment.builder()
                .studentFee(studentFee)
                .amount(amount)
                .paymentDate(LocalDate.now())
                .paymentMethod(method)
                .transactionId(transactionId)
                .confirmationNumber(generateConfirmationNumber())
                .receivedBy(receivedBy)
                .refunded(false)
                .build();

        FeePayment saved = paymentRepository.save(payment);

        // Update student fee
        studentFee.setAmountPaid(studentFee.getAmountPaid().add(amount));
        studentFeeRepository.save(studentFee);

        log.info("Payment recorded successfully. Confirmation: {}", saved.getConfirmationNumber());
        return saved;
    }

    @Override
    @Transactional
    public void refundPayment(Long paymentId, String reason, String refundedBy) {
        log.info("Processing refund for payment ID: {}", paymentId);

        FeePayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));

        if (payment.getRefunded()) {
            throw new IllegalStateException("Payment already refunded");
        }

        payment.setRefunded(true);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundReason(reason);
        paymentRepository.save(payment);

        // Update student fee
        StudentFee studentFee = payment.getStudentFee();
        studentFee.setAmountPaid(studentFee.getAmountPaid().subtract(payment.getAmount()));
        studentFeeRepository.save(studentFee);

        log.info("Payment refunded successfully");
    }

    // ========================================================================
    // FEE WAIVERS
    // ========================================================================

    @Override
    @Transactional
    public void waiveFee(Long studentFeeId, String reason, String waivedBy) {
        log.info("Waiving fee for student fee ID: {}", studentFeeId);

        StudentFee studentFee = studentFeeRepository.findById(studentFeeId)
                .orElseThrow(() -> new IllegalArgumentException("Student fee not found with ID: " + studentFeeId));

        if (!studentFee.getFee().getWaivable()) {
            throw new IllegalStateException("This fee cannot be waived");
        }

        BigDecimal balance = studentFee.getBalance();
        studentFee.setAmountWaived(studentFee.getAmountWaived().add(balance));
        studentFee.setWaived(true);
        studentFee.setWaiverReason(reason);
        studentFee.setWaivedBy(waivedBy);
        studentFee.setWaivedAt(LocalDateTime.now());

        studentFeeRepository.save(studentFee);
        log.info("Fee waived successfully");
    }

    @Override
    @Transactional
    public void partialWaiveFee(Long studentFeeId, BigDecimal waiveAmount, String reason, String waivedBy) {
        log.info("Partially waiving {} for student fee ID: {}", waiveAmount, studentFeeId);

        StudentFee studentFee = studentFeeRepository.findById(studentFeeId)
                .orElseThrow(() -> new IllegalArgumentException("Student fee not found with ID: " + studentFeeId));

        if (!studentFee.getFee().getWaivable()) {
            throw new IllegalStateException("This fee cannot be waived");
        }

        studentFee.setAmountWaived(studentFee.getAmountWaived().add(waiveAmount));
        studentFee.setWaiverReason(reason);
        studentFee.setWaivedBy(waivedBy);
        studentFee.setWaivedAt(LocalDateTime.now());

        studentFeeRepository.save(studentFee);
        log.info("Partial fee waiver applied successfully");
    }

    @Override
    @Transactional
    public void removeWaiver(Long studentFeeId) {
        log.info("Removing waiver for student fee ID: {}", studentFeeId);

        StudentFee studentFee = studentFeeRepository.findById(studentFeeId)
                .orElseThrow(() -> new IllegalArgumentException("Student fee not found with ID: " + studentFeeId));

        studentFee.setAmountWaived(BigDecimal.ZERO);
        studentFee.setWaived(false);
        studentFee.setWaiverReason(null);
        studentFee.setWaivedBy(null);
        studentFee.setWaivedAt(null);

        studentFeeRepository.save(studentFee);
        log.info("Waiver removed successfully");
    }

    // ========================================================================
    // STUDENT FEE QUERIES
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<StudentFee> getStudentFees(Long studentId) {
        return studentFeeRepository.findByStudentIdOrderByDueDateAsc(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentFee> getOutstandingFees(Long studentId) {
        return studentFeeRepository.findOutstandingFeesByStudent(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentFee> getOverdueFees(Long studentId) {
        return studentFeeRepository.findOverdueFeesByStudent(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getStudentOutstandingBalance(Long studentId) {
        BigDecimal balance = studentFeeRepository.calculateOutstandingBalance(studentId);
        return balance != null ? balance : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getStudentTotalPaid(Long studentId) {
        BigDecimal total = paymentRepository.calculateTotalPaidByStudent(studentId);
        return total != null ? total : BigDecimal.ZERO;
    }

    // ========================================================================
    // PAYMENT HISTORY
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<FeePayment> getStudentPaymentHistory(Long studentId) {
        return paymentRepository.findByStudentIdOrderByPaymentDateDesc(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeePayment> getPaymentHistory(Long studentFeeId) {
        return paymentRepository.findByStudentFeeIdOrderByPaymentDateDesc(studentFeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeePayment> getPaymentsInDateRange(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.findPaymentsInDateRange(startDate, endDate);
    }

    // ========================================================================
    // REPORTING
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<StudentFee> getAllOverdueFees() {
        return studentFeeRepository.findAllOverdueFees();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalOutstanding() {
        List<StudentFee> allFees = studentFeeRepository.findAll();
        return allFees.stream()
                .map(StudentFee::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCollected(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = paymentRepository.calculateTotalPaymentsInDateRange(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getFeeCollectionReport(LocalDate startDate, LocalDate endDate) {
        List<FeePayment> payments = getPaymentsInDateRange(startDate, endDate);

        Map<String, Object> report = new HashMap<>();
        report.put("totalPayments", payments.size());
        report.put("totalAmount", payments.stream()
                .filter(p -> !p.getRefunded())
                .map(FeePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        report.put("totalRefunds", payments.stream()
                .filter(FeePayment::getRefunded)
                .count());

        // Group by payment method
        Map<FeePayment.PaymentMethod, BigDecimal> byMethod = payments.stream()
                .filter(p -> !p.getRefunded())
                .collect(Collectors.groupingBy(
                        FeePayment::getPaymentMethod,
                        Collectors.reducing(BigDecimal.ZERO, FeePayment::getAmount, BigDecimal::add)
                ));
        report.put("byPaymentMethod", byMethod);

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentFeeStatement(Long studentId) {
        List<StudentFee> fees = getStudentFees(studentId);
        List<FeePayment> payments = getStudentPaymentHistory(studentId);

        Map<String, Object> statement = new HashMap<>();
        statement.put("studentId", studentId);
        statement.put("fees", fees);
        statement.put("payments", payments);
        statement.put("totalDue", fees.stream()
                .map(StudentFee::getAmountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        statement.put("totalPaid", payments.stream()
                .filter(p -> !p.getRefunded())
                .map(FeePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        statement.put("outstandingBalance", getStudentOutstandingBalance(studentId));

        return statement;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getFeeCollectionByType(LocalDate startDate, LocalDate endDate) {
        List<FeePayment> payments = getPaymentsInDateRange(startDate, endDate);

        Map<Fee.FeeType, BigDecimal> collectionByType = new HashMap<>();
        for (FeePayment payment : payments) {
            if (!payment.getRefunded()) {
                Fee.FeeType type = payment.getStudentFee().getFee().getFeeType();
                collectionByType.merge(type, payment.getAmount(), BigDecimal::add);
            }
        }

        return collectionByType.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("feeType", entry.getKey().toString());
                    map.put("totalCollected", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // ========================================================================
    // AUTOMATED FEE ASSIGNMENT
    // ========================================================================

    @Override
    @Transactional
    public void assignMandatoryFeesToNewStudent(Long studentId) {
        log.info("Assigning mandatory fees to new student ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        List<Fee> mandatoryFees = feeRepository.findApplicableFeesForStudent(
                student.getGradeLevel(),
                getCurrentAcademicYear()
        ).stream()
         .filter(Fee::getMandatory)
         .toList();

        for (Fee fee : mandatoryFees) {
            try {
                assignFeeToStudent(studentId, fee.getId());
            } catch (Exception e) {
                log.error("Failed to assign mandatory fee {}: {}", fee.getId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void assignAnnualFeesToAllStudents(String academicYear) {
        log.info("Assigning annual fees for academic year: {}", academicYear);

        List<Student> allStudents = studentRepository.findByActiveTrue();
        List<Fee> annualFees = feeRepository.findByAcademicYearAndActiveTrue(academicYear);

        for (Student student : allStudents) {
            for (Fee fee : annualFees) {
                if (fee.getFrequency() == Fee.FeeFrequency.ANNUAL) {
                    try {
                        assignFeeToStudent(student.getId(), fee.getId());
                    } catch (Exception e) {
                        log.error("Failed to assign annual fee: {}", e.getMessage());
                    }
                }
            }
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private String generateConfirmationNumber() {
        return "PAY-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    private String getCurrentAcademicYear() {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        if (month >= 8) {
            return year + "-" + (year + 1);
        } else {
            return (year - 1) + "-" + year;
        }
    }
}
