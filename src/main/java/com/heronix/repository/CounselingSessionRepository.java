package com.heronix.repository;

import com.heronix.model.domain.CounselingSession;
import com.heronix.model.domain.CounselingSession.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Counseling Session entities
 * Provides data access for counseling session tracking and management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface CounselingSessionRepository extends JpaRepository<CounselingSession, Long> {

    // Basic Queries
    List<CounselingSession> findByStudentId(Long studentId);

    List<CounselingSession> findByCounselorId(Long counselorId);

    List<CounselingSession> findBySessionType(SessionType sessionType);

    List<CounselingSession> findBySessionStatus(SessionStatus sessionStatus);

    // Date Queries
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.sessionDate = :date " +
           "ORDER BY cs.sessionTime ASC")
    List<CounselingSession> findByDate(@Param("date") LocalDate date);

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.sessionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findByDateRange(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.counselor.id = :counselorId " +
           "AND cs.sessionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findByCounselorAndDateRange(@Param("counselorId") Long counselorId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.student.id = :studentId " +
           "AND cs.sessionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findByStudentAndDateRange(@Param("studentId") Long studentId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    // Session Type Queries
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.sessionType IN " +
           "('ACADEMIC_ADVISING', 'COURSE_SELECTION', 'SCHEDULE_CHANGE', 'GRADUATION_PLANNING', 'COLLEGE_PLANNING') " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findAcademicCounselingSessions();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.sessionType = 'CAREER_COUNSELING' " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findCareerCounselingSessions();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.sessionType IN " +
           "('PERSONAL_COUNSELING', 'CRISIS_INTERVENTION', 'BEHAVIORAL_SUPPORT') " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findPersonalCounselingSessions();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.sessionType = 'CRISIS_INTERVENTION' " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findCrisisInterventionSessions();

    // Session Format Queries
    List<CounselingSession> findBySessionFormat(SessionFormat sessionFormat);

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.sessionFormat = 'GROUP' " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findGroupSessions();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.sessionFormat IN ('VIRTUAL', 'PHONE') " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findRemoteSessions();

    // Counseling Focus Queries
    List<CounselingSession> findByPrimaryFocus(CounselingFocus primaryFocus);

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.primaryFocus = 'MENTAL_HEALTH' " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findMentalHealthSessions();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.primaryFocus IN " +
           "('CRISIS', 'ANXIETY_STRESS', 'GRIEF_LOSS', 'MENTAL_HEALTH') " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findCrisisAndMentalHealthSessions();

    // Crisis and Risk Queries
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.crisisSituation = true " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findCrisisSessions();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.safetyConcerns = true " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findSessionsWithSafetyConcerns();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.riskLevel IN ('HIGH', 'IMMINENT') " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findHighRiskSessions();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.safetyPlanCreated = true " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findSessionsWithSafetyPlan();

    // Parent Notification Queries
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.parentNotified = false " +
           "AND (cs.crisisSituation = true OR cs.riskLevel IN ('HIGH', 'IMMINENT')) " +
           "ORDER BY cs.sessionDate ASC")
    List<CounselingSession> findNeedingParentNotification();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.parentNotified = true " +
           "ORDER BY cs.parentNotificationDate DESC")
    List<CounselingSession> findWithParentNotification();

    // Referral Queries
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.referralMade = true " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findSessionsWithReferrals();

    List<CounselingSession> findByReferralType(ReferralType referralType);

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.referralType IN " +
           "('EXTERNAL_THERAPIST', 'PSYCHIATRIST', 'COMMUNITY_MENTAL_HEALTH', 'HOSPITAL') " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findExternalReferrals();

    // Follow-up Queries
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.followUpNeeded = true " +
           "ORDER BY cs.followUpDate ASC")
    List<CounselingSession> findNeedingFollowUp();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.followUpNeeded = true " +
           "AND cs.followUpDate IS NOT NULL " +
           "AND cs.followUpDate <= :date " +
           "ORDER BY cs.followUpDate ASC")
    List<CounselingSession> findOverdueFollowUp(@Param("date") LocalDate date);

    // Attendance Queries
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.studentAttended = false " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findStudentNoShows();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.parentAttended = true " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findParentConferenceSessions();

    // Academic Counseling Specific
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.graduationPlanReviewed = true " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findGraduationPlanReviews();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.scheduleChangesMade = true " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findScheduleChangeSessions();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.collegePlanningDiscussed = true " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findCollegePlanningSessions();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.creditsDiscussed = true " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findCreditDiscussionSessions();

    // Career Counseling Specific
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.careerAssessmentAdministered = true " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findCareerAssessmentSessions();

    @Query("SELECT cs FROM CounselingSession cs WHERE cs.careerInterestsExplored = true " +
           "ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findCareerExplorationSessions();

    // Statistics
    @Query("SELECT COUNT(cs) FROM CounselingSession cs WHERE cs.sessionDate = :today")
    Long countToday(@Param("today") LocalDate today);

    @Query("SELECT COUNT(cs) FROM CounselingSession cs WHERE cs.counselor.id = :counselorId " +
           "AND cs.sessionDate BETWEEN :startDate AND :endDate")
    Long countByCounselorAndDateRange(@Param("counselorId") Long counselorId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT cs.sessionType, COUNT(cs) FROM CounselingSession cs " +
           "GROUP BY cs.sessionType ORDER BY COUNT(cs) DESC")
    List<Object[]> countBySessionType();

    @Query("SELECT cs.primaryFocus, COUNT(cs) FROM CounselingSession cs " +
           "GROUP BY cs.primaryFocus ORDER BY COUNT(cs) DESC")
    List<Object[]> countByPrimaryFocus();

    @Query("SELECT COUNT(cs) FROM CounselingSession cs WHERE cs.crisisSituation = true")
    Long countCrisisSessions();

    @Query("SELECT COUNT(cs) FROM CounselingSession cs WHERE cs.referralMade = true")
    Long countSessionsWithReferrals();

    // Student Session Count
    @Query("SELECT COUNT(cs) FROM CounselingSession cs WHERE cs.student.id = :studentId")
    Long countByStudent(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(cs) FROM CounselingSession cs WHERE cs.student.id = :studentId " +
           "AND cs.sessionType = :sessionType")
    Long countByStudentAndType(@Param("studentId") Long studentId,
                                @Param("sessionType") SessionType sessionType);
}
