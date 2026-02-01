package com.heronix.repository;

import com.heronix.model.domain.RecordLock;
import com.heronix.model.domain.RecordLock.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RecordLock entity
 *
 * Provides data access for record locking system.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 */
@Repository
public interface RecordLockRepository extends JpaRepository<RecordLock, Long> {

    /**
     * Find active lock for a specific entity
     */
    @Query("SELECT r FROM RecordLock r WHERE r.entityType = :entityType " +
           "AND r.entityId = :entityId AND r.active = true AND r.expiresAt > :now")
    Optional<RecordLock> findActiveLock(
        @Param("entityType") EntityType entityType,
        @Param("entityId") Long entityId,
        @Param("now") LocalDateTime now
    );

    /**
     * Find active lock for a specific entity (convenience method)
     */
    default Optional<RecordLock> findActiveLock(EntityType entityType, Long entityId) {
        return findActiveLock(entityType, entityId, LocalDateTime.now());
    }

    /**
     * Find all active locks for an entity type
     */
    @Query("SELECT r FROM RecordLock r WHERE r.entityType = :entityType " +
           "AND r.active = true AND r.expiresAt > :now ORDER BY r.lockedAt DESC")
    List<RecordLock> findActiveLocksByType(
        @Param("entityType") EntityType entityType,
        @Param("now") LocalDateTime now
    );

    /**
     * Find all active locks held by a user
     */
    @Query("SELECT r FROM RecordLock r WHERE r.lockedByUserId = :userId " +
           "AND r.active = true AND r.expiresAt > :now ORDER BY r.lockedAt DESC")
    List<RecordLock> findActiveLocksByUser(
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now
    );

    /**
     * Find all active locks for a session
     */
    @Query("SELECT r FROM RecordLock r WHERE r.sessionId = :sessionId " +
           "AND r.active = true AND r.expiresAt > :now")
    List<RecordLock> findActiveLocksBySession(
        @Param("sessionId") String sessionId,
        @Param("now") LocalDateTime now
    );

    /**
     * Find all expired but still marked active locks (for cleanup)
     */
    @Query("SELECT r FROM RecordLock r WHERE r.active = true AND r.expiresAt <= :now")
    List<RecordLock> findExpiredLocks(@Param("now") LocalDateTime now);

    /**
     * Count active locks by user
     */
    @Query("SELECT COUNT(r) FROM RecordLock r WHERE r.lockedByUserId = :userId " +
           "AND r.active = true AND r.expiresAt > :now")
    long countActiveLocksByUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Count active locks system-wide
     */
    @Query("SELECT COUNT(r) FROM RecordLock r WHERE r.active = true AND r.expiresAt > :now")
    long countActiveLocks(@Param("now") LocalDateTime now);

    /**
     * Release all locks for a user
     */
    @Modifying
    @Query("UPDATE RecordLock r SET r.active = false WHERE r.lockedByUserId = :userId AND r.active = true")
    int releaseAllLocksByUser(@Param("userId") Long userId);

    /**
     * Release all locks for a session
     */
    @Modifying
    @Query("UPDATE RecordLock r SET r.active = false WHERE r.sessionId = :sessionId AND r.active = true")
    int releaseAllLocksBySession(@Param("sessionId") String sessionId);

    /**
     * Release expired locks
     */
    @Modifying
    @Query("UPDATE RecordLock r SET r.active = false WHERE r.active = true AND r.expiresAt <= :now")
    int releaseExpiredLocks(@Param("now") LocalDateTime now);

    /**
     * Delete old inactive locks (cleanup)
     */
    @Modifying
    @Query("DELETE FROM RecordLock r WHERE r.active = false AND r.expiresAt < :cutoff")
    int deleteOldLocks(@Param("cutoff") LocalDateTime cutoff);

    /**
     * Find all active locks (for admin dashboard)
     */
    @Query("SELECT r FROM RecordLock r WHERE r.active = true AND r.expiresAt > :now " +
           "ORDER BY r.entityType, r.lockedAt DESC")
    List<RecordLock> findAllActiveLocks(@Param("now") LocalDateTime now);

    /**
     * Check if entity is locked by another user
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RecordLock r " +
           "WHERE r.entityType = :entityType AND r.entityId = :entityId " +
           "AND r.lockedByUserId != :userId AND r.active = true AND r.expiresAt > :now")
    boolean isLockedByAnotherUser(
        @Param("entityType") EntityType entityType,
        @Param("entityId") Long entityId,
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now
    );

    /**
     * Find lock by entity and user (to check if user already has lock)
     */
    @Query("SELECT r FROM RecordLock r WHERE r.entityType = :entityType " +
           "AND r.entityId = :entityId AND r.lockedByUserId = :userId " +
           "AND r.active = true AND r.expiresAt > :now")
    Optional<RecordLock> findLockByEntityAndUser(
        @Param("entityType") EntityType entityType,
        @Param("entityId") Long entityId,
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now
    );
}
