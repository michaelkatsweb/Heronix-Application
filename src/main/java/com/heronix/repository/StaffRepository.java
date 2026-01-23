package com.heronix.repository;

import com.heronix.model.domain.Staff;
import com.heronix.model.enums.StaffOccupation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for Staff entity operations
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Staff/Teacher Separation
 */
@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    Optional<Staff> findByEmployeeId(String employeeId);

    Optional<Staff> findByEmail(String email);

    List<Staff> findByActiveTrue();

    List<Staff> findByActiveTrueAndDeletedFalse();

    List<Staff> findByOccupation(StaffOccupation occupation);

    List<Staff> findByOccupationAndActiveTrue(StaffOccupation occupation);

    List<Staff> findByDepartment(String department);

    List<Staff> findByDepartmentAndActiveTrue(String department);

    // ========================================================================
    // CAMPUS-BASED QUERIES
    // ========================================================================

    @Query("SELECT s FROM Staff s WHERE s.primaryCampus.id = :campusId AND s.active = true AND s.deleted = false")
    List<Staff> findActiveByCampusId(@Param("campusId") Long campusId);

    @Query("SELECT s FROM Staff s WHERE s.primaryCampus.id = :campusId AND s.occupation = :occupation AND s.active = true AND s.deleted = false")
    List<Staff> findByCampusAndOccupation(@Param("campusId") Long campusId, @Param("occupation") StaffOccupation occupation);

    @Query("SELECT s FROM Staff s WHERE s.primaryCampus.id = :campusId AND s.department = :department AND s.active = true AND s.deleted = false")
    List<Staff> findByCampusAndDepartment(@Param("campusId") Long campusId, @Param("department") String department);

    // ========================================================================
    // COUNT QUERIES
    // ========================================================================

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.active = true AND s.deleted = false")
    Long countActiveStaff();

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.primaryCampus.id = :campusId AND s.active = true AND s.deleted = false")
    Long countActiveStaffByCampus(@Param("campusId") Long campusId);

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.occupation = :occupation AND s.active = true AND s.deleted = false")
    Long countByOccupation(@Param("occupation") StaffOccupation occupation);

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.primaryCampus.id = :campusId AND s.occupation = :occupation AND s.active = true AND s.deleted = false")
    Long countByOccupationAndCampus(@Param("occupation") StaffOccupation occupation, @Param("campusId") Long campusId);

    // ========================================================================
    // ANALYTICS QUERIES - Department Breakdown
    // ========================================================================

    @Query("SELECT s.department, COUNT(s) FROM Staff s WHERE s.active = true AND s.deleted = false GROUP BY s.department ORDER BY COUNT(s) DESC")
    List<Object[]> getDepartmentBreakdown();

    @Query("SELECT s.department, COUNT(s) FROM Staff s WHERE s.primaryCampus.id = :campusId AND s.active = true AND s.deleted = false GROUP BY s.department ORDER BY COUNT(s) DESC")
    List<Object[]> getDepartmentBreakdownByCampus(@Param("campusId") Long campusId);

    // ========================================================================
    // ANALYTICS QUERIES - Occupation Distribution
    // ========================================================================

    @Query("SELECT s.occupation, COUNT(s) FROM Staff s WHERE s.active = true AND s.deleted = false GROUP BY s.occupation ORDER BY COUNT(s) DESC")
    List<Object[]> getOccupationDistribution();

    @Query("SELECT s.occupation, COUNT(s) FROM Staff s WHERE s.primaryCampus.id = :campusId AND s.active = true AND s.deleted = false GROUP BY s.occupation ORDER BY COUNT(s) DESC")
    List<Object[]> getOccupationDistributionByCampus(@Param("campusId") Long campusId);

    // ========================================================================
    // ANALYTICS QUERIES - Experience Distribution
    // ========================================================================

    @Query("SELECT CASE " +
           "  WHEN s.yearsExperience IS NULL THEN 'Unknown' " +
           "  WHEN s.yearsExperience < 2 THEN '0-1 years' " +
           "  WHEN s.yearsExperience < 5 THEN '2-4 years' " +
           "  WHEN s.yearsExperience < 10 THEN '5-9 years' " +
           "  WHEN s.yearsExperience < 20 THEN '10-19 years' " +
           "  ELSE '20+ years' " +
           "END, COUNT(s) " +
           "FROM Staff s WHERE s.active = true AND s.deleted = false " +
           "GROUP BY CASE " +
           "  WHEN s.yearsExperience IS NULL THEN 'Unknown' " +
           "  WHEN s.yearsExperience < 2 THEN '0-1 years' " +
           "  WHEN s.yearsExperience < 5 THEN '2-4 years' " +
           "  WHEN s.yearsExperience < 10 THEN '5-9 years' " +
           "  WHEN s.yearsExperience < 20 THEN '10-19 years' " +
           "  ELSE '20+ years' " +
           "END")
    List<Object[]> getExperienceDistribution();

    @Query("SELECT CASE " +
           "  WHEN s.yearsExperience IS NULL THEN 'Unknown' " +
           "  WHEN s.yearsExperience < 2 THEN '0-1 years' " +
           "  WHEN s.yearsExperience < 5 THEN '2-4 years' " +
           "  WHEN s.yearsExperience < 10 THEN '5-9 years' " +
           "  WHEN s.yearsExperience < 20 THEN '10-19 years' " +
           "  ELSE '20+ years' " +
           "END, COUNT(s) " +
           "FROM Staff s WHERE s.primaryCampus.id = :campusId AND s.active = true AND s.deleted = false " +
           "GROUP BY CASE " +
           "  WHEN s.yearsExperience IS NULL THEN 'Unknown' " +
           "  WHEN s.yearsExperience < 2 THEN '0-1 years' " +
           "  WHEN s.yearsExperience < 5 THEN '2-4 years' " +
           "  WHEN s.yearsExperience < 10 THEN '5-9 years' " +
           "  WHEN s.yearsExperience < 20 THEN '10-19 years' " +
           "  ELSE '20+ years' " +
           "END")
    List<Object[]> getExperienceDistributionByCampus(@Param("campusId") Long campusId);

    // ========================================================================
    // ANALYTICS QUERIES - Average Experience
    // ========================================================================

    @Query("SELECT AVG(COALESCE(s.yearsExperience, 0)) FROM Staff s WHERE s.active = true AND s.deleted = false")
    Double getAverageExperience();

    @Query("SELECT AVG(COALESCE(s.yearsExperience, 0)) FROM Staff s WHERE s.primaryCampus.id = :campusId AND s.active = true AND s.deleted = false")
    Double getAverageExperienceByCampus(@Param("campusId") Long campusId);

    @Query("SELECT s.department, AVG(COALESCE(s.yearsExperience, 0)) FROM Staff s WHERE s.active = true AND s.deleted = false GROUP BY s.department")
    List<Object[]> getAverageExperienceByDepartment();

    // ========================================================================
    // BACKGROUND CHECK QUERIES
    // ========================================================================

    @Query("SELECT s FROM Staff s WHERE s.backgroundCheckExpiration <= :date AND s.active = true AND s.deleted = false ORDER BY s.backgroundCheckExpiration ASC")
    List<Staff> findWithExpiringBackgroundChecks(@Param("date") LocalDate date);

    @Query("SELECT s FROM Staff s WHERE s.primaryCampus.id = :campusId AND s.backgroundCheckExpiration <= :date AND s.active = true AND s.deleted = false ORDER BY s.backgroundCheckExpiration ASC")
    List<Staff> findWithExpiringBackgroundChecksByCampus(@Param("campusId") Long campusId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.backgroundCheckExpiration < :today AND s.active = true AND s.deleted = false")
    Long countExpiredBackgroundChecks(@Param("today") LocalDate today);

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.backgroundCheckExpiration BETWEEN :today AND :futureDate AND s.active = true AND s.deleted = false")
    Long countExpiringBackgroundChecks(@Param("today") LocalDate today, @Param("futureDate") LocalDate futureDate);

    // ========================================================================
    // I-9 COMPLIANCE QUERIES
    // ========================================================================

    @Query("SELECT s FROM Staff s WHERE s.i9ExpirationDate <= :date AND s.active = true AND s.deleted = false ORDER BY s.i9ExpirationDate ASC")
    List<Staff> findWithExpiringI9(@Param("date") LocalDate date);

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.i9CompletionDate IS NULL AND s.active = true AND s.deleted = false")
    Long countMissingI9();

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.i9ExpirationDate < :today AND s.active = true AND s.deleted = false")
    Long countExpiredI9(@Param("today") LocalDate today);

    // ========================================================================
    // OCCUPATION-SPECIFIC QUERIES - Paraprofessionals
    // ========================================================================

    @Query("SELECT s FROM Staff s WHERE s.occupation IN (com.heronix.model.enums.StaffOccupation.PARAPROFESSIONAL, com.heronix.model.enums.StaffOccupation.TEACHER_AIDE) AND s.active = true AND s.deleted = false")
    List<Staff> findAllParaprofessionals();

    @Query("SELECT s FROM Staff s WHERE s.primaryCampus.id = :campusId AND s.occupation IN (com.heronix.model.enums.StaffOccupation.PARAPROFESSIONAL, com.heronix.model.enums.StaffOccupation.TEACHER_AIDE) AND s.active = true AND s.deleted = false")
    List<Staff> findParaprofessionalsByCampus(@Param("campusId") Long campusId);

    @Query("SELECT s FROM Staff s WHERE s.paraAssignmentType = :assignmentType AND s.active = true AND s.deleted = false")
    List<Staff> findByParaAssignmentType(@Param("assignmentType") String assignmentType);

    // ========================================================================
    // OCCUPATION-SPECIFIC QUERIES - Counselors
    // ========================================================================

    @Query("SELECT s FROM Staff s WHERE s.occupation IN (com.heronix.model.enums.StaffOccupation.COUNSELOR, com.heronix.model.enums.StaffOccupation.GUIDANCE_COUNSELOR, com.heronix.model.enums.StaffOccupation.SCHOOL_PSYCHOLOGIST, com.heronix.model.enums.StaffOccupation.SOCIAL_WORKER) AND s.active = true AND s.deleted = false")
    List<Staff> findAllCounselors();

    @Query("SELECT s FROM Staff s WHERE s.counselorLicenseExpiration <= :date AND s.active = true AND s.deleted = false")
    List<Staff> findCounselorsWithExpiringLicense(@Param("date") LocalDate date);

    // ========================================================================
    // OCCUPATION-SPECIFIC QUERIES - Nurses
    // ========================================================================

    @Query("SELECT s FROM Staff s WHERE s.occupation IN (com.heronix.model.enums.StaffOccupation.SCHOOL_NURSE, com.heronix.model.enums.StaffOccupation.NURSE_AIDE) AND s.active = true AND s.deleted = false")
    List<Staff> findAllNurses();

    @Query("SELECT s FROM Staff s WHERE s.nursingLicenseExpiration <= :date AND s.active = true AND s.deleted = false")
    List<Staff> findNursesWithExpiringLicense(@Param("date") LocalDate date);

    // ========================================================================
    // OCCUPATION-SPECIFIC QUERIES - Bus Drivers
    // ========================================================================

    @Query("SELECT s FROM Staff s WHERE s.occupation IN (com.heronix.model.enums.StaffOccupation.BUS_DRIVER, com.heronix.model.enums.StaffOccupation.BUS_AIDE, com.heronix.model.enums.StaffOccupation.TRANSPORTATION_COORDINATOR) AND s.active = true AND s.deleted = false")
    List<Staff> findAllTransportationStaff();

    @Query("SELECT s FROM Staff s WHERE s.cdlExpiration <= :date AND s.active = true AND s.deleted = false")
    List<Staff> findDriversWithExpiringCDL(@Param("date") LocalDate date);

    // ========================================================================
    // SEARCH QUERIES
    // ========================================================================

    @Query("SELECT s FROM Staff s WHERE (LOWER(s.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(s.employeeId) LIKE LOWER(CONCAT('%', :term, '%'))) AND s.active = true AND s.deleted = false")
    List<Staff> searchByNameOrEmployeeId(@Param("term") String term);

    @Query("SELECT s FROM Staff s WHERE s.primaryCampus.id = :campusId AND (LOWER(s.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(s.employeeId) LIKE LOWER(CONCAT('%', :term, '%'))) AND s.active = true AND s.deleted = false")
    List<Staff> searchByNameOrEmployeeIdAndCampus(@Param("term") String term, @Param("campusId") Long campusId);

    // ========================================================================
    // COMPLIANCE SUMMARY QUERIES
    // ========================================================================

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.backgroundCheckStatus = 'Passed' AND s.backgroundCheckExpiration > :today AND s.active = true AND s.deleted = false")
    Long countWithValidBackgroundCheck(@Param("today") LocalDate today);

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.i9Status = 'Completed' AND (s.i9ExpirationDate IS NULL OR s.i9ExpirationDate > :today) AND s.active = true AND s.deleted = false")
    Long countWithValidI9(@Param("today") LocalDate today);
}
