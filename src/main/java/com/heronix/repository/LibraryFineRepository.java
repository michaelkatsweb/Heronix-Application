package com.heronix.repository;

import com.heronix.model.domain.LibraryFine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for LibraryFine entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface LibraryFineRepository extends JpaRepository<LibraryFine, Long> {

    List<LibraryFine> findByStudentId(Long studentId);

    List<LibraryFine> findByStudentIdOrderByFineDateDesc(Long studentId);

    @Query("SELECT f FROM LibraryFine f WHERE f.student.id = :studentId AND f.status != 'PAID' AND f.status != 'WAIVED'")
    List<LibraryFine> findOutstandingFinesByStudent(@Param("studentId") Long studentId);

    @Query("SELECT f FROM LibraryFine f WHERE f.status = :status ORDER BY f.fineDate DESC")
    List<LibraryFine> findByStatus(@Param("status") LibraryFine.FineStatus status);

    @Query("SELECT SUM(f.balance) FROM LibraryFine f WHERE f.student.id = :studentId AND f.status != 'PAID' AND f.status != 'WAIVED'")
    BigDecimal calculateOutstandingBalance(@Param("studentId") Long studentId);

    @Query("SELECT SUM(f.balance) FROM LibraryFine f WHERE f.status != 'PAID' AND f.status != 'WAIVED'")
    BigDecimal calculateTotalOutstanding();

    @Query("SELECT COUNT(f) FROM LibraryFine f WHERE f.status = 'UNPAID'")
    long countUnpaidFines();

    List<LibraryFine> findByFineType(LibraryFine.FineType fineType);
}
