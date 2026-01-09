package com.heronix.service;

import com.heronix.dto.ReportServerless;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Report Serverless Service
 *
 * Manages serverless computing and Function-as-a-Service for report services.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 97 - Report Serverless & Function-as-a-Service
 */
@Service
@Slf4j
public class ReportServerlessService {

    private final Map<Long, ReportServerless> platforms = new ConcurrentHashMap<>();
    private Long nextPlatformId = 1L;

    /**
     * Create platform
     */
    public ReportServerless createPlatform(ReportServerless platform) {
        synchronized (this) {
            platform.setPlatformId(nextPlatformId++);
        }

        platform.setStatus(ReportServerless.PlatformStatus.INITIALIZING);
        platform.setCreatedAt(LocalDateTime.now());
        platform.setIsActive(false);

        // Initialize collections
        if (platform.getFunctions() == null) {
            platform.setFunctions(new ArrayList<>());
        }
        if (platform.getExecutions() == null) {
            platform.setExecutions(new ArrayList<>());
        }
        if (platform.getTriggers() == null) {
            platform.setTriggers(new ArrayList<>());
        }
        if (platform.getLayers() == null) {
            platform.setLayers(new ArrayList<>());
        }
        if (platform.getEvents() == null) {
            platform.setEvents(new ArrayList<>());
        }

        // Initialize registries
        platform.setFunctionRegistry(new ConcurrentHashMap<>());
        platform.setExecutionRegistry(new ConcurrentHashMap<>());
        platform.setTriggerRegistry(new ConcurrentHashMap<>());
        platform.setLayerRegistry(new ConcurrentHashMap<>());

        // Initialize counters
        platform.setTotalFunctions(0);
        platform.setActiveFunctions(0);
        platform.setFailedFunctions(0);
        platform.setTotalExecutions(0L);
        platform.setSuccessfulExecutions(0L);
        platform.setFailedExecutions(0L);
        platform.setTotalTriggers(0);
        platform.setActiveTriggers(0);

        platforms.put(platform.getPlatformId(), platform);
        log.info("Created serverless platform: {} (ID: {})", platform.getPlatformName(), platform.getPlatformId());

        return platform;
    }

    /**
     * Get platform
     */
    public Optional<ReportServerless> getPlatform(Long platformId) {
        return Optional.ofNullable(platforms.get(platformId));
    }

    /**
     * Start platform
     */
    public void startPlatform(Long platformId) {
        ReportServerless platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Platform not found: " + platformId);
        }

        platform.startPlatform();
        log.info("Started serverless platform: {}", platformId);
    }

    /**
     * Stop platform
     */
    public void stopPlatform(Long platformId) {
        ReportServerless platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Platform not found: " + platformId);
        }

        platform.stopPlatform();
        log.info("Stopped serverless platform: {}", platformId);
    }

    /**
     * Deploy function
     */
    public ReportServerless.Function deployFunction(Long platformId, String functionName,
                                                     ReportServerless.Runtime runtime, String handler) {
        ReportServerless platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Platform not found: " + platformId);
        }

        ReportServerless.Function function = ReportServerless.Function.builder()
                .functionId(UUID.randomUUID().toString())
                .functionName(functionName)
                .status(ReportServerless.FunctionStatus.DEPLOYING)
                .runtime(runtime)
                .handler(handler)
                .version("$LATEST")
                .environmentVariables(new HashMap<>())
                .layers(new ArrayList<>())
                .permissions(new ArrayList<>())
                .aliases(new ArrayList<>())
                .tags(new HashMap<>())
                .metadata(new HashMap<>())
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .totalInvocations(0L)
                .successfulInvocations(0L)
                .failedInvocations(0L)
                .coldStarts(0L)
                .warmStarts(0L)
                .publishedVersion(false)
                .currentConcurrency(0)
                .build();

        platform.registerFunction(function);
        log.info("Deployed function {} in platform {}", functionName, platformId);

        return function;
    }

    /**
     * Update function status
     */
    public void updateFunctionStatus(Long platformId, String functionId, ReportServerless.FunctionStatus status) {
        ReportServerless platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Platform not found: " + platformId);
        }

        platform.updateFunctionStatus(functionId, status);
        log.info("Updated function {} status to {} in platform {}", functionId, status, platformId);
    }

    /**
     * Invoke function
     */
    public ReportServerless.Execution invokeFunction(Long platformId, String functionId,
                                                      Map<String, Object> input,
                                                      ReportServerless.TriggerType triggerType) {
        ReportServerless platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Platform not found: " + platformId);
        }

        ReportServerless.Function function = platform.getFunction(functionId);
        if (function == null) {
            throw new IllegalArgumentException("Function not found: " + functionId);
        }

        // Simulate execution
        LocalDateTime startTime = LocalDateTime.now();
        boolean coldStart = function.getCurrentConcurrency() == 0;
        boolean success = Math.random() > 0.05; // 95% success rate

        long durationMs = coldStart ? (long) (Math.random() * 500 + 200) : (long) (Math.random() * 200 + 50);
        long initDurationMs = coldStart ? (long) (Math.random() * 300 + 100) : 0L;

        ReportServerless.Execution execution = ReportServerless.Execution.builder()
                .executionId(UUID.randomUUID().toString())
                .functionId(functionId)
                .functionName(function.getFunctionName())
                .status(success ? ReportServerless.ExecutionStatus.SUCCEEDED : ReportServerless.ExecutionStatus.FAILED)
                .startedAt(startTime)
                .completedAt(startTime.plusNanos(durationMs * 1_000_000))
                .durationMs(durationMs)
                .triggerType(triggerType)
                .input(input != null ? input : new HashMap<>())
                .output(success ? Map.of("result", "success") : Map.of())
                .success(success)
                .coldStart(coldStart)
                .initDurationMs(initDurationMs)
                .billedDurationMs((durationMs / 100 + 1) * 100)
                .logMessages(new ArrayList<>())
                .requestId(UUID.randomUUID().toString())
                .metadata(new HashMap<>())
                .build();

        if (!success) {
            execution.setErrorMessage("Simulated execution failure");
            execution.setErrorType("RuntimeException");
        }

        platform.recordExecution(execution);

        // Update function concurrency
        function.setCurrentConcurrency(function.getCurrentConcurrency() + 1);

        log.info("Invoked function {} in platform {}: {} ({}ms, coldStart: {})",
                functionId, platformId, success ? "SUCCESS" : "FAILED", durationMs, coldStart);

        return execution;
    }

    /**
     * Create trigger
     */
    public ReportServerless.Trigger createTrigger(Long platformId, String triggerName,
                                                   ReportServerless.TriggerType triggerType,
                                                   String functionId) {
        ReportServerless platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Platform not found: " + platformId);
        }

        ReportServerless.Trigger trigger = ReportServerless.Trigger.builder()
                .triggerId(UUID.randomUUID().toString())
                .triggerName(triggerName)
                .triggerType(triggerType)
                .functionId(functionId)
                .enabled(true)
                .eventFilter(new HashMap<>())
                .configuration(new HashMap<>())
                .totalTriggers(0L)
                .successfulTriggers(0L)
                .failedTriggers(0L)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        platform.registerTrigger(trigger);
        log.info("Created trigger {} for function {} in platform {}", triggerName, functionId, platformId);

        return trigger;
    }

    /**
     * Create layer
     */
    public ReportServerless.Layer createLayer(Long platformId, String layerName,
                                               List<ReportServerless.Runtime> compatibleRuntimes) {
        ReportServerless platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Platform not found: " + platformId);
        }

        ReportServerless.Layer layer = ReportServerless.Layer.builder()
                .layerId(UUID.randomUUID().toString())
                .layerName(layerName)
                .version(1)
                .compatibleRuntimes(compatibleRuntimes != null ? compatibleRuntimes : new ArrayList<>())
                .usedByFunctions(new ArrayList<>())
                .functionCount(0)
                .tags(new HashMap<>())
                .createdAt(LocalDateTime.now())
                .build();

        platform.registerLayer(layer);
        log.info("Created layer {} in platform {}", layerName, platformId);

        return layer;
    }

    /**
     * Update configuration
     */
    public void updateFunctionConfiguration(Long platformId, String functionId,
                                             Integer timeoutSeconds, Integer memoryMb,
                                             Map<String, String> environmentVariables) {
        ReportServerless platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Platform not found: " + platformId);
        }

        ReportServerless.Function function = platform.getFunction(functionId);
        if (function == null) {
            throw new IllegalArgumentException("Function not found: " + functionId);
        }

        if (timeoutSeconds != null) {
            function.setTimeoutSeconds(timeoutSeconds);
        }
        if (memoryMb != null) {
            function.setMemoryMb(memoryMb);
        }
        if (environmentVariables != null) {
            function.setEnvironmentVariables(environmentVariables);
        }

        function.setLastModifiedAt(LocalDateTime.now());

        platform.recordEvent("FUNCTION_CONFIG_UPDATED",
                "Function configuration updated: " + function.getFunctionName(),
                "FUNCTION", functionId);

        log.info("Updated configuration for function {} in platform {}", functionId, platformId);
    }

    /**
     * Update metrics
     */
    public void updateMetrics(Long platformId) {
        ReportServerless platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Platform not found: " + platformId);
        }

        int totalFunctions = platform.getTotalFunctions() != null ? platform.getTotalFunctions() : 0;
        int activeFunctions = platform.getActiveFunctions() != null ? platform.getActiveFunctions().size() : 0;
        long totalExecutions = platform.getTotalExecutions() != null ? platform.getTotalExecutions() : 0L;
        long successfulExecutions = platform.getSuccessfulExecutions() != null ? platform.getSuccessfulExecutions().size() : 0L;
        long failedExecutions = platform.getFailedExecutions() != null ? platform.getFailedExecutions() : 0L;

        double successRate = totalExecutions > 0 ?
                (successfulExecutions * 100.0 / totalExecutions) : 0.0;

        // Calculate average duration
        double avgDuration = platform.getExecutions() != null ?
                platform.getExecutions().stream()
                        .filter(e -> e.getDurationMs() != null)
                        .mapToLong(ReportServerless.Execution::getDurationMs)
                        .average()
                        .orElse(0.0) : 0.0;

        // Calculate cold start rate
        long totalColdStarts = platform.getExecutions() != null ?
                platform.getExecutions().stream()
                        .filter(e -> Boolean.TRUE.equals(e.getColdStart()))
                        .count() : 0L;

        double coldStartRate = totalExecutions > 0 ?
                (totalColdStarts * 100.0 / totalExecutions) : 0.0;

        ReportServerless.PlatformMetrics metrics = ReportServerless.PlatformMetrics.builder()
                .totalFunctions(totalFunctions)
                .activeFunctions(activeFunctions)
                .totalExecutions(totalExecutions)
                .successfulExecutions(successfulExecutions)
                .failedExecutions(failedExecutions)
                .successRate(successRate)
                .averageDurationMs(avgDuration)
                .p50DurationMs(avgDuration * 0.8)
                .p95DurationMs(avgDuration * 1.8)
                .p99DurationMs(avgDuration * 2.5)
                .totalColdStarts(totalColdStarts)
                .coldStartRate(coldStartRate)
                .measuredAt(LocalDateTime.now())
                .build();

        platform.setMetrics(metrics);

        log.debug("Updated metrics for platform {}: {} functions, {} executions, {:.1f}% success",
                platformId, totalFunctions, totalExecutions, successRate);
    }

    /**
     * Delete platform
     */
    public void deletePlatform(Long platformId) {
        ReportServerless platform = platforms.get(platformId);
        if (platform != null && platform.isHealthy()) {
            stopPlatform(platformId);
        }

        ReportServerless removed = platforms.remove(platformId);
        if (removed != null) {
            log.info("Deleted serverless platform {}", platformId);
        }
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalPlatforms", platforms.size());

        long activePlatforms = platforms.values().stream()
                .filter(ReportServerless::isHealthy)
                .count();

        long totalFunctions = platforms.values().stream()
                .mapToLong(p -> p.getTotalFunctions() != null ? p.getTotalFunctions() : 0L)
                .sum();

        long activeFunctions = platforms.values().stream()
                .mapToLong(p -> p.getActiveFunctions() != null ? p.getActiveFunctions().size() : 0L)
                .sum();

        long totalExecutions = platforms.values().stream()
                .mapToLong(p -> p.getTotalExecutions() != null ? p.getTotalExecutions() : 0L)
                .sum();

        long successfulExecutions = platforms.values().stream()
                .mapToLong(p -> p.getSuccessfulExecutions() != null ? p.getSuccessfulExecutions().size() : 0L)
                .sum();

        stats.put("activePlatforms", activePlatforms);
        stats.put("totalFunctions", totalFunctions);
        stats.put("activeFunctions", activeFunctions);
        stats.put("totalExecutions", totalExecutions);
        stats.put("successfulExecutions", successfulExecutions);

        log.debug("Generated serverless statistics: {} platforms, {} functions, {} executions",
                platforms.size(), totalFunctions, totalExecutions);

        return stats;
    }
}
