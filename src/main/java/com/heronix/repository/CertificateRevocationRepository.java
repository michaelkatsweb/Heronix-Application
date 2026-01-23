package com.heronix.repository;

import com.heronix.model.domain.CertificateRevocationEntry;
import com.heronix.model.domain.CertificateRevocationEntry.RevocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Certificate Revocation List (CRL) persistence operations.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 20, 2026
 */
@Repository
public interface CertificateRevocationRepository extends JpaRepository<CertificateRevocationEntry, Long> {

    /**
     * Find by certificate serial number
     */
    Optional<CertificateRevocationEntry> findBySerialNumber(String serialNumber);

    /**
     * Check if certificate is revoked
     */
    boolean existsBySerialNumber(String serialNumber);

    /**
     * Find all revocations ordered by revocation date (newest first)
     */
    List<CertificateRevocationEntry> findAllByOrderByRevokedAtDesc();

    /**
     * Find revocations for a specific device
     */
    List<CertificateRevocationEntry> findByDeviceIdOrderByRevokedAtDesc(String deviceId);

    /**
     * Find revocations for a specific account
     */
    List<CertificateRevocationEntry> findByAccountTokenOrderByRevokedAtDesc(String accountToken);

    /**
     * Find revocations by type
     */
    List<CertificateRevocationEntry> findByRevocationTypeOrderByRevokedAtDesc(RevocationType revocationType);

    /**
     * Find revocations not yet synced to DMZ
     */
    List<CertificateRevocationEntry> findBySyncedToDmzFalseOrderByRevokedAtAsc();

    /**
     * Count entries not synced
     */
    long countBySyncedToDmzFalse();

    /**
     * Find revocations since a specific date (for incremental sync)
     */
    @Query("SELECT c FROM CertificateRevocationEntry c WHERE c.revokedAt >= :since ORDER BY c.revokedAt ASC")
    List<CertificateRevocationEntry> findRevocationsSince(@Param("since") LocalDateTime since);

    /**
     * Find revocations by date range
     */
    @Query("SELECT c FROM CertificateRevocationEntry c WHERE c.revokedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY c.revokedAt DESC")
    List<CertificateRevocationEntry> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Mark entries as synced
     */
    @Modifying
    @Query("UPDATE CertificateRevocationEntry c SET c.syncedToDmz = true, c.lastSyncedAt = :syncTime " +
           "WHERE c.id IN :ids")
    int markAsSynced(@Param("ids") List<Long> ids, @Param("syncTime") LocalDateTime syncTime);

    /**
     * Get all serial numbers (for quick lookup set)
     */
    @Query("SELECT c.serialNumber FROM CertificateRevocationEntry c")
    List<String> findAllSerialNumbers();

    /**
     * Count total revocations
     */
    @Query("SELECT COUNT(c) FROM CertificateRevocationEntry c")
    long countTotalRevocations();

    /**
     * Count revocations by type
     */
    long countByRevocationType(RevocationType revocationType);

    /**
     * Find recent revocations (last N days)
     */
    @Query("SELECT c FROM CertificateRevocationEntry c WHERE c.revokedAt >= :threshold " +
           "ORDER BY c.revokedAt DESC")
    List<CertificateRevocationEntry> findRecentRevocations(@Param("threshold") LocalDateTime threshold);

    /**
     * Search by serial number or reason
     */
    @Query("SELECT c FROM CertificateRevocationEntry c WHERE " +
           "LOWER(c.serialNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.reason) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<CertificateRevocationEntry> searchRevocations(@Param("search") String search);

    /**
     * Find revocations by revoker
     */
    List<CertificateRevocationEntry> findByRevokedByOrderByRevokedAtDesc(String revokedBy);
}
