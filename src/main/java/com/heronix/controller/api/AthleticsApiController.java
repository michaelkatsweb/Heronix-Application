package com.heronix.controller.api;

import com.heronix.model.domain.AthleticEvent;
import com.heronix.model.domain.AthleticTeam;
import com.heronix.model.domain.TeamMembership;
import com.heronix.service.AthleticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Athletics Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@RestController
@RequestMapping("/api/athletics")
@RequiredArgsConstructor
public class AthleticsApiController {

    private final AthleticsService athleticsService;

    // ========== Team Management ==========

    @PostMapping("/teams")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR')")
    public ResponseEntity<AthleticTeam> createTeam(@RequestBody AthleticTeam team) {
        AthleticTeam created = athleticsService.createTeam(team);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/teams/{teamId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR')")
    public ResponseEntity<AthleticTeam> updateTeam(
            @PathVariable Long teamId,
            @RequestBody AthleticTeam team) {
        AthleticTeam updated = athleticsService.updateTeam(teamId, team);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/teams/{teamId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR')")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long teamId) {
        athleticsService.deleteTeam(teamId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/teams/{teamId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<AthleticTeam> getTeamById(@PathVariable Long teamId) {
        AthleticTeam team = athleticsService.getTeamById(teamId);
        return ResponseEntity.ok(team);
    }

    @GetMapping("/teams")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<AthleticTeam>> getAllTeams() {
        List<AthleticTeam> teams = athleticsService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/teams/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<AthleticTeam>> getActiveTeams() {
        List<AthleticTeam> teams = athleticsService.getActiveTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/teams/year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<AthleticTeam>> getTeamsByYear(@PathVariable String academicYear) {
        List<AthleticTeam> teams = athleticsService.getTeamsByYear(academicYear);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/teams/sport/{sport}/year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<AthleticTeam>> getTeamsBySport(
            @PathVariable AthleticTeam.Sport sport,
            @PathVariable String academicYear) {
        List<AthleticTeam> teams = athleticsService.getTeamsBySport(sport, academicYear);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/teams/season/{season}/year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<AthleticTeam>> getTeamsBySeason(
            @PathVariable AthleticTeam.Season season,
            @PathVariable String academicYear) {
        List<AthleticTeam> teams = athleticsService.getTeamsBySeason(season, academicYear);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/teams/accepting-members/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<AthleticTeam>> getTeamsAcceptingMembers(@PathVariable String academicYear) {
        List<AthleticTeam> teams = athleticsService.getTeamsAcceptingMembers(academicYear);
        return ResponseEntity.ok(teams);
    }

    // ========== Team Membership Management ==========

    @PostMapping("/memberships")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<TeamMembership> addPlayerToTeam(
            @RequestParam Long teamId,
            @RequestParam Long studentId,
            @RequestParam String academicYear,
            @RequestParam String position) {
        TeamMembership membership = athleticsService.addPlayerToTeam(teamId, studentId, academicYear, position);
        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

    @PutMapping("/memberships/{membershipId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<TeamMembership> updateMembership(
            @PathVariable Long membershipId,
            @RequestBody TeamMembership membership) {
        TeamMembership updated = athleticsService.updateMembership(membershipId, membership);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/memberships/{membershipId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<Void> removePlayerFromTeam(@PathVariable Long membershipId) {
        athleticsService.removePlayerFromTeam(membershipId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/memberships/{membershipId}/captain")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<TeamMembership> assignCaptain(@PathVariable Long membershipId) {
        TeamMembership updated = athleticsService.assignCaptain(membershipId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/memberships/{membershipId}/co-captain")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<TeamMembership> assignCoCaptain(@PathVariable Long membershipId) {
        TeamMembership updated = athleticsService.assignCoCaptain(membershipId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/memberships/{membershipId}/eligibility")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<TeamMembership> updateEligibility(
            @PathVariable Long membershipId,
            @RequestParam boolean eligible,
            @RequestParam String notes) {
        TeamMembership updated = athleticsService.updateEligibility(membershipId, eligible, notes);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/memberships/{membershipId}/physical")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<TeamMembership> updatePhysical(
            @PathVariable Long membershipId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate) {
        TeamMembership updated = athleticsService.updatePhysical(membershipId, expirationDate);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/memberships/{membershipId}/documents")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<TeamMembership> updateMembershipDocuments(
            @PathVariable Long membershipId,
            @RequestParam boolean physical,
            @RequestParam boolean consent,
            @RequestParam boolean emergency) {
        TeamMembership updated = athleticsService.updateMembershipDocuments(
                membershipId, physical, consent, emergency);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/teams/{teamId}/roster")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<TeamMembership>> getTeamRoster(@PathVariable Long teamId) {
        List<TeamMembership> roster = athleticsService.getTeamRoster(teamId);
        return ResponseEntity.ok(roster);
    }

    @GetMapping("/teams/{teamId}/roster/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<TeamMembership>> getActiveRoster(@PathVariable Long teamId) {
        List<TeamMembership> roster = athleticsService.getActiveRoster(teamId);
        return ResponseEntity.ok(roster);
    }

    @GetMapping("/students/{studentId}/teams")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<TeamMembership>> getStudentTeams(@PathVariable Long studentId) {
        List<TeamMembership> teams = athleticsService.getStudentTeams(studentId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/students/{studentId}/teams/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<TeamMembership>> getStudentActiveTeams(@PathVariable Long studentId) {
        List<TeamMembership> teams = athleticsService.getStudentActiveTeams(studentId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/memberships/ineligible")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<List<TeamMembership>> getIneligiblePlayers() {
        List<TeamMembership> players = athleticsService.getIneligiblePlayers();
        return ResponseEntity.ok(players);
    }

    @GetMapping("/memberships/physicals-needed")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<List<TeamMembership>> getPlayersNeedingPhysicals() {
        List<TeamMembership> players = athleticsService.getPlayersNeedingPhysicals();
        return ResponseEntity.ok(players);
    }

    @GetMapping("/memberships/missing-documents")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<List<TeamMembership>> getPlayersWithMissingDocuments() {
        List<TeamMembership> players = athleticsService.getPlayersWithMissingDocuments();
        return ResponseEntity.ok(players);
    }

    // ========== Event Management ==========

    @PostMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<AthleticEvent> scheduleEvent(@RequestBody AthleticEvent event) {
        AthleticEvent created = athleticsService.scheduleEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/events/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<AthleticEvent> updateEvent(
            @PathVariable Long eventId,
            @RequestBody AthleticEvent event) {
        AthleticEvent updated = athleticsService.updateEvent(eventId, event);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/events/{eventId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<Void> cancelEvent(
            @PathVariable Long eventId,
            @RequestParam String reason) {
        athleticsService.cancelEvent(eventId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/{eventId}/postpone")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<Void> postponeEvent(
            @PathVariable Long eventId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDate) {
        athleticsService.postponeEvent(eventId, newDate);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/{eventId}/result")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<AthleticEvent> recordGameResult(
            @PathVariable Long eventId,
            @RequestParam Integer teamScore,
            @RequestParam Integer opponentScore) {
        AthleticEvent updated = athleticsService.recordGameResult(eventId, teamScore, opponentScore);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/events/{eventId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH')")
    public ResponseEntity<AthleticEvent> updateEventAttendance(
            @PathVariable Long eventId,
            @RequestParam Integer attendance) {
        AthleticEvent updated = athleticsService.updateEventAttendance(eventId, attendance);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/events/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        athleticsService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<AthleticEvent> getEventById(@PathVariable Long eventId) {
        AthleticEvent event = athleticsService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/teams/{teamId}/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<AthleticEvent>> getTeamEvents(@PathVariable Long teamId) {
        List<AthleticEvent> events = athleticsService.getTeamEvents(teamId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/teams/{teamId}/events/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<AthleticEvent>> getUpcomingEvents(@PathVariable Long teamId) {
        List<AthleticEvent> events = athleticsService.getUpcomingEvents(teamId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<AthleticEvent>> getAllUpcomingEvents() {
        List<AthleticEvent> events = athleticsService.getAllUpcomingEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/teams/{teamId}/games/completed")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<AthleticEvent>> getCompletedGames(@PathVariable Long teamId) {
        List<AthleticEvent> games = athleticsService.getCompletedGames(teamId);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/events/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER')")
    public ResponseEntity<List<AthleticEvent>> getEventsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AthleticEvent> events = athleticsService.getEventsInDateRange(startDate, endDate);
        return ResponseEntity.ok(events);
    }

    // ========== Statistics and Reporting ==========

    @GetMapping("/teams/{teamId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER')")
    public ResponseEntity<Map<String, Object>> getTeamStatistics(@PathVariable Long teamId) {
        Map<String, Object> stats = athleticsService.getTeamStatistics(teamId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/memberships/{membershipId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<Map<String, Object>> getPlayerStatistics(@PathVariable Long membershipId) {
        Map<String, Object> stats = athleticsService.getPlayerStatistics(membershipId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/reports/overview/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR')")
    public ResponseEntity<Map<String, Object>> getAthleticsOverview(@PathVariable String academicYear) {
        Map<String, Object> overview = athleticsService.getAthleticsOverview(academicYear);
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/reports/sport-participation/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR')")
    public ResponseEntity<List<Map<String, Object>>> getSportParticipation(@PathVariable String academicYear) {
        List<Map<String, Object>> participation = athleticsService.getSportParticipation(academicYear);
        return ResponseEntity.ok(participation);
    }

    @GetMapping("/teams/{teamId}/record")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATHLETIC_DIRECTOR', 'COACH', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<Map<String, Object>> getTeamRecord(@PathVariable Long teamId) {
        Map<String, Object> record = athleticsService.getTeamRecord(teamId);
        return ResponseEntity.ok(record);
    }
}
