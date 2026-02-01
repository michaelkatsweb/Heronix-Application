package com.heronix.service;

import com.heronix.model.domain.Student;
import com.heronix.model.domain.StudentToken;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.StudentTokenRepository;
import com.heronix.security.SecurityContext;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Student Tokenization Service
 *
 * Generates and manages anonymized tokens for student data that can be safely
 * shared with external systems (parent portal, third-party services) without
 * exposing actual student PII.
 *
 * SECURITY ARCHITECTURE:
 * ----------------------
 * The SIS (Server 1) remains airgapped with NO external connections.
 * External services connect to Server 2/3 which only have tokenized data.
 * Even if external systems are compromised, no real student data is exposed.
 *
 * TOKEN FORMAT: STU-[6-char-hex] (e.g., STU-7A3F2E)
 * ALGORITHM: SHA-256(student_id + salt + timestamp + school_year)
 * COLLISION RESISTANCE: 2^24 possible tokens (16.7M unique tokens)
 * ROTATION: Annual automatic rotation + on-demand rotation capability
 *
 * DATA FLOW:
 * Server 1 (SIS/Airgapped) → Server 3 (DMZ/Sync) → Server 2 (External Portal)
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 20, 2026
 */
@Slf4j
@Service
public class StudentTokenizationService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentTokenRepository tokenRepository;

    @Autowired
    private EntityManager entityManager;

    // Secure random for salt generation
    private final SecureRandom secureRandom = new SecureRandom();

    // Master salt - loaded from secure storage at startup
    private volatile byte[] masterSaltBytes;

    // Encrypted master salt cache (for persistence)
    private volatile byte[] encryptedMasterSalt;

    // Key Encryption Key (KEK) - derived from system properties or HSM
    private volatile SecretKey keyEncryptionKey;

    // Token prefix
    private static final String TOKEN_PREFIX = "STU-";

    // AES-GCM encryption parameters
    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int SALT_LENGTH_BYTES = 32;

    // Secure storage paths
    @Value("${heronix.tokenization.secure-storage-path:${user.home}/.heronix/secure}")
    private String secureStoragePath;

    @Value("${heronix.tokenization.use-hsm:false}")
    private boolean useHsm;

    @Value("${heronix.tokenization.hsm-provider:SunPKCS11}")
    private String hsmProvider;

    @Value("${heronix.tokenization.hsm-config-path:}")
    private String hsmConfigPath;

    // Fallback master salt for development only (DO NOT USE IN PRODUCTION)
    @Value("${heronix.tokenization.master-salt:}")
    private String fallbackMasterSalt;

    // Token hex length (6 characters = 24 bits = 16.7M combinations)
    private static final int TOKEN_HEX_LENGTH = 6;

    // ========================================================================
    // INITIALIZATION - SECURE KEY MANAGEMENT
    // ========================================================================

    /**
     * Initialize secure key management on service startup.
     *
     * This method implements a layered security approach:
     * 1. HSM/TPM Integration (Production): Keys stored in hardware security module
     * 2. Software Key Store (Development): Keys encrypted with KEK and stored on disk
     * 3. Fallback (Testing Only): Environment variable or config-based salt
     *
     * SECURITY NOTE: In production, always use HSM/TPM for master salt storage.
     * The software-based encryption is only for development environments.
     */
    @PostConstruct
    public void initializeSecureKeyManagement() {
        log.info("TOKENIZATION: Initializing secure key management system");

        try {
            if (useHsm) {
                // Production mode: Load keys from HSM/TPM
                initializeFromHsm();
            } else {
                // Development mode: Use software-based key management
                initializeFromSecureStorage();
            }

            log.info("TOKENIZATION: Secure key management initialized successfully. HSM Mode: {}", useHsm);

        } catch (Exception e) {
            log.error("TOKENIZATION: Failed to initialize secure key management", e);

            // Fall back to config-based salt for development only
            if (fallbackMasterSalt != null && !fallbackMasterSalt.isEmpty()) {
                log.warn("TOKENIZATION: Using fallback master salt from configuration - NOT SECURE FOR PRODUCTION");
                masterSaltBytes = fallbackMasterSalt.getBytes(StandardCharsets.UTF_8);
            } else {
                throw new SecurityException("Failed to initialize tokenization security. " +
                        "Configure HSM or set fallback salt for development.", e);
            }
        }
    }

    /**
     * Initialize key management from HSM/TPM.
     *
     * This method connects to a PKCS#11 compatible HSM or uses the platform's TPM.
     * Keys are stored in tamper-resistant hardware and never exported.
     */
    private void initializeFromHsm() throws Exception {
        log.info("TOKENIZATION: Initializing from HSM/TPM. Provider: {}", hsmProvider);

        // Check for HSM provider configuration
        if (hsmConfigPath == null || hsmConfigPath.isEmpty()) {
            throw new SecurityException("HSM configuration path not specified. " +
                    "Set heronix.tokenization.hsm-config-path property.");
        }

        // Load PKCS#11 provider for HSM access
        // NOTE: In production, this would be configured with the actual HSM vendor's driver
        Provider hsmProviderInstance = Security.getProvider(hsmProvider);

        if (hsmProviderInstance == null) {
            // Try to load PKCS#11 provider dynamically
            log.info("TOKENIZATION: Loading PKCS#11 provider from config: {}", hsmConfigPath);

            // For SunPKCS11, configuration is provided via config file
            // Example config file content:
            // name = HeronixHSM
            // library = /path/to/pkcs11/library.so
            // slot = 0

            try {
                hsmProviderInstance = Security.getProvider("SunPKCS11");
                if (hsmProviderInstance == null) {
                    // PKCS#11 provider not available - fall back to TPM
                    log.warn("TOKENIZATION: SunPKCS11 not available, attempting TPM access");
                    initializeFromTpm();
                    return;
                }
            } catch (Exception e) {
                log.warn("TOKENIZATION: Failed to load PKCS#11 provider, attempting TPM", e);
                initializeFromTpm();
                return;
            }
        }

        // Access key from HSM
        try {
            KeyStore hsmKeyStore = KeyStore.getInstance("PKCS11", hsmProviderInstance);
            hsmKeyStore.load(null, getHsmPin());

            // Look for existing master salt key
            String keyAlias = "heronix-tokenization-master-salt";

            if (hsmKeyStore.containsAlias(keyAlias)) {
                // Load existing key
                Key key = hsmKeyStore.getKey(keyAlias, getHsmPin());
                if (key instanceof SecretKey) {
                    keyEncryptionKey = (SecretKey) key;
                    log.info("TOKENIZATION: Loaded existing master salt key from HSM");
                }
            } else {
                // Generate new key in HSM
                KeyGenerator keyGen = KeyGenerator.getInstance("AES", hsmProviderInstance);
                keyGen.init(256, secureRandom);
                keyEncryptionKey = keyGen.generateKey();

                // Store in HSM
                hsmKeyStore.setKeyEntry(keyAlias, keyEncryptionKey, getHsmPin(), null);
                log.warn("TOKENIZATION: Generated new master salt key in HSM");
            }

            // Generate or load master salt encrypted with HSM key
            loadOrGenerateMasterSalt();

        } catch (Exception e) {
            log.error("TOKENIZATION: HSM key access failed", e);
            throw new SecurityException("Failed to access HSM keys", e);
        }
    }

    /**
     * Initialize from TPM (Trusted Platform Module).
     *
     * Uses Windows TPM or Linux TPM2 for secure key storage.
     */
    private void initializeFromTpm() throws Exception {
        log.info("TOKENIZATION: Attempting TPM-based key management");

        // TPM access varies by platform
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            // Windows: Use Windows-MY key store which can be backed by TPM
            initializeFromWindowsKeyStore();
        } else if (osName.contains("linux")) {
            // Linux: Use TPM2 tools or PKCS#11 interface
            // This would require tpm2-pkcs11 library
            log.warn("TOKENIZATION: Linux TPM integration requires tpm2-pkcs11. Using software fallback.");
            initializeFromSecureStorage();
        } else {
            log.warn("TOKENIZATION: TPM not available on {}. Using software fallback.", osName);
            initializeFromSecureStorage();
        }
    }

    /**
     * Initialize from Windows key store (potentially TPM-backed).
     */
    private void initializeFromWindowsKeyStore() throws Exception {
        log.info("TOKENIZATION: Using Windows key store for key management");

        // Windows-MY key store may be TPM-backed depending on system configuration
        // This provides OS-level key protection even without dedicated HSM

        try {
            // For development, we'll use software-based approach since
            // Windows-MY doesn't easily support symmetric keys
            initializeFromSecureStorage();

        } catch (Exception e) {
            log.error("TOKENIZATION: Windows key store access failed", e);
            throw e;
        }
    }

    /**
     * Initialize from secure file-based storage (development mode).
     *
     * Keys are encrypted with a Key Encryption Key (KEK) derived from
     * system properties and stored in a protected directory.
     */
    private void initializeFromSecureStorage() throws Exception {
        log.info("TOKENIZATION: Using secure file storage for key management");

        // Create secure storage directory if needed
        Path storagePath = Paths.get(secureStoragePath);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
            // Set restrictive permissions (owner only)
            try {
                storagePath.toFile().setReadable(false, false);
                storagePath.toFile().setReadable(true, true);
                storagePath.toFile().setWritable(false, false);
                storagePath.toFile().setWritable(true, true);
                storagePath.toFile().setExecutable(false, false);
            } catch (Exception e) {
                log.warn("TOKENIZATION: Could not set file permissions on {}", storagePath);
            }
        }

        // Derive Key Encryption Key from system properties
        deriveKeyEncryptionKey();

        // Load or generate master salt
        loadOrGenerateMasterSalt();
    }

    /**
     * Derive the Key Encryption Key (KEK) from system properties.
     *
     * In production, this should be derived from hardware-bound properties
     * or stored in HSM. This implementation uses system properties as a
     * fallback for development environments.
     */
    private void deriveKeyEncryptionKey() throws Exception {
        // Combine system properties to create a machine-specific key derivation input
        String systemId = System.getProperty("user.name", "unknown") +
                "|" + System.getProperty("os.name", "unknown") +
                "|" + System.getProperty("os.arch", "unknown") +
                "|" + getMachineId();

        // Derive KEK using PBKDF2
        byte[] salt = "HeronixTokenizationKEK".getBytes(StandardCharsets.UTF_8);
        int iterations = 100000;

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] input = systemId.getBytes(StandardCharsets.UTF_8);

        // Simple key derivation (in production, use proper PBKDF2)
        for (int i = 0; i < iterations; i++) {
            sha256.update(salt);
            sha256.update(input);
            sha256.update(intToBytes(i));
            input = sha256.digest();
            sha256.reset();
        }

        keyEncryptionKey = new SecretKeySpec(input, "AES");
        log.debug("TOKENIZATION: Derived KEK from system properties");
    }

    /**
     * Get a machine-specific identifier for key derivation.
     */
    private String getMachineId() {
        try {
            // Try to get machine ID from system properties
            String computerName = System.getenv("COMPUTERNAME");
            if (computerName == null) {
                computerName = System.getenv("HOSTNAME");
            }
            if (computerName == null) {
                computerName = java.net.InetAddress.getLocalHost().getHostName();
            }
            return computerName != null ? computerName : "unknown-machine";
        } catch (Exception e) {
            return "fallback-machine-id";
        }
    }

    private byte[] intToBytes(int value) {
        return new byte[] {
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }

    /**
     * Load existing master salt or generate a new one.
     */
    private void loadOrGenerateMasterSalt() throws Exception {
        Path saltFile = Paths.get(secureStoragePath, "master-salt.enc");

        if (Files.exists(saltFile)) {
            // Load and decrypt existing salt
            byte[] encryptedData = Files.readAllBytes(saltFile);
            masterSaltBytes = decryptMasterSalt(encryptedData);
            log.info("TOKENIZATION: Loaded existing master salt from secure storage");
        } else {
            // Generate new master salt
            masterSaltBytes = new byte[SALT_LENGTH_BYTES];
            secureRandom.nextBytes(masterSaltBytes);

            // Encrypt and store
            byte[] encryptedData = encryptMasterSalt(masterSaltBytes);
            Files.write(saltFile, encryptedData);

            // Set restrictive permissions
            saltFile.toFile().setReadable(false, false);
            saltFile.toFile().setReadable(true, true);
            saltFile.toFile().setWritable(false, false);
            saltFile.toFile().setWritable(true, true);

            log.warn("TOKENIZATION: Generated new master salt and stored in secure storage");
        }

        encryptedMasterSalt = encryptMasterSalt(masterSaltBytes);
    }

    /**
     * Encrypt master salt with KEK using AES-GCM.
     */
    private byte[] encryptMasterSalt(byte[] plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keyEncryptionKey, gcmSpec);

        byte[] ciphertext = cipher.doFinal(plaintext);

        // Prepend IV to ciphertext
        byte[] result = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);

        return result;
    }

    /**
     * Decrypt master salt using KEK.
     */
    private byte[] decryptMasterSalt(byte[] encryptedData) throws Exception {
        // Extract IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);

        // Extract ciphertext
        byte[] ciphertext = new byte[encryptedData.length - GCM_IV_LENGTH];
        System.arraycopy(encryptedData, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, keyEncryptionKey, gcmSpec);

        return cipher.doFinal(ciphertext);
    }

    /**
     * Get HSM PIN (in production, this would be from secure input).
     */
    private char[] getHsmPin() {
        // In production, PIN should come from:
        // 1. Secure environment variable
        // 2. Encrypted configuration
        // 3. Interactive prompt during startup
        String pin = System.getenv("HERONIX_HSM_PIN");
        if (pin != null) {
            return pin.toCharArray();
        }
        // Default PIN for development only
        return "development-pin".toCharArray();
    }

    /**
     * Get master salt as string for hashing operations.
     * This method provides the internal master salt for token generation.
     */
    private String getMasterSalt() {
        if (masterSaltBytes == null) {
            throw new SecurityException("Master salt not initialized. Call initializeSecureKeyManagement first.");
        }
        // Convert to hex string for hash input
        return bytesToHex(masterSaltBytes);
    }

    /**
     * Convert byte array to hex string.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Rotate master salt (security operation - requires re-tokenization).
     *
     * CAUTION: This will invalidate all existing tokens!
     * Should only be called in case of suspected compromise.
     *
     * @param authorizedBy User authorizing the rotation
     * @param reason Reason for rotation
     */
    @Transactional
    public void rotateMasterSalt(String authorizedBy, String reason) {
        log.warn("SECURITY: Master salt rotation initiated by {} - Reason: {}", authorizedBy, reason);

        try {
            // Generate new master salt
            byte[] newSalt = new byte[SALT_LENGTH_BYTES];
            secureRandom.nextBytes(newSalt);

            // Store old salt for audit (encrypted)
            Path auditFile = Paths.get(secureStoragePath,
                    "master-salt-rotated-" + System.currentTimeMillis() + ".enc");
            Files.write(auditFile, encryptMasterSalt(masterSaltBytes));

            // Update current salt
            masterSaltBytes = newSalt;
            encryptedMasterSalt = encryptMasterSalt(newSalt);

            // Store new salt
            Path saltFile = Paths.get(secureStoragePath, "master-salt.enc");
            Files.write(saltFile, encryptedMasterSalt);

            log.warn("SECURITY: Master salt rotated successfully. All tokens must be regenerated.");

        } catch (Exception e) {
            log.error("SECURITY: Master salt rotation failed", e);
            throw new SecurityException("Failed to rotate master salt", e);
        }
    }

    // ========================================================================
    // TOKEN GENERATION
    // ========================================================================

    /**
     * Generate a new token for a student.
     *
     * Algorithm: SHA-256(student_id + salt + timestamp + school_year)
     * Output: STU-[6-char-hex]
     *
     * @param studentId The internal student ID
     * @return Generated token with metadata
     */
    @Transactional
    public StudentToken generateToken(Long studentId) {
        log.info("TOKENIZATION: Generating token for student ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Generate unique salt for this token
        String tokenSalt = generateSecureSalt();

        // Current school year (e.g., "2025-2026")
        String schoolYear = getCurrentSchoolYear();

        // Generate timestamp
        LocalDateTime createdAt = LocalDateTime.now();

        // Create hash input using secure master salt
        String hashInput = String.format("%d|%s|%s|%s|%s",
                studentId,
                getMasterSalt(),
                tokenSalt,
                createdAt.toString(),
                schoolYear);

        // Generate SHA-256 hash
        String fullHash = sha256Hash(hashInput);

        // Extract 6 hex characters (24 bits) for the token
        String tokenValue = TOKEN_PREFIX + fullHash.substring(0, TOKEN_HEX_LENGTH).toUpperCase();

        // Check for collision (extremely rare but must handle)
        if (tokenRepository.existsByTokenValue(tokenValue)) {
            log.warn("TOKENIZATION: Token collision detected, regenerating...");
            // Add extra entropy and retry
            hashInput = hashInput + "|" + secureRandom.nextLong();
            fullHash = sha256Hash(hashInput);
            tokenValue = TOKEN_PREFIX + fullHash.substring(0, TOKEN_HEX_LENGTH).toUpperCase();

            if (tokenRepository.existsByTokenValue(tokenValue)) {
                throw new IllegalStateException("Token collision after retry - contact system administrator");
            }
        }

        // Get current username for audit
        String currentUser = SecurityContext.getCurrentUsername().orElse("system");

        // Create token entity
        StudentToken token = StudentToken.builder()
                .tokenValue(tokenValue)
                .studentId(studentId)
                .schoolYear(schoolYear)
                .salt(tokenSalt)
                .createdAt(createdAt)
                .expiresAt(calculateExpirationDate(schoolYear))
                .active(true)
                .rotationCount(0)
                .createdBy(currentUser)
                .build();

        // Save to repository
        token = tokenRepository.save(token);

        log.info("TOKENIZATION: Generated token {} for student {}", tokenValue, studentId);

        return token;
    }

    /**
     * Generate tokens for all students (batch operation).
     *
     * @return Summary of token generation
     */
    @Transactional
    public TokenGenerationSummary generateAllTokens() {
        log.warn("TOKENIZATION: Starting batch token generation for all students");

        List<Student> students = studentRepository.findAll();
        int generated = 0;
        int skipped = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (Student student : students) {
            try {
                // Check if student already has active token for current year
                Optional<StudentToken> existingToken = getActiveTokenForStudent(student.getId());
                if (existingToken.isPresent()) {
                    skipped++;
                    continue;
                }

                generateToken(student.getId());
                generated++;

            } catch (Exception e) {
                failed++;
                errors.add("Student " + student.getId() + ": " + e.getMessage());
                log.error("TOKENIZATION: Failed to generate token for student {}: {}",
                        student.getId(), e.getMessage());
            }
        }

        log.warn("TOKENIZATION: Batch generation complete. Generated: {}, Skipped: {}, Failed: {}",
                generated, skipped, failed);

        return TokenGenerationSummary.builder()
                .totalStudents(students.size())
                .tokensGenerated(generated)
                .tokensSkipped(skipped)
                .tokensFailed(failed)
                .errors(errors)
                .generatedAt(LocalDateTime.now())
                .schoolYear(getCurrentSchoolYear())
                .build();
    }

    /**
     * Rotate token for a student (on-demand or annual).
     *
     * @param studentId Student ID
     * @param reason Rotation reason
     * @return New token
     */
    @Transactional
    public StudentToken rotateToken(Long studentId, String reason) {
        log.warn("TOKENIZATION: Rotating token for student {} - Reason: {}", studentId, reason);

        String currentUser = SecurityContext.getCurrentUsername().orElse("system");

        // Deactivate existing tokens via repository
        tokenRepository.deactivateStudentTokens(studentId, LocalDateTime.now(), currentUser);

        // Generate new token
        StudentToken newToken = generateToken(studentId);

        // Update rotation metadata
        newToken.setRotationCount(newToken.getRotationCount() + 1);
        newToken.setRotationReason(reason);
        newToken.setLastRotatedAt(LocalDateTime.now());

        // Save updated token
        newToken = tokenRepository.save(newToken);

        log.warn("TOKENIZATION: Token rotated for student {}. New token: {}",
                studentId, newToken.getTokenValue());

        return newToken;
    }

    /**
     * Perform annual token rotation for all students.
     *
     * @return Rotation summary
     */
    @Transactional
    public TokenRotationSummary performAnnualRotation() {
        log.warn("TOKENIZATION: Starting annual token rotation");

        String currentYear = getCurrentSchoolYear();
        List<Student> students = studentRepository.findAll();

        int rotated = 0;
        int skipped = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (Student student : students) {
            try {
                Optional<StudentToken> existingToken = getActiveTokenForStudent(student.getId());

                // Skip if already has token for current year
                if (existingToken.isPresent() &&
                    currentYear.equals(existingToken.get().getSchoolYear())) {
                    skipped++;
                    continue;
                }

                rotateToken(student.getId(), "Annual rotation - " + currentYear);
                rotated++;

            } catch (Exception e) {
                failed++;
                errors.add("Student " + student.getId() + ": " + e.getMessage());
            }
        }

        log.warn("TOKENIZATION: Annual rotation complete. Rotated: {}, Skipped: {}, Failed: {}",
                rotated, skipped, failed);

        return TokenRotationSummary.builder()
                .schoolYear(currentYear)
                .tokensRotated(rotated)
                .tokensSkipped(skipped)
                .tokensFailed(failed)
                .errors(errors)
                .completedAt(LocalDateTime.now())
                .build();
    }

    // ========================================================================
    // TOKEN LOOKUP
    // ========================================================================

    /**
     * Get student ID from token (internal use only - never expose externally).
     *
     * @param tokenValue The token value
     * @return Student ID if valid, empty if invalid/expired
     */
    public Optional<Long> getStudentIdFromToken(String tokenValue) {
        return tokenRepository.findByTokenValue(tokenValue)
                .filter(StudentToken::isValid)
                .map(StudentToken::getStudentId);
    }

    /**
     * Get active token for a student.
     *
     * @param studentId Student ID
     * @return Active token if exists
     */
    public Optional<StudentToken> getActiveTokenForStudent(Long studentId) {
        return tokenRepository.findActiveTokenForStudent(studentId, LocalDateTime.now());
    }

    /**
     * Validate a token.
     *
     * @param tokenValue Token to validate
     * @return Validation result
     */
    public TokenValidationResult validateToken(String tokenValue) {
        if (tokenValue == null || !tokenValue.startsWith(TOKEN_PREFIX)) {
            return TokenValidationResult.builder()
                    .valid(false)
                    .reason("Invalid token format")
                    .build();
        }

        Optional<StudentToken> tokenOpt = tokenRepository.findByTokenValue(tokenValue);

        if (tokenOpt.isEmpty()) {
            return TokenValidationResult.builder()
                    .valid(false)
                    .reason("Token not found")
                    .build();
        }

        StudentToken t = tokenOpt.get();

        if (!t.getActive()) {
            return TokenValidationResult.builder()
                    .valid(false)
                    .reason("Token is deactivated")
                    .deactivatedAt(t.getDeactivatedAt())
                    .build();
        }

        if (t.getExpiresAt() != null && LocalDateTime.now().isAfter(t.getExpiresAt())) {
            return TokenValidationResult.builder()
                    .valid(false)
                    .reason("Token has expired")
                    .expiredAt(t.getExpiresAt())
                    .build();
        }

        return TokenValidationResult.builder()
                .valid(true)
                .tokenValue(tokenValue)
                .schoolYear(t.getSchoolYear())
                .createdAt(t.getCreatedAt())
                .expiresAt(t.getExpiresAt())
                .build();
    }

    // ========================================================================
    // TOKENIZED DATA EXPORT (For Sync to External Systems)
    // ========================================================================

    /**
     * Generate tokenized student data for burst sync to external systems.
     * Contains NO PII - only tokens and non-identifying data.
     *
     * @return List of tokenized student records
     */
    public List<TokenizedStudentData> generateTokenizedDataForSync() {
        log.info("TOKENIZATION: Generating tokenized data for external sync");

        List<Student> students = studentRepository.findAll();
        List<TokenizedStudentData> tokenizedData = new ArrayList<>();

        for (Student student : students) {
            try {
                Optional<StudentToken> tokenOpt = getActiveTokenForStudent(student.getId());

                if (tokenOpt.isEmpty()) {
                    // Generate token if doesn't exist
                    StudentToken newToken = generateToken(student.getId());
                    tokenOpt = Optional.of(newToken);
                }

                StudentToken token = tokenOpt.get();

                // Create tokenized record - NO PII included
                // Convert grade level string to integer
                Integer gradeLevelInt = null;
                try {
                    gradeLevelInt = Integer.parseInt(student.getGradeLevel());
                } catch (NumberFormatException ignored) {
                    // Keep null for non-numeric grade levels like K, PK
                }

                TokenizedStudentData data = TokenizedStudentData.builder()
                        .token(token.getTokenValue())
                        .gradeLevel(gradeLevelInt)
                        .schoolYear(token.getSchoolYear())
                        .enrollmentStatus(student.getStudentStatus() != null ? student.getStudentStatus().name() : "ACTIVE")
                        .lastUpdated(LocalDateTime.now())
                        .checksum(generateRecordChecksum(token.getTokenValue(), gradeLevelInt))
                        .build();

                tokenizedData.add(data);

            } catch (Exception e) {
                log.error("TOKENIZATION: Failed to tokenize student {}: {}", student.getId(), e.getMessage());
            }
        }

        log.info("TOKENIZATION: Generated {} tokenized records for sync", tokenizedData.size());

        return tokenizedData;
    }

    /**
     * Generate tokenized grade data for a student (for sync).
     *
     * @param studentId Student ID
     * @return Tokenized grade data
     */
    public Optional<TokenizedGradeData> generateTokenizedGradeData(Long studentId) {
        Optional<StudentToken> tokenOpt = getActiveTokenForStudent(studentId);

        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        StudentToken token = tokenOpt.get();

        // In production, would fetch actual grades
        // For now, return structure with token
        return Optional.of(TokenizedGradeData.builder()
                .studentToken(token.getTokenValue())
                .schoolYear(token.getSchoolYear())
                .lastUpdated(LocalDateTime.now())
                .courses(new ArrayList<>()) // Would be populated with actual course grades
                .checksum(generateRecordChecksum(token.getTokenValue(), 0))
                .build());
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String generateSecureSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        StringBuilder hexString = new StringBuilder();
        for (byte b : salt) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String getCurrentSchoolYear() {
        int currentYear = Year.now().getValue();
        int currentMonth = java.time.LocalDate.now().getMonthValue();

        // School year starts in August/September
        if (currentMonth >= 8) {
            return currentYear + "-" + (currentYear + 1);
        } else {
            return (currentYear - 1) + "-" + currentYear;
        }
    }

    private LocalDateTime calculateExpirationDate(String schoolYear) {
        // Tokens expire at end of school year (June 30)
        String[] years = schoolYear.split("-");
        int endYear = Integer.parseInt(years[1]);
        return LocalDateTime.of(endYear, 6, 30, 23, 59, 59);
    }

    private String generateRecordChecksum(String token, Integer gradeLevel) {
        String input = token + "|" + gradeLevel + "|" + System.currentTimeMillis();
        return sha256Hash(input).substring(0, 8).toUpperCase();
    }

    // ========================================================================
    // SERVICE METHODS FOR UI
    // ========================================================================

    /**
     * Get all tokens with optional filtering.
     *
     * @param active Filter by active status (null for all)
     * @param schoolYear Filter by school year (null for all)
     * @return List of tokens
     */
    public List<StudentToken> getAllTokens(Boolean active, String schoolYear) {
        if (active == null && schoolYear == null) {
            return tokenRepository.findAll();
        }
        return tokenRepository.findByFilters(active, schoolYear);
    }

    /**
     * Get all tokens.
     */
    public List<StudentToken> getAllTokens() {
        return tokenRepository.findAll();
    }

    /**
     * Search tokens by token value.
     *
     * @param search Search string
     * @return Matching tokens
     */
    public List<StudentToken> searchTokens(String search) {
        return tokenRepository.searchByTokenValue(search);
    }

    /**
     * Count active tokens.
     */
    public long countActiveTokens() {
        return tokenRepository.countActiveTokens(LocalDateTime.now());
    }

    /**
     * Count expired tokens.
     */
    public long countExpiredTokens() {
        return tokenRepository.countExpiredTokens(LocalDateTime.now());
    }

    /**
     * Get tokens expiring within specified days.
     *
     * @param days Number of days
     * @return Tokens expiring soon
     */
    public List<StudentToken> getTokensExpiringSoon(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(days);
        return tokenRepository.findTokensExpiringSoon(now, threshold);
    }

    /**
     * Get token by value.
     *
     * @param tokenValue The token value
     * @return Token if found
     */
    public Optional<StudentToken> getTokenByValue(String tokenValue) {
        return tokenRepository.findByTokenValue(tokenValue);
    }

    // ========================================================================
    // DTO CLASSES (StudentToken is now an entity in model.domain)
    // ========================================================================

    @Data
    @Builder
    public static class TokenGenerationSummary {
        private int totalStudents;
        private int tokensGenerated;
        private int tokensSkipped;
        private int tokensFailed;
        private List<String> errors;
        private LocalDateTime generatedAt;
        private String schoolYear;
    }

    @Data
    @Builder
    public static class TokenRotationSummary {
        private String schoolYear;
        private int tokensRotated;
        private int tokensSkipped;
        private int tokensFailed;
        private List<String> errors;
        private LocalDateTime completedAt;
    }

    @Data
    @Builder
    public static class TokenValidationResult {
        private boolean valid;
        private String reason;
        private String tokenValue;
        private String schoolYear;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private LocalDateTime expiredAt;
        private LocalDateTime deactivatedAt;
    }

    @Data
    @Builder
    public static class TokenizedStudentData {
        private String token;              // STU-XXXXXX (no PII)
        private Integer gradeLevel;        // Non-identifying
        private String schoolYear;         // Non-identifying
        private String enrollmentStatus;   // Non-identifying
        private LocalDateTime lastUpdated;
        private String checksum;
    }

    @Data
    @Builder
    public static class TokenizedGradeData {
        private String studentToken;
        private String schoolYear;
        private LocalDateTime lastUpdated;
        private List<TokenizedCourseGrade> courses;
        private String checksum;
    }

    @Data
    @Builder
    public static class TokenizedCourseGrade {
        private String courseToken;        // Tokenized course ID
        private Double gradePercentage;
        private String letterGrade;
        private LocalDateTime lastUpdated;
    }
}
