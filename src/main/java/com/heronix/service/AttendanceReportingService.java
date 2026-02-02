package com.heronix.service;

import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.AttendanceRecord.AttendanceStatus;
import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceRepository;
import com.heronix.repository.StudentRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Attendance Reporting Service
 *
 * Provides state-mandated attendance reporting including ADA (Average Daily Attendance)
 * and ADM (Average Daily Membership) calculations for funding and compliance.
 *
 * Key Responsibilities:
 * - Calculate ADA (Average Daily Attendance) for state funding
 * - Calculate ADM (Average Daily Membership) for enrollment reporting
 * - Generate period-based attendance reports for teachers
 * - Produce state-mandated attendance reports
 * - Track attendance by demographic groups
 * - Generate truancy reports
 * - Export attendance data for state systems
 *
 * ADA Formula: Total Days Present / Total Days in Period
 * ADM Formula: Total Days Enrolled / Total Days in Period
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Enhancement
 */
@Slf4j
@Service
public class AttendanceReportingService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    // ========================================================================
    // ADA/ADM CALCULATIONS
    // ========================================================================

    /**
     * Calculate Average Daily Attendance (ADA) for a school/district
     * This is critical for state funding calculations
     */
    public ADACalculation calculateADA(
            Long schoolId,
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Calculating ADA for school {} from {} to {}", schoolId, startDate, endDate);

        // Use existing repository methods to find records
        List<AttendanceRecord> records = attendanceRepository.findAll().stream()
                .filter(r -> !r.getAttendanceDate().isBefore(startDate) && !r.getAttendanceDate().isAfter(endDate))
                .filter(r -> schoolId == null || (r.getCampus() != null && r.getCampus().getId().equals(schoolId)))
                .toList();

        if (records.isEmpty()) {
            return ADACalculation.builder()
                    .schoolId(schoolId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalDaysInPeriod(calculateSchoolDays(startDate, endDate))
                    .totalDaysPresent(0.0)
                    .totalDaysAbsent(0.0)
                    .ada(0.0)
                    .attendanceRate(0.0)
                    .build();
        }

        // Calculate present days (full day = 1.0, tardy = 0.5, etc.)
        double totalDaysPresent = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
                .count();

        // Tardies count as partial attendance (configurable)
        double tardyDays = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.TARDY)
                .count() * 0.5; // Tardy = 0.5 day credit

        totalDaysPresent += tardyDays;

        double totalDaysAbsent = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                           r.getStatus() == AttendanceStatus.UNEXCUSED_ABSENT ||
                           r.getStatus() == AttendanceStatus.EXCUSED_ABSENT)
                .count();

        int schoolDays = calculateSchoolDays(startDate, endDate);
        double ada = totalDaysPresent / schoolDays;
        double attendanceRate = (totalDaysPresent / (totalDaysPresent + totalDaysAbsent)) * 100;

        return ADACalculation.builder()
                .schoolId(schoolId)
                .startDate(startDate)
                .endDate(endDate)
                .totalDaysInPeriod(schoolDays)
                .totalDaysPresent(totalDaysPresent)
                .totalDaysAbsent(totalDaysAbsent)
                .ada(ada)
                .attendanceRate(attendanceRate)
                .calculatedDate(LocalDate.now())
                .build();
    }

    /**
     * Calculate Average Daily Membership (ADM)
     * Tracks total enrollment days for state reporting
     */
    public ADMCalculation calculateADM(
            Long schoolId,
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Calculating ADM for school {} from {} to {}", schoolId, startDate, endDate);

        // Use existing repository method
        List<Student> enrolledStudents = studentRepository.findAllActive();

        if (enrolledStudents.isEmpty()) {
            return ADMCalculation.builder()
                    .schoolId(schoolId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalDaysInPeriod(calculateSchoolDays(startDate, endDate))
                    .totalEnrollmentDays(0.0)
                    .adm(0.0)
                    .averageEnrollment(0.0)
                    .build();
        }

        int schoolDays = calculateSchoolDays(startDate, endDate);

        // Calculate total membership days (students × days enrolled)
        double totalEnrollmentDays = 0.0;
        for (Student student : enrolledStudents) {
            int daysEnrolled = calculateDaysEnrolled(student, startDate, endDate);
            totalEnrollmentDays += daysEnrolled;
        }

        double adm = totalEnrollmentDays / schoolDays;
        double avgEnrollment = (double) enrolledStudents.size();

        return ADMCalculation.builder()
                .schoolId(schoolId)
                .startDate(startDate)
                .endDate(endDate)
                .totalDaysInPeriod(schoolDays)
                .totalEnrollmentDays(totalEnrollmentDays)
                .totalStudents(enrolledStudents.size())
                .adm(adm)
                .averageEnrollment(avgEnrollment)
                .calculatedDate(LocalDate.now())
                .build();
    }

    /**
     * Calculate both ADA and ADM together for comprehensive reporting
     */
    public AttendanceFundingReport generateFundingReport(
            Long schoolId,
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Generating funding report for school {} from {} to {}", schoolId, startDate, endDate);

        ADACalculation ada = calculateADA(schoolId, startDate, endDate);
        ADMCalculation adm = calculateADM(schoolId, startDate, endDate);

        // Calculate funding metrics
        double attendanceToEnrollmentRatio = (adm.getAdm() > 0) ?
                (ada.getAda() / adm.getAdm()) * 100 : 0.0;

        return AttendanceFundingReport.builder()
                .schoolId(schoolId)
                .startDate(startDate)
                .endDate(endDate)
                .ada(ada)
                .adm(adm)
                .attendanceToEnrollmentRatio(attendanceToEnrollmentRatio)
                .reportGeneratedDate(LocalDate.now())
                .build();
    }

    // ========================================================================
    // PERIOD-BASED REPORTING (For Teachers)
    // ========================================================================

    /**
     * Get attendance report for a specific class period
     * Used by teachers to view/record attendance
     */
    public PeriodAttendanceReport getPeriodAttendanceReport(
            Long courseId,
            Integer periodNumber,
            LocalDate date) {

        log.info("Getting period attendance for course {} period {} on {}", courseId, periodNumber, date);

        // Use existing repository method (attendance by course and date, filter by period)
        List<AttendanceRecord> records = attendanceRepository
                .findByCourseIdAndAttendanceDate(courseId, date).stream()
                .filter(r -> r.getPeriodNumber() != null && r.getPeriodNumber().equals(periodNumber))
                .toList();

        Map<AttendanceStatus, Long> statusCounts = records.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getStatus, Collectors.counting()));

        List<StudentAttendanceRecord> studentRecords = records.stream()
                .map(r -> StudentAttendanceRecord.builder()
                        .studentId(r.getStudent().getId())
                        .studentName(r.getStudent().getFullName())
                        .status(r.getStatus())
                        .arrivalTime(r.getArrivalTime())
                        .notes(r.getNotes())
                        .build())
                .collect(Collectors.toList());

        return PeriodAttendanceReport.builder()
                .courseId(courseId)
                .periodNumber(periodNumber)
                .date(date)
                .totalStudents(records.size())
                .presentCount(statusCounts.getOrDefault(AttendanceStatus.PRESENT, 0L).intValue())
                .absentCount(statusCounts.getOrDefault(AttendanceStatus.ABSENT, 0L).intValue() +
                           statusCounts.getOrDefault(AttendanceStatus.UNEXCUSED_ABSENT, 0L).intValue())
                .tardyCount(statusCounts.getOrDefault(AttendanceStatus.TARDY, 0L).intValue())
                .studentRecords(studentRecords)
                .build();
    }

    /**
     * Get attendance summary for all periods in a day
     */
    public DailyPeriodSummary getDailyPeriodSummary(LocalDate date) {
        log.info("Getting daily period summary for {}", date);

        // Get all records for the date across all campuses
        List<AttendanceRecord> records = attendanceRepository.findAll().stream()
                .filter(r -> r.getAttendanceDate().equals(date))
                .toList();

        Map<Integer, List<AttendanceRecord>> byPeriod = records.stream()
                .filter(r -> r.getPeriodNumber() != null)
                .collect(Collectors.groupingBy(AttendanceRecord::getPeriodNumber));

        List<PeriodSummary> periodSummaries = new ArrayList<>();

        for (Map.Entry<Integer, List<AttendanceRecord>> entry : byPeriod.entrySet()) {
            List<AttendanceRecord> periodRecords = entry.getValue();
            long present = periodRecords.stream()
                    .filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            long absent = periodRecords.stream()
                    .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                               r.getStatus() == AttendanceStatus.UNEXCUSED_ABSENT).count();

            periodSummaries.add(PeriodSummary.builder()
                    .periodNumber(entry.getKey())
                    .totalStudents(periodRecords.size())
                    .presentCount((int) present)
                    .absentCount((int) absent)
                    .attendanceRate(periodRecords.size() > 0 ?
                            (double) present / periodRecords.size() * 100 : 0.0)
                    .build());
        }

        return DailyPeriodSummary.builder()
                .date(date)
                .periodSummaries(periodSummaries)
                .overallAttendanceRate(calculateOverallRate(periodSummaries))
                .build();
    }

    // ========================================================================
    // TRUANCY REPORTING
    // ========================================================================

    /**
     * Generate truancy report for students with excessive absences
     */
    public TruancyReport generateTruancyReport(
            LocalDate startDate,
            LocalDate endDate,
            int truancyThreshold) {

        log.info("Generating truancy report from {} to {} with threshold {}",
                startDate, endDate, truancyThreshold);

        // Use existing findStudentsWithChronicAbsences method (equivalent for truancy)
        List<Object[]> results = attendanceRepository
                .findStudentsWithChronicAbsences(startDate, endDate, (long) truancyThreshold);

        List<TruancyCase> truancyCases = new ArrayList<>();

        for (Object[] row : results) {
            Long studentId = (Long) row[0];
            Long unexcusedAbsences = (Long) row[1];

            Student student = studentRepository.findById(studentId).orElse(null);
            if (student != null) {
                String severity = unexcusedAbsences >= truancyThreshold * 2 ? "SEVERE" :
                                unexcusedAbsences >= truancyThreshold * 1.5 ? "MODERATE" : "MILD";

                truancyCases.add(TruancyCase.builder()
                        .studentId(studentId)
                        .studentName(student.getFullName())
                        .gradeLevel(student.getGradeLevel())
                        .unexcusedAbsences(unexcusedAbsences.intValue())
                        .severity(severity)
                        .requiresIntervention(unexcusedAbsences >= truancyThreshold)
                        .build());
            }
        }

        return TruancyReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .truancyThreshold(truancyThreshold)
                .totalTruantStudents(truancyCases.size())
                .truancyCases(truancyCases)
                .reportGeneratedDate(LocalDate.now())
                .build();
    }

    // ========================================================================
    // ATTENDANCE AGGREGATION BY TEACHER/GRADE
    // ========================================================================

    /**
     * Get attendance statistics aggregated by teacher
     * Useful for evaluating teacher attendance recording patterns
     */
    public List<TeacherAttendanceStats> getAttendanceByTeacher(LocalDate startDate, LocalDate endDate) {
        log.info("Generating attendance statistics by teacher from {} to {}", startDate, endDate);

        List<AttendanceRecord> records = attendanceRepository.findAll().stream()
                .filter(r -> !r.getAttendanceDate().isBefore(startDate) && !r.getAttendanceDate().isAfter(endDate))
                .filter(r -> r.getCourse() != null && r.getCourse().getTeacher() != null)
                .toList();

        // Group by teacher
        Map<Long, List<AttendanceRecord>> byTeacher = records.stream()
                .collect(Collectors.groupingBy(r -> r.getCourse().getTeacher().getId()));

        List<TeacherAttendanceStats> stats = new ArrayList<>();

        for (Map.Entry<Long, List<AttendanceRecord>> entry : byTeacher.entrySet()) {
            List<AttendanceRecord> teacherRecords = entry.getValue();
            if (teacherRecords.isEmpty()) continue;

            var teacher = teacherRecords.get(0).getCourse().getTeacher();

            long totalRecords = teacherRecords.size();
            long presentCount = teacherRecords.stream()
                    .filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            long absentCount = teacherRecords.stream()
                    .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                               r.getStatus() == AttendanceStatus.UNEXCUSED_ABSENT ||
                               r.getStatus() == AttendanceStatus.EXCUSED_ABSENT).count();
            long tardyCount = teacherRecords.stream()
                    .filter(r -> r.getStatus() == AttendanceStatus.TARDY).count();

            double attendanceRate = totalRecords > 0 ? (double) presentCount / totalRecords * 100 : 0.0;

            // Count unique courses
            long uniqueCourses = teacherRecords.stream()
                    .map(r -> r.getCourse().getId())
                    .distinct()
                    .count();

            // Count unique students
            long uniqueStudents = teacherRecords.stream()
                    .map(r -> r.getStudent().getId())
                    .distinct()
                    .count();

            stats.add(TeacherAttendanceStats.builder()
                    .teacherId(teacher.getId())
                    .teacherName(teacher.getFirstName() + " " + teacher.getLastName())
                    .totalRecords((int) totalRecords)
                    .presentCount((int) presentCount)
                    .absentCount((int) absentCount)
                    .tardyCount((int) tardyCount)
                    .attendanceRate(attendanceRate)
                    .coursesCount((int) uniqueCourses)
                    .studentsCount((int) uniqueStudents)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build());
        }

        return stats;
    }

    /**
     * Get attendance statistics aggregated by grade level
     * Useful for identifying grade-level attendance trends
     */
    public List<GradeAttendanceStats> getAttendanceByGrade(LocalDate startDate, LocalDate endDate) {
        log.info("Generating attendance statistics by grade from {} to {}", startDate, endDate);

        List<AttendanceRecord> records = attendanceRepository.findAll().stream()
                .filter(r -> !r.getAttendanceDate().isBefore(startDate) && !r.getAttendanceDate().isAfter(endDate))
                .filter(r -> r.getStudent() != null && r.getStudent().getGradeLevel() != null)
                .toList();

        // Group by grade level
        Map<String, List<AttendanceRecord>> byGrade = records.stream()
                .collect(Collectors.groupingBy(r -> r.getStudent().getGradeLevel()));

        List<GradeAttendanceStats> stats = new ArrayList<>();

        for (Map.Entry<String, List<AttendanceRecord>> entry : byGrade.entrySet()) {
            List<AttendanceRecord> gradeRecords = entry.getValue();

            long totalRecords = gradeRecords.size();
            long presentCount = gradeRecords.stream()
                    .filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            long absentCount = gradeRecords.stream()
                    .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                               r.getStatus() == AttendanceStatus.UNEXCUSED_ABSENT ||
                               r.getStatus() == AttendanceStatus.EXCUSED_ABSENT).count();
            long tardyCount = gradeRecords.stream()
                    .filter(r -> r.getStatus() == AttendanceStatus.TARDY).count();

            double attendanceRate = totalRecords > 0 ? (double) presentCount / totalRecords * 100 : 0.0;

            // Count unique students
            long uniqueStudents = gradeRecords.stream()
                    .map(r -> r.getStudent().getId())
                    .distinct()
                    .count();

            // Calculate chronic absenteeism (10%+ absence rate)
            int schoolDays = calculateSchoolDays(startDate, endDate);
            double chronicThreshold = schoolDays * 0.10;

            Map<Long, Long> studentAbsences = gradeRecords.stream()
                    .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                               r.getStatus() == AttendanceStatus.UNEXCUSED_ABSENT)
                    .collect(Collectors.groupingBy(
                            r -> r.getStudent().getId(),
                            Collectors.counting()
                    ));

            long chronicAbsentCount = studentAbsences.values().stream()
                    .filter(count -> count >= chronicThreshold)
                    .count();

            double chronicAbsentRate = uniqueStudents > 0 ?
                    (double) chronicAbsentCount / uniqueStudents * 100 : 0.0;

            stats.add(GradeAttendanceStats.builder()
                    .gradeLevel(entry.getKey())
                    .totalRecords((int) totalRecords)
                    .presentCount((int) presentCount)
                    .absentCount((int) absentCount)
                    .tardyCount((int) tardyCount)
                    .attendanceRate(attendanceRate)
                    .studentsCount((int) uniqueStudents)
                    .chronicAbsentCount((int) chronicAbsentCount)
                    .chronicAbsentRate(chronicAbsentRate)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build());
        }

        // Sort by grade level
        stats.sort(Comparator.comparing(GradeAttendanceStats::getGradeLevel));

        return stats;
    }

    /**
     * Get comprehensive attendance comparison across all grades
     */
    public GradeComparisonReport getGradeComparisonReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating grade comparison report from {} to {}", startDate, endDate);

        List<GradeAttendanceStats> gradeStats = getAttendanceByGrade(startDate, endDate);

        // Find best and worst performing grades
        GradeAttendanceStats bestGrade = gradeStats.stream()
                .max(Comparator.comparing(GradeAttendanceStats::getAttendanceRate))
                .orElse(null);

        GradeAttendanceStats worstGrade = gradeStats.stream()
                .min(Comparator.comparing(GradeAttendanceStats::getAttendanceRate))
                .orElse(null);

        // Calculate overall statistics
        int totalStudents = gradeStats.stream()
                .mapToInt(GradeAttendanceStats::getStudentsCount)
                .sum();

        double averageAttendanceRate = gradeStats.stream()
                .mapToDouble(GradeAttendanceStats::getAttendanceRate)
                .average()
                .orElse(0.0);

        int totalChronicAbsent = gradeStats.stream()
                .mapToInt(GradeAttendanceStats::getChronicAbsentCount)
                .sum();

        return GradeComparisonReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .gradeStats(gradeStats)
                .bestPerformingGrade(bestGrade != null ? bestGrade.getGradeLevel() : "N/A")
                .worstPerformingGrade(worstGrade != null ? worstGrade.getGradeLevel() : "N/A")
                .totalStudents(totalStudents)
                .averageAttendanceRate(averageAttendanceRate)
                .totalChronicAbsent(totalChronicAbsent)
                .reportGeneratedDate(LocalDate.now())
                .build();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private int calculateSchoolDays(LocalDate startDate, LocalDate endDate) {
        // Simplified: exclude weekends
        // In production, would also exclude holidays from school calendar
        int days = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (current.getDayOfWeek().getValue() < 6) { // Monday-Friday
                days++;
            }
            current = current.plusDays(1);
        }
        return days;
    }

    private int calculateDaysEnrolled(Student student, LocalDate startDate, LocalDate endDate) {
        // Student entity lacks enrollmentDate/withdrawalDate fields.
        // Assumes student was enrolled for entire period until those fields are added.
        LocalDate enrollStart = startDate;
        LocalDate enrollEnd = endDate;

        LocalDate effectiveStart = (enrollStart != null && enrollStart.isAfter(startDate)) ?
                enrollStart : startDate;
        LocalDate effectiveEnd = (enrollEnd != null && enrollEnd.isBefore(endDate)) ?
                enrollEnd : endDate;

        if (effectiveStart.isAfter(effectiveEnd)) {
            return 0;
        }

        return calculateSchoolDays(effectiveStart, effectiveEnd);
    }

    private double calculateOverallRate(List<PeriodSummary> summaries) {
        if (summaries.isEmpty()) return 0.0;

        int totalPresent = summaries.stream().mapToInt(PeriodSummary::getPresentCount).sum();
        int totalStudents = summaries.stream().mapToInt(PeriodSummary::getTotalStudents).sum();

        return totalStudents > 0 ? (double) totalPresent / totalStudents * 100 : 0.0;
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class ADACalculation {
        private Long schoolId;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalDaysInPeriod;
        private Double totalDaysPresent;
        private Double totalDaysAbsent;
        private Double ada;
        private Double attendanceRate;
        private LocalDate calculatedDate;
    }

    @Data
    @Builder
    public static class ADMCalculation {
        private Long schoolId;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalDaysInPeriod;
        private Double totalEnrollmentDays;
        private Integer totalStudents;
        private Double adm;
        private Double averageEnrollment;
        private LocalDate calculatedDate;
    }

    @Data
    @Builder
    public static class AttendanceFundingReport {
        private Long schoolId;
        private LocalDate startDate;
        private LocalDate endDate;
        private ADACalculation ada;
        private ADMCalculation adm;
        private Double attendanceToEnrollmentRatio;
        private LocalDate reportGeneratedDate;
    }

    @Data
    @Builder
    public static class PeriodAttendanceReport {
        private Long courseId;
        private Integer periodNumber;
        private LocalDate date;
        private Integer totalStudents;
        private Integer presentCount;
        private Integer absentCount;
        private Integer tardyCount;
        private List<StudentAttendanceRecord> studentRecords;
    }

    @Data
    @Builder
    public static class StudentAttendanceRecord {
        private Long studentId;
        private String studentName;
        private AttendanceStatus status;
        private java.time.LocalTime arrivalTime;
        private String notes;
    }

    @Data
    @Builder
    public static class PeriodSummary {
        private Integer periodNumber;
        private Integer totalStudents;
        private Integer presentCount;
        private Integer absentCount;
        private Double attendanceRate;
    }

    @Data
    @Builder
    public static class DailyPeriodSummary {
        private LocalDate date;
        private List<PeriodSummary> periodSummaries;
        private Double overallAttendanceRate;
    }

    @Data
    @Builder
    public static class TruancyReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer truancyThreshold;
        private Integer totalTruantStudents;
        private List<TruancyCase> truancyCases;
        private LocalDate reportGeneratedDate;
    }

    @Data
    @Builder
    public static class TruancyCase {
        private Long studentId;
        private String studentName;
        private String gradeLevel;
        private Integer unexcusedAbsences;
        private String severity;
        private Boolean requiresIntervention;
    }

    @Data
    @Builder
    public static class TeacherAttendanceStats {
        private Long teacherId;
        private String teacherName;
        private Integer totalRecords;
        private Integer presentCount;
        private Integer absentCount;
        private Integer tardyCount;
        private Double attendanceRate;
        private Integer coursesCount;
        private Integer studentsCount;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    @Builder
    public static class GradeAttendanceStats {
        private String gradeLevel;
        private Integer totalRecords;
        private Integer presentCount;
        private Integer absentCount;
        private Integer tardyCount;
        private Double attendanceRate;
        private Integer studentsCount;
        private Integer chronicAbsentCount;
        private Double chronicAbsentRate;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    @Builder
    public static class GradeComparisonReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private List<GradeAttendanceStats> gradeStats;
        private String bestPerformingGrade;
        private String worstPerformingGrade;
        private Integer totalStudents;
        private Double averageAttendanceRate;
        private Integer totalChronicAbsent;
        private LocalDate reportGeneratedDate;
    }
}
