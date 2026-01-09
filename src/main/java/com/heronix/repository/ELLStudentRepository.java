package com.heronix.repository;

import com.heronix.model.domain.ELLStudent;
import com.heronix.model.domain.ELLStudent.ELLStatus;
import com.heronix.model.domain.ELLStudent.ProficiencyLevel;
import com.heronix.model.domain.ELLStudent.ProgramType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ELL Student entities
 * Provides data access for ELL identification, program placement, and monitoring
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface ELLStudentRepository extends JpaRepository<ELLStudent, Long> {

    // Basic Queries
    Optional<ELLStudent> findByStudentId(Long studentId);

    List<ELLStudent> findByEllStatus(ELLStatus status);

    List<ELLStudent> findByProficiencyLevel(ProficiencyLevel level);

    List<ELLStudent> findByProgramType(ProgramType programType);

    // Active ELL Students
    @Query("SELECT e FROM ELLStudent e WHERE e.ellStatus = 'ACTIVE' " +
           "ORDER BY e.student.lastName ASC")
    List<ELLStudent> findAllActive();

    @Query("SELECT e FROM ELLStudent e WHERE e.ellStatus IN ('ACTIVE', 'MONITORED_YEAR_1', 'MONITORED_YEAR_2', 'MONITORED_YEAR_3', 'MONITORED_YEAR_4') " +
           "ORDER BY e.student.lastName ASC")
    List<ELLStudent> findActiveAndMonitored();

    // Proficiency Level Queries
    @Query("SELECT e FROM ELLStudent e WHERE e.proficiencyLevel IN ('ENTERING', 'EMERGING') " +
           "AND e.ellStatus = 'ACTIVE' ORDER BY e.student.lastName ASC")
    List<ELLStudent> findLowProficiency();

    @Query("SELECT e FROM ELLStudent e WHERE e.proficiencyLevel IN ('EXPANDING', 'BRIDGING', 'REACHING') " +
           "AND e.ellStatus = 'ACTIVE' ORDER BY e.student.lastName ASC")
    List<ELLStudent> findHighProficiency();

    // Annual Assessment
    @Query("SELECT e FROM ELLStudent e WHERE e.annualAssessmentRequired = true " +
           "AND (e.nextAnnualAssessmentDate IS NULL OR e.nextAnnualAssessmentDate <= :today) " +
           "AND e.ellStatus = 'ACTIVE' ORDER BY e.nextAnnualAssessmentDate ASC NULLS FIRST")
    List<ELLStudent> findNeedingAnnualAssessment(@Param("today") LocalDate today);

    @Query("SELECT e FROM ELLStudent e WHERE e.nextAnnualAssessmentDate BETWEEN :today AND :futureDate " +
           "AND e.ellStatus = 'ACTIVE' ORDER BY e.nextAnnualAssessmentDate ASC")
    List<ELLStudent> findUpcomingAnnualAssessments(@Param("today") LocalDate today,
                                                     @Param("futureDate") LocalDate futureDate);

    // Progress Monitoring
    @Query("SELECT e FROM ELLStudent e WHERE e.ellStatus = 'ACTIVE' " +
           "AND (e.lastProgressMonitoringDate IS NULL OR e.lastProgressMonitoringDate < :cutoffDate) " +
           "ORDER BY e.lastProgressMonitoringDate ASC NULLS FIRST")
    List<ELLStudent> findOverdueForProgressMonitoring(@Param("cutoffDate") LocalDate cutoffDate);

    // Reclassification
    @Query("SELECT e FROM ELLStudent e WHERE e.eligibleForReclassification = true " +
           "AND e.ellStatus = 'ACTIVE' ORDER BY e.reclassificationEligibilityDate ASC")
    List<ELLStudent> findEligibleForReclassification();

    @Query("SELECT e FROM ELLStudent e WHERE e.ellStatus = 'RECLASSIFIED' " +
           "ORDER BY e.reclassificationDate DESC")
    List<ELLStudent> findReclassified();

    // Monitoring Status
    @Query("SELECT e FROM ELLStudent e WHERE e.ellStatus IN ('MONITORED_YEAR_1', 'MONITORED_YEAR_2', 'MONITORED_YEAR_3', 'MONITORED_YEAR_4') " +
           "ORDER BY e.ellStatus ASC, e.student.lastName ASC")
    List<ELLStudent> findMonitored();

    @Query("SELECT e FROM ELLStudent e WHERE e.ellStatus = :status " +
           "ORDER BY e.student.lastName ASC")
    List<ELLStudent> findByMonitoredYear(@Param("status") ELLStatus status);

    // Parent Communication
    @Query("SELECT e FROM ELLStudent e WHERE (e.parentNotificationSent = false OR e.parentNotificationSent IS NULL) " +
           "AND e.ellStatus = 'ACTIVE' ORDER BY e.identificationDate ASC")
    List<ELLStudent> findNeedingParentNotification();

    @Query("SELECT e FROM ELLStudent e WHERE e.translationServicesRequired = true " +
           "AND e.ellStatus = 'ACTIVE' ORDER BY e.student.lastName ASC")
    List<ELLStudent> findNeedingTranslation();

    @Query("SELECT e FROM ELLStudent e WHERE e.interpreterRequired = true " +
           "AND e.ellStatus = 'ACTIVE' ORDER BY e.student.lastName ASC")
    List<ELLStudent> findNeedingInterpreter();

    // Service Provider
    @Query("SELECT e FROM ELLStudent e WHERE e.serviceProvider.id = :providerId " +
           "AND e.ellStatus = 'ACTIVE' ORDER BY e.student.lastName ASC")
    List<ELLStudent> findByServiceProvider(@Param("providerId") Long providerId);

    // Home Language Survey
    @Query("SELECT e FROM ELLStudent e WHERE (e.homeLanguageSurveyCompleted = false OR e.homeLanguageSurveyCompleted IS NULL) " +
           "ORDER BY e.identificationDate ASC")
    List<ELLStudent> findNeedingHomeLanguageSurvey();

    // Language Queries
    @Query("SELECT e FROM ELLStudent e WHERE e.nativeLanguage = :language " +
           "AND e.ellStatus IN ('ACTIVE', 'MONITORED_YEAR_1', 'MONITORED_YEAR_2', 'MONITORED_YEAR_3', 'MONITORED_YEAR_4') " +
           "ORDER BY e.student.lastName ASC")
    List<ELLStudent> findByNativeLanguage(@Param("language") String language);

    @Query("SELECT e.nativeLanguage, COUNT(e) FROM ELLStudent e " +
           "WHERE e.ellStatus = 'ACTIVE' AND e.nativeLanguage IS NOT NULL " +
           "GROUP BY e.nativeLanguage ORDER BY COUNT(e) DESC")
    List<Object[]> countByNativeLanguage();

    // Program Type
    @Query("SELECT e FROM ELLStudent e WHERE e.programType = :programType " +
           "AND e.ellStatus = 'ACTIVE' ORDER BY e.student.lastName ASC")
    List<ELLStudent> findActiveByProgramType(@Param("programType") ProgramType programType);

    @Query("SELECT e.programType, COUNT(e) FROM ELLStudent e " +
           "WHERE e.ellStatus = 'ACTIVE' GROUP BY e.programType")
    List<Object[]> countByProgramType();

    // Title III
    @Query("SELECT e FROM ELLStudent e WHERE e.titleIIIEligible = true " +
           "AND e.ellStatus = 'ACTIVE' ORDER BY e.student.lastName ASC")
    List<ELLStudent> findTitleIIIEligible();

    @Query("SELECT e FROM ELLStudent e WHERE e.titleIIIFunded = true " +
           "AND e.ellStatus = 'ACTIVE' ORDER BY e.student.lastName ASC")
    List<ELLStudent> findTitleIIIFunded();

    // Newcomers
    @Query("SELECT e FROM ELLStudent e WHERE e.enrollmentInUSSchoolsDate >= :cutoffDate " +
           "AND e.ellStatus = 'ACTIVE' ORDER BY e.enrollmentInUSSchoolsDate DESC")
    List<ELLStudent> findNewcomers(@Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT e FROM ELLStudent e WHERE e.yearsInUSSchools IS NOT NULL " +
           "AND e.yearsInUSSchools < :years AND e.ellStatus = 'ACTIVE' " +
           "ORDER BY e.yearsInUSSchools ASC")
    List<ELLStudent> findByYearsInUSSchools(@Param("years") Integer years);

    // Statistics
    @Query("SELECT COUNT(e) FROM ELLStudent e WHERE e.ellStatus = 'ACTIVE'")
    Long countActive();

    @Query("SELECT e.ellStatus, COUNT(e) FROM ELLStudent e GROUP BY e.ellStatus")
    List<Object[]> countByStatus();

    @Query("SELECT e.proficiencyLevel, COUNT(e) FROM ELLStudent e " +
           "WHERE e.ellStatus = 'ACTIVE' GROUP BY e.proficiencyLevel")
    List<Object[]> countByProficiencyLevel();

    // Date Range Queries
    @Query("SELECT e FROM ELLStudent e WHERE e.identificationDate BETWEEN :startDate AND :endDate " +
           "ORDER BY e.identificationDate DESC")
    List<ELLStudent> findByIdentificationDateRange(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM ELLStudent e WHERE e.reclassificationDate BETWEEN :startDate AND :endDate " +
           "ORDER BY e.reclassificationDate DESC")
    List<ELLStudent> findReclassifiedInDateRange(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
}
