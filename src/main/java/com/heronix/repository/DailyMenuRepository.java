package com.heronix.repository;

import com.heronix.model.domain.DailyMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DailyMenu entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface DailyMenuRepository extends JpaRepository<DailyMenu, Long> {

    Optional<DailyMenu> findByMenuDateAndMealPeriod(LocalDate menuDate, DailyMenu.MealPeriod mealPeriod);

    List<DailyMenu> findByMenuDate(LocalDate menuDate);

    List<DailyMenu> findByMealPeriod(DailyMenu.MealPeriod mealPeriod);

    @Query("SELECT m FROM DailyMenu m WHERE m.menuDate >= :startDate AND m.menuDate <= :endDate ORDER BY m.menuDate, m.mealPeriod")
    List<DailyMenu> findByDateRange(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT m FROM DailyMenu m WHERE m.menuDate >= :startDate AND m.menuDate <= :endDate AND m.mealPeriod = :period ORDER BY m.menuDate")
    List<DailyMenu> findByDateRangeAndPeriod(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("period") DailyMenu.MealPeriod mealPeriod);

    @Query("SELECT m FROM DailyMenu m WHERE m.published = true AND m.menuDate >= :date ORDER BY m.menuDate, m.mealPeriod")
    List<DailyMenu> findPublishedMenusFromDate(@Param("date") LocalDate date);

    @Query("SELECT m FROM DailyMenu m WHERE m.published = false ORDER BY m.menuDate")
    List<DailyMenu> findUnpublishedMenus();

    @Query("SELECT m FROM DailyMenu m WHERE m.vegetarianOptions = true AND m.menuDate = :date")
    List<DailyMenu> findVegetarianMenusByDate(@Param("date") LocalDate date);

    @Query("SELECT m FROM DailyMenu m WHERE m.glutenFreeOptions = true AND m.menuDate = :date")
    List<DailyMenu> findGlutenFreeMenusByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(m) FROM DailyMenu m WHERE m.published = true")
    long countPublishedMenus();
}
