package com.heronix.ui.controller;

import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.service.TruancyInterventionService;
import com.heronix.service.TruancyInterventionService.InterventionType;
import com.heronix.service.TruancyInterventionService.TruancyCase;
import com.heronix.service.TruancyInterventionService.TruancySeverity;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Truancy Intervention Management Controller
 *
 * Manages truancy cases, interventions, and court referrals.
 * Provides interfaces for:
 * - Active truancy case monitoring
 * - Intervention tracking
 * - Case management and resolution
 * - Court referral documentation
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Enhancement
 */
@Slf4j
@Component
public class TruancyInterventionController {

    @Autowired
    private TruancyInterventionService truancyService;

    @Autowired
    private StudentRepository studentRepository;

    // ========================================================================
    // FXML Components
    // ========================================================================

    @FXML private VBox rootContainer;
    @FXML private TabPane mainTabPane;

    // Active Cases Tab
    @FXML private Tab activeCasesTab;
    @FXML private ComboBox<TruancySeverity> severityFilterComboBox;
    @FXML private TableView<CaseRow> activeCasesTable;
    @FXML private TableColumn<CaseRow, String> caseIdCol;
    @FXML private TableColumn<CaseRow, String> studentNameCol;
    @FXML private TableColumn<CaseRow, String> gradeCol;
    @FXML private TableColumn<CaseRow, Integer> absencesCol;
    @FXML private TableColumn<CaseRow, String> severityCol;
    @FXML private TableColumn<CaseRow, String> statusCol;
    @FXML private TableColumn<CaseRow, String> openedDateCol;
    @FXML private Label totalCasesLabel;
    @FXML private Label activeCasesLabel;
    @FXML private Label courtReferralsLabel;

    // New Case Tab
    @FXML private Tab newCaseTab;
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private Spinner<Integer> absencesSpinner;
    @FXML private TextArea reasonArea;
    @FXML private TextArea newCaseResultArea;

    // Intervention Tab
    @FXML private Tab interventionTab;
    @FXML private TextField caseIdField;
    @FXML private ComboBox<InterventionType> interventionTypeComboBox;
    @FXML private TextArea interventionDescriptionArea;
    @FXML private TextField assignedToField;
    @FXML private TextArea interventionResultArea;

    // Close Case Tab
    @FXML private Tab closeCaseTab;
    @FXML private TextField closeCaseIdField;
    @FXML private ComboBox<String> resolutionComboBox;
    @FXML private TextField closedByField;
    @FXML private TextArea resolutionNotesArea;
    @FXML private TextArea closeCaseResultArea;

    // Observable Lists
    private final ObservableList<CaseRow> activeCasesData = FXCollections.observableArrayList();

    // ========================================================================
    // Initialization
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing TruancyInterventionController");

        setupActiveCasesTab();
        setupNewCaseTab();
        setupInterventionTab();
        setupCloseCaseTab();

        loadStudents();
        loadActiveCases();
    }

    private void setupActiveCasesTab() {
        // Setup severity filter
        if (severityFilterComboBox != null) {
            severityFilterComboBox.setItems(FXCollections.observableArrayList(TruancySeverity.values()));
            severityFilterComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(TruancySeverity severity) {
                    return severity != null ? formatSeverity(severity) : "All Severities";
                }

                @Override
                public TruancySeverity fromString(String string) {
                    return null;
                }
            });
        }

        // Setup active cases table
        if (caseIdCol != null) {
            caseIdCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCaseId()));
            studentNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
            gradeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGrade()));
            absencesCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getAbsences()));
            severityCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSeverity()));
            statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
            openedDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOpenedDate()));

            // Style severity column
            severityCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (item.contains("CRITICAL")) {
                            setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                        } else if (item.contains("MODERATE")) {
                            setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                        } else if (item.contains("LOW")) {
                            setStyle("-fx-text-fill: #FFC107;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            });
        }

        if (activeCasesTable != null) {
            activeCasesTable.setItems(activeCasesData);
        }
    }

    private void setupNewCaseTab() {
        // Setup student combo box
        if (studentComboBox != null) {
            studentComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Student student) {
                    if (student == null) return "";
                    return String.format("%s - %s %s",
                        student.getStudentId(),
                        student.getFirstName(),
                        student.getLastName());
                }

                @Override
                public Student fromString(String string) {
                    return null;
                }
            });
        }

        // Setup absences spinner
        if (absencesSpinner != null) {
            SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 50, 10);
            absencesSpinner.setValueFactory(valueFactory);
        }
    }

    private void setupInterventionTab() {
        // Setup intervention type combo box
        if (interventionTypeComboBox != null) {
            interventionTypeComboBox.setItems(FXCollections.observableArrayList(InterventionType.values()));
            interventionTypeComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(InterventionType type) {
                    return type != null ? formatInterventionType(type) : "";
                }

                @Override
                public InterventionType fromString(String string) {
                    return null;
                }
            });
        }
    }

    private void setupCloseCaseTab() {
        // Setup resolution combo box
        if (resolutionComboBox != null) {
            resolutionComboBox.setItems(FXCollections.observableArrayList(
                "Improved Attendance",
                "Completed Intervention Plan",
                "Family Relocation",
                "Transferred Schools",
                "Court-Ordered Compliance",
                "Other"
            ));
        }
    }

    // ========================================================================
    // Data Loading
    // ========================================================================

    private void loadStudents() {
        try {
            List<Student> students = studentRepository.findAllActive()
                .stream()
                .sorted((s1, s2) -> {
                    String name1 = s1.getLastName() + " " + s1.getFirstName();
                    String name2 = s2.getLastName() + " " + s2.getFirstName();
                    return name1.compareTo(name2);
                })
                .collect(Collectors.toList());

            if (studentComboBox != null) {
                studentComboBox.setItems(FXCollections.observableArrayList(students));
            }
        } catch (Exception e) {
            log.error("Error loading students", e);
            showError("Error loading students: " + e.getMessage());
        }
    }

    @FXML
    private void loadActiveCases() {
        try {
            TruancySeverity selectedSeverity = severityFilterComboBox != null ?
                severityFilterComboBox.getValue() : null;

            List<TruancyCase> cases;
            if (selectedSeverity != null) {
                cases = truancyService.getCasesBySeverity(selectedSeverity);
            } else {
                cases = truancyService.getActiveCases();
            }

            activeCasesData.clear();
            int courtReferrals = 0;

            for (TruancyCase truancyCase : cases) {
                String openedDate = truancyCase.getOpenedDate() != null ?
                    truancyCase.getOpenedDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "N/A";

                activeCasesData.add(new CaseRow(
                    truancyCase.getCaseId(),
                    truancyCase.getStudentName(),
                    truancyCase.getGradeLevel() != null ? String.valueOf(truancyCase.getGradeLevel()) : "N/A",
                    truancyCase.getUnexcusedAbsences(),
                    formatSeverity(truancyCase.getSeverity()),
                    truancyCase.getStatus(),
                    openedDate
                ));

                if ("COURT_REFERRAL".equals(truancyCase.getStatus())) {
                    courtReferrals++;
                }
            }

            // Update summary labels
            if (totalCasesLabel != null) {
                totalCasesLabel.setText(String.valueOf(cases.size()));
            }
            if (activeCasesLabel != null) {
                int active = (int) cases.stream().filter(c -> "OPEN".equals(c.getStatus())).count();
                activeCasesLabel.setText(String.valueOf(active));
            }
            if (courtReferralsLabel != null) {
                courtReferralsLabel.setText(String.valueOf(courtReferrals));
            }

            log.info("Loaded {} truancy cases", cases.size());

        } catch (Exception e) {
            log.error("Error loading truancy cases", e);
            showError("Error loading cases: " + e.getMessage());
        }
    }

    // ========================================================================
    // New Case Handlers
    // ========================================================================

    @FXML
    private void handleOpenCase() {
        try {
            Student student = studentComboBox.getValue();
            Integer absences = absencesSpinner.getValue();
            String reason = reasonArea.getText();

            if (student == null) {
                showWarning("Please select a student");
                return;
            }
            if (reason == null || reason.trim().isEmpty()) {
                showWarning("Please enter a reason for the truancy case");
                return;
            }

            // Open truancy case
            TruancyCase truancyCase = truancyService.openTruancyCase(
                student.getId(),
                absences,
                reason
            );

            // Display result
            StringBuilder output = new StringBuilder();
            output.append("=== TRUANCY CASE OPENED ===\n\n");
            output.append("Case ID: ").append(truancyCase.getCaseId()).append("\n");
            output.append("Student: ").append(truancyCase.getStudentName()).append("\n");
            output.append("Grade: ").append(truancyCase.getGradeLevel()).append("\n");
            output.append("Unexcused Absences: ").append(truancyCase.getUnexcusedAbsences()).append("\n");
            output.append("Severity: ").append(formatSeverity(truancyCase.getSeverity())).append("\n");
            output.append("Opened Date: ").append(truancyCase.getOpenedDate()).append("\n");
            output.append("Status: ").append(truancyCase.getStatus()).append("\n");
            output.append("\nReason:\n").append(reason).append("\n");

            newCaseResultArea.setText(output.toString());

            // Clear form
            studentComboBox.setValue(null);
            reasonArea.clear();

            // Refresh active cases
            loadActiveCases();

            showSuccess("Truancy case opened successfully");

        } catch (Exception e) {
            log.error("Error opening truancy case", e);
            showError("Error opening case: " + e.getMessage());
        }
    }

    // ========================================================================
    // Intervention Handlers
    // ========================================================================

    @FXML
    private void handleAddIntervention() {
        try {
            String caseId = caseIdField.getText();
            InterventionType interventionType = interventionTypeComboBox.getValue();
            String description = interventionDescriptionArea.getText();
            String assignedTo = assignedToField.getText();

            if (caseId == null || caseId.trim().isEmpty()) {
                showWarning("Please enter a case ID");
                return;
            }
            if (interventionType == null) {
                showWarning("Please select an intervention type");
                return;
            }
            if (description == null || description.trim().isEmpty()) {
                showWarning("Please enter intervention description");
                return;
            }
            if (assignedTo == null || assignedTo.trim().isEmpty()) {
                showWarning("Please enter who the intervention is assigned to");
                return;
            }

            // Add intervention
            TruancyCase updatedCase = truancyService.addIntervention(
                caseId,
                interventionType,
                description,
                assignedTo
            );

            // Display result
            StringBuilder output = new StringBuilder();
            output.append("=== INTERVENTION ADDED ===\n\n");
            output.append("Case ID: ").append(updatedCase.getCaseId()).append("\n");
            output.append("Student: ").append(updatedCase.getStudentName()).append("\n");
            output.append("Intervention Type: ").append(formatInterventionType(interventionType)).append("\n");
            output.append("Assigned To: ").append(assignedTo).append("\n");
            output.append("\nDescription:\n").append(description).append("\n");
            output.append("\nTotal Interventions: ").append(updatedCase.getInterventions().size()).append("\n");

            interventionResultArea.setText(output.toString());

            // Clear form
            caseIdField.clear();
            interventionTypeComboBox.setValue(null);
            interventionDescriptionArea.clear();
            assignedToField.clear();

            showSuccess("Intervention added successfully");

        } catch (Exception e) {
            log.error("Error adding intervention", e);
            showError("Error adding intervention: " + e.getMessage());
        }
    }

    // ========================================================================
    // Close Case Handlers
    // ========================================================================

    @FXML
    private void handleCloseCase() {
        try {
            String caseId = closeCaseIdField.getText();
            String resolution = resolutionComboBox.getValue();
            String closedBy = closedByField.getText();
            String notes = resolutionNotesArea.getText();

            if (caseId == null || caseId.trim().isEmpty()) {
                showWarning("Please enter a case ID");
                return;
            }
            if (resolution == null) {
                showWarning("Please select a resolution");
                return;
            }
            if (closedBy == null || closedBy.trim().isEmpty()) {
                showWarning("Please enter who is closing the case");
                return;
            }

            String fullResolution = resolution;
            if (notes != null && !notes.trim().isEmpty()) {
                fullResolution += "\n\nNotes: " + notes;
            }

            // Close case
            TruancyCase closedCase = truancyService.closeTruancyCase(
                caseId,
                fullResolution,
                closedBy
            );

            // Display result
            StringBuilder output = new StringBuilder();
            output.append("=== CASE CLOSED ===\n\n");
            output.append("Case ID: ").append(closedCase.getCaseId()).append("\n");
            output.append("Student: ").append(closedCase.getStudentName()).append("\n");
            output.append("Closed Date: ").append(closedCase.getClosedDate()).append("\n");
            output.append("Closed By: ").append(closedBy).append("\n");
            output.append("Resolution: ").append(resolution).append("\n");
            output.append("\nCase Summary:\n");
            output.append("Total Interventions: ").append(closedCase.getInterventions().size()).append("\n");
            output.append("Case Duration: ");
            if (closedCase.getOpenedDate() != null && closedCase.getClosedDate() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                    closedCase.getOpenedDate(), closedCase.getClosedDate());
                output.append(days).append(" days\n");
            } else {
                output.append("N/A\n");
            }

            closeCaseResultArea.setText(output.toString());

            // Clear form
            closeCaseIdField.clear();
            resolutionComboBox.setValue(null);
            closedByField.clear();
            resolutionNotesArea.clear();

            // Refresh active cases
            loadActiveCases();

            showSuccess("Case closed successfully");

        } catch (Exception e) {
            log.error("Error closing case", e);
            showError("Error closing case: " + e.getMessage());
        }
    }

    // ========================================================================
    // Action Handlers
    // ========================================================================

    @FXML
    private void handleRefresh() {
        loadActiveCases();
        showSuccess("Cases refreshed");
    }

    @FXML
    private void handleFilterBySeverity() {
        loadActiveCases();
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private String formatSeverity(TruancySeverity severity) {
        if (severity == null) return "Unknown";
        return switch (severity) {
            case MILD -> "⚠️ Mild";
            case MODERATE -> "⚠️⚠️ Moderate";
            case SEVERE -> "⚠️⚠️⚠️ Severe";
            case CRITICAL -> "🚨 CRITICAL";
        };
    }

    private String formatInterventionType(InterventionType type) {
        if (type == null) return "";
        return switch (type) {
            case PARENT_CONFERENCE -> "👨‍👩‍👧 Parent Conference";
            case COUNSELING -> "💬 Counseling";
            case ATTENDANCE_CONTRACT -> "📝 Attendance Contract";
            case IMPROVEMENT_PLAN -> "📋 Improvement Plan";
            case HOME_VISIT -> "🏠 Home Visit";
            case TRANSPORTATION_ASSISTANCE -> "🚌 Transportation Assistance";
            case MENTOR_PROGRAM -> "🤝 Mentor Program";
            case INCENTIVE_PROGRAM -> "🌟 Incentive Program";
            case SOCIAL_SERVICES_REFERRAL -> "🏥 Social Services Referral";
            case MEDICAL_REFERRAL -> "⚕️ Medical Referral";
            case TRUANCY_COURT_DIVERSION -> "⚖️ Truancy Court Diversion";
            case COMMUNITY_SERVICE -> "🤲 Community Service";
            case OTHER -> "📍 Other";
        };
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ========================================================================
    // Data Classes
    // ========================================================================

    @Data
    public static class CaseRow {
        private final String caseId;
        private final String studentName;
        private final String grade;
        private final Integer absences;
        private final String severity;
        private final String status;
        private final String openedDate;
    }
}
