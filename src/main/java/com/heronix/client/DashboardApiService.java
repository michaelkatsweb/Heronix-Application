package com.heronix.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard API Service
 *
 * Service layer for consuming attendance analytics REST APIs from JavaFX UI.
 * Provides methods to fetch dashboard data, analytics, and reports.
 *
 * API Endpoints:
 * - GET /attendance-analytics/dashboard - School-wide metrics
 * - GET /attendance-analytics/ada - Average Daily Attendance
 * - GET /attendance-analytics/trends - Attendance trends
 * - GET /attendance-analytics/chronic-absenteeism - At-risk students
 *
 * Error Handling:
 * - Returns empty data on network errors
 * - Logs errors for debugging
 * - Graceful degradation for offline mode
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 46 - Frontend Dashboard Integration
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardApiService {

    private final RestTemplate restTemplate;
    private final ApiRetryHandler retryHandler;

    /**
     * Fetch school-wide attendance dashboard data
     *
     * @param startDate Start date of period
     * @param endDate End date of period
     * @return Dashboard data map
     */
    public Map<String, Object> getAttendanceDashboard(LocalDate startDate, LocalDate endDate) {
        return retryHandler.executeWithRetrySafe(() -> {
            String url = "/attendance-analytics/dashboard?startDate={startDate}&endDate={endDate}";

            Map<String, Object> params = new HashMap<>();
            params.put("startDate", startDate.toString());
            params.put("endDate", endDate.toString());

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {},
                params
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (Boolean.TRUE.equals(body.get("success"))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dashboard = (Map<String, Object>) body.get("dashboard");
                    return dashboard != null ? dashboard : new HashMap<>();
                }
            }

            log.warn("Failed to fetch dashboard data: {}", response.getStatusCode());
            return new HashMap<>();

        }, "Get attendance dashboard");
    }

    /**
     * Calculate Average Daily Attendance (ADA)
     *
     * @param startDate Start date
     * @param endDate End date
     * @return ADA data
     */
    public Map<String, Object> calculateADA(LocalDate startDate, LocalDate endDate) {
        return retryHandler.executeWithRetrySafe(() -> {
            String url = "/attendance-analytics/ada?startDate={startDate}&endDate={endDate}";

            Map<String, Object> params = new HashMap<>();
            params.put("startDate", startDate.toString());
            params.put("endDate", endDate.toString());

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {},
                params
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (Boolean.TRUE.equals(body.get("success"))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> ada = (Map<String, Object>) body.get("ada");
                    return ada != null ? ada : new HashMap<>();
                }
            }

            return new HashMap<>();

        }, "Calculate ADA");
    }

    /**
     * Get attendance trends
     *
     * @param startDate Start date
     * @param endDate End date
     * @param groupBy Grouping (day, week, month)
     * @return Trends data
     */
    public Map<String, Object> getAttendanceTrends(LocalDate startDate, LocalDate endDate, String groupBy) {
        return retryHandler.executeWithRetrySafe(() -> {
            String url = "/attendance-analytics/trends?startDate={startDate}&endDate={endDate}&groupBy={groupBy}";

            Map<String, Object> params = new HashMap<>();
            params.put("startDate", startDate.toString());
            params.put("endDate", endDate.toString());
            params.put("groupBy", groupBy);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {},
                params
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

            return new HashMap<>();

        }, "Get attendance trends");
    }

    /**
     * Get chronic absenteeism data
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Chronic absenteeism data
     */
    public Map<String, Object> getChronicAbsenteeism(LocalDate startDate, LocalDate endDate) {
        return retryHandler.executeWithRetrySafe(() -> {
            String url = "/attendance-analytics/chronic-absenteeism?startDate={startDate}&endDate={endDate}";

            Map<String, Object> params = new HashMap<>();
            params.put("startDate", startDate.toString());
            params.put("endDate", endDate.toString());

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {},
                params
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

            return new HashMap<>();

        }, "Get chronic absenteeism data");
    }

    /**
     * Check if API is available
     *
     * @return true if API is reachable
     */
    public boolean isApiAvailable() {
        try {
            // Simple health check - try to access base endpoint
            ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("API not available: {}", e.getMessage());
            return false;
        }
    }
}
