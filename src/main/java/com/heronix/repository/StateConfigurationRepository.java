package com.heronix.repository;

import com.heronix.model.domain.StateConfiguration;
import com.heronix.model.enums.USState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for StateConfiguration entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01 - State Configuration Feature
 */
@Repository
public interface StateConfigurationRepository extends JpaRepository<StateConfiguration, Long> {

    /**
     * Find configuration by state
     */
    Optional<StateConfiguration> findByState(USState state);

    /**
     * Find active configuration for a state
     */
    Optional<StateConfiguration> findByStateAndActiveTrue(USState state);

    /**
     * Check if configuration exists for a state
     */
    boolean existsByState(USState state);

    /**
     * Find all active configurations
     */
    List<StateConfiguration> findByActiveTrueOrderByStateName();

    /**
     * Find all states with configurations
     */
    @Query("SELECT DISTINCT sc.state FROM StateConfiguration sc WHERE sc.active = true ORDER BY sc.state")
    List<USState> findStatesWithConfiguration();

    /**
     * Find configurations by school year
     */
    List<StateConfiguration> findBySchoolYear(String schoolYear);
}
