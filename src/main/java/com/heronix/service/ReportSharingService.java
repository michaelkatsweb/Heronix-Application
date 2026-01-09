package com.heronix.service;

import com.heronix.dto.ReportComment;
import com.heronix.dto.ReportShare;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Report Sharing Service
 *
 * Manages report sharing and collaboration.
 *
 * Features:
 * - Share reports with users/groups
 * - Public link generation
 * - Access control
 * - Share expiration
 * - Activity tracking
 * - Comment management
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 71 - Report Collaboration & Sharing
 */
@Service
@Slf4j
public class ReportSharingService {

    // Share storage (in production, use database)
    private final Map<Long, ReportShare> shares = new ConcurrentHashMap<>();
    private final Map<String, ReportShare> sharesByToken = new ConcurrentHashMap<>();
    private Long nextShareId = 1L;

    // Comment storage (in production, use database)
    private final Map<Long, ReportComment> comments = new ConcurrentHashMap<>();
    private Long nextCommentId = 1L;

    // ============================================================
    // Share Management
    // ============================================================

    /**
     * Create new share
     */
    public ReportShare createShare(ReportShare share) {
        synchronized (this) {
            share.validate();

            // Set defaults
            share.setShareId(nextShareId++);
            share.setSharedAt(LocalDateTime.now());
            share.setStatus(ReportShare.ShareStatus.ACTIVE);
            share.setViewCount(0);
            share.setDownloadCount(0);

            // Apply access level defaults if permissions not set
            if (share.getCanView() == null) {
                share.applyAccessLevelDefaults();
            }

            // Generate share token for public links
            if (share.getShareType() == ReportShare.ShareType.PUBLIC_LINK) {
                if (share.getShareToken() == null) {
                    share.setShareToken(generateShareToken());
                }
                share.setPublicLinkUrl("/shared/" + share.getShareToken());
                sharesByToken.put(share.getShareToken(), share);
            }

            shares.put(share.getShareId(), share);
            log.info("Created share {} for report {} with {}",
                    share.getShareId(), share.getReportId(), getRecipientDescription(share));

            return share;
        }
    }

    /**
     * Get share by ID
     */
    public Optional<ReportShare> getShare(Long shareId) {
        return Optional.ofNullable(shares.get(shareId));
    }

    /**
     * Get share by token
     */
    public Optional<ReportShare> getShareByToken(String token) {
        return Optional.ofNullable(sharesByToken.get(token));
    }

    /**
     * Get all shares for a report
     */
    public List<ReportShare> getSharesForReport(Long reportId) {
        return shares.values().stream()
                .filter(s -> s.getReportId().equals(reportId))
                .collect(Collectors.toList());
    }

    /**
     * Get active shares for a report
     */
    public List<ReportShare> getActiveSharesForReport(Long reportId) {
        return shares.values().stream()
                .filter(s -> s.getReportId().equals(reportId))
                .filter(ReportShare::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Get shares for a user
     */
    public List<ReportShare> getSharesForUser(String username) {
        return shares.values().stream()
                .filter(s -> username.equals(s.getSharedWithUsername()))
                .collect(Collectors.toList());
    }

    /**
     * Get shares created by user
     */
    public List<ReportShare> getSharesCreatedBy(String username) {
        return shares.values().stream()
                .filter(s -> username.equals(s.getSharedBy()))
                .collect(Collectors.toList());
    }

    /**
     * Update share
     */
    public ReportShare updateShare(ReportShare share) {
        share.validate();
        shares.put(share.getShareId(), share);
        log.info("Updated share {}", share.getShareId());
        return share;
    }

    /**
     * Revoke share
     */
    public void revokeShare(Long shareId) {
        ReportShare share = shares.get(shareId);
        if (share != null) {
            share.setStatus(ReportShare.ShareStatus.REVOKED);
            log.info("Revoked share {}", shareId);
        }
    }

    /**
     * Delete share
     */
    public void deleteShare(Long shareId) {
        ReportShare share = shares.remove(shareId);
        if (share != null && share.getShareToken() != null) {
            sharesByToken.remove(share.getShareToken());
        }
        log.info("Deleted share {}", shareId);
    }

    // ============================================================
    // Access Validation
    // ============================================================

    /**
     * Check if user has access to report
     */
    public boolean hasAccess(Long reportId, String username) {
        return shares.values().stream()
                .filter(s -> s.getReportId().equals(reportId))
                .filter(ReportShare::isActive)
                .anyMatch(s -> isUserInShare(s, username));
    }

    /**
     * Check if user can perform action on report
     */
    public boolean canPerformAction(Long reportId, String username, String action) {
        return shares.values().stream()
                .filter(s -> s.getReportId().equals(reportId))
                .filter(ReportShare::isActive)
                .filter(s -> isUserInShare(s, username))
                .anyMatch(s -> s.isActionAllowed(action));
    }

    /**
     * Validate share access with token
     */
    public boolean validateShareAccess(String token, String password) {
        ReportShare share = sharesByToken.get(token);
        if (share == null || !share.isActive()) {
            return false;
        }

        // Check password if required
        if (Boolean.TRUE.equals(share.getRequirePassword())) {
            return password != null && password.equals(share.getPasswordHash());
        }

        return true;
    }

    /**
     * Record share access
     */
    public void recordAccess(Long shareId, String username, String action, String ipAddress) {
        ReportShare share = shares.get(shareId);
        if (share == null) {
            return;
        }

        // Update counters
        switch (action.toUpperCase()) {
            case "VIEW":
                share.incrementViewCount();
                break;
            case "DOWNLOAD":
                share.incrementDownloadCount();
                break;
        }

        // Log access
        share.logAccess(username, action, ipAddress);

        // Check if share should expire
        if (!share.hasViewsRemaining() || !share.hasDownloadsRemaining()) {
            share.setStatus(ReportShare.ShareStatus.EXPIRED);
        }

        log.info("Recorded {} access for share {} by {}", action, shareId, username);
    }

    // ============================================================
    // Comment Management
    // ============================================================

    /**
     * Add comment to report
     */
    public ReportComment addComment(ReportComment comment) {
        synchronized (this) {
            comment.validate();

            comment.setCommentId(nextCommentId++);
            comment.setCreatedAt(LocalDateTime.now());
            comment.setStatus(ReportComment.CommentStatus.ACTIVE);
            comment.setIsEdited(false);
            comment.setEditCount(0);
            comment.setReplyCount(0);
            comment.setLikeCount(0);

            // Set thread level
            if (comment.getParentCommentId() != null) {
                ReportComment parent = comments.get(comment.getParentCommentId());
                if (parent != null) {
                    comment.setThreadLevel((parent.getThreadLevel() != null ? parent.getThreadLevel() : 0) + 1);
                    parent.addReply(comment);
                }
            } else {
                comment.setThreadLevel(0);
            }

            comments.put(comment.getCommentId(), comment);
            log.info("Added comment {} to report {} by {}",
                    comment.getCommentId(), comment.getReportId(), comment.getAuthorUsername());

            return comment;
        }
    }

    /**
     * Get comment by ID
     */
    public Optional<ReportComment> getComment(Long commentId) {
        return Optional.ofNullable(comments.get(commentId));
    }

    /**
     * Get all comments for report
     */
    public List<ReportComment> getCommentsForReport(Long reportId) {
        return comments.values().stream()
                .filter(c -> c.getReportId().equals(reportId))
                .filter(c -> c.getParentCommentId() == null) // Top-level only
                .sorted(Comparator.comparing(ReportComment::getCreatedAt))
                .collect(Collectors.toList());
    }

    /**
     * Get active comments for report
     */
    public List<ReportComment> getActiveCommentsForReport(Long reportId) {
        return comments.values().stream()
                .filter(c -> c.getReportId().equals(reportId))
                .filter(ReportComment::isActive)
                .filter(c -> c.getParentCommentId() == null)
                .sorted(Comparator.comparing(ReportComment::getCreatedAt))
                .collect(Collectors.toList());
    }

    /**
     * Get replies to comment
     */
    public List<ReportComment> getReplies(Long commentId) {
        return comments.values().stream()
                .filter(c -> commentId.equals(c.getParentCommentId()))
                .sorted(Comparator.comparing(ReportComment::getCreatedAt))
                .collect(Collectors.toList());
    }

    /**
     * Update comment
     */
    public ReportComment updateComment(Long commentId, String newText, String editedBy, String reason) {
        ReportComment comment = comments.get(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + commentId);
        }

        comment.edit(newText, editedBy, reason);
        log.info("Updated comment {} by {}", commentId, editedBy);
        return comment;
    }

    /**
     * Delete comment
     */
    public void deleteComment(Long commentId) {
        ReportComment comment = comments.get(commentId);
        if (comment != null) {
            comment.softDelete();
            log.info("Deleted comment {}", commentId);
        }
    }

    /**
     * Add reaction to comment
     */
    public void addReaction(Long commentId, String emoji) {
        ReportComment comment = comments.get(commentId);
        if (comment != null) {
            comment.addReaction(emoji);
            log.info("Added reaction {} to comment {}", emoji, commentId);
        }
    }

    /**
     * Remove reaction from comment
     */
    public void removeReaction(Long commentId, String emoji) {
        ReportComment comment = comments.get(commentId);
        if (comment != null) {
            comment.removeReaction(emoji);
            log.info("Removed reaction {} from comment {}", emoji, commentId);
        }
    }

    /**
     * Resolve comment
     */
    public void resolveComment(Long commentId, String username, String resolutionComment) {
        ReportComment comment = comments.get(commentId);
        if (comment != null) {
            comment.markResolved(username, resolutionComment);
            log.info("Resolved comment {} by {}", commentId, username);
        }
    }

    /**
     * Pin comment
     */
    public void pinComment(Long commentId) {
        ReportComment comment = comments.get(commentId);
        if (comment != null) {
            comment.pin();
            log.info("Pinned comment {}", commentId);
        }
    }

    // ============================================================
    // Expiration Management
    // ============================================================

    /**
     * Check and expire old shares
     */
    public int expireOldShares() {
        int expired = 0;
        LocalDateTime now = LocalDateTime.now();

        for (ReportShare share : shares.values()) {
            if (share.getStatus() == ReportShare.ShareStatus.ACTIVE && share.isExpired()) {
                share.setStatus(ReportShare.ShareStatus.EXPIRED);
                expired++;
            }
        }

        if (expired > 0) {
            log.info("Expired {} old shares", expired);
        }

        return expired;
    }

    /**
     * Get expiring shares
     */
    public List<ReportShare> getExpiringShares(int daysAhead) {
        return shares.values().stream()
                .filter(s -> s.getStatus() == ReportShare.ShareStatus.ACTIVE)
                .filter(s -> s.isExpiringSoon(daysAhead))
                .collect(Collectors.toList());
    }

    // ============================================================
    // Statistics
    // ============================================================

    /**
     * Get sharing statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalShares", shares.size());
        stats.put("activeShares", shares.values().stream()
                .filter(ReportShare::isActive).count());
        stats.put("expiredShares", shares.values().stream()
                .filter(s -> s.getStatus() == ReportShare.ShareStatus.EXPIRED).count());
        stats.put("revokedShares", shares.values().stream()
                .filter(s -> s.getStatus() == ReportShare.ShareStatus.REVOKED).count());

        stats.put("totalComments", comments.size());
        stats.put("activeComments", comments.values().stream()
                .filter(ReportComment::isActive).count());
        stats.put("resolvedComments", comments.values().stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsResolved())).count());

        stats.put("sharesByType", shares.values().stream()
                .collect(Collectors.groupingBy(
                        s -> s.getShareType().toString(),
                        Collectors.counting())));

        return stats;
    }

    /**
     * Get report statistics
     */
    public Map<String, Object> getReportStatistics(Long reportId) {
        Map<String, Object> stats = new HashMap<>();

        List<ReportShare> reportShares = getSharesForReport(reportId);
        List<ReportComment> reportComments = getCommentsForReport(reportId);

        stats.put("shareCount", reportShares.size());
        stats.put("activeShareCount", reportShares.stream()
                .filter(ReportShare::isActive).count());
        stats.put("totalViews", reportShares.stream()
                .mapToInt(s -> s.getViewCount() != null ? s.getViewCount() : 0).sum());
        stats.put("totalDownloads", reportShares.stream()
                .mapToInt(s -> s.getDownloadCount() != null ? s.getDownloadCount() : 0).sum());

        stats.put("commentCount", reportComments.size());
        stats.put("activeCommentCount", reportComments.stream()
                .filter(ReportComment::isActive).count());
        stats.put("resolvedCommentCount", reportComments.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsResolved())).count());

        return stats;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Generate unique share token
     */
    private String generateShareToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Check if user is in share
     */
    private boolean isUserInShare(ReportShare share, String username) {
        switch (share.getShareType()) {
            case USER:
                return username.equals(share.getSharedWithUsername());
            case GROUP:
                // In production, check if user is in group
                return false;
            case DEPARTMENT:
                // In production, check if user is in department
                return false;
            case ROLE:
                // In production, check if user has role
                return false;
            case PUBLIC_LINK:
                return Boolean.TRUE.equals(share.getAllowAnonymous());
            case EMAIL:
                // In production, check if user email matches
                return false;
            default:
                return false;
        }
    }

    /**
     * Get recipient description
     */
    private String getRecipientDescription(ReportShare share) {
        return switch (share.getShareType()) {
            case USER -> "user: " + share.getSharedWithUsername();
            case GROUP -> "group: " + share.getSharedWithGroup();
            case DEPARTMENT -> "department: " + share.getSharedWithDepartment();
            case ROLE -> "role: " + share.getSharedWithRole();
            case PUBLIC_LINK -> "public link";
            case EMAIL -> "email: " + (share.getEmailAddresses() != null ?
                    share.getEmailAddresses().size() + " recipients" : "0 recipients");
        };
    }
}
