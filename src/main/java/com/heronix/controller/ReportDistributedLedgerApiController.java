package com.heronix.controller;

import com.heronix.dto.ReportDistributedLedger;
import com.heronix.service.ReportDistributedLedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Distributed Ledger API Controller
 *
 * REST API endpoints for distributed ledger management, consensus protocols,
 * immutable records, and cryptographic verification.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 129 - Report Distributed Ledger Technology
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/distributed-ledger")
@RequiredArgsConstructor
@Slf4j
public class ReportDistributedLedgerApiController {

    private final ReportDistributedLedgerService ledgerService;

    /**
     * Create distributed ledger
     * POST /api/distributed-ledger
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createLedger(
            @RequestBody ReportDistributedLedger ledger) {
        try {
            ReportDistributedLedger created = ledgerService.createLedger(ledger);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Distributed ledger created successfully");
            response.put("ledgerId", created.getLedgerId());
            response.put("ledgerName", created.getLedgerName());
            response.put("ledgerType", created.getLedgerType());
            response.put("platform", created.getPlatform());
            response.put("status", created.getStatus());
            response.put("createdAt", created.getCreatedAt());

            log.info("Distributed ledger created via API: {}", created.getLedgerId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error creating distributed ledger: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create distributed ledger: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get distributed ledger
     * GET /api/distributed-ledger/{ledgerId}
     */
    @GetMapping("/{ledgerId}")
    public ResponseEntity<Map<String, Object>> getLedger(@PathVariable Long ledgerId) {
        try {
            return ledgerService.getLedger(ledgerId)
                    .map(ledger -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("ledger", ledger);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "Distributed ledger not found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                    });

        } catch (Exception e) {
            log.error("Error retrieving distributed ledger {}: {}", ledgerId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve distributed ledger: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Deploy distributed ledger
     * POST /api/distributed-ledger/{ledgerId}/deploy
     */
    @PostMapping("/{ledgerId}/deploy")
    public ResponseEntity<Map<String, Object>> deployLedger(@PathVariable Long ledgerId) {
        try {
            ledgerService.deployLedger(ledgerId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Distributed ledger deployed successfully");
            response.put("ledgerId", ledgerId);
            response.put("deployedAt", LocalDateTime.now());

            log.info("Distributed ledger deployed via API: {}", ledgerId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error deploying distributed ledger {}: {}", ledgerId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to deploy distributed ledger: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Add ledger entry
     * POST /api/distributed-ledger/{ledgerId}/entry
     */
    @PostMapping("/{ledgerId}/entry")
    public ResponseEntity<Map<String, Object>> addEntry(
            @PathVariable Long ledgerId,
            @RequestBody Map<String, Object> entryRequest) {
        try {
            String dataHash = (String) entryRequest.get("dataHash");
            String creator = (String) entryRequest.get("creator");
            @SuppressWarnings("unchecked")
            List<String> transactionIds = (List<String>) entryRequest.get("transactionIds");

            ReportDistributedLedger.LedgerEntry entry = ledgerService.addEntry(
                    ledgerId, dataHash, creator, transactionIds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ledger entry added successfully");
            response.put("entryId", entry.getEntryId());
            response.put("entryHash", entry.getEntryHash());
            response.put("entryNumber", entry.getEntryNumber());
            response.put("createdAt", entry.getCreatedAt());

            log.info("Ledger entry added via API: {} (ledger: {})", entry.getEntryId(), ledgerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error adding ledger entry for ledger {}: {}", ledgerId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add ledger entry: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Register participant
     * POST /api/distributed-ledger/{ledgerId}/participant
     */
    @PostMapping("/{ledgerId}/participant")
    public ResponseEntity<Map<String, Object>> registerParticipant(
            @PathVariable Long ledgerId,
            @RequestBody Map<String, Object> participantRequest) {
        try {
            String organizationName = (String) participantRequest.get("organizationName");
            String mspId = (String) participantRequest.get("mspId");
            String role = (String) participantRequest.get("role");
            @SuppressWarnings("unchecked")
            List<String> permissions = (List<String>) participantRequest.get("permissions");

            ReportDistributedLedger.Participant participant = ledgerService.registerParticipant(
                    ledgerId, organizationName, mspId, role, permissions);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Participant registered successfully");
            response.put("participantId", participant.getParticipantId());
            response.put("organizationName", participant.getOrganizationName());
            response.put("mspId", participant.getMspId());
            response.put("role", participant.getRole());
            response.put("joinedAt", participant.getJoinedAt());

            log.info("Participant registered via API: {} (ledger: {})", participant.getParticipantId(), ledgerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error registering participant for ledger {}: {}", ledgerId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to register participant: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create channel
     * POST /api/distributed-ledger/{ledgerId}/channel
     */
    @PostMapping("/{ledgerId}/channel")
    public ResponseEntity<Map<String, Object>> createChannel(
            @PathVariable Long ledgerId,
            @RequestBody Map<String, Object> channelRequest) {
        try {
            String channelName = (String) channelRequest.get("channelName");
            String description = (String) channelRequest.get("description");
            @SuppressWarnings("unchecked")
            List<String> participants = (List<String>) channelRequest.get("participants");

            ReportDistributedLedger.Channel channel = ledgerService.createChannel(
                    ledgerId, channelName, description, participants);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Channel created successfully");
            response.put("channelId", channel.getChannelId());
            response.put("channelName", channel.getChannelName());
            response.put("participantCount", channel.getParticipantCount());
            response.put("createdAt", channel.getCreatedAt());

            log.info("Channel created via API: {} (ledger: {})", channel.getChannelId(), ledgerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error creating channel for ledger {}: {}", ledgerId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create channel: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Deploy chaincode
     * POST /api/distributed-ledger/{ledgerId}/chaincode
     */
    @PostMapping("/{ledgerId}/chaincode")
    public ResponseEntity<Map<String, Object>> deployChaincode(
            @PathVariable Long ledgerId,
            @RequestBody Map<String, Object> chaincodeRequest) {
        try {
            String chaincodeName = (String) chaincodeRequest.get("chaincodeName");
            String version = (String) chaincodeRequest.get("version");
            String language = (String) chaincodeRequest.get("language");
            String deployer = (String) chaincodeRequest.get("deployer");
            @SuppressWarnings("unchecked")
            List<String> channels = (List<String>) chaincodeRequest.get("channels");

            ReportDistributedLedger.Chaincode chaincode = ledgerService.deployChaincode(
                    ledgerId, chaincodeName, version, language, deployer, channels);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Chaincode deployed successfully");
            response.put("chaincodeId", chaincode.getChaincodeId());
            response.put("chaincodeName", chaincode.getChaincodeName());
            response.put("version", chaincode.getVersion());
            response.put("language", chaincode.getLanguage());
            response.put("deployedAt", chaincode.getDeployedAt());

            log.info("Chaincode deployed via API: {} (ledger: {})", chaincode.getChaincodeId(), ledgerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error deploying chaincode for ledger {}: {}", ledgerId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to deploy chaincode: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create asset
     * POST /api/distributed-ledger/{ledgerId}/asset
     */
    @PostMapping("/{ledgerId}/asset")
    public ResponseEntity<Map<String, Object>> createAsset(
            @PathVariable Long ledgerId,
            @RequestBody Map<String, Object> assetRequest) {
        try {
            String assetType = (String) assetRequest.get("assetType");
            String assetName = (String) assetRequest.get("assetName");
            String owner = (String) assetRequest.get("owner");
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) assetRequest.get("properties");

            ReportDistributedLedger.Asset asset = ledgerService.createAsset(
                    ledgerId, assetType, assetName, owner, properties);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Asset created successfully");
            response.put("assetId", asset.getAssetId());
            response.put("assetType", asset.getAssetType());
            response.put("assetName", asset.getAssetName());
            response.put("owner", asset.getOwner());
            response.put("hash", asset.getHash());
            response.put("createdAt", asset.getCreatedAt());

            log.info("Asset created via API: {} (ledger: {})", asset.getAssetId(), ledgerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error creating asset for ledger {}: {}", ledgerId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create asset: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Submit transaction
     * POST /api/distributed-ledger/{ledgerId}/transaction
     */
    @PostMapping("/{ledgerId}/transaction")
    public ResponseEntity<Map<String, Object>> submitTransaction(
            @PathVariable Long ledgerId,
            @RequestBody Map<String, Object> transactionRequest) {
        try {
            String channelId = (String) transactionRequest.get("channelId");
            String chaincodeId = (String) transactionRequest.get("chaincodeId");
            String functionName = (String) transactionRequest.get("functionName");
            @SuppressWarnings("unchecked")
            List<String> arguments = (List<String>) transactionRequest.get("arguments");
            String creator = (String) transactionRequest.get("creator");

            ReportDistributedLedger.LedgerTransaction transaction = ledgerService.submitTransaction(
                    ledgerId, channelId, chaincodeId, functionName, arguments, creator);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Transaction submitted successfully");
            response.put("transactionId", transaction.getTransactionId());
            response.put("channelId", transaction.getChannelId());
            response.put("chaincodeId", transaction.getChaincodeId());
            response.put("functionName", transaction.getFunctionName());
            response.put("status", transaction.getStatus());
            response.put("createdAt", transaction.getCreatedAt());

            log.info("Transaction submitted via API: {} (ledger: {})", transaction.getTransactionId(), ledgerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error submitting transaction for ledger {}: {}", ledgerId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to submit transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endorse transaction
     * POST /api/distributed-ledger/{ledgerId}/transaction/{transactionId}/endorse
     */
    @PostMapping("/{ledgerId}/transaction/{transactionId}/endorse")
    public ResponseEntity<Map<String, Object>> endorseTransaction(
            @PathVariable Long ledgerId,
            @PathVariable String transactionId,
            @RequestBody Map<String, Object> endorsementRequest) {
        try {
            String endorser = (String) endorsementRequest.get("endorser");

            ReportDistributedLedger.Endorsement endorsement = ledgerService.endorseTransaction(
                    ledgerId, transactionId, endorser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Transaction endorsed successfully");
            response.put("endorsementId", endorsement.getEndorsementId());
            response.put("transactionId", endorsement.getTransactionId());
            response.put("endorser", endorsement.getEndorser());
            response.put("signature", endorsement.getSignature());
            response.put("timestamp", endorsement.getTimestamp());

            log.info("Transaction endorsed via API: {} by {} (ledger: {})", transactionId, endorser, ledgerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error endorsing transaction {} for ledger {}: {}", transactionId, ledgerId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to endorse transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Commit transaction
     * POST /api/distributed-ledger/{ledgerId}/transaction/{transactionId}/commit
     */
    @PostMapping("/{ledgerId}/transaction/{transactionId}/commit")
    public ResponseEntity<Map<String, Object>> commitTransaction(
            @PathVariable Long ledgerId,
            @PathVariable String transactionId) {
        try {
            ledgerService.commitTransaction(ledgerId, transactionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Transaction committed successfully");
            response.put("transactionId", transactionId);
            response.put("committedAt", LocalDateTime.now());

            log.info("Transaction committed via API: {} (ledger: {})", transactionId, ledgerId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error committing transaction {} for ledger {}: {}", transactionId, ledgerId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to commit transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Issue certificate
     * POST /api/distributed-ledger/{ledgerId}/certificate
     */
    @PostMapping("/{ledgerId}/certificate")
    public ResponseEntity<Map<String, Object>> issueCertificate(
            @PathVariable Long ledgerId,
            @RequestBody Map<String, Object> certificateRequest) {
        try {
            String subjectName = (String) certificateRequest.get("subjectName");
            String issuer = (String) certificateRequest.get("issuer");
            String certificateType = (String) certificateRequest.get("certificateType");
            @SuppressWarnings("unchecked")
            List<String> permissions = (List<String>) certificateRequest.get("permissions");

            ReportDistributedLedger.Certificate certificate = ledgerService.issueCertificate(
                    ledgerId, subjectName, issuer, certificateType, permissions);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Certificate issued successfully");
            response.put("certificateId", certificate.getCertificateId());
            response.put("subjectName", certificate.getSubjectName());
            response.put("issuer", certificate.getIssuer());
            response.put("serialNumber", certificate.getSerialNumber());
            response.put("issuedAt", certificate.getIssuedAt());
            response.put("expiresAt", certificate.getExpiresAt());

            log.info("Certificate issued via API: {} (ledger: {})", certificate.getCertificateId(), ledgerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error issuing certificate for ledger {}: {}", ledgerId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to issue certificate: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create identity
     * POST /api/distributed-ledger/{ledgerId}/identity
     */
    @PostMapping("/{ledgerId}/identity")
    public ResponseEntity<Map<String, Object>> createIdentity(
            @PathVariable Long ledgerId,
            @RequestBody Map<String, Object> identityRequest) {
        try {
            String userId = (String) identityRequest.get("userId");
            String mspId = (String) identityRequest.get("mspId");
            String certificateId = (String) identityRequest.get("certificateId");
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) identityRequest.get("roles");

            ReportDistributedLedger.Identity identity = ledgerService.createIdentity(
                    ledgerId, userId, mspId, certificateId, roles);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Identity created successfully");
            response.put("identityId", identity.getIdentityId());
            response.put("userId", identity.getUserId());
            response.put("mspId", identity.getMspId());
            response.put("createdAt", identity.getCreatedAt());

            log.info("Identity created via API: {} (ledger: {})", identity.getIdentityId(), ledgerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error creating identity for ledger {}: {}", ledgerId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create identity: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Transfer asset
     * PUT /api/distributed-ledger/{ledgerId}/asset/{assetId}/transfer
     */
    @PutMapping("/{ledgerId}/asset/{assetId}/transfer")
    public ResponseEntity<Map<String, Object>> transferAsset(
            @PathVariable Long ledgerId,
            @PathVariable String assetId,
            @RequestBody Map<String, Object> transferRequest) {
        try {
            String newOwner = (String) transferRequest.get("newOwner");

            ledgerService.transferAsset(ledgerId, assetId, newOwner);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Asset transferred successfully");
            response.put("assetId", assetId);
            response.put("newOwner", newOwner);
            response.put("timestamp", LocalDateTime.now());

            log.info("Asset transferred via API: {} to {} (ledger: {})", assetId, newOwner, ledgerId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error transferring asset {} for ledger {}: {}", assetId, ledgerId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to transfer asset: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete distributed ledger
     * DELETE /api/distributed-ledger/{ledgerId}
     */
    @DeleteMapping("/{ledgerId}")
    public ResponseEntity<Map<String, Object>> deleteLedger(@PathVariable Long ledgerId) {
        try {
            ledgerService.deleteLedger(ledgerId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Distributed ledger deleted successfully");
            response.put("ledgerId", ledgerId);

            log.info("Distributed ledger deleted via API: {}", ledgerId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting distributed ledger {}: {}", ledgerId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete distributed ledger: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get statistics
     * GET /api/distributed-ledger/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = ledgerService.getStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving distributed ledger statistics: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
