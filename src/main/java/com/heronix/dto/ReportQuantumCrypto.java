package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Quantum Cryptography DTO
 *
 * Represents quantum-resistant cryptography, post-quantum algorithms,
 * hybrid cryptographic schemes, and quantum key distribution.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 140 - Quantum-Ready Cryptography & Post-Quantum Security
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportQuantumCrypto {

    private Long cryptoId;
    private String cryptoName;
    private String description;

    // Post-Quantum Algorithms
    private String pqcAlgorithm; // CRYSTALS_KYBER, CRYSTALS_DILITHIUM, SPHINCS_PLUS, FALCON, NTRU
    private String pqcType; // KEY_ENCAPSULATION, DIGITAL_SIGNATURE, HASH_BASED
    private Integer securityLevel; // NIST levels: 1, 3, 5
    private Map<String, Object> algorithmConfig;

    // Hybrid Cryptography
    private Boolean hybridModeEnabled;
    private String classicalAlgorithm; // RSA, ECC, AES
    private String quantumResistantAlgorithm;
    private String hybridScheme; // CONCATENATE, XOR, NESTED
    private Map<String, Object> hybridConfig;

    // Key Management
    private String keyType; // SYMMETRIC, ASYMMETRIC, HYBRID
    private Integer keySize;
    private String keyFormat;
    private Boolean quantumKeyDistribution; // QKD
    private String qkdProtocol; // BB84, E91, B92
    private Map<String, Object> keyManagementConfig;

    // Key Generation
    private String keyGenerationMethod;
    private Boolean trulyRandomSource; // QRNG - Quantum Random Number Generator
    private String entropySource;
    private Integer keyGenerationTimeMs;
    private Map<String, Object> keyGenConfig;

    // Key Exchange
    private String keyExchangeProtocol; // PQ_KEM, HYBRID_KEM
    private Boolean forwardSecrecy;
    private Boolean postCompromiseSecrecy;
    private Map<String, Object> keyExchangeConfig;

    // Encryption
    private String encryptionMode; // GCM, CCM, CTR, CBC
    private Boolean authenticatedEncryption;
    private Integer nonceSize;
    private Integer tagSize;
    private Map<String, Object> encryptionConfig;

    // Digital Signatures
    private Boolean signatureEnabled;
    private String signatureAlgorithm; // DILITHIUM, FALCON, SPHINCS_PLUS
    private Integer signatureSize;
    private Integer verificationTimeMs;
    private Map<String, Object> signatureConfig;

    // Hash Functions
    private String hashAlgorithm; // SHA3, SHAKE, BLAKE3
    private Integer hashOutputSize;
    private Boolean quantumResistantHash;
    private Map<String, Object> hashConfig;

    // Quantum-Safe TLS
    private Boolean quantumSafeTlsEnabled;
    private String tlsVersion; // TLS_1_3_PQ
    private List<String> supportedCipherSuites;
    private Boolean hybridHandshake;
    private Map<String, Object> tlsConfig;

    // Certificate Management
    private Boolean pqCertificatesEnabled;
    private String certificateType; // X509_PQ, HYBRID_CERT
    private Integer certificateValidityDays;
    private LocalDateTime certificateExpiry;
    private Map<String, Object> certConfig;

    // Quantum Random Number Generation
    private Boolean qrngEnabled;
    private String qrngType; // PHOTONIC, VACUUM_FLUCTUATION, RADIOACTIVE_DECAY
    private Integer randomnessQuality; // Min-entropy bits
    private Long randomBytesGenerated;
    private Map<String, Object> qrngConfig;

    // Migration Strategy
    private String migrationPhase; // ASSESSMENT, PLANNING, HYBRID, FULL_PQ
    private Boolean legacySupport;
    private List<String> legacyAlgorithms;
    private LocalDateTime migrationStartDate;
    private LocalDateTime migrationCompletionDate;
    private Map<String, Object> migrationConfig;

    // Crypto Agility
    private Boolean cryptoAgilityEnabled;
    private List<String> supportedAlgorithms;
    private Boolean algorithmNegotiation;
    private String defaultAlgorithm;
    private Map<String, Object> agilityConfig;

    // Performance Optimization
    private Boolean hardwareAcceleration;
    private String acceleratorType; // FPGA, ASIC, GPU, QUANTUM_PROCESSOR
    private Boolean parallelProcessing;
    private Integer processingThreads;
    private Map<String, Object> performanceConfig;

    // Compliance & Standards
    private List<String> standards; // NIST_PQC, ISO_IEC, ETSI, NSA_CNSA_2_0
    private Boolean nistCompliant;
    private Boolean fipsValidated;
    private LocalDateTime lastComplianceCheck;
    private Map<String, Object> complianceData;

    // Security Assessment
    private String securityStatus; // SECURE, AT_RISK, COMPROMISED, UNKNOWN
    private LocalDateTime lastSecurityAudit;
    private List<String> vulnerabilities;
    private List<String> mitigations;
    private Map<String, Object> securityAssessment;

    // Quantum Threat Modeling
    private Integer quantumBitStrength; // Estimated qubits needed to break
    private Integer classicalBitStrength; // Classical security equivalent
    private Double quantumAdvantage; // Speed-up factor
    private String threatLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private Map<String, Object> threatModel;

    // Side-Channel Protection
    private Boolean sideChannelProtection;
    private List<String> protectedChannels; // TIMING, POWER, EM, CACHE
    private Boolean constantTimeImplementation;
    private Boolean maskingEnabled;
    private Map<String, Object> sideChannelConfig;

    // Cryptographic Operations
    private Long totalEncryptions;
    private Long totalDecryptions;
    private Long totalSignatures;
    private Long totalVerifications;
    private Long totalKeyExchanges;
    private Map<String, Long> operationsByType;

    // Performance Metrics
    private Double averageEncryptionTimeMs;
    private Double averageDecryptionTimeMs;
    private Double averageSigningTimeMs;
    private Double averageVerificationTimeMs;
    private Double throughputOpsPerSecond;
    private Map<String, Object> performanceMetrics;

    // Resource Utilization
    private Double cpuUtilization;
    private Double memoryUsageMb;
    private Long bandwidthUsedKb;
    private Integer activeSessions;
    private Map<String, Object> resourceMetrics;

    // Error Tracking
    private Long encryptionErrors;
    private Long decryptionErrors;
    private Long signatureErrors;
    private Long verificationFailures;
    private Map<String, Long> errorsByType;

    // Integration
    private List<String> integratedSystems;
    private Boolean apiIntegration;
    private Boolean libraryIntegration;
    private String sdkVersion;
    private Map<String, Object> integrationConfig;

    // Status
    private String cryptoStatus; // INITIALIZING, ACTIVE, UPDATING, DEGRADED, DISABLED
    private LocalDateTime activatedAt;
    private LocalDateTime lastOperationAt;
    private LocalDateTime lastKeyRotation;

    // Metadata
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;

    // Helper Methods
    public void incrementOperation(String operationType, boolean successful) {
        switch (operationType.toUpperCase()) {
            case "ENCRYPTION":
                this.totalEncryptions = (this.totalEncryptions != null ? this.totalEncryptions : 0L) + 1;
                if (!successful) {
                    this.encryptionErrors = (this.encryptionErrors != null ? this.encryptionErrors : 0L) + 1;
                }
                break;
            case "DECRYPTION":
                this.totalDecryptions = (this.totalDecryptions != null ? this.totalDecryptions : 0L) + 1;
                if (!successful) {
                    this.decryptionErrors = (this.decryptionErrors != null ? this.decryptionErrors : 0L) + 1;
                }
                break;
            case "SIGNATURE":
                this.totalSignatures = (this.totalSignatures != null ? this.totalSignatures : 0L) + 1;
                if (!successful) {
                    this.signatureErrors = (this.signatureErrors != null ? this.signatureErrors : 0L) + 1;
                }
                break;
            case "VERIFICATION":
                this.totalVerifications = (this.totalVerifications != null ? this.totalVerifications : 0L) + 1;
                if (!successful) {
                    this.verificationFailures = (this.verificationFailures != null ? this.verificationFailures : 0L) + 1;
                }
                break;
            case "KEY_EXCHANGE":
                this.totalKeyExchanges = (this.totalKeyExchanges != null ? this.totalKeyExchanges : 0L) + 1;
                break;
        }

        if (this.operationsByType == null) {
            this.operationsByType = new java.util.HashMap<>();
        }
        this.operationsByType.merge(operationType, 1L, Long::sum);
    }

    public boolean isActive() {
        return "ACTIVE".equals(cryptoStatus);
    }

    public boolean isQuantumResistant() {
        return pqcAlgorithm != null || Boolean.TRUE.equals(hybridModeEnabled);
    }

    public boolean isNistCompliant() {
        return Boolean.TRUE.equals(nistCompliant);
    }

    public boolean requiresKeyRotation(int maxDaysSinceRotation) {
        if (lastKeyRotation == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(lastKeyRotation.plusDays(maxDaysSinceRotation));
    }

    public boolean isCertificateExpiring(int daysThreshold) {
        if (certificateExpiry == null) {
            return false;
        }
        return LocalDateTime.now().plusDays(daysThreshold).isAfter(certificateExpiry);
    }

    public double getErrorRate() {
        long totalOps = (totalEncryptions != null ? totalEncryptions : 0L) +
                        (totalDecryptions != null ? totalDecryptions : 0L) +
                        (totalSignatures != null ? totalSignatures : 0L) +
                        (totalVerifications != null ? totalVerifications : 0L);

        if (totalOps == 0) {
            return 0.0;
        }

        long totalErrors = (encryptionErrors != null ? encryptionErrors : 0L) +
                           (decryptionErrors != null ? decryptionErrors : 0L) +
                           (signatureErrors != null ? signatureErrors : 0L) +
                           (verificationFailures != null ? verificationFailures : 0L);

        return (totalErrors * 100.0) / totalOps;
    }

    public boolean isHighSecurity() {
        return securityLevel != null && securityLevel >= 3;
    }

    public boolean needsUpgrade() {
        return !isQuantumResistant() || "AT_RISK".equals(securityStatus);
    }
}
