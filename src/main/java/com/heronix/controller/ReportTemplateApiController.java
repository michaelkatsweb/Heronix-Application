package com.heronix.controller;

import com.heronix.dto.ReportTemplate;
import com.heronix.service.ReportTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/report-template")
@RequiredArgsConstructor
public class ReportTemplateApiController {
    private final ReportTemplateService templateService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTemplate(@RequestBody ReportTemplate template) {
        try {
            ReportTemplate created = templateService.createTemplate(template);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("templateId", created.getTemplateId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<Map<String, Object>> getTemplate(@PathVariable Long templateId) {
        try {
            ReportTemplate template = templateService.getTemplate(templateId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("template", template);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/{templateId}/clone")
    public ResponseEntity<Map<String, Object>> cloneTemplate(
            @PathVariable Long templateId,
            @RequestBody Map<String, String> request) {
        try {
            String newName = request.get("newName");
            ReportTemplate cloned = templateService.cloneTemplate(templateId, newName);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("templateId", cloned.getTemplateId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(@PathVariable Long templateId) {
        try {
            templateService.deleteTemplate(templateId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", templateService.getStatistics());
        return ResponseEntity.ok(response);
    }
}
