package com.heronix.repository;

import com.heronix.model.domain.MealTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for MealTransaction entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface MealTransactionRepository extends JpaRepository<MealTransaction, Long> {

    List<MealTransaction> findByStudentId(Long studentId);

    List<MealTransaction> findByAccountId(Long accountId);

    List<MealTransaction> findByTransactionType(MealTransaction.TransactionType transactionType);

    @Query("SELECT t FROM MealTransaction t WHERE t.student.id = :studentId ORDER BY t.transactionDate DESC")
    List<MealTransaction> findByStudentIdOrderByDateDesc(@Param("studentId") Long studentId);

    @Query("SELECT t FROM MealTransaction t WHERE t.transactionDate >= :startDate AND t.transactionDate < :endDate ORDER BY t.transactionDate")
    List<MealTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM MealTransaction t WHERE t.student.id = :studentId AND t.transactionDate >= :startDate AND t.transactionDate < :endDate")
    List<MealTransaction> findByStudentAndDateRange(@Param("studentId") Long studentId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM MealTransaction t WHERE DATE(t.transactionDate) = :date")
    List<MealTransaction> findByDate(@Param("date") LocalDate date);

    @Query("SELECT t FROM MealTransaction t WHERE DATE(t.transactionDate) = :date AND t.mealType = :mealType")
    List<MealTransaction> findByDateAndMealType(@Param("date") LocalDate date,
                                                  @Param("mealType") MealTransaction.MealType mealType);

    @Query("SELECT COUNT(t) FROM MealTransaction t WHERE DATE(t.transactionDate) = :date AND t.transactionType = 'PURCHASE'")
    long countMealsServedByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(t) FROM MealTransaction t WHERE DATE(t.transactionDate) = :date AND t.mealType = :mealType AND t.transactionType = 'PURCHASE'")
    long countMealsByTypeAndDate(@Param("mealType") MealTransaction.MealType mealType,
                                  @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM MealTransaction t WHERE DATE(t.transactionDate) = :date AND t.transactionType = 'PURCHASE'")
    BigDecimal getTotalRevenueByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM MealTransaction t WHERE t.transactionDate >= :startDate AND t.transactionDate < :endDate AND t.transactionType = 'PURCHASE'")
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(DISTINCT t.student.id) FROM MealTransaction t WHERE DATE(t.transactionDate) = :date AND t.transactionType = 'PURCHASE'")
    long countUniqueStudentsByDate(@Param("date") LocalDate date);
}
