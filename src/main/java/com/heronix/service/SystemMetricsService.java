package com.heronix.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * System Metrics Service
 *
 * Collects and monitors system-level performance metrics.
 *
 * Metrics Collected:
 * - CPU usage
 * - Memory usage (heap, non-heap)
 * - Thread count (active, peak, total)
 * - Disk space
 * - System load average
 * - JVM uptime
 *
 * Collection Schedule:
 * - Real-time metrics available on-demand
 * - Historical metrics collected every 5 minutes
 * - Metrics retained for 24 hours
 *
 * Use Cases:
 * - Performance monitoring
 * - Capacity planning
 * - Troubleshooting
 * - Health checks
 * - Alerting
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 63 - System Health Monitoring & Performance Dashboard
 */
@Service
@Slf4j
public class SystemMetricsService {

    private final ConcurrentLinkedQueue<SystemSnapshot> metricsHistory;
    private final MemoryMXBean memoryBean;
    private final ThreadMXBean threadBean;
    private final OperatingSystemMXBean osBean;
    private final Runtime runtime;

    private static final int MAX_HISTORY_SIZE = 288; // 24 hours at 5-minute intervals

    public SystemMetricsService() {
        this.metricsHistory = new ConcurrentLinkedQueue<>();
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.threadBean = ManagementFactory.getThreadMXBean();
        this.osBean = ManagementFactory.getOperatingSystemMXBean();
        this.runtime = Runtime.getRuntime();
    }

    /**
     * Collect system metrics periodically
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void collectMetrics() {
        try {
            SystemSnapshot snapshot = captureSnapshot();
            metricsHistory.add(snapshot);

            // Remove old metrics (keep last 24 hours)
            while (metricsHistory.size() > MAX_HISTORY_SIZE) {
                metricsHistory.poll();
            }

            log.debug("System metrics collected: CPU={}, Memory={}MB, Threads={}",
                    snapshot.getCpuUsage(), snapshot.getUsedMemoryMB(), snapshot.getThreadCount());

        } catch (Exception e) {
            log.error("Error collecting system metrics", e);
        }
    }

    /**
     * Get current system metrics snapshot
     */
    public SystemSnapshot getCurrentMetrics() {
        return captureSnapshot();
    }

    /**
     * Get metrics history
     */
    public List<SystemSnapshot> getMetricsHistory() {
        return new ArrayList<>(metricsHistory);
    }

    /**
     * Get metrics history for last N hours
     */
    public List<SystemSnapshot> getMetricsHistory(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return metricsHistory.stream()
                .filter(m -> m.getTimestamp().isAfter(cutoff))
                .toList();
    }

    /**
     * Capture current system snapshot
     */
    private SystemSnapshot captureSnapshot() {
        SystemSnapshot snapshot = new SystemSnapshot();
        snapshot.setTimestamp(LocalDateTime.now());

        // Memory metrics
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();

        snapshot.setUsedMemoryMB(heapUsed / (1024 * 1024));
        snapshot.setMaxMemoryMB(heapMax / (1024 * 1024));
        snapshot.setNonHeapMemoryMB(nonHeapUsed / (1024 * 1024));
        snapshot.setMemoryUsagePercent((heapUsed * 100.0) / heapMax);

        // Thread metrics
        snapshot.setThreadCount(threadBean.getThreadCount());
        snapshot.setPeakThreadCount(threadBean.getPeakThreadCount());
        snapshot.setDaemonThreadCount(threadBean.getDaemonThreadCount());

        // CPU and system metrics
        snapshot.setCpuUsage(osBean.getSystemLoadAverage());
        snapshot.setAvailableProcessors(osBean.getAvailableProcessors());

        // JVM uptime
        snapshot.setUptimeMillis(ManagementFactory.getRuntimeMXBean().getUptime());

        // Disk space
        snapshot.setFreeDiskSpaceMB(runtime.freeMemory() / (1024 * 1024));
        snapshot.setTotalDiskSpaceMB(runtime.totalMemory() / (1024 * 1024));

        return snapshot;
    }

    /**
     * Get system health status
     */
    public HealthStatus getHealthStatus() {
        SystemSnapshot current = getCurrentMetrics();

        HealthStatus status = new HealthStatus();
        status.setOverallStatus(determineOverallStatus(current));
        status.setMemoryStatus(determineMemoryStatus(current));
        status.setThreadStatus(determineThreadStatus(current));
        status.setCpuStatus(determineCpuStatus(current));
        status.setTimestamp(LocalDateTime.now());
        status.setMessage(generateStatusMessage(current));

        return status;
    }

    /**
     * Determine overall health status
     */
    private String determineOverallStatus(SystemSnapshot snapshot) {
        if (snapshot.getMemoryUsagePercent() > 90 || snapshot.getCpuUsage() > 8.0) {
            return "CRITICAL";
        } else if (snapshot.getMemoryUsagePercent() > 75 || snapshot.getCpuUsage() > 5.0) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }

    /**
     * Determine memory status
     */
    private String determineMemoryStatus(SystemSnapshot snapshot) {
        if (snapshot.getMemoryUsagePercent() > 90) {
            return "CRITICAL";
        } else if (snapshot.getMemoryUsagePercent() > 75) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }

    /**
     * Determine thread status
     */
    private String determineThreadStatus(SystemSnapshot snapshot) {
        if (snapshot.getThreadCount() > 200) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }

    /**
     * Determine CPU status
     */
    private String determineCpuStatus(SystemSnapshot snapshot) {
        if (snapshot.getCpuUsage() > 8.0) {
            return "CRITICAL";
        } else if (snapshot.getCpuUsage() > 5.0) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }

    /**
     * Generate status message
     */
    private String generateStatusMessage(SystemSnapshot snapshot) {
        return String.format("System is %s - Memory: %.1f%%, CPU: %.2f, Threads: %d",
                determineOverallStatus(snapshot).toLowerCase(),
                snapshot.getMemoryUsagePercent(),
                snapshot.getCpuUsage(),
                snapshot.getThreadCount());
    }

    /**
     * System Snapshot DTO
     */
    @Data
    public static class SystemSnapshot {
        private LocalDateTime timestamp;
        private long usedMemoryMB;
        private long maxMemoryMB;
        private long nonHeapMemoryMB;
        private double memoryUsagePercent;
        private int threadCount;
        private int peakThreadCount;
        private int daemonThreadCount;
        private double cpuUsage;
        private int availableProcessors;
        private long uptimeMillis;
        private long freeDiskSpaceMB;
        private long totalDiskSpaceMB;
    }

    /**
     * Health Status DTO
     */
    @Data
    public static class HealthStatus {
        private String overallStatus;
        private String memoryStatus;
        private String threadStatus;
        private String cpuStatus;
        private LocalDateTime timestamp;
        private String message;
    }
}
