package com.heronix.service;

import com.heronix.model.domain.CertificateRevocationEntry;
import com.heronix.model.domain.CertificateRevocationEntry.RevocationType;
import com.heronix.model.domain.RegisteredDevice;
import com.heronix.model.domain.RegisteredDevice.DeviceStatus;
import com.heronix.repository.CertificateRevocationRepository;
import com.heronix.repository.RegisteredDeviceRepository;
import com.heronix.security.SecurityContext;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Device Authentication Service
 *
 * Manages device registration, authentication, and certificate management for
 * external devices that sync with the SIS via the DMZ server (Server 3).
 *
 * SECURITY ARCHITECTURE:
 * ----------------------
 * - X.509 certificates (2048-bit RSA) for device authentication
 * - MAC address whitelisting for additional verification
 * - Device fingerprinting for fraud detection
 * - Multi-device support (up to 5 devices per student/parent)
 * - Certificate Revocation List (CRL) for compromised devices
 *
 * DEVICE FLOW:
 * 1. Device requests registration with MAC address + device fingerprint
 * 2. Admin approves device registration
 * 3. Certificate is generated and must be installed on device
 * 4. Device uses certificate for all future communications
 * 5. Device can be revoked at any time (added to CRL)
 *
 * IMPORTANT: This service runs on the SIS (Server 1) and only manages
 * device metadata. The actual certificate validation occurs on Server 3 (DMZ).
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 20, 2026
 */
@Slf4j
@Service
public class DeviceAuthenticationService {

    // Maximum devices per student/parent account
    private static final int MAX_DEVICES_PER_ACCOUNT = 5;

    // MAC address validation pattern
    private static final Pattern MAC_PATTERN = Pattern.compile(
            "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");

    // Certificate validity period (days)
    @Value("${heronix.device.certificate.validity-days:365}")
    private int certificateValidityDays;

    @Autowired
    private RegisteredDeviceRepository deviceRepository;

    @Autowired
    private CertificateRevocationRepository crlRepository;

    // Cached whitelist for performance (refreshed on changes)
    private volatile Set<String> cachedWhitelistedMacs = null;

    // ========================================================================
    // DEVICE REGISTRATION
    // ========================================================================

    /**
     * Request device registration (creates pending registration).
     *
     * @param request Device registration request
     * @return Registration result with pending status
     */
    @Transactional
    public DeviceRegistrationResult requestRegistration(DeviceRegistrationRequest request) {
        log.info("DEVICE_AUTH: Registration request for account {} from MAC {}",
                request.getAccountToken(), request.getMacAddress());

        // Validate MAC address format
        if (!isValidMacAddress(request.getMacAddress())) {
            return DeviceRegistrationResult.builder()
                    .success(false)
                    .errorMessage("Invalid MAC address format")
                    .build();
        }

        // Check if device already registered
        if (deviceRepository.existsByMacAddressActive(request.getMacAddress())) {
            Optional<RegisteredDevice> existing = deviceRepository.findByMacAddressIgnoreCase(request.getMacAddress());
            return DeviceRegistrationResult.builder()
                    .success(false)
                    .errorMessage("Device is already registered")
                    .existingDeviceId(existing.map(RegisteredDevice::getDeviceId).orElse(null))
                    .build();
        }

        // Check device limit for account
        int currentDeviceCount = deviceRepository.countActiveDevicesForAccount(request.getAccountToken());
        if (currentDeviceCount >= MAX_DEVICES_PER_ACCOUNT) {
            return DeviceRegistrationResult.builder()
                    .success(false)
                    .errorMessage("Maximum device limit reached (" + MAX_DEVICES_PER_ACCOUNT + ")")
                    .currentDeviceCount(currentDeviceCount)
                    .build();
        }

        // Generate device ID
        String deviceId = generateDeviceId();

        // Create pending registration
        RegisteredDevice device = RegisteredDevice.builder()
                .deviceId(deviceId)
                .accountToken(request.getAccountToken())
                .macAddress(request.getMacAddress().toUpperCase())
                .deviceFingerprint(request.getDeviceFingerprint())
                .deviceName(request.getDeviceName())
                .deviceType(request.getDeviceType())
                .operatingSystem(request.getOperatingSystem())
                .status(DeviceStatus.PENDING_APPROVAL)
                .registrationRequestedAt(LocalDateTime.now())
                .build();

        // Save to repository
        device = deviceRepository.save(device);

        log.info("DEVICE_AUTH: Created pending registration {} for account {}",
                deviceId, request.getAccountToken());

        return DeviceRegistrationResult.builder()
                .success(true)
                .deviceId(deviceId)
                .status(DeviceStatus.PENDING_APPROVAL)
                .message("Registration request submitted. Awaiting administrator approval.")
                .build();
    }

    /**
     * Approve a pending device registration (admin action).
     *
     * @param deviceId Device ID to approve
     * @param approvedBy Staff username who approved
     * @return Approval result with certificate info
     */
    @Transactional
    public DeviceApprovalResult approveRegistration(String deviceId, String approvedBy) {
        log.warn("DEVICE_AUTH: Approving device {} by {}", deviceId, approvedBy);

        Optional<RegisteredDevice> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (deviceOpt.isEmpty()) {
            return DeviceApprovalResult.builder()
                    .success(false)
                    .errorMessage("Device not found: " + deviceId)
                    .build();
        }

        RegisteredDevice device = deviceOpt.get();

        if (device.getStatus() != DeviceStatus.PENDING_APPROVAL) {
            return DeviceApprovalResult.builder()
                    .success(false)
                    .errorMessage("Device is not in pending status: " + device.getStatus())
                    .build();
        }

        // Generate certificate info (actual certificate generation would be done by PKI system)
        CertificateInfo certInfo = generateCertificateInfo(device);

        // Update device status
        device.setStatus(DeviceStatus.ACTIVE);
        device.setApprovedAt(LocalDateTime.now());
        device.setApprovedBy(approvedBy);
        device.setCertificateSerialNumber(certInfo.getSerialNumber());
        device.setCertificateExpiresAt(certInfo.getExpiresAt());
        device.setCertificateFingerprint(certInfo.getFingerprint());

        // Save to repository
        deviceRepository.save(device);

        // Invalidate whitelist cache
        cachedWhitelistedMacs = null;

        log.warn("DEVICE_AUTH: Device {} approved by {}. Certificate: {}",
                deviceId, approvedBy, certInfo.getSerialNumber());

        return DeviceApprovalResult.builder()
                .success(true)
                .deviceId(deviceId)
                .certificateSerialNumber(certInfo.getSerialNumber())
                .certificateExpiresAt(certInfo.getExpiresAt())
                .message("Device approved. Certificate must be installed on device.")
                .certificateInstallationInstructions(
                        "1. Download certificate from admin portal\n" +
                        "2. Install certificate in device trust store\n" +
                        "3. Configure app to use certificate for authentication")
                .build();
    }

    /**
     * Reject a pending device registration.
     *
     * @param deviceId Device ID to reject
     * @param rejectedBy Staff username who rejected
     * @param reason Rejection reason
     * @return Rejection result
     */
    @Transactional
    public DeviceRejectionResult rejectRegistration(String deviceId, String rejectedBy, String reason) {
        log.warn("DEVICE_AUTH: Rejecting device {} by {} - Reason: {}", deviceId, rejectedBy, reason);

        Optional<RegisteredDevice> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (deviceOpt.isEmpty()) {
            return DeviceRejectionResult.builder()
                    .success(false)
                    .errorMessage("Device not found: " + deviceId)
                    .build();
        }

        RegisteredDevice device = deviceOpt.get();
        device.setStatus(DeviceStatus.REJECTED);
        device.setRejectedAt(LocalDateTime.now());
        device.setRejectedBy(rejectedBy);
        device.setRejectionReason(reason);

        deviceRepository.save(device);

        return DeviceRejectionResult.builder()
                .success(true)
                .deviceId(deviceId)
                .message("Device registration rejected")
                .build();
    }

    // ========================================================================
    // CERTIFICATE REVOCATION
    // ========================================================================

    /**
     * Revoke a device certificate (add to CRL).
     *
     * @param deviceId Device ID to revoke
     * @param revokedBy Staff username who revoked
     * @param reason Revocation reason
     * @return Revocation result
     */
    @Transactional
    public CertificateRevocationResult revokeCertificate(String deviceId, String revokedBy, String reason) {
        log.warn("SECURITY: Revoking certificate for device {} by {} - Reason: {}",
                deviceId, revokedBy, reason);

        Optional<RegisteredDevice> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (deviceOpt.isEmpty()) {
            return CertificateRevocationResult.builder()
                    .success(false)
                    .errorMessage("Device not found: " + deviceId)
                    .build();
        }

        RegisteredDevice device = deviceOpt.get();
        LocalDateTime revokedAt = LocalDateTime.now();

        // Add to CRL repository
        if (device.getCertificateSerialNumber() != null) {
            CertificateRevocationEntry crlEntry = CertificateRevocationEntry.builder()
                    .serialNumber(device.getCertificateSerialNumber())
                    .deviceId(device.getDeviceId())
                    .accountToken(device.getAccountToken())
                    .revokedAt(revokedAt)
                    .revokedBy(revokedBy)
                    .reason(reason)
                    .revocationType(RevocationType.SECURITY_CONCERN)
                    .certificateFingerprint(device.getCertificateFingerprint())
                    .originalExpiresAt(device.getCertificateExpiresAt())
                    .build();
            crlRepository.save(crlEntry);
        }

        // Update device status
        device.setStatus(DeviceStatus.REVOKED);
        device.setRevokedAt(revokedAt);
        device.setRevokedBy(revokedBy);
        device.setRevocationReason(reason);

        deviceRepository.save(device);

        // Invalidate whitelist cache
        cachedWhitelistedMacs = null;

        log.warn("SECURITY: Certificate {} revoked for device {}",
                device.getCertificateSerialNumber(), deviceId);

        return CertificateRevocationResult.builder()
                .success(true)
                .deviceId(deviceId)
                .certificateSerialNumber(device.getCertificateSerialNumber())
                .revokedAt(device.getRevokedAt())
                .message("Certificate revoked and added to CRL")
                .build();
    }

    /**
     * Get current Certificate Revocation List.
     *
     * @return CRL data for sync to DMZ server
     */
    public CertificateRevocationList getCertificateRevocationList() {
        List<CertificateRevocationEntry> crlEntries = crlRepository.findAllByOrderByRevokedAtDesc();

        List<CRLEntry> entries = crlEntries.stream()
                .map(entry -> CRLEntry.builder()
                        .serialNumber(entry.getSerialNumber())
                        .revokedAt(entry.getRevokedAt())
                        .reason(entry.getReason())
                        .build())
                .collect(Collectors.toList());

        return CertificateRevocationList.builder()
                .generatedAt(LocalDateTime.now())
                .entries(entries)
                .totalRevoked(entries.size())
                .checksum(generateCRLChecksum(entries))
                .build();
    }

    // ========================================================================
    // DEVICE VALIDATION
    // ========================================================================

    /**
     * Validate device authentication request.
     *
     * @param certificateSerial Certificate serial number
     * @param macAddress MAC address
     * @param deviceFingerprint Device fingerprint
     * @return Validation result
     */
    @Transactional
    public DeviceValidationResult validateDevice(String certificateSerial,
                                                  String macAddress,
                                                  String deviceFingerprint) {

        // Check if certificate is revoked
        if (crlRepository.existsBySerialNumber(certificateSerial)) {
            log.warn("SECURITY: Rejected revoked certificate: {}", certificateSerial);
            return DeviceValidationResult.builder()
                    .valid(false)
                    .reason("Certificate has been revoked")
                    .build();
        }

        // Find device by certificate
        Optional<RegisteredDevice> deviceOpt = deviceRepository.findByCertificateSerialNumber(certificateSerial);

        if (deviceOpt.isEmpty()) {
            log.warn("SECURITY: Unknown certificate: {}", certificateSerial);
            return DeviceValidationResult.builder()
                    .valid(false)
                    .reason("Certificate not recognized")
                    .build();
        }

        RegisteredDevice device = deviceOpt.get();

        // Validate MAC address
        if (!device.getMacAddress().equalsIgnoreCase(macAddress)) {
            log.warn("SECURITY: MAC address mismatch for device {}. Expected: {}, Got: {}",
                    device.getDeviceId(), device.getMacAddress(), macAddress);
            return DeviceValidationResult.builder()
                    .valid(false)
                    .reason("Device identifier mismatch")
                    .securityAlert(true)
                    .build();
        }

        // Validate MAC whitelist
        Set<String> whitelist = getWhitelistedMacAddresses();
        if (!whitelist.contains(macAddress.toUpperCase())) {
            log.warn("SECURITY: MAC not whitelisted: {}", macAddress);
            return DeviceValidationResult.builder()
                    .valid(false)
                    .reason("Device not authorized")
                    .build();
        }

        // Validate device fingerprint (if enabled)
        if (device.getDeviceFingerprint() != null &&
            !device.getDeviceFingerprint().equals(deviceFingerprint)) {
            log.warn("SECURITY: Device fingerprint mismatch for {}. Possible device clone.",
                    device.getDeviceId());
            return DeviceValidationResult.builder()
                    .valid(false)
                    .reason("Device verification failed")
                    .securityAlert(true)
                    .build();
        }

        // Check certificate expiration
        if (device.getCertificateExpiresAt() != null &&
            LocalDateTime.now().isAfter(device.getCertificateExpiresAt())) {
            return DeviceValidationResult.builder()
                    .valid(false)
                    .reason("Certificate has expired")
                    .build();
        }

        // Check device status
        if (device.getStatus() != DeviceStatus.ACTIVE) {
            return DeviceValidationResult.builder()
                    .valid(false)
                    .reason("Device is not active: " + device.getStatus())
                    .build();
        }

        // Update last seen
        device.setLastSeenAt(LocalDateTime.now());
        deviceRepository.save(device);

        return DeviceValidationResult.builder()
                .valid(true)
                .deviceId(device.getDeviceId())
                .accountToken(device.getAccountToken())
                .build();
    }

    // ========================================================================
    // DEVICE MANAGEMENT
    // ========================================================================

    /**
     * Get all devices for an account.
     *
     * @param accountToken Account token
     * @return List of registered devices
     */
    public List<RegisteredDevice> getDevicesForAccount(String accountToken) {
        return deviceRepository.findByAccountTokenOrderByRegistrationRequestedAtDesc(accountToken);
    }

    /**
     * Get all pending device registrations (for admin review).
     *
     * @return List of RegisteredDevice
     */
    public List<RegisteredDevice> getPendingRegistrations() {
        return deviceRepository.findPendingRegistrations();
    }

    /**
     * Get all active devices.
     *
     * @return List of active RegisteredDevice
     */
    public List<RegisteredDevice> getActiveDevices() {
        return deviceRepository.findActiveDevices();
    }

    /**
     * Remove a device from an account.
     *
     * @param deviceId Device ID
     * @param removedBy User who removed
     * @return Removal result
     */
    @Transactional
    public DeviceRemovalResult removeDevice(String deviceId, String removedBy) {
        log.info("DEVICE_AUTH: Removing device {} by {}", deviceId, removedBy);

        Optional<RegisteredDevice> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (deviceOpt.isEmpty()) {
            return DeviceRemovalResult.builder()
                    .success(false)
                    .errorMessage("Device not found")
                    .build();
        }

        RegisteredDevice device = deviceOpt.get();

        // Revoke certificate if active
        if (device.getCertificateSerialNumber() != null) {
            CertificateRevocationEntry crlEntry = CertificateRevocationEntry.builder()
                    .serialNumber(device.getCertificateSerialNumber())
                    .deviceId(device.getDeviceId())
                    .accountToken(device.getAccountToken())
                    .revokedAt(LocalDateTime.now())
                    .revokedBy(removedBy)
                    .reason("Device removed from account")
                    .revocationType(RevocationType.DEVICE_REMOVED)
                    .certificateFingerprint(device.getCertificateFingerprint())
                    .originalExpiresAt(device.getCertificateExpiresAt())
                    .build();
            crlRepository.save(crlEntry);
        }

        // Mark as removed
        device.setStatus(DeviceStatus.REMOVED);
        device.setRemovedAt(LocalDateTime.now());
        device.setRemovedBy(removedBy);

        deviceRepository.save(device);

        // Invalidate whitelist cache
        cachedWhitelistedMacs = null;

        return DeviceRemovalResult.builder()
                .success(true)
                .deviceId(deviceId)
                .message("Device removed successfully")
                .build();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private boolean isValidMacAddress(String mac) {
        return mac != null && MAC_PATTERN.matcher(mac).matches();
    }

    /**
     * Get cached whitelist of MAC addresses.
     */
    private Set<String> getWhitelistedMacAddresses() {
        if (cachedWhitelistedMacs == null) {
            List<String> macs = deviceRepository.findAllWhitelistedMacAddresses();
            cachedWhitelistedMacs = new HashSet<>(macs);
        }
        return cachedWhitelistedMacs;
    }

    private String generateDeviceId() {
        return "DEV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private CertificateInfo generateCertificateInfo(RegisteredDevice device) {
        // TODO: Integrate with actual PKI/CA system for certificate generation
        // TODO: Use HSM/TPM for private key storage and signing operations
        // TODO: Implement proper X.509 certificate with 2048-bit RSA key pair
        // MOCK IMPLEMENTATION - Replace with real PKI integration
        String serial = "CERT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(certificateValidityDays);
        String fingerprint = "SHA256:" + UUID.randomUUID().toString().replace("-", "").toUpperCase();

        return CertificateInfo.builder()
                .serialNumber(serial)
                .expiresAt(expiresAt)
                .fingerprint(fingerprint)
                .keyAlgorithm("RSA-2048")
                .signatureAlgorithm("SHA256withRSA")
                .build();
    }

    private String generateCRLChecksum(List<CRLEntry> entries) {
        StringBuilder sb = new StringBuilder();
        for (CRLEntry entry : entries) {
            sb.append(entry.getSerialNumber()).append("|");
        }
        return Integer.toHexString(sb.toString().hashCode()).toUpperCase();
    }

    // ========================================================================
    // SERVICE METHODS FOR UI
    // ========================================================================

    /**
     * Count pending devices.
     */
    public long countPendingDevices() {
        return deviceRepository.countByStatus(DeviceStatus.PENDING_APPROVAL);
    }

    /**
     * Count active devices.
     */
    public long countActiveDevices() {
        return deviceRepository.countByStatus(DeviceStatus.ACTIVE);
    }

    /**
     * Count revoked certificates.
     */
    public long countRevokedCertificates() {
        return crlRepository.countTotalRevocations();
    }

    /**
     * Search devices by name, MAC, or ID.
     */
    public List<RegisteredDevice> searchDevices(String search) {
        return deviceRepository.searchDevices(search);
    }

    /**
     * Get devices with expiring certificates.
     */
    public List<RegisteredDevice> getDevicesWithExpiringCertificates(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(days);
        return deviceRepository.findDevicesWithExpiringCertificates(now, threshold);
    }

    // ========================================================================
    // DTOs (RegisteredDevice and DeviceStatus are now entities in model.domain)
    // ========================================================================

    @Data
    @Builder
    public static class DeviceRegistrationRequest {
        private String accountToken;
        private String macAddress;
        private String deviceFingerprint;
        private String deviceName;
        private String deviceType;
        private String operatingSystem;
    }

    @Data
    @Builder
    public static class DeviceRegistrationResult {
        private boolean success;
        private String deviceId;
        private DeviceStatus status;
        private String message;
        private String errorMessage;
        private String existingDeviceId;
        private int currentDeviceCount;
    }

    @Data
    @Builder
    public static class DeviceApprovalResult {
        private boolean success;
        private String deviceId;
        private String certificateSerialNumber;
        private LocalDateTime certificateExpiresAt;
        private String message;
        private String errorMessage;
        private String certificateInstallationInstructions;
    }

    @Data
    @Builder
    public static class DeviceRejectionResult {
        private boolean success;
        private String deviceId;
        private String message;
        private String errorMessage;
    }

    @Data
    @Builder
    public static class CertificateRevocationResult {
        private boolean success;
        private String deviceId;
        private String certificateSerialNumber;
        private LocalDateTime revokedAt;
        private String message;
        private String errorMessage;
    }

    @Data
    @Builder
    public static class DeviceValidationResult {
        private boolean valid;
        private String deviceId;
        private String accountToken;
        private String reason;
        private boolean securityAlert;
    }

    @Data
    @Builder
    public static class DeviceRemovalResult {
        private boolean success;
        private String deviceId;
        private String message;
        private String errorMessage;
    }

    @Data
    @Builder
    public static class CertificateInfo {
        private String serialNumber;
        private LocalDateTime expiresAt;
        private String fingerprint;
        private String keyAlgorithm;
        private String signatureAlgorithm;
    }

    @Data
    @Builder
    public static class CertificateRevocationList {
        private LocalDateTime generatedAt;
        private List<CRLEntry> entries;
        private int totalRevoked;
        private String checksum;
    }

    @Data
    @Builder
    public static class CRLEntry {
        private String serialNumber;
        private LocalDateTime revokedAt;
        private String reason;
    }
}
