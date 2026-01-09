package com.heronix.service;

import com.heronix.dto.ChartConfig;
import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.AttendanceRecord.AttendanceStatus;
import com.heronix.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Chart Data Service
 *
 * Generates chart data and configurations from attendance records.
 *
 * Provides Pre-Built Charts:
 * - Daily attendance trend (line chart)
 * - Status distribution (pie chart)
 * - Grade-level comparison (bar chart)
 * - Weekly patterns (area chart)
 * - Monthly summary (stacked bar chart)
 *
 * Features:
 * - Automatic data aggregation
 * - Smart color selection
 * - Responsive configuration
 * - Multiple time ranges
 * - Custom filtering
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 66 - Report Data Visualization & Charts
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChartDataService {

    private final AttendanceRepository attendanceRepository;

    /**
     * Generate daily attendance trend chart
     */
    public ChartConfig generateDailyTrendChart(LocalDate startDate, LocalDate endDate) {
        log.info("Generating daily trend chart: {} to {}", startDate, endDate);

        // Fetch attendance records
        List<AttendanceRecord> allRecords = attendanceRepository.findAll();
        List<AttendanceRecord> records = allRecords.stream()
                .filter(r -> !r.getAttendanceDate().isBefore(startDate) && !r.getAttendanceDate().isAfter(endDate))
                .collect(Collectors.toList());

        // Group by date
        Map<LocalDate, Map<AttendanceStatus, Long>> dailyData = records.stream()
                .collect(Collectors.groupingBy(
                        AttendanceRecord::getAttendanceDate,
                        Collectors.groupingBy(
                                AttendanceRecord::getStatus,
                                Collectors.counting()
                        )
                ));

        // Generate labels (dates)
        List<String> labels = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            labels.add(current.format(DateTimeFormatter.ofPattern("MM/dd")));
            current = current.plusDays(1);
        }

        // Generate data series
        List<ChartConfig.ChartDataSeries> series = new ArrayList<>();

        // Present series
        series.add(ChartConfig.ChartDataSeries.builder()
                .label("Present")
                .data(generateDailySeriesData(dailyData, startDate, endDate, AttendanceStatus.PRESENT))
                .color("#28a745")
                .borderWidth(2)
                .pointRadius(3)
                .tension(0.4)
                .build());

        // Absent series
        series.add(ChartConfig.ChartDataSeries.builder()
                .label("Absent")
                .data(generateDailySeriesData(dailyData, startDate, endDate,
                        AttendanceStatus.ABSENT, AttendanceStatus.EXCUSED_ABSENT, AttendanceStatus.UNEXCUSED_ABSENT))
                .color("#dc3545")
                .borderWidth(2)
                .pointRadius(3)
                .tension(0.4)
                .build());

        // Tardy series
        series.add(ChartConfig.ChartDataSeries.builder()
                .label("Tardy")
                .data(generateDailySeriesData(dailyData, startDate, endDate, AttendanceStatus.TARDY))
                .color("#ffc107")
                .borderWidth(2)
                .pointRadius(3)
                .tension(0.4)
                .build());

        return ChartConfig.builder()
                .chartId("daily-trend-" + UUID.randomUUID())
                .chartType(ChartConfig.ChartType.LINE)
                .title("Daily Attendance Trend")
                .subtitle(String.format("%s to %s", startDate, endDate))
                .theme(ChartConfig.ChartTheme.DEFAULT)
                .labels(labels)
                .dataSeries(series)
                .xAxisLabel("Date")
                .yAxisLabel("Number of Students")
                .width(800)
                .height(400)
                .showLegend(true)
                .legendPosition("top")
                .showGrid(true)
                .showDataLabels(false)
                .enableAnimations(true)
                .enableTooltips(true)
                .responsive(true)
                .smoothCurves(true)
                .generatedAt(java.time.LocalDateTime.now())
                .sourceReport("DAILY_ATTENDANCE")
                .build();
    }

    /**
     * Generate attendance status distribution pie chart
     */
    public ChartConfig generateStatusDistributionChart(LocalDate startDate, LocalDate endDate) {
        log.info("Generating status distribution chart: {} to {}", startDate, endDate);

        // Fetch attendance records
        List<AttendanceRecord> allRecords = attendanceRepository.findAll();
        List<AttendanceRecord> records = allRecords.stream()
                .filter(r -> !r.getAttendanceDate().isBefore(startDate) && !r.getAttendanceDate().isAfter(endDate))
                .collect(Collectors.toList());

        // Count by status
        Map<AttendanceStatus, Long> statusCounts = records.stream()
                .collect(Collectors.groupingBy(
                        AttendanceRecord::getStatus,
                        Collectors.counting()
                ));

        // Generate labels and data
        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();
        List<String> colors = List.of("#28a745", "#dc3545", "#ffc107", "#17a2b8", "#6c757d");

        int colorIndex = 0;
        for (Map.Entry<AttendanceStatus, Long> entry : statusCounts.entrySet()) {
            labels.add(entry.getKey().name().replace("_", " "));
            data.add(entry.getValue().doubleValue());
            colorIndex++;
        }

        ChartConfig.ChartDataSeries series = ChartConfig.ChartDataSeries.builder()
                .label("Attendance Status")
                .data(data)
                .build();

        return ChartConfig.builder()
                .chartId("status-distribution-" + UUID.randomUUID())
                .chartType(ChartConfig.ChartType.PIE)
                .title("Attendance Status Distribution")
                .subtitle(String.format("%s to %s", startDate, endDate))
                .theme(ChartConfig.ChartTheme.EDUCATIONAL)
                .labels(labels)
                .dataSeries(List.of(series))
                .width(600)
                .height(400)
                .showLegend(true)
                .legendPosition("right")
                .showDataLabels(true)
                .enableAnimations(true)
                .enableTooltips(true)
                .responsive(true)
                .showPercentages(true)
                .generatedAt(java.time.LocalDateTime.now())
                .sourceReport("STATUS_DISTRIBUTION")
                .build();
    }

    /**
     * Generate grade-level attendance comparison chart
     */
    public ChartConfig generateGradeLevelComparisonChart(LocalDate startDate, LocalDate endDate) {
        log.info("Generating grade-level comparison chart: {} to {}", startDate, endDate);

        // Fetch attendance records
        List<AttendanceRecord> allRecords = attendanceRepository.findAll();
        List<AttendanceRecord> records = allRecords.stream()
                .filter(r -> !r.getAttendanceDate().isBefore(startDate) && !r.getAttendanceDate().isAfter(endDate))
                .collect(Collectors.toList());

        // Group by grade level
        Map<String, Map<AttendanceStatus, Long>> gradeData = records.stream()
                .filter(r -> r.getStudent() != null && r.getStudent().getGradeLevel() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getStudent().getGradeLevel(),
                        Collectors.groupingBy(
                                AttendanceRecord::getStatus,
                                Collectors.counting()
                        )
                ));

        // Generate labels (grade levels)
        List<String> labels = gradeData.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        // Generate data series
        List<ChartConfig.ChartDataSeries> series = new ArrayList<>();

        series.add(ChartConfig.ChartDataSeries.builder()
                .label("Present")
                .data(generateGradeSeriesData(gradeData, labels, AttendanceStatus.PRESENT))
                .color("#28a745")
                .borderWidth(1)
                .build());

        series.add(ChartConfig.ChartDataSeries.builder()
                .label("Absent")
                .data(generateGradeSeriesData(gradeData, labels,
                        AttendanceStatus.ABSENT, AttendanceStatus.EXCUSED_ABSENT, AttendanceStatus.UNEXCUSED_ABSENT))
                .color("#dc3545")
                .borderWidth(1)
                .build());

        series.add(ChartConfig.ChartDataSeries.builder()
                .label("Tardy")
                .data(generateGradeSeriesData(gradeData, labels, AttendanceStatus.TARDY))
                .color("#ffc107")
                .borderWidth(1)
                .build());

        return ChartConfig.builder()
                .chartId("grade-comparison-" + UUID.randomUUID())
                .chartType(ChartConfig.ChartType.BAR)
                .title("Attendance by Grade Level")
                .subtitle(String.format("%s to %s", startDate, endDate))
                .theme(ChartConfig.ChartTheme.DEFAULT)
                .labels(labels)
                .dataSeries(series)
                .xAxisLabel("Grade Level")
                .yAxisLabel("Number of Records")
                .width(800)
                .height(500)
                .showLegend(true)
                .legendPosition("top")
                .showGrid(true)
                .showDataLabels(false)
                .enableAnimations(true)
                .enableTooltips(true)
                .responsive(true)
                .stacked(false)
                .generatedAt(java.time.LocalDateTime.now())
                .sourceReport("GRADE_COMPARISON")
                .build();
    }

    /**
     * Generate weekly attendance pattern chart
     */
    public ChartConfig generateWeeklyPatternChart(LocalDate startDate, LocalDate endDate) {
        log.info("Generating weekly pattern chart: {} to {}", startDate, endDate);

        // Fetch attendance records
        List<AttendanceRecord> allRecords = attendanceRepository.findAll();
        List<AttendanceRecord> records = allRecords.stream()
                .filter(r -> !r.getAttendanceDate().isBefore(startDate) && !r.getAttendanceDate().isAfter(endDate))
                .collect(Collectors.toList());

        // Group by day of week
        Map<java.time.DayOfWeek, Map<AttendanceStatus, Long>> weeklyData = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getAttendanceDate().getDayOfWeek(),
                        Collectors.groupingBy(
                                AttendanceRecord::getStatus,
                                Collectors.counting()
                        )
                ));

        // Generate labels (days of week)
        List<String> labels = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");

        // Generate data series
        List<ChartConfig.ChartDataSeries> series = new ArrayList<>();

        series.add(ChartConfig.ChartDataSeries.builder()
                .label("Present")
                .data(generateWeeklySeriesData(weeklyData, AttendanceStatus.PRESENT))
                .color("#28a745")
                .borderWidth(2)
                .fill(true)
                .tension(0.4)
                .build());

        series.add(ChartConfig.ChartDataSeries.builder()
                .label("Absent")
                .data(generateWeeklySeriesData(weeklyData,
                        AttendanceStatus.ABSENT, AttendanceStatus.EXCUSED_ABSENT, AttendanceStatus.UNEXCUSED_ABSENT))
                .color("#dc3545")
                .borderWidth(2)
                .fill(true)
                .tension(0.4)
                .build());

        return ChartConfig.builder()
                .chartId("weekly-pattern-" + UUID.randomUUID())
                .chartType(ChartConfig.ChartType.AREA)
                .title("Weekly Attendance Pattern")
                .subtitle(String.format("%s to %s", startDate, endDate))
                .theme(ChartConfig.ChartTheme.DEFAULT)
                .labels(labels)
                .dataSeries(series)
                .xAxisLabel("Day of Week")
                .yAxisLabel("Number of Students")
                .width(800)
                .height(400)
                .showLegend(true)
                .legendPosition("top")
                .showGrid(true)
                .showDataLabels(false)
                .enableAnimations(true)
                .enableTooltips(true)
                .responsive(true)
                .fillArea(true)
                .generatedAt(java.time.LocalDateTime.now())
                .sourceReport("WEEKLY_PATTERN")
                .build();
    }

    /**
     * Generate custom chart from configuration
     */
    public ChartConfig generateCustomChart(ChartConfig config) {
        log.info("Generating custom chart: {}", config.getChartType());

        // Validate configuration
        config.validate();

        // Set defaults
        if (config.getGeneratedAt() == null) {
            config.setGeneratedAt(java.time.LocalDateTime.now());
        }
        if (config.getChartId() == null) {
            config.setChartId("custom-" + UUID.randomUUID());
        }

        // Apply theme colors if needed
        if (config.getCustomColors() == null || config.getCustomColors().isEmpty()) {
            List<String> palette = config.getColorPalette();
            for (int i = 0; i < config.getDataSeries().size(); i++) {
                ChartConfig.ChartDataSeries series = config.getDataSeries().get(i);
                if (series.getColor() == null) {
                    series.setColor(palette.get(i % palette.size()));
                }
            }
        }

        return config;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Generate daily series data for specific statuses
     */
    private List<Double> generateDailySeriesData(
            Map<LocalDate, Map<AttendanceStatus, Long>> dailyData,
            LocalDate startDate,
            LocalDate endDate,
            AttendanceStatus... statuses) {

        List<Double> data = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            Map<AttendanceStatus, Long> dayData = dailyData.get(current);
            long count = 0;

            if (dayData != null) {
                for (AttendanceStatus status : statuses) {
                    count += dayData.getOrDefault(status, 0L);
                }
            }

            data.add((double) count);
            current = current.plusDays(1);
        }

        return data;
    }

    /**
     * Generate grade-level series data
     */
    private List<Double> generateGradeSeriesData(
            Map<String, Map<AttendanceStatus, Long>> gradeData,
            List<String> grades,
            AttendanceStatus... statuses) {

        List<Double> data = new ArrayList<>();

        for (String grade : grades) {
            Map<AttendanceStatus, Long> gradeStatusData = gradeData.get(grade);
            long count = 0;

            if (gradeStatusData != null) {
                for (AttendanceStatus status : statuses) {
                    count += gradeStatusData.getOrDefault(status, 0L);
                }
            }

            data.add((double) count);
        }

        return data;
    }

    /**
     * Generate weekly series data
     */
    private List<Double> generateWeeklySeriesData(
            Map<java.time.DayOfWeek, Map<AttendanceStatus, Long>> weeklyData,
            AttendanceStatus... statuses) {

        List<Double> data = new ArrayList<>();
        java.time.DayOfWeek[] weekdays = {
                java.time.DayOfWeek.MONDAY,
                java.time.DayOfWeek.TUESDAY,
                java.time.DayOfWeek.WEDNESDAY,
                java.time.DayOfWeek.THURSDAY,
                java.time.DayOfWeek.FRIDAY
        };

        for (java.time.DayOfWeek day : weekdays) {
            Map<AttendanceStatus, Long> dayData = weeklyData.get(day);
            long count = 0;

            if (dayData != null) {
                for (AttendanceStatus status : statuses) {
                    count += dayData.getOrDefault(status, 0L);
                }
            }

            data.add((double) count);
        }

        return data;
    }

    /**
     * Calculate attendance rate for data series
     */
    public double calculateAttendanceRate(List<Double> presentData, List<Double> totalData) {
        double totalPresent = presentData.stream().mapToDouble(Double::doubleValue).sum();
        double totalRecords = totalData.stream().mapToDouble(Double::doubleValue).sum();

        if (totalRecords == 0) {
            return 0.0;
        }

        return (totalPresent / totalRecords) * 100.0;
    }
}
