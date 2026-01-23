package com.heronix.repository;

import com.heronix.model.domain.StateTerminology;
import com.heronix.model.enums.USState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for StateTerminology entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01 - State Configuration Feature
 */
@Repository
public interface StateTerminologyRepository extends JpaRepository<StateTerminology, Long> {

    /**
     * Find all terminology for a state
     */
    List<StateTerminology> findByStateAndActiveTrue(USState state);

    /**
     * Find specific term for a state
     */
    Optional<StateTerminology> findByStateAndTermKeyAndActiveTrue(USState state, String termKey);

    /**
     * Find all terms by category for a state
     */
    List<StateTerminology> findByStateAndCategoryAndActiveTrue(USState state, String category);

    /**
     * Get the state-specific term, or return default if not found
     */
    @Query("SELECT st.stateTerm FROM StateTerminology st WHERE st.state = :state " +
           "AND st.termKey = :termKey AND st.active = true")
    Optional<String> getStateTerm(@Param("state") USState state, @Param("termKey") String termKey);

    /**
     * Get the abbreviated state term
     */
    @Query("SELECT st.stateTermShort FROM StateTerminology st WHERE st.state = :state " +
           "AND st.termKey = :termKey AND st.active = true")
    Optional<String> getStateTermShort(@Param("state") USState state, @Param("termKey") String termKey);

    /**
     * Check if a term exists for a state
     */
    boolean existsByStateAndTermKey(USState state, String termKey);

    /**
     * Delete all terminology for a state
     */
    void deleteByState(USState state);
}
