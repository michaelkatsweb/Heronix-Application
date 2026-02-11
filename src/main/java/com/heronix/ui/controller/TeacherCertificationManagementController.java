package com.heronix.ui.controller;

import com.heronix.model.domain.Teacher;
import com.heronix.model.domain.TeacherCertification;
import com.heronix.model.domain.TeacherCertification.CertificateType;
import com.heronix.model.domain.TeacherCertification.CertificationStatus;
import com.heronix.repository.TeacherRepository;
import com.heronix.service.TeacherCertificationService;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeacherCertificationManagementController {

    private final TeacherCertificationService certificationService;
    private final TeacherRepository teacherRepository;

    // Filters
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private TextField searchField;
    @FXML private CheckBox expiringSoonToggle;

    // Summary labels
    @FXML private Label totalLabel;
    @FXML private Label activeLabel;
    @FXML private Label expiringSoonLabel;
    @FXML private Label expiredLabel;
    @FXML private Label hqtLabel;
    @FXML private Label outOfFieldLabel;

    // Table
    @FXML private TableView<TeacherCertification> certTable;
    @FXML private TableColumn<TeacherCertification, String> teacherNameCol;
    @FXML private TableColumn<TeacherCertification, String> certNumberCol;
    @FXML private TableColumn<TeacherCertification, String> certTypeCol;
    @FXML private TableColumn<TeacherCertification, String> certTitleCol;
    @FXML private TableColumn<TeacherCertification, String> expirationCol;
    @FXML private TableColumn<TeacherCertification, String> daysLeftCol;
    @FXML private TableColumn<TeacherCertification, String> statusCol;
    @FXML private TableColumn<TeacherCertification, String> subjectsCol;
    @FXML private TableColumn<TeacherCertification, String> hqtCol;
    @FXML private TableColumn<TeacherCertification, Void> actionsCol;

    // Compliance tab
    @FXML private Label complianceActiveLabel;
    @FXML private Label complianceExpiringSoonLabel;
    @FXML private Label complianceExpiredLabel;
    @FXML private Label compliancePendingRenewalLabel;
    @FXML private VBox statusDistributionBox;
    @FXML private VBox typeDistributionBox;

    private ObservableList<TeacherCertification> allData = FXCollections.observableArrayList();
    private ObservableList<TeacherCertification> filteredData = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadData();
    }

    private void setupTable() {
        teacherNameCol.setCellValueFactory(cell -> {
            Teacher t = cell.getValue().getTeacher();
            return new SimpleStringProperty(t != null ? t.getFullName() : "");
        });
        certNumberCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCertificateNumber()));
        certTypeCol.setCellValueFactory(cell -> {
            CertificateType type = cell.getValue().getCertificateType();
            return new SimpleStringProperty(type != null ? type.name().replace("_", " ") : "");
        });
        certTitleCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCertificateTitle()));
        expirationCol.setCellValueFactory(cell -> {
            LocalDate exp = cell.getValue().getExpirationDate();
            return new SimpleStringProperty(exp != null ? exp.format(DATE_FMT) : "");
        });
        daysLeftCol.setCellValueFactory(cell -> {
            int days = cell.getValue().getDaysUntilExpiration();
            return new SimpleStringProperty(String.valueOf(days));
        });
        daysLeftCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    try {
                        int days = Integer.parseInt(item);
                        if (days < 0) setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                        else if (days <= 90) setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                        else setStyle("-fx-text-fill: #10b981;");
                    } catch (NumberFormatException e) {
                        setStyle("");
                    }
                }
            }
        });
        statusCol.setCellValueFactory(cell -> {
            CertificationStatus s = cell.getValue().getStatus();
            return new SimpleStringProperty(s != null ? s.name().replace("_", " ") : "");
        });
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "ACTIVE" -> setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                        case "EXPIRING SOON" -> setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                        case "EXPIRED" -> setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                        case "PENDING RENEWAL" -> setStyle("-fx-text-fill: #6366f1; -fx-font-weight: bold;");
                        case "RENEWED" -> setStyle("-fx-text-fill: #06b6d4; -fx-font-weight: bold;");
                        case "DENIED" -> setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                        default -> setStyle("-fx-text-fill: #64748b;");
                    }
                }
            }
        });
        subjectsCol.setCellValueFactory(cell -> {
            List<String> subjects = cell.getValue().getSubjects();
            return new SimpleStringProperty(subjects != null ? String.join(", ", subjects) : "");
        });
        hqtCol.setCellValueFactory(cell -> {
            Boolean hqt = cell.getValue().getHighlyQualified();
            return new SimpleStringProperty(Boolean.TRUE.equals(hqt) ? "Yes" : "No");
        });

        setupActionsColumn();
        certTable.setItems(filteredData);
    }

    private void setupActionsColumn() {
        actionsCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                TeacherCertification cert = getTableRow().getItem();
                if (cert == null) {
                    setGraphic(null);
                    return;
                }

                HBox buttons = new HBox(4);
                buttons.setAlignment(Pos.CENTER);

                Button editBtn = new Button("Edit");
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                editBtn.setOnAction(e -> handleEdit(cert));

                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> handleDelete(cert));

                buttons.getChildren().addAll(editBtn, deleteBtn);

                CertificationStatus status = cert.getStatus();
                if (status == CertificationStatus.ACTIVE || status == CertificationStatus.EXPIRING_SOON) {
                    if (!Boolean.TRUE.equals(cert.getRenewalReminderSent())) {
                        Button reminderBtn = new Button("Remind");
                        reminderBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                        reminderBtn.setOnAction(e -> handleSendReminder(cert));
                        buttons.getChildren().add(reminderBtn);
                    }
                    if (!Boolean.TRUE.equals(cert.getRenewalInProgress())) {
                        Button renewBtn = new Button("Renew");
                        renewBtn.setStyle("-fx-background-color: #06b6d4; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                        renewBtn.setOnAction(e -> handleStartRenewal(cert));
                        buttons.getChildren().add(renewBtn);
                    }
                } else if (status == CertificationStatus.PENDING_RENEWAL) {
                    Button approveBtn = new Button("Approve");
                    approveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                    approveBtn.setOnAction(e -> handleApproveRenewal(cert));

                    Button denyBtn = new Button("Deny");
                    denyBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                    denyBtn.setOnAction(e -> handleDenyRenewal(cert));

                    buttons.getChildren().addAll(approveBtn, denyBtn);
                }

                setGraphic(buttons);
            }
        });
    }

    private void setupFilters() {
        ObservableList<String> statuses = FXCollections.observableArrayList("All");
        Arrays.stream(CertificationStatus.values()).forEach(s -> statuses.add(s.name().replace("_", " ")));
        statusFilter.setItems(statuses);
        statusFilter.setValue("All");

        ObservableList<String> types = FXCollections.observableArrayList("All");
        Arrays.stream(CertificateType.values()).forEach(t -> types.add(t.name().replace("_", " ")));
        typeFilter.setItems(types);
        typeFilter.setValue("All");

        statusFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        typeFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        expiringSoonToggle.selectedProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void loadData() {
        new Thread(() -> {
            try {
                List<TeacherCertification> certs = certificationService.getAllCertifications();
                Platform.runLater(() -> {
                    allData.setAll(certs);
                    applyFilters();
                    updateSummary();
                    loadComplianceDashboard();
                });
            } catch (Exception e) {
                log.error("Failed to load certifications", e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load certifications: " + e.getMessage()));
            }
        }).start();
    }

    private void applyFilters() {
        String statusVal = statusFilter.getValue();
        String typeVal = typeFilter.getValue();
        String search = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        boolean expiringSoon = expiringSoonToggle.isSelected();

        filteredData.setAll(allData.stream().filter(c -> {
            if (!"All".equals(statusVal) && c.getStatus() != null) {
                if (!c.getStatus().name().replace("_", " ").equals(statusVal)) return false;
            }
            if (!"All".equals(typeVal) && c.getCertificateType() != null) {
                if (!c.getCertificateType().name().replace("_", " ").equals(typeVal)) return false;
            }
            if (!search.isEmpty()) {
                String teacherName = c.getTeacher() != null ? c.getTeacher().getFullName().toLowerCase() : "";
                String certNum = c.getCertificateNumber() != null ? c.getCertificateNumber().toLowerCase() : "";
                if (!teacherName.contains(search) && !certNum.contains(search)) return false;
            }
            if (expiringSoon) {
                if (c.getDaysUntilExpiration() > 90 || c.getDaysUntilExpiration() < 0) return false;
            }
            return true;
        }).collect(Collectors.toList()));

        updateSummary();
    }

    private void updateSummary() {
        totalLabel.setText(String.valueOf(filteredData.size()));
        activeLabel.setText(String.valueOf(filteredData.stream().filter(c -> c.getStatus() == CertificationStatus.ACTIVE).count()));
        expiringSoonLabel.setText(String.valueOf(filteredData.stream().filter(c -> c.getStatus() == CertificationStatus.EXPIRING_SOON).count()));
        expiredLabel.setText(String.valueOf(filteredData.stream().filter(c -> c.getStatus() == CertificationStatus.EXPIRED).count()));
        hqtLabel.setText(String.valueOf(filteredData.stream().filter(c -> Boolean.TRUE.equals(c.getHighlyQualified())).count()));
        outOfFieldLabel.setText(String.valueOf(filteredData.stream().filter(c -> Boolean.TRUE.equals(c.getOutOfFieldTeaching())).count()));
    }

    private void loadComplianceDashboard() {
        long active = allData.stream().filter(c -> c.getStatus() == CertificationStatus.ACTIVE).count();
        long expSoon = allData.stream().filter(c -> c.getStatus() == CertificationStatus.EXPIRING_SOON).count();
        long expired = allData.stream().filter(c -> c.getStatus() == CertificationStatus.EXPIRED).count();
        long pendingRenewal = allData.stream().filter(c -> c.getStatus() == CertificationStatus.PENDING_RENEWAL).count();

        complianceActiveLabel.setText(String.valueOf(active));
        complianceExpiringSoonLabel.setText(String.valueOf(expSoon));
        complianceExpiredLabel.setText(String.valueOf(expired));
        compliancePendingRenewalLabel.setText(String.valueOf(pendingRenewal));

        // Status distribution
        Map<CertificationStatus, Long> statusDist = allData.stream()
                .filter(c -> c.getStatus() != null)
                .collect(Collectors.groupingBy(TeacherCertification::getStatus, Collectors.counting()));
        statusDistributionBox.getChildren().clear();
        long total = allData.size();
        statusDist.forEach((status, count) -> {
            double pct = total > 0 ? (count * 100.0 / total) : 0;
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Label nameLabel = new Label(status.name().replace("_", " "));
            nameLabel.setPrefWidth(140);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
            ProgressBar bar = new ProgressBar(pct / 100.0);
            bar.setPrefWidth(300);
            bar.setPrefHeight(16);
            HBox.setHgrow(bar, Priority.ALWAYS);
            Label pctLabel = new Label(String.format("%.1f%% (%d)", pct, count));
            pctLabel.setStyle("-fx-text-fill: #64748b;");
            row.getChildren().addAll(nameLabel, bar, pctLabel);
            statusDistributionBox.getChildren().add(row);
        });

        // Type distribution
        Map<CertificateType, Long> typeDist = allData.stream()
                .filter(c -> c.getCertificateType() != null)
                .collect(Collectors.groupingBy(TeacherCertification::getCertificateType, Collectors.counting()));
        typeDistributionBox.getChildren().clear();
        typeDist.forEach((type, count) -> {
            double pct = total > 0 ? (count * 100.0 / total) : 0;
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Label nameLabel = new Label(type.name().replace("_", " "));
            nameLabel.setPrefWidth(140);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
            ProgressBar bar = new ProgressBar(pct / 100.0);
            bar.setPrefWidth(300);
            bar.setPrefHeight(16);
            HBox.setHgrow(bar, Priority.ALWAYS);
            Label pctLabel = new Label(String.format("%.1f%% (%d)", pct, count));
            pctLabel.setStyle("-fx-text-fill: #64748b;");
            row.getChildren().addAll(nameLabel, bar, pctLabel);
            typeDistributionBox.getChildren().add(row);
        });
    }

    // ========================================================================
    // CRUD HANDLERS
    // ========================================================================

    @FXML
    private void handleAdd() {
        showCertificationDialog(null);
    }

    private void handleEdit(TeacherCertification cert) {
        showCertificationDialog(cert);
    }

    private void handleDelete(TeacherCertification cert) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete certification \"" + cert.getCertificateNumber() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    certificationService.deleteCertification(cert.getId());
                    loadData();
                } catch (Exception e) {
                    log.error("Failed to delete certification", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete: " + e.getMessage());
                }
            }
        });
    }

    private void showCertificationDialog(TeacherCertification existing) {
        Dialog<TeacherCertification> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Certification" : "Edit Certification");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(550);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Teacher ComboBox
        ComboBox<Teacher> teacherCombo = new ComboBox<>();
        try {
            teacherCombo.setItems(FXCollections.observableArrayList(teacherRepository.findAll()));
        } catch (Exception e) {
            log.warn("Could not load teachers", e);
        }
        teacherCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Teacher t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? "" : t.getFullName());
            }
        });
        teacherCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Teacher t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? "" : t.getFullName());
            }
        });

        TextField certNumberField = new TextField();
        ComboBox<CertificateType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(CertificateType.values()));
        TextField titleField = new TextField();
        TextField issuingAuthorityField = new TextField();
        DatePicker issueDatePicker = new DatePicker();
        DatePicker expirationDatePicker = new DatePicker();
        TextField gradeLevelsField = new TextField();
        gradeLevelsField.setPromptText("Comma-separated (e.g., K-5, 6-8)");
        TextField subjectsField = new TextField();
        subjectsField.setPromptText("Comma-separated");
        TextField endorsementsField = new TextField();
        endorsementsField.setPromptText("Comma-separated");
        CheckBox hqtCheckbox = new CheckBox("Highly Qualified Teacher");
        TextField documentPathField = new TextField();
        documentPathField.setPromptText("Path to certification document");

        int row = 0;
        grid.add(new Label("Teacher:"), 0, row);
        grid.add(teacherCombo, 1, row++);
        grid.add(new Label("Certificate #:"), 0, row);
        grid.add(certNumberField, 1, row++);
        grid.add(new Label("Type:"), 0, row);
        grid.add(typeCombo, 1, row++);
        grid.add(new Label("Title:"), 0, row);
        grid.add(titleField, 1, row++);
        grid.add(new Label("Issuing Authority:"), 0, row);
        grid.add(issuingAuthorityField, 1, row++);
        grid.add(new Label("Issue Date:"), 0, row);
        grid.add(issueDatePicker, 1, row++);
        grid.add(new Label("Expiration Date:"), 0, row);
        grid.add(expirationDatePicker, 1, row++);
        grid.add(new Label("Grade Levels:"), 0, row);
        grid.add(gradeLevelsField, 1, row++);
        grid.add(new Label("Subjects:"), 0, row);
        grid.add(subjectsField, 1, row++);
        grid.add(new Label("Endorsements:"), 0, row);
        grid.add(endorsementsField, 1, row++);
        grid.add(hqtCheckbox, 1, row++);
        grid.add(new Label("Document Path:"), 0, row);
        grid.add(documentPathField, 1, row++);

        // Pre-populate for edit
        if (existing != null) {
            teacherCombo.setValue(existing.getTeacher());
            certNumberField.setText(existing.getCertificateNumber());
            typeCombo.setValue(existing.getCertificateType());
            titleField.setText(existing.getCertificateTitle());
            issuingAuthorityField.setText(existing.getIssuingAuthority());
            issueDatePicker.setValue(existing.getIssueDate());
            expirationDatePicker.setValue(existing.getExpirationDate());
            if (existing.getGradeLevels() != null) gradeLevelsField.setText(String.join(", ", existing.getGradeLevels()));
            if (existing.getSubjects() != null) subjectsField.setText(String.join(", ", existing.getSubjects()));
            if (existing.getEndorsements() != null) endorsementsField.setText(String.join(", ", existing.getEndorsements()));
            hqtCheckbox.setSelected(Boolean.TRUE.equals(existing.getHighlyQualified()));
            documentPathField.setText(existing.getDocumentPath());
        }

        dialog.getDialogPane().setContent(new ScrollPane(grid));

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                TeacherCertification cert = existing != null ? existing : new TeacherCertification();
                cert.setTeacher(teacherCombo.getValue());
                cert.setCertificateNumber(certNumberField.getText());
                cert.setCertificateType(typeCombo.getValue());
                cert.setCertificateTitle(titleField.getText());
                cert.setIssuingAuthority(issuingAuthorityField.getText());
                cert.setIssueDate(issueDatePicker.getValue());
                cert.setExpirationDate(expirationDatePicker.getValue());
                cert.setGradeLevels(parseCommaSeparated(gradeLevelsField.getText()));
                cert.setSubjects(parseCommaSeparated(subjectsField.getText()));
                cert.setEndorsements(parseCommaSeparated(endorsementsField.getText()));
                cert.setHighlyQualified(hqtCheckbox.isSelected());
                cert.setDocumentPath(documentPathField.getText());
                return cert;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(cert -> {
            try {
                if (existing == null) {
                    certificationService.createCertification(cert);
                } else {
                    certificationService.updateCertification(cert);
                }
                loadData();
            } catch (Exception e) {
                log.error("Failed to save certification", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save: " + e.getMessage());
            }
        });
    }

    // ========================================================================
    // RENEWAL HANDLERS
    // ========================================================================

    private void handleSendReminder(TeacherCertification cert) {
        try {
            certificationService.sendRenewalReminder(cert.getId());
            showAlert(Alert.AlertType.INFORMATION, "Reminder Sent", "Renewal reminder sent for " + cert.getCertificateNumber());
            loadData();
        } catch (Exception e) {
            log.error("Failed to send reminder", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to send reminder: " + e.getMessage());
        }
    }

    private void handleStartRenewal(TeacherCertification cert) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Start renewal process for \"" + cert.getCertificateNumber() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Start Renewal");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    certificationService.startRenewalProcess(cert.getId());
                    loadData();
                } catch (Exception e) {
                    log.error("Failed to start renewal", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to start renewal: " + e.getMessage());
                }
            }
        });
    }

    private void handleApproveRenewal(TeacherCertification cert) {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Approve Renewal");
        dialog.setHeaderText("Set new expiration date for " + cert.getCertificateNumber());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker(LocalDate.now().plusYears(5));
        VBox content = new VBox(10, new Label("New Expiration Date:"), datePicker);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? datePicker.getValue() : null);

        dialog.showAndWait().ifPresent(date -> {
            try {
                certificationService.approveRenewal(cert.getId(), date);
                showAlert(Alert.AlertType.INFORMATION, "Approved", "Renewal approved. New expiration: " + date.format(DATE_FMT));
                loadData();
            } catch (Exception e) {
                log.error("Failed to approve renewal", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve: " + e.getMessage());
            }
        });
    }

    private void handleDenyRenewal(TeacherCertification cert) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Deny Renewal");
        dialog.setHeaderText("Provide reason for denying renewal of " + cert.getCertificateNumber());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Enter reason for denial...");
        reasonArea.setPrefRowCount(4);
        VBox content = new VBox(10, new Label("Reason:"), reasonArea);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? reasonArea.getText() : null);

        dialog.showAndWait().ifPresent(reason -> {
            try {
                certificationService.denyRenewal(cert.getId(), reason);
                showAlert(Alert.AlertType.INFORMATION, "Denied", "Renewal denied for " + cert.getCertificateNumber());
                loadData();
            } catch (Exception e) {
                log.error("Failed to deny renewal", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to deny: " + e.getMessage());
            }
        });
    }

    // ========================================================================
    // UTILITIES
    // ========================================================================

    @FXML
    private void handleClearFilters() {
        statusFilter.setValue("All");
        typeFilter.setValue("All");
        searchField.clear();
        expiringSoonToggle.setSelected(false);
    }

    private List<String> parseCommaSeparated(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
