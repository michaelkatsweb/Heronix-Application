package com.heronix.service.impl;

import com.heronix.model.domain.AthleticEvent;
import com.heronix.model.domain.AthleticTeam;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.TeamMembership;
import com.heronix.repository.AthleticEventRepository;
import com.heronix.repository.AthleticTeamRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.TeamMembershipRepository;
import com.heronix.service.AthleticsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of AthleticsService
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Service
@RequiredArgsConstructor
public class AthleticsServiceImpl implements AthleticsService {

    private final AthleticTeamRepository teamRepository;
    private final TeamMembershipRepository membershipRepository;
    private final AthleticEventRepository eventRepository;
    private final StudentRepository studentRepository;

    // ========== Team Management ==========

    @Override
    @Transactional
    public AthleticTeam createTeam(AthleticTeam team) {
        team.setActive(true);
        return teamRepository.save(team);
    }

    @Override
    @Transactional
    public AthleticTeam updateTeam(Long teamId, AthleticTeam team) {
        AthleticTeam existing = getTeamById(teamId);
        existing.setTeamName(team.getTeamName());
        existing.setSport(team.getSport());
        existing.setSeason(team.getSeason());
        existing.setLevel(team.getLevel());
        existing.setGender(team.getGender());
        existing.setGradeLevel(team.getGradeLevel());
        existing.setHeadCoach(team.getHeadCoach());
        existing.setAssistantCoach(team.getAssistantCoach());
        existing.setMaxRosterSize(team.getMaxRosterSize());
        existing.setSeasonStartDate(team.getSeasonStartDate());
        existing.setSeasonEndDate(team.getSeasonEndDate());
        existing.setHomeVenue(team.getHomeVenue());
        existing.setPracticeSchedule(team.getPracticeSchedule());
        existing.setStatus(team.getStatus());
        existing.setConference(team.getConference());
        existing.setDivision(team.getDivision());
        existing.setDescription(team.getDescription());
        existing.setRequirements(team.getRequirements());
        existing.setTryoutsRequired(team.getTryoutsRequired());
        existing.setTryoutDate(team.getTryoutDate());
        return teamRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteTeam(Long teamId) {
        AthleticTeam team = getTeamById(teamId);
        team.setActive(false);
        teamRepository.save(team);
    }

    @Override
    @Transactional(readOnly = true)
    public AthleticTeam getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + teamId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AthleticTeam> getAllTeams() {
        return teamRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AthleticTeam> getActiveTeams() {
        return teamRepository.findByActiveTrueOrderByTeamNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AthleticTeam> getTeamsByYear(String academicYear) {
        return teamRepository.findActiveTeamsByYear(academicYear);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AthleticTeam> getTeamsBySport(AthleticTeam.Sport sport, String academicYear) {
        return teamRepository.findBySportAndAcademicYear(sport, academicYear);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AthleticTeam> getTeamsBySeason(AthleticTeam.Season season, String academicYear) {
        return teamRepository.findBySeasonAndAcademicYear(season, academicYear);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AthleticTeam> getTeamsAcceptingMembers(String academicYear) {
        return teamRepository.findTeamsAcceptingMembers(academicYear);
    }

    // ========== Team Membership Management ==========

    @Override
    @Transactional
    public TeamMembership addPlayerToTeam(Long teamId, Long studentId, String academicYear, String position) {
        AthleticTeam team = getTeamById(teamId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));

        if (team.isRosterFull()) {
            throw new IllegalStateException("Team roster is full");
        }

        TeamMembership membership = TeamMembership.builder()
                .team(team)
                .student(student)
                .academicYear(academicYear)
                .position(position)
                .status(TeamMembership.MembershipStatus.ACTIVE)
                .joinDate(LocalDate.now())
                .eligible(true)
                .build();

        TeamMembership saved = membershipRepository.save(membership);

        team.setCurrentRosterSize(team.getCurrentRosterSize() + 1);
        teamRepository.save(team);

        return saved;
    }

    @Override
    @Transactional
    public TeamMembership updateMembership(Long membershipId, TeamMembership membership) {
        TeamMembership existing = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + membershipId));

        existing.setPosition(membership.getPosition());
        existing.setJerseyNumber(membership.getJerseyNumber());
        existing.setStatus(membership.getStatus());
        existing.setGpa(membership.getGpa());
        existing.setStatistics(membership.getStatistics());
        existing.setAwards(membership.getAwards());
        existing.setNotes(membership.getNotes());

        return membershipRepository.save(existing);
    }

    @Override
    @Transactional
    public void removePlayerFromTeam(Long membershipId) {
        TeamMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + membershipId));

        membership.setStatus(TeamMembership.MembershipStatus.QUIT);
        membership.setLeaveDate(LocalDate.now());
        membershipRepository.save(membership);

        AthleticTeam team = membership.getTeam();
        team.setCurrentRosterSize(Math.max(0, team.getCurrentRosterSize() - 1));
        teamRepository.save(team);
    }

    @Override
    @Transactional
    public TeamMembership assignCaptain(Long membershipId) {
        TeamMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + membershipId));
        membership.setCaptain(true);
        return membershipRepository.save(membership);
    }

    @Override
    @Transactional
    public TeamMembership assignCoCaptain(Long membershipId) {
        TeamMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + membershipId));
        membership.setCoCaptain(true);
        return membershipRepository.save(membership);
    }

    @Override
    @Transactional
    public TeamMembership updateEligibility(Long membershipId, boolean eligible, String notes) {
        TeamMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + membershipId));
        membership.setEligible(eligible);
        membership.setEligibilityNotes(notes);
        return membershipRepository.save(membership);
    }

    @Override
    @Transactional
    public TeamMembership updatePhysical(Long membershipId, LocalDate expirationDate) {
        TeamMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + membershipId));
        membership.setPhysicalOnFile(true);
        membership.setPhysicalExpirationDate(expirationDate);
        return membershipRepository.save(membership);
    }

    @Override
    @Transactional
    public TeamMembership updateMembershipDocuments(Long membershipId, boolean physical, boolean consent, boolean emergency) {
        TeamMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + membershipId));
        membership.setPhysicalOnFile(physical);
        membership.setConsentFormSigned(consent);
        membership.setEmergencyContactOnFile(emergency);
        return membershipRepository.save(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMembership> getTeamRoster(Long teamId) {
        return membershipRepository.findByTeamId(teamId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMembership> getActiveRoster(Long teamId) {
        return membershipRepository.findActiveRoster(teamId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMembership> getStudentTeams(Long studentId) {
        return membershipRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMembership> getStudentActiveTeams(Long studentId) {
        return membershipRepository.findActiveByStudent(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMembership> getIneligiblePlayers() {
        return membershipRepository.findIneligiblePlayers();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMembership> getPlayersNeedingPhysicals() {
        return membershipRepository.findExpiredPhysicals(LocalDate.now().plusDays(30));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMembership> getPlayersWithMissingDocuments() {
        return membershipRepository.findMissingRequiredDocuments();
    }

    // ========== Event Management ==========

    @Override
    @Transactional
    public AthleticEvent scheduleEvent(AthleticEvent event) {
        event.setStatus(AthleticEvent.EventStatus.SCHEDULED);
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public AthleticEvent updateEvent(Long eventId, AthleticEvent event) {
        AthleticEvent existing = getEventById(eventId);
        existing.setEventType(event.getEventType());
        existing.setEventName(event.getEventName());
        existing.setEventDate(event.getEventDate());
        existing.setEndDate(event.getEndDate());
        existing.setOpponent(event.getOpponent());
        existing.setLocation(event.getLocation());
        existing.setAddress(event.getAddress());
        existing.setHomeAway(event.getHomeAway());
        existing.setConference(event.getConference());
        existing.setPlayoff(event.getPlayoff());
        existing.setTournament(event.getTournament());
        existing.setTournamentName(event.getTournamentName());
        existing.setTransportation(event.getTransportation());
        existing.setDepartureTime(event.getDepartureTime());
        existing.setReturnTime(event.getReturnTime());
        existing.setUniformRequirements(event.getUniformRequirements());
        existing.setSpecialInstructions(event.getSpecialInstructions());
        return eventRepository.save(existing);
    }

    @Override
    @Transactional
    public void cancelEvent(Long eventId, String reason) {
        AthleticEvent event = getEventById(eventId);
        event.setStatus(AthleticEvent.EventStatus.CANCELLED);
        event.setCancelled(true);
        event.setCancellationReason(reason);
        eventRepository.save(event);
    }

    @Override
    @Transactional
    public void postponeEvent(Long eventId, LocalDateTime newDate) {
        AthleticEvent event = getEventById(eventId);
        event.setStatus(AthleticEvent.EventStatus.POSTPONED);
        event.setPostponed(true);
        event.setRescheduleDate(newDate);
        eventRepository.save(event);
    }

    @Override
    @Transactional
    public AthleticEvent recordGameResult(Long eventId, Integer teamScore, Integer opponentScore) {
        AthleticEvent event = getEventById(eventId);
        event.setTeamScore(teamScore);
        event.setOpponentScore(opponentScore);
        event.setStatus(AthleticEvent.EventStatus.COMPLETED);

        if (teamScore > opponentScore) {
            event.setResult(AthleticEvent.GameResult.WIN);
        } else if (teamScore < opponentScore) {
            event.setResult(AthleticEvent.GameResult.LOSS);
        } else {
            event.setResult(AthleticEvent.GameResult.TIE);
        }

        AthleticEvent saved = eventRepository.save(event);

        updateTeamRecord(event.getTeam());

        return saved;
    }

    @Override
    @Transactional
    public AthleticEvent updateEventAttendance(Long eventId, Integer attendance) {
        AthleticEvent event = getEventById(eventId);
        event.setAttendance(attendance);
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        eventRepository.deleteById(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public AthleticEvent getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AthleticEvent> getTeamEvents(Long teamId) {
        return eventRepository.findByTeamIdOrderByEventDateDesc(teamId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AthleticEvent> getUpcomingEvents(Long teamId) {
        return eventRepository.findUpcomingByTeam(teamId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AthleticEvent> getAllUpcomingEvents() {
        return eventRepository.findAllUpcoming(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AthleticEvent> getCompletedGames(Long teamId) {
        return eventRepository.findCompletedByTeam(teamId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AthleticEvent> getEventsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return eventRepository.findEventsInDateRange(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AthleticEvent> getTeamEventsInDateRange(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        return eventRepository.findTeamEventsInDateRange(teamId, startDate, endDate);
    }

    // ========== Statistics and Reporting ==========

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTeamStatistics(Long teamId) {
        AthleticTeam team = getTeamById(teamId);
        Map<String, Object> stats = new HashMap<>();

        stats.put("teamId", teamId);
        stats.put("teamName", team.getTeamName());
        stats.put("sport", team.getSport());
        stats.put("wins", team.getWins());
        stats.put("losses", team.getLosses());
        stats.put("ties", team.getTies());
        stats.put("record", team.getRecord());
        stats.put("winningPercentage", team.getWinningPercentage());
        stats.put("rosterSize", team.getCurrentRosterSize());
        stats.put("maxRosterSize", team.getMaxRosterSize());

        long activeRoster = membershipRepository.countActiveByTeam(teamId);
        stats.put("activeRoster", activeRoster);

        List<AthleticEvent> upcomingEvents = getUpcomingEvents(teamId);
        stats.put("upcomingGames", upcomingEvents.size());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPlayerStatistics(Long membershipId) {
        TeamMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + membershipId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("membershipId", membershipId);
        stats.put("studentName", membership.getStudent().getFullName());
        stats.put("teamName", membership.getTeam().getTeamName());
        stats.put("position", membership.getPosition());
        stats.put("jerseyNumber", membership.getJerseyNumber());
        stats.put("gamesPlayed", membership.getGamesPlayed());
        stats.put("gamesStarted", membership.getGamesStarted());
        stats.put("isCaptain", membership.getCaptain());
        stats.put("isStarter", membership.getStarter());
        stats.put("eligible", membership.getEligible());
        stats.put("gpa", membership.getGpa());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAthleticsOverview(String academicYear) {
        Map<String, Object> overview = new HashMap<>();

        long totalTeams = teamRepository.countActiveTeamsByYear(academicYear);
        int totalAthletes = teamRepository.countTotalAthletes(academicYear);
        long activeMemberships = membershipRepository.countActiveByYear(academicYear);

        overview.put("academicYear", academicYear);
        overview.put("totalTeams", totalTeams);
        overview.put("totalAthletes", totalAthletes);
        overview.put("activeMemberships", activeMemberships);

        List<AthleticEvent> upcomingEvents = getAllUpcomingEvents();
        overview.put("upcomingEvents", upcomingEvents.size());

        return overview;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSportParticipation(String academicYear) {
        return teamRepository.findActiveTeamsByYear(academicYear).stream()
                .collect(Collectors.groupingBy(AthleticTeam::getSport))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> sportData = new HashMap<>();
                    sportData.put("sport", entry.getKey().toString());
                    sportData.put("teams", entry.getValue().size());
                    int athletes = entry.getValue().stream()
                            .mapToInt(AthleticTeam::getCurrentRosterSize)
                            .sum();
                    sportData.put("athletes", athletes);
                    return sportData;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTeamRecord(Long teamId) {
        AthleticTeam team = getTeamById(teamId);
        Map<String, Object> record = new HashMap<>();

        record.put("teamId", teamId);
        record.put("teamName", team.getTeamName());
        record.put("wins", team.getWins());
        record.put("losses", team.getLosses());
        record.put("ties", team.getTies());
        record.put("record", team.getRecord());
        record.put("winningPercentage", team.getWinningPercentage());

        return record;
    }

    // ========== Helper Methods ==========

    private void updateTeamRecord(AthleticTeam team) {
        long wins = eventRepository.countWins(team.getId());
        long losses = eventRepository.countLosses(team.getId());
        long ties = eventRepository.countTies(team.getId());

        team.setWins((int) wins);
        team.setLosses((int) losses);
        team.setTies((int) ties);

        teamRepository.save(team);
    }
}
