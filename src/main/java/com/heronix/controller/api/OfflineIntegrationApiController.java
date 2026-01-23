package com.heronix.controller.api;

import com.heronix.service.StateReportingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Offline-First External Integration API Controller
 *
 * Provides integration capabilities that work in airgapped/offline environments.
 * All integrations are file-based - NO external API calls or internet connectivity required.
 *
 * Design Philosophy:
 * - All state reporting exports to local files for manual submission
 * - Data imports from local files (CSV, Excel, XML)
 * - No OAuth, no external SSO, no cloud dependencies
 * - Manual approval workflow for all exports
 * - Full audit trail for compliance
 *
 * Supported Integrations:
 * - State Enrollment Reporting (file export)
 * - State Attendance Reporting (file export)
 * - CRDC Discipline Reporting (file export)
 * - Immunization Compliance (file export)
 * - Student Data Import (file import)
 * - Staff Data Import (file import)
 *
 * NOT Supported (requires internet - violates airgapped approach):
 * - Google Classroom (requires Google OAuth)
 * - Canvas LMS (requires Canvas API)
 * - Microsoft Teams (requires MS Graph API)
 * - Clever SSO (requires Clever OAuth)
 * - ClassLink SSO (requires ClassLink API)
 * - Real-time state portal submissions
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 20, 2026
 */
@RestController
@RequestMapping("/api/offline-integrations")
@RequiredArgsConstructor
@Slf4j
public class OfflineIntegrationApiController {

    private final StateReportingService stateReportingService;

    // ========================================================================
    // STATE REPORTING EXPORTS (Offline-First, File-Based)
    // ========================================================================

    /**
     * Generate state enrollment report export
     *
     * POST /api/offline-integrations/state-reporting/enrollment
     *
     * Exports student enrollment data to a local CSV file for manual submission
     * to state reporting systems.
     */
    @PostMapping("/state-reporting/enrollment")
    public ResponseEntity<Map<String, Object>> exportEnrollmentReport(
            @RequestBody Map<String, Object> request) {

        try {
            LocalDate reportDate = parseDate(request, "reportDate", LocalDate.now());
            String exportDirectory = (String) request.get("exportDirectory");
            Long staffId = Long.valueOf(request.get("staffId").toString());

            if (exportDirectory == null || exportDirectory.isEmpty()) {
                return badRequest("Export directory is required");
            }

            // Validate export directory for security
            StateReportingService.ExportValidation validation =
                    stateReportingService.validateExportDirectory(exportDirectory);

            if (!validation.isValid()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", validation.getMessage());
                response.put("securityWarning", validation.hasSecurityWarning());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            StateReportingService.StateReportExport export =
                    stateReportingService.generateEnrollmentReport(reportDate, exportDirectory, staffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", export.isSuccess());
            response.put("message", export.getMessage());
            response.put("filePath", export.getExportFilePath());
            response.put("recordCount", export.getRecordCount());
            response.put("checksum", export.getChecksum());
            response.put("exportedAt", export.getExportDate());
            response.put("offlineNote", "File exported locally. Please manually submit to state reporting portal.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Enrollment export error: {}", e.getMessage());
            return serverError("Failed to generate enrollment report: " + e.getMessage());
        }
    }

    /**
     * Generate immunization compliance report export
     *
     * POST /api/offline-integrations/state-reporting/immunization
     */
    @PostMapping("/state-reporting/immunization")
    public ResponseEntity<Map<String, Object>> exportImmunizationReport(
            @RequestBody Map<String, Object> request) {

        try {
            LocalDate reportDate = parseDate(request, "reportDate", LocalDate.now());
            String exportDirectory = (String) request.get("exportDirectory");
            Long staffId = Long.valueOf(request.get("staffId").toString());

            if (exportDirectory == null || exportDirectory.isEmpty()) {
                return badRequest("Export directory is required");
            }

            StateReportingService.ExportValidation validation =
                    stateReportingService.validateExportDirectory(exportDirectory);

            if (!validation.isValid()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", validation.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            StateReportingService.StateReportExport export =
                    stateReportingService.generateImmunizationComplianceReport(
                            reportDate, exportDirectory, staffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", export.isSuccess());
            response.put("message", export.getMessage());
            response.put("filePath", export.getExportFilePath());
            response.put("recordCount", export.getRecordCount());
            response.put("checksum", export.getChecksum());
            response.put("exportedAt", export.getExportDate());
            response.put("offlineNote", "File exported locally. Please manually submit to health department.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Immunization report export error: {}", e.getMessage());
            return serverError("Failed to generate immunization report: " + e.getMessage());
        }
    }

    /**
     * Generate CRDC discipline report export
     *
     * POST /api/offline-integrations/state-reporting/crdc
     */
    @PostMapping("/state-reporting/crdc")
    public ResponseEntity<Map<String, Object>> exportCRDCReport(
            @RequestBody Map<String, Object> request) {

        try {
            LocalDate schoolYearStart = parseDate(request, "schoolYearStart", LocalDate.now().minusMonths(6));
            LocalDate schoolYearEnd = parseDate(request, "schoolYearEnd", LocalDate.now());
            String exportDirectory = (String) request.get("exportDirectory");
            Long staffId = Long.valueOf(request.get("staffId").toString());

            if (exportDirectory == null || exportDirectory.isEmpty()) {
                return badRequest("Export directory is required");
            }

            StateReportingService.ExportValidation validation =
                    stateReportingService.validateExportDirectory(exportDirectory);

            if (!validation.isValid()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", validation.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            StateReportingService.StateReportExport export =
                    stateReportingService.generateCRDCReport(
                            schoolYearStart, schoolYearEnd, exportDirectory, staffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", export.isSuccess());
            response.put("message", export.getMessage());
            response.put("filePath", export.getExportFilePath());
            response.put("exportedAt", export.getExportDate());
            response.put("offlineNote", "Files exported locally. Please manually submit to CRDC portal.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("CRDC report export error: {}", e.getMessage());
            return serverError("Failed to generate CRDC report: " + e.getMessage());
        }
    }

    /**
     * Validate export directory security
     *
     * POST /api/offline-integrations/validate-directory
     */
    @PostMapping("/validate-directory")
    public ResponseEntity<Map<String, Object>> validateDirectory(
            @RequestBody Map<String, String> request) {

        try {
            String directory = request.get("directory");

            StateReportingService.ExportValidation validation =
                    stateReportingService.validateExportDirectory(directory);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", validation.isValid());
            response.put("message", validation.getMessage());
            response.put("securityWarning", validation.hasSecurityWarning());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return serverError("Failed to validate directory: " + e.getMessage());
        }
    }

    // ========================================================================
    // SUPPORTED OFFLINE INTEGRATIONS
    // ========================================================================

    /**
     * Get list of supported offline integrations
     *
     * GET /api/offline-integrations/supported
     */
    @GetMapping("/supported")
    public ResponseEntity<Map<String, Object>> getSupportedIntegrations() {
        List<Map<String, Object>> supported = new ArrayList<>();

        // File Export Integrations
        supported.add(createIntegration(
                "STATE_ENROLLMENT_EXPORT",
                "State Enrollment Reporting",
                "Export",
                "Export student enrollment data to CSV for manual state submission",
                true
        ));

        supported.add(createIntegration(
                "STATE_ATTENDANCE_EXPORT",
                "State Attendance Reporting",
                "Export",
                "Export attendance records to CSV for manual state submission",
                true
        ));

        supported.add(createIntegration(
                "CRDC_DISCIPLINE_EXPORT",
                "CRDC Discipline Reporting",
                "Export",
                "Export discipline data for Civil Rights Data Collection",
                true
        ));

        supported.add(createIntegration(
                "IMMUNIZATION_EXPORT",
                "Immunization Compliance",
                "Export",
                "Export immunization compliance data for health department",
                true
        ));

        supported.add(createIntegration(
                "STUDENT_DATA_IMPORT",
                "Student Data Import",
                "Import",
                "Import student data from CSV/Excel files",
                true
        ));

        supported.add(createIntegration(
                "STAFF_DATA_IMPORT",
                "Staff Data Import",
                "Import",
                "Import staff data from CSV/Excel files",
                true
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("integrations", supported);
        response.put("count", supported.size());
        response.put("offlineCompatible", true);

        return ResponseEntity.ok(response);
    }

    /**
     * Get list of NOT supported integrations (require internet)
     *
     * GET /api/offline-integrations/not-supported
     */
    @GetMapping("/not-supported")
    public ResponseEntity<Map<String, Object>> getNotSupportedIntegrations() {
        List<Map<String, Object>> notSupported = new ArrayList<>();

        notSupported.add(createIntegration(
                "GOOGLE_CLASSROOM",
                "Google Classroom",
                "LMS",
                "Requires Google OAuth and internet connectivity",
                false
        ));

        notSupported.add(createIntegration(
                "CANVAS",
                "Canvas LMS",
                "LMS",
                "Requires Canvas API and internet connectivity",
                false
        ));

        notSupported.add(createIntegration(
                "MICROSOFT_TEAMS",
                "Microsoft Teams",
                "Communication",
                "Requires MS Graph API and internet connectivity",
                false
        ));

        notSupported.add(createIntegration(
                "CLEVER_SSO",
                "Clever SSO",
                "SSO",
                "Requires Clever OAuth and internet connectivity",
                false
        ));

        notSupported.add(createIntegration(
                "CLASSLINK_SSO",
                "ClassLink SSO",
                "SSO",
                "Requires ClassLink API and internet connectivity",
                false
        ));

        notSupported.add(createIntegration(
                "POWERSCHOOL_API",
                "PowerSchool API Sync",
                "SIS",
                "Requires PowerSchool API and internet connectivity",
                false
        ));

        notSupported.add(createIntegration(
                "STATE_PORTAL_DIRECT",
                "Direct State Portal Submission",
                "Compliance",
                "Requires internet connectivity for direct API submission",
                false
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("notSupportedIntegrations", notSupported);
        response.put("count", notSupported.size());
        response.put("reason", "These integrations require internet connectivity and are not " +
                "compatible with airgapped/offline-first deployment");
        response.put("alternative", "Use file-based export/import workflows instead");

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // METADATA & HELP
    // ========================================================================

    /**
     * Get API metadata
     *
     * GET /api/offline-integrations/metadata
     */
    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("apiVersion", "1.0.0");
        metadata.put("apiName", "Heronix Offline Integration API");
        metadata.put("offlineFirst", true);
        metadata.put("airgappedCompatible", true);
        metadata.put("internetRequired", false);

        metadata.put("designPrinciples", List.of(
                "All exports are file-based (CSV, Excel, XML)",
                "No external API calls or OAuth flows",
                "Manual approval workflow for sensitive exports",
                "Full audit logging for compliance",
                "Security validation for export directories",
                "Checksum verification for data integrity"
        ));

        metadata.put("securityFeatures", List.of(
                "Export directory validation (no network drives)",
                "Staff ID audit trail on all exports",
                "File checksums for integrity verification",
                "PII data warnings in logs",
                "Local-only session management"
        ));

        metadata.put("supportedFormats", Map.of(
                "export", List.of("CSV", "Excel (XLSX)", "XML"),
                "import", List.of("CSV", "Excel (XLSX)", "XML")
        ));

        return ResponseEntity.ok(metadata);
    }

    /**
     * Get API help documentation
     *
     * GET /api/offline-integrations/help
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();

        help.put("description", "Heronix Offline-First Integration API");

        help.put("stateReporting", Map.of(
                "enrollment", "POST /api/offline-integrations/state-reporting/enrollment",
                "immunization", "POST /api/offline-integrations/state-reporting/immunization",
                "crdc", "POST /api/offline-integrations/state-reporting/crdc"
        ));

        help.put("utilities", Map.of(
                "validateDirectory", "POST /api/offline-integrations/validate-directory",
                "supportedIntegrations", "GET /api/offline-integrations/supported",
                "notSupportedIntegrations", "GET /api/offline-integrations/not-supported"
        ));

        help.put("workflow", List.of(
                "1. Validate export directory: POST /validate-directory",
                "2. Generate report: POST /state-reporting/{type}",
                "3. File is saved locally to specified directory",
                "4. Review exported file for accuracy",
                "5. Manually upload to state reporting portal",
                "6. Keep local copy for audit records"
        ));

        help.put("exampleRequest", Map.of(
                "endpoint", "POST /api/offline-integrations/state-reporting/enrollment",
                "body", Map.of(
                        "reportDate", "2026-01-20",
                        "exportDirectory", "C:/HeronixExports/StateReports",
                        "staffId", 1
                )
        ));

        help.put("offlineFirstExplanation",
                "This API is designed for schools that operate in airgapped or offline environments. " +
                        "All data exports are saved to local files that can be manually transferred to state " +
                        "systems via secure methods (USB drive, air-gapped workstation, etc.). No internet " +
                        "connectivity is required for any operation."
        );

        return ResponseEntity.ok(help);
    }

    /**
     * Get dashboard summary
     *
     * GET /api/offline-integrations/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("title", "Offline Integration Dashboard");
        dashboard.put("lastUpdated", LocalDateTime.now());

        dashboard.put("availableReports", List.of(
                Map.of("type", "ENROLLMENT", "name", "State Enrollment Report",
                        "description", "Student enrollment data for state reporting"),
                Map.of("type", "IMMUNIZATION", "name", "Immunization Compliance",
                        "description", "Vaccination compliance for health department"),
                Map.of("type", "CRDC", "name", "CRDC Discipline Report",
                        "description", "Civil Rights Data Collection discipline data"),
                Map.of("type", "ATTENDANCE", "name", "Attendance Report",
                        "description", "Daily attendance for state reporting")
        ));

        dashboard.put("quickActions", List.of(
                Map.of("action", "Generate Enrollment Report",
                        "endpoint", "POST /api/offline-integrations/state-reporting/enrollment"),
                Map.of("action", "Validate Export Directory",
                        "endpoint", "POST /api/offline-integrations/validate-directory"),
                Map.of("action", "View Supported Integrations",
                        "endpoint", "GET /api/offline-integrations/supported")
        ));

        dashboard.put("securityNote",
                "All exports require staff authentication and are logged for audit compliance. " +
                        "Exports containing PII must be handled securely per school district policy.");

        return ResponseEntity.ok(dashboard);
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private Map<String, Object> createIntegration(String code, String name, String category,
                                                   String description, boolean offlineCompatible) {
        Map<String, Object> integration = new HashMap<>();
        integration.put("code", code);
        integration.put("name", name);
        integration.put("category", category);
        integration.put("description", description);
        integration.put("offlineCompatible", offlineCompatible);
        return integration;
    }

    private LocalDate parseDate(Map<String, Object> request, String key, LocalDate defaultValue) {
        Object value = request.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String) {
            return LocalDate.parse((String) value, DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return defaultValue;
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    private ResponseEntity<Map<String, Object>> serverError(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
