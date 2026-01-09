package com.heronix.repository;

import com.heronix.model.domain.SpecialEducationEvaluation;
import com.heronix.model.domain.SpecialEducationEvaluation.EligibilityCategory;
import com.heronix.model.domain.SpecialEducationEvaluation.EvaluationStatus;
import com.heronix.model.domain.SpecialEducationEvaluation.EvaluationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Special Education Evaluation entities
 * Provides data access for evaluations, re-evaluations, and compliance tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface SpecialEducationEvaluationRepository extends JpaRepository<SpecialEducationEvaluation, Long> {

    // Basic Queries
    List<SpecialEducationEvaluation> findByStudentId(Long studentId);

    List<SpecialEducationEvaluation> findByStudentIdOrderByReferralDateDesc(Long studentId);

    List<SpecialEducationEvaluation> findByEvaluationType(EvaluationType type);

    List<SpecialEducationEvaluation> findByStatus(EvaluationStatus status);

    // Active Evaluations
    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.status NOT IN ('COMPLETED', 'CANCELLED') " +
           "ORDER BY e.dueDate ASC")
    List<SpecialEducationEvaluation> findAllActive();

    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.student.id = :studentId " +
           "AND e.status NOT IN ('COMPLETED', 'CANCELLED') " +
           "ORDER BY e.referralDate DESC")
    List<SpecialEducationEvaluation> findActiveByStudent(@Param("studentId") Long studentId);

    // Timeline Compliance
    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.dueDate < :today " +
           "AND e.status NOT IN ('COMPLETED', 'CANCELLED') " +
           "ORDER BY e.dueDate ASC")
    List<SpecialEducationEvaluation> findOverdue(@Param("today") LocalDate today);

    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.dueDate BETWEEN :today AND :futureDate " +
           "AND e.status NOT IN ('COMPLETED', 'CANCELLED') " +
           "ORDER BY e.dueDate ASC")
    List<SpecialEducationEvaluation> findDueSoon(@Param("today") LocalDate today,
                                                   @Param("futureDate") LocalDate futureDate);

    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.status NOT IN ('COMPLETED', 'CANCELLED') " +
           "AND e.timelinePaused = false " +
           "ORDER BY e.dueDate ASC NULLS LAST")
    List<SpecialEducationEvaluation> findActiveWithTimeline();

    // Consent Tracking
    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.status = 'CONSENT_PENDING' " +
           "ORDER BY e.consentSentDate ASC")
    List<SpecialEducationEvaluation> findAwaitingConsent();

    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.status = 'CONSENT_PENDING' " +
           "AND e.consentSentDate < :cutoffDate " +
           "ORDER BY e.consentSentDate ASC")
    List<SpecialEducationEvaluation> findOverdueForConsent(@Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.parentConsentForEvaluation = false " +
           "OR e.parentConsentForEvaluation IS NULL " +
           "AND e.status NOT IN ('COMPLETED', 'CANCELLED') " +
           "ORDER BY e.referralDate ASC")
    List<SpecialEducationEvaluation> findNeedingParentConsent();

    // Assessment Progress
    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.status = 'ASSESSMENTS_IN_PROGRESS' " +
           "ORDER BY e.dueDate ASC")
    List<SpecialEducationEvaluation> findInProgress();

    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.status = 'ASSESSMENTS_COMPLETED' " +
           "OR e.status = 'REPORT_BEING_WRITTEN' " +
           "ORDER BY e.evaluationCompletedDate ASC")
    List<SpecialEducationEvaluation> findReadyForEligibilityMeeting();

    // Eligibility Meetings
    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.status = 'ELIGIBILITY_MEETING_SCHEDULED' " +
           "AND e.eligibilityMeetingDate IS NOT NULL " +
           "ORDER BY e.eligibilityMeetingDate ASC")
    List<SpecialEducationEvaluation> findWithScheduledMeetings();

    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.eligibilityMeetingDate = :date " +
           "ORDER BY e.student.lastName ASC")
    List<SpecialEducationEvaluation> findMeetingsOnDate(@Param("date") LocalDate date);

    // Eligibility Determination
    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.eligibleForServices = true " +
           "ORDER BY e.eligibilityMeetingDate DESC")
    List<SpecialEducationEvaluation> findEligibleStudents();

    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.eligibleForServices = false " +
           "ORDER BY e.eligibilityMeetingDate DESC")
    List<SpecialEducationEvaluation> findNotEligibleStudents();

    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.eligibilityCategory = :category " +
           "ORDER BY e.eligibilityMeetingDate DESC")
    List<SpecialEducationEvaluation> findByEligibilityCategory(@Param("category") EligibilityCategory category);

    // Triennial Tracking
    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.evaluationType = 'TRIENNIAL' " +
           "ORDER BY e.referralDate DESC")
    List<SpecialEducationEvaluation> findTriennials();

    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.nextTriennialDueDate IS NOT NULL " +
           "AND e.nextTriennialDueDate BETWEEN :today AND :futureDate " +
           "ORDER BY e.nextTriennialDueDate ASC")
    List<SpecialEducationEvaluation> findUpcomingTriennials(@Param("today") LocalDate today,
                                                              @Param("futureDate") LocalDate futureDate);

    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.nextTriennialDueDate < :today " +
           "ORDER BY e.nextTriennialDueDate ASC")
    List<SpecialEducationEvaluation> findOverdueTriennials(@Param("today") LocalDate today);

    // IEP Development
    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.eligibleForServices = true " +
           "AND (e.iepDeveloped = false OR e.iepDeveloped IS NULL) " +
           "ORDER BY e.eligibilityMeetingDate ASC")
    List<SpecialEducationEvaluation> findNeedingIEP();

    // Case Manager
    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.caseManager = :caseManager " +
           "AND e.status NOT IN ('COMPLETED', 'CANCELLED') " +
           "ORDER BY e.dueDate ASC")
    List<SpecialEducationEvaluation> findActiveByCaseManager(@Param("caseManager") String caseManager);

    // Interpreter Needs
    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.interpreterRequired = true " +
           "AND e.status = 'ELIGIBILITY_MEETING_SCHEDULED' " +
           "ORDER BY e.eligibilityMeetingDate ASC")
    List<SpecialEducationEvaluation> findNeedingInterpreter();

    // Compliance Reports
    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.status = 'COMPLETED' " +
           "AND (e.completedWithinTimeline = false OR e.completedWithinTimeline IS NULL) " +
           "ORDER BY e.evaluationCompletedDate DESC")
    List<SpecialEducationEvaluation> findOutOfCompliance();

    // Date Range Queries
    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.referralDate BETWEEN :startDate AND :endDate " +
           "ORDER BY e.referralDate DESC")
    List<SpecialEducationEvaluation> findByReferralDateRange(@Param("startDate") LocalDate startDate,
                                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM SpecialEducationEvaluation e WHERE e.evaluationCompletedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY e.evaluationCompletedDate DESC")
    List<SpecialEducationEvaluation> findCompletedInDateRange(@Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate);

    // Statistics
    @Query("SELECT e.evaluationType, COUNT(e) FROM SpecialEducationEvaluation e " +
           "WHERE e.referralDate BETWEEN :startDate AND :endDate " +
           "GROUP BY e.evaluationType")
    List<Object[]> countByTypeInDateRange(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT e.eligibilityCategory, COUNT(e) FROM SpecialEducationEvaluation e " +
           "WHERE e.eligibleForServices = true " +
           "AND e.eligibilityMeetingDate BETWEEN :startDate AND :endDate " +
           "GROUP BY e.eligibilityCategory")
    List<Object[]> countEligibleByCategory(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Query("SELECT e.status, COUNT(e) FROM SpecialEducationEvaluation e " +
           "GROUP BY e.status")
    List<Object[]> countByStatus();

    @Query("SELECT COUNT(e) FROM SpecialEducationEvaluation e " +
           "WHERE e.status NOT IN ('COMPLETED', 'CANCELLED')")
    Long countActive();

    @Query("SELECT COUNT(e) FROM SpecialEducationEvaluation e " +
           "WHERE e.dueDate < :today AND e.status NOT IN ('COMPLETED', 'CANCELLED')")
    Long countOverdue(@Param("today") LocalDate today);
}
