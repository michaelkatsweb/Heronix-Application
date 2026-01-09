package com.heronix.service;

import com.heronix.dto.ReportVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Report Version Service
 *
 * Provides version control, change tracking,
 * and rollback capabilities for reports.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 88 - Report Version Control
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportVersionService {

    private final Map<Long, ReportVersion> versionStore = new ConcurrentHashMap<>();
    private final AtomicLong versionIdGenerator = new AtomicLong(1);

    public ReportVersion createVersion(ReportVersion version) {
        Long versionId = versionIdGenerator.getAndIncrement();
        version.setVersionId(versionId);
        version.setCreatedAt(LocalDateTime.now());

        versionStore.put(versionId, version);

        log.info("Version created: {} (report: {}, number: {})",
                versionId, version.getReportId(), version.getVersionNumber());
        return version;
    }

    public ReportVersion getVersion(Long versionId) {
        ReportVersion version = versionStore.get(versionId);
        if (version == null) {
            throw new IllegalArgumentException("Version not found: " + versionId);
        }
        return version;
    }

    public List<ReportVersion> getVersionsByReport(Long reportId) {
        return versionStore.values().stream()
                .filter(v -> v.getReportId().equals(reportId))
                .sorted(Comparator.comparing(ReportVersion::getVersionNumber).reversed())
                .collect(Collectors.toList());
    }

    public ReportVersion getLatestVersion(Long reportId) {
        return versionStore.values().stream()
                .filter(v -> v.getReportId().equals(reportId))
                .filter(v -> Boolean.TRUE.equals(v.getIsCurrent()))
                .findFirst()
                .orElse(null);
    }

    public void deleteVersion(Long versionId) {
        ReportVersion version = versionStore.remove(versionId);
        if (version == null) {
            throw new IllegalArgumentException("Version not found: " + versionId);
        }
        log.info("Version deleted: {}", versionId);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVersions", versionStore.size());
        stats.put("timestamp", LocalDateTime.now());
        return stats;
    }
}
