package com.heronix.repository;

import com.heronix.model.domain.StudentFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for StudentFee entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface StudentFeeRepository extends JpaRepository<StudentFee, Long> {

    List<StudentFee> findByStudentId(Long studentId);

    List<StudentFee> findByStudentIdOrderByDueDateAsc(Long studentId);

    List<StudentFee> findByStudentIdAndStatus(Long studentId, StudentFee.FeeStatus status);

    Optional<StudentFee> findByStudentIdAndFeeId(Long studentId, Long feeId);

    @Query("SELECT sf FROM StudentFee sf WHERE sf.student.id = :studentId AND sf.status != 'PAID' AND sf.status != 'WAIVED'")
    List<StudentFee> findOutstandingFeesByStudent(@Param("studentId") Long studentId);

    @Query("SELECT sf FROM StudentFee sf WHERE sf.status = 'OVERDUE' ORDER BY sf.dueDate ASC")
    List<StudentFee> findAllOverdueFees();

    @Query("SELECT sf FROM StudentFee sf WHERE sf.student.id = :studentId AND sf.status = 'OVERDUE'")
    List<StudentFee> findOverdueFeesByStudent(@Param("studentId") Long studentId);

    @Query("SELECT SUM(sf.amountDue - sf.amountPaid - sf.amountWaived) FROM StudentFee sf " +
           "WHERE sf.student.id = :studentId AND sf.status != 'PAID' AND sf.status != 'WAIVED'")
    BigDecimal calculateOutstandingBalance(@Param("studentId") Long studentId);

    @Query("SELECT SUM(sf.amountPaid) FROM StudentFee sf WHERE sf.student.id = :studentId")
    BigDecimal calculateTotalPaid(@Param("studentId") Long studentId);

    @Query("SELECT sf FROM StudentFee sf WHERE sf.dueDate BETWEEN :startDate AND :endDate")
    List<StudentFee> findFeesDueInDateRange(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(sf) FROM StudentFee sf WHERE sf.status = 'OVERDUE'")
    long countOverdueFees();

    @Query("SELECT sf FROM StudentFee sf WHERE sf.fee.feeType = :feeType AND sf.student.id = :studentId")
    List<StudentFee> findByStudentAndFeeType(@Param("studentId") Long studentId,
                                               @Param("feeType") com.heronix.model.domain.Fee.FeeType feeType);
}
