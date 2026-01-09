package com.heronix.repository;

import com.heronix.model.domain.CafeteriaMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CafeteriaMenuItem entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface CafeteriaMenuItemRepository extends JpaRepository<CafeteriaMenuItem, Long> {

    List<CafeteriaMenuItem> findByMenuId(Long menuId);

    List<CafeteriaMenuItem> findByCategory(CafeteriaMenuItem.ItemCategory category);

    List<CafeteriaMenuItem> findByAvailableTrue();

    @Query("SELECT i FROM CafeteriaMenuItem i WHERE i.menu.id = :menuId AND i.available = true ORDER BY i.category, i.itemName")
    List<CafeteriaMenuItem> findAvailableItemsByMenu(@Param("menuId") Long menuId);

    @Query("SELECT i FROM CafeteriaMenuItem i WHERE i.menu.id = :menuId AND i.category = :category ORDER BY i.itemName")
    List<CafeteriaMenuItem> findByMenuAndCategory(@Param("menuId") Long menuId,
                                          @Param("category") CafeteriaMenuItem.ItemCategory category);

    @Query("SELECT i FROM CafeteriaMenuItem i WHERE i.vegetarian = true AND i.available = true")
    List<CafeteriaMenuItem> findVegetarianItems();

    @Query("SELECT i FROM CafeteriaMenuItem i WHERE i.vegan = true AND i.available = true")
    List<CafeteriaMenuItem> findVeganItems();

    @Query("SELECT i FROM CafeteriaMenuItem i WHERE i.glutenFree = true AND i.available = true")
    List<CafeteriaMenuItem> findGlutenFreeItems();

    @Query("SELECT i FROM CafeteriaMenuItem i WHERE i.dairyFree = true AND i.available = true")
    List<CafeteriaMenuItem> findDairyFreeItems();

    @Query("SELECT i FROM CafeteriaMenuItem i WHERE i.nutFree = true AND i.available = true")
    List<CafeteriaMenuItem> findNutFreeItems();

    @Query("SELECT i FROM CafeteriaMenuItem i WHERE LOWER(i.itemName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND i.available = true")
    List<CafeteriaMenuItem> searchAvailableItems(@Param("searchTerm") String searchTerm);
}
