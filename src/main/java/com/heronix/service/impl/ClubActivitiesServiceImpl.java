package com.heronix.service.impl;

import com.heronix.model.domain.Club;
import com.heronix.model.domain.ClubActivity;
import com.heronix.model.domain.ClubMembership;
import com.heronix.model.domain.Student;
import com.heronix.repository.ClubActivityRepository;
import com.heronix.repository.ClubMembershipRepository;
import com.heronix.repository.ClubRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.service.ClubActivitiesService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of ClubActivitiesService
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Service
@RequiredArgsConstructor
public class ClubActivitiesServiceImpl implements ClubActivitiesService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final ClubActivityRepository activityRepository;
    private final StudentRepository studentRepository;

    // ========== Club Membership Management ==========

    @Override
    @Transactional
    public ClubMembership joinClub(Long clubId, Long studentId, String academicYear) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new EntityNotFoundException("Club not found with id: " + clubId));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));

        if (club.isAtCapacity()) {
            throw new IllegalStateException("Club is at capacity");
        }

        ClubMembership membership = ClubMembership.builder()
                .club(club)
                .student(student)
                .academicYear(academicYear)
                .role(ClubMembership.MembershipRole.MEMBER)
                .status(ClubMembership.MembershipStatus.ACTIVE)
                .joinDate(LocalDate.now())
                .build();

        ClubMembership saved = membershipRepository.save(membership);

        club.setCurrentEnrollment(club.getCurrentEnrollment() + 1);
        clubRepository.save(club);

        return saved;
    }

    @Override
    @Transactional
    public ClubMembership updateMembership(Long membershipId, ClubMembership membership) {
        ClubMembership existing = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + membershipId));

        existing.setRole(membership.getRole());
        existing.setCustomRole(membership.getCustomRole());
        existing.setMeetingsAttended(membership.getMeetingsAttended());
        existing.setEventsAttended(membership.getEventsAttended());
        existing.setContributions(membership.getContributions());
        existing.setAwards(membership.getAwards());
        existing.setNotes(membership.getNotes());

        return membershipRepository.save(existing);
    }

    @Override
    @Transactional
    public void leaveClub(Long membershipId) {
        ClubMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + membershipId));

        membership.setStatus(ClubMembership.MembershipStatus.RESIGNED);
        membership.setLeaveDate(LocalDate.now());
        membershipRepository.save(membership);

        Club club = membership.getClub();
        club.setCurrentEnrollment(Math.max(0, club.getCurrentEnrollment() - 1));
        clubRepository.save(club);
    }

    @Override
    @Transactional
    public ClubMembership assignOfficer(Long membershipId, ClubMembership.MembershipRole role) {
        ClubMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + membershipId));

        membership.setRole(role);

        switch (role) {
            case PRESIDENT -> membership.setPresident(true);
            case VICE_PRESIDENT -> membership.setVicePresident(true);
            case SECRETARY -> membership.setSecretary(true);
            case TREASURER -> membership.setTreasurer(true);
            default -> {}
        }

        return membershipRepository.save(membership);
    }

    @Override
    @Transactional
    public ClubMembership recordServiceHours(Long membershipId, Integer hours) {
        ClubMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + membershipId));

        membership.setServiceHours(membership.getServiceHours() + hours);
        return membershipRepository.save(membership);
    }

    @Override
    @Transactional
    public ClubMembership updateParentConsent(Long membershipId, boolean received) {
        ClubMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + membershipId));

        membership.setParentConsentReceived(received);
        return membershipRepository.save(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubMembership> getClubMembers(Long clubId) {
        return membershipRepository.findByClubId(clubId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubMembership> getActiveClubMembers(Long clubId) {
        return membershipRepository.findActiveByClub(clubId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubMembership> getStudentClubs(Long studentId) {
        return membershipRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubMembership> getStudentActiveClubs(Long studentId) {
        return membershipRepository.findActiveByStudent(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubMembership> getClubOfficers(Long clubId) {
        return membershipRepository.findOfficers(clubId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubMembership> getMembersNeedingConsent() {
        return membershipRepository.findMissingParentConsent();
    }

    @Override
    @Transactional(readOnly = true)
    public int getStudentTotalServiceHours(Long studentId) {
        return membershipRepository.sumServiceHoursByStudent(studentId);
    }

    // ========== Club Activity Management ==========

    @Override
    @Transactional
    public ClubActivity scheduleActivity(ClubActivity activity) {
        activity.setStatus(ClubActivity.ActivityStatus.PLANNED);
        return activityRepository.save(activity);
    }

    @Override
    @Transactional
    public ClubActivity updateActivity(Long activityId, ClubActivity activity) {
        ClubActivity existing = getActivityById(activityId);
        existing.setActivityType(activity.getActivityType());
        existing.setActivityName(activity.getActivityName());
        existing.setDescription(activity.getDescription());
        existing.setActivityDate(activity.getActivityDate());
        existing.setEndDate(activity.getEndDate());
        existing.setLocation(activity.getLocation());
        existing.setAddress(activity.getAddress());
        existing.setExpectedAttendance(activity.getExpectedAttendance());
        existing.setMandatory(activity.getMandatory());
        existing.setOpenToNonMembers(activity.getOpenToNonMembers());
        existing.setServiceHoursAwarded(activity.getServiceHoursAwarded());
        existing.setRequirements(activity.getRequirements());
        existing.setNotes(activity.getNotes());
        return activityRepository.save(existing);
    }

    @Override
    @Transactional
    public void cancelActivity(Long activityId, String reason) {
        ClubActivity activity = getActivityById(activityId);
        activity.setStatus(ClubActivity.ActivityStatus.CANCELLED);
        activity.setCancelled(true);
        activity.setCancellationReason(reason);
        activityRepository.save(activity);
    }

    @Override
    @Transactional
    public ClubActivity recordAttendance(Long activityId, Integer attendance) {
        ClubActivity activity = getActivityById(activityId);
        activity.setActualAttendance(attendance);
        activity.setStatus(ClubActivity.ActivityStatus.COMPLETED);
        return activityRepository.save(activity);
    }

    @Override
    @Transactional
    public void deleteActivity(Long activityId) {
        activityRepository.deleteById(activityId);
    }

    @Override
    @Transactional(readOnly = true)
    public ClubActivity getActivityById(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException("Activity not found with id: " + activityId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubActivity> getClubActivities(Long clubId) {
        return activityRepository.findByClubIdOrderByActivityDateDesc(clubId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubActivity> getUpcomingActivities(Long clubId) {
        return activityRepository.findUpcomingByClub(clubId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubActivity> getAllUpcomingActivities() {
        return activityRepository.findAllUpcoming(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubActivity> getCompletedActivities(Long clubId) {
        return activityRepository.findCompletedByClub(clubId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubActivity> getActivitiesInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return activityRepository.findActivitiesInDateRange(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubActivity> getOpenActivities() {
        return activityRepository.findOpenActivities(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubActivity> getServiceProjects() {
        return activityRepository.findServiceProjects();
    }

    // ========== Reporting ==========

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getClubStatistics(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new EntityNotFoundException("Club not found with id: " + clubId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("clubId", clubId);
        stats.put("clubName", club.getName());
        stats.put("category", club.getCategory());
        stats.put("currentMembers", club.getCurrentEnrollment());
        stats.put("maxCapacity", club.getMaxCapacity());

        long activeMembers = membershipRepository.countActiveByClub(clubId);
        stats.put("activeMembers", activeMembers);

        long totalActivities = activityRepository.countCompletedByClub(clubId);
        stats.put("totalActivities", totalActivities);

        int serviceHours = activityRepository.sumServiceHoursByClub(clubId);
        stats.put("totalServiceHours", serviceHours);

        List<ClubActivity> upcomingActivities = getUpcomingActivities(clubId);
        stats.put("upcomingActivities", upcomingActivities.size());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMemberStatistics(Long membershipId) {
        ClubMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + membershipId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("membershipId", membershipId);
        stats.put("studentName", membership.getStudent().getFullName());
        stats.put("clubName", membership.getClub().getName());
        stats.put("role", membership.getRole());
        stats.put("isOfficer", membership.isOfficer());
        stats.put("meetingsAttended", membership.getMeetingsAttended());
        stats.put("eventsAttended", membership.getEventsAttended());
        stats.put("serviceHours", membership.getServiceHours());
        stats.put("joinDate", membership.getJoinDate());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getClubsOverview(String academicYear) {
        Map<String, Object> overview = new HashMap<>();

        List<Club> activeClubs = clubRepository.findByActiveTrueOrderByNameAsc();
        overview.put("totalClubs", activeClubs.size());

        long activeMemberships = membershipRepository.countActiveByYear(academicYear);
        overview.put("activeMemberships", activeMemberships);

        List<ClubActivity> upcomingActivities = getAllUpcomingActivities();
        overview.put("upcomingActivities", upcomingActivities.size());

        return overview;
    }

    @Override
    @Transactional(readOnly = true)
    public int getClubServiceHours(Long clubId) {
        return activityRepository.sumServiceHoursByClub(clubId);
    }
}
