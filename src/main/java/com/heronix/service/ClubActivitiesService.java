package com.heronix.service;

import com.heronix.model.domain.Club;
import com.heronix.model.domain.ClubActivity;
import com.heronix.model.domain.ClubMembership;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Club Activities Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
public interface ClubActivitiesService {

    // Club Membership Management
    ClubMembership joinClub(Long clubId, Long studentId, String academicYear);
    ClubMembership updateMembership(Long membershipId, ClubMembership membership);
    void leaveClub(Long membershipId);
    ClubMembership assignOfficer(Long membershipId, ClubMembership.MembershipRole role);
    ClubMembership recordServiceHours(Long membershipId, Integer hours);
    ClubMembership updateParentConsent(Long membershipId, boolean received);
    List<ClubMembership> getClubMembers(Long clubId);
    List<ClubMembership> getActiveClubMembers(Long clubId);
    List<ClubMembership> getStudentClubs(Long studentId);
    List<ClubMembership> getStudentActiveClubs(Long studentId);
    List<ClubMembership> getClubOfficers(Long clubId);
    List<ClubMembership> getMembersNeedingConsent();
    int getStudentTotalServiceHours(Long studentId);

    // Club Activity Management
    ClubActivity scheduleActivity(ClubActivity activity);
    ClubActivity updateActivity(Long activityId, ClubActivity activity);
    void cancelActivity(Long activityId, String reason);
    ClubActivity recordAttendance(Long activityId, Integer attendance);
    void deleteActivity(Long activityId);
    ClubActivity getActivityById(Long activityId);
    List<ClubActivity> getClubActivities(Long clubId);
    List<ClubActivity> getUpcomingActivities(Long clubId);
    List<ClubActivity> getAllUpcomingActivities();
    List<ClubActivity> getCompletedActivities(Long clubId);
    List<ClubActivity> getActivitiesInDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<ClubActivity> getOpenActivities();
    List<ClubActivity> getServiceProjects();

    // Reporting
    Map<String, Object> getClubStatistics(Long clubId);
    Map<String, Object> getMemberStatistics(Long membershipId);
    Map<String, Object> getClubsOverview(String academicYear);
    int getClubServiceHours(Long clubId);
}
