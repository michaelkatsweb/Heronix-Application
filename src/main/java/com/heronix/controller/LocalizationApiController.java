package com.heronix.controller;

import com.heronix.dto.LocaleConfig;
import com.heronix.service.LocalizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Localization API Controller
 *
 * REST API endpoints for localization and internationalization.
 *
 * Provides Endpoints For:
 * - Locale management
 * - Message translation
 * - Date/time formatting
 * - Number formatting
 * - Currency formatting
 *
 * Endpoints:
 * - GET /api/locales - Get all active locales
 * - GET /api/locales/{code} - Get specific locale
 * - GET /api/locales/default - Get default locale
 * - POST /api/locales - Register new locale
 * - POST /api/locales/format/date - Format date
 * - POST /api/locales/format/number - Format number
 * - POST /api/locales/format/currency - Format currency
 * - GET /api/locales/message/{key} - Get translated message
 * - GET /api/locales/stats - Get localization statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 70 - Report Internationalization & Localization
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/locales")
@RequiredArgsConstructor
@Slf4j
public class LocalizationApiController {

    private final LocalizationService localizationService;

    /**
     * Get all active locales
     *
     * @return List of active locales
     */
    @GetMapping
    public ResponseEntity<List<LocaleConfig>> getActiveLocales() {
        log.info("GET /api/locales");

        try {
            List<LocaleConfig> locales = localizationService.getActiveLocales();
            return ResponseEntity.ok(locales);

        } catch (Exception e) {
            log.error("Error fetching locales", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get specific locale by code
     *
     * @param code Locale code (e.g., en-US, es-ES)
     * @return Locale configuration
     */
    @GetMapping("/{code}")
    public ResponseEntity<LocaleConfig> getLocale(@PathVariable String code) {
        log.info("GET /api/locales/{}", code);

        try {
            LocaleConfig locale = localizationService.getLocale(code);
            return ResponseEntity.ok(locale);

        } catch (Exception e) {
            log.error("Error fetching locale: {}", code, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get default locale
     *
     * @return Default locale configuration
     */
    @GetMapping("/default")
    public ResponseEntity<LocaleConfig> getDefaultLocale() {
        log.info("GET /api/locales/default");

        try {
            LocaleConfig locale = localizationService.getDefaultLocale();
            return ResponseEntity.ok(locale);

        } catch (Exception e) {
            log.error("Error fetching default locale", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get supported locale codes
     *
     * @return Set of supported locale codes
     */
    @GetMapping("/codes")
    public ResponseEntity<Set<String>> getSupportedLocaleCodes() {
        log.info("GET /api/locales/codes");

        try {
            Set<String> codes = localizationService.getSupportedLocaleCodes();
            return ResponseEntity.ok(codes);

        } catch (Exception e) {
            log.error("Error fetching locale codes", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Register new locale
     *
     * @param locale Locale configuration
     * @return Success response
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> registerLocale(@RequestBody LocaleConfig locale) {
        log.info("POST /api/locales - Registering locale: {}", locale.getLocaleCode());

        try {
            locale.validate();
            localizationService.registerLocale(locale);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Locale registered successfully");
            response.put("localeCode", locale.getLocaleCode());
            response.put("displayName", locale.getDisplayName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid locale: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error registering locale", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================
    // Formatting Endpoints
    // ============================================================

    /**
     * Format date according to locale
     *
     * @param request Format request with date and locale code
     * @return Formatted date
     */
    @PostMapping("/format/date")
    public ResponseEntity<Map<String, Object>> formatDate(@RequestBody Map<String, String> request) {
        log.info("POST /api/locales/format/date");

        try {
            String dateStr = request.get("date");
            String localeCode = request.getOrDefault("localeCode", "en-US");

            LocalDate date = LocalDate.parse(dateStr);
            String formatted = localizationService.formatDate(date, localeCode);

            Map<String, Object> response = new HashMap<>();
            response.put("original", dateStr);
            response.put("formatted", formatted);
            response.put("localeCode", localeCode);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error formatting date", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Format datetime according to locale
     *
     * @param request Format request with datetime and locale code
     * @return Formatted datetime
     */
    @PostMapping("/format/datetime")
    public ResponseEntity<Map<String, Object>> formatDateTime(@RequestBody Map<String, String> request) {
        log.info("POST /api/locales/format/datetime");

        try {
            String dateTimeStr = request.get("datetime");
            String localeCode = request.getOrDefault("localeCode", "en-US");

            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr);
            String formatted = localizationService.formatDateTime(dateTime, localeCode);

            Map<String, Object> response = new HashMap<>();
            response.put("original", dateTimeStr);
            response.put("formatted", formatted);
            response.put("localeCode", localeCode);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error formatting datetime", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Format number according to locale
     *
     * @param request Format request with number and locale code
     * @return Formatted number
     */
    @PostMapping("/format/number")
    public ResponseEntity<Map<String, Object>> formatNumber(@RequestBody Map<String, Object> request) {
        log.info("POST /api/locales/format/number");

        try {
            Double number = Double.parseDouble(request.get("number").toString());
            String localeCode = (String) request.getOrDefault("localeCode", "en-US");

            String formatted = localizationService.formatNumber(number, localeCode);

            Map<String, Object> response = new HashMap<>();
            response.put("original", number);
            response.put("formatted", formatted);
            response.put("localeCode", localeCode);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error formatting number", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Format currency according to locale
     *
     * @param request Format request with amount and locale code
     * @return Formatted currency
     */
    @PostMapping("/format/currency")
    public ResponseEntity<Map<String, Object>> formatCurrency(@RequestBody Map<String, Object> request) {
        log.info("POST /api/locales/format/currency");

        try {
            Double amount = Double.parseDouble(request.get("amount").toString());
            String localeCode = (String) request.getOrDefault("localeCode", "en-US");

            String formatted = localizationService.formatCurrency(amount, localeCode);

            Map<String, Object> response = new HashMap<>();
            response.put("original", amount);
            response.put("formatted", formatted);
            response.put("localeCode", localeCode);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error formatting currency", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Format percentage according to locale
     *
     * @param request Format request with value and locale code
     * @return Formatted percentage
     */
    @PostMapping("/format/percent")
    public ResponseEntity<Map<String, Object>> formatPercent(@RequestBody Map<String, Object> request) {
        log.info("POST /api/locales/format/percent");

        try {
            Double value = Double.parseDouble(request.get("value").toString());
            String localeCode = (String) request.getOrDefault("localeCode", "en-US");

            String formatted = localizationService.formatPercent(value, localeCode);

            Map<String, Object> response = new HashMap<>();
            response.put("original", value);
            response.put("formatted", formatted);
            response.put("localeCode", localeCode);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error formatting percent", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================================
    // Translation Endpoints
    // ============================================================

    /**
     * Get translated message
     *
     * @param key Message key
     * @param localeCode Locale code (optional, defaults to en-US)
     * @return Translated message
     */
    @GetMapping("/message/{key}")
    public ResponseEntity<Map<String, Object>> getMessage(
            @PathVariable String key,
            @RequestParam(defaultValue = "en-US") String localeCode) {
        log.info("GET /api/locales/message/{} (locale: {})", key, localeCode);

        try {
            String message = localizationService.getMessage(key, localeCode);

            Map<String, Object> response = new HashMap<>();
            response.put("key", key);
            response.put("message", message);
            response.put("localeCode", localeCode);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching message: {}", key, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get multiple translated messages
     *
     * @param request Request with keys and locale code
     * @return Map of translated messages
     */
    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> getMessages(@RequestBody Map<String, Object> request) {
        log.info("POST /api/locales/messages");

        try {
            @SuppressWarnings("unchecked")
            List<String> keys = (List<String>) request.get("keys");
            String localeCode = (String) request.getOrDefault("localeCode", "en-US");

            Map<String, String> messages = new HashMap<>();
            for (String key : keys) {
                String message = localizationService.getMessage(key, localeCode);
                messages.put(key, message);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages);
            response.put("localeCode", localeCode);
            response.put("count", messages.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching messages", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get translated report type name
     *
     * @param reportType Report type
     * @param localeCode Locale code (optional)
     * @return Translated report type name
     */
    @GetMapping("/reporttype/{reportType}")
    public ResponseEntity<Map<String, Object>> getReportTypeName(
            @PathVariable String reportType,
            @RequestParam(defaultValue = "en-US") String localeCode) {
        log.info("GET /api/locales/reporttype/{} (locale: {})", reportType, localeCode);

        try {
            String translated = localizationService.getReportTypeName(reportType, localeCode);

            Map<String, Object> response = new HashMap<>();
            response.put("reportType", reportType);
            response.put("translatedName", translated);
            response.put("localeCode", localeCode);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error translating report type: {}", reportType, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get translated field name
     *
     * @param fieldKey Field key
     * @param localeCode Locale code (optional)
     * @return Translated field name
     */
    @GetMapping("/field/{fieldKey}")
    public ResponseEntity<Map<String, Object>> getFieldName(
            @PathVariable String fieldKey,
            @RequestParam(defaultValue = "en-US") String localeCode) {
        log.info("GET /api/locales/field/{} (locale: {})", fieldKey, localeCode);

        try {
            String translated = localizationService.getFieldName(fieldKey, localeCode);

            Map<String, Object> response = new HashMap<>();
            response.put("fieldKey", fieldKey);
            response.put("translatedName", translated);
            response.put("localeCode", localeCode);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error translating field: {}", fieldKey, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================
    // Report Formatting
    // ============================================================

    /**
     * Format GPA according to locale
     *
     * @param request Request with GPA and locale code
     * @return Formatted GPA
     */
    @PostMapping("/format/gpa")
    public ResponseEntity<Map<String, Object>> formatGPA(@RequestBody Map<String, Object> request) {
        log.info("POST /api/locales/format/gpa");

        try {
            Double gpa = Double.parseDouble(request.get("gpa").toString());
            String localeCode = (String) request.getOrDefault("localeCode", "en-US");

            String formatted = localizationService.formatGPA(gpa, localeCode);

            Map<String, Object> response = new HashMap<>();
            response.put("original", gpa);
            response.put("formatted", formatted);
            response.put("localeCode", localeCode);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error formatting GPA", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Format attendance according to locale
     *
     * @param request Request with present, total, and locale code
     * @return Formatted attendance
     */
    @PostMapping("/format/attendance")
    public ResponseEntity<Map<String, Object>> formatAttendance(@RequestBody Map<String, Object> request) {
        log.info("POST /api/locales/format/attendance");

        try {
            Integer present = Integer.parseInt(request.get("present").toString());
            Integer total = Integer.parseInt(request.get("total").toString());
            String localeCode = (String) request.getOrDefault("localeCode", "en-US");

            String formatted = localizationService.formatAttendance(present, total, localeCode);

            Map<String, Object> response = new HashMap<>();
            response.put("present", present);
            response.put("total", total);
            response.put("formatted", formatted);
            response.put("localeCode", localeCode);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error formatting attendance", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================================
    // Statistics
    // ============================================================

    /**
     * Get localization statistics
     *
     * @return Localization statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/locales/stats");

        try {
            Map<String, Object> stats = localizationService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching localization statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
