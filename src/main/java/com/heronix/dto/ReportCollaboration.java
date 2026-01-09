package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Collaboration DTO
 *
 * Represents collaboration and sharing configuration for reports.
 *
 * Features:
 * - Team collaboration and shared workspaces
 * - Real-time comments and annotations
 * - Activity feeds and notifications
 * - Share links with access control
 * - Co-editing and version control
 * - @mentions and task assignments
 * - Discussion threads
 * - Approval workflows
 *
 * Collaboration Types:
 * - Private (individual access)
 * - Team (shared within team)
 * - Organization (shared within organization)
 * - Public (publicly accessible)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 85 - Report Collaboration & Sharing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCollaboration {

    /**
     * Collaboration type enumeration
     */
    public enum CollaborationType {
        PRIVATE,            // Private access
        TEAM,               // Team collaboration
        DEPARTMENT,         // Department-wide
        ORGANIZATION,       // Organization-wide
        PUBLIC,             // Public access
        EXTERNAL,           // External sharing
        CUSTOM              // Custom access
    }

    /**
     * Permission level enumeration
     */
    public enum PermissionLevel {
        VIEWER,             // View only
        COMMENTER,          // Can comment
        EDITOR,             // Can edit
        ADMIN,              // Full control
        OWNER               // Owner
    }

    /**
     * Activity type enumeration
     */
    public enum ActivityType {
        VIEWED,             // Report viewed
        EDITED,             // Report edited
        COMMENTED,          // Comment added
        SHARED,             // Report shared
        DOWNLOADED,         // Report downloaded
        EXPORTED,           // Report exported
        MENTIONED,          // User mentioned
        ASSIGNED,           // Task assigned
        APPROVED,           // Report approved
        REJECTED            // Report rejected
    }

    /**
     * Comment type enumeration
     */
    public enum CommentType {
        GENERAL,            // General comment
        ANNOTATION,         // Data annotation
        SUGGESTION,         // Suggestion
        QUESTION,           // Question
        ISSUE,              // Issue/problem
        APPROVAL,           // Approval comment
        RESOLUTION          // Resolution
    }

    /**
     * Share type enumeration
     */
    public enum ShareType {
        DIRECT,             // Direct share to users
        LINK,               // Share via link
        EMAIL,              // Email invitation
        EMBED,              // Embedded view
        API,                // API access
        PUBLIC_URL          // Public URL
    }

    // ============================================================
    // Basic Information
    // ============================================================

    /**
     * Collaboration ID
     */
    private Long collaborationId;

    /**
     * Report ID
     */
    private Long reportId;

    /**
     * Report name
     */
    private String reportName;

    /**
     * Collaboration type
     */
    private CollaborationType collaborationType;

    /**
     * Owner
     */
    private String owner;

    /**
     * Created at
     */
    private LocalDateTime createdAt;

    /**
     * Updated at
     */
    private LocalDateTime updatedAt;

    /**
     * Last activity at
     */
    private LocalDateTime lastActivityAt;

    // ============================================================
    // Permissions & Access
    // ============================================================

    /**
     * Collaborators
     */
    private List<Collaborator> collaborators;

    /**
     * Team permissions
     */
    private Map<String, PermissionLevel> teamPermissions;

    /**
     * Default permission level
     */
    private PermissionLevel defaultPermissionLevel;

    /**
     * Allow public access
     */
    private Boolean allowPublicAccess;

    /**
     * Allow external sharing
     */
    private Boolean allowExternalSharing;

    /**
     * Require authentication
     */
    private Boolean requireAuthentication;

    /**
     * Total collaborators
     */
    private Integer totalCollaborators;

    /**
     * Active collaborators (last 7 days)
     */
    private Integer activeCollaborators;

    // ============================================================
    // Comments & Discussions
    // ============================================================

    /**
     * Comments enabled
     */
    private Boolean commentsEnabled;

    /**
     * Comments
     */
    private List<Comment> comments;

    /**
     * Total comments
     */
    private Integer totalComments;

    /**
     * Unresolved comments
     */
    private Integer unresolvedComments;

    /**
     * Allow threaded discussions
     */
    private Boolean allowThreadedDiscussions;

    /**
     * Mentions enabled
     */
    private Boolean mentionsEnabled;

    /**
     * Comment moderation enabled
     */
    private Boolean commentModerationEnabled;

    // ============================================================
    // Activity Tracking
    // ============================================================

    /**
     * Activity feed
     */
    private List<Activity> activityFeed;

    /**
     * Total activities
     */
    private Long totalActivities;

    /**
     * Recent activities (last 24h)
     */
    private Integer recentActivities;

    /**
     * Track views
     */
    private Boolean trackViews;

    /**
     * Track edits
     */
    private Boolean trackEdits;

    /**
     * Total views
     */
    private Long totalViews;

    /**
     * Total edits
     */
    private Long totalEdits;

    // ============================================================
    // Sharing
    // ============================================================

    /**
     * Shared links
     */
    private List<SharedLink> sharedLinks;

    /**
     * Total shares
     */
    private Integer totalShares;

    /**
     * Share expiration enabled
     */
    private Boolean shareExpirationEnabled;

    /**
     * Default share expiration (days)
     */
    private Integer defaultShareExpirationDays;

    /**
     * Allow link sharing
     */
    private Boolean allowLinkSharing;

    /**
     * Require password for links
     */
    private Boolean requirePasswordForLinks;

    /**
     * Download enabled for viewers
     */
    private Boolean downloadEnabledForViewers;

    // ============================================================
    // Notifications
    // ============================================================

    /**
     * Notifications enabled
     */
    private Boolean notificationsEnabled;

    /**
     * Notification settings
     */
    private NotificationSettings notificationSettings;

    /**
     * Email notifications enabled
     */
    private Boolean emailNotificationsEnabled;

    /**
     * In-app notifications enabled
     */
    private Boolean inAppNotificationsEnabled;

    /**
     * Notify on comment
     */
    private Boolean notifyOnComment;

    /**
     * Notify on mention
     */
    private Boolean notifyOnMention;

    /**
     * Notify on share
     */
    private Boolean notifyOnShare;

    // ============================================================
    // Version Control
    // ============================================================

    /**
     * Version control enabled
     */
    private Boolean versionControlEnabled;

    /**
     * Current version
     */
    private String currentVersion;

    /**
     * Version history
     */
    private List<Version> versionHistory;

    /**
     * Total versions
     */
    private Integer totalVersions;

    /**
     * Allow rollback
     */
    private Boolean allowRollback;

    /**
     * Auto-save enabled
     */
    private Boolean autoSaveEnabled;

    /**
     * Auto-save interval (seconds)
     */
    private Integer autoSaveIntervalSeconds;

    // ============================================================
    // Tasks & Assignments
    // ============================================================

    /**
     * Tasks enabled
     */
    private Boolean tasksEnabled;

    /**
     * Tasks
     */
    private List<Task> tasks;

    /**
     * Total tasks
     */
    private Integer totalTasks;

    /**
     * Completed tasks
     */
    private Integer completedTasks;

    /**
     * Pending tasks
     */
    private Integer pendingTasks;

    /**
     * Overdue tasks
     */
    private Integer overdueTasks;

    // ============================================================
    // Approvals
    // ============================================================

    /**
     * Approval workflow enabled
     */
    private Boolean approvalWorkflowEnabled;

    /**
     * Pending approvals
     */
    private List<Approval> pendingApprovals;

    /**
     * Approval history
     */
    private List<Approval> approvalHistory;

    /**
     * Requires approval
     */
    private Boolean requiresApproval;

    /**
     * Approval status
     */
    private String approvalStatus; // PENDING, APPROVED, REJECTED

    /**
     * Approved by
     */
    private List<String> approvedBy;

    /**
     * Rejected by
     */
    private List<String> rejectedBy;

    // ============================================================
    // Settings
    // ============================================================

    /**
     * Enable real-time collaboration
     */
    private Boolean enableRealTimeCollaboration;

    /**
     * Show presence indicators
     */
    private Boolean showPresenceIndicators;

    /**
     * Currently active users
     */
    private List<String> currentlyActiveUsers;

    /**
     * Allow anonymous comments
     */
    private Boolean allowAnonymousComments;

    /**
     * Moderation required
     */
    private Boolean moderationRequired;

    /**
     * Moderators
     */
    private List<String> moderators;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Tags
     */
    private List<String> tags;

    /**
     * Custom properties
     */
    private Map<String, Object> customProperties;

    /**
     * Notes
     */
    private String notes;

    // ============================================================
    // Nested Classes
    // ============================================================

    /**
     * Collaborator
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Collaborator {
        private String userId;
        private String username;
        private String displayName;
        private String email;
        private PermissionLevel permissionLevel;
        private LocalDateTime addedAt;
        private String addedBy;
        private LocalDateTime lastActive;
        private Long viewCount;
        private Long editCount;
        private Long commentCount;
        private Boolean active;
    }

    /**
     * Comment
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Comment {
        private String commentId;
        private String username;
        private String displayName;
        private CommentType type;
        private String content;
        private String parentCommentId;
        private List<Comment> replies;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Boolean edited;
        private Boolean resolved;
        private LocalDateTime resolvedAt;
        private String resolvedBy;
        private List<String> mentions;
        private List<String> attachments;
        private Integer likeCount;
        private Map<String, Object> metadata;
    }

    /**
     * Activity
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Activity {
        private String activityId;
        private ActivityType activityType;
        private String username;
        private String displayName;
        private String action;
        private String details;
        private LocalDateTime timestamp;
        private String ipAddress;
        private Map<String, Object> metadata;
    }

    /**
     * Shared link
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SharedLink {
        private String linkId;
        private String linkUrl;
        private ShareType shareType;
        private PermissionLevel permissionLevel;
        private String sharedBy;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private Boolean expired;
        private Boolean passwordProtected;
        private String password; // Encrypted
        private Integer accessCount;
        private LocalDateTime lastAccessed;
        private Boolean enabled;
        private Map<String, Object> restrictions;
    }

    /**
     * Notification settings
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationSettings {
        private Boolean emailOnComment;
        private Boolean emailOnMention;
        private Boolean emailOnShare;
        private Boolean emailOnEdit;
        private Boolean emailOnApproval;
        private String notificationFrequency; // IMMEDIATE, DAILY, WEEKLY
        private List<String> mutedUsers;
        private Boolean digestEnabled;
    }

    /**
     * Version
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Version {
        private String versionId;
        private String versionNumber;
        private String createdBy;
        private LocalDateTime createdAt;
        private String comment;
        private Boolean current;
        private Long size;
        private Map<String, Object> changes;
    }

    /**
     * Task
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Task {
        private String taskId;
        private String title;
        private String description;
        private String assignedTo;
        private String assignedBy;
        private String status; // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
        private String priority; // LOW, MEDIUM, HIGH, URGENT
        private LocalDateTime dueDate;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
        private String completedBy;
        private List<String> attachments;
        private Map<String, Object> metadata;
    }

    /**
     * Approval
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Approval {
        private String approvalId;
        private String approver;
        private String status; // PENDING, APPROVED, REJECTED
        private String comment;
        private LocalDateTime requestedAt;
        private LocalDateTime respondedAt;
        private Boolean required;
        private Integer sequence;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Add collaborator
     */
    public void addCollaborator(Collaborator collaborator) {
        if (collaborators == null) {
            collaborators = new java.util.ArrayList<>();
        }
        collaborators.add(collaborator);
        totalCollaborators = collaborators.size();
    }

    /**
     * Remove collaborator
     */
    public void removeCollaborator(String userId) {
        if (collaborators != null) {
            collaborators.removeIf(c -> c.getUserId().equals(userId));
            totalCollaborators = collaborators.size();
        }
    }

    /**
     * Add comment
     */
    public void addComment(Comment comment) {
        if (comments == null) {
            comments = new java.util.ArrayList<>();
        }
        comments.add(comment);
        totalComments = (totalComments != null ? totalComments : 0) + 1;

        if (!Boolean.TRUE.equals(comment.getResolved())) {
            unresolvedComments = (unresolvedComments != null ? unresolvedComments : 0) + 1;
        }
    }

    /**
     * Resolve comment
     */
    public void resolveComment(String commentId, String resolvedBy) {
        if (comments != null) {
            comments.stream()
                    .filter(c -> c.getCommentId().equals(commentId))
                    .findFirst()
                    .ifPresent(comment -> {
                        comment.setResolved(true);
                        comment.setResolvedAt(LocalDateTime.now());
                        comment.setResolvedBy(resolvedBy);
                        unresolvedComments = Math.max(0, (unresolvedComments != null ? unresolvedComments : 1) - 1);
                    });
        }
    }

    /**
     * Log activity
     */
    public void logActivity(Activity activity) {
        if (activityFeed == null) {
            activityFeed = new java.util.ArrayList<>();
        }
        activityFeed.add(activity);
        totalActivities = (totalActivities != null ? totalActivities : 0) + 1;
        lastActivityAt = LocalDateTime.now();

        // Keep only last 1000 activities
        if (activityFeed.size() > 1000) {
            activityFeed.remove(0);
        }
    }

    /**
     * Create shared link
     */
    public SharedLink createSharedLink(ShareType shareType, PermissionLevel permission, String sharedBy) {
        SharedLink link = SharedLink.builder()
                .linkId(java.util.UUID.randomUUID().toString())
                .linkUrl("/share/" + java.util.UUID.randomUUID().toString())
                .shareType(shareType)
                .permissionLevel(permission)
                .sharedBy(sharedBy)
                .createdAt(LocalDateTime.now())
                .expired(false)
                .enabled(true)
                .accessCount(0)
                .build();

        if (Boolean.TRUE.equals(shareExpirationEnabled) && defaultShareExpirationDays != null) {
            link.setExpiresAt(LocalDateTime.now().plusDays(defaultShareExpirationDays));
        }

        if (sharedLinks == null) {
            sharedLinks = new java.util.ArrayList<>();
        }
        sharedLinks.add(link);
        totalShares = (totalShares != null ? totalShares : 0) + 1;

        return link;
    }

    /**
     * Add task
     */
    public void addTask(Task task) {
        if (tasks == null) {
            tasks = new java.util.ArrayList<>();
        }
        tasks.add(task);
        totalTasks = (totalTasks != null ? totalTasks : 0) + 1;

        if ("PENDING".equals(task.getStatus())) {
            pendingTasks = (pendingTasks != null ? pendingTasks : 0) + 1;
        }
    }

    /**
     * Complete task
     */
    public void completeTask(String taskId, String completedBy) {
        if (tasks != null) {
            tasks.stream()
                    .filter(t -> t.getTaskId().equals(taskId))
                    .findFirst()
                    .ifPresent(task -> {
                        task.setStatus("COMPLETED");
                        task.setCompletedAt(LocalDateTime.now());
                        task.setCompletedBy(completedBy);

                        pendingTasks = Math.max(0, (pendingTasks != null ? pendingTasks : 1) - 1);
                        completedTasks = (completedTasks != null ? completedTasks : 0) + 1;
                    });
        }
    }

    /**
     * Calculate task completion rate
     */
    public Double getTaskCompletionRate() {
        if (totalTasks == null || totalTasks == 0) {
            return 0.0;
        }
        int completed = completedTasks != null ? completedTasks : 0;
        return (completed * 100.0) / totalTasks;
    }

    /**
     * Check if has unresolved comments
     */
    public boolean hasUnresolvedComments() {
        return unresolvedComments != null && unresolvedComments > 0;
    }

    /**
     * Check if has pending approvals
     */
    public boolean hasPendingApprovals() {
        return pendingApprovals != null && !pendingApprovals.isEmpty();
    }

    /**
     * Get collaborator by user ID
     */
    public Collaborator getCollaborator(String userId) {
        if (collaborators == null) {
            return null;
        }
        return collaborators.stream()
                .filter(c -> c.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if user is collaborator
     */
    public boolean isCollaborator(String userId) {
        return getCollaborator(userId) != null;
    }

    /**
     * Get permission level for user
     */
    public PermissionLevel getPermissionLevel(String userId) {
        if (owner != null && owner.equals(userId)) {
            return PermissionLevel.OWNER;
        }

        Collaborator collaborator = getCollaborator(userId);
        if (collaborator != null) {
            return collaborator.getPermissionLevel();
        }

        return defaultPermissionLevel;
    }

    /**
     * Check if user can edit
     */
    public boolean canEdit(String userId) {
        PermissionLevel level = getPermissionLevel(userId);
        return level == PermissionLevel.EDITOR ||
               level == PermissionLevel.ADMIN ||
               level == PermissionLevel.OWNER;
    }

    /**
     * Check if user can comment
     */
    public boolean canComment(String userId) {
        PermissionLevel level = getPermissionLevel(userId);
        return level == PermissionLevel.COMMENTER ||
               level == PermissionLevel.EDITOR ||
               level == PermissionLevel.ADMIN ||
               level == PermissionLevel.OWNER;
    }
}
