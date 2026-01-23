package com.heronix.ui.fees;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Fee Management View
 * Interface for managing student fees, tuition, and payments.
 *
 * Features:
 * - Account balance overview
 * - Fee item breakdown
 * - Payment history
 * - Payment processing
 * - Payment plans
 * - Invoice generation
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class FeeManagementView extends BorderPane {

    // ========================================================================
    // DATA
    // ========================================================================

    private final ObservableList<FeeItem> feeItems = FXCollections.observableArrayList();
    private final ObservableList<Payment> payments = FXCollections.observableArrayList();

    @Getter @Setter
    private String studentId;

    @Getter @Setter
    private String studentName;

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private VBox accountSummary;
    private TableView<FeeItem> feesTable;
    private TableView<Payment> paymentsTable;
    private Label balanceLabel;
    private Label dueAmountLabel;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<Payment> onPaymentProcessed;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public FeeManagementView() {
        setStyle("-fx-background-color: #F8FAFC;");

        setTop(createToolbar());
        setCenter(createMainContent());
        setRight(createPaymentPanel());

        loadDemoData();

        log.info("FeeManagementView initialized");
    }

    // ========================================================================
    // TOOLBAR
    // ========================================================================

    private VBox createToolbar() {
        VBox toolbar = new VBox(12);
        toolbar.setPadding(new Insets(16, 24, 16, 24));
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Fee Management");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        TextField studentSearch = new TextField();
        studentSearch.setPromptText("Search student...");
        studentSearch.setPrefWidth(250);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addFeeBtn = new Button("+ Add Fee");
        addFeeBtn.getStyleClass().addAll("btn", "btn-ghost");
        addFeeBtn.setOnAction(e -> showAddFeeDialog());

        Button recordPaymentBtn = new Button("Record Payment");
        recordPaymentBtn.getStyleClass().addAll("btn", "btn-primary");
        recordPaymentBtn.setOnAction(e -> showRecordPaymentDialog());

        Button invoiceBtn = new Button("Generate Invoice");
        invoiceBtn.getStyleClass().addAll("btn", "btn-ghost");

        titleRow.getChildren().addAll(title, studentSearch, spacer, addFeeBtn, recordPaymentBtn, invoiceBtn);

        toolbar.getChildren().add(titleRow);
        return toolbar;
    }

    // ========================================================================
    // MAIN CONTENT
    // ========================================================================

    private VBox createMainContent() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(16));

        // Account summary cards
        accountSummary = createAccountSummary();

        // Tabs for fees and payments
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab feesTab = new Tab("Current Fees", createFeesPane());
        Tab paymentsTab = new Tab("Payment History", createPaymentsPane());
        Tab plansTab = new Tab("Payment Plans", createPaymentPlansPane());

        tabPane.getTabs().addAll(feesTab, paymentsTab, plansTab);

        content.getChildren().addAll(accountSummary, tabPane);
        return content;
    }

    private VBox createAccountSummary() {
        VBox summary = new VBox(16);

        // Student info header
        HBox studentHeader = new HBox(16);
        studentHeader.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 32px;");

        VBox studentInfo = new VBox(2);
        Label nameLabel = new Label("Emma Johnson");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");
        Label idLabel = new Label("ID: S1001 • Grade 10");
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        studentInfo.getChildren().addAll(nameLabel, idLabel);

        studentHeader.getChildren().addAll(avatar, studentInfo);

        // Balance cards
        HBox balanceCards = new HBox(16);

        balanceCards.getChildren().addAll(
            createBalanceCard("Total Balance", "$2,450.00", "#0F172A", false),
            createBalanceCard("Amount Due Now", "$850.00", "#EF4444", true),
            createBalanceCard("Next Due Date", "Feb 15, 2026", "#F59E0B", false),
            createBalanceCard("Total Paid (Year)", "$4,550.00", "#10B981", false)
        );

        summary.getChildren().addAll(studentHeader, balanceCards);
        return summary;
    }

    private VBox createBalanceCard(String title, String value, String color, boolean highlight) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(16));
        card.setPrefWidth(180);

        String bgColor = highlight ? "#FEF2F2" : "white";
        String borderColor = highlight ? "#FECACA" : "#E2E8F0";

        card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 8;
            -fx-border-color: %s;
            -fx-border-radius: 8;
            """, bgColor, borderColor));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");

        if (title.equals("Total Balance")) {
            balanceLabel = valueLabel;
        } else if (title.equals("Amount Due Now")) {
            dueAmountLabel = valueLabel;
        }

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    // ========================================================================
    // FEES PANE
    // ========================================================================

    private VBox createFeesPane() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(16));

        feesTable = new TableView<>(feeItems);
        feesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(feesTable, Priority.ALWAYS);

        TableColumn<FeeItem, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))));
        dateCol.setPrefWidth(100);

        TableColumn<FeeItem, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescription()));
        descCol.setPrefWidth(250);

        TableColumn<FeeItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory()));
        categoryCol.setPrefWidth(120);

        TableColumn<FeeItem, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cell -> new SimpleStringProperty(
            String.format("$%.2f", cell.getValue().getAmount())));
        amountCol.setPrefWidth(100);
        amountCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<FeeItem, String> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(cell -> new SimpleStringProperty(
            String.format("$%.2f", cell.getValue().getPaidAmount())));
        paidCol.setPrefWidth(100);
        paidCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<FeeItem, String> balanceCol = new TableColumn<>("Balance");
        balanceCol.setCellValueFactory(cell -> new SimpleStringProperty(
            String.format("$%.2f", cell.getValue().getBalance())));
        balanceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    FeeItem fee = getTableView().getItems().get(getIndex());
                    String color = fee.getBalance() > 0 ? "#EF4444" : "#10B981";
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: 600; -fx-alignment: CENTER-RIGHT;");
                }
            }
        });
        balanceCol.setPrefWidth(100);

        TableColumn<FeeItem, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    String color = item.equals("Paid") ? "#10B981" :
                                  item.equals("Partial") ? "#F59E0B" : "#EF4444";
                    badge.setStyle(String.format("""
                        -fx-background-color: %s20;
                        -fx-text-fill: %s;
                        -fx-font-size: 11px;
                        -fx-padding: 2 8;
                        -fx-background-radius: 4;
                        """, color, color));
                    setGraphic(badge);
                }
            }
        });
        statusCol.setPrefWidth(80);

        feesTable.getColumns().addAll(dateCol, descCol, categoryCol, amountCol, paidCol, balanceCol, statusCol);

        pane.getChildren().add(feesTable);
        return pane;
    }

    // ========================================================================
    // PAYMENTS PANE
    // ========================================================================

    private VBox createPaymentsPane() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(16));

        paymentsTable = new TableView<>(payments);
        paymentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(paymentsTable, Priority.ALWAYS);

        TableColumn<Payment, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))));
        dateCol.setPrefWidth(100);

        TableColumn<Payment, String> receiptCol = new TableColumn<>("Receipt #");
        receiptCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getReceiptNumber()));
        receiptCol.setPrefWidth(120);

        TableColumn<Payment, String> methodCol = new TableColumn<>("Method");
        methodCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMethod()));
        methodCol.setPrefWidth(120);

        TableColumn<Payment, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cell -> new SimpleStringProperty(
            String.format("$%.2f", cell.getValue().getAmount())));
        amountCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        amountCol.setPrefWidth(100);

        TableColumn<Payment, String> appliedToCol = new TableColumn<>("Applied To");
        appliedToCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAppliedTo()));
        appliedToCol.setPrefWidth(200);

        TableColumn<Payment, String> processedByCol = new TableColumn<>("Processed By");
        processedByCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProcessedBy()));
        processedByCol.setPrefWidth(150);

        paymentsTable.getColumns().addAll(dateCol, receiptCol, methodCol, amountCol, appliedToCol, processedByCol);

        pane.getChildren().add(paymentsTable);
        return pane;
    }

    // ========================================================================
    // PAYMENT PLANS PANE
    // ========================================================================

    private VBox createPaymentPlansPane() {
        VBox pane = new VBox(16);
        pane.setPadding(new Insets(16));

        // Current plan card
        VBox planCard = new VBox(12);
        planCard.setPadding(new Insets(16));
        planCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

        HBox planHeader = new HBox(12);
        planHeader.setAlignment(Pos.CENTER_LEFT);

        Label planTitle = new Label("Monthly Payment Plan");
        planTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Label activeBadge = new Label("Active");
        activeBadge.setStyle("-fx-background-color: #ECFDF5; -fx-text-fill: #059669; -fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 4;");

        planHeader.getChildren().addAll(planTitle, activeBadge);

        GridPane planDetails = new GridPane();
        planDetails.setHgap(24);
        planDetails.setVgap(8);

        planDetails.add(createPlanDetailRow("Total Amount:", "$7,000.00"), 0, 0);
        planDetails.add(createPlanDetailRow("Payments Made:", "5 of 10"), 1, 0);
        planDetails.add(createPlanDetailRow("Monthly Payment:", "$700.00"), 0, 1);
        planDetails.add(createPlanDetailRow("Next Payment Due:", "Feb 15, 2026"), 1, 1);
        planDetails.add(createPlanDetailRow("Remaining Balance:", "$3,500.00"), 0, 2);
        planDetails.add(createPlanDetailRow("Final Payment:", "Jul 15, 2026"), 1, 2);

        // Progress bar
        VBox progressBox = new VBox(4);
        Label progressLabel = new Label("Payment Progress: 50%");
        progressLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        ProgressBar progressBar = new ProgressBar(0.5);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #10B981;");
        progressBox.getChildren().addAll(progressLabel, progressBar);

        planCard.getChildren().addAll(planHeader, planDetails, progressBox);

        // Payment schedule
        Label scheduleTitle = new Label("Upcoming Payments");
        scheduleTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        VBox schedule = new VBox(8);
        schedule.getChildren().addAll(
            createScheduleItem("Feb 15, 2026", "$700.00", "Upcoming"),
            createScheduleItem("Mar 15, 2026", "$700.00", "Scheduled"),
            createScheduleItem("Apr 15, 2026", "$700.00", "Scheduled"),
            createScheduleItem("May 15, 2026", "$700.00", "Scheduled"),
            createScheduleItem("Jun 15, 2026", "$700.00", "Scheduled")
        );

        pane.getChildren().addAll(planCard, scheduleTitle, schedule);
        return pane;
    }

    private HBox createPlanDetailRow(String label, String value) {
        HBox row = new HBox(8);
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        labelNode.setMinWidth(120);
        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #0F172A;");
        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }

    private HBox createScheduleItem(String date, String amount, String status) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12));
        item.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-border-color: #E2E8F0; -fx-border-radius: 6;");

        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #0F172A;");
        dateLabel.setMinWidth(120);

        Label amountLabel = new Label(amount);
        amountLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String badgeColor = status.equals("Upcoming") ? "#2563EB" : "#64748B";
        Label statusBadge = new Label(status);
        statusBadge.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 11px;", badgeColor));

        item.getChildren().addAll(dateLabel, amountLabel, spacer, statusBadge);
        return item;
    }

    // ========================================================================
    // PAYMENT PANEL
    // ========================================================================

    private VBox createPaymentPanel() {
        VBox panel = new VBox(16);
        panel.setPrefWidth(280);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Quick Payment");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        // Amount input
        VBox amountBox = new VBox(4);
        Label amountLabel = new Label("Payment Amount");
        amountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        TextField amountField = new TextField();
        amountField.setPromptText("$0.00");
        amountField.setStyle("-fx-font-size: 16px;");
        amountBox.getChildren().addAll(amountLabel, amountField);

        // Quick amount buttons
        Label quickLabel = new Label("Quick Amounts");
        quickLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        FlowPane quickAmounts = new FlowPane(8, 8);
        String[] amounts = {"$50", "$100", "$250", "$500", "Pay Full"};
        for (String amt : amounts) {
            Button btn = new Button(amt);
            btn.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");
            btn.setOnAction(e -> {
                if (amt.equals("Pay Full")) {
                    amountField.setText("2450.00");
                } else {
                    amountField.setText(amt.replace("$", ""));
                }
            });
            quickAmounts.getChildren().add(btn);
        }

        // Payment method
        VBox methodBox = new VBox(4);
        Label methodLabel = new Label("Payment Method");
        methodLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        ComboBox<String> methodCombo = new ComboBox<>();
        methodCombo.getItems().addAll("Credit Card", "Debit Card", "Cash", "Check", "Bank Transfer");
        methodCombo.setPromptText("Select method");
        methodCombo.setMaxWidth(Double.MAX_VALUE);
        methodBox.getChildren().addAll(methodLabel, methodCombo);

        // Process button
        Button processBtn = new Button("Process Payment");
        processBtn.getStyleClass().addAll("btn", "btn-primary");
        processBtn.setMaxWidth(Double.MAX_VALUE);
        processBtn.setOnAction(e -> processQuickPayment(amountField.getText(), methodCombo.getValue()));

        // Accepted cards info
        Label acceptedLabel = new Label("Accepted Payment Methods:");
        acceptedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B; -fx-padding: 16 0 0 0;");
        Label cardsLabel = new Label("💳 Visa, Mastercard, Amex, Discover\n🏦 ACH Bank Transfer\n💵 Cash, Check");
        cardsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        panel.getChildren().addAll(title, amountBox, quickLabel, quickAmounts, methodBox, processBtn,
                                  acceptedLabel, cardsLabel);

        return panel;
    }

    // ========================================================================
    // DIALOGS
    // ========================================================================

    private void showAddFeeDialog() {
        Dialog<FeeItem> dialog = new Dialog<>();
        dialog.setTitle("Add Fee");
        dialog.setHeaderText("Add a new fee to the student account");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));

        TextField descField = new TextField();
        descField.setPromptText("Fee description");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Tuition", "Activity Fee", "Lab Fee", "Sports Fee",
                                     "Transportation", "Lunch", "Field Trip", "Other");
        categoryBox.setPromptText("Select category");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        DatePicker dueDatePicker = new DatePicker(LocalDate.now().plusDays(30));

        form.add(new Label("Description:"), 0, 0);
        form.add(descField, 1, 0);
        form.add(new Label("Category:"), 0, 1);
        form.add(categoryBox, 1, 1);
        form.add(new Label("Amount:"), 0, 2);
        form.add(amountField, 1, 2);
        form.add(new Label("Due Date:"), 0, 3);
        form.add(dueDatePicker, 1, 3);

        pane.setContent(form);

        dialog.showAndWait();
    }

    private void showRecordPaymentDialog() {
        Dialog<Payment> dialog = new Dialog<>();
        dialog.setTitle("Record Payment");
        dialog.setHeaderText("Record a payment");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));

        TextField amountField = new TextField();
        amountField.setPromptText("Payment amount");

        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("Credit Card", "Debit Card", "Cash", "Check", "Bank Transfer");
        methodBox.setPromptText("Payment method");

        TextField referenceField = new TextField();
        referenceField.setPromptText("Reference/Check number");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Notes");
        notesArea.setPrefRowCount(2);

        form.add(new Label("Amount:"), 0, 0);
        form.add(amountField, 1, 0);
        form.add(new Label("Method:"), 0, 1);
        form.add(methodBox, 1, 1);
        form.add(new Label("Reference:"), 0, 2);
        form.add(referenceField, 1, 2);
        form.add(new Label("Notes:"), 0, 3);
        form.add(notesArea, 1, 3);

        pane.setContent(form);

        dialog.showAndWait();
    }

    private void processQuickPayment(String amount, String method) {
        if (amount == null || amount.isEmpty() || method == null) {
            showAlert("Missing Information", "Please enter an amount and select a payment method.");
            return;
        }

        try {
            double paymentAmount = Double.parseDouble(amount.replace("$", "").replace(",", ""));

            Payment payment = new Payment();
            payment.setDate(LocalDate.now());
            payment.setReceiptNumber("R" + System.currentTimeMillis());
            payment.setMethod(method);
            payment.setAmount(paymentAmount);
            payment.setAppliedTo("Account Balance");
            payment.setProcessedBy("Current User");

            payments.add(0, payment);

            showAlert("Payment Processed", String.format("Payment of $%.2f has been processed.\nReceipt: %s",
                paymentAmount, payment.getReceiptNumber()));

            if (onPaymentProcessed != null) {
                onPaymentProcessed.accept(payment);
            }

        } catch (NumberFormatException e) {
            showAlert("Invalid Amount", "Please enter a valid payment amount.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ========================================================================
    // DEMO DATA
    // ========================================================================

    private void loadDemoData() {
        feeItems.addAll(
            new FeeItem(LocalDate.now().minusMonths(4), "Tuition - Semester 1", "Tuition", 3500.00, 3500.00),
            new FeeItem(LocalDate.now().minusMonths(4), "Technology Fee", "Activity Fee", 150.00, 150.00),
            new FeeItem(LocalDate.now().minusMonths(1), "Tuition - Semester 2", "Tuition", 3500.00, 1050.00),
            new FeeItem(LocalDate.now().minusWeeks(2), "Science Lab Fee", "Lab Fee", 75.00, 0.00),
            new FeeItem(LocalDate.now().minusWeeks(1), "Spring Sports Fee", "Sports Fee", 225.00, 0.00)
        );

        payments.addAll(
            new Payment(LocalDate.now().minusMonths(4), "R2025001", "Credit Card", 3650.00, "Tuition + Tech Fee", "Admin"),
            new Payment(LocalDate.now().minusMonths(2), "R2025002", "Bank Transfer", 700.00, "Tuition Payment", "Admin"),
            new Payment(LocalDate.now().minusMonths(1), "R2025003", "Credit Card", 350.00, "Tuition Payment", "Admin")
        );
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    public void setOnPaymentProcessed(Consumer<Payment> callback) {
        this.onPaymentProcessed = callback;
    }

    // ========================================================================
    // INNER CLASSES
    // ========================================================================

    @Getter @Setter
    public static class FeeItem {
        private LocalDate date;
        private String description;
        private String category;
        private double amount;
        private double paidAmount;

        public FeeItem(LocalDate date, String description, String category, double amount, double paidAmount) {
            this.date = date;
            this.description = description;
            this.category = category;
            this.amount = amount;
            this.paidAmount = paidAmount;
        }

        public double getBalance() {
            return amount - paidAmount;
        }

        public String getStatus() {
            if (paidAmount >= amount) return "Paid";
            if (paidAmount > 0) return "Partial";
            return "Unpaid";
        }
    }

    @Getter @Setter
    public static class Payment {
        private LocalDate date;
        private String receiptNumber;
        private String method;
        private double amount;
        private String appliedTo;
        private String processedBy;

        public Payment() {}

        public Payment(LocalDate date, String receiptNumber, String method, double amount, String appliedTo, String processedBy) {
            this.date = date;
            this.receiptNumber = receiptNumber;
            this.method = method;
            this.amount = amount;
            this.appliedTo = appliedTo;
            this.processedBy = processedBy;
        }
    }
}
