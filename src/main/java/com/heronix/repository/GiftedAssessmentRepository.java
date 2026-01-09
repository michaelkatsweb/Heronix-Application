package com.heronix.repository;

import com.heronix.model.domain.GiftedAssessment;
import com.heronix.model.domain.GiftedAssessment.AssessmentPurpose;
import com.heronix.model.domain.GiftedAssessment.AssessmentType;
import com.heronix.model.domain.GiftedStudent.GiftedArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Gifted Assessment entities
 * Provides data access for gifted screening, evaluation, and assessment tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface GiftedAssessmentRepository extends JpaRepository<GiftedAssessment, Long> {

    // Basic Queries
    List<GiftedAssessment> findByGiftedStudentId(Long giftedStudentId);

    List<GiftedAssessment> findByAssessmentType(AssessmentType type);

    List<GiftedAssessment> findByAssessmentPurpose(AssessmentPurpose purpose);

    // Assessment Completion
    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.resultsReceivedDate IS NULL " +
           "AND ga.assessmentDate <= :today ORDER BY ga.assessmentDate ASC")
    List<GiftedAssessment> findPendingResults(@Param("today") LocalDate today);

    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.validScore = true " +
           "ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findValidAssessments();

    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.validScore = false " +
           "ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findInvalidatedAssessments();

    // Eligibility
    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.meetsEligibilityCriteria = true " +
           "ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findMeetingEligibility();

    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.recommendedForServices = true " +
           "ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findRecommendedForServices();

    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.recommendedArea = :area " +
           "ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findByRecommendedArea(@Param("area") GiftedArea area);

    // IQ Assessments
    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.fullScaleIq IS NOT NULL " +
           "ORDER BY ga.fullScaleIq DESC")
    List<GiftedAssessment> findWithIQScores();

    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.fullScaleIq >= :minIq " +
           "AND ga.validScore = true ORDER BY ga.fullScaleIq DESC")
    List<GiftedAssessment> findByMinIQ(@Param("minIq") Integer minIq);

    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.fullScaleIq >= 130 " +
           "AND ga.validScore = true ORDER BY ga.fullScaleIq DESC")
    List<GiftedAssessment> findHighlyGifted();

    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.fullScaleIq >= 145 " +
           "AND ga.validScore = true ORDER BY ga.fullScaleIq DESC")
    List<GiftedAssessment> findExceptionallyGifted();

    // Achievement Assessments
    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.readingPercentile >= :minPercentile " +
           "AND ga.validScore = true ORDER BY ga.readingPercentile DESC")
    List<GiftedAssessment> findHighReadingAchievement(@Param("minPercentile") Integer minPercentile);

    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.mathPercentile >= :minPercentile " +
           "AND ga.validScore = true ORDER BY ga.mathPercentile DESC")
    List<GiftedAssessment> findHighMathAchievement(@Param("minPercentile") Integer minPercentile);

    @Query("SELECT ga FROM GiftedAssessment ga WHERE (ga.readingScore IS NOT NULL " +
           "OR ga.mathScore IS NOT NULL OR ga.writingScore IS NOT NULL OR ga.scienceScore IS NOT NULL) " +
           "ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findWithAchievementScores();

    // Creativity Assessments
    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.creativityPercentile >= :minPercentile " +
           "AND ga.validScore = true ORDER BY ga.creativityPercentile DESC")
    List<GiftedAssessment> findHighCreativity(@Param("minPercentile") Integer minPercentile);

    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.creativityScore IS NOT NULL " +
           "ORDER BY ga.creativityScore DESC")
    List<GiftedAssessment> findWithCreativityScores();

    // Parent Notification
    @Query("SELECT ga FROM GiftedAssessment ga WHERE (ga.parentNotificationSent = false " +
           "OR ga.parentNotificationSent IS NULL) AND ga.resultsReceivedDate IS NOT NULL " +
           "ORDER BY ga.resultsReceivedDate ASC")
    List<GiftedAssessment> findNeedingParentNotification();

    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.parentNotificationDate IS NOT NULL " +
           "ORDER BY ga.parentNotificationDate DESC")
    List<GiftedAssessment> findWithParentNotification();

    // Assessment Type Queries
    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.assessmentType = :type " +
           "AND ga.validScore = true ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findValidByType(@Param("type") AssessmentType type);

    @Query("SELECT ga.assessmentType, COUNT(ga) FROM GiftedAssessment ga " +
           "WHERE ga.validScore = true GROUP BY ga.assessmentType")
    List<Object[]> countByAssessmentType();

    // Assessment Purpose Queries
    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.assessmentPurpose = :purpose " +
           "ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findByPurpose(@Param("purpose") AssessmentPurpose purpose);

    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.assessmentPurpose = 'INITIAL_SCREENING' " +
           "ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findScreeningAssessments();

    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.assessmentPurpose = 'COMPREHENSIVE_EVALUATION' " +
           "ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findComprehensiveEvaluations();

    // Administrator
    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.administrator.id = :administratorId " +
           "ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findByAdministrator(@Param("administratorId") Long administratorId);

    // Date Range Queries
    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.assessmentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findByAssessmentDateRange(@Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.resultsReceivedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ga.resultsReceivedDate DESC")
    List<GiftedAssessment> findByResultsDateRange(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    // Recent Assessments
    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.assessmentDate >= :cutoffDate " +
           "AND ga.validScore = true ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findRecentAssessments(@Param("cutoffDate") LocalDate cutoffDate);

    // Student Query
    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.giftedStudent.id = :giftedStudentId " +
           "AND ga.validScore = true ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findValidByGiftedStudent(@Param("giftedStudentId") Long giftedStudentId);

    @Query("SELECT ga FROM GiftedAssessment ga WHERE ga.giftedStudent.student.id = :studentId " +
           "ORDER BY ga.assessmentDate DESC")
    List<GiftedAssessment> findByStudentId(@Param("studentId") Long studentId);

    // Statistics
    @Query("SELECT COUNT(ga) FROM GiftedAssessment ga WHERE ga.validScore = true")
    Long countValidAssessments();

    @Query("SELECT AVG(ga.fullScaleIq) FROM GiftedAssessment ga " +
           "WHERE ga.fullScaleIq IS NOT NULL AND ga.validScore = true")
    Double getAverageIQ();

    @Query("SELECT AVG(ga.percentileRank) FROM GiftedAssessment ga " +
           "WHERE ga.percentileRank IS NOT NULL AND ga.validScore = true")
    Double getAveragePercentile();

    @Query("SELECT ga.assessmentPurpose, COUNT(ga) FROM GiftedAssessment ga " +
           "WHERE ga.assessmentPurpose IS NOT NULL GROUP BY ga.assessmentPurpose")
    List<Object[]> countByPurpose();
}
