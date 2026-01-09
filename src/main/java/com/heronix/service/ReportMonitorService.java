package com.heronix.service;

import com.heronix.dto.ReportMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Report Monitor Service
 *
 * Provides monitoring, health checks, performance metrics,
 * and alert management for report systems.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 84 - Report Monitoring & Alerting
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportMonitorService {

    private final Map<Long, ReportMonitor> monitorStore = new ConcurrentHashMap<>();
    private final AtomicLong monitorIdGenerator = new AtomicLong(1);

    public ReportMonitor createMonitor(ReportMonitor monitor) {
        Long monitorId = monitorIdGenerator.getAndIncrement();
        monitor.setMonitorId(monitorId);
        monitor.setCreatedAt(LocalDateTime.now());
        monitor.setUpdatedAt(LocalDateTime.now());

        monitorStore.put(monitorId, monitor);

        return monitor;
    }

    public ReportMonitor getMonitor(Long monitorId) {
        ReportMonitor monitor = monitorStore.get(monitorId);
        if (monitor == null) {
            throw new IllegalArgumentException("Monitor not found: " + monitorId);
        }
        return monitor;
    }

    public ReportMonitor checkHealth(Long monitorId) {
        ReportMonitor monitor = getMonitor(monitorId);

        monitor.setLastCheckAt(LocalDateTime.now());

        log.info("Health check completed: {} (score: {})", monitorId, monitor.getHealthScore());
        return monitor;
    }

    public void deleteMonitor(Long monitorId) {
        ReportMonitor monitor = monitorStore.remove(monitorId);
        if (monitor == null) {
            throw new IllegalArgumentException("Monitor not found: " + monitorId);
        }
        log.info("Monitor deleted: {}", monitorId);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMonitors", monitorStore.size());
        stats.put("timestamp", LocalDateTime.now());
        return stats;
    }
}
