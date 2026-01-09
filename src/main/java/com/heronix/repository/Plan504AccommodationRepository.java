package com.heronix.repository;

import com.heronix.model.domain.Plan504Accommodation;
import com.heronix.model.domain.Plan504Accommodation.AccommodationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for 504 Plan Accommodation entities
 * Provides data access for 504 accommodations and monitoring
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface Plan504AccommodationRepository extends JpaRepository<Plan504Accommodation, Long> {

    // Basic Queries
    List<Plan504Accommodation> findByPlan504Id(Long plan504Id);

    List<Plan504Accommodation> findByPlan504IdAndIsActiveTrue(Long plan504Id);

    List<Plan504Accommodation> findByCategory(AccommodationCategory category);

    // Student Accommodations
    @Query("SELECT a FROM Plan504Accommodation a WHERE a.plan504.student.id = :studentId " +
           "AND a.isActive = true ORDER BY a.category ASC")
    List<Plan504Accommodation> findActiveByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT a FROM Plan504Accommodation a WHERE a.plan504.student.id = :studentId " +
           "ORDER BY a.category ASC")
    List<Plan504Accommodation> findByStudentId(@Param("studentId") Long studentId);

    // Subject/Class Specific
    @Query("SELECT a FROM Plan504Accommodation a WHERE a.plan504.id = :plan504Id " +
           "AND (a.appliesToAllClasses = true OR :subject MEMBER OF a.applicableSubjects) " +
           "AND a.isActive = true ORDER BY a.category ASC")
    List<Plan504Accommodation> findByPlan504IdAndSubject(@Param("plan504Id") Long plan504Id,
                                                           @Param("subject") String subject);

    // State Testing
    @Query("SELECT a FROM Plan504Accommodation a WHERE a.plan504.student.id = :studentId " +
           "AND a.appliesToStateTests = true AND a.isActive = true " +
           "ORDER BY a.category ASC")
    List<Plan504Accommodation> findStateTestingAccommodations(@Param("studentId") Long studentId);

    // Distribution Tracking
    @Query("SELECT a FROM Plan504Accommodation a WHERE a.isActive = true " +
           "AND (a.distributedToTeachers = false OR a.distributedToTeachers IS NULL) " +
           "ORDER BY a.plan504.student.lastName ASC")
    List<Plan504Accommodation> findNeedingDistribution();

    @Query("SELECT a FROM Plan504Accommodation a WHERE a.plan504.id = :plan504Id " +
           "AND a.distributedToTeachers = true " +
           "ORDER BY a.distributionDate DESC")
    List<Plan504Accommodation> findDistributedByPlan(@Param("plan504Id") Long plan504Id);

    // Training Needs
    @Query("SELECT a FROM Plan504Accommodation a WHERE a.requiresTeacherTraining = true " +
           "AND (a.trainingCompleted = false OR a.trainingCompleted IS NULL) " +
           "AND a.isActive = true " +
           "ORDER BY a.plan504.student.lastName ASC")
    List<Plan504Accommodation> findNeedingTraining();

    // Monitoring
    @Query("SELECT a FROM Plan504Accommodation a WHERE a.isActive = true " +
           "AND (a.lastMonitoredDate IS NULL OR a.lastMonitoredDate < :cutoffDate) " +
           "ORDER BY a.lastMonitoredDate ASC NULLS FIRST")
    List<Plan504Accommodation> findOverdueForMonitoring(@Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT a FROM Plan504Accommodation a WHERE a.plan504.id = :plan504Id " +
           "AND a.lastMonitoredDate IS NOT NULL " +
           "ORDER BY a.lastMonitoredDate DESC")
    List<Plan504Accommodation> findMonitoredByPlan(@Param("plan504Id") Long plan504Id);

    // Effectiveness
    @Query("SELECT a FROM Plan504Accommodation a WHERE a.effectivenessRating IS NOT NULL " +
           "AND a.effectivenessRating < :minRating AND a.isActive = true " +
           "ORDER BY a.effectivenessRating ASC")
    List<Plan504Accommodation> findLowEffectiveness(@Param("minRating") Integer minRating);

    // Concerns
    @Query("SELECT a FROM Plan504Accommodation a WHERE a.concerns IS NOT NULL " +
           "AND a.isActive = true " +
           "ORDER BY a.plan504.student.lastName ASC")
    List<Plan504Accommodation> findWithConcerns();

    // Active Status
    @Query("SELECT a FROM Plan504Accommodation a WHERE a.isActive = true " +
           "AND a.effectiveDate <= :today " +
           "AND (a.endDate IS NULL OR a.endDate >= :today) " +
           "ORDER BY a.plan504.student.lastName ASC")
    List<Plan504Accommodation> findCurrentlyActive(@Param("today") LocalDate today);

    // Statistics
    @Query("SELECT a.category, COUNT(a) FROM Plan504Accommodation a " +
           "WHERE a.plan504.id = :plan504Id AND a.isActive = true " +
           "GROUP BY a.category")
    List<Object[]> countByCategory(@Param("plan504Id") Long plan504Id);

    @Query("SELECT COUNT(a) FROM Plan504Accommodation a " +
           "WHERE a.plan504.id = :plan504Id AND a.isActive = true")
    Long countActiveByPlan(@Param("plan504Id") Long plan504Id);
}
