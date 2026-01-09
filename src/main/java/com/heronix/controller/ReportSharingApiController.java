package com.heronix.controller;

import com.heronix.dto.ReportComment;
import com.heronix.dto.ReportShare;
import com.heronix.service.ReportSharingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Sharing API Controller
 *
 * REST API endpoints for report sharing and collaboration.
 *
 * Provides Endpoints For:
 * - Share management
 * - Access validation
 * - Comment management
 * - Activity tracking
 *
 * Endpoints:
 * - POST /api/shares - Create share
 * - GET /api/shares/{id} - Get share
 * - GET /api/shares/token/{token} - Get share by token
 * - GET /api/shares/report/{reportId} - Get shares for report
 * - PUT /api/shares/{id} - Update share
 * - DELETE /api/shares/{id} - Delete share
 * - POST /api/shares/{id}/revoke - Revoke share
 * - POST /api/shares/{id}/access - Record access
 * - POST /api/comments - Add comment
 * - GET /api/comments/{id} - Get comment
 * - GET /api/comments/report/{reportId} - Get comments for report
 * - PUT /api/comments/{id} - Update comment
 * - DELETE /api/comments/{id} - Delete comment
 * - POST /api/comments/{id}/react - Add reaction
 * - POST /api/comments/{id}/resolve - Resolve comment
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 71 - Report Collaboration & Sharing
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ReportSharingApiController {

    private final ReportSharingService sharingService;

    // ============================================================
    // Share Management Endpoints
    // ============================================================

    /**
     * Create new share
     *
     * @param share Share configuration
     * @return Created share
     */
    @PostMapping("/shares")
    public ResponseEntity<ReportShare> createShare(@RequestBody ReportShare share) {
        log.info("POST /api/shares - Creating share for report {}", share.getReportId());

        try {
            ReportShare created = sharingService.createShare(share);
            return ResponseEntity.ok(created);

        } catch (IllegalArgumentException e) {
            log.error("Invalid share: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error creating share", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get share by ID
     *
     * @param id Share ID
     * @return Share if found
     */
    @GetMapping("/shares/{id}")
    public ResponseEntity<ReportShare> getShare(@PathVariable Long id) {
        log.info("GET /api/shares/{}", id);

        try {
            return sharingService.getShare(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching share: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get share by token
     *
     * @param token Share token
     * @return Share if found
     */
    @GetMapping("/shares/token/{token}")
    public ResponseEntity<ReportShare> getShareByToken(@PathVariable String token) {
        log.info("GET /api/shares/token/{}", token);

        try {
            return sharingService.getShareByToken(token)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching share by token: {}", token, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all shares for a report
     *
     * @param reportId Report ID
     * @return List of shares
     */
    @GetMapping("/shares/report/{reportId}")
    public ResponseEntity<List<ReportShare>> getSharesForReport(@PathVariable Long reportId) {
        log.info("GET /api/shares/report/{}", reportId);

        try {
            List<ReportShare> shares = sharingService.getSharesForReport(reportId);
            return ResponseEntity.ok(shares);

        } catch (Exception e) {
            log.error("Error fetching shares for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get active shares for a report
     *
     * @param reportId Report ID
     * @return List of active shares
     */
    @GetMapping("/shares/report/{reportId}/active")
    public ResponseEntity<List<ReportShare>> getActiveSharesForReport(@PathVariable Long reportId) {
        log.info("GET /api/shares/report/{}/active", reportId);

        try {
            List<ReportShare> shares = sharingService.getActiveSharesForReport(reportId);
            return ResponseEntity.ok(shares);

        } catch (Exception e) {
            log.error("Error fetching active shares for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get shares for a user
     *
     * @param username Username
     * @return List of shares
     */
    @GetMapping("/shares/user/{username}")
    public ResponseEntity<List<ReportShare>> getSharesForUser(@PathVariable String username) {
        log.info("GET /api/shares/user/{}", username);

        try {
            List<ReportShare> shares = sharingService.getSharesForUser(username);
            return ResponseEntity.ok(shares);

        } catch (Exception e) {
            log.error("Error fetching shares for user: {}", username, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update share
     *
     * @param id Share ID
     * @param share Updated share data
     * @return Updated share
     */
    @PutMapping("/shares/{id}")
    public ResponseEntity<ReportShare> updateShare(
            @PathVariable Long id,
            @RequestBody ReportShare share) {
        log.info("PUT /api/shares/{}", id);

        try {
            share.setShareId(id);
            ReportShare updated = sharingService.updateShare(share);
            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            log.error("Invalid share: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error updating share: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Revoke share
     *
     * @param id Share ID
     * @return Success response
     */
    @PostMapping("/shares/{id}/revoke")
    public ResponseEntity<Map<String, Object>> revokeShare(@PathVariable Long id) {
        log.info("POST /api/shares/{}/revoke", id);

        try {
            sharingService.revokeShare(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Share revoked successfully");
            response.put("shareId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error revoking share: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete share
     *
     * @param id Share ID
     * @return Success response
     */
    @DeleteMapping("/shares/{id}")
    public ResponseEntity<Map<String, Object>> deleteShare(@PathVariable Long id) {
        log.info("DELETE /api/shares/{}", id);

        try {
            sharingService.deleteShare(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Share deleted successfully");
            response.put("shareId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting share: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Record share access
     *
     * @param id Share ID
     * @param request Access details
     * @return Success response
     */
    @PostMapping("/shares/{id}/access")
    public ResponseEntity<Map<String, Object>> recordAccess(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/shares/{}/access", id);

        try {
            String username = request.get("username");
            String action = request.get("action");
            String ipAddress = request.get("ipAddress");

            sharingService.recordAccess(id, username, action, ipAddress);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Access recorded");
            response.put("shareId", id);
            response.put("action", action);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error recording access for share: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Validate share access
     *
     * @param request Validation request
     * @return Validation result
     */
    @PostMapping("/shares/validate")
    public ResponseEntity<Map<String, Object>> validateAccess(@RequestBody Map<String, String> request) {
        log.info("POST /api/shares/validate");

        try {
            String token = request.get("token");
            String password = request.get("password");

            boolean valid = sharingService.validateShareAccess(token, password);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", valid);
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error validating share access", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================
    // Comment Management Endpoints
    // ============================================================

    /**
     * Add comment to report
     *
     * @param comment Comment data
     * @return Created comment
     */
    @PostMapping("/comments")
    public ResponseEntity<ReportComment> addComment(@RequestBody ReportComment comment) {
        log.info("POST /api/comments - Adding comment to report {}", comment.getReportId());

        try {
            ReportComment created = sharingService.addComment(comment);
            return ResponseEntity.ok(created);

        } catch (IllegalArgumentException e) {
            log.error("Invalid comment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error adding comment", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get comment by ID
     *
     * @param id Comment ID
     * @return Comment if found
     */
    @GetMapping("/comments/{id}")
    public ResponseEntity<ReportComment> getComment(@PathVariable Long id) {
        log.info("GET /api/comments/{}", id);

        try {
            return sharingService.getComment(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching comment: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get comments for report
     *
     * @param reportId Report ID
     * @return List of comments
     */
    @GetMapping("/comments/report/{reportId}")
    public ResponseEntity<List<ReportComment>> getCommentsForReport(@PathVariable Long reportId) {
        log.info("GET /api/comments/report/{}", reportId);

        try {
            List<ReportComment> comments = sharingService.getCommentsForReport(reportId);
            return ResponseEntity.ok(comments);

        } catch (Exception e) {
            log.error("Error fetching comments for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get active comments for report
     *
     * @param reportId Report ID
     * @return List of active comments
     */
    @GetMapping("/comments/report/{reportId}/active")
    public ResponseEntity<List<ReportComment>> getActiveCommentsForReport(@PathVariable Long reportId) {
        log.info("GET /api/comments/report/{}/active", reportId);

        try {
            List<ReportComment> comments = sharingService.getActiveCommentsForReport(reportId);
            return ResponseEntity.ok(comments);

        } catch (Exception e) {
            log.error("Error fetching active comments for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get replies to comment
     *
     * @param commentId Comment ID
     * @return List of replies
     */
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<List<ReportComment>> getReplies(@PathVariable Long commentId) {
        log.info("GET /api/comments/{}/replies", commentId);

        try {
            List<ReportComment> replies = sharingService.getReplies(commentId);
            return ResponseEntity.ok(replies);

        } catch (Exception e) {
            log.error("Error fetching replies for comment: {}", commentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update comment
     *
     * @param id Comment ID
     * @param request Update request
     * @return Updated comment
     */
    @PutMapping("/comments/{id}")
    public ResponseEntity<ReportComment> updateComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/comments/{}", id);

        try {
            String newText = request.get("text");
            String editedBy = request.get("editedBy");
            String reason = request.get("reason");

            ReportComment updated = sharingService.updateComment(id, newText, editedBy, reason);
            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            log.error("Comment not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating comment: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete comment
     *
     * @param id Comment ID
     * @return Success response
     */
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Long id) {
        log.info("DELETE /api/comments/{}", id);

        try {
            sharingService.deleteComment(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comment deleted successfully");
            response.put("commentId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting comment: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add reaction to comment
     *
     * @param id Comment ID
     * @param request Reaction request
     * @return Success response
     */
    @PostMapping("/comments/{id}/react")
    public ResponseEntity<Map<String, Object>> addReaction(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/comments/{}/react", id);

        try {
            String emoji = request.get("emoji");
            sharingService.addReaction(id, emoji);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reaction added");
            response.put("commentId", id);
            response.put("emoji", emoji);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error adding reaction to comment: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Remove reaction from comment
     *
     * @param id Comment ID
     * @param request Reaction request
     * @return Success response
     */
    @PostMapping("/comments/{id}/unreact")
    public ResponseEntity<Map<String, Object>> removeReaction(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/comments/{}/unreact", id);

        try {
            String emoji = request.get("emoji");
            sharingService.removeReaction(id, emoji);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reaction removed");
            response.put("commentId", id);
            response.put("emoji", emoji);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error removing reaction from comment: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resolve comment
     *
     * @param id Comment ID
     * @param request Resolution request
     * @return Success response
     */
    @PostMapping("/comments/{id}/resolve")
    public ResponseEntity<Map<String, Object>> resolveComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/comments/{}/resolve", id);

        try {
            String username = request.get("username");
            String resolutionComment = request.get("resolutionComment");

            sharingService.resolveComment(id, username, resolutionComment);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comment resolved");
            response.put("commentId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error resolving comment: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Pin comment
     *
     * @param id Comment ID
     * @return Success response
     */
    @PostMapping("/comments/{id}/pin")
    public ResponseEntity<Map<String, Object>> pinComment(@PathVariable Long id) {
        log.info("POST /api/comments/{}/pin", id);

        try {
            sharingService.pinComment(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comment pinned");
            response.put("commentId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error pinning comment: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================
    // Statistics Endpoints
    // ============================================================

    /**
     * Get sharing statistics
     *
     * @return Sharing statistics
     */
    @GetMapping("/shares/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/shares/stats");

        try {
            Map<String, Object> stats = sharingService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get report statistics
     *
     * @param reportId Report ID
     * @return Report statistics
     */
    @GetMapping("/shares/report/{reportId}/stats")
    public ResponseEntity<Map<String, Object>> getReportStatistics(@PathVariable Long reportId) {
        log.info("GET /api/shares/report/{}/stats", reportId);

        try {
            Map<String, Object> stats = sharingService.getReportStatistics(reportId);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching report statistics: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Expire old shares
     *
     * @return Expiration result
     */
    @PostMapping("/shares/expire")
    public ResponseEntity<Map<String, Object>> expireOldShares() {
        log.info("POST /api/shares/expire");

        try {
            int expired = sharingService.expireOldShares();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Expired old shares");
            response.put("expiredCount", expired);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error expiring shares", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
