package com.heronix.ui.controller;

import com.heronix.repository.StudentRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class EnrollmentReportsFormController {

    @Autowired
    private StudentRepository studentRepository;

    @FXML private Label totalEnrollmentLabel;
    @FXML private BarChart<String, Number> gradeLevelChart;
    @FXML private CategoryAxis glXAxis;
    @FXML private NumberAxis glYAxis;
    @FXML private TableView<String[]> gradeLevelTable;
    @FXML private TableColumn<String[], String> colGradeLevel;
    @FXML private TableColumn<String[], String> colGradeCount;
    @FXML private TableColumn<String[], String> colGradeAvgGpa;
    @FXML private TableView<String[]> genderTable;
    @FXML private TableColumn<String[], String> colGender;
    @FXML private TableColumn<String[], String> colGenderCount;
    @FXML private Label iepLabel;
    @FXML private Label plan504Label;
    @FXML private Label giftedLabel;
    @FXML private Label ellLabel;
    @FXML private Label activeLabel;
    @FXML private Label inactiveLabel;
    @FXML private Label graduatedLabel;
    @FXML private Label statusLabel;

    private List<String[]> gradeLevelData = new ArrayList<>();

    @FXML
    public void initialize() {
        log.info("Initializing EnrollmentReportsFormController");

        colGradeLevel.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[0]));
        colGradeCount.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[1]));
        colGradeAvgGpa.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[2]));
        colGender.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[0]));
        colGenderCount.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[1]));

        loadReportData();
    }

    @FXML
    private void handleRefresh() {
        loadReportData();
    }

    private void loadReportData() {
        statusLabel.setText("Loading enrollment data...");
        new Thread(() -> {
            try {
                // Enrollment by grade level
                List<Object[]> gradeCounts = studentRepository.countByGradeLevelForAnalytics(null);
                List<Object[]> gradeGpas = studentRepository.getAverageGPAByGrade(null);

                // Special services
                Long iepCount = studentRepository.countIEPStudents(null);
                Long plan504Count = studentRepository.count504Students(null);
                Long giftedCount = studentRepository.countGiftedStudents(null);
                Long ellCount = studentRepository.countELLStudents(null);

                // Gender
                List<Object[]> genderCounts = studentRepository.countByGender(null);

                // Status counts
                Long totalActive = studentRepository.countActiveStudents(null);
                long totalAll = studentRepository.findAllNonDeleted().size();
                long graduated = studentRepository.findByGraduatedTrue().size();
                long inactive = totalAll - totalActive - graduated;

                // Build grade level rows merging counts and GPAs
                List<String[]> glRows = new ArrayList<>();
                for (Object[] row : gradeCounts) {
                    String level = row[0] != null ? row[0].toString() : "Unknown";
                    String count = row[1] != null ? row[1].toString() : "0";
                    // Find matching GPA
                    String avgGpa = "--";
                    for (Object[] gpaRow : gradeGpas) {
                        if (level.equals(gpaRow[0] != null ? gpaRow[0].toString() : "")) {
                            avgGpa = gpaRow[1] != null ? String.format("%.2f", ((Number) gpaRow[1]).doubleValue()) : "--";
                            break;
                        }
                    }
                    glRows.add(new String[]{level, count, avgGpa});
                }

                // Gender rows
                List<String[]> genderRows = new ArrayList<>();
                for (Object[] row : genderCounts) {
                    genderRows.add(new String[]{
                            row[0] != null ? row[0].toString() : "Unknown",
                            row[1] != null ? row[1].toString() : "0"
                    });
                }

                Platform.runLater(() -> {
                    gradeLevelData = glRows;

                    totalEnrollmentLabel.setText("Total Enrolled: " + totalActive);

                    // Chart
                    gradeLevelChart.getData().clear();
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Students");
                    for (String[] row : glRows) {
                        series.getData().add(new XYChart.Data<>(row[0], Long.parseLong(row[1])));
                    }
                    gradeLevelChart.getData().add(series);

                    // Grade level table
                    gradeLevelTable.setItems(FXCollections.observableArrayList(glRows));

                    // Gender table
                    genderTable.setItems(FXCollections.observableArrayList(genderRows));

                    // Special services
                    iepLabel.setText("IEP Students: " + (iepCount != null ? iepCount : 0));
                    plan504Label.setText("504 Plan: " + (plan504Count != null ? plan504Count : 0));
                    giftedLabel.setText("Gifted: " + (giftedCount != null ? giftedCount : 0));
                    ellLabel.setText("English Learners: " + (ellCount != null ? ellCount : 0));

                    // Status
                    activeLabel.setText("Active: " + totalActive);
                    inactiveLabel.setText("Inactive: " + Math.max(0, inactive));
                    graduatedLabel.setText("Graduated: " + graduated);

                    statusLabel.setText("Report loaded");
                });
            } catch (Exception e) {
                log.error("Failed to load enrollment report", e);
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Enrollment Report");
        fileChooser.setInitialFileName("enrollment_report.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Stage stage = (Stage) gradeLevelChart.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8))) {
                pw.write('\ufeff');
                pw.println("Grade Level,Student Count,Average GPA");
                for (String[] row : gradeLevelData) {
                    pw.printf("%s,%s,%s%n", row[0], row[1], row[2]);
                }
                statusLabel.setText("Exported to " + file.getName());
            } catch (Exception e) {
                log.error("Failed to export CSV", e);
                statusLabel.setText("Export failed: " + e.getMessage());
            }
        }
    }
}
