package com.heronix.gateway.device;

import com.heronix.gateway.encryption.GatewayEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for managing device registration and verification.
 *
 * All external devices must be registered and verified before they can
 * receive any data from the SIS gateway.
 *
 * @author Heronix Development Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceRegistrationService {

    private final DeviceRegistrationRepository deviceRepository;
    private final GatewayEncryptionService encryptionService;

    /**
     * Register a new device for data reception
     */
    @Transactional
    public RegisteredDevice registerDevice(DeviceRegistrationRequest request) {
        log.info("Processing device registration request for: {}", request.getDeviceName());

        // Validate the registration request
        validateRegistrationRequest(request);

        // Check for duplicate device ID
        if (deviceRepository.existsByDeviceId(request.getDeviceId())) {
            throw new DeviceRegistrationException("Device ID already registered: " + request.getDeviceId());
        }

        // Hash the public key
        String publicKeyHash = hashPublicKey(request.getPublicKeyCertificate());

        // Check for duplicate public key
        if (deviceRepository.existsByPublicKeyHash(publicKeyHash)) {
            throw new DeviceRegistrationException("Public key certificate already registered");
        }

        // Generate a symmetric key for this device
        String symmetricKey = encryptionService.generateDeviceSymmetricKey();
        String encryptedKey = encryptionService.encryptWithMasterKey(symmetricKey);

        // Create the device entity
        RegisteredDevice device = RegisteredDevice.builder()
            .deviceId(request.getDeviceId())
            .deviceName(request.getDeviceName())
            .deviceType(request.getDeviceType())
            .organizationName(request.getOrganizationName())
            .adminEmail(request.getAdminEmail())
            .publicKeyHash(publicKeyHash)
            .publicKeyCertificate(request.getPublicKeyCertificate())
            .deviceFingerprint(request.getDeviceFingerprint())
            .allowedIpRanges(request.getAllowedIpRanges())
            .status(RegisteredDevice.DeviceStatus.PENDING_APPROVAL)
            .permissions(new HashSet<>(request.getRequestedPermissions()))
            .encryptedSymmetricKey(encryptedKey)
            .registeredAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusYears(1))
            .notes(request.getNotes())
            .build();

        device = deviceRepository.save(device);

        log.info("Device registered with ID: {} (pending approval)", device.getDeviceId());
        return device;
    }

    /**
     * Approve a pending device registration
     */
    @Transactional
    public RegisteredDevice approveDevice(String deviceId, String approvedBy,
                                          Set<RegisteredDevice.DataPermission> grantedPermissions) {
        RegisteredDevice device = getDeviceOrThrow(deviceId);

        if (device.getStatus() != RegisteredDevice.DeviceStatus.PENDING_APPROVAL) {
            throw new DeviceRegistrationException("Device is not pending approval: " + device.getStatus());
        }

        device.setStatus(RegisteredDevice.DeviceStatus.ACTIVE);
        device.setApprovedBy(approvedBy);
        device.setApprovedAt(LocalDateTime.now());
        device.setPermissions(grantedPermissions);

        device = deviceRepository.save(device);

        log.info("Device approved: {} by {}", deviceId, approvedBy);
        return device;
    }

    /**
     * Revoke a device registration
     */
    @Transactional
    public RegisteredDevice revokeDevice(String deviceId, String reason, String revokedBy) {
        RegisteredDevice device = getDeviceOrThrow(deviceId);

        device.setStatus(RegisteredDevice.DeviceStatus.REVOKED);
        device.setRevocationReason(reason);
        device.setRevokedAt(LocalDateTime.now());
        device.setNotes(device.getNotes() + "\nRevoked by: " + revokedBy);

        device = deviceRepository.save(device);

        log.warn("Device revoked: {} - Reason: {}", deviceId, reason);
        return device;
    }

    /**
     * Suspend a device temporarily
     */
    @Transactional
    public RegisteredDevice suspendDevice(String deviceId, String reason) {
        RegisteredDevice device = getDeviceOrThrow(deviceId);

        device.setStatus(RegisteredDevice.DeviceStatus.SUSPENDED);
        device.setNotes(device.getNotes() + "\nSuspended: " + reason + " at " + LocalDateTime.now());

        device = deviceRepository.save(device);

        log.warn("Device suspended: {} - Reason: {}", deviceId, reason);
        return device;
    }

    /**
     * Verify if a device is authorized to receive data
     */
    public DeviceVerificationResult verifyDevice(String deviceId, String publicKeyHash, String sourceIp) {
        log.debug("Verifying device: {} from IP: {}", deviceId, sourceIp);

        Optional<RegisteredDevice> deviceOpt = deviceRepository.findByDeviceId(deviceId);

        if (deviceOpt.isEmpty()) {
            log.warn("SECURITY: Unregistered device attempted access: {}", deviceId);
            return DeviceVerificationResult.unregistered();
        }

        RegisteredDevice device = deviceOpt.get();

        // Check device status
        if (!device.isActive()) {
            log.warn("SECURITY: Inactive device attempted access: {} (status: {})",
                deviceId, device.getStatus());
            return DeviceVerificationResult.inactive(device.getStatus().name());
        }

        // Verify public key hash
        if (!device.getPublicKeyHash().equals(publicKeyHash)) {
            log.warn("SECURITY: Public key mismatch for device: {}", deviceId);
            return DeviceVerificationResult.invalidCredentials();
        }

        // Verify IP address if configured
        if (device.getAllowedIpRanges() != null && !device.getAllowedIpRanges().isEmpty()) {
            if (!isIpAllowed(sourceIp, device.getAllowedIpRanges())) {
                log.warn("SECURITY: IP not allowed for device: {} (IP: {})", deviceId, sourceIp);
                return DeviceVerificationResult.ipNotAllowed();
            }
        }

        // Update last verified timestamp
        device.setLastVerifiedAt(LocalDateTime.now());
        deviceRepository.save(device);

        log.debug("Device verified successfully: {}", deviceId);
        return DeviceVerificationResult.success(device);
    }

    /**
     * Get device by ID
     */
    public Optional<RegisteredDevice> getDevice(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }

    /**
     * Get all active devices
     */
    public List<RegisteredDevice> getActiveDevices() {
        return deviceRepository.findAllActiveDevices(LocalDateTime.now());
    }

    /**
     * Get devices pending approval
     */
    public List<RegisteredDevice> getPendingDevices() {
        return deviceRepository.findByStatus(RegisteredDevice.DeviceStatus.PENDING_APPROVAL);
    }

    /**
     * Get devices expiring within specified days
     */
    public List<RegisteredDevice> getDevicesExpiringSoon(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(days);
        return deviceRepository.findDevicesExpiringSoon(now, threshold);
    }

    /**
     * Renew device registration
     */
    @Transactional
    public RegisteredDevice renewDevice(String deviceId, int additionalYears) {
        RegisteredDevice device = getDeviceOrThrow(deviceId);

        LocalDateTime newExpiry = device.getExpiresAt().plusYears(additionalYears);
        device.setExpiresAt(newExpiry);

        if (device.getStatus() == RegisteredDevice.DeviceStatus.EXPIRED) {
            device.setStatus(RegisteredDevice.DeviceStatus.ACTIVE);
        }

        device = deviceRepository.save(device);

        log.info("Device registration renewed: {} until {}", deviceId, newExpiry);
        return device;
    }

    /**
     * Record transmission result
     */
    @Transactional
    public void recordTransmission(String deviceId, boolean success) {
        Optional<RegisteredDevice> deviceOpt = deviceRepository.findByDeviceId(deviceId);

        deviceOpt.ifPresent(device -> {
            if (success) {
                device.recordSuccessfulTransmission();
            } else {
                device.recordFailedTransmission();
            }
            deviceRepository.save(device);
        });
    }

    /**
     * Get the symmetric key for a device (decrypted)
     */
    public String getDeviceSymmetricKey(String deviceId) {
        RegisteredDevice device = getDeviceOrThrow(deviceId);

        if (!device.isActive()) {
            throw new DeviceRegistrationException("Cannot get key for inactive device");
        }

        return encryptionService.decryptWithMasterKey(device.getEncryptedSymmetricKey());
    }

    // ================== Private Helper Methods ==================

    private RegisteredDevice getDeviceOrThrow(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new DeviceRegistrationException("Device not found: " + deviceId));
    }

    private void validateRegistrationRequest(DeviceRegistrationRequest request) {
        if (request.getDeviceId() == null || request.getDeviceId().isBlank()) {
            throw new DeviceRegistrationException("Device ID is required");
        }
        if (request.getDeviceName() == null || request.getDeviceName().isBlank()) {
            throw new DeviceRegistrationException("Device name is required");
        }
        if (request.getDeviceType() == null) {
            throw new DeviceRegistrationException("Device type is required");
        }
        if (request.getPublicKeyCertificate() == null || request.getPublicKeyCertificate().isBlank()) {
            throw new DeviceRegistrationException("Public key certificate is required");
        }
        if (request.getOrganizationName() == null || request.getOrganizationName().isBlank()) {
            throw new DeviceRegistrationException("Organization name is required");
        }
    }

    private String hashPublicKey(String publicKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(publicKey.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new DeviceRegistrationException("Failed to hash public key", e);
        }
    }

    private boolean isIpAllowed(String sourceIp, String allowedRanges) {
        if (sourceIp == null || allowedRanges == null) {
            return false;
        }

        try {
            String[] ranges = allowedRanges.split(",");
            InetAddress sourceAddr = InetAddress.getByName(sourceIp);

            for (String range : ranges) {
                range = range.trim();
                if (isIpInRange(sourceAddr, range)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            log.error("Failed to check IP range: {}", e.getMessage());
            return false;
        }
    }

    private boolean isIpInRange(InetAddress ip, String cidr) {
        try {
            if (cidr.contains("/")) {
                String[] parts = cidr.split("/");
                InetAddress rangeAddr = InetAddress.getByName(parts[0]);
                int prefixLength = Integer.parseInt(parts[1]);

                byte[] ipBytes = ip.getAddress();
                byte[] rangeBytes = rangeAddr.getAddress();

                if (ipBytes.length != rangeBytes.length) {
                    return false;
                }

                int fullBytes = prefixLength / 8;
                int remainingBits = prefixLength % 8;

                for (int i = 0; i < fullBytes; i++) {
                    if (ipBytes[i] != rangeBytes[i]) {
                        return false;
                    }
                }

                if (remainingBits > 0 && fullBytes < ipBytes.length) {
                    int mask = 0xFF << (8 - remainingBits);
                    if ((ipBytes[fullBytes] & mask) != (rangeBytes[fullBytes] & mask)) {
                        return false;
                    }
                }

                return true;
            } else {
                // Exact IP match
                return ip.equals(InetAddress.getByName(cidr));
            }
        } catch (Exception e) {
            return false;
        }
    }
}
