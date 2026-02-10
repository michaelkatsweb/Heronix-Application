package com.heronix.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heronix.dto.scheduler.SchedulerDataPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Client for communicating with Heronix-SchedulerV2 REST API
 * Handles data export, schedule generation requests, and result retrieval
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Component
public class SchedulerApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SchedulerApiClient(@Qualifier("schedulerRestTemplate") RestTemplate restTemplate,
                              ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Value("${heronix.scheduler.api-url:http://localhost:8090}")
    private String schedulerApiUrl;

    @Value("${heronix.scheduler.timeout-seconds:300}")
    private Integer timeoutSeconds;

    @Value("${heronix.scheduler.enabled:true}")
    private Boolean schedulerEnabled;

    // ========================================================================
    // HEALTH CHECK
    // ========================================================================

    /**
     * Check if SchedulerV2 is running and accessible
     *
     * @return true if SchedulerV2 is online and responding
     */
    public boolean isSchedulerAvailable() {
        if (!schedulerEnabled) {
            log.warn("SchedulerV2 integration is disabled");
            return false;
        }

        try {
            String healthUrl = schedulerApiUrl + "/actuator/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(healthUrl, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                if (body != null && "UP".equals(body.get("status"))) {
                    log.info("SchedulerV2 health check passed");
                    return true;
                }
            }

            log.warn("SchedulerV2 health check failed: {}", response.getStatusCode());
            return false;

        } catch (RestClientException e) {
            log.error("SchedulerV2 is not accessible: {}", e.getMessage());
            return false;
        }
    }

    // ========================================================================
    // DATA IMPORT
    // ========================================================================

    /**
     * Send scheduling data to SchedulerV2 for import
     *
     * @param payload Complete scheduling data payload
     * @return Import result with status
     * @throws SchedulerApiException if import fails
     */
    public SchedulerImportResult importData(SchedulerDataPayload payload) throws SchedulerApiException {
        if (!schedulerEnabled) {
            throw new SchedulerApiException("SchedulerV2 integration is disabled");
        }

        if (!isSchedulerAvailable()) {
            throw new SchedulerApiException("SchedulerV2 is not available");
        }

        try {
            String importUrl = schedulerApiUrl + "/api/admin/data/import";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<SchedulerDataPayload> request = new HttpEntity<>(payload, headers);

            log.info("Sending data import request to SchedulerV2: {} students, {} courses, {} teachers",
                    payload.getStudentRequests() != null ? payload.getStudentRequests().size() : 0,
                    payload.getCourses() != null ? payload.getCourses().size() : 0,
                    payload.getTeachers() != null ? payload.getTeachers().size() : 0);

            ResponseEntity<Map> response = restTemplate.postForEntity(importUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> body = response.getBody();
                log.info("Data import successful: {}", body);

                return SchedulerImportResult.builder()
                        .success(true)
                        .message("Data imported successfully")
                        .importId(body != null ? String.valueOf(body.get("importId")) : null)
                        .timestamp(System.currentTimeMillis())
                        .build();
            } else {
                throw new SchedulerApiException("Data import failed with status: " + response.getStatusCode());
            }

        } catch (RestClientException e) {
            log.error("Failed to import data to SchedulerV2", e);
            throw new SchedulerApiException("Data import failed: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // SCHEDULE GENERATION
    // ========================================================================

    /**
     * Request schedule generation from SchedulerV2
     *
     * @param generationRequest Generation parameters
     * @return Job ID for tracking the generation process
     * @throws SchedulerApiException if request fails
     */
    public String requestScheduleGeneration(ScheduleGenerationRequest generationRequest) throws SchedulerApiException {
        if (!schedulerEnabled) {
            throw new SchedulerApiException("SchedulerV2 integration is disabled");
        }

        if (!isSchedulerAvailable()) {
            throw new SchedulerApiException("SchedulerV2 is not available");
        }

        try {
            String generateUrl = schedulerApiUrl + "/api/admin/schedule/generate";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ScheduleGenerationRequest> request = new HttpEntity<>(generationRequest, headers);

            log.info("Requesting schedule generation from SchedulerV2");

            ResponseEntity<Map> response = restTemplate.postForEntity(generateUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.ACCEPTED) {
                Map<String, Object> body = response.getBody();
                if (body != null && body.containsKey("jobId")) {
                    String jobId = String.valueOf(body.get("jobId"));
                    log.info("Schedule generation started with job ID: {}", jobId);
                    return jobId;
                } else {
                    throw new SchedulerApiException("Schedule generation response missing jobId");
                }
            } else {
                throw new SchedulerApiException("Schedule generation failed with status: " + response.getStatusCode());
            }

        } catch (RestClientException e) {
            log.error("Failed to request schedule generation", e);
            throw new SchedulerApiException("Schedule generation request failed: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // STATUS POLLING
    // ========================================================================

    /**
     * Check the status of a schedule generation job
     *
     * @param jobId Job ID from schedule generation request
     * @return Job status information
     * @throws SchedulerApiException if status check fails
     */
    public ScheduleJobStatus getJobStatus(String jobId) throws SchedulerApiException {
        if (!schedulerEnabled) {
            throw new SchedulerApiException("SchedulerV2 integration is disabled");
        }

        try {
            String statusUrl = schedulerApiUrl + "/api/admin/schedule/" + jobId + "/status";

            ResponseEntity<Map> response = restTemplate.getForEntity(statusUrl, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();

                return ScheduleJobStatus.builder()
                        .jobId(jobId)
                        .status(body != null ? String.valueOf(body.get("status")) : "UNKNOWN")
                        .progress(body != null && body.containsKey("progress") ?
                                Integer.valueOf(String.valueOf(body.get("progress"))) : 0)
                        .message(body != null ? String.valueOf(body.get("message")) : null)
                        .hardScore(body != null && body.containsKey("hardScore") ?
                                Integer.valueOf(String.valueOf(body.get("hardScore"))) : null)
                        .softScore(body != null && body.containsKey("softScore") ?
                                Integer.valueOf(String.valueOf(body.get("softScore"))) : null)
                        .elapsedSeconds(body != null && body.containsKey("elapsedSeconds") ?
                                Integer.valueOf(String.valueOf(body.get("elapsedSeconds"))) : null)
                        .build();
            } else {
                throw new SchedulerApiException("Status check failed with status: " + response.getStatusCode());
            }

        } catch (RestClientException e) {
            log.error("Failed to get job status for job ID: {}", jobId, e);
            throw new SchedulerApiException("Status check failed: " + e.getMessage(), e);
        }
    }

    /**
     * Poll job status until completion or timeout
     *
     * @param jobId Job ID to monitor
     * @param pollIntervalSeconds Seconds between status checks
     * @return Final job status
     * @throws SchedulerApiException if polling fails or times out
     */
    public ScheduleJobStatus pollUntilComplete(String jobId, int pollIntervalSeconds) throws SchedulerApiException {
        log.info("Polling job {} for completion (interval: {}s, timeout: {}s)",
                jobId, pollIntervalSeconds, timeoutSeconds);

        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000L;

        while (true) {
            ScheduleJobStatus status = getJobStatus(jobId);

            log.debug("Job {} status: {} (progress: {}%)", jobId, status.getStatus(), status.getProgress());

            // Check if completed
            if ("COMPLETED".equalsIgnoreCase(status.getStatus()) ||
                "SUCCESS".equalsIgnoreCase(status.getStatus())) {
                log.info("Job {} completed successfully", jobId);
                return status;
            }

            // Check if failed
            if ("FAILED".equalsIgnoreCase(status.getStatus()) ||
                "ERROR".equalsIgnoreCase(status.getStatus())) {
                throw new SchedulerApiException("Job " + jobId + " failed: " + status.getMessage());
            }

            // Check timeout
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > timeoutMillis) {
                throw new SchedulerApiException("Job " + jobId + " timed out after " + timeoutSeconds + " seconds");
            }

            // Wait before next poll
            try {
                Thread.sleep(pollIntervalSeconds * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SchedulerApiException("Polling interrupted", e);
            }
        }
    }

    // ========================================================================
    // RESULT EXPORT
    // ========================================================================

    /**
     * Export the generated schedule from SchedulerV2
     *
     * @param jobId Job ID of completed schedule generation
     * @return Generated schedule data
     * @throws SchedulerApiException if export fails
     */
    public Map<String, Object> exportSchedule(String jobId) throws SchedulerApiException {
        if (!schedulerEnabled) {
            throw new SchedulerApiException("SchedulerV2 integration is disabled");
        }

        try {
            String exportUrl = schedulerApiUrl + "/api/admin/schedule/" + jobId + "/export";

            log.info("Exporting schedule from job: {}", jobId);

            ResponseEntity<Map> response = restTemplate.getForEntity(exportUrl, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                log.info("Schedule export successful");
                return body;
            } else {
                throw new SchedulerApiException("Schedule export failed with status: " + response.getStatusCode());
            }

        } catch (RestClientException e) {
            log.error("Failed to export schedule for job ID: {}", jobId, e);
            throw new SchedulerApiException("Schedule export failed: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // HELPER CLASSES
    // ========================================================================

    /**
     * Schedule generation request
     */
    @lombok.Data
    @lombok.Builder
    public static class ScheduleGenerationRequest {
        private Integer optimizationTimeSeconds;
        private Boolean enableAdvancedOptimization;
        private String optimizationMode; // "FAST", "BALANCED", "THOROUGH"
    }

    /**
     * Import result
     */
    @lombok.Data
    @lombok.Builder
    public static class SchedulerImportResult {
        private Boolean success;
        private String message;
        private String importId;
        private Long timestamp;
    }

    /**
     * Job status information
     */
    @lombok.Data
    @lombok.Builder
    public static class ScheduleJobStatus {
        private String jobId;
        private String status; // "PENDING", "RUNNING", "COMPLETED", "FAILED"
        private Integer progress; // 0-100
        private String message;
        private Integer hardScore;
        private Integer softScore;
        private Integer elapsedSeconds;
    }

    /**
     * Custom exception for Scheduler API errors
     */
    public static class SchedulerApiException extends Exception {
        public SchedulerApiException(String message) {
            super(message);
        }

        public SchedulerApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
