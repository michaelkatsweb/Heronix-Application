package com.heronix.repository;

import com.heronix.model.domain.ReportNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Report Notification Repository
 *
 * Data access layer for report notification management.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 62 - Real-time Report Notifications & Alerts
 */
@Repository
public interface ReportNotificationRepository extends JpaRepository<ReportNotification, Long> {

    /**
     * Find notifications by recipient
     */
    List<ReportNotification> findByRecipientOrderByCreatedAtDesc(String recipient);

    /**
     * Find unread notifications by recipient
     */
    List<ReportNotification> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(String recipient);

    /**
     * Find notifications by type
     */
    List<ReportNotification> findByNotificationTypeOrderByCreatedAtDesc(
            ReportNotification.NotificationType notificationType);

    /**
     * Find urgent notifications by recipient
     */
    List<ReportNotification> findByRecipientAndPriorityOrderByCreatedAtDesc(
            String recipient, ReportNotification.NotificationPriority priority);

    /**
     * Find unsent notifications
     */
    List<ReportNotification> findByIsSentFalseOrderByCreatedAtAsc();

    /**
     * Find expired notifications
     */
    @Query("SELECT n FROM ReportNotification n WHERE n.expiresAt < :now")
    List<ReportNotification> findExpiredNotifications(@Param("now") LocalDateTime now);

    /**
     * Count unread notifications for recipient
     */
    long countByRecipientAndIsReadFalse(String recipient);

    /**
     * Count urgent notifications for recipient
     */
    long countByRecipientAndPriorityAndIsReadFalse(
            String recipient, ReportNotification.NotificationPriority priority);

    /**
     * Mark notification as read
     */
    @Modifying
    @Query("UPDATE ReportNotification n SET n.isRead = true, n.readAt = :readAt WHERE n.id = :id")
    void markAsRead(@Param("id") Long id, @Param("readAt") LocalDateTime readAt);

    /**
     * Mark all notifications as read for recipient
     */
    @Modifying
    @Query("UPDATE ReportNotification n SET n.isRead = true, n.readAt = :readAt WHERE n.recipient = :recipient AND n.isRead = false")
    void markAllAsReadForRecipient(@Param("recipient") String recipient,
                                   @Param("readAt") LocalDateTime readAt);

    /**
     * Delete expired notifications
     */
    @Modifying
    @Query("DELETE FROM ReportNotification n WHERE n.expiresAt < :now")
    void deleteExpiredNotifications(@Param("now") LocalDateTime now);

    /**
     * Find recent notifications (last N days)
     */
    @Query("SELECT n FROM ReportNotification n WHERE n.recipient = :recipient AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<ReportNotification> findRecentNotifications(
            @Param("recipient") String recipient,
            @Param("since") LocalDateTime since);
}
