package com.heronix.controller.api;

import com.heronix.model.domain.Plan504;
import com.heronix.model.domain.Plan504Document;
import com.heronix.model.domain.Plan504Document.DocumentType;
import com.heronix.model.enums.Plan504Status;
import com.heronix.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 504 Plan Management REST API Controller
 *
 * Provides RESTful endpoints for managing 504 plans, accommodations, documents,
 * monitoring, and notifications via web or mobile applications.
 *
 * Key Features:
 * - Complete CRUD operations for 504 plans
 * - Accommodation tracking and recommendations
 * - Document upload and management with versioning
 * - Progress monitoring and meeting notes
 * - Template-based plan creation
 * - Notification management
 * - Compliance reporting
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - 504 Management Enhancement
 */
@Slf4j
@RestController
@RequestMapping("/api/504plans")
@RequiredArgsConstructor
@Tag(name = "504 Plan Management", description = "REST API for Section 504 plan management")
public class Plan504ApiController {

    private final Plan504Service plan504Service;
    private final Plan504AccommodationService accommodationService;
    private final Plan504DocumentService documentService;
    private final Plan504TemplateService templateService;
    private final Plan504MonitoringService monitoringService;
    private final Plan504NotificationService notificationService;

    // ========================================================================
    // 504 PLAN CRUD OPERATIONS
    // ========================================================================

    @GetMapping
    @Operation(summary = "Get all 504 plans")
    public ResponseEntity<List<Plan504>> getAllPlans(@RequestParam(required = false) String status) {
        log.info("GET /api/504plans - status={}", status);
        try {
            List<Plan504> plans = status != null ?
                plan504Service.findByStatus(Plan504Status.valueOf(status.toUpperCase())) :
                plan504Service.findAllActivePlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            log.error("Error retrieving 504 plans", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get 504 plan by ID")
    public ResponseEntity<Plan504> getPlanById(@PathVariable Long id) {
        log.info("GET /api/504plans/{}", id);
        return plan504Service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get active plan for student")
    public ResponseEntity<Plan504> getActivePlanForStudent(@PathVariable Long studentId) {
        log.info("GET /api/504plans/student/{}", studentId);
        return plan504Service.findActivePlanForStudent(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new 504 plan")
    public ResponseEntity<Plan504> createPlan(@Valid @RequestBody Plan504 plan) {
        log.info("POST /api/504plans");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(plan504Service.createPlan(plan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update 504 plan")
    public ResponseEntity<Plan504> updatePlan(@PathVariable Long id, @Valid @RequestBody Plan504 plan) {
        log.info("PUT /api/504plans/{}", id);
        try {
            plan.setId(id);
            return ResponseEntity.ok(plan504Service.updatePlan(plan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete 504 plan")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        log.info("DELETE /api/504plans/{}", id);
        try {
            plan504Service.deletePlan(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================================================
    // ACCOMMODATION MANAGEMENT
    // ========================================================================

    @GetMapping("/{planId}/accommodations")
    @Operation(summary = "Get plan accommodations")
    public ResponseEntity<List<Plan504AccommodationService.Accommodation>> getAccommodations(@PathVariable Long planId) {
        try {
            return ResponseEntity.ok(accommodationService.parseAccommodations(planId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/accommodations/recommendations/{disability}")
    @Operation(summary = "Get accommodation recommendations")
    public ResponseEntity<List<Plan504AccommodationService.AccommodationRecommendation>> getRecommendations(@PathVariable String disability) {
        try {
            return ResponseEntity.ok(accommodationService.getRecommendations(disability));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================================================
    // DOCUMENT MANAGEMENT
    // ========================================================================

    @GetMapping("/{planId}/documents")
    @Operation(summary = "Get plan documents")
    public ResponseEntity<List<Plan504Document>> getDocuments(
            @PathVariable Long planId,
            @RequestParam(defaultValue = "true") boolean currentOnly) {
        try {
            List<Plan504Document> documents = currentOnly ?
                documentService.getCurrentDocuments(planId) :
                documentService.getDocumentsForPlan(planId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{planId}/documents")
    @Operation(summary = "Upload document")
    public ResponseEntity<Plan504Document> uploadDocument(
            @PathVariable Long planId,
            @RequestParam("file") MultipartFile file,
            @RequestParam DocumentType documentType,
            @RequestParam String description,
            @RequestParam(defaultValue = "false") boolean isConfidential,
            @RequestParam String uploadedBy) {
        try {
            byte[] fileData = file.getBytes();
            Plan504Document document = documentService.uploadDocument(
                planId, file.getOriginalFilename(), fileData, documentType,
                description, isConfidential, uploadedBy
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(document);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/documents/{documentId}/download")
    @Operation(summary = "Download document")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable Long documentId,
            @RequestParam String accessedBy) {
        try {
            Plan504Document doc = documentService.getDocumentMetadata(documentId);
            byte[] data = documentService.downloadDocument(documentId, accessedBy);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", doc.getFileName());
            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================================================
    // TEMPLATE MANAGEMENT
    // ========================================================================

    @GetMapping("/templates")
    @Operation(summary = "Get all templates")
    public ResponseEntity<List<Plan504TemplateService.Plan504Template>> getAllTemplates() {
        try {
            return ResponseEntity.ok(templateService.getAllTemplates());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/templates/{disability}")
    @Operation(summary = "Get template by disability")
    public ResponseEntity<Plan504TemplateService.Plan504Template> getTemplate(@PathVariable String disability) {
        Plan504TemplateService.Plan504Template template = templateService.getTemplateForDisability(disability);
        return template != null ? ResponseEntity.ok(template) : ResponseEntity.notFound().build();
    }

    // ========================================================================
    // MONITORING AND NOTIFICATIONS
    // ========================================================================

    @GetMapping("/monitoring/alerts")
    @Operation(summary = "Get review alerts")
    public ResponseEntity<List<Plan504MonitoringService.ReviewAlert>> getReviewAlerts() {
        try {
            return ResponseEntity.ok(monitoringService.getPlansNeedingReview());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{planId}/notifications/parent-creation")
    @Operation(summary = "Notify parents of plan creation")
    public ResponseEntity<Plan504NotificationService.NotificationResult> notifyParentsOfCreation(@PathVariable Long planId) {
        try {
            return ResponseEntity.ok(notificationService.notifyParentsOfPlanCreation(planId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/notifications/batch")
    @Operation(summary = "Send batch notifications")
    public ResponseEntity<Plan504NotificationService.BatchNotificationResult> sendBatchNotifications() {
        try {
            return ResponseEntity.ok(notificationService.sendPendingNotifications());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/statistics/summary")
    @Operation(summary = "Get system statistics")
    public ResponseEntity<Map<String, Object>> getSystemStatistics() {
        try {
            long activeCount = plan504Service.countActivePlans();
            List<Object[]> disabilityCounts = plan504Service.getPlanCountByDisability();

            Map<String, Object> stats = Map.of(
                "activePlans", activeCount,
                "plansByDisability", disabilityCounts,
                "timestamp", LocalDate.now().toString()
            );
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
