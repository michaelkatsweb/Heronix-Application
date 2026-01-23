package com.heronix.ui.reports;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Scheduled Report View
 * Configure and manage automated report delivery.
 *
 * Features:
 * - Schedule reports (daily, weekly, monthly)
 * - Multiple delivery methods (email, SFTP, cloud storage)
 * - Recipient management
 * - Schedule history and logs
 * - Pause/resume schedules
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class ScheduledReportView extends BorderPane {

    // ========================================================================
    // DATA
    // ========================================================================

    private final ObservableList<ScheduledReport> schedules = FXCollections.observableArrayList();
    private final ObservableList<ScheduledReport> filteredSchedules = FXCollections.observableArrayList();
    private final ObjectProperty<ScheduledReport> selectedSchedule = new SimpleObjectProperty<>();

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private TableView<ScheduledReport> scheduleTable;
    private VBox detailPane;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<ScheduledReport> onScheduleCreated;
    private Consumer<ScheduledReport> onScheduleUpdated;
    private Consumer<ScheduledReport> onScheduleDeleted;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public ScheduledReportView() {
        getStyleClass().add("scheduled-reports");
        setStyle("-fx-background-color: #F8FAFC;");

        // Header
        setTop(createHeader());

        // Main content
        SplitPane content = new SplitPane();
        content.setDividerPositions(0.65);

        // Left: Schedule table
        VBox tablePane = createTablePane();

        // Right: Detail/Create pane
        detailPane = createDetailPane();

        content.getItems().addAll(tablePane, detailPane);
        setCenter(content);

        // Load demo data
        loadDemoSchedules();
        filterSchedules();
    }

    // ========================================================================
    // HEADER
    // ========================================================================

    private HBox createHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        // Title
        VBox titleBox = new VBox(2);
        Label title = new Label("Scheduled Reports");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");
        Label subtitle = new Label("Automate report generation and delivery");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Stats
        HBox stats = new HBox(24);
        stats.setAlignment(Pos.CENTER_RIGHT);

        stats.getChildren().addAll(
            createStatBadge("Active", "12", "#10B981"),
            createStatBadge("Paused", "3", "#F59E0B"),
            createStatBadge("This Week", "47", "#3B82F6")
        );

        // Create button
        Button createBtn = new Button("+ New Schedule");
        createBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 20;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        createBtn.setOnAction(e -> showCreatePane());

        header.getChildren().addAll(titleBox, spacer, stats, createBtn);
        return header;
    }

    private VBox createStatBadge(String label, String value, String color) {
        VBox badge = new VBox(0);
        badge.setAlignment(Pos.CENTER);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        badge.getChildren().addAll(valueLabel, labelText);
        return badge;
    }

    // ========================================================================
    // TABLE PANE
    // ========================================================================

    private VBox createTablePane() {
        VBox pane = new VBox(0);
        pane.setStyle("-fx-background-color: white;");

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search schedules...");
        searchField.setPrefWidth(220);
        searchField.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-background-radius: 6;
            -fx-padding: 8 12;
            -fx-font-size: 12px;
            """);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "Active", "Paused", "Error");
        statusFilter.setValue("All Status");
        statusFilter.setStyle("-fx-font-size: 12px;");

        ComboBox<String> frequencyFilter = new ComboBox<>();
        frequencyFilter.getItems().addAll("All Frequencies", "Daily", "Weekly", "Monthly");
        frequencyFilter.setValue("All Frequencies");
        frequencyFilter.setStyle("-fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("⟳");
        refreshBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-font-size: 14px;
            -fx-padding: 8 12;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """);
        refreshBtn.setTooltip(new Tooltip("Refresh"));

        toolbar.getChildren().addAll(searchField, statusFilter, frequencyFilter, spacer, refreshBtn);

        // Table
        scheduleTable = new TableView<>(filteredSchedules);
        scheduleTable.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scheduleTable, Priority.ALWAYS);

        // Columns
        TableColumn<ScheduledReport, String> nameCol = new TableColumn<>("Report Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReportName()));
        nameCol.setPrefWidth(200);
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    VBox box = new VBox(2);
                    Label name = new Label(item);
                    name.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1E293B;");

                    ScheduledReport report = getTableRow().getItem();
                    if (report != null) {
                        Label template = new Label(report.getTemplateName());
                        template.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
                        box.getChildren().addAll(name, template);
                    } else {
                        box.getChildren().add(name);
                    }
                    setGraphic(box);
                }
            }
        });

        TableColumn<ScheduledReport, String> frequencyCol = new TableColumn<>("Frequency");
        frequencyCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFrequency()));
        frequencyCol.setPrefWidth(120);
        frequencyCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    String color = switch (item) {
                        case "Daily" -> "#3B82F6";
                        case "Weekly" -> "#8B5CF6";
                        case "Monthly" -> "#10B981";
                        default -> "#64748B";
                    };
                    badge.setStyle(String.format("""
                        -fx-background-color: %s15;
                        -fx-text-fill: %s;
                        -fx-font-size: 11px;
                        -fx-font-weight: 600;
                        -fx-padding: 4 10;
                        -fx-background-radius: 12;
                        """, color, color));
                    setGraphic(badge);
                }
            }
        });

        TableColumn<ScheduledReport, String> nextRunCol = new TableColumn<>("Next Run");
        nextRunCol.setCellValueFactory(data -> new SimpleStringProperty(
            formatDateTime(data.getValue().getNextRun())
        ));
        nextRunCol.setPrefWidth(150);

        TableColumn<ScheduledReport, String> recipientsCol = new TableColumn<>("Recipients");
        recipientsCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getRecipients().size() + " recipients"
        ));
        recipientsCol.setPrefWidth(100);

        TableColumn<ScheduledReport, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(6);
                    box.setAlignment(Pos.CENTER_LEFT);

                    String color = switch (item) {
                        case "Active" -> "#10B981";
                        case "Paused" -> "#F59E0B";
                        case "Error" -> "#EF4444";
                        default -> "#64748B";
                    };

                    Label dot = new Label("●");
                    dot.setStyle("-fx-font-size: 8px; -fx-text-fill: " + color + ";");

                    Label text = new Label(item);
                    text.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");

                    box.getChildren().addAll(dot, text);
                    setGraphic(box);
                }
            }
        });

        TableColumn<ScheduledReport, Void> actionsCol = new TableColumn<>("");
        actionsCol.setPrefWidth(120);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button pauseBtn = new Button("⏸");
            private final Button runBtn = new Button("▶");
            private final Button editBtn = new Button("✏");

            {
                HBox box = new HBox(4);
                box.setAlignment(Pos.CENTER);

                String btnStyle = """
                    -fx-background-color: #F1F5F9;
                    -fx-font-size: 11px;
                    -fx-padding: 4 8;
                    -fx-background-radius: 4;
                    -fx-cursor: hand;
                    """;
                pauseBtn.setStyle(btnStyle);
                runBtn.setStyle(btnStyle);
                editBtn.setStyle(btnStyle);

                pauseBtn.setTooltip(new Tooltip("Pause/Resume"));
                runBtn.setTooltip(new Tooltip("Run Now"));
                editBtn.setTooltip(new Tooltip("Edit"));

                pauseBtn.setOnAction(e -> {
                    ScheduledReport report = getTableRow().getItem();
                    if (report != null) {
                        togglePause(report);
                    }
                });

                runBtn.setOnAction(e -> {
                    ScheduledReport report = getTableRow().getItem();
                    if (report != null) {
                        runNow(report);
                    }
                });

                editBtn.setOnAction(e -> {
                    ScheduledReport report = getTableRow().getItem();
                    if (report != null) {
                        selectedSchedule.set(report);
                        showEditPane(report);
                    }
                });

                box.getChildren().addAll(pauseBtn, runBtn, editBtn);
                setGraphic(box);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });

        scheduleTable.getColumns().addAll(nameCol, frequencyCol, nextRunCol, recipientsCol, statusCol, actionsCol);

        // Selection listener
        scheduleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedSchedule.set(newVal);
            if (newVal != null) {
                showDetailPane(newVal);
            }
        });

        pane.getChildren().addAll(toolbar, scheduleTable);
        return pane;
    }

    // ========================================================================
    // DETAIL PANE
    // ========================================================================

    private VBox createDetailPane() {
        VBox pane = new VBox(0);
        pane.setStyle("-fx-background-color: #F8FAFC;");

        // Placeholder
        VBox placeholder = new VBox(12);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(60));
        VBox.setVgrow(placeholder, Priority.ALWAYS);

        Label icon = new Label("📅");
        icon.setStyle("-fx-font-size: 48px; -fx-text-fill: #CBD5E1;");

        Label text = new Label("Select a schedule to view details");
        text.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");

        Label hint = new Label("or create a new scheduled report");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #CBD5E1;");

        placeholder.getChildren().addAll(icon, text, hint);
        pane.getChildren().add(placeholder);

        return pane;
    }

    private void showDetailPane(ScheduledReport report) {
        detailPane.getChildren().clear();

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox content = new VBox(16);
        content.setPadding(new Insets(20));

        // Header
        VBox header = new VBox(8);

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(report.getReportName());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");
        HBox.setHgrow(name, Priority.ALWAYS);

        Label statusBadge = new Label(report.getStatus());
        String statusColor = switch (report.getStatus()) {
            case "Active" -> "#10B981";
            case "Paused" -> "#F59E0B";
            default -> "#64748B";
        };
        statusBadge.setStyle(String.format("""
            -fx-background-color: %s20;
            -fx-text-fill: %s;
            -fx-font-size: 11px;
            -fx-font-weight: 600;
            -fx-padding: 4 12;
            -fx-background-radius: 12;
            """, statusColor, statusColor));

        titleRow.getChildren().addAll(name, statusBadge);

        Label template = new Label("Template: " + report.getTemplateName());
        template.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        header.getChildren().addAll(titleRow, template);

        // Quick actions
        HBox actions = new HBox(8);

        Button runNowBtn = new Button("▶ Run Now");
        runNowBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 12px;
            -fx-font-weight: 600;
            -fx-padding: 8 16;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """);
        runNowBtn.setOnAction(e -> runNow(report));

        Button pauseBtn = new Button(report.getStatus().equals("Active") ? "⏸ Pause" : "▶ Resume");
        pauseBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-text-fill: #374151;
            -fx-font-size: 12px;
            -fx-padding: 8 16;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """);
        pauseBtn.setOnAction(e -> togglePause(report));

        Button editBtn = new Button("✏ Edit");
        editBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-text-fill: #374151;
            -fx-font-size: 12px;
            -fx-padding: 8 16;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """);
        editBtn.setOnAction(e -> showEditPane(report));

        actions.getChildren().addAll(runNowBtn, pauseBtn, editBtn);

        // Schedule info
        VBox scheduleInfo = createInfoSection("Schedule", List.of(
            new String[]{"Frequency", report.getFrequency()},
            new String[]{"Time", report.getScheduleTime()},
            new String[]{"Next Run", formatDateTime(report.getNextRun())},
            new String[]{"Last Run", formatDateTime(report.getLastRun())}
        ));

        // Delivery info
        VBox deliveryInfo = createInfoSection("Delivery", List.of(
            new String[]{"Method", report.getDeliveryMethod()},
            new String[]{"Format", report.getOutputFormat()},
            new String[]{"Recipients", report.getRecipients().size() + " recipients"}
        ));

        // Recipients list
        VBox recipientsBox = new VBox(8);
        Label recipientsLabel = new Label("Recipients");
        recipientsLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #64748B;");

        VBox recipientsList = new VBox(4);
        for (String recipient : report.getRecipients()) {
            HBox item = new HBox(8);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(6, 10, 6, 10));
            item.setStyle("-fx-background-color: white; -fx-background-radius: 4;");

            Label emailIcon = new Label("✉");
            Label email = new Label(recipient);
            email.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

            item.getChildren().addAll(emailIcon, email);
            recipientsList.getChildren().add(item);
        }

        recipientsBox.getChildren().addAll(recipientsLabel, recipientsList);

        // History
        VBox historyBox = new VBox(8);
        Label historyLabel = new Label("Recent History");
        historyLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #64748B;");

        VBox historyList = new VBox(4);
        for (ReportExecution exec : report.getHistory()) {
            historyList.getChildren().add(createHistoryItem(exec));
        }

        historyBox.getChildren().addAll(historyLabel, historyList);

        content.getChildren().addAll(header, actions, scheduleInfo, deliveryInfo, recipientsBox, historyBox);
        scroll.setContent(content);
        detailPane.getChildren().add(scroll);
    }

    private VBox createInfoSection(String title, List<String[]> items) {
        VBox section = new VBox(8);

        Label label = new Label(title);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #64748B;");

        VBox itemsBox = new VBox(4);
        itemsBox.setPadding(new Insets(12));
        itemsBox.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        for (String[] item : items) {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);

            Label key = new Label(item[0]);
            key.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
            key.setMinWidth(100);

            Label value = new Label(item[1]);
            value.setStyle("-fx-font-size: 12px; -fx-text-fill: #1E293B;");

            row.getChildren().addAll(key, value);
            itemsBox.getChildren().add(row);
        }

        section.getChildren().addAll(label, itemsBox);
        return section;
    }

    private HBox createHistoryItem(ReportExecution exec) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8, 12, 8, 12));
        item.setStyle("-fx-background-color: white; -fx-background-radius: 6;");

        String statusColor = exec.isSuccess() ? "#10B981" : "#EF4444";
        Label statusIcon = new Label(exec.isSuccess() ? "✓" : "✕");
        statusIcon.setStyle("-fx-font-size: 12px; -fx-text-fill: " + statusColor + ";");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label time = new Label(formatDateTime(exec.getExecutionTime()));
        time.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

        Label details = new Label(exec.isSuccess() ?
            "Sent to " + exec.getRecipientsCount() + " recipients" :
            exec.getErrorMessage());
        details.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (exec.isSuccess() ? "#64748B" : "#EF4444") + ";");

        info.getChildren().addAll(time, details);

        item.getChildren().addAll(statusIcon, info);
        return item;
    }

    private void showCreatePane() {
        showEditPane(null);
    }

    private void showEditPane(ScheduledReport existing) {
        detailPane.getChildren().clear();

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox form = new VBox(16);
        form.setPadding(new Insets(20));

        // Header
        Label title = new Label(existing == null ? "Create Schedule" : "Edit Schedule");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        // Report selection
        VBox reportBox = createFormField("Report Template");
        ComboBox<String> reportCombo = new ComboBox<>();
        reportCombo.getItems().addAll("Student Roster", "Grade Distribution", "Attendance Report", "GPA Report");
        reportCombo.setMaxWidth(Double.MAX_VALUE);
        reportCombo.setValue(existing != null ? existing.getTemplateName() : null);
        reportCombo.setPromptText("Select a report template");
        reportBox.getChildren().add(reportCombo);

        // Schedule name
        VBox nameBox = createFormField("Schedule Name");
        TextField nameField = new TextField(existing != null ? existing.getReportName() : "");
        nameField.setPromptText("Enter a name for this schedule");
        nameBox.getChildren().add(nameField);

        // Frequency
        VBox frequencyBox = createFormField("Frequency");
        HBox freqOptions = new HBox(8);

        ToggleGroup freqGroup = new ToggleGroup();
        ToggleButton dailyBtn = createFrequencyToggle("Daily", freqGroup);
        ToggleButton weeklyBtn = createFrequencyToggle("Weekly", freqGroup);
        ToggleButton monthlyBtn = createFrequencyToggle("Monthly", freqGroup);

        if (existing != null) {
            switch (existing.getFrequency()) {
                case "Daily" -> dailyBtn.setSelected(true);
                case "Weekly" -> weeklyBtn.setSelected(true);
                case "Monthly" -> monthlyBtn.setSelected(true);
            }
        }

        freqOptions.getChildren().addAll(dailyBtn, weeklyBtn, monthlyBtn);
        frequencyBox.getChildren().add(freqOptions);

        // Time
        VBox timeBox = createFormField("Run Time");
        HBox timeRow = new HBox(12);

        ComboBox<String> hourCombo = new ComboBox<>();
        for (int i = 1; i <= 12; i++) hourCombo.getItems().add(String.format("%02d", i));
        hourCombo.setValue("08");
        hourCombo.setPrefWidth(70);

        ComboBox<String> minuteCombo = new ComboBox<>();
        minuteCombo.getItems().addAll("00", "15", "30", "45");
        minuteCombo.setValue("00");
        minuteCombo.setPrefWidth(70);

        ComboBox<String> ampmCombo = new ComboBox<>();
        ampmCombo.getItems().addAll("AM", "PM");
        ampmCombo.setValue("AM");
        ampmCombo.setPrefWidth(70);

        timeRow.getChildren().addAll(hourCombo, new Label(":"), minuteCombo, ampmCombo);
        timeBox.getChildren().add(timeRow);

        // Delivery method
        VBox deliveryBox = createFormField("Delivery Method");
        ComboBox<String> deliveryCombo = new ComboBox<>();
        deliveryCombo.getItems().addAll("Email", "SFTP", "Google Drive", "Dropbox");
        deliveryCombo.setValue(existing != null ? existing.getDeliveryMethod() : "Email");
        deliveryCombo.setMaxWidth(Double.MAX_VALUE);
        deliveryBox.getChildren().add(deliveryCombo);

        // Output format
        VBox formatBox = createFormField("Output Format");
        HBox formatOptions = new HBox(8);

        ToggleGroup formatGroup = new ToggleGroup();
        ToggleButton pdfBtn = createFormatToggle("PDF", formatGroup);
        ToggleButton excelBtn = createFormatToggle("Excel", formatGroup);
        ToggleButton csvBtn = createFormatToggle("CSV", formatGroup);
        pdfBtn.setSelected(true);

        formatOptions.getChildren().addAll(pdfBtn, excelBtn, csvBtn);
        formatBox.getChildren().add(formatOptions);

        // Recipients
        VBox recipientsBox = createFormField("Recipients");
        TextArea recipientsArea = new TextArea();
        recipientsArea.setPromptText("Enter email addresses, one per line");
        recipientsArea.setPrefRowCount(4);
        if (existing != null) {
            recipientsArea.setText(String.join("\n", existing.getRecipients()));
        }
        recipientsBox.getChildren().add(recipientsArea);

        // Buttons
        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(16, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #64748B;
            -fx-font-size: 13px;
            -fx-padding: 10 20;
            -fx-cursor: hand;
            """);
        cancelBtn.setOnAction(e -> {
            if (selectedSchedule.get() != null) {
                showDetailPane(selectedSchedule.get());
            } else {
                detailPane.getChildren().clear();
                detailPane.getChildren().add(createDetailPane().getChildren().get(0));
            }
        });

        Button saveBtn = new Button(existing == null ? "Create Schedule" : "Save Changes");
        saveBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 24;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        saveBtn.setOnAction(e -> saveSchedule(existing, nameField.getText(), reportCombo.getValue()));

        buttons.getChildren().addAll(cancelBtn, saveBtn);

        form.getChildren().addAll(title, reportBox, nameBox, frequencyBox, timeBox,
            deliveryBox, formatBox, recipientsBox, buttons);

        scroll.setContent(form);
        detailPane.getChildren().add(scroll);
    }

    private VBox createFormField(String label) {
        VBox box = new VBox(6);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        box.getChildren().add(lbl);
        return box;
    }

    private ToggleButton createFrequencyToggle(String text, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(group);
        btn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-text-fill: #374151;
            -fx-font-size: 12px;
            -fx-padding: 8 20;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """);

        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            btn.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-font-size: 12px;
                -fx-padding: 8 20;
                -fx-background-radius: 6;
                -fx-cursor: hand;
                """,
                isSelected ? "#3B82F6" : "#F1F5F9",
                isSelected ? "white" : "#374151"
            ));
        });

        return btn;
    }

    private ToggleButton createFormatToggle(String text, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(group);
        btn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-text-fill: #374151;
            -fx-font-size: 12px;
            -fx-padding: 8 16;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """);

        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            btn.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-font-size: 12px;
                -fx-padding: 8 16;
                -fx-background-radius: 6;
                -fx-cursor: hand;
                """,
                isSelected ? "#3B82F6" : "#F1F5F9",
                isSelected ? "white" : "#374151"
            ));
        });

        return btn;
    }

    // ========================================================================
    // ACTIONS
    // ========================================================================

    private void togglePause(ScheduledReport report) {
        if (report.getStatus().equals("Active")) {
            report.setStatus("Paused");
        } else {
            report.setStatus("Active");
        }
        scheduleTable.refresh();
        if (selectedSchedule.get() == report) {
            showDetailPane(report);
        }
        log.info("Toggled schedule status: {} -> {}", report.getReportName(), report.getStatus());
    }

    private void runNow(ScheduledReport report) {
        ReportExecution exec = new ReportExecution();
        exec.setExecutionTime(LocalDateTime.now());
        exec.setSuccess(true);
        exec.setRecipientsCount(report.getRecipients().size());
        report.getHistory().add(0, exec);
        report.setLastRun(LocalDateTime.now());
        scheduleTable.refresh();
        if (selectedSchedule.get() == report) {
            showDetailPane(report);
        }
        log.info("Running report now: {}", report.getReportName());
    }

    private void saveSchedule(ScheduledReport existing, String name, String template) {
        if (existing == null) {
            ScheduledReport report = new ScheduledReport();
            report.setId(UUID.randomUUID().toString());
            report.setReportName(name);
            report.setTemplateName(template);
            report.setFrequency("Weekly");
            report.setScheduleTime("8:00 AM");
            report.setDeliveryMethod("Email");
            report.setOutputFormat("PDF");
            report.setStatus("Active");
            report.setNextRun(LocalDateTime.now().plusDays(7));
            report.setRecipients(new ArrayList<>());
            report.setHistory(new ArrayList<>());

            schedules.add(report);
            filterSchedules();

            if (onScheduleCreated != null) {
                onScheduleCreated.accept(report);
            }
        } else {
            existing.setReportName(name);
            existing.setTemplateName(template);
            scheduleTable.refresh();

            if (onScheduleUpdated != null) {
                onScheduleUpdated.accept(existing);
            }
        }

        log.info("Saved schedule: {}", name);
    }

    private void filterSchedules() {
        filteredSchedules.clear();
        filteredSchedules.addAll(schedules);
    }

    // ========================================================================
    // UTILITIES
    // ========================================================================

    private String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "Never";
        return dt.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"));
    }

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    public void setOnScheduleCreated(Consumer<ScheduledReport> callback) {
        this.onScheduleCreated = callback;
    }

    public void setOnScheduleUpdated(Consumer<ScheduledReport> callback) {
        this.onScheduleUpdated = callback;
    }

    public void setOnScheduleDeleted(Consumer<ScheduledReport> callback) {
        this.onScheduleDeleted = callback;
    }

    // ========================================================================
    // DEMO DATA
    // ========================================================================

    private void loadDemoSchedules() {
        schedules.addAll(
            createSchedule("Daily Attendance Report", "Attendance Report", "Daily", "6:00 AM",
                "Active", List.of("principal@school.edu", "attendance@school.edu")),
            createSchedule("Weekly Grade Summary", "Grade Distribution", "Weekly", "8:00 AM",
                "Active", List.of("principal@school.edu", "academics@school.edu")),
            createSchedule("Monthly Enrollment Report", "Student Roster", "Monthly", "9:00 AM",
                "Active", List.of("admin@school.edu", "registrar@school.edu")),
            createSchedule("Weekly GPA Rankings", "GPA Report", "Weekly", "7:00 AM",
                "Paused", List.of("counselor@school.edu")),
            createSchedule("Daily Absence Alert", "Attendance Report", "Daily", "10:00 AM",
                "Active", List.of("parents-list@school.edu"))
        );
    }

    private ScheduledReport createSchedule(String name, String template, String frequency,
                                           String time, String status, List<String> recipients) {
        ScheduledReport report = new ScheduledReport();
        report.setId(UUID.randomUUID().toString());
        report.setReportName(name);
        report.setTemplateName(template);
        report.setFrequency(frequency);
        report.setScheduleTime(time);
        report.setDeliveryMethod("Email");
        report.setOutputFormat("PDF");
        report.setStatus(status);
        report.setRecipients(new ArrayList<>(recipients));
        report.setNextRun(LocalDateTime.now().plusDays(frequency.equals("Daily") ? 1 :
            frequency.equals("Weekly") ? 7 : 30));
        report.setLastRun(LocalDateTime.now().minusDays(frequency.equals("Daily") ? 1 :
            frequency.equals("Weekly") ? 7 : 30));

        // Add some history
        List<ReportExecution> history = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ReportExecution exec = new ReportExecution();
            exec.setExecutionTime(LocalDateTime.now().minusDays(i * (frequency.equals("Daily") ? 1 : 7)));
            exec.setSuccess(i != 2);
            exec.setRecipientsCount(recipients.size());
            exec.setErrorMessage(i == 2 ? "SMTP connection timeout" : null);
            history.add(exec);
        }
        report.setHistory(history);

        return report;
    }

    // ========================================================================
    // DATA CLASSES
    // ========================================================================

    @Getter @Setter
    public static class ScheduledReport {
        private String id;
        private String reportName;
        private String templateName;
        private String frequency;
        private String scheduleTime;
        private String deliveryMethod;
        private String outputFormat;
        private String status;
        private List<String> recipients;
        private LocalDateTime nextRun;
        private LocalDateTime lastRun;
        private List<ReportExecution> history;
    }

    @Getter @Setter
    public static class ReportExecution {
        private LocalDateTime executionTime;
        private boolean success;
        private int recipientsCount;
        private String errorMessage;
    }
}
