package com.heronix.repository;

import com.heronix.model.domain.IEPAccommodation;
import com.heronix.model.domain.IEPAccommodation.AccommodationCategory;
import com.heronix.model.domain.IEPAccommodation.AccommodationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for IEP Accommodation entities
 * Provides data access for accommodations and modifications
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface IEPAccommodationRepository extends JpaRepository<IEPAccommodation, Long> {

    // Basic Queries
    List<IEPAccommodation> findByIepId(Long iepId);

    List<IEPAccommodation> findByIepIdAndIsActiveTrue(Long iepId);

    List<IEPAccommodation> findByType(AccommodationType type);

    List<IEPAccommodation> findByCategory(AccommodationCategory category);

    // Student Accommodations
    @Query("SELECT a FROM IEPAccommodation a WHERE a.iep.student.id = :studentId " +
           "AND a.isActive = true ORDER BY a.category ASC")
    List<IEPAccommodation> findActiveByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT a FROM IEPAccommodation a WHERE a.iep.student.id = :studentId " +
           "ORDER BY a.category ASC, a.type ASC")
    List<IEPAccommodation> findByStudentId(@Param("studentId") Long studentId);

    // Subject/Class Specific
    @Query("SELECT a FROM IEPAccommodation a WHERE a.iep.id = :iepId " +
           "AND (a.appliesToAllClasses = true OR :subject MEMBER OF a.applicableSubjects) " +
           "AND a.isActive = true ORDER BY a.category ASC")
    List<IEPAccommodation> findByIepIdAndSubject(@Param("iepId") Long iepId,
                                                   @Param("subject") String subject);

    // State Testing
    @Query("SELECT a FROM IEPAccommodation a WHERE a.iep.student.id = :studentId " +
           "AND a.appliesToStateTesting = true AND a.isActive = true " +
           "ORDER BY a.category ASC")
    List<IEPAccommodation> findStateTestingAccommodations(@Param("studentId") Long studentId);

    // Training Needs
    @Query("SELECT a FROM IEPAccommodation a WHERE a.requiresTraining = true " +
           "AND a.trainingProvided = false AND a.isActive = true " +
           "ORDER BY a.iep.student.lastName ASC")
    List<IEPAccommodation> findNeedingTraining();

    // Modifications
    @Query("SELECT a FROM IEPAccommodation a WHERE a.iep.id = :iepId " +
           "AND a.type = 'MODIFICATION' AND a.isActive = true " +
           "ORDER BY a.category ASC")
    List<IEPAccommodation> findModificationsByIep(@Param("iepId") Long iepId);

    // Category Queries
    @Query("SELECT a FROM IEPAccommodation a WHERE a.iep.id = :iepId " +
           "AND a.category = :category AND a.isActive = true " +
           "ORDER BY a.type ASC")
    List<IEPAccommodation> findByIepIdAndCategory(@Param("iepId") Long iepId,
                                                    @Param("category") AccommodationCategory category);

    @Query("SELECT a.category, COUNT(a) FROM IEPAccommodation a " +
           "WHERE a.iep.id = :iepId AND a.isActive = true " +
           "GROUP BY a.category")
    List<Object[]> countByCategory(@Param("iepId") Long iepId);

    // Statistics
    @Query("SELECT COUNT(a) FROM IEPAccommodation a " +
           "WHERE a.iep.id = :iepId AND a.isActive = true")
    Long countActiveByIep(@Param("iepId") Long iepId);

    @Query("SELECT COUNT(a) FROM IEPAccommodation a " +
           "WHERE a.iep.id = :iepId AND a.type = 'MODIFICATION' AND a.isActive = true")
    Long countModificationsByIep(@Param("iepId") Long iepId);
}
