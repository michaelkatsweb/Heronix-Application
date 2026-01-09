package com.heronix.ui.controller;

import com.heronix.model.domain.*;
import com.heronix.service.CafeteriaManagementService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * UI Controller for Cafeteria and Meal Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@Component
@RequiredArgsConstructor
public class CafeteriaMealManagementController {

    private final CafeteriaManagementService cafeteriaService;
    private static final String CURRENT_ACADEMIC_YEAR = "2024-2025";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    // Header Stats
    @FXML private Label mealsServedLabel;
    @FXML private Label freeReducedLabel;
    @FXML private Label revenueLabel;
    @FXML private Label activeAccountsLabel;
    @FXML private Label lowBalanceLabel;

    // Meal Plans Tab
    @FXML private TableView<MealPlan> mealPlansTableView;
    @FXML private TableColumn<MealPlan, String> planNameColumn;
    @FXML private TableColumn<MealPlan, String> planTypeColumn;
    @FXML private TableColumn<MealPlan, String> planPriceColumn;
    @FXML private TableColumn<MealPlan, String> planStudentsColumn;
    @FXML private TableColumn<MealPlan, String> planStatusColumn;
    @FXML private TextField planNameField;
    @FXML private ComboBox<MealPlan.PlanType> planTypeComboBox;
    @FXML private TextField dailyRateField;
    @FXML private TextField weeklyRateField;
    @FXML private TextField monthlyRateField;

    // Student Accounts Tab
    @FXML private TextField accountSearchField;
    @FXML private ComboBox<String> eligibilityFilterComboBox;
    @FXML private ComboBox<String> accountStatusFilterComboBox;
    @FXML private TableView<AccountDTO> accountsTableView;
    @FXML private TableColumn<AccountDTO, String> accountStudentColumn;
    @FXML private TableColumn<AccountDTO, String> accountBalanceColumn;
    @FXML private TableColumn<AccountDTO, String> accountPlanColumn;
    @FXML private TableColumn<AccountDTO, String> accountEligibilityColumn;
    @FXML private TableColumn<AccountDTO, String> accountStatusColumn;
    @FXML private TableColumn<AccountDTO, String> accountLastActivityColumn;
    @FXML private Label accountDetailNameLabel;
    @FXML private Label accountBalanceDetailLabel;
    @FXML private Label accountMealsRemainingLabel;
    @FXML private Label accountEligibilityLabel;
    @FXML private TextField addBalanceField;

    // Daily Menus Tab
    @FXML private DatePicker menuDatePicker;
    @FXML private ComboBox<DailyMenu.MealPeriod> mealPeriodComboBox;
    @FXML private TableView<MenuItemDTO> menuItemsTableView;
    @FXML private TableColumn<MenuItemDTO, String> itemNameColumn;
    @FXML private TableColumn<MenuItemDTO, String> itemCategoryColumn;
    @FXML private TableColumn<MenuItemDTO, String> itemPriceColumn;
    @FXML private TableColumn<MenuItemDTO, String> itemCaloriesColumn;
    @FXML private TableColumn<MenuItemDTO, String> itemAvailableColumn;
    @FXML private TextField itemNameField;
    @FXML private ComboBox<CafeteriaMenuItem.ItemCategory> itemCategoryComboBox;
    @FXML private TextField itemPriceField;
    @FXML private TextField itemCaloriesField;
    @FXML private CheckBox vegetarianCheckBox;
    @FXML private CheckBox veganCheckBox;
    @FXML private CheckBox glutenFreeCheckBox;
    @FXML private TableView menuTableView;

    // Transactions Tab
    @FXML private DatePicker transactionDatePicker;
    @FXML private ComboBox<MealTransaction.MealType> transactionTypeComboBox;
    @FXML private TableView<TransactionDTO> transactionsTableView;
    @FXML private TableColumn<TransactionDTO, String> transactionTimeColumn;
    @FXML private TableColumn<TransactionDTO, String> transactionStudentColumn;
    @FXML private TableColumn<TransactionDTO, String> transactionTypeColumn;
    @FXML private TableColumn<TransactionDTO, String> transactionAmountColumn;
    @FXML private TableColumn<TransactionDTO, String> transactionBalanceColumn;
    @FXML private Label dailyTransactionsLabel;
    @FXML private Label dailyRevenueLabel;

    // Reports Tab
    @FXML private DatePicker reportStartDatePicker;
    @FXML private DatePicker reportEndDatePicker;
    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private TextArea reportTextArea;

    // Footer
    @FXML private Label statusLabel;
    @FXML private Label lastUpdatedLabel;

    // Data
    private ObservableList<MealPlan> mealPlans = FXCollections.observableArrayList();
    private ObservableList<AccountDTO> accounts = FXCollections.observableArrayList();
    private ObservableList<MenuItemDTO> menuItems = FXCollections.observableArrayList();
    private ObservableList<TransactionDTO> transactions = FXCollections.observableArrayList();

    private DailyMenu currentMenu;
    private StudentMealAccount selectedAccount;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        loadInitialData();
        setupListeners();
        updateHeaderStats();
        updateLastUpdatedLabel();
    }

    private void setupTableColumns() {
        // Meal Plans Table
        if (planNameColumn != null) {
            planNameColumn.setCellValueFactory(new PropertyValueFactory<>("planName"));
            planTypeColumn.setCellValueFactory(new PropertyValueFactory<>("planType"));
            planPriceColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatPrice(cellData.getValue())));
            planStatusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                    Boolean.TRUE.equals(cellData.getValue().getActive()) ? "Active" : "Inactive"));
        }

        // Accounts Table
        if (accountStudentColumn != null) {
            accountStudentColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
            accountBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
            accountPlanColumn.setCellValueFactory(new PropertyValueFactory<>("planName"));
            accountEligibilityColumn.setCellValueFactory(new PropertyValueFactory<>("eligibility"));
            accountStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            accountLastActivityColumn.setCellValueFactory(new PropertyValueFactory<>("lastActivity"));
        }

        // Menu Items Table
        if (itemNameColumn != null) {
            itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
            itemCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            itemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
            itemCaloriesColumn.setCellValueFactory(new PropertyValueFactory<>("calories"));
            itemAvailableColumn.setCellValueFactory(new PropertyValueFactory<>("available"));
        }

        // Transactions Table
        if (transactionTimeColumn != null) {
            transactionTimeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
            transactionStudentColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
            transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            transactionBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        }
    }

    private void setupComboBoxes() {
        if (planTypeComboBox != null) {
            planTypeComboBox.setItems(FXCollections.observableArrayList(MealPlan.PlanType.values()));
        }

        if (mealPeriodComboBox != null) {
            mealPeriodComboBox.setItems(FXCollections.observableArrayList(DailyMenu.MealPeriod.values()));
        }

        if (itemCategoryComboBox != null) {
            itemCategoryComboBox.setItems(FXCollections.observableArrayList(CafeteriaMenuItem.ItemCategory.values()));
        }

        if (transactionTypeComboBox != null) {
            transactionTypeComboBox.setItems(FXCollections.observableArrayList(MealTransaction.MealType.values()));
        }

        if (eligibilityFilterComboBox != null) {
            eligibilityFilterComboBox.setItems(FXCollections.observableArrayList(
                "All", "Full Price", "Free", "Reduced Price", "Pending"));
        }

        if (accountStatusFilterComboBox != null) {
            accountStatusFilterComboBox.setItems(FXCollections.observableArrayList(
                "All", "Active", "Suspended", "Closed"));
        }

        if (reportTypeComboBox != null) {
            reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Daily Statistics", "Weekly Summary", "Monthly Report",
                "Eligibility Statistics", "Financial Summary"));
        }
    }

    private void setupListeners() {
        if (accountsTableView != null) {
            accountsTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadAccountDetails(newSelection);
                    }
                });
        }

        if (menuDatePicker != null && mealPeriodComboBox != null) {
            menuDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && mealPeriodComboBox.getValue() != null) {
                    loadMenuForDateAndPeriod(newVal, mealPeriodComboBox.getValue());
                }
            });

            mealPeriodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && menuDatePicker.getValue() != null) {
                    loadMenuForDateAndPeriod(menuDatePicker.getValue(), newVal);
                }
            });
        }

        if (transactionDatePicker != null) {
            transactionDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadTransactionsByDate(newVal);
                }
            });
        }
    }

    private void loadInitialData() {
        loadMealPlans();
        loadAccounts();

        if (menuDatePicker != null) {
            menuDatePicker.setValue(LocalDate.now());
        }

        if (mealPeriodComboBox != null) {
            mealPeriodComboBox.setValue(getCurrentMealPeriod());
        }

        if (transactionDatePicker != null) {
            transactionDatePicker.setValue(LocalDate.now());
            loadTransactionsByDate(LocalDate.now());
        }
    }

    // ==================== Meal Plans ====================

    @FXML
    private void handleCreateMealPlan() {
        try {
            if (planNameField == null || planTypeComboBox == null) return;

            MealPlan plan = MealPlan.builder()
                .planName(planNameField.getText())
                .planType(planTypeComboBox.getValue())
                .dailyRate(parseBigDecimal(dailyRateField != null ? dailyRateField.getText() : null))
                .weeklyRate(parseBigDecimal(weeklyRateField != null ? weeklyRateField.getText() : null))
                .monthlyRate(parseBigDecimal(monthlyRateField != null ? monthlyRateField.getText() : null))
                .academicYear(CURRENT_ACADEMIC_YEAR)
                .active(true)
                .build();

            cafeteriaService.createMealPlan(plan);
            loadMealPlans();
            clearPlanFields();
            showSuccess("Meal plan created successfully");
        } catch (Exception e) {
            showError("Error creating meal plan: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateMealPlan() {
        if (mealPlansTableView == null) return;
        MealPlan selected = mealPlansTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a meal plan to update");
            return;
        }

        try {
            if (planNameField != null) selected.setPlanName(planNameField.getText());
            if (planTypeComboBox != null) selected.setPlanType(planTypeComboBox.getValue());
            if (dailyRateField != null) selected.setDailyRate(parseBigDecimal(dailyRateField.getText()));
            if (weeklyRateField != null) selected.setWeeklyRate(parseBigDecimal(weeklyRateField.getText()));
            if (monthlyRateField != null) selected.setMonthlyRate(parseBigDecimal(monthlyRateField.getText()));

            cafeteriaService.updateMealPlan(selected.getId(), selected);
            loadMealPlans();
            showSuccess("Meal plan updated successfully");
        } catch (Exception e) {
            showError("Error updating meal plan: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteMealPlan() {
        if (mealPlansTableView == null) return;
        MealPlan selected = mealPlansTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a meal plan to delete");
            return;
        }

        if (showConfirmation("Delete Meal Plan", "Are you sure you want to delete this meal plan?")) {
            try {
                cafeteriaService.deleteMealPlan(selected.getId());
                loadMealPlans();
                showSuccess("Meal plan deleted successfully");
            } catch (Exception e) {
                showError("Error deleting meal plan: " + e.getMessage());
            }
        }
    }

    private void loadMealPlans() {
        new Thread(() -> {
            try {
                List<MealPlan> plans = cafeteriaService.getActiveMealPlans();
                Platform.runLater(() -> {
                    mealPlans.setAll(plans);
                    if (mealPlansTableView != null) {
                        mealPlansTableView.setItems(mealPlans);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error loading meal plans: " + e.getMessage()));
            }
        }).start();
    }

    // ==================== Student Accounts ====================

    @FXML
    private void handleCreateAccount() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Meal Account");
        dialog.setHeaderText("Enter Student ID");
        dialog.setContentText("Student ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(studentIdStr -> {
            try {
                Long studentId = Long.parseLong(studentIdStr);
                cafeteriaService.createStudentAccount(studentId, CURRENT_ACADEMIC_YEAR);
                loadAccounts();
                showSuccess("Account created successfully");
            } catch (NumberFormatException e) {
                showError("Invalid student ID format");
            } catch (Exception e) {
                showError("Error creating account: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleAddBalance() {
        if (selectedAccount == null) {
            showWarning("Please select an account");
            return;
        }

        try {
            if (addBalanceField == null) return;
            BigDecimal amount = parseBigDecimal(addBalanceField.getText());
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                showWarning("Amount must be greater than zero");
                return;
            }

            cafeteriaService.addBalance(selectedAccount.getId(), amount, "CASH");
            loadAccounts();
            addBalanceField.clear();
            showSuccess("Balance added successfully");
        } catch (Exception e) {
            showError("Error adding balance: " + e.getMessage());
        }
    }

    @FXML
    private void handleAssignPlan() {
        if (selectedAccount == null) {
            showWarning("Please select an account");
            return;
        }

        if (mealPlansTableView == null) return;
        MealPlan selectedPlan = mealPlansTableView.getSelectionModel().getSelectedItem();
        if (selectedPlan == null) {
            showWarning("Please select a meal plan from the Meal Plans tab");
            return;
        }

        try {
            cafeteriaService.assignMealPlan(selectedAccount.getId(), selectedPlan.getId());
            loadAccounts();
            showSuccess("Meal plan assigned successfully");
        } catch (Exception e) {
            showError("Error assigning meal plan: " + e.getMessage());
        }
    }

    @FXML
    private void handleSuspendAccount() {
        if (selectedAccount == null) {
            showWarning("Please select an account");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Suspend Account");
        dialog.setHeaderText("Enter reason for suspension");
        dialog.setContentText("Reason:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            try {
                cafeteriaService.suspendAccount(selectedAccount.getId(), reason);
                loadAccounts();
                showSuccess("Account suspended");
            } catch (Exception e) {
                showError("Error suspending account: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleActivateAccount() {
        if (selectedAccount == null) {
            showWarning("Please select an account");
            return;
        }

        try {
            cafeteriaService.activateAccount(selectedAccount.getId());
            loadAccounts();
            showSuccess("Account activated");
        } catch (Exception e) {
            showError("Error activating account: " + e.getMessage());
        }
    }

    private void loadAccounts() {
        new Thread(() -> {
            try {
                List<StudentMealAccount> accountList = cafeteriaService.getAccountsByYear(CURRENT_ACADEMIC_YEAR);
                Platform.runLater(() -> {
                    accounts.clear();
                    for (StudentMealAccount account : accountList) {
                        accounts.add(new AccountDTO(account));
                    }
                    if (accountsTableView != null) {
                        accountsTableView.setItems(accounts);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error loading accounts: " + e.getMessage()));
            }
        }).start();
    }

    private void loadAccountDetails(AccountDTO accountDTO) {
        new Thread(() -> {
            try {
                selectedAccount = cafeteriaService.getAccountById(accountDTO.getAccountId());
                Platform.runLater(() -> {
                    if (accountDetailNameLabel != null) {
                        accountDetailNameLabel.setText(accountDTO.getStudentName());
                    }
                    if (accountBalanceDetailLabel != null) {
                        accountBalanceDetailLabel.setText(formatCurrency(selectedAccount.getBalance()));
                    }
                    if (accountMealsRemainingLabel != null) {
                        Integer meals = selectedAccount.getMealsRemaining();
                        accountMealsRemainingLabel.setText(meals != null ? meals.toString() : "Unlimited");
                    }
                    if (accountEligibilityLabel != null) {
                        accountEligibilityLabel.setText(selectedAccount.getEligibilityStatus().toString());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error loading account details: " + e.getMessage()));
            }
        }).start();
    }

    // ==================== Daily Menus ====================

    @FXML
    private void handleCreateMenu() {
        if (menuDatePicker == null || mealPeriodComboBox == null) {
            showWarning("Menu controls not initialized");
            return;
        }

        LocalDate date = menuDatePicker.getValue();
        DailyMenu.MealPeriod period = mealPeriodComboBox.getValue();

        if (date == null || period == null) {
            showWarning("Please select date and meal period");
            return;
        }

        try {
            DailyMenu menu = DailyMenu.builder()
                .menuDate(date)
                .mealPeriod(period)
                .published(false)
                .vegetarianOptions(false)
                .glutenFreeOptions(false)
                .dairyFreeOptions(false)
                .build();

            currentMenu = cafeteriaService.createMenu(menu);
            showSuccess("Menu created successfully");
        } catch (Exception e) {
            showError("Error creating menu: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddMenuItem() {
        if (currentMenu == null) {
            showWarning("Please create or select a menu first");
            return;
        }

        try {
            if (itemNameField == null || itemCategoryComboBox == null) return;

            CafeteriaMenuItem item = CafeteriaMenuItem.builder()
                .itemName(itemNameField.getText())
                .category(itemCategoryComboBox.getValue())
                .price(parseBigDecimal(itemPriceField != null ? itemPriceField.getText() : null))
                .calories(itemCaloriesField != null ? Integer.parseInt(itemCaloriesField.getText()) : null)
                .vegetarian(vegetarianCheckBox != null && vegetarianCheckBox.isSelected())
                .vegan(veganCheckBox != null && veganCheckBox.isSelected())
                .glutenFree(glutenFreeCheckBox != null && glutenFreeCheckBox.isSelected())
                .available(true)
                .build();

            cafeteriaService.addMenuItem(currentMenu.getId(), item);
            loadMenuItems();
            clearMenuItemFields();
            showSuccess("Menu item added successfully");
        } catch (Exception e) {
            showError("Error adding menu item: " + e.getMessage());
        }
    }

    @FXML
    private void handlePublishMenu() {
        if (currentMenu == null) {
            showWarning("Please select a menu to publish");
            return;
        }

        try {
            cafeteriaService.publishMenu(currentMenu.getId());
            showSuccess("Menu published successfully");
        } catch (Exception e) {
            showError("Error publishing menu: " + e.getMessage());
        }
    }

    private void loadMenuForDateAndPeriod(LocalDate date, DailyMenu.MealPeriod period) {
        new Thread(() -> {
            try {
                DailyMenu menu = cafeteriaService.getMenuByDateAndPeriod(date, period);
                Platform.runLater(() -> {
                    if (menu != null) {
                        currentMenu = menu;
                        loadMenuItems();
                    } else {
                        currentMenu = null;
                        menuItems.clear();
                        showInfo("No menu found for selected date and period");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    currentMenu = null;
                    menuItems.clear();
                });
            }
        }).start();
    }

    private void loadMenuItems() {
        if (currentMenu == null) return;

        new Thread(() -> {
            try {
                List<CafeteriaMenuItem> items = cafeteriaService.getItemsByMenu(currentMenu.getId());
                Platform.runLater(() -> {
                    menuItems.clear();
                    for (CafeteriaMenuItem item : items) {
                        menuItems.add(new MenuItemDTO(item));
                    }
                    if (menuItemsTableView != null) {
                        menuItemsTableView.setItems(menuItems);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error loading menu items: " + e.getMessage()));
            }
        }).start();
    }

    // ==================== Transactions ====================

    private void loadTransactionsByDate(LocalDate date) {
        new Thread(() -> {
            try {
                List<MealTransaction> transactionList = cafeteriaService.getTransactionsByDate(date);
                Map<String, Object> stats = cafeteriaService.getDailyStatistics(date);

                Platform.runLater(() -> {
                    transactions.clear();
                    for (MealTransaction transaction : transactionList) {
                        transactions.add(new TransactionDTO(transaction));
                    }
                    if (transactionsTableView != null) {
                        transactionsTableView.setItems(transactions);
                    }

                    if (dailyTransactionsLabel != null) {
                        dailyTransactionsLabel.setText(String.valueOf(stats.get("totalMealsServed")));
                    }
                    if (dailyRevenueLabel != null) {
                        dailyRevenueLabel.setText(formatCurrency((BigDecimal) stats.get("totalRevenue")));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error loading transactions: " + e.getMessage()));
            }
        }).start();
    }

    // ==================== Reports ====================

    @FXML
    private void handleGenerateReport() {
        if (reportTypeComboBox == null) return;
        String reportType = reportTypeComboBox.getValue();
        if (reportType == null) {
            showWarning("Please select a report type");
            return;
        }

        new Thread(() -> {
            try {
                String report = generateReport(reportType);
                Platform.runLater(() -> {
                    if (reportTextArea != null) {
                        reportTextArea.setText(report);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error generating report: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleMealCountReport() {
        if (reportTypeComboBox != null) {
            reportTypeComboBox.setValue("Daily Statistics");
        }
        handleGenerateReport();
    }

    @FXML
    private void handleFinancialReport() {
        if (reportTypeComboBox != null) {
            reportTypeComboBox.setValue("Financial Summary");
        }
        handleGenerateReport();
    }

    @FXML
    private void handleAllergyReport() {
        new Thread(() -> {
            try {
                List<CafeteriaMenuItem> items = cafeteriaService.getVegetarianItems();
                StringBuilder report = new StringBuilder();
                report.append("DIETARY RESTRICTIONS REPORT\n");
                report.append("=".repeat(50)).append("\n\n");
                report.append("Vegetarian Items: ").append(items.size()).append("\n");

                Platform.runLater(() -> {
                    if (reportTextArea != null) {
                        reportTextArea.setText(report.toString());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error generating allergy report: " + e.getMessage()));
            }
        }).start();
    }

    private String generateReport(String reportType) {
        StringBuilder report = new StringBuilder();
        report.append("CAFETERIA MANAGEMENT REPORT\n");
        report.append("=".repeat(50)).append("\n\n");

        try {
            switch (reportType) {
                case "Daily Statistics":
                    LocalDate date = transactionDatePicker != null ?
                        transactionDatePicker.getValue() : LocalDate.now();
                    Map<String, Object> dailyStats = cafeteriaService.getDailyStatistics(date);
                    report.append("Date: ").append(date.format(DATE_FORMATTER)).append("\n\n");
                    report.append("Total Meals Served: ").append(dailyStats.get("totalMealsServed")).append("\n");
                    report.append("Breakfast: ").append(dailyStats.get("breakfastCount")).append("\n");
                    report.append("Lunch: ").append(dailyStats.get("lunchCount")).append("\n");
                    report.append("Unique Students: ").append(dailyStats.get("uniqueStudents")).append("\n");
                    report.append("Total Revenue: ").append(formatCurrency((BigDecimal) dailyStats.get("totalRevenue"))).append("\n");
                    break;

                case "Eligibility Statistics":
                    Map<String, Object> eligStats = cafeteriaService.getEligibilityStatistics(CURRENT_ACADEMIC_YEAR);
                    report.append("Academic Year: ").append(CURRENT_ACADEMIC_YEAR).append("\n\n");
                    report.append("Free Meals: ").append(eligStats.get("freeCount")).append("\n");
                    report.append("Reduced Price: ").append(eligStats.get("reducedCount")).append("\n");
                    report.append("Full Price: ").append(eligStats.get("fullPriceCount")).append("\n");
                    break;

                default:
                    report.append("Report type not implemented yet");
            }
        } catch (Exception e) {
            report.append("Error generating report: ").append(e.getMessage());
        }

        return report.toString();
    }

    // ==================== Header Stats ====================

    private void updateHeaderStats() {
        new Thread(() -> {
            try {
                LocalDate today = LocalDate.now();
                Map<String, Object> dailyStats = cafeteriaService.getDailyStatistics(today);
                Map<String, Object> eligStats = cafeteriaService.getEligibilityStatistics(CURRENT_ACADEMIC_YEAR);
                List<StudentMealAccount> lowBalanceAccounts = cafeteriaService.getAccountsWithLowBalance(new BigDecimal("10.00"));
                List<StudentMealAccount> activeAccounts = cafeteriaService.getActiveAccountsByYear(CURRENT_ACADEMIC_YEAR);

                Platform.runLater(() -> {
                    if (mealsServedLabel != null) {
                        mealsServedLabel.setText(String.valueOf(dailyStats.get("totalMealsServed")));
                    }
                    if (freeReducedLabel != null) {
                        long freeCount = (long) eligStats.getOrDefault("freeCount", 0L);
                        long reducedCount = (long) eligStats.getOrDefault("reducedCount", 0L);
                        freeReducedLabel.setText(String.valueOf(freeCount + reducedCount));
                    }
                    if (revenueLabel != null) {
                        revenueLabel.setText(formatCurrency((BigDecimal) dailyStats.get("totalRevenue")));
                    }
                    if (activeAccountsLabel != null) {
                        activeAccountsLabel.setText(String.valueOf(activeAccounts.size()));
                    }
                    if (lowBalanceLabel != null) {
                        lowBalanceLabel.setText(String.valueOf(lowBalanceAccounts.size()));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> updateStatus("Error loading statistics: " + e.getMessage()));
            }
        }).start();
    }

    // ==================== Utility Methods ====================

    private DailyMenu.MealPeriod getCurrentMealPeriod() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 10) return DailyMenu.MealPeriod.BREAKFAST;
        if (hour < 14) return DailyMenu.MealPeriod.LUNCH;
        return DailyMenu.MealPeriod.DINNER;
    }

    private String formatPrice(MealPlan plan) {
        if (plan.getDailyRate() != null) {
            return formatCurrency(plan.getDailyRate()) + "/day";
        } else if (plan.getWeeklyRate() != null) {
            return formatCurrency(plan.getWeeklyRate()) + "/week";
        } else if (plan.getMonthlyRate() != null) {
            return formatCurrency(plan.getMonthlyRate()) + "/month";
        }
        return "N/A";
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("$%.2f", amount != null ? amount : BigDecimal.ZERO);
    }

    private BigDecimal parseBigDecimal(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        return new BigDecimal(text.trim().replace("$", "").replace(",", ""));
    }

    private void clearPlanFields() {
        if (planNameField != null) planNameField.clear();
        if (dailyRateField != null) dailyRateField.clear();
        if (weeklyRateField != null) weeklyRateField.clear();
        if (monthlyRateField != null) monthlyRateField.clear();
    }

    private void clearMenuItemFields() {
        if (itemNameField != null) itemNameField.clear();
        if (itemPriceField != null) itemPriceField.clear();
        if (itemCaloriesField != null) itemCaloriesField.clear();
        if (vegetarianCheckBox != null) vegetarianCheckBox.setSelected(false);
        if (veganCheckBox != null) veganCheckBox.setSelected(false);
        if (glutenFreeCheckBox != null) glutenFreeCheckBox.setSelected(false);
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void updateLastUpdatedLabel() {
        if (lastUpdatedLabel != null) {
            String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
            lastUpdatedLabel.setText("Last updated: " + timestamp);
        }
    }

    private void showSuccess(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Success", message);
        updateStatus(message);
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
        updateStatus("Error: " + message);
    }

    private void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, "Warning", message);
    }

    private void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Information", message);
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ==================== DTO Classes ====================

    public static class AccountDTO {
        private final Long accountId;
        private final String studentName;
        private final String balance;
        private final String planName;
        private final String eligibility;
        private final String status;
        private final String lastActivity;

        public AccountDTO(StudentMealAccount account) {
            this.accountId = account.getId();
            this.studentName = account.getStudent() != null ?
                account.getStudent().getFirstName() + " " + account.getStudent().getLastName() : "Unknown";
            this.balance = String.format("$%.2f", account.getBalance());
            this.planName = account.getMealPlan() != null ? account.getMealPlan().getPlanName() : "None";
            this.eligibility = account.getEligibilityStatus() != null ? account.getEligibilityStatus().toString() : "Unknown";
            this.status = account.getStatus().toString();
            this.lastActivity = account.getPlanStartDate() != null ?
                account.getPlanStartDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Never";
        }

        public Long getAccountId() { return accountId; }
        public String getStudentName() { return studentName; }
        public String getBalance() { return balance; }
        public String getPlanName() { return planName; }
        public String getEligibility() { return eligibility; }
        public String getStatus() { return status; }
        public String getLastActivity() { return lastActivity; }
    }

    public static class MenuItemDTO {
        private final String itemName;
        private final String category;
        private final String price;
        private final String calories;
        private final String available;

        public MenuItemDTO(CafeteriaMenuItem item) {
            this.itemName = item.getItemName();
            this.category = item.getCategory().toString();
            this.price = String.format("$%.2f", item.getPrice());
            this.calories = item.getCalories() != null ? item.getCalories().toString() : "N/A";
            this.available = Boolean.TRUE.equals(item.getAvailable()) ? "Yes" : "No";
        }

        public String getItemName() { return itemName; }
        public String getCategory() { return category; }
        public String getPrice() { return price; }
        public String getCalories() { return calories; }
        public String getAvailable() { return available; }
    }

    public static class TransactionDTO {
        private final String time;
        private final String studentName;
        private final String type;
        private final String amount;
        private final String balance;

        public TransactionDTO(MealTransaction transaction) {
            this.time = transaction.getTransactionDate().format(
                DateTimeFormatter.ofPattern("hh:mm a"));
            this.studentName = transaction.getStudent() != null ?
                transaction.getStudent().getFirstName() + " " + transaction.getStudent().getLastName() : "Unknown";
            this.type = transaction.getMealType() != null ? transaction.getMealType().toString() :
                transaction.getTransactionType().toString();
            this.amount = String.format("$%.2f", transaction.getAmount());
            this.balance = String.format("$%.2f", transaction.getBalanceAfter());
        }

        public String getTime() { return time; }
        public String getStudentName() { return studentName; }
        public String getType() { return type; }
        public String getAmount() { return amount; }
        public String getBalance() { return balance; }
    }
}
