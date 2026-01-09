package com.heronix.repository;

import com.heronix.model.domain.ELLAccommodation;
import com.heronix.model.domain.ELLAccommodation.AccommodationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for ELL Accommodation entities
 * Provides data access for ELL accommodations and monitoring
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface ELLAccommodationRepository extends JpaRepository<ELLAccommodation, Long> {

    // Basic Queries
    List<ELLAccommodation> findByEllStudentId(Long ellStudentId);

    List<ELLAccommodation> findByEllStudentIdAndIsActiveTrue(Long ellStudentId);

    List<ELLAccommodation> findByCategory(AccommodationCategory category);

    // Active Accommodations
    @Query("SELECT a FROM ELLAccommodation a WHERE a.isActive = true " +
           "AND a.effectiveDate <= :today " +
           "AND (a.endDate IS NULL OR a.endDate >= :today) " +
           "ORDER BY a.ellStudent.student.lastName ASC")
    List<ELLAccommodation> findCurrentlyActive(@Param("today") LocalDate today);

    // State Testing
    @Query("SELECT a FROM ELLAccommodation a WHERE a.appliesToStateTests = true " +
           "AND a.isActive = true ORDER BY a.ellStudent.student.lastName ASC")
    List<ELLAccommodation> findStateTestingAccommodations();

    @Query("SELECT a FROM ELLAccommodation a WHERE a.appliesToStateTests = true " +
           "AND (a.stateTestingApproved = false OR a.stateTestingApproved IS NULL) " +
           "AND a.isActive = true ORDER BY a.ellStudent.student.lastName ASC")
    List<ELLAccommodation> findNeedingStateApproval();

    // Distribution
    @Query("SELECT a FROM ELLAccommodation a WHERE a.isActive = true " +
           "AND (a.distributedToTeachers = false OR a.distributedToTeachers IS NULL) " +
           "ORDER BY a.effectiveDate ASC")
    List<ELLAccommodation> findNeedingDistribution();

    // Training
    @Query("SELECT a FROM ELLAccommodation a WHERE a.requiresTeacherTraining = true " +
           "AND (a.trainingCompleted = false OR a.trainingCompleted IS NULL) " +
           "AND a.isActive = true ORDER BY a.ellStudent.student.lastName ASC")
    List<ELLAccommodation> findNeedingTraining();

    // Monitoring
    @Query("SELECT a FROM ELLAccommodation a WHERE a.isActive = true " +
           "AND (a.lastMonitoredDate IS NULL OR a.lastMonitoredDate < :cutoffDate) " +
           "ORDER BY a.lastMonitoredDate ASC NULLS FIRST")
    List<ELLAccommodation> findOverdueForMonitoring(@Param("cutoffDate") LocalDate cutoffDate);

    // Effectiveness
    @Query("SELECT a FROM ELLAccommodation a WHERE a.effectivenessRating IS NOT NULL " +
           "AND a.effectivenessRating < :minRating AND a.isActive = true " +
           "ORDER BY a.effectivenessRating ASC")
    List<ELLAccommodation> findLowEffectiveness(@Param("minRating") Integer minRating);

    @Query("SELECT a FROM ELLAccommodation a WHERE a.implementationFidelityRating IS NOT NULL " +
           "AND a.implementationFidelityRating < :minRating AND a.isActive = true " +
           "ORDER BY a.implementationFidelityRating ASC")
    List<ELLAccommodation> findLowImplementationFidelity(@Param("minRating") Integer minRating);

    // Concerns
    @Query("SELECT a FROM ELLAccommodation a WHERE a.concerns IS NOT NULL " +
           "AND a.isActive = true ORDER BY a.ellStudent.student.lastName ASC")
    List<ELLAccommodation> findWithConcerns();

    // Subject/Class Specific
    @Query("SELECT a FROM ELLAccommodation a WHERE a.ellStudent.id = :ellStudentId " +
           "AND (a.appliesToAllClasses = true OR :subject MEMBER OF a.applicableSubjects) " +
           "AND a.isActive = true ORDER BY a.category ASC")
    List<ELLAccommodation> findByStudentAndSubject(@Param("ellStudentId") Long ellStudentId,
                                                     @Param("subject") String subject);

    // Statistics
    @Query("SELECT a.category, COUNT(a) FROM ELLAccommodation a " +
           "WHERE a.ellStudent.id = :ellStudentId AND a.isActive = true " +
           "GROUP BY a.category")
    List<Object[]> countByCategoryForStudent(@Param("ellStudentId") Long ellStudentId);

    @Query("SELECT COUNT(a) FROM ELLAccommodation a " +
           "WHERE a.ellStudent.id = :ellStudentId AND a.isActive = true")
    Long countActiveByStudent(@Param("ellStudentId") Long ellStudentId);
}
