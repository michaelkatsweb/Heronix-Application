package com.heronix.repository;

import com.heronix.model.domain.SchoolCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for School Calendar entities
 * Provides data access for school calendar management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface SchoolCalendarRepository extends JpaRepository<SchoolCalendar, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find calendar by academic year
     */
    List<SchoolCalendar> findByAcademicYear(String academicYear);

    /**
     * Find calendar by academic year and campus
     */
    Optional<SchoolCalendar> findByAcademicYearAndCampusId(String academicYear, Long campusId);

    /**
     * Find calendars by campus
     */
    List<SchoolCalendar> findByCampusIdOrderByStartDateDesc(Long campusId);

    /**
     * Find active calendars
     */
    @Query("SELECT sc FROM SchoolCalendar sc WHERE sc.active = true " +
           "ORDER BY sc.startDate DESC")
    List<SchoolCalendar> findAllActive();

    // ========================================================================
    // CURRENT CALENDAR QUERIES
    // ========================================================================

    /**
     * Find the current calendar (where today falls within the year)
     */
    @Query("SELECT sc FROM SchoolCalendar sc WHERE sc.active = true " +
           "AND :today BETWEEN sc.startDate AND sc.endDate")
    List<SchoolCalendar> findCurrentCalendar(@Param("today") LocalDate today);

    /**
     * Find current calendar for a specific campus
     */
    @Query("SELECT sc FROM SchoolCalendar sc WHERE sc.active = true " +
           "AND sc.campus.id = :campusId " +
           "AND :today BETWEEN sc.startDate AND sc.endDate")
    Optional<SchoolCalendar> findCurrentCalendarByCampus(
            @Param("campusId") Long campusId,
            @Param("today") LocalDate today);

    /**
     * Find current district-wide calendar
     */
    @Query("SELECT sc FROM SchoolCalendar sc WHERE sc.active = true " +
           "AND sc.campus IS NULL " +
           "AND :today BETWEEN sc.startDate AND sc.endDate")
    Optional<SchoolCalendar> findCurrentDistrictWideCalendar(@Param("today") LocalDate today);

    // ========================================================================
    // DATE RANGE QUERIES
    // ========================================================================

    /**
     * Find calendars that overlap with a given date range
     */
    @Query("SELECT sc FROM SchoolCalendar sc WHERE sc.active = true " +
           "AND sc.startDate <= :endDate " +
           "AND sc.endDate >= :startDate " +
           "ORDER BY sc.startDate ASC")
    List<SchoolCalendar> findOverlappingCalendars(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find upcoming calendars (starting after today)
     */
    @Query("SELECT sc FROM SchoolCalendar sc WHERE sc.active = true " +
           "AND sc.startDate > :today " +
           "ORDER BY sc.startDate ASC")
    List<SchoolCalendar> findUpcomingCalendars(@Param("today") LocalDate today);

    /**
     * Find past calendars (ended before today)
     */
    @Query("SELECT sc FROM SchoolCalendar sc " +
           "WHERE sc.endDate < :today " +
           "ORDER BY sc.endDate DESC")
    List<SchoolCalendar> findPastCalendars(@Param("today") LocalDate today);

    // ========================================================================
    // VALIDATION QUERIES
    // ========================================================================

    /**
     * Check if a calendar with the same academic year already exists for a campus
     */
    @Query("SELECT COUNT(sc) > 0 FROM SchoolCalendar sc " +
           "WHERE sc.academicYear = :academicYear " +
           "AND (:campusId IS NULL AND sc.campus IS NULL OR sc.campus.id = :campusId) " +
           "AND (:excludeId IS NULL OR sc.id != :excludeId)")
    boolean existsByAcademicYearAndCampus(
            @Param("academicYear") String academicYear,
            @Param("campusId") Long campusId,
            @Param("excludeId") Long excludeId);

    /**
     * Find calendars that would overlap with the given date range (excluding a specific calendar)
     */
    @Query("SELECT sc FROM SchoolCalendar sc " +
           "WHERE (:campusId IS NULL AND sc.campus IS NULL OR sc.campus.id = :campusId) " +
           "AND sc.startDate <= :endDate " +
           "AND sc.endDate >= :startDate " +
           "AND (:excludeId IS NULL OR sc.id != :excludeId)")
    List<SchoolCalendar> findConflictingCalendars(
            @Param("campusId") Long campusId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") Long excludeId);

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Get distinct academic years
     */
    @Query("SELECT DISTINCT sc.academicYear FROM SchoolCalendar sc " +
           "ORDER BY sc.academicYear DESC")
    List<String> findDistinctAcademicYears();

    /**
     * Count calendars by academic year
     */
    @Query("SELECT COUNT(sc) FROM SchoolCalendar sc " +
           "WHERE sc.academicYear = :academicYear")
    Long countByAcademicYear(@Param("academicYear") String academicYear);

    /**
     * Count calendars by campus
     */
    @Query("SELECT COUNT(sc) FROM SchoolCalendar sc " +
           "WHERE sc.campus.id = :campusId")
    Long countByCampus(@Param("campusId") Long campusId);

    // ========================================================================
    // SPECIAL QUERIES
    // ========================================================================

    /**
     * Find district-wide calendars (no campus assignment)
     */
    @Query("SELECT sc FROM SchoolCalendar sc WHERE sc.campus IS NULL " +
           "AND sc.active = true " +
           "ORDER BY sc.startDate DESC")
    List<SchoolCalendar> findDistrictWide();

    /**
     * Find calendars for a specific date
     */
    @Query("SELECT sc FROM SchoolCalendar sc WHERE sc.active = true " +
           "AND :date BETWEEN sc.startDate AND sc.endDate " +
           "ORDER BY sc.campus.id ASC")
    List<SchoolCalendar> findByDate(@Param("date") LocalDate date);

    /**
     * Find the most recent calendar
     */
    @Query("SELECT sc FROM SchoolCalendar sc WHERE sc.active = true " +
           "ORDER BY sc.startDate DESC")
    List<SchoolCalendar> findMostRecent();
}
