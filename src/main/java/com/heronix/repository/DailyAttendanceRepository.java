package com.heronix.repository;

import com.heronix.model.domain.DailyAttendance;
import com.heronix.model.domain.DailyAttendance.OverallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DailyAttendance entities
 * Manages daily attendance summaries for students
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface DailyAttendanceRepository extends JpaRepository<DailyAttendance, Long> {

    /**
     * Find daily attendance for a specific student and date
     */
    Optional<DailyAttendance> findByStudentIdAndAttendanceDate(Long studentId, LocalDate date);

    /**
     * Find all daily attendance for a student in date range
     */
    List<DailyAttendance> findByStudentIdAndAttendanceDateBetween(
        Long studentId, LocalDate startDate, LocalDate endDate);

    /**
     * Find all daily attendance for a specific date
     */
    List<DailyAttendance> findByAttendanceDate(LocalDate date);

    /**
     * Find all daily attendance for a date and campus
     */
    List<DailyAttendance> findByAttendanceDateAndCampusId(LocalDate date, Long campusId);

    /**
     * Find students with specific overall status on a date
     */
    List<DailyAttendance> findByAttendanceDateAndOverallStatus(LocalDate date, OverallStatus status);

    /**
     * Count absences for a student in date range
     */
    @Query("SELECT COUNT(d) FROM DailyAttendance d WHERE d.student.id = :studentId " +
           "AND d.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND d.overallStatus = 'ABSENT'")
    long countAbsencesByStudent(@Param("studentId") Long studentId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    /**
     * Count present days for a student in date range
     */
    @Query("SELECT COUNT(d) FROM DailyAttendance d WHERE d.student.id = :studentId " +
           "AND d.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND d.overallStatus IN ('PRESENT', 'PARTIAL')")
    long countPresentDaysByStudent(@Param("studentId") Long studentId,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    /**
     * Calculate total periods present for a student
     */
    @Query("SELECT SUM(d.periodsPresent) FROM DailyAttendance d WHERE d.student.id = :studentId " +
           "AND d.attendanceDate BETWEEN :startDate AND :endDate")
    Integer sumPeriodsPresent(@Param("studentId") Long studentId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

    /**
     * Calculate total periods absent for a student
     */
    @Query("SELECT SUM(d.periodsAbsent) FROM DailyAttendance d WHERE d.student.id = :studentId " +
           "AND d.attendanceDate BETWEEN :startDate AND :endDate")
    Integer sumPeriodsAbsent(@Param("studentId") Long studentId,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate);

    /**
     * Find students with chronic absenteeism (attendance rate < 90%)
     */
    @Query("SELECT d.student.id FROM DailyAttendance d " +
           "WHERE d.attendanceDate BETWEEN :startDate AND :endDate " +
           "GROUP BY d.student.id " +
           "HAVING (SUM(d.periodsPresent) * 1.0 / SUM(d.totalPeriods)) < 0.9")
    List<Long> findChronicAbsentStudents(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    /**
     * Get attendance rate for a student
     */
    @Query("SELECT (SUM(d.periodsPresent) * 100.0 / SUM(d.totalPeriods)) FROM DailyAttendance d " +
           "WHERE d.student.id = :studentId " +
           "AND d.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND d.totalPeriods > 0")
    Double getAttendanceRate(@Param("studentId") Long studentId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);

    /**
     * Find consecutive absences for a student
     */
    @Query("SELECT d FROM DailyAttendance d WHERE d.student.id = :studentId " +
           "AND d.overallStatus = 'ABSENT' " +
           "AND d.attendanceDate >= :fromDate " +
           "ORDER BY d.attendanceDate ASC")
    List<DailyAttendance> findConsecutiveAbsences(@Param("studentId") Long studentId,
                                                    @Param("fromDate") LocalDate fromDate);

    /**
     * Get daily attendance statistics for a date
     */
    @Query("SELECT d.overallStatus, COUNT(d) FROM DailyAttendance d " +
           "WHERE d.attendanceDate = :date " +
           "GROUP BY d.overallStatus")
    List<Object[]> getDailyStatistics(@Param("date") LocalDate date);

    /**
     * Get daily attendance statistics for campus
     */
    @Query("SELECT d.overallStatus, COUNT(d) FROM DailyAttendance d " +
           "WHERE d.attendanceDate = :date AND d.campus.id = :campusId " +
           "GROUP BY d.overallStatus")
    List<Object[]> getDailyStatisticsByCampus(@Param("date") LocalDate date,
                                              @Param("campusId") Long campusId);

    /**
     * Calculate ADA (Average Daily Attendance) for period
     */
    @Query("SELECT AVG(d.periodsPresent * 1.0 / d.totalPeriods) FROM DailyAttendance d " +
           "WHERE d.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND d.campus.id = :campusId " +
           "AND d.totalPeriods > 0")
    Double calculateADA(@Param("campusId") Long campusId,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate);

    /**
     * Find students with perfect attendance
     */
    @Query("SELECT d.student.id FROM DailyAttendance d " +
           "WHERE d.attendanceDate BETWEEN :startDate AND :endDate " +
           "GROUP BY d.student.id " +
           "HAVING MIN(d.periodsPresent) = MAX(d.totalPeriods) " +
           "AND COUNT(d) = :expectedDays")
    List<Long> findPerfectAttendanceStudents(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate,
                                             @Param("expectedDays") long expectedDays);
}
