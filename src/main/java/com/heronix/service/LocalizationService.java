package com.heronix.service;

import com.heronix.dto.LocaleConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Localization Service
 *
 * Provides internationalization and localization services for reports.
 *
 * Features:
 * - Multi-language support
 * - Date/time formatting
 * - Number formatting
 * - Currency formatting
 * - Message translation
 * - Regional settings
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 70 - Report Internationalization & Localization
 */
@Service
@Slf4j
public class LocalizationService {

    // Locale storage (in production, use database)
    private final Map<String, LocaleConfig> locales = new ConcurrentHashMap<>();
    private LocaleConfig defaultLocale;

    // Message bundles cache
    private final Map<String, Map<String, String>> messageBundles = new ConcurrentHashMap<>();

    public LocalizationService() {
        // Initialize with default locale
        defaultLocale = LocaleConfig.createDefault();
        locales.put(defaultLocale.getLocaleCode(), defaultLocale);

        // Initialize common locales
        initializeCommonLocales();

        // Load message bundles
        loadMessageBundles();
    }

    // ============================================================
    // Locale Management
    // ============================================================

    /**
     * Register locale configuration
     */
    public void registerLocale(LocaleConfig localeConfig) {
        localeConfig.validate();
        locales.put(localeConfig.getLocaleCode(), localeConfig);

        if (Boolean.TRUE.equals(localeConfig.getIsDefault())) {
            defaultLocale = localeConfig;
        }

        log.info("Registered locale: {} ({})", localeConfig.getDisplayName(), localeConfig.getLocaleCode());
    }

    /**
     * Get locale by code
     */
    public LocaleConfig getLocale(String localeCode) {
        return locales.getOrDefault(localeCode, defaultLocale);
    }

    /**
     * Get default locale
     */
    public LocaleConfig getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Get all active locales
     */
    public List<LocaleConfig> getActiveLocales() {
        return locales.values().stream()
                .filter(LocaleConfig::isActive)
                .toList();
    }

    /**
     * Get all supported locale codes
     */
    public Set<String> getSupportedLocaleCodes() {
        return new HashSet<>(locales.keySet());
    }

    // ============================================================
    // Date/Time Formatting
    // ============================================================

    /**
     * Format date according to locale
     */
    public String formatDate(LocalDate date, String localeCode) {
        LocaleConfig locale = getLocale(localeCode);
        String pattern = locale.getEffectiveDatePattern();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale.toLocale());
            return date.format(formatter);
        } catch (Exception e) {
            log.error("Error formatting date for locale {}: {}", localeCode, e.getMessage());
            return date.toString();
        }
    }

    /**
     * Format datetime according to locale
     */
    public String formatDateTime(LocalDateTime dateTime, String localeCode) {
        LocaleConfig locale = getLocale(localeCode);
        String datePattern = locale.getEffectiveDatePattern();
        String timePattern = locale.getEffectiveTimePattern();
        String pattern = datePattern + " " + timePattern;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale.toLocale());
            return dateTime.format(formatter);
        } catch (Exception e) {
            log.error("Error formatting datetime for locale {}: {}", localeCode, e.getMessage());
            return dateTime.toString();
        }
    }

    /**
     * Format time according to locale
     */
    public String formatTime(LocalDateTime time, String localeCode) {
        LocaleConfig locale = getLocale(localeCode);
        String pattern = locale.getEffectiveTimePattern();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale.toLocale());
            return time.format(formatter);
        } catch (Exception e) {
            log.error("Error formatting time for locale {}: {}", localeCode, e.getMessage());
            return time.toLocalTime().toString();
        }
    }

    /**
     * Get timezone for locale
     */
    public ZoneId getTimezone(String localeCode) {
        LocaleConfig locale = getLocale(localeCode);
        String timezoneId = locale.getTimezoneId();

        if (timezoneId != null && !timezoneId.isEmpty()) {
            return ZoneId.of(timezoneId);
        }

        return ZoneId.systemDefault();
    }

    // ============================================================
    // Number Formatting
    // ============================================================

    /**
     * Format number according to locale
     */
    public String formatNumber(Number number, String localeCode) {
        LocaleConfig locale = getLocale(localeCode);

        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale.toLocale());

            if (locale.getDecimalSeparator() != null) {
                symbols.setDecimalSeparator(locale.getDecimalSeparator().charAt(0));
            }
            if (locale.getThousandsSeparator() != null) {
                symbols.setGroupingSeparator(locale.getThousandsSeparator().charAt(0));
            }

            DecimalFormat formatter = new DecimalFormat();
            formatter.setDecimalFormatSymbols(symbols);
            formatter.setGroupingUsed(Boolean.TRUE.equals(locale.getUseGrouping()));

            if (locale.getDecimalPlaces() != null) {
                formatter.setMinimumFractionDigits(locale.getDecimalPlaces());
                formatter.setMaximumFractionDigits(locale.getDecimalPlaces());
            }

            return formatter.format(number);

        } catch (Exception e) {
            log.error("Error formatting number for locale {}: {}", localeCode, e.getMessage());
            return number.toString();
        }
    }

    /**
     * Format decimal number
     */
    public String formatDecimal(double value, String localeCode) {
        return formatNumber(value, localeCode);
    }

    /**
     * Format integer
     */
    public String formatInteger(long value, String localeCode) {
        LocaleConfig locale = getLocale(localeCode);

        try {
            NumberFormat formatter = NumberFormat.getIntegerInstance(locale.toLocale());
            formatter.setGroupingUsed(Boolean.TRUE.equals(locale.getUseGrouping()));
            return formatter.format(value);

        } catch (Exception e) {
            log.error("Error formatting integer for locale {}: {}", localeCode, e.getMessage());
            return String.valueOf(value);
        }
    }

    /**
     * Format percentage
     */
    public String formatPercent(double value, String localeCode) {
        LocaleConfig locale = getLocale(localeCode);

        try {
            NumberFormat formatter = NumberFormat.getPercentInstance(locale.toLocale());
            if (locale.getDecimalPlaces() != null) {
                formatter.setMinimumFractionDigits(locale.getDecimalPlaces());
                formatter.setMaximumFractionDigits(locale.getDecimalPlaces());
            }
            return formatter.format(value);

        } catch (Exception e) {
            log.error("Error formatting percent for locale {}: {}", localeCode, e.getMessage());
            return (value * 100) + "%";
        }
    }

    // ============================================================
    // Currency Formatting
    // ============================================================

    /**
     * Format currency according to locale
     */
    public String formatCurrency(double amount, String localeCode) {
        LocaleConfig locale = getLocale(localeCode);

        try {
            String formatted = formatNumber(amount, localeCode);
            String symbol = locale.getCurrencySymbol() != null ? locale.getCurrencySymbol() : "$";

            if ("BEFORE".equals(locale.getCurrencySymbolPosition())) {
                return symbol + formatted;
            } else {
                return formatted + " " + symbol;
            }

        } catch (Exception e) {
            log.error("Error formatting currency for locale {}: {}", localeCode, e.getMessage());
            return "$" + amount;
        }
    }

    /**
     * Format currency with code
     */
    public String formatCurrencyWithCode(double amount, String localeCode) {
        LocaleConfig locale = getLocale(localeCode);
        String formatted = formatNumber(amount, localeCode);
        String code = locale.getCurrencyCode() != null ? locale.getCurrencyCode() : "USD";

        return code + " " + formatted;
    }

    // ============================================================
    // Message Translation
    // ============================================================

    /**
     * Get translated message
     */
    public String getMessage(String key, String localeCode) {
        return getMessage(key, localeCode, key);
    }

    /**
     * Get translated message with default
     */
    public String getMessage(String key, String localeCode, String defaultMessage) {
        // Check custom translations first
        LocaleConfig locale = getLocale(localeCode);
        String customMessage = locale.getMessage(key, null);
        if (customMessage != null) {
            return customMessage;
        }

        // Check message bundles
        Map<String, String> bundle = messageBundles.get(localeCode);
        if (bundle != null && bundle.containsKey(key)) {
            return bundle.get(key);
        }

        // Fallback to default locale
        if (!localeCode.equals(defaultLocale.getLocaleCode())) {
            bundle = messageBundles.get(defaultLocale.getLocaleCode());
            if (bundle != null && bundle.containsKey(key)) {
                return bundle.get(key);
            }
        }

        return defaultMessage;
    }

    /**
     * Get translated message with parameters
     */
    public String getMessage(String key, String localeCode, Object... params) {
        String message = getMessage(key, localeCode);

        try {
            return String.format(message, params);
        } catch (Exception e) {
            log.error("Error formatting message with params: {}", e.getMessage());
            return message;
        }
    }

    /**
     * Get translated report type name
     */
    public String getReportTypeName(String reportType, String localeCode) {
        LocaleConfig locale = getLocale(localeCode);
        return locale.getReportTypeName(reportType, reportType);
    }

    /**
     * Get translated field name
     */
    public String getFieldName(String fieldKey, String localeCode) {
        LocaleConfig locale = getLocale(localeCode);
        return locale.getFieldName(fieldKey, fieldKey);
    }

    // ============================================================
    // Format Report Data
    // ============================================================

    /**
     * Format GPA according to locale
     */
    public String formatGPA(double gpa, String localeCode) {
        LocaleConfig locale = getLocale(localeCode);
        String format = locale.getGpaFormat();

        if (format != null && format.contains("out of")) {
            return formatDecimal(gpa, localeCode) + " " + getMessage("gpa.outof", localeCode, "out of 4.0");
        }

        return formatDecimal(gpa, localeCode);
    }

    /**
     * Format grade according to locale
     */
    public String formatGrade(String grade, String localeCode) {
        LocaleConfig locale = getLocale(localeCode);
        String format = locale.getGradeDisplayFormat();

        // Allow custom grade translation
        String translatedGrade = getMessage("grade." + grade, localeCode, grade);
        return translatedGrade;
    }

    /**
     * Format attendance according to locale
     */
    public String formatAttendance(int present, int total, String localeCode) {
        LocaleConfig locale = getLocale(localeCode);
        String format = locale.getAttendanceFormat();

        if ("PERCENTAGE".equals(format)) {
            double percentage = (double) present / total;
            return formatPercent(percentage, localeCode);
        }

        return formatInteger(present, localeCode) + "/" + formatInteger(total, localeCode);
    }

    // ============================================================
    // Initialization
    // ============================================================

    /**
     * Initialize common locales
     */
    private void initializeCommonLocales() {
        // Spanish (Spain)
        registerLocale(LocaleConfig.builder()
                .languageCode("es")
                .countryCode("ES")
                .displayName("Spanish (Spain)")
                .nativeDisplayName("Español (España)")
                .active(true)
                .rightToLeft(false)
                .dateFormat(LocaleConfig.DateFormat.MEDIUM)
                .timeFormat(LocaleConfig.TimeFormat.HOUR_24)
                .numberFormat(LocaleConfig.NumberFormat.DECIMAL)
                .currencyFormat(LocaleConfig.CurrencyFormat.SYMBOL)
                .currencyCode("EUR")
                .currencySymbol("€")
                .currencySymbolPosition("AFTER")
                .decimalSeparator(",")
                .thousandsSeparator(".")
                .decimalPlaces(2)
                .useGrouping(true)
                .textDirection("LTR")
                .textAlignment("LEFT")
                .encoding("UTF-8")
                .defaultPaperSize("A4")
                .measurementSystem("METRIC")
                .weekStartDay("MONDAY")
                .timezoneId("Europe/Madrid")
                .build());

        // French (France)
        registerLocale(LocaleConfig.builder()
                .languageCode("fr")
                .countryCode("FR")
                .displayName("French (France)")
                .nativeDisplayName("Français (France)")
                .active(true)
                .rightToLeft(false)
                .dateFormat(LocaleConfig.DateFormat.MEDIUM)
                .timeFormat(LocaleConfig.TimeFormat.HOUR_24)
                .numberFormat(LocaleConfig.NumberFormat.DECIMAL)
                .currencyFormat(LocaleConfig.CurrencyFormat.SYMBOL)
                .currencyCode("EUR")
                .currencySymbol("€")
                .currencySymbolPosition("AFTER")
                .decimalSeparator(",")
                .thousandsSeparator(" ")
                .decimalPlaces(2)
                .useGrouping(true)
                .textDirection("LTR")
                .textAlignment("LEFT")
                .encoding("UTF-8")
                .defaultPaperSize("A4")
                .measurementSystem("METRIC")
                .weekStartDay("MONDAY")
                .timezoneId("Europe/Paris")
                .build());

        // German (Germany)
        registerLocale(LocaleConfig.builder()
                .languageCode("de")
                .countryCode("DE")
                .displayName("German (Germany)")
                .nativeDisplayName("Deutsch (Deutschland)")
                .active(true)
                .rightToLeft(false)
                .dateFormat(LocaleConfig.DateFormat.MEDIUM)
                .timeFormat(LocaleConfig.TimeFormat.HOUR_24)
                .numberFormat(LocaleConfig.NumberFormat.DECIMAL)
                .currencyFormat(LocaleConfig.CurrencyFormat.SYMBOL)
                .currencyCode("EUR")
                .currencySymbol("€")
                .currencySymbolPosition("AFTER")
                .decimalSeparator(",")
                .thousandsSeparator(".")
                .decimalPlaces(2)
                .useGrouping(true)
                .textDirection("LTR")
                .textAlignment("LEFT")
                .encoding("UTF-8")
                .defaultPaperSize("A4")
                .measurementSystem("METRIC")
                .weekStartDay("MONDAY")
                .timezoneId("Europe/Berlin")
                .build());

        // Chinese (China)
        registerLocale(LocaleConfig.builder()
                .languageCode("zh")
                .countryCode("CN")
                .displayName("Chinese (China)")
                .nativeDisplayName("中文（中国）")
                .active(true)
                .rightToLeft(false)
                .dateFormat(LocaleConfig.DateFormat.ISO)
                .timeFormat(LocaleConfig.TimeFormat.HOUR_24)
                .numberFormat(LocaleConfig.NumberFormat.DECIMAL)
                .currencyFormat(LocaleConfig.CurrencyFormat.SYMBOL)
                .currencyCode("CNY")
                .currencySymbol("¥")
                .currencySymbolPosition("BEFORE")
                .decimalSeparator(".")
                .thousandsSeparator(",")
                .decimalPlaces(2)
                .useGrouping(true)
                .textDirection("LTR")
                .textAlignment("LEFT")
                .encoding("UTF-8")
                .defaultPaperSize("A4")
                .measurementSystem("METRIC")
                .weekStartDay("MONDAY")
                .timezoneId("Asia/Shanghai")
                .build());

        // Arabic (Saudi Arabia)
        registerLocale(LocaleConfig.builder()
                .languageCode("ar")
                .countryCode("SA")
                .displayName("Arabic (Saudi Arabia)")
                .nativeDisplayName("العربية (المملكة العربية السعودية)")
                .active(true)
                .rightToLeft(true)
                .dateFormat(LocaleConfig.DateFormat.MEDIUM)
                .timeFormat(LocaleConfig.TimeFormat.HOUR_24)
                .numberFormat(LocaleConfig.NumberFormat.DECIMAL)
                .currencyFormat(LocaleConfig.CurrencyFormat.SYMBOL)
                .currencyCode("SAR")
                .currencySymbol("﷼")
                .currencySymbolPosition("BEFORE")
                .decimalSeparator(".")
                .thousandsSeparator(",")
                .decimalPlaces(2)
                .useGrouping(true)
                .textDirection("RTL")
                .textAlignment("RIGHT")
                .encoding("UTF-8")
                .defaultPaperSize("A4")
                .measurementSystem("METRIC")
                .weekStartDay("SATURDAY")
                .timezoneId("Asia/Riyadh")
                .build());
    }

    /**
     * Load message bundles
     */
    private void loadMessageBundles() {
        // English messages
        Map<String, String> enMessages = new HashMap<>();
        enMessages.put("report.title", "Report");
        enMessages.put("report.generated", "Generated");
        enMessages.put("report.date", "Date");
        enMessages.put("student.name", "Student Name");
        enMessages.put("student.id", "Student ID");
        enMessages.put("grade", "Grade");
        enMessages.put("gpa", "GPA");
        enMessages.put("gpa.outof", "out of 4.0");
        enMessages.put("attendance", "Attendance");
        enMessages.put("course", "Course");
        enMessages.put("teacher", "Teacher");
        messageBundles.put("en-US", enMessages);

        // Spanish messages
        Map<String, String> esMessages = new HashMap<>();
        esMessages.put("report.title", "Informe");
        esMessages.put("report.generated", "Generado");
        esMessages.put("report.date", "Fecha");
        esMessages.put("student.name", "Nombre del Estudiante");
        esMessages.put("student.id", "ID de Estudiante");
        esMessages.put("grade", "Calificación");
        esMessages.put("gpa", "Promedio");
        esMessages.put("gpa.outof", "de 4.0");
        esMessages.put("attendance", "Asistencia");
        esMessages.put("course", "Curso");
        esMessages.put("teacher", "Profesor");
        messageBundles.put("es-ES", esMessages);

        // French messages
        Map<String, String> frMessages = new HashMap<>();
        frMessages.put("report.title", "Rapport");
        frMessages.put("report.generated", "Généré");
        frMessages.put("report.date", "Date");
        frMessages.put("student.name", "Nom de l'étudiant");
        frMessages.put("student.id", "ID étudiant");
        frMessages.put("grade", "Note");
        frMessages.put("gpa", "Moyenne");
        frMessages.put("gpa.outof", "sur 4.0");
        frMessages.put("attendance", "Présence");
        frMessages.put("course", "Cours");
        frMessages.put("teacher", "Professeur");
        messageBundles.put("fr-FR", frMessages);

        log.info("Loaded message bundles for {} locales", messageBundles.size());
    }

    /**
     * Get localization statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLocales", locales.size());
        stats.put("activeLocales", getActiveLocales().size());
        stats.put("defaultLocale", defaultLocale.getLocaleCode());
        stats.put("messageBundles", messageBundles.size());

        return stats;
    }
}
