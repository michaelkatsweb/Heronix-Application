package com.heronix.service;

import com.heronix.model.domain.NetworkDevice;
import com.heronix.model.enums.NetworkDeviceStatus;
import com.heronix.model.enums.NetworkDeviceType;
import com.heronix.model.enums.NetworkCheckType;
import com.heronix.repository.NetworkDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Service for managing network devices and performing network operations.
 *
 * Provides functionality for:
 * - Device CRUD operations
 * - Ping/connectivity checks
 * - Port scanning
 * - Network status monitoring
 * - Alert management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 21, 2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NetworkService {

    private final NetworkDeviceRepository networkDeviceRepository;

    private final ExecutorService pingExecutor = Executors.newFixedThreadPool(10);

    // ==================== Device CRUD Operations ====================

    /**
     * Get all network devices
     */
    public List<NetworkDevice> getAllDevices() {
        return networkDeviceRepository.findAllByOrderByDeviceNameAsc();
    }

    /**
     * Get device by ID
     */
    public Optional<NetworkDevice> getDeviceById(Long id) {
        return networkDeviceRepository.findById(id);
    }

    /**
     * Get device by IP address
     */
    public Optional<NetworkDevice> getDeviceByIpAddress(String ipAddress) {
        return networkDeviceRepository.findByIpAddress(ipAddress);
    }

    /**
     * Save a network device
     */
    @Transactional
    public NetworkDevice saveDevice(NetworkDevice device) {
        if (device.getId() == null) {
            device.setCreatedAt(LocalDateTime.now());
        }
        device.setUpdatedAt(LocalDateTime.now());
        return networkDeviceRepository.save(device);
    }

    /**
     * Delete a network device
     */
    @Transactional
    public void deleteDevice(Long id) {
        networkDeviceRepository.deleteById(id);
    }

    /**
     * Check if IP address is already registered
     */
    public boolean isIpAddressRegistered(String ipAddress) {
        return networkDeviceRepository.existsByIpAddress(ipAddress);
    }

    /**
     * Search devices
     */
    public List<NetworkDevice> searchDevices(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllDevices();
        }
        return networkDeviceRepository.searchDevices(query.trim());
    }

    // ==================== Device Filtering ====================

    /**
     * Get devices by type
     */
    public List<NetworkDevice> getDevicesByType(NetworkDeviceType type) {
        return networkDeviceRepository.findByDeviceTypeOrderByDeviceNameAsc(type);
    }

    /**
     * Get devices by status
     */
    public List<NetworkDevice> getDevicesByStatus(NetworkDeviceStatus status) {
        return networkDeviceRepository.findByStatusOrderByDeviceNameAsc(status);
    }

    /**
     * Get devices by location
     */
    public List<NetworkDevice> getDevicesByLocation(String location) {
        return networkDeviceRepository.findByLocationContainingIgnoreCaseOrderByDeviceNameAsc(location);
    }

    /**
     * Get devices requiring attention
     */
    public List<NetworkDevice> getDevicesRequiringAttention() {
        return networkDeviceRepository.findDevicesRequiringAttention();
    }

    /**
     * Get online devices
     */
    public List<NetworkDevice> getOnlineDevices() {
        return networkDeviceRepository.findOnlineDevices();
    }

    /**
     * Get offline devices
     */
    public List<NetworkDevice> getOfflineDevices() {
        return networkDeviceRepository.findOfflineDevices();
    }

    // ==================== Network Operations ====================

    /**
     * Ping a single device
     */
    public PingResult pingDevice(NetworkDevice device) {
        long startTime = System.currentTimeMillis();
        boolean reachable = false;
        String errorMessage = null;

        try {
            InetAddress address = InetAddress.getByName(device.getIpAddress());
            reachable = address.isReachable(device.getPingTimeoutMs());
        } catch (IOException e) {
            errorMessage = e.getMessage();
            log.warn("Ping failed for device {} ({}): {}", device.getDeviceName(), device.getIpAddress(), e.getMessage());
        }

        long latency = System.currentTimeMillis() - startTime;
        return new PingResult(device, reachable, reachable ? latency : null, errorMessage);
    }

    /**
     * Ping a device by IP address
     */
    public PingResult pingIpAddress(String ipAddress, int timeoutMs) {
        long startTime = System.currentTimeMillis();
        boolean reachable = false;
        String errorMessage = null;

        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            reachable = address.isReachable(timeoutMs);
        } catch (IOException e) {
            errorMessage = e.getMessage();
        }

        long latency = System.currentTimeMillis() - startTime;
        return new PingResult(null, reachable, reachable ? latency : null, errorMessage);
    }

    /**
     * Ping all devices with monitoring enabled
     */
    @Async
    public CompletableFuture<List<PingResult>> pingAllDevices() {
        List<NetworkDevice> devices = networkDeviceRepository.findByMonitoringEnabledTrueOrderByLastPingTimeAsc();
        return pingDevices(devices);
    }

    /**
     * Ping a list of devices
     */
    public CompletableFuture<List<PingResult>> pingDevices(List<NetworkDevice> devices) {
        List<CompletableFuture<PingResult>> futures = devices.stream()
                .map(device -> CompletableFuture.supplyAsync(() -> {
                    PingResult result = pingDevice(device);
                    updateDeviceAfterPing(device, result);
                    return result;
                }, pingExecutor))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    /**
     * Update device after ping
     */
    @Transactional
    public void updateDeviceAfterPing(NetworkDevice device, PingResult result) {
        if (result.isReachable()) {
            device.recordSuccessfulPing(result.getLatencyMs());
        } else {
            device.recordFailedPing();
        }
        networkDeviceRepository.save(device);
    }

    /**
     * Check if a port is open
     */
    public boolean isPortOpen(String ipAddress, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ipAddress, port), timeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Scan common ports on a device
     */
    public Map<Integer, Boolean> scanCommonPorts(String ipAddress, int timeoutMs) {
        int[] commonPorts = {22, 23, 80, 443, 445, 3389, 5900, 8080, 9100};
        Map<Integer, Boolean> results = new LinkedHashMap<>();

        for (int port : commonPorts) {
            results.put(port, isPortOpen(ipAddress, port, timeoutMs));
        }

        return results;
    }

    /**
     * Scan a range of ports
     */
    public Map<Integer, Boolean> scanPortRange(String ipAddress, int startPort, int endPort, int timeoutMs) {
        Map<Integer, Boolean> results = new LinkedHashMap<>();

        for (int port = startPort; port <= endPort; port++) {
            results.put(port, isPortOpen(ipAddress, port, timeoutMs));
        }

        return results;
    }

    /**
     * Set device to maintenance mode
     */
    @Transactional
    public NetworkDevice setMaintenanceMode(Long deviceId, boolean maintenance, String updatedBy) {
        NetworkDevice device = networkDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));

        if (maintenance) {
            device.setStatus(NetworkDeviceStatus.MAINTENANCE);
            device.setMonitoringEnabled(false);
        } else {
            device.setStatus(NetworkDeviceStatus.UNKNOWN);
            device.setMonitoringEnabled(true);
        }

        device.setUpdatedBy(updatedBy);
        return networkDeviceRepository.save(device);
    }

    // ==================== Statistics ====================

    /**
     * Get device count by status
     */
    public Map<NetworkDeviceStatus, Long> getStatusCounts() {
        Map<NetworkDeviceStatus, Long> counts = new EnumMap<>(NetworkDeviceStatus.class);
        for (NetworkDeviceStatus status : NetworkDeviceStatus.values()) {
            counts.put(status, networkDeviceRepository.countByStatus(status));
        }
        return counts;
    }

    /**
     * Get device count by type
     */
    public Map<NetworkDeviceType, Long> getTypeCounts() {
        Map<NetworkDeviceType, Long> counts = new EnumMap<>(NetworkDeviceType.class);
        for (NetworkDeviceType type : NetworkDeviceType.values()) {
            counts.put(type, networkDeviceRepository.countByDeviceType(type));
        }
        return counts;
    }

    /**
     * Get total device count
     */
    public long getTotalDeviceCount() {
        return networkDeviceRepository.count();
    }

    /**
     * Get online device count
     */
    public long getOnlineDeviceCount() {
        return networkDeviceRepository.countOnlineDevices();
    }

    /**
     * Get offline device count
     */
    public long getOfflineDeviceCount() {
        return networkDeviceRepository.countOfflineDevices();
    }

    /**
     * Get average network latency
     */
    public Double getAverageLatency() {
        return networkDeviceRepository.getAverageLatency();
    }

    /**
     * Get network health summary
     */
    public NetworkHealthSummary getNetworkHealthSummary() {
        long total = getTotalDeviceCount();
        long online = getOnlineDeviceCount();
        long offline = getOfflineDeviceCount();
        long warning = networkDeviceRepository.countByStatus(NetworkDeviceStatus.WARNING);
        long maintenance = networkDeviceRepository.countByStatus(NetworkDeviceStatus.MAINTENANCE);
        long unknown = networkDeviceRepository.countByStatus(NetworkDeviceStatus.UNKNOWN);
        Double avgLatency = getAverageLatency();

        double healthPercentage = total > 0 ? (online * 100.0 / total) : 100.0;

        return new NetworkHealthSummary(total, online, offline, warning, maintenance, unknown,
                avgLatency != null ? avgLatency : 0.0, healthPercentage);
    }

    /**
     * Get unique locations
     */
    public List<String> getUniqueLocations() {
        return networkDeviceRepository.findAll().stream()
                .map(NetworkDevice::getLocation)
                .filter(loc -> loc != null && !loc.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ==================== Inner Classes ====================

    /**
     * Result of a ping operation
     */
    public static class PingResult {
        private final NetworkDevice device;
        private final boolean reachable;
        private final Long latencyMs;
        private final String errorMessage;

        public PingResult(NetworkDevice device, boolean reachable, Long latencyMs, String errorMessage) {
            this.device = device;
            this.reachable = reachable;
            this.latencyMs = latencyMs;
            this.errorMessage = errorMessage;
        }

        public NetworkDevice getDevice() { return device; }
        public boolean isReachable() { return reachable; }
        public Long getLatencyMs() { return latencyMs; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Network health summary
     */
    public static class NetworkHealthSummary {
        private final long totalDevices;
        private final long onlineDevices;
        private final long offlineDevices;
        private final long warningDevices;
        private final long maintenanceDevices;
        private final long unknownDevices;
        private final double averageLatencyMs;
        private final double healthPercentage;

        public NetworkHealthSummary(long totalDevices, long onlineDevices, long offlineDevices,
                                    long warningDevices, long maintenanceDevices, long unknownDevices,
                                    double averageLatencyMs, double healthPercentage) {
            this.totalDevices = totalDevices;
            this.onlineDevices = onlineDevices;
            this.offlineDevices = offlineDevices;
            this.warningDevices = warningDevices;
            this.maintenanceDevices = maintenanceDevices;
            this.unknownDevices = unknownDevices;
            this.averageLatencyMs = averageLatencyMs;
            this.healthPercentage = healthPercentage;
        }

        public long getTotalDevices() { return totalDevices; }
        public long getOnlineDevices() { return onlineDevices; }
        public long getOfflineDevices() { return offlineDevices; }
        public long getWarningDevices() { return warningDevices; }
        public long getMaintenanceDevices() { return maintenanceDevices; }
        public long getUnknownDevices() { return unknownDevices; }
        public double getAverageLatencyMs() { return averageLatencyMs; }
        public double getHealthPercentage() { return healthPercentage; }

        public String getHealthStatus() {
            if (healthPercentage >= 95) return "Excellent";
            if (healthPercentage >= 85) return "Good";
            if (healthPercentage >= 70) return "Fair";
            return "Poor";
        }
    }
}
