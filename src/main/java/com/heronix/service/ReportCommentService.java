package com.heronix.service;

import com.heronix.dto.ReportComment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Report Comment Service
 *
 * Provides comment management operations including threaded comments,
 * mentions, reactions, edit history, and resolution tracking.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 71 - Report Collaboration & Sharing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportCommentService {

    private final Map<Long, ReportComment> commentStore = new ConcurrentHashMap<>();
    private final AtomicLong commentIdGenerator = new AtomicLong(1);

    /**
     * Create comment
     */
    public ReportComment createComment(ReportComment comment) {
        Long commentId = commentIdGenerator.getAndIncrement();
        comment.setCommentId(commentId);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setStatus(ReportComment.CommentStatus.ACTIVE);
        comment.setIsEdited(false);
        comment.setEditCount(0);
        comment.setReplyCount(0);
        comment.setIsResolved(false);
        comment.setIsPinned(false);
        comment.setIsHighlighted(false);
        comment.setHasAttachments(comment.getAttachmentUrls() != null && !comment.getAttachmentUrls().isEmpty());

        // Validate
        comment.validate();

        // Initialize collections
        if (comment.getReplies() == null) {
            comment.setReplies(new ArrayList<>());
        }
        if (comment.getMentions() == null) {
            comment.setMentions(new ArrayList<>());
        }
        if (comment.getReactions() == null) {
            comment.setReactions(new HashMap<>());
        }

        commentStore.put(commentId, comment);

        log.info("Comment created: {} (report: {}, author: {})",
                commentId, comment.getReportId(), comment.getAuthorUsername());
        return comment;
    }

    /**
     * Get comment by ID
     */
    public ReportComment getComment(Long commentId) {
        ReportComment comment = commentStore.get(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + commentId);
        }
        return comment;
    }

    /**
     * Get comments by report ID
     */
    public List<ReportComment> getCommentsByReport(Long reportId) {
        return commentStore.values().stream()
                .filter(c -> c.getReportId().equals(reportId))
                .filter(c -> c.getParentCommentId() == null) // Top-level comments only
                .sorted(Comparator.comparing(ReportComment::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Add reply to comment
     */
    public ReportComment addReply(Long parentCommentId, ReportComment reply) {
        ReportComment parentComment = commentStore.get(parentCommentId);
        if (parentComment == null) {
            throw new IllegalArgumentException("Parent comment not found: " + parentCommentId);
        }

        reply.setParentCommentId(parentCommentId);
        reply.setReportId(parentComment.getReportId());
        reply.setThreadLevel((parentComment.getThreadLevel() != null ? parentComment.getThreadLevel() : 0) + 1);

        ReportComment created = createComment(reply);

        // Update parent
        parentComment.addReply(created);

        log.info("Reply added: {} (parent: {})", created.getCommentId(), parentCommentId);
        return created;
    }

    /**
     * Edit comment
     */
    public ReportComment editComment(Long commentId, String newText, String editedBy, String reason) {
        ReportComment comment = commentStore.get(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + commentId);
        }

        if (!comment.canEdit(editedBy)) {
            throw new IllegalArgumentException("User not authorized to edit this comment");
        }

        comment.edit(newText, editedBy, reason);

        log.info("Comment edited: {} (by: {})", commentId, editedBy);
        return comment;
    }

    /**
     * Delete comment (soft delete)
     */
    public void deleteComment(Long commentId, String username) {
        ReportComment comment = commentStore.get(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + commentId);
        }

        if (!comment.canDelete(username)) {
            throw new IllegalArgumentException("User not authorized to delete this comment");
        }

        comment.softDelete();

        log.info("Comment deleted: {} (by: {})", commentId, username);
    }

    /**
     * Add reaction to comment
     */
    public ReportComment addReaction(Long commentId, String emoji) {
        ReportComment comment = commentStore.get(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + commentId);
        }

        comment.addReaction(emoji);

        log.info("Reaction added to comment: {} (emoji: {})", commentId, emoji);
        return comment;
    }

    /**
     * Remove reaction from comment
     */
    public ReportComment removeReaction(Long commentId, String emoji) {
        ReportComment comment = commentStore.get(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + commentId);
        }

        comment.removeReaction(emoji);

        log.info("Reaction removed from comment: {} (emoji: {})", commentId, emoji);
        return comment;
    }

    /**
     * Mark comment as resolved
     */
    public ReportComment resolveComment(Long commentId, String username, String resolutionComment) {
        ReportComment comment = commentStore.get(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + commentId);
        }

        comment.markResolved(username, resolutionComment);

        log.info("Comment resolved: {} (by: {})", commentId, username);
        return comment;
    }

    /**
     * Mark comment as unresolved
     */
    public ReportComment unresolveComment(Long commentId) {
        ReportComment comment = commentStore.get(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + commentId);
        }

        comment.markUnresolved();

        log.info("Comment unresolved: {}", commentId);
        return comment;
    }

    /**
     * Pin comment
     */
    public ReportComment pinComment(Long commentId) {
        ReportComment comment = commentStore.get(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + commentId);
        }

        comment.pin();

        log.info("Comment pinned: {}", commentId);
        return comment;
    }

    /**
     * Unpin comment
     */
    public ReportComment unpinComment(Long commentId) {
        ReportComment comment = commentStore.get(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + commentId);
        }

        comment.unpin();

        log.info("Comment unpinned: {}", commentId);
        return comment;
    }

    /**
     * Add mention to comment
     */
    public ReportComment addMention(Long commentId, String username) {
        ReportComment comment = commentStore.get(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + commentId);
        }

        comment.addMention(username);

        log.info("Mention added to comment: {} (user: {})", commentId, username);
        return comment;
    }

    /**
     * Get comments by user
     */
    public List<ReportComment> getCommentsByUser(String username) {
        return commentStore.values().stream()
                .filter(c -> username.equals(c.getAuthorUsername()))
                .sorted(Comparator.comparing(ReportComment::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get comments mentioning user
     */
    public List<ReportComment> getCommentsMentioningUser(String username) {
        return commentStore.values().stream()
                .filter(c -> c.getMentions() != null && c.getMentions().contains(username))
                .sorted(Comparator.comparing(ReportComment::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get unresolved comments for report
     */
    public List<ReportComment> getUnresolvedComments(Long reportId) {
        return commentStore.values().stream()
                .filter(c -> c.getReportId().equals(reportId))
                .filter(c -> !Boolean.TRUE.equals(c.getIsResolved()))
                .filter(c -> c.isActive())
                .sorted(Comparator.comparing(ReportComment::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get all comments statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalComments", commentStore.size());

        long activeComments = commentStore.values().stream()
                .filter(ReportComment::isActive)
                .count();

        long resolvedComments = commentStore.values().stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsResolved()))
                .count();

        long pinnedComments = commentStore.values().stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsPinned()))
                .count();

        long totalReactions = commentStore.values().stream()
                .mapToInt(ReportComment::getTotalReactions)
                .sum();

        stats.put("activeComments", activeComments);
        stats.put("resolvedComments", resolvedComments);
        stats.put("pinnedComments", pinnedComments);
        stats.put("totalReactions", totalReactions);
        stats.put("timestamp", LocalDateTime.now());

        return stats;
    }
}
