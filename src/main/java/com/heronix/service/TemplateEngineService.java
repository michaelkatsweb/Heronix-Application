package com.heronix.service;

import com.heronix.dto.ReportTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Template Engine Service
 *
 * Processes and renders report templates with variable substitution.
 *
 * Features:
 * - Variable substitution ${variable}
 * - Conditional rendering #if...#endif
 * - Loops #foreach...#endforeach
 * - Expression evaluation
 * - Nested templates
 *
 * Template Syntax:
 * - Variables: ${variableName}
 * - Conditionals: #if(condition)...#endif
 * - Loops: #foreach(item in collection)...#endforeach
 * - Comments: ##comment
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 69 - Report Template System & Customization
 */
@Service
@Slf4j
public class TemplateEngineService {

    // Template storage (in production, use database)
    private final Map<Long, ReportTemplate> templates = new ConcurrentHashMap<>();
    private Long nextTemplateId = 1L;

    /**
     * Render template with data
     */
    public String renderTemplate(ReportTemplate template, Map<String, Object> data) {
        log.info("Rendering template: {}", template.getTemplateName());

        try {
            // Validate template
            template.validate();

            // Merge default values with provided data
            Map<String, Object> mergedData = new HashMap<>();
            if (template.getDefaultValues() != null) {
                mergedData.putAll(template.getDefaultValues());
            }
            if (data != null) {
                mergedData.putAll(data);
            }

            // Add system variables
            addSystemVariables(mergedData);

            // Process template content
            String rendered = template.getContent();
            rendered = processComments(rendered);
            rendered = processConditionals(rendered, mergedData);
            rendered = processLoops(rendered, mergedData);
            rendered = processVariables(rendered, mergedData);

            return rendered;

        } catch (Exception e) {
            log.error("Error rendering template: {}", template.getTemplateName(), e);
            throw new RuntimeException("Template rendering failed: " + e.getMessage(), e);
        }
    }

    /**
     * Save template
     */
    public ReportTemplate saveTemplate(ReportTemplate template) {
        synchronized (this) {
            if (template.getTemplateId() == null) {
                template.setTemplateId(nextTemplateId++);
                template.setCreatedAt(LocalDateTime.now());
            }
            template.setModifiedAt(LocalDateTime.now());
            template.setUsageCount(template.getUsageCount() != null ? template.getUsageCount() : 0);

            templates.put(template.getTemplateId(), template);
            log.info("Saved template: {} (ID: {})", template.getTemplateName(), template.getTemplateId());

            return template;
        }
    }

    /**
     * Get template by ID
     */
    public Optional<ReportTemplate> getTemplate(Long templateId) {
        return Optional.ofNullable(templates.get(templateId));
    }

    /**
     * Get all templates
     */
    public List<ReportTemplate> getAllTemplates() {
        return new ArrayList<>(templates.values());
    }

    /**
     * Get templates by type
     */
    public List<ReportTemplate> getTemplatesByType(ReportTemplate.TemplateType type) {
        return templates.values().stream()
                .filter(t -> t.getTemplateType() == type)
                .toList();
    }

    /**
     * Get active templates
     */
    public List<ReportTemplate> getActiveTemplates() {
        return templates.values().stream()
                .filter(ReportTemplate::isActive)
                .toList();
    }

    /**
     * Delete template
     */
    public void deleteTemplate(Long templateId) {
        templates.remove(templateId);
        log.info("Deleted template: {}", templateId);
    }

    /**
     * Clone template
     */
    public ReportTemplate cloneTemplate(Long templateId, String newName) {
        ReportTemplate original = templates.get(templateId);
        if (original == null) {
            throw new IllegalArgumentException("Template not found: " + templateId);
        }

        ReportTemplate clone = ReportTemplate.builder()
                .templateName(newName)
                .description(original.getDescription() + " (Copy)")
                .templateType(original.getTemplateType())
                .templateFormat(original.getTemplateFormat())
                .status(ReportTemplate.TemplateStatus.DRAFT)
                .content(original.getContent())
                .styles(original.getStyles())
                .scripts(original.getScripts())
                .variables(original.getVariables())
                .defaultValues(original.getDefaultValues())
                .pageSize(original.getPageSize())
                .orientation(original.getOrientation())
                .margins(original.getMargins())
                .parentTemplateId(templateId)
                .build();

        return saveTemplate(clone);
    }

    // ============================================================
    // Template Processing Methods
    // ============================================================

    /**
     * Process comments (remove them)
     */
    private String processComments(String content) {
        return content.replaceAll("##.*?\n", "");
    }

    /**
     * Process variable substitutions ${variable}
     */
    private String processVariables(String content, Map<String, Object> data) {
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            Object value = resolveVariable(variableName, data);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Process conditional blocks #if(condition)...#endif
     */
    private String processConditionals(String content, Map<String, Object> data) {
        Pattern pattern = Pattern.compile("#if\\(([^)]+)\\)(.*?)#endif", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String condition = matcher.group(1).trim();
            String ifContent = matcher.group(2);

            boolean conditionResult = evaluateCondition(condition, data);
            String replacement = conditionResult ? ifContent : "";

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Process loop blocks #foreach(item in collection)...#endforeach
     */
    private String processLoops(String content, Map<String, Object> data) {
        Pattern pattern = Pattern.compile("#foreach\\(([^)]+)\\)(.*?)#endforeach", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String loopDef = matcher.group(1).trim();
            String loopContent = matcher.group(2);

            String[] parts = loopDef.split("\\s+in\\s+");
            if (parts.length != 2) {
                continue;
            }

            String itemVar = parts[0].trim();
            String collectionName = parts[1].trim();

            Object collection = data.get(collectionName);
            StringBuilder loopResult = new StringBuilder();

            if (collection instanceof List) {
                List<?> list = (List<?>) collection;
                for (int i = 0; i < list.size(); i++) {
                    Map<String, Object> loopData = new HashMap<>(data);
                    loopData.put(itemVar, list.get(i));
                    loopData.put(itemVar + "_index", i);
                    loopData.put(itemVar + "_first", i == 0);
                    loopData.put(itemVar + "_last", i == list.size() - 1);

                    String processedContent = processVariables(loopContent, loopData);
                    loopResult.append(processedContent);
                }
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(loopResult.toString()));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Resolve variable from data map (supports dot notation)
     */
    private Object resolveVariable(String variableName, Map<String, Object> data) {
        if (variableName.contains(".")) {
            String[] parts = variableName.split("\\.");
            Object current = data.get(parts[0]);

            for (int i = 1; i < parts.length && current != null; i++) {
                if (current instanceof Map) {
                    current = ((Map<?, ?>) current).get(parts[i]);
                } else {
                    try {
                        String methodName = "get" + capitalize(parts[i]);
                        current = current.getClass().getMethod(methodName).invoke(current);
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
            return current;
        }

        return data.get(variableName);
    }

    /**
     * Evaluate condition expression
     */
    private boolean evaluateCondition(String condition, Map<String, Object> data) {
        // Simple condition evaluation (can be enhanced with expression parser)
        condition = condition.trim();

        // Check for negation
        boolean negate = false;
        if (condition.startsWith("!")) {
            negate = true;
            condition = condition.substring(1).trim();
        }

        // Check for comparison operators
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            Object left = resolveVariable(parts[0].trim(), data);
            String right = parts[1].trim().replace("\"", "").replace("'", "");
            boolean result = left != null && left.toString().equals(right);
            return negate ? !result : result;
        }

        // Simple boolean check
        Object value = resolveVariable(condition, data);
        boolean result = value != null && (
                (value instanceof Boolean && (Boolean) value) ||
                        (!(value instanceof Boolean) && !value.toString().isEmpty())
        );

        return negate ? !result : result;
    }

    /**
     * Add system variables to data
     */
    private void addSystemVariables(Map<String, Object> data) {
        LocalDateTime now = LocalDateTime.now();
        data.put("currentDate", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        data.put("currentTime", now.format(DateTimeFormatter.ISO_LOCAL_TIME));
        data.put("currentDateTime", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        data.put("currentYear", String.valueOf(now.getYear()));
        data.put("currentMonth", now.getMonth().toString());
        data.put("systemName", "Heronix SIS");
    }

    /**
     * Capitalize first letter
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Get template statistics
     */
    public Map<String, Object> getTemplateStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTemplates", templates.size());
        stats.put("activeTemplates", templates.values().stream()
                .filter(ReportTemplate::isActive).count());
        stats.put("draftTemplates", templates.values().stream()
                .filter(t -> t.getStatus() == ReportTemplate.TemplateStatus.DRAFT).count());
        stats.put("publicTemplates", templates.values().stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsPublic())).count());

        return stats;
    }
}
