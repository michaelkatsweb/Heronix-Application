package com.heronix.controller;

import com.heronix.dto.ReportQuantumCrypto;
import com.heronix.service.ReportQuantumCryptoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Quantum Cryptography API Controller
 *
 * REST API endpoints for quantum-resistant cryptography, post-quantum algorithms,
 * hybrid cryptographic schemes, and quantum key distribution.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 140 - Quantum-Ready Cryptography & Post-Quantum Security
 */
@Slf4j
@RestController
@RequestMapping("/api/quantum-crypto")
@RequiredArgsConstructor
public class ReportQuantumCryptoApiController {

    private final ReportQuantumCryptoService quantumCryptoService;

    /**
     * Create new quantum crypto configuration
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createQuantumCrypto(
            @RequestBody ReportQuantumCrypto crypto) {
        try {
            ReportQuantumCrypto created = quantumCryptoService.createQuantumCrypto(crypto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quantum crypto configuration created successfully");
            response.put("cryptoId", created.getCryptoId());
            response.put("cryptoName", created.getCryptoName());
            response.put("pqcAlgorithm", created.getPqcAlgorithm());
            response.put("securityLevel", created.getSecurityLevel());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create quantum crypto configuration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get quantum crypto configuration by ID
     */
    @GetMapping("/{cryptoId}")
    public ResponseEntity<Map<String, Object>> getQuantumCrypto(@PathVariable Long cryptoId) {
        try {
            ReportQuantumCrypto crypto = quantumCryptoService.getQuantumCrypto(cryptoId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("crypto", crypto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Activate quantum crypto
     */
    @PostMapping("/{cryptoId}/activate")
    public ResponseEntity<Map<String, Object>> activate(@PathVariable Long cryptoId) {
        try {
            ReportQuantumCrypto crypto = quantumCryptoService.activate(cryptoId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quantum crypto activated successfully");
            response.put("cryptoStatus", crypto.getCryptoStatus());
            response.put("activatedAt", crypto.getActivatedAt());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Deactivate quantum crypto
     */
    @PostMapping("/{cryptoId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivate(@PathVariable Long cryptoId) {
        try {
            ReportQuantumCrypto crypto = quantumCryptoService.deactivate(cryptoId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quantum crypto deactivated successfully");
            response.put("cryptoStatus", crypto.getCryptoStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Encrypt data
     */
    @PostMapping("/{cryptoId}/encrypt")
    public ResponseEntity<Map<String, Object>> encrypt(
            @PathVariable Long cryptoId,
            @RequestBody Map<String, Object> data) {
        try {
            Map<String, Object> result = quantumCryptoService.encrypt(cryptoId, data);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Decrypt data
     */
    @PostMapping("/{cryptoId}/decrypt")
    public ResponseEntity<Map<String, Object>> decrypt(
            @PathVariable Long cryptoId,
            @RequestBody Map<String, String> request) {
        try {
            String ciphertext = request.get("ciphertext");
            if (ciphertext == null || ciphertext.isEmpty()) {
                throw new IllegalArgumentException("Ciphertext is required");
            }

            Map<String, Object> result = quantumCryptoService.decrypt(cryptoId, ciphertext);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Generate digital signature
     */
    @PostMapping("/{cryptoId}/sign")
    public ResponseEntity<Map<String, Object>> sign(
            @PathVariable Long cryptoId,
            @RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            if (message == null || message.isEmpty()) {
                throw new IllegalArgumentException("Message is required");
            }

            Map<String, Object> result = quantumCryptoService.sign(cryptoId, message);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Verify digital signature
     */
    @PostMapping("/{cryptoId}/verify")
    public ResponseEntity<Map<String, Object>> verify(
            @PathVariable Long cryptoId,
            @RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            String signature = request.get("signature");

            if (message == null || message.isEmpty() || signature == null || signature.isEmpty()) {
                throw new IllegalArgumentException("Message and signature are required");
            }

            Map<String, Object> result = quantumCryptoService.verify(cryptoId, message, signature);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Perform key exchange
     */
    @PostMapping("/{cryptoId}/key-exchange")
    public ResponseEntity<Map<String, Object>> keyExchange(@PathVariable Long cryptoId) {
        try {
            Map<String, Object> result = quantumCryptoService.keyExchange(cryptoId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Rotate cryptographic keys
     */
    @PostMapping("/{cryptoId}/rotate-keys")
    public ResponseEntity<Map<String, Object>> rotateKeys(@PathVariable Long cryptoId) {
        try {
            ReportQuantumCrypto crypto = quantumCryptoService.rotateKeys(cryptoId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Keys rotated successfully");
            response.put("lastKeyRotation", crypto.getLastKeyRotation());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Perform security assessment
     */
    @GetMapping("/{cryptoId}/security-assessment")
    public ResponseEntity<Map<String, Object>> assessSecurity(@PathVariable Long cryptoId) {
        try {
            Map<String, Object> assessment = quantumCryptoService.assessSecurity(cryptoId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assessment", assessment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get performance statistics
     */
    @GetMapping("/{cryptoId}/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceStats(@PathVariable Long cryptoId) {
        try {
            Map<String, Object> stats = quantumCryptoService.getPerformanceStats(cryptoId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("performance", stats);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get all quantum crypto configurations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllQuantumCrypto() {
        List<ReportQuantumCrypto> configs = quantumCryptoService.getAllQuantumCrypto();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get quantum-resistant configurations
     */
    @GetMapping("/quantum-resistant")
    public ResponseEntity<Map<String, Object>> getQuantumResistantConfigs() {
        List<ReportQuantumCrypto> configs = quantumCryptoService.getQuantumResistantConfigs();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete quantum crypto configuration
     */
    @DeleteMapping("/{cryptoId}")
    public ResponseEntity<Map<String, Object>> deleteQuantumCrypto(@PathVariable Long cryptoId) {
        try {
            quantumCryptoService.deleteQuantumCrypto(cryptoId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Quantum crypto configuration deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = quantumCryptoService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }
}
