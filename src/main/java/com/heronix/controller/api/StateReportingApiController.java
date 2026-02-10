package com.heronix.controller.api;

import com.heronix.model.domain.StateConfiguration;
import com.heronix.model.enums.USState;
import com.heronix.repository.StateConfigurationRepository;
import com.heronix.service.StateReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API Controller for State Reporting
 *
 * SECURITY CRITICAL: Generates exports for manual submission to state agencies.
 * Does NOT send data automatically - exports must be manually reviewed and uploaded
 * by authorized personnel to maintain air-gap security.
 *
 * Supported Reports:
 * - Student Enrollment Reports (PII - requires encryption)
 * - Immunization Compliance Reports
 * - CRDC (Civil Rights Data Collection) Reports
 * - Demographics and Special Programs
 *
 * Security Features:
 * - Manual approval workflow required
 * - Data validation checksums
 * - Generation timestamp and user audit
 * - Export directory validation
 * - PII encryption recommendations
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/state-reporting")
@RequiredArgsConstructor
public class StateReportingApiController {

    private final StateReportingService stateReportingService;
    private final StateConfigurationRepository stateConfigurationRepository;

    // ==================== Export Generation ====================

    @PostMapping("/enrollment-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'REGISTRAR')")
    public ResponseEntity<Map<String, Object>> generateEnrollmentReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate,
            @RequestParam String exportDirectory,
            @RequestParam Long exportedByStaffId) {

        try {
            // Validate export directory first
            StateReportingService.ExportValidation validation =
                stateReportingService.validateExportDirectory(exportDirectory);

            if (!validation.isValid()) {
                Map<String, Object> validationError = new HashMap<>();
                validationError.put("success", false);
                validationError.put("error", "Export directory validation failed");
                validationError.put("message", validation.getMessage());
                validationError.put("securityWarning", validation.hasSecurityWarning());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
            }

            StateReportingService.StateReportExport export =
                stateReportingService.generateEnrollmentReport(reportDate, exportDirectory, exportedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", export.isSuccess());
            response.put("reportType", "ENROLLMENT");
            response.put("reportDate", export.getReportDate());
            response.put("exportDate", export.getExportDate());
            response.put("exportFilePath", export.getExportFilePath());
            response.put("recordCount", export.getRecordCount());
            response.put("checksum", export.getChecksum());
            response.put("exportedByStaffId", export.getExportedByStaffId());
            response.put("message", export.getMessage());

            response.put("securityWarning", Map.of(
                "containsPII", true,
                "requiresEncryption", true,
                "recommendation", "Encrypt file before leaving the building",
                "manualApproval", "Required before submission to state"
            ));

            response.put("nextSteps", List.of(
                "1. Review export file for accuracy",
                "2. Encrypt file using approved encryption tool",
                "3. Obtain administrative approval",
                "4. Manually upload to state system",
                "5. Document submission in audit log"
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/immunization-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'NURSE')")
    public ResponseEntity<Map<String, Object>> generateImmunizationReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate,
            @RequestParam String exportDirectory,
            @RequestParam Long exportedByStaffId) {

        try {
            // Validate export directory
            StateReportingService.ExportValidation validation =
                stateReportingService.validateExportDirectory(exportDirectory);

            if (!validation.isValid()) {
                Map<String, Object> validationError = new HashMap<>();
                validationError.put("success", false);
                validationError.put("error", "Export directory validation failed");
                validationError.put("message", validation.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
            }

            StateReportingService.StateReportExport export =
                stateReportingService.generateImmunizationComplianceReport(reportDate, exportDirectory, exportedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", export.isSuccess());
            response.put("reportType", "IMMUNIZATION_COMPLIANCE");
            response.put("reportDate", export.getReportDate());
            response.put("exportDate", export.getExportDate());
            response.put("exportFilePath", export.getExportFilePath());
            response.put("recordCount", export.getRecordCount());
            response.put("checksum", export.getChecksum());
            response.put("exportedByStaffId", export.getExportedByStaffId());
            response.put("message", export.getMessage());

            response.put("securityWarning", Map.of(
                "containsPHI", true,
                "requiresEncryption", true,
                "recommendation", "Medical data requires HIPAA-compliant encryption"
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/crdc-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    public ResponseEntity<Map<String, Object>> generateCRDCReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate schoolYearStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate schoolYearEnd,
            @RequestParam String exportDirectory,
            @RequestParam Long exportedByStaffId) {

        try {
            // Validate export directory
            StateReportingService.ExportValidation validation =
                stateReportingService.validateExportDirectory(exportDirectory);

            if (!validation.isValid()) {
                Map<String, Object> validationError = new HashMap<>();
                validationError.put("success", false);
                validationError.put("error", "Export directory validation failed");
                validationError.put("message", validation.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
            }

            StateReportingService.StateReportExport export =
                stateReportingService.generateCRDCReport(schoolYearStart, schoolYearEnd, exportDirectory, exportedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", export.isSuccess());
            response.put("reportType", "CRDC");
            response.put("reportDate", export.getReportDate());
            response.put("exportDate", export.getExportDate());
            response.put("exportFilePath", export.getExportFilePath());
            response.put("recordCount", export.getRecordCount());
            response.put("checksum", export.getChecksum());
            response.put("exportedByStaffId", export.getExportedByStaffId());
            response.put("message", export.getMessage());

            response.put("reportDescription", Map.of(
                "fullName", "Civil Rights Data Collection",
                "purpose", "Federal reporting on discipline, enrollment, and program participation by demographic groups",
                "frequency", "Biennial (every 2 years)",
                "agency", "U.S. Department of Education Office for Civil Rights"
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Export Validation ====================

    @PostMapping("/validate-directory")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'REGISTRAR', 'NURSE')")
    public ResponseEntity<Map<String, Object>> validateExportDirectory(
            @RequestParam String directory) {

        StateReportingService.ExportValidation validation =
            stateReportingService.validateExportDirectory(directory);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", validation.isValid());
        response.put("directory", directory);
        response.put("message", validation.getMessage());
        response.put("securityWarning", validation.hasSecurityWarning());

        if (!validation.isValid()) {
            response.put("recommendations", List.of(
                "Ensure directory exists",
                "Verify write permissions",
                "Use secure, local directory (not network share)",
                "Ensure directory is encrypted at rest"
            ));
        }

        if (validation.hasSecurityWarning()) {
            response.put("securityRecommendations", List.of(
                "Do not use network-mounted directories",
                "Ensure directory is on encrypted volume",
                "Restrict directory access to authorized personnel only",
                "Enable audit logging for directory access"
            ));
        }

        return ResponseEntity.ok(response);
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("availableReports", Map.of(
            "enrollment", Map.of(
                "type", "ENROLLMENT",
                "description", "Student enrollment data for state reporting",
                "containsPII", true,
                "frequency", "Monthly or as required",
                "endpoint", "POST /api/state-reporting/enrollment-report"
            ),
            "immunization", Map.of(
                "type", "IMMUNIZATION_COMPLIANCE",
                "description", "Student immunization compliance status",
                "containsPHI", true,
                "frequency", "Quarterly or as required",
                "endpoint", "POST /api/state-reporting/immunization-report"
            ),
            "crdc", Map.of(
                "type", "CRDC",
                "description", "Civil Rights Data Collection",
                "frequency", "Biennial",
                "agency", "U.S. Dept of Education OCR",
                "endpoint", "POST /api/state-reporting/crdc-report"
            )
        ));

        dashboard.put("securityProtocol", Map.of(
            "airGapSecurity", "Exports are generated locally - NO automatic transmission",
            "manualApproval", "All exports require administrative review before submission",
            "encryption", "PII/PHI exports must be encrypted before leaving building",
            "auditTrail", "All exports logged with timestamp and staff ID"
        ));

        dashboard.put("compliance", Map.of(
            "dataProtection", "FERPA compliance for student records",
            "medicalPrivacy", "HIPAA compliance for immunization records",
            "civilRights", "OCR compliance for CRDC reporting"
        ));

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> summary = new HashMap<>();

        summary.put("reportTypes", 3);
        summary.put("securityModel", "Air-gap (manual submission only)");
        summary.put("encryptionRequired", true);
        summary.put("manualApprovalRequired", true);

        summary.put("reportCategories", List.of(
            "Enrollment",
            "Immunization/Health",
            "Civil Rights Data"
        ));

        return ResponseEntity.ok(summary);
    }

    // ==================== Configuration ====================

    @GetMapping("/configuration/report-types")
    public ResponseEntity<Map<String, Object>> getReportTypes() {
        Map<String, Object> types = new HashMap<>();

        types.put("ENROLLMENT", Map.of(
            "name", "Student Enrollment Report",
            "description", "Complete student enrollment data for state funding and reporting",
            "dataClassification", "PII - Personally Identifiable Information",
            "encryptionRequired", true,
            "frequency", "Monthly or as required by state",
            "recipientAgency", "State Department of Education"
        ));

        types.put("IMMUNIZATION_COMPLIANCE", Map.of(
            "name", "Immunization Compliance Report",
            "description", "Student immunization records and compliance status",
            "dataClassification", "PHI - Protected Health Information",
            "encryptionRequired", true,
            "hipaaCompliant", true,
            "frequency", "Quarterly or as required",
            "recipientAgency", "State Health Department"
        ));

        types.put("CRDC", Map.of(
            "name", "Civil Rights Data Collection",
            "description", "Federal reporting on discipline, enrollment, and programs by demographics",
            "dataClassification", "Demographic and Disciplinary Data",
            "frequency", "Biennial (every 2 years)",
            "recipientAgency", "U.S. Department of Education Office for Civil Rights",
            "website", "https://ocrdata.ed.gov/"
        ));

        return ResponseEntity.ok(types);
    }

    @GetMapping("/configuration/security-requirements")
    public ResponseEntity<Map<String, Object>> getSecurityRequirements() {
        Map<String, Object> security = new HashMap<>();

        security.put("exportProtocol", Map.of(
            "manualGeneration", "Exports generated on-demand by authorized staff",
            "noAutomaticTransmission", "System does NOT automatically send data to state",
            "airGapSecurity", "Maintains air-gap - manual submission only",
            "administrativeApproval", "Required before submission to state agencies"
        ));

        security.put("dataProtection", Map.of(
            "encryption", "All PII/PHI exports must be encrypted before leaving facility",
            "encryptionStandard", "AES-256 or approved equivalent",
            "accessControl", "Exports restricted to authorized personnel only",
            "auditLogging", "All export operations logged with user ID and timestamp"
        ));

        security.put("compliance", Map.of(
            "FERPA", "Family Educational Rights and Privacy Act - student records",
            "HIPAA", "Health Insurance Portability and Accountability Act - medical records",
            "StateRegulations", "Specific state data protection requirements"
        ));

        security.put("workflow", List.of(
            "1. Authorized staff generates export via API",
            "2. System creates export file with validation checksum",
            "3. Staff reviews export for accuracy",
            "4. Staff encrypts file using approved encryption tool",
            "5. Staff obtains administrative approval",
            "6. Staff manually uploads to state portal",
            "7. Staff documents submission in audit log"
        ));

        return ResponseEntity.ok(security);
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "1.0.0");
        metadata.put("securityModel", "Air-gap manual submission");

        metadata.put("features", Map.of(
            "enrollmentReports", "Student enrollment exports for state reporting",
            "immunizationReports", "HIPAA-compliant immunization compliance exports",
            "crdcReports", "Federal civil rights data collection",
            "exportValidation", "Directory and security validation",
            "auditTrail", "Complete audit logging of all exports"
        ));

        metadata.put("criticalSecurity", Map.of(
            "noAutomaticTransmission", "Exports are NEVER automatically sent to external systems",
            "manualApprovalRequired", "Administrative review required before submission",
            "encryptionRequired", "PII/PHI must be encrypted before leaving facility",
            "auditLogging", "All operations logged for compliance"
        ));

        metadata.put("supportedReports", List.of(
            "Student Enrollment (PII)",
            "Immunization Compliance (PHI)",
            "CRDC - Civil Rights Data Collection"
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();

        help.put("title", "State Reporting Help");

        help.put("workflows", Map.of(
            "monthlyEnrollmentReport", List.of(
                "1. Prepare secure export directory (validate with /validate-directory)",
                "2. POST /api/state-reporting/enrollment-report with reportDate, directory, staffId",
                "3. Review generated export file for accuracy",
                "4. Encrypt file using approved encryption tool (AES-256)",
                "5. Obtain administrative approval for submission",
                "6. Manually upload to state portal",
                "7. Document submission date and confirmation number"
            ),
            "immunizationCompliance", List.of(
                "1. Validate export directory",
                "2. Generate report via /immunization-report endpoint",
                "3. Encrypt file (HIPAA-compliant encryption required)",
                "4. Submit to State Health Department portal",
                "5. Retain encrypted copy for records"
            ),
            "crdcReporting", List.of(
                "1. Generate CRDC report (biennial requirement)",
                "2. Review demographic and discipline data for accuracy",
                "3. Obtain district approval",
                "4. Upload to federal OCR data collection portal",
                "5. Confirm submission receipt"
            )
        ));

        help.put("endpoints", Map.of(
            "enrollmentReport", "POST /api/state-reporting/enrollment-report",
            "immunizationReport", "POST /api/state-reporting/immunization-report",
            "crdcReport", "POST /api/state-reporting/crdc-report",
            "validateDirectory", "POST /api/state-reporting/validate-directory"
        ));

        help.put("requiredParameters", Map.of(
            "reportDate", "Date of report (ISO format: YYYY-MM-DD)",
            "exportDirectory", "Secure local directory for export file",
            "exportedByStaffId", "Staff ID of person generating export (for audit trail)"
        ));

        help.put("security", Map.of(
            "critical", "NEVER automatically transmit exports",
            "encryption", "Always encrypt PII/PHI exports before leaving facility",
            "approval", "Obtain administrative approval before submission",
            "audit", "Document all submissions in compliance log"
        ));

        help.put("examples", Map.of(
            "validateDirectory", "curl -X POST 'http://localhost:9590/api/state-reporting/validate-directory?directory=C:/SecureExports'",
            "enrollmentReport", "curl -X POST 'http://localhost:9590/api/state-reporting/enrollment-report?reportDate=2025-01-31&exportDirectory=C:/SecureExports&exportedByStaffId=123'",
            "dashboard", "curl 'http://localhost:9590/api/state-reporting/dashboard'"
        ));

        return ResponseEntity.ok(help);
    }

    // ==================== State Configuration Setup (IT Admin) ====================

    @GetMapping("/setup/states")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, String>>> listAvailableStates() {
        List<Map<String, String>> states = Arrays.stream(USState.values())
                .filter(s -> s != USState.NBPTS && s != USState.INTERSTATE && s != USState.OTHER)
                .map(s -> Map.of(
                        "code", s.name(),
                        "name", s.getDisplayName(),
                        "agency", s.getCertifyingAgency() != null ? s.getCertifyingAgency() : ""
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(states);
    }

    @GetMapping("/setup/current")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCurrentStateConfig() {
        StateConfiguration config = stateReportingService.getActiveStateConfiguration();
        Map<String, Object> response = new HashMap<>();

        if (config == null) {
            response.put("configured", false);
            response.put("message", "No state reporting system has been configured. Please select your state to set up reporting.");
            return ResponseEntity.ok(response);
        }

        response.put("configured", true);
        response.put("state", config.getState().name());
        response.put("stateName", config.getStateName());
        response.put("reportingSystem", config.getStateReportingSystem());
        response.put("educationDepartment", config.getEducationDepartment());
        response.put("departmentAbbreviation", config.getDepartmentAbbreviation());
        response.put("studentIdLabel", config.getStudentIdLabelOrDefault());
        response.put("courseCodeLabel", config.getCourseCodeLabelOrDefault());
        response.put("reportingPeriodsPerYear", config.getReportingPeriodsPerYear());
        response.put("active", config.getActive());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/setup/select-state")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> selectState(@RequestParam String stateCode) {
        Map<String, Object> response = new HashMap<>();

        USState state;
        try {
            state = USState.valueOf(stateCode.toUpperCase());
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", "Invalid state code: " + stateCode);
            return ResponseEntity.badRequest().body(response);
        }

        // Check if already configured
        Optional<StateConfiguration> existingOpt = stateConfigurationRepository.findByState(state);
        if (existingOpt.isPresent()) {
            StateConfiguration existing = existingOpt.get();
            existing.setActive(true);
            stateConfigurationRepository.save(existing);
            response.put("success", true);
            response.put("message", "State reporting activated for " + state.getDisplayName());
            response.put("reportingSystem", existing.getStateReportingSystem());
            return ResponseEntity.ok(response);
        }

        // Create new configuration with state-specific defaults
        StateConfiguration config = new StateConfiguration();
        config.setState(state);
        config.setStateName(state.getDisplayName());
        config.setEducationDepartment(state.getCertifyingAgency());
        config.setActive(true);

        applyStateDefaults(config, state);

        stateConfigurationRepository.save(config);

        response.put("success", true);
        response.put("message", "State reporting configured for " + state.getDisplayName());
        response.put("reportingSystem", config.getStateReportingSystem());
        response.put("nextSteps", List.of(
                "Review configuration at GET /api/state-reporting/setup/current",
                "Customize settings with PUT /api/state-reporting/setup/configure",
                "Generate reports from the State Reporting dashboard"
        ));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/setup/configure")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateStateConfig(@RequestBody Map<String, Object> updates) {
        StateConfiguration config = stateReportingService.getActiveStateConfiguration();
        Map<String, Object> response = new HashMap<>();

        if (config == null) {
            response.put("success", false);
            response.put("error", "No state is configured. Use POST /setup/select-state first.");
            return ResponseEntity.badRequest().body(response);
        }

        // Apply updates
        if (updates.containsKey("reportingSystem")) config.setStateReportingSystem((String) updates.get("reportingSystem"));
        if (updates.containsKey("studentIdLabel")) config.setStudentIdLabel((String) updates.get("studentIdLabel"));
        if (updates.containsKey("courseCodeLabel")) config.setCourseCodeLabel((String) updates.get("courseCodeLabel"));
        if (updates.containsKey("studentIdFormat")) config.setStudentIdFormat((String) updates.get("studentIdFormat"));
        if (updates.containsKey("courseCodeFormat")) config.setCourseCodeFormat((String) updates.get("courseCodeFormat"));
        if (updates.containsKey("reportingPeriodsPerYear")) config.setReportingPeriodsPerYear((Integer) updates.get("reportingPeriodsPerYear"));
        if (updates.containsKey("departmentUrl")) config.setDepartmentUrl((String) updates.get("departmentUrl"));

        stateConfigurationRepository.save(config);

        response.put("success", true);
        response.put("message", "State configuration updated");
        return ResponseEntity.ok(response);
    }

    /**
     * Apply well-known defaults for common state reporting systems.
     */
    private void applyStateDefaults(StateConfiguration config, USState state) {
        switch (state) {
            case TX -> {
                config.setStateReportingSystem("PEIMS");
                config.setDepartmentAbbreviation("TEA");
                config.setStudentIdSystemName("PEIMS ID");
                config.setStudentIdLabel("PEIMS Student ID");
                config.setStudentIdFormat("#########");
                config.setStudentIdLength(9);
                config.setCourseCodeSystemName("PEIMS Service IDs");
                config.setCourseCodeLabel("Service ID");
                config.setCourseCodeFormat("########");
                config.setCourseCodeLength(8);
                config.setSchoolIdSystemName("Campus ID");
                config.setSchoolIdLabel("Campus Number");
                config.setReportingPeriodsPerYear(6);
                config.setEllLabel("Emergent Bilingual");
            }
            case CA -> {
                config.setStateReportingSystem("CALPADS");
                config.setDepartmentAbbreviation("CDE");
                config.setStudentIdSystemName("SSID");
                config.setStudentIdLabel("Statewide Student ID (SSID)");
                config.setStudentIdFormat("##########");
                config.setStudentIdLength(10);
                config.setCourseCodeSystemName("CALPADS Course Codes");
                config.setCourseCodeLabel("State Course Code");
                config.setSchoolIdSystemName("CDS Code");
                config.setSchoolIdLabel("County-District-School Code");
                config.setReportingPeriodsPerYear(4);
            }
            case OH -> {
                config.setStateReportingSystem("EMIS");
                config.setDepartmentAbbreviation("ODE");
                config.setStudentIdSystemName("SSID");
                config.setStudentIdLabel("State Student ID (SSID)");
                config.setStudentIdFormat("#########");
                config.setStudentIdLength(9);
                config.setSchoolIdSystemName("IRN");
                config.setSchoolIdLabel("Information Retrieval Number");
                config.setReportingPeriodsPerYear(5);
            }
            case MA -> {
                config.setStateReportingSystem("SIMS");
                config.setDepartmentAbbreviation("DESE");
                config.setStudentIdSystemName("SASID");
                config.setStudentIdLabel("State Assigned Student ID");
                config.setStudentIdFormat("##########");
                config.setStudentIdLength(10);
                config.setReportingPeriodsPerYear(3);
            }
            case NY -> {
                config.setStateReportingSystem("SIRS");
                config.setDepartmentAbbreviation("NYSED");
                config.setStudentIdSystemName("NYSSIS ID");
                config.setStudentIdLabel("NYSSIS Student ID");
                config.setStudentIdFormat("##########");
                config.setStudentIdLength(10);
                config.setSchoolIdSystemName("BEDS Code");
                config.setSchoolIdLabel("BEDS Code");
                config.setReportingPeriodsPerYear(4);
            }
            case FL -> {
                config.setStateReportingSystem("FASTER");
                config.setDepartmentAbbreviation("FLDOE");
                config.setStudentIdSystemName("Florida Student ID");
                config.setStudentIdLabel("Florida Student ID");
                config.setStudentIdFormat("##########");
                config.setStudentIdLength(10);
                config.setSpecialEdLabel("Exceptional Student Education");
                config.setReportingPeriodsPerYear(4);
            }
            case NM -> {
                config.setStateReportingSystem("STARS");
                config.setDepartmentAbbreviation("PED");
                config.setStudentIdSystemName("STARS ID");
                config.setStudentIdLabel("STARS Student ID");
                config.setReportingPeriodsPerYear(4);
            }
            default -> {
                config.setStateReportingSystem(state.name() + " State Reporting");
                config.setStudentIdLabel("State Student ID");
                config.setCourseCodeLabel("State Course Code");
            }
        }
    }
}
