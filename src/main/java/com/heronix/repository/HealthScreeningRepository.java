package com.heronix.repository;

import com.heronix.model.domain.HealthScreening;
import com.heronix.model.domain.HealthScreening.ScreeningResult;
import com.heronix.model.domain.HealthScreening.ScreeningStatus;
import com.heronix.model.domain.HealthScreening.ScreeningType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Health Screening entities
 * Provides data access for health screening management and tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface HealthScreeningRepository extends JpaRepository<HealthScreening, Long> {

    // Basic Queries
    List<HealthScreening> findByStudentId(Long studentId);

    List<HealthScreening> findByScreeningType(ScreeningType type);

    List<HealthScreening> findByScreeningStatus(ScreeningStatus status);

    List<HealthScreening> findByResult(ScreeningResult result);

    // Scheduled Screenings
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningStatus = 'SCHEDULED' " +
           "ORDER BY hs.scheduledDate ASC")
    List<HealthScreening> findScheduled();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningStatus = 'SCHEDULED' " +
           "AND hs.scheduledDate = :date ORDER BY hs.student.lastName ASC")
    List<HealthScreening> findScheduledForDate(@Param("date") LocalDate date);

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningStatus = 'SCHEDULED' " +
           "AND hs.scheduledDate BETWEEN :startDate AND :endDate " +
           "ORDER BY hs.scheduledDate ASC")
    List<HealthScreening> findScheduledInRange(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningStatus = 'SCHEDULED' " +
           "AND hs.scheduledDate < :today ORDER BY hs.scheduledDate ASC")
    List<HealthScreening> findOverdueScreenings(@Param("today") LocalDate today);

    // Completed Screenings
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningStatus = 'COMPLETED' " +
           "ORDER BY hs.completedDate DESC")
    List<HealthScreening> findCompleted();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningStatus = 'COMPLETED' " +
           "AND hs.completedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY hs.completedDate DESC")
    List<HealthScreening> findCompletedInRange(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    // Type-Specific Queries
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = :type " +
           "AND hs.screeningStatus = 'COMPLETED' ORDER BY hs.completedDate DESC")
    List<HealthScreening> findCompletedByType(@Param("type") ScreeningType type);

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = 'VISION' " +
           "AND hs.screeningStatus = 'COMPLETED' ORDER BY hs.completedDate DESC")
    List<HealthScreening> findVisionScreenings();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = 'HEARING' " +
           "AND hs.screeningStatus = 'COMPLETED' ORDER BY hs.completedDate DESC")
    List<HealthScreening> findHearingScreenings();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = 'DENTAL' " +
           "AND hs.screeningStatus = 'COMPLETED' ORDER BY hs.completedDate DESC")
    List<HealthScreening> findDentalScreenings();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = 'SCOLIOSIS' " +
           "AND hs.screeningStatus = 'COMPLETED' ORDER BY hs.completedDate DESC")
    List<HealthScreening> findScoliosisScreenings();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = 'BMI' " +
           "AND hs.screeningStatus = 'COMPLETED' ORDER BY hs.completedDate DESC")
    List<HealthScreening> findBMIScreenings();

    // Results Queries
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.result = 'FAIL' " +
           "OR hs.result = 'REFER' ORDER BY hs.completedDate DESC")
    List<HealthScreening> findFailedOrReferred();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.result = 'REFER' " +
           "ORDER BY hs.completedDate DESC")
    List<HealthScreening> findNeedingReferral();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.result = :result " +
           "AND hs.screeningType = :type ORDER BY hs.completedDate DESC")
    List<HealthScreening> findByTypeAndResult(@Param("type") ScreeningType type,
                                                @Param("result") ScreeningResult result);

    // Follow-up Tracking
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.requiresFollowUp = true " +
           "ORDER BY hs.completedDate ASC")
    List<HealthScreening> findNeedingFollowUp();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.referralNeeded = true " +
           "AND hs.referralCompleted = false ORDER BY hs.referralDate ASC NULLS FIRST")
    List<HealthScreening> findWithOutstandingReferrals();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.referralCompleted = true " +
           "ORDER BY hs.referralCompletionDate DESC")
    List<HealthScreening> findWithCompletedReferrals();

    // Parent Notification
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.parentNotificationRequired = true " +
           "AND hs.parentNotified = false ORDER BY hs.completedDate ASC")
    List<HealthScreening> findNeedingParentNotification();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.parentNotified = true " +
           "ORDER BY hs.parentNotificationDate DESC")
    List<HealthScreening> findWithParentNotification();

    // Screener Queries
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screener.id = :screenerId " +
           "ORDER BY hs.screeningDate DESC")
    List<HealthScreening> findByScreener(@Param("screenerId") Long screenerId);

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screenerName = :screenerName " +
           "ORDER BY hs.screeningDate DESC")
    List<HealthScreening> findByScreenerName(@Param("screenerName") String screenerName);

    // Student Queries
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.student.id = :studentId " +
           "AND hs.screeningType = :type ORDER BY hs.screeningDate DESC")
    List<HealthScreening> findByStudentAndType(@Param("studentId") Long studentId,
                                                 @Param("type") ScreeningType type);

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.student.id = :studentId " +
           "AND hs.screeningStatus = 'COMPLETED' ORDER BY hs.completedDate DESC")
    List<HealthScreening> findCompletedByStudent(@Param("studentId") Long studentId);

    // Vision Specific
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = 'VISION' " +
           "AND (hs.result = 'FAIL' OR hs.result = 'REFER') " +
           "ORDER BY hs.completedDate DESC")
    List<HealthScreening> findVisionImpairments();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = 'VISION' " +
           "AND hs.glassesWornForTest = false AND (hs.result = 'FAIL' OR hs.result = 'REFER') " +
           "ORDER BY hs.completedDate DESC")
    List<HealthScreening> findNeedingGlasses();

    // Hearing Specific
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = 'HEARING' " +
           "AND (hs.result = 'FAIL' OR hs.result = 'REFER') " +
           "ORDER BY hs.completedDate DESC")
    List<HealthScreening> findHearingImpairments();

    // Dental Specific
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = 'DENTAL' " +
           "AND hs.cavitiesDetected = true ORDER BY hs.completedDate DESC")
    List<HealthScreening> findWithCavities();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = 'DENTAL' " +
           "AND hs.orthodonticNeeds = true ORDER BY hs.completedDate DESC")
    List<HealthScreening> findNeedingOrthodontics();

    // Scoliosis Specific
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = 'SCOLIOSIS' " +
           "AND hs.spineCurvatureDetected = true ORDER BY hs.completedDate DESC")
    List<HealthScreening> findWithSpineCurvature();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = 'SCOLIOSIS' " +
           "AND hs.spineCurvatureDetected = true AND hs.curvatureDegree >= :minDegrees " +
           "ORDER BY hs.curvatureDegree DESC")
    List<HealthScreening> findSevereScoliosis(@Param("minDegrees") Integer minDegrees);

    // BMI Specific
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = 'BMI' " +
           "AND hs.bmiPercentileCategory IN ('UNDERWEIGHT', 'OVERWEIGHT', 'OBESE') " +
           "ORDER BY hs.completedDate DESC")
    List<HealthScreening> findAbnormalBMI();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningType = 'BMI' " +
           "AND hs.bmiPercentileCategory = 'OBESE' ORDER BY hs.bmiPercentile DESC")
    List<HealthScreening> findObese();

    // State Reporting
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.stateReported = false " +
           "AND hs.screeningStatus = 'COMPLETED' ORDER BY hs.completedDate ASC")
    List<HealthScreening> findNeedingStateReporting();

    @Query("SELECT hs FROM HealthScreening hs WHERE hs.stateReported = true " +
           "ORDER BY hs.stateReportDate DESC")
    List<HealthScreening> findStateReported();

    // Date Range Queries
    @Query("SELECT hs FROM HealthScreening hs WHERE hs.screeningDate BETWEEN :startDate AND :endDate " +
           "ORDER BY hs.screeningDate ASC")
    List<HealthScreening> findByDateRange(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    // Statistics
    @Query("SELECT COUNT(hs) FROM HealthScreening hs WHERE hs.screeningStatus = 'COMPLETED'")
    Long countCompleted();

    @Query("SELECT hs.screeningType, COUNT(hs) FROM HealthScreening hs " +
           "WHERE hs.screeningStatus = 'COMPLETED' GROUP BY hs.screeningType")
    List<Object[]> countByType();

    @Query("SELECT hs.result, COUNT(hs) FROM HealthScreening hs " +
           "WHERE hs.screeningStatus = 'COMPLETED' AND hs.result IS NOT NULL " +
           "GROUP BY hs.result")
    List<Object[]> countByResult();

    @Query("SELECT hs.screeningStatus, COUNT(hs) FROM HealthScreening hs " +
           "GROUP BY hs.screeningStatus")
    List<Object[]> countByStatus();
}
