package com.heronix.service;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

/**
 * Attendance Configuration Service
 *
 * Manages system-wide attendance configuration settings and business rules.
 * Provides centralized configuration for attendance policies, thresholds, and automation.
 *
 * Key Responsibilities:
 * - Manage attendance policy settings
 * - Configure absence thresholds and alerts
 * - Set notification preferences
 * - Configure QR code settings
 * - Manage facial recognition settings
 * - Set chronic absence thresholds
 * - Configure attendance automation rules
 * - Manage school calendar and bell schedules
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Services Enhancement
 */
@Slf4j
@Service
public class AttendanceConfigurationService {

    // QR Code Settings
    @Value("${qr.attendance.enabled:true}")
    private boolean qrAttendanceEnabled;

    @Value("${qr.attendance.duplicate.window.minutes:5}")
    private int qrDuplicateWindowMinutes;

    @Value("${qr.attendance.require.photo:true}")
    private boolean qrRequirePhoto;

    @Value("${qr.code.expiry.days:365}")
    private int qrCodeExpiryDays;

    @Value("${qr.code.rotation.days:90}")
    private int qrCodeRotationDays;

    // Facial Recognition Settings
    @Value("${facial.recognition.enabled:true}")
    private boolean facialRecognitionEnabled;

    @Value("${facial.recognition.confidence.threshold:0.85}")
    private double facialRecognitionThreshold;

    @Value("${facial.recognition.provider:MOCK}")
    private String facialRecognitionProvider;

    // Attendance Policy Settings
    @Value("${attendance.chronic.absence.threshold:0.10}")
    private double chronicAbsenceThreshold;

    @Value("${attendance.tardy.grace.period.minutes:10}")
    private int tardyGracePeriodMinutes;

    @Value("${attendance.auto.mark.absent.enabled:true}")
    private boolean autoMarkAbsentEnabled;

    // Notification Settings
    @Value("${notification.parent.enabled:true}")
    private boolean parentNotificationEnabled;

    @Value("${notification.parent.on.absence:true}")
    private boolean notifyParentOnAbsence;

    @Value("${notification.parent.on.tardy:true}")
    private boolean notifyParentOnTardy;

    // ========================================================================
    // CONFIGURATION RETRIEVAL
    // ========================================================================

    /**
     * Get all attendance configuration settings
     */
    public AttendanceConfiguration getAllConfiguration() {
        log.info("Retrieving all attendance configuration settings");

        return AttendanceConfiguration.builder()
                .qrSettings(getQrCodeSettings())
                .facialRecognitionSettings(getFacialRecognitionSettings())
                .policySettings(getAttendancePolicySettings())
                .notificationSettings(getNotificationSettings())
                .thresholds(getThresholdSettings())
                .build();
    }

    /**
     * Get QR code configuration settings
     */
    public QrCodeSettings getQrCodeSettings() {
        return QrCodeSettings.builder()
                .enabled(qrAttendanceEnabled)
                .duplicateWindowMinutes(qrDuplicateWindowMinutes)
                .requirePhoto(qrRequirePhoto)
                .codeExpiryDays(qrCodeExpiryDays)
                .codeRotationDays(qrCodeRotationDays)
                .build();
    }

    /**
     * Get facial recognition configuration settings
     */
    public FacialRecognitionSettings getFacialRecognitionSettings() {
        return FacialRecognitionSettings.builder()
                .enabled(facialRecognitionEnabled)
                .confidenceThreshold(facialRecognitionThreshold)
                .provider(facialRecognitionProvider)
                .supportedProviders(Arrays.asList("MOCK", "OPENCV", "AWS", "AZURE", "GOOGLE"))
                .build();
    }

    /**
     * Get attendance policy configuration settings
     */
    public AttendancePolicySettings getAttendancePolicySettings() {
        return AttendancePolicySettings.builder()
                .tardyGracePeriodMinutes(tardyGracePeriodMinutes)
                .autoMarkAbsentEnabled(autoMarkAbsentEnabled)
                .schoolStartTime(LocalTime.of(8, 0))
                .schoolEndTime(LocalTime.of(15, 0))
                .build();
    }

    /**
     * Get notification configuration settings
     */
    public NotificationSettings getNotificationSettings() {
        return NotificationSettings.builder()
                .parentNotificationEnabled(parentNotificationEnabled)
                .notifyOnAbsence(notifyParentOnAbsence)
                .notifyOnTardy(notifyParentOnTardy)
                .build();
    }

    /**
     * Get threshold configuration settings
     */
    public ThresholdSettings getThresholdSettings() {
        return ThresholdSettings.builder()
                .chronicAbsenceThreshold(chronicAbsenceThreshold)
                .atRiskThreshold(0.85) // Below 85% attendance
                .perfectAttendanceThreshold(1.0)
                .build();
    }

    // ========================================================================
    // CONFIGURATION UPDATES
    // ========================================================================

    /**
     * Update QR code settings
     */
    public QrCodeSettings updateQrCodeSettings(QrCodeSettings settings) {
        log.info("Updating QR code settings");

        // In production, would save to database/configuration store
        // For now, just return the updated settings

        return settings;
    }

    /**
     * Update facial recognition settings
     */
    public FacialRecognitionSettings updateFacialRecognitionSettings(
            FacialRecognitionSettings settings) {
        log.info("Updating facial recognition settings");

        // Validate provider
        if (settings.getProvider() != null &&
            !settings.getSupportedProviders().contains(settings.getProvider())) {
            throw new IllegalArgumentException("Unsupported facial recognition provider: " +
                    settings.getProvider());
        }

        // Validate threshold
        if (settings.getConfidenceThreshold() < 0.0 || settings.getConfidenceThreshold() > 1.0) {
            throw new IllegalArgumentException("Confidence threshold must be between 0.0 and 1.0");
        }

        return settings;
    }

    /**
     * Update attendance policy settings
     */
    public AttendancePolicySettings updatePolicySettings(AttendancePolicySettings settings) {
        log.info("Updating attendance policy settings");

        return settings;
    }

    /**
     * Update threshold settings
     */
    public ThresholdSettings updateThresholdSettings(ThresholdSettings settings) {
        log.info("Updating threshold settings");

        // Validate thresholds
        if (settings.getChronicAbsenceThreshold() < 0.0 ||
            settings.getChronicAbsenceThreshold() > 1.0) {
            throw new IllegalArgumentException("Chronic absence threshold must be between 0.0 and 1.0");
        }

        return settings;
    }

    // ========================================================================
    // CONFIGURATION VALIDATION
    // ========================================================================

    /**
     * Validate configuration settings
     */
    public ConfigurationValidationResult validateConfiguration(AttendanceConfiguration config) {
        log.info("Validating attendance configuration");

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate QR settings
        if (config.getQrSettings() != null) {
            QrCodeSettings qr = config.getQrSettings();
            if (qr.getCodeExpiryDays() <= 0) {
                errors.add("QR code expiry days must be positive");
            }
            if (qr.getDuplicateWindowMinutes() < 1) {
                errors.add("Duplicate window must be at least 1 minute");
            }
        }

        // Validate facial recognition settings
        if (config.getFacialRecognitionSettings() != null) {
            FacialRecognitionSettings fr = config.getFacialRecognitionSettings();
            if (fr.getConfidenceThreshold() < 0.5) {
                warnings.add("Low facial recognition threshold may result in false positives");
            }
            if (fr.getConfidenceThreshold() > 0.95) {
                warnings.add("High facial recognition threshold may result in false negatives");
            }
        }

        // Validate thresholds
        if (config.getThresholds() != null) {
            ThresholdSettings t = config.getThresholds();
            if (t.getChronicAbsenceThreshold() > 0.20) {
                warnings.add("Chronic absence threshold is higher than typical 10-15%");
            }
        }

        boolean isValid = errors.isEmpty();

        return ConfigurationValidationResult.builder()
                .valid(isValid)
                .errors(errors)
                .warnings(warnings)
                .message(isValid ? "Configuration is valid" :
                        String.format("Configuration has %d error(s)", errors.size()))
                .build();
    }

    // ========================================================================
    // PRESET CONFIGURATIONS
    // ========================================================================

    /**
     * Get default configuration preset
     */
    public AttendanceConfiguration getDefaultConfiguration() {
        return AttendanceConfiguration.builder()
                .qrSettings(QrCodeSettings.builder()
                        .enabled(true)
                        .requirePhoto(true)
                        .duplicateWindowMinutes(5)
                        .codeExpiryDays(365)
                        .codeRotationDays(90)
                        .build())
                .facialRecognitionSettings(FacialRecognitionSettings.builder()
                        .enabled(true)
                        .confidenceThreshold(0.85)
                        .provider("MOCK")
                        .supportedProviders(Arrays.asList("MOCK", "OPENCV", "AWS", "AZURE", "GOOGLE"))
                        .build())
                .policySettings(AttendancePolicySettings.builder()
                        .tardyGracePeriodMinutes(10)
                        .autoMarkAbsentEnabled(true)
                        .schoolStartTime(LocalTime.of(8, 0))
                        .schoolEndTime(LocalTime.of(15, 0))
                        .build())
                .notificationSettings(NotificationSettings.builder()
                        .parentNotificationEnabled(true)
                        .notifyOnAbsence(true)
                        .notifyOnTardy(true)
                        .build())
                .thresholds(ThresholdSettings.builder()
                        .chronicAbsenceThreshold(0.10)
                        .atRiskThreshold(0.85)
                        .perfectAttendanceThreshold(1.0)
                        .build())
                .build();
    }

    /**
     * Get strict configuration preset
     */
    public AttendanceConfiguration getStrictConfiguration() {
        AttendanceConfiguration config = getDefaultConfiguration();
        config.getQrSettings().setRequirePhoto(true);
        config.getFacialRecognitionSettings().setConfidenceThreshold(0.90);
        config.getPolicySettings().setTardyGracePeriodMinutes(5);
        config.getThresholds().setChronicAbsenceThreshold(0.05);
        return config;
    }

    /**
     * Get lenient configuration preset
     */
    public AttendanceConfiguration getLenientConfiguration() {
        AttendanceConfiguration config = getDefaultConfiguration();
        config.getQrSettings().setRequirePhoto(false);
        config.getFacialRecognitionSettings().setConfidenceThreshold(0.75);
        config.getPolicySettings().setTardyGracePeriodMinutes(15);
        config.getThresholds().setChronicAbsenceThreshold(0.15);
        return config;
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class AttendanceConfiguration {
        private QrCodeSettings qrSettings;
        private FacialRecognitionSettings facialRecognitionSettings;
        private AttendancePolicySettings policySettings;
        private NotificationSettings notificationSettings;
        private ThresholdSettings thresholds;
    }

    @Data
    @Builder
    public static class QrCodeSettings {
        private boolean enabled;
        private int duplicateWindowMinutes;
        private boolean requirePhoto;
        private int codeExpiryDays;
        private int codeRotationDays;
    }

    @Data
    @Builder
    public static class FacialRecognitionSettings {
        private boolean enabled;
        private double confidenceThreshold;
        private String provider;
        private List<String> supportedProviders;
    }

    @Data
    @Builder
    public static class AttendancePolicySettings {
        private int tardyGracePeriodMinutes;
        private boolean autoMarkAbsentEnabled;
        private LocalTime schoolStartTime;
        private LocalTime schoolEndTime;
    }

    @Data
    @Builder
    public static class NotificationSettings {
        private boolean parentNotificationEnabled;
        private boolean notifyOnAbsence;
        private boolean notifyOnTardy;
    }

    @Data
    @Builder
    public static class ThresholdSettings {
        private double chronicAbsenceThreshold;
        private double atRiskThreshold;
        private double perfectAttendanceThreshold;
    }

    @Data
    @Builder
    public static class ConfigurationValidationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
        private String message;
    }
}
