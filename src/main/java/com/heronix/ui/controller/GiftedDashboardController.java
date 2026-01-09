package com.heronix.ui.controller;

import com.heronix.model.domain.GiftedStudent;
import com.heronix.model.domain.Student;
import com.heronix.service.GiftedManagementService;
import com.heronix.service.StudentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GiftedDashboardController {

    @Autowired
    private GiftedManagementService giftedManagementService;

    @Autowired
    private StudentService studentService;

    // Summary Statistics
    @FXML private Label totalGiftedLabel;
    @FXML private Label activeServicesLabel;
    @FXML private Label screeningLabel;
    @FXML private Label reviewsDueLabel;
    @FXML private Label advancedCoursesLabel;
    @FXML private Label multiTalentedLabel;

    // Charts
    @FXML private PieChart giftedAreasChart;
    @FXML private PieChart programTypeChart;
    @FXML private BarChart<String, Number> statusChart;

    // Alert Lists
    @FXML private ListView<String> annualReviewsDueListView;
    @FXML private ListView<String> parentConsentNeededListView;
    @FXML private ListView<String> underperformingListView;
    @FXML private ListView<String> progressReviewsOverdueListView;

    // Filters
    @FXML private ComboBox<GiftedStudent.GiftedStatus> statusFilterComboBox;
    @FXML private ComboBox<GiftedStudent.GiftedArea> giftedAreaFilterComboBox;
    @FXML private ComboBox<GiftedStudent.ProgramType> programTypeFilterComboBox;
    @FXML private ComboBox<String> gradeFilterComboBox;
    @FXML private TextField searchField;

    // Students Table
    @FXML private TableView<GiftedStudent> studentsTableView;
    @FXML private TableColumn<GiftedStudent, String> studentIdColumn;
    @FXML private TableColumn<GiftedStudent, String> lastNameColumn;
    @FXML private TableColumn<GiftedStudent, String> firstNameColumn;
    @FXML private TableColumn<GiftedStudent, String> gradeColumn;
    @FXML private TableColumn<GiftedStudent, String> statusColumn;
    @FXML private TableColumn<GiftedStudent, String> primaryAreaColumn;
    @FXML private TableColumn<GiftedStudent, String> programTypeColumn;
    @FXML private TableColumn<GiftedStudent, Double> gpaColumn;
    @FXML private TableColumn<GiftedStudent, Integer> advancedCoursesColumn;
    @FXML private TableColumn<GiftedStudent, String> reviewStatusColumn;
    @FXML private TableColumn<GiftedStudent, Void> actionsColumn;

    private List<GiftedStudent> rawGiftedStudents = new ArrayList<>();
    private ObservableList<GiftedStudent> filteredGiftedStudents = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTable();
        loadData();
        setupSearchListener();
    }

    private void setupComboBoxes() {
        // Status Filter
        ObservableList<GiftedStudent.GiftedStatus> statuses = FXCollections.observableArrayList(
                GiftedStudent.GiftedStatus.values());
        statusFilterComboBox.setItems(statuses);
        statusFilterComboBox.setConverter(new StringConverter<GiftedStudent.GiftedStatus>() {
            @Override
            public String toString(GiftedStudent.GiftedStatus status) {
                return status != null ? status.getDisplayName() : "All Statuses";
            }

            @Override
            public GiftedStudent.GiftedStatus fromString(String string) {
                return null;
            }
        });

        // Gifted Area Filter
        ObservableList<GiftedStudent.GiftedArea> areas = FXCollections.observableArrayList(
                GiftedStudent.GiftedArea.values());
        giftedAreaFilterComboBox.setItems(areas);
        giftedAreaFilterComboBox.setConverter(new StringConverter<GiftedStudent.GiftedArea>() {
            @Override
            public String toString(GiftedStudent.GiftedArea area) {
                return area != null ? area.getDisplayName() : "All Areas";
            }

            @Override
            public GiftedStudent.GiftedArea fromString(String string) {
                return null;
            }
        });

        // Program Type Filter
        ObservableList<GiftedStudent.ProgramType> programs = FXCollections.observableArrayList(
                GiftedStudent.ProgramType.values());
        programTypeFilterComboBox.setItems(programs);
        programTypeFilterComboBox.setConverter(new StringConverter<GiftedStudent.ProgramType>() {
            @Override
            public String toString(GiftedStudent.ProgramType type) {
                return type != null ? type.getDisplayName() : "All Programs";
            }

            @Override
            public GiftedStudent.ProgramType fromString(String string) {
                return null;
            }
        });

        // Grade Filter
        List<String> grades = Arrays.asList("K", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
        gradeFilterComboBox.setItems(FXCollections.observableArrayList(grades));
    }

    private void setupTable() {
        // Student ID
        studentIdColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue().getStudent();
            return new SimpleStringProperty(student != null ? student.getStudentId() : "");
        });

        // Last Name
        lastNameColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue().getStudent();
            return new SimpleStringProperty(student != null ? student.getLastName() : "");
        });

        // First Name
        firstNameColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue().getStudent();
            return new SimpleStringProperty(student != null ? student.getFirstName() : "");
        });

        // Grade
        gradeColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue().getStudent();
            return new SimpleStringProperty(student != null && student.getGradeLevel() != null ?
                    student.getGradeLevel().toString() : "");
        });

        // Status
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getGiftedStatus() != null ?
                        cellData.getValue().getGiftedStatus().getDisplayName() : ""));

        // Primary Area
        primaryAreaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPrimaryGiftedArea() != null ?
                        cellData.getValue().getPrimaryGiftedArea().getDisplayName() : ""));

        // Program Type
        programTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getProgramType() != null ?
                        cellData.getValue().getProgramType().getDisplayName() : ""));

        // GPA
        gpaColumn.setCellValueFactory(new PropertyValueFactory<>("currentGpa"));

        // Advanced Courses
        advancedCoursesColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAdvancedCourseCount()));

        // Review Status
        reviewStatusColumn.setCellValueFactory(cellData -> {
            GiftedStudent gs = cellData.getValue();
            if (gs.needsAnnualReview()) {
                return new SimpleStringProperty("Annual Review Due");
            } else if (gs.needsProgressReview()) {
                return new SimpleStringProperty("Progress Review Due");
            } else {
                return new SimpleStringProperty("Up to Date");
            }
        });

        // Actions Column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View Profile");

            {
                viewButton.setStyle("-fx-background-color: #6a1b9a; -fx-text-fill: white; -fx-cursor: hand;");
                viewButton.setOnAction(event -> {
                    GiftedStudent giftedStudent = getTableView().getItems().get(getIndex());
                    handleViewProfile(giftedStudent);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });

        studentsTableView.setItems(filteredGiftedStudents);
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void loadData() {
        rawGiftedStudents = giftedManagementService.findAllGiftedStudents();
        applyFilters();
        updateStatistics();
        updateCharts();
        updateAlertLists();
    }

    private void updateStatistics() {
        int total = rawGiftedStudents.size();
        long activeServices = rawGiftedStudents.stream()
                .filter(GiftedStudent::isActive)
                .count();
        long screening = rawGiftedStudents.stream()
                .filter(gs -> gs.getGiftedStatus() == GiftedStudent.GiftedStatus.REFERRED ||
                             gs.getGiftedStatus() == GiftedStudent.GiftedStatus.SCREENING_IN_PROGRESS ||
                             gs.getGiftedStatus() == GiftedStudent.GiftedStatus.ASSESSMENT_IN_PROGRESS)
                .count();
        long reviewsDue = rawGiftedStudents.stream()
                .filter(GiftedStudent::needsAnnualReview)
                .count();
        int totalAdvancedCourses = rawGiftedStudents.stream()
                .mapToInt(GiftedStudent::getAdvancedCourseCount)
                .sum();
        long multiTalented = rawGiftedStudents.stream()
                .filter(GiftedStudent::isMultiTalented)
                .count();

        totalGiftedLabel.setText(String.valueOf(total));
        activeServicesLabel.setText(String.valueOf(activeServices));
        screeningLabel.setText(String.valueOf(screening));
        reviewsDueLabel.setText(String.valueOf(reviewsDue));
        advancedCoursesLabel.setText(String.valueOf(totalAdvancedCourses));
        multiTalentedLabel.setText(String.valueOf(multiTalented));
    }

    private void updateCharts() {
        updateGiftedAreasChart();
        updateProgramTypeChart();
        updateStatusChart();
    }

    private void updateGiftedAreasChart() {
        Map<GiftedStudent.GiftedArea, Long> areaCounts = new HashMap<>();

        for (GiftedStudent gs : rawGiftedStudents) {
            if (gs.getPrimaryGiftedArea() != null) {
                areaCounts.put(gs.getPrimaryGiftedArea(),
                        areaCounts.getOrDefault(gs.getPrimaryGiftedArea(), 0L) + 1);
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        areaCounts.forEach((area, count) ->
                pieChartData.add(new PieChart.Data(area.getDisplayName() + " (" + count + ")", count)));

        giftedAreasChart.setData(pieChartData);
    }

    private void updateProgramTypeChart() {
        Map<GiftedStudent.ProgramType, Long> programCounts = rawGiftedStudents.stream()
                .filter(gs -> gs.getProgramType() != null)
                .collect(Collectors.groupingBy(GiftedStudent::getProgramType, Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        programCounts.forEach((program, count) ->
                pieChartData.add(new PieChart.Data(program.getDisplayName() + " (" + count + ")", count)));

        programTypeChart.setData(pieChartData);
    }

    private void updateStatusChart() {
        Map<GiftedStudent.GiftedStatus, Long> statusCounts = rawGiftedStudents.stream()
                .collect(Collectors.groupingBy(GiftedStudent::getGiftedStatus, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Students");

        for (GiftedStudent.GiftedStatus status : GiftedStudent.GiftedStatus.values()) {
            long count = statusCounts.getOrDefault(status, 0L);
            if (count > 0) {
                series.getData().add(new XYChart.Data<>(status.getDisplayName(), count));
            }
        }

        statusChart.getData().clear();
        statusChart.getData().add(series);
    }

    private void updateAlertLists() {
        // Annual Reviews Due
        List<String> reviewsDueList = rawGiftedStudents.stream()
                .filter(GiftedStudent::needsAnnualReview)
                .map(gs -> {
                    Student s = gs.getStudent();
                    int daysUntil = gs.getDaysUntilAnnualReview();
                    String daysText = daysUntil < 0 ? "OVERDUE" :
                            daysUntil == 0 ? "DUE TODAY" :
                            "Due in " + daysUntil + " days";
                    return s.getLastName() + ", " + s.getFirstName() +
                           " (Grade " + s.getGradeLevel() + ") - " + daysText;
                })
                .collect(Collectors.toList());
        annualReviewsDueListView.setItems(FXCollections.observableArrayList(reviewsDueList));

        // Parent Consent Needed
        List<String> consentNeededList = rawGiftedStudents.stream()
                .filter(GiftedStudent::needsParentConsent)
                .filter(gs -> gs.getGiftedStatus() == GiftedStudent.GiftedStatus.ELIGIBLE ||
                             gs.getGiftedStatus() == GiftedStudent.GiftedStatus.ACTIVE)
                .map(gs -> {
                    Student s = gs.getStudent();
                    return s.getLastName() + ", " + s.getFirstName() +
                           " (Grade " + s.getGradeLevel() + ") - " +
                           gs.getGiftedStatus().getDisplayName();
                })
                .collect(Collectors.toList());
        parentConsentNeededListView.setItems(FXCollections.observableArrayList(consentNeededList));

        // Underperforming Students
        List<String> underperformingList = rawGiftedStudents.stream()
                .filter(GiftedStudent::isActive)
                .filter(GiftedStudent::isUnderperforming)
                .map(gs -> {
                    Student s = gs.getStudent();
                    String gpaText = gs.getCurrentGpa() != null ?
                            String.format("GPA: %.2f", gs.getCurrentGpa()) :
                            "Not meeting expectations";
                    return s.getLastName() + ", " + s.getFirstName() +
                           " (Grade " + s.getGradeLevel() + ") - " + gpaText;
                })
                .collect(Collectors.toList());
        underperformingListView.setItems(FXCollections.observableArrayList(underperformingList));

        // Progress Reviews Overdue
        List<String> progressOverdueList = rawGiftedStudents.stream()
                .filter(GiftedStudent::isActive)
                .filter(GiftedStudent::needsProgressReview)
                .map(gs -> {
                    Student s = gs.getStudent();
                    int daysSince = gs.getDaysSinceProgressReview();
                    String daysText = daysSince < 0 ? "Never reviewed" :
                            "Last reviewed " + daysSince + " days ago";
                    return s.getLastName() + ", " + s.getFirstName() +
                           " (Grade " + s.getGradeLevel() + ") - " + daysText;
                })
                .collect(Collectors.toList());
        progressReviewsOverdueListView.setItems(FXCollections.observableArrayList(progressOverdueList));
    }

    @FXML
    private void handleApplyFilters() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        statusFilterComboBox.setValue(null);
        giftedAreaFilterComboBox.setValue(null);
        programTypeFilterComboBox.setValue(null);
        gradeFilterComboBox.setValue(null);
        searchField.clear();
        applyFilters();
    }

    private void applyFilters() {
        List<GiftedStudent> filtered = new ArrayList<>(rawGiftedStudents);

        // Status filter
        if (statusFilterComboBox.getValue() != null) {
            filtered = filtered.stream()
                    .filter(gs -> gs.getGiftedStatus() == statusFilterComboBox.getValue())
                    .collect(Collectors.toList());
        }

        // Gifted Area filter
        if (giftedAreaFilterComboBox.getValue() != null) {
            filtered = filtered.stream()
                    .filter(gs -> gs.getPrimaryGiftedArea() == giftedAreaFilterComboBox.getValue() ||
                                 (gs.getGiftedAreas() != null &&
                                  gs.getGiftedAreas().contains(giftedAreaFilterComboBox.getValue())))
                    .collect(Collectors.toList());
        }

        // Program Type filter
        if (programTypeFilterComboBox.getValue() != null) {
            filtered = filtered.stream()
                    .filter(gs -> gs.getProgramType() == programTypeFilterComboBox.getValue())
                    .collect(Collectors.toList());
        }

        // Grade filter
        if (gradeFilterComboBox.getValue() != null) {
            String grade = gradeFilterComboBox.getValue();
            filtered = filtered.stream()
                    .filter(gs -> gs.getStudent() != null &&
                                 gs.getStudent().getGradeLevel() != null &&
                                 gs.getStudent().getGradeLevel().toString().equals(grade))
                    .collect(Collectors.toList());
        }

        // Search filter
        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String searchLower = searchText.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(gs -> {
                        Student s = gs.getStudent();
                        if (s == null) return false;
                        return (s.getFirstName() != null && s.getFirstName().toLowerCase().contains(searchLower)) ||
                               (s.getLastName() != null && s.getLastName().toLowerCase().contains(searchLower)) ||
                               (s.getStudentId() != null && s.getStudentId().toLowerCase().contains(searchLower));
                    })
                    .collect(Collectors.toList());
        }

        filteredGiftedStudents.setAll(filtered);
    }

    @FXML
    private void handleAddGiftedStudent() {
        // TODO: Open GiftedStudentProfile in add mode
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Gifted Student");
        alert.setHeaderText("Add New Gifted Student");
        alert.setContentText("This will open the Gifted Student Profile form.");
        alert.showAndWait();
    }

    @FXML
    private void handleExportReport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Report");
        alert.setHeaderText("Export Gifted Program Report");
        alert.setContentText("Exporting " + rawGiftedStudents.size() + " gifted student records to PDF...");
        alert.showAndWait();
    }

    @FXML
    private void handleRefresh() {
        loadData();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Refresh");
        alert.setHeaderText("Data Refreshed");
        alert.setContentText("Dashboard data has been refreshed successfully.");
        alert.showAndWait();
    }

    private void handleViewProfile(GiftedStudent giftedStudent) {
        // TODO: Open GiftedStudentProfile in view mode
        Student s = giftedStudent.getStudent();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("View Profile");
        alert.setHeaderText("View Gifted Student Profile");
        alert.setContentText("Opening profile for: " + s.getLastName() + ", " + s.getFirstName());
        alert.showAndWait();
    }
}
