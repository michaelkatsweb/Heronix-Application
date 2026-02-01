package com.heronix.ui.controller;

import com.heronix.model.domain.StudentGrade;
import com.heronix.repository.StudentGradeRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StudentAssessmentDashboardController {

    @Autowired
    private StudentGradeRepository studentGradeRepository;

    @FXML private BarChart<String, Number> assessmentChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private TableView<StudentGrade> assessmentTableView;
    @FXML private TableColumn<StudentGrade, String> colStudent;
    @FXML private TableColumn<StudentGrade, String> colCourse;
    @FXML private TableColumn<StudentGrade, String> colGrade;
    @FXML private TableColumn<StudentGrade, String> colScore;
    @FXML private TableColumn<StudentGrade, String> colType;
    @FXML private TableColumn<StudentGrade, String> colTerm;
    @FXML private TableColumn<StudentGrade, String> colDate;
    @FXML private TableColumn<StudentGrade, String> colTeacher;
    @FXML private Label summaryLabel;
    @FXML private Label totalGradesLabel;
    @FXML private Label avgGpaLabel;
    @FXML private Label honorRollLabel;
    @FXML private Label atRiskLabel;
    @FXML private Label improvingLabel;
    @FXML private Label decliningLabel;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        log.info("Initializing StudentAssessmentDashboardController");

        // Table columns
        colStudent.setCellValueFactory(cd -> {
            StudentGrade g = cd.getValue();
            String name = g.getStudent() != null ? g.getStudent().getFullName() : "Unknown";
            return new SimpleStringProperty(name);
        });
        colCourse.setCellValueFactory(cd -> {
            StudentGrade g = cd.getValue();
            String name = g.getCourse() != null ? g.getCourse().getCourseName() : "Unknown";
            return new SimpleStringProperty(name);
        });
        colGrade.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getLetterGrade() != null ? cd.getValue().getLetterGrade() : ""));
        colScore.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getNumericalGrade() != null
                        ? String.format("%.0f", cd.getValue().getNumericalGrade()) : ""));
        colType.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getGradeType() != null ? cd.getValue().getGradeType() : ""));
        colTerm.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getTerm() != null ? cd.getValue().getTerm() : ""));
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getGradeDate() != null ? cd.getValue().getGradeDate().toString() : ""));
        colTeacher.setCellValueFactory(cd -> {
            StudentGrade g = cd.getValue();
            String name = g.getTeacher() != null ? g.getTeacher().getName() : "";
            return new SimpleStringProperty(name);
        });

        loadDashboardData();
    }

    @FXML
    private void handleRefresh() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        statusLabel.setText("Loading assessment data...");
        new Thread(() -> {
            try {
                // Recent grades (last 30 days)
                LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
                List<StudentGrade> recentGrades = studentGradeRepository.findGradesEnteredBetween(
                        thirtyDaysAgo, LocalDate.now());

                // Stats
                Long totalFinal = studentGradeRepository.countTotalFinalGrades();
                Double avgGpa = studentGradeRepository.getAverageGPAAcrossAllStudents();
                long honorCount = studentGradeRepository.findHonorsEligibleStudents(3.5).size();
                long atRiskCount = studentGradeRepository.findStudentsWithGPAInRange(0.0, 1.99).size();
                long improvingCount = studentGradeRepository.findStudentsWithImprovedGPA().size();
                long decliningCount = studentGradeRepository.findStudentsWithDecliningGPA().size();

                // Grade distribution from all final grades
                List<StudentGrade> allFinal = studentGradeRepository.findAll().stream()
                        .filter(g -> Boolean.TRUE.equals(g.getIsFinal()))
                        .collect(Collectors.toList());

                Map<String, Long> distribution = new LinkedHashMap<>();
                for (String grade : Arrays.asList("A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F")) {
                    distribution.put(grade, allFinal.stream()
                            .filter(g -> grade.equals(g.getLetterGrade()))
                            .count());
                }

                Platform.runLater(() -> {
                    // Chart
                    assessmentChart.getData().clear();
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Students");
                    distribution.forEach((grade, count) ->
                            series.getData().add(new XYChart.Data<>(grade, count)));
                    assessmentChart.getData().add(series);

                    // Stats labels
                    totalGradesLabel.setText("Total Final Grades: " + (totalFinal != null ? totalFinal : 0));
                    avgGpaLabel.setText(String.format("School Avg GPA: %.2f", avgGpa != null ? avgGpa : 0.0));
                    honorRollLabel.setText("Honor Roll (3.5+): " + honorCount);
                    atRiskLabel.setText("At Risk (< 2.0): " + atRiskCount);
                    improvingLabel.setText("GPA Improving: " + improvingCount);
                    decliningLabel.setText("GPA Declining: " + decliningCount);

                    // Recent grades table (limit to 200)
                    List<StudentGrade> display = recentGrades.size() > 200
                            ? recentGrades.subList(0, 200) : recentGrades;
                    assessmentTableView.setItems(FXCollections.observableArrayList(display));

                    summaryLabel.setText(recentGrades.size() + " grades in last 30 days");
                    statusLabel.setText("Loaded " + recentGrades.size() + " recent grades");
                });
            } catch (Exception e) {
                log.error("Failed to load assessment dashboard", e);
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            }
        }).start();
    }
}
