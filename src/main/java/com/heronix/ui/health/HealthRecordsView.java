package com.heronix.ui.health;

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
 * Health Records View
 * Interface for managing student health information.
 *
 * Features:
 * - Immunization tracking
 * - Medical conditions/allergies
 * - Medications administration log
 * - Health screenings
 * - Emergency contacts
 * - Nurse visit log
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class HealthRecordsView extends BorderPane {

    // ========================================================================
    // DATA
    // ========================================================================

    @Getter @Setter
    private String studentId;

    @Getter @Setter
    private String studentName;

    private final ObservableList<Immunization> immunizations = FXCollections.observableArrayList();
    private final ObservableList<MedicalCondition> conditions = FXCollections.observableArrayList();
    private final ObservableList<Medication> medications = FXCollections.observableArrayList();
    private final ObservableList<NurseVisit> nurseVisits = FXCollections.observableArrayList();

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private TabPane tabPane;
    private VBox summaryPane;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<String> onSave;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public HealthRecordsView() {
        setStyle("-fx-background-color: #F8FAFC;");

        setTop(createHeader());
        setLeft(createSummaryPane());
        setCenter(createTabPane());

        loadDemoData();

        log.info("HealthRecordsView initialized");
    }

    // ========================================================================
    // HEADER
    // ========================================================================

    private VBox createHeader() {
        VBox header = new VBox(8);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Health Records");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        // Student selector (in real app, would search students)
        TextField studentSearch = new TextField();
        studentSearch.setPromptText("Search student...");
        studentSearch.setPrefWidth(250);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button printBtn = new Button("Print Records");
        printBtn.getStyleClass().addAll("btn", "btn-ghost");

        Button exportBtn = new Button("Export PDF");
        exportBtn.getStyleClass().addAll("btn", "btn-primary");

        titleRow.getChildren().addAll(title, studentSearch, spacer, printBtn, exportBtn);

        header.getChildren().add(titleRow);
        return header;
    }

    // ========================================================================
    // SUMMARY PANE
    // ========================================================================

    private VBox createSummaryPane() {
        summaryPane = new VBox(16);
        summaryPane.setPrefWidth(280);
        summaryPane.setPadding(new Insets(16));
        summaryPane.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 1 0 0;");

        // Student info card
        VBox studentCard = new VBox(8);
        studentCard.setPadding(new Insets(16));
        studentCard.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8;");

        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 40px;");

        Label nameLabel = new Label("Emma Johnson");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Label detailsLabel = new Label("Grade 10 • ID: S1001\nDOB: 05/15/2010 (15 years)");
        detailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        studentCard.getChildren().addAll(avatar, nameLabel, detailsLabel);

        // Quick status cards
        VBox immunizationStatus = createStatusCard("Immunizations", "Up to Date", "#10B981", "✓");
        VBox conditionStatus = createStatusCard("Medical Alerts", "2 Conditions", "#F59E0B", "⚠");
        VBox medicationStatus = createStatusCard("Medications", "1 Active", "#3B82F6", "💊");

        // Emergency contacts
        Label emergencyTitle = new Label("Emergency Contacts");
        emergencyTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #0F172A; -fx-padding: 8 0 0 0;");

        VBox contact1 = createContactCard("Sarah Johnson", "Mother", "(555) 123-4567", true);
        VBox contact2 = createContactCard("Michael Johnson", "Father", "(555) 987-6543", false);

        summaryPane.getChildren().addAll(studentCard, immunizationStatus, conditionStatus, medicationStatus,
                                        emergencyTitle, contact1, contact2);

        return summaryPane;
    }

    private VBox createStatusCard(String title, String value, String color, String icon) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        header.getChildren().addAll(iconLabel, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(header, valueLabel);
        return card;
    }

    private VBox createContactCard(String name, String relation, String phone, boolean primary) {
        VBox card = new VBox(2);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 6;");

        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #0F172A;");

        if (primary) {
            Label badge = new Label("Primary");
            badge.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #2563EB; -fx-font-size: 9px; -fx-padding: 1 4; -fx-background-radius: 3;");
            nameRow.getChildren().add(badge);
        }

        nameRow.getChildren().add(0, nameLabel);

        Label relationLabel = new Label(relation + " • " + phone);
        relationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        card.getChildren().addAll(nameRow, relationLabel);
        return card;
    }

    // ========================================================================
    // TAB PANE
    // ========================================================================

    private TabPane createTabPane() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("health-tabs");

        Tab immunizationsTab = new Tab("Immunizations", createImmunizationsPane());
        Tab conditionsTab = new Tab("Medical Conditions", createConditionsPane());
        Tab medicationsTab = new Tab("Medications", createMedicationsPane());
        Tab screeningsTab = new Tab("Health Screenings", createScreeningsPane());
        Tab visitsTab = new Tab("Nurse Visits", createVisitsPane());

        tabPane.getTabs().addAll(immunizationsTab, conditionsTab, medicationsTab, screeningsTab, visitsTab);

        return tabPane;
    }

    // ========================================================================
    // IMMUNIZATIONS TAB
    // ========================================================================

    private VBox createImmunizationsPane() {
        VBox pane = new VBox(16);
        pane.setPadding(new Insets(16));

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Immunization Records");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Immunization");
        addBtn.getStyleClass().addAll("btn", "btn-primary", "btn-sm");
        addBtn.setOnAction(e -> showAddImmunizationDialog());

        header.getChildren().addAll(title, spacer, addBtn);

        // Immunization table
        TableView<Immunization> table = new TableView<>(immunizations);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Immunization, String> vaccineCol = new TableColumn<>("Vaccine");
        vaccineCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getVaccineName()));
        vaccineCol.setPrefWidth(200);

        TableColumn<Immunization, String> doseCol = new TableColumn<>("Dose");
        doseCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDoseNumber()));
        doseCol.setPrefWidth(80);

        TableColumn<Immunization, String> dateCol = new TableColumn<>("Date Given");
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getDateGiven().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))));
        dateCol.setPrefWidth(100);

        TableColumn<Immunization, String> providerCol = new TableColumn<>("Provider");
        providerCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProvider()));
        providerCol.setPrefWidth(150);

        TableColumn<Immunization, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    String color = item.equals("Complete") ? "#10B981" : "#F59E0B";
                    badge.setStyle(String.format("-fx-background-color: %s20; -fx-text-fill: %s; -fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 4;", color, color));
                    setGraphic(badge);
                }
            }
        });
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(vaccineCol, doseCol, dateCol, providerCol, statusCol);

        pane.getChildren().addAll(header, table);
        return pane;
    }

    // ========================================================================
    // MEDICAL CONDITIONS TAB
    // ========================================================================

    private VBox createConditionsPane() {
        VBox pane = new VBox(16);
        pane.setPadding(new Insets(16));

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Medical Conditions & Allergies");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Condition");
        addBtn.getStyleClass().addAll("btn", "btn-primary", "btn-sm");

        header.getChildren().addAll(title, spacer, addBtn);

        // Conditions list
        VBox conditionsList = new VBox(12);
        VBox.setVgrow(conditionsList, Priority.ALWAYS);

        for (MedicalCondition condition : conditions) {
            conditionsList.getChildren().add(createConditionCard(condition));
        }

        ScrollPane scrollPane = new ScrollPane(conditionsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        pane.getChildren().addAll(header, scrollPane);
        return pane;
    }

    private VBox createConditionCard(MedicalCondition condition) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label severityIcon = new Label(condition.getSeverity().equals("Severe") ? "🔴" :
                                       condition.getSeverity().equals("Moderate") ? "🟡" : "🟢");

        Label nameLabel = new Label(condition.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Label typeLabel = new Label(condition.getType());
        typeLabel.setStyle("-fx-background-color: #E0E7FF; -fx-text-fill: #3730A3; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");

        titleRow.getChildren().addAll(severityIcon, nameLabel, typeLabel, spacer, editBtn);

        Label descLabel = new Label(condition.getDescription());
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        descLabel.setWrapText(true);

        if (condition.getActionPlan() != null && !condition.getActionPlan().isEmpty()) {
            Label actionTitle = new Label("Action Plan:");
            actionTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #334155;");

            Label actionLabel = new Label(condition.getActionPlan());
            actionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #0F172A;");
            actionLabel.setWrapText(true);

            card.getChildren().addAll(titleRow, descLabel, actionTitle, actionLabel);
        } else {
            card.getChildren().addAll(titleRow, descLabel);
        }

        return card;
    }

    // ========================================================================
    // MEDICATIONS TAB
    // ========================================================================

    private VBox createMedicationsPane() {
        VBox pane = new VBox(16);
        pane.setPadding(new Insets(16));

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Medications");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Medication");
        addBtn.getStyleClass().addAll("btn", "btn-primary", "btn-sm");

        header.getChildren().addAll(title, spacer, addBtn);

        // Active medications
        Label activeLabel = new Label("Active Medications");
        activeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #334155;");

        VBox medicationsList = new VBox(12);

        for (Medication med : medications) {
            if (med.isActive()) {
                medicationsList.getChildren().add(createMedicationCard(med));
            }
        }

        pane.getChildren().addAll(header, activeLabel, medicationsList);
        return pane;
    }

    private VBox createMedicationCard(Medication med) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(med.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Label dosageLabel = new Label(med.getDosage());
        dosageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logBtn = new Button("Log Administration");
        logBtn.getStyleClass().addAll("btn", "btn-sm");

        titleRow.getChildren().addAll(nameLabel, dosageLabel, spacer, logBtn);

        // Schedule
        HBox scheduleRow = new HBox(16);

        Label scheduleLabel = new Label("Schedule: " + med.getSchedule());
        scheduleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Label nextDoseLabel = new Label("Next dose: " + med.getNextDose());
        nextDoseLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2563EB;");

        scheduleRow.getChildren().addAll(scheduleLabel, nextDoseLabel);

        // Prescriber
        Label prescriberLabel = new Label("Prescribed by: " + med.getPrescriber());
        prescriberLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        card.getChildren().addAll(titleRow, scheduleRow, prescriberLabel);
        return card;
    }

    // ========================================================================
    // SCREENINGS TAB
    // ========================================================================

    private VBox createScreeningsPane() {
        VBox pane = new VBox(16);
        pane.setPadding(new Insets(16));

        Label title = new Label("Health Screenings");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        // Screening cards grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        grid.add(createScreeningCard("Vision", "20/20", "09/15/2025", "Pass", "#10B981"), 0, 0);
        grid.add(createScreeningCard("Hearing", "Normal", "09/15/2025", "Pass", "#10B981"), 1, 0);
        grid.add(createScreeningCard("Height", "5'4\"", "09/15/2025", "—", "#64748B"), 0, 1);
        grid.add(createScreeningCard("Weight", "120 lbs", "09/15/2025", "—", "#64748B"), 1, 1);
        grid.add(createScreeningCard("BMI", "20.6", "09/15/2025", "Normal", "#10B981"), 0, 2);
        grid.add(createScreeningCard("Scoliosis", "—", "09/15/2025", "Pass", "#10B981"), 1, 2);

        pane.getChildren().addAll(title, grid);
        return pane;
    }

    private VBox createScreeningCard(String name, String result, String date, String status, String statusColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + statusColor + "; -fx-font-weight: 600;");

        header.getChildren().addAll(nameLabel, spacer, statusLabel);

        Label resultLabel = new Label(result);
        resultLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        Label dateLabel = new Label("Last screened: " + date);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        card.getChildren().addAll(header, resultLabel, dateLabel);
        return card;
    }

    // ========================================================================
    // NURSE VISITS TAB
    // ========================================================================

    private VBox createVisitsPane() {
        VBox pane = new VBox(16);
        pane.setPadding(new Insets(16));

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Nurse Visit Log");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Log Visit");
        addBtn.getStyleClass().addAll("btn", "btn-primary", "btn-sm");

        header.getChildren().addAll(title, spacer, addBtn);

        // Visits table
        TableView<NurseVisit> table = new TableView<>(nurseVisits);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<NurseVisit, String> dateCol = new TableColumn<>("Date/Time");
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getVisitTime().format(DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a"))));
        dateCol.setPrefWidth(150);

        TableColumn<NurseVisit, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getReason()));
        reasonCol.setPrefWidth(150);

        TableColumn<NurseVisit, String> symptomsCol = new TableColumn<>("Symptoms");
        symptomsCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSymptoms()));
        symptomsCol.setPrefWidth(200);

        TableColumn<NurseVisit, String> actionCol = new TableColumn<>("Action Taken");
        actionCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getActionTaken()));
        actionCol.setPrefWidth(150);

        TableColumn<NurseVisit, String> dispositionCol = new TableColumn<>("Disposition");
        dispositionCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDisposition()));
        dispositionCol.setPrefWidth(120);

        table.getColumns().addAll(dateCol, reasonCol, symptomsCol, actionCol, dispositionCol);

        pane.getChildren().addAll(header, table);
        return pane;
    }

    // ========================================================================
    // DIALOGS
    // ========================================================================

    private void showAddImmunizationDialog() {
        Dialog<Immunization> dialog = new Dialog<>();
        dialog.setTitle("Add Immunization Record");
        dialog.setHeaderText("Enter immunization details");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));

        ComboBox<String> vaccineBox = new ComboBox<>();
        vaccineBox.getItems().addAll("DTaP", "Tdap", "MMR", "Polio", "Hepatitis B", "Varicella",
                                    "Meningococcal", "HPV", "Influenza", "COVID-19");
        vaccineBox.setPromptText("Select vaccine");

        TextField doseField = new TextField();
        doseField.setPromptText("e.g., 1st dose, Booster");

        DatePicker datePicker = new DatePicker();

        TextField providerField = new TextField();
        providerField.setPromptText("Healthcare provider");

        form.add(new Label("Vaccine:"), 0, 0);
        form.add(vaccineBox, 1, 0);
        form.add(new Label("Dose:"), 0, 1);
        form.add(doseField, 1, 1);
        form.add(new Label("Date Given:"), 0, 2);
        form.add(datePicker, 1, 2);
        form.add(new Label("Provider:"), 0, 3);
        form.add(providerField, 1, 3);

        pane.setContent(form);

        dialog.showAndWait();
    }

    // ========================================================================
    // DEMO DATA
    // ========================================================================

    private void loadDemoData() {
        // Immunizations
        immunizations.addAll(
            new Immunization("DTaP", "5th dose", LocalDate.of(2020, 8, 15), "Dr. Smith Pediatrics", "Complete"),
            new Immunization("Tdap", "Booster", LocalDate.of(2024, 9, 10), "School Clinic", "Complete"),
            new Immunization("MMR", "2nd dose", LocalDate.of(2016, 4, 20), "Children's Hospital", "Complete"),
            new Immunization("Polio", "4th dose", LocalDate.of(2020, 8, 15), "Dr. Smith Pediatrics", "Complete"),
            new Immunization("Hepatitis B", "3rd dose", LocalDate.of(2011, 6, 1), "Birth Hospital", "Complete"),
            new Immunization("Meningococcal", "1st dose", LocalDate.of(2024, 9, 10), "School Clinic", "Due for 2nd")
        );

        // Medical conditions
        conditions.addAll(
            new MedicalCondition("Peanut Allergy", "Allergy", "Severe",
                "Severe allergic reaction to peanuts and peanut products",
                "EpiPen available in nurse's office. Call 911 immediately if exposed."),
            new MedicalCondition("Asthma", "Respiratory", "Moderate",
                "Exercise-induced asthma, well-controlled with medication",
                "Rescue inhaler permitted for self-carry. Notify nurse before PE class.")
        );

        // Medications
        medications.add(new Medication("Albuterol Inhaler", "2 puffs as needed", "As needed for asthma symptoms",
            "Dr. Johnson", true, "N/A"));

        // Nurse visits
        nurseVisits.addAll(
            new NurseVisit(java.time.LocalDateTime.now().minusDays(5), "Headache", "Mild headache, no fever",
                "Rest, water, Tylenol 500mg", "Returned to class"),
            new NurseVisit(java.time.LocalDateTime.now().minusDays(15), "Stomach ache", "Nausea, no vomiting",
                "Rest, observation", "Returned to class after 30 min"),
            new NurseVisit(java.time.LocalDateTime.now().minusMonths(1), "Asthma", "Wheezing after PE",
                "Administered rescue inhaler", "Returned to class")
        );
    }

    // ========================================================================
    // INNER CLASSES
    // ========================================================================

    @Getter @Setter
    public static class Immunization {
        private String vaccineName;
        private String doseNumber;
        private LocalDate dateGiven;
        private String provider;
        private String status;

        public Immunization(String vaccineName, String doseNumber, LocalDate dateGiven, String provider, String status) {
            this.vaccineName = vaccineName;
            this.doseNumber = doseNumber;
            this.dateGiven = dateGiven;
            this.provider = provider;
            this.status = status;
        }
    }

    @Getter @Setter
    public static class MedicalCondition {
        private String name;
        private String type;
        private String severity;
        private String description;
        private String actionPlan;

        public MedicalCondition(String name, String type, String severity, String description, String actionPlan) {
            this.name = name;
            this.type = type;
            this.severity = severity;
            this.description = description;
            this.actionPlan = actionPlan;
        }
    }

    @Getter @Setter
    public static class Medication {
        private String name;
        private String dosage;
        private String schedule;
        private String prescriber;
        private boolean active;
        private String nextDose;

        public Medication(String name, String dosage, String schedule, String prescriber, boolean active, String nextDose) {
            this.name = name;
            this.dosage = dosage;
            this.schedule = schedule;
            this.prescriber = prescriber;
            this.active = active;
            this.nextDose = nextDose;
        }
    }

    @Getter @Setter
    public static class NurseVisit {
        private java.time.LocalDateTime visitTime;
        private String reason;
        private String symptoms;
        private String actionTaken;
        private String disposition;

        public NurseVisit(java.time.LocalDateTime visitTime, String reason, String symptoms, String actionTaken, String disposition) {
            this.visitTime = visitTime;
            this.reason = reason;
            this.symptoms = symptoms;
            this.actionTaken = actionTaken;
            this.disposition = disposition;
        }
    }
}
