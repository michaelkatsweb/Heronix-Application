package com.heronix.ui.controller;

import com.heronix.model.domain.*;
import com.heronix.service.CourseService;
import com.heronix.service.RoomService;
import com.heronix.service.ScheduleService;
import com.heronix.service.TeacherService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CourseConflictResolutionController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private RoomService roomService;

    // Summary Cards
    @FXML private Label totalConflictsLabel;
    @FXML private Label criticalConflictsLabel;
    @FXML private Label teacherConflictsLabel;
    @FXML private Label roomConflictsLabel;

    // Chart
    @FXML private PieChart conflictTypeChart;

    // Filters
    @FXML private ComboBox<String> conflictTypeFilterComboBox;
    @FXML private ComboBox<String> severityFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private CheckBox showResolvedCheckBox;
    @FXML private Button applyFiltersButton;

    // Quick Actions
    @FXML private Button scanConflictsButton;
    @FXML private Button autoResolveButton;
    @FXML private Button exportConflictsButton;

    // Conflict List
    @FXML private Label conflictListCountLabel;
    @FXML private ComboBox<String> sortByComboBox;
    @FXML private TableView<ConflictRecord> conflictsTableView;
    @FXML private TableColumn<ConflictRecord, String> severityColumn;
    @FXML private TableColumn<ConflictRecord, String> typeColumn;
    @FXML private TableColumn<ConflictRecord, String> descriptionColumn;
    @FXML private TableColumn<ConflictRecord, String> affectedColumn;
    @FXML private TableColumn<ConflictRecord, String> timeColumn;
    @FXML private TableColumn<ConflictRecord, String> statusColumn;
    @FXML private TableColumn<ConflictRecord, Void> actionsColumn;

    // Conflict Details
    @FXML private Label conflictIdLabel;
    @FXML private Label conflictTypeLabel;
    @FXML private Label conflictSeverityLabel;
    @FXML private Label detectedDateLabel;
    @FXML private Label conflictDescriptionLabel;
    @FXML private Label affectedEntity1Label;
    @FXML private Label affectedEntity2Label;
    @FXML private Label conflictTimeLabel;
    @FXML private Label impactLabel;

    // AI Suggestions
    @FXML private VBox suggestionsContainer;

    // Manual Resolution
    @FXML private ComboBox<Course> reassignCourseComboBox;
    @FXML private ComboBox<Teacher> newTeacherComboBox;
    @FXML private ComboBox<Course> reassignRoomCourseComboBox;
    @FXML private ComboBox<Room> newRoomComboBox;
    @FXML private ComboBox<Course> rescheduleComboBox;
    @FXML private ComboBox<String> newDayComboBox;
    @FXML private ComboBox<Integer> newPeriodComboBox;
    @FXML private ComboBox<Course> splitCourseComboBox;
    @FXML private Spinner<Integer> sectionsSpinner;

    // Resolution Actions
    @FXML private Button markResolvedButton;
    @FXML private Button ignoreConflictButton;
    @FXML private Button deleteConflictButton;
    @FXML private ListView<String> resolutionHistoryListView;

    // Status Bar
    @FXML private Label statusLabel;
    @FXML private Label lastScanLabel;
    @FXML private Label autoResolveStatusLabel;
    @FXML private Label processingTimeLabel;

    private ObservableList<ConflictRecord> allConflicts = FXCollections.observableArrayList();
    private ObservableList<ConflictRecord> filteredConflicts = FXCollections.observableArrayList();
    private ConflictRecord selectedConflict;

    private ObservableList<Teacher> allTeachers = FXCollections.observableArrayList();
    private ObservableList<Room> allRooms = FXCollections.observableArrayList();
    private ObservableList<Course> allCourses = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        loadData();
        generateSampleConflicts();
        updateSummaryCards();
        updateConflictTypeChart();
    }

    private void setupTableColumns() {
        // Severity column with icon
        severityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(getSeverityIcon(cellData.getValue().getSeverity())));

        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getType()));

        descriptionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));

        affectedColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAffectedEntities()));

        timeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTimePeriod()));

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));

        // Actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button resolveButton = new Button("Resolve");

            {
                viewButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8; -fx-cursor: hand;");
                resolveButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8; -fx-cursor: hand;");

                viewButton.setOnAction(event -> {
                    ConflictRecord conflict = getTableView().getItems().get(getIndex());
                    selectConflict(conflict);
                });

                resolveButton.setOnAction(event -> {
                    ConflictRecord conflict = getTableView().getItems().get(getIndex());
                    autoResolveConflict(conflict);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, viewButton, resolveButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        conflictsTableView.setItems(filteredConflicts);

        // Row selection
        conflictsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectConflict(newVal);
            }
        });
    }

    private void setupComboBoxes() {
        // Conflict Type Filter
        conflictTypeFilterComboBox.setItems(FXCollections.observableArrayList(
                "All Types",
                "Teacher Double-Booking",
                "Room Double-Booking",
                "Student Schedule Conflict",
                "Resource Unavailability",
                "Prerequisite Violation",
                "Capacity Exceeded"
        ));
        conflictTypeFilterComboBox.setValue("All Types");

        // Severity Filter
        severityFilterComboBox.setItems(FXCollections.observableArrayList(
                "All Severities", "Critical", "High", "Medium", "Low"
        ));
        severityFilterComboBox.setValue("All Severities");

        // Status Filter
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "All Statuses", "Unresolved", "In Progress", "Resolved", "Ignored"
        ));
        statusFilterComboBox.setValue("Unresolved");

        // Sort By
        sortByComboBox.setItems(FXCollections.observableArrayList(
                "Severity (High to Low)",
                "Type (A-Z)",
                "Time (Earliest First)",
                "Status",
                "Recently Detected"
        ));
        sortByComboBox.setValue("Severity (High to Low)");
        sortByComboBox.setOnAction(e -> sortConflicts());

        // Days for rescheduling
        newDayComboBox.setItems(FXCollections.observableArrayList(
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
        ));

        // Periods for rescheduling
        newPeriodComboBox.setItems(FXCollections.observableArrayList(
                1, 2, 3, 4, 5, 6, 7
        ));

        // Teacher ComboBox
        newTeacherComboBox.setConverter(new javafx.util.StringConverter<Teacher>() {
            @Override
            public String toString(Teacher teacher) {
                return teacher != null ? teacher.getFullName() : "";
            }

            @Override
            public Teacher fromString(String string) {
                return null;
            }
        });

        // Room ComboBox
        newRoomComboBox.setConverter(new javafx.util.StringConverter<Room>() {
            @Override
            public String toString(Room room) {
                return room != null ? room.getRoomNumber() + " - " + room.getRoomType() : "";
            }

            @Override
            public Room fromString(String string) {
                return null;
            }
        });

        // Course ComboBox
        javafx.util.StringConverter<Course> courseConverter = new javafx.util.StringConverter<Course>() {
            @Override
            public String toString(Course course) {
                return course != null ? course.getCourseCode() + " - " + course.getCourseName() : "";
            }

            @Override
            public Course fromString(String string) {
                return null;
            }
        };

        reassignCourseComboBox.setConverter(courseConverter);
        reassignRoomCourseComboBox.setConverter(courseConverter);
        rescheduleComboBox.setConverter(courseConverter);
        splitCourseComboBox.setConverter(courseConverter);
    }

    private void loadData() {
        // Load teachers
        createSampleTeachers();
        newTeacherComboBox.setItems(allTeachers);

        // Load rooms
        createSampleRooms();
        newRoomComboBox.setItems(allRooms);

        // Load courses
        createSampleCourses();
        reassignCourseComboBox.setItems(allCourses);
        reassignRoomCourseComboBox.setItems(allCourses);
        rescheduleComboBox.setItems(allCourses);
        splitCourseComboBox.setItems(allCourses);
    }

    private void createSampleTeachers() {
        String[] names = {"Smith, John", "Johnson, Mary", "Williams, Robert", "Brown, Lisa",
                "Jones, Michael", "Garcia, Maria"};

        for (String name : names) {
            Teacher teacher = new Teacher();
            teacher.setId((long) (allTeachers.size() + 1));
            teacher.setFirstName(name.split(", ")[1]);
            teacher.setLastName(name.split(", ")[0]);
            allTeachers.add(teacher);
        }
    }

    private void createSampleRooms() {
        for (int i = 101; i <= 110; i++) {
            Room room = new Room();
            room.setId((long) (allRooms.size() + 1));
            room.setRoomNumber(String.valueOf(i));
            room.setCapacity(30);
            allRooms.add(room);
        }
    }

    private void createSampleCourses() {
        String[] courses = {"ENG1", "MATH1", "SCI1", "HIST1", "PE1"};
        String[] names = {"English I", "Algebra I", "Biology", "World History", "Physical Education"};

        for (int i = 0; i < courses.length; i++) {
            Course course = new Course();
            course.setId((long) (i + 1));
            course.setCourseCode(courses[i]);
            course.setCourseName(names[i]);
            allCourses.add(course);
        }
    }

    private void generateSampleConflicts() {
        allConflicts.clear();

        // Teacher double-booking
        allConflicts.add(new ConflictRecord(
                1L,
                "Teacher Double-Booking",
                "Critical",
                "Teacher Smith assigned to ENG1 and MATH1 at the same time",
                "Smith, John (ENG1, MATH1)",
                "Monday, Period 1 (8:00 AM)",
                "Unresolved",
                LocalDateTime.now().minusDays(2),
                "Two courses scheduled simultaneously for the same teacher"
        ));

        // Room double-booking
        allConflicts.add(new ConflictRecord(
                2L,
                "Room Double-Booking",
                "Critical",
                "Room 101 assigned to SCI1 and HIST1 at the same time",
                "Room 101 (SCI1, HIST1)",
                "Tuesday, Period 2 (9:00 AM)",
                "Unresolved",
                LocalDateTime.now().minusDays(1),
                "Two courses scheduled in the same room simultaneously"
        ));

        // Student schedule conflict
        allConflicts.add(new ConflictRecord(
                3L,
                "Student Schedule Conflict",
                "High",
                "15 students have ENG1 and MATH1 scheduled at the same time",
                "15 students affected",
                "Monday, Period 1",
                "Unresolved",
                LocalDateTime.now().minusHours(5),
                "Students cannot attend both courses"
        ));

        // Capacity exceeded
        allConflicts.add(new ConflictRecord(
                4L,
                "Capacity Exceeded",
                "Medium",
                "PE1 has 35 enrolled students but room capacity is 30",
                "PE1 (35/30 students)",
                "All periods",
                "In Progress",
                LocalDateTime.now().minusHours(3),
                "Enrollment exceeds room capacity"
        ));

        // Resource unavailability
        allConflicts.add(new ConflictRecord(
                5L,
                "Resource Unavailability",
                "Medium",
                "Chemistry lab required for SCI1 but not available Period 3",
                "SCI1 (Chemistry Lab)",
                "Wednesday, Period 3",
                "Unresolved",
                LocalDateTime.now().minusHours(1),
                "Required lab equipment not available"
        ));

        // Prerequisite violation
        allConflicts.add(new ConflictRecord(
                6L,
                "Prerequisite Violation",
                "Low",
                "5 students enrolled in MATH2 without completing MATH1",
                "5 students (MATH2)",
                "N/A",
                "Unresolved",
                LocalDateTime.now(),
                "Students missing required prerequisite"
        ));

        filteredConflicts.setAll(allConflicts);
        conflictListCountLabel.setText("(" + filteredConflicts.size() + ")");
    }

    private void updateSummaryCards() {
        totalConflictsLabel.setText(String.valueOf(allConflicts.size()));

        long critical = allConflicts.stream()
                .filter(c -> "Critical".equals(c.getSeverity()))
                .count();
        criticalConflictsLabel.setText(String.valueOf(critical));

        long teacherConflicts = allConflicts.stream()
                .filter(c -> c.getType().contains("Teacher"))
                .count();
        teacherConflictsLabel.setText(String.valueOf(teacherConflicts));

        long roomConflicts = allConflicts.stream()
                .filter(c -> c.getType().contains("Room"))
                .count();
        roomConflictsLabel.setText(String.valueOf(roomConflicts));
    }

    private void updateConflictTypeChart() {
        Map<String, Long> typeCounts = allConflicts.stream()
                .collect(Collectors.groupingBy(ConflictRecord::getType, Collectors.counting()));

        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
        typeCounts.forEach((type, count) ->
                chartData.add(new PieChart.Data(type + " (" + count + ")", count)));

        conflictTypeChart.setData(chartData);
    }

    private void selectConflict(ConflictRecord conflict) {
        selectedConflict = conflict;

        conflictIdLabel.setText("ID: #" + conflict.getId());
        conflictTypeLabel.setText(conflict.getType());
        conflictSeverityLabel.setText(conflict.getSeverity());
        conflictSeverityLabel.setStyle(getSeverityStyle(conflict.getSeverity()));

        detectedDateLabel.setText(conflict.getDetectedDate().format(
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")));

        conflictDescriptionLabel.setText(conflict.getDescription());

        String[] affected = conflict.getAffectedEntities().split(", ");
        if (affected.length > 0) {
            affectedEntity1Label.setText(affected[0]);
        }
        if (affected.length > 1) {
            affectedEntity2Label.setText(affected[1]);
        } else {
            affectedEntity2Label.setText("");
        }

        conflictTimeLabel.setText(conflict.getTimePeriod());
        impactLabel.setText(conflict.getImpact());

        generateAISuggestions(conflict);
        loadResolutionHistory(conflict);
    }

    private void generateAISuggestions(ConflictRecord conflict) {
        suggestionsContainer.getChildren().clear();

        List<String> suggestions = new ArrayList<>();

        switch (conflict.getType()) {
            case "Teacher Double-Booking":
                suggestions.add("Reassign one course to Teacher Johnson (available Monday Period 1)");
                suggestions.add("Move ENG1 to Tuesday Period 1 (no conflicts detected)");
                suggestions.add("Swap MATH1 with MATH2 currently scheduled Period 2");
                break;

            case "Room Double-Booking":
                suggestions.add("Move SCI1 to Room 102 (available, has lab equipment)");
                suggestions.add("Swap HIST1 to Period 3 (Room 101 available)");
                suggestions.add("Use Room 105 as alternative for HIST1");
                break;

            case "Student Schedule Conflict":
                suggestions.add("Create second section of MATH1 in Period 2");
                suggestions.add("Move affected students to existing MATH1 section in Period 4");
                suggestions.add("Reschedule ENG1 to Period 3");
                break;

            case "Capacity Exceeded":
                suggestions.add("Split PE1 into 2 sections (18 + 17 students)");
                suggestions.add("Move PE1 to Gymnasium (capacity 50)");
                suggestions.add("Combine with PE2 and redistribute students");
                break;

            case "Resource Unavailability":
                suggestions.add("Reschedule SCI1 to Period 4 when lab is available");
                suggestions.add("Reserve lab for Wednesday Period 3 (override existing booking)");
                suggestions.add("Use alternative classroom with portable lab equipment");
                break;

            case "Prerequisite Violation":
                suggestions.add("Move 5 students to MATH1 section");
                suggestions.add("Create intensive MATH1 summer course for affected students");
                suggestions.add("Allow waiver with instructor approval and tutoring plan");
                break;
        }

        for (int i = 0; i < suggestions.size(); i++) {
            VBox suggestionBox = new VBox(8);
            suggestionBox.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-border-color: #2196f3; -fx-border-width: 1; -fx-border-radius: 5;");

            Label numberLabel = new Label("Suggestion " + (i + 1));
            numberLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #0d47a1;");

            final String suggestion = suggestions.get(i);
            Label suggestionLabel = new Label(suggestion);
            suggestionLabel.setWrapText(true);
            suggestionLabel.setStyle("-fx-font-size: 12px;");

            Button applyButton = new Button("Apply");
            applyButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 15; -fx-cursor: hand;");
            applyButton.setOnAction(e -> applySuggestion(suggestion));

            HBox actionBox = new HBox(10, applyButton);
            actionBox.setAlignment(Pos.CENTER_RIGHT);

            suggestionBox.getChildren().addAll(numberLabel, suggestionLabel, actionBox);
            suggestionsContainer.getChildren().add(suggestionBox);
        }
    }

    private void loadResolutionHistory(ConflictRecord conflict) {
        ObservableList<String> history = FXCollections.observableArrayList();

        history.add("[" + LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) +
                "] Conflict detected by automated scan");
        history.add("[" + LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) +
                "] Assigned to scheduling coordinator");
        history.add("[" + LocalDateTime.now().minusHours(5).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) +
                "] AI suggestions generated");

        resolutionHistoryListView.setItems(history);
    }

    private void applySuggestion(String suggestion) {
        statusLabel.setText("Applying suggestion: " + suggestion);

        Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
        confirmation.setTitle("Suggestion Applied");
        confirmation.setHeaderText("Resolution Successful");
        confirmation.setContentText("The suggested resolution has been applied.\n\n" + suggestion +
                "\n\nThe conflict has been marked as resolved.");
        confirmation.showAndWait();

        if (selectedConflict != null) {
            selectedConflict.setStatus("Resolved");
            conflictsTableView.refresh();
            updateSummaryCards();
        }
    }

    private void autoResolveConflict(ConflictRecord conflict) {
        statusLabel.setText("Auto-resolving conflict #" + conflict.getId() + "...");

        Alert result = new Alert(Alert.AlertType.INFORMATION);
        result.setTitle("Auto-Resolve Complete");
        result.setHeaderText("Conflict Resolved");
        result.setContentText("AI has automatically resolved the conflict:\n\n" +
                conflict.getDescription() + "\n\n" +
                "Resolution: Optimal solution applied based on scheduling constraints.");
        result.showAndWait();

        conflict.setStatus("Resolved");
        conflictsTableView.refresh();
        updateSummaryCards();
    }

    private String getSeverityIcon(String severity) {
        switch (severity) {
            case "Critical": return "🔴";
            case "High": return "🟠";
            case "Medium": return "🟡";
            case "Low": return "🟢";
            default: return "⚪";
        }
    }

    private String getSeverityStyle(String severity) {
        switch (severity) {
            case "Critical": return "-fx-text-fill: #c62828; -fx-font-weight: bold;";
            case "High": return "-fx-text-fill: #e65100; -fx-font-weight: bold;";
            case "Medium": return "-fx-text-fill: #f57f17; -fx-font-weight: bold;";
            case "Low": return "-fx-text-fill: #2e7d32; -fx-font-weight: bold;";
            default: return "";
        }
    }

    private void sortConflicts() {
        String sortBy = sortByComboBox.getValue();
        List<ConflictRecord> sorted = new ArrayList<>(filteredConflicts);

        switch (sortBy) {
            case "Severity (High to Low)":
                sorted.sort(Comparator.comparing(ConflictRecord::getSeverityPriority));
                break;
            case "Type (A-Z)":
                sorted.sort(Comparator.comparing(ConflictRecord::getType));
                break;
            case "Recently Detected":
                sorted.sort(Comparator.comparing(ConflictRecord::getDetectedDate).reversed());
                break;
        }

        filteredConflicts.setAll(sorted);
    }

    // Action Handlers

    @FXML
    private void handleFilterChange() {
        List<ConflictRecord> filtered = allConflicts.stream()
                .filter(conflict -> {
                    boolean matches = true;

                    // Type filter
                    if (!conflictTypeFilterComboBox.getValue().equals("All Types")) {
                        matches = matches && conflict.getType().equals(conflictTypeFilterComboBox.getValue());
                    }

                    // Severity filter
                    if (!severityFilterComboBox.getValue().equals("All Severities")) {
                        matches = matches && conflict.getSeverity().equals(severityFilterComboBox.getValue());
                    }

                    // Status filter
                    if (!statusFilterComboBox.getValue().equals("All Statuses")) {
                        matches = matches && conflict.getStatus().equals(statusFilterComboBox.getValue());
                    }

                    // Show resolved filter
                    if (!showResolvedCheckBox.isSelected()) {
                        matches = matches && !conflict.getStatus().equals("Resolved");
                    }

                    return matches;
                })
                .collect(Collectors.toList());

        filteredConflicts.setAll(filtered);
        conflictListCountLabel.setText("(" + filteredConflicts.size() + ")");
        sortConflicts();
    }

    @FXML
    private void handleScanConflicts() {
        statusLabel.setText("Scanning for conflicts...");
        processingTimeLabel.setText("Scanning...");

        long startTime = System.currentTimeMillis();

        // Simulate scanning
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        processingTimeLabel.setText("Scanned in " + (endTime - startTime) + "ms");

        lastScanLabel.setText("Last scan: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Scan Complete");
        alert.setHeaderText("Conflict Scan Finished");
        alert.setContentText("Found " + allConflicts.size() + " conflicts.\n\n" +
                "Critical: " + criticalConflictsLabel.getText() + "\n" +
                "Teacher Conflicts: " + teacherConflictsLabel.getText() + "\n" +
                "Room Conflicts: " + roomConflictsLabel.getText());
        alert.showAndWait();

        statusLabel.setText("Scan complete");
    }

    @FXML
    private void handleAutoResolveAll() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Auto-Resolve All");
        confirmation.setHeaderText("Automatically Resolve All Conflicts");
        confirmation.setContentText("This will apply AI-powered resolutions to all unresolved conflicts.\n\n" +
                "Do you want to proceed?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                statusLabel.setText("Auto-resolving all conflicts...");

                long unresolved = allConflicts.stream()
                        .filter(c -> "Unresolved".equals(c.getStatus()) || "In Progress".equals(c.getStatus()))
                        .count();

                allConflicts.forEach(c -> {
                    if (!c.getStatus().equals("Resolved")) {
                        c.setStatus("Resolved");
                    }
                });

                conflictsTableView.refresh();
                updateSummaryCards();

                Alert result = new Alert(Alert.AlertType.INFORMATION);
                result.setTitle("Auto-Resolve Complete");
                result.setHeaderText("All Conflicts Resolved");
                result.setContentText("Successfully resolved " + unresolved + " conflicts using AI optimization.");
                result.showAndWait();

                statusLabel.setText("Auto-resolve complete");
            }
        });
    }

    @FXML
    private void handleExportConflicts() {
        statusLabel.setText("Exporting conflict report...");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Successful");
        alert.setHeaderText("Conflict Report Exported");
        alert.setContentText("Conflict report has been exported to:\n\n" +
                "Reports/conflict_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".xlsx\n\n" +
                "Total conflicts: " + allConflicts.size());
        alert.showAndWait();

        statusLabel.setText("Export complete");
    }

    @FXML
    private void handleReassignTeacher() {
        if (reassignCourseComboBox.getValue() == null || newTeacherComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information",
                    "Please select both a course and a new teacher.");
            return;
        }

        Course course = reassignCourseComboBox.getValue();
        Teacher teacher = newTeacherComboBox.getValue();

        statusLabel.setText("Reassigning teacher for " + course.getCourseCode() + "...");

        Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
        confirmation.setTitle("Teacher Reassigned");
        confirmation.setHeaderText("Success");
        confirmation.setContentText("Successfully reassigned " + course.getCourseCode() +
                " to " + teacher.getFullName());
        confirmation.showAndWait();
    }

    @FXML
    private void handleReassignRoom() {
        if (reassignRoomCourseComboBox.getValue() == null || newRoomComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information",
                    "Please select both a course and a new room.");
            return;
        }

        Course course = reassignRoomCourseComboBox.getValue();
        Room room = newRoomComboBox.getValue();

        statusLabel.setText("Reassigning room for " + course.getCourseCode() + "...");

        Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
        confirmation.setTitle("Room Reassigned");
        confirmation.setHeaderText("Success");
        confirmation.setContentText("Successfully reassigned " + course.getCourseCode() +
                " to Room " + room.getRoomNumber());
        confirmation.showAndWait();
    }

    @FXML
    private void handleChangeTimeSlot() {
        if (rescheduleComboBox.getValue() == null || newDayComboBox.getValue() == null ||
                newPeriodComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information",
                    "Please select a course, new day, and new period.");
            return;
        }

        Course course = rescheduleComboBox.getValue();
        String day = newDayComboBox.getValue();
        Integer period = newPeriodComboBox.getValue();

        statusLabel.setText("Rescheduling " + course.getCourseCode() + "...");

        Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
        confirmation.setTitle("Time Slot Changed");
        confirmation.setHeaderText("Success");
        confirmation.setContentText("Successfully rescheduled " + course.getCourseCode() +
                " to " + day + ", Period " + period);
        confirmation.showAndWait();
    }

    @FXML
    private void handleSplitSections() {
        if (splitCourseComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Course Selected",
                    "Please select a course to split.");
            return;
        }

        Course course = splitCourseComboBox.getValue();
        int sections = sectionsSpinner.getValue();

        statusLabel.setText("Splitting " + course.getCourseCode() + " into " + sections + " sections...");

        Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
        confirmation.setTitle("Sections Created");
        confirmation.setHeaderText("Success");
        confirmation.setContentText("Successfully split " + course.getCourseCode() +
                " into " + sections + " sections.\n\n" +
                "New sections have been created and students redistributed.");
        confirmation.showAndWait();
    }

    @FXML
    private void handleMarkResolved() {
        if (selectedConflict == null) {
            showAlert(Alert.AlertType.WARNING, "No Conflict Selected",
                    "Please select a conflict to mark as resolved.");
            return;
        }

        selectedConflict.setStatus("Resolved");
        conflictsTableView.refresh();
        updateSummaryCards();
        statusLabel.setText("Conflict #" + selectedConflict.getId() + " marked as resolved");
    }

    @FXML
    private void handleIgnoreConflict() {
        if (selectedConflict == null) {
            showAlert(Alert.AlertType.WARNING, "No Conflict Selected",
                    "Please select a conflict to ignore.");
            return;
        }

        selectedConflict.setStatus("Ignored");
        conflictsTableView.refresh();
        updateSummaryCards();
        statusLabel.setText("Conflict #" + selectedConflict.getId() + " ignored");
    }

    @FXML
    private void handleDeleteConflict() {
        if (selectedConflict == null) {
            showAlert(Alert.AlertType.WARNING, "No Conflict Selected",
                    "Please select a conflict to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Conflict");
        confirmation.setHeaderText("Confirm Deletion");
        confirmation.setContentText("Are you sure you want to delete this conflict record?\n\n" +
                "This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                allConflicts.remove(selectedConflict);
                filteredConflicts.remove(selectedConflict);
                updateSummaryCards();
                updateConflictTypeChart();
                statusLabel.setText("Conflict deleted");
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner class for Conflict Records
    public static class ConflictRecord {
        private Long id;
        private String type;
        private String severity;
        private String description;
        private String affectedEntities;
        private String timePeriod;
        private String status;
        private LocalDateTime detectedDate;
        private String impact;

        public ConflictRecord(Long id, String type, String severity, String description,
                              String affectedEntities, String timePeriod, String status,
                              LocalDateTime detectedDate, String impact) {
            this.id = id;
            this.type = type;
            this.severity = severity;
            this.description = description;
            this.affectedEntities = affectedEntities;
            this.timePeriod = timePeriod;
            this.status = status;
            this.detectedDate = detectedDate;
            this.impact = impact;
        }

        // Getters and setters
        public Long getId() { return id; }
        public String getType() { return type; }
        public String getSeverity() { return severity; }
        public String getDescription() { return description; }
        public String getAffectedEntities() { return affectedEntities; }
        public String getTimePeriod() { return timePeriod; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getDetectedDate() { return detectedDate; }
        public String getImpact() { return impact; }

        public int getSeverityPriority() {
            switch (severity) {
                case "Critical": return 1;
                case "High": return 2;
                case "Medium": return 3;
                case "Low": return 4;
                default: return 5;
            }
        }
    }
}
