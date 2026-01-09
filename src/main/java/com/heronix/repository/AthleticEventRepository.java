package com.heronix.repository;

import com.heronix.model.domain.AthleticEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AthleticEvent entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface AthleticEventRepository extends JpaRepository<AthleticEvent, Long> {

    List<AthleticEvent> findByTeamId(Long teamId);

    List<AthleticEvent> findByTeamIdOrderByEventDateDesc(Long teamId);

    List<AthleticEvent> findByEventType(AthleticEvent.EventType eventType);

    List<AthleticEvent> findByStatus(AthleticEvent.EventStatus status);

    @Query("SELECT e FROM AthleticEvent e WHERE e.team.id = :teamId AND e.eventDate > :now AND e.status != 'CANCELLED' ORDER BY e.eventDate")
    List<AthleticEvent> findUpcomingByTeam(@Param("teamId") Long teamId, @Param("now") LocalDateTime now);

    @Query("SELECT e FROM AthleticEvent e WHERE e.eventDate > :now AND e.status != 'CANCELLED' ORDER BY e.eventDate")
    List<AthleticEvent> findAllUpcoming(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM AthleticEvent e WHERE e.team.id = :teamId AND e.status = 'COMPLETED' ORDER BY e.eventDate DESC")
    List<AthleticEvent> findCompletedByTeam(@Param("teamId") Long teamId);

    @Query("SELECT e FROM AthleticEvent e WHERE e.eventDate BETWEEN :startDate AND :endDate ORDER BY e.eventDate")
    List<AthleticEvent> findEventsInDateRange(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM AthleticEvent e WHERE e.team.id = :teamId AND e.eventDate BETWEEN :startDate AND :endDate ORDER BY e.eventDate")
    List<AthleticEvent> findTeamEventsInDateRange(@Param("teamId") Long teamId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM AthleticEvent e WHERE e.team.id = :teamId AND e.status = 'COMPLETED' AND e.result = :result")
    List<AthleticEvent> findByTeamAndResult(@Param("teamId") Long teamId,
                                              @Param("result") AthleticEvent.GameResult result);

    @Query("SELECT COUNT(e) FROM AthleticEvent e WHERE e.team.id = :teamId AND e.status = 'COMPLETED' AND e.result = 'WIN'")
    long countWins(@Param("teamId") Long teamId);

    @Query("SELECT COUNT(e) FROM AthleticEvent e WHERE e.team.id = :teamId AND e.status = 'COMPLETED' AND e.result = 'LOSS'")
    long countLosses(@Param("teamId") Long teamId);

    @Query("SELECT COUNT(e) FROM AthleticEvent e WHERE e.team.id = :teamId AND e.status = 'COMPLETED' AND e.result = 'TIE'")
    long countTies(@Param("teamId") Long teamId);

    @Query("SELECT e FROM AthleticEvent e WHERE e.homeAway = 'HOME' AND e.eventDate > :now AND e.status != 'CANCELLED' ORDER BY e.eventDate")
    List<AthleticEvent> findUpcomingHomeEvents(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM AthleticEvent e WHERE e.playoff = true OR e.tournament = true ORDER BY e.eventDate DESC")
    List<AthleticEvent> findPlayoffAndTournamentEvents();
}
