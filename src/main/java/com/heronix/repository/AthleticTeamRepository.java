package com.heronix.repository;

import com.heronix.model.domain.AthleticTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AthleticTeam entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface AthleticTeamRepository extends JpaRepository<AthleticTeam, Long> {

    List<AthleticTeam> findByActiveTrue();

    List<AthleticTeam> findByActiveTrueOrderByTeamNameAsc();

    List<AthleticTeam> findByAcademicYear(String academicYear);

    List<AthleticTeam> findByAcademicYearAndActiveTrue(String academicYear);

    List<AthleticTeam> findBySportAndAcademicYear(AthleticTeam.Sport sport, String academicYear);

    List<AthleticTeam> findBySeasonAndAcademicYear(AthleticTeam.Season season, String academicYear);

    List<AthleticTeam> findByLevelAndAcademicYear(AthleticTeam.TeamLevel level, String academicYear);

    List<AthleticTeam> findByStatus(AthleticTeam.TeamStatus status);

    List<AthleticTeam> findByHeadCoachId(Long coachId);

    @Query("SELECT t FROM AthleticTeam t WHERE t.academicYear = :academicYear AND t.active = true AND t.status = 'ACTIVE' ORDER BY t.sport, t.level")
    List<AthleticTeam> findActiveTeamsByYear(@Param("academicYear") String academicYear);

    @Query("SELECT t FROM AthleticTeam t WHERE t.academicYear = :academicYear AND t.currentRosterSize < t.maxRosterSize AND t.active = true")
    List<AthleticTeam> findTeamsAcceptingMembers(@Param("academicYear") String academicYear);

    @Query("SELECT COUNT(t) FROM AthleticTeam t WHERE t.academicYear = :academicYear AND t.active = true")
    long countActiveTeamsByYear(@Param("academicYear") String academicYear);

    @Query("SELECT COUNT(t) FROM AthleticTeam t WHERE t.sport = :sport AND t.academicYear = :academicYear AND t.active = true")
    long countBySportAndYear(@Param("sport") AthleticTeam.Sport sport, @Param("academicYear") String academicYear);

    @Query("SELECT COALESCE(SUM(t.currentRosterSize), 0) FROM AthleticTeam t WHERE t.academicYear = :academicYear AND t.active = true")
    int countTotalAthletes(@Param("academicYear") String academicYear);
}
