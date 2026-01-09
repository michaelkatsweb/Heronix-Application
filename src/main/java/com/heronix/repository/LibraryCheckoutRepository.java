package com.heronix.repository;

import com.heronix.model.domain.LibraryCheckout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for LibraryCheckout entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface LibraryCheckoutRepository extends JpaRepository<LibraryCheckout, Long> {

    List<LibraryCheckout> findByStudentId(Long studentId);

    List<LibraryCheckout> findByStudentIdOrderByCheckoutDateDesc(Long studentId);

    List<LibraryCheckout> findByBookId(Long bookId);

    @Query("SELECT c FROM LibraryCheckout c WHERE c.student.id = :studentId AND c.status = 'CHECKED_OUT'")
    List<LibraryCheckout> findActiveCheckoutsByStudent(@Param("studentId") Long studentId);

    @Query("SELECT c FROM LibraryCheckout c WHERE c.status = 'CHECKED_OUT' AND c.dueDate < :today")
    List<LibraryCheckout> findOverdueCheckouts(@Param("today") LocalDate today);

    @Query("SELECT c FROM LibraryCheckout c WHERE c.student.id = :studentId AND c.status = 'CHECKED_OUT' AND c.dueDate < :today")
    List<LibraryCheckout> findOverdueCheckoutsByStudent(@Param("studentId") Long studentId, @Param("today") LocalDate today);

    @Query("SELECT c FROM LibraryCheckout c WHERE c.status = :status ORDER BY c.checkoutDate DESC")
    List<LibraryCheckout> findByStatus(@Param("status") LibraryCheckout.CheckoutStatus status);

    @Query("SELECT c FROM LibraryCheckout c WHERE c.checkoutDate BETWEEN :startDate AND :endDate ORDER BY c.checkoutDate")
    List<LibraryCheckout> findCheckoutsInDateRange(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(c) FROM LibraryCheckout c WHERE c.student.id = :studentId AND c.status = 'CHECKED_OUT'")
    long countActiveCheckoutsByStudent(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(c) FROM LibraryCheckout c WHERE c.status = 'CHECKED_OUT'")
    long countActiveCheckouts();

    @Query("SELECT COUNT(c) FROM LibraryCheckout c WHERE c.status = 'CHECKED_OUT' AND c.dueDate < :today")
    long countOverdueCheckouts(@Param("today") LocalDate today);

    @Query("SELECT c FROM LibraryCheckout c WHERE c.status = 'LOST'")
    List<LibraryCheckout> findLostBooks();
}
