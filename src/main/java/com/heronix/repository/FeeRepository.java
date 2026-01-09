package com.heronix.repository;

import com.heronix.model.domain.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Fee entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {

    List<Fee> findByActiveTrue();

    List<Fee> findByActiveTrueOrderByFeeNameAsc();

    List<Fee> findByFeeTypeAndActiveTrue(Fee.FeeType feeType);

    List<Fee> findByAcademicYearAndActiveTrue(String academicYear);

    List<Fee> findByGradeLevelAndActiveTrue(String gradeLevel);

    @Query("SELECT f FROM Fee f WHERE f.active = true AND " +
           "(f.gradeLevel IS NULL OR f.gradeLevel = :gradeLevel) AND " +
           "(f.academicYear = :academicYear)")
    List<Fee> findApplicableFeesForStudent(@Param("gradeLevel") String gradeLevel,
                                            @Param("academicYear") String academicYear);

    @Query("SELECT f FROM Fee f WHERE f.campus.id = :campusId AND f.active = true")
    List<Fee> findByCampusId(@Param("campusId") Long campusId);

    @Query("SELECT f FROM Fee f WHERE f.mandatory = true AND f.active = true")
    List<Fee> findMandatoryFees();
}
