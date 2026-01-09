package com.heronix.repository;

import com.heronix.model.domain.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MealPlan entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    Optional<MealPlan> findByPlanName(String planName);

    List<MealPlan> findByActiveTrue();

    List<MealPlan> findByActiveTrueOrderByPlanNameAsc();

    List<MealPlan> findByAcademicYear(String academicYear);

    List<MealPlan> findByAcademicYearAndActiveTrue(String academicYear);

    List<MealPlan> findByPlanType(MealPlan.PlanType planType);

    @Query("SELECT mp FROM MealPlan mp WHERE mp.active = true AND mp.academicYear = :year AND mp.planType = :type")
    List<MealPlan> findActivePlansByYearAndType(@Param("year") String academicYear,
                                                 @Param("type") MealPlan.PlanType planType);

    @Query("SELECT COUNT(mp) FROM MealPlan mp WHERE mp.active = true")
    long countActivePlans();
}
