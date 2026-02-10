package com.heronix.controller.api;

import com.heronix.service.AttendanceConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Attendance Configuration
 *
 * Manages system-wide attendance configuration settings and business rules.
 * Provides centralized configuration for:
 * - Attendance policies (grace periods, auto-absence marking)
 * - QR code settings (enabled status, expiry, rotation, photo requirements)
 * - Facial recognition settings (enabled status, confidence threshold, provider)
 * - Notification preferences (parent notifications on absence/tardy)
 * - Thresholds (chronic absence, tardy limits, alert triggers)
 *
 * Configuration Profiles:
 * - DEFAULT: Balanced settings for typical schools
 * - STRICT: Stricter thresholds for accountability-focused environments
 * - LENIENT: More flexible thresholds for alternative programs
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/attendance-configuration")
@RequiredArgsConstructor
public class AttendanceConfigurationApiController {

    private final AttendanceConfigurationService attendanceConfigurationService;

    // ==================== Configuration Retrieval ====================

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllConfiguration() {
        try {
            AttendanceConfigurationService.AttendanceConfiguration config =
                attendanceConfigurationService.getAllConfiguration();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("configuration", config);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Configuration Updates ====================

    @PutMapping("/qr-code")
    public ResponseEntity<Map<String, Object>> updateQrCodeSettings(
            @RequestBody AttendanceConfigurationService.QrCodeSettings settings) {

        try {
            AttendanceConfigurationService.QrCodeSettings updated =
                attendanceConfigurationService.updateQrCodeSettings(settings);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("settings", updated);
            response.put("message", "QR code settings updated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/facial-recognition")
    public ResponseEntity<Map<String, Object>> updateFacialRecognitionSettings(
            @RequestBody AttendanceConfigurationService.FacialRecognitionSettings settings) {

        try {
            AttendanceConfigurationService.FacialRecognitionSettings updated =
                attendanceConfigurationService.updateFacialRecognitionSettings(settings);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("settings", updated);
            response.put("message", "Facial recognition settings updated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/policy")
    public ResponseEntity<Map<String, Object>> updatePolicySettings(
            @RequestBody AttendanceConfigurationService.AttendancePolicySettings settings) {

        try {
            AttendanceConfigurationService.AttendancePolicySettings updated =
                attendanceConfigurationService.updatePolicySettings(settings);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("settings", updated);
            response.put("message", "Attendance policy settings updated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/thresholds")
    public ResponseEntity<Map<String, Object>> updateThresholdSettings(
            @RequestBody AttendanceConfigurationService.ThresholdSettings settings) {

        try {
            AttendanceConfigurationService.ThresholdSettings updated =
                attendanceConfigurationService.updateThresholdSettings(settings);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("settings", updated);
            response.put("message", "Threshold settings updated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Configuration Validation ====================

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateConfiguration(
            @RequestBody AttendanceConfigurationService.AttendanceConfiguration config) {

        try {
            AttendanceConfigurationService.ConfigurationValidationResult result =
                attendanceConfigurationService.validateConfiguration(config);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", result.isValid());
            response.put("validationResult", result);

            if (!result.isValid()) {
                response.put("errors", result.getErrors());
                response.put("warnings", result.getWarnings());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Configuration Profiles ====================

    @GetMapping("/profiles/default")
    public ResponseEntity<Map<String, Object>> getDefaultConfiguration() {
        try {
            AttendanceConfigurationService.AttendanceConfiguration config =
                attendanceConfigurationService.getDefaultConfiguration();

            Map<String, Object> response = new HashMap<>();
            response.put("profile", "DEFAULT");
            response.put("description", "Balanced settings for typical schools");
            response.put("configuration", config);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/profiles/strict")
    public ResponseEntity<Map<String, Object>> getStrictConfiguration() {
        try {
            AttendanceConfigurationService.AttendanceConfiguration config =
                attendanceConfigurationService.getStrictConfiguration();

            Map<String, Object> response = new HashMap<>();
            response.put("profile", "STRICT");
            response.put("description", "Stricter thresholds for accountability-focused environments");
            response.put("configuration", config);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/profiles/lenient")
    public ResponseEntity<Map<String, Object>> getLenientConfiguration() {
        try {
            AttendanceConfigurationService.AttendanceConfiguration config =
                attendanceConfigurationService.getLenientConfiguration();

            Map<String, Object> response = new HashMap<>();
            response.put("profile", "LENIENT");
            response.put("description", "More flexible thresholds for alternative programs");
            response.put("configuration", config);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            AttendanceConfigurationService.AttendanceConfiguration config =
                attendanceConfigurationService.getAllConfiguration();

            Map<String, Object> dashboard = new HashMap<>();

            dashboard.put("currentConfiguration", Map.of(
                "qrEnabled", config.getQrSettings() != null && config.getQrSettings().isEnabled(),
                "facialRecognitionEnabled", config.getFacialRecognitionSettings() != null &&
                    config.getFacialRecognitionSettings().isEnabled(),
                "autoMarkAbsentEnabled", config.getPolicySettings() != null &&
                    config.getPolicySettings().isAutoMarkAbsentEnabled(),
                "parentNotificationsEnabled", config.getNotificationSettings() != null &&
                    config.getNotificationSettings().isParentNotificationEnabled()
            ));

            dashboard.put("features", Map.of(
                "qrCodeAttendance", "Quick attendance check-in with QR codes",
                "facialRecognition", "AI-powered attendance verification",
                "automaticMarking", "Auto-mark students absent if not checked in",
                "parentNotifications", "Automated parent notifications for absences and tardies"
            ));

            dashboard.put("quickActions", Map.of(
                "viewAll", "GET /api/attendance-configuration/all",
                "updateQR", "PUT /api/attendance-configuration/qr-code",
                "updatePolicy", "PUT /api/attendance-configuration/policy",
                "validateConfig", "POST /api/attendance-configuration/validate"
            ));

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Configuration Reference ====================

    @GetMapping("/reference/settings")
    public ResponseEntity<Map<String, Object>> getSettingsReference() {
        Map<String, Object> reference = new HashMap<>();

        reference.put("qrCodeSettings", Map.of(
            "enabled", "Enable/disable QR code attendance",
            "duplicateWindowMinutes", "Time window to prevent duplicate scans",
            "requirePhoto", "Require photo capture with QR scan",
            "codeExpiryDays", "Days until QR code expires",
            "codeRotationDays", "Days between automatic QR code rotation"
        ));

        reference.put("facialRecognitionSettings", Map.of(
            "enabled", "Enable/disable facial recognition",
            "confidenceThreshold", "Minimum confidence score for match (0.0-1.0)",
            "provider", "Facial recognition provider (MOCK, AWS_REKOGNITION, etc.)"
        ));

        reference.put("policySettings", Map.of(
            "chronicAbsenceThreshold", "Percentage threshold for chronic absence (e.g., 0.10 = 10%)",
            "tardyGracePeriodMinutes", "Minutes after bell before marking tardy",
            "autoMarkAbsentEnabled", "Automatically mark students absent if not checked in"
        ));

        reference.put("thresholds", Map.of(
            "description", "Thresholds for attendance alerts and interventions",
            "chronicAbsence", "Typically 0.10 (10% of school days)",
            "tardyLimit", "Number of tardies before intervention",
            "absenceLimit", "Number of absences before intervention"
        ));

        reference.put("notificationSettings", Map.of(
            "parentNotificationEnabled", "Enable parent notifications",
            "notifyParentOnAbsence", "Send notification when student is absent",
            "notifyParentOnTardy", "Send notification when student is tardy"
        ));

        return ResponseEntity.ok(reference);
    }

    @GetMapping("/reference/profiles")
    public ResponseEntity<Map<String, Object>> getProfilesReference() {
        Map<String, Object> profiles = new HashMap<>();

        profiles.put("DEFAULT", Map.of(
            "name", "Default Configuration",
            "description", "Balanced settings suitable for most schools",
            "chronicAbsenceThreshold", "0.10 (10%)",
            "tardyGracePeriod", "10 minutes",
            "autoMarkAbsent", true,
            "qrEnabled", true,
            "facialRecognition", true
        ));

        profiles.put("STRICT", Map.of(
            "name", "Strict Configuration",
            "description", "Stricter thresholds for accountability-focused environments",
            "chronicAbsenceThreshold", "0.05 (5%)",
            "tardyGracePeriod", "5 minutes",
            "autoMarkAbsent", true,
            "requiresPhoto", true
        ));

        profiles.put("LENIENT", Map.of(
            "name", "Lenient Configuration",
            "description", "Flexible thresholds for alternative programs",
            "chronicAbsenceThreshold", "0.15 (15%)",
            "tardyGracePeriod", "15 minutes",
            "autoMarkAbsent", false
        ));

        return ResponseEntity.ok(profiles);
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "1.0.0");
        metadata.put("features", Map.of(
            "qrCodeAttendance", "QR code-based attendance check-in",
            "facialRecognition", "AI-powered facial recognition verification",
            "automaticMarking", "Auto-mark absent students",
            "parentNotifications", "Automated parent notifications",
            "configurationProfiles", "Pre-configured setting profiles (Default, Strict, Lenient)"
        ));

        metadata.put("configurationCategories", List.of(
            "QR Code Settings",
            "Facial Recognition Settings",
            "Attendance Policy Settings",
            "Threshold Settings",
            "Notification Settings"
        ));

        metadata.put("integrations", Map.of(
            "qrCodeGeneration", "QRCodeGenerationService",
            "facialRecognition", "FacialRecognitionService",
            "notifications", "AttendanceNotificationService"
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();

        help.put("title", "Attendance Configuration Help");

        help.put("workflows", Map.of(
            "initialSetup", List.of(
                "1. Choose a configuration profile (default, strict, or lenient)",
                "2. GET /api/attendance-configuration/profiles/{profileName}",
                "3. Customize settings as needed for your school",
                "4. Validate configuration: POST /api/attendance-configuration/validate",
                "5. Apply settings via individual PUT endpoints"
            ),
            "enableQrAttendance", List.of(
                "1. GET current QR settings: GET /api/attendance-configuration/all",
                "2. Update QR settings: PUT /api/attendance-configuration/qr-code",
                "3. Set enabled=true, configure expiry and rotation",
                "4. Generate QR codes for students"
            ),
            "adjustThresholds", List.of(
                "1. Review current thresholds: GET /api/attendance-configuration/all",
                "2. Update thresholds: PUT /api/attendance-configuration/thresholds",
                "3. Validate new configuration: POST /api/attendance-configuration/validate",
                "4. Monitor impact on attendance alerts"
            )
        ));

        help.put("endpoints", Map.of(
            "viewAll", "GET /api/attendance-configuration/all",
            "updateQR", "PUT /api/attendance-configuration/qr-code",
            "updateFacialRecognition", "PUT /api/attendance-configuration/facial-recognition",
            "updatePolicy", "PUT /api/attendance-configuration/policy",
            "updateThresholds", "PUT /api/attendance-configuration/thresholds",
            "validate", "POST /api/attendance-configuration/validate"
        ));

        help.put("examples", Map.of(
            "viewConfiguration", "curl http://localhost:9590/api/attendance-configuration/all",
            "getDefaultProfile", "curl http://localhost:9590/api/attendance-configuration/profiles/default",
            "dashboard", "curl http://localhost:9590/api/attendance-configuration/dashboard"
        ));

        return ResponseEntity.ok(help);
    }
}
