package com.heronix.repository;

import com.heronix.model.domain.FeePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for FeePayment entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {

    List<FeePayment> findByStudentFeeId(Long studentFeeId);

    List<FeePayment> findByStudentFeeIdOrderByPaymentDateDesc(Long studentFeeId);

    @Query("SELECT fp FROM FeePayment fp WHERE fp.studentFee.student.id = :studentId ORDER BY fp.paymentDate DESC")
    List<FeePayment> findByStudentIdOrderByPaymentDateDesc(@Param("studentId") Long studentId);

    @Query("SELECT fp FROM FeePayment fp WHERE fp.paymentDate BETWEEN :startDate AND :endDate ORDER BY fp.paymentDate")
    List<FeePayment> findPaymentsInDateRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(fp.amount) FROM FeePayment fp WHERE fp.paymentDate BETWEEN :startDate AND :endDate AND fp.refunded = false")
    BigDecimal calculateTotalPaymentsInDateRange(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT fp FROM FeePayment fp WHERE fp.paymentMethod = :method ORDER BY fp.paymentDate DESC")
    List<FeePayment> findByPaymentMethod(@Param("method") FeePayment.PaymentMethod method);

    @Query("SELECT fp FROM FeePayment fp WHERE fp.refunded = true ORDER BY fp.refundedAt DESC")
    List<FeePayment> findAllRefundedPayments();

    @Query("SELECT SUM(fp.amount) FROM FeePayment fp WHERE fp.studentFee.student.id = :studentId AND fp.refunded = false")
    BigDecimal calculateTotalPaidByStudent(@Param("studentId") Long studentId);
}
