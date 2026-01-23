package com.heronix.controller;

import com.heronix.dto.ReportCache;
import com.heronix.service.ReportPerformanceCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Report Performance Cache API Controller
 *
 * REST API endpoints for cache management and performance optimization.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 78 - Report Caching & Performance Optimization
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class ReportPerformanceCacheApiController {

    private final ReportPerformanceCacheService cacheService;

    /**
     * Put data in cache
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> putCache(@RequestBody Map<String, Object> cacheData) {
        try {
            String cacheKey = (String) cacheData.get("cacheKey");
            String data = (String) cacheData.get("data");
            ReportCache.CacheType cacheType = ReportCache.CacheType.valueOf(
                    (String) cacheData.getOrDefault("cacheType", "REPORT_DATA"));
            Integer ttlSeconds = cacheData.containsKey("ttlSeconds") ?
                    Integer.parseInt(cacheData.get("ttlSeconds").toString()) : null;

            ReportCache cache = cacheService.put(cacheKey, data, cacheType, ttlSeconds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data cached successfully");
            response.put("cacheId", cache.getCacheId());
            response.put("cacheKey", cache.getCacheKey());
            response.put("dataSizeBytes", cache.getDataSizeBytes());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to cache data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get data from cache
     */
    @GetMapping("/{cacheKey}")
    public ResponseEntity<Map<String, Object>> getCache(@PathVariable String cacheKey) {
        Optional<String> cachedData = cacheService.get(cacheKey);

        if (cachedData.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hit", true);
            response.put("data", cachedData.get());
            response.put("cacheKey", cacheKey);
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hit", false);
            response.put("message", "Cache miss");
            response.put("cacheKey", cacheKey);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Get cache metadata
     */
    @GetMapping("/{cacheKey}/metadata")
    public ResponseEntity<Map<String, Object>> getCacheMetadata(@PathVariable String cacheKey) {
        Optional<ReportCache> cache = cacheService.getCacheMetadata(cacheKey);

        if (cache.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("metadata", cache.get());
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Cache not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Check if cache key exists
     */
    @GetMapping("/{cacheKey}/exists")
    public ResponseEntity<Map<String, Object>> exists(@PathVariable String cacheKey) {
        boolean exists = cacheService.exists(cacheKey);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("exists", exists);
        response.put("cacheKey", cacheKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Invalidate cache entry
     */
    @DeleteMapping("/{cacheKey}")
    public ResponseEntity<Map<String, Object>> invalidate(
            @PathVariable String cacheKey,
            @RequestParam(required = false, defaultValue = "Manual invalidation") String reason) {

        try {
            cacheService.invalidate(cacheKey, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cache invalidated successfully");
            response.put("cacheKey", cacheKey);
            response.put("reason", reason);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to invalidate cache: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Invalidate by pattern
     */
    @DeleteMapping("/pattern/{pattern}")
    public ResponseEntity<Map<String, Object>> invalidateByPattern(@PathVariable String pattern) {
        try {
            int invalidatedCount = cacheService.invalidateByPattern(pattern);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cache entries invalidated by pattern");
            response.put("invalidatedCount", invalidatedCount);
            response.put("pattern", pattern);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to invalidate by pattern: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Refresh cache entry
     */
    @PutMapping("/{cacheKey}/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
            @PathVariable String cacheKey,
            @RequestBody Map<String, Object> refreshData) {

        try {
            String newData = (String) refreshData.get("data");
            cacheService.refresh(cacheKey, newData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cache refreshed successfully");
            response.put("cacheKey", cacheKey);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to refresh cache: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Extend TTL
     */
    @PutMapping("/{cacheKey}/extend-ttl")
    public ResponseEntity<Map<String, Object>> extendTTL(
            @PathVariable String cacheKey,
            @RequestParam Integer additionalSeconds) {

        try {
            cacheService.extendTTL(cacheKey, additionalSeconds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "TTL extended successfully");
            response.put("cacheKey", cacheKey);
            response.put("additionalSeconds", additionalSeconds);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to extend TTL: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all cache entries
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCacheEntries() {
        List<ReportCache> entries = cacheService.getAllCacheEntries();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("entries", entries);
        response.put("count", entries.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get cache entries by type
     */
    @GetMapping("/type/{cacheType}")
    public ResponseEntity<Map<String, Object>> getCacheEntriesByType(@PathVariable String cacheType) {
        try {
            ReportCache.CacheType type = ReportCache.CacheType.valueOf(cacheType);
            List<ReportCache> entries = cacheService.getCacheEntriesByType(type);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("entries", entries);
            response.put("count", entries.size());
            response.put("cacheType", cacheType);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid cache type: " + cacheType);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Evict expired entries
     */
    @DeleteMapping("/evict/expired")
    public ResponseEntity<Map<String, Object>> evictExpired() {
        int evictedCount = cacheService.evictExpired();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Expired entries evicted");
        response.put("evictedCount", evictedCount);
        return ResponseEntity.ok(response);
    }

    /**
     * Evict by policy
     */
    @DeleteMapping("/evict/policy/{policy}")
    public ResponseEntity<Map<String, Object>> evictByPolicy(
            @PathVariable String policy,
            @RequestParam(defaultValue = "10") int count) {

        try {
            ReportCache.EvictionPolicy evictionPolicy = ReportCache.EvictionPolicy.valueOf(policy);
            int evictedCount = cacheService.evictByPolicy(evictionPolicy, count);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Entries evicted by policy");
            response.put("evictedCount", evictedCount);
            response.put("policy", policy);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid eviction policy: " + policy);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Clear all cache
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAll() {
        cacheService.clear();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All cache cleared successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Clear cache by type
     */
    @DeleteMapping("/clear/type/{cacheType}")
    public ResponseEntity<Map<String, Object>> clearByType(@PathVariable String cacheType) {
        try {
            ReportCache.CacheType type = ReportCache.CacheType.valueOf(cacheType);
            int clearedCount = cacheService.clearByType(type);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cache cleared by type");
            response.put("clearedCount", clearedCount);
            response.put("cacheType", cacheType);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid cache type: " + cacheType);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Warm cache
     */
    @PostMapping("/warm")
    public ResponseEntity<Map<String, Object>> warmCache(@RequestBody Map<String, Object> warmData) {
        try {
            String cacheKey = (String) warmData.get("cacheKey");
            String data = (String) warmData.get("data");
            ReportCache.CacheType cacheType = ReportCache.CacheType.valueOf(
                    (String) warmData.getOrDefault("cacheType", "REPORT_DATA"));

            cacheService.warmCache(cacheKey, data, cacheType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cache warmed successfully");
            response.put("cacheKey", cacheKey);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to warm cache: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get cache size
     */
    @GetMapping("/size")
    public ResponseEntity<Map<String, Object>> getCacheSize() {
        int size = cacheService.size();
        long totalSizeBytes = cacheService.getTotalSizeBytes();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("entryCount", size);
        response.put("totalSizeBytes", totalSizeBytes);
        response.put("totalSizeMB", totalSizeBytes / (1024.0 * 1024.0));
        return ResponseEntity.ok(response);
    }

    /**
     * Get cache statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        ReportCache.CacheStatistics stats = cacheService.getStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }

    /**
     * Get access logs
     */
    @GetMapping("/{cacheKey}/access-logs")
    public ResponseEntity<Map<String, Object>> getAccessLogs(@PathVariable String cacheKey) {
        List<ReportCache.CacheAccessLog> logs = cacheService.getAccessLogs(cacheKey);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("accessLogs", logs);
        response.put("count", logs.size());
        response.put("cacheKey", cacheKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Get invalidation events
     */
    @GetMapping("/{cacheKey}/invalidation-events")
    public ResponseEntity<Map<String, Object>> getInvalidationEvents(@PathVariable String cacheKey) {
        List<ReportCache.CacheInvalidationEvent> events = cacheService.getInvalidationEvents(cacheKey);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("invalidationEvents", events);
        response.put("count", events.size());
        response.put("cacheKey", cacheKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Get hit rate
     */
    @GetMapping("/metrics/hit-rate")
    public ResponseEntity<Map<String, Object>> getHitRate() {
        double hitRate = cacheService.getHitRate();
        double missRate = cacheService.getMissRate();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("hitRate", hitRate);
        response.put("missRate", missRate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get top accessed entries
     */
    @GetMapping("/top-accessed")
    public ResponseEntity<Map<String, Object>> getTopAccessedEntries(
            @RequestParam(defaultValue = "10") int limit) {

        List<ReportCache> entries = cacheService.getTopAccessedEntries(limit);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("entries", entries);
        response.put("count", entries.size());
        response.put("limit", limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Get largest entries
     */
    @GetMapping("/largest")
    public ResponseEntity<Map<String, Object>> getLargestEntries(
            @RequestParam(defaultValue = "10") int limit) {

        List<ReportCache> entries = cacheService.getLargestEntries(limit);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("entries", entries);
        response.put("count", entries.size());
        response.put("limit", limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Get cache health report
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthReport() {
        Map<String, Object> health = cacheService.getHealthReport();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.putAll(health);
        return ResponseEntity.ok(response);
    }
}
