package com.heronix.ui.controller;

import com.heronix.model.domain.Vendor;
import com.heronix.model.domain.Vendor.VendorStatus;
import com.heronix.model.domain.VendorCategory;
import com.heronix.repository.VendorCategoryRepository;
import com.heronix.repository.VendorRepository;
import com.heronix.service.VendorService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class VendorManagementController {

    private final VendorService vendorService;
    private final VendorRepository vendorRepository;
    private final VendorCategoryRepository categoryRepository;

    // Vendor filters
    @FXML private ComboBox<String> vendorStatusFilter;
    @FXML private ComboBox<String> vendorCategoryFilter;
    @FXML private TextField vendorSearchField;

    // Vendor summary
    @FXML private Label vendorTotalLabel;
    @FXML private Label vendorPendingLabel;
    @FXML private Label vendorApprovedLabel;
    @FXML private Label vendorSuspendedLabel;
    @FXML private Label vendorRejectedLabel;

    // Vendor table
    @FXML private TableView<Vendor> vendorTable;
    @FXML private TableColumn<Vendor, String> vendorCodeCol;
    @FXML private TableColumn<Vendor, String> vendorNameCol;
    @FXML private TableColumn<Vendor, String> vendorCategoryCol;
    @FXML private TableColumn<Vendor, String> vendorStatusCol;
    @FXML private TableColumn<Vendor, String> vendorPhoneCol;
    @FXML private TableColumn<Vendor, String> vendorEmailCol;
    @FXML private TableColumn<Vendor, String> vendorContactCol;
    @FXML private TableColumn<Vendor, String> vendorActiveCol;
    @FXML private TableColumn<Vendor, Void> vendorActionsCol;

    // Category table
    @FXML private TableView<VendorCategory> categoryTable;
    @FXML private TableColumn<VendorCategory, String> catNameCol;
    @FXML private TableColumn<VendorCategory, String> catIconCol;
    @FXML private TableColumn<VendorCategory, String> catColorCol;
    @FXML private TableColumn<VendorCategory, String> catActiveCol;
    @FXML private TableColumn<VendorCategory, String> catApprovalCol;
    @FXML private TableColumn<VendorCategory, String> catThresholdCol;
    @FXML private TableColumn<VendorCategory, Void> catActionsCol;

    private ObservableList<Vendor> allVendors = FXCollections.observableArrayList();
    private ObservableList<Vendor> filteredVendors = FXCollections.observableArrayList();
    private ObservableList<VendorCategory> allCategories = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupVendorsTab();
        setupCategoriesTab();
        loadVendors();
        loadCategories();
    }

    // ========================================================================
    // VENDORS TAB
    // ========================================================================

    private void setupVendorsTab() {
        vendorCodeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getVendorCode()));
        vendorNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        vendorCategoryCol.setCellValueFactory(cell -> {
            VendorCategory cat = cell.getValue().getCategory();
            return new SimpleStringProperty(cat != null ? cat.getName() : "");
        });
        vendorStatusCol.setCellValueFactory(cell -> {
            VendorStatus s = cell.getValue().getStatus();
            return new SimpleStringProperty(s != null ? s.getDisplayName() : "");
        });
        vendorStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "Approved" -> setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    case "Pending Approval" -> setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    case "Suspended" -> setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    case "Rejected" -> setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
                    default -> setStyle("");
                }
            }
        });
        vendorPhoneCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPhone()));
        vendorEmailCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));
        vendorContactCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getContactPerson()));
        vendorActiveCol.setCellValueFactory(cell -> new SimpleStringProperty(Boolean.TRUE.equals(cell.getValue().getActive()) ? "Yes" : "No"));

        setupVendorActionsColumn();

        vendorTable.setItems(filteredVendors);

        // Filters
        ObservableList<String> statuses = FXCollections.observableArrayList("All");
        Arrays.stream(VendorStatus.values()).forEach(s -> statuses.add(s.getDisplayName()));
        vendorStatusFilter.setItems(statuses);
        vendorStatusFilter.setValue("All");

        vendorStatusFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        vendorCategoryFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        vendorSearchField.textProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void setupVendorActionsColumn() {
        vendorActionsCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Vendor vendor = getTableRow().getItem();
                if (vendor == null) { setGraphic(null); return; }

                HBox buttons = new HBox(4);
                buttons.setAlignment(Pos.CENTER);

                Button editBtn = new Button("Edit");
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                editBtn.setOnAction(e -> handleEditVendor(vendor));

                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> handleDeleteVendor(vendor));

                buttons.getChildren().addAll(editBtn, deleteBtn);

                VendorStatus status = vendor.getStatus();
                if (status == VendorStatus.PENDING) {
                    Button approveBtn = new Button("Approve");
                    approveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                    approveBtn.setOnAction(e -> handleApproveVendor(vendor));

                    Button rejectBtn = new Button("Reject");
                    rejectBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                    rejectBtn.setOnAction(e -> handleRejectVendor(vendor));

                    buttons.getChildren().addAll(approveBtn, rejectBtn);
                } else if (status == VendorStatus.APPROVED) {
                    Button suspendBtn = new Button("Suspend");
                    suspendBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                    suspendBtn.setOnAction(e -> handleSuspendVendor(vendor));
                    buttons.getChildren().add(suspendBtn);
                } else if (status == VendorStatus.SUSPENDED || status == VendorStatus.REJECTED) {
                    Button reactivateBtn = new Button("Reactivate");
                    reactivateBtn.setStyle("-fx-background-color: #06b6d4; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                    reactivateBtn.setOnAction(e -> handleReactivateVendor(vendor));
                    buttons.getChildren().add(reactivateBtn);
                }

                setGraphic(buttons);
            }
        });
    }

    private void loadVendors() {
        new Thread(() -> {
            try {
                List<Vendor> vendors = vendorRepository.findAll();
                Platform.runLater(() -> {
                    allVendors.setAll(vendors);
                    applyFilters();
                    updateCategoryFilterOptions();
                });
            } catch (Exception e) {
                log.error("Failed to load vendors", e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load vendors: " + e.getMessage()));
            }
        }).start();
    }

    private void updateCategoryFilterOptions() {
        ObservableList<String> categories = FXCollections.observableArrayList("All");
        allCategories.forEach(c -> categories.add(c.getName()));
        String current = vendorCategoryFilter.getValue();
        vendorCategoryFilter.setItems(categories);
        vendorCategoryFilter.setValue(current != null && categories.contains(current) ? current : "All");
    }

    private void applyFilters() {
        String statusVal = vendorStatusFilter.getValue();
        String categoryVal = vendorCategoryFilter.getValue();
        String search = vendorSearchField.getText() != null ? vendorSearchField.getText().toLowerCase() : "";

        filteredVendors.setAll(allVendors.stream().filter(v -> {
            if (!"All".equals(statusVal) && v.getStatus() != null) {
                if (!v.getStatus().getDisplayName().equals(statusVal)) return false;
            }
            if (!"All".equals(categoryVal) && categoryVal != null) {
                if (v.getCategory() == null || !v.getCategory().getName().equals(categoryVal)) return false;
            }
            if (!search.isEmpty()) {
                String name = v.getName() != null ? v.getName().toLowerCase() : "";
                String code = v.getVendorCode() != null ? v.getVendorCode().toLowerCase() : "";
                String email = v.getEmail() != null ? v.getEmail().toLowerCase() : "";
                if (!name.contains(search) && !code.contains(search) && !email.contains(search)) return false;
            }
            return true;
        }).collect(Collectors.toList()));

        vendorTotalLabel.setText(String.valueOf(filteredVendors.size()));
        vendorPendingLabel.setText(String.valueOf(filteredVendors.stream().filter(v -> v.getStatus() == VendorStatus.PENDING).count()));
        vendorApprovedLabel.setText(String.valueOf(filteredVendors.stream().filter(v -> v.getStatus() == VendorStatus.APPROVED).count()));
        vendorSuspendedLabel.setText(String.valueOf(filteredVendors.stream().filter(v -> v.getStatus() == VendorStatus.SUSPENDED).count()));
        vendorRejectedLabel.setText(String.valueOf(filteredVendors.stream().filter(v -> v.getStatus() == VendorStatus.REJECTED).count()));
    }

    // ========================================================================
    // CATEGORIES TAB
    // ========================================================================

    private void setupCategoriesTab() {
        catNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        catIconCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getIcon()));
        catColorCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getColor()));
        catColorCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("-fx-background-color: " + item + "22; -fx-text-fill: " + item + ";");
            }
        });
        catActiveCol.setCellValueFactory(cell -> new SimpleStringProperty(Boolean.TRUE.equals(cell.getValue().getActive()) ? "Yes" : "No"));
        catApprovalCol.setCellValueFactory(cell -> new SimpleStringProperty(Boolean.TRUE.equals(cell.getValue().getRequiresApproval()) ? "Yes" : "No"));
        catThresholdCol.setCellValueFactory(cell -> {
            BigDecimal val = cell.getValue().getMaxAmountWithoutApproval();
            return new SimpleStringProperty(val != null ? "$" + val.toPlainString() : "N/A");
        });

        catActionsCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                VendorCategory cat = getTableRow().getItem();
                if (cat == null) { setGraphic(null); return; }

                Button editBtn = new Button("Edit");
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                editBtn.setOnAction(e -> handleEditCategory(cat));

                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> handleDeleteCategory(cat));

                setGraphic(new HBox(4, editBtn, deleteBtn));
            }
        });

        categoryTable.setItems(allCategories);
    }

    private void loadCategories() {
        new Thread(() -> {
            try {
                List<VendorCategory> categories = categoryRepository.findAllByOrderByDisplayOrder();
                Platform.runLater(() -> {
                    allCategories.setAll(categories);
                    updateCategoryFilterOptions();
                });
            } catch (Exception e) {
                log.error("Failed to load categories", e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage()));
            }
        }).start();
    }

    // ========================================================================
    // VENDOR CRUD & APPROVAL
    // ========================================================================

    @FXML
    private void handleAddVendor() {
        showVendorDialog(null);
    }

    private void handleEditVendor(Vendor vendor) {
        showVendorDialog(vendor);
    }

    private void handleDeleteVendor(Vendor vendor) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete vendor \"" + vendor.getName() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    vendorService.deleteVendor(vendor.getId(), "admin");
                    loadVendors();
                } catch (Exception e) {
                    log.error("Failed to delete vendor", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete: " + e.getMessage());
                }
            }
        });
    }

    private void showVendorDialog(Vendor existing) {
        Dialog<Vendor> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Vendor" : "Edit Vendor");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(600);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Basic info
        TextField nameField = new TextField();
        TextField codeField = new TextField();
        TextArea descField = new TextArea();
        descField.setPrefRowCount(2);
        ComboBox<VendorCategory> categoryCombo = new ComboBox<>(allCategories);
        categoryCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(VendorCategory c, boolean empty) {
                super.updateItem(c, empty); setText(empty || c == null ? "" : c.getName());
            }
        });
        categoryCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(VendorCategory c, boolean empty) {
                super.updateItem(c, empty); setText(empty || c == null ? "" : c.getName());
            }
        });

        // Contact
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        TextField contactField = new TextField();
        TextField websiteField = new TextField();

        // Address
        TextField addr1Field = new TextField();
        TextField addr2Field = new TextField();
        TextField cityField = new TextField();
        TextField stateField = new TextField();
        TextField zipField = new TextField();

        // Purchase limits
        TextField minPurchaseField = new TextField();
        minPurchaseField.setPromptText("0.00");
        TextField maxPurchaseField = new TextField();
        maxPurchaseField.setPromptText("Optional");
        TextField quoteAboveField = new TextField();
        quoteAboveField.setPromptText("Optional");

        // Payment
        TextField paymentTermsField = new TextField();
        CheckBox poCheckbox = new CheckBox("Purchase Order");
        CheckBox ccCheckbox = new CheckBox("Credit Card");
        CheckBox checkCheckbox = new CheckBox("Check");

        // Integration
        CheckBox integrationCheckbox = new CheckBox("Integration Enabled");
        TextField catalogUrlField = new TextField();
        TextField apiEndpointField = new TextField();

        int row = 0;
        grid.add(new Label("--- Basic Info ---"), 0, row++, 2, 1);
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Vendor Code:"), 0, row); grid.add(codeField, 1, row++);
        grid.add(new Label("Description:"), 0, row); grid.add(descField, 1, row++);
        grid.add(new Label("Category:"), 0, row); grid.add(categoryCombo, 1, row++);
        grid.add(new Label("--- Contact ---"), 0, row++, 2, 1);
        grid.add(new Label("Phone:"), 0, row); grid.add(phoneField, 1, row++);
        grid.add(new Label("Email:"), 0, row); grid.add(emailField, 1, row++);
        grid.add(new Label("Contact Person:"), 0, row); grid.add(contactField, 1, row++);
        grid.add(new Label("Website:"), 0, row); grid.add(websiteField, 1, row++);
        grid.add(new Label("--- Address ---"), 0, row++, 2, 1);
        grid.add(new Label("Address Line 1:"), 0, row); grid.add(addr1Field, 1, row++);
        grid.add(new Label("Address Line 2:"), 0, row); grid.add(addr2Field, 1, row++);
        grid.add(new Label("City:"), 0, row); grid.add(cityField, 1, row++);
        grid.add(new Label("State:"), 0, row); grid.add(stateField, 1, row++);
        grid.add(new Label("Zip:"), 0, row); grid.add(zipField, 1, row++);
        grid.add(new Label("--- Purchase Limits ---"), 0, row++, 2, 1);
        grid.add(new Label("Min Purchase:"), 0, row); grid.add(minPurchaseField, 1, row++);
        grid.add(new Label("Max Purchase:"), 0, row); grid.add(maxPurchaseField, 1, row++);
        grid.add(new Label("Requires Quote Above:"), 0, row); grid.add(quoteAboveField, 1, row++);
        grid.add(new Label("--- Payment ---"), 0, row++, 2, 1);
        grid.add(new Label("Payment Terms:"), 0, row); grid.add(paymentTermsField, 1, row++);
        grid.add(new HBox(10, poCheckbox, ccCheckbox, checkCheckbox), 0, row++, 2, 1);
        grid.add(new Label("--- Integration ---"), 0, row++, 2, 1);
        grid.add(integrationCheckbox, 1, row++);
        grid.add(new Label("Catalog URL:"), 0, row); grid.add(catalogUrlField, 1, row++);
        grid.add(new Label("API Endpoint:"), 0, row); grid.add(apiEndpointField, 1, row++);

        // Pre-populate for edit
        if (existing != null) {
            nameField.setText(existing.getName());
            codeField.setText(existing.getVendorCode());
            descField.setText(existing.getDescription());
            categoryCombo.setValue(existing.getCategory());
            phoneField.setText(existing.getPhone());
            emailField.setText(existing.getEmail());
            contactField.setText(existing.getContactPerson());
            websiteField.setText(existing.getWebsite());
            addr1Field.setText(existing.getAddressLine1());
            addr2Field.setText(existing.getAddressLine2());
            cityField.setText(existing.getCity());
            stateField.setText(existing.getState());
            zipField.setText(existing.getZipCode());
            if (existing.getMinPurchaseAmount() != null) minPurchaseField.setText(existing.getMinPurchaseAmount().toPlainString());
            if (existing.getMaxPurchaseAmount() != null) maxPurchaseField.setText(existing.getMaxPurchaseAmount().toPlainString());
            if (existing.getRequiresQuoteAbove() != null) quoteAboveField.setText(existing.getRequiresQuoteAbove().toPlainString());
            paymentTermsField.setText(existing.getPaymentTerms());
            poCheckbox.setSelected(Boolean.TRUE.equals(existing.getAcceptsPurchaseOrder()));
            ccCheckbox.setSelected(Boolean.TRUE.equals(existing.getAcceptsCreditCard()));
            checkCheckbox.setSelected(Boolean.TRUE.equals(existing.getAcceptsCheck()));
            integrationCheckbox.setSelected(Boolean.TRUE.equals(existing.getIntegrationEnabled()));
            catalogUrlField.setText(existing.getCatalogUrl());
            apiEndpointField.setText(existing.getApiEndpoint());
        } else {
            poCheckbox.setSelected(true);
            checkCheckbox.setSelected(true);
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        dialog.getDialogPane().setContent(scrollPane);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Vendor v = existing != null ? existing : new Vendor();
                v.setName(nameField.getText());
                v.setVendorCode(codeField.getText());
                v.setDescription(descField.getText());
                v.setCategory(categoryCombo.getValue());
                v.setPhone(phoneField.getText());
                v.setEmail(emailField.getText());
                v.setContactPerson(contactField.getText());
                v.setWebsite(websiteField.getText());
                v.setAddressLine1(addr1Field.getText());
                v.setAddressLine2(addr2Field.getText());
                v.setCity(cityField.getText());
                v.setState(stateField.getText());
                v.setZipCode(zipField.getText());
                v.setMinPurchaseAmount(parseBigDecimal(minPurchaseField.getText()));
                v.setMaxPurchaseAmount(parseBigDecimal(maxPurchaseField.getText()));
                v.setRequiresQuoteAbove(parseBigDecimal(quoteAboveField.getText()));
                v.setPaymentTerms(paymentTermsField.getText());
                v.setAcceptsPurchaseOrder(poCheckbox.isSelected());
                v.setAcceptsCreditCard(ccCheckbox.isSelected());
                v.setAcceptsCheck(checkCheckbox.isSelected());
                v.setIntegrationEnabled(integrationCheckbox.isSelected());
                v.setCatalogUrl(catalogUrlField.getText());
                v.setApiEndpoint(apiEndpointField.getText());
                return v;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(v -> {
            try {
                if (existing == null) {
                    vendorService.createVendor(v, "admin");
                } else {
                    vendorService.updateVendor(v.getId(), v, "admin");
                }
                loadVendors();
            } catch (Exception e) {
                log.error("Failed to save vendor", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save vendor: " + e.getMessage());
            }
        });
    }

    private void handleApproveVendor(Vendor vendor) {
        try {
            vendorService.approveVendor(vendor.getId(), "admin");
            showAlert(Alert.AlertType.INFORMATION, "Approved", "Vendor \"" + vendor.getName() + "\" approved.");
            loadVendors();
        } catch (Exception e) {
            log.error("Failed to approve vendor", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve: " + e.getMessage());
        }
    }

    private void handleRejectVendor(Vendor vendor) {
        showReasonDialog("Reject Vendor", "Reason for rejecting " + vendor.getName() + ":").ifPresent(reason -> {
            try {
                vendorService.rejectVendor(vendor.getId(), reason);
                loadVendors();
            } catch (Exception e) {
                log.error("Failed to reject vendor", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject: " + e.getMessage());
            }
        });
    }

    private void handleSuspendVendor(Vendor vendor) {
        showReasonDialog("Suspend Vendor", "Reason for suspending " + vendor.getName() + ":").ifPresent(reason -> {
            try {
                vendorService.suspendVendor(vendor.getId(), reason);
                loadVendors();
            } catch (Exception e) {
                log.error("Failed to suspend vendor", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to suspend: " + e.getMessage());
            }
        });
    }

    private void handleReactivateVendor(Vendor vendor) {
        try {
            vendorService.reactivateVendor(vendor.getId());
            showAlert(Alert.AlertType.INFORMATION, "Reactivated", "Vendor \"" + vendor.getName() + "\" reactivated.");
            loadVendors();
        } catch (Exception e) {
            log.error("Failed to reactivate vendor", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to reactivate: " + e.getMessage());
        }
    }

    // ========================================================================
    // CATEGORY CRUD
    // ========================================================================

    @FXML
    private void handleAddCategory() {
        showCategoryDialog(null);
    }

    private void handleEditCategory(VendorCategory category) {
        showCategoryDialog(category);
    }

    private void handleDeleteCategory(VendorCategory category) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete category \"" + category.getName() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    categoryRepository.deleteById(category.getId());
                    loadCategories();
                    loadVendors();
                } catch (Exception e) {
                    log.error("Failed to delete category", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete: " + e.getMessage());
                }
            }
        });
    }

    private void showCategoryDialog(VendorCategory existing) {
        Dialog<VendorCategory> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Category" : "Edit Category");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextArea descField = new TextArea();
        descField.setPrefRowCount(2);
        TextField iconField = new TextField();
        iconField.setPromptText("e.g., shopping-cart");
        TextField colorField = new TextField();
        colorField.setPromptText("e.g., #3b82f6");
        CheckBox activeCheckbox = new CheckBox("Active");
        activeCheckbox.setSelected(true);
        CheckBox approvalCheckbox = new CheckBox("Requires Approval");
        Spinner<Double> thresholdSpinner = new Spinner<>(0.0, 1000000.0, 0.0, 100.0);
        thresholdSpinner.setEditable(true);
        Spinner<Integer> orderSpinner = new Spinner<>(0, 100, 0);

        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Description:"), 0, row); grid.add(descField, 1, row++);
        grid.add(new Label("Icon:"), 0, row); grid.add(iconField, 1, row++);
        grid.add(new Label("Color:"), 0, row); grid.add(colorField, 1, row++);
        grid.add(activeCheckbox, 1, row++);
        grid.add(approvalCheckbox, 1, row++);
        grid.add(new Label("Max w/o Approval:"), 0, row); grid.add(thresholdSpinner, 1, row++);
        grid.add(new Label("Display Order:"), 0, row); grid.add(orderSpinner, 1, row++);

        if (existing != null) {
            nameField.setText(existing.getName());
            descField.setText(existing.getDescription());
            iconField.setText(existing.getIcon());
            colorField.setText(existing.getColor());
            activeCheckbox.setSelected(Boolean.TRUE.equals(existing.getActive()));
            approvalCheckbox.setSelected(Boolean.TRUE.equals(existing.getRequiresApproval()));
            if (existing.getMaxAmountWithoutApproval() != null) thresholdSpinner.getValueFactory().setValue(existing.getMaxAmountWithoutApproval().doubleValue());
            if (existing.getDisplayOrder() != null) orderSpinner.getValueFactory().setValue(existing.getDisplayOrder());
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                VendorCategory cat = existing != null ? existing : new VendorCategory();
                cat.setName(nameField.getText());
                cat.setDescription(descField.getText());
                cat.setIcon(iconField.getText());
                cat.setColor(colorField.getText());
                cat.setActive(activeCheckbox.isSelected());
                cat.setRequiresApproval(approvalCheckbox.isSelected());
                cat.setMaxAmountWithoutApproval(BigDecimal.valueOf(thresholdSpinner.getValue()));
                cat.setDisplayOrder(orderSpinner.getValue());
                return cat;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(cat -> {
            try {
                if (existing == null) {
                    vendorService.createCategory(cat, "admin");
                } else {
                    vendorService.updateCategory(cat.getId(), cat);
                }
                loadCategories();
            } catch (Exception e) {
                log.error("Failed to save category", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save category: " + e.getMessage());
            }
        });
    }

    // ========================================================================
    // FILTER HANDLERS
    // ========================================================================

    @FXML
    private void handleClearVendorFilters() {
        vendorStatusFilter.setValue("All");
        vendorCategoryFilter.setValue("All");
        vendorSearchField.clear();
    }

    // ========================================================================
    // UTILITIES
    // ========================================================================

    private java.util.Optional<String> showReasonDialog(String title, String header) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Enter reason...");
        reasonArea.setPrefRowCount(4);
        VBox content = new VBox(10, new Label("Reason:"), reasonArea);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? reasonArea.getText() : null);
        return dialog.showAndWait();
    }

    private BigDecimal parseBigDecimal(String text) {
        if (text == null || text.isBlank()) return null;
        try { return new BigDecimal(text.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
