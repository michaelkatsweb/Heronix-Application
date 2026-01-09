package com.heronix.service;

import com.heronix.dto.ReportCollaboration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Report Collaboration Service
 *
 * Manages collaboration, sharing, and teamwork features for reports.
 *
 * Features:
 * - Collaborator management
 * - Comments and discussions
 * - Activity tracking
 * - Shared links
 * - Version control
 * - Tasks and assignments
 * - Approval workflows
 * - Real-time notifications
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 85 - Report Collaboration & Sharing
 */
@Service
@Slf4j
public class ReportCollaborationService {

    private final Map<Long, ReportCollaboration> collaborations = new ConcurrentHashMap<>();
    private Long nextCollaborationId = 1L;

    /**
     * Create collaboration
     */
    public ReportCollaboration createCollaboration(ReportCollaboration collaboration) {
        synchronized (this) {
            collaboration.setCollaborationId(nextCollaborationId++);
            collaboration.setCreatedAt(LocalDateTime.now());
            collaboration.setTotalCollaborators(0);
            collaboration.setActiveCollaborators(0);
            collaboration.setTotalComments(0);
            collaboration.setUnresolvedComments(0);
            collaboration.setTotalActivities(0L);
            collaboration.setRecentActivities(0);
            collaboration.setTotalViews(0L);
            collaboration.setTotalEdits(0L);
            collaboration.setTotalShares(0);
            collaboration.setTotalTasks(0);
            collaboration.setCompletedTasks(0);
            collaboration.setPendingTasks(0);
            collaboration.setOverdueTasks(0);

            // Set defaults
            if (collaboration.getDefaultPermissionLevel() == null) {
                collaboration.setDefaultPermissionLevel(ReportCollaboration.PermissionLevel.VIEWER);
            }

            if (collaboration.getCommentsEnabled() == null) {
                collaboration.setCommentsEnabled(true);
            }

            if (collaboration.getAllowThreadedDiscussions() == null) {
                collaboration.setAllowThreadedDiscussions(true);
            }

            if (collaboration.getMentionsEnabled() == null) {
                collaboration.setMentionsEnabled(true);
            }

            if (collaboration.getNotificationsEnabled() == null) {
                collaboration.setNotificationsEnabled(true);
            }

            if (collaboration.getVersionControlEnabled() == null) {
                collaboration.setVersionControlEnabled(true);
            }

            if (collaboration.getCurrentVersion() == null) {
                collaboration.setCurrentVersion("1.0");
            }

            if (collaboration.getAutoSaveEnabled() == null) {
                collaboration.setAutoSaveEnabled(true);
            }

            if (collaboration.getAutoSaveIntervalSeconds() == null) {
                collaboration.setAutoSaveIntervalSeconds(60);
            }

            collaborations.put(collaboration.getCollaborationId(), collaboration);

            log.info("Created collaboration {} for report {} with type {}",
                    collaboration.getCollaborationId(), collaboration.getReportId(),
                    collaboration.getCollaborationType());

            // Log activity
            logActivity(collaboration, ReportCollaboration.ActivityType.SHARED,
                    collaboration.getOwner(), "Collaboration created", null);

            return collaboration;
        }
    }

    /**
     * Get collaboration
     */
    public Optional<ReportCollaboration> getCollaboration(Long collaborationId) {
        return Optional.ofNullable(collaborations.get(collaborationId));
    }

    /**
     * Get collaboration by report
     */
    public Optional<ReportCollaboration> getCollaborationByReport(Long reportId) {
        return collaborations.values().stream()
                .filter(c -> reportId.equals(c.getReportId()))
                .findFirst();
    }

    /**
     * Add collaborator
     */
    public void addCollaborator(Long collaborationId, String userId, String username,
                               ReportCollaboration.PermissionLevel permissionLevel, String addedBy) {
        ReportCollaboration collaboration = collaborations.get(collaborationId);
        if (collaboration == null) {
            throw new IllegalArgumentException("Collaboration not found: " + collaborationId);
        }

        ReportCollaboration.Collaborator collaborator = ReportCollaboration.Collaborator.builder()
                .userId(userId)
                .username(username)
                .displayName(username)
                .permissionLevel(permissionLevel)
                .addedAt(LocalDateTime.now())
                .addedBy(addedBy)
                .viewCount(0L)
                .editCount(0L)
                .commentCount(0L)
                .active(true)
                .build();

        collaboration.addCollaborator(collaborator);

        log.info("Added collaborator {} to collaboration {} with permission {}",
                username, collaborationId, permissionLevel);

        logActivity(collaboration, ReportCollaboration.ActivityType.SHARED,
                addedBy, "Added collaborator: " + username, null);
    }

    /**
     * Remove collaborator
     */
    public void removeCollaborator(Long collaborationId, String userId, String removedBy) {
        ReportCollaboration collaboration = collaborations.get(collaborationId);
        if (collaboration == null) {
            throw new IllegalArgumentException("Collaboration not found: " + collaborationId);
        }

        ReportCollaboration.Collaborator collaborator = collaboration.getCollaborator(userId);
        if (collaborator != null) {
            collaboration.removeCollaborator(userId);

            log.info("Removed collaborator {} from collaboration {}", userId, collaborationId);

            logActivity(collaboration, ReportCollaboration.ActivityType.SHARED,
                    removedBy, "Removed collaborator: " + collaborator.getUsername(), null);
        }
    }

    /**
     * Add comment
     */
    public ReportCollaboration.Comment addComment(Long collaborationId, String username,
                                                  ReportCollaboration.CommentType type,
                                                  String content, String parentCommentId) {
        ReportCollaboration collaboration = collaborations.get(collaborationId);
        if (collaboration == null) {
            throw new IllegalArgumentException("Collaboration not found: " + collaborationId);
        }

        if (!Boolean.TRUE.equals(collaboration.getCommentsEnabled())) {
            throw new IllegalStateException("Comments are not enabled");
        }

        // Check permission
        if (!collaboration.canComment(username)) {
            throw new IllegalStateException("User does not have permission to comment");
        }

        // Extract mentions
        List<String> mentions = extractMentions(content);

        ReportCollaboration.Comment comment = ReportCollaboration.Comment.builder()
                .commentId(UUID.randomUUID().toString())
                .username(username)
                .displayName(username)
                .type(type)
                .content(content)
                .parentCommentId(parentCommentId)
                .createdAt(LocalDateTime.now())
                .edited(false)
                .resolved(false)
                .mentions(mentions)
                .likeCount(0)
                .build();

        collaboration.addComment(comment);

        // Update collaborator stats
        updateCollaboratorStats(collaboration, username, "comment");

        log.info("Added comment to collaboration {}: {} by {}", collaborationId, type, username);

        logActivity(collaboration, ReportCollaboration.ActivityType.COMMENTED,
                username, "Added comment", null);

        // Notify mentioned users
        if (Boolean.TRUE.equals(collaboration.getMentionsEnabled()) && mentions != null) {
            for (String mentioned : mentions) {
                notifyUser(collaboration, mentioned, "mentioned", username);
            }
        }

        return comment;
    }

    /**
     * Extract mentions from content
     */
    private List<String> extractMentions(String content) {
        List<String> mentions = new ArrayList<>();
        if (content == null) {
            return mentions;
        }

        // Simple mention extraction (look for @username pattern)
        String[] words = content.split("\\s+");
        for (String word : words) {
            if (word.startsWith("@") && word.length() > 1) {
                mentions.add(word.substring(1));
            }
        }

        return mentions;
    }

    /**
     * Resolve comment
     */
    public void resolveComment(Long collaborationId, String commentId, String resolvedBy) {
        ReportCollaboration collaboration = collaborations.get(collaborationId);
        if (collaboration == null) {
            throw new IllegalArgumentException("Collaboration not found: " + collaborationId);
        }

        collaboration.resolveComment(commentId, resolvedBy);

        log.info("Resolved comment {} in collaboration {}", commentId, collaborationId);

        logActivity(collaboration, ReportCollaboration.ActivityType.COMMENTED,
                resolvedBy, "Resolved comment", null);
    }

    /**
     * Create shared link
     */
    public ReportCollaboration.SharedLink createSharedLink(Long collaborationId,
                                                          ReportCollaboration.ShareType shareType,
                                                          ReportCollaboration.PermissionLevel permissionLevel,
                                                          String sharedBy) {
        ReportCollaboration collaboration = collaborations.get(collaborationId);
        if (collaboration == null) {
            throw new IllegalArgumentException("Collaboration not found: " + collaborationId);
        }

        if (!Boolean.TRUE.equals(collaboration.getAllowLinkSharing())) {
            throw new IllegalStateException("Link sharing is not allowed");
        }

        ReportCollaboration.SharedLink link = collaboration.createSharedLink(shareType, permissionLevel, sharedBy);

        log.info("Created shared link {} for collaboration {} with permission {}",
                link.getLinkId(), collaborationId, permissionLevel);

        logActivity(collaboration, ReportCollaboration.ActivityType.SHARED,
                sharedBy, "Created shared link", null);

        return link;
    }

    /**
     * Track view
     */
    public void trackView(Long collaborationId, String username) {
        ReportCollaboration collaboration = collaborations.get(collaborationId);
        if (collaboration == null) {
            return;
        }

        if (!Boolean.TRUE.equals(collaboration.getTrackViews())) {
            return;
        }

        collaboration.setTotalViews((collaboration.getTotalViews() != null ? collaboration.getTotalViews() : 0) + 1);

        // Update collaborator stats
        updateCollaboratorStats(collaboration, username, "view");

        logActivity(collaboration, ReportCollaboration.ActivityType.VIEWED,
                username, "Viewed report", null);
    }

    /**
     * Track edit
     */
    public void trackEdit(Long collaborationId, String username, String details) {
        ReportCollaboration collaboration = collaborations.get(collaborationId);
        if (collaboration == null) {
            return;
        }

        if (!Boolean.TRUE.equals(collaboration.getTrackEdits())) {
            return;
        }

        if (!collaboration.canEdit(username)) {
            throw new IllegalStateException("User does not have permission to edit");
        }

        collaboration.setTotalEdits((collaboration.getTotalEdits() != null ? collaboration.getTotalEdits() : 0) + 1);

        // Update collaborator stats
        updateCollaboratorStats(collaboration, username, "edit");

        logActivity(collaboration, ReportCollaboration.ActivityType.EDITED,
                username, "Edited report", details);
    }

    /**
     * Update collaborator stats
     */
    private void updateCollaboratorStats(ReportCollaboration collaboration, String username, String action) {
        ReportCollaboration.Collaborator collaborator = collaboration.getCollaborator(username);
        if (collaborator == null) {
            return;
        }

        collaborator.setLastActive(LocalDateTime.now());

        switch (action) {
            case "view" -> collaborator.setViewCount(
                    (collaborator.getViewCount() != null ? collaborator.getViewCount() : 0) + 1);
            case "edit" -> collaborator.setEditCount(
                    (collaborator.getEditCount() != null ? collaborator.getEditCount() : 0) + 1);
            case "comment" -> collaborator.setCommentCount(
                    (collaborator.getCommentCount() != null ? collaborator.getCommentCount() : 0) + 1);
        }
    }

    /**
     * Add task
     */
    public ReportCollaboration.Task addTask(Long collaborationId, String title, String description,
                                           String assignedTo, String assignedBy,
                                           LocalDateTime dueDate, String priority) {
        ReportCollaboration collaboration = collaborations.get(collaborationId);
        if (collaboration == null) {
            throw new IllegalArgumentException("Collaboration not found: " + collaborationId);
        }

        if (!Boolean.TRUE.equals(collaboration.getTasksEnabled())) {
            throw new IllegalStateException("Tasks are not enabled");
        }

        ReportCollaboration.Task task = ReportCollaboration.Task.builder()
                .taskId(UUID.randomUUID().toString())
                .title(title)
                .description(description)
                .assignedTo(assignedTo)
                .assignedBy(assignedBy)
                .status("PENDING")
                .priority(priority != null ? priority : "MEDIUM")
                .dueDate(dueDate)
                .createdAt(LocalDateTime.now())
                .build();

        collaboration.addTask(task);

        log.info("Added task {} to collaboration {}: assigned to {}",
                task.getTaskId(), collaborationId, assignedTo);

        logActivity(collaboration, ReportCollaboration.ActivityType.ASSIGNED,
                assignedBy, "Assigned task to " + assignedTo + ": " + title, null);

        // Notify assigned user
        notifyUser(collaboration, assignedTo, "task_assigned", assignedBy);

        return task;
    }

    /**
     * Complete task
     */
    public void completeTask(Long collaborationId, String taskId, String completedBy) {
        ReportCollaboration collaboration = collaborations.get(collaborationId);
        if (collaboration == null) {
            throw new IllegalArgumentException("Collaboration not found: " + collaborationId);
        }

        collaboration.completeTask(taskId, completedBy);

        log.info("Completed task {} in collaboration {}", taskId, collaborationId);

        logActivity(collaboration, ReportCollaboration.ActivityType.ASSIGNED,
                completedBy, "Completed task", null);
    }

    /**
     * Log activity
     */
    private void logActivity(ReportCollaboration collaboration,
                            ReportCollaboration.ActivityType activityType,
                            String username, String action, String details) {
        ReportCollaboration.Activity activity = ReportCollaboration.Activity.builder()
                .activityId(UUID.randomUUID().toString())
                .activityType(activityType)
                .username(username)
                .displayName(username)
                .action(action)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        collaboration.logActivity(activity);
    }

    /**
     * Notify user
     */
    private void notifyUser(ReportCollaboration collaboration, String username,
                           String notificationType, String triggeredBy) {
        if (!Boolean.TRUE.equals(collaboration.getNotificationsEnabled())) {
            return;
        }

        log.debug("Sending {} notification to {} from {} for collaboration {}",
                notificationType, username, triggeredBy, collaboration.getCollaborationId());

        // In a real implementation, this would send actual notifications
        // (email, push, in-app, etc.)
    }

    /**
     * Get activity feed
     */
    public List<ReportCollaboration.Activity> getActivityFeed(Long collaborationId, int limit) {
        ReportCollaboration collaboration = collaborations.get(collaborationId);
        if (collaboration == null || collaboration.getActivityFeed() == null) {
            return new ArrayList<>();
        }

        return collaboration.getActivityFeed().stream()
                .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get comments
     */
    public List<ReportCollaboration.Comment> getComments(Long collaborationId, boolean unresolvedOnly) {
        ReportCollaboration collaboration = collaborations.get(collaborationId);
        if (collaboration == null || collaboration.getComments() == null) {
            return new ArrayList<>();
        }

        if (unresolvedOnly) {
            return collaboration.getComments().stream()
                    .filter(c -> !Boolean.TRUE.equals(c.getResolved()))
                    .collect(Collectors.toList());
        }

        return collaboration.getComments();
    }

    /**
     * Delete collaboration
     */
    public void deleteCollaboration(Long collaborationId) {
        ReportCollaboration removed = collaborations.remove(collaborationId);
        if (removed != null) {
            log.info("Deleted collaboration {}", collaborationId);
        }
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalCollaborations", collaborations.size());

        long totalCollaborators = collaborations.values().stream()
                .mapToLong(c -> c.getTotalCollaborators() != null ? c.getTotalCollaborators() : 0)
                .sum();

        long totalComments = collaborations.values().stream()
                .mapToLong(c -> c.getTotalComments() != null ? c.getTotalComments() : 0)
                .sum();

        long totalShares = collaborations.values().stream()
                .mapToLong(c -> c.getTotalShares() != null ? c.getTotalShares() : 0)
                .sum();

        long totalTasks = collaborations.values().stream()
                .mapToLong(c -> c.getTotalTasks() != null ? c.getTotalTasks() : 0)
                .sum();

        stats.put("totalCollaborators", totalCollaborators);
        stats.put("totalComments", totalComments);
        stats.put("totalShares", totalShares);
        stats.put("totalTasks", totalTasks);

        double avgCollaboratorsPerReport = collaborations.isEmpty() ? 0.0 :
                (double) totalCollaborators / collaborations.size();

        stats.put("averageCollaboratorsPerReport", avgCollaboratorsPerReport);

        // Count by collaboration type
        Map<ReportCollaboration.CollaborationType, Long> byType = collaborations.values().stream()
                .collect(Collectors.groupingBy(ReportCollaboration::getCollaborationType, Collectors.counting()));
        stats.put("collaborationsByType", byType);

        return stats;
    }
}
