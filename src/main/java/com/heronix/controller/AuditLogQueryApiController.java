package com.heronix.controller;

import com.heronix.model.domain.AuditLog;
import com.heronix.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit Log Query API Controller
 *
 * REST API endpoints for audit log access and reporting.
 *
 * Endpoints:
 * - GET /api/audit - Get paginated audit logs
 * - GET /api/audit/{id} - Get specific audit log
 * - GET /api/audit/user/{username} - Get logs by user
 * - GET /api/audit/search - Search audit logs
 *
 * Security:
 * - Admin-only access recommended
 * - Read-only operations (no delete/update)
 * - Audit log viewing is itself audited
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 64 - Report Audit Trail & Compliance Logging
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditLogQueryApiController {

    private final AuditLogRepository auditLogRepository;

    /**
     * Get paginated audit logs
     */
    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.info("GET /api/audit - page: {}, size: {}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size,
                    Sort.by(Sort.Direction.DESC, "timestamp"));
            Page<AuditLog> logs = auditLogRepository.findAll(pageable);

            return ResponseEntity.ok(logs);

        } catch (Exception e) {
            log.error("Error fetching audit logs", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get audit logs by username
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "100") int limit) {

        log.info("GET /api/audit/user/{} - limit: {}", username, limit);

        try {
            List<AuditLog> logs = auditLogRepository.findByUsernameOrderByTimestampDesc(username);

            // Limit results
            if (logs.size() > limit) {
                logs = logs.subList(0, limit);
            }

            return ResponseEntity.ok(logs);

        } catch (Exception e) {
            log.error("Error fetching audit logs for user: {}", username, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Search audit logs by date range and action
     */
    @GetMapping("/search")
    public ResponseEntity<List<AuditLog>> searchAuditLogs(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,

            @RequestParam(required = false)
            AuditLog.AuditAction action,

            @RequestParam(defaultValue = "100") int limit) {

        log.info("GET /api/audit/search - startDate: {}, endDate: {}, action: {}",
                startDate, endDate, action);

        try {
            List<AuditLog> logs;

            if (startDate != null && endDate != null && action != null) {
                logs = auditLogRepository.findByActionAndTimestampBetweenOrderByTimestampDesc(
                        action, startDate, endDate);
            } else if (startDate != null && endDate != null) {
                logs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
                        startDate, endDate);
            } else if (action != null) {
                logs = auditLogRepository.findByActionOrderByTimestampDesc(action);
            } else {
                Pageable pageable = PageRequest.of(0, limit,
                        Sort.by(Sort.Direction.DESC, "timestamp"));
                logs = auditLogRepository.findAll(pageable).getContent();
            }

            // Limit results if needed
            if (logs.size() > limit) {
                logs = logs.subList(0, limit);
            }

            return ResponseEntity.ok(logs);

        } catch (Exception e) {
            log.error("Error searching audit logs", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get audit log by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getAuditLog(@PathVariable Long id) {
        log.info("GET /api/audit/{}", id);

        return auditLogRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
