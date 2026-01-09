package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Template DTO
 *
 * Defines customizable templates for report generation.
 *
 * Template Types:
 * - Header/Footer templates
 * - Page layout templates
 * - Section templates
 * - Data table templates
 * - Chart templates
 *
 * Features:
 * - Variable substitution
 * - Conditional sections
 * - Loops and iterations
 * - Custom styling
 * - Branding elements
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 69 - Report Template System & Customization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTemplate {

    /**
     * Template type enumeration
     */
    public enum TemplateType {
        HEADER,             // Report header
        FOOTER,             // Report footer
        COVER_PAGE,         // Cover/title page
        TABLE_OF_CONTENTS,  // Table of contents
        DATA_TABLE,         // Data table section
        CHART,              // Chart section
        SUMMARY,            // Summary section
        DETAIL,             // Detail section
        FULL_REPORT,        // Complete report template
        CUSTOM              // Custom section
    }

    /**
     * Template format
     */
    public enum TemplateFormat {
        HTML,               // HTML template
        MARKDOWN,           // Markdown template
        THYMELEAF,          // Thymeleaf template
        FREEMARKER,         // FreeMarker template
        VELOCITY,           // Velocity template
        CUSTOM              // Custom format
    }

    /**
     * Template status
     */
    public enum TemplateStatus {
        DRAFT,              // In development
        ACTIVE,             // Currently active
        DEPRECATED,         // Old version
        ARCHIVED            // No longer used
    }

    // ============================================================
    // Basic Template Information
    // ============================================================

    /**
     * Template ID
     */
    private Long templateId;

    /**
     * Template name
     */
    private String templateName;

    /**
     * Description
     */
    private String description;

    /**
     * Template type
     */
    private TemplateType templateType;

    /**
     * Template format
     */
    private TemplateFormat templateFormat;

    /**
     * Template status
     */
    private TemplateStatus status;

    /**
     * Version number
     */
    private String version;

    // ============================================================
    // Template Content
    // ============================================================

    /**
     * Template content/markup
     */
    private String content;

    /**
     * Template CSS styles
     */
    private String styles;

    /**
     * Template JavaScript
     */
    private String scripts;

    /**
     * Additional resources (images, fonts, etc.)
     */
    private Map<String, String> resources;

    // ============================================================
    // Template Variables
    // ============================================================

    /**
     * Available template variables
     */
    private List<TemplateVariable> variables;

    /**
     * Default variable values
     */
    private Map<String, Object> defaultValues;

    /**
     * Required variables
     */
    private List<String> requiredVariables;

    // ============================================================
    // Layout Configuration
    // ============================================================

    /**
     * Page size (A4, Letter, Legal, etc.)
     */
    private String pageSize;

    /**
     * Page orientation (portrait, landscape)
     */
    private String orientation;

    /**
     * Margin settings (top, right, bottom, left in mm)
     */
    private Map<String, Integer> margins;

    /**
     * Header height (mm)
     */
    private Integer headerHeight;

    /**
     * Footer height (mm)
     */
    private Integer footerHeight;

    /**
     * Number of columns
     */
    private Integer columns;

    /**
     * Column spacing (mm)
     */
    private Integer columnSpacing;

    // ============================================================
    // Branding and Styling
    // ============================================================

    /**
     * Logo URL or path
     */
    private String logoUrl;

    /**
     * Primary color (hex)
     */
    private String primaryColor;

    /**
     * Secondary color (hex)
     */
    private String secondaryColor;

    /**
     * Accent color (hex)
     */
    private String accentColor;

    /**
     * Font family
     */
    private String fontFamily;

    /**
     * Font size (points)
     */
    private Integer fontSize;

    /**
     * Custom CSS classes
     */
    private List<String> customClasses;

    // ============================================================
    // Template Sections
    // ============================================================

    /**
     * Include header
     */
    private Boolean includeHeader;

    /**
     * Include footer
     */
    private Boolean includeFooter;

    /**
     * Include page numbers
     */
    private Boolean includePageNumbers;

    /**
     * Include timestamp
     */
    private Boolean includeTimestamp;

    /**
     * Include watermark
     */
    private Boolean includeWatermark;

    /**
     * Watermark text
     */
    private String watermarkText;

    // ============================================================
    // Conditional Rendering
    // ============================================================

    /**
     * Conditions for template rendering
     */
    private List<TemplateCondition> conditions;

    /**
     * Template sections (nested templates)
     */
    private List<TemplateSection> sections;

    // ============================================================
    // Template Metadata
    // ============================================================

    /**
     * Created by username
     */
    private String createdBy;

    /**
     * Created at timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Modified by username
     */
    private String modifiedBy;

    /**
     * Modified at timestamp
     */
    private LocalDateTime modifiedAt;

    /**
     * Is public template
     */
    private Boolean isPublic;

    /**
     * Is system template
     */
    private Boolean isSystem;

    /**
     * Category/tags
     */
    private List<String> tags;

    /**
     * Usage count
     */
    private Integer usageCount;

    /**
     * Parent template ID (for inheritance)
     */
    private Long parentTemplateId;

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Template variable definition
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateVariable {
        private String name;
        private String type;  // STRING, NUMBER, DATE, BOOLEAN, OBJECT, ARRAY
        private String description;
        private Boolean required;
        private Object defaultValue;
        private String format;  // For dates, numbers
        private List<String> allowedValues;  // For enums
    }

    /**
     * Template condition for conditional rendering
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateCondition {
        private String expression;  // Condition expression
        private String ifContent;   // Content if true
        private String elseContent; // Content if false
    }

    /**
     * Template section definition
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateSection {
        private String sectionName;
        private String sectionContent;
        private Integer order;
        private Boolean repeatable;
        private String iteratorVariable;
    }

    /**
     * Check if template is active
     */
    public boolean isActive() {
        return status == TemplateStatus.ACTIVE;
    }

    /**
     * Check if template is editable
     */
    public boolean isEditable() {
        return !Boolean.TRUE.equals(isSystem) && status != TemplateStatus.ARCHIVED;
    }

    /**
     * Get full template content with styles
     */
    public String getFullContent() {
        StringBuilder full = new StringBuilder();

        if (styles != null && !styles.isEmpty()) {
            full.append("<style>").append(styles).append("</style>");
        }

        if (content != null) {
            full.append(content);
        }

        if (scripts != null && !scripts.isEmpty()) {
            full.append("<script>").append(scripts).append("</script>");
        }

        return full.toString();
    }

    /**
     * Validate template
     */
    public void validate() {
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name is required");
        }

        if (templateType == null) {
            throw new IllegalArgumentException("Template type is required");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Template content is required");
        }

        // Check required variables are defined
        if (requiredVariables != null && variables != null) {
            List<String> definedVars = variables.stream()
                    .map(TemplateVariable::getName)
                    .toList();

            for (String required : requiredVariables) {
                if (!definedVars.contains(required)) {
                    throw new IllegalArgumentException("Required variable not defined: " + required);
                }
            }
        }
    }
}
