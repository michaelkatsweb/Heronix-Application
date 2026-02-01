package com.heronix.service;

import com.heronix.model.domain.RecordLock;
import com.heronix.model.domain.RecordLock.EntityType;
import com.heronix.model.domain.RecordLock.LockMode;
import com.heronix.repository.RecordLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Record Lock Service
 *
 * Manages pessimistic locking for records to prevent concurrent editing.
 * Provides lock acquisition, release, heartbeat, and notification features.
 *
 * Features:
 * - Acquire/release locks on any entity
 * - Heartbeat mechanism to keep locks alive
 * - Automatic expiry of stale locks
 * - WebSocket notifications for lock changes
 * - Admin force-unlock capability
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordLockService {

    private final RecordLockRepository lockRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${record.lock.timeout-minutes:15}")
    private int defaultLockTimeoutMinutes;

    @Value("${record.lock.heartbeat-interval-minutes:2}")
    private int heartbeatIntervalMinutes;

    @Value("${record.lock.max-locks-per-user:10}")
    private int maxLocksPerUser;

    // ==================== Lock Acquisition ====================

    /**
     * Attempt to acquire a lock on an entity
     *
     * @return LockResult with success status and lock details or conflict info
     */
    @Transactional
    public LockResult acquireLock(LockRequest request) {
        EntityType entityType = request.getEntityType();
        Long entityId = request.getEntityId();
        Long userId = request.getUserId();

        log.info("Lock request: {} {} by user {}", entityType, entityId, userId);

        // Check if user already has this lock
        Optional<RecordLock> existingUserLock = lockRepository.findLockByEntityAndUser(
            entityType, entityId, userId, LocalDateTime.now()
        );

        if (existingUserLock.isPresent()) {
            // User already has the lock - refresh it
            RecordLock lock = existingUserLock.get();
            lock.refreshHeartbeat(defaultLockTimeoutMinutes);
            lock.setSessionId(request.getSessionId());
            lockRepository.save(lock);
            log.info("Lock refreshed for user {} on {} {}", userId, entityType, entityId);
            return LockResult.success(lock, "Lock refreshed");
        }

        // Check if locked by another user
        Optional<RecordLock> existingLock = lockRepository.findActiveLock(entityType, entityId);

        if (existingLock.isPresent()) {
            RecordLock lock = existingLock.get();
            log.warn("Lock conflict: {} {} is locked by {}", entityType, entityId, lock.getLockedByUsername());
            return LockResult.conflict(lock);
        }

        // Check if user has too many locks
        long userLockCount = lockRepository.countActiveLocksByUser(userId, LocalDateTime.now());
        if (userLockCount >= maxLocksPerUser) {
            log.warn("User {} has too many locks ({})", userId, userLockCount);
            return LockResult.failure("You have too many records open. Please close some before opening more.");
        }

        // Create new lock
        RecordLock newLock = RecordLock.builder()
            .entityType(entityType)
            .entityId(entityId)
            .lockedByUserId(userId)
            .lockedByUsername(request.getUsername())
            .lockedByDisplayName(request.getDisplayName())
            .lockedByRole(request.getRole())
            .sessionId(request.getSessionId())
            .clientIpAddress(request.getClientIpAddress())
            .lockReason(request.getLockReason() != null ? request.getLockReason() : "Editing")
            .lockMode(request.getLockMode() != null ? request.getLockMode() : LockMode.EDIT)
            .lockedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusMinutes(defaultLockTimeoutMinutes))
            .lastHeartbeat(LocalDateTime.now())
            .active(true)
            .build();

        lockRepository.save(newLock);
        log.info("Lock acquired: {} {} by user {}", entityType, entityId, userId);

        // Notify other clients about the lock
        notifyLockChange(entityType, entityId, "LOCKED", newLock);

        return LockResult.success(newLock, "Lock acquired successfully");
    }

    // ==================== Lock Release ====================

    /**
     * Release a lock
     */
    @Transactional
    public boolean releaseLock(EntityType entityType, Long entityId, Long userId) {
        Optional<RecordLock> lockOpt = lockRepository.findLockByEntityAndUser(
            entityType, entityId, userId, LocalDateTime.now()
        );

        if (lockOpt.isPresent()) {
            RecordLock lock = lockOpt.get();
            lock.release();
            lockRepository.save(lock);
            log.info("Lock released: {} {} by user {}", entityType, entityId, userId);

            // Notify other clients
            notifyLockChange(entityType, entityId, "RELEASED", null);
            return true;
        }

        log.warn("No lock found to release: {} {} for user {}", entityType, entityId, userId);
        return false;
    }

    /**
     * Release a lock by ID
     */
    @Transactional
    public boolean releaseLockById(Long lockId, Long userId, boolean isAdmin) {
        Optional<RecordLock> lockOpt = lockRepository.findById(lockId);

        if (lockOpt.isEmpty()) {
            return false;
        }

        RecordLock lock = lockOpt.get();

        // Only allow release by owner or admin
        if (!lock.getLockedByUserId().equals(userId) && !isAdmin) {
            log.warn("User {} attempted to release lock owned by {}", userId, lock.getLockedByUserId());
            return false;
        }

        lock.release();
        lockRepository.save(lock);
        log.info("Lock {} released by user {} (admin={})", lockId, userId, isAdmin);

        notifyLockChange(lock.getEntityType(), lock.getEntityId(), "RELEASED", null);
        return true;
    }

    /**
     * Force release a lock (admin only)
     */
    @Transactional
    public boolean forceReleaseLock(Long lockId, Long adminUserId, String reason) {
        Optional<RecordLock> lockOpt = lockRepository.findById(lockId);

        if (lockOpt.isEmpty()) {
            return false;
        }

        RecordLock lock = lockOpt.get();
        Long originalOwner = lock.getLockedByUserId();

        lock.release();
        lockRepository.save(lock);

        log.info("Lock {} force-released by admin {} (was held by {}). Reason: {}",
            lockId, adminUserId, originalOwner, reason);

        // Notify the original owner
        notifyLockForceReleased(lock, adminUserId, reason);

        notifyLockChange(lock.getEntityType(), lock.getEntityId(), "FORCE_RELEASED", null);
        return true;
    }

    /**
     * Release all locks for a user (e.g., on logout)
     */
    @Transactional
    public int releaseAllUserLocks(Long userId) {
        int released = lockRepository.releaseAllLocksByUser(userId);
        log.info("Released {} locks for user {}", released, userId);
        return released;
    }

    /**
     * Release all locks for a session
     */
    @Transactional
    public int releaseSessionLocks(String sessionId) {
        int released = lockRepository.releaseAllLocksBySession(sessionId);
        log.info("Released {} locks for session {}", released, sessionId);
        return released;
    }

    // ==================== Heartbeat ====================

    /**
     * Send heartbeat to keep lock alive
     */
    @Transactional
    public boolean heartbeat(EntityType entityType, Long entityId, Long userId) {
        Optional<RecordLock> lockOpt = lockRepository.findLockByEntityAndUser(
            entityType, entityId, userId, LocalDateTime.now()
        );

        if (lockOpt.isPresent()) {
            RecordLock lock = lockOpt.get();
            lock.refreshHeartbeat(defaultLockTimeoutMinutes);
            lockRepository.save(lock);
            return true;
        }

        return false;
    }

    /**
     * Batch heartbeat for multiple locks
     */
    @Transactional
    public int heartbeatAll(Long userId, String sessionId) {
        List<RecordLock> locks = lockRepository.findActiveLocksBySession(sessionId, LocalDateTime.now());
        int count = 0;

        for (RecordLock lock : locks) {
            if (lock.getLockedByUserId().equals(userId)) {
                lock.refreshHeartbeat(defaultLockTimeoutMinutes);
                lockRepository.save(lock);
                count++;
            }
        }

        return count;
    }

    // ==================== Lock Queries ====================

    /**
     * Check if an entity is locked
     */
    public Optional<RecordLock> checkLock(EntityType entityType, Long entityId) {
        return lockRepository.findActiveLock(entityType, entityId);
    }

    /**
     * Check if an entity is locked by another user
     */
    public boolean isLockedByAnother(EntityType entityType, Long entityId, Long userId) {
        return lockRepository.isLockedByAnotherUser(entityType, entityId, userId, LocalDateTime.now());
    }

    /**
     * Get all active locks for a user
     */
    public List<RecordLock> getUserLocks(Long userId) {
        return lockRepository.findActiveLocksByUser(userId, LocalDateTime.now());
    }

    /**
     * Get all active locks (admin dashboard)
     */
    public List<RecordLock> getAllActiveLocks() {
        return lockRepository.findAllActiveLocks(LocalDateTime.now());
    }

    /**
     * Get lock statistics
     */
    public Map<String, Object> getLockStatistics() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        stats.put("totalActiveLocks", lockRepository.countActiveLocks(now));

        // Count by entity type
        Map<EntityType, Long> byType = new HashMap<>();
        for (EntityType type : EntityType.values()) {
            long count = lockRepository.findActiveLocksByType(type, now).size();
            if (count > 0) {
                byType.put(type, count);
            }
        }
        stats.put("locksByType", byType);

        return stats;
    }

    // ==================== Cleanup ====================

    /**
     * Scheduled cleanup of expired locks
     */
    @Scheduled(fixedDelayString = "${record.lock.cleanup-interval-ms:60000}")
    @Transactional
    public void cleanupExpiredLocks() {
        int released = lockRepository.releaseExpiredLocks(LocalDateTime.now());
        if (released > 0) {
            log.info("Released {} expired locks", released);
        }

        // Delete old inactive locks (older than 24 hours)
        int deleted = lockRepository.deleteOldLocks(LocalDateTime.now().minusHours(24));
        if (deleted > 0) {
            log.info("Deleted {} old lock records", deleted);
        }
    }

    // ==================== WebSocket Notifications ====================

    private void notifyLockChange(EntityType entityType, Long entityId, String action, RecordLock lock) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("action", action);
            message.put("entityType", entityType.name());
            message.put("entityId", entityId);
            message.put("timestamp", LocalDateTime.now().toString());

            if (lock != null) {
                message.put("lockedBy", lock.getLockedByDescription());
                message.put("lockedByUserId", lock.getLockedByUserId());
                message.put("lockMode", lock.getLockMode().name());
                message.put("lockReason", lock.getLockReason());
            }

            // Broadcast to topic for this entity
            String topic = "/topic/locks/" + entityType.name().toLowerCase() + "/" + entityId;
            messagingTemplate.convertAndSend(topic, message);

            // Also broadcast to general locks topic
            messagingTemplate.convertAndSend("/topic/locks", message);

        } catch (Exception e) {
            log.warn("Failed to send lock notification: {}", e.getMessage());
        }
    }

    private void notifyLockForceReleased(RecordLock lock, Long adminUserId, String reason) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("action", "FORCE_RELEASED");
            message.put("entityType", lock.getEntityType().name());
            message.put("entityId", lock.getEntityId());
            message.put("releasedByAdmin", adminUserId);
            message.put("reason", reason);
            message.put("timestamp", LocalDateTime.now().toString());

            // Notify the original lock owner
            String userTopic = "/topic/user/" + lock.getLockedByUserId() + "/locks";
            messagingTemplate.convertAndSend(userTopic, message);

        } catch (Exception e) {
            log.warn("Failed to send force-release notification: {}", e.getMessage());
        }
    }

    // ==================== Inner Classes ====================

    /**
     * Lock acquisition request
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LockRequest {
        private EntityType entityType;
        private Long entityId;
        private Long userId;
        private String username;
        private String displayName;
        private String role;
        private String sessionId;
        private String clientIpAddress;
        private String lockReason;
        private LockMode lockMode;
    }

    /**
     * Lock acquisition result
     */
    @lombok.Data
    @lombok.Builder
    public static class LockResult {
        private boolean success;
        private boolean conflict;
        private String message;
        private RecordLock lock;
        private RecordLock conflictingLock;

        public static LockResult success(RecordLock lock, String message) {
            return LockResult.builder()
                .success(true)
                .conflict(false)
                .message(message)
                .lock(lock)
                .build();
        }

        public static LockResult conflict(RecordLock existingLock) {
            return LockResult.builder()
                .success(false)
                .conflict(true)
                .message("Record is currently being edited by " + existingLock.getLockedByDescription())
                .conflictingLock(existingLock)
                .build();
        }

        public static LockResult failure(String message) {
            return LockResult.builder()
                .success(false)
                .conflict(false)
                .message(message)
                .build();
        }
    }
}
