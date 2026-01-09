package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Quantum Computing DTO
 *
 * Manages quantum computing and quantum-resistant cryptography for reports.
 *
 * Features:
 * - Quantum computing integration
 * - Quantum-resistant encryption
 * - Post-quantum cryptography (PQC)
 * - Quantum key distribution (QKD)
 * - Quantum optimization algorithms
 * - Quantum machine learning
 * - Hybrid classical-quantum processing
 * - Quantum circuit simulation
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 101 - Report Quantum Computing & Advanced Cryptography
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportQuantum {

    // System Information
    private Long quantumSystemId;
    private String systemName;
    private String description;
    private QuantumSystemStatus status;
    private Boolean isActive;

    // Quantum Computing Platform
    private QuantumPlatform platform;
    private String platformVersion;
    private Integer totalQubits;
    private Integer availableQubits;
    private Double quantumVolume;
    private Double gateErrorRate;
    private Double readoutErrorRate;
    private Integer coherenceTimeMs;

    // Quantum Circuits
    private List<QuantumCircuit> circuits;
    private Map<String, QuantumCircuit> circuitRegistry;
    private Integer totalCircuits;
    private Integer executedCircuits;

    // Quantum Jobs
    private List<QuantumJob> jobs;
    private Map<String, QuantumJob> jobRegistry;
    private Long totalJobs;
    private Long completedJobs;
    private Long failedJobs;
    private Integer activeJobs;

    // Cryptography
    private List<QuantumCrypto> cryptoSystems;
    private Map<String, QuantumCrypto> cryptoRegistry;
    private CryptographyAlgorithm defaultAlgorithm;
    private Integer keySize;
    private Boolean quantumResistant;

    // Quantum Keys
    private List<QuantumKey> quantumKeys;
    private Map<String, QuantumKey> keyRegistry;
    private Long totalKeysGenerated;
    private Long activeKeys;

    // Encrypted Reports
    private List<EncryptedReport> encryptedReports;
    private Map<String, EncryptedReport> encryptedReportRegistry;
    private Long totalEncryptedReports;

    // Quantum Algorithms
    private List<QuantumAlgorithm> algorithms;
    private Map<String, QuantumAlgorithm> algorithmRegistry;
    private AlgorithmType defaultAlgorithmType;

    // Optimization Results
    private List<OptimizationResult> optimizationResults;
    private Map<String, OptimizationResult> optimizationRegistry;
    private Long totalOptimizations;
    private Long successfulOptimizations;

    // Metrics
    private QuantumMetrics metrics;
    private LocalDateTime lastMetricsUpdate;

    // Events
    private List<QuantumEvent> events;
    private LocalDateTime lastEventAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    private LocalDateTime lastExecutionAt;

    /**
     * Quantum System Status
     */
    public enum QuantumSystemStatus {
        INITIALIZING,
        CALIBRATING,
        READY,
        RUNNING,
        ERROR,
        MAINTENANCE
    }

    /**
     * Quantum Platform
     */
    public enum QuantumPlatform {
        IBM_QUANTUM,
        AWS_BRAKET,
        AZURE_QUANTUM,
        GOOGLE_QUANTUM,
        RIGETTI,
        IONQ,
        DWAVE,
        SIMULATOR
    }

    /**
     * Cryptography Algorithm
     */
    public enum CryptographyAlgorithm {
        CRYSTALS_KYBER,      // NIST PQC - Key Encapsulation
        CRYSTALS_DILITHIUM,  // NIST PQC - Digital Signatures
        FALCON,              // NIST PQC - Digital Signatures
        SPHINCS_PLUS,        // NIST PQC - Digital Signatures
        NTRU,                // Lattice-based
        SABER,               // Lattice-based
        FRODO_KEM,           // Lattice-based
        CLASSIC_MCELIECE,    // Code-based
        RSA_4096,            // Classical (transitional)
        AES_256_GCM          // Classical (symmetric)
    }

    /**
     * Algorithm Type
     */
    public enum AlgorithmType {
        GROVERS,             // Search
        SHORS,               // Factorization
        VQE,                 // Variational Quantum Eigensolver
        QAOA,                // Quantum Approximate Optimization
        QML,                 // Quantum Machine Learning
        QSVM,                // Quantum Support Vector Machine
        QUANTUM_ANNEALING,   // Optimization
        QUANTUM_WALK         // Graph algorithms
    }

    /**
     * Circuit Status
     */
    public enum CircuitStatus {
        DESIGNING,
        COMPILED,
        OPTIMIZED,
        READY,
        EXECUTING,
        COMPLETED,
        FAILED
    }

    /**
     * Job Status
     */
    public enum JobStatus {
        QUEUED,
        VALIDATING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    /**
     * Key Status
     */
    public enum KeyStatus {
        GENERATING,
        ACTIVE,
        EXPIRED,
        REVOKED,
        COMPROMISED
    }

    /**
     * Quantum Circuit
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantumCircuit {
        private String circuitId;
        private String circuitName;
        private CircuitStatus status;
        private Integer numQubits;
        private Integer numGates;
        private Integer circuitDepth;
        private List<QuantumGate> gates;
        private Map<String, Object> parameters;
        private String qiskitCode;
        private String qsharpCode;
        private Boolean optimized;
        private LocalDateTime createdAt;
        private LocalDateTime compiledAt;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Quantum Gate
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantumGate {
        private String gateType;
        private List<Integer> qubits;
        private Double rotationAngle;
        private Map<String, Object> parameters;
    }

    /**
     * Quantum Job
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantumJob {
        private String jobId;
        private String jobName;
        private JobStatus status;
        private String circuitId;
        private Integer shots;
        private Integer actualShots;
        private Map<String, Integer> results;
        private Map<String, Double> probabilities;
        private Double fidelity;
        private LocalDateTime queuedAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long executionTimeMs;
        private String errorMessage;
        private Map<String, Object> metadata;
    }

    /**
     * Quantum Cryptography System
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantumCrypto {
        private String cryptoId;
        private String cryptoName;
        private CryptographyAlgorithm algorithm;
        private Integer keySize;
        private Boolean quantumResistant;
        private Integer securityLevel;
        private String publicKey;
        private Boolean publicKeyGenerated;
        private Long totalEncryptions;
        private Long totalDecryptions;
        private Long failedOperations;
        private LocalDateTime createdAt;
        private LocalDateTime lastUsedAt;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Quantum Key
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantumKey {
        private String keyId;
        private String keyName;
        private KeyStatus status;
        private CryptographyAlgorithm algorithm;
        private Integer keySize;
        private String publicKey;
        private String keyFingerprint;
        private Boolean quantumGenerated;
        private Boolean quantumResistant;
        private LocalDateTime generatedAt;
        private LocalDateTime expiresAt;
        private LocalDateTime lastUsedAt;
        private Long usageCount;
        private String ownerId;
        private List<String> authorizedUsers;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Encrypted Report
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EncryptedReport {
        private String encryptedReportId;
        private String reportId;
        private String reportName;
        private CryptographyAlgorithm algorithm;
        private String keyId;
        private String encryptedData;
        private String initializationVector;
        private String authenticationTag;
        private Long originalSize;
        private Long encryptedSize;
        private LocalDateTime encryptedAt;
        private String encryptedBy;
        private Integer accessCount;
        private LocalDateTime lastAccessedAt;
        private Boolean quantumSecure;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Quantum Algorithm
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantumAlgorithm {
        private String algorithmId;
        private String algorithmName;
        private AlgorithmType algorithmType;
        private String circuitId;
        private Map<String, Object> parameters;
        private Integer requiredQubits;
        private Integer estimatedGates;
        private Long totalExecutions;
        private Long successfulExecutions;
        private Double averageSuccessRate;
        private String description;
        private LocalDateTime createdAt;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Optimization Result
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationResult {
        private String resultId;
        private String algorithmId;
        private AlgorithmType algorithmType;
        private Map<String, Object> inputProblem;
        private Map<String, Object> solution;
        private Double energyValue;
        private Double approximationRatio;
        private Integer iterations;
        private Long executionTimeMs;
        private Boolean optimal;
        private LocalDateTime computedAt;
        private Map<String, Object> metadata;
    }

    /**
     * Quantum Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantumMetrics {
        private Integer totalQubits;
        private Integer availableQubits;
        private Double averageGateErrorRate;
        private Double averageReadoutErrorRate;
        private Long totalJobs;
        private Long completedJobs;
        private Long failedJobs;
        private Double jobSuccessRate;
        private Double averageExecutionTimeMs;
        private Long totalEncryptions;
        private Long totalDecryptions;
        private Long totalKeysGenerated;
        private Double averageFidelity;
        private LocalDateTime measuredAt;
    }

    /**
     * Quantum Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantumEvent {
        private String eventId;
        private LocalDateTime timestamp;
        private String eventType;
        private String description;
        private String resourceType;
        private String resourceId;
        private Map<String, Object> details;
    }

    // Helper Methods

    /**
     * Start quantum system
     */
    public void startSystem() {
        this.status = QuantumSystemStatus.CALIBRATING;
        this.isActive = true;
        this.startedAt = LocalDateTime.now();

        recordEvent("SYSTEM_STARTED", "Quantum system started", "SYSTEM",
                quantumSystemId != null ? quantumSystemId.toString() : null);

        this.status = QuantumSystemStatus.READY;
    }

    /**
     * Stop quantum system
     */
    public void stopSystem() {
        this.status = QuantumSystemStatus.MAINTENANCE;
        this.isActive = false;
        this.stoppedAt = LocalDateTime.now();

        recordEvent("SYSTEM_STOPPED", "Quantum system stopped", "SYSTEM",
                quantumSystemId != null ? quantumSystemId.toString() : null);
    }

    /**
     * Register quantum circuit
     */
    public void registerCircuit(QuantumCircuit circuit) {
        if (circuits == null) {
            circuits = new java.util.ArrayList<>();
        }
        circuits.add(circuit);

        if (circuitRegistry == null) {
            circuitRegistry = new java.util.HashMap<>();
        }
        circuitRegistry.put(circuit.getCircuitId(), circuit);

        totalCircuits = (totalCircuits != null ? totalCircuits : 0) + 1;

        recordEvent("CIRCUIT_REGISTERED", "Circuit registered: " + circuit.getCircuitName(),
                "CIRCUIT", circuit.getCircuitId());
    }

    /**
     * Submit quantum job
     */
    public void submitJob(QuantumJob job) {
        if (jobs == null) {
            jobs = new java.util.ArrayList<>();
        }
        jobs.add(job);

        if (jobRegistry == null) {
            jobRegistry = new java.util.HashMap<>();
        }
        jobRegistry.put(job.getJobId(), job);

        totalJobs = (totalJobs != null ? totalJobs : 0L) + 1;
        activeJobs = (activeJobs != null ? activeJobs : 0) + 1;

        recordEvent("JOB_SUBMITTED", "Quantum job submitted: " + job.getJobName(),
                "JOB", job.getJobId());
    }

    /**
     * Complete quantum job
     */
    public void completeJob(String jobId, boolean success) {
        QuantumJob job = jobRegistry != null ? jobRegistry.get(jobId) : null;
        if (job != null) {
            job.setStatus(success ? JobStatus.COMPLETED : JobStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());

            if (success) {
                completedJobs = (completedJobs != null ? completedJobs : 0L) + 1;
            } else {
                failedJobs = (failedJobs != null ? failedJobs : 0L) + 1;
            }

            activeJobs = activeJobs != null && activeJobs > 0 ? activeJobs - 1 : 0;
            lastExecutionAt = LocalDateTime.now();
        }
    }

    /**
     * Register crypto system
     */
    public void registerCrypto(QuantumCrypto crypto) {
        if (cryptoSystems == null) {
            cryptoSystems = new java.util.ArrayList<>();
        }
        cryptoSystems.add(crypto);

        if (cryptoRegistry == null) {
            cryptoRegistry = new java.util.HashMap<>();
        }
        cryptoRegistry.put(crypto.getCryptoId(), crypto);

        recordEvent("CRYPTO_REGISTERED", "Cryptography system registered: " + crypto.getCryptoName(),
                "CRYPTO", crypto.getCryptoId());
    }

    /**
     * Register quantum key
     */
    public void registerKey(QuantumKey key) {
        if (quantumKeys == null) {
            quantumKeys = new java.util.ArrayList<>();
        }
        quantumKeys.add(key);

        if (keyRegistry == null) {
            keyRegistry = new java.util.HashMap<>();
        }
        keyRegistry.put(key.getKeyId(), key);

        totalKeysGenerated = (totalKeysGenerated != null ? totalKeysGenerated : 0L) + 1;
        if (key.getStatus() == KeyStatus.ACTIVE) {
            activeKeys = (activeKeys != null ? activeKeys : 0L) + 1;
        }

        recordEvent("KEY_GENERATED", "Quantum key generated: " + key.getKeyName(),
                "KEY", key.getKeyId());
    }

    /**
     * Store encrypted report
     */
    public void storeEncryptedReport(EncryptedReport report) {
        if (encryptedReports == null) {
            encryptedReports = new java.util.ArrayList<>();
        }
        encryptedReports.add(report);

        if (encryptedReportRegistry == null) {
            encryptedReportRegistry = new java.util.HashMap<>();
        }
        encryptedReportRegistry.put(report.getEncryptedReportId(), report);

        totalEncryptedReports = (totalEncryptedReports != null ? totalEncryptedReports : 0L) + 1;

        recordEvent("REPORT_ENCRYPTED", "Report encrypted: " + report.getReportName(),
                "REPORT", report.getEncryptedReportId());
    }

    /**
     * Register algorithm
     */
    public void registerAlgorithm(QuantumAlgorithm algorithm) {
        if (algorithms == null) {
            algorithms = new java.util.ArrayList<>();
        }
        algorithms.add(algorithm);

        if (algorithmRegistry == null) {
            algorithmRegistry = new java.util.HashMap<>();
        }
        algorithmRegistry.put(algorithm.getAlgorithmId(), algorithm);

        recordEvent("ALGORITHM_REGISTERED", "Quantum algorithm registered: " + algorithm.getAlgorithmName(),
                "ALGORITHM", algorithm.getAlgorithmId());
    }

    /**
     * Store optimization result
     */
    public void storeOptimizationResult(OptimizationResult result) {
        if (optimizationResults == null) {
            optimizationResults = new java.util.ArrayList<>();
        }
        optimizationResults.add(result);

        if (optimizationRegistry == null) {
            optimizationRegistry = new java.util.HashMap<>();
        }
        optimizationRegistry.put(result.getResultId(), result);

        totalOptimizations = (totalOptimizations != null ? totalOptimizations : 0L) + 1;
        if (Boolean.TRUE.equals(result.getOptimal())) {
            successfulOptimizations = (successfulOptimizations != null ? successfulOptimizations : 0L) + 1;
        }
    }

    /**
     * Get circuit by ID
     */
    public QuantumCircuit getCircuit(String circuitId) {
        return circuitRegistry != null ? circuitRegistry.get(circuitId) : null;
    }

    /**
     * Get job by ID
     */
    public QuantumJob getJob(String jobId) {
        return jobRegistry != null ? jobRegistry.get(jobId) : null;
    }

    /**
     * Get crypto by ID
     */
    public QuantumCrypto getCrypto(String cryptoId) {
        return cryptoRegistry != null ? cryptoRegistry.get(cryptoId) : null;
    }

    /**
     * Get key by ID
     */
    public QuantumKey getKey(String keyId) {
        return keyRegistry != null ? keyRegistry.get(keyId) : null;
    }

    /**
     * Record event
     */
    public void recordEvent(String eventType, String description, String resourceType, String resourceId) {
        if (events == null) {
            events = new java.util.ArrayList<>();
        }

        QuantumEvent event = QuantumEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .eventType(eventType)
                .description(description)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(new java.util.HashMap<>())
                .build();

        events.add(event);
        lastEventAt = LocalDateTime.now();
    }

    /**
     * Check if system is healthy
     */
    public boolean isHealthy() {
        return status == QuantumSystemStatus.READY || status == QuantumSystemStatus.RUNNING;
    }

    /**
     * Get active jobs
     */
    public List<QuantumJob> getActiveJobs() {
        if (jobs == null) {
            return new java.util.ArrayList<>();
        }
        return jobs.stream()
                .filter(j -> j.getStatus() == JobStatus.QUEUED ||
                           j.getStatus() == JobStatus.VALIDATING ||
                           j.getStatus() == JobStatus.RUNNING)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get active keys
     */
    public List<QuantumKey> getActiveKeys() {
        if (quantumKeys == null) {
            return new java.util.ArrayList<>();
        }
        return quantumKeys.stream()
                .filter(k -> k.getStatus() == KeyStatus.ACTIVE)
                .collect(java.util.stream.Collectors.toList());
    }
}
