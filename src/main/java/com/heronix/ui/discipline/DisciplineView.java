package com.heronix.ui.discipline;

import com.heronix.model.domain.BehaviorIncident;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.service.BehaviorIncidentService;
import com.heronix.service.DisciplineManagementService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Discipline Incident View
 * Interface for recording and tracking student discipline incidents.
 *
 * Features:
 * - Incident creation form
 * - Incident list with filtering
 * - Student discipline history
 * - Action/consequence tracking
 * - Parent notification management
 * - Reporting and analytics
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class DisciplineView extends BorderPane {

    // ========================================================================
    // DATA
    // ========================================================================

    private final ObservableList<DisciplineIncident> incidents = FXCollections.observableArrayList();

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private TextField searchField;
    private ComboBox<String> typeFilter;
    private ComboBox<String> statusFilter;
    private DatePicker fromDate;
    private DatePicker toDate;
    private TableView<DisciplineIncident> incidentTable;
    private VBox detailsPane;

    // Teacher Referrals tab components
    private ApplicationContext applicationContext;
    private TableView<BehaviorIncident> referralTable;
    private final ObservableList<BehaviorIncident> pendingReferrals = FXCollections.observableArrayList();
    private VBox referralDetailPane;
    private Label referralCountBadge;
    private Timeline autoRefreshTimeline;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<DisciplineIncident> onIncidentSelect;
    private Consumer<DisciplineIncident> onIncidentSave;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public DisciplineView() {
        this(null);
    }

    public DisciplineView(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        setStyle("-fx-background-color: #F8FAFC;");

        setTop(createToolbar());

        // Create TabPane with existing content and new Teacher Referrals tab
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: All Incidents (existing content)
        Tab allIncidentsTab = new Tab("All Incidents");
        BorderPane allIncidentsContent = new BorderPane();
        allIncidentsContent.setCenter(createMainContent());
        allIncidentsContent.setRight(createDetailsPane());
        allIncidentsTab.setContent(allIncidentsContent);

        // Tab 2: Teacher Referrals
        Tab referralsTab = new Tab("Teacher Referrals");
        referralsTab.setContent(createTeacherReferralsTab());

        tabPane.getTabs().addAll(allIncidentsTab, referralsTab);
        setCenter(tabPane);

        loadDemoData();

        // Load real referral data if Spring context is available
        if (applicationContext != null) {
            loadPendingReferrals();
            startAutoRefresh();
        }

        log.info("DisciplineView initialized with Teacher Referrals tab");
    }

    // ========================================================================
    // TOOLBAR
    // ========================================================================

    private VBox createToolbar() {
        VBox toolbar = new VBox(12);
        toolbar.setPadding(new Insets(16, 24, 16, 24));
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        // Title row
        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Discipline Management");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newIncidentBtn = new Button("+ New Incident");
        newIncidentBtn.getStyleClass().addAll("btn", "btn-primary");
        newIncidentBtn.setOnAction(e -> showNewIncidentDialog());

        Button exportBtn = new Button("Export");
        exportBtn.getStyleClass().addAll("btn", "btn-ghost");

        titleRow.getChildren().addAll(title, spacer, newIncidentBtn, exportBtn);

        // Filter row
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search student or incident...");
        searchField.setPrefWidth(200);

        typeFilter = new ComboBox<>();
        typeFilter.setPromptText("Incident Type");
        typeFilter.getItems().addAll("All Types", "Minor", "Major", "Severe");
        typeFilter.setValue("All Types");

        statusFilter = new ComboBox<>();
        statusFilter.setPromptText("Status");
        statusFilter.getItems().addAll("All Status", "Open", "In Progress", "Resolved", "Appealed");
        statusFilter.setValue("All Status");

        fromDate = new DatePicker(LocalDate.now().minusMonths(1));
        fromDate.setPromptText("From");
        fromDate.setPrefWidth(120);

        toDate = new DatePicker(LocalDate.now());
        toDate.setPromptText("To");
        toDate.setPrefWidth(120);

        filterRow.getChildren().addAll(
            searchField,
            new Label("Type:"), typeFilter,
            new Label("Status:"), statusFilter,
            new Label("Date:"), fromDate, new Label("to"), toDate
        );

        toolbar.getChildren().addAll(titleRow, filterRow);
        return toolbar;
    }

    // ========================================================================
    // MAIN CONTENT
    // ========================================================================

    private VBox createMainContent() {
        VBox content = new VBox(0);
        content.setPadding(new Insets(16));

        // Stats cards
        HBox statsRow = createStatsRow();

        // Incident table
        incidentTable = createIncidentTable();
        VBox.setVgrow(incidentTable, Priority.ALWAYS);

        content.getChildren().addAll(statsRow, incidentTable);
        return content;
    }

    private HBox createStatsRow() {
        HBox stats = new HBox(16);
        stats.setPadding(new Insets(0, 0, 16, 0));

        stats.getChildren().addAll(
            createStatCard("Total Incidents", "47", "This semester", "#3B82F6"),
            createStatCard("Open Cases", "8", "Pending resolution", "#F59E0B"),
            createStatCard("Resolved", "39", "This semester", "#10B981"),
            createStatCard("Repeat Offenders", "5", "3+ incidents", "#EF4444")
        );

        return stats;
    }

    private VBox createStatCard(String title, String value, String subtitle, String color) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
        card.setPrefWidth(180);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        card.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        return card;
    }

    private TableView<DisciplineIncident> createIncidentTable() {
        TableView<DisciplineIncident> table = new TableView<>(incidents);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Date column
        TableColumn<DisciplineIncident, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))));
        dateCol.setPrefWidth(100);

        // Student column
        TableColumn<DisciplineIncident, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStudentName()));
        studentCol.setPrefWidth(150);

        // Grade column
        TableColumn<DisciplineIncident, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGrade()));
        gradeCol.setPrefWidth(60);

        // Type column with badge
        TableColumn<DisciplineIncident, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getIncidentType().getDisplayName()));
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    DisciplineIncident incident = getTableView().getItems().get(getIndex());
                    String color = incident.getIncidentType().getColor();
                    badge.setStyle(String.format("""
                        -fx-background-color: %s20;
                        -fx-text-fill: %s;
                        -fx-font-size: 11px;
                        -fx-font-weight: 600;
                        -fx-padding: 2 8;
                        -fx-background-radius: 4;
                        """, color, color));
                    setGraphic(badge);
                }
            }
        });
        typeCol.setPrefWidth(80);

        // Description column
        TableColumn<DisciplineIncident, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescription()));
        descCol.setPrefWidth(200);

        // Action column
        TableColumn<DisciplineIncident, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getActions().isEmpty() ? "—" : cell.getValue().getActions().get(0).getType()));
        actionCol.setPrefWidth(120);

        // Status column
        TableColumn<DisciplineIncident, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().getDisplayName()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    DisciplineIncident incident = getTableView().getItems().get(getIndex());
                    String color = incident.getStatus().getColor();
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
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(dateCol, studentCol, gradeCol, typeCol, descCol, actionCol, statusCol);

        // Selection listener
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showIncidentDetails(newVal);
                if (onIncidentSelect != null) {
                    onIncidentSelect.accept(newVal);
                }
            }
        });

        return table;
    }

    // ========================================================================
    // DETAILS PANE
    // ========================================================================

    private VBox createDetailsPane() {
        detailsPane = new VBox(16);
        detailsPane.setPrefWidth(320);
        detailsPane.setPadding(new Insets(16));
        detailsPane.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 0 1;");

        Label placeholder = new Label("Select an incident to view details");
        placeholder.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");
        placeholder.setWrapText(true);

        detailsPane.getChildren().add(placeholder);
        return detailsPane;
    }

    private void showIncidentDetails(DisciplineIncident incident) {
        detailsPane.getChildren().clear();

        // Header
        Label header = new Label("Incident Details");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        // Student info
        VBox studentCard = new VBox(4);
        studentCard.setPadding(new Insets(12));
        studentCard.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8;");

        Label studentName = new Label(incident.getStudentName());
        studentName.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Label studentDetails = new Label(String.format("Grade %s • ID: %s", incident.getGrade(), incident.getStudentId()));
        studentDetails.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Hyperlink viewProfile = new Hyperlink("View Student Profile →");
        viewProfile.setStyle("-fx-font-size: 11px;");

        studentCard.getChildren().addAll(studentName, studentDetails, viewProfile);

        // Incident info
        VBox incidentInfo = createDetailSection("Incident Information", List.of(
            new String[]{"Date", incident.getDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))},
            new String[]{"Time", incident.getTime() != null ? incident.getTime().format(DateTimeFormatter.ofPattern("h:mm a")) : "N/A"},
            new String[]{"Location", incident.getLocation()},
            new String[]{"Type", incident.getIncidentType().getDisplayName()},
            new String[]{"Reported By", incident.getReportedBy()},
            new String[]{"Issued By", incident.getIssuedByAdministrator() != null ? incident.getIssuedByAdministrator() : "N/A"}
        ));

        // Description
        VBox descSection = new VBox(4);
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #334155;");
        Label descText = new Label(incident.getDescription());
        descText.setStyle("-fx-font-size: 13px; -fx-text-fill: #0F172A;");
        descText.setWrapText(true);
        descSection.getChildren().addAll(descLabel, descText);

        // Actions taken
        VBox actionsSection = new VBox(8);
        Label actionsLabel = new Label("Actions Taken");
        actionsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");
        actionsSection.getChildren().add(actionsLabel);

        if (incident.getActions().isEmpty()) {
            Label noActions = new Label("No actions recorded");
            noActions.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
            actionsSection.getChildren().add(noActions);
        } else {
            for (DisciplineAction action : incident.getActions()) {
                HBox actionItem = new HBox(8);
                actionItem.setAlignment(Pos.TOP_LEFT);

                Label bullet = new Label("•");
                bullet.setStyle("-fx-text-fill: #64748B;");

                VBox actionInfo = new VBox(2);
                Label actionType = new Label(action.getType());
                actionType.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #0F172A;");
                Label actionDate = new Label(action.getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                actionDate.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
                actionInfo.getChildren().addAll(actionType, actionDate);

                actionItem.getChildren().addAll(bullet, actionInfo);
                actionsSection.getChildren().add(actionItem);
            }
        }

        // Buttons
        HBox buttons = new HBox(8);
        buttons.setPadding(new Insets(16, 0, 0, 0));

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().addAll("btn", "btn-ghost");
        editBtn.setOnAction(e -> showEditIncidentDialog(incident));

        Button addActionBtn = new Button("Add Action");
        addActionBtn.getStyleClass().addAll("btn", "btn-primary");
        addActionBtn.setOnAction(e -> showAddActionDialog(incident));

        buttons.getChildren().addAll(editBtn, addActionBtn);

        detailsPane.getChildren().addAll(header, studentCard, incidentInfo, descSection, actionsSection, buttons);
    }

    private VBox createDetailSection(String title, List<String[]> items) {
        VBox section = new VBox(8);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");
        section.getChildren().add(titleLabel);

        for (String[] item : items) {
            HBox row = new HBox(8);
            Label label = new Label(item[0] + ":");
            label.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
            label.setMinWidth(80);
            Label value = new Label(item[1]);
            value.setStyle("-fx-font-size: 12px; -fx-text-fill: #0F172A;");
            row.getChildren().addAll(label, value);
            section.getChildren().add(row);
        }

        return section;
    }

    // ========================================================================
    // DIALOGS
    // ========================================================================

    private void showNewIncidentDialog() {
        Dialog<DisciplineIncident> dialog = new Dialog<>();
        dialog.setTitle("New Discipline Incident");
        dialog.setHeaderText("Record a new incident");
        dialog.setResizable(true);

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setPrefWidth(500);
        pane.setPrefHeight(500);
        pane.setMaxHeight(Screen.getPrimary().getVisualBounds().getHeight() * 0.85);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));

        // Student search
        TextField studentField = new TextField();
        studentField.setPromptText("Search student...");

        // Date/Time
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField timeField = new TextField();
        timeField.setPromptText("HH:MM AM/PM");

        // Location
        TextField locationField = new TextField();
        locationField.setPromptText("e.g., Cafeteria, Room 204");

        // Type
        ComboBox<IncidentType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(IncidentType.values());
        typeBox.setValue(IncidentType.MINOR);

        // Description
        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the incident...");
        descArea.setPrefRowCount(4);

        // Reporter
        TextField reporterField = new TextField();
        reporterField.setPromptText("Your name");

        // Issued By Administrator
        ComboBox<String> administratorBox = new ComboBox<>();
        administratorBox.getItems().addAll("Dean", "Vice Principal", "Principal", "Assistant Principal", "Counselor", "Other");
        administratorBox.setPromptText("Select administrator...");

        form.add(new Label("Student:"), 0, 0);
        form.add(studentField, 1, 0);
        form.add(new Label("Date:"), 0, 1);
        form.add(datePicker, 1, 1);
        form.add(new Label("Time:"), 0, 2);
        form.add(timeField, 1, 2);
        form.add(new Label("Location:"), 0, 3);
        form.add(locationField, 1, 3);
        form.add(new Label("Type:"), 0, 4);
        form.add(typeBox, 1, 4);
        form.add(new Label("Description:"), 0, 5);
        form.add(descArea, 1, 5);
        form.add(new Label("Reported By:"), 0, 6);
        form.add(reporterField, 1, 6);
        form.add(new Label("Issued By:"), 0, 7);
        form.add(administratorBox, 1, 7);

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.setContent(scrollPane);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                DisciplineIncident incident = new DisciplineIncident();
                incident.setId(UUID.randomUUID().toString());
                incident.setStudentName(studentField.getText());
                incident.setDate(datePicker.getValue());
                incident.setLocation(locationField.getText());
                incident.setIncidentType(typeBox.getValue());
                incident.setDescription(descArea.getText());
                incident.setReportedBy(reporterField.getText());
                incident.setIssuedByAdministrator(administratorBox.getValue());
                incident.setStatus(IncidentStatus.OPEN);
                return incident;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(incident -> {
            incidents.add(0, incident);
            incidentTable.getSelectionModel().select(incident);
        });
    }

    private void showEditIncidentDialog(DisciplineIncident incident) {
        // Similar to new incident dialog but pre-populated
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Incident");
        alert.setContentText("Edit functionality would open here with pre-populated data.");
        alert.showAndWait();
    }

    private void showAddActionDialog(DisciplineIncident incident) {
        Dialog<DisciplineAction> dialog = new Dialog<>();
        dialog.setTitle("Add Action");
        dialog.setHeaderText("Record an action for this incident");
        dialog.setResizable(true);

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox form = new VBox(12);
        form.setPadding(new Insets(20));

        ComboBox<String> actionType = new ComboBox<>();
        actionType.getItems().addAll("Verbal Warning", "Written Warning", "Detention", "In-School Suspension",
                                     "Out-of-School Suspension", "Parent Conference", "Counselor Referral", "Other");
        actionType.setPromptText("Select action type");

        DatePicker actionDate = new DatePicker(LocalDate.now());

        TextArea notes = new TextArea();
        notes.setPromptText("Additional notes...");
        notes.setPrefRowCount(3);

        CheckBox notifyParent = new CheckBox("Notify parent/guardian");
        notifyParent.setSelected(true);

        form.getChildren().addAll(
            new Label("Action Type:"), actionType,
            new Label("Date:"), actionDate,
            new Label("Notes:"), notes,
            notifyParent
        );

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.setContent(scrollPane);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK && actionType.getValue() != null) {
                DisciplineAction action = new DisciplineAction();
                action.setType(actionType.getValue());
                action.setDate(actionDate.getValue());
                action.setNotes(notes.getText());
                return action;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(action -> {
            incident.getActions().add(action);
            incident.setStatus(IncidentStatus.IN_PROGRESS);
            showIncidentDetails(incident);
            incidentTable.refresh();
        });
    }

    // ========================================================================
    // TEACHER REFERRALS TAB
    // ========================================================================

    private BorderPane createTeacherReferralsTab() {
        BorderPane content = new BorderPane();
        content.setStyle("-fx-background-color: #F8FAFC;");

        // Header
        HBox header = new HBox(12);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Pending Teacher Referrals");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        referralCountBadge = new Label("0");
        referralCountBadge.setStyle("-fx-background-color: #EF444420; -fx-text-fill: #EF4444; " +
                "-fx-font-weight: 700; -fx-padding: 2 10; -fx-background-radius: 12; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().addAll("btn", "btn-ghost");
        refreshBtn.setOnAction(e -> loadPendingReferrals());

        header.getChildren().addAll(title, referralCountBadge, spacer, refreshBtn);
        content.setTop(header);

        // Referral table
        referralTable = createReferralTable();
        VBox tableContainer = new VBox(referralTable);
        tableContainer.setPadding(new Insets(16));
        VBox.setVgrow(referralTable, Priority.ALWAYS);
        content.setCenter(tableContainer);

        // Detail / action panel on right
        referralDetailPane = new VBox(16);
        referralDetailPane.setPrefWidth(340);
        referralDetailPane.setPadding(new Insets(16));
        referralDetailPane.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 0 1;");

        Label placeholder = new Label("Select a referral to view details and take action");
        placeholder.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");
        placeholder.setWrapText(true);
        referralDetailPane.getChildren().add(placeholder);

        content.setRight(referralDetailPane);

        return content;
    }

    private TableView<BehaviorIncident> createReferralTable() {
        TableView<BehaviorIncident> table = new TableView<>(pendingReferrals);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No pending teacher referrals"));

        TableColumn<BehaviorIncident, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getIncidentDate() != null ?
                        cell.getValue().getIncidentDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : ""));
        dateCol.setPrefWidth(90);

        TableColumn<BehaviorIncident, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getIncidentTime() != null ?
                        cell.getValue().getIncidentTime().format(DateTimeFormatter.ofPattern("h:mm a")) : ""));
        timeCol.setPrefWidth(80);

        TableColumn<BehaviorIncident, String> teacherCol = new TableColumn<>("Teacher");
        teacherCol.setCellValueFactory(cell -> {
            Teacher t = cell.getValue().getReportingStaff();
            return new SimpleStringProperty(t != null ? t.getFirstName() + " " + t.getLastName() : "");
        });
        teacherCol.setPrefWidth(130);

        TableColumn<BehaviorIncident, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(cell -> {
            Student s = cell.getValue().getStudent();
            return new SimpleStringProperty(s != null ? s.getFirstName() + " " + s.getLastName() : "");
        });
        studentCol.setPrefWidth(130);

        TableColumn<BehaviorIncident, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(cell -> {
            Student s = cell.getValue().getStudent();
            return new SimpleStringProperty(s != null && s.getGradeLevel() != null ?
                    s.getGradeLevel().toString() : "");
        });
        gradeCol.setPrefWidth(55);

        TableColumn<BehaviorIncident, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getBehaviorCategory() != null ?
                        cell.getValue().getBehaviorCategory().name() : ""));
        categoryCol.setPrefWidth(120);

        TableColumn<BehaviorIncident, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getSeverity() != null ? cell.getValue().getSeverity().name() : ""));
        severityCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    String color = switch (item) {
                        case "MAJOR" -> "#EF4444";
                        case "SEVERE" -> "#7C3AED";
                        case "MODERATE" -> "#F59E0B";
                        default -> "#3B82F6";
                    };
                    badge.setStyle(String.format(
                            "-fx-background-color: %s20; -fx-text-fill: %s; " +
                                    "-fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 2 8; -fx-background-radius: 4;",
                            color, color));
                    setGraphic(badge);
                }
            }
        });
        severityCol.setPrefWidth(85);

        TableColumn<BehaviorIncident, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getStatus() != null ? cell.getValue().getStatus().name() : ""));
        statusCol.setPrefWidth(90);

        table.getColumns().addAll(dateCol, timeCol, teacherCol, studentCol, gradeCol, categoryCol, severityCol, statusCol);

        // Selection listener
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showReferralDetails(newVal);
            }
        });

        return table;
    }

    private void showReferralDetails(BehaviorIncident incident) {
        referralDetailPane.getChildren().clear();

        // Header
        Label header = new Label("Referral Details");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        // Student info card
        VBox studentCard = new VBox(4);
        studentCard.setPadding(new Insets(12));
        studentCard.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8;");

        Student student = incident.getStudent();
        Label studentName = new Label(student != null ? student.getFirstName() + " " + student.getLastName() : "Unknown");
        studentName.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Label studentDetails = new Label(String.format("Grade %s • ID: %s",
                student != null && student.getGradeLevel() != null ? student.getGradeLevel() : "N/A",
                student != null ? student.getStudentId() : "N/A"));
        studentDetails.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        studentCard.getChildren().addAll(studentName, studentDetails);

        // Teacher info
        Teacher teacher = incident.getReportingStaff();
        VBox teacherInfo = new VBox(4);
        Label reportedBy = new Label("Reported by: " + (teacher != null ? teacher.getFirstName() + " " + teacher.getLastName() : "Unknown"));
        reportedBy.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Label dateInfo = new Label(String.format("Date: %s at %s",
                incident.getIncidentDate() != null ? incident.getIncidentDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "N/A",
                incident.getIncidentTime() != null ? incident.getIncidentTime().format(DateTimeFormatter.ofPattern("h:mm a")) : "N/A"));
        dateInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Label categoryInfo = new Label("Category: " + (incident.getBehaviorCategory() != null ? incident.getBehaviorCategory().name() : "N/A"));
        categoryInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Label locationInfo = new Label("Location: " + (incident.getLocation() != null ? incident.getLocation() : "N/A"));
        locationInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        teacherInfo.getChildren().addAll(reportedBy, dateInfo, categoryInfo, locationInfo);

        // Description
        VBox descSection = new VBox(4);
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #334155;");

        Label descText = new Label(incident.getIncidentDescription() != null ? incident.getIncidentDescription() : "No description");
        descText.setStyle("-fx-font-size: 12px; -fx-text-fill: #0F172A;");
        descText.setWrapText(true);
        descSection.getChildren().addAll(descLabel, descText);

        // Intervention
        if (incident.getInterventionApplied() != null && !incident.getInterventionApplied().isEmpty()) {
            VBox intSection = new VBox(4);
            Label intLabel = new Label("Intervention Attempted");
            intLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #334155;");

            Label intText = new Label(incident.getInterventionApplied());
            intText.setStyle("-fx-font-size: 12px; -fx-text-fill: #0F172A;");
            intText.setWrapText(true);
            intSection.getChildren().addAll(intLabel, intText);
            referralDetailPane.getChildren().addAll(header, studentCard, teacherInfo, descSection, intSection);
        } else {
            referralDetailPane.getChildren().addAll(header, studentCard, teacherInfo, descSection);
        }

        // Action buttons
        VBox actions = new VBox(8);
        actions.setPadding(new Insets(12, 0, 0, 0));

        Label actionsLabel = new Label("Actions");
        actionsLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #334155;");

        Button acceptBtn = new Button("Accept & Review");
        acceptBtn.setMaxWidth(Double.MAX_VALUE);
        acceptBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 6;");
        acceptBtn.setOnAction(e -> handleAcceptReferral(incident));

        Button consequenceBtn = new Button("Add Consequence");
        consequenceBtn.setMaxWidth(Double.MAX_VALUE);
        consequenceBtn.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 6;");
        consequenceBtn.setOnAction(e -> handleAddConsequence(incident));

        Button resolveBtn = new Button("Resolve");
        resolveBtn.setMaxWidth(Double.MAX_VALUE);
        resolveBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 6;");
        resolveBtn.setOnAction(e -> handleResolveReferral(incident));

        Button escalateBtn = new Button("Escalate");
        escalateBtn.setMaxWidth(Double.MAX_VALUE);
        escalateBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 6;");
        escalateBtn.setOnAction(e -> handleEscalateReferral(incident));

        Button contactParentBtn = new Button("Contact Parent");
        contactParentBtn.setMaxWidth(Double.MAX_VALUE);
        contactParentBtn.getStyleClass().addAll("btn", "btn-ghost");
        contactParentBtn.setOnAction(e -> handleContactParent(incident));

        actions.getChildren().addAll(actionsLabel, acceptBtn, consequenceBtn, resolveBtn, escalateBtn, contactParentBtn);

        referralDetailPane.getChildren().add(actions);
    }

    // ========================================================================
    // REFERRAL ACTIONS
    // ========================================================================

    private void handleAcceptReferral(BehaviorIncident incident) {
        if (applicationContext == null) {
            showActionInfo("Accept & Review", "Would change status to UNDER_REVIEW.");
            return;
        }
        try {
            BehaviorIncidentService incidentService = applicationContext.getBean(BehaviorIncidentService.class);
            incident.setStatus(BehaviorIncident.IncidentStatus.UNDER_REVIEW);
            incidentService.saveIncident(incident);
            log.info("Referral {} accepted for review", incident.getId());
            loadPendingReferrals();
            showActionInfo("Referral Accepted", "Status changed to UNDER_REVIEW. A disciplinary referral record has been created.");
        } catch (Exception e) {
            log.error("Failed to accept referral", e);
            showActionInfo("Error", "Failed to accept referral: " + e.getMessage());
        }
    }

    private void handleAddConsequence(BehaviorIncident incident) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Consequence");
        dialog.setHeaderText("Add consequence for this incident");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox form = new VBox(12);
        form.setPadding(new Insets(20));

        ComboBox<String> actionType = new ComboBox<>();
        actionType.getItems().addAll("Verbal Warning", "Written Warning", "Detention",
                "In-School Suspension", "Out-of-School Suspension", "Parent Conference",
                "Counselor Referral", "Behavior Contract", "Loss of Privilege", "Other");
        actionType.setPromptText("Select consequence type");

        TextArea notes = new TextArea();
        notes.setPromptText("Additional notes...");
        notes.setPrefRowCount(3);

        form.getChildren().addAll(new Label("Consequence Type:"), actionType, new Label("Notes:"), notes);
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK && actionType.getValue() != null) {
                return actionType.getValue() + ": " + (notes.getText() != null ? notes.getText() : "");
            }
            return null;
        });

        dialog.showAndWait().ifPresent(outcome -> {
            if (applicationContext != null) {
                try {
                    BehaviorIncidentService incidentService = applicationContext.getBean(BehaviorIncidentService.class);
                    incidentService.recordIntervention(incident.getId(), outcome);
                    log.info("Consequence added to referral {}: {}", incident.getId(), outcome);
                    loadPendingReferrals();
                } catch (Exception e) {
                    log.error("Failed to add consequence", e);
                }
            }
            showActionInfo("Consequence Added", "Consequence recorded: " + outcome);
        });
    }

    private void handleResolveReferral(BehaviorIncident incident) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Resolve Referral");
        dialog.setHeaderText("Resolve this referral");
        dialog.setContentText("Resolution notes:");

        dialog.showAndWait().ifPresent(notes -> {
            if (applicationContext != null) {
                try {
                    DisciplineManagementService disciplineService = applicationContext.getBean(DisciplineManagementService.class);
                    disciplineService.processReferral(incident.getId(), "RESOLVED: " + notes, LocalDate.now());
                    log.info("Referral {} resolved", incident.getId());
                    loadPendingReferrals();
                } catch (Exception e) {
                    log.error("Failed to resolve referral", e);
                }
            }
            showActionInfo("Referral Resolved", "This referral has been resolved.");
        });
    }

    private void handleEscalateReferral(BehaviorIncident incident) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Escalate Referral");
        dialog.setHeaderText("Escalate this referral");
        dialog.setContentText("Escalation reason:");

        dialog.showAndWait().ifPresent(reason -> {
            if (applicationContext != null) {
                try {
                    BehaviorIncidentService incidentService = applicationContext.getBean(BehaviorIncidentService.class);
                    incident.setStatus(BehaviorIncident.IncidentStatus.ESCALATED);
                    incidentService.recordIntervention(incident.getId(), "ESCALATED: " + reason);
                    incidentService.saveIncident(incident);
                    log.info("Referral {} escalated: {}", incident.getId(), reason);
                    loadPendingReferrals();
                } catch (Exception e) {
                    log.error("Failed to escalate referral", e);
                }
            }
            showActionInfo("Referral Escalated", "This referral has been escalated for further review.");
        });
    }

    private void handleContactParent(BehaviorIncident incident) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Contact Parent");
        dialog.setHeaderText("Record parent contact");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox form = new VBox(12);
        form.setPadding(new Insets(20));

        ComboBox<String> methodCombo = new ComboBox<>();
        methodCombo.getItems().addAll("PHONE", "EMAIL", "IN_PERSON", "LETTER", "TEXT_MESSAGE", "PARENT_PORTAL");
        methodCombo.setPromptText("Contact method");
        methodCombo.setValue("PHONE");

        DatePicker contactDate = new DatePicker(LocalDate.now());

        form.getChildren().addAll(new Label("Contact Method:"), methodCombo, new Label("Contact Date:"), contactDate);
        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(button -> {
            if (button == ButtonType.OK && applicationContext != null) {
                try {
                    BehaviorIncidentService incidentService = applicationContext.getBean(BehaviorIncidentService.class);
                    incidentService.recordParentContact(incident.getId(), contactDate.getValue(),
                            BehaviorIncident.ContactMethod.valueOf(methodCombo.getValue()));
                    log.info("Parent contact recorded for referral {}", incident.getId());
                    loadPendingReferrals();
                } catch (Exception e) {
                    log.error("Failed to record parent contact", e);
                }
            }
        });
    }

    // ========================================================================
    // REFERRAL DATA LOADING
    // ========================================================================

    private void loadPendingReferrals() {
        if (applicationContext == null) return;

        new Thread(() -> {
            try {
                DisciplineManagementService disciplineService = applicationContext.getBean(DisciplineManagementService.class);
                List<BehaviorIncident> referrals = disciplineService.getPendingReferrals();

                Platform.runLater(() -> {
                    pendingReferrals.setAll(referrals);
                    referralCountBadge.setText(String.valueOf(referrals.size()));
                    log.debug("Loaded {} pending referrals", referrals.size());
                });
            } catch (Exception e) {
                log.error("Failed to load pending referrals", e);
            }
        }).start();
    }

    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> loadPendingReferrals()));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
        log.info("Auto-refresh started (30s interval)");
    }

    public void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
    }

    private void showActionInfo(String title, String message) {
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
        incidents.addAll(
            createIncident("Emma Johnson", "10", IncidentType.MINOR, "Tardy to class (3rd occurrence)",
                          "Main Hallway", IncidentStatus.RESOLVED, LocalDate.now().minusDays(2)),
            createIncident("Liam Williams", "9", IncidentType.MAJOR, "Disruptive behavior in classroom",
                          "Room 204", IncidentStatus.IN_PROGRESS, LocalDate.now().minusDays(1)),
            createIncident("Olivia Brown", "11", IncidentType.MINOR, "Cell phone use during class",
                          "Room 108", IncidentStatus.OPEN, LocalDate.now()),
            createIncident("Noah Davis", "10", IncidentType.SEVERE, "Physical altercation with another student",
                          "Cafeteria", IncidentStatus.IN_PROGRESS, LocalDate.now().minusDays(5)),
            createIncident("Ava Martinez", "12", IncidentType.MINOR, "Dress code violation",
                          "Main Entrance", IncidentStatus.RESOLVED, LocalDate.now().minusDays(7))
        );
    }

    private DisciplineIncident createIncident(String student, String grade, IncidentType type,
                                              String desc, String location, IncidentStatus status, LocalDate date) {
        DisciplineIncident incident = new DisciplineIncident();
        incident.setId(UUID.randomUUID().toString());
        incident.setStudentId("S" + String.format("%04d", new Random().nextInt(9999)));
        incident.setStudentName(student);
        incident.setGrade(grade);
        incident.setIncidentType(type);
        incident.setDescription(desc);
        incident.setLocation(location);
        incident.setStatus(status);
        incident.setDate(date);
        incident.setReportedBy("Mr. Smith");
        incident.setIssuedByAdministrator("Dean");

        if (status == IncidentStatus.IN_PROGRESS || status == IncidentStatus.RESOLVED) {
            DisciplineAction action = new DisciplineAction();
            action.setType(type == IncidentType.SEVERE ? "Parent Conference" : "Verbal Warning");
            action.setDate(date.plusDays(1));
            incident.getActions().add(action);
        }

        return incident;
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    public void setOnIncidentSelect(Consumer<DisciplineIncident> callback) {
        this.onIncidentSelect = callback;
    }

    public void setOnIncidentSave(Consumer<DisciplineIncident> callback) {
        this.onIncidentSave = callback;
    }

    // ========================================================================
    // INNER CLASSES
    // ========================================================================

    @Getter @Setter
    public static class DisciplineIncident {
        private String id;
        private String studentId;
        private String studentName;
        private String grade;
        private LocalDate date;
        private java.time.LocalTime time;
        private String location;
        private IncidentType incidentType;
        private String description;
        private String reportedBy;
        private IncidentStatus status;
        private List<DisciplineAction> actions = new ArrayList<>();
        private List<String> witnesses = new ArrayList<>();
        private String parentNotified;
        private String issuedByAdministrator;
    }

    @Getter @Setter
    public static class DisciplineAction {
        private String id;
        private String type;
        private LocalDate date;
        private String notes;
        private String assignedBy;
        private boolean completed;
    }

    @Getter
    public enum IncidentType {
        MINOR("Minor", "#3B82F6"),
        MAJOR("Major", "#F59E0B"),
        SEVERE("Severe", "#EF4444");

        private final String displayName;
        private final String color;

        IncidentType(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
    }

    @Getter
    public enum IncidentStatus {
        OPEN("Open", "#3B82F6"),
        IN_PROGRESS("In Progress", "#F59E0B"),
        RESOLVED("Resolved", "#10B981"),
        APPEALED("Appealed", "#8B5CF6");

        private final String displayName;
        private final String color;

        IncidentStatus(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
    }
}
