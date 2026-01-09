package com.heronix.repository;

import com.heronix.model.domain.GiftedStudent;
import com.heronix.model.domain.GiftedStudent.GiftedArea;
import com.heronix.model.domain.GiftedStudent.GiftedStatus;
import com.heronix.model.domain.GiftedStudent.ProgramType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Gifted Student entities
 * Provides data access for gifted identification, program placement, and monitoring
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface GiftedStudentRepository extends JpaRepository<GiftedStudent, Long> {

    // Basic Queries
    Optional<GiftedStudent> findByStudentId(Long studentId);

    List<GiftedStudent> findByGiftedStatus(GiftedStatus status);

    List<GiftedStudent> findByPrimaryGiftedArea(GiftedArea area);

    List<GiftedStudent> findByProgramType(ProgramType programType);

    // Active Gifted Students
    @Query("SELECT g FROM GiftedStudent g WHERE g.giftedStatus = 'ACTIVE' " +
           "ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findAllActive();

    @Query("SELECT g FROM GiftedStudent g WHERE g.giftedStatus IN ('ELIGIBLE', 'ACTIVE') " +
           "ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findEligibleAndActive();

    @Query("SELECT g FROM GiftedStudent g WHERE g.eligibleForServices = true " +
           "ORDER BY g.eligibilityDeterminationDate DESC")
    List<GiftedStudent> findEligible();

    // Screening and Assessment
    @Query("SELECT g FROM GiftedStudent g WHERE g.giftedStatus IN ('REFERRED', 'SCREENING_IN_PROGRESS') " +
           "AND (g.screeningCompleted = false OR g.screeningCompleted IS NULL) " +
           "ORDER BY g.referralDate ASC")
    List<GiftedStudent> findAwaitingScreening();

    @Query("SELECT g FROM GiftedStudent g WHERE g.giftedStatus = 'ASSESSMENT_IN_PROGRESS' " +
           "ORDER BY g.screeningDate ASC")
    List<GiftedStudent> findInAssessment();

    @Query("SELECT g FROM GiftedStudent g WHERE g.giftedStatus = 'SCREENING_COMPLETE' " +
           "ORDER BY g.screeningDate DESC")
    List<GiftedStudent> findScreeningComplete();

    // Annual Review
    @Query("SELECT g FROM GiftedStudent g WHERE g.annualReviewRequired = true " +
           "AND (g.nextAnnualReviewDate IS NULL OR g.nextAnnualReviewDate <= :today) " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.nextAnnualReviewDate ASC NULLS FIRST")
    List<GiftedStudent> findNeedingAnnualReview(@Param("today") LocalDate today);

    @Query("SELECT g FROM GiftedStudent g WHERE g.nextAnnualReviewDate BETWEEN :today AND :futureDate " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.nextAnnualReviewDate ASC")
    List<GiftedStudent> findUpcomingAnnualReviews(@Param("today") LocalDate today,
                                                    @Param("futureDate") LocalDate futureDate);

    // Progress Monitoring
    @Query("SELECT g FROM GiftedStudent g WHERE g.giftedStatus = 'ACTIVE' " +
           "AND (g.lastProgressReviewDate IS NULL OR g.lastProgressReviewDate < :cutoffDate) " +
           "ORDER BY g.lastProgressReviewDate ASC NULLS FIRST")
    List<GiftedStudent> findOverdueForProgressReview(@Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT g FROM GiftedStudent g WHERE g.nextProgressReviewDate <= :today " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.nextProgressReviewDate ASC")
    List<GiftedStudent> findProgressReviewDue(@Param("today") LocalDate today);

    // Parent Communication
    @Query("SELECT g FROM GiftedStudent g WHERE (g.parentNotificationSent = false OR g.parentNotificationSent IS NULL) " +
           "AND g.giftedStatus IN ('ELIGIBLE', 'ACTIVE') ORDER BY g.eligibilityDeterminationDate ASC")
    List<GiftedStudent> findNeedingParentNotification();

    @Query("SELECT g FROM GiftedStudent g WHERE (g.parentConsentReceived = false OR g.parentConsentReceived IS NULL) " +
           "AND g.giftedStatus = 'ELIGIBLE' ORDER BY g.eligibilityDeterminationDate ASC")
    List<GiftedStudent> findNeedingParentConsent();

    // Service Provider
    @Query("SELECT g FROM GiftedStudent g WHERE g.serviceProvider.id = :providerId " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findByServiceProvider(@Param("providerId") Long providerId);

    @Query("SELECT g FROM GiftedStudent g WHERE g.serviceProviderName = :providerName " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findByServiceProviderName(@Param("providerName") String providerName);

    // Program Type
    @Query("SELECT g FROM GiftedStudent g WHERE g.programType = :programType " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findActiveByProgramType(@Param("programType") ProgramType programType);

    @Query("SELECT g.programType, COUNT(g) FROM GiftedStudent g " +
           "WHERE g.giftedStatus = 'ACTIVE' GROUP BY g.programType")
    List<Object[]> countByProgramType();

    // Gifted Area
    @Query("SELECT g FROM GiftedStudent g JOIN g.giftedAreas ga " +
           "WHERE ga = :area AND g.giftedStatus = 'ACTIVE' " +
           "ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findByGiftedArea(@Param("area") GiftedArea area);

    @Query("SELECT g FROM GiftedStudent g WHERE g.primaryGiftedArea = :area " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findByPrimaryArea(@Param("area") GiftedArea area);

    @Query("SELECT g.primaryGiftedArea, COUNT(g) FROM GiftedStudent g " +
           "WHERE g.giftedStatus = 'ACTIVE' AND g.primaryGiftedArea IS NOT NULL " +
           "GROUP BY g.primaryGiftedArea ORDER BY COUNT(g) DESC")
    List<Object[]> countByPrimaryArea();

    // Advanced Coursework
    @Query("SELECT g FROM GiftedStudent g WHERE g.apCoursesEnrolled > 0 " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.apCoursesEnrolled DESC")
    List<GiftedStudent> findEnrolledInAP();

    @Query("SELECT g FROM GiftedStudent g WHERE g.honorsCoursesEnrolled > 0 " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.honorsCoursesEnrolled DESC")
    List<GiftedStudent> findEnrolledInHonors();

    @Query("SELECT g FROM GiftedStudent g WHERE g.dualEnrollment = true " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findDualEnrollment();

    @Query("SELECT g FROM GiftedStudent g WHERE g.gradeAcceleration = true " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findGradeAccelerated();

    @Query("SELECT g FROM GiftedStudent g WHERE g.subjectAcceleration IS NOT NULL " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findSubjectAccelerated();

    // Cluster Grouping
    @Query("SELECT g FROM GiftedStudent g WHERE g.clusterGrouped = true " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.clusterGroupName ASC, g.student.lastName ASC")
    List<GiftedStudent> findClusterGrouped();

    @Query("SELECT g FROM GiftedStudent g WHERE g.clusterGroupName = :groupName " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findByClusterGroup(@Param("groupName") String groupName);

    // Talent Development
    @Query("SELECT g FROM GiftedStudent g WHERE g.talentDevelopmentPlanActive = true " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findWithActiveTalentPlan();

    @Query("SELECT g FROM GiftedStudent g WHERE g.mentorshipProgram = true " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findInMentorshipProgram();

    // Performance Tracking
    @Query("SELECT g FROM GiftedStudent g WHERE g.meetingExpectations = false " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findNotMeetingExpectations();

    @Query("SELECT g FROM GiftedStudent g WHERE g.currentGpa IS NOT NULL " +
           "AND g.currentGpa < :minGpa AND g.giftedStatus = 'ACTIVE' " +
           "ORDER BY g.currentGpa ASC")
    List<GiftedStudent> findBelowGPA(@Param("minGpa") Double minGpa);

    @Query("SELECT g FROM GiftedStudent g WHERE g.concerns IS NOT NULL " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.student.lastName ASC")
    List<GiftedStudent> findWithConcerns();

    // Statistics
    @Query("SELECT COUNT(g) FROM GiftedStudent g WHERE g.giftedStatus = 'ACTIVE'")
    Long countActive();

    @Query("SELECT g.giftedStatus, COUNT(g) FROM GiftedStudent g GROUP BY g.giftedStatus")
    List<Object[]> countByStatus();

    @Query("SELECT COUNT(g) FROM GiftedStudent g WHERE g.eligibleForServices = true")
    Long countEligible();

    // Referral Source
    @Query("SELECT g.referralType, COUNT(g) FROM GiftedStudent g " +
           "WHERE g.referralType IS NOT NULL GROUP BY g.referralType")
    List<Object[]> countByReferralType();

    // Date Range Queries
    @Query("SELECT g FROM GiftedStudent g WHERE g.identificationDate BETWEEN :startDate AND :endDate " +
           "ORDER BY g.identificationDate DESC")
    List<GiftedStudent> findByIdentificationDateRange(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT g FROM GiftedStudent g WHERE g.programEntryDate BETWEEN :startDate AND :endDate " +
           "ORDER BY g.programEntryDate DESC")
    List<GiftedStudent> findByProgramEntryDateRange(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    // Multi-talented Students
    @Query("SELECT g FROM GiftedStudent g WHERE SIZE(g.giftedAreas) > 1 " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY SIZE(g.giftedAreas) DESC")
    List<GiftedStudent> findMultiTalented();

    // IQ Score Queries
    @Query("SELECT g FROM GiftedStudent g WHERE g.iqScore >= :minScore " +
           "AND g.giftedStatus = 'ACTIVE' ORDER BY g.iqScore DESC")
    List<GiftedStudent> findByMinIQScore(@Param("minScore") Integer minScore);

    @Query("SELECT AVG(g.iqScore) FROM GiftedStudent g " +
           "WHERE g.iqScore IS NOT NULL AND g.giftedStatus = 'ACTIVE'")
    Double getAverageIQ();
}
