package com.heronix.controller;

import com.heronix.dto.ReportBlockchainLedger;
import com.heronix.service.ReportBlockchainLedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Blockchain & Distributed Ledger API Controller
 *
 * REST API endpoints for blockchain networks, smart contracts, transactions, and credentials.
 *
 * Endpoints:
 * - POST /api/blockchain-ledger - Create ledger
 * - GET /api/blockchain-ledger/{id} - Get ledger
 * - POST /api/blockchain-ledger/{id}/deploy - Deploy network
 * - POST /api/blockchain-ledger/{id}/block - Add block
 * - POST /api/blockchain-ledger/{id}/transaction - Add transaction
 * - POST /api/blockchain-ledger/{id}/transaction/confirm - Confirm transaction
 * - POST /api/blockchain-ledger/{id}/contract - Deploy contract
 * - POST /api/blockchain-ledger/{id}/node - Register node
 * - POST /api/blockchain-ledger/{id}/wallet - Create wallet
 * - POST /api/blockchain-ledger/{id}/credential - Issue credential
 * - POST /api/blockchain-ledger/{id}/credential/verify - Verify credential
 * - POST /api/blockchain-ledger/{id}/consensus - Start consensus
 * - POST /api/blockchain-ledger/{id}/consensus/complete - Complete consensus
 * - POST /api/blockchain-ledger/{id}/validation - Record validation
 * - DELETE /api/blockchain-ledger/{id} - Delete ledger
 * - GET /api/blockchain-ledger/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 114 - Report Blockchain & Distributed Ledger
 */
@RestController
@RequestMapping("/api/blockchain-ledger")
@RequiredArgsConstructor
@Slf4j
public class ReportBlockchainLedgerApiController {

    private final ReportBlockchainLedgerService ledgerService;

    /**
     * Create blockchain ledger
     */
    @PostMapping
    public ResponseEntity<ReportBlockchainLedger> createLedger(@RequestBody ReportBlockchainLedger ledger) {
        log.info("POST /api/blockchain-ledger - Creating blockchain ledger: {}", ledger.getLedgerName());

        try {
            ReportBlockchainLedger created = ledgerService.createLedger(ledger);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating blockchain ledger", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get blockchain ledger
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportBlockchainLedger> getLedger(@PathVariable Long id) {
        log.info("GET /api/blockchain-ledger/{}", id);

        try {
            return ledgerService.getLedger(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching blockchain ledger: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy blockchain network
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployNetwork(@PathVariable Long id) {
        log.info("POST /api/blockchain-ledger/{}/deploy", id);

        try {
            ledgerService.deployNetwork(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Blockchain network deployed successfully");
            response.put("ledgerId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain ledger not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying blockchain network: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add block to chain
     */
    @PostMapping("/{id}/block")
    public ResponseEntity<ReportBlockchainLedger.Block> addBlock(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/blockchain-ledger/{}/block", id);

        try {
            String previousHash = (String) request.get("previousHash");
            @SuppressWarnings("unchecked")
            List<String> transactionIds = (List<String>) request.get("transactionIds");
            String minerAddress = (String) request.get("minerAddress");

            ReportBlockchainLedger.Block block = ledgerService.addBlock(
                    id, previousHash, transactionIds, minerAddress
            );

            return ResponseEntity.ok(block);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain ledger not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding block: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add transaction
     */
    @PostMapping("/{id}/transaction")
    public ResponseEntity<ReportBlockchainLedger.Transaction> addTransaction(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/blockchain-ledger/{}/transaction", id);

        try {
            String fromAddress = (String) request.get("fromAddress");
            String toAddress = (String) request.get("toAddress");
            String transactionType = (String) request.get("transactionType");
            Long amount = request.get("amount") != null ?
                    ((Number) request.get("amount")).longValue() : 0L;

            ReportBlockchainLedger.Transaction transaction = ledgerService.addTransaction(
                    id, fromAddress, toAddress, transactionType, amount
            );

            return ResponseEntity.ok(transaction);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain ledger not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding transaction: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Confirm transaction
     */
    @PostMapping("/{id}/transaction/confirm")
    public ResponseEntity<Map<String, Object>> confirmTransaction(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/blockchain-ledger/{}/transaction/confirm", id);

        try {
            String transactionId = request.get("transactionId");
            ledgerService.confirmTransaction(id, transactionId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Transaction confirmed");
            response.put("transactionId", transactionId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain ledger not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error confirming transaction: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deploy smart contract
     */
    @PostMapping("/{id}/contract")
    public ResponseEntity<ReportBlockchainLedger.SmartContract> deployContract(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/blockchain-ledger/{}/contract", id);

        try {
            String contractName = request.get("contractName");
            String contractType = request.get("contractType");
            String sourceCode = request.get("sourceCode");
            String creator = request.get("creator");

            ReportBlockchainLedger.SmartContract contract = ledgerService.deployContract(
                    id, contractName, contractType, sourceCode, creator
            );

            return ResponseEntity.ok(contract);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain ledger not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying smart contract: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register node
     */
    @PostMapping("/{id}/node")
    public ResponseEntity<ReportBlockchainLedger.Node> registerNode(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/blockchain-ledger/{}/node", id);

        try {
            String nodeAddress = (String) request.get("nodeAddress");
            String nodeTypeStr = (String) request.get("nodeType");
            String ipAddress = (String) request.get("ipAddress");
            Integer port = request.get("port") != null ?
                    ((Number) request.get("port")).intValue() : 8080;

            ReportBlockchainLedger.NodeType nodeType =
                    ReportBlockchainLedger.NodeType.valueOf(nodeTypeStr);

            ReportBlockchainLedger.Node node = ledgerService.registerNode(
                    id, nodeAddress, nodeType, ipAddress, port
            );

            return ResponseEntity.ok(node);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain ledger not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering node: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create wallet
     */
    @PostMapping("/{id}/wallet")
    public ResponseEntity<ReportBlockchainLedger.Wallet> createWallet(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/blockchain-ledger/{}/wallet", id);

        try {
            String ownerId = request.get("ownerId");
            String ownerName = request.get("ownerName");
            String walletType = request.get("walletType");

            ReportBlockchainLedger.Wallet wallet = ledgerService.createWallet(
                    id, ownerId, ownerName, walletType
            );

            return ResponseEntity.ok(wallet);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain ledger not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating wallet: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Issue credential
     */
    @PostMapping("/{id}/credential")
    public ResponseEntity<ReportBlockchainLedger.Credential> issueCredential(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/blockchain-ledger/{}/credential", id);

        try {
            String credentialTypeStr = (String) request.get("credentialType");
            String studentId = (String) request.get("studentId");
            String studentName = (String) request.get("studentName");
            String issuer = (String) request.get("issuer");
            String title = (String) request.get("title");
            @SuppressWarnings("unchecked")
            Map<String, Object> credentialData = (Map<String, Object>) request.get("credentialData");

            ReportBlockchainLedger.CredentialType credentialType =
                    ReportBlockchainLedger.CredentialType.valueOf(credentialTypeStr);

            ReportBlockchainLedger.Credential credential = ledgerService.issueCredential(
                    id, credentialType, studentId, studentName, issuer, title,
                    credentialData != null ? credentialData : new HashMap<>()
            );

            return ResponseEntity.ok(credential);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain ledger not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error issuing credential: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Verify credential
     */
    @PostMapping("/{id}/credential/verify")
    public ResponseEntity<Map<String, Object>> verifyCredential(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/blockchain-ledger/{}/credential/verify", id);

        try {
            String credentialId = request.get("credentialId");
            ledgerService.verifyCredential(id, credentialId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Credential verified");
            response.put("credentialId", credentialId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain ledger not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error verifying credential: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start consensus round
     */
    @PostMapping("/{id}/consensus")
    public ResponseEntity<ReportBlockchainLedger.ConsensusRound> startConsensusRound(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/blockchain-ledger/{}/consensus", id);

        try {
            String blockHash = (String) request.get("blockHash");
            String algorithmStr = (String) request.get("algorithm");
            @SuppressWarnings("unchecked")
            List<String> participatingNodes = (List<String>) request.get("participatingNodes");

            ReportBlockchainLedger.ConsensusAlgorithm algorithm =
                    ReportBlockchainLedger.ConsensusAlgorithm.valueOf(algorithmStr);

            ReportBlockchainLedger.ConsensusRound round = ledgerService.startConsensusRound(
                    id, blockHash, algorithm, participatingNodes
            );

            return ResponseEntity.ok(round);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain ledger not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting consensus round: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete consensus round
     */
    @PostMapping("/{id}/consensus/complete")
    public ResponseEntity<Map<String, Object>> completeConsensusRound(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/blockchain-ledger/{}/consensus/complete", id);

        try {
            String roundId = (String) request.get("roundId");
            Boolean reached = (Boolean) request.get("consensusReached");

            ledgerService.completeConsensusRound(id, roundId, reached != null && reached);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Consensus round completed");
            response.put("roundId", roundId);
            response.put("consensusReached", reached);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain ledger not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing consensus round: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record validation
     */
    @PostMapping("/{id}/validation")
    public ResponseEntity<ReportBlockchainLedger.ValidationEvent> recordValidation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/blockchain-ledger/{}/validation", id);

        try {
            String validationType = (String) request.get("validationType");
            String targetId = (String) request.get("targetId");
            String targetType = (String) request.get("targetType");
            String validatorId = (String) request.get("validatorId");
            Boolean isValid = (Boolean) request.get("isValid");

            ReportBlockchainLedger.ValidationEvent validation = ledgerService.recordValidation(
                    id, validationType, targetId, targetType, validatorId, isValid != null && isValid
            );

            return ResponseEntity.ok(validation);

        } catch (IllegalArgumentException e) {
            log.error("Blockchain ledger not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording validation: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete ledger
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLedger(@PathVariable Long id) {
        log.info("DELETE /api/blockchain-ledger/{}", id);

        try {
            ledgerService.deleteLedger(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Blockchain ledger deleted");
            response.put("ledgerId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting blockchain ledger: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/blockchain-ledger/stats");

        try {
            Map<String, Object> stats = ledgerService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching blockchain statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
