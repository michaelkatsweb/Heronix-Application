package com.heronix.service;

import com.heronix.model.domain.ExcuseCode;
import com.heronix.model.domain.ExcuseCode.ExcuseCategory;
import com.heronix.repository.ExcuseCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing Excuse Codes
 *
 * Provides CRUD operations and utility methods for attendance excuse codes.
 * Includes automatic initialization of default codes on first run.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 58 - Attendance Enhancement - January 2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcuseCodeService {

    private final ExcuseCodeRepository excuseCodeRepository;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    /**
     * Initialize default excuse codes if none exist
     */
    @PostConstruct
    @Transactional
    public void initializeDefaultCodes() {
        if (excuseCodeRepository.countByActiveTrue() == 0) {
            log.info("No excuse codes found, initializing defaults...");

            ExcuseCode[] defaults = ExcuseCode.getDefaultExcuseCodes();
            for (ExcuseCode code : defaults) {
                if (!excuseCodeRepository.existsByCode(code.getCode())) {
                    excuseCodeRepository.save(code);
                }
            }

            log.info("Initialized {} default excuse codes", defaults.length);
        }
    }

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    /**
     * Get all active excuse codes
     */
    public List<ExcuseCode> getAllActiveCodes() {
        return excuseCodeRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    /**
     * Get all excuse codes (including inactive)
     */
    public List<ExcuseCode> getAllCodes() {
        return excuseCodeRepository.findAll();
    }

    /**
     * Get excuse code by ID
     */
    public Optional<ExcuseCode> findById(Long id) {
        return excuseCodeRepository.findById(id);
    }

    /**
     * Get excuse code by short code
     */
    public Optional<ExcuseCode> findByCode(String code) {
        return excuseCodeRepository.findByCode(code);
    }

    /**
     * Save or update an excuse code
     */
    @Transactional
    public ExcuseCode save(ExcuseCode excuseCode) {
        return excuseCodeRepository.save(excuseCode);
    }

    /**
     * Delete an excuse code (soft delete - marks as inactive)
     */
    @Transactional
    public void deactivate(Long id) {
        excuseCodeRepository.findById(id).ifPresent(code -> {
            code.setActive(false);
            excuseCodeRepository.save(code);
            log.info("Deactivated excuse code: {}", code.getCode());
        });
    }

    /**
     * Reactivate an excuse code
     */
    @Transactional
    public void activate(Long id) {
        excuseCodeRepository.findById(id).ifPresent(code -> {
            code.setActive(true);
            excuseCodeRepository.save(code);
            log.info("Reactivated excuse code: {}", code.getCode());
        });
    }

    // ========================================================================
    // CATEGORY-BASED QUERIES
    // ========================================================================

    /**
     * Get excuse codes by category
     */
    public List<ExcuseCode> findByCategory(ExcuseCategory category) {
        return excuseCodeRepository.findByCategoryAndActiveTrueOrderBySortOrderAsc(category);
    }

    /**
     * Get excuse codes grouped by category
     */
    public Map<ExcuseCategory, List<ExcuseCode>> getCodesByCategory() {
        return getAllActiveCodes().stream()
                .collect(Collectors.groupingBy(ExcuseCode::getCategory));
    }

    // ========================================================================
    // UTILITY QUERIES
    // ========================================================================

    /**
     * Get codes that count as excused
     */
    public List<ExcuseCode> getExcusedCodes() {
        return excuseCodeRepository.findByCountsAsExcusedTrueAndActiveTrueOrderBySortOrderAsc();
    }

    /**
     * Get codes that require documentation
     */
    public List<ExcuseCode> getCodesRequiringDocumentation() {
        return excuseCodeRepository.findByDocumentationRequiredTrueAndActiveTrueOrderBySortOrderAsc();
    }

    /**
     * Get codes that require approval
     */
    public List<ExcuseCode> getCodesRequiringApproval() {
        return excuseCodeRepository.findByRequiresApprovalTrueAndActiveTrueOrderBySortOrderAsc();
    }

    /**
     * Search codes by name or code
     */
    public List<ExcuseCode> search(String searchTerm) {
        return excuseCodeRepository.searchByCodeOrName(searchTerm);
    }

    // ========================================================================
    // VALIDATION
    // ========================================================================

    /**
     * Check if a code exists
     */
    public boolean codeExists(String code) {
        return excuseCodeRepository.existsByCode(code);
    }

    /**
     * Validate an excuse code for use
     *
     * @param code the excuse code
     * @param consecutiveDays number of consecutive days being excused
     * @return validation result with error message if invalid
     */
    public ValidationResult validateCode(String code, int consecutiveDays) {
        Optional<ExcuseCode> excuseCodeOpt = findByCode(code);

        if (excuseCodeOpt.isEmpty()) {
            return new ValidationResult(false, "Excuse code not found: " + code);
        }

        ExcuseCode excuseCode = excuseCodeOpt.get();

        if (!Boolean.TRUE.equals(excuseCode.getActive())) {
            return new ValidationResult(false, "Excuse code is not active: " + code);
        }

        if (excuseCode.getMaxConsecutiveDays() != null &&
            excuseCode.getMaxConsecutiveDays() > 0 &&
            consecutiveDays > excuseCode.getMaxConsecutiveDays()) {
            return new ValidationResult(false,
                    String.format("Exceeds maximum consecutive days (%d) for excuse code %s. Requires approval.",
                            excuseCode.getMaxConsecutiveDays(), code));
        }

        return new ValidationResult(true, null);
    }

    /**
     * Simple validation result record
     */
    public record ValidationResult(boolean valid, String errorMessage) {}

    // ========================================================================
    // STATE REPORTING
    // ========================================================================

    /**
     * Get excuse code by state code (for state reporting integration)
     */
    public Optional<ExcuseCode> findByStateCode(String stateCode) {
        return excuseCodeRepository.findByStateCode(stateCode);
    }

    /**
     * Update state code mapping
     */
    @Transactional
    public void updateStateCode(Long id, String stateCode) {
        excuseCodeRepository.findById(id).ifPresent(code -> {
            code.setStateCode(stateCode);
            excuseCodeRepository.save(code);
            log.info("Updated state code for {}: {}", code.getCode(), stateCode);
        });
    }
}
