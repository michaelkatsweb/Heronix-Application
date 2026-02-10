package com.heronix.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST client for SIS-Server /api/time/* endpoints.
 * Used by the Time & HR module in the SIS desktop app.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TimeApiService {

    private final RestTemplate restTemplate;
    private final ApiRetryHandler retryHandler;

    private static final String BASE_PATH = "/api/time";

    // ========================================================================
    // DASHBOARD
    // ========================================================================

    @SuppressWarnings("unchecked")
    public Map<String, Object> getDashboard() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(BASE_PATH + "/dashboard", Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch time dashboard", e);
            return new HashMap<>();
        }
    }

    // ========================================================================
    // TIME ENTRIES
    // ========================================================================

    public List<Map<String, Object>> getActiveEntries() {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    BASE_PATH + "/entries/active", HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch active entries", e);
            return List.of();
        }
    }

    public List<Map<String, Object>> getEntries(Long staffId, LocalDate startDate, LocalDate endDate) {
        try {
            String url = BASE_PATH + "/entries?staffId=" + staffId + "&startDate=" + startDate + "&endDate=" + endDate;
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch time entries for staff {}", staffId, e);
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getHoursSummary(Long staffId, LocalDate startDate, LocalDate endDate) {
        try {
            String url = BASE_PATH + "/summary?staffId=" + staffId + "&startDate=" + startDate + "&endDate=" + endDate;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch hours summary for staff {}", staffId, e);
            return new HashMap<>();
        }
    }

    // ========================================================================
    // LEAVE REQUESTS
    // ========================================================================

    public List<Map<String, Object>> getLeaveRequests(Long staffId) {
        try {
            String url = BASE_PATH + "/leave-requests?staffId=" + staffId;
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch leave requests for staff {}", staffId, e);
            return List.of();
        }
    }

    public List<Map<String, Object>> getPendingLeaveRequests() {
        try {
            String url = BASE_PATH + "/leave-requests?status=PENDING";
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch pending leave requests", e);
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> approveLeaveRequest(Long requestId, String reviewedBy, String notes) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("reviewedBy", reviewedBy);
            body.put("notes", notes);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_PATH + "/leave-request/" + requestId + "/approve",
                    HttpMethod.PUT, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to approve leave request {}", requestId, e);
            return new HashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> denyLeaveRequest(Long requestId, String reviewedBy, String notes) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("reviewedBy", reviewedBy);
            body.put("notes", notes);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_PATH + "/leave-request/" + requestId + "/deny",
                    HttpMethod.PUT, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to deny leave request {}", requestId, e);
            return new HashMap<>();
        }
    }

    // ========================================================================
    // DOCUMENTS
    // ========================================================================

    public List<Map<String, Object>> getDocuments(Long staffId) {
        try {
            String url = BASE_PATH + "/documents?staffId=" + staffId;
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch documents for staff {}", staffId, e);
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyDocument(Long documentId, String verifiedBy) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("verifiedBy", verifiedBy);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body);
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_PATH + "/documents/" + documentId + "/verify",
                    HttpMethod.PUT, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to verify document {}", documentId, e);
            return new HashMap<>();
        }
    }

    // ========================================================================
    // ACCESS LOG
    // ========================================================================

    public List<Map<String, Object>> getAccessLog(Long staffId, LocalDate date) {
        try {
            String url = BASE_PATH + "/access-log?staffId=" + staffId + "&date=" + date;
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch access log", e);
            return List.of();
        }
    }

    public List<Map<String, Object>> getLocations() {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    BASE_PATH + "/locations", HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch locations", e);
            return List.of();
        }
    }
}
