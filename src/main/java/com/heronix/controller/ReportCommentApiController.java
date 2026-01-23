package com.heronix.controller;

import com.heronix.dto.ReportComment;
import com.heronix.service.ReportCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Comment API Controller
 *
 * REST API endpoints for comment management including threaded comments,
 * mentions, reactions, edit history, and resolution tracking.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 71 - Report Collaboration & Sharing
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/report-comments")
@RequiredArgsConstructor
public class ReportCommentApiController {

    private final ReportCommentService commentService;

    /**
     * Create new comment
     * POST /api/report-comments
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createComment(
            @RequestBody ReportComment comment) {
        try {
            ReportComment created = commentService.createComment(comment);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Comment created successfully");
            response.put("commentId", created.getCommentId());
            response.put("reportId", created.getReportId());
            response.put("authorUsername", created.getAuthorUsername());
            response.put("createdAt", created.getCreatedAt());

            log.info("Comment created via API: {}", created.getCommentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create comment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get comment by ID
     * GET /api/report-comments/{commentId}
     */
    @GetMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> getComment(@PathVariable Long commentId) {
        try {
            ReportComment comment = commentService.getComment(commentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("comment", comment);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get comments by report ID
     * GET /api/report-comments/report/{reportId}
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<Map<String, Object>> getCommentsByReport(@PathVariable Long reportId) {
        List<ReportComment> comments = commentService.getCommentsByReport(reportId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("comments", comments);
        response.put("count", comments.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Add reply to comment
     * POST /api/report-comments/{commentId}/reply
     */
    @PostMapping("/{commentId}/reply")
    public ResponseEntity<Map<String, Object>> addReply(
            @PathVariable Long commentId,
            @RequestBody ReportComment reply) {
        try {
            ReportComment created = commentService.addReply(commentId, reply);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reply added successfully");
            response.put("commentId", created.getCommentId());
            response.put("parentCommentId", commentId);

            log.info("Reply added via API: {} (parent: {})", created.getCommentId(), commentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Edit comment
     * PUT /api/report-comments/{commentId}
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> editComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> editRequest) {
        try {
            String newText = (String) editRequest.get("newText");
            String editedBy = (String) editRequest.get("editedBy");
            String reason = (String) editRequest.get("reason");

            ReportComment comment = commentService.editComment(commentId, newText, editedBy, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Comment edited successfully");
            response.put("commentId", commentId);
            response.put("editCount", comment.getEditCount());
            response.put("editedAt", comment.getEditedAt());

            log.info("Comment edited via API: {}", commentId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Delete comment
     * DELETE /api/report-comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable Long commentId,
            @RequestParam String username) {
        try {
            commentService.deleteComment(commentId, username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Comment deleted successfully");
            response.put("commentId", commentId);
            response.put("deletedAt", LocalDateTime.now());

            log.info("Comment deleted via API: {}", commentId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Add reaction to comment
     * POST /api/report-comments/{commentId}/reaction
     */
    @PostMapping("/{commentId}/reaction")
    public ResponseEntity<Map<String, Object>> addReaction(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> reactionRequest) {
        try {
            String emoji = reactionRequest.get("emoji");

            ReportComment comment = commentService.addReaction(commentId, emoji);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reaction added successfully");
            response.put("commentId", commentId);
            response.put("emoji", emoji);
            response.put("totalReactions", comment.getTotalReactions());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Remove reaction from comment
     * DELETE /api/report-comments/{commentId}/reaction
     */
    @DeleteMapping("/{commentId}/reaction")
    public ResponseEntity<Map<String, Object>> removeReaction(
            @PathVariable Long commentId,
            @RequestParam String emoji) {
        try {
            ReportComment comment = commentService.removeReaction(commentId, emoji);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reaction removed successfully");
            response.put("commentId", commentId);
            response.put("emoji", emoji);
            response.put("totalReactions", comment.getTotalReactions());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Resolve comment
     * POST /api/report-comments/{commentId}/resolve
     */
    @PostMapping("/{commentId}/resolve")
    public ResponseEntity<Map<String, Object>> resolveComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> resolveRequest) {
        try {
            String username = resolveRequest.get("username");
            String resolutionComment = resolveRequest.get("resolutionComment");

            ReportComment comment = commentService.resolveComment(commentId, username, resolutionComment);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Comment resolved successfully");
            response.put("commentId", commentId);
            response.put("resolvedAt", comment.getResolvedAt());
            response.put("resolvedBy", comment.getResolvedBy());

            log.info("Comment resolved via API: {}", commentId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Unresolve comment
     * POST /api/report-comments/{commentId}/unresolve
     */
    @PostMapping("/{commentId}/unresolve")
    public ResponseEntity<Map<String, Object>> unresolveComment(@PathVariable Long commentId) {
        try {
            ReportComment comment = commentService.unresolveComment(commentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Comment unresolved successfully");
            response.put("commentId", commentId);

            log.info("Comment unresolved via API: {}", commentId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Pin comment
     * POST /api/report-comments/{commentId}/pin
     */
    @PostMapping("/{commentId}/pin")
    public ResponseEntity<Map<String, Object>> pinComment(@PathVariable Long commentId) {
        try {
            commentService.pinComment(commentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Comment pinned successfully");
            response.put("commentId", commentId);

            log.info("Comment pinned via API: {}", commentId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Unpin comment
     * POST /api/report-comments/{commentId}/unpin
     */
    @PostMapping("/{commentId}/unpin")
    public ResponseEntity<Map<String, Object>> unpinComment(@PathVariable Long commentId) {
        try {
            commentService.unpinComment(commentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Comment unpinned successfully");
            response.put("commentId", commentId);

            log.info("Comment unpinned via API: {}", commentId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get comments by user
     * GET /api/report-comments/user/{username}
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<Map<String, Object>> getCommentsByUser(@PathVariable String username) {
        List<ReportComment> comments = commentService.getCommentsByUser(username);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("comments", comments);
        response.put("count", comments.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get comments mentioning user
     * GET /api/report-comments/mentions/{username}
     */
    @GetMapping("/mentions/{username}")
    public ResponseEntity<Map<String, Object>> getCommentsMentioningUser(@PathVariable String username) {
        List<ReportComment> comments = commentService.getCommentsMentioningUser(username);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("comments", comments);
        response.put("count", comments.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get unresolved comments for report
     * GET /api/report-comments/report/{reportId}/unresolved
     */
    @GetMapping("/report/{reportId}/unresolved")
    public ResponseEntity<Map<String, Object>> getUnresolvedComments(@PathVariable Long reportId) {
        List<ReportComment> comments = commentService.getUnresolvedComments(reportId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("comments", comments);
        response.put("count", comments.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get all comments statistics
     * GET /api/report-comments/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = commentService.getStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);

        return ResponseEntity.ok(response);
    }
}
