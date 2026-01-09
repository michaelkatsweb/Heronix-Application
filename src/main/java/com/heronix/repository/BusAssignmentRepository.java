package com.heronix.repository;

import com.heronix.model.domain.BusAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BusAssignment entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface BusAssignmentRepository extends JpaRepository<BusAssignment, Long> {

    List<BusAssignment> findByStudentId(Long studentId);

    List<BusAssignment> findByRouteId(Long routeId);

    List<BusAssignment> findByStudentIdAndAcademicYear(Long studentId, String academicYear);

    List<BusAssignment> findByRouteIdAndAcademicYear(Long routeId, String academicYear);

    Optional<BusAssignment> findByStudentIdAndRouteIdAndAcademicYear(Long studentId, Long routeId, String academicYear);

    @Query("SELECT a FROM BusAssignment a WHERE a.student.id = :studentId AND a.status = 'ACTIVE'")
    List<BusAssignment> findActiveByStudent(@Param("studentId") Long studentId);

    @Query("SELECT a FROM BusAssignment a WHERE a.route.id = :routeId AND a.status = 'ACTIVE' ORDER BY a.student.lastName, a.student.firstName")
    List<BusAssignment> findActiveByRoute(@Param("routeId") Long routeId);

    @Query("SELECT a FROM BusAssignment a WHERE a.morningStop.id = :stopId AND a.status = 'ACTIVE'")
    List<BusAssignment> findActiveByMorningStop(@Param("stopId") Long stopId);

    @Query("SELECT a FROM BusAssignment a WHERE a.afternoonStop.id = :stopId AND a.status = 'ACTIVE'")
    List<BusAssignment> findActiveByAfternoonStop(@Param("stopId") Long stopId);

    @Query("SELECT a FROM BusAssignment a WHERE a.status = 'ACTIVE' AND a.requiresSpecialAccommodations = true")
    List<BusAssignment> findActiveWithSpecialAccommodations();

    @Query("SELECT a FROM BusAssignment a WHERE a.status = 'PENDING' OR (a.status = 'ACTIVE' AND a.parentApprovalReceived = false)")
    List<BusAssignment> findPendingApprovals();

    @Query("SELECT COUNT(a) FROM BusAssignment a WHERE a.route.id = :routeId AND a.status = 'ACTIVE'")
    long countActiveByRoute(@Param("routeId") Long routeId);

    @Query("SELECT COUNT(a) FROM BusAssignment a WHERE a.student.id = :studentId AND a.status = 'ACTIVE'")
    long countActiveByStudent(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(a) FROM BusAssignment a WHERE a.academicYear = :year AND a.status = 'ACTIVE'")
    long countActiveByYear(@Param("year") String year);
}
