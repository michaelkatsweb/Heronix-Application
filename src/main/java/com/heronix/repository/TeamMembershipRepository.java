package com.heronix.repository;

import com.heronix.model.domain.TeamMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TeamMembership entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface TeamMembershipRepository extends JpaRepository<TeamMembership, Long> {

    List<TeamMembership> findByStudentId(Long studentId);

    List<TeamMembership> findByTeamId(Long teamId);

    List<TeamMembership> findByStudentIdAndAcademicYear(Long studentId, String academicYear);

    List<TeamMembership> findByTeamIdAndAcademicYear(Long teamId, String academicYear);

    Optional<TeamMembership> findByTeamIdAndStudentIdAndAcademicYear(Long teamId, Long studentId, String academicYear);

    @Query("SELECT m FROM TeamMembership m WHERE m.student.id = :studentId AND m.status = 'ACTIVE'")
    List<TeamMembership> findActiveByStudent(@Param("studentId") Long studentId);

    @Query("SELECT m FROM TeamMembership m WHERE m.team.id = :teamId AND m.status = 'ACTIVE' ORDER BY m.captain DESC, m.student.lastName")
    List<TeamMembership> findActiveRoster(@Param("teamId") Long teamId);

    @Query("SELECT m FROM TeamMembership m WHERE m.team.id = :teamId AND (m.captain = true OR m.coCaptain = true)")
    List<TeamMembership> findTeamLeaders(@Param("teamId") Long teamId);

    @Query("SELECT m FROM TeamMembership m WHERE m.eligible = false AND m.status = 'ACTIVE'")
    List<TeamMembership> findIneligiblePlayers();

    @Query("SELECT m FROM TeamMembership m WHERE m.physicalExpirationDate < :date AND m.status = 'ACTIVE'")
    List<TeamMembership> findExpiredPhysicals(@Param("date") LocalDate date);

    @Query("SELECT m FROM TeamMembership m WHERE m.status = 'ACTIVE' AND (m.physicalOnFile = false OR m.consentFormSigned = false OR m.emergencyContactOnFile = false)")
    List<TeamMembership> findMissingRequiredDocuments();

    @Query("SELECT COUNT(m) FROM TeamMembership m WHERE m.team.id = :teamId AND m.status = 'ACTIVE'")
    long countActiveByTeam(@Param("teamId") Long teamId);

    @Query("SELECT COUNT(m) FROM TeamMembership m WHERE m.student.id = :studentId AND m.status = 'ACTIVE'")
    long countActiveByStudent(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(m) FROM TeamMembership m WHERE m.academicYear = :year AND m.status = 'ACTIVE'")
    long countActiveByYear(@Param("year") String year);
}
