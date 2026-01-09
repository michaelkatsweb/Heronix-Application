package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Locale Configuration DTO
 *
 * Defines internationalization and localization settings.
 *
 * Supports:
 * - Multiple languages
 * - Regional formats
 * - Date/time formatting
 * - Number formatting
 * - Currency formatting
 * - Custom translations
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 70 - Report Internationalization & Localization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocaleConfig {

    /**
     * Date format style
     */
    public enum DateFormat {
        SHORT,          // 1/1/24
        MEDIUM,         // Jan 1, 2024
        LONG,           // January 1, 2024
        FULL,           // Monday, January 1, 2024
        ISO,            // 2024-01-01
        CUSTOM          // Custom format string
    }

    /**
     * Time format style
     */
    public enum TimeFormat {
        SHORT,          // 1:30 PM
        MEDIUM,         // 1:30:45 PM
        LONG,           // 1:30:45 PM EST
        FULL,           // 1:30:45 PM Eastern Standard Time
        HOUR_24,        // 13:30
        ISO,            // 13:30:45
        CUSTOM          // Custom format string
    }

    /**
     * Number format style
     */
    public enum NumberFormat {
        DECIMAL,        // 1,234.56
        INTEGER,        // 1,234
        PERCENT,        // 12.34%
        SCIENTIFIC,     // 1.23E3
        CUSTOM          // Custom format string
    }

    /**
     * Currency format style
     */
    public enum CurrencyFormat {
        SYMBOL,         // $1,234.56
        CODE,           // USD 1,234.56
        NAME,           // 1,234.56 US Dollars
        CUSTOM          // Custom format string
    }

    // ============================================================
    // Basic Locale Information
    // ============================================================

    /**
     * Locale configuration ID
     */
    private Long localeId;

    /**
     * Language code (ISO 639-1)
     */
    private String languageCode;

    /**
     * Country code (ISO 3166-1)
     */
    private String countryCode;

    /**
     * Display name
     */
    private String displayName;

    /**
     * Native display name
     */
    private String nativeDisplayName;

    /**
     * Is default locale
     */
    private Boolean isDefault;

    /**
     * Active status
     */
    private Boolean active;

    /**
     * Is right-to-left language
     */
    private Boolean rightToLeft;

    // ============================================================
    // Date and Time Formatting
    // ============================================================

    /**
     * Date format style
     */
    private DateFormat dateFormat;

    /**
     * Custom date format pattern
     */
    private String customDatePattern;

    /**
     * Time format style
     */
    private TimeFormat timeFormat;

    /**
     * Custom time format pattern
     */
    private String customTimePattern;

    /**
     * Timezone ID
     */
    private String timezoneId;

    /**
     * Week starts on (MONDAY, SUNDAY, etc.)
     */
    private String weekStartDay;

    /**
     * Calendar system (GREGORIAN, ISLAMIC, etc.)
     */
    private String calendarSystem;

    // ============================================================
    // Number Formatting
    // ============================================================

    /**
     * Number format style
     */
    private NumberFormat numberFormat;

    /**
     * Custom number format pattern
     */
    private String customNumberPattern;

    /**
     * Decimal separator
     */
    private String decimalSeparator;

    /**
     * Thousands separator
     */
    private String thousandsSeparator;

    /**
     * Decimal places
     */
    private Integer decimalPlaces;

    /**
     * Use grouping (thousands separators)
     */
    private Boolean useGrouping;

    // ============================================================
    // Currency Formatting
    // ============================================================

    /**
     * Currency format style
     */
    private CurrencyFormat currencyFormat;

    /**
     * Custom currency format pattern
     */
    private String customCurrencyPattern;

    /**
     * Currency code (ISO 4217)
     */
    private String currencyCode;

    /**
     * Currency symbol
     */
    private String currencySymbol;

    /**
     * Currency symbol position (BEFORE, AFTER)
     */
    private String currencySymbolPosition;

    // ============================================================
    // Text Formatting
    // ============================================================

    /**
     * Text direction (LTR, RTL)
     */
    private String textDirection;

    /**
     * Text alignment default (LEFT, RIGHT, CENTER)
     */
    private String textAlignment;

    /**
     * Font family preference
     */
    private String fontFamily;

    /**
     * Character encoding
     */
    private String encoding;

    // ============================================================
    // Message Bundles
    // ============================================================

    /**
     * Message bundle base name
     */
    private String messageBundleName;

    /**
     * Fallback locale code
     */
    private String fallbackLocale;

    /**
     * Custom translations (key -> translated text)
     */
    private Map<String, String> customTranslations;

    /**
     * Supported report types with translations
     */
    private Map<String, String> reportTypeTranslations;

    /**
     * Field name translations
     */
    private Map<String, String> fieldNameTranslations;

    // ============================================================
    // Regional Settings
    // ============================================================

    /**
     * Paper size default (A4, Letter, Legal)
     */
    private String defaultPaperSize;

    /**
     * Measurement system (METRIC, IMPERIAL)
     */
    private String measurementSystem;

    /**
     * Address format
     */
    private String addressFormat;

    /**
     * Phone format
     */
    private String phoneFormat;

    /**
     * Postal code format
     */
    private String postalCodeFormat;

    // ============================================================
    // Report-Specific Settings
    // ============================================================

    /**
     * Report header format
     */
    private String reportHeaderFormat;

    /**
     * Report footer format
     */
    private String reportFooterFormat;

    /**
     * Grade display format
     */
    private String gradeDisplayFormat;

    /**
     * GPA format
     */
    private String gpaFormat;

    /**
     * Attendance format
     */
    private String attendanceFormat;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Created by
     */
    private String createdBy;

    /**
     * Created at
     */
    private LocalDateTime createdAt;

    /**
     * Modified by
     */
    private String modifiedBy;

    /**
     * Modified at
     */
    private LocalDateTime modifiedAt;

    /**
     * Usage count
     */
    private Integer usageCount;

    /**
     * Notes
     */
    private String notes;

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Get Java Locale object
     */
    public Locale toLocale() {
        if (countryCode != null && !countryCode.isEmpty()) {
            return new Locale(languageCode, countryCode);
        }
        return new Locale(languageCode);
    }

    /**
     * Get locale code (language-country)
     */
    public String getLocaleCode() {
        if (countryCode != null && !countryCode.isEmpty()) {
            return languageCode + "-" + countryCode;
        }
        return languageCode;
    }

    /**
     * Check if locale is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    /**
     * Check if this is default locale
     */
    public boolean isDefaultLocale() {
        return Boolean.TRUE.equals(isDefault);
    }

    /**
     * Check if language is right-to-left
     */
    public boolean isRightToLeft() {
        return Boolean.TRUE.equals(rightToLeft);
    }

    /**
     * Get effective date pattern
     */
    public String getEffectiveDatePattern() {
        if (dateFormat == DateFormat.CUSTOM && customDatePattern != null) {
            return customDatePattern;
        }

        return switch (dateFormat != null ? dateFormat : DateFormat.MEDIUM) {
            case SHORT -> "M/d/yy";
            case MEDIUM -> "MMM d, yyyy";
            case LONG -> "MMMM d, yyyy";
            case FULL -> "EEEE, MMMM d, yyyy";
            case ISO -> "yyyy-MM-dd";
            case CUSTOM -> customDatePattern != null ? customDatePattern : "MMM d, yyyy";
        };
    }

    /**
     * Get effective time pattern
     */
    public String getEffectiveTimePattern() {
        if (timeFormat == TimeFormat.CUSTOM && customTimePattern != null) {
            return customTimePattern;
        }

        return switch (timeFormat != null ? timeFormat : TimeFormat.SHORT) {
            case SHORT -> "h:mm a";
            case MEDIUM -> "h:mm:ss a";
            case LONG -> "h:mm:ss a z";
            case FULL -> "h:mm:ss a zzzz";
            case HOUR_24 -> "HH:mm";
            case ISO -> "HH:mm:ss";
            case CUSTOM -> customTimePattern != null ? customTimePattern : "h:mm a";
        };
    }

    /**
     * Get translated message
     */
    public String getMessage(String key, String defaultMessage) {
        if (customTranslations != null && customTranslations.containsKey(key)) {
            return customTranslations.get(key);
        }
        return defaultMessage;
    }

    /**
     * Get translated report type
     */
    public String getReportTypeName(String reportType, String defaultName) {
        if (reportTypeTranslations != null && reportTypeTranslations.containsKey(reportType)) {
            return reportTypeTranslations.get(reportType);
        }
        return defaultName;
    }

    /**
     * Get translated field name
     */
    public String getFieldName(String fieldKey, String defaultName) {
        if (fieldNameTranslations != null && fieldNameTranslations.containsKey(fieldKey)) {
            return fieldNameTranslations.get(fieldKey);
        }
        return defaultName;
    }

    /**
     * Validate locale configuration
     */
    public void validate() {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Language code is required");
        }

        if (languageCode.length() != 2) {
            throw new IllegalArgumentException("Language code must be 2 characters (ISO 639-1)");
        }

        if (countryCode != null && !countryCode.isEmpty() && countryCode.length() != 2) {
            throw new IllegalArgumentException("Country code must be 2 characters (ISO 3166-1)");
        }

        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name is required");
        }
    }

    /**
     * Create default locale (English-US)
     */
    public static LocaleConfig createDefault() {
        return LocaleConfig.builder()
                .languageCode("en")
                .countryCode("US")
                .displayName("English (United States)")
                .nativeDisplayName("English (United States)")
                .isDefault(true)
                .active(true)
                .rightToLeft(false)
                .dateFormat(DateFormat.MEDIUM)
                .timeFormat(TimeFormat.SHORT)
                .numberFormat(NumberFormat.DECIMAL)
                .currencyFormat(CurrencyFormat.SYMBOL)
                .currencyCode("USD")
                .currencySymbol("$")
                .currencySymbolPosition("BEFORE")
                .decimalSeparator(".")
                .thousandsSeparator(",")
                .decimalPlaces(2)
                .useGrouping(true)
                .textDirection("LTR")
                .textAlignment("LEFT")
                .encoding("UTF-8")
                .defaultPaperSize("Letter")
                .measurementSystem("IMPERIAL")
                .weekStartDay("SUNDAY")
                .calendarSystem("GREGORIAN")
                .timezoneId("America/New_York")
                .build();
    }
}
