package com.heronix.repository;

import com.heronix.model.domain.StudentToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Student Token persistence operations.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 20, 2026
 */
@Repository
public interface StudentTokenRepository extends JpaRepository<StudentToken, Long> {

    /**
     * Find token by its unique value
     */
    Optional<StudentToken> findByTokenValue(String tokenValue);

    /**
     * Check if token value exists (for collision detection)
     */
    boolean existsByTokenValue(String tokenValue);

    /**
     * Find all tokens for a specific student
     */
    List<StudentToken> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    /**
     * Find active token for a student
     */
    @Query("SELECT t FROM StudentToken t WHERE t.studentId = :studentId AND t.active = true " +
           "AND (t.expiresAt IS NULL OR t.expiresAt > :now)")
    Optional<StudentToken> findActiveTokenForStudent(@Param("studentId") Long studentId,
                                                      @Param("now") LocalDateTime now);

    /**
     * Find all active tokens
     */
    @Query("SELECT t FROM StudentToken t WHERE t.active = true " +
           "AND (t.expiresAt IS NULL OR t.expiresAt > :now)")
    List<StudentToken> findAllActiveTokens(@Param("now") LocalDateTime now);

    /**
     * Find tokens by school year
     */
    List<StudentToken> findBySchoolYear(String schoolYear);

    /**
     * Find tokens expiring soon (within specified days)
     */
    @Query("SELECT t FROM StudentToken t WHERE t.active = true " +
           "AND t.expiresAt BETWEEN :now AND :threshold")
    List<StudentToken> findTokensExpiringSoon(@Param("now") LocalDateTime now,
                                               @Param("threshold") LocalDateTime threshold);

    /**
     * Find expired but still active tokens (need deactivation)
     */
    @Query("SELECT t FROM StudentToken t WHERE t.active = true AND t.expiresAt < :now")
    List<StudentToken> findExpiredActiveTokens(@Param("now") LocalDateTime now);

    /**
     * Deactivate all tokens for a student
     */
    @Modifying
    @Query("UPDATE StudentToken t SET t.active = false, t.deactivatedAt = :now, " +
           "t.deactivatedBy = :deactivatedBy WHERE t.studentId = :studentId AND t.active = true")
    int deactivateStudentTokens(@Param("studentId") Long studentId,
                                 @Param("now") LocalDateTime now,
                                 @Param("deactivatedBy") String deactivatedBy);

    /**
     * Count active tokens
     */
    @Query("SELECT COUNT(t) FROM StudentToken t WHERE t.active = true " +
           "AND (t.expiresAt IS NULL OR t.expiresAt > :now)")
    long countActiveTokens(@Param("now") LocalDateTime now);

    /**
     * Count expired tokens
     */
    @Query("SELECT COUNT(t) FROM StudentToken t WHERE t.expiresAt < :now")
    long countExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Count tokens by school year
     */
    long countBySchoolYear(String schoolYear);

    /**
     * Find tokens needing annual rotation (previous school year, still active)
     */
    @Query("SELECT t FROM StudentToken t WHERE t.active = true AND t.schoolYear != :currentYear")
    List<StudentToken> findTokensNeedingRotation(@Param("currentYear") String currentYear);

    /**
     * Search tokens by partial token value (for admin lookup)
     */
    @Query("SELECT t FROM StudentToken t WHERE UPPER(t.tokenValue) LIKE UPPER(CONCAT('%', :search, '%'))")
    List<StudentToken> searchByTokenValue(@Param("search") String search);

    /**
     * Find tokens by status filter
     */
    @Query("SELECT t FROM StudentToken t WHERE " +
           "(:active IS NULL OR t.active = :active) AND " +
           "(:schoolYear IS NULL OR t.schoolYear = :schoolYear) " +
           "ORDER BY t.createdAt DESC")
    List<StudentToken> findByFilters(@Param("active") Boolean active,
                                      @Param("schoolYear") String schoolYear);
}
