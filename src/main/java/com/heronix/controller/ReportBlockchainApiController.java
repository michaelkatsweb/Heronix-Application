package com.heronix.controller;

import com.heronix.dto.ReportBlockchain;
import com.heronix.service.ReportBlockchainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Blockchain API Controller
 *
 * REST API endpoints for blockchain integration and verification.
 *
 * Endpoints:
 * - POST /api/blockchain - Create blockchain
 * - GET /api/blockchain/{id} - Get blockchain
 * - POST /api/blockchain/{id}/start - Start blockchain
 * - POST /api/blockchain/{id}/stop - Stop blockchain
 * - POST /api/blockchain/{id}/contract - Deploy smart contract
 * - POST /api/blockchain/{id}/hash - Store report hash
 * - POST /api/blockchain/{id}/verify - Verify report hash
 * - POST /api/blockchain/{id}/audit - Create audit entry
 * - POST /api/blockchain/{id}/signature - Add digital signature
 * - PUT /api/blockchain/{id}/metrics - Update metrics
 * - GET /api/blockchain/{id}/transactions/confirmed - Get confirmed transactions
 * - GET /api/blockchain/{id}/verifications/successful - Get successful verifications
 * - GET /api/blockchain/{id}/audit/{reportId} - Get audit trail
 * - DELETE /api/blockchain/{id} - Delete blockchain
 * - GET /api/blockchain/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 99 - Report Blockchain Integration & Verification
 */
@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
@Slf4j
public class ReportBlockchainApiController {

    private final ReportBlockchainService blockchainService;

    /**
     * Create blockchain
     */
    @PostMapping
    public ResponseEntity<ReportBlockchain> createBlockchain(@RequestBody ReportBlockchain blockchain) {
        log.info("POST /api/blockchain - Creating blockchain: {}", blockchain.getChainName());

        try {
            ReportBlockchain created = blockchainService.createBlockchain(blockchain);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating blockchain", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get blockchain
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportBlockchain> getBlockchain(@PathVariable Long id) {
        log.info("GET /api/blockchain/{}", id);

        try {
            return blockchainService.getBlockchain(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching blockchain: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start blockchain
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startBlockchain(@PathVariable Long id) {
        log.info("POST /api/blockchain/{}/start", id);

        try {
            blockchainService.startBlockchain(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Blockchain started");
            response.put("chainId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting blockchain: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop blockchain
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopBlockchain(@PathVariable Long id) {
        log.info("POST /api/blockchain/{}/stop", id);

        try {
            blockchainService.stopBlockchain(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Blockchain stopped");
            response.put("chainId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping blockchain: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy smart contract
     */
    @PostMapping("/{id}/contract")
    public ResponseEntity<ReportBlockchain.SmartContract> deployContract(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/blockchain/{}/contract", id);

        try {
            String contractName = request.get("contractName");
            String sourceCode = request.get("sourceCode");

            ReportBlockchain.SmartContract contract = blockchainService.deployContract(
                    id, contractName, sourceCode
            );

            return ResponseEntity.ok(contract);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying contract on blockchain: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Store report hash
     */
    @PostMapping("/{id}/hash")
    public ResponseEntity<ReportBlockchain.ReportHash> storeReportHash(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/blockchain/{}/hash", id);

        try {
            String reportId = request.get("reportId");
            String reportName = request.get("reportName");
            String hash = request.get("hash");

            ReportBlockchain.ReportHash reportHash = blockchainService.storeReportHash(
                    id, reportId, reportName, hash
            );

            return ResponseEntity.ok(reportHash);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error storing report hash on blockchain: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Verify report hash
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<ReportBlockchain.Verification> verifyReportHash(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/blockchain/{}/verify", id);

        try {
            String reportId = request.get("reportId");
            String providedHash = request.get("hash");

            ReportBlockchain.Verification verification = blockchainService.verifyReportHash(
                    id, reportId, providedHash
            );

            return ResponseEntity.ok(verification);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error verifying report hash on blockchain: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create audit entry
     */
    @PostMapping("/{id}/audit")
    public ResponseEntity<ReportBlockchain.AuditEntry> createAuditEntry(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/blockchain/{}/audit", id);

        try {
            String reportId = request.get("reportId");
            String action = request.get("action");
            String userId = request.get("userId");
            String description = request.get("description");

            ReportBlockchain.AuditEntry entry = blockchainService.createAuditEntry(
                    id, reportId, action, userId, description
            );

            return ResponseEntity.ok(entry);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating audit entry on blockchain: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add digital signature
     */
    @PostMapping("/{id}/signature")
    public ResponseEntity<ReportBlockchain.DigitalSignature> addDigitalSignature(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/blockchain/{}/signature", id);

        try {
            String reportId = request.get("reportId");
            String signerAddress = request.get("signerAddress");
            String signature = request.get("signature");

            ReportBlockchain.DigitalSignature digitalSignature = blockchainService.addDigitalSignature(
                    id, reportId, signerAddress, signature
            );

            return ResponseEntity.ok(digitalSignature);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding digital signature on blockchain: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/blockchain/{}/metrics", id);

        try {
            blockchainService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("chainId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for blockchain: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get confirmed transactions
     */
    @GetMapping("/{id}/transactions/confirmed")
    public ResponseEntity<Map<String, Object>> getConfirmedTransactions(@PathVariable Long id) {
        log.info("GET /api/blockchain/{}/transactions/confirmed", id);

        try {
            return blockchainService.getBlockchain(id)
                    .map(chain -> {
                        List<ReportBlockchain.Transaction> transactions = chain.getConfirmedTransactions();
                        Map<String, Object> response = new HashMap<>();
                        response.put("chainId", id);
                        response.put("transactions", transactions);
                        response.put("count", transactions.size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching confirmed transactions for blockchain: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get successful verifications
     */
    @GetMapping("/{id}/verifications/successful")
    public ResponseEntity<Map<String, Object>> getSuccessfulVerifications(@PathVariable Long id) {
        log.info("GET /api/blockchain/{}/verifications/successful", id);

        try {
            return blockchainService.getBlockchain(id)
                    .map(chain -> {
                        List<ReportBlockchain.Verification> verifications = chain.getSuccessfulVerifications();
                        Map<String, Object> response = new HashMap<>();
                        response.put("chainId", id);
                        response.put("verifications", verifications);
                        response.put("count", verifications.size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching successful verifications for blockchain: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get audit trail
     */
    @GetMapping("/{id}/audit/{reportId}")
    public ResponseEntity<Map<String, Object>> getAuditTrail(
            @PathVariable Long id,
            @PathVariable String reportId) {
        log.info("GET /api/blockchain/{}/audit/{}", id, reportId);

        try {
            return blockchainService.getBlockchain(id)
                    .map(chain -> {
                        List<ReportBlockchain.AuditEntry> auditTrail = chain.getAuditTrail(reportId);
                        Map<String, Object> response = new HashMap<>();
                        response.put("chainId", id);
                        response.put("reportId", reportId);
                        response.put("auditTrail", auditTrail);
                        response.put("count", auditTrail.size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching audit trail for blockchain: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete blockchain
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBlockchain(@PathVariable Long id) {
        log.info("DELETE /api/blockchain/{}", id);

        try {
            blockchainService.deleteBlockchain(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Blockchain deleted");
            response.put("chainId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting blockchain: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/blockchain/stats");

        try {
            Map<String, Object> stats = blockchainService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching blockchain statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
