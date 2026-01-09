package com.heronix.service;

import com.heronix.model.domain.Fee;
import com.heronix.model.domain.FeePayment;
import com.heronix.model.domain.StudentFee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Fee Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
public interface FeeManagementService {

    // Fee CRUD
    Fee createFee(Fee fee);
    Fee updateFee(Long feeId, Fee fee);
    void deleteFee(Long feeId);
    Fee getFeeById(Long feeId);
    List<Fee> getAllActiveFees();
    List<Fee> getFeesByType(Fee.FeeType feeType);
    List<Fee> getFeesByAcademicYear(String academicYear);

    // Student Fee Assignment
    StudentFee assignFeeToStudent(Long studentId, Long feeId);
    StudentFee assignFeeToStudent(Long studentId, Long feeId, BigDecimal customAmount, LocalDate customDueDate);
    void assignFeeToMultipleStudents(Long feeId, List<Long> studentIds);
    void assignFeesToGradeLevel(String gradeLevel, String academicYear);
    void removeStudentFee(Long studentFeeId);

    // Payment Processing
    FeePayment recordPayment(Long studentFeeId, BigDecimal amount, FeePayment.PaymentMethod method, String receivedBy);
    FeePayment recordPayment(Long studentFeeId, BigDecimal amount, FeePayment.PaymentMethod method,
                             String transactionId, String receivedBy);
    void refundPayment(Long paymentId, String reason, String refundedBy);

    // Fee Waivers
    void waiveFee(Long studentFeeId, String reason, String waivedBy);
    void partialWaiveFee(Long studentFeeId, BigDecimal waiveAmount, String reason, String waivedBy);
    void removeWaiver(Long studentFeeId);

    // Student Fee Queries
    List<StudentFee> getStudentFees(Long studentId);
    List<StudentFee> getOutstandingFees(Long studentId);
    List<StudentFee> getOverdueFees(Long studentId);
    BigDecimal getStudentOutstandingBalance(Long studentId);
    BigDecimal getStudentTotalPaid(Long studentId);

    // Payment History
    List<FeePayment> getStudentPaymentHistory(Long studentId);
    List<FeePayment> getPaymentHistory(Long studentFeeId);
    List<FeePayment> getPaymentsInDateRange(LocalDate startDate, LocalDate endDate);

    // Reporting
    List<StudentFee> getAllOverdueFees();
    BigDecimal calculateTotalOutstanding();
    BigDecimal calculateTotalCollected(LocalDate startDate, LocalDate endDate);
    Map<String, Object> getFeeCollectionReport(LocalDate startDate, LocalDate endDate);
    Map<String, Object> getStudentFeeStatement(Long studentId);
    List<Map<String, Object>> getFeeCollectionByType(LocalDate startDate, LocalDate endDate);

    // Automated Fee Assignment
    void assignMandatoryFeesToNewStudent(Long studentId);
    void assignAnnualFeesToAllStudents(String academicYear);
}
