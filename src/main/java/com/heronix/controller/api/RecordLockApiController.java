package com.heronix.controller.api;

import com.heronix.model.domain.RecordLock;
import com.heronix.model.domain.RecordLock.EntityType;
import com.heronix.model.domain.RecordLock.LockMode;
import com.heronix.service.RecordLockService;
import com.heronix.service.RecordLockService.LockRequest;
import com.heronix.service.RecordLockService.LockResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API Controller for Record Locking
 *
 * Provides endpoints for managing pessimistic locks on records.
 * Used by multiple SIS clients to coordinate concurrent editing.
 *
 * Endpoints:
 * - POST /api/locks/acquire - Acquire a lock
 * - POST /api/locks/release - Release a lock
 * - POST /api/locks/heartbeat - Keep lock alive
 * - GET /api/locks/check/{entityType}/{entityId} - Check lock status
 * - GET /api/locks/user/{userId} - Get user's locks
 * - GET /api/locks/all - Get all active locks (admin)
 * - DELETE /api/locks/{lockId}/force - Force release (admin)
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/locks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RecordLockApiController {

    private final RecordLockService lockService;

    // ==================== Lock Acquisition ====================

    /**
     * Acquire a lock on an entity
     */
    @PostMapping("/acquire")
    public ResponseEntity<Map<String, Object>> acquireLock(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            EntityType entityType = EntityType.valueOf((String) request.get("entityType"));
            Long entityId = Long.valueOf(request.get("entityId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());
            String username = (String) request.get("username");
            String displayName = (String) request.get("displayName");
            String role = (String) request.get("role");
            String sessionId = (String) request.get("sessionId");
            String lockReason = (String) request.getOrDefault("lockReason", "Editing");
            String lockModeStr = (String) request.getOrDefault("lockMode", "EDIT");

            LockRequest lockRequest = LockRequest.builder()
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .username(username)
                .displayName(displayName)
                .role(role)
                .sessionId(sessionId)
                .clientIpAddress(getClientIp(httpRequest))
                .lockReason(lockReason)
                .lockMode(LockMode.valueOf(lockModeStr))
                .build();

            LockResult result = lockService.acquireLock(lockRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("conflict", result.isConflict());
            response.put("message", result.getMessage());

            if (result.isSuccess() && result.getLock() != null) {
                response.put("lock", lockToMap(result.getLock()));
            }

            if (result.isConflict() && result.getConflictingLock() != null) {
                RecordLock conflict = result.getConflictingLock();
                Map<String, Object> conflictInfo = new HashMap<>();
                conflictInfo.put("lockedBy", conflict.getLockedByDescription());
                conflictInfo.put("lockedByUserId", conflict.getLockedByUserId());
                conflictInfo.put("lockedByRole", conflict.getLockedByRole());
                conflictInfo.put("lockedAt", conflict.getLockedAt().toString());
                conflictInfo.put("lockReason", conflict.getLockReason());
                conflictInfo.put("durationMinutes", conflict.getLockDurationMinutes());
                response.put("conflictingLock", conflictInfo);
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Invalid request: " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error acquiring lock", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to acquire lock: " + e.getMessage()
            ));
        }
    }

    // ==================== Lock Release ====================

    /**
     * Release a lock
     */
    @PostMapping("/release")
    public ResponseEntity<Map<String, Object>> releaseLock(@RequestBody Map<String, Object> request) {
        try {
            EntityType entityType = EntityType.valueOf((String) request.get("entityType"));
            Long entityId = Long.valueOf(request.get("entityId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());

            boolean released = lockService.releaseLock(entityType, entityId, userId);

            return ResponseEntity.ok(Map.of(
                "success", released,
                "message", released ? "Lock released" : "No lock found to release"
            ));

        } catch (Exception e) {
            log.error("Error releasing lock", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to release lock: " + e.getMessage()
            ));
        }
    }

    /**
     * Release a lock by ID
     */
    @DeleteMapping("/{lockId}")
    public ResponseEntity<Map<String, Object>> releaseLockById(
            @PathVariable Long lockId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "false") boolean isAdmin) {
        try {
            boolean released = lockService.releaseLockById(lockId, userId, isAdmin);

            return ResponseEntity.ok(Map.of(
                "success", released,
                "message", released ? "Lock released" : "Lock not found or access denied"
            ));

        } catch (Exception e) {
            log.error("Error releasing lock by ID", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to release lock: " + e.getMessage()
            ));
        }
    }

    /**
     * Force release a lock (admin only)
     */
    @DeleteMapping("/{lockId}/force")
    public ResponseEntity<Map<String, Object>> forceReleaseLock(
            @PathVariable Long lockId,
            @RequestParam Long adminUserId,
            @RequestParam(defaultValue = "Administrative action") String reason) {
        try {
            boolean released = lockService.forceReleaseLock(lockId, adminUserId, reason);

            return ResponseEntity.ok(Map.of(
                "success", released,
                "message", released ? "Lock force-released" : "Lock not found"
            ));

        } catch (Exception e) {
            log.error("Error force-releasing lock", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to force-release lock: " + e.getMessage()
            ));
        }
    }

    /**
     * Release all locks for a user (e.g., on logout)
     */
    @PostMapping("/release/user/{userId}")
    public ResponseEntity<Map<String, Object>> releaseAllUserLocks(@PathVariable Long userId) {
        try {
            int count = lockService.releaseAllUserLocks(userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "releasedCount", count,
                "message", "Released " + count + " locks"
            ));

        } catch (Exception e) {
            log.error("Error releasing user locks", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to release user locks: " + e.getMessage()
            ));
        }
    }

    /**
     * Release all locks for a session
     */
    @PostMapping("/release/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> releaseSessionLocks(@PathVariable String sessionId) {
        try {
            int count = lockService.releaseSessionLocks(sessionId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "releasedCount", count,
                "message", "Released " + count + " locks"
            ));

        } catch (Exception e) {
            log.error("Error releasing session locks", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to release session locks: " + e.getMessage()
            ));
        }
    }

    // ==================== Heartbeat ====================

    /**
     * Send heartbeat to keep lock alive
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat(@RequestBody Map<String, Object> request) {
        try {
            EntityType entityType = EntityType.valueOf((String) request.get("entityType"));
            Long entityId = Long.valueOf(request.get("entityId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());

            boolean success = lockService.heartbeat(entityType, entityId, userId);

            return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Heartbeat received" : "Lock not found"
            ));

        } catch (Exception e) {
            log.error("Error processing heartbeat", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to process heartbeat: " + e.getMessage()
            ));
        }
    }

    /**
     * Batch heartbeat for all locks in a session
     */
    @PostMapping("/heartbeat/session")
    public ResponseEntity<Map<String, Object>> heartbeatSession(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String sessionId = (String) request.get("sessionId");

            int count = lockService.heartbeatAll(userId, sessionId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "refreshedCount", count
            ));

        } catch (Exception e) {
            log.error("Error processing batch heartbeat", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to process heartbeat: " + e.getMessage()
            ));
        }
    }

    // ==================== Lock Queries ====================

    /**
     * Check if an entity is locked
     */
    @GetMapping("/check/{entityType}/{entityId}")
    public ResponseEntity<Map<String, Object>> checkLock(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(required = false) Long userId) {
        try {
            EntityType type = EntityType.valueOf(entityType.toUpperCase());
            Optional<RecordLock> lockOpt = lockService.checkLock(type, entityId);

            Map<String, Object> response = new HashMap<>();
            response.put("entityType", entityType);
            response.put("entityId", entityId);
            response.put("isLocked", lockOpt.isPresent());

            if (lockOpt.isPresent()) {
                RecordLock lock = lockOpt.get();
                response.put("lock", lockToMap(lock));
                response.put("isLockedByCurrentUser", userId != null && lock.getLockedByUserId().equals(userId));
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid entity type: " + entityType
            ));
        } catch (Exception e) {
            log.error("Error checking lock", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to check lock: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all locks held by a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserLocks(@PathVariable Long userId) {
        try {
            List<RecordLock> locks = lockService.getUserLocks(userId);

            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "lockCount", locks.size(),
                "locks", locks.stream().map(this::lockToMap).collect(Collectors.toList())
            ));

        } catch (Exception e) {
            log.error("Error getting user locks", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to get user locks: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all active locks (admin dashboard)
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllActiveLocks() {
        try {
            List<RecordLock> locks = lockService.getAllActiveLocks();
            Map<String, Object> stats = lockService.getLockStatistics();

            return ResponseEntity.ok(Map.of(
                "totalLocks", locks.size(),
                "statistics", stats,
                "locks", locks.stream().map(this::lockToMap).collect(Collectors.toList())
            ));

        } catch (Exception e) {
            log.error("Error getting all locks", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to get locks: " + e.getMessage()
            ));
        }
    }

    /**
     * Get lock statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getLockStatistics() {
        try {
            return ResponseEntity.ok(lockService.getLockStatistics());
        } catch (Exception e) {
            log.error("Error getting lock statistics", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to get statistics: " + e.getMessage()
            ));
        }
    }

    // ==================== Reference Data ====================

    /**
     * Get supported entity types
     */
    @GetMapping("/entity-types")
    public ResponseEntity<List<Map<String, String>>> getEntityTypes() {
        List<Map<String, String>> types = Arrays.stream(EntityType.values())
            .map(t -> Map.of(
                "value", t.name(),
                "displayName", formatEntityType(t)
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(types);
    }

    /**
     * Get lock modes
     */
    @GetMapping("/lock-modes")
    public ResponseEntity<List<Map<String, String>>> getLockModes() {
        List<Map<String, String>> modes = Arrays.stream(LockMode.values())
            .map(m -> Map.of(
                "value", m.name(),
                "description", formatLockMode(m)
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(modes);
    }

    // ==================== Helpers ====================

    private Map<String, Object> lockToMap(RecordLock lock) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", lock.getId());
        map.put("entityType", lock.getEntityType().name());
        map.put("entityId", lock.getEntityId());
        map.put("lockedByUserId", lock.getLockedByUserId());
        map.put("lockedByUsername", lock.getLockedByUsername());
        map.put("lockedByDisplayName", lock.getLockedByDisplayName());
        map.put("lockedByDescription", lock.getLockedByDescription());
        map.put("lockedByRole", lock.getLockedByRole());
        map.put("lockReason", lock.getLockReason());
        map.put("lockMode", lock.getLockMode().name());
        map.put("lockedAt", lock.getLockedAt().toString());
        map.put("expiresAt", lock.getExpiresAt().toString());
        map.put("lastHeartbeat", lock.getLastHeartbeat().toString());
        map.put("durationMinutes", lock.getLockDurationMinutes());
        map.put("active", lock.isActive());
        map.put("valid", lock.isValid());
        return map;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String formatEntityType(EntityType type) {
        return type.name().replace("_", " ");
    }

    private String formatLockMode(LockMode mode) {
        switch (mode) {
            case VIEW: return "Viewing - others can view but not edit";
            case EDIT: return "Editing - others see warning";
            case EXCLUSIVE: return "Exclusive - no other access allowed";
            default: return mode.name();
        }
    }
}
