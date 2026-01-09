package com.heronix.service;

import com.heronix.dto.AttendanceStatistics;
import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceRepository;
import com.heronix.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Report Analytics Service
 *
 * Provides advanced analytics and statistical analysis for attendance reports.
 *
 * Features:
 * - Overall attendance statistics
 * - Daily attendance trends
 * - Grade-level breakdowns
 * - Trend analysis (improving, declining, stable)
 * - Top absentees identification
 * - Day-of-week patterns
 * - Insights and recommendations
 *
 * Analytics Calculations:
 * - Attendance rates (present / total)
 * - Absenteeism rates (absent / total)
 * - Tardy rates (tardy / total)
 * - Trend detection using linear regression
 * - Risk level classification
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 59 - Report Analytics Dashboard
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportAnalyticsService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    /**
     * Get comprehensive attendance statistics for a period
     *
     * @param startDate Period start date
     * @param endDate Period end date
     * @return Attendance statistics
     */
    public AttendanceStatistics getAttendanceStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("Generating attendance statistics for {} to {}", startDate, endDate);

        List<Student> allStudents = studentRepository.findAll();

        AttendanceStatistics stats = AttendanceStatistics.builder()
            .overall(calculateOverallStats(startDate, endDate, allStudents))
            .dailyStats(calculateDailyStats(startDate, endDate))
            .gradeBreakdown(calculateGradeBreakdown(startDate, endDate, allStudents))
            .topAbsentees(calculateTopAbsentees(startDate, endDate, allStudents, 10))
            .build();

        // Calculate trends based on daily stats
        stats.setTrends(calculateTrends(stats.getDailyStats()));

        log.info("Statistics generated: {} students, {:.2f}% attendance rate",
            stats.getOverall().getTotalStudents(),
            stats.getOverall().getAttendanceRate());

        return stats;
    }

    /**
     * Calculate overall statistics
     */
    private AttendanceStatistics.OverallStats calculateOverallStats(
            LocalDate startDate, LocalDate endDate, List<Student> students) {

        long totalStudents = students.size();
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;

        Map<AttendanceRecord.AttendanceStatus, Long> statusCounts = new HashMap<>();
        long totalRecords = 0;

        for (Student student : students) {
            List<AttendanceRecord> records = attendanceRepository
                .findByStudentAndDateBetween(student, startDate, endDate);

            totalRecords += records.size();

            for (AttendanceRecord record : records) {
                statusCounts.merge(record.getStatus(), 1L, Long::sum);
            }
        }

        long presentCount = statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.PRESENT, 0L);
        long absentCount = statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.ABSENT, 0L);
        long tardyCount = statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.TARDY, 0L);

        double attendanceRate = totalRecords > 0 ? (presentCount * 100.0 / totalRecords) : 0.0;
        double absenteeismRate = totalRecords > 0 ? (absentCount * 100.0 / totalRecords) : 0.0;
        double tardyRate = totalRecords > 0 ? (tardyCount * 100.0 / totalRecords) : 0.0;

        return AttendanceStatistics.OverallStats.builder()
            .totalStudents(totalStudents)
            .totalDays(totalDays)
            .totalRecords(totalRecords)
            .presentCount(presentCount)
            .absentCount(absentCount)
            .tardyCount(tardyCount)
            .attendanceRate(attendanceRate)
            .absenteeismRate(absenteeismRate)
            .tardyRate(tardyRate)
            .periodStart(startDate)
            .periodEnd(endDate)
            .build();
    }

    /**
     * Calculate daily statistics
     */
    private List<AttendanceStatistics.DailyStats> calculateDailyStats(
            LocalDate startDate, LocalDate endDate) {

        List<AttendanceStatistics.DailyStats> dailyStats = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<AttendanceRecord> records = attendanceRepository.findByAttendanceDate(date);

            long presentCount = records.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                .count();
            long absentCount = records.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT)
                .count();
            long tardyCount = records.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.TARDY)
                .count();

            long totalCount = records.size();
            double attendanceRate = totalCount > 0 ? (presentCount * 100.0 / totalCount) : 0.0;

            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            dailyStats.add(AttendanceStatistics.DailyStats.builder()
                .date(date)
                .presentCount(presentCount)
                .absentCount(absentCount)
                .tardyCount(tardyCount)
                .totalCount(totalCount)
                .attendanceRate(attendanceRate)
                .dayOfWeek(dayOfWeek)
                .build());
        }

        return dailyStats;
    }

    /**
     * Calculate grade-level breakdown
     */
    private Map<String, AttendanceStatistics.GradeStats> calculateGradeBreakdown(
            LocalDate startDate, LocalDate endDate, List<Student> students) {

        Map<String, AttendanceStatistics.GradeStats> gradeMap = new HashMap<>();

        for (Student student : students) {
            String grade = student.getGradeLevel() != null ?
                student.getGradeLevel().toString() : "Unknown";

            List<AttendanceRecord> records = attendanceRepository
                .findByStudentAndDateBetween(student, startDate, endDate);

            AttendanceStatistics.GradeStats gradeStats = gradeMap.computeIfAbsent(grade, g ->
                AttendanceStatistics.GradeStats.builder()
                    .grade(g)
                    .studentCount(0)
                    .presentCount(0)
                    .absentCount(0)
                    .tardyCount(0)
                    .build()
            );

            gradeStats.setStudentCount(gradeStats.getStudentCount() + 1);

            for (AttendanceRecord record : records) {
                switch (record.getStatus()) {
                    case PRESENT -> gradeStats.setPresentCount(gradeStats.getPresentCount() + 1);
                    case ABSENT, EXCUSED_ABSENT, UNEXCUSED_ABSENT -> gradeStats.setAbsentCount(gradeStats.getAbsentCount() + 1);
                    case TARDY -> gradeStats.setTardyCount(gradeStats.getTardyCount() + 1);
                    case REMOTE, SCHOOL_ACTIVITY, EARLY_DEPARTURE, SUSPENDED, HALF_DAY -> {
                        // Count these statuses as present for attendance rate purposes
                        gradeStats.setPresentCount(gradeStats.getPresentCount() + 1);
                    }
                }
            }
        }

        // Calculate rates
        for (AttendanceStatistics.GradeStats stats : gradeMap.values()) {
            long total = stats.getPresentCount() + stats.getAbsentCount() + stats.getTardyCount();
            stats.setAttendanceRate(total > 0 ? (stats.getPresentCount() * 100.0 / total) : 0.0);
            stats.setAbsenteeismRate(total > 0 ? (stats.getAbsentCount() * 100.0 / total) : 0.0);
        }

        return gradeMap;
    }

    /**
     * Calculate top absentees
     */
    private List<AttendanceStatistics.StudentAbsenceRecord> calculateTopAbsentees(
            LocalDate startDate, LocalDate endDate, List<Student> students, int limit) {

        return students.stream()
            .map(student -> {
                List<AttendanceRecord> records = attendanceRepository
                    .findByStudentAndDateBetween(student, startDate, endDate);

                long totalDays = records.size();
                long absentDays = records.stream()
                    .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT)
                    .count();

                double absenteeismRate = totalDays > 0 ? (absentDays * 100.0 / totalDays) : 0.0;

                return AttendanceStatistics.StudentAbsenceRecord.builder()
                    .studentId(student.getId())
                    .studentName(student.getFullName())
                    .grade(student.getGradeLevel() != null ? student.getGradeLevel().toString() : "N/A")
                    .totalDays(totalDays)
                    .absentDays(absentDays)
                    .absenteeismRate(absenteeismRate)
                    .riskLevel(getRiskLevel(100.0 - absenteeismRate))
                    .build();
            })
            .filter(record -> record.getAbsentDays() > 0)
            .sorted(Comparator.comparingDouble(AttendanceStatistics.StudentAbsenceRecord::getAbsenteeismRate).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Calculate trend analysis
     */
    private AttendanceStatistics.TrendAnalysis calculateTrends(
            List<AttendanceStatistics.DailyStats> dailyStats) {

        if (dailyStats.isEmpty()) {
            return AttendanceStatistics.TrendAnalysis.builder()
                .trend("UNKNOWN")
                .changePercentage(0.0)
                .averageAttendanceRate(0.0)
                .insights(List.of("Insufficient data for trend analysis"))
                .build();
        }

        // Calculate average
        double averageRate = dailyStats.stream()
            .mapToDouble(AttendanceStatistics.DailyStats::getAttendanceRate)
            .average()
            .orElse(0.0);

        // Find best and worst days
        AttendanceStatistics.DailyStats bestDay = dailyStats.stream()
            .max(Comparator.comparingDouble(AttendanceStatistics.DailyStats::getAttendanceRate))
            .orElse(null);

        AttendanceStatistics.DailyStats worstDay = dailyStats.stream()
            .min(Comparator.comparingDouble(AttendanceStatistics.DailyStats::getAttendanceRate))
            .orElse(null);

        // Calculate trend using simple linear regression
        String trend = calculateTrendDirection(dailyStats);
        double changePercentage = calculateChangePercentage(dailyStats);

        // Generate insights
        List<String> insights = generateInsights(dailyStats, averageRate, trend);

        return AttendanceStatistics.TrendAnalysis.builder()
            .trend(trend)
            .changePercentage(changePercentage)
            .averageAttendanceRate(averageRate)
            .bestDayRate(bestDay != null ? bestDay.getAttendanceRate() : 0.0)
            .worstDayRate(worstDay != null ? worstDay.getAttendanceRate() : 0.0)
            .bestDay(bestDay != null ? bestDay.getDate().toString() + " (" + bestDay.getDayOfWeek() + ")" : "N/A")
            .worstDay(worstDay != null ? worstDay.getDate().toString() + " (" + worstDay.getDayOfWeek() + ")" : "N/A")
            .insights(insights)
            .build();
    }

    /**
     * Calculate trend direction using simple linear regression
     */
    private String calculateTrendDirection(List<AttendanceStatistics.DailyStats> dailyStats) {
        if (dailyStats.size() < 2) {
            return "STABLE";
        }

        // Simple approach: compare first half vs second half
        int midpoint = dailyStats.size() / 2;

        double firstHalfAvg = dailyStats.subList(0, midpoint).stream()
            .mapToDouble(AttendanceStatistics.DailyStats::getAttendanceRate)
            .average()
            .orElse(0.0);

        double secondHalfAvg = dailyStats.subList(midpoint, dailyStats.size()).stream()
            .mapToDouble(AttendanceStatistics.DailyStats::getAttendanceRate)
            .average()
            .orElse(0.0);

        double diff = secondHalfAvg - firstHalfAvg;

        if (diff > 2.0) {
            return "IMPROVING";
        } else if (diff < -2.0) {
            return "DECLINING";
        } else {
            return "STABLE";
        }
    }

    /**
     * Calculate percentage change
     */
    private double calculateChangePercentage(List<AttendanceStatistics.DailyStats> dailyStats) {
        if (dailyStats.size() < 2) {
            return 0.0;
        }

        double firstRate = dailyStats.get(0).getAttendanceRate();
        double lastRate = dailyStats.get(dailyStats.size() - 1).getAttendanceRate();

        if (firstRate == 0) {
            return 0.0;
        }

        return ((lastRate - firstRate) / firstRate) * 100.0;
    }

    /**
     * Generate insights based on data
     */
    private List<String> generateInsights(List<AttendanceStatistics.DailyStats> dailyStats,
                                         double averageRate, String trend) {
        List<String> insights = new ArrayList<>();

        // Overall performance
        if (averageRate >= 95.0) {
            insights.add("Excellent attendance - maintain current practices");
        } else if (averageRate >= 90.0) {
            insights.add("Good attendance - minor improvements possible");
        } else if (averageRate >= 85.0) {
            insights.add("Moderate attendance - intervention recommended");
        } else {
            insights.add("Poor attendance - immediate action required");
        }

        // Trend insight
        switch (trend) {
            case "IMPROVING" -> insights.add("Positive trend - attendance is improving");
            case "DECLINING" -> insights.add("Concerning trend - attendance is declining");
            case "STABLE" -> insights.add("Stable trend - attendance is consistent");
        }

        // Day of week patterns
        Map<DayOfWeek, Double> dayAverages = dailyStats.stream()
            .collect(Collectors.groupingBy(
                stat -> LocalDate.parse(stat.getDate().toString()).getDayOfWeek(),
                Collectors.averagingDouble(AttendanceStatistics.DailyStats::getAttendanceRate)
            ));

        DayOfWeek worstDay = dayAverages.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);

        if (worstDay != null) {
            insights.add(String.format("Lowest attendance on %ss - consider targeted interventions",
                worstDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH)));
        }

        return insights;
    }

    /**
     * Determine risk level based on attendance rate
     */
    private String getRiskLevel(double attendanceRate) {
        if (attendanceRate < 80.0) {
            return "CRITICAL";
        } else if (attendanceRate < 85.0) {
            return "HIGH";
        } else if (attendanceRate < 90.0) {
            return "MODERATE";
        } else {
            return "LOW";
        }
    }
}
