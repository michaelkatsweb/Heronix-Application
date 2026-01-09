package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Report Share DTO
 *
 * Defines sharing configuration for reports.
 *
 * Share Types:
 * - Direct user share
 * - Group share
 * - Public link share
 * - Department share
 * - Role-based share
 *
 * Features:
 * - Access control
 * - Expiration dates
 * - View/comment/edit permissions
 * - Share tracking
 * - Notification on share
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 71 - Report Collaboration & Sharing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportShare {

    /**
     * Share type enumeration
     */
    public enum ShareType {
        USER,           // Shared with specific user
        GROUP,          // Shared with user group
        DEPARTMENT,     // Shared with department
        ROLE,           // Shared with role
        PUBLIC_LINK,    // Public link share
        EMAIL           // Shared via email
    }

    /**
     * Access level enumeration
     */
    public enum AccessLevel {
        VIEW,           // View only
        COMMENT,        // View and comment
        EDIT,           // View, comment, and edit
        FULL            // Full access including delete
    }

    /**
     * Share status enumeration
     */
    public enum ShareStatus {
        ACTIVE,         // Currently active
        EXPIRED,        // Expired
        REVOKED,        // Revoked by owner
        PENDING         // Pending acceptance
    }

    // ============================================================
    // Basic Share Information
    // ============================================================

    /**
     * Share ID
     */
    private Long shareId;

    /**
     * Report ID being shared
     */
    private Long reportId;

    /**
     * Report name
     */
    private String reportName;

    /**
     * Share type
     */
    private ShareType shareType;

    /**
     * Access level
     */
    private AccessLevel accessLevel;

    /**
     * Share status
     */
    private ShareStatus status;

    // ============================================================
    // Recipient Information
    // ============================================================

    /**
     * Shared with username (for USER type)
     */
    private String sharedWithUsername;

    /**
     * Shared with user display name
     */
    private String sharedWithDisplayName;

    /**
     * Shared with group name (for GROUP type)
     */
    private String sharedWithGroup;

    /**
     * Shared with department (for DEPARTMENT type)
     */
    private String sharedWithDepartment;

    /**
     * Shared with role (for ROLE type)
     */
    private String sharedWithRole;

    /**
     * Email addresses (for EMAIL type)
     */
    private List<String> emailAddresses;

    // ============================================================
    // Share Owner Information
    // ============================================================

    /**
     * Shared by username
     */
    private String sharedBy;

    /**
     * Shared by display name
     */
    private String sharedByDisplayName;

    /**
     * Shared at timestamp
     */
    private LocalDateTime sharedAt;

    // ============================================================
    // Access Control
    // ============================================================

    /**
     * Can view report
     */
    private Boolean canView;

    /**
     * Can download report
     */
    private Boolean canDownload;

    /**
     * Can comment on report
     */
    private Boolean canComment;

    /**
     * Can edit report parameters
     */
    private Boolean canEditParameters;

    /**
     * Can regenerate report
     */
    private Boolean canRegenerate;

    /**
     * Can share with others
     */
    private Boolean canReshare;

    /**
     * Can delete report
     */
    private Boolean canDelete;

    // ============================================================
    // Expiration and Limits
    // ============================================================

    /**
     * Expires at timestamp
     */
    private LocalDateTime expiresAt;

    /**
     * Maximum views allowed
     */
    private Integer maxViews;

    /**
     * Current view count
     */
    private Integer viewCount;

    /**
     * Maximum downloads allowed
     */
    private Integer maxDownloads;

    /**
     * Current download count
     */
    private Integer downloadCount;

    /**
     * Require password to access
     */
    private Boolean requirePassword;

    /**
     * Access password (hashed)
     */
    private String passwordHash;

    // ============================================================
    // Public Link Share
    // ============================================================

    /**
     * Share token (for public links)
     */
    private String shareToken;

    /**
     * Public link URL
     */
    private String publicLinkUrl;

    /**
     * Allow anonymous access
     */
    private Boolean allowAnonymous;

    /**
     * Track anonymous views
     */
    private Boolean trackAnonymousViews;

    // ============================================================
    // Notifications
    // ============================================================

    /**
     * Notify recipient on share
     */
    private Boolean notifyOnShare;

    /**
     * Notify owner on view
     */
    private Boolean notifyOwnerOnView;

    /**
     * Notify owner on comment
     */
    private Boolean notifyOwnerOnComment;

    /**
     * Send reminder before expiration
     */
    private Boolean sendExpirationReminder;

    /**
     * Reminder days before expiration
     */
    private Integer reminderDaysBefore;

    // ============================================================
    // Activity Tracking
    // ============================================================

    /**
     * Last viewed at
     */
    private LocalDateTime lastViewedAt;

    /**
     * Last downloaded at
     */
    private LocalDateTime lastDownloadedAt;

    /**
     * Last commented at
     */
    private LocalDateTime lastCommentedAt;

    /**
     * Access history
     */
    private List<ShareAccessLog> accessHistory;

    // ============================================================
    // Additional Settings
    // ============================================================

    /**
     * Custom message to recipient
     */
    private String message;

    /**
     * Tags for organization
     */
    private Set<String> tags;

    /**
     * Notes (visible to owner only)
     */
    private String notes;

    /**
     * Metadata
     */
    private java.util.Map<String, Object> metadata;

    // ============================================================
    // Nested Classes
    // ============================================================

    /**
     * Share access log entry
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareAccessLog {
        private LocalDateTime accessTime;
        private String username;
        private String action;  // VIEW, DOWNLOAD, COMMENT, EDIT
        private String ipAddress;
        private String userAgent;
        private String details;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Check if share is active
     */
    public boolean isActive() {
        if (status != ShareStatus.ACTIVE) {
            return false;
        }

        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }

        if (maxViews != null && viewCount != null && viewCount >= maxViews) {
            return false;
        }

        if (maxDownloads != null && downloadCount != null && downloadCount >= maxDownloads) {
            return false;
        }

        return true;
    }

    /**
     * Check if share is expired
     */
    public boolean isExpired() {
        if (status == ShareStatus.EXPIRED) {
            return true;
        }

        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return true;
        }

        return false;
    }

    /**
     * Check if action is allowed
     */
    public boolean isActionAllowed(String action) {
        if (!isActive()) {
            return false;
        }

        return switch (action.toUpperCase()) {
            case "VIEW" -> Boolean.TRUE.equals(canView);
            case "DOWNLOAD" -> Boolean.TRUE.equals(canDownload);
            case "COMMENT" -> Boolean.TRUE.equals(canComment);
            case "EDIT" -> Boolean.TRUE.equals(canEditParameters);
            case "REGENERATE" -> Boolean.TRUE.equals(canRegenerate);
            case "RESHARE" -> Boolean.TRUE.equals(canReshare);
            case "DELETE" -> Boolean.TRUE.equals(canDelete);
            default -> false;
        };
    }

    /**
     * Increment view count
     */
    public void incrementViewCount() {
        viewCount = (viewCount != null ? viewCount : 0) + 1;
        lastViewedAt = LocalDateTime.now();
    }

    /**
     * Increment download count
     */
    public void incrementDownloadCount() {
        downloadCount = (downloadCount != null ? downloadCount : 0) + 1;
        lastDownloadedAt = LocalDateTime.now();
    }

    /**
     * Check if views remaining
     */
    public boolean hasViewsRemaining() {
        if (maxViews == null) {
            return true;
        }
        return viewCount == null || viewCount < maxViews;
    }

    /**
     * Check if downloads remaining
     */
    public boolean hasDownloadsRemaining() {
        if (maxDownloads == null) {
            return true;
        }
        return downloadCount == null || downloadCount < maxDownloads;
    }

    /**
     * Get remaining days until expiration
     */
    public Long getDaysUntilExpiration() {
        if (expiresAt == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiresAt)) {
            return 0L;
        }

        return java.time.Duration.between(now, expiresAt).toDays();
    }

    /**
     * Check if expiring soon
     */
    public boolean isExpiringSoon(int days) {
        Long daysRemaining = getDaysUntilExpiration();
        return daysRemaining != null && daysRemaining <= days && daysRemaining > 0;
    }

    /**
     * Log access
     */
    public void logAccess(String username, String action, String ipAddress) {
        if (accessHistory == null) {
            accessHistory = new java.util.ArrayList<>();
        }

        ShareAccessLog log = ShareAccessLog.builder()
                .accessTime(LocalDateTime.now())
                .username(username)
                .action(action)
                .ipAddress(ipAddress)
                .build();

        accessHistory.add(log);
    }

    /**
     * Validate share configuration
     */
    public void validate() {
        if (reportId == null) {
            throw new IllegalArgumentException("Report ID is required");
        }

        if (shareType == null) {
            throw new IllegalArgumentException("Share type is required");
        }

        if (accessLevel == null) {
            throw new IllegalArgumentException("Access level is required");
        }

        if (sharedBy == null || sharedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Shared by username is required");
        }

        // Type-specific validation
        switch (shareType) {
            case USER:
                if (sharedWithUsername == null || sharedWithUsername.trim().isEmpty()) {
                    throw new IllegalArgumentException("Shared with username is required for USER share type");
                }
                break;
            case GROUP:
                if (sharedWithGroup == null || sharedWithGroup.trim().isEmpty()) {
                    throw new IllegalArgumentException("Shared with group is required for GROUP share type");
                }
                break;
            case DEPARTMENT:
                if (sharedWithDepartment == null || sharedWithDepartment.trim().isEmpty()) {
                    throw new IllegalArgumentException("Shared with department is required for DEPARTMENT share type");
                }
                break;
            case ROLE:
                if (sharedWithRole == null || sharedWithRole.trim().isEmpty()) {
                    throw new IllegalArgumentException("Shared with role is required for ROLE share type");
                }
                break;
            case EMAIL:
                if (emailAddresses == null || emailAddresses.isEmpty()) {
                    throw new IllegalArgumentException("Email addresses are required for EMAIL share type");
                }
                break;
            case PUBLIC_LINK:
                if (shareToken == null || shareToken.trim().isEmpty()) {
                    throw new IllegalArgumentException("Share token is required for PUBLIC_LINK share type");
                }
                break;
        }
    }

    /**
     * Create default permissions for access level
     */
    public void applyAccessLevelDefaults() {
        switch (accessLevel) {
            case VIEW:
                canView = true;
                canDownload = true;
                canComment = false;
                canEditParameters = false;
                canRegenerate = false;
                canReshare = false;
                canDelete = false;
                break;
            case COMMENT:
                canView = true;
                canDownload = true;
                canComment = true;
                canEditParameters = false;
                canRegenerate = false;
                canReshare = false;
                canDelete = false;
                break;
            case EDIT:
                canView = true;
                canDownload = true;
                canComment = true;
                canEditParameters = true;
                canRegenerate = true;
                canReshare = false;
                canDelete = false;
                break;
            case FULL:
                canView = true;
                canDownload = true;
                canComment = true;
                canEditParameters = true;
                canRegenerate = true;
                canReshare = true;
                canDelete = true;
                break;
        }
    }
}
