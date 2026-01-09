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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Attendance Summary Report Service
 *
 * Generates comprehensive attendance summary reports on weekly, monthly, and yearly schedules.
 * Provides trend analysis, comparative statistics, and benchmark reporting for administrators.
 *
 * Key Responsibilities:
 * - Weekly attendance summaries with day-by-day breakdown
 * - Monthly attendance reports with trend analysis
 * - Yearly attendance reports with semester comparison
 * - Attendance trend identification and forecasting
 * - Benchmark comparisons across time periods
 * - Executive summary dashboards
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Enhancement
 */
@Slf4j
@Service
public class AttendanceSummaryReportService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AttendanceReportingService reportingService;

    // ========================================================================
    // WEEKLY SUMMARY REPORTS
    // ========================================================================

    /**
     * Generate weekly attendance summary with day-by-day breakdown
     */
    public WeeklySummaryReport generateWeeklySummary(LocalDate weekStart) {
        log.info("Generating weekly summary starting {}", weekStart);

        // Ensure weekStart is Monday
        LocalDate monday = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate friday = monday.plusDays(4); // Monday to Friday

        List<AttendanceRecord> weekRecords = attendanceRepository.findAll().stream()
                .filter(r -> !r.getAttendanceDate().isBefore(monday) && !r.getAttendanceDate().isAfter(friday))
                .toList();

        // Daily breakdown
        List<DailySummary> dailySummaries = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LocalDate day = monday.plusDays(i);
            dailySummaries.add(generateDailySummary(day, weekRecords));
        }

        // Calculate week totals
        int totalRecords = weekRecords.size();
        long presentCount = weekRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absentCount = weekRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                           r.getStatus() == AttendanceStatus.UNEXCUSED_ABSENT ||
                           r.getStatus() == AttendanceStatus.EXCUSED_ABSENT).count();
        long tardyCount = weekRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.TARDY).count();

        double weeklyAttendanceRate = totalRecords > 0 ? (double) presentCount / totalRecords * 100 : 0.0;

        // Identify students with perfect weekly attendance
        List<Long> perfectAttendanceStudents = findPerfectWeeklyAttendance(weekRecords, monday, friday);

        // Identify students with multiple absences this week
        List<Student> atRiskStudents = findWeeklyAtRiskStudents(weekRecords);

        return WeeklySummaryReport.builder()
                .weekStartDate(monday)
                .weekEndDate(friday)
                .dailySummaries(dailySummaries)
                .totalRecords(totalRecords)
                .presentCount((int) presentCount)
                .absentCount((int) absentCount)
                .tardyCount((int) tardyCount)
                .weeklyAttendanceRate(weeklyAttendanceRate)
                .perfectAttendanceStudents(perfectAttendanceStudents)
                .atRiskStudentCount(atRiskStudents.size())
                .generatedDate(LocalDate.now())
                .build();
    }

    /**
     * Generate multiple week summary for trend analysis
     */
    public WeeklyTrendReport generateWeeklyTrend(LocalDate startDate, int numberOfWeeks) {
        log.info("Generating weekly trend for {} weeks starting {}", numberOfWeeks, startDate);

        List<WeeklySummaryReport> weeklySummaries = new ArrayList<>();
        LocalDate currentWeek = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (int i = 0; i < numberOfWeeks; i++) {
            weeklySummaries.add(generateWeeklySummary(currentWeek));
            currentWeek = currentWeek.plusWeeks(1);
        }

        // Calculate trend direction
        double avgAttendanceRate = weeklySummaries.stream()
                .mapToDouble(WeeklySummaryReport::getWeeklyAttendanceRate)
                .average()
                .orElse(0.0);

        String trend = determineTrend(weeklySummaries);

        return WeeklyTrendReport.builder()
                .startDate(startDate)
                .numberOfWeeks(numberOfWeeks)
                .weeklySummaries(weeklySummaries)
                .averageAttendanceRate(avgAttendanceRate)
                .trend(trend)
                .generatedDate(LocalDate.now())
                .build();
    }

    // ========================================================================
    // MONTHLY SUMMARY REPORTS
    // ========================================================================

    /**
     * Generate monthly attendance summary
     */
    public MonthlySummaryReport generateMonthlySummary(int year, int month) {
        log.info("Generating monthly summary for {}-{}", year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<AttendanceRecord> monthRecords = attendanceRepository.findAll().stream()
                .filter(r -> !r.getAttendanceDate().isBefore(startDate) && !r.getAttendanceDate().isAfter(endDate))
                .toList();

        // Calculate month statistics
        int totalRecords = monthRecords.size();
        long presentCount = monthRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absentCount = monthRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                           r.getStatus() == AttendanceStatus.UNEXCUSED_ABSENT ||
                           r.getStatus() == AttendanceStatus.EXCUSED_ABSENT).count();
        long tardyCount = monthRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.TARDY).count();

        double monthlyAttendanceRate = totalRecords > 0 ? (double) presentCount / totalRecords * 100 : 0.0;

        // ADA/ADM for the month
        var ada = reportingService.calculateADA(null, startDate, endDate);
        var adm = reportingService.calculateADM(null, startDate, endDate);

        // Week-by-week breakdown
        List<WeeklySummaryReport> weeklyBreakdown = generateWeeklyBreakdownForMonth(startDate, endDate);

        // Top attendance days
        Map<LocalDate, Long> dailyAttendanceCounts = monthRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
                .collect(Collectors.groupingBy(AttendanceRecord::getAttendanceDate, Collectors.counting()));

        LocalDate bestAttendanceDay = dailyAttendanceCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        LocalDate worstAttendanceDay = dailyAttendanceCounts.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return MonthlySummaryReport.builder()
                .year(year)
                .month(month)
                .startDate(startDate)
                .endDate(endDate)
                .totalRecords(totalRecords)
                .presentCount((int) presentCount)
                .absentCount((int) absentCount)
                .tardyCount((int) tardyCount)
                .monthlyAttendanceRate(monthlyAttendanceRate)
                .ada(ada.getAda())
                .adm(adm.getAdm())
                .weeklyBreakdown(weeklyBreakdown)
                .bestAttendanceDay(bestAttendanceDay)
                .worstAttendanceDay(worstAttendanceDay)
                .generatedDate(LocalDate.now())
                .build();
    }

    /**
     * Generate monthly comparison report
     */
    public MonthlyComparisonReport generateMonthlyComparison(int year, List<Integer> months) {
        log.info("Generating monthly comparison for year {} months {}", year, months);

        List<MonthlySummaryReport> monthlySummaries = new ArrayList<>();
        for (Integer month : months) {
            monthlySummaries.add(generateMonthlySummary(year, month));
        }

        // Find best and worst months
        MonthlySummaryReport bestMonth = monthlySummaries.stream()
                .max(Comparator.comparing(MonthlySummaryReport::getMonthlyAttendanceRate))
                .orElse(null);

        MonthlySummaryReport worstMonth = monthlySummaries.stream()
                .min(Comparator.comparing(MonthlySummaryReport::getMonthlyAttendanceRate))
                .orElse(null);

        double averageAttendanceRate = monthlySummaries.stream()
                .mapToDouble(MonthlySummaryReport::getMonthlyAttendanceRate)
                .average()
                .orElse(0.0);

        return MonthlyComparisonReport.builder()
                .year(year)
                .months(months)
                .monthlySummaries(monthlySummaries)
                .bestPerformingMonth(bestMonth != null ? bestMonth.getMonth() : 0)
                .worstPerformingMonth(worstMonth != null ? worstMonth.getMonth() : 0)
                .averageAttendanceRate(averageAttendanceRate)
                .generatedDate(LocalDate.now())
                .build();
    }

    // ========================================================================
    // YEARLY SUMMARY REPORTS
    // ========================================================================

    /**
     * Generate yearly attendance summary
     */
    public YearlySummaryReport generateYearlySummary(int year) {
        log.info("Generating yearly summary for {}", year);

        LocalDate startDate = LocalDate.of(year, 8, 1); // August 1 (school year start)
        LocalDate endDate = LocalDate.of(year + 1, 5, 31); // May 31 (school year end)

        List<AttendanceRecord> yearRecords = attendanceRepository.findAll().stream()
                .filter(r -> !r.getAttendanceDate().isBefore(startDate) && !r.getAttendanceDate().isAfter(endDate))
                .toList();

        // Calculate year statistics
        int totalRecords = yearRecords.size();
        long presentCount = yearRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absentCount = yearRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                           r.getStatus() == AttendanceStatus.UNEXCUSED_ABSENT ||
                           r.getStatus() == AttendanceStatus.EXCUSED_ABSENT).count();
        long tardyCount = yearRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.TARDY).count();

        double yearlyAttendanceRate = totalRecords > 0 ? (double) presentCount / totalRecords * 100 : 0.0;

        // ADA/ADM for the year
        var ada = reportingService.calculateADA(null, startDate, endDate);
        var adm = reportingService.calculateADM(null, startDate, endDate);

        // Semester breakdown
        LocalDate semester1End = LocalDate.of(year, 12, 31);
        SemesterSummary semester1 = generateSemesterSummary(startDate, semester1End, "Fall " + year);
        SemesterSummary semester2 = generateSemesterSummary(semester1End.plusDays(1), endDate, "Spring " + (year + 1));

        // Monthly breakdown
        List<MonthlySummaryReport> monthlyBreakdown = new ArrayList<>();
        for (int month = 8; month <= 12; month++) {
            monthlyBreakdown.add(generateMonthlySummary(year, month));
        }
        for (int month = 1; month <= 5; month++) {
            monthlyBreakdown.add(generateMonthlySummary(year + 1, month));
        }

        // Grade level performance
        var gradeStats = reportingService.getAttendanceByGrade(startDate, endDate);

        return YearlySummaryReport.builder()
                .schoolYear(year + "-" + (year + 1))
                .startDate(startDate)
                .endDate(endDate)
                .totalRecords(totalRecords)
                .presentCount((int) presentCount)
                .absentCount((int) absentCount)
                .tardyCount((int) tardyCount)
                .yearlyAttendanceRate(yearlyAttendanceRate)
                .ada(ada.getAda())
                .adm(adm.getAdm())
                .semester1Summary(semester1)
                .semester2Summary(semester2)
                .monthlyBreakdown(monthlyBreakdown)
                .gradeStats(gradeStats)
                .generatedDate(LocalDate.now())
                .build();
    }

    /**
     * Generate multi-year comparison report
     */
    public YearlyComparisonReport generateYearlyComparison(List<Integer> years) {
        log.info("Generating yearly comparison for years {}", years);

        List<YearlySummaryReport> yearlySummaries = new ArrayList<>();
        for (Integer year : years) {
            yearlySummaries.add(generateYearlySummary(year));
        }

        // Find best and worst years
        YearlySummaryReport bestYear = yearlySummaries.stream()
                .max(Comparator.comparing(YearlySummaryReport::getYearlyAttendanceRate))
                .orElse(null);

        YearlySummaryReport worstYear = yearlySummaries.stream()
                .min(Comparator.comparing(YearlySummaryReport::getYearlyAttendanceRate))
                .orElse(null);

        double averageAttendanceRate = yearlySummaries.stream()
                .mapToDouble(YearlySummaryReport::getYearlyAttendanceRate)
                .average()
                .orElse(0.0);

        String trend = determineYearlyTrend(yearlySummaries);

        return YearlyComparisonReport.builder()
                .years(years)
                .yearlySummaries(yearlySummaries)
                .bestPerformingYear(bestYear != null ? bestYear.getSchoolYear() : "N/A")
                .worstPerformingYear(worstYear != null ? worstYear.getSchoolYear() : "N/A")
                .averageAttendanceRate(averageAttendanceRate)
                .trend(trend)
                .generatedDate(LocalDate.now())
                .build();
    }

    // ========================================================================
    // EXECUTIVE DASHBOARD
    // ========================================================================

    /**
     * Generate executive dashboard with current metrics and alerts
     */
    public ExecutiveDashboard generateExecutiveDashboard() {
        log.info("Generating executive dashboard");

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        // Today's snapshot
        WeeklySummaryReport thisWeek = generateWeeklySummary(weekStart);
        MonthlySummaryReport thisMonth = generateMonthlySummary(currentYear, currentMonth);

        // School year to date
        int schoolYear = today.getMonthValue() >= 8 ? currentYear : currentYear - 1;
        LocalDate schoolYearStart = LocalDate.of(schoolYear, 8, 1);

        List<AttendanceRecord> ytdRecords = attendanceRepository.findAll().stream()
                .filter(r -> !r.getAttendanceDate().isBefore(schoolYearStart) && !r.getAttendanceDate().isAfter(today))
                .toList();

        long ytdPresent = ytdRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        double ytdAttendanceRate = ytdRecords.size() > 0 ? (double) ytdPresent / ytdRecords.size() * 100 : 0.0;

        // Alerts and action items
        List<String> alerts = generateAlerts(today);

        return ExecutiveDashboard.builder()
                .dashboardDate(today)
                .todayAttendanceRate(calculateTodayAttendanceRate(today))
                .weekAttendanceRate(thisWeek.getWeeklyAttendanceRate())
                .monthAttendanceRate(thisMonth.getMonthlyAttendanceRate())
                .yearToDateAttendanceRate(ytdAttendanceRate)
                .weeklyPerfectAttendanceCount(thisWeek.getPerfectAttendanceStudents().size())
                .weeklyAtRiskCount(thisWeek.getAtRiskStudentCount())
                .monthlyADA(thisMonth.getAda())
                .monthlyADM(thisMonth.getAdm())
                .alerts(alerts)
                .lastUpdated(LocalDate.now())
                .build();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private DailySummary generateDailySummary(LocalDate date, List<AttendanceRecord> weekRecords) {
        List<AttendanceRecord> dayRecords = weekRecords.stream()
                .filter(r -> r.getAttendanceDate().equals(date))
                .toList();

        int totalRecords = dayRecords.size();
        long presentCount = dayRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absentCount = dayRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                           r.getStatus() == AttendanceStatus.UNEXCUSED_ABSENT ||
                           r.getStatus() == AttendanceStatus.EXCUSED_ABSENT).count();
        long tardyCount = dayRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.TARDY).count();

        double dailyRate = totalRecords > 0 ? (double) presentCount / totalRecords * 100 : 0.0;

        return DailySummary.builder()
                .date(date)
                .dayOfWeek(date.getDayOfWeek().toString())
                .totalRecords(totalRecords)
                .presentCount((int) presentCount)
                .absentCount((int) absentCount)
                .tardyCount((int) tardyCount)
                .attendanceRate(dailyRate)
                .build();
    }

    private List<Long> findPerfectWeeklyAttendance(List<AttendanceRecord> weekRecords, LocalDate monday, LocalDate friday) {
        Map<Long, List<AttendanceRecord>> byStudent = weekRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getStudent().getId()));

        List<Long> perfectStudents = new ArrayList<>();

        for (Map.Entry<Long, List<AttendanceRecord>> entry : byStudent.entrySet()) {
            boolean isPerfect = entry.getValue().stream()
                    .allMatch(r -> r.getStatus() == AttendanceStatus.PRESENT);

            if (isPerfect && entry.getValue().size() >= 5) { // At least 5 present records
                perfectStudents.add(entry.getKey());
            }
        }

        return perfectStudents;
    }

    private List<Student> findWeeklyAtRiskStudents(List<AttendanceRecord> weekRecords) {
        Map<Long, Long> absencesByStudent = weekRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                           r.getStatus() == AttendanceStatus.UNEXCUSED_ABSENT)
                .collect(Collectors.groupingBy(
                        r -> r.getStudent().getId(),
                        Collectors.counting()
                ));

        List<Student> atRiskStudents = new ArrayList<>();

        for (Map.Entry<Long, Long> entry : absencesByStudent.entrySet()) {
            if (entry.getValue() >= 2) { // 2+ absences in one week = at risk
                studentRepository.findById(entry.getKey())
                        .ifPresent(atRiskStudents::add);
            }
        }

        return atRiskStudents;
    }

    private String determineTrend(List<WeeklySummaryReport> weeklySummaries) {
        if (weeklySummaries.size() < 2) return "INSUFFICIENT_DATA";

        double firstWeek = weeklySummaries.get(0).getWeeklyAttendanceRate();
        double lastWeek = weeklySummaries.get(weeklySummaries.size() - 1).getWeeklyAttendanceRate();

        double change = lastWeek - firstWeek;

        if (change > 2.0) return "IMPROVING";
        if (change < -2.0) return "DECLINING";
        return "STABLE";
    }

    private List<WeeklySummaryReport> generateWeeklyBreakdownForMonth(LocalDate startDate, LocalDate endDate) {
        List<WeeklySummaryReport> weeklyReports = new ArrayList<>();
        LocalDate currentMonday = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        while (!currentMonday.isAfter(endDate)) {
            weeklyReports.add(generateWeeklySummary(currentMonday));
            currentMonday = currentMonday.plusWeeks(1);
        }

        return weeklyReports;
    }

    private SemesterSummary generateSemesterSummary(LocalDate startDate, LocalDate endDate, String semesterName) {
        List<AttendanceRecord> semesterRecords = attendanceRepository.findAll().stream()
                .filter(r -> !r.getAttendanceDate().isBefore(startDate) && !r.getAttendanceDate().isAfter(endDate))
                .toList();

        int totalRecords = semesterRecords.size();
        long presentCount = semesterRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absentCount = semesterRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.ABSENT ||
                           r.getStatus() == AttendanceStatus.UNEXCUSED_ABSENT ||
                           r.getStatus() == AttendanceStatus.EXCUSED_ABSENT).count();

        double semesterRate = totalRecords > 0 ? (double) presentCount / totalRecords * 100 : 0.0;

        return SemesterSummary.builder()
                .semesterName(semesterName)
                .startDate(startDate)
                .endDate(endDate)
                .totalRecords(totalRecords)
                .presentCount((int) presentCount)
                .absentCount((int) absentCount)
                .attendanceRate(semesterRate)
                .build();
    }

    private String determineYearlyTrend(List<YearlySummaryReport> yearlySummaries) {
        if (yearlySummaries.size() < 2) return "INSUFFICIENT_DATA";

        double firstYear = yearlySummaries.get(0).getYearlyAttendanceRate();
        double lastYear = yearlySummaries.get(yearlySummaries.size() - 1).getYearlyAttendanceRate();

        double change = lastYear - firstYear;

        if (change > 1.0) return "IMPROVING";
        if (change < -1.0) return "DECLINING";
        return "STABLE";
    }

    private double calculateTodayAttendanceRate(LocalDate today) {
        List<AttendanceRecord> todayRecords = attendanceRepository.findAll().stream()
                .filter(r -> r.getAttendanceDate().equals(today))
                .toList();

        if (todayRecords.isEmpty()) return 0.0;

        long presentCount = todayRecords.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();

        return (double) presentCount / todayRecords.size() * 100;
    }

    private List<String> generateAlerts(LocalDate today) {
        List<String> alerts = new ArrayList<>();

        // Check today's attendance rate
        double todayRate = calculateTodayAttendanceRate(today);
        if (todayRate < 90.0) {
            alerts.add("ALERT: Today's attendance rate below 90% (" + String.format("%.1f%%", todayRate) + ")");
        }

        // Check for chronic absenteeism spike
        LocalDate weekAgo = today.minusWeeks(1);
        var chronicStudents = attendanceRepository.findStudentsWithChronicAbsences(weekAgo, today, 3L);
        if (!chronicStudents.isEmpty()) {
            alerts.add("WARNING: " + chronicStudents.size() + " students with 3+ absences this week");
        }

        if (alerts.isEmpty()) {
            alerts.add("No critical alerts at this time");
        }

        return alerts;
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class DailySummary {
        private LocalDate date;
        private String dayOfWeek;
        private Integer totalRecords;
        private Integer presentCount;
        private Integer absentCount;
        private Integer tardyCount;
        private Double attendanceRate;
    }

    @Data
    @Builder
    public static class WeeklySummaryReport {
        private LocalDate weekStartDate;
        private LocalDate weekEndDate;
        private List<DailySummary> dailySummaries;
        private Integer totalRecords;
        private Integer presentCount;
        private Integer absentCount;
        private Integer tardyCount;
        private Double weeklyAttendanceRate;
        private List<Long> perfectAttendanceStudents;
        private Integer atRiskStudentCount;
        private LocalDate generatedDate;
    }

    @Data
    @Builder
    public static class WeeklyTrendReport {
        private LocalDate startDate;
        private Integer numberOfWeeks;
        private List<WeeklySummaryReport> weeklySummaries;
        private Double averageAttendanceRate;
        private String trend;
        private LocalDate generatedDate;
    }

    @Data
    @Builder
    public static class MonthlySummaryReport {
        private Integer year;
        private Integer month;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalRecords;
        private Integer presentCount;
        private Integer absentCount;
        private Integer tardyCount;
        private Double monthlyAttendanceRate;
        private Double ada;
        private Double adm;
        private List<WeeklySummaryReport> weeklyBreakdown;
        private LocalDate bestAttendanceDay;
        private LocalDate worstAttendanceDay;
        private LocalDate generatedDate;
    }

    @Data
    @Builder
    public static class MonthlyComparisonReport {
        private Integer year;
        private List<Integer> months;
        private List<MonthlySummaryReport> monthlySummaries;
        private Integer bestPerformingMonth;
        private Integer worstPerformingMonth;
        private Double averageAttendanceRate;
        private LocalDate generatedDate;
    }

    @Data
    @Builder
    public static class SemesterSummary {
        private String semesterName;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalRecords;
        private Integer presentCount;
        private Integer absentCount;
        private Double attendanceRate;
    }

    @Data
    @Builder
    public static class YearlySummaryReport {
        private String schoolYear;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalRecords;
        private Integer presentCount;
        private Integer absentCount;
        private Integer tardyCount;
        private Double yearlyAttendanceRate;
        private Double ada;
        private Double adm;
        private SemesterSummary semester1Summary;
        private SemesterSummary semester2Summary;
        private List<MonthlySummaryReport> monthlyBreakdown;
        private List<AttendanceReportingService.GradeAttendanceStats> gradeStats;
        private LocalDate generatedDate;
    }

    @Data
    @Builder
    public static class YearlyComparisonReport {
        private List<Integer> years;
        private List<YearlySummaryReport> yearlySummaries;
        private String bestPerformingYear;
        private String worstPerformingYear;
        private Double averageAttendanceRate;
        private String trend;
        private LocalDate generatedDate;
    }

    @Data
    @Builder
    public static class ExecutiveDashboard {
        private LocalDate dashboardDate;
        private Double todayAttendanceRate;
        private Double weekAttendanceRate;
        private Double monthAttendanceRate;
        private Double yearToDateAttendanceRate;
        private Integer weeklyPerfectAttendanceCount;
        private Integer weeklyAtRiskCount;
        private Double monthlyADA;
        private Double monthlyADM;
        private List<String> alerts;
        private LocalDate lastUpdated;
    }
}
