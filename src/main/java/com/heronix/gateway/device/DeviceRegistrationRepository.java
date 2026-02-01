package com.heronix.gateway.device;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing registered device entities.
 *
 * @author Heronix Development Team
 * @version 1.0
 */
@Repository
public interface DeviceRegistrationRepository extends JpaRepository<RegisteredDevice, Long> {

    /**
     * Find device by its unique device ID
     */
    Optional<RegisteredDevice> findByDeviceId(String deviceId);

    /**
     * Find device by public key hash
     */
    Optional<RegisteredDevice> findByPublicKeyHash(String publicKeyHash);

    /**
     * Find all active devices
     */
    @Query("SELECT d FROM GatewayRegisteredDevice d WHERE d.status = 'ACTIVE' AND d.expiresAt > :now")
    List<RegisteredDevice> findAllActiveDevices(@Param("now") LocalDateTime now);

    /**
     * Find devices by type
     */
    List<RegisteredDevice> findByDeviceType(RegisteredDevice.DeviceType deviceType);

    /**
     * Find devices by organization
     */
    List<RegisteredDevice> findByOrganizationNameContainingIgnoreCase(String organizationName);

    /**
     * Find devices pending approval
     */
    List<RegisteredDevice> findByStatus(RegisteredDevice.DeviceStatus status);

    /**
     * Find devices expiring soon
     */
    @Query("SELECT d FROM GatewayRegisteredDevice d WHERE d.status = 'ACTIVE' AND d.expiresAt BETWEEN :now AND :threshold")
    List<RegisteredDevice> findDevicesExpiringSoon(
        @Param("now") LocalDateTime now,
        @Param("threshold") LocalDateTime threshold
    );

    /**
     * Find devices with specific permission
     */
    @Query("SELECT d FROM GatewayRegisteredDevice d JOIN d.permissions p WHERE p = :permission AND d.status = 'ACTIVE'")
    List<RegisteredDevice> findByPermission(@Param("permission") RegisteredDevice.DataPermission permission);

    /**
     * Count active devices by type
     */
    @Query("SELECT d.deviceType, COUNT(d) FROM GatewayRegisteredDevice d WHERE d.status = 'ACTIVE' GROUP BY d.deviceType")
    List<Object[]> countActiveDevicesByType();

    /**
     * Check if device ID exists
     */
    boolean existsByDeviceId(String deviceId);

    /**
     * Check if public key hash is already registered
     */
    boolean existsByPublicKeyHash(String publicKeyHash);

    /**
     * Find devices with high failure rates
     */
    @Query("SELECT d FROM GatewayRegisteredDevice d WHERE d.failedTransmissionCount > :threshold AND d.status = 'ACTIVE'")
    List<RegisteredDevice> findDevicesWithHighFailureRate(@Param("threshold") Long threshold);
}
