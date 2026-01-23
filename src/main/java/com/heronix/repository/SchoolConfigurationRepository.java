package com.heronix.repository;

import com.heronix.model.domain.SchoolConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for SchoolConfiguration entity
 *
 * Note: This is a singleton entity - there should only be one configuration per installation.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Repository
public interface SchoolConfigurationRepository extends JpaRepository<SchoolConfiguration, Long> {

    /**
     * Get the current school configuration (singleton)
     * Returns the first/only configuration record
     */
    @Query("SELECT c FROM SchoolConfiguration c ORDER BY c.id ASC LIMIT 1")
    Optional<SchoolConfiguration> findCurrentConfiguration();

    /**
     * Check if initial setup has been completed
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM SchoolConfiguration c WHERE c.setupComplete = true")
    boolean isSetupComplete();

    /**
     * Check if any configuration exists
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM SchoolConfiguration c")
    boolean configurationExists();
}
