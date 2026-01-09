package com.heronix.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Cache Configuration
 *
 * Configures Spring Cache for dashboard metrics and other frequently accessed data.
 * Uses simple in-memory caching with ConcurrentHashMap backend.
 *
 * Location: src/main/java/com/heronix/config/CacheConfig.java
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since 2025-12-12
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure cache manager with named caches.
     *
     * Available caches:
     * - dashboardMetrics: Dashboard statistics and metrics (DashboardMetricsService)
     * - attendanceAnalytics: Attendance analytics and reports (Phase 38)
     * - behaviorReports: Behavior reporting analytics (Phase 38)
     * - assignmentReports: Assignment and grade reports (Phase 38)
     * - conflictAnalysis: Schedule conflict analysis (Phase 38)
     * - studentData: Student profile and enrollment data
     * - courseData: Course catalog and section information
     *
     * @return configured CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("dashboardMetrics"),
            new ConcurrentMapCache("attendanceAnalytics"),
            new ConcurrentMapCache("behaviorReports"),
            new ConcurrentMapCache("assignmentReports"),
            new ConcurrentMapCache("conflictAnalysis"),
            new ConcurrentMapCache("studentData"),
            new ConcurrentMapCache("courseData")
        ));
        return cacheManager;
    }
}
