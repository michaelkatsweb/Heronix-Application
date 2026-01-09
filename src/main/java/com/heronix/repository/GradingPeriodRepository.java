package com.heronix.repository;

import com.heronix.model.domain.GradingPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for GradingPeriod entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface GradingPeriodRepository extends JpaRepository<GradingPeriod, Long> {

    /**
     * Find all grading periods for a specific academic year
     */
    List<GradingPeriod> findByAcademicYearOrderByPeriodNumberAsc(String academicYear);

    /**
     * Find all active grading periods
     */
    List<GradingPeriod> findByActiveTrue();

    /**
     * Find all active grading periods for a specific academic year
     */
    List<GradingPeriod> findByAcademicYearAndActiveTrueOrderByPeriodNumberAsc(String academicYear);

    /**
     * Find grading periods by type
     */
    List<GradingPeriod> findByPeriodType(GradingPeriod.PeriodType periodType);

    /**
     * Find grading period by academic year and period number
     */
    Optional<GradingPeriod> findByAcademicYearAndPeriodNumber(String academicYear, Integer periodNumber);

    /**
     * Find the current grading period (period that contains today's date)
     */
    @Query("SELECT gp FROM GradingPeriod gp WHERE gp.active = true AND :today BETWEEN gp.startDate AND gp.endDate")
    Optional<GradingPeriod> findCurrentPeriod(@Param("today") LocalDate today);

    /**
     * Find all grading periods for a specific campus
     */
    List<GradingPeriod> findByCampusIdOrderByStartDateAsc(Long campusId);

    /**
     * Find grading periods that overlap with a given date range
     */
    @Query("SELECT gp FROM GradingPeriod gp WHERE gp.academicYear = :year AND " +
           "((gp.startDate <= :endDate AND gp.endDate >= :startDate))")
    List<GradingPeriod> findOverlappingPeriods(
        @Param("year") String academicYear,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Check if a grading period exists with the given academic year and period number
     */
    boolean existsByAcademicYearAndPeriodNumber(String academicYear, Integer periodNumber);

    /**
     * Count grading periods for a specific academic year
     */
    long countByAcademicYear(String academicYear);

    /**
     * Find all distinct academic years
     */
    @Query("SELECT DISTINCT gp.academicYear FROM GradingPeriod gp ORDER BY gp.academicYear DESC")
    List<String> findDistinctAcademicYears();

    /**
     * Delete all grading periods for a specific academic year
     */
    void deleteByAcademicYear(String academicYear);
}
