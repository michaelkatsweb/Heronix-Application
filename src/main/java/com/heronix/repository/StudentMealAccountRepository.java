package com.heronix.repository;

import com.heronix.model.domain.StudentMealAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for StudentMealAccount entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface StudentMealAccountRepository extends JpaRepository<StudentMealAccount, Long> {

    Optional<StudentMealAccount> findByStudentIdAndAcademicYear(Long studentId, String academicYear);

    List<StudentMealAccount> findByStudentId(Long studentId);

    List<StudentMealAccount> findByAcademicYear(String academicYear);

    List<StudentMealAccount> findByStatus(StudentMealAccount.AccountStatus status);

    List<StudentMealAccount> findByEligibilityStatus(StudentMealAccount.EligibilityStatus eligibilityStatus);

    @Query("SELECT a FROM StudentMealAccount a WHERE a.status = 'ACTIVE' AND a.academicYear = :year")
    List<StudentMealAccount> findActiveAccountsByYear(@Param("year") String academicYear);

    @Query("SELECT a FROM StudentMealAccount a WHERE a.balance < :threshold AND a.status = 'ACTIVE'")
    List<StudentMealAccount> findAccountsWithLowBalance(@Param("threshold") BigDecimal threshold);

    @Query("SELECT a FROM StudentMealAccount a WHERE a.autoReload = true AND a.balance < a.reloadThreshold")
    List<StudentMealAccount> findAccountsNeedingReload();

    @Query("SELECT a FROM StudentMealAccount a WHERE a.eligibilityStatus = 'PENDING_VERIFICATION'")
    List<StudentMealAccount> findAccountsPendingVerification();

    @Query("SELECT COUNT(a) FROM StudentMealAccount a WHERE a.academicYear = :year AND a.status = 'ACTIVE'")
    long countActiveAccountsByYear(@Param("year") String academicYear);

    @Query("SELECT COUNT(a) FROM StudentMealAccount a WHERE a.academicYear = :year AND a.eligibilityStatus = :status")
    long countByYearAndEligibility(@Param("year") String academicYear,
                                    @Param("status") StudentMealAccount.EligibilityStatus status);

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM StudentMealAccount a WHERE a.academicYear = :year AND a.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByYear(@Param("year") String academicYear);
}
