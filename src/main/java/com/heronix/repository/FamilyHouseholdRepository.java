package com.heronix.repository;

import com.heronix.model.domain.FamilyHousehold;
import com.heronix.model.domain.FamilyHousehold.FamilyStatus;
import com.heronix.model.domain.FamilyHousehold.HouseholdType;
import com.heronix.model.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for FamilyHousehold Entity
 * Handles database operations for family household management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Repository
public interface FamilyHouseholdRepository extends JpaRepository<FamilyHousehold, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find family household by family ID
     */
    Optional<FamilyHousehold> findByFamilyId(String familyId);

    /**
     * Find all families by status
     */
    List<FamilyHousehold> findByStatus(FamilyStatus status);

    /**
     * Find all active families
     */
    List<FamilyHousehold> findByIsActiveTrue();

    /**
     * Find families by household type
     */
    List<FamilyHousehold> findByHouseholdType(HouseholdType householdType);

    // ========================================================================
    // SEARCH QUERIES
    // ========================================================================

    /**
     * Search families by family name
     */
    List<FamilyHousehold> findByFamilyNameContainingIgnoreCase(String familyName);

    /**
     * Search by primary parent name
     */
    List<FamilyHousehold> findByPrimaryParentNameContainingIgnoreCase(String parentName);

    /**
     * Search by primary email
     */
    Optional<FamilyHousehold> findByPrimaryEmail(String email);

    /**
     * Search by primary phone
     */
    List<FamilyHousehold> findByPrimaryPhoneContaining(String phone);

    /**
     * Search by parent 1 email
     */
    Optional<FamilyHousehold> findByParent1Email(String email);

    /**
     * Search by parent 2 email
     */
    Optional<FamilyHousehold> findByParent2Email(String email);

    // ========================================================================
    // ADDRESS QUERIES
    // ========================================================================

    /**
     * Find families by city
     */
    List<FamilyHousehold> findByPrimaryCity(String city);

    /**
     * Find families by state
     */
    List<FamilyHousehold> findByPrimaryState(String state);

    /**
     * Find families by zip code
     */
    List<FamilyHousehold> findByPrimaryZipCode(String zipCode);

    /**
     * Find families by county
     */
    List<FamilyHousehold> findByPrimaryCounty(String county);

    // ========================================================================
    // CHILDREN QUERIES
    // ========================================================================

    /**
     * Find families with specific number of enrolled children
     */
    List<FamilyHousehold> findByEnrolledChildren(Integer enrolledChildren);

    /**
     * Find families with enrolled children greater than or equal to count
     */
    List<FamilyHousehold> findByEnrolledChildrenGreaterThanEqual(Integer minChildren);

    /**
     * Find families with multiple children (siblings)
     */
    @Query("SELECT fh FROM FamilyHousehold fh WHERE fh.enrolledChildren >= 2")
    List<FamilyHousehold> findFamiliesWithMultipleChildren();

    /**
     * Find families with pending children
     */
    @Query("SELECT fh FROM FamilyHousehold fh WHERE fh.pendingChildren > 0")
    List<FamilyHousehold> findFamiliesWithPendingChildren();

    /**
     * Find family household for a specific student
     */
    @Query("SELECT fh FROM FamilyHousehold fh JOIN fh.children s WHERE s.id = :studentId")
    Optional<FamilyHousehold> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find siblings for a student (same family household)
     */
    @Query("SELECT s FROM Student s WHERE s.familyHousehold.id = :familyId AND s.id != :studentId")
    List<Student> findSiblingsForStudent(@Param("familyId") Long familyId, @Param("studentId") Long studentId);

    // ========================================================================
    // DISCOUNT QUERIES
    // ========================================================================

    /**
     * Find families with sibling discount applied
     */
    @Query("SELECT fh FROM FamilyHousehold fh WHERE fh.discount2ndChild = true OR fh.discount3rdPlusChildren = true")
    List<FamilyHousehold> findFamiliesWithSiblingDiscount();

    /**
     * Find families with early bird discount
     */
    List<FamilyHousehold> findByEarlyBirdDiscountAppliedTrue();

    /**
     * Find families with specific discount type
     */
    List<FamilyHousehold> findByDiscountType(FamilyHousehold.DiscountType discountType);

    /**
     * Find families eligible for discounts but not applied
     */
    @Query("SELECT fh FROM FamilyHousehold fh WHERE " +
           "fh.enrolledChildren >= 2 AND " +
           "fh.discount2ndChild = false AND " +
           "fh.isActive = true")
    List<FamilyHousehold> findEligibleForDiscountsNotApplied();

    // ========================================================================
    // CUSTODY QUERIES
    // ========================================================================

    /**
     * Find families with custody papers on file
     */
    List<FamilyHousehold> findByCustodyPapersOnFileTrue();

    /**
     * Find families with pickup restrictions
     */
    @Query("SELECT fh FROM FamilyHousehold fh WHERE fh.pickupRestrictions IS NOT NULL")
    List<FamilyHousehold> findFamiliesWithPickupRestrictions();

    /**
     * Find families by custody arrangement
     */
    List<FamilyHousehold> findByCustodyArrangement(FamilyHousehold.CustodyArrangement custodyArrangement);

    // ========================================================================
    // DATE RANGE QUERIES
    // ========================================================================

    /**
     * Find families created within date range
     */
    List<FamilyHousehold> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find families created after specific date
     */
    List<FamilyHousehold> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find recently updated families
     */
    List<FamilyHousehold> findByUpdatedAtAfter(LocalDateTime date);

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Count families by status
     */
    long countByStatus(FamilyStatus status);

    /**
     * Count active families
     */
    long countByIsActiveTrue();

    /**
     * Count families with multiple children
     */
    @Query("SELECT COUNT(fh) FROM FamilyHousehold fh WHERE fh.enrolledChildren >= 2")
    long countFamiliesWithMultipleChildren();

    /**
     * Get total enrolled children across all families
     */
    @Query("SELECT SUM(fh.enrolledChildren) FROM FamilyHousehold fh WHERE fh.isActive = true")
    Long getTotalEnrolledChildren();

    /**
     * Get statistics by household type
     */
    @Query("SELECT fh.householdType, COUNT(fh) FROM FamilyHousehold fh " +
           "WHERE fh.isActive = true GROUP BY fh.householdType")
    List<Object[]> getCountByHouseholdType();

    /**
     * Get families grouped by number of children
     */
    @Query("SELECT fh.enrolledChildren, COUNT(fh) FROM FamilyHousehold fh " +
           "WHERE fh.isActive = true GROUP BY fh.enrolledChildren ORDER BY fh.enrolledChildren")
    List<Object[]> getCountByNumberOfChildren();

    /**
     * Get total family discount amounts
     */
    @Query("SELECT SUM(fh.totalFamilyDiscount) FROM FamilyHousehold fh WHERE fh.isActive = true")
    Double getTotalFamilyDiscounts();

    // ========================================================================
    // VALIDATION QUERIES
    // ========================================================================

    /**
     * Check if family ID already exists
     */
    boolean existsByFamilyId(String familyId);

    /**
     * Check if primary email already exists
     */
    boolean existsByPrimaryEmail(String email);

    /**
     * Find incomplete families (missing required information)
     */
    @Query("SELECT fh FROM FamilyHousehold fh WHERE " +
           "fh.familyName IS NULL OR " +
           "fh.primaryParentName IS NULL OR " +
           "fh.primaryPhone IS NULL OR " +
           "fh.primaryEmail IS NULL OR " +
           "fh.primaryAddress IS NULL")
    List<FamilyHousehold> findIncompleteFamilies();

    // ========================================================================
    // CUSTOM COMPLEX QUERIES
    // ========================================================================

    /**
     * Find families for discount eligibility report
     */
    @Query("SELECT fh FROM FamilyHousehold fh WHERE " +
           "fh.isActive = true AND " +
           "fh.enrolledChildren >= 2 " +
           "ORDER BY fh.enrolledChildren DESC, fh.familyName ASC")
    List<FamilyHousehold> findFamiliesForDiscountReport();

    /**
     * Find families needing contact information update
     */
    @Query("SELECT fh FROM FamilyHousehold fh WHERE " +
           "fh.isActive = true AND " +
           "(fh.primaryEmail IS NULL OR fh.primaryPhone IS NULL)")
    List<FamilyHousehold> findFamiliesNeedingContactUpdate();
}
