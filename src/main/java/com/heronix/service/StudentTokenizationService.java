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

import jakarta.persistence.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

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

    // TODO: Store master salt in HSM/TPM for production security
    // TODO: Implement key rotation mechanism for master salt
    @Value("${heronix.tokenization.master-salt:HERONIX_SECURE_SALT_2026}")
    private String masterSalt;

    // Token prefix
    private static final String TOKEN_PREFIX = "STU-";

    // Token hex length (6 characters = 24 bits = 16.7M combinations)
    private static final int TOKEN_HEX_LENGTH = 6;

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

        // Create hash input
        String hashInput = String.format("%d|%s|%s|%s|%s",
                studentId,
                masterSalt,
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
