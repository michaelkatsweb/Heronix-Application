package com.heronix.repository;

import com.heronix.model.domain.IEPMeeting;
import com.heronix.model.domain.IEPMeeting.MeetingStatus;
import com.heronix.model.domain.IEPMeeting.MeetingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for IEP Meeting entities
 * Provides data access for meeting scheduling and compliance tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface IEPMeetingRepository extends JpaRepository<IEPMeeting, Long> {

    // Basic Queries
    List<IEPMeeting> findByIepId(Long iepId);

    List<IEPMeeting> findByIepIdOrderByMeetingDateDesc(Long iepId);

    List<IEPMeeting> findByMeetingType(MeetingType type);

    List<IEPMeeting> findByStatus(MeetingStatus status);

    // Date Queries
    @Query("SELECT m FROM IEPMeeting m WHERE m.meetingDate = :date " +
           "AND m.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY m.meetingTime ASC")
    List<IEPMeeting> findScheduledForDate(@Param("date") LocalDate date);

    @Query("SELECT m FROM IEPMeeting m WHERE m.meetingDate BETWEEN :startDate AND :endDate " +
           "ORDER BY m.meetingDate ASC, m.meetingTime ASC")
    List<IEPMeeting> findByDateRange(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    // Upcoming Meetings
    @Query("SELECT m FROM IEPMeeting m WHERE m.meetingDate >= :today " +
           "AND m.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY m.meetingDate ASC, m.meetingTime ASC")
    List<IEPMeeting> findUpcoming(@Param("today") LocalDate today);

    @Query("SELECT m FROM IEPMeeting m WHERE m.meetingDate BETWEEN :today AND :futureDate " +
           "AND m.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY m.meetingDate ASC")
    List<IEPMeeting> findUpcomingWithinDays(@Param("today") LocalDate today,
                                             @Param("futureDate") LocalDate futureDate);

    // Overdue Meetings
    @Query("SELECT m FROM IEPMeeting m WHERE m.meetingDate < :today " +
           "AND m.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY m.meetingDate ASC")
    List<IEPMeeting> findPastDue(@Param("today") LocalDate today);

    // Annual Reviews
    @Query("SELECT m FROM IEPMeeting m WHERE m.isAnnualReview = true " +
           "ORDER BY m.meetingDate DESC")
    List<IEPMeeting> findAnnualReviews();

    @Query("SELECT m FROM IEPMeeting m WHERE m.isAnnualReview = true " +
           "AND m.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND m.meetingDate < :date " +
           "ORDER BY m.meetingDate ASC")
    List<IEPMeeting> findOverdueAnnualReviews(@Param("date") LocalDate date);

    // Triennial Re-evaluations
    @Query("SELECT m FROM IEPMeeting m WHERE m.isTriennialReevaluation = true " +
           "ORDER BY m.meetingDate DESC")
    List<IEPMeeting> findTriennialReevaluations();

    @Query("SELECT m FROM IEPMeeting m WHERE m.isTriennialReevaluation = true " +
           "AND m.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND m.meetingDate < :date " +
           "ORDER BY m.meetingDate ASC")
    List<IEPMeeting> findOverdueTriennials(@Param("date") LocalDate date);

    // Parent Participation
    @Query("SELECT m FROM IEPMeeting m WHERE m.status = 'COMPLETED' " +
           "AND (m.parentAttended IS NULL OR m.parentAttended = false) " +
           "ORDER BY m.meetingDate DESC")
    List<IEPMeeting> findWithoutParentParticipation();

    @Query("SELECT m FROM IEPMeeting m WHERE m.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND m.parentInvitationSentDate IS NULL " +
           "AND m.meetingDate <= :cutoffDate " +
           "ORDER BY m.meetingDate ASC")
    List<IEPMeeting> findNeedingParentInvitation(@Param("cutoffDate") LocalDate cutoffDate);

    // Consent Tracking
    @Query("SELECT m FROM IEPMeeting m WHERE (m.isInitialIEP = true OR m.isTriennialReevaluation = true) " +
           "AND (m.parentConsentReceived IS NULL OR m.parentConsentReceived = false) " +
           "AND m.status != 'CANCELLED' " +
           "ORDER BY m.meetingDate ASC")
    List<IEPMeeting> findNeedingParentConsent();

    // PWN (Prior Written Notice)
    @Query("SELECT m FROM IEPMeeting m WHERE m.status = 'COMPLETED' " +
           "AND m.pwnIssued = false " +
           "ORDER BY m.meetingDate ASC")
    List<IEPMeeting> findNeedingPWN();

    // Interpreter Needs
    @Query("SELECT m FROM IEPMeeting m WHERE m.interpreterRequired = true " +
           "AND (m.interpreterProvided IS NULL OR m.interpreterProvided = false) " +
           "AND m.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY m.meetingDate ASC")
    List<IEPMeeting> findNeedingInterpreter();

    // Follow-up
    @Query("SELECT m FROM IEPMeeting m WHERE m.followUpNeeded = true " +
           "AND m.status = 'COMPLETED' " +
           "ORDER BY m.meetingDate DESC")
    List<IEPMeeting> findNeedingFollowUp();

    // Compliance
    @Query("SELECT m FROM IEPMeeting m WHERE m.status = 'COMPLETED' " +
           "AND (m.meetingHeldWithinTimeline IS NULL OR m.meetingHeldWithinTimeline = false) " +
           "ORDER BY m.meetingDate DESC")
    List<IEPMeeting> findOutOfCompliance();

    // Student Meetings
    @Query("SELECT m FROM IEPMeeting m WHERE m.iep.student.id = :studentId " +
           "ORDER BY m.meetingDate DESC")
    List<IEPMeeting> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT m FROM IEPMeeting m WHERE m.iep.student.id = :studentId " +
           "AND m.meetingType = :type ORDER BY m.meetingDate DESC")
    List<IEPMeeting> findByStudentIdAndType(@Param("studentId") Long studentId,
                                             @Param("type") MeetingType type);

    // Statistics
    @Query("SELECT m.meetingType, COUNT(m) FROM IEPMeeting m " +
           "WHERE m.meetingDate BETWEEN :startDate AND :endDate " +
           "GROUP BY m.meetingType")
    List<Object[]> countByTypeInDateRange(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(m) FROM IEPMeeting m WHERE m.iep.id = :iepId " +
           "AND m.status = 'COMPLETED'")
    Long countCompletedByIep(@Param("iepId") Long iepId);
}
