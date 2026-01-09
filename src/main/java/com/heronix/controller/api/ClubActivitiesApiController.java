package com.heronix.controller.api;

import com.heronix.model.domain.ClubActivity;
import com.heronix.model.domain.ClubMembership;
import com.heronix.service.ClubActivitiesService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Club Activities Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubActivitiesApiController {

    private final ClubActivitiesService clubActivitiesService;

    // ========== Club Membership Management ==========

    @PostMapping("/memberships")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ClubMembership> joinClub(
            @RequestParam Long clubId,
            @RequestParam Long studentId,
            @RequestParam String academicYear) {
        ClubMembership membership = clubActivitiesService.joinClub(clubId, studentId, academicYear);
        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

    @PutMapping("/memberships/{membershipId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ClubMembership> updateMembership(
            @PathVariable Long membershipId,
            @RequestBody ClubMembership membership) {
        ClubMembership updated = clubActivitiesService.updateMembership(membershipId, membership);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/memberships/{membershipId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Void> leaveClub(@PathVariable Long membershipId) {
        clubActivitiesService.leaveClub(membershipId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/memberships/{membershipId}/officer")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ClubMembership> assignOfficer(
            @PathVariable Long membershipId,
            @RequestParam ClubMembership.MembershipRole role) {
        ClubMembership updated = clubActivitiesService.assignOfficer(membershipId, role);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/memberships/{membershipId}/service-hours")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ClubMembership> recordServiceHours(
            @PathVariable Long membershipId,
            @RequestParam Integer hours) {
        ClubMembership updated = clubActivitiesService.recordServiceHours(membershipId, hours);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/memberships/{membershipId}/parent-consent")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'PARENT')")
    public ResponseEntity<ClubMembership> updateParentConsent(
            @PathVariable Long membershipId,
            @RequestParam boolean received) {
        ClubMembership updated = clubActivitiesService.updateParentConsent(membershipId, received);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{clubId}/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<ClubMembership>> getClubMembers(@PathVariable Long clubId) {
        List<ClubMembership> members = clubActivitiesService.getClubMembers(clubId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{clubId}/members/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<ClubMembership>> getActiveClubMembers(@PathVariable Long clubId) {
        List<ClubMembership> members = clubActivitiesService.getActiveClubMembers(clubId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{clubId}/officers")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<ClubMembership>> getClubOfficers(@PathVariable Long clubId) {
        List<ClubMembership> officers = clubActivitiesService.getClubOfficers(clubId);
        return ResponseEntity.ok(officers);
    }

    @GetMapping("/students/{studentId}/clubs")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<ClubMembership>> getStudentClubs(@PathVariable Long studentId) {
        List<ClubMembership> clubs = clubActivitiesService.getStudentClubs(studentId);
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/students/{studentId}/clubs/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<ClubMembership>> getStudentActiveClubs(@PathVariable Long studentId) {
        List<ClubMembership> clubs = clubActivitiesService.getStudentActiveClubs(studentId);
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/students/{studentId}/service-hours")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<Map<String, Integer>> getStudentTotalServiceHours(@PathVariable Long studentId) {
        int hours = clubActivitiesService.getStudentTotalServiceHours(studentId);
        Map<String, Integer> response = new HashMap<>();
        response.put("totalServiceHours", hours);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/memberships/missing-consent")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ClubMembership>> getMembersNeedingConsent() {
        List<ClubMembership> members = clubActivitiesService.getMembersNeedingConsent();
        return ResponseEntity.ok(members);
    }

    // ========== Club Activity Management ==========

    @PostMapping("/activities")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ClubActivity> scheduleActivity(@RequestBody ClubActivity activity) {
        ClubActivity created = clubActivitiesService.scheduleActivity(activity);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/activities/{activityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ClubActivity> updateActivity(
            @PathVariable Long activityId,
            @RequestBody ClubActivity activity) {
        ClubActivity updated = clubActivitiesService.updateActivity(activityId, activity);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/activities/{activityId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> cancelActivity(
            @PathVariable Long activityId,
            @RequestParam String reason) {
        clubActivitiesService.cancelActivity(activityId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/activities/{activityId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ClubActivity> recordAttendance(
            @PathVariable Long activityId,
            @RequestParam Integer attendance) {
        ClubActivity updated = clubActivitiesService.recordAttendance(activityId, attendance);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/activities/{activityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long activityId) {
        clubActivitiesService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/activities/{activityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ClubActivity> getActivityById(@PathVariable Long activityId) {
        ClubActivity activity = clubActivitiesService.getActivityById(activityId);
        return ResponseEntity.ok(activity);
    }

    @GetMapping("/{clubId}/activities")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<ClubActivity>> getClubActivities(@PathVariable Long clubId) {
        List<ClubActivity> activities = clubActivitiesService.getClubActivities(clubId);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/{clubId}/activities/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<ClubActivity>> getUpcomingActivities(@PathVariable Long clubId) {
        List<ClubActivity> activities = clubActivitiesService.getUpcomingActivities(clubId);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/{clubId}/activities/completed")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<ClubActivity>> getCompletedActivities(@PathVariable Long clubId) {
        List<ClubActivity> activities = clubActivitiesService.getCompletedActivities(clubId);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/activities/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<ClubActivity>> getAllUpcomingActivities() {
        List<ClubActivity> activities = clubActivitiesService.getAllUpcomingActivities();
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/activities/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ClubActivity>> getActivitiesInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ClubActivity> activities = clubActivitiesService.getActivitiesInDateRange(startDate, endDate);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/activities/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<ClubActivity>> getOpenActivities() {
        List<ClubActivity> activities = clubActivitiesService.getOpenActivities();
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/activities/service-projects")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<ClubActivity>> getServiceProjects() {
        List<ClubActivity> projects = clubActivitiesService.getServiceProjects();
        return ResponseEntity.ok(projects);
    }

    // ========== Reporting ==========

    @GetMapping("/{clubId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Map<String, Object>> getClubStatistics(@PathVariable Long clubId) {
        Map<String, Object> stats = clubActivitiesService.getClubStatistics(clubId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/memberships/{membershipId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<Map<String, Object>> getMemberStatistics(@PathVariable Long membershipId) {
        Map<String, Object> stats = clubActivitiesService.getMemberStatistics(membershipId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/reports/overview/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Map<String, Object>> getClubsOverview(@PathVariable String academicYear) {
        Map<String, Object> overview = clubActivitiesService.getClubsOverview(academicYear);
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/{clubId}/service-hours")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<Map<String, Integer>> getClubServiceHours(@PathVariable Long clubId) {
        int hours = clubActivitiesService.getClubServiceHours(clubId);
        Map<String, Integer> response = new HashMap<>();
        response.put("totalServiceHours", hours);
        return ResponseEntity.ok(response);
    }
}
