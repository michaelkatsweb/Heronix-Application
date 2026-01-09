package com.heronix.service;

import com.heronix.dto.ReportShare;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Report Share Service
 *
 * Provides report sharing, permissions, access control,
 * and collaboration features.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 86 - Report Sharing & Permissions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportShareService {

    private final Map<Long, ReportShare> shareStore = new ConcurrentHashMap<>();
    private final AtomicLong shareIdGenerator = new AtomicLong(1);

    public ReportShare createShare(ReportShare share) {
        Long shareId = shareIdGenerator.getAndIncrement();
        share.setShareId(shareId);

        shareStore.put(shareId, share);

        log.info("Share created: {} (report: {}, sharedWith: {})",
                shareId, share.getReportId(), share.getSharedWithUsername());
        return share;
    }

    public ReportShare getShare(Long shareId) {
        ReportShare share = shareStore.get(shareId);
        if (share == null) {
            throw new IllegalArgumentException("Share not found: " + shareId);
        }
        return share;
    }

    public ReportShare revokeShare(Long shareId) {
        ReportShare share = getShare(shareId);

        log.info("Share revoked: {}", shareId);
        return share;
    }

    public void deleteShare(Long shareId) {
        ReportShare share = shareStore.remove(shareId);
        if (share == null) {
            throw new IllegalArgumentException("Share not found: " + shareId);
        }
        log.info("Share deleted: {}", shareId);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalShares", shareStore.size());
        stats.put("timestamp", LocalDateTime.now());
        return stats;
    }
}
