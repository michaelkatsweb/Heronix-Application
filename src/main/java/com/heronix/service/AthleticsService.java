package com.heronix.service;

import com.heronix.model.domain.AthleticEvent;
import com.heronix.model.domain.AthleticTeam;
import com.heronix.model.domain.TeamMembership;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Athletics Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
public interface AthleticsService {

    // Team Management
    AthleticTeam createTeam(AthleticTeam team);
    AthleticTeam updateTeam(Long teamId, AthleticTeam team);
    void deleteTeam(Long teamId);
    AthleticTeam getTeamById(Long teamId);
    List<AthleticTeam> getAllTeams();
    List<AthleticTeam> getActiveTeams();
    List<AthleticTeam> getTeamsByYear(String academicYear);
    List<AthleticTeam> getTeamsBySport(AthleticTeam.Sport sport, String academicYear);
    List<AthleticTeam> getTeamsBySeason(AthleticTeam.Season season, String academicYear);
    List<AthleticTeam> getTeamsAcceptingMembers(String academicYear);

    // Team Membership Management
    TeamMembership addPlayerToTeam(Long teamId, Long studentId, String academicYear, String position);
    TeamMembership updateMembership(Long membershipId, TeamMembership membership);
    void removePlayerFromTeam(Long membershipId);
    TeamMembership assignCaptain(Long membershipId);
    TeamMembership assignCoCaptain(Long membershipId);
    TeamMembership updateEligibility(Long membershipId, boolean eligible, String notes);
    TeamMembership updatePhysical(Long membershipId, java.time.LocalDate expirationDate);
    TeamMembership updateMembershipDocuments(Long membershipId, boolean physical, boolean consent, boolean emergency);
    List<TeamMembership> getTeamRoster(Long teamId);
    List<TeamMembership> getActiveRoster(Long teamId);
    List<TeamMembership> getStudentTeams(Long studentId);
    List<TeamMembership> getStudentActiveTeams(Long studentId);
    List<TeamMembership> getIneligiblePlayers();
    List<TeamMembership> getPlayersNeedingPhysicals();
    List<TeamMembership> getPlayersWithMissingDocuments();

    // Event Management
    AthleticEvent scheduleEvent(AthleticEvent event);
    AthleticEvent updateEvent(Long eventId, AthleticEvent event);
    void cancelEvent(Long eventId, String reason);
    void postponeEvent(Long eventId, LocalDateTime newDate);
    AthleticEvent recordGameResult(Long eventId, Integer teamScore, Integer opponentScore);
    AthleticEvent updateEventAttendance(Long eventId, Integer attendance);
    void deleteEvent(Long eventId);
    AthleticEvent getEventById(Long eventId);
    List<AthleticEvent> getTeamEvents(Long teamId);
    List<AthleticEvent> getUpcomingEvents(Long teamId);
    List<AthleticEvent> getAllUpcomingEvents();
    List<AthleticEvent> getCompletedGames(Long teamId);
    List<AthleticEvent> getEventsInDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<AthleticEvent> getTeamEventsInDateRange(Long teamId, LocalDateTime startDate, LocalDateTime endDate);

    // Statistics and Reporting
    Map<String, Object> getTeamStatistics(Long teamId);
    Map<String, Object> getPlayerStatistics(Long membershipId);
    Map<String, Object> getAthleticsOverview(String academicYear);
    List<Map<String, Object>> getSportParticipation(String academicYear);
    Map<String, Object> getTeamRecord(Long teamId);
}
