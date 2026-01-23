package com.heronix.repository;

import com.heronix.model.domain.NetworkDevice;
import com.heronix.model.enums.NetworkDeviceStatus;
import com.heronix.model.enums.NetworkDeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Network Device persistence operations.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 21, 2026
 */
@Repository
public interface NetworkDeviceRepository extends JpaRepository<NetworkDevice, Long> {

    /**
     * Find device by IP address
     */
    Optional<NetworkDevice> findByIpAddress(String ipAddress);

    /**
     * Check if IP address exists
     */
    boolean existsByIpAddress(String ipAddress);

    /**
     * Find device by MAC address
     */
    Optional<NetworkDevice> findByMacAddressIgnoreCase(String macAddress);

    /**
     * Find devices by type
     */
    List<NetworkDevice> findByDeviceTypeOrderByDeviceNameAsc(NetworkDeviceType deviceType);

    /**
     * Find devices by status
     */
    List<NetworkDevice> findByStatusOrderByDeviceNameAsc(NetworkDeviceStatus status);

    /**
     * Find devices by location
     */
    List<NetworkDevice> findByLocationContainingIgnoreCaseOrderByDeviceNameAsc(String location);

    /**
     * Find all devices with monitoring enabled
     */
    List<NetworkDevice> findByMonitoringEnabledTrueOrderByLastPingTimeAsc();

    /**
     * Find devices that need to be pinged
     */
    @Query("SELECT d FROM NetworkDevice d WHERE d.monitoringEnabled = true AND " +
           "(d.lastPingTime IS NULL OR d.lastPingTime < :threshold)")
    List<NetworkDevice> findDevicesNeedingPing(@Param("threshold") LocalDateTime threshold);

    /**
     * Find online devices
     */
    default List<NetworkDevice> findOnlineDevices() {
        return findByStatusOrderByDeviceNameAsc(NetworkDeviceStatus.ONLINE);
    }

    /**
     * Find offline devices
     */
    default List<NetworkDevice> findOfflineDevices() {
        return findByStatusOrderByDeviceNameAsc(NetworkDeviceStatus.OFFLINE);
    }

    /**
     * Find devices requiring attention (offline or warning)
     */
    @Query("SELECT d FROM NetworkDevice d WHERE d.status IN ('OFFLINE', 'WARNING') ORDER BY d.status, d.deviceName")
    List<NetworkDevice> findDevicesRequiringAttention();

    /**
     * Find devices under maintenance
     */
    default List<NetworkDevice> findDevicesUnderMaintenance() {
        return findByStatusOrderByDeviceNameAsc(NetworkDeviceStatus.MAINTENANCE);
    }

    /**
     * Count devices by status
     */
    long countByStatus(NetworkDeviceStatus status);

    /**
     * Count devices by type
     */
    long countByDeviceType(NetworkDeviceType deviceType);

    /**
     * Count online devices
     */
    default long countOnlineDevices() {
        return countByStatus(NetworkDeviceStatus.ONLINE);
    }

    /**
     * Count offline devices
     */
    default long countOfflineDevices() {
        return countByStatus(NetworkDeviceStatus.OFFLINE);
    }

    /**
     * Update device status
     */
    @Modifying
    @Query("UPDATE NetworkDevice d SET d.status = :status, d.lastStatusChange = :timestamp WHERE d.id = :id")
    int updateDeviceStatus(@Param("id") Long id, @Param("status") NetworkDeviceStatus status,
                           @Param("timestamp") LocalDateTime timestamp);

    /**
     * Update last ping info
     */
    @Modifying
    @Query("UPDATE NetworkDevice d SET d.lastPingTime = :pingTime, d.lastPingLatencyMs = :latency, " +
           "d.status = :status, d.consecutiveFailures = :failures WHERE d.id = :id")
    int updatePingResult(@Param("id") Long id, @Param("pingTime") LocalDateTime pingTime,
                         @Param("latency") Long latency, @Param("status") NetworkDeviceStatus status,
                         @Param("failures") Integer failures);

    /**
     * Search devices by name, IP, or location
     */
    @Query("SELECT d FROM NetworkDevice d WHERE " +
           "LOWER(d.deviceName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.ipAddress) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.location) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.macAddress) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY d.deviceName")
    List<NetworkDevice> searchDevices(@Param("search") String search);

    /**
     * Find devices with alerts enabled that are offline
     */
    @Query("SELECT d FROM NetworkDevice d WHERE d.alertOnOffline = true AND d.status = 'OFFLINE'")
    List<NetworkDevice> findOfflineDevicesWithAlerts();

    /**
     * Get average latency for online devices
     */
    @Query("SELECT AVG(d.lastPingLatencyMs) FROM NetworkDevice d WHERE d.status = 'ONLINE' AND d.lastPingLatencyMs IS NOT NULL")
    Double getAverageLatency();

    /**
     * Find devices by type and status
     */
    List<NetworkDevice> findByDeviceTypeAndStatusOrderByDeviceNameAsc(NetworkDeviceType type, NetworkDeviceStatus status);

    /**
     * Get device statistics summary
     */
    @Query("SELECT d.status, COUNT(d) FROM NetworkDevice d GROUP BY d.status")
    List<Object[]> getStatusSummary();

    /**
     * Get device type distribution
     */
    @Query("SELECT d.deviceType, COUNT(d) FROM NetworkDevice d GROUP BY d.deviceType")
    List<Object[]> getTypeDistribution();

    /**
     * Find all devices ordered by name
     */
    List<NetworkDevice> findAllByOrderByDeviceNameAsc();

    /**
     * Find devices with high latency
     */
    @Query("SELECT d FROM NetworkDevice d WHERE d.lastPingLatencyMs > :threshold ORDER BY d.lastPingLatencyMs DESC")
    List<NetworkDevice> findDevicesWithHighLatency(@Param("threshold") Long thresholdMs);

    /**
     * Find devices with consecutive failures
     */
    @Query("SELECT d FROM NetworkDevice d WHERE d.consecutiveFailures >= :threshold ORDER BY d.consecutiveFailures DESC")
    List<NetworkDevice> findDevicesWithConsecutiveFailures(@Param("threshold") Integer threshold);
}
