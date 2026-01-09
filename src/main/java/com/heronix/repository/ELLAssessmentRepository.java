package com.heronix.repository;

import com.heronix.model.domain.ELLAssessment;
import com.heronix.model.domain.ELLAssessment.AssessmentPurpose;
import com.heronix.model.domain.ELLAssessment.AssessmentType;
import com.heronix.model.domain.ELLStudent.ProficiencyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for ELL Assessment entities
 * Provides data access for language proficiency assessments and progress tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface ELLAssessmentRepository extends JpaRepository<ELLAssessment, Long> {

    // Basic Queries
    List<ELLAssessment> findByEllStudentId(Long ellStudentId);

    List<ELLAssessment> findByEllStudentIdOrderByAssessmentDateDesc(Long ellStudentId);

    List<ELLAssessment> findByEllStudent(com.heronix.model.domain.ELLStudent ellStudent);

    List<ELLAssessment> findByAssessmentType(AssessmentType type);

    List<ELLAssessment> findByAssessmentPurpose(AssessmentPurpose purpose);

    // Annual Assessments
    @Query("SELECT a FROM ELLAssessment a WHERE a.assessmentPurpose = 'ANNUAL_PROFICIENCY' " +
           "ORDER BY a.assessmentDate DESC")
    List<ELLAssessment> findAnnualAssessments();

    @Query("SELECT a FROM ELLAssessment a WHERE a.assessmentPurpose = 'ANNUAL_PROFICIENCY' " +
           "AND a.schoolYear = :schoolYear ORDER BY a.ellStudent.student.lastName ASC")
    List<ELLAssessment> findAnnualAssessmentsBySchoolYear(@Param("schoolYear") String schoolYear);

    // Results Pending
    @Query("SELECT a FROM ELLAssessment a WHERE a.resultsReceivedDate IS NULL " +
           "AND a.assessmentDate < :cutoffDate ORDER BY a.assessmentDate ASC")
    List<ELLAssessment> findPendingResults(@Param("cutoffDate") LocalDate cutoffDate);

    // Parent Notification
    @Query("SELECT a FROM ELLAssessment a WHERE (a.parentNotificationSent = false OR a.parentNotificationSent IS NULL) " +
           "AND a.resultsReceivedDate IS NOT NULL ORDER BY a.resultsReceivedDate ASC")
    List<ELLAssessment> findNeedingParentNotification();

    // Reclassification
    @Query("SELECT a FROM ELLAssessment a WHERE a.meetsReclassificationCriteria = true " +
           "ORDER BY a.assessmentDate DESC")
    List<ELLAssessment> findMeetingReclassificationCriteria();

    @Query("SELECT a FROM ELLAssessment a WHERE a.assessmentPurpose = 'RECLASSIFICATION' " +
           "ORDER BY a.assessmentDate DESC")
    List<ELLAssessment> findReclassificationAssessments();

    // Proficiency Levels
    @Query("SELECT a FROM ELLAssessment a WHERE a.overallPerformanceLevel = :level " +
           "AND a.assessmentPurpose = 'ANNUAL_PROFICIENCY' " +
           "ORDER BY a.assessmentDate DESC")
    List<ELLAssessment> findByProficiencyLevel(@Param("level") ProficiencyLevel level);

    // Growth Tracking
    @Query("SELECT a FROM ELLAssessment a WHERE a.growthFromPreviousYear IS NOT NULL " +
           "AND a.growthFromPreviousYear > 0 ORDER BY a.growthFromPreviousYear DESC")
    List<ELLAssessment> findShowingGrowth();

    @Query("SELECT a FROM ELLAssessment a WHERE a.metGrowthTarget = true " +
           "AND a.schoolYear = :schoolYear ORDER BY a.ellStudent.student.lastName ASC")
    List<ELLAssessment> findMetGrowthTarget(@Param("schoolYear") String schoolYear);

    // Date Queries
    @Query("SELECT a FROM ELLAssessment a WHERE a.assessmentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.assessmentDate DESC")
    List<ELLAssessment> findByDateRange(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    // Testing Window
    @Query("SELECT a FROM ELLAssessment a WHERE a.testingWindowStart = :windowStart " +
           "ORDER BY a.ellStudent.student.lastName ASC")
    List<ELLAssessment> findByTestingWindow(@Param("windowStart") LocalDate windowStart);

    // State Reporting
    @Query("SELECT a FROM ELLAssessment a WHERE (a.stateReported = false OR a.stateReported IS NULL) " +
           "AND a.validScore = true AND a.resultsReceivedDate IS NOT NULL " +
           "ORDER BY a.assessmentDate ASC")
    List<ELLAssessment> findNeedingStateReporting();

    // Student Queries
    @Query("SELECT a FROM ELLAssessment a WHERE a.ellStudent.student.id = :studentId " +
           "ORDER BY a.assessmentDate DESC")
    List<ELLAssessment> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT a FROM ELLAssessment a WHERE a.ellStudent.student.id = :studentId " +
           "AND a.assessmentPurpose = 'ANNUAL_PROFICIENCY' " +
           "ORDER BY a.assessmentDate DESC")
    List<ELLAssessment> findAnnualAssessmentsByStudent(@Param("studentId") Long studentId);

    // Latest Assessment
    @Query("SELECT a FROM ELLAssessment a WHERE a.ellStudent.id = :ellStudentId " +
           "AND a.validScore = true ORDER BY a.assessmentDate DESC")
    List<ELLAssessment> findLatestByELLStudent(@Param("ellStudentId") Long ellStudentId);

    // Statistics
    @Query("SELECT a.assessmentType, COUNT(a) FROM ELLAssessment a " +
           "WHERE a.assessmentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY a.assessmentType")
    List<Object[]> countByTypeInDateRange(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT a.overallPerformanceLevel, COUNT(a) FROM ELLAssessment a " +
           "WHERE a.schoolYear = :schoolYear AND a.assessmentPurpose = 'ANNUAL_PROFICIENCY' " +
           "GROUP BY a.overallPerformanceLevel")
    List<Object[]> countByProficiencyLevelInSchoolYear(@Param("schoolYear") String schoolYear);

    @Query("SELECT AVG(a.compositeScore) FROM ELLAssessment a " +
           "WHERE a.schoolYear = :schoolYear AND a.compositeScore IS NOT NULL")
    Double getAverageCompositeScore(@Param("schoolYear") String schoolYear);
}
