package com.heronix.repository;

import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.AttendanceRecord.AttendanceStatus;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Attendance Record Repository
 *
 * Data access layer for student attendance tracking.
 * Provides queries for attendance analysis, reporting, and monitoring.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 16 - Attendance System
 */
@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    /**
     * Find attendance records for a student
     */
    List<AttendanceRecord> findByStudentOrderByAttendanceDateDesc(Student student);

    /**
     * Find attendance records for a student by student ID
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.student.id = :studentId ORDER BY a.attendanceDate DESC")
    List<AttendanceRecord> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find attendance records for a student on a specific date
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.student.id = :studentId AND a.attendanceDate = :date")
    List<AttendanceRecord> findByStudentIdAndDate(@Param("studentId") Long studentId, @Param("date") LocalDate date);

    /**
     * Find attendance records for a student in a date range
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.student.id = :studentId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.attendanceDate DESC")
    List<AttendanceRecord> findByStudentIdAndDateRange(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find attendance records for a course
     */
    List<AttendanceRecord> findByCourseOrderByAttendanceDateDesc(Course course);

    /**
     * Find attendance records for a course by course ID
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.course.id = :courseId ORDER BY a.attendanceDate DESC")
    List<AttendanceRecord> findByCourseId(@Param("courseId") Long courseId);

    /**
     * Find attendance records for a course on a specific date
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.course.id = :courseId AND a.attendanceDate = :date")
    List<AttendanceRecord> findByCourseIdAndDate(@Param("courseId") Long courseId, @Param("date") LocalDate date);

    /**
     * Find attendance records for student in a specific course
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.student.id = :studentId AND a.course.id = :courseId " +
           "ORDER BY a.attendanceDate DESC")
    List<AttendanceRecord> findByStudentIdAndCourseId(
        @Param("studentId") Long studentId,
        @Param("courseId") Long courseId
    );

    /**
     * Find attendance record for student, course, and date (unique combination)
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.student.id = :studentId " +
           "AND a.course.id = :courseId AND a.attendanceDate = :date")
    Optional<AttendanceRecord> findByStudentIdAndCourseIdAndDate(
        @Param("studentId") Long studentId,
        @Param("courseId") Long courseId,
        @Param("date") LocalDate date
    );

    /**
     * Find all attendance records for a specific date
     */
    List<AttendanceRecord> findByAttendanceDate(LocalDate date);

    /**
     * Find attendance records by date range
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.attendanceDate DESC")
    List<AttendanceRecord> findByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find attendance records by status
     */
    List<AttendanceRecord> findByStatusOrderByAttendanceDateDesc(AttendanceStatus status);

    /**
     * Find attendance records for student by status
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.student.id = :studentId AND a.status = :status " +
           "ORDER BY a.attendanceDate DESC")
    List<AttendanceRecord> findByStudentIdAndStatus(
        @Param("studentId") Long studentId,
        @Param("status") AttendanceStatus status
    );

    /**
     * Count absences for a student
     */
    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.student.id = :studentId " +
           "AND a.status IN ('ABSENT', 'EXCUSED_ABSENT', 'UNEXCUSED_ABSENT')")
    Long countAbsencesByStudentId(@Param("studentId") Long studentId);

    /**
     * Count absences for a student in date range
     */
    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.student.id = :studentId " +
           "AND a.status IN ('ABSENT', 'EXCUSED_ABSENT', 'UNEXCUSED_ABSENT') " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Long countAbsencesByStudentIdAndDateRange(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Count tardies for a student
     */
    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.student.id = :studentId AND a.status = 'TARDY'")
    Long countTardiesByStudentId(@Param("studentId") Long studentId);

    /**
     * Count tardies for a student in date range
     */
    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.student.id = :studentId " +
           "AND a.status = 'TARDY' " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Long countTardiesByStudentIdAndDateRange(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Count unexcused absences for a student
     */
    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.student.id = :studentId " +
           "AND a.status = 'UNEXCUSED_ABSENT'")
    Long countUnexcusedAbsencesByStudentId(@Param("studentId") Long studentId);

    /**
     * Count unexcused absences for a student in date range
     */
    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.student.id = :studentId " +
           "AND a.status = 'UNEXCUSED_ABSENT' " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Long countUnexcusedAbsencesByStudentIdAndDateRange(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate attendance rate for a student
     */
    @Query("SELECT " +
           "CAST(SUM(CASE WHEN a.status IN ('PRESENT', 'TARDY', 'REMOTE') THEN 1 ELSE 0 END) AS double) / " +
           "CAST(COUNT(a) AS double) * 100.0 " +
           "FROM AttendanceRecord a WHERE a.student.id = :studentId")
    Double calculateAttendanceRateByStudentId(@Param("studentId") Long studentId);

    /**
     * Calculate attendance rate for a student in date range
     */
    @Query("SELECT " +
           "CAST(SUM(CASE WHEN a.status IN ('PRESENT', 'TARDY', 'REMOTE') THEN 1 ELSE 0 END) AS double) / " +
           "CAST(COUNT(a) AS double) * 100.0 " +
           "FROM AttendanceRecord a WHERE a.student.id = :studentId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Double calculateAttendanceRateByStudentIdAndDateRange(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find students with chronic absenteeism (attendance rate below threshold)
     */
    @Query("SELECT a.student FROM AttendanceRecord a " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "GROUP BY a.student " +
           "HAVING (CAST(SUM(CASE WHEN a.status IN ('PRESENT', 'TARDY', 'REMOTE') THEN 1 ELSE 0 END) AS double) / " +
           "CAST(COUNT(a) AS double) * 100.0) < :threshold")
    List<Student> findStudentsWithChronicAbsenteeism(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("threshold") Double threshold
    );

    /**
     * Find students absent on a specific date
     */
    @Query("SELECT a.student FROM AttendanceRecord a WHERE a.attendanceDate = :date " +
           "AND a.status IN ('ABSENT', 'EXCUSED_ABSENT', 'UNEXCUSED_ABSENT')")
    List<Student> findStudentsAbsentOnDate(@Param("date") LocalDate date);

    /**
     * Find students present on a specific date
     */
    @Query("SELECT a.student FROM AttendanceRecord a WHERE a.attendanceDate = :date " +
           "AND a.status IN ('PRESENT', 'TARDY', 'REMOTE')")
    List<Student> findStudentsPresentOnDate(@Param("date") LocalDate date);

    /**
     * Find unverified attendance records
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.verified = false ORDER BY a.attendanceDate DESC")
    List<AttendanceRecord> findUnverifiedRecords();

    /**
     * Find records where parents have not been notified
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.parentNotified = false " +
           "AND a.status IN ('ABSENT', 'UNEXCUSED_ABSENT', 'TARDY') " +
           "ORDER BY a.attendanceDate DESC")
    List<AttendanceRecord> findRecordsRequiringParentNotification();

    /**
     * Get attendance statistics for a course
     */
    @Query("SELECT a.status, COUNT(a) FROM AttendanceRecord a " +
           "WHERE a.course.id = :courseId AND a.attendanceDate = :date " +
           "GROUP BY a.status")
    List<Object[]> getAttendanceStatisticsByCourseAndDate(
        @Param("courseId") Long courseId,
        @Param("date") LocalDate date
    );

    /**
     * Get daily attendance statistics
     */
    @Query("SELECT a.status, COUNT(a) FROM AttendanceRecord a " +
           "WHERE a.attendanceDate = :date " +
           "GROUP BY a.status")
    List<Object[]> getDailyAttendanceStatistics(@Param("date") LocalDate date);

    /**
     * Find records created/updated in date range (for audit)
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.updatedAt BETWEEN :startTime AND :endTime " +
           "ORDER BY a.updatedAt DESC")
    List<AttendanceRecord> findRecordsUpdatedBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Find records by recorder (for audit)
     */
    List<AttendanceRecord> findByRecordedByOrderByAttendanceDateDesc(String recordedBy);

    /**
     * Delete attendance records for a student (for data cleanup)
     */
    @Query("DELETE FROM AttendanceRecord a WHERE a.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);

    /**
     * Delete attendance records for a date (rollback functionality)
     */
    @Query("DELETE FROM AttendanceRecord a WHERE a.attendanceDate = :date")
    void deleteAllByDate(@Param("date") LocalDate date);

    /**
     * Count total attendance records
     */
    @Query("SELECT COUNT(a) FROM AttendanceRecord a")
    Long countTotalRecords();

    /**
     * Get most recent attendance record for a student
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.student.id = :studentId " +
           "ORDER BY a.attendanceDate DESC, a.createdAt DESC LIMIT 1")
    Optional<AttendanceRecord> findMostRecentByStudentId(@Param("studentId") Long studentId);

    // ========================================================================
    // ADDITIONAL METHODS FOR ATTENDANCE ADJUSTMENT SERVICE
    // Phase 58: Attendance Enhancement - January 2026
    // ========================================================================

    /**
     * Find attendance records for a student on a specific date, ordered by period
     */
    List<AttendanceRecord> findByStudentAndAttendanceDateOrderByPeriodNumberAsc(
            Student student, LocalDate attendanceDate);

    /**
     * Find attendance record for a student on a specific date and period
     */
    List<AttendanceRecord> findByStudentAndAttendanceDateAndPeriodNumber(
            Student student, LocalDate attendanceDate, Integer periodNumber);

    /**
     * Find all attendance records for a specific period on a date
     */
    List<AttendanceRecord> findByAttendanceDateAndPeriodNumberOrderByStudentLastNameAsc(
            LocalDate attendanceDate, Integer periodNumber);

    /**
     * Find attendance records by multiple statuses
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.student = :student " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND a.status IN :statuses " +
           "ORDER BY a.attendanceDate DESC")
    List<AttendanceRecord> findByStudentAndDateRangeAndStatuses(
            @Param("student") Student student,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<AttendanceStatus> statuses);

    /**
     * Count records by status on a specific date
     */
    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.attendanceDate = :date AND a.status = :status")
    Long countByDateAndStatus(@Param("date") LocalDate date, @Param("status") AttendanceStatus status);

    /**
     * Find students without attendance on a specific date
     * (students enrolled but no attendance record exists)
     */
    @Query("SELECT s FROM Student s WHERE s.active = true " +
           "AND s.id NOT IN (SELECT a.student.id FROM AttendanceRecord a WHERE a.attendanceDate = :date)")
    List<Student> findStudentsWithoutAttendanceOnDate(@Param("date") LocalDate date);

    /**
     * Get attendance summary for a date (for dashboard)
     */
    @Query("SELECT a.status, COUNT(DISTINCT a.student.id) FROM AttendanceRecord a " +
           "WHERE a.attendanceDate = :date GROUP BY a.status")
    List<Object[]> getAttendanceSummaryByDate(@Param("date") LocalDate date);

    /**
     * Find attendance records with specific excuse code
     */
    List<AttendanceRecord> findByExcuseCodeOrderByAttendanceDateDesc(String excuseCode);

    // ========================================================================
    // ANALYTICS QUERIES - Phase 59
    // ========================================================================

    /**
     * Get attendance rate for a specific date and optional campus
     */
    @Query("SELECT " +
           "CAST(SUM(CASE WHEN a.status IN ('PRESENT', 'TARDY', 'REMOTE') THEN 1 ELSE 0 END) AS double) * 100.0 / " +
           "CAST(COUNT(a) AS double) " +
           "FROM AttendanceRecord a " +
           "WHERE a.attendanceDate = :date " +
           "AND (:campusId IS NULL OR a.campus.id = :campusId)")
    Double getAttendanceRateForDate(
            @Param("date") LocalDate date,
            @Param("campusId") Long campusId);

    /**
     * Get daily attendance trends for date range
     */
    @Query("SELECT a.attendanceDate, a.status, COUNT(a) FROM AttendanceRecord a " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR a.campus.id = :campusId) " +
           "GROUP BY a.attendanceDate, a.status " +
           "ORDER BY a.attendanceDate")
    List<Object[]> getAttendanceTrends(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Find chronically absent students (attendance rate below threshold)
     */
    @Query("SELECT s.id, s.studentId, s.firstName, s.lastName, s.gradeLevel, " +
           "CAST(SUM(CASE WHEN a.status IN ('ABSENT', 'EXCUSED_ABSENT', 'UNEXCUSED_ABSENT') THEN 1 ELSE 0 END) AS double) * 100.0 / " +
           "CAST(COUNT(a) AS double) AS absenceRate " +
           "FROM AttendanceRecord a JOIN a.student s " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR a.campus.id = :campusId) " +
           "AND s.active = true " +
           "GROUP BY s.id, s.studentId, s.firstName, s.lastName, s.gradeLevel " +
           "HAVING CAST(SUM(CASE WHEN a.status IN ('ABSENT', 'EXCUSED_ABSENT', 'UNEXCUSED_ABSENT') THEN 1 ELSE 0 END) AS double) * 100.0 / " +
           "CAST(COUNT(a) AS double) >= :absenceThreshold " +
           "ORDER BY absenceRate DESC")
    List<Object[]> findChronicallyAbsentStudentsAnalytics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId,
            @Param("absenceThreshold") Double absenceThreshold);

    /**
     * Get attendance rate by grade level
     */
    @Query("SELECT s.gradeLevel, " +
           "CAST(SUM(CASE WHEN a.status IN ('PRESENT', 'TARDY', 'REMOTE') THEN 1 ELSE 0 END) AS double) * 100.0 / " +
           "CAST(COUNT(a) AS double) " +
           "FROM AttendanceRecord a JOIN a.student s " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR a.campus.id = :campusId) " +
           "GROUP BY s.gradeLevel " +
           "ORDER BY s.gradeLevel")
    List<Object[]> getAttendanceRateByGrade(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Get tardy counts by period
     */
    @Query("SELECT a.periodNumber, COUNT(a) FROM AttendanceRecord a " +
           "WHERE a.status = 'TARDY' " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND a.periodNumber IS NOT NULL " +
           "AND (:campusId IS NULL OR a.campus.id = :campusId) " +
           "GROUP BY a.periodNumber " +
           "ORDER BY a.periodNumber")
    List<Object[]> getTardyCountsByPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Get attendance rate by ethnicity for equity analysis
     */
    @Query("SELECT s.ethnicity, " +
           "CAST(SUM(CASE WHEN a.status IN ('PRESENT', 'TARDY', 'REMOTE') THEN 1 ELSE 0 END) AS double) * 100.0 / " +
           "CAST(COUNT(a) AS double), COUNT(DISTINCT s.id) " +
           "FROM AttendanceRecord a JOIN a.student s " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR a.campus.id = :campusId) " +
           "AND s.ethnicity IS NOT NULL " +
           "GROUP BY s.ethnicity")
    List<Object[]> getAttendanceRateByEthnicity(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Get attendance rate by gender for equity analysis
     */
    @Query("SELECT s.gender, " +
           "CAST(SUM(CASE WHEN a.status IN ('PRESENT', 'TARDY', 'REMOTE') THEN 1 ELSE 0 END) AS double) * 100.0 / " +
           "CAST(COUNT(a) AS double), COUNT(DISTINCT s.id) " +
           "FROM AttendanceRecord a JOIN a.student s " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR a.campus.id = :campusId) " +
           "AND s.gender IS NOT NULL " +
           "GROUP BY s.gender")
    List<Object[]> getAttendanceRateByGender(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Get overall attendance metrics for date range
     */
    @Query("SELECT " +
           "COUNT(a), " +
           "SUM(CASE WHEN a.status IN ('PRESENT', 'TARDY', 'REMOTE') THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN a.status IN ('ABSENT', 'EXCUSED_ABSENT', 'UNEXCUSED_ABSENT') THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN a.status = 'TARDY' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN a.status = 'EXCUSED_ABSENT' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN a.status = 'UNEXCUSED_ABSENT' THEN 1 ELSE 0 END) " +
           "FROM AttendanceRecord a " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR a.campus.id = :campusId)")
    List<Object[]> getOverallAttendanceMetrics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Count unique students with attendance in date range
     */
    @Query("SELECT COUNT(DISTINCT a.student.id) FROM AttendanceRecord a " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR a.campus.id = :campusId)")
    Long countStudentsWithAttendance(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId);

    /**
     * Get attendance by campus for district-wide view
     */
    @Query("SELECT a.campus.id, a.campus.name, " +
           "CAST(SUM(CASE WHEN a.status IN ('PRESENT', 'TARDY', 'REMOTE') THEN 1 ELSE 0 END) AS double) * 100.0 / " +
           "CAST(COUNT(a) AS double), COUNT(DISTINCT a.student.id) " +
           "FROM AttendanceRecord a " +
           "WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND a.campus IS NOT NULL " +
           "GROUP BY a.campus.id, a.campus.name")
    List<Object[]> getAttendanceRateByCampus(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find students with most tardies
     */
    @Query("SELECT s.id, s.studentId, s.firstName, s.lastName, s.gradeLevel, COUNT(a) " +
           "FROM AttendanceRecord a JOIN a.student s " +
           "WHERE a.status = 'TARDY' " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND (:campusId IS NULL OR a.campus.id = :campusId) " +
           "GROUP BY s.id, s.studentId, s.firstName, s.lastName, s.gradeLevel " +
           "HAVING COUNT(a) >= :minTardies " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findFrequentTardyStudents(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("campusId") Long campusId,
            @Param("minTardies") Long minTardies);
}
