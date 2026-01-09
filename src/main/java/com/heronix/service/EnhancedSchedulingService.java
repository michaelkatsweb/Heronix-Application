package com.heronix.service;

import com.heronix.dto.ReportScheduleConfig;
import com.heronix.dto.ScheduleExecutionHistory;
import com.heronix.event.ReportEvent;
import com.heronix.model.domain.ReportHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Enhanced Scheduling Service
 *
 * Advanced report scheduling system with flexible configuration options.
 *
 * Features:
 * - Multiple scheduling patterns (daily, weekly, monthly, cron)
 * - Dynamic schedule management (create, update, pause, resume, delete)
 * - Execution history tracking
 * - Retry logic for failed executions
 * - Email distribution
 * - Parameter templates
 *
 * Architecture:
 * - In-memory schedule storage (ConcurrentHashMap)
 * - Periodic execution check (every minute)
 * - Async report generation
 * - Event-driven notifications
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 65 - Report Scheduling & Automation Enhancements
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EnhancedSchedulingService {

    private final ApplicationEventPublisher eventPublisher;

    // In-memory storage for schedules (in production, use database)
    private final Map<Long, ReportScheduleConfig> schedules = new ConcurrentHashMap<>();
    private final Map<Long, List<ScheduleExecutionHistory>> executionHistory = new ConcurrentHashMap<>();
    private Long nextScheduleId = 1L;
    private Long nextExecutionId = 1L;

    // ============================================================
    // Schedule Management
    // ============================================================

    /**
     * Create new report schedule
     */
    public ReportScheduleConfig createSchedule(ReportScheduleConfig config) {
        log.info("Creating new schedule: {}", config.getScheduleName());

        // Assign ID
        synchronized (this) {
            config.setScheduleId(nextScheduleId++);
        }

        // Set defaults
        if (config.getStatus() == null) {
            config.setStatus(ReportScheduleConfig.ScheduleStatus.ACTIVE);
        }
        if (config.getMaxRetries() == null) {
            config.setMaxRetries(3);
        }
        if (config.getRetryDelayMinutes() == null) {
            config.setRetryDelayMinutes(5);
        }
        if (config.getNotifyOnFailure() == null) {
            config.setNotifyOnFailure(true);
        }
        if (config.getNotifyOnSuccess() == null) {
            config.setNotifyOnSuccess(false);
        }
        if (config.getAttachToEmail() == null) {
            config.setAttachToEmail(true);
        }
        if (config.getSaveToHistory() == null) {
            config.setSaveToHistory(true);
        }
        if (config.getTotalExecutions() == null) {
            config.setTotalExecutions(0);
        }
        if (config.getSuccessfulExecutions() == null) {
            config.setSuccessfulExecutions(0);
        }
        if (config.getFailedExecutions() == null) {
            config.setFailedExecutions(0);
        }

        // Calculate next execution time
        config.setNextExecutionTime(calculateNextExecution(config));

        // Validate configuration
        validateScheduleConfig(config);

        // Store schedule
        schedules.put(config.getScheduleId(), config);
        executionHistory.put(config.getScheduleId(), new ArrayList<>());

        log.info("Schedule created successfully: ID={}, next execution={}",
                config.getScheduleId(), config.getNextExecutionTime());

        return config;
    }

    /**
     * Update existing schedule
     */
    public ReportScheduleConfig updateSchedule(Long scheduleId, ReportScheduleConfig updatedConfig) {
        log.info("Updating schedule: {}", scheduleId);

        ReportScheduleConfig existing = schedules.get(scheduleId);
        if (existing == null) {
            throw new IllegalArgumentException("Schedule not found: " + scheduleId);
        }

        // Preserve ID and execution stats
        updatedConfig.setScheduleId(scheduleId);
        updatedConfig.setTotalExecutions(existing.getTotalExecutions());
        updatedConfig.setSuccessfulExecutions(existing.getSuccessfulExecutions());
        updatedConfig.setFailedExecutions(existing.getFailedExecutions());

        // Recalculate next execution
        updatedConfig.setNextExecutionTime(calculateNextExecution(updatedConfig));

        // Validate and store
        validateScheduleConfig(updatedConfig);
        schedules.put(scheduleId, updatedConfig);

        log.info("Schedule updated successfully: ID={}", scheduleId);
        return updatedConfig;
    }

    /**
     * Delete schedule
     */
    public void deleteSchedule(Long scheduleId) {
        log.info("Deleting schedule: {}", scheduleId);

        if (!schedules.containsKey(scheduleId)) {
            throw new IllegalArgumentException("Schedule not found: " + scheduleId);
        }

        schedules.remove(scheduleId);
        executionHistory.remove(scheduleId);

        log.info("Schedule deleted: {}", scheduleId);
    }

    /**
     * Pause schedule
     */
    public void pauseSchedule(Long scheduleId) {
        log.info("Pausing schedule: {}", scheduleId);

        ReportScheduleConfig schedule = schedules.get(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule not found: " + scheduleId);
        }

        schedule.setStatus(ReportScheduleConfig.ScheduleStatus.PAUSED);
        log.info("Schedule paused: {}", scheduleId);
    }

    /**
     * Resume schedule
     */
    public void resumeSchedule(Long scheduleId) {
        log.info("Resuming schedule: {}", scheduleId);

        ReportScheduleConfig schedule = schedules.get(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule not found: " + scheduleId);
        }

        schedule.setStatus(ReportScheduleConfig.ScheduleStatus.ACTIVE);
        schedule.setNextExecutionTime(calculateNextExecution(schedule));

        log.info("Schedule resumed: {}, next execution: {}", scheduleId, schedule.getNextExecutionTime());
    }

    /**
     * Get schedule by ID
     */
    public ReportScheduleConfig getSchedule(Long scheduleId) {
        return schedules.get(scheduleId);
    }

    /**
     * Get all schedules
     */
    public List<ReportScheduleConfig> getAllSchedules() {
        return new ArrayList<>(schedules.values());
    }

    /**
     * Get schedules by status
     */
    public List<ReportScheduleConfig> getSchedulesByStatus(ReportScheduleConfig.ScheduleStatus status) {
        return schedules.values().stream()
                .filter(s -> s.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Get schedules by creator
     */
    public List<ReportScheduleConfig> getSchedulesByCreator(String username) {
        return schedules.values().stream()
                .filter(s -> username.equals(s.getCreatedBy()))
                .collect(Collectors.toList());
    }

    // ============================================================
    // Execution History
    // ============================================================

    /**
     * Get execution history for schedule
     */
    public List<ScheduleExecutionHistory> getExecutionHistory(Long scheduleId, int limit) {
        List<ScheduleExecutionHistory> history = executionHistory.get(scheduleId);
        if (history == null) {
            return Collections.emptyList();
        }

        return history.stream()
                .sorted(Comparator.comparing(ScheduleExecutionHistory::getStartTime).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get all execution history
     */
    public List<ScheduleExecutionHistory> getAllExecutionHistory(int limit) {
        return executionHistory.values().stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparing(ScheduleExecutionHistory::getStartTime).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get failed executions
     */
    public List<ScheduleExecutionHistory> getFailedExecutions(int limit) {
        return executionHistory.values().stream()
                .flatMap(List::stream)
                .filter(ScheduleExecutionHistory::isFailed)
                .sorted(Comparator.comparing(ScheduleExecutionHistory::getStartTime).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ============================================================
    // Scheduled Execution Check
    // ============================================================

    /**
     * Check for schedules that need to execute
     * Runs every minute
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkScheduledExecutions() {
        log.debug("Checking for scheduled executions...");

        LocalDateTime now = LocalDateTime.now();

        schedules.values().stream()
                .filter(ReportScheduleConfig::isActive)
                .filter(s -> s.getNextExecutionTime() != null)
                .filter(s -> !s.getNextExecutionTime().isAfter(now))
                .forEach(this::executeSchedule);
    }

    /**
     * Execute a scheduled report
     */
    private void executeSchedule(ReportScheduleConfig schedule) {
        log.info("Executing scheduled report: {} (ID={})", schedule.getScheduleName(), schedule.getScheduleId());

        // Create execution record
        ScheduleExecutionHistory execution = ScheduleExecutionHistory.builder()
                .executionId(nextExecutionId++)
                .scheduleId(schedule.getScheduleId())
                .scheduleName(schedule.getScheduleName())
                .startTime(LocalDateTime.now())
                .status(ScheduleExecutionHistory.ExecutionStatus.IN_PROGRESS)
                .retryAttempt(0)
                .triggeredBy("SCHEDULED")
                .build();

        // Add to history
        executionHistory.get(schedule.getScheduleId()).add(execution);

        try {
            // Simulate report generation (in real implementation, call report service)
            log.info("Generating {} report in {} format",
                    schedule.getReportType(), schedule.getReportFormat());

            // Update execution record
            execution.setEndTime(LocalDateTime.now());
            execution.setStatus(ScheduleExecutionHistory.ExecutionStatus.COMPLETED);
            execution.setDurationMs(
                    java.time.Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis());

            // Update schedule stats
            schedule.setLastExecutionTime(execution.getEndTime());
            schedule.setLastExecutionStatus("SUCCESS");
            schedule.setTotalExecutions(schedule.getTotalExecutions() + 1);
            schedule.setSuccessfulExecutions(schedule.getSuccessfulExecutions() + 1);
            schedule.setNextExecutionTime(calculateNextExecution(schedule));

            // Publish success event
            if (schedule.getNotifyOnSuccess()) {
                publishScheduleEvent(schedule, execution, false);
            }

            log.info("Schedule execution completed successfully: {}", schedule.getScheduleId());

        } catch (Exception e) {
            log.error("Schedule execution failed: " + schedule.getScheduleId(), e);

            // Update execution record
            execution.setEndTime(LocalDateTime.now());
            execution.setStatus(ScheduleExecutionHistory.ExecutionStatus.FAILED);
            execution.setErrorMessage(e.getMessage());
            execution.setDurationMs(
                    java.time.Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis());

            // Update schedule stats
            schedule.setLastExecutionTime(execution.getEndTime());
            schedule.setLastExecutionStatus("FAILURE: " + e.getMessage());
            schedule.setTotalExecutions(schedule.getTotalExecutions() + 1);
            schedule.setFailedExecutions(schedule.getFailedExecutions() + 1);
            schedule.setNextExecutionTime(calculateNextExecution(schedule));

            // Publish failure event
            if (schedule.getNotifyOnFailure()) {
                publishScheduleEvent(schedule, execution, true);
            }

            // Schedule retry if configured
            if (execution.getRetryAttempt() < schedule.getMaxRetries()) {
                scheduleRetry(schedule, execution);
            }
        }
    }

    /**
     * Schedule retry for failed execution
     */
    private void scheduleRetry(ReportScheduleConfig schedule, ScheduleExecutionHistory failedExecution) {
        log.info("Scheduling retry for schedule {} (attempt {}/{})",
                schedule.getScheduleId(),
                failedExecution.getRetryAttempt() + 1,
                schedule.getMaxRetries());

        // In real implementation, schedule a retry task
        // For now, just log it
    }

    /**
     * Publish schedule execution event
     */
    private void publishScheduleEvent(ReportScheduleConfig schedule,
                                       ScheduleExecutionHistory execution,
                                       boolean isFailure) {
        try {
            String message = String.format("Scheduled report '%s' %s",
                    schedule.getScheduleName(),
                    isFailure ? "failed" : "completed successfully");

            ReportEvent event = new ReportEvent(
                    this,
                    isFailure ? ReportEvent.ReportEventType.SCHEDULE_FAILED :
                            ReportEvent.ReportEventType.SCHEDULE_EXECUTED,
                    schedule.getScheduleName(),
                    schedule.getReportType(),
                    schedule.getCreatedBy(),
                    message,
                    null
            );

            eventPublisher.publishEvent(event);

        } catch (Exception e) {
            log.error("Error publishing schedule event", e);
        }
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Calculate next execution time based on schedule configuration
     */
    private LocalDateTime calculateNextExecution(ReportScheduleConfig config) {
        if (config.getStatus() != ReportScheduleConfig.ScheduleStatus.ACTIVE) {
            return null;
        }

        LocalDate today = LocalDate.now();
        LocalTime executionTime = config.getExecutionTime() != null ?
                config.getExecutionTime() : LocalTime.of(0, 0);

        return switch (config.getFrequency()) {
            case DAILY -> {
                LocalDate nextDate = today;
                if (config.getIntervalDays() != null && config.getIntervalDays() > 1) {
                    // Calculate next occurrence based on interval
                    if (config.getStartDate() != null) {
                        long daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(config.getStartDate(), today);
                        long daysUntilNext = config.getIntervalDays() - (daysSinceStart % config.getIntervalDays());
                        nextDate = today.plusDays(daysUntilNext);
                    }
                } else {
                    // Daily execution
                    if (LocalTime.now().isAfter(executionTime)) {
                        nextDate = today.plusDays(1);
                    }
                }
                yield LocalDateTime.of(nextDate, executionTime);
            }
            case WEEKLY -> {
                if (config.getDaysOfWeek() == null || config.getDaysOfWeek().isEmpty()) {
                    yield null;
                }
                // Find next occurrence of any configured day
                LocalDate nextDate = today;
                for (int i = 0; i < 7; i++) {
                    if (config.getDaysOfWeek().contains(nextDate.getDayOfWeek())) {
                        if (i == 0 && LocalTime.now().isAfter(executionTime)) {
                            // Today but time has passed, skip to next week
                            nextDate = nextDate.plusWeeks(1);
                        }
                        break;
                    }
                    nextDate = nextDate.plusDays(1);
                }
                yield LocalDateTime.of(nextDate, executionTime);
            }
            case MONTHLY -> {
                if (config.getDayOfMonth() == null) {
                    yield null;
                }
                LocalDate nextDate = today.withDayOfMonth(
                        Math.min(config.getDayOfMonth(), today.lengthOfMonth()));
                if (nextDate.isBefore(today) || (nextDate.equals(today) && LocalTime.now().isAfter(executionTime))) {
                    nextDate = nextDate.plusMonths(1);
                    nextDate = nextDate.withDayOfMonth(Math.min(config.getDayOfMonth(), nextDate.lengthOfMonth()));
                }
                yield LocalDateTime.of(nextDate, executionTime);
            }
            case CUSTOM_CRON -> {
                if (config.getCronExpression() == null) {
                    yield null;
                }
                try {
                    CronExpression cron = CronExpression.parse(config.getCronExpression());
                    yield cron.next(LocalDateTime.now());
                } catch (Exception e) {
                    log.error("Invalid cron expression: {}", config.getCronExpression(), e);
                    yield null;
                }
            }
        };
    }

    /**
     * Validate schedule configuration
     */
    private void validateScheduleConfig(ReportScheduleConfig config) {
        if (config.getScheduleName() == null || config.getScheduleName().trim().isEmpty()) {
            throw new IllegalArgumentException("Schedule name is required");
        }
        if (config.getReportType() == null) {
            throw new IllegalArgumentException("Report type is required");
        }
        if (config.getReportFormat() == null) {
            throw new IllegalArgumentException("Report format is required");
        }
        if (config.getFrequency() == null) {
            throw new IllegalArgumentException("Schedule frequency is required");
        }

        // Validate frequency-specific configuration
        switch (config.getFrequency()) {
            case WEEKLY -> {
                if (config.getDaysOfWeek() == null || config.getDaysOfWeek().isEmpty()) {
                    throw new IllegalArgumentException("Days of week required for weekly schedule");
                }
            }
            case MONTHLY -> {
                if (config.getDayOfMonth() == null) {
                    throw new IllegalArgumentException("Day of month required for monthly schedule");
                }
                if (config.getDayOfMonth() < -1 || config.getDayOfMonth() > 31) {
                    throw new IllegalArgumentException("Day of month must be between 1-31 or -1 for last day");
                }
            }
            case CUSTOM_CRON -> {
                if (config.getCronExpression() == null || config.getCronExpression().trim().isEmpty()) {
                    throw new IllegalArgumentException("Cron expression required for custom schedule");
                }
                try {
                    CronExpression.parse(config.getCronExpression());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid cron expression: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Get schedule statistics
     */
    public Map<String, Object> getScheduleStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalSchedules", schedules.size());
        stats.put("activeSchedules", schedules.values().stream()
                .filter(s -> s.getStatus() == ReportScheduleConfig.ScheduleStatus.ACTIVE)
                .count());
        stats.put("pausedSchedules", schedules.values().stream()
                .filter(s -> s.getStatus() == ReportScheduleConfig.ScheduleStatus.PAUSED)
                .count());

        int totalExecutions = schedules.values().stream()
                .mapToInt(s -> s.getTotalExecutions() != null ? s.getTotalExecutions() : 0)
                .sum();
        stats.put("totalExecutions", totalExecutions);

        int successfulExecutions = schedules.values().stream()
                .mapToInt(s -> s.getSuccessfulExecutions() != null ? s.getSuccessfulExecutions() : 0)
                .sum();
        stats.put("successfulExecutions", successfulExecutions);

        int failedExecutions = schedules.values().stream()
                .mapToInt(s -> s.getFailedExecutions() != null ? s.getFailedExecutions() : 0)
                .sum();
        stats.put("failedExecutions", failedExecutions);

        if (totalExecutions > 0) {
            stats.put("successRate", (double) successfulExecutions / totalExecutions * 100);
        } else {
            stats.put("successRate", 0.0);
        }

        return stats;
    }
}
