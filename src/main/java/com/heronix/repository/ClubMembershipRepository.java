package com.heronix.repository;

import com.heronix.model.domain.ClubMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ClubMembership entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {

    List<ClubMembership> findByStudentId(Long studentId);

    List<ClubMembership> findByClubId(Long clubId);

    List<ClubMembership> findByStudentIdAndAcademicYear(Long studentId, String academicYear);

    List<ClubMembership> findByClubIdAndAcademicYear(Long clubId, String academicYear);

    Optional<ClubMembership> findByClubIdAndStudentIdAndAcademicYear(Long clubId, Long studentId, String academicYear);

    @Query("SELECT m FROM ClubMembership m WHERE m.student.id = :studentId AND m.status = 'ACTIVE'")
    List<ClubMembership> findActiveByStudent(@Param("studentId") Long studentId);

    @Query("SELECT m FROM ClubMembership m WHERE m.club.id = :clubId AND m.status = 'ACTIVE' ORDER BY m.role, m.student.lastName")
    List<ClubMembership> findActiveByClub(@Param("clubId") Long clubId);

    @Query("SELECT m FROM ClubMembership m WHERE m.club.id = :clubId AND (m.president = true OR m.vicePresident = true OR m.secretary = true OR m.treasurer = true)")
    List<ClubMembership> findOfficers(@Param("clubId") Long clubId);

    @Query("SELECT m FROM ClubMembership m WHERE m.status = 'ACTIVE' AND m.parentConsentReceived = false")
    List<ClubMembership> findMissingParentConsent();

    @Query("SELECT COUNT(m) FROM ClubMembership m WHERE m.club.id = :clubId AND m.status = 'ACTIVE'")
    long countActiveByClub(@Param("clubId") Long clubId);

    @Query("SELECT COUNT(m) FROM ClubMembership m WHERE m.student.id = :studentId AND m.status = 'ACTIVE'")
    long countActiveByStudent(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(m) FROM ClubMembership m WHERE m.academicYear = :year AND m.status = 'ACTIVE'")
    long countActiveByYear(@Param("year") String year);

    @Query("SELECT COALESCE(SUM(m.serviceHours), 0) FROM ClubMembership m WHERE m.student.id = :studentId")
    int sumServiceHoursByStudent(@Param("studentId") Long studentId);
}
