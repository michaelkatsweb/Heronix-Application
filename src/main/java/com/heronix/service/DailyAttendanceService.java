package com.heronix.service;

import com.heronix.model.domain.DailyAttendance;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for DailyAttendance operations
 * Manages daily attendance summaries aggregated from period-by-period attendance
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
public interface DailyAttendanceService {

    // ========================================================================
    // DAILY SUMMARY MANAGEMENT
    // ========================================================================

    /**
     * Generate daily attendance summary for a student
     * Aggregates period-by-period attendance into daily summary
     */
    DailyAttendance generateDailySummary(Long studentId, LocalDate date);

    /**
     * Generate daily summaries for all students on a date
     */
    List<DailyAttendance> generateDailySummariesForDate(LocalDate date, Long campusId);

    /**
     * Update existing daily attendance summary
     */
    DailyAttendance updateDailySummary(Long dailyAttendanceId, DailyAttendance updatedData);

    /**
     * Recalculate daily summary from period attendance records
     */
    DailyAttendance recalculateDailySummary(Long studentId, LocalDate date);

    // ========================================================================
    // QUERIES
    // ========================================================================

    /**
     * Get daily attendance for student on specific date
     */
    DailyAttendance getDailyAttendance(Long studentId, LocalDate date);

    /**
     * Get daily attendance for student in date range
     */
    List<DailyAttendance> getDailyAttendanceRange(Long studentId, LocalDate startDate, LocalDate endDate);

    /**
     * Get all daily attendance for a specific date
     */
    List<DailyAttendance> getDailyAttendanceForDate(LocalDate date);

    /**
     * Get daily attendance for campus on date
     */
    List<DailyAttendance> getDailyAttendanceForCampus(Long campusId, LocalDate date);

    // ========================================================================
    // STATISTICS & ANALYTICS
    // ========================================================================

    /**
     * Calculate attendance rate for student over period
     */
    Double calculateAttendanceRate(Long studentId, LocalDate startDate, LocalDate endDate);

    /**
     * Get attendance statistics for a date
     */
    Map<String, Object> getDailyStatistics(LocalDate date, Long campusId);

    /**
     * Calculate ADA (Average Daily Attendance) for campus/period
     */
    Double calculateADA(Long campusId, LocalDate startDate, LocalDate endDate);

    /**
     * Calculate ADM (Average Daily Membership) for campus/period
     */
    Double calculateADM(Long campusId, LocalDate startDate, LocalDate endDate);

    // ========================================================================
    // ATTENDANCE PATTERNS
    // ========================================================================

    /**
     * Find students with chronic absenteeism
     */
    List<Long> findChronicAbsentStudents(LocalDate startDate, LocalDate endDate);

    /**
     * Find students with perfect attendance
     */
    List<Long> findPerfectAttendanceStudents(LocalDate startDate, LocalDate endDate);

    /**
     * Detect consecutive absences for a student
     */
    int getConsecutiveAbsences(Long studentId, LocalDate fromDate);

    /**
     * Get absent students for a date
     */
    List<DailyAttendance> getAbsentStudents(LocalDate date, Long campusId);

    /**
     * Get tardy students for a date
     */
    List<DailyAttendance> getTardyStudents(LocalDate date, Long campusId);
}
