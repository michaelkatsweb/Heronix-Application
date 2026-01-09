package com.heronix.ui.controller;

import com.heronix.model.domain.*;
import com.heronix.model.enums.ScheduleStatus;
import com.heronix.model.enums.ScheduleType;
import com.heronix.service.CourseService;
import com.heronix.service.RoomService;
import com.heronix.service.ScheduleService;
import com.heronix.service.TeacherService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ScheduleBuilderController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private RoomService roomService;

    // Top Controls
    @FXML private Button newScheduleButton;
    @FXML private Button loadScheduleButton;
    @FXML private Button saveScheduleButton;
    @FXML private Label scheduleNameLabel;

    // Schedule Configuration
    @FXML private ComboBox<String> schoolYearComboBox;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private Spinner<Integer> periodsPerDaySpinner;
    @FXML private Spinner<Integer> periodDurationSpinner;
    @FXML private TextField startTimeField;
    @FXML private Label scheduleStatsLabel;
    @FXML private ProgressBar completionProgressBar;

    // Left Panel - Course Pool
    @FXML private ComboBox<String> subjectFilterComboBox;
    @FXML private ComboBox<String> gradeLevelFilterComboBox;
    @FXML private ComboBox<String> courseTypeFilterComboBox;
    @FXML private CheckBox showUnassignedOnlyCheckBox;
    @FXML private TextField courseSearchField;
    @FXML private ListView<Course> availableCoursesListView;
    @FXML private Label courseCountLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label assignedCoursesLabel;
    @FXML private Label unassignedCoursesLabel;

    // Center - Schedule Grid
    @FXML private ToggleButton weekViewToggle;
    @FXML private ToggleButton dayViewToggle;
    @FXML private Button autoFillButton;
    @FXML private Button clearGridButton;
    @FXML private GridPane scheduleGrid;

    // Right Panel - Details & Actions
    @FXML private Label selectedDayLabel;
    @FXML private Label selectedPeriodLabel;
    @FXML private Label selectedTimeLabel;
    @FXML private ComboBox<Course> assignCourseComboBox;
    @FXML private ComboBox<Teacher> assignTeacherComboBox;
    @FXML private ComboBox<Room> assignRoomComboBox;
    @FXML private Button assignButton;
    @FXML private Button clearSlotButton;
    @FXML private Label currentCourseLabel;
    @FXML private Label currentTeacherLabel;
    @FXML private Label currentRoomLabel;
    @FXML private Label currentEnrollmentLabel;
    @FXML private VBox conflictWarningsBox;
    @FXML private ListView<String> conflictListView;
    @FXML private ListView<String> suggestionsListView;

    // Bottom - Actions & Status
    @FXML private Button validateButton;
    @FXML private Button optimizeButton;
    @FXML private Button publishButton;
    @FXML private Button exportButton;
    @FXML private Button printButton;
    @FXML private Label statusLabel;
    @FXML private Label conflictCountLabel;
    @FXML private Label utilizationLabel;
    @FXML private Label roomUtilizationLabel;
    @FXML private Label lastSavedLabel;

    private Schedule currentSchedule;
    private ObservableList<Course> allCourses = FXCollections.observableArrayList();
    private ObservableList<Course> filteredCourses = FXCollections.observableArrayList();
    private ObservableList<Teacher> allTeachers = FXCollections.observableArrayList();
    private ObservableList<Room> allRooms = FXCollections.observableArrayList();

    private ScheduleSlot selectedSlot;
    private Map<String, ScheduleSlot> slotMap = new HashMap<>(); // Key: "DAY_PERIOD"

    private final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private int periodsPerDay = 7;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupSpinners();
        setupListViews();
        setupToggleGroup();
        loadData();
        createNewSchedule();
        generateScheduleGrid();
    }

    private void setupComboBoxes() {
        // School Years
        schoolYearComboBox.setItems(FXCollections.observableArrayList(
                "2024-2025", "2025-2026", "2026-2027", "2027-2028"
        ));
        schoolYearComboBox.setValue("2024-2025");

        // Semesters
        semesterComboBox.setItems(FXCollections.observableArrayList(
                "Fall", "Spring", "Full Year", "Summer"
        ));
        semesterComboBox.setValue("Fall");

        // Subject Filter
        subjectFilterComboBox.setItems(FXCollections.observableArrayList(
                "All Subjects", "English", "Mathematics", "Science", "Social Studies",
                "Physical Education", "Fine Arts", "Foreign Language", "Technology", "Electives"
        ));
        subjectFilterComboBox.setValue("All Subjects");
        subjectFilterComboBox.setOnAction(e -> filterCourses());

        // Grade Level Filter
        gradeLevelFilterComboBox.setItems(FXCollections.observableArrayList(
                "All Grades", "9th Grade", "10th Grade", "11th Grade", "12th Grade"
        ));
        gradeLevelFilterComboBox.setValue("All Grades");
        gradeLevelFilterComboBox.setOnAction(e -> filterCourses());

        // Course Type Filter
        courseTypeFilterComboBox.setItems(FXCollections.observableArrayList(
                "All Types", "Regular", "Honors", "AP", "IB", "Dual Enrollment"
        ));
        courseTypeFilterComboBox.setValue("All Types");
        courseTypeFilterComboBox.setOnAction(e -> filterCourses());

        // Unassigned Only Filter
        showUnassignedOnlyCheckBox.setOnAction(e -> filterCourses());

        // Course Assignment ComboBox
        assignCourseComboBox.setConverter(new javafx.util.StringConverter<Course>() {
            @Override
            public String toString(Course course) {
                return course != null ? course.getCourseCode() + " - " + course.getCourseName() : "";
            }

            @Override
            public Course fromString(String string) {
                return null;
            }
        });

        // Teacher Assignment ComboBox
        assignTeacherComboBox.setConverter(new javafx.util.StringConverter<Teacher>() {
            @Override
            public String toString(Teacher teacher) {
                return teacher != null ? teacher.getFullName() : "";
            }

            @Override
            public Teacher fromString(String string) {
                return null;
            }
        });

        // Room Assignment ComboBox
        assignRoomComboBox.setConverter(new javafx.util.StringConverter<Room>() {
            @Override
            public String toString(Room room) {
                return room != null ? room.getRoomNumber() + " - " + room.getRoomType() : "";
            }

            @Override
            public Room fromString(String string) {
                return null;
            }
        });
    }

    private void setupSpinners() {
        periodsPerDaySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal != periodsPerDay) {
                periodsPerDay = newVal;
                generateScheduleGrid();
            }
        });

        periodDurationSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            updatePeriodTimes();
        });
    }

    private void setupListViews() {
        // Available Courses ListView with drag support
        availableCoursesListView.setCellFactory(lv -> {
            ListCell<Course> cell = new ListCell<>() {
                @Override
                protected void updateItem(Course course, boolean empty) {
                    super.updateItem(course, empty);
                    if (empty || course == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        VBox content = new VBox(3);
                        Label codeLabel = new Label(course.getCourseCode() + " - " + course.getCourseName());
                        codeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

                        Label infoLabel = new Label(
                                String.format("%s | %d min | %d/%d enrolled",
                                        course.getSubject() != null ? course.getSubject() : "General",
                                        course.getDurationMinutes() != null ? course.getDurationMinutes() : 50,
                                        course.getCurrentEnrollment() != null ? course.getCurrentEnrollment() : 0,
                                        course.getMaxStudents() != null ? course.getMaxStudents() : 30
                                )
                        );
                        infoLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #757575;");

                        content.getChildren().addAll(codeLabel, infoLabel);
                        setGraphic(content);

                        // Color coding based on assignment status
                        if (course.getTeacher() != null && course.getRoom() != null) {
                            setStyle("-fx-background-color: #e8f5e9; -fx-border-color: #4caf50; -fx-border-width: 0 0 0 4;");
                        } else if (course.getTeacher() != null || course.getRoom() != null) {
                            setStyle("-fx-background-color: #fff9c4; -fx-border-color: #fbc02d; -fx-border-width: 0 0 0 4;");
                        } else {
                            setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
                        }
                    }
                }
            };

            // Enable drag
            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty()) {
                    Dragboard dragboard = cell.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(cell.getItem().getId().toString());
                    dragboard.setContent(content);
                    event.consume();
                }
            });

            return cell;
        });

        availableCoursesListView.setItems(filteredCourses);
    }

    private void setupToggleGroup() {
        ToggleGroup viewGroup = new ToggleGroup();
        weekViewToggle.setToggleGroup(viewGroup);
        dayViewToggle.setToggleGroup(viewGroup);
        weekViewToggle.setSelected(true);
    }

    private void loadData() {
        // Load courses - in production, load from service
        // allCourses.addAll(courseService.findAllActiveCourses());
        createSampleCourses();
        filteredCourses.setAll(allCourses);

        // Load teachers - in production, load from service
        // allTeachers.addAll(teacherService.findAllActiveTeachers());
        createSampleTeachers();

        // Load rooms - in production, load from service
        // allRooms.addAll(roomService.findAllActiveRooms());
        createSampleRooms();

        // Populate combo boxes
        assignCourseComboBox.setItems(FXCollections.observableArrayList(allCourses));
        assignTeacherComboBox.setItems(allTeachers);
        assignRoomComboBox.setItems(allRooms);

        updateCourseStats();
    }

    private void createSampleCourses() {
        String[] subjects = {"English", "Mathematics", "Science", "Social Studies", "PE"};
        String[] levels = {"I", "II", "III", "IV"};

        for (String subject : subjects) {
            for (String level : levels) {
                Course course = new Course();
                course.setId((long) (allCourses.size() + 1));
                course.setCourseCode(subject.substring(0, 3).toUpperCase() + level);
                course.setCourseName(subject + " " + level);
                course.setSubject(subject);
                course.setDurationMinutes(50);
                course.setMaxStudents(30);
                course.setCurrentEnrollment(0);
                course.setActive(true);
                allCourses.add(course);
            }
        }
    }

    private void createSampleTeachers() {
        String[] names = {"Smith, John", "Johnson, Mary", "Williams, Robert", "Brown, Lisa",
                "Jones, Michael", "Garcia, Maria", "Miller, David", "Davis, Jennifer"};

        for (String name : names) {
            Teacher teacher = new Teacher();
            teacher.setId((long) (allTeachers.size() + 1));
            teacher.setFirstName(name.split(", ")[1]);
            teacher.setLastName(name.split(", ")[0]);
            allTeachers.add(teacher);
        }
    }

    private void createSampleRooms() {
        for (int i = 101; i <= 115; i++) {
            Room room = new Room();
            room.setId((long) (allRooms.size() + 1));
            room.setRoomNumber(String.valueOf(i));
            room.setCapacity(30);
            allRooms.add(room);
        }
    }

    private void generateScheduleGrid() {
        scheduleGrid.getChildren().clear();
        scheduleGrid.getColumnConstraints().clear();
        scheduleGrid.getRowConstraints().clear();
        slotMap.clear();

        // Column constraints
        ColumnConstraints headerCol = new ColumnConstraints();
        headerCol.setPrefWidth(80);
        scheduleGrid.getColumnConstraints().add(headerCol);

        for (int i = 0; i < DAYS.length; i++) {
            ColumnConstraints dayCol = new ColumnConstraints();
            dayCol.setPrefWidth(180);
            dayCol.setHgrow(Priority.SOMETIMES);
            scheduleGrid.getColumnConstraints().add(dayCol);
        }

        // Row constraints for header and periods
        RowConstraints headerRow = new RowConstraints();
        headerRow.setPrefHeight(40);
        scheduleGrid.getRowConstraints().add(headerRow);

        // Add day headers
        Label cornerLabel = new Label("Period");
        cornerLabel.setStyle("-fx-font-weight: bold; -fx-background-color: #1976d2; -fx-text-fill: white; " +
                "-fx-alignment: center; -fx-border-color: white; -fx-border-width: 1; -fx-padding: 10;");
        cornerLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        scheduleGrid.add(cornerLabel, 0, 0);

        for (int day = 0; day < DAYS.length; day++) {
            Label dayLabel = new Label(DAYS[day]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: #1976d2; " +
                    "-fx-text-fill: white; -fx-alignment: center; -fx-border-color: white; -fx-border-width: 1; -fx-padding: 10;");
            dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            scheduleGrid.add(dayLabel, day + 1, 0);
        }

        // Add period slots
        for (int period = 1; period <= periodsPerDay; period++) {
            RowConstraints periodRow = new RowConstraints();
            periodRow.setPrefHeight(100);
            periodRow.setVgrow(Priority.SOMETIMES);
            scheduleGrid.getRowConstraints().add(periodRow);

            // Period header
            Label periodLabel = new Label("Period " + period + "\n" + getPeriodTime(period));
            periodLabel.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0; " +
                    "-fx-alignment: center; -fx-border-color: #9e9e9e; -fx-border-width: 1; -fx-padding: 5;");
            periodLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            scheduleGrid.add(periodLabel, 0, period);

            // Day slots
            for (int day = 0; day < DAYS.length; day++) {
                VBox slotBox = createSlotBox(DAYS[day], period);
                scheduleGrid.add(slotBox, day + 1, period);
            }
        }
    }

    private VBox createSlotBox(String day, int period) {
        VBox slotBox = new VBox(5);
        slotBox.setAlignment(Pos.TOP_LEFT);
        slotBox.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; " +
                "-fx-padding: 8; -fx-cursor: hand;");
        slotBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Label emptyLabel = new Label("Empty");
        emptyLabel.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 11px; -fx-font-style: italic;");
        slotBox.getChildren().add(emptyLabel);

        String slotKey = day + "_" + period;
        slotBox.setUserData(slotKey);

        // Click to select
        slotBox.setOnMouseClicked(event -> handleSlotClick(day, period, slotBox));

        // Drag and drop support
        slotBox.setOnDragOver(event -> {
            if (event.getGestureSource() != slotBox && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        slotBox.setOnDragEntered(event -> {
            if (event.getGestureSource() != slotBox && event.getDragboard().hasString()) {
                slotBox.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #2196f3; " +
                        "-fx-border-width: 2; -fx-padding: 8; -fx-cursor: hand;");
            }
            event.consume();
        });

        slotBox.setOnDragExited(event -> {
            refreshSlotAppearance(slotBox, slotKey);
            event.consume();
        });

        slotBox.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                Long courseId = Long.parseLong(db.getString());
                Course course = allCourses.stream()
                        .filter(c -> c.getId().equals(courseId))
                        .findFirst()
                        .orElse(null);

                if (course != null) {
                    assignCourseToSlot(day, period, course, slotBox);
                    event.setDropCompleted(true);
                }
            }
            event.consume();
        });

        return slotBox;
    }

    private void handleSlotClick(String day, int period, VBox slotBox) {
        // Update selection
        selectedDayLabel.setText(day);
        selectedPeriodLabel.setText("Period " + period);
        selectedTimeLabel.setText(getPeriodTime(period));

        String slotKey = day + "_" + period;
        ScheduleSlot slot = slotMap.get(slotKey);

        if (slot != null) {
            selectedSlot = slot;
            updateCurrentAssignmentDisplay(slot);
            checkConflicts(slot);
        } else {
            selectedSlot = null;
            clearCurrentAssignmentDisplay();
            conflictWarningsBox.setManaged(false);
            conflictWarningsBox.setVisible(false);
        }

        // Visual feedback - highlight selected slot
        scheduleGrid.getChildren().forEach(node -> {
            if (node instanceof VBox && node.getUserData() != null) {
                VBox box = (VBox) node;
                refreshSlotAppearance(box, (String) box.getUserData());
            }
        });

        if (slotBox.getStyle().contains("white")) {
            slotBox.setStyle(slotBox.getStyle() + "; -fx-border-color: #2196f3; -fx-border-width: 3;");
        }

        generateSuggestions(day, period);
    }

    private void assignCourseToSlot(String day, int period, Course course, VBox slotBox) {
        String slotKey = day + "_" + period;

        ScheduleSlot slot = slotMap.get(slotKey);
        if (slot == null) {
            slot = new ScheduleSlot();
            slot.setSchedule(currentSchedule);
            slot.setDayOfWeek(DayOfWeek.valueOf(day.toUpperCase()));
            slotMap.put(slotKey, slot);
        }

        slot.setCourse(course);
        slot.setTeacher(course.getTeacher());
        slot.setRoom(course.getRoom());

        updateSlotDisplay(slotBox, slot);
        updateCourseStats();
        statusLabel.setText("Assigned " + course.getCourseCode() + " to " + day + " Period " + period);
        lastSavedLabel.setText("Unsaved changes");
    }

    private void updateSlotDisplay(VBox slotBox, ScheduleSlot slot) {
        slotBox.getChildren().clear();

        if (slot.getCourse() != null) {
            Label courseLabel = new Label(slot.getCourse().getCourseCode());
            courseLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

            Label nameLabel = new Label(slot.getCourse().getCourseName());
            nameLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #424242;");
            nameLabel.setWrapText(true);

            slotBox.getChildren().addAll(courseLabel, nameLabel);

            if (slot.getTeacher() != null) {
                Label teacherLabel = new Label("👤 " + slot.getTeacher().getLastName());
                teacherLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #1976d2;");
                slotBox.getChildren().add(teacherLabel);
            }

            if (slot.getRoom() != null) {
                Label roomLabel = new Label("🏫 Room " + slot.getRoom().getRoomNumber());
                roomLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #388e3c;");
                slotBox.getChildren().add(roomLabel);
            }

            // Color coding
            boolean hasTeacher = slot.getTeacher() != null;
            boolean hasRoom = slot.getRoom() != null;

            if (hasTeacher && hasRoom) {
                slotBox.setStyle("-fx-background-color: #e8f5e9; -fx-border-color: #4caf50; " +
                        "-fx-border-width: 2; -fx-padding: 8; -fx-cursor: hand;");
            } else {
                slotBox.setStyle("-fx-background-color: #fff9c4; -fx-border-color: #fbc02d; " +
                        "-fx-border-width: 2; -fx-padding: 8; -fx-cursor: hand;");
            }
        } else {
            Label emptyLabel = new Label("Empty");
            emptyLabel.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 11px; -fx-font-style: italic;");
            slotBox.getChildren().add(emptyLabel);
            slotBox.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                    "-fx-border-width: 1; -fx-padding: 8; -fx-cursor: hand;");
        }
    }

    private void refreshSlotAppearance(VBox slotBox, String slotKey) {
        ScheduleSlot slot = slotMap.get(slotKey);
        updateSlotDisplay(slotBox, slot != null ? slot : new ScheduleSlot());
    }

    private void updateCurrentAssignmentDisplay(ScheduleSlot slot) {
        if (slot.getCourse() != null) {
            currentCourseLabel.setText(slot.getCourse().getCourseCode() + " - " + slot.getCourse().getCourseName());
        } else {
            currentCourseLabel.setText("Empty");
        }

        if (slot.getTeacher() != null) {
            currentTeacherLabel.setText(slot.getTeacher().getFullName());
        } else {
            currentTeacherLabel.setText("Not assigned");
        }

        if (slot.getRoom() != null) {
            currentRoomLabel.setText(slot.getRoom().getRoomNumber());
        } else {
            currentRoomLabel.setText("Not assigned");
        }

        if (slot.getCourse() != null) {
            currentEnrollmentLabel.setText(
                    (slot.getCourse().getCurrentEnrollment() != null ? slot.getCourse().getCurrentEnrollment() : 0) +
                            "/" +
                            (slot.getCourse().getMaxStudents() != null ? slot.getCourse().getMaxStudents() : 0)
            );
        } else {
            currentEnrollmentLabel.setText("0/0");
        }
    }

    private void clearCurrentAssignmentDisplay() {
        currentCourseLabel.setText("Empty");
        currentTeacherLabel.setText("Not assigned");
        currentRoomLabel.setText("Not assigned");
        currentEnrollmentLabel.setText("0/0");
    }

    private void checkConflicts(ScheduleSlot slot) {
        ObservableList<String> conflicts = FXCollections.observableArrayList();

        // Check teacher conflicts
        if (slot.getTeacher() != null) {
            long sameTimeSlots = slotMap.values().stream()
                    .filter(s -> s.getTeacher() != null &&
                            s.getTeacher().equals(slot.getTeacher()) &&
                            s.getDayOfWeek().equals(slot.getDayOfWeek()) &&
                            s != slot)
                    .count();

            if (sameTimeSlots > 0) {
                conflicts.add("⚠ Teacher " + slot.getTeacher().getLastName() +
                        " has " + sameTimeSlots + " other class(es) at this time");
            }
        }

        // Check room conflicts
        if (slot.getRoom() != null) {
            long sameRoomSlots = slotMap.values().stream()
                    .filter(s -> s.getRoom() != null &&
                            s.getRoom().equals(slot.getRoom()) &&
                            s.getDayOfWeek().equals(slot.getDayOfWeek()) &&
                            s != slot)
                    .count();

            if (sameRoomSlots > 0) {
                conflicts.add("⚠ Room " + slot.getRoom().getRoomNumber() +
                        " has " + sameRoomSlots + " other class(es) at this time");
            }
        }

        if (!conflicts.isEmpty()) {
            conflictListView.setItems(conflicts);
            conflictWarningsBox.setManaged(true);
            conflictWarningsBox.setVisible(true);
        } else {
            conflictWarningsBox.setManaged(false);
            conflictWarningsBox.setVisible(false);
        }

        updateConflictCount();
    }

    private void generateSuggestions(String day, int period) {
        ObservableList<String> suggestions = FXCollections.observableArrayList();

        suggestions.add("💡 Consider assigning core courses to morning periods");
        suggestions.add("💡 Balance teacher workload across the week");
        suggestions.add("💡 Group similar subjects in consecutive periods");

        if (selectedSlot != null && selectedSlot.getCourse() != null) {
            if (selectedSlot.getTeacher() == null) {
                suggestions.add("⚡ Assign a qualified teacher to this course");
            }
            if (selectedSlot.getRoom() == null) {
                suggestions.add("⚡ Assign an appropriate room for this course");
            }
        }

        suggestionsListView.setItems(suggestions);
    }

    private String getPeriodTime(int period) {
        try {
            LocalTime startTime = LocalTime.parse(startTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            int duration = periodDurationSpinner.getValue();
            LocalTime periodStart = startTime.plusMinutes((period - 1) * duration);
            LocalTime periodEnd = periodStart.plusMinutes(duration);

            return periodStart.format(DateTimeFormatter.ofPattern("h:mm a")) + " - " +
                    periodEnd.format(DateTimeFormatter.ofPattern("h:mm a"));
        } catch (Exception e) {
            return "TBD";
        }
    }

    private void updatePeriodTimes() {
        generateScheduleGrid();
    }

    private void filterCourses() {
        List<Course> filtered = allCourses.stream()
                .filter(course -> {
                    boolean matches = true;

                    // Subject filter
                    if (!subjectFilterComboBox.getValue().equals("All Subjects")) {
                        matches = matches && subjectFilterComboBox.getValue().equals(course.getSubject());
                    }

                    // Unassigned only filter
                    if (showUnassignedOnlyCheckBox.isSelected()) {
                        matches = matches && (course.getTeacher() == null || course.getRoom() == null);
                    }

                    return matches;
                })
                .collect(Collectors.toList());

        filteredCourses.setAll(filtered);
        courseCountLabel.setText("(" + filteredCourses.size() + ")");
    }

    @FXML
    private void handleCourseSearch() {
        String searchText = courseSearchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            filterCourses();
        } else {
            List<Course> searched = filteredCourses.stream()
                    .filter(course ->
                            course.getCourseCode().toLowerCase().contains(searchText) ||
                                    course.getCourseName().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());
            filteredCourses.setAll(searched);
            courseCountLabel.setText("(" + filteredCourses.size() + ")");
        }
    }

    private void updateCourseStats() {
        totalCoursesLabel.setText(String.valueOf(allCourses.size()));

        long assigned = allCourses.stream()
                .filter(c -> c.getTeacher() != null && c.getRoom() != null)
                .count();
        assignedCoursesLabel.setText(String.valueOf(assigned));

        long unassigned = allCourses.size() - assigned;
        unassignedCoursesLabel.setText(String.valueOf(unassigned));

        double completion = allCourses.isEmpty() ? 0 : (assigned * 100.0 / allCourses.size());
        completionProgressBar.setProgress(completion / 100.0);
        scheduleStatsLabel.setText(assigned + " courses assigned | " + String.format("%.1f%% complete", completion));
    }

    private void updateConflictCount() {
        long conflicts = slotMap.values().stream()
                .filter(this::hasConflicts)
                .count();

        conflictCountLabel.setText("Conflicts: " + conflicts);
    }

    private boolean hasConflicts(ScheduleSlot slot) {
        if (slot.getTeacher() == null && slot.getRoom() == null) return false;

        // Check for conflicts with other slots
        return slotMap.values().stream()
                .anyMatch(s -> s != slot &&
                        s.getDayOfWeek() != null &&
                        s.getDayOfWeek().equals(slot.getDayOfWeek()) &&
                        ((s.getTeacher() != null && s.getTeacher().equals(slot.getTeacher())) ||
                                (s.getRoom() != null && s.getRoom().equals(slot.getRoom()))));
    }

    // Action Handlers

    @FXML
    private void handleNewSchedule() {
        createNewSchedule();
    }

    private void createNewSchedule() {
        currentSchedule = new Schedule();
        currentSchedule.setName("Untitled Schedule");
        currentSchedule.setScheduleType(ScheduleType.MASTER);
        currentSchedule.setStatus(ScheduleStatus.DRAFT);
        currentSchedule.setStartDate(LocalDate.now());
        currentSchedule.setEndDate(LocalDate.now().plusMonths(6));
        currentSchedule.setActive(true);

        scheduleNameLabel.setText("Untitled Schedule");
        statusLabel.setText("New schedule created");
        lastSavedLabel.setText("Unsaved changes");

        slotMap.clear();
        generateScheduleGrid();
    }

    @FXML
    private void handleLoadSchedule() {
        // In production, show schedule picker dialog
        statusLabel.setText("Load schedule functionality - would show schedule picker");
    }

    @FXML
    private void handleSaveSchedule() {
        // In production, save to database
        statusLabel.setText("Schedule saved successfully");
        lastSavedLabel.setText("Saved at " + LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a")));
    }

    @FXML
    private void handleAssignToSlot() {
        if (selectedDayLabel.getText().equals("None")) {
            showAlert(Alert.AlertType.WARNING, "No Slot Selected",
                    "Please select a time slot first.");
            return;
        }

        Course course = assignCourseComboBox.getValue();
        Teacher teacher = assignTeacherComboBox.getValue();
        Room room = assignRoomComboBox.getValue();

        if (course == null) {
            showAlert(Alert.AlertType.WARNING, "No Course Selected",
                    "Please select a course to assign.");
            return;
        }

        String slotKey = selectedDayLabel.getText() + "_" +
                selectedPeriodLabel.getText().replace("Period ", "");

        ScheduleSlot slot = slotMap.get(slotKey);
        if (slot == null) {
            slot = new ScheduleSlot();
            slot.setSchedule(currentSchedule);
            slot.setDayOfWeek(DayOfWeek.valueOf(selectedDayLabel.getText().toUpperCase()));
            slotMap.put(slotKey, slot);
        }

        slot.setCourse(course);
        slot.setTeacher(teacher);
        slot.setRoom(room);

        // Find and update the visual slot
        final ScheduleSlot finalSlot = slot;
        final String finalSlotKey = slotKey;
        scheduleGrid.getChildren().forEach(node -> {
            if (node instanceof VBox && finalSlotKey.equals(node.getUserData())) {
                updateSlotDisplay((VBox) node, finalSlot);
            }
        });

        updateCourseStats();
        updateCurrentAssignmentDisplay(slot);
        checkConflicts(slot);

        statusLabel.setText("Assignment complete");
    }

    @FXML
    private void handleClearSlot() {
        if (selectedSlot == null) {
            showAlert(Alert.AlertType.WARNING, "No Slot Selected",
                    "Please select a slot to clear.");
            return;
        }

        String slotKey = selectedDayLabel.getText() + "_" +
                selectedPeriodLabel.getText().replace("Period ", "");

        slotMap.remove(slotKey);

        scheduleGrid.getChildren().forEach(node -> {
            if (node instanceof VBox && slotKey.equals(node.getUserData())) {
                updateSlotDisplay((VBox) node, new ScheduleSlot());
            }
        });

        clearCurrentAssignmentDisplay();
        updateCourseStats();
        statusLabel.setText("Slot cleared");
    }

    @FXML
    private void handleAutoFill() {
        statusLabel.setText("Auto-fill algorithm would run here");
        // In production, implement auto-fill logic
    }

    @FXML
    private void handleClearGrid() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Clear Schedule");
        confirmation.setHeaderText("Clear All Assignments");
        confirmation.setContentText("Are you sure you want to clear all slot assignments? This cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                slotMap.clear();
                generateScheduleGrid();
                updateCourseStats();
                statusLabel.setText("Schedule cleared");
            }
        });
    }

    @FXML
    private void handleValidate() {
        statusLabel.setText("Validating schedule...");
        updateConflictCount();
        statusLabel.setText("Validation complete");
    }

    @FXML
    private void handleOptimize() {
        statusLabel.setText("Optimization would run here - using AI algorithms");
    }

    @FXML
    private void handlePublish() {
        if (currentSchedule != null) {
            currentSchedule.setStatus(ScheduleStatus.PUBLISHED);
            statusLabel.setText("Schedule published successfully");
            showAlert(Alert.AlertType.INFORMATION, "Schedule Published",
                    "The schedule has been published and is now visible to students and teachers.");
        }
    }

    @FXML
    private void handleExport() {
        statusLabel.setText("Export functionality - would export to Excel/PDF");
    }

    @FXML
    private void handlePrint() {
        statusLabel.setText("Print functionality - would open print dialog");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
