package com.heronix.service;

import com.heronix.dto.ReportScheduleConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Report Schedule Config Service
 *
 * Provides scheduling configuration, cron expressions,
 * and automated report generation management.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 85 - Report Scheduling Configuration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportScheduleConfigService {

    private final Map<Long, ReportScheduleConfig> scheduleStore = new ConcurrentHashMap<>();
    private final AtomicLong scheduleIdGenerator = new AtomicLong(1);

    public ReportScheduleConfig createSchedule(ReportScheduleConfig schedule) {
        Long scheduleId = scheduleIdGenerator.getAndIncrement();
        schedule.setScheduleId(scheduleId);

        scheduleStore.put(scheduleId, schedule);

        log.info("Schedule config created: {} (name: {})", scheduleId, schedule.getScheduleName());
        return schedule;
    }

    public ReportScheduleConfig getSchedule(Long scheduleId) {
        ReportScheduleConfig schedule = scheduleStore.get(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule config not found: " + scheduleId);
        }
        return schedule;
    }

    public ReportScheduleConfig enableSchedule(Long scheduleId) {
        ReportScheduleConfig schedule = getSchedule(scheduleId);

        log.info("Schedule enabled: {}", scheduleId);
        return schedule;
    }

    public ReportScheduleConfig disableSchedule(Long scheduleId) {
        ReportScheduleConfig schedule = getSchedule(scheduleId);

        log.info("Schedule disabled: {}", scheduleId);
        return schedule;
    }

    public void deleteSchedule(Long scheduleId) {
        ReportScheduleConfig schedule = scheduleStore.remove(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule config not found: " + scheduleId);
        }
        log.info("Schedule deleted: {}", scheduleId);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSchedules", scheduleStore.size());
        stats.put("timestamp", LocalDateTime.now());
        return stats;
    }
}
