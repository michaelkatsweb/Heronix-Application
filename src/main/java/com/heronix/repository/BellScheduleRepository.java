package com.heronix.repository;

import com.heronix.model.domain.BellSchedule;
import com.heronix.model.domain.BellSchedule.ScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Bell Schedule entities
 * Provides data access for bell schedule management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface BellScheduleRepository extends JpaRepository<BellSchedule, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find all bell schedules by name
     */
    List<BellSchedule> findByNameContainingIgnoreCase(String name);

    /**
     * Find bell schedules by schedule type
     */
    List<BellSchedule> findByScheduleType(ScheduleType scheduleType);

    /**
     * Find bell schedules by campus
     */
    List<BellSchedule> findByCampusIdOrderByNameAsc(Long campusId);

    /**
     * Find bell schedules by academic year
     */
    List<BellSchedule> findByAcademicYearIdOrderByNameAsc(Long academicYearId);

    // ========================================================================
    // ACTIVE STATUS QUERIES
    // ========================================================================

    /**
     * Find all active bell schedules
     */
    @Query("SELECT bs FROM BellSchedule bs WHERE bs.active = true " +
           "ORDER BY bs.name ASC")
    List<BellSchedule> findAllActive();

    /**
     * Find active bell schedules for a specific campus
     */
    @Query("SELECT bs FROM BellSchedule bs WHERE bs.active = true " +
           "AND bs.campus.id = :campusId " +
           "ORDER BY bs.name ASC")
    List<BellSchedule> findActiveByCampus(@Param("campusId") Long campusId);

    /**
     * Find active bell schedules for a specific academic year
     */
    @Query("SELECT bs FROM BellSchedule bs WHERE bs.active = true " +
           "AND bs.academicYear.id = :academicYearId " +
           "ORDER BY bs.name ASC")
    List<BellSchedule> findActiveByAcademicYear(@Param("academicYearId") Long academicYearId);

    // ========================================================================
    // DEFAULT SCHEDULE QUERIES
    // ========================================================================

    /**
     * Find the default schedule for a campus
     */
    @Query("SELECT bs FROM BellSchedule bs WHERE bs.active = true " +
           "AND bs.isDefault = true " +
           "AND bs.campus.id = :campusId")
    Optional<BellSchedule> findDefaultByCampus(@Param("campusId") Long campusId);

    /**
     * Find the default district-wide schedule
     */
    @Query("SELECT bs FROM BellSchedule bs WHERE bs.active = true " +
           "AND bs.isDefault = true " +
           "AND bs.campus IS NULL")
    Optional<BellSchedule> findDefaultDistrictWide();

    /**
     * Find default schedule for academic year and campus
     */
    @Query("SELECT bs FROM BellSchedule bs WHERE bs.active = true " +
           "AND bs.isDefault = true " +
           "AND bs.academicYear.id = :academicYearId " +
           "AND bs.campus.id = :campusId")
    Optional<BellSchedule> findDefaultByAcademicYearAndCampus(
            @Param("academicYearId") Long academicYearId,
            @Param("campusId") Long campusId);

    // ========================================================================
    // DATE-BASED QUERIES
    // ========================================================================

    /**
     * Find schedules that apply to a specific date (via specificDates)
     */
    @Query("SELECT bs FROM BellSchedule bs " +
           "JOIN bs.specificDates sd " +
           "WHERE bs.active = true " +
           "AND sd = :date " +
           "ORDER BY bs.campus.id ASC, bs.name ASC")
    List<BellSchedule> findBySpecificDate(@Param("date") LocalDate date);

    /**
     * Find schedules for a specific date and campus
     */
    @Query("SELECT bs FROM BellSchedule bs " +
           "JOIN bs.specificDates sd " +
           "WHERE bs.active = true " +
           "AND sd = :date " +
           "AND bs.campus.id = :campusId")
    List<BellSchedule> findBySpecificDateAndCampus(
            @Param("date") LocalDate date,
            @Param("campusId") Long campusId);

    /**
     * Find schedules applicable to a date range
     */
    @Query("SELECT DISTINCT bs FROM BellSchedule bs " +
           "LEFT JOIN bs.specificDates sd " +
           "WHERE bs.active = true " +
           "AND (sd BETWEEN :startDate AND :endDate OR sd IS NULL) " +
           "ORDER BY bs.name ASC")
    List<BellSchedule> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ========================================================================
    // VALIDATION QUERIES
    // ========================================================================

    /**
     * Check if a bell schedule with the same name already exists for a campus
     */
    @Query("SELECT COUNT(bs) > 0 FROM BellSchedule bs " +
           "WHERE LOWER(bs.name) = LOWER(:name) " +
           "AND (:campusId IS NULL AND bs.campus IS NULL OR bs.campus.id = :campusId) " +
           "AND (:excludeId IS NULL OR bs.id != :excludeId)")
    boolean existsByNameAndCampus(
            @Param("name") String name,
            @Param("campusId") Long campusId,
            @Param("excludeId") Long excludeId);

    /**
     * Check if another default schedule exists for the same campus
     */
    @Query("SELECT COUNT(bs) > 0 FROM BellSchedule bs " +
           "WHERE bs.isDefault = true " +
           "AND bs.active = true " +
           "AND (:campusId IS NULL AND bs.campus IS NULL OR bs.campus.id = :campusId) " +
           "AND (:excludeId IS NULL OR bs.id != :excludeId)")
    boolean existsDefaultSchedule(
            @Param("campusId") Long campusId,
            @Param("excludeId") Long excludeId);

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Count bell schedules by campus
     */
    @Query("SELECT COUNT(bs) FROM BellSchedule bs " +
           "WHERE bs.campus.id = :campusId")
    Long countByCampus(@Param("campusId") Long campusId);

    /**
     * Count active bell schedules
     */
    @Query("SELECT COUNT(bs) FROM BellSchedule bs " +
           "WHERE bs.active = true")
    Long countActive();

    /**
     * Count schedules by type
     */
    @Query("SELECT COUNT(bs) FROM BellSchedule bs " +
           "WHERE bs.scheduleType = :scheduleType")
    Long countByScheduleType(@Param("scheduleType") ScheduleType scheduleType);

    // ========================================================================
    // SPECIAL QUERIES
    // ========================================================================

    /**
     * Find district-wide schedules (no campus assignment)
     */
    @Query("SELECT bs FROM BellSchedule bs WHERE bs.campus IS NULL " +
           "AND bs.active = true " +
           "ORDER BY bs.name ASC")
    List<BellSchedule> findDistrictWide();

    /**
     * Find schedules with no periods defined
     */
    @Query("SELECT bs FROM BellSchedule bs " +
           "LEFT JOIN bs.periods p " +
           "GROUP BY bs.id " +
           "HAVING COUNT(p) = 0")
    List<BellSchedule> findWithNoPeriods();

    /**
     * Find schedules by schedule type and campus
     */
    @Query("SELECT bs FROM BellSchedule bs " +
           "WHERE bs.scheduleType = :scheduleType " +
           "AND bs.campus.id = :campusId " +
           "AND bs.active = true " +
           "ORDER BY bs.name ASC")
    List<BellSchedule> findByScheduleTypeAndCampus(
            @Param("scheduleType") ScheduleType scheduleType,
            @Param("campusId") Long campusId);
}
