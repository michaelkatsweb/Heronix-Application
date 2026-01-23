package com.heronix.ui.attendance;

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
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Consumer;

/**
 * Modern Attendance View
 * A quick-entry interface for taking and managing class attendance.
 *
 * Features:
 * - Quick status buttons for fast entry
 * - Grid view for weekly/monthly overview
 * - Student photos and names
 * - Real-time statistics
 * - Batch operations (mark all present, etc.)
 * - Date navigation
 * - Seating chart mode
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class AttendanceView extends BorderPane {

    // ========================================================================
    // DATA
    // ========================================================================

    private final ObservableList<StudentAttendance> students = FXCollections.observableArrayList();
    private final ObjectProperty<LocalDate> selectedDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<ViewMode> viewMode = new SimpleObjectProperty<>(ViewMode.DAILY);

    @Getter @Setter
    private String classId;

    @Getter @Setter
    private String className;

    @Getter @Setter
    private String periodName;

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private ComboBox<String> classSelector;
    private DatePicker datePicker;
    private ToggleGroup viewModeGroup;

    private VBox studentListPane;
    private GridPane weeklyGrid;
    private ScrollPane contentScrollPane;

    // Statistics
    private Label presentCountLabel;
    private Label absentCountLabel;
    private Label tardyCountLabel;
    private Label attendanceRateLabel;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<AttendanceChange> onAttendanceChanged;
    private Consumer<StudentAttendance> onStudentClick;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public AttendanceView() {
        getStyleClass().add("attendance-view");
        setStyle("-fx-background-color: #F8FAFC;");

        setTop(createToolbar());
        setCenter(createContent());
        setRight(createStatsPanel());

        // Load demo data
        loadDemoData();

        // Listeners
        selectedDate.addListener((obs, oldDate, newDate) -> refreshView());
        viewMode.addListener((obs, oldMode, newMode) -> refreshView());

        log.info("AttendanceView initialized");
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

        Label title = new Label("Attendance");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        classSelector = new ComboBox<>();
        classSelector.setPromptText("Select Class");
        classSelector.setPrefWidth(200);
        classSelector.getItems().addAll("Algebra II - Period 1", "Geometry - Period 2", "Pre-Calculus - Period 4");
        classSelector.setValue("Algebra II - Period 1");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button submitBtn = new Button("Submit Attendance");
        submitBtn.getStyleClass().addAll("btn", "btn-primary");
        submitBtn.setOnAction(e -> submitAttendance());

        titleRow.getChildren().addAll(title, classSelector, spacer, submitBtn);

        // Controls row
        HBox controlsRow = new HBox(16);
        controlsRow.setAlignment(Pos.CENTER_LEFT);

        // Date navigation
        Button prevDayBtn = new Button("◀");
        prevDayBtn.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");
        prevDayBtn.setOnAction(e -> selectedDate.set(selectedDate.get().minusDays(1)));

        datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(130);
        datePicker.valueProperty().bindBidirectional(selectedDate);

        Button nextDayBtn = new Button("▶");
        nextDayBtn.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");
        nextDayBtn.setOnAction(e -> selectedDate.set(selectedDate.get().plusDays(1)));

        Button todayBtn = new Button("Today");
        todayBtn.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");
        todayBtn.setOnAction(e -> selectedDate.set(LocalDate.now()));

        HBox dateNav = new HBox(4, prevDayBtn, datePicker, nextDayBtn, todayBtn);
        dateNav.setAlignment(Pos.CENTER_LEFT);

        // View mode toggle
        viewModeGroup = new ToggleGroup();

        ToggleButton dailyBtn = new ToggleButton("Daily");
        dailyBtn.setToggleGroup(viewModeGroup);
        dailyBtn.setSelected(true);
        dailyBtn.setUserData(ViewMode.DAILY);

        ToggleButton weeklyBtn = new ToggleButton("Weekly");
        weeklyBtn.setToggleGroup(viewModeGroup);
        weeklyBtn.setUserData(ViewMode.WEEKLY);

        HBox viewToggle = new HBox(0, dailyBtn, weeklyBtn);
        viewToggle.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 6;");

        dailyBtn.setStyle("-fx-background-color: white; -fx-background-radius: 6 0 0 6; -fx-border-color: #E2E8F0;");
        weeklyBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 0 6 6 0; -fx-border-color: #E2E8F0;");

        viewModeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                viewMode.set((ViewMode) newToggle.getUserData());
                dailyBtn.setStyle(newToggle == dailyBtn ?
                    "-fx-background-color: white; -fx-background-radius: 6 0 0 6; -fx-border-color: #E2E8F0;" :
                    "-fx-background-color: transparent; -fx-background-radius: 6 0 0 6; -fx-border-color: #E2E8F0;");
                weeklyBtn.setStyle(newToggle == weeklyBtn ?
                    "-fx-background-color: white; -fx-background-radius: 0 6 6 0; -fx-border-color: #E2E8F0;" :
                    "-fx-background-color: transparent; -fx-background-radius: 0 6 6 0; -fx-border-color: #E2E8F0;");
            }
        });

        Region controlsSpacer = new Region();
        HBox.setHgrow(controlsSpacer, Priority.ALWAYS);

        // Quick actions
        Button markAllPresentBtn = new Button("Mark All Present");
        markAllPresentBtn.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");
        markAllPresentBtn.setOnAction(e -> markAllPresent());

        Button clearAllBtn = new Button("Clear All");
        clearAllBtn.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");
        clearAllBtn.setOnAction(e -> clearAll());

        // Legend
        HBox legend = AttendanceStatusButton.createStatusLegend();

        controlsRow.getChildren().addAll(dateNav, viewToggle, controlsSpacer, markAllPresentBtn, clearAllBtn);

        // Legend row
        HBox legendRow = new HBox(legend);
        legendRow.setAlignment(Pos.CENTER_LEFT);
        legendRow.setPadding(new Insets(8, 0, 0, 0));

        toolbar.getChildren().addAll(titleRow, controlsRow, legendRow);
        return toolbar;
    }

    // ========================================================================
    // CONTENT
    // ========================================================================

    private ScrollPane createContent() {
        studentListPane = new VBox(0);
        studentListPane.setPadding(new Insets(16));
        studentListPane.setStyle("-fx-background-color: white;");

        weeklyGrid = new GridPane();
        weeklyGrid.setPadding(new Insets(16));
        weeklyGrid.setHgap(1);
        weeklyGrid.setVgap(1);
        weeklyGrid.setStyle("-fx-background-color: #434d5aff;");

        contentScrollPane = new ScrollPane();
        contentScrollPane.setFitToWidth(true);
        contentScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScrollPane.setStyle("-fx-background-color: white; -fx-border-width: 0;");

        return contentScrollPane;
    }

    private void refreshView() {
        if (viewMode.get() == ViewMode.DAILY) {
            buildDailyView();
            contentScrollPane.setContent(studentListPane);
        } else {
            buildWeeklyView();
            contentScrollPane.setContent(weeklyGrid);
        }
        updateStatistics();
    }

    private void buildDailyView() {
        studentListPane.getChildren().clear();

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle("-fx-background-color: #F1F5F9; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        Label dateHeader = new Label(selectedDate.get().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #334155;");

        header.getChildren().add(dateHeader);
        studentListPane.getChildren().add(header);

        // Student rows
        for (StudentAttendance student : students) {
            studentListPane.getChildren().add(createStudentRow(student));
        }
    }

    private HBox createStudentRow(StudentAttendance student) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        // Photo placeholder
        Label photoPlaceholder = new Label(student.getInitials());
        photoPlaceholder.setAlignment(Pos.CENTER);
        photoPlaceholder.setPrefSize(40, 40);
        photoPlaceholder.setStyle("""
            -fx-background-color: #E0E7FF;
            -fx-background-radius: 20;
            -fx-text-fill: #3730A3;
            -fx-font-size: 14px;
            -fx-font-weight: 600;
            """);

        // Student info
        VBox studentInfo = new VBox(2);
        HBox.setHgrow(studentInfo, Priority.ALWAYS);

        Label nameLabel = new Label(student.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #0F172A;");

        Label idLabel = new Label("ID: " + student.getStudentId());
        idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        studentInfo.getChildren().addAll(nameLabel, idLabel);

        // Attendance rate badge
        double rate = student.getAttendanceRate();
        Label rateBadge = new Label(String.format("%.0f%%", rate));
        rateBadge.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-font-size: 11px;
            -fx-font-weight: 600;
            -fx-padding: 4 8;
            -fx-background-radius: 4;
            """,
            rate >= 95 ? "#ECFDF5" : rate >= 90 ? "#FFFBEB" : "#FEF2F2",
            rate >= 95 ? "#059669" : rate >= 90 ? "#D97706" : "#DC2626"));

        // Status buttons
        HBox statusButtons = new HBox(8);
        statusButtons.setAlignment(Pos.CENTER_RIGHT);

        for (AttendanceStatusButton.AttendanceStatus status : AttendanceStatusButton.AttendanceStatus.values()) {
            if (status == AttendanceStatusButton.AttendanceStatus.UNMARKED) continue;

            Button btn = new Button(status.getShortCode());
            btn.setPrefSize(40, 32);
            btn.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-font-size: 12px;
                -fx-font-weight: 600;
                -fx-background-radius: 4;
                -fx-border-color: %s;
                -fx-border-radius: 4;
                """,
                student.getStatus(selectedDate.get()) == status ? status.getBackgroundColor() : "white",
                student.getStatus(selectedDate.get()) == status ? status.getTextColor() : "#64748B",
                student.getStatus(selectedDate.get()) == status ? status.getTextColor() : "#E2E8F0"));

            btn.setOnAction(e -> {
                student.setStatus(selectedDate.get(), status);
                buildDailyView();
                updateStatistics();
                if (onAttendanceChanged != null) {
                    onAttendanceChanged.accept(new AttendanceChange(student.getStudentId(), selectedDate.get(), status));
                }
            });

            statusButtons.getChildren().add(btn);
        }

        row.getChildren().addAll(photoPlaceholder, studentInfo, rateBadge, statusButtons);

        // Click on row for details
        row.setCursor(javafx.scene.Cursor.HAND);
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseClicked(e -> {
            if (onStudentClick != null) {
                onStudentClick.accept(student);
            }
        });

        return row;
    }

    private void buildWeeklyView() {
        weeklyGrid.getChildren().clear();
        weeklyGrid.getColumnConstraints().clear();
        weeklyGrid.getRowConstraints().clear();

        LocalDate startOfWeek = selectedDate.get().minusDays(selectedDate.get().getDayOfWeek().getValue() - 1);
        List<LocalDate> weekDays = new ArrayList<>();
        for (int i = 0; i < 5; i++) { // Mon-Fri
            weekDays.add(startOfWeek.plusDays(i));
        }

        // Column constraints
        ColumnConstraints nameCol = new ColumnConstraints(180);
        weeklyGrid.getColumnConstraints().add(nameCol);

        for (int i = 0; i < 5; i++) {
            ColumnConstraints col = new ColumnConstraints(60);
            col.setHalignment(javafx.geometry.HPos.CENTER);
            weeklyGrid.getColumnConstraints().add(col);
        }

        // Header row
        Label studentHeader = new Label("Student");
        studentHeader.setStyle("-fx-font-weight: 600; -fx-text-fill: #334155;");
        studentHeader.setMaxWidth(Double.MAX_VALUE);
        studentHeader.setPadding(new Insets(8));
        studentHeader.setStyle("-fx-background-color: #F1F5F9; -fx-font-weight: 600;");
        weeklyGrid.add(studentHeader, 0, 0);

        for (int i = 0; i < weekDays.size(); i++) {
            LocalDate day = weekDays.get(i);
            VBox dayHeader = new VBox(2);
            dayHeader.setAlignment(Pos.CENTER);
            dayHeader.setPadding(new Insets(4));
            dayHeader.setStyle("-fx-background-color: #F1F5F9;");

            Label dayName = new Label(day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            dayName.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

            Label dayNum = new Label(String.valueOf(day.getDayOfMonth()));
            dayNum.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

            if (day.equals(LocalDate.now())) {
                dayNum.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #2563EB;");
            }

            dayHeader.getChildren().addAll(dayName, dayNum);
            weeklyGrid.add(dayHeader, i + 1, 0);
        }

        // Student rows
        for (int r = 0; r < students.size(); r++) {
            StudentAttendance student = students.get(r);

            // Name cell
            Label nameCell = new Label(student.getName());
            nameCell.setMaxWidth(Double.MAX_VALUE);
            nameCell.setPadding(new Insets(8, 12, 8, 12));
            nameCell.setStyle("-fx-background-color: white; -fx-font-size: 13px;");
            weeklyGrid.add(nameCell, 0, r + 1);

            // Status cells
            for (int c = 0; c < weekDays.size(); c++) {
                LocalDate day = weekDays.get(c);
                AttendanceStatusButton btn = new AttendanceStatusButton(student.getStatus(day));
                btn.setStudentId(student.getStudentId());

                final int studentIndex = r;
                final LocalDate cellDate = day;

                btn.setOnStatusChanged(status -> {
                    student.setStatus(cellDate, status);
                    updateStatistics();
                    if (onAttendanceChanged != null) {
                        onAttendanceChanged.accept(new AttendanceChange(student.getStudentId(), cellDate, status));
                    }
                });

                StackPane cellWrapper = new StackPane(btn);
                cellWrapper.setStyle("-fx-background-color: white;");
                cellWrapper.setPadding(new Insets(4));
                weeklyGrid.add(cellWrapper, c + 1, r + 1);
            }
        }
    }

    // ========================================================================
    // STATISTICS PANEL
    // ========================================================================

    private VBox createStatsPanel() {
        VBox panel = new VBox(16);
        panel.setPrefWidth(200);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 0 1;");

        Label statsTitle = new Label("Today's Summary");
        statsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        // Present
        VBox presentCard = createStatCard("Present", "0", "#10B981");
        presentCountLabel = (Label) presentCard.lookup(".stat-value");

        // Absent
        VBox absentCard = createStatCard("Absent", "0", "#EF4444");
        absentCountLabel = (Label) absentCard.lookup(".stat-value");

        // Tardy
        VBox tardyCard = createStatCard("Tardy", "0", "#F59E0B");
        tardyCountLabel = (Label) tardyCard.lookup(".stat-value");

        // Attendance rate
        VBox rateCard = createStatCard("Attendance Rate", "0%", "#2563EB");
        attendanceRateLabel = (Label) rateCard.lookup(".stat-value");

        // Notes section
        Label notesTitle = new Label("Quick Notes");
        notesTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #334155; -fx-padding: 16 0 8 0;");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Add notes for this class period...");
        notesArea.setPrefRowCount(4);
        notesArea.setWrapText(true);
        VBox.setVgrow(notesArea, Priority.NEVER);

        panel.getChildren().addAll(statsTitle, presentCard, absentCard, tardyCard, rateCard, notesTitle, notesArea);

        return panel;
    }

    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8;");

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("stat-value");
        valueNode.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(labelNode, valueNode);
        return card;
    }

    private void updateStatistics() {
        LocalDate date = selectedDate.get();
        int present = 0, absent = 0, tardy = 0, excused = 0;

        for (StudentAttendance student : students) {
            AttendanceStatusButton.AttendanceStatus status = student.getStatus(date);
            switch (status) {
                case PRESENT -> present++;
                case ABSENT -> absent++;
                case TARDY -> tardy++;
                case EXCUSED -> excused++;
            }
        }

        int total = students.size();
        double rate = total > 0 ? ((present + tardy + excused) * 100.0 / total) : 0;

        if (presentCountLabel != null) presentCountLabel.setText(String.valueOf(present));
        if (absentCountLabel != null) absentCountLabel.setText(String.valueOf(absent));
        if (tardyCountLabel != null) tardyCountLabel.setText(String.valueOf(tardy));
        if (attendanceRateLabel != null) attendanceRateLabel.setText(String.format("%.1f%%", rate));
    }

    // ========================================================================
    // ACTIONS
    // ========================================================================

    private void markAllPresent() {
        LocalDate date = selectedDate.get();
        for (StudentAttendance student : students) {
            student.setStatus(date, AttendanceStatusButton.AttendanceStatus.PRESENT);
        }
        refreshView();
    }

    private void clearAll() {
        LocalDate date = selectedDate.get();
        for (StudentAttendance student : students) {
            student.setStatus(date, AttendanceStatusButton.AttendanceStatus.UNMARKED);
        }
        refreshView();
    }

    private void submitAttendance() {
        // Count unmarked
        LocalDate date = selectedDate.get();
        long unmarked = students.stream()
            .filter(s -> s.getStatus(date) == AttendanceStatusButton.AttendanceStatus.UNMARKED)
            .count();

        if (unmarked > 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Submit Attendance");
            alert.setHeaderText(unmarked + " student(s) unmarked");
            alert.setContentText("Do you want to mark remaining students as Present and submit?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    for (StudentAttendance student : students) {
                        if (student.getStatus(date) == AttendanceStatusButton.AttendanceStatus.UNMARKED) {
                            student.setStatus(date, AttendanceStatusButton.AttendanceStatus.PRESENT);
                        }
                    }
                    refreshView();
                    showSubmitConfirmation();
                }
            });
        } else {
            showSubmitConfirmation();
        }
    }

    private void showSubmitConfirmation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Attendance Submitted");
        alert.setHeaderText(null);
        alert.setContentText("Attendance has been submitted successfully.");
        alert.showAndWait();
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    private void loadDemoData() {
        String[] names = {"Anderson, Emma", "Brown, Liam", "Chen, Olivia", "Davis, Noah", "Evans, Ava",
                         "Foster, Ethan", "Garcia, Isabella", "Harris, Mason", "Ibrahim, Sophia", "Johnson, James",
                         "Kim, Emily", "Lee, Michael", "Martinez, Sofia", "Nelson, William", "O'Brien, Charlotte"};

        Random rand = new Random(42);

        for (int i = 0; i < names.length; i++) {
            StudentAttendance student = new StudentAttendance("S" + String.format("%04d", i + 1), names[i]);

            // Generate some historical attendance
            for (int d = 0; d < 30; d++) {
                LocalDate date = LocalDate.now().minusDays(d);
                if (date.getDayOfWeek().getValue() <= 5) { // Weekdays only
                    double r = rand.nextDouble();
                    if (r < 0.85) {
                        student.setStatus(date, AttendanceStatusButton.AttendanceStatus.PRESENT);
                    } else if (r < 0.92) {
                        student.setStatus(date, AttendanceStatusButton.AttendanceStatus.TARDY);
                    } else if (r < 0.97) {
                        student.setStatus(date, AttendanceStatusButton.AttendanceStatus.ABSENT);
                    } else {
                        student.setStatus(date, AttendanceStatusButton.AttendanceStatus.EXCUSED);
                    }
                }
            }

            // Today might be unmarked
            if (rand.nextDouble() < 0.3) {
                student.setStatus(LocalDate.now(), AttendanceStatusButton.AttendanceStatus.UNMARKED);
            }

            students.add(student);
        }

        refreshView();
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    public void setOnAttendanceChanged(Consumer<AttendanceChange> callback) {
        this.onAttendanceChanged = callback;
    }

    public void setOnStudentClick(Consumer<StudentAttendance> callback) {
        this.onStudentClick = callback;
    }

    public void refresh() {
        refreshView();
    }

    // ========================================================================
    // INNER CLASSES
    // ========================================================================

    public enum ViewMode {
        DAILY, WEEKLY
    }

    @Getter
    public static class StudentAttendance {
        private final String studentId;
        private final String name;
        private final Map<LocalDate, AttendanceStatusButton.AttendanceStatus> attendance = new HashMap<>();

        public StudentAttendance(String studentId, String name) {
            this.studentId = studentId;
            this.name = name;
        }

        public String getInitials() {
            String[] parts = name.split("[, ]+");
            if (parts.length >= 2) {
                return (parts[parts.length - 1].charAt(0) + "" + parts[0].charAt(0)).toUpperCase();
            }
            return name.substring(0, Math.min(2, name.length())).toUpperCase();
        }

        public AttendanceStatusButton.AttendanceStatus getStatus(LocalDate date) {
            return attendance.getOrDefault(date, AttendanceStatusButton.AttendanceStatus.UNMARKED);
        }

        public void setStatus(LocalDate date, AttendanceStatusButton.AttendanceStatus status) {
            attendance.put(date, status);
        }

        public double getAttendanceRate() {
            if (attendance.isEmpty()) return 100;

            long present = attendance.values().stream()
                .filter(s -> s == AttendanceStatusButton.AttendanceStatus.PRESENT ||
                            s == AttendanceStatusButton.AttendanceStatus.TARDY ||
                            s == AttendanceStatusButton.AttendanceStatus.EXCUSED)
                .count();

            long total = attendance.values().stream()
                .filter(s -> s != AttendanceStatusButton.AttendanceStatus.UNMARKED)
                .count();

            return total > 0 ? (present * 100.0 / total) : 100;
        }
    }

    @Getter
    public static class AttendanceChange {
        private final String studentId;
        private final LocalDate date;
        private final AttendanceStatusButton.AttendanceStatus status;

        public AttendanceChange(String studentId, LocalDate date, AttendanceStatusButton.AttendanceStatus status) {
            this.studentId = studentId;
            this.date = date;
            this.status = status;
        }
    }
}
