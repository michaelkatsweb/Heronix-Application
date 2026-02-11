package com.heronix.ui.controller;

import com.heronix.model.domain.Teacher;
import com.heronix.model.domain.TeacherEvaluation;
import com.heronix.model.domain.TeacherEvaluation.*;
import com.heronix.repository.TeacherRepository;
import com.heronix.service.TeacherEvaluationService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeacherEvaluationManagementController {

    private final TeacherEvaluationService evaluationService;
    private final TeacherRepository teacherRepository;

    // Filters
    @FXML private ComboBox<String> schoolYearFilter;
    @FXML private ComboBox<String> evalStatusFilter;
    @FXML private ComboBox<String> evalTypeFilter;
    @FXML private ComboBox<String> ratingFilter;
    @FXML private ComboBox<String> evaluatorFilter;
    @FXML private TextField evalSearchField;

    // Summary
    @FXML private Label evalTotalLabel;
    @FXML private Label evalScheduledLabel;
    @FXML private Label evalInProgressLabel;
    @FXML private Label evalCompletedLabel;
    @FXML private Label evalOverdueLabel;
    @FXML private Label evalPendingSigLabel;
    @FXML private Label evalNeedingImpLabel;

    // Table
    @FXML private TableView<TeacherEvaluation> evalTable;
    @FXML private TableColumn<TeacherEvaluation, String> evalTeacherCol;
    @FXML private TableColumn<TeacherEvaluation, String> evalEvaluatorCol;
    @FXML private TableColumn<TeacherEvaluation, String> evalSchoolYearCol;
    @FXML private TableColumn<TeacherEvaluation, String> evalTypeCol;
    @FXML private TableColumn<TeacherEvaluation, String> evalDateCol;
    @FXML private TableColumn<TeacherEvaluation, String> evalStatusCol;
    @FXML private TableColumn<TeacherEvaluation, String> evalRatingCol;
    @FXML private TableColumn<TeacherEvaluation, Void> evalActionsCol;

    // Overview
    @FXML private Label overviewCompletedLabel;
    @FXML private Label overviewHighlyEffectiveLabel;
    @FXML private Label overviewNeedingImprovLabel;
    @FXML private Label overviewPendingLabel;
    @FXML private VBox ratingDistributionBox;

    private ObservableList<TeacherEvaluation> allData = FXCollections.observableArrayList();
    private ObservableList<TeacherEvaluation> filteredData = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private static final Set<EvaluationStatus> IN_PROGRESS_STATUSES = Set.of(
            EvaluationStatus.PRE_CONFERENCE_PENDING, EvaluationStatus.PRE_CONFERENCE_COMPLETE,
            EvaluationStatus.OBSERVATION_PENDING, EvaluationStatus.OBSERVATION_COMPLETE,
            EvaluationStatus.POST_CONFERENCE_PENDING, EvaluationStatus.POST_CONFERENCE_COMPLETE,
            EvaluationStatus.DRAFT, EvaluationStatus.PENDING_TEACHER_REVIEW, EvaluationStatus.PENDING_SIGNATURE
    );

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadData();
    }

    private void setupTable() {
        evalTeacherCol.setCellValueFactory(cell -> {
            Teacher t = cell.getValue().getTeacher();
            return new SimpleStringProperty(t != null ? t.getFullName() : "");
        });
        evalEvaluatorCol.setCellValueFactory(cell -> {
            Teacher ev = cell.getValue().getEvaluator();
            return new SimpleStringProperty(ev != null ? ev.getFullName() : "");
        });
        evalSchoolYearCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSchoolYear()));
        evalTypeCol.setCellValueFactory(cell -> {
            EvaluationType type = cell.getValue().getEvaluationType();
            return new SimpleStringProperty(type != null ? type.name().replace("_", " ") : "");
        });
        evalDateCol.setCellValueFactory(cell -> {
            LocalDate d = cell.getValue().getScheduledDate();
            return new SimpleStringProperty(d != null ? d.format(DATE_FMT) : "");
        });
        evalStatusCol.setCellValueFactory(cell -> {
            EvaluationStatus s = cell.getValue().getStatus();
            return new SimpleStringProperty(s != null ? s.name().replace("_", " ") : "");
        });
        evalStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.contains("COMPLETE") || item.equals("COMPLETED")) setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                else if (item.equals("SCHEDULED")) setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
                else if (item.contains("PENDING")) setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                else if (item.equals("DRAFT")) setStyle("-fx-text-fill: #8b5cf6; -fx-font-weight: bold;");
                else setStyle("-fx-text-fill: #64748b;");
            }
        });
        evalRatingCol.setCellValueFactory(cell -> {
            PerformanceRating r = cell.getValue().getOverallRating();
            return new SimpleStringProperty(r != null ? r.name().replace("_", " ") : "--");
        });
        evalRatingCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || "--".equals(item)) { setText(item); setStyle(""); return; }
                setText(item);
                if (item.contains("HIGHLY") || item.contains("DISTINGUISHED")) setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
                else if (item.contains("EFFECTIVE") || item.contains("PROFICIENT")) setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                else if (item.contains("DEVELOPING") || item.contains("BASIC")) setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                else if (item.contains("INEFFECTIVE") || item.contains("UNSATISFACTORY")) setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                else setStyle("");
            }
        });

        setupActionsColumn();
        evalTable.setItems(filteredData);
    }

    private void setupActionsColumn() {
        evalActionsCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                TeacherEvaluation eval = getTableRow().getItem();
                if (eval == null) { setGraphic(null); return; }

                HBox buttons = new HBox(3);
                buttons.setAlignment(Pos.CENTER);

                EvaluationStatus status = eval.getStatus();

                // Status-dependent workflow buttons
                if (status == EvaluationStatus.SCHEDULED) {
                    addBtn(buttons, "Pre-Conf.", "#8b5cf6", e -> handlePreConference(eval));
                } else if (status == EvaluationStatus.PRE_CONFERENCE_COMPLETE) {
                    addBtn(buttons, "Observe", "#06b6d4", e -> handleObservation(eval));
                } else if (status == EvaluationStatus.OBSERVATION_COMPLETE) {
                    addBtn(buttons, "Post-Conf.", "#8b5cf6", e -> handlePostConference(eval));
                } else if (status == EvaluationStatus.POST_CONFERENCE_COMPLETE) {
                    addBtn(buttons, "Score", "#f59e0b", e -> handleScoreEvaluation(eval));
                } else if (status == EvaluationStatus.DRAFT) {
                    addBtn(buttons, "Eval Sign", "#3b82f6", e -> handleEvaluatorSignature(eval));
                } else if (status == EvaluationStatus.PENDING_TEACHER_REVIEW) {
                    addBtn(buttons, "Tchr Sign", "#10b981", e -> handleTeacherSignature(eval));
                }

                // Improvement plan button if required and not created
                if (Boolean.TRUE.equals(eval.getImprovementPlanRequired()) && !Boolean.TRUE.equals(eval.getImprovementPlanCreated())) {
                    addBtn(buttons, "Improv. Plan", "#f97316", e -> handleImprovementPlan(eval));
                }

                // Always available: Edit and Delete
                addBtn(buttons, "Edit", "#3b82f6", e -> handleEdit(eval));
                addBtn(buttons, "Del", "#ef4444", e -> handleDelete(eval));

                setGraphic(buttons);
            }

            private void addBtn(HBox parent, String text, String color, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
                Button btn = new Button(text);
                btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 2 6; -fx-font-size: 10; -fx-background-radius: 4; -fx-cursor: hand;");
                btn.setOnAction(handler);
                parent.getChildren().add(btn);
            }
        });
    }

    private void setupFilters() {
        // School year
        ObservableList<String> years = FXCollections.observableArrayList("All");
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear + 1; y >= currentYear - 5; y--) {
            years.add((y - 1) + "-" + y);
        }
        schoolYearFilter.setItems(years);
        schoolYearFilter.setValue("All");

        // Status
        ObservableList<String> statuses = FXCollections.observableArrayList("All");
        Arrays.stream(EvaluationStatus.values()).forEach(s -> statuses.add(s.name().replace("_", " ")));
        evalStatusFilter.setItems(statuses);
        evalStatusFilter.setValue("All");

        // Type
        ObservableList<String> types = FXCollections.observableArrayList("All");
        Arrays.stream(EvaluationType.values()).forEach(t -> types.add(t.name().replace("_", " ")));
        evalTypeFilter.setItems(types);
        evalTypeFilter.setValue("All");

        // Rating
        ObservableList<String> ratings = FXCollections.observableArrayList("All");
        Arrays.stream(PerformanceRating.values()).forEach(r -> ratings.add(r.name().replace("_", " ")));
        ratingFilter.setItems(ratings);
        ratingFilter.setValue("All");

        // Evaluator - populated when data loads
        evaluatorFilter.setItems(FXCollections.observableArrayList("All"));
        evaluatorFilter.setValue("All");

        // Listeners
        schoolYearFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        evalStatusFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        evalTypeFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        ratingFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        evaluatorFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        evalSearchField.textProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void loadData() {
        new Thread(() -> {
            try {
                List<TeacherEvaluation> evals = evaluationService.getAllEvaluations();
                Platform.runLater(() -> {
                    allData.setAll(evals);
                    populateEvaluatorFilter();
                    applyFilters();
                    loadOverview();
                });
            } catch (Exception e) {
                log.error("Failed to load evaluations", e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load evaluations: " + e.getMessage()));
            }
        }).start();
    }

    private void populateEvaluatorFilter() {
        ObservableList<String> evaluators = FXCollections.observableArrayList("All");
        allData.stream()
                .filter(e -> e.getEvaluator() != null)
                .map(e -> e.getEvaluator().getFullName())
                .distinct()
                .sorted()
                .forEach(evaluators::add);
        String current = evaluatorFilter.getValue();
        evaluatorFilter.setItems(evaluators);
        evaluatorFilter.setValue(current != null && evaluators.contains(current) ? current : "All");
    }

    private void applyFilters() {
        String year = schoolYearFilter.getValue();
        String status = evalStatusFilter.getValue();
        String type = evalTypeFilter.getValue();
        String rating = ratingFilter.getValue();
        String evaluator = evaluatorFilter.getValue();
        String search = evalSearchField.getText() != null ? evalSearchField.getText().toLowerCase() : "";

        filteredData.setAll(allData.stream().filter(e -> {
            if (!"All".equals(year) && !year.equals(e.getSchoolYear())) return false;
            if (!"All".equals(status) && e.getStatus() != null && !e.getStatus().name().replace("_", " ").equals(status)) return false;
            if (!"All".equals(type) && e.getEvaluationType() != null && !e.getEvaluationType().name().replace("_", " ").equals(type)) return false;
            if (!"All".equals(rating) && e.getOverallRating() != null && !e.getOverallRating().name().replace("_", " ").equals(rating)) return false;
            if (!"All".equals(evaluator) && evaluator != null) {
                if (e.getEvaluator() == null || !e.getEvaluator().getFullName().equals(evaluator)) return false;
            }
            if (!search.isEmpty()) {
                String teacherName = e.getTeacher() != null ? e.getTeacher().getFullName().toLowerCase() : "";
                if (!teacherName.contains(search)) return false;
            }
            return true;
        }).collect(Collectors.toList()));

        updateSummary();
    }

    private void updateSummary() {
        evalTotalLabel.setText(String.valueOf(filteredData.size()));
        evalScheduledLabel.setText(String.valueOf(filteredData.stream().filter(e -> e.getStatus() == EvaluationStatus.SCHEDULED).count()));
        evalInProgressLabel.setText(String.valueOf(filteredData.stream().filter(e -> IN_PROGRESS_STATUSES.contains(e.getStatus())).count()));
        evalCompletedLabel.setText(String.valueOf(filteredData.stream().filter(e -> e.getStatus() == EvaluationStatus.COMPLETED).count()));
        evalOverdueLabel.setText(String.valueOf(filteredData.stream().filter(TeacherEvaluation::isOverdue).count()));
        evalPendingSigLabel.setText(String.valueOf(filteredData.stream().filter(TeacherEvaluation::needsSignature).count()));
        evalNeedingImpLabel.setText(String.valueOf(filteredData.stream().filter(TeacherEvaluation::requiresImprovement).count()));
    }

    private void loadOverview() {
        long completed = allData.stream().filter(e -> e.getStatus() == EvaluationStatus.COMPLETED).count();
        long highlyEffective = allData.stream().filter(e -> e.getOverallRating() != null &&
                (e.getOverallRating() == PerformanceRating.HIGHLY_EFFECTIVE || e.getOverallRating() == PerformanceRating.DISTINGUISHED)).count();
        long needingImprovement = allData.stream().filter(e -> e.getOverallRating() != null &&
                (e.getOverallRating() == PerformanceRating.DEVELOPING || e.getOverallRating() == PerformanceRating.INEFFECTIVE ||
                 e.getOverallRating() == PerformanceRating.BASIC || e.getOverallRating() == PerformanceRating.UNSATISFACTORY)).count();
        long pending = allData.stream().filter(e -> e.getStatus() != EvaluationStatus.COMPLETED && e.getStatus() != EvaluationStatus.REVISED).count();

        overviewCompletedLabel.setText(String.valueOf(completed));
        overviewHighlyEffectiveLabel.setText(String.valueOf(highlyEffective));
        overviewNeedingImprovLabel.setText(String.valueOf(needingImprovement));
        overviewPendingLabel.setText(String.valueOf(pending));

        // Rating distribution
        Map<PerformanceRating, Long> ratingDist = allData.stream()
                .filter(e -> e.getOverallRating() != null)
                .collect(Collectors.groupingBy(TeacherEvaluation::getOverallRating, Collectors.counting()));
        ratingDistributionBox.getChildren().clear();
        long totalRated = ratingDist.values().stream().mapToLong(Long::longValue).sum();
        ratingDist.forEach((r, count) -> {
            double pct = totalRated > 0 ? (count * 100.0 / totalRated) : 0;
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Label nameLabel = new Label(r.name().replace("_", " "));
            nameLabel.setPrefWidth(160);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
            ProgressBar bar = new ProgressBar(pct / 100.0);
            bar.setPrefWidth(300);
            bar.setPrefHeight(16);
            HBox.setHgrow(bar, Priority.ALWAYS);
            Label pctLabel = new Label(String.format("%.1f%% (%d)", pct, count));
            pctLabel.setStyle("-fx-text-fill: #64748b;");
            row.getChildren().addAll(nameLabel, bar, pctLabel);
            ratingDistributionBox.getChildren().add(row);
        });
        if (ratingDist.isEmpty()) {
            ratingDistributionBox.getChildren().add(new Label("No rated evaluations yet."));
        }
    }

    // ========================================================================
    // SCHEDULE / EDIT / DELETE
    // ========================================================================

    @FXML
    private void handleScheduleEvaluation() {
        showScheduleDialog(null);
    }

    private void handleEdit(TeacherEvaluation eval) {
        showScheduleDialog(eval);
    }

    private void handleDelete(TeacherEvaluation eval) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this evaluation?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    evaluationService.deleteEvaluation(eval.getId());
                    loadData();
                } catch (Exception e) {
                    log.error("Failed to delete evaluation", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete: " + e.getMessage());
                }
            }
        });
    }

    private void showScheduleDialog(TeacherEvaluation existing) {
        Dialog<TeacherEvaluation> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Schedule Evaluation" : "Edit Evaluation");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(550);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        List<Teacher> teachers;
        try { teachers = teacherRepository.findAll(); }
        catch (Exception e) { teachers = List.of(); }

        ComboBox<Teacher> teacherCombo = createTeacherCombo(teachers);
        ComboBox<Teacher> evaluatorCombo = createTeacherCombo(teachers);

        TextField schoolYearField = new TextField();
        int yr = LocalDate.now().getYear();
        schoolYearField.setPromptText((yr - 1) + "-" + yr);
        ComboBox<EvaluationType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(EvaluationType.values()));
        ComboBox<RubricType> rubricCombo = new ComboBox<>(FXCollections.observableArrayList(RubricType.values()));
        DatePicker datePicker = new DatePicker();
        TextField timeField = new TextField();
        timeField.setPromptText("HH:mm (e.g. 09:00)");
        TextField locationField = new TextField();
        locationField.setPromptText("Room or location");

        int row = 0;
        grid.add(new Label("Teacher:"), 0, row); grid.add(teacherCombo, 1, row++);
        grid.add(new Label("Evaluator:"), 0, row); grid.add(evaluatorCombo, 1, row++);
        grid.add(new Label("School Year:"), 0, row); grid.add(schoolYearField, 1, row++);
        grid.add(new Label("Type:"), 0, row); grid.add(typeCombo, 1, row++);
        grid.add(new Label("Rubric:"), 0, row); grid.add(rubricCombo, 1, row++);
        grid.add(new Label("Date:"), 0, row); grid.add(datePicker, 1, row++);
        grid.add(new Label("Time:"), 0, row); grid.add(timeField, 1, row++);
        grid.add(new Label("Location:"), 0, row); grid.add(locationField, 1, row++);

        if (existing != null) {
            teacherCombo.setValue(existing.getTeacher());
            evaluatorCombo.setValue(existing.getEvaluator());
            schoolYearField.setText(existing.getSchoolYear());
            typeCombo.setValue(existing.getEvaluationType());
            rubricCombo.setValue(existing.getRubricType());
            datePicker.setValue(existing.getScheduledDate());
            if (existing.getScheduledTime() != null) timeField.setText(existing.getScheduledTime().toString());
            locationField.setText(existing.getObservationLocation());
        }

        dialog.getDialogPane().setContent(new ScrollPane(grid));

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                TeacherEvaluation eval = existing != null ? existing : new TeacherEvaluation();
                eval.setTeacher(teacherCombo.getValue());
                eval.setEvaluator(evaluatorCombo.getValue());
                eval.setSchoolYear(schoolYearField.getText());
                eval.setEvaluationType(typeCombo.getValue());
                eval.setRubricType(rubricCombo.getValue());
                eval.setScheduledDate(datePicker.getValue());
                if (timeField.getText() != null && !timeField.getText().isBlank()) {
                    try { eval.setScheduledTime(LocalTime.parse(timeField.getText())); }
                    catch (Exception ignored) {}
                }
                eval.setObservationLocation(locationField.getText());
                if (existing == null) eval.setStatus(EvaluationStatus.SCHEDULED);
                return eval;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(eval -> {
            try {
                if (existing == null) evaluationService.createEvaluation(eval);
                else evaluationService.updateEvaluation(eval);
                loadData();
            } catch (Exception e) {
                log.error("Failed to save evaluation", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save: " + e.getMessage());
            }
        });
    }

    // ========================================================================
    // WORKFLOW DIALOGS
    // ========================================================================

    private void handlePreConference(TeacherEvaluation eval) {
        showNotesDialog("Pre-Conference Notes", "Record pre-conference discussion notes:", eval.getPreConferenceNotes())
                .ifPresent(notes -> {
                    try {
                        evaluationService.conductPreConference(eval.getId(), notes);
                        loadData();
                    } catch (Exception e) {
                        log.error("Failed to record pre-conference", e);
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed: " + e.getMessage());
                    }
                });
    }

    private void handleObservation(TeacherEvaluation eval) {
        showNotesDialog("Observation Notes", "Record classroom observation notes:", eval.getObservationNotes())
                .ifPresent(notes -> {
                    try {
                        evaluationService.conductObservation(eval.getId(), notes);
                        loadData();
                    } catch (Exception e) {
                        log.error("Failed to record observation", e);
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed: " + e.getMessage());
                    }
                });
    }

    private void handlePostConference(TeacherEvaluation eval) {
        showNotesDialog("Post-Conference Notes", "Record post-conference discussion notes:", eval.getPostConferenceNotes())
                .ifPresent(notes -> {
                    try {
                        evaluationService.conductPostConference(eval.getId(), notes);
                        loadData();
                    } catch (Exception e) {
                        log.error("Failed to record post-conference", e);
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed: " + e.getMessage());
                    }
                });
    }

    private void handleScoreEvaluation(TeacherEvaluation eval) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Score Evaluation");
        dialog.setHeaderText("Score evaluation for " + (eval.getTeacher() != null ? eval.getTeacher().getFullName() : "teacher"));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(600);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Domain scores
        Spinner<Double> d1Score = new Spinner<>(0.0, 4.0, eval.getDomain1Score() != null ? eval.getDomain1Score() : 0.0, 0.1);
        Spinner<Double> d2Score = new Spinner<>(0.0, 4.0, eval.getDomain2Score() != null ? eval.getDomain2Score() : 0.0, 0.1);
        Spinner<Double> d3Score = new Spinner<>(0.0, 4.0, eval.getDomain3Score() != null ? eval.getDomain3Score() : 0.0, 0.1);
        Spinner<Double> d4Score = new Spinner<>(0.0, 4.0, eval.getDomain4Score() != null ? eval.getDomain4Score() : 0.0, 0.1);
        d1Score.setEditable(true); d2Score.setEditable(true); d3Score.setEditable(true); d4Score.setEditable(true);

        ComboBox<PerformanceRating> d1Rating = new ComboBox<>(FXCollections.observableArrayList(PerformanceRating.values()));
        ComboBox<PerformanceRating> d2Rating = new ComboBox<>(FXCollections.observableArrayList(PerformanceRating.values()));
        ComboBox<PerformanceRating> d3Rating = new ComboBox<>(FXCollections.observableArrayList(PerformanceRating.values()));
        ComboBox<PerformanceRating> d4Rating = new ComboBox<>(FXCollections.observableArrayList(PerformanceRating.values()));

        TextArea d1Comments = new TextArea(); d1Comments.setPrefRowCount(2);
        TextArea d2Comments = new TextArea(); d2Comments.setPrefRowCount(2);
        TextArea d3Comments = new TextArea(); d3Comments.setPrefRowCount(2);
        TextArea d4Comments = new TextArea(); d4Comments.setPrefRowCount(2);

        Spinner<Double> overallScore = new Spinner<>(0.0, 4.0, eval.getOverallScore() != null ? eval.getOverallScore() : 0.0, 0.1);
        overallScore.setEditable(true);
        ComboBox<PerformanceRating> overallRating = new ComboBox<>(FXCollections.observableArrayList(PerformanceRating.values()));

        TextArea strengthsArea = new TextArea(eval.getStrengths()); strengthsArea.setPrefRowCount(2);
        TextArea growthArea = new TextArea(eval.getAreasForGrowth()); growthArea.setPrefRowCount(2);
        TextArea recommendationsArea = new TextArea(eval.getRecommendations()); recommendationsArea.setPrefRowCount(2);
        TextArea goalsArea = new TextArea(eval.getGoals() != null ? String.join("\n", eval.getGoals()) : "");
        goalsArea.setPrefRowCount(2);

        // Pre-populate domain ratings/comments
        if (eval.getDomain1Rating() != null) d1Rating.setValue(eval.getDomain1Rating());
        if (eval.getDomain2Rating() != null) d2Rating.setValue(eval.getDomain2Rating());
        if (eval.getDomain3Rating() != null) d3Rating.setValue(eval.getDomain3Rating());
        if (eval.getDomain4Rating() != null) d4Rating.setValue(eval.getDomain4Rating());
        d1Comments.setText(eval.getDomain1Comments());
        d2Comments.setText(eval.getDomain2Comments());
        d3Comments.setText(eval.getDomain3Comments());
        d4Comments.setText(eval.getDomain4Comments());
        if (eval.getOverallRating() != null) overallRating.setValue(eval.getOverallRating());

        int row = 0;
        for (int d = 1; d <= 4; d++) {
            grid.add(new Label("Domain " + d + " Score:"), 0, row);
            grid.add(d == 1 ? d1Score : d == 2 ? d2Score : d == 3 ? d3Score : d4Score, 1, row++);
            grid.add(new Label("Domain " + d + " Rating:"), 0, row);
            grid.add(d == 1 ? d1Rating : d == 2 ? d2Rating : d == 3 ? d3Rating : d4Rating, 1, row++);
            grid.add(new Label("Domain " + d + " Comments:"), 0, row);
            grid.add(d == 1 ? d1Comments : d == 2 ? d2Comments : d == 3 ? d3Comments : d4Comments, 1, row++);
        }
        grid.add(new Label("Overall Score:"), 0, row); grid.add(overallScore, 1, row++);
        grid.add(new Label("Overall Rating:"), 0, row); grid.add(overallRating, 1, row++);
        grid.add(new Label("Strengths:"), 0, row); grid.add(strengthsArea, 1, row++);
        grid.add(new Label("Areas for Growth:"), 0, row); grid.add(growthArea, 1, row++);
        grid.add(new Label("Recommendations:"), 0, row); grid.add(recommendationsArea, 1, row++);
        grid.add(new Label("Goals (one/line):"), 0, row); grid.add(goalsArea, 1, row++);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(500);
        dialog.getDialogPane().setContent(scroll);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    // Save domain details first
                    eval.setDomain1Score(d1Score.getValue());
                    eval.setDomain1Rating(d1Rating.getValue());
                    eval.setDomain1Comments(d1Comments.getText());
                    eval.setDomain2Score(d2Score.getValue());
                    eval.setDomain2Rating(d2Rating.getValue());
                    eval.setDomain2Comments(d2Comments.getText());
                    eval.setDomain3Score(d3Score.getValue());
                    eval.setDomain3Rating(d3Rating.getValue());
                    eval.setDomain3Comments(d3Comments.getText());
                    eval.setDomain4Score(d4Score.getValue());
                    eval.setDomain4Rating(d4Rating.getValue());
                    eval.setDomain4Comments(d4Comments.getText());
                    eval.setStrengths(strengthsArea.getText());
                    eval.setAreasForGrowth(growthArea.getText());
                    eval.setRecommendations(recommendationsArea.getText());
                    if (goalsArea.getText() != null && !goalsArea.getText().isBlank()) {
                        eval.setGoals(Arrays.stream(goalsArea.getText().split("\n"))
                                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
                    }
                    eval.setStatus(EvaluationStatus.DRAFT);
                    evaluationService.scoreEvaluation(eval.getId(),
                            d1Score.getValue(), d2Score.getValue(),
                            d3Score.getValue(), d4Score.getValue(),
                            overallScore.getValue(), overallRating.getValue());
                    // Save additional fields
                    evaluationService.updateEvaluation(eval);
                    loadData();
                } catch (Exception e) {
                    log.error("Failed to score evaluation", e);
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to score: " + e.getMessage()));
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void handleEvaluatorSignature(TeacherEvaluation eval) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Sign this evaluation as the evaluator? This will send it to the teacher for review.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Evaluator Signature");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    evaluationService.recordEvaluatorSignature(eval.getId());
                    loadData();
                } catch (Exception e) {
                    log.error("Failed to record evaluator signature", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed: " + e.getMessage());
                }
            }
        });
    }

    private void handleTeacherSignature(TeacherEvaluation eval) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Teacher Signature");
        dialog.setHeaderText("Teacher acknowledges and signs the evaluation");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextArea commentsArea = new TextArea();
        commentsArea.setPromptText("Teacher comments (optional)...");
        commentsArea.setPrefRowCount(4);
        VBox content = new VBox(10, new Label("Teacher Comments:"), commentsArea);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? commentsArea.getText() : null);

        dialog.showAndWait().ifPresent(comments -> {
            try {
                evaluationService.recordTeacherSignature(eval.getId(), comments);
                loadData();
            } catch (Exception e) {
                log.error("Failed to record teacher signature", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed: " + e.getMessage());
            }
        });
    }

    private void handleImprovementPlan(TeacherEvaluation eval) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Improvement Plan Document");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx", "*.doc"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        java.io.File file = fileChooser.showOpenDialog(evalTable.getScene().getWindow());
        if (file != null) {
            try {
                evaluationService.createImprovementPlan(eval.getId(), file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Improvement plan attached.");
                loadData();
            } catch (Exception e) {
                log.error("Failed to attach improvement plan", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed: " + e.getMessage());
            }
        }
    }

    // ========================================================================
    // UTILITIES
    // ========================================================================

    @FXML
    private void handleClearFilters() {
        schoolYearFilter.setValue("All");
        evalStatusFilter.setValue("All");
        evalTypeFilter.setValue("All");
        ratingFilter.setValue("All");
        evaluatorFilter.setValue("All");
        evalSearchField.clear();
    }

    private Optional<String> showNotesDialog(String title, String header, String existingNotes) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextArea notesArea = new TextArea(existingNotes);
        notesArea.setPromptText("Enter notes...");
        notesArea.setPrefRowCount(6);
        content.getChildren().addAll(new Label("Date:"), datePicker, new Label("Notes:"), notesArea);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? notesArea.getText() : null);
        return dialog.showAndWait();
    }

    private ComboBox<Teacher> createTeacherCombo(List<Teacher> teachers) {
        ComboBox<Teacher> combo = new ComboBox<>(FXCollections.observableArrayList(teachers));
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Teacher t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? "" : t.getFullName());
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Teacher t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? "" : t.getFullName());
            }
        });
        return combo;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
