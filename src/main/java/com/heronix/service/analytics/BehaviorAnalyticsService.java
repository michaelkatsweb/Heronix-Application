package com.heronix.service.analytics;

import com.heronix.dto.analytics.*;
import com.heronix.repository.BehaviorIncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Behavior Analytics Service
 *
 * Provides comprehensive analytics for behavior incidents including:
 * - Incident trends and counts
 * - Category and type breakdown
 * - Location analysis (incident hotspots)
 * - Repeat offender identification
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Comprehensive Analytics Module
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BehaviorAnalyticsService {

    private final BehaviorIncidentRepository behaviorIncidentRepository;

    // ========================================================================
    // BEHAVIOR OVERVIEW
    // ========================================================================

    /**
     * Get comprehensive behavior analytics
     */
    @Cacheable(value = "behaviorAnalytics", key = "#filter.campusId + '_' + #filter.startDate + '_' + #filter.endDate")
    public BehaviorAnalyticsDTO getBehaviorAnalytics(AnalyticsFilterDTO filter) {
        log.info("Fetching behavior analytics for campus: {}", filter.getCampusId());

        Long campusId = filter.getCampusId();
        LocalDate startDate = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();

        // Get summary data
        Map<String, Object> summary = getIncidentSummary(campusId, startDate, endDate);
        Map<String, Long> severityData = getSeverityBreakdown(campusId, startDate, endDate);

        return BehaviorAnalyticsDTO.builder()
                .campusId(campusId)
                .startDate(startDate)
                .endDate(endDate)
                // Overall metrics from summary
                .totalIncidents(summary.get("totalIncidents") != null ? ((Number) summary.get("totalIncidents")).longValue() : 0L)
                .positiveIncidents(summary.get("positiveIncidents") != null ? ((Number) summary.get("positiveIncidents")).longValue() : 0L)
                .negativeIncidents(summary.get("negativeIncidents") != null ? ((Number) summary.get("negativeIncidents")).longValue() : 0L)
                // Severity breakdown
                .minorIncidents(severityData.getOrDefault("Minor", 0L))
                .moderateIncidents(severityData.getOrDefault("Moderate", 0L))
                .majorIncidents(severityData.getOrDefault("Major", 0L))
                .severeIncidents(severityData.getOrDefault("Severe", 0L))
                // Category and location distribution
                .categoryDistribution(getCategoryBreakdown(campusId, startDate, endDate))
                .locationDistribution(getLocationBreakdown(campusId, startDate, endDate))
                .build();
    }

    // ========================================================================
    // INCIDENT SUMMARY
    // ========================================================================

    /**
     * Get incident summary statistics
     */
    public Map<String, Object> getIncidentSummary(Long campusId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> summary = new LinkedHashMap<>();

        List<Object[]> data = behaviorIncidentRepository.getIncidentSummary(startDate, endDate, campusId);

        if (data != null && !data.isEmpty()) {
            Object[] row = data.get(0);
            summary.put("totalIncidents", row[0] != null ? row[0] : 0L);
            summary.put("positiveIncidents", row[1] != null ? row[1] : 0L);
            summary.put("negativeIncidents", row[2] != null ? row[2] : 0L);
            summary.put("uniqueStudents", row[3] != null ? row[3] : 0L);
        } else {
            summary.put("totalIncidents", 0L);
            summary.put("positiveIncidents", 0L);
            summary.put("negativeIncidents", 0L);
            summary.put("uniqueStudents", 0L);
        }

        // Today's incidents
        List<Object[]> todayData = behaviorIncidentRepository.getDailyIncidentCounts(
                LocalDate.now(), LocalDate.now(), campusId);
        long todayCount = todayData.isEmpty() ? 0L :
                (todayData.get(0)[1] != null ? (Long) todayData.get(0)[1] : 0L);
        summary.put("todayIncidents", todayCount);

        return summary;
    }

    // ========================================================================
    // DAILY TRENDS
    // ========================================================================

    /**
     * Get daily incident counts for trend chart
     */
    public List<Map<String, Object>> getDailyIncidentCounts(Long campusId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> data = behaviorIncidentRepository.getDailyIncidentCounts(startDate, endDate, campusId);

        return data.stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("date", row[0]);
                    map.put("count", row[1] != null ? row[1] : 0L);
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculate daily average incidents
     */
    public Double getDailyAverageIncidents(Long campusId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> data = behaviorIncidentRepository.getDailyIncidentCounts(startDate, endDate, campusId);

        if (data.isEmpty()) return 0.0;

        long total = data.stream()
                .mapToLong(row -> row[1] != null ? (Long) row[1] : 0L)
                .sum();

        return Math.round(total * 10.0 / data.size()) / 10.0;
    }

    // ========================================================================
    // CATEGORY BREAKDOWN
    // ========================================================================

    /**
     * Get incidents by category
     */
    @Cacheable(value = "incidentCategories", key = "#campusId + '_' + #startDate + '_' + #endDate")
    public Map<String, Long> getCategoryBreakdown(Long campusId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> data = behaviorIncidentRepository.getIncidentCountsByCategory(startDate, endDate, campusId);

        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (Object[] row : data) {
            if (row[0] != null) {
                String category = (String) row[0];
                Long count = row[1] != null ? (Long) row[1] : 0L;
                breakdown.put(category, count);
            }
        }

        return breakdown;
    }

    /**
     * Get incidents by type (positive vs negative)
     */
    public Map<String, Long> getTypeBreakdown(Long campusId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> data = behaviorIncidentRepository.getIncidentCountsByType(startDate, endDate, campusId);

        Map<String, Long> breakdown = new LinkedHashMap<>();
        breakdown.put("Positive", 0L);
        breakdown.put("Negative", 0L);

        for (Object[] row : data) {
            if (row[0] != null) {
                String type = row[0].toString();
                Long count = row[1] != null ? (Long) row[1] : 0L;
                breakdown.put(type, count);
            }
        }

        return breakdown;
    }

    // ========================================================================
    // LOCATION ANALYSIS
    // ========================================================================

    /**
     * Get incidents by location (for hotspot analysis)
     */
    @Cacheable(value = "incidentLocations", key = "#campusId + '_' + #startDate + '_' + #endDate")
    public Map<String, Long> getLocationBreakdown(Long campusId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> data = behaviorIncidentRepository.getIncidentCountsByLocation(startDate, endDate, campusId);

        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (Object[] row : data) {
            if (row[0] != null) {
                String location = (String) row[0];
                Long count = row[1] != null ? (Long) row[1] : 0L;
                breakdown.put(location, count);
            }
        }

        // Sort by count descending
        return breakdown.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Get top incident locations
     */
    public List<String> getTopIncidentLocations(Long campusId, LocalDate startDate, LocalDate endDate, int limit) {
        Map<String, Long> locations = getLocationBreakdown(campusId, startDate, endDate);

        return locations.entrySet().stream()
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // ========================================================================
    // SEVERITY ANALYSIS
    // ========================================================================

    /**
     * Get incidents by severity level
     */
    public Map<String, Long> getSeverityBreakdown(Long campusId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> data = behaviorIncidentRepository.getIncidentCountsBySeverity(startDate, endDate, campusId);

        Map<String, Long> breakdown = new LinkedHashMap<>();
        // Initialize with expected severity levels
        breakdown.put("Minor", 0L);
        breakdown.put("Moderate", 0L);
        breakdown.put("Major", 0L);
        breakdown.put("Severe", 0L);

        for (Object[] row : data) {
            if (row[0] != null) {
                String severity = row[0].toString();
                Long count = row[1] != null ? (Long) row[1] : 0L;
                breakdown.put(severity, count);
            }
        }

        return breakdown;
    }

    // ========================================================================
    // REPEAT OFFENDERS
    // ========================================================================

    /**
     * Get repeat offenders (students with multiple incidents)
     */
    public List<Map<String, Object>> getRepeatOffenders(Long campusId, LocalDate sinceDate) {
        List<Object[]> data = behaviorIncidentRepository.findRepeatOffenders(sinceDate, campusId, 3L);

        return data.stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("studentId", row[0]);
                    map.put("studentName", row[1]);
                    map.put("incidentCount", row[2]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get students with positive behavior recognition
     */
    public List<Map<String, Object>> getTopPositiveBehaviorStudents(Long campusId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> data = behaviorIncidentRepository.findTopPositiveBehaviorStudents(startDate, endDate, campusId);

        return data.stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("studentId", row[0]);
                    map.put("studentName", row[1]);
                    map.put("positiveCount", row[2]);
                    return map;
                })
                .collect(Collectors.toList());
    }
}
