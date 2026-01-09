package com.heronix.repository;

import com.heronix.model.domain.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for API Key Management
 *
 * Provides data access methods for API key CRUD operations and queries.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 42 - API Security & Authentication
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    /**
     * Find API key by its hash
     * Used during authentication
     */
    Optional<ApiKey> findByKeyHash(String keyHash);

    /**
     * Find all API keys for a user
     */
    List<ApiKey> findByUserId(String userId);

    /**
     * Find all API keys for a user ordered by created date descending
     */
    List<ApiKey> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Find all active API keys for a user
     */
    List<ApiKey> findByUserIdAndActiveTrue(String userId);

    /**
     * Find API keys by user ID and active status
     */
    List<ApiKey> findByUserIdAndActive(String userId, Boolean active);

    /**
     * Find all expired API keys
     */
    @Query("SELECT a FROM ApiKey a WHERE a.expiresAt IS NOT NULL AND a.expiresAt < :now")
    List<ApiKey> findExpiredKeys(LocalDateTime now);

    /**
     * Count active API keys for a user
     */
    long countByUserIdAndActiveTrue(String userId);

    /**
     * Find API keys by key prefix
     * Useful for identifying keys in logs
     */
    List<ApiKey> findByKeyPrefix(String keyPrefix);
}
