package com.heronix.repository;

import com.heronix.model.domain.ExcuseCode;
import com.heronix.model.domain.ExcuseCode.ExcuseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ExcuseCode entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 58 - Attendance Enhancement - January 2026
 */
@Repository
public interface ExcuseCodeRepository extends JpaRepository<ExcuseCode, Long> {

    /**
     * Find excuse code by its short code
     */
    Optional<ExcuseCode> findByCode(String code);

    /**
     * Find all active excuse codes ordered by sort order
     */
    List<ExcuseCode> findByActiveTrueOrderBySortOrderAsc();

    /**
     * Find active excuse codes by category
     */
    List<ExcuseCode> findByCategoryAndActiveTrueOrderBySortOrderAsc(ExcuseCategory category);

    /**
     * Find all excuse codes that count as excused
     */
    List<ExcuseCode> findByCountsAsExcusedTrueAndActiveTrueOrderBySortOrderAsc();

    /**
     * Find excuse codes that require documentation
     */
    List<ExcuseCode> findByDocumentationRequiredTrueAndActiveTrueOrderBySortOrderAsc();

    /**
     * Find excuse codes that require approval
     */
    List<ExcuseCode> findByRequiresApprovalTrueAndActiveTrueOrderBySortOrderAsc();

    /**
     * Search excuse codes by name or code
     */
    @Query("SELECT e FROM ExcuseCode e WHERE e.active = true AND " +
           "(LOWER(e.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ExcuseCode> searchByCodeOrName(@Param("search") String search);

    /**
     * Find by state code for state reporting
     */
    Optional<ExcuseCode> findByStateCode(String stateCode);

    /**
     * Check if code exists
     */
    boolean existsByCode(String code);

    /**
     * Count active excuse codes
     */
    long countByActiveTrue();
}
