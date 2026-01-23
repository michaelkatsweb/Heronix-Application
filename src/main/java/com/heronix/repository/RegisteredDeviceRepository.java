package com.heronix.repository;

import com.heronix.model.domain.RegisteredDevice;
import com.heronix.model.domain.RegisteredDevice.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Registered Device persistence operations.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 20, 2026
 */
@Repository
public interface RegisteredDeviceRepository extends JpaRepository<RegisteredDevice, Long> {

    /**
     * Find device by device ID
     */
    Optional<RegisteredDevice> findByDeviceId(String deviceId);

    /**
     * Check if device ID exists
     */
    boolean existsByDeviceId(String deviceId);

    /**
     * Find device by MAC address
     */
    Optional<RegisteredDevice> findByMacAddressIgnoreCase(String macAddress);

    /**
     * Check if MAC address is already registered (excluding removed/rejected)
     */
    @Query("SELECT COUNT(d) > 0 FROM RegisteredDevice d WHERE UPPER(d.macAddress) = UPPER(:macAddress) " +
           "AND d.status NOT IN ('REMOVED', 'REJECTED')")
    boolean existsByMacAddressActive(@Param("macAddress") String macAddress);

    /**
     * Find all devices for an account
     */
    List<RegisteredDevice> findByAccountTokenOrderByRegistrationRequestedAtDesc(String accountToken);

    /**
     * Count active/pending devices for an account (for limit checking)
     */
    @Query("SELECT COUNT(d) FROM RegisteredDevice d WHERE d.accountToken = :accountToken " +
           "AND d.status IN ('ACTIVE', 'PENDING_APPROVAL')")
    int countActiveDevicesForAccount(@Param("accountToken") String accountToken);

    /**
     * Find pending registrations for admin review
     */
    List<RegisteredDevice> findByStatusOrderByRegistrationRequestedAtAsc(DeviceStatus status);

    /**
     * Find all pending devices
     */
    default List<RegisteredDevice> findPendingRegistrations() {
        return findByStatusOrderByRegistrationRequestedAtAsc(DeviceStatus.PENDING_APPROVAL);
    }

    /**
     * Find all active devices
     */
    default List<RegisteredDevice> findActiveDevices() {
        return findByStatusOrderByRegistrationRequestedAtAsc(DeviceStatus.ACTIVE);
    }

    /**
     * Find device by certificate serial number
     */
    Optional<RegisteredDevice> findByCertificateSerialNumber(String certificateSerialNumber);

    /**
     * Find devices with expiring certificates
     */
    @Query("SELECT d FROM RegisteredDevice d WHERE d.status = 'ACTIVE' " +
           "AND d.certificateExpiresAt BETWEEN :now AND :threshold")
    List<RegisteredDevice> findDevicesWithExpiringCertificates(@Param("now") LocalDateTime now,
                                                                @Param("threshold") LocalDateTime threshold);

    /**
     * Find devices with expired certificates that are still active
     */
    @Query("SELECT d FROM RegisteredDevice d WHERE d.status = 'ACTIVE' " +
           "AND d.certificateExpiresAt < :now")
    List<RegisteredDevice> findDevicesWithExpiredCertificates(@Param("now") LocalDateTime now);

    /**
     * Find all revoked devices
     */
    List<RegisteredDevice> findByStatusOrderByRevokedAtDesc(DeviceStatus status);

    /**
     * Get all revoked devices
     */
    default List<RegisteredDevice> findRevokedDevices() {
        return findByStatusOrderByRevokedAtDesc(DeviceStatus.REVOKED);
    }

    /**
     * Find devices by status list
     */
    List<RegisteredDevice> findByStatusIn(List<DeviceStatus> statuses);

    /**
     * Count devices by status
     */
    long countByStatus(DeviceStatus status);

    /**
     * Find devices not seen since a specific time (possibly inactive)
     */
    @Query("SELECT d FROM RegisteredDevice d WHERE d.status = 'ACTIVE' " +
           "AND (d.lastSeenAt IS NULL OR d.lastSeenAt < :threshold)")
    List<RegisteredDevice> findInactiveDevices(@Param("threshold") LocalDateTime threshold);

    /**
     * Update last seen timestamp
     */
    @Modifying
    @Query("UPDATE RegisteredDevice d SET d.lastSeenAt = :lastSeen WHERE d.deviceId = :deviceId")
    int updateLastSeen(@Param("deviceId") String deviceId, @Param("lastSeen") LocalDateTime lastSeen);

    /**
     * Get all whitelisted MAC addresses (active devices)
     */
    @Query("SELECT d.macAddress FROM RegisteredDevice d WHERE d.status = 'ACTIVE'")
    List<String> findAllWhitelistedMacAddresses();

    /**
     * Search devices by name or MAC
     */
    @Query("SELECT d FROM RegisteredDevice d WHERE " +
           "LOWER(d.deviceName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.macAddress) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.deviceId) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<RegisteredDevice> searchDevices(@Param("search") String search);
}
