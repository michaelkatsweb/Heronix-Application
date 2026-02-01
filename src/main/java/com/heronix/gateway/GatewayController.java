package com.heronix.gateway;

import com.heronix.gateway.device.*;
import com.heronix.gateway.proxy.SecureOutboundProxyService;
import com.heronix.gateway.proxy.TransmissionResult;
import com.heronix.gateway.sanitization.SanitizationContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API for Secure Gateway operations.
 *
 * Provides endpoints for:
 * - Device registration and management
 * - Secure data transmission
 * - Gateway status and monitoring
 *
 * All endpoints require appropriate authorization.
 *
 * @author Heronix Development Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/gateway")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Secure Gateway", description = "Device registration and secure data transmission")
@SecurityRequirement(name = "bearerAuth")
public class GatewayController {

    private final DeviceRegistrationService deviceService;
    private final SecureOutboundProxyService proxyService;

    // ================== Device Registration Endpoints ==================

    @PostMapping("/devices/register")
    @Operation(summary = "Register a new device for data reception")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SYSTEM')")
    public ResponseEntity<Map<String, Object>> registerDevice(
            @RequestBody DeviceRegistrationRequest request) {

        try {
            RegisteredDevice device = deviceService.registerDevice(request);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("deviceId", device.getDeviceId());
            response.put("status", device.getStatus().name());
            response.put("message", "Device registered successfully. Awaiting approval.");
            response.put("expiresAt", device.getExpiresAt().toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (DeviceRegistrationException e) {
            log.warn("Device registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/devices/{deviceId}/approve")
    @Operation(summary = "Approve a pending device registration")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> approveDevice(
            @PathVariable String deviceId,
            @RequestBody ApprovalRequest request,
            @RequestHeader("X-Approved-By") String approvedBy) {

        try {
            RegisteredDevice device = deviceService.approveDevice(
                deviceId, approvedBy, request.getPermissions()
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "deviceId", device.getDeviceId(),
                "status", device.getStatus().name(),
                "permissions", device.getPermissions()
            ));

        } catch (DeviceRegistrationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/devices/{deviceId}/revoke")
    @Operation(summary = "Revoke a device registration")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> revokeDevice(
            @PathVariable String deviceId,
            @RequestBody RevocationRequest request,
            @RequestHeader("X-Revoked-By") String revokedBy) {

        try {
            RegisteredDevice device = deviceService.revokeDevice(
                deviceId, request.getReason(), revokedBy
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "deviceId", device.getDeviceId(),
                "status", device.getStatus().name(),
                "revokedAt", device.getRevokedAt().toString()
            ));

        } catch (DeviceRegistrationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/devices")
    @Operation(summary = "List all registered devices")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> listDevices(
            @RequestParam(required = false) String status) {

        List<SecureOutboundProxyService.DeviceSummary> devices = proxyService.getRegisteredDevices();

        return ResponseEntity.ok(Map.of(
            "devices", devices,
            "count", devices.size()
        ));
    }

    @GetMapping("/devices/{deviceId}")
    @Operation(summary = "Get device details")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDevice(@PathVariable String deviceId) {
        Optional<RegisteredDevice> deviceOpt = deviceService.getDevice(deviceId);

        if (deviceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RegisteredDevice device = deviceOpt.get();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deviceId", device.getDeviceId());
        response.put("deviceName", device.getDeviceName());
        response.put("deviceType", device.getDeviceType().name());
        response.put("organization", device.getOrganizationName());
        response.put("status", device.getStatus().name());
        response.put("permissions", device.getPermissions());
        response.put("registeredAt", device.getRegisteredAt().toString());
        response.put("expiresAt", device.getExpiresAt().toString());
        response.put("lastVerifiedAt", device.getLastVerifiedAt() != null ?
            device.getLastVerifiedAt().toString() : null);
        response.put("transmissionCount", device.getTransmissionCount());
        response.put("failedTransmissionCount", device.getFailedTransmissionCount());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/devices/pending")
    @Operation(summary = "List devices pending approval")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getPendingDevices() {
        List<RegisteredDevice> pending = deviceService.getPendingDevices();

        List<Map<String, Object>> response = pending.stream()
            .map(d -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("deviceId", d.getDeviceId());
                map.put("deviceName", d.getDeviceName());
                map.put("deviceType", d.getDeviceType().name());
                map.put("organization", d.getOrganizationName());
                map.put("adminEmail", d.getAdminEmail());
                map.put("registeredAt", d.getRegisteredAt().toString());
                map.put("requestedPermissions", d.getPermissions());
                return map;
            })
            .toList();

        return ResponseEntity.ok(response);
    }

    // ================== Data Transmission Endpoints ==================

    @PostMapping("/transmit")
    @Operation(summary = "Transmit data through the secure gateway")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM') or hasAuthority('SCOPE_gateway:transmit')")
    public ResponseEntity<Map<String, Object>> transmitData(
            @RequestBody TransmissionRequest request,
            HttpServletRequest httpRequest) {

        String sourceIp = getClientIp(httpRequest);

        TransmissionResult result = proxyService.transmitToDevice(
            request.getDeviceId(),
            request.getData(),
            request.getContext(),
            sourceIp,
            request.getPublicKeyHash()
        );

        if (result.isSuccess()) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("transmissionId", result.getTransmissionId());
            response.put("encryptedPayload", Map.of(
                "algorithm", result.getEncryptedPayload().getAlgorithm(),
                "data", result.getEncryptedPayload().getEncryptedData(),
                "contentHash", result.getEncryptedPayload().getContentHash(),
                "timestamp", result.getEncryptedPayload().getTimestamp()
            ));
            return ResponseEntity.ok(response);
        }

        HttpStatus status = result.isBlocked() ? HttpStatus.FORBIDDEN : HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(status).body(Map.of(
            "success", false,
            "transmissionId", result.getTransmissionId(),
            "errorCode", result.getErrorCode(),
            "errorMessage", result.getErrorMessage()
        ));
    }

    @PostMapping("/transmit/verify")
    @Operation(summary = "Verify if a device can receive specific data type")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SYSTEM')")
    public ResponseEntity<Map<String, Object>> verifyTransmission(
            @RequestParam String deviceId,
            @RequestParam SanitizationContext.DataType dataType) {

        boolean canReceive = proxyService.canDeviceReceive(deviceId, dataType);

        return ResponseEntity.ok(Map.of(
            "deviceId", deviceId,
            "dataType", dataType.name(),
            "canReceive", canReceive
        ));
    }

    // ================== Gateway Status Endpoints ==================

    @GetMapping("/status")
    @Operation(summary = "Get gateway status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatus() {
        List<SecureOutboundProxyService.DeviceSummary> devices = proxyService.getRegisteredDevices();

        long totalTransmissions = devices.stream()
            .mapToLong(SecureOutboundProxyService.DeviceSummary::transmissionCount)
            .sum();

        return ResponseEntity.ok(Map.of(
            "status", "OPERATIONAL",
            "activeDevices", devices.size(),
            "totalTransmissions", totalTransmissions,
            "encryptionAlgorithm", "AES-256-GCM",
            "keyExchangeAlgorithm", "RSA-2048-OAEP"
        ));
    }

    // ================== Request/Response DTOs ==================

    @lombok.Data
    public static class ApprovalRequest {
        private Set<RegisteredDevice.DataPermission> permissions;
    }

    @lombok.Data
    public static class RevocationRequest {
        private String reason;
    }

    @lombok.Data
    public static class TransmissionRequest {
        private String deviceId;
        private String publicKeyHash;
        private Map<String, Object> data;
        private SanitizationContext context;
    }

    // ================== Helper Methods ==================

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
