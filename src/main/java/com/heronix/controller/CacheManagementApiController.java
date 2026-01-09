package com.heronix.controller;

import com.heronix.dto.CacheStatistics;
import com.heronix.service.EnhancedCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Cache Management API Controller
 *
 * REST API endpoints for cache management and monitoring.
 *
 * Provides Endpoints For:
 * - Cache statistics retrieval
 * - Cache invalidation
 * - Cache clearing
 * - Performance monitoring
 *
 * Endpoints:
 * - GET /api/cache/stats - Get cache statistics
 * - GET /api/cache/keys - Get all cache keys
 * - DELETE /api/cache/clear - Clear entire cache
 * - DELETE /api/cache/invalidate/{key} - Invalidate specific key
 * - DELETE /api/cache/invalidate - Invalidate by pattern
 *
 * Security:
 * - Admin-only access recommended
 * - Audit logging for all cache operations
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 67 - Report Performance Optimization & Caching
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheManagementApiController {

    private final EnhancedCacheService cacheService;

    /**
     * Get cache statistics
     *
     * @return Cache statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<CacheStatistics> getCacheStatistics() {
        log.info("GET /api/cache/stats");

        try {
            CacheStatistics stats = cacheService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching cache statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all cache keys
     *
     * @return Set of cache keys
     */
    @GetMapping("/keys")
    public ResponseEntity<Set<String>> getCacheKeys() {
        log.info("GET /api/cache/keys");

        try {
            Set<String> keys = cacheService.getKeys();
            return ResponseEntity.ok(keys);

        } catch (Exception e) {
            log.error("Error fetching cache keys", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get cache size
     *
     * @return Cache size information
     */
    @GetMapping("/size")
    public ResponseEntity<Map<String, Object>> getCacheSize() {
        log.info("GET /api/cache/size");

        try {
            Map<String, Object> sizeInfo = new HashMap<>();
            sizeInfo.put("currentSize", cacheService.size());

            CacheStatistics stats = cacheService.getStatistics();
            sizeInfo.put("maxSize", stats.getMaxSize());
            sizeInfo.put("fillPercentage", stats.getFillPercentage());
            sizeInfo.put("memoryUsage", stats.getFormattedMemorySize());

            return ResponseEntity.ok(sizeInfo);

        } catch (Exception e) {
            log.error("Error fetching cache size", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Clear entire cache
     *
     * @return Success response
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        log.info("DELETE /api/cache/clear");

        try {
            int sizeBefore = cacheService.size();
            cacheService.clear();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cache cleared successfully");
            response.put("entriesRemoved", sizeBefore);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error clearing cache", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Invalidate specific cache entry
     *
     * @param key Cache key to invalidate
     * @return Success response
     */
    @DeleteMapping("/invalidate/{key}")
    public ResponseEntity<Map<String, Object>> invalidateCacheEntry(@PathVariable String key) {
        log.info("DELETE /api/cache/invalidate/{}", key);

        try {
            cacheService.invalidate(key);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cache entry invalidated");
            response.put("key", key);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error invalidating cache entry: {}", key, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Invalidate cache entries by pattern
     *
     * @param pattern Regex pattern to match keys
     * @return Success response with count
     */
    @DeleteMapping("/invalidate")
    public ResponseEntity<Map<String, Object>> invalidateByPattern(@RequestParam String pattern) {
        log.info("DELETE /api/cache/invalidate?pattern={}", pattern);

        try {
            int count = cacheService.invalidateByPattern(pattern);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cache entries invalidated");
            response.put("pattern", pattern);
            response.put("count", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error invalidating by pattern: {}", pattern, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get cache health status
     *
     * @return Health status information
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getCacheHealth() {
        log.info("GET /api/cache/health");

        try {
            CacheStatistics stats = cacheService.getStatistics();

            Map<String, Object> health = new HashMap<>();
            health.put("status", stats.getHitRate() != null && stats.getHitRate() > 50 ? "healthy" : "degraded");
            health.put("hitRate", stats.getHitRate());
            health.put("efficiencyScore", stats.getEfficiencyScore());
            health.put("size", cacheService.size());
            health.put("fillPercentage", stats.getFillPercentage());

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            log.error("Error fetching cache health", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Trigger manual cleanup of expired entries
     *
     * @return Success response
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupExpired() {
        log.info("POST /api/cache/cleanup");

        try {
            int sizeBefore = cacheService.size();
            cacheService.cleanupExpired();
            int sizeAfter = cacheService.size();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Expired entries cleaned up");
            response.put("entriesRemoved", sizeBefore - sizeAfter);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error cleaning up cache", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
