package com.heronix.service;

import com.heronix.dto.ReportAnalytics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Report Usage Analytics Service
 *
 * Provides analytics and insights about report usage and performance.
 *
 * Features:
 * - Usage statistics
 * - Performance metrics
 * - Trend analysis
 * - User engagement tracking
 * - Recommendations
 * - Anomaly detection
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 73 - Report Analytics & Insights
 */
@Service
@Slf4j
public class ReportUsageAnalyticsService {

    // Analytics event storage (in production, use database/time-series DB)
    private final Map<Long, List<AnalyticsEvent>> reportEvents = new ConcurrentHashMap<>();
    private final List<AnalyticsEvent> globalEvents = Collections.synchronizedList(new ArrayList<>());

    // ============================================================
    // Event Tracking
    // ============================================================

    /**
     * Track report view
     */
    public void trackView(Long reportId, String username, String reportType) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .reportId(reportId)
                .eventType("VIEW")
                .username(username)
                .reportType(reportType)
                .timestamp(LocalDateTime.now())
                .build();

        trackEvent(event);
    }

    /**
     * Track report download
     */
    public void trackDownload(Long reportId, String username, String reportType, String format, Long fileSize) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .reportId(reportId)
                .eventType("DOWNLOAD")
                .username(username)
                .reportType(reportType)
                .format(format)
                .fileSize(fileSize)
                .timestamp(LocalDateTime.now())
                .build();

        trackEvent(event);
    }

    /**
     * Track report export
     */
    public void trackExport(Long reportId, String username, String reportType, String format) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .reportId(reportId)
                .eventType("EXPORT")
                .username(username)
                .reportType(reportType)
                .format(format)
                .timestamp(LocalDateTime.now())
                .build();

        trackEvent(event);
    }

    /**
     * Track report share
     */
    public void trackShare(Long reportId, String username, String reportType) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .reportId(reportId)
                .eventType("SHARE")
                .username(username)
                .reportType(reportType)
                .timestamp(LocalDateTime.now())
                .build();

        trackEvent(event);
    }

    /**
     * Track report generation
     */
    public void trackGeneration(Long reportId, String username, String reportType,
                               Long generationTime, boolean success, String errorMessage) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .reportId(reportId)
                .eventType(success ? "GENERATION_SUCCESS" : "GENERATION_ERROR")
                .username(username)
                .reportType(reportType)
                .generationTime(generationTime)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();

        trackEvent(event);
    }

    /**
     * Track event
     */
    private void trackEvent(AnalyticsEvent event) {
        globalEvents.add(event);

        if (event.getReportId() != null) {
            reportEvents.computeIfAbsent(event.getReportId(), k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(event);
        }

        log.debug("Tracked {} event for report {}", event.getEventType(), event.getReportId());
    }

    // ============================================================
    // Analytics Generation
    // ============================================================

    /**
     * Generate analytics for report
     */
    public ReportAnalytics generateReportAnalytics(Long reportId, ReportAnalytics.TimePeriod period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = getStartDate(endDate, period);

        List<AnalyticsEvent> events = getEventsForReport(reportId, startDate, endDate);

        ReportAnalytics analytics = ReportAnalytics.builder()
                .reportId(reportId)
                .timePeriod(period)
                .startDate(startDate)
                .endDate(endDate)
                .generatedAt(LocalDateTime.now())
                .build();

        // Calculate all metrics
        calculateUsageStatistics(analytics, events);
        calculatePerformanceMetrics(analytics, events);
        calculateQualityMetrics(analytics, events);
        calculateUserEngagement(analytics, events);
        calculateTimeBasedAnalysis(analytics, events);
        calculateFormatDistribution(analytics, events);
        analyzeErrors(analytics, events);
        generateInsights(analytics);
        generateRecommendations(analytics);

        log.info("Generated analytics for report {} for period {}", reportId, period);
        return analytics;
    }

    /**
     * Generate global analytics
     */
    public ReportAnalytics generateGlobalAnalytics(ReportAnalytics.TimePeriod period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = getStartDate(endDate, period);

        List<AnalyticsEvent> events = getGlobalEvents(startDate, endDate);

        ReportAnalytics analytics = ReportAnalytics.builder()
                .timePeriod(period)
                .startDate(startDate)
                .endDate(endDate)
                .generatedAt(LocalDateTime.now())
                .build();

        calculateUsageStatistics(analytics, events);
        calculatePerformanceMetrics(analytics, events);
        calculateQualityMetrics(analytics, events);
        calculateUserEngagement(analytics, events);
        calculateTimeBasedAnalysis(analytics, events);
        calculateFormatDistribution(analytics, events);
        calculateTopReports(analytics, events);
        analyzeErrors(analytics, events);
        generateInsights(analytics);
        generateRecommendations(analytics);

        log.info("Generated global analytics for period {}", period);
        return analytics;
    }

    // ============================================================
    // Calculation Methods
    // ============================================================

    private void calculateUsageStatistics(ReportAnalytics analytics, List<AnalyticsEvent> events) {
        analytics.setTotalViews(countEventType(events, "VIEW"));
        analytics.setTotalDownloads(countEventType(events, "DOWNLOAD"));
        analytics.setTotalExports(countEventType(events, "EXPORT"));
        analytics.setTotalShares(countEventType(events, "SHARE"));

        long successCount = countEventType(events, "GENERATION_SUCCESS");
        long errorCount = countEventType(events, "GENERATION_ERROR");
        analytics.setTotalGenerations(successCount + errorCount);

        analytics.setUniqueViewers((long) events.stream()
                .filter(e -> "VIEW".equals(e.getEventType()))
                .map(AnalyticsEvent::getUsername)
                .distinct()
                .count());
    }

    private void calculatePerformanceMetrics(ReportAnalytics analytics, List<AnalyticsEvent> events) {
        List<Long> generationTimes = events.stream()
                .filter(e -> e.getGenerationTime() != null)
                .map(AnalyticsEvent::getGenerationTime)
                .sorted()
                .collect(Collectors.toList());

        if (!generationTimes.isEmpty()) {
            analytics.setAvgGenerationTime(generationTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0));
            analytics.setMinGenerationTime(generationTimes.get(0));
            analytics.setMaxGenerationTime(generationTimes.get(generationTimes.size() - 1));
            analytics.setMedianGenerationTime(generationTimes.get(generationTimes.size() / 2));
        }

        List<Long> fileSizes = events.stream()
                .filter(e -> e.getFileSize() != null)
                .map(AnalyticsEvent::getFileSize)
                .collect(Collectors.toList());

        if (!fileSizes.isEmpty()) {
            analytics.setAvgFileSize(fileSizes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0));
            analytics.setMinFileSize(fileSizes.stream().min(Long::compareTo).orElse(0L));
            analytics.setMaxFileSize(fileSizes.stream().max(Long::compareTo).orElse(0L));
            analytics.setTotalStorageUsed(fileSizes.stream().mapToLong(Long::longValue).sum());
        }
    }

    private void calculateQualityMetrics(ReportAnalytics analytics, List<AnalyticsEvent> events) {
        long successCount = countEventType(events, "GENERATION_SUCCESS");
        long errorCount = countEventType(events, "GENERATION_ERROR");

        analytics.setSuccessCount(successCount);
        analytics.setErrorCount(errorCount);
        analytics.calculateSuccessRate();
    }

    private void calculateUserEngagement(ReportAnalytics analytics, List<AnalyticsEvent> events) {
        Set<String> allUsers = events.stream()
                .map(AnalyticsEvent::getUsername)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        analytics.setTotalUsers((long) allUsers.size());
        analytics.setActiveUsers((long) allUsers.size());
        analytics.calculateAverages();

        // Most active users
        Map<String, Long> userActivityCount = events.stream()
                .filter(e -> e.getUsername() != null)
                .collect(Collectors.groupingBy(AnalyticsEvent::getUsername, Collectors.counting()));

        List<ReportAnalytics.UserActivity> mostActive = userActivityCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    String username = e.getKey();
                    long views = countUserEvents(events, username, "VIEW");
                    long downloads = countUserEvents(events, username, "DOWNLOAD");
                    long shares = countUserEvents(events, username, "SHARE");
                    LocalDateTime lastActive = getLastActivity(events, username);

                    return ReportAnalytics.UserActivity.builder()
                            .username(username)
                            .displayName(username)
                            .viewCount(views)
                            .downloadCount(downloads)
                            .shareCount(shares)
                            .lastActive(lastActive)
                            .build();
                })
                .collect(Collectors.toList());

        analytics.setMostActiveUsers(mostActive);
    }

    private void calculateTimeBasedAnalysis(ReportAnalytics analytics, List<AnalyticsEvent> events) {
        // Views by hour
        Map<Integer, Long> viewsByHour = events.stream()
                .filter(e -> "VIEW".equals(e.getEventType()))
                .collect(Collectors.groupingBy(
                        e -> e.getTimestamp().getHour(),
                        Collectors.counting()));
        analytics.setViewsByHour(viewsByHour);

        analytics.setPeakUsageHour(viewsByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0));

        // Views by day of week
        Map<String, Long> viewsByDay = events.stream()
                .filter(e -> "VIEW".equals(e.getEventType()))
                .collect(Collectors.groupingBy(
                        e -> e.getTimestamp().getDayOfWeek().toString(),
                        Collectors.counting()));
        analytics.setViewsByDayOfWeek(viewsByDay);

        analytics.setPeakUsageDay(viewsByDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("MONDAY"));

        analyzeTrend(analytics, events);
    }

    private void calculateFormatDistribution(ReportAnalytics analytics, List<AnalyticsEvent> events) {
        Map<String, Long> exportsByFormat = events.stream()
                .filter(e -> ("EXPORT".equals(e.getEventType()) || "DOWNLOAD".equals(e.getEventType()))
                        && e.getFormat() != null)
                .collect(Collectors.groupingBy(AnalyticsEvent::getFormat, Collectors.counting()));

        analytics.setExportsByFormat(exportsByFormat);
        analytics.setMostPopularFormat(exportsByFormat.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("PDF"));
    }

    private void calculateTopReports(ReportAnalytics analytics, List<AnalyticsEvent> events) {
        Map<Long, Long> viewsByReport = events.stream()
                .filter(e -> "VIEW".equals(e.getEventType()) && e.getReportId() != null)
                .collect(Collectors.groupingBy(AnalyticsEvent::getReportId, Collectors.counting()));

        List<ReportAnalytics.ReportRanking> mostViewed = viewsByReport.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> ReportAnalytics.ReportRanking.builder()
                        .reportId(e.getKey())
                        .reportName("Report " + e.getKey())
                        .count(e.getValue())
                        .build())
                .collect(Collectors.toList());

        analytics.setMostViewedReports(mostViewed);
    }

    private void analyzeErrors(ReportAnalytics analytics, List<AnalyticsEvent> events) {
        List<AnalyticsEvent> errorEvents = events.stream()
                .filter(e -> "GENERATION_ERROR".equals(e.getEventType()))
                .collect(Collectors.toList());

        if (errorEvents.isEmpty()) {
            return;
        }

        Map<String, Long> errorsByType = errorEvents.stream()
                .filter(e -> e.getErrorMessage() != null)
                .collect(Collectors.groupingBy(AnalyticsEvent::getErrorMessage, Collectors.counting()));

        analytics.setErrorsByType(errorsByType);
        analytics.setMostCommonError(errorsByType.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown"));
    }

    private void analyzeTrend(ReportAnalytics analytics, List<AnalyticsEvent> events) {
        if (events.isEmpty()) {
            analytics.setTrendDirection("STABLE");
            analytics.setGrowthRate(0.0);
            return;
        }

        events.sort(Comparator.comparing(AnalyticsEvent::getTimestamp));
        int midPoint = events.size() / 2;
        long firstHalfCount = events.subList(0, midPoint).stream()
                .filter(e -> "VIEW".equals(e.getEventType())).count();
        long secondHalfCount = events.subList(midPoint, events.size()).stream()
                .filter(e -> "VIEW".equals(e.getEventType())).count();

        if (firstHalfCount == 0) {
            analytics.setTrendDirection("INCREASING");
            analytics.setGrowthRate(100.0);
        } else {
            double growth = ((secondHalfCount - firstHalfCount) * 100.0) / firstHalfCount;
            analytics.setGrowthRate(growth);

            if (growth > 10) {
                analytics.setTrendDirection("INCREASING");
            } else if (growth < -10) {
                analytics.setTrendDirection("DECREASING");
            } else {
                analytics.setTrendDirection("STABLE");
            }
        }
    }

    private void generateInsights(ReportAnalytics analytics) {
        if (analytics.isUsageIncreasing()) {
            analytics.addInsight(String.format("Usage is increasing with %.1f%% growth", analytics.getGrowthRate()));
        }

        if (analytics.getPeakUsageHour() != null) {
            analytics.addInsight(String.format("Peak usage hour is %d:00", analytics.getPeakUsageHour()));
        }

        if (analytics.getSuccessRate() != null && analytics.getSuccessRate() > 95) {
            analytics.addInsight(String.format("Excellent success rate of %.1f%%", analytics.getSuccessRate()));
        }
    }

    private void generateRecommendations(ReportAnalytics analytics) {
        if (analytics.hasErrors() && analytics.getErrorRate() != null && analytics.getErrorRate() > 5) {
            analytics.addRecommendation("Error rate is high - investigate recent failures");
        }

        if (analytics.getAvgGenerationTime() != null && analytics.getAvgGenerationTime() > 10000) {
            analytics.addRecommendation("Average generation time is slow - consider optimization");
        }

        if (analytics.isUsageDecreasing()) {
            analytics.addRecommendation("Usage is declining - review report relevance and user feedback");
        }
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private LocalDateTime getStartDate(LocalDateTime endDate, ReportAnalytics.TimePeriod period) {
        return switch (period) {
            case HOURLY -> endDate.minusHours(1);
            case DAILY -> endDate.minusDays(1);
            case WEEKLY -> endDate.minusWeeks(1);
            case MONTHLY -> endDate.minusMonths(1);
            case QUARTERLY -> endDate.minusMonths(3);
            case YEARLY -> endDate.minusYears(1);
            default -> endDate.minusMonths(1);
        };
    }

    private List<AnalyticsEvent> getEventsForReport(Long reportId, LocalDateTime start, LocalDateTime end) {
        return reportEvents.getOrDefault(reportId, Collections.emptyList()).stream()
                .filter(e -> !e.getTimestamp().isBefore(start) && !e.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    private List<AnalyticsEvent> getGlobalEvents(LocalDateTime start, LocalDateTime end) {
        return globalEvents.stream()
                .filter(e -> !e.getTimestamp().isBefore(start) && !e.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    private long countEventType(List<AnalyticsEvent> events, String eventType) {
        return events.stream().filter(e -> eventType.equals(e.getEventType())).count();
    }

    private long countUserEvents(List<AnalyticsEvent> events, String username, String eventType) {
        return events.stream()
                .filter(e -> username.equals(e.getUsername()) && eventType.equals(e.getEventType()))
                .count();
    }

    private LocalDateTime getLastActivity(List<AnalyticsEvent> events, String username) {
        return events.stream()
                .filter(e -> username.equals(e.getUsername()))
                .map(AnalyticsEvent::getTimestamp)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", globalEvents.size());
        stats.put("totalReports", reportEvents.size());
        return stats;
    }

    // ============================================================
    // Analytics Event Class
    // ============================================================

    @lombok.Data
    @lombok.Builder
    private static class AnalyticsEvent {
        private Long reportId;
        private String reportType;
        private String eventType;
        private String username;
        private String format;
        private Long generationTime;
        private Long fileSize;
        private String errorMessage;
        private LocalDateTime timestamp;
    }
}
