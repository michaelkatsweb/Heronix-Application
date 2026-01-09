package com.heronix.controller.api;

import com.heronix.model.domain.*;
import com.heronix.service.CafeteriaManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Cafeteria and Meal Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/cafeteria")
@RequiredArgsConstructor
public class CafeteriaManagementApiController {

    private final CafeteriaManagementService cafeteriaService;

    // ==================== Meal Plan Management ====================

    @PostMapping("/meal-plans")
    public ResponseEntity<MealPlan> createMealPlan(@RequestBody MealPlan mealPlan) {
        MealPlan created = cafeteriaService.createMealPlan(mealPlan);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/meal-plans/{id}")
    public ResponseEntity<MealPlan> updateMealPlan(
            @PathVariable Long id,
            @RequestBody MealPlan mealPlan) {
        MealPlan updated = cafeteriaService.updateMealPlan(id, mealPlan);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/meal-plans/{id}")
    public ResponseEntity<MealPlan> getMealPlanById(@PathVariable Long id) {
        MealPlan plan = cafeteriaService.getMealPlanById(id);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/meal-plans")
    public ResponseEntity<List<MealPlan>> getAllMealPlans() {
        List<MealPlan> plans = cafeteriaService.getAllMealPlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/meal-plans/active")
    public ResponseEntity<List<MealPlan>> getActiveMealPlans() {
        List<MealPlan> plans = cafeteriaService.getActiveMealPlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/meal-plans/by-type/{planType}")
    public ResponseEntity<List<MealPlan>> getMealPlansByType(
            @PathVariable MealPlan.PlanType planType) {
        List<MealPlan> plans = cafeteriaService.getMealPlansByType(planType);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/meal-plans/year/{year}")
    public ResponseEntity<List<MealPlan>> getMealPlansByYear(@PathVariable String year) {
        List<MealPlan> plans = cafeteriaService.getMealPlansByYear(year);
        return ResponseEntity.ok(plans);
    }

    @DeleteMapping("/meal-plans/{id}")
    public ResponseEntity<Void> deleteMealPlan(@PathVariable Long id) {
        cafeteriaService.deleteMealPlan(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Student Account Management ====================

    @PostMapping("/accounts/student/{studentId}/year/{academicYear}")
    public ResponseEntity<StudentMealAccount> createStudentAccount(
            @PathVariable Long studentId,
            @PathVariable String academicYear) {
        StudentMealAccount account = cafeteriaService.createStudentAccount(studentId, academicYear);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PutMapping("/accounts/{id}")
    public ResponseEntity<StudentMealAccount> updateStudentAccount(
            @PathVariable Long id,
            @RequestBody StudentMealAccount account) {
        StudentMealAccount updated = cafeteriaService.updateStudentAccount(id, account);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<StudentMealAccount> getAccountById(@PathVariable Long id) {
        StudentMealAccount account = cafeteriaService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/accounts/student/{studentId}/year/{academicYear}")
    public ResponseEntity<StudentMealAccount> getAccountByStudent(
            @PathVariable Long studentId,
            @PathVariable String academicYear) {
        StudentMealAccount account = cafeteriaService.getAccountByStudent(studentId, academicYear);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/accounts/year/{academicYear}")
    public ResponseEntity<List<StudentMealAccount>> getAccountsByYear(
            @PathVariable String academicYear) {
        List<StudentMealAccount> accounts = cafeteriaService.getAccountsByYear(academicYear);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/active/year/{academicYear}")
    public ResponseEntity<List<StudentMealAccount>> getActiveAccountsByYear(
            @PathVariable String academicYear) {
        List<StudentMealAccount> accounts = cafeteriaService.getActiveAccountsByYear(academicYear);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/low-balance")
    public ResponseEntity<List<StudentMealAccount>> getAccountsWithLowBalance(
            @RequestParam BigDecimal threshold) {
        List<StudentMealAccount> accounts = cafeteriaService.getAccountsWithLowBalance(threshold);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/needs-reload")
    public ResponseEntity<List<StudentMealAccount>> getAccountsNeedingReload() {
        List<StudentMealAccount> accounts = cafeteriaService.getAccountsNeedingReload();
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/accounts/{id}/balance")
    public ResponseEntity<StudentMealAccount> addBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam String paymentMethod) {
        StudentMealAccount account = cafeteriaService.addBalance(id, amount, paymentMethod);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/accounts/{id}/assign-plan/{planId}")
    public ResponseEntity<StudentMealAccount> assignMealPlan(
            @PathVariable Long id,
            @PathVariable Long planId) {
        StudentMealAccount account = cafeteriaService.assignMealPlan(id, planId);
        return ResponseEntity.ok(account);
    }

    @PatchMapping("/accounts/{id}/eligibility/{status}")
    public ResponseEntity<StudentMealAccount> updateEligibility(
            @PathVariable Long id,
            @PathVariable StudentMealAccount.EligibilityStatus status) {
        StudentMealAccount account = cafeteriaService.updateEligibility(id, status);
        return ResponseEntity.ok(account);
    }

    @PatchMapping("/accounts/{id}/auto-reload")
    public ResponseEntity<StudentMealAccount> setAutoReload(
            @PathVariable Long id,
            @RequestParam boolean enabled,
            @RequestParam(required = false) BigDecimal threshold,
            @RequestParam(required = false) BigDecimal amount) {
        StudentMealAccount account = cafeteriaService.setAutoReload(id, enabled, threshold, amount);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/accounts/{id}/suspend")
    public ResponseEntity<StudentMealAccount> suspendAccount(
            @PathVariable Long id,
            @RequestParam String reason) {
        StudentMealAccount account = cafeteriaService.suspendAccount(id, reason);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/accounts/{id}/activate")
    public ResponseEntity<StudentMealAccount> activateAccount(@PathVariable Long id) {
        StudentMealAccount account = cafeteriaService.activateAccount(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/accounts/pending-verification")
    public ResponseEntity<List<StudentMealAccount>> getAccountsPendingVerification() {
        List<StudentMealAccount> accounts = cafeteriaService.getAccountsPendingVerification();
        return ResponseEntity.ok(accounts);
    }

    // ==================== Transaction Management ====================

    @PostMapping("/transactions/purchase")
    public ResponseEntity<MealTransaction> recordPurchase(
            @RequestParam Long studentId,
            @RequestParam MealTransaction.MealType mealType,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String menuItem,
            @RequestParam(required = false) String cashierName) {
        MealTransaction transaction = cafeteriaService.recordPurchase(
                studentId, mealType, amount, menuItem, cashierName);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<MealTransaction> getTransactionById(@PathVariable Long id) {
        MealTransaction transaction = cafeteriaService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/transactions/student/{studentId}")
    public ResponseEntity<List<MealTransaction>> getTransactionsByStudent(
            @PathVariable Long studentId) {
        List<MealTransaction> transactions = cafeteriaService.getTransactionsByStudent(studentId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/account/{accountId}")
    public ResponseEntity<List<MealTransaction>> getTransactionsByAccount(
            @PathVariable Long accountId) {
        List<MealTransaction> transactions = cafeteriaService.getTransactionsByAccount(accountId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/date/{date}")
    public ResponseEntity<List<MealTransaction>> getTransactionsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<MealTransaction> transactions = cafeteriaService.getTransactionsByDate(date);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/date-range")
    public ResponseEntity<List<MealTransaction>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<MealTransaction> transactions = cafeteriaService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/student/{studentId}/date-range")
    public ResponseEntity<List<MealTransaction>> getStudentTransactionsByDateRange(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<MealTransaction> transactions = cafeteriaService.getStudentTransactionsByDateRange(studentId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/transactions/deposit")
    public ResponseEntity<MealTransaction> recordDeposit(
            @RequestParam Long accountId,
            @RequestParam BigDecimal amount,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String referenceNumber) {
        MealTransaction transaction = cafeteriaService.recordDeposit(accountId, amount, paymentMethod, referenceNumber);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/transactions/refund")
    public ResponseEntity<MealTransaction> recordRefund(
            @RequestParam Long accountId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason) {
        MealTransaction transaction = cafeteriaService.recordRefund(accountId, amount, reason);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    // ==================== Menu Management ====================

    @PostMapping("/menus")
    public ResponseEntity<DailyMenu> createMenu(@RequestBody DailyMenu menu) {
        DailyMenu created = cafeteriaService.createMenu(menu);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/menus/{id}")
    public ResponseEntity<DailyMenu> updateMenu(
            @PathVariable Long id,
            @RequestBody DailyMenu menu) {
        DailyMenu updated = cafeteriaService.updateMenu(id, menu);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/menus/{id}")
    public ResponseEntity<DailyMenu> getMenuById(@PathVariable Long id) {
        DailyMenu menu = cafeteriaService.getMenuById(id);
        return ResponseEntity.ok(menu);
    }

    @GetMapping("/menus/date/{date}/period/{period}")
    public ResponseEntity<DailyMenu> getMenuByDateAndPeriod(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable DailyMenu.MealPeriod period) {
        DailyMenu menu = cafeteriaService.getMenuByDateAndPeriod(date, period);
        return ResponseEntity.ok(menu);
    }

    @GetMapping("/menus/date/{date}")
    public ResponseEntity<List<DailyMenu>> getMenusByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DailyMenu> menus = cafeteriaService.getMenusByDate(date);
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/menus/date-range")
    public ResponseEntity<List<DailyMenu>> getMenusByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailyMenu> menus = cafeteriaService.getMenusByDateRange(startDate, endDate);
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/menus/published")
    public ResponseEntity<List<DailyMenu>> getPublishedMenusFromDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate) {
        List<DailyMenu> menus = cafeteriaService.getPublishedMenusFromDate(fromDate);
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/menus/unpublished")
    public ResponseEntity<List<DailyMenu>> getUnpublishedMenus() {
        List<DailyMenu> menus = cafeteriaService.getUnpublishedMenus();
        return ResponseEntity.ok(menus);
    }

    @PatchMapping("/menus/{id}/publish")
    public ResponseEntity<DailyMenu> publishMenu(@PathVariable Long id) {
        DailyMenu menu = cafeteriaService.publishMenu(id);
        return ResponseEntity.ok(menu);
    }

    @PatchMapping("/menus/{id}/unpublish")
    public ResponseEntity<DailyMenu> unpublishMenu(@PathVariable Long id) {
        DailyMenu menu = cafeteriaService.unpublishMenu(id);
        return ResponseEntity.ok(menu);
    }

    @DeleteMapping("/menus/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        cafeteriaService.deleteMenu(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Menu Item Management ====================

    @PostMapping("/menu-items/menu/{menuId}")
    public ResponseEntity<CafeteriaMenuItem> addMenuItem(
            @PathVariable Long menuId,
            @RequestBody CafeteriaMenuItem item) {
        CafeteriaMenuItem created = cafeteriaService.addMenuItem(menuId, item);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/menu-items/{id}")
    public ResponseEntity<CafeteriaMenuItem> updateMenuItem(
            @PathVariable Long id,
            @RequestBody CafeteriaMenuItem item) {
        CafeteriaMenuItem updated = cafeteriaService.updateMenuItem(id, item);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/menu-items/{id}")
    public ResponseEntity<CafeteriaMenuItem> getMenuItemById(@PathVariable Long id) {
        CafeteriaMenuItem item = cafeteriaService.getMenuItemById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/menu-items/menu/{menuId}")
    public ResponseEntity<List<CafeteriaMenuItem>> getItemsByMenu(@PathVariable Long menuId) {
        List<CafeteriaMenuItem> items = cafeteriaService.getItemsByMenu(menuId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/menu-items/available/menu/{menuId}")
    public ResponseEntity<List<CafeteriaMenuItem>> getAvailableItemsByMenu(@PathVariable Long menuId) {
        List<CafeteriaMenuItem> items = cafeteriaService.getAvailableItemsByMenu(menuId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/menu-items/dietary/vegetarian")
    public ResponseEntity<List<CafeteriaMenuItem>> getVegetarianItems() {
        List<CafeteriaMenuItem> items = cafeteriaService.getVegetarianItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/menu-items/dietary/gluten-free")
    public ResponseEntity<List<CafeteriaMenuItem>> getGlutenFreeItems() {
        List<CafeteriaMenuItem> items = cafeteriaService.getGlutenFreeItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/menu-items/search")
    public ResponseEntity<List<CafeteriaMenuItem>> searchMenuItems(@RequestParam String searchTerm) {
        List<CafeteriaMenuItem> items = cafeteriaService.searchMenuItems(searchTerm);
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/menu-items/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        cafeteriaService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Reporting and Statistics ====================

    @GetMapping("/statistics/daily/{date}")
    public ResponseEntity<Map<String, Object>> getDailyStatistics(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> stats = cafeteriaService.getDailyStatistics(date);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics/weekly/{startDate}")
    public ResponseEntity<Map<String, Object>> getWeeklyStatistics(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        Map<String, Object> stats = cafeteriaService.getWeeklyStatistics(startDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics/monthly/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getMonthlyStatistics(
            @PathVariable int year,
            @PathVariable int month) {
        Map<String, Object> stats = cafeteriaService.getMonthlyStatistics(year, month);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics/eligibility/{academicYear}")
    public ResponseEntity<Map<String, Object>> getEligibilityStatistics(
            @PathVariable String academicYear) {
        Map<String, Object> stats = cafeteriaService.getEligibilityStatistics(academicYear);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics/revenue/date/{date}")
    public ResponseEntity<BigDecimal> getTotalRevenueByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        BigDecimal revenue = cafeteriaService.getTotalRevenueByDate(date);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/statistics/revenue/date-range")
    public ResponseEntity<BigDecimal> getTotalRevenueByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        BigDecimal revenue = cafeteriaService.getTotalRevenueByDateRange(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/statistics/meals/date/{date}")
    public ResponseEntity<Long> getMealsServedByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        long count = cafeteriaService.getMealsServedByDate(date);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/meals/type/{mealType}/date/{date}")
    public ResponseEntity<Long> getMealsByTypeAndDate(
            @PathVariable MealTransaction.MealType mealType,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        long count = cafeteriaService.getMealsByTypeAndDate(mealType, date);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/account/{accountId}")
    public ResponseEntity<Map<String, Object>> getAccountStatistics(@PathVariable Long accountId) {
        Map<String, Object> stats = cafeteriaService.getAccountStatistics(accountId);
        return ResponseEntity.ok(stats);
    }
}
