package com.heronix.controller;

import com.heronix.dto.ReportCache;
import com.heronix.service.ReportPerformanceCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Cache API Controller
 *
 * REST API endpoints for cache management and monitoring.
 *
 * Endpoints:
 * - POST /api/cache - Put data in cache
 * - GET /api/cache/{key} - Get cached data
 * - GET /api/cache/{key}/metadata - Get cache metadata
 * - DELETE /api/cache/{key} - Invalidate cache entry
 * - POST /api/cache/invalidate/pattern - Invalidate by pattern
 * - POST /api/cache/invalidate/tags - Invalidate by tags
 * - POST /api/cache/{key}/refresh - Refresh cache entry
 * - POST /api/cache/{key}/extend-ttl - Extend TTL
 * - GET /api/cache - Get all cache entries
 * - GET /api/cache/type/{type} - Get cache entries by type
 * - GET /api/cache/status/{status} - Get cache entries by status
 * - DELETE /api/cache/evict/expired - Evict expired entries
 * - POST /api/cache/evict/policy - Evict by policy
 * - DELETE /api/cache - Clear all cache
 * - DELETE /api/cache/type/{type} - Clear by type
 * - POST /api/cache/warm - Warm cache
 * - GET /api/cache/stats - Get cache statistics
 * - GET /api/cache/health - Get cache health report
 * - GET /api/cache/top-accessed - Get top accessed entries
 * - GET /api/cache/largest - Get largest entries
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 78 - Report Caching & Performance Optimization
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
public class ReportCacheApiController {

    private final ReportPerformanceCacheService cacheService;

    /**
     * Put data in cache
     */
    @PostMapping
    public ResponseEntity<ReportCache> putCache(@RequestBody Map<String, Object> request) {
        log.info("POST /api/cache");

        try {
            String cacheKey = (String) request.get("cacheKey");
            String data = (String) request.get("data");
            String cacheTypeStr = (String) request.get("cacheType");
            Integer ttlSeconds = request.get("ttlSeconds") != null ?
                    Integer.valueOf(request.get("ttlSeconds").toString()) : null;

            ReportCache.CacheType cacheType = ReportCache.CacheType.valueOf(cacheTypeStr.toUpperCase());

            ReportCache cache = cacheService.put(cacheKey, data, cacheType, ttlSeconds);
            return ResponseEntity.ok(cache);

        } catch (Exception e) {
            log.error("Error caching data", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get cached data
     */
    @GetMapping("/{key}")
    public ResponseEntity<Map<String, Object>> getCache(@PathVariable String key) {
        log.info("GET /api/cache/{}", key);

        try {
            return cacheService.get(key)
                    .map(data -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("data", data);
                        response.put("hit", true);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("hit", false);
                        return ResponseEntity.ok(response);
                    });

        } catch (Exception e) {
            log.error("Error getting cache: {}", key, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get cache metadata
     */
    @GetMapping("/{key}/metadata")
    public ResponseEntity<ReportCache> getCacheMetadata(@PathVariable String key) {
        log.info("GET /api/cache/{}/metadata", key);

        try {
            return cacheService.getCacheMetadata(key)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error getting cache metadata: {}", key, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Invalidate cache entry
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, Object>> invalidateCache(
            @PathVariable String key,
            @RequestParam(required = false, defaultValue = "Manual invalidation") String reason) {
        log.info("DELETE /api/cache/{}", key);

        try {
            cacheService.invalidate(key, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cache invalidated");
            response.put("cacheKey", key);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error invalidating cache: {}", key, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Invalidate by pattern
     */
    @PostMapping("/invalidate/pattern")
    public ResponseEntity<Map<String, Object>> invalidateByPattern(@RequestBody Map<String, String> request) {
        log.info("POST /api/cache/invalidate/pattern");

        try {
            String pattern = request.get("pattern");
            int count = cacheService.invalidateByPattern(pattern);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Caches invalidated");
            response.put("pattern", pattern);
            response.put("count", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error invalidating by pattern", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Invalidate by tags
     */
    @PostMapping("/invalidate/tags")
    public ResponseEntity<Map<String, Object>> invalidateByTags(@RequestBody Map<String, List<String>> request) {
        log.info("POST /api/cache/invalidate/tags");

        try {
            List<String> tags = request.get("tags");
            int count = cacheService.invalidateByTags(tags);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Caches invalidated");
            response.put("tags", tags);
            response.put("count", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error invalidating by tags", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Refresh cache entry
     */
    @PostMapping("/{key}/refresh")
    public ResponseEntity<Map<String, Object>> refreshCache(
            @PathVariable String key,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/cache/{}/refresh", key);

        try {
            String newData = request.get("data");
            cacheService.refresh(key, newData);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cache refreshed");
            response.put("cacheKey", key);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error refreshing cache: {}", key, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Extend TTL
     */
    @PostMapping("/{key}/extend-ttl")
    public ResponseEntity<Map<String, Object>> extendTTL(
            @PathVariable String key,
            @RequestBody Map<String, Integer> request) {
        log.info("POST /api/cache/{}/extend-ttl", key);

        try {
            Integer additionalSeconds = request.get("additionalSeconds");
            cacheService.extendTTL(key, additionalSeconds);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "TTL extended");
            response.put("cacheKey", key);
            response.put("additionalSeconds", additionalSeconds);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error extending TTL: {}", key, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all cache entries
     */
    @GetMapping
    public ResponseEntity<List<ReportCache>> getAllCacheEntries() {
        log.info("GET /api/cache");

        try {
            List<ReportCache> entries = cacheService.getAllCacheEntries();
            return ResponseEntity.ok(entries);

        } catch (Exception e) {
            log.error("Error getting all cache entries", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get cache entries by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ReportCache>> getCacheEntriesByType(@PathVariable String type) {
        log.info("GET /api/cache/type/{}", type);

        try {
            ReportCache.CacheType cacheType = ReportCache.CacheType.valueOf(type.toUpperCase());
            List<ReportCache> entries = cacheService.getCacheEntriesByType(cacheType);
            return ResponseEntity.ok(entries);

        } catch (IllegalArgumentException e) {
            log.error("Invalid cache type: {}", type);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error getting cache entries by type: {}", type, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get cache entries by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReportCache>> getCacheEntriesByStatus(@PathVariable String status) {
        log.info("GET /api/cache/status/{}", status);

        try {
            ReportCache.CacheStatus cacheStatus = ReportCache.CacheStatus.valueOf(status.toUpperCase());
            List<ReportCache> entries = cacheService.getCacheEntriesByStatus(cacheStatus);
            return ResponseEntity.ok(entries);

        } catch (IllegalArgumentException e) {
            log.error("Invalid cache status: {}", status);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error getting cache entries by status: {}", status, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Evict expired entries
     */
    @DeleteMapping("/evict/expired")
    public ResponseEntity<Map<String, Object>> evictExpired() {
        log.info("DELETE /api/cache/evict/expired");

        try {
            int count = cacheService.evictExpired();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Expired entries evicted");
            response.put("count", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error evicting expired entries", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Evict by policy
     */
    @PostMapping("/evict/policy")
    public ResponseEntity<Map<String, Object>> evictByPolicy(@RequestBody Map<String, Object> request) {
        log.info("POST /api/cache/evict/policy");

        try {
            String policyStr = (String) request.get("policy");
            Integer count = Integer.valueOf(request.get("count").toString());

            ReportCache.EvictionPolicy policy = ReportCache.EvictionPolicy.valueOf(policyStr.toUpperCase());
            int evicted = cacheService.evictByPolicy(policy, count);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Entries evicted by policy");
            response.put("policy", policy);
            response.put("count", evicted);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error evicting by policy", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Clear all cache
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearAllCache() {
        log.info("DELETE /api/cache");

        try {
            cacheService.clear();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "All cache cleared");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error clearing all cache", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Clear cache by type
     */
    @DeleteMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> clearByType(@PathVariable String type) {
        log.info("DELETE /api/cache/type/{}", type);

        try {
            ReportCache.CacheType cacheType = ReportCache.CacheType.valueOf(type.toUpperCase());
            int count = cacheService.clearByType(cacheType);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cache cleared by type");
            response.put("type", cacheType);
            response.put("count", count);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid cache type: {}", type);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error clearing cache by type: {}", type, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Warm cache
     */
    @PostMapping("/warm")
    public ResponseEntity<Map<String, Object>> warmCache(@RequestBody Map<String, Object> request) {
        log.info("POST /api/cache/warm");

        try {
            String cacheKey = (String) request.get("cacheKey");
            String data = (String) request.get("data");
            String cacheTypeStr = (String) request.get("cacheType");

            ReportCache.CacheType cacheType = ReportCache.CacheType.valueOf(cacheTypeStr.toUpperCase());
            cacheService.warmCache(cacheKey, data, cacheType);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cache warmed");
            response.put("cacheKey", cacheKey);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error warming cache", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get cache statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ReportCache.CacheStatistics> getStatistics() {
        log.info("GET /api/cache/stats");

        try {
            ReportCache.CacheStatistics stats = cacheService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error getting cache statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get cache health report
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthReport() {
        log.info("GET /api/cache/health");

        try {
            Map<String, Object> health = cacheService.getHealthReport();
            return ResponseEntity.ok(health);

        } catch (Exception e) {
            log.error("Error getting cache health report", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get top accessed entries
     */
    @GetMapping("/top-accessed")
    public ResponseEntity<List<ReportCache>> getTopAccessedEntries(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/cache/top-accessed?limit={}", limit);

        try {
            List<ReportCache> entries = cacheService.getTopAccessedEntries(limit);
            return ResponseEntity.ok(entries);

        } catch (Exception e) {
            log.error("Error getting top accessed entries", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get largest entries
     */
    @GetMapping("/largest")
    public ResponseEntity<List<ReportCache>> getLargestEntries(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/cache/largest?limit={}", limit);

        try {
            List<ReportCache> entries = cacheService.getLargestEntries(limit);
            return ResponseEntity.ok(entries);

        } catch (Exception e) {
            log.error("Error getting largest entries", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
