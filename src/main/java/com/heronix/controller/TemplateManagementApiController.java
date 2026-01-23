package com.heronix.controller;

import com.heronix.dto.ReportTemplate;
import com.heronix.service.TemplateEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Template Management API Controller
 *
 * REST API endpoints for managing report templates.
 *
 * Provides Endpoints For:
 * - Template CRUD operations
 * - Template rendering
 * - Template cloning
 * - Template statistics
 *
 * Endpoints:
 * - GET /api/templates - Get all templates
 * - GET /api/templates/{id} - Get template by ID
 * - GET /api/templates/type/{type} - Get templates by type
 * - GET /api/templates/active - Get active templates
 * - POST /api/templates - Create new template
 * - PUT /api/templates/{id} - Update template
 * - DELETE /api/templates/{id} - Delete template
 * - POST /api/templates/{id}/clone - Clone template
 * - POST /api/templates/{id}/render - Render template
 * - GET /api/templates/stats - Get template statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 69 - Report Template System & Customization
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@Slf4j
public class TemplateManagementApiController {

    private final TemplateEngineService templateService;

    /**
     * Get all templates
     *
     * @return List of all templates
     */
    @GetMapping
    public ResponseEntity<List<ReportTemplate>> getAllTemplates() {
        log.info("GET /api/templates");

        try {
            List<ReportTemplate> templates = templateService.getAllTemplates();
            return ResponseEntity.ok(templates);

        } catch (Exception e) {
            log.error("Error fetching templates", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get template by ID
     *
     * @param id Template ID
     * @return Template if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportTemplate> getTemplate(@PathVariable Long id) {
        log.info("GET /api/templates/{}", id);

        try {
            return templateService.getTemplate(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching template: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get templates by type
     *
     * @param type Template type
     * @return List of templates of specified type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ReportTemplate>> getTemplatesByType(@PathVariable String type) {
        log.info("GET /api/templates/type/{}", type);

        try {
            ReportTemplate.TemplateType templateType = ReportTemplate.TemplateType.valueOf(type.toUpperCase());
            List<ReportTemplate> templates = templateService.getTemplatesByType(templateType);
            return ResponseEntity.ok(templates);

        } catch (IllegalArgumentException e) {
            log.error("Invalid template type: {}", type);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error fetching templates by type: {}", type, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get active templates
     *
     * @return List of active templates
     */
    @GetMapping("/active")
    public ResponseEntity<List<ReportTemplate>> getActiveTemplates() {
        log.info("GET /api/templates/active");

        try {
            List<ReportTemplate> templates = templateService.getActiveTemplates();
            return ResponseEntity.ok(templates);

        } catch (Exception e) {
            log.error("Error fetching active templates", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create new template
     *
     * @param template Template to create
     * @return Created template
     */
    @PostMapping
    public ResponseEntity<ReportTemplate> createTemplate(@RequestBody ReportTemplate template) {
        log.info("POST /api/templates - Creating template: {}", template.getTemplateName());

        try {
            // Validate template
            template.validate();

            // Save template
            ReportTemplate saved = templateService.saveTemplate(template);
            return ResponseEntity.ok(saved);

        } catch (IllegalArgumentException e) {
            log.error("Invalid template: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error creating template", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update template
     *
     * @param id Template ID
     * @param template Updated template data
     * @return Updated template
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReportTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody ReportTemplate template) {
        log.info("PUT /api/templates/{} - Updating template", id);

        try {
            // Check if template exists
            if (templateService.getTemplate(id).isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Validate template
            template.setTemplateId(id);
            template.validate();

            // Save template
            ReportTemplate updated = templateService.saveTemplate(template);
            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            log.error("Invalid template: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error updating template: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete template
     *
     * @param id Template ID
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(@PathVariable Long id) {
        log.info("DELETE /api/templates/{}", id);

        try {
            // Check if template exists
            if (templateService.getTemplate(id).isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Delete template
            templateService.deleteTemplate(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Template deleted successfully");
            response.put("templateId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting template: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Clone template
     *
     * @param id Template ID to clone
     * @param request Clone request with new name
     * @return Cloned template
     */
    @PostMapping("/{id}/clone")
    public ResponseEntity<ReportTemplate> cloneTemplate(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/templates/{}/clone", id);

        try {
            String newName = request.get("newName");
            if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            ReportTemplate cloned = templateService.cloneTemplate(id, newName);
            return ResponseEntity.ok(cloned);

        } catch (IllegalArgumentException e) {
            log.error("Template not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error cloning template: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Render template with data
     *
     * @param id Template ID
     * @param data Data for rendering
     * @return Rendered template content
     */
    @PostMapping("/{id}/render")
    public ResponseEntity<Map<String, Object>> renderTemplate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> data) {
        log.info("POST /api/templates/{}/render", id);

        try {
            // Get template
            ReportTemplate template = templateService.getTemplate(id)
                    .orElseThrow(() -> new IllegalArgumentException("Template not found: " + id));

            // Render template
            String rendered = templateService.renderTemplate(template, data);

            // Increment usage count
            template.setUsageCount(template.getUsageCount() != null ? template.getUsageCount() + 1 : 1);
            templateService.saveTemplate(template);

            Map<String, Object> response = new HashMap<>();
            response.put("templateId", id);
            response.put("templateName", template.getTemplateName());
            response.put("renderedContent", rendered);
            response.put("format", template.getTemplateFormat());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Template not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error rendering template: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Preview template rendering
     *
     * @param request Template and data for preview
     * @return Rendered preview
     */
    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewTemplate(
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/templates/preview");

        try {
            // Extract template and data from request
            @SuppressWarnings("unchecked")
            Map<String, Object> templateData = (Map<String, Object>) request.get("template");
            @SuppressWarnings("unchecked")
            Map<String, Object> renderData = (Map<String, Object>) request.get("data");

            // Create temporary template
            ReportTemplate template = ReportTemplate.builder()
                    .templateName("Preview")
                    .templateType(ReportTemplate.TemplateType.CUSTOM)
                    .content((String) templateData.get("content"))
                    .build();

            // Render template
            String rendered = templateService.renderTemplate(template, renderData);

            Map<String, Object> response = new HashMap<>();
            response.put("renderedContent", rendered);
            response.put("preview", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error previewing template", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get template statistics
     *
     * @return Template statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTemplateStatistics() {
        log.info("GET /api/templates/stats");

        try {
            Map<String, Object> stats = templateService.getTemplateStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching template statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Validate template
     *
     * @param template Template to validate
     * @return Validation result
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateTemplate(@RequestBody ReportTemplate template) {
        log.info("POST /api/templates/validate");

        try {
            template.validate();

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("message", "Template is valid");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Template validation failed: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("Error validating template", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
