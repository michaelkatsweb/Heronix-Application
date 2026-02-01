package com.heronix.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * RecordLock Entity
 *
 * Implements pessimistic locking for records to prevent concurrent editing.
 * When a user opens a record for editing, a lock is created. Other users
 * see a warning showing who has the lock.
 *
 * Features:
 * - Supports any entity type (Student, Teacher, Course, etc.)
 * - Automatic timeout for stale locks
 * - Heartbeat mechanism for active sessions
 * - Force unlock capability for admins
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 68 - January 2026
 */
@Entity
@Table(name = "record_locks", indexes = {
    @Index(name = "idx_lock_entity", columnList = "entityType, entityId"),
    @Index(name = "idx_lock_user", columnList = "lockedByUserId"),
    @Index(name = "idx_lock_expiry", columnList = "expiresAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type of entity being locked (e.g., "STUDENT", "TEACHER", "COURSE")
     */
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    /**
     * ID of the entity being locked
     */
    @Column(nullable = false)
    private Long entityId;

    /**
     * User ID who holds the lock
     */
    @Column(nullable = false)
    private Long lockedByUserId;

    /**
     * Username for display purposes
     */
    @Column(nullable = false, length = 100)
    private String lockedByUsername;

    /**
     * Display name of the user (e.g., "John Smith")
     */
    @Column(length = 150)
    private String lockedByDisplayName;

    /**
     * User's role (e.g., "ADMIN", "COUNSELOR", "TEACHER")
     */
    @Column(length = 50)
    private String lockedByRole;

    /**
     * Client/session identifier (for multiple tabs/windows)
     */
    @Column(length = 100)
    private String sessionId;

    /**
     * IP address of the client holding the lock
     */
    @Column(length = 50)
    private String clientIpAddress;

    /**
     * Reason for the lock (e.g., "Editing", "Reviewing", "Data Entry")
     */
    @Column(length = 100)
    @Builder.Default
    private String lockReason = "Editing";

    /**
     * When the lock was acquired
     */
    @Column(nullable = false)
    private LocalDateTime lockedAt;

    /**
     * When the lock expires (auto-release if no heartbeat)
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Last heartbeat from the client (proves session is still active)
     */
    @Column(nullable = false)
    private LocalDateTime lastHeartbeat;

    /**
     * Whether the lock is still active
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Lock mode - READ allows others to view, WRITE prevents all access
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LockMode lockMode = LockMode.EDIT;

    /**
     * Entity types that can be locked
     */
    public enum EntityType {
        STUDENT,
        TEACHER,
        COURSE,
        COURSE_SECTION,
        ROOM,
        SCHEDULE,
        IEP,
        PLAN_504,
        HEALTH_RECORD,
        ENROLLMENT,
        GRADE,
        ATTENDANCE,
        DISCIPLINE,
        PARENT,
        USER,
        REPORT,
        CONFIGURATION
    }

    /**
     * Lock modes
     */
    public enum LockMode {
        /** User is viewing - warn others but allow read */
        VIEW,
        /** User is editing - warn others, prevent edits */
        EDIT,
        /** Exclusive lock - prevent all access */
        EXCLUSIVE
    }

    /**
     * Check if the lock has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if the lock is still valid (active and not expired)
     */
    public boolean isValid() {
        return active && !isExpired();
    }

    /**
     * Extend the lock expiry time (heartbeat)
     */
    public void refreshHeartbeat(int extensionMinutes) {
        this.lastHeartbeat = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(extensionMinutes);
    }

    /**
     * Release the lock
     */
    public void release() {
        this.active = false;
    }

    /**
     * Get a display-friendly description of who holds the lock
     */
    public String getLockedByDescription() {
        if (lockedByDisplayName != null && !lockedByDisplayName.isEmpty()) {
            return lockedByDisplayName + " (" + lockedByUsername + ")";
        }
        return lockedByUsername;
    }

    /**
     * Get how long the lock has been held
     */
    public long getLockDurationMinutes() {
        return java.time.Duration.between(lockedAt, LocalDateTime.now()).toMinutes();
    }
}
