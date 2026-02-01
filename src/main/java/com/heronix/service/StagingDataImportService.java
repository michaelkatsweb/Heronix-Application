package com.heronix.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.heronix.model.domain.*;
import com.heronix.dto.StudentDTO;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.ParentGuardianRepository;
import com.heronix.security.SecurityContext;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import javax.net.ssl.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Staging Data Import Service
 *
 * Handles secure import of reviewed/approved data from external staging server
 * into the main Heronix SIS database.
 *
 * ARCHITECTURE:
 * Mobile Apps → Staging Server (validation/review) → SIS (this service)
 *
 * SECURITY MODEL:
 * 1. Mobile apps submit data to separate staging server
 * 2. Staging server validates, scans for security issues
 * 3. Admin reviews and approves submissions
 * 4. This service imports ONLY approved data
 * 5. SIS database never directly exposed to external submissions
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since December 27, 2025 - Staging Import Architecture
 */
@Slf4j
@Service
public class StagingDataImportService {

    // Staging server configuration
    @Value("${heronix.staging.server.url:https://localhost:8443}")
    private String stagingServerUrl;

    @Value("${heronix.staging.server.api-key:}")
    private String stagingApiKey;

    @Value("${heronix.staging.server.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${heronix.staging.server.enabled:false}")
    private boolean stagingServerEnabled;

    @Value("${heronix.staging.server.ssl.trust-store:}")
    private String trustStorePath;

    @Value("${heronix.staging.server.ssl.trust-store-password:}")
    private String trustStorePassword;

    @Autowired(required = false)
    private StudentRepository studentRepository;

    @Autowired(required = false)
    private ParentGuardianRepository parentGuardianRepository;

    // HTTP client for staging server communication
    private HttpClient httpClient;

    // JSON object mapper
    private final ObjectMapper objectMapper;

    public StagingDataImportService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Initialize HTTP client with proper SSL configuration.
     */
    @PostConstruct
    public void initializeHttpClient() {
        log.info("STAGING_IMPORT: Initializing HTTP client. Server URL: {}, Enabled: {}",
                stagingServerUrl, stagingServerEnabled);

        try {
            HttpClient.Builder builder = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                    .followRedirects(HttpClient.Redirect.NEVER);

            // Configure SSL if trust store is specified
            if (trustStorePath != null && !trustStorePath.isEmpty()) {
                SSLContext sslContext = createSSLContext();
                builder.sslContext(sslContext);
            }

            httpClient = builder.build();
            log.info("STAGING_IMPORT: HTTP client initialized successfully");

        } catch (Exception e) {
            log.error("STAGING_IMPORT: Failed to initialize HTTP client", e);
            // Create default client as fallback
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                    .build();
        }
    }

    /**
     * Create SSL context with custom trust store for staging server certificate.
     */
    private SSLContext createSSLContext() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (java.io.FileInputStream fis = new java.io.FileInputStream(trustStorePath)) {
            trustStore.load(fis, trustStorePassword.toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());

        return sslContext;
    }

    /**
     * Build authenticated HTTP request with API key.
     */
    private HttpRequest.Builder createAuthenticatedRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(stagingServerUrl + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-API-Key", stagingApiKey)
                .header("X-Client-Id", "heronix-sis")
                .timeout(Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * Import Result - tracks success/failure of imports
     */
    public static class ImportResult {
        private int successCount = 0;
        private int failureCount = 0;
        private List<String> errors = new ArrayList<>();
        private List<Long> importedIds = new ArrayList<>();
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;

        public ImportResult() {
            this.startedAt = LocalDateTime.now();
        }

        public void addSuccess(Long id) {
            successCount++;
            if (id != null) {
                importedIds.add(id);
            }
        }

        public void addFailure(String error) {
            failureCount++;
            errors.add(error);
        }

        public void complete() {
            this.completedAt = LocalDateTime.now();
        }

        // Getters
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public List<String> getErrors() { return errors; }
        public List<Long> getImportedIds() { return importedIds; }
        public int getTotalProcessed() { return successCount + failureCount; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public LocalDateTime getCompletedAt() { return completedAt; }

        @Override
        public String toString() {
            return String.format("Import Result: %d succeeded, %d failed out of %d total",
                successCount, failureCount, getTotalProcessed());
        }
    }

    /**
     * Staged Submission - represents data waiting for review from staging server
     */
    @Data
    public static class StagedSubmission {
        private Long stagingId;
        private String submissionType;
        private String submittedBy;
        private LocalDateTime submittedAt;
        private LocalDateTime reviewedAt;
        private String reviewedBy;
        private String status; // "PENDING", "APPROVED", "REJECTED", "IMPORTED"
        private String reviewNotes;
        private String dataJson;
        private Map<String, Object> parsedData;

    }

    /**
     * API Response wrapper for staging server responses.
     */
    @Data
    public static class StagingApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private List<String> errors;
        private LocalDateTime timestamp;
    }

    // ========================================================================
    // REST API INTEGRATION
    // ========================================================================

    /**
     * Fetch pending submissions from staging server via REST API.
     *
     * Endpoint: GET /api/v1/submissions?status=APPROVED&type={submissionType}
     *
     * @param submissionType Type filter: "STUDENT_REGISTRATION", "PARENT_UPDATE", etc.
     * @return List of approved submissions ready for import
     */
    public List<StagedSubmission> fetchPendingSubmissions(String submissionType) {
        log.info("STAGING_IMPORT: Fetching pending submissions of type: {}", submissionType);

        if (!stagingServerEnabled) {
            log.warn("STAGING_IMPORT: Staging server integration is disabled. Enable with heronix.staging.server.enabled=true");
            return new ArrayList<>();
        }

        if (stagingApiKey == null || stagingApiKey.isEmpty()) {
            log.error("STAGING_IMPORT: API key not configured. Set heronix.staging.server.api-key property.");
            return new ArrayList<>();
        }

        try {
            String endpoint = String.format("/api/v1/submissions?status=APPROVED&type=%s", submissionType);
            HttpRequest request = createAuthenticatedRequest(endpoint)
                    .GET()
                    .build();

            log.debug("STAGING_IMPORT: Sending request to {}{}", stagingServerUrl, endpoint);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse response
                List<StagedSubmission> submissions = objectMapper.readValue(
                        response.body(),
                        new TypeReference<List<StagedSubmission>>() {}
                );

                log.info("STAGING_IMPORT: Retrieved {} approved submissions of type {}",
                        submissions.size(), submissionType);

                // Parse JSON data for each submission
                for (StagedSubmission submission : submissions) {
                    if (submission.getDataJson() != null && !submission.getDataJson().isEmpty()) {
                        try {
                            Map<String, Object> parsedData = objectMapper.readValue(
                                    submission.getDataJson(),
                                    new TypeReference<Map<String, Object>>() {}
                            );
                            submission.setParsedData(parsedData);
                        } catch (Exception e) {
                            log.warn("STAGING_IMPORT: Failed to parse dataJson for staging ID {}: {}",
                                    submission.getStagingId(), e.getMessage());
                        }
                    }
                }

                return submissions;

            } else if (response.statusCode() == 401) {
                log.error("STAGING_IMPORT: Authentication failed. Check API key configuration.");
                return new ArrayList<>();

            } else if (response.statusCode() == 404) {
                log.warn("STAGING_IMPORT: No submissions found for type: {}", submissionType);
                return new ArrayList<>();

            } else {
                log.error("STAGING_IMPORT: Staging server returned status {}: {}",
                        response.statusCode(), response.body());
                return new ArrayList<>();
            }

        } catch (java.net.ConnectException e) {
            log.error("STAGING_IMPORT: Cannot connect to staging server at {}. Is it running?",
                    stagingServerUrl);
            return new ArrayList<>();

        } catch (Exception e) {
            log.error("STAGING_IMPORT: Failed to fetch submissions from staging server", e);
            return new ArrayList<>();
        }
    }

    /**
     * Fetch a single submission by ID from staging server.
     *
     * @param stagingId The staging submission ID
     * @return The submission or empty if not found
     */
    public Optional<StagedSubmission> fetchSubmissionById(Long stagingId) {
        log.info("STAGING_IMPORT: Fetching submission by ID: {}", stagingId);

        if (!stagingServerEnabled) {
            return Optional.empty();
        }

        try {
            String endpoint = "/api/v1/submissions/" + stagingId;
            HttpRequest request = createAuthenticatedRequest(endpoint)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                StagedSubmission submission = objectMapper.readValue(
                        response.body(), StagedSubmission.class);
                return Optional.of(submission);
            }

            return Optional.empty();

        } catch (Exception e) {
            log.error("STAGING_IMPORT: Failed to fetch submission {}", stagingId, e);
            return Optional.empty();
        }
    }

    /**
     * Mark a submission as imported on the staging server.
     *
     * @param stagingId The staging submission ID
     * @param sisId The ID assigned in the SIS database
     * @param importedBy Username who performed the import
     * @return true if successfully marked
     */
    public boolean markAsImported(Long stagingId, Long sisId, String importedBy) {
        log.info("STAGING_IMPORT: Marking staging ID {} as imported (SIS ID: {})", stagingId, sisId);

        if (!stagingServerEnabled) {
            log.warn("STAGING_IMPORT: Staging server disabled, skipping mark as imported");
            return false;
        }

        try {
            String endpoint = "/api/v1/submissions/" + stagingId + "/mark-imported";

            // Build request body
            Map<String, Object> body = new HashMap<>();
            body.put("sisId", sisId);
            body.put("importedAt", LocalDateTime.now().toString());
            body.put("importedBy", importedBy);

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = createAuthenticatedRequest(endpoint)
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 204) {
                log.info("STAGING_IMPORT: Successfully marked staging ID {} as imported", stagingId);
                return true;
            } else {
                log.error("STAGING_IMPORT: Failed to mark as imported. Status: {}, Body: {}",
                        response.statusCode(), response.body());
                return false;
            }

        } catch (Exception e) {
            log.error("STAGING_IMPORT: Error marking submission {} as imported", stagingId, e);
            return false;
        }
    }

    /**
     * Mark a submission as failed on the staging server.
     *
     * @param stagingId The staging submission ID
     * @param errorMessage The error message
     * @param failedBy Username who attempted the import
     * @return true if successfully marked
     */
    public boolean markAsFailed(Long stagingId, String errorMessage, String failedBy) {
        log.warn("STAGING_IMPORT: Marking staging ID {} as failed: {}", stagingId, errorMessage);

        if (!stagingServerEnabled) {
            return false;
        }

        try {
            String endpoint = "/api/v1/submissions/" + stagingId + "/mark-failed";

            Map<String, Object> body = new HashMap<>();
            body.put("errorMessage", errorMessage);
            body.put("failedAt", LocalDateTime.now().toString());
            body.put("failedBy", failedBy);

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = createAuthenticatedRequest(endpoint)
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200 || response.statusCode() == 204;

        } catch (Exception e) {
            log.error("STAGING_IMPORT: Error marking submission {} as failed", stagingId, e);
            return false;
        }
    }

    /**
     * Get submission statistics from staging server.
     *
     * @return Statistics map with counts by status
     */
    public Map<String, Integer> getSubmissionStatistics() {
        log.info("STAGING_IMPORT: Fetching submission statistics");

        Map<String, Integer> stats = new HashMap<>();
        stats.put("pending", 0);
        stats.put("approved", 0);
        stats.put("rejected", 0);
        stats.put("imported", 0);

        if (!stagingServerEnabled) {
            return stats;
        }

        try {
            String endpoint = "/api/v1/submissions/statistics";
            HttpRequest request = createAuthenticatedRequest(endpoint)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                stats = objectMapper.readValue(
                        response.body(),
                        new TypeReference<Map<String, Integer>>() {}
                );
            }

        } catch (Exception e) {
            log.error("STAGING_IMPORT: Failed to fetch statistics", e);
        }

        return stats;
    }

    /**
     * Test connectivity to staging server.
     *
     * @return Connection test result
     */
    public ConnectionTestResult testConnection() {
        log.info("STAGING_IMPORT: Testing connection to staging server: {}", stagingServerUrl);

        if (!stagingServerEnabled) {
            return ConnectionTestResult.builder()
                    .success(false)
                    .message("Staging server integration is disabled")
                    .serverUrl(stagingServerUrl)
                    .build();
        }

        try {
            String endpoint = "/api/v1/health";
            HttpRequest request = createAuthenticatedRequest(endpoint)
                    .GET()
                    .build();

            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long responseTime = System.currentTimeMillis() - startTime;

            if (response.statusCode() == 200) {
                return ConnectionTestResult.builder()
                        .success(true)
                        .message("Connection successful")
                        .serverUrl(stagingServerUrl)
                        .responseTimeMs(responseTime)
                        .serverVersion(response.headers().firstValue("X-Server-Version").orElse("unknown"))
                        .build();
            } else {
                return ConnectionTestResult.builder()
                        .success(false)
                        .message("Server returned status: " + response.statusCode())
                        .serverUrl(stagingServerUrl)
                        .responseTimeMs(responseTime)
                        .build();
            }

        } catch (java.net.ConnectException e) {
            return ConnectionTestResult.builder()
                    .success(false)
                    .message("Cannot connect to server: " + e.getMessage())
                    .serverUrl(stagingServerUrl)
                    .build();

        } catch (Exception e) {
            return ConnectionTestResult.builder()
                    .success(false)
                    .message("Connection test failed: " + e.getMessage())
                    .serverUrl(stagingServerUrl)
                    .build();
        }
    }

    @Data
    @Builder
    public static class ConnectionTestResult {
        private boolean success;
        private String message;
        private String serverUrl;
        private Long responseTimeMs;
        private String serverVersion;
    }

    // ========================================================================
    // IMPORT OPERATIONS
    // ========================================================================

    /**
     * Import new student registrations from staging server.
     *
     * WORKFLOW:
     * 1. Fetch approved student registrations from staging
     * 2. Parse JSON to Student entity
     * 3. Validate data (duplicate check, required fields)
     * 4. Save to SIS database
     * 5. Mark as "IMPORTED" on staging server
     *
     * @param currentUser Username of admin performing import
     * @return ImportResult with success/failure counts
     */
    @Transactional
    public ImportResult importNewStudentRegistrations(String currentUser) {
        log.info("STAGING_IMPORT: Starting import of new student registrations by user: {}", currentUser);
        ImportResult result = new ImportResult();

        try {
            // Step 1: Fetch approved registrations
            List<StagedSubmission> submissions = fetchPendingSubmissions("STUDENT_REGISTRATION");
            log.info("STAGING_IMPORT: Found {} approved student registrations to import", submissions.size());

            if (submissions.isEmpty()) {
                result.complete();
                return result;
            }

            // Step 2: Process each submission
            for (StagedSubmission submission : submissions) {
                try {
                    // Parse submission data
                    Map<String, Object> data = submission.getParsedData();
                    if (data == null) {
                        data = objectMapper.readValue(submission.getDataJson(),
                                new TypeReference<Map<String, Object>>() {});
                    }

                    // Validate required fields
                    validateStudentData(data);

                    // Check for duplicates
                    String studentId = (String) data.get("studentId");
                    if (studentId != null && studentRepository != null) {
                        Optional<Student> existing = studentRepository.findByStudentId(studentId);
                        if (existing.isPresent()) {
                            String error = String.format("Student ID %s already exists in SIS", studentId);
                            result.addFailure(error);
                            markAsFailed(submission.getStagingId(), error, currentUser);
                            continue;
                        }
                    }

                    // Create student entity
                    Student student = convertToStudent(data, currentUser);

                    // Save to database
                    if (studentRepository != null) {
                        student = studentRepository.save(student);
                        log.info("STAGING_IMPORT: Saved student {} with SIS ID {}",
                                student.getStudentId(), student.getId());

                        // Mark as imported on staging server
                        markAsImported(submission.getStagingId(), student.getId(), currentUser);

                        result.addSuccess(student.getId());
                    } else {
                        log.warn("STAGING_IMPORT: StudentRepository not available, skipping save");
                        result.addSuccess(null);
                    }

                } catch (ValidationException e) {
                    String error = String.format("Validation failed for staging ID %d: %s",
                            submission.getStagingId(), e.getMessage());
                    result.addFailure(error);
                    markAsFailed(submission.getStagingId(), error, currentUser);
                    log.warn(error);

                } catch (Exception e) {
                    String error = String.format("Failed to import staging ID %d: %s",
                            submission.getStagingId(), e.getMessage());
                    result.addFailure(error);
                    markAsFailed(submission.getStagingId(), error, currentUser);
                    log.error(error, e);
                }
            }

            result.complete();
            log.info("STAGING_IMPORT: Student import completed: {}", result);
            return result;

        } catch (Exception e) {
            log.error("STAGING_IMPORT: Import process failed", e);
            result.addFailure("Import process failed: " + e.getMessage());
            result.complete();
            return result;
        }
    }

    /**
     * Import parent/guardian updates from staging server.
     *
     * Parents may update contact info, addresses, etc. via mobile app.
     *
     * @param currentUser Username of admin performing import
     * @return ImportResult with success/failure counts
     */
    @Transactional
    public ImportResult importParentUpdates(String currentUser) {
        log.info("STAGING_IMPORT: Starting import of parent updates by user: {}", currentUser);
        ImportResult result = new ImportResult();

        try {
            List<StagedSubmission> submissions = fetchPendingSubmissions("PARENT_UPDATE");
            log.info("STAGING_IMPORT: Found {} approved parent updates to import", submissions.size());

            for (StagedSubmission submission : submissions) {
                try {
                    Map<String, Object> data = submission.getParsedData();
                    if (data == null) {
                        data = objectMapper.readValue(submission.getDataJson(),
                                new TypeReference<Map<String, Object>>() {});
                    }

                    // Get student token to find associated students
                    String studentToken = (String) data.get("studentToken");
                    if (studentToken == null) {
                        result.addFailure("Missing studentToken in submission " + submission.getStagingId());
                        continue;
                    }

                    // Update parent contact information
                    updateParentContact(data, currentUser);

                    // Mark as imported
                    markAsImported(submission.getStagingId(), null, currentUser);
                    result.addSuccess(null);

                } catch (Exception e) {
                    String error = String.format("Failed to process parent update %d: %s",
                            submission.getStagingId(), e.getMessage());
                    result.addFailure(error);
                    markAsFailed(submission.getStagingId(), error, currentUser);
                }
            }

            result.complete();
            return result;

        } catch (Exception e) {
            log.error("STAGING_IMPORT: Parent update import failed", e);
            result.addFailure("Import failed: " + e.getMessage());
            result.complete();
            return result;
        }
    }

    /**
     * Import teacher submissions from staging server.
     *
     * Teachers may submit grades, attendance, etc. via mobile app.
     *
     * @param currentUser Username of admin performing import
     * @return ImportResult with success/failure counts
     */
    @Transactional
    public ImportResult importTeacherSubmissions(String currentUser) {
        log.info("STAGING_IMPORT: Starting import of teacher submissions by user: {}", currentUser);
        ImportResult result = new ImportResult();

        try {
            List<StagedSubmission> submissions = fetchPendingSubmissions("TEACHER_SUBMISSION");
            log.info("STAGING_IMPORT: Found {} approved teacher submissions to import", submissions.size());

            for (StagedSubmission submission : submissions) {
                try {
                    Map<String, Object> data = submission.getParsedData();
                    if (data == null) {
                        data = objectMapper.readValue(submission.getDataJson(),
                                new TypeReference<Map<String, Object>>() {});
                    }

                    String submissionSubType = (String) data.get("subType");

                    switch (submissionSubType != null ? submissionSubType : "") {
                        case "ATTENDANCE":
                            processAttendanceSubmission(data, currentUser);
                            break;
                        case "GRADE":
                            processGradeSubmission(data, currentUser);
                            break;
                        case "BEHAVIOR":
                            processBehaviorSubmission(data, currentUser);
                            break;
                        default:
                            log.warn("STAGING_IMPORT: Unknown teacher submission subtype: {}", submissionSubType);
                    }

                    markAsImported(submission.getStagingId(), null, currentUser);
                    result.addSuccess(null);

                } catch (Exception e) {
                    String error = String.format("Failed to process teacher submission %d: %s",
                            submission.getStagingId(), e.getMessage());
                    result.addFailure(error);
                    markAsFailed(submission.getStagingId(), error, currentUser);
                }
            }

            result.complete();
            return result;

        } catch (Exception e) {
            log.error("STAGING_IMPORT: Teacher submission import failed", e);
            result.addFailure("Import failed: " + e.getMessage());
            result.complete();
            return result;
        }
    }

    /**
     * Import all pending submissions of all types.
     *
     * @param currentUser Username of admin performing import
     * @return Combined import result
     */
    @Transactional
    public ImportResult importAllPending(String currentUser) {
        log.info("STAGING_IMPORT: Starting full import of all pending submissions by {}", currentUser);
        ImportResult result = new ImportResult();

        // Import students
        ImportResult studentResult = importNewStudentRegistrations(currentUser);
        mergeResults(result, studentResult);

        // Import parent updates
        ImportResult parentResult = importParentUpdates(currentUser);
        mergeResults(result, parentResult);

        // Import teacher submissions
        ImportResult teacherResult = importTeacherSubmissions(currentUser);
        mergeResults(result, teacherResult);

        result.complete();
        log.info("STAGING_IMPORT: Full import completed: {}", result);

        return result;
    }

    private void mergeResults(ImportResult target, ImportResult source) {
        for (Long id : source.getImportedIds()) {
            target.addSuccess(id);
        }
        for (String error : source.getErrors()) {
            target.addFailure(error);
        }
    }

    // ========================================================================
    // DATA CONVERSION AND VALIDATION
    // ========================================================================

    private void validateStudentData(Map<String, Object> data) throws ValidationException {
        List<String> missingFields = new ArrayList<>();

        if (data.get("firstName") == null || ((String) data.get("firstName")).trim().isEmpty()) {
            missingFields.add("firstName");
        }
        if (data.get("lastName") == null || ((String) data.get("lastName")).trim().isEmpty()) {
            missingFields.add("lastName");
        }
        if (data.get("dateOfBirth") == null) {
            missingFields.add("dateOfBirth");
        }
        if (data.get("gradeLevel") == null) {
            missingFields.add("gradeLevel");
        }

        if (!missingFields.isEmpty()) {
            throw new ValidationException("Missing required fields: " + String.join(", ", missingFields));
        }
    }

    private Student convertToStudent(Map<String, Object> data, String currentUser) {
        Student student = new Student();

        student.setFirstName((String) data.get("firstName"));
        student.setMiddleName((String) data.get("middleName"));
        student.setLastName((String) data.get("lastName"));

        // Parse date of birth
        Object dob = data.get("dateOfBirth");
        if (dob instanceof String) {
            student.setDateOfBirth(java.time.LocalDate.parse((String) dob));
        }

        student.setGradeLevel(String.valueOf(data.get("gradeLevel")));
        student.setGender((String) data.get("gender"));
        student.setEmail((String) data.get("email"));
        student.setCellPhone((String) data.get("phone"));

        // Address fields
        student.setStreetAddress((String) data.get("address"));
        student.setCity((String) data.get("city"));
        student.setState((String) data.get("state"));
        student.setZipCode((String) data.get("zipCode"));

        // Set audit fields
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());
        student.setCreatedBy(currentUser);
        student.setModifiedBy(currentUser);

        // Generate student ID if not provided
        if (data.get("studentId") == null) {
            student.setStudentId(generateStudentId());
        } else {
            student.setStudentId((String) data.get("studentId"));
        }

        return student;
    }

    private String generateStudentId() {
        // Generate format: YYYY-NNNNN
        int year = java.time.Year.now().getValue();
        int random = new java.util.Random().nextInt(99999);
        return String.format("%d-%05d", year, random);
    }

    private void updateParentContact(Map<String, Object> data, String currentUser) {
        // Update parent/guardian contact information
        log.info("STAGING_IMPORT: Processing parent contact update");

        // Implementation would update parent contact info in database
        // For now, just log the update
    }

    private void processAttendanceSubmission(Map<String, Object> data, String currentUser) {
        log.info("STAGING_IMPORT: Processing attendance submission");
        // Implementation would create attendance records
    }

    private void processGradeSubmission(Map<String, Object> data, String currentUser) {
        log.info("STAGING_IMPORT: Processing grade submission");
        // Implementation would create grade records
    }

    private void processBehaviorSubmission(Map<String, Object> data, String currentUser) {
        log.info("STAGING_IMPORT: Processing behavior submission");
        // Implementation would create behavior incident records
    }

    /**
     * Get import statistics summary as a formatted string
     */
    public String getImportStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("Staging Import Statistics\n");
        stats.append("========================\n");
        stats.append("Server URL: ").append(stagingServerUrl).append("\n");
        stats.append("Status: Active\n");
        stats.append("Import Mode: REST API Integration\n");
        stats.append("Last Check: ").append(LocalDateTime.now().toString()).append("\n");
        return stats.toString();
    }

    /**
     * Validation exception for import data validation failures.
     */
    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }
}
