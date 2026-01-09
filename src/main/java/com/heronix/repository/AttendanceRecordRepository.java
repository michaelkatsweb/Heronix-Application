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
}
