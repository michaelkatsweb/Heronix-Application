package com.heronix.repository;

import com.heronix.model.domain.GiftedService;
import com.heronix.model.domain.GiftedService.EngagementLevel;
import com.heronix.model.domain.GiftedService.FocusArea;
import com.heronix.model.domain.GiftedService.ServiceStatus;
import com.heronix.model.domain.GiftedService.ServiceType;
import com.heronix.model.domain.GiftedStudent.ServiceDeliveryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Gifted Service entities
 * Provides data access for gifted service delivery, tracking, and reporting
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface GiftedServiceRepository extends JpaRepository<GiftedService, Long> {

    // Basic Queries
    List<GiftedService> findByGiftedStudentId(Long giftedStudentId);

    List<GiftedService> findByServiceType(ServiceType type);

    List<GiftedService> findByFocusArea(FocusArea area);

    List<GiftedService> findByStatus(ServiceStatus status);

    // Date Queries
    @Query("SELECT gs FROM GiftedService gs WHERE gs.serviceDate = :date " +
           "ORDER BY gs.giftedStudent.student.lastName ASC")
    List<GiftedService> findByServiceDate(@Param("date") LocalDate date);

    @Query("SELECT gs FROM GiftedService gs WHERE gs.serviceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY gs.serviceDate ASC, gs.giftedStudent.student.lastName ASC")
    List<GiftedService> findByDateRange(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT gs FROM GiftedService gs WHERE gs.serviceDate >= :startDate " +
           "ORDER BY gs.serviceDate DESC")
    List<GiftedService> findSinceDate(@Param("startDate") LocalDate startDate);

    // Status Queries
    @Query("SELECT gs FROM GiftedService gs WHERE gs.status = 'SCHEDULED' " +
           "AND gs.serviceDate >= :today ORDER BY gs.serviceDate ASC")
    List<GiftedService> findUpcoming(@Param("today") LocalDate today);

    @Query("SELECT gs FROM GiftedService gs WHERE gs.status = 'SCHEDULED' " +
           "AND gs.serviceDate < :today ORDER BY gs.serviceDate DESC")
    List<GiftedService> findPastDue(@Param("today") LocalDate today);

    @Query("SELECT gs FROM GiftedService gs WHERE gs.status = 'COMPLETED' " +
           "ORDER BY gs.serviceDate DESC")
    List<GiftedService> findCompleted();

    @Query("SELECT gs FROM GiftedService gs WHERE gs.status = 'CANCELLED' " +
           "ORDER BY gs.serviceDate DESC")
    List<GiftedService> findCancelled();

    @Query("SELECT gs FROM GiftedService gs WHERE gs.status = 'NO_SHOW' " +
           "ORDER BY gs.serviceDate DESC")
    List<GiftedService> findNoShows();

    // Attendance
    @Query("SELECT gs FROM GiftedService gs WHERE gs.studentAttended = true " +
           "AND gs.status = 'COMPLETED' ORDER BY gs.serviceDate DESC")
    List<GiftedService> findAttended();

    @Query("SELECT gs FROM GiftedService gs WHERE gs.studentAttended = false " +
           "AND gs.status = 'COMPLETED' ORDER BY gs.serviceDate DESC")
    List<GiftedService> findAbsent();

    @Query("SELECT gs FROM GiftedService gs WHERE gs.studentAttended = true " +
           "AND gs.serviceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY gs.serviceDate ASC")
    List<GiftedService> findAttendedInRange(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    // Documentation
    @Query("SELECT gs FROM GiftedService gs WHERE gs.status = 'COMPLETED' " +
           "AND gs.progressNotes IS NULL ORDER BY gs.serviceDate ASC")
    List<GiftedService> findNeedingDocumentation();

    @Query("SELECT gs FROM GiftedService gs WHERE gs.status = 'COMPLETED' " +
           "AND gs.progressNotes IS NOT NULL ORDER BY gs.serviceDate DESC")
    List<GiftedService> findDocumented();

    // Service Provider
    @Query("SELECT gs FROM GiftedService gs WHERE gs.serviceProvider.id = :providerId " +
           "ORDER BY gs.serviceDate DESC")
    List<GiftedService> findByServiceProvider(@Param("providerId") Long providerId);

    @Query("SELECT gs FROM GiftedService gs WHERE gs.serviceProvider.id = :providerId " +
           "AND gs.serviceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY gs.serviceDate ASC")
    List<GiftedService> findByProviderAndDateRange(@Param("providerId") Long providerId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT gs FROM GiftedService gs WHERE gs.serviceProviderName = :providerName " +
           "ORDER BY gs.serviceDate DESC")
    List<GiftedService> findByProviderName(@Param("providerName") String providerName);

    // Student Queries
    @Query("SELECT gs FROM GiftedService gs WHERE gs.giftedStudent.student.id = :studentId " +
           "ORDER BY gs.serviceDate DESC")
    List<GiftedService> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT gs FROM GiftedService gs WHERE gs.giftedStudent.id = :giftedStudentId " +
           "AND gs.serviceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY gs.serviceDate ASC")
    List<GiftedService> findByGiftedStudentAndDateRange(@Param("giftedStudentId") Long giftedStudentId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT gs FROM GiftedService gs WHERE gs.giftedStudent.id = :giftedStudentId " +
           "AND gs.status = 'COMPLETED' ORDER BY gs.serviceDate DESC")
    List<GiftedService> findCompletedByGiftedStudent(@Param("giftedStudentId") Long giftedStudentId);

    // Service Type
    @Query("SELECT gs FROM GiftedService gs WHERE gs.serviceType = :type " +
           "AND gs.status = 'COMPLETED' ORDER BY gs.serviceDate DESC")
    List<GiftedService> findCompletedByType(@Param("type") ServiceType type);

    @Query("SELECT gs.serviceType, COUNT(gs) FROM GiftedService gs " +
           "WHERE gs.status = 'COMPLETED' GROUP BY gs.serviceType")
    List<Object[]> countByServiceType();

    // Focus Area
    @Query("SELECT gs FROM GiftedService gs WHERE gs.focusArea = :area " +
           "AND gs.status = 'COMPLETED' ORDER BY gs.serviceDate DESC")
    List<GiftedService> findCompletedByFocusArea(@Param("area") FocusArea area);

    @Query("SELECT gs.focusArea, COUNT(gs) FROM GiftedService gs " +
           "WHERE gs.status = 'COMPLETED' AND gs.focusArea IS NOT NULL " +
           "GROUP BY gs.focusArea ORDER BY COUNT(gs) DESC")
    List<Object[]> countByFocusArea();

    // Delivery Model
    @Query("SELECT gs FROM GiftedService gs WHERE gs.deliveryModel = :model " +
           "ORDER BY gs.serviceDate DESC")
    List<GiftedService> findByDeliveryModel(@Param("model") ServiceDeliveryModel model);

    @Query("SELECT gs.deliveryModel, COUNT(gs) FROM GiftedService gs " +
           "WHERE gs.deliveryModel IS NOT NULL GROUP BY gs.deliveryModel")
    List<Object[]> countByDeliveryModel();

    // Engagement Level
    @Query("SELECT gs FROM GiftedService gs WHERE gs.engagementLevel = :level " +
           "AND gs.status = 'COMPLETED' ORDER BY gs.serviceDate DESC")
    List<GiftedService> findByEngagementLevel(@Param("level") EngagementLevel level);

    @Query("SELECT gs FROM GiftedService gs WHERE gs.engagementLevel IN ('EXCEPTIONAL', 'STRONG') " +
           "AND gs.status = 'COMPLETED' ORDER BY gs.serviceDate DESC")
    List<GiftedService> findHighEngagement();

    @Query("SELECT gs FROM GiftedService gs WHERE gs.engagementLevel IN ('MODERATE', 'LOW') " +
           "AND gs.status = 'COMPLETED' ORDER BY gs.serviceDate DESC")
    List<GiftedService> findLowEngagement();

    @Query("SELECT gs.engagementLevel, COUNT(gs) FROM GiftedService gs " +
           "WHERE gs.status = 'COMPLETED' AND gs.engagementLevel IS NOT NULL " +
           "GROUP BY gs.engagementLevel")
    List<Object[]> countByEngagementLevel();

    // Service Minutes
    @Query("SELECT SUM(gs.durationMinutes) FROM GiftedService gs " +
           "WHERE gs.giftedStudent.id = :giftedStudentId AND gs.studentAttended = true " +
           "AND gs.serviceDate BETWEEN :startDate AND :endDate")
    Long calculateTotalMinutes(@Param("giftedStudentId") Long giftedStudentId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    @Query("SELECT gs.giftedStudent.id, SUM(gs.durationMinutes) FROM GiftedService gs " +
           "WHERE gs.studentAttended = true AND gs.serviceDate BETWEEN :startDate AND :endDate " +
           "GROUP BY gs.giftedStudent.id")
    List<Object[]> calculateMinutesByStudent(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT AVG(gs.durationMinutes) FROM GiftedService gs " +
           "WHERE gs.status = 'COMPLETED' AND gs.durationMinutes IS NOT NULL")
    Double getAverageDuration();

    // Follow-up
    @Query("SELECT gs FROM GiftedService gs WHERE gs.followUpNeeded = true " +
           "AND gs.status = 'COMPLETED' ORDER BY gs.serviceDate ASC")
    List<GiftedService> findNeedingFollowUp();

    @Query("SELECT gs FROM GiftedService gs WHERE gs.extensionActivityAssigned = true " +
           "ORDER BY gs.serviceDate DESC")
    List<GiftedService> findWithExtensionActivities();

    // Assessment
    @Query("SELECT gs FROM GiftedService gs WHERE gs.assessmentAdministered = true " +
           "AND gs.status = 'COMPLETED' ORDER BY gs.serviceDate DESC")
    List<GiftedService> findWithAssessments();

    @Query("SELECT gs FROM GiftedService gs WHERE gs.assessmentAdministered = true " +
           "AND gs.assessmentResults IS NOT NULL ORDER BY gs.serviceDate DESC")
    List<GiftedService> findWithAssessmentResults();

    // Group Services
    @Query("SELECT gs FROM GiftedService gs WHERE gs.groupSize > 1 " +
           "AND gs.status = 'COMPLETED' ORDER BY gs.groupSize DESC")
    List<GiftedService> findGroupServices();

    @Query("SELECT gs FROM GiftedService gs WHERE gs.peerCollaboration = true " +
           "AND gs.status = 'COMPLETED' ORDER BY gs.serviceDate DESC")
    List<GiftedService> findWithPeerCollaboration();

    @Query("SELECT gs FROM GiftedService gs WHERE gs.crossGradeGrouping = true " +
           "AND gs.status = 'COMPLETED' ORDER BY gs.serviceDate DESC")
    List<GiftedService> findCrossGradeGrouping();

    // Technology Integration
    @Query("SELECT gs FROM GiftedService gs WHERE gs.technologyIntegration IS NOT NULL " +
           "AND gs.status = 'COMPLETED' ORDER BY gs.serviceDate DESC")
    List<GiftedService> findWithTechnology();

    // Statistics
    @Query("SELECT COUNT(gs) FROM GiftedService gs WHERE gs.status = 'COMPLETED'")
    Long countCompleted();

    @Query("SELECT COUNT(gs) FROM GiftedService gs WHERE gs.studentAttended = true")
    Long countAttended();

    @Query("SELECT gs.status, COUNT(gs) FROM GiftedService gs GROUP BY gs.status")
    List<Object[]> countByStatus();

    @Query("SELECT COUNT(DISTINCT gs.giftedStudent.id) FROM GiftedService gs " +
           "WHERE gs.serviceDate BETWEEN :startDate AND :endDate AND gs.status = 'COMPLETED'")
    Long countUniqueStudentsServed(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);
}
