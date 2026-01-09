package com.heronix.controller;

import com.heronix.dto.ReportCollaboration;
import com.heronix.service.ReportCollaborationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Collaboration API Controller
 *
 * REST API endpoints for collaboration and sharing management.
 *
 * Endpoints:
 * - POST /api/collaboration - Create collaboration
 * - GET /api/collaboration/{id} - Get collaboration
 * - GET /api/collaboration/report/{reportId} - Get collaboration by report
 * - POST /api/collaboration/{id}/collaborator - Add collaborator
 * - DELETE /api/collaboration/{id}/collaborator/{userId} - Remove collaborator
 * - POST /api/collaboration/{id}/comment - Add comment
 * - POST /api/collaboration/{id}/comment/{commentId}/resolve - Resolve comment
 * - GET /api/collaboration/{id}/comments - Get comments
 * - POST /api/collaboration/{id}/share - Create shared link
 * - POST /api/collaboration/{id}/view - Track view
 * - POST /api/collaboration/{id}/edit - Track edit
 * - POST /api/collaboration/{id}/task - Add task
 * - POST /api/collaboration/{id}/task/{taskId}/complete - Complete task
 * - GET /api/collaboration/{id}/activity - Get activity feed
 * - DELETE /api/collaboration/{id} - Delete collaboration
 * - GET /api/collaboration/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 85 - Report Collaboration & Sharing
 */
@RestController
@RequestMapping("/api/collaboration")
@RequiredArgsConstructor
@Slf4j
public class ReportCollaborationApiController {

    private final ReportCollaborationService collaborationService;

    /**
     * Create collaboration
     */
    @PostMapping
    public ResponseEntity<ReportCollaboration> createCollaboration(@RequestBody ReportCollaboration collaboration) {
        log.info("POST /api/collaboration - Creating collaboration for report {}", collaboration.getReportId());

        try {
            ReportCollaboration created = collaborationService.createCollaboration(collaboration);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating collaboration", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get collaboration
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportCollaboration> getCollaboration(@PathVariable Long id) {
        log.info("GET /api/collaboration/{}", id);

        try {
            return collaborationService.getCollaboration(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching collaboration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get collaboration by report
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<ReportCollaboration> getCollaborationByReport(@PathVariable Long reportId) {
        log.info("GET /api/collaboration/report/{}", reportId);

        try {
            return collaborationService.getCollaborationByReport(reportId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching collaboration for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add collaborator
     */
    @PostMapping("/{id}/collaborator")
    public ResponseEntity<Map<String, Object>> addCollaborator(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/collaboration/{}/collaborator", id);

        try {
            String userId = request.get("userId");
            String username = request.get("username");
            String permissionLevel = request.get("permissionLevel");
            String addedBy = request.get("addedBy");

            collaborationService.addCollaborator(
                    id,
                    userId,
                    username,
                    ReportCollaboration.PermissionLevel.valueOf(permissionLevel),
                    addedBy
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Collaborator added");
            response.put("collaborationId", id);
            response.put("userId", userId);
            response.put("username", username);
            response.put("permissionLevel", permissionLevel);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Collaboration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding collaborator to collaboration: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove collaborator
     */
    @DeleteMapping("/{id}/collaborator/{userId}")
    public ResponseEntity<Map<String, Object>> removeCollaborator(
            @PathVariable Long id,
            @PathVariable String userId,
            @RequestParam String removedBy) {
        log.info("DELETE /api/collaboration/{}/collaborator/{}", id, userId);

        try {
            collaborationService.removeCollaborator(id, userId, removedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Collaborator removed");
            response.put("collaborationId", id);
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Collaboration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error removing collaborator from collaboration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add comment
     */
    @PostMapping("/{id}/comment")
    public ResponseEntity<ReportCollaboration.Comment> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/collaboration/{}/comment", id);

        try {
            String username = request.get("username");
            String type = request.get("type");
            String content = request.get("content");
            String parentCommentId = request.get("parentCommentId");

            ReportCollaboration.Comment comment = collaborationService.addComment(
                    id,
                    username,
                    ReportCollaboration.CommentType.valueOf(type),
                    content,
                    parentCommentId
            );

            return ResponseEntity.ok(comment);

        } catch (IllegalArgumentException e) {
            log.error("Collaboration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for comment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error adding comment to collaboration: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Resolve comment
     */
    @PostMapping("/{id}/comment/{commentId}/resolve")
    public ResponseEntity<Map<String, Object>> resolveComment(
            @PathVariable Long id,
            @PathVariable String commentId,
            @RequestParam String resolvedBy) {
        log.info("POST /api/collaboration/{}/comment/{}/resolve", id, commentId);

        try {
            collaborationService.resolveComment(id, commentId, resolvedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comment resolved");
            response.put("collaborationId", id);
            response.put("commentId", commentId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Collaboration or comment not found: {}, {}", id, commentId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error resolving comment: {}", commentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get comments
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<ReportCollaboration.Comment>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean unresolvedOnly) {
        log.info("GET /api/collaboration/{}/comments?unresolvedOnly={}", id, unresolvedOnly);

        try {
            List<ReportCollaboration.Comment> comments = collaborationService.getComments(id, unresolvedOnly);
            return ResponseEntity.ok(comments);

        } catch (Exception e) {
            log.error("Error fetching comments for collaboration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create shared link
     */
    @PostMapping("/{id}/share")
    public ResponseEntity<ReportCollaboration.SharedLink> createSharedLink(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/collaboration/{}/share", id);

        try {
            String shareType = request.get("shareType");
            String permissionLevel = request.get("permissionLevel");
            String sharedBy = request.get("sharedBy");

            ReportCollaboration.SharedLink link = collaborationService.createSharedLink(
                    id,
                    ReportCollaboration.ShareType.valueOf(shareType),
                    ReportCollaboration.PermissionLevel.valueOf(permissionLevel),
                    sharedBy
            );

            return ResponseEntity.ok(link);

        } catch (IllegalArgumentException e) {
            log.error("Collaboration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for share: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error creating shared link for collaboration: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Track view
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<Map<String, Object>> trackView(
            @PathVariable Long id,
            @RequestParam String username) {
        log.info("POST /api/collaboration/{}/view?username={}", id, username);

        try {
            collaborationService.trackView(id, username);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "View tracked");
            response.put("collaborationId", id);
            response.put("username", username);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error tracking view for collaboration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Track edit
     */
    @PostMapping("/{id}/edit")
    public ResponseEntity<Map<String, Object>> trackEdit(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/collaboration/{}/edit", id);

        try {
            String username = request.get("username");
            String details = request.get("details");

            collaborationService.trackEdit(id, username, details);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Edit tracked");
            response.put("collaborationId", id);
            response.put("username", username);

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            log.error("Invalid state for edit: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error tracking edit for collaboration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add task
     */
    @PostMapping("/{id}/task")
    public ResponseEntity<ReportCollaboration.Task> addTask(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/collaboration/{}/task", id);

        try {
            String title = request.get("title");
            String description = request.get("description");
            String assignedTo = request.get("assignedTo");
            String assignedBy = request.get("assignedBy");
            String dueDateStr = request.get("dueDate");
            String priority = request.get("priority");

            LocalDateTime dueDate = dueDateStr != null ? LocalDateTime.parse(dueDateStr) : null;

            ReportCollaboration.Task task = collaborationService.addTask(
                    id,
                    title,
                    description,
                    assignedTo,
                    assignedBy,
                    dueDate,
                    priority
            );

            return ResponseEntity.ok(task);

        } catch (IllegalArgumentException e) {
            log.error("Collaboration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for task: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error adding task to collaboration: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete task
     */
    @PostMapping("/{id}/task/{taskId}/complete")
    public ResponseEntity<Map<String, Object>> completeTask(
            @PathVariable Long id,
            @PathVariable String taskId,
            @RequestParam String completedBy) {
        log.info("POST /api/collaboration/{}/task/{}/complete", id, taskId);

        try {
            collaborationService.completeTask(id, taskId, completedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Task completed");
            response.put("collaborationId", id);
            response.put("taskId", taskId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Collaboration or task not found: {}, {}", id, taskId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing task: {}", taskId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get activity feed
     */
    @GetMapping("/{id}/activity")
    public ResponseEntity<List<ReportCollaboration.Activity>> getActivityFeed(
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") int limit) {
        log.info("GET /api/collaboration/{}/activity?limit={}", id, limit);

        try {
            List<ReportCollaboration.Activity> activities = collaborationService.getActivityFeed(id, limit);
            return ResponseEntity.ok(activities);

        } catch (Exception e) {
            log.error("Error fetching activity feed for collaboration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete collaboration
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCollaboration(@PathVariable Long id) {
        log.info("DELETE /api/collaboration/{}", id);

        try {
            collaborationService.deleteCollaboration(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Collaboration deleted");
            response.put("collaborationId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting collaboration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/collaboration/stats");

        try {
            Map<String, Object> stats = collaborationService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching collaboration statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
