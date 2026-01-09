package com.heronix.repository;

import com.heronix.model.domain.ELLService;
import com.heronix.model.domain.ELLService.FocusArea;
import com.heronix.model.domain.ELLService.ServiceStatus;
import com.heronix.model.domain.ELLService.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for ELL Service entities
 * Provides data access for ELL service delivery and documentation
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface ELLServiceRepository extends JpaRepository<ELLService, Long> {

    // Basic Queries
    List<ELLService> findByEllStudentId(Long ellStudentId);

    List<ELLService> findByEllStudentIdOrderByServiceDateDesc(Long ellStudentId);

    List<ELLService> findByServiceType(ServiceType type);

    List<ELLService> findByStatus(ServiceStatus status);

    // Date Queries
    @Query("SELECT s FROM ELLService s WHERE s.serviceDate = :date " +
           "AND s.status IN ('SCHEDULED', 'COMPLETED') " +
           "ORDER BY s.serviceProvider.lastName ASC")
    List<ELLService> findByServiceDate(@Param("date") LocalDate date);

    @Query("SELECT s FROM ELLService s WHERE s.serviceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.serviceDate ASC")
    List<ELLService> findByDateRange(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    // Service Provider
    @Query("SELECT s FROM ELLService s WHERE s.serviceProvider.id = :providerId " +
           "AND s.status IN ('SCHEDULED', 'COMPLETED') " +
           "ORDER BY s.serviceDate DESC")
    List<ELLService> findByServiceProvider(@Param("providerId") Long providerId);

    @Query("SELECT s FROM ELLService s WHERE s.serviceProvider.id = :providerId " +
           "AND s.serviceDate = :date ORDER BY s.durationMinutes DESC")
    List<ELLService> findByProviderAndDate(@Param("providerId") Long providerId,
                                            @Param("date") LocalDate date);

    // Attendance
    @Query("SELECT s FROM ELLService s WHERE s.status = 'COMPLETED' " +
           "AND s.studentAttended = true " +
           "AND s.ellStudent.id = :ellStudentId " +
           "ORDER BY s.serviceDate DESC")
    List<ELLService> findAttendedByStudent(@Param("ellStudentId") Long ellStudentId);

    @Query("SELECT s FROM ELLService s WHERE s.status IN ('NO_SHOW', 'COMPLETED') " +
           "AND (s.studentAttended = false OR s.studentAttended IS NULL) " +
           "AND s.ellStudent.id = :ellStudentId " +
           "ORDER BY s.serviceDate DESC")
    List<ELLService> findMissedByStudent(@Param("ellStudentId") Long ellStudentId);

    // Upcoming Services
    @Query("SELECT s FROM ELLService s WHERE s.status = 'SCHEDULED' " +
           "AND s.serviceDate >= :today ORDER BY s.serviceDate ASC")
    List<ELLService> findUpcoming(@Param("today") LocalDate today);

    @Query("SELECT s FROM ELLService s WHERE s.status = 'SCHEDULED' " +
           "AND s.serviceDate < :today ORDER BY s.serviceDate ASC")
    List<ELLService> findPastDue(@Param("today") LocalDate today);

    // Documentation
    @Query("SELECT s FROM ELLService s WHERE s.status = 'COMPLETED' " +
           "AND s.progressNotes IS NULL ORDER BY s.serviceDate ASC")
    List<ELLService> findNeedingDocumentation();

    // Title III
    @Query("SELECT s FROM ELLService s WHERE s.titleIIIFunded = true " +
           "AND s.serviceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.serviceDate ASC")
    List<ELLService> findTitleIIIFunded(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM ELLService s WHERE s.titleIIIFunded = true " +
           "AND (s.reportedForCompliance = false OR s.reportedForCompliance IS NULL) " +
           "ORDER BY s.serviceDate ASC")
    List<ELLService> findNeedingComplianceReporting();

    // Service Minutes
    @Query("SELECT SUM(s.durationMinutes) FROM ELLService s " +
           "WHERE s.ellStudent.id = :ellStudentId " +
           "AND s.status = 'COMPLETED' AND s.studentAttended = true " +
           "AND s.serviceDate BETWEEN :startDate AND :endDate")
    Integer sumMinutesByStudent(@Param("ellStudentId") Long ellStudentId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    // Focus Area
    @Query("SELECT s FROM ELLService s WHERE s.focusArea = :focusArea " +
           "AND s.ellStudent.id = :ellStudentId " +
           "ORDER BY s.serviceDate DESC")
    List<ELLService> findByStudentAndFocusArea(@Param("ellStudentId") Long ellStudentId,
                                                 @Param("focusArea") FocusArea focusArea);

    // Statistics
    @Query("SELECT s.serviceType, COUNT(s) FROM ELLService s " +
           "WHERE s.serviceDate BETWEEN :startDate AND :endDate " +
           "AND s.status = 'COMPLETED' GROUP BY s.serviceType")
    List<Object[]> countByTypeInDateRange(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(s) FROM ELLService s " +
           "WHERE s.ellStudent.id = :ellStudentId AND s.status = 'COMPLETED' " +
           "AND s.studentAttended = true")
    Long countAttendedByStudent(@Param("ellStudentId") Long ellStudentId);
}
