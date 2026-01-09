package com.heronix.repository;

import com.heronix.model.domain.GiftedEducationPlan;
import com.heronix.model.domain.GiftedEducationPlan.PlanStatus;
import com.heronix.model.domain.GiftedStudent.GiftedArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Gifted Education Plan (GEP) entities
 * Provides data access for GEP creation, tracking, and compliance
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface GiftedEducationPlanRepository extends JpaRepository<GiftedEducationPlan, Long> {

    // Basic Queries
    List<GiftedEducationPlan> findByGiftedStudentId(Long giftedStudentId);

    Optional<GiftedEducationPlan> findByPlanNumber(String planNumber);

    List<GiftedEducationPlan> findByStatus(PlanStatus status);

    List<GiftedEducationPlan> findBySchoolYear(String schoolYear);

    // Active Plans
    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.status = 'ACTIVE' " +
           "AND :today BETWEEN gep.startDate AND gep.endDate " +
           "ORDER BY gep.giftedStudent.student.lastName ASC")
    List<GiftedEducationPlan> findActivePlans(@Param("today") LocalDate today);

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.status = 'ACTIVE' " +
           "ORDER BY gep.startDate DESC")
    List<GiftedEducationPlan> findByActiveStatus();

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.status IN ('ACTIVE', 'UNDER_REVIEW') " +
           "ORDER BY gep.startDate DESC")
    List<GiftedEducationPlan> findActiveAndUnderReview();

    // Plan Status
    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.status = 'DRAFT' " +
           "ORDER BY gep.createdAt DESC")
    List<GiftedEducationPlan> findDrafts();

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.status = 'PENDING_APPROVAL' " +
           "ORDER BY gep.updatedAt ASC")
    List<GiftedEducationPlan> findPendingApproval();

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.status = 'UNDER_REVIEW' " +
           "ORDER BY gep.lastProgressReviewDate ASC NULLS FIRST")
    List<GiftedEducationPlan> findUnderReview();

    // Expiration and Review
    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.endDate <= :today " +
           "AND gep.status = 'ACTIVE' ORDER BY gep.endDate ASC")
    List<GiftedEducationPlan> findExpired(@Param("today") LocalDate today);

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.endDate BETWEEN :today AND :futureDate " +
           "AND gep.status = 'ACTIVE' ORDER BY gep.endDate ASC")
    List<GiftedEducationPlan> findExpiringSoon(@Param("today") LocalDate today,
                                                 @Param("futureDate") LocalDate futureDate);

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE " +
           "(gep.nextReviewDate IS NULL OR gep.nextReviewDate <= :today) " +
           "AND gep.status = 'ACTIVE' ORDER BY gep.nextReviewDate ASC NULLS FIRST")
    List<GiftedEducationPlan> findDueForReview(@Param("today") LocalDate today);

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.nextReviewDate BETWEEN :today AND :futureDate " +
           "AND gep.status = 'ACTIVE' ORDER BY gep.nextReviewDate ASC")
    List<GiftedEducationPlan> findUpcomingReviews(@Param("today") LocalDate today,
                                                    @Param("futureDate") LocalDate futureDate);

    // Progress Monitoring
    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.status = 'ACTIVE' " +
           "AND (gep.lastProgressReviewDate IS NULL OR gep.lastProgressReviewDate < :cutoffDate) " +
           "ORDER BY gep.lastProgressReviewDate ASC NULLS FIRST")
    List<GiftedEducationPlan> findOverdueForProgressReview(@Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.progressSummary IS NULL " +
           "AND gep.status = 'ACTIVE' AND gep.lastProgressReviewDate IS NOT NULL " +
           "ORDER BY gep.lastProgressReviewDate ASC")
    List<GiftedEducationPlan> findNeedingProgressDocumentation();

    // Annual Review
    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.annualReviewCompleted = false " +
           "AND gep.status = 'ACTIVE' AND :today > gep.startDate + 1 YEAR " +
           "ORDER BY gep.startDate ASC")
    List<GiftedEducationPlan> findNeedingAnnualReview(@Param("today") LocalDate today);

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.annualReviewDate IS NOT NULL " +
           "ORDER BY gep.annualReviewDate DESC")
    List<GiftedEducationPlan> findWithAnnualReview();

    // Parent Involvement
    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE (gep.parentConsentReceived = false " +
           "OR gep.parentConsentReceived IS NULL) AND gep.status IN ('PENDING_APPROVAL', 'ACTIVE') " +
           "ORDER BY gep.createdAt ASC")
    List<GiftedEducationPlan> findNeedingParentConsent();

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.parentConferenceHeld = false " +
           "AND gep.status = 'ACTIVE' ORDER BY gep.startDate ASC")
    List<GiftedEducationPlan> findNeedingParentConference();

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.parentInput IS NULL " +
           "AND gep.status IN ('DRAFT', 'PENDING_APPROVAL') ORDER BY gep.createdAt ASC")
    List<GiftedEducationPlan> findNeedingParentInput();

    // Case Manager
    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.caseManager.id = :caseManagerId " +
           "AND gep.status = 'ACTIVE' ORDER BY gep.giftedStudent.student.lastName ASC")
    List<GiftedEducationPlan> findByCaseManager(@Param("caseManagerId") Long caseManagerId);

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.caseManagerName = :caseManagerName " +
           "AND gep.status = 'ACTIVE' ORDER BY gep.giftedStudent.student.lastName ASC")
    List<GiftedEducationPlan> findByCaseManagerName(@Param("caseManagerName") String caseManagerName);

    // Gifted Areas
    @Query("SELECT gep FROM GiftedEducationPlan gep JOIN gep.areasAddressed aa " +
           "WHERE aa = :area AND gep.status = 'ACTIVE' " +
           "ORDER BY gep.giftedStudent.student.lastName ASC")
    List<GiftedEducationPlan> findByAreaAddressed(@Param("area") GiftedArea area);

    // Goals and Strategies
    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE SIZE(gep.goals) = 0 " +
           "AND gep.status IN ('DRAFT', 'PENDING_APPROVAL') ORDER BY gep.createdAt ASC")
    List<GiftedEducationPlan> findWithoutGoals();

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE SIZE(gep.instructionalStrategies) = 0 " +
           "AND gep.status IN ('DRAFT', 'PENDING_APPROVAL') ORDER BY gep.createdAt ASC")
    List<GiftedEducationPlan> findWithoutStrategies();

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE SIZE(gep.goals) >= :minGoals " +
           "AND gep.status = 'ACTIVE' ORDER BY SIZE(gep.goals) DESC")
    List<GiftedEducationPlan> findByMinGoalCount(@Param("minGoals") Integer minGoals);

    // Services
    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.pullOutServicesMinutes IS NOT NULL " +
           "AND gep.pullOutServicesMinutes > 0 AND gep.status = 'ACTIVE' " +
           "ORDER BY gep.pullOutServicesMinutes DESC")
    List<GiftedEducationPlan> findWithPullOutServices();

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE SIZE(gep.plannedAdvancedCourses) > 0 " +
           "AND gep.status = 'ACTIVE' ORDER BY SIZE(gep.plannedAdvancedCourses) DESC")
    List<GiftedEducationPlan> findWithAdvancedCourses();

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.gradeAccelerationPlan IS NOT NULL " +
           "AND gep.status = 'ACTIVE' ORDER BY gep.giftedStudent.student.lastName ASC")
    List<GiftedEducationPlan> findWithGradeAcceleration();

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.subjectAccelerationPlan IS NOT NULL " +
           "AND gep.status = 'ACTIVE' ORDER BY gep.giftedStudent.student.lastName ASC")
    List<GiftedEducationPlan> findWithSubjectAcceleration();

    // Student Query
    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.giftedStudent.student.id = :studentId " +
           "ORDER BY gep.startDate DESC")
    List<GiftedEducationPlan> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.giftedStudent.id = :giftedStudentId " +
           "AND gep.status = 'ACTIVE' ORDER BY gep.startDate DESC")
    List<GiftedEducationPlan> findActiveByGiftedStudent(@Param("giftedStudentId") Long giftedStudentId);

    // Date Range Queries
    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE gep.startDate BETWEEN :startDate AND :endDate " +
           "ORDER BY gep.startDate DESC")
    List<GiftedEducationPlan> findByStartDateRange(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT gep FROM GiftedEducationPlan gep WHERE :date BETWEEN gep.startDate AND gep.endDate " +
           "AND gep.status = 'ACTIVE' ORDER BY gep.startDate ASC")
    List<GiftedEducationPlan> findActiveOnDate(@Param("date") LocalDate date);

    // Statistics
    @Query("SELECT COUNT(gep) FROM GiftedEducationPlan gep WHERE gep.status = 'ACTIVE'")
    Long countActive();

    @Query("SELECT gep.status, COUNT(gep) FROM GiftedEducationPlan gep GROUP BY gep.status")
    List<Object[]> countByStatus();

    @Query("SELECT gep.schoolYear, COUNT(gep) FROM GiftedEducationPlan gep " +
           "WHERE gep.schoolYear IS NOT NULL GROUP BY gep.schoolYear ORDER BY gep.schoolYear DESC")
    List<Object[]> countBySchoolYear();

    @Query("SELECT AVG(gep.pullOutServicesMinutes) FROM GiftedEducationPlan gep " +
           "WHERE gep.pullOutServicesMinutes IS NOT NULL AND gep.status = 'ACTIVE'")
    Double getAverageServiceMinutes();
}
