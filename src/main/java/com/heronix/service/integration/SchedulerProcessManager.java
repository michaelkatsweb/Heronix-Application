package com.heronix.service.integration;

import com.heronix.integration.SchedulerApiClient;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Manages the SchedulerV2 process lifecycle.
 * When SchedulerV2 is not running and auto-start is enabled,
 * this service launches it as a subprocess and waits for it to boot.
 */
@Slf4j
@Service
public class SchedulerProcessManager {

    private final SchedulerApiClient schedulerApiClient;

    @Value("${heronix.scheduler.auto-start.enabled:true}")
    private boolean autoStartEnabled;

    @Value("${heronix.scheduler.auto-start.jar-path:}")
    private String jarPath;

    @Value("${heronix.scheduler.auto-start.startup-timeout:30}")
    private int startupTimeout;

    @Value("${heronix.scheduler.auto-start.java-path:java}")
    private String javaPath;

    @Value("${heronix.scheduler.api-url:http://localhost:8090}")
    private String schedulerApiUrl;

    private Process schedulerProcess;

    public SchedulerProcessManager(@Lazy SchedulerApiClient schedulerApiClient) {
        this.schedulerApiClient = schedulerApiClient;
    }

    /**
     * Ensures SchedulerV2 is running. If not available and auto-start is enabled,
     * launches it as a subprocess and waits for it to become healthy.
     *
     * @return true if SchedulerV2 is running after this call
     */
    public boolean ensureSchedulerRunning() {
        if (schedulerApiClient.isSchedulerAvailable()) {
            return true;
        }

        if (!isAutoStartEnabled()) {
            log.warn("SchedulerV2 is not available and auto-start is not configured");
            return false;
        }

        return startScheduler();
    }

    /**
     * Starts the SchedulerV2 process and waits for it to become healthy.
     *
     * @return true if SchedulerV2 started successfully and is responding
     */
    private boolean startScheduler() {
        File jarFile = resolveJarFile();
        if (jarFile == null) {
            return false;
        }

        log.info("Starting SchedulerV2 from: {}", jarFile.getAbsolutePath());

        try {
            // Extract port from API URL for --server.port argument
            String port = extractPort(schedulerApiUrl);

            ProcessBuilder pb = new ProcessBuilder(
                    javaPath, "-jar", jarFile.getAbsolutePath(),
                    "--server.port=" + port
            );
            pb.redirectErrorStream(true);

            // Redirect subprocess output to SIS log via inherited IO
            pb.inheritIO();

            schedulerProcess = pb.start();
            log.info("SchedulerV2 process started (PID: {})", schedulerProcess.pid());

        } catch (IOException e) {
            log.error("Failed to start SchedulerV2: {}", e.getMessage(), e);
            return false;
        }

        // Poll health endpoint until ready or timeout
        long deadline = System.currentTimeMillis() + (startupTimeout * 1000L);
        while (System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for SchedulerV2 to start");
                return false;
            }

            // Check if process died
            if (!schedulerProcess.isAlive()) {
                log.error("SchedulerV2 process exited with code: {}", schedulerProcess.exitValue());
                return false;
            }

            if (schedulerApiClient.isSchedulerAvailable()) {
                log.info("SchedulerV2 is now available");
                return true;
            }

            log.debug("Waiting for SchedulerV2 to start...");
        }

        log.error("SchedulerV2 failed to start within {} seconds", startupTimeout);
        return false;
    }

    /**
     * Stops the SchedulerV2 subprocess if it was launched by this manager.
     */
    @PreDestroy
    public void stopScheduler() {
        if (schedulerProcess == null || !schedulerProcess.isAlive()) {
            return;
        }

        log.info("Stopping SchedulerV2 subprocess...");
        schedulerProcess.destroy();

        try {
            boolean exited = schedulerProcess.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
            if (!exited) {
                log.warn("SchedulerV2 did not stop gracefully, forcing termination");
                schedulerProcess.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            schedulerProcess.destroyForcibly();
        }

        log.info("SchedulerV2 subprocess stopped");
    }

    /**
     * @return true if auto-start is enabled and a JAR path is configured
     */
    public boolean isAutoStartEnabled() {
        return autoStartEnabled && jarPath != null && !jarPath.isBlank();
    }

    private File resolveJarFile() {
        if (jarPath == null || jarPath.isBlank()) {
            log.warn("SchedulerV2 JAR path is not configured (heronix.scheduler.auto-start.jar-path)");
            return null;
        }

        File jarFile = new File(jarPath);
        if (!jarFile.isAbsolute()) {
            jarFile = new File(System.getProperty("user.dir"), jarPath);
        }

        if (!jarFile.exists()) {
            log.error("SchedulerV2 JAR not found at: {}", jarFile.getAbsolutePath());
            return null;
        }

        return jarFile;
    }

    private String extractPort(String url) {
        try {
            java.net.URI uri = java.net.URI.create(url);
            int port = uri.getPort();
            return port > 0 ? String.valueOf(port) : "8090";
        } catch (Exception e) {
            return "8090";
        }
    }
}
