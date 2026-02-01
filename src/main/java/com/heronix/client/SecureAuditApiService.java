package com.heronix.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * API client for fetching decrypted secure audit logs from the SIS Server.
 * These logs are encrypted Hub activity records that only the server can decrypt.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SecureAuditApiService {

    private final RestTemplate restTemplate;
    private final ApiRetryHandler retryHandler;

    /**
     * Get paginated decrypted secure audit logs.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getLogs(int page, int size) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "/secure-audit/logs?page=" + page + "&size=" + size,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return Map.of("content", List.of(), "totalElements", 0);
        }, "Get secure audit logs");
    }

    /**
     * Search secure audit logs by date range.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> searchByDateRange(String from, String to, int page, int size) {
        return retryHandler.executeWithRetrySafe(() -> {
            String url = "/secure-audit/logs/search?from=" + from + "&to=" + to
                    + "&page=" + page + "&size=" + size;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return Map.of("content", List.of(), "totalElements", 0);
        }, "Search secure audit logs by date");
    }

    /**
     * Search secure audit logs by Hub device ID.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> searchByDevice(String hubDeviceId, int page, int size) {
        return retryHandler.executeWithRetrySafe(() -> {
            String url = "/secure-audit/logs/search?hubDeviceId=" + hubDeviceId
                    + "&page=" + page + "&size=" + size;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return Map.of("content", List.of(), "totalElements", 0);
        }, "Search secure audit logs by device");
    }

    /**
     * Get secure audit stats.
     */
    public Map<String, Object> getStats(int lastHours) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "/secure-audit/stats?lastHours=" + lastHours,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return Map.of("totalEntries", 0, "periodHours", lastHours);
        }, "Get secure audit stats");
    }
}
