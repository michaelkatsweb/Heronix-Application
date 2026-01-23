package com.heronix.controller;

import com.heronix.dto.ReportQuantum;
import com.heronix.service.ReportQuantumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Quantum Computing API Controller
 *
 * REST API endpoints for quantum computing and quantum-resistant cryptography.
 *
 * Endpoints:
 * - POST /api/quantum - Create quantum system
 * - GET /api/quantum/{id} - Get quantum system
 * - POST /api/quantum/{id}/start - Start quantum system
 * - POST /api/quantum/{id}/stop - Stop quantum system
 * - POST /api/quantum/{id}/circuit - Create quantum circuit
 * - POST /api/quantum/{id}/circuit/{circuitId}/execute - Execute circuit
 * - POST /api/quantum/{id}/crypto - Create cryptography system
 * - POST /api/quantum/{id}/key - Generate quantum key
 * - POST /api/quantum/{id}/encrypt - Encrypt report
 * - POST /api/quantum/{id}/algorithm - Create quantum algorithm
 * - POST /api/quantum/{id}/optimize - Run optimization
 * - PUT /api/quantum/{id}/metrics - Update metrics
 * - DELETE /api/quantum/{id} - Delete quantum system
 * - GET /api/quantum/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 101 - Report Quantum Computing & Advanced Cryptography
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/quantum")
@RequiredArgsConstructor
@Slf4j
public class ReportQuantumApiController {

    private final ReportQuantumService quantumService;

    /**
     * Create quantum system
     */
    @PostMapping
    public ResponseEntity<ReportQuantum> createQuantumSystem(@RequestBody ReportQuantum system) {
        log.info("POST /api/quantum - Creating quantum system: {}", system.getSystemName());

        try {
            ReportQuantum created = quantumService.createQuantumSystem(system);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating quantum system", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get quantum system
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportQuantum> getQuantumSystem(@PathVariable Long id) {
        log.info("GET /api/quantum/{}", id);

        try {
            return quantumService.getQuantumSystem(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching quantum system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start quantum system
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startQuantumSystem(@PathVariable Long id) {
        log.info("POST /api/quantum/{}/start", id);

        try {
            quantumService.startQuantumSystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quantum system started");
            response.put("quantumSystemId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting quantum system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop quantum system
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopQuantumSystem(@PathVariable Long id) {
        log.info("POST /api/quantum/{}/stop", id);

        try {
            quantumService.stopQuantumSystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quantum system stopped");
            response.put("quantumSystemId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping quantum system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create quantum circuit
     */
    @PostMapping("/{id}/circuit")
    public ResponseEntity<ReportQuantum.QuantumCircuit> createCircuit(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum/{}/circuit", id);

        try {
            String circuitName = (String) request.get("circuitName");
            Integer numQubits = ((Number) request.get("numQubits")).intValue();
            @SuppressWarnings("unchecked")
            List<ReportQuantum.QuantumGate> gates = (List<ReportQuantum.QuantumGate>) request.get("gates");

            ReportQuantum.QuantumCircuit circuit = quantumService.createCircuit(
                    id, circuitName, numQubits, gates
            );

            return ResponseEntity.ok(circuit);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating circuit in quantum system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Execute quantum circuit
     */
    @PostMapping("/{id}/circuit/{circuitId}/execute")
    public ResponseEntity<ReportQuantum.QuantumJob> executeCircuit(
            @PathVariable Long id,
            @PathVariable String circuitId,
            @RequestBody Map<String, Integer> request) {
        log.info("POST /api/quantum/{}/circuit/{}/execute", id, circuitId);

        try {
            Integer shots = request.get("shots");

            ReportQuantum.QuantumJob job = quantumService.executeCircuit(id, circuitId, shots);

            return ResponseEntity.ok(job);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system or circuit not found: {}, {}", id, circuitId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error executing circuit in quantum system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create cryptography system
     */
    @PostMapping("/{id}/crypto")
    public ResponseEntity<ReportQuantum.QuantumCrypto> createCryptoSystem(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum/{}/crypto", id);

        try {
            String cryptoName = (String) request.get("cryptoName");
            String algorithmStr = (String) request.get("algorithm");
            Integer keySize = ((Number) request.get("keySize")).intValue();

            ReportQuantum.CryptographyAlgorithm algorithm =
                    ReportQuantum.CryptographyAlgorithm.valueOf(algorithmStr);

            ReportQuantum.QuantumCrypto crypto = quantumService.createCryptoSystem(
                    id, cryptoName, algorithm, keySize
            );

            return ResponseEntity.ok(crypto);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating crypto system in quantum system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Generate quantum key
     */
    @PostMapping("/{id}/key")
    public ResponseEntity<ReportQuantum.QuantumKey> generateQuantumKey(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum/{}/key", id);

        try {
            String keyName = (String) request.get("keyName");
            String algorithmStr = (String) request.get("algorithm");
            Integer keySize = ((Number) request.get("keySize")).intValue();
            String ownerId = (String) request.get("ownerId");

            ReportQuantum.CryptographyAlgorithm algorithm =
                    ReportQuantum.CryptographyAlgorithm.valueOf(algorithmStr);

            ReportQuantum.QuantumKey key = quantumService.generateQuantumKey(
                    id, keyName, algorithm, keySize, ownerId
            );

            return ResponseEntity.ok(key);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error generating quantum key in quantum system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Encrypt report
     */
    @PostMapping("/{id}/encrypt")
    public ResponseEntity<ReportQuantum.EncryptedReport> encryptReport(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/quantum/{}/encrypt", id);

        try {
            String reportId = request.get("reportId");
            String reportName = request.get("reportName");
            String keyId = request.get("keyId");
            String reportData = request.get("reportData");

            ReportQuantum.EncryptedReport encryptedReport = quantumService.encryptReport(
                    id, reportId, reportName, keyId, reportData
            );

            return ResponseEntity.ok(encryptedReport);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system or key not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error encrypting report in quantum system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create quantum algorithm
     */
    @PostMapping("/{id}/algorithm")
    public ResponseEntity<ReportQuantum.QuantumAlgorithm> createAlgorithm(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum/{}/algorithm", id);

        try {
            String algorithmName = (String) request.get("algorithmName");
            String algorithmTypeStr = (String) request.get("algorithmType");
            Integer requiredQubits = ((Number) request.get("requiredQubits")).intValue();

            ReportQuantum.AlgorithmType algorithmType =
                    ReportQuantum.AlgorithmType.valueOf(algorithmTypeStr);

            ReportQuantum.QuantumAlgorithm algorithm = quantumService.createAlgorithm(
                    id, algorithmName, algorithmType, requiredQubits
            );

            return ResponseEntity.ok(algorithm);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating algorithm in quantum system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Run optimization
     */
    @PostMapping("/{id}/optimize")
    public ResponseEntity<ReportQuantum.OptimizationResult> runOptimization(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/quantum/{}/optimize", id);

        try {
            String algorithmId = (String) request.get("algorithmId");
            @SuppressWarnings("unchecked")
            Map<String, Object> inputProblem = (Map<String, Object>) request.get("inputProblem");

            ReportQuantum.OptimizationResult result = quantumService.runOptimization(
                    id, algorithmId, inputProblem
            );

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system or algorithm not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error running optimization in quantum system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/quantum/{}/metrics", id);

        try {
            quantumService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("quantumSystemId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Quantum system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for quantum system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete quantum system
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteQuantumSystem(@PathVariable Long id) {
        log.info("DELETE /api/quantum/{}", id);

        try {
            quantumService.deleteQuantumSystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quantum system deleted");
            response.put("quantumSystemId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting quantum system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/quantum/stats");

        try {
            Map<String, Object> stats = quantumService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching quantum statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
