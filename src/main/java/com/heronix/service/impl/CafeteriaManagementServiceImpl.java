package com.heronix.service.impl;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import com.heronix.service.CafeteriaManagementService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of CafeteriaManagementService
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Service
@RequiredArgsConstructor
public class CafeteriaManagementServiceImpl implements CafeteriaManagementService {

    private final MealPlanRepository mealPlanRepository;
    private final StudentMealAccountRepository accountRepository;
    private final MealTransactionRepository transactionRepository;
    private final DailyMenuRepository menuRepository;
    private final CafeteriaMenuItemRepository menuItemRepository;
    private final StudentRepository studentRepository;

    // ========== Meal Plan Management ==========

    @Override
    @Transactional
    public MealPlan createMealPlan(MealPlan mealPlan) {
        mealPlan.setActive(true);
        return mealPlanRepository.save(mealPlan);
    }

    @Override
    @Transactional
    public MealPlan updateMealPlan(Long planId, MealPlan mealPlan) {
        MealPlan existing = getMealPlanById(planId);
        mealPlan.setId(existing.getId());
        return mealPlanRepository.save(mealPlan);
    }

    @Override
    @Transactional
    public void deleteMealPlan(Long planId) {
        MealPlan plan = getMealPlanById(planId);
        plan.setActive(false);
        mealPlanRepository.save(plan);
    }

    @Override
    public MealPlan getMealPlanById(Long planId) {
        return mealPlanRepository.findById(planId)
            .orElseThrow(() -> new EntityNotFoundException("Meal plan not found with id: " + planId));
    }

    @Override
    public MealPlan getMealPlanByName(String planName) {
        return mealPlanRepository.findByPlanName(planName)
            .orElseThrow(() -> new EntityNotFoundException("Meal plan not found with name: " + planName));
    }

    @Override
    public List<MealPlan> getAllMealPlans() {
        return mealPlanRepository.findAll();
    }

    @Override
    public List<MealPlan> getActiveMealPlans() {
        return mealPlanRepository.findByActiveTrueOrderByPlanNameAsc();
    }

    @Override
    public List<MealPlan> getMealPlansByYear(String academicYear) {
        return mealPlanRepository.findByAcademicYearAndActiveTrue(academicYear);
    }

    @Override
    public List<MealPlan> getMealPlansByType(MealPlan.PlanType planType) {
        return mealPlanRepository.findByPlanType(planType);
    }

    // ========== Student Account Management ==========

    @Override
    @Transactional
    public StudentMealAccount createStudentAccount(Long studentId, String academicYear) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));

        // Check if account already exists
        accountRepository.findByStudentIdAndAcademicYear(studentId, academicYear)
            .ifPresent(acc -> {
                throw new IllegalStateException("Account already exists for this student and year");
            });

        StudentMealAccount account = StudentMealAccount.builder()
            .student(student)
            .academicYear(academicYear)
            .balance(BigDecimal.ZERO)
            .status(StudentMealAccount.AccountStatus.ACTIVE)
            .eligibilityStatus(StudentMealAccount.EligibilityStatus.PENDING_VERIFICATION)
            .autoReload(false)
            .build();

        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public StudentMealAccount updateStudentAccount(Long accountId, StudentMealAccount account) {
        StudentMealAccount existing = getAccountById(accountId);
        account.setId(existing.getId());
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public StudentMealAccount assignMealPlan(Long accountId, Long mealPlanId) {
        StudentMealAccount account = getAccountById(accountId);
        MealPlan plan = getMealPlanById(mealPlanId);

        account.setMealPlan(plan);
        account.setPlanStartDate(LocalDate.now());

        // Set plan end date to end of academic year
        account.setPlanEndDate(LocalDate.now().withMonth(6).withDayOfMonth(30));

        // Set meals remaining if applicable
        if (plan.getMealsPerWeek() != null && plan.getMealsPerWeek() > 0) {
            // Calculate remaining weeks in year
            int weeksRemaining = 40; // Approximate school year weeks
            account.setMealsRemaining(plan.getMealsPerWeek() * weeksRemaining);
        }

        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public StudentMealAccount addBalance(Long accountId, BigDecimal amount, String paymentMethod) {
        StudentMealAccount account = getAccountById(accountId);

        BigDecimal balanceBefore = account.getBalance();
        account.addBalance(amount);

        // Record deposit transaction
        MealTransaction transaction = MealTransaction.builder()
            .student(account.getStudent())
            .account(account)
            .transactionType(MealTransaction.TransactionType.DEPOSIT)
            .mealType(MealTransaction.MealType.OTHER)
            .amount(amount)
            .balanceBefore(balanceBefore)
            .balanceAfter(account.getBalance())
            .paymentMethod(MealTransaction.PaymentMethod.valueOf(paymentMethod.toUpperCase()))
            .transactionDate(LocalDateTime.now())
            .build();

        transactionRepository.save(transaction);

        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public StudentMealAccount setAutoReload(Long accountId, boolean enabled,
                                           BigDecimal threshold, BigDecimal amount) {
        StudentMealAccount account = getAccountById(accountId);
        account.setAutoReload(enabled);
        account.setReloadThreshold(threshold);
        account.setReloadAmount(amount);
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public StudentMealAccount updateEligibility(Long accountId,
                                               StudentMealAccount.EligibilityStatus status) {
        StudentMealAccount account = getAccountById(accountId);
        account.setEligibilityStatus(status);
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public StudentMealAccount suspendAccount(Long accountId, String reason) {
        StudentMealAccount account = getAccountById(accountId);
        account.setStatus(StudentMealAccount.AccountStatus.SUSPENDED);
        account.setNotes(reason);
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public StudentMealAccount activateAccount(Long accountId) {
        StudentMealAccount account = getAccountById(accountId);
        account.setStatus(StudentMealAccount.AccountStatus.ACTIVE);
        return accountRepository.save(account);
    }

    @Override
    public StudentMealAccount getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + accountId));
    }

    @Override
    public StudentMealAccount getAccountByStudent(Long studentId, String academicYear) {
        return accountRepository.findByStudentIdAndAcademicYear(studentId, academicYear)
            .orElseThrow(() -> new EntityNotFoundException(
                "No account found for student " + studentId + " in year " + academicYear));
    }

    @Override
    public List<StudentMealAccount> getAccountsByYear(String academicYear) {
        return accountRepository.findByAcademicYear(academicYear);
    }

    @Override
    public List<StudentMealAccount> getActiveAccountsByYear(String academicYear) {
        return accountRepository.findActiveAccountsByYear(academicYear);
    }

    @Override
    public List<StudentMealAccount> getAccountsWithLowBalance(BigDecimal threshold) {
        return accountRepository.findAccountsWithLowBalance(threshold);
    }

    @Override
    public List<StudentMealAccount> getAccountsNeedingReload() {
        return accountRepository.findAccountsNeedingReload();
    }

    @Override
    public List<StudentMealAccount> getAccountsPendingVerification() {
        return accountRepository.findAccountsPendingVerification();
    }

    // ========== Transaction Management ==========

    @Override
    @Transactional
    public MealTransaction recordPurchase(Long studentId, MealTransaction.MealType mealType,
                                         BigDecimal amount, String menuItem, String cashierName) {
        // Find student's active account
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));

        String currentYear = getCurrentAcademicYear();
        StudentMealAccount account = getAccountByStudent(studentId, currentYear);

        if (!account.canPurchase(amount)) {
            throw new IllegalStateException("Insufficient balance or account not active");
        }

        BigDecimal balanceBefore = account.getBalance();
        account.deductBalance(amount);

        MealTransaction transaction = MealTransaction.builder()
            .student(student)
            .account(account)
            .transactionType(MealTransaction.TransactionType.PURCHASE)
            .mealType(mealType)
            .amount(amount)
            .balanceBefore(balanceBefore)
            .balanceAfter(account.getBalance())
            .menuItem(menuItem)
            .cashierName(cashierName)
            .paymentMethod(MealTransaction.PaymentMethod.ACCOUNT_BALANCE)
            .transactionDate(LocalDateTime.now())
            .build();

        accountRepository.save(account);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public MealTransaction recordDeposit(Long accountId, BigDecimal amount,
                                        String paymentMethod, String referenceNumber) {
        StudentMealAccount account = getAccountById(accountId);

        BigDecimal balanceBefore = account.getBalance();
        account.addBalance(amount);

        MealTransaction transaction = MealTransaction.builder()
            .student(account.getStudent())
            .account(account)
            .transactionType(MealTransaction.TransactionType.DEPOSIT)
            .mealType(MealTransaction.MealType.OTHER)
            .amount(amount)
            .balanceBefore(balanceBefore)
            .balanceAfter(account.getBalance())
            .paymentMethod(MealTransaction.PaymentMethod.valueOf(paymentMethod.toUpperCase()))
            .referenceNumber(referenceNumber)
            .transactionDate(LocalDateTime.now())
            .build();

        accountRepository.save(account);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public MealTransaction recordRefund(Long accountId, BigDecimal amount, String reason) {
        StudentMealAccount account = getAccountById(accountId);

        BigDecimal balanceBefore = account.getBalance();
        account.addBalance(amount);

        MealTransaction transaction = MealTransaction.builder()
            .student(account.getStudent())
            .account(account)
            .transactionType(MealTransaction.TransactionType.REFUND)
            .mealType(MealTransaction.MealType.OTHER)
            .amount(amount)
            .balanceBefore(balanceBefore)
            .balanceAfter(account.getBalance())
            .notes(reason)
            .transactionDate(LocalDateTime.now())
            .build();

        accountRepository.save(account);
        return transactionRepository.save(transaction);
    }

    @Override
    public MealTransaction getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));
    }

    @Override
    public List<MealTransaction> getTransactionsByStudent(Long studentId) {
        return transactionRepository.findByStudentIdOrderByDateDesc(studentId);
    }

    @Override
    public List<MealTransaction> getTransactionsByAccount(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    @Override
    public List<MealTransaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public List<MealTransaction> getTransactionsByDate(LocalDate date) {
        return transactionRepository.findByDate(date);
    }

    @Override
    public List<MealTransaction> getStudentTransactionsByDateRange(Long studentId,
                                                                   LocalDateTime startDate,
                                                                   LocalDateTime endDate) {
        return transactionRepository.findByStudentAndDateRange(studentId, startDate, endDate);
    }

    // ========== Menu Management ==========

    @Override
    @Transactional
    public DailyMenu createMenu(DailyMenu menu) {
        menu.setPublished(false);
        return menuRepository.save(menu);
    }

    @Override
    @Transactional
    public DailyMenu updateMenu(Long menuId, DailyMenu menu) {
        DailyMenu existing = getMenuById(menuId);
        menu.setId(existing.getId());
        return menuRepository.save(menu);
    }

    @Override
    @Transactional
    public void deleteMenu(Long menuId) {
        menuRepository.deleteById(menuId);
    }

    @Override
    @Transactional
    public DailyMenu publishMenu(Long menuId) {
        DailyMenu menu = getMenuById(menuId);
        menu.setPublished(true);
        return menuRepository.save(menu);
    }

    @Override
    @Transactional
    public DailyMenu unpublishMenu(Long menuId) {
        DailyMenu menu = getMenuById(menuId);
        menu.setPublished(false);
        return menuRepository.save(menu);
    }

    @Override
    public DailyMenu getMenuById(Long menuId) {
        return menuRepository.findById(menuId)
            .orElseThrow(() -> new EntityNotFoundException("Menu not found with id: " + menuId));
    }

    @Override
    public DailyMenu getMenuByDateAndPeriod(LocalDate date, DailyMenu.MealPeriod period) {
        return menuRepository.findByMenuDateAndMealPeriod(date, period)
            .orElse(null);
    }

    @Override
    public List<DailyMenu> getMenusByDate(LocalDate date) {
        return menuRepository.findByMenuDate(date);
    }

    @Override
    public List<DailyMenu> getMenusByDateRange(LocalDate startDate, LocalDate endDate) {
        return menuRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public List<DailyMenu> getPublishedMenusFromDate(LocalDate date) {
        return menuRepository.findPublishedMenusFromDate(date);
    }

    @Override
    public List<DailyMenu> getUnpublishedMenus() {
        return menuRepository.findUnpublishedMenus();
    }

    // ========== Menu Item Management ==========

    @Override
    @Transactional
    public CafeteriaMenuItem addMenuItem(Long menuId, CafeteriaMenuItem item) {
        DailyMenu menu = getMenuById(menuId);
        item.setMenu(menu);
        item.setAvailable(true);
        return menuItemRepository.save(item);
    }

    @Override
    @Transactional
    public CafeteriaMenuItem updateMenuItem(Long itemId, CafeteriaMenuItem item) {
        CafeteriaMenuItem existing = getMenuItemById(itemId);
        item.setId(existing.getId());
        return menuItemRepository.save(item);
    }

    @Override
    @Transactional
    public void deleteMenuItem(Long itemId) {
        menuItemRepository.deleteById(itemId);
    }

    @Override
    public CafeteriaMenuItem getMenuItemById(Long itemId) {
        return menuItemRepository.findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException("Menu item not found with id: " + itemId));
    }

    @Override
    public List<CafeteriaMenuItem> getItemsByMenu(Long menuId) {
        return menuItemRepository.findByMenuId(menuId);
    }

    @Override
    public List<CafeteriaMenuItem> getAvailableItemsByMenu(Long menuId) {
        return menuItemRepository.findAvailableItemsByMenu(menuId);
    }

    @Override
    public List<CafeteriaMenuItem> getVegetarianItems() {
        return menuItemRepository.findVegetarianItems();
    }

    @Override
    public List<CafeteriaMenuItem> getGlutenFreeItems() {
        return menuItemRepository.findGlutenFreeItems();
    }

    @Override
    public List<CafeteriaMenuItem> searchMenuItems(String searchTerm) {
        return menuItemRepository.searchAvailableItems(searchTerm);
    }

    // ========== Reporting and Statistics ==========

    @Override
    public Map<String, Object> getDailyStatistics(LocalDate date) {
        Map<String, Object> stats = new HashMap<>();

        long totalMeals = transactionRepository.countMealsServedByDate(date);
        long breakfastCount = transactionRepository.countMealsByTypeAndDate(
            MealTransaction.MealType.BREAKFAST, date);
        long lunchCount = transactionRepository.countMealsByTypeAndDate(
            MealTransaction.MealType.LUNCH, date);
        long uniqueStudents = transactionRepository.countUniqueStudentsByDate(date);
        BigDecimal revenue = transactionRepository.getTotalRevenueByDate(date);

        stats.put("date", date);
        stats.put("totalMealsServed", totalMeals);
        stats.put("breakfastCount", breakfastCount);
        stats.put("lunchCount", lunchCount);
        stats.put("uniqueStudents", uniqueStudents);
        stats.put("totalRevenue", revenue);

        return stats;
    }

    @Override
    public Map<String, Object> getWeeklyStatistics(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDateTime startDateTime = weekStart.atStartOfDay();
        LocalDateTime endDateTime = weekEnd.atTime(23, 59, 59);

        Map<String, Object> stats = new HashMap<>();
        stats.put("weekStart", weekStart);
        stats.put("weekEnd", weekEnd);
        stats.put("totalRevenue", transactionRepository.getTotalRevenueByDateRange(startDateTime, endDateTime));

        return stats;
    }

    @Override
    public Map<String, Object> getMonthlyStatistics(int year, int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
        LocalDateTime startDateTime = monthStart.atStartOfDay();
        LocalDateTime endDateTime = monthEnd.atTime(23, 59, 59);

        Map<String, Object> stats = new HashMap<>();
        stats.put("year", year);
        stats.put("month", month);
        stats.put("totalRevenue", transactionRepository.getTotalRevenueByDateRange(startDateTime, endDateTime));

        return stats;
    }

    @Override
    public Map<String, Object> getAccountStatistics(Long accountId) {
        StudentMealAccount account = getAccountById(accountId);
        List<MealTransaction> transactions = transactionRepository.findByAccountId(accountId);

        BigDecimal totalSpent = transactions.stream()
            .filter(t -> t.getTransactionType() == MealTransaction.TransactionType.PURCHASE)
            .map(MealTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDeposited = transactions.stream()
            .filter(t -> t.getTransactionType() == MealTransaction.TransactionType.DEPOSIT)
            .map(MealTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("accountId", accountId);
        stats.put("currentBalance", account.getBalance());
        stats.put("totalSpent", totalSpent);
        stats.put("totalDeposited", totalDeposited);
        stats.put("transactionCount", transactions.size());
        stats.put("eligibilityStatus", account.getEligibilityStatus());

        return stats;
    }

    @Override
    public long getMealsServedByDate(LocalDate date) {
        return transactionRepository.countMealsServedByDate(date);
    }

    @Override
    public long getMealsByTypeAndDate(MealTransaction.MealType mealType, LocalDate date) {
        return transactionRepository.countMealsByTypeAndDate(mealType, date);
    }

    @Override
    public BigDecimal getTotalRevenueByDate(LocalDate date) {
        return transactionRepository.getTotalRevenueByDate(date);
    }

    @Override
    public BigDecimal getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getTotalRevenueByDateRange(startDate, endDate);
    }

    @Override
    public Map<String, Object> getEligibilityStatistics(String academicYear) {
        long totalAccounts = accountRepository.countActiveAccountsByYear(academicYear);
        long freeCount = accountRepository.countByYearAndEligibility(
            academicYear, StudentMealAccount.EligibilityStatus.FREE);
        long reducedCount = accountRepository.countByYearAndEligibility(
            academicYear, StudentMealAccount.EligibilityStatus.REDUCED_PRICE);
        long fullPriceCount = accountRepository.countByYearAndEligibility(
            academicYear, StudentMealAccount.EligibilityStatus.FULL_PRICE);

        Map<String, Object> stats = new HashMap<>();
        stats.put("academicYear", academicYear);
        stats.put("totalAccounts", totalAccounts);
        stats.put("freeEligible", freeCount);
        stats.put("reducedPrice", reducedCount);
        stats.put("fullPrice", fullPriceCount);
        stats.put("freePercentage", totalAccounts > 0 ? (freeCount * 100.0 / totalAccounts) : 0);
        stats.put("reducedPercentage", totalAccounts > 0 ? (reducedCount * 100.0 / totalAccounts) : 0);

        return stats;
    }

    private String getCurrentAcademicYear() {
        // Simple logic - can be enhanced
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        if (now.getMonthValue() >= 7) {
            return year + "-" + (year + 1);
        } else {
            return (year - 1) + "-" + year;
        }
    }
}
