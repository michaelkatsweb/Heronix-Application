package com.heronix.service;

import com.heronix.model.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Cafeteria/Meal Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
public interface CafeteriaManagementService {

    // Meal Plan Management
    MealPlan createMealPlan(MealPlan mealPlan);
    MealPlan updateMealPlan(Long planId, MealPlan mealPlan);
    void deleteMealPlan(Long planId);
    MealPlan getMealPlanById(Long planId);
    MealPlan getMealPlanByName(String planName);
    List<MealPlan> getAllMealPlans();
    List<MealPlan> getActiveMealPlans();
    List<MealPlan> getMealPlansByYear(String academicYear);
    List<MealPlan> getMealPlansByType(MealPlan.PlanType planType);

    // Student Account Management
    StudentMealAccount createStudentAccount(Long studentId, String academicYear);
    StudentMealAccount updateStudentAccount(Long accountId, StudentMealAccount account);
    StudentMealAccount assignMealPlan(Long accountId, Long mealPlanId);
    StudentMealAccount addBalance(Long accountId, BigDecimal amount, String paymentMethod);
    StudentMealAccount setAutoReload(Long accountId, boolean enabled, BigDecimal threshold, BigDecimal amount);
    StudentMealAccount updateEligibility(Long accountId, StudentMealAccount.EligibilityStatus status);
    StudentMealAccount suspendAccount(Long accountId, String reason);
    StudentMealAccount activateAccount(Long accountId);
    StudentMealAccount getAccountById(Long accountId);
    StudentMealAccount getAccountByStudent(Long studentId, String academicYear);
    List<StudentMealAccount> getAccountsByYear(String academicYear);
    List<StudentMealAccount> getActiveAccountsByYear(String academicYear);
    List<StudentMealAccount> getAccountsWithLowBalance(BigDecimal threshold);
    List<StudentMealAccount> getAccountsNeedingReload();
    List<StudentMealAccount> getAccountsPendingVerification();

    // Transaction Management
    MealTransaction recordPurchase(Long studentId, MealTransaction.MealType mealType,
                                   BigDecimal amount, String menuItem, String cashierName);
    MealTransaction recordDeposit(Long accountId, BigDecimal amount, String paymentMethod, String referenceNumber);
    MealTransaction recordRefund(Long accountId, BigDecimal amount, String reason);
    MealTransaction getTransactionById(Long transactionId);
    List<MealTransaction> getTransactionsByStudent(Long studentId);
    List<MealTransaction> getTransactionsByAccount(Long accountId);
    List<MealTransaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<MealTransaction> getTransactionsByDate(LocalDate date);
    List<MealTransaction> getStudentTransactionsByDateRange(Long studentId, LocalDateTime startDate, LocalDateTime endDate);

    // Menu Management
    DailyMenu createMenu(DailyMenu menu);
    DailyMenu updateMenu(Long menuId, DailyMenu menu);
    void deleteMenu(Long menuId);
    DailyMenu publishMenu(Long menuId);
    DailyMenu unpublishMenu(Long menuId);
    DailyMenu getMenuById(Long menuId);
    DailyMenu getMenuByDateAndPeriod(LocalDate date, DailyMenu.MealPeriod period);
    List<DailyMenu> getMenusByDate(LocalDate date);
    List<DailyMenu> getMenusByDateRange(LocalDate startDate, LocalDate endDate);
    List<DailyMenu> getPublishedMenusFromDate(LocalDate date);
    List<DailyMenu> getUnpublishedMenus();

    // Menu Item Management
    CafeteriaMenuItem addMenuItem(Long menuId, CafeteriaMenuItem item);
    CafeteriaMenuItem updateMenuItem(Long itemId, CafeteriaMenuItem item);
    void deleteMenuItem(Long itemId);
    CafeteriaMenuItem getMenuItemById(Long itemId);
    List<CafeteriaMenuItem> getItemsByMenu(Long menuId);
    List<CafeteriaMenuItem> getAvailableItemsByMenu(Long menuId);
    List<CafeteriaMenuItem> getVegetarianItems();
    List<CafeteriaMenuItem> getGlutenFreeItems();
    List<CafeteriaMenuItem> searchMenuItems(String searchTerm);

    // Reporting and Statistics
    Map<String, Object> getDailyStatistics(LocalDate date);
    Map<String, Object> getWeeklyStatistics(LocalDate weekStart);
    Map<String, Object> getMonthlyStatistics(int year, int month);
    Map<String, Object> getAccountStatistics(Long accountId);
    long getMealsServedByDate(LocalDate date);
    long getMealsByTypeAndDate(MealTransaction.MealType mealType, LocalDate date);
    BigDecimal getTotalRevenueByDate(LocalDate date);
    BigDecimal getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    Map<String, Object> getEligibilityStatistics(String academicYear);
}
