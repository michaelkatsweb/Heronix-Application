package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Report Comment DTO
 *
 * Represents comments on reports for collaboration.
 *
 * Features:
 * - Threaded comments (replies)
 * - Mentions (@username)
 * - Attachments
 * - Edit history
 * - Reactions
 * - Status tracking
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 71 - Report Collaboration & Sharing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportComment {

    /**
     * Comment type enumeration
     */
    public enum CommentType {
        GENERAL,        // General comment
        QUESTION,       // Question about the report
        SUGGESTION,     // Suggestion for improvement
        ISSUE,          // Issue or problem
        APPROVAL,       // Approval comment
        REJECTION       // Rejection comment
    }

    /**
     * Comment status enumeration
     */
    public enum CommentStatus {
        ACTIVE,         // Active comment
        EDITED,         // Edited comment
        DELETED,        // Deleted (soft delete)
        RESOLVED,       // Issue resolved
        ARCHIVED        // Archived
    }

    // ============================================================
    // Basic Comment Information
    // ============================================================

    /**
     * Comment ID
     */
    private Long commentId;

    /**
     * Report ID
     */
    private Long reportId;

    /**
     * Parent comment ID (for replies)
     */
    private Long parentCommentId;

    /**
     * Comment type
     */
    private CommentType commentType;

    /**
     * Comment status
     */
    private CommentStatus status;

    /**
     * Comment text
     */
    private String text;

    // ============================================================
    // Author Information
    // ============================================================

    /**
     * Author username
     */
    private String authorUsername;

    /**
     * Author display name
     */
    private String authorDisplayName;

    /**
     * Author role
     */
    private String authorRole;

    /**
     * Created at timestamp
     */
    private LocalDateTime createdAt;

    // ============================================================
    // Edit Information
    // ============================================================

    /**
     * Is edited
     */
    private Boolean isEdited;

    /**
     * Last edited at
     */
    private LocalDateTime editedAt;

    /**
     * Edited by username
     */
    private String editedBy;

    /**
     * Edit count
     */
    private Integer editCount;

    /**
     * Edit history
     */
    private List<CommentEdit> editHistory;

    // ============================================================
    // Threading and Replies
    // ============================================================

    /**
     * Reply count
     */
    private Integer replyCount;

    /**
     * Replies (nested comments)
     */
    private List<ReportComment> replies;

    /**
     * Thread depth level
     */
    private Integer threadLevel;

    // ============================================================
    // Mentions and References
    // ============================================================

    /**
     * Mentioned usernames
     */
    private List<String> mentions;

    /**
     * Referenced report section
     */
    private String referencedSection;

    /**
     * Referenced page number
     */
    private Integer referencedPage;

    /**
     * Highlighted text
     */
    private String highlightedText;

    // ============================================================
    // Attachments
    // ============================================================

    /**
     * Attachment URLs
     */
    private List<String> attachmentUrls;

    /**
     * Attachment file names
     */
    private List<String> attachmentNames;

    /**
     * Has attachments
     */
    private Boolean hasAttachments;

    // ============================================================
    // Reactions and Engagement
    // ============================================================

    /**
     * Reaction counts (emoji -> count)
     */
    private java.util.Map<String, Integer> reactions;

    /**
     * Like count
     */
    private Integer likeCount;

    /**
     * Is pinned
     */
    private Boolean isPinned;

    /**
     * Is highlighted
     */
    private Boolean isHighlighted;

    // ============================================================
    // Resolution Tracking
    // ============================================================

    /**
     * Is resolved
     */
    private Boolean isResolved;

    /**
     * Resolved at
     */
    private LocalDateTime resolvedAt;

    /**
     * Resolved by username
     */
    private String resolvedBy;

    /**
     * Resolution comment
     */
    private String resolutionComment;

    // ============================================================
    // Visibility and Permissions
    // ============================================================

    /**
     * Is private (visible only to owner and author)
     */
    private Boolean isPrivate;

    /**
     * Visible to roles
     */
    private List<String> visibleToRoles;

    /**
     * Visible to users
     */
    private List<String> visibleToUsers;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Tags
     */
    private List<String> tags;

    /**
     * Priority (1=low, 5=high)
     */
    private Integer priority;

    /**
     * Custom metadata
     */
    private java.util.Map<String, Object> metadata;

    // ============================================================
    // Nested Classes
    // ============================================================

    /**
     * Comment edit history entry
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentEdit {
        private LocalDateTime editedAt;
        private String editedBy;
        private String previousText;
        private String newText;
        private String reason;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Check if comment is active
     */
    public boolean isActive() {
        return status == CommentStatus.ACTIVE || status == CommentStatus.EDITED;
    }

    /**
     * Check if comment is deleted
     */
    public boolean isDeleted() {
        return status == CommentStatus.DELETED;
    }

    /**
     * Check if comment is a reply
     */
    public boolean isReply() {
        return parentCommentId != null;
    }

    /**
     * Check if comment has replies
     */
    public boolean hasReplies() {
        return replyCount != null && replyCount > 0;
    }

    /**
     * Add reply
     */
    public void addReply(ReportComment reply) {
        if (replies == null) {
            replies = new java.util.ArrayList<>();
        }
        replies.add(reply);
        replyCount = replies.size();
    }

    /**
     * Add mention
     */
    public void addMention(String username) {
        if (mentions == null) {
            mentions = new java.util.ArrayList<>();
        }
        if (!mentions.contains(username)) {
            mentions.add(username);
        }
    }

    /**
     * Add reaction
     */
    public void addReaction(String emoji) {
        if (reactions == null) {
            reactions = new java.util.HashMap<>();
        }
        reactions.put(emoji, reactions.getOrDefault(emoji, 0) + 1);
    }

    /**
     * Remove reaction
     */
    public void removeReaction(String emoji) {
        if (reactions != null && reactions.containsKey(emoji)) {
            int count = reactions.get(emoji);
            if (count <= 1) {
                reactions.remove(emoji);
            } else {
                reactions.put(emoji, count - 1);
            }
        }
    }

    /**
     * Mark as resolved
     */
    public void markResolved(String username, String resolutionComment) {
        this.isResolved = true;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = username;
        this.resolutionComment = resolutionComment;
        this.status = CommentStatus.RESOLVED;
    }

    /**
     * Mark as unresolved
     */
    public void markUnresolved() {
        this.isResolved = false;
        this.resolvedAt = null;
        this.resolvedBy = null;
        this.resolutionComment = null;
        this.status = CommentStatus.ACTIVE;
    }

    /**
     * Edit comment
     */
    public void edit(String newText, String editedBy, String reason) {
        if (editHistory == null) {
            editHistory = new java.util.ArrayList<>();
        }

        CommentEdit edit = CommentEdit.builder()
                .editedAt(LocalDateTime.now())
                .editedBy(editedBy)
                .previousText(this.text)
                .newText(newText)
                .reason(reason)
                .build();

        editHistory.add(edit);
        this.text = newText;
        this.isEdited = true;
        this.editedAt = LocalDateTime.now();
        this.editedBy = editedBy;
        this.editCount = (editCount != null ? editCount : 0) + 1;
        this.status = CommentStatus.EDITED;
    }

    /**
     * Soft delete comment
     */
    public void softDelete() {
        this.status = CommentStatus.DELETED;
        this.text = "[Comment deleted]";
    }

    /**
     * Pin comment
     */
    public void pin() {
        this.isPinned = true;
    }

    /**
     * Unpin comment
     */
    public void unpin() {
        this.isPinned = false;
    }

    /**
     * Check if user can edit
     */
    public boolean canEdit(String username) {
        return authorUsername != null && authorUsername.equals(username);
    }

    /**
     * Check if user can delete
     */
    public boolean canDelete(String username) {
        return authorUsername != null && authorUsername.equals(username);
    }

    /**
     * Get total reaction count
     */
    public int getTotalReactions() {
        if (reactions == null) {
            return 0;
        }
        return reactions.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Validate comment
     */
    public void validate() {
        if (reportId == null) {
            throw new IllegalArgumentException("Report ID is required");
        }

        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text is required");
        }

        if (authorUsername == null || authorUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Author username is required");
        }

        if (text.length() > 5000) {
            throw new IllegalArgumentException("Comment text exceeds maximum length of 5000 characters");
        }
    }
}
